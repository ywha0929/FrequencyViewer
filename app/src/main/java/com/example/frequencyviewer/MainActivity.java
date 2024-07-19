package com.example.frequencyviewer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import org.jtransforms.fft.DoubleFFT_1D;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "FrequencyViewer MainActivity";
    AudioTrack mAudioTrack;
    int samplingRate = 44100;
    boolean isPlay;
    Thread thread;
    Thread converter;
    TextView tvThreshold;
    Button btnStart;
    Button btnStop;
    TextView tvFrequency;
    TextView tvFrequency2;
    SeekBar Threshold;
    static boolean dataUsageCheck = false;
    LinkedBlockingQueue<Short> data = new LinkedBlockingQueue<>();
//    byte[] data2 = new byte[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(getApplicationContext()));
        }
        Recorder recorder = new Recorder();
        tvThreshold = findViewById(R.id.textThreshold);
        tvFrequency = findViewById(R.id.textFrequency);
        tvFrequency2 = findViewById(R.id.textFrequency2);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        Threshold = findViewById(R.id.seekBar);
        Threshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvThreshold.setText(""+progress*5000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPlay = true;
                int ret = recorder.startRecording(getApplicationContext());
                recorder.setContinueRecording(true);
                if(ret ==0 ){
                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                recorder.processData(data);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

//                            recorder.writeAudioData("buffer");


                        }
                    });
                    int blockSize = samplingRate/10;
                    double y[] = new double[blockSize];
                    for (int i = 0; i < blockSize; i++) {
                        y[i] = 0;
                    }

                    converter = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            long offset = 0;
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            while(isPlay) {
                                Log.d(TAG, "run: start loop");
                                int threshold = Integer.parseInt(tvThreshold.getText().toString());
                                Python python = Python.getInstance();

                                PyObject sys = python.getModule("sys");
                                PyObject io = python.getModule("io");
                                PyObject textOutputStream = io.callAttr("StringIO");
                                sys.put("stdout", textOutputStream);

                                int interval = (Recorder.SAMPLE_RATE/10) * 2;
                                short[] left = new short[interval];
                                short[] right = new short[interval];

                                for(int i = 0; i< interval; i++) {
                                    try {
                                        left[i] = data.take();
                                        right[i] = data.take();
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
//                                    Short thisData = data.poll();
//                                    while(thisData == null){
//                                        thisData = data.poll();
//                                    }
//                                    left[i] = thisData;
//                                    thisData = data.poll();
//                                    while(thisData == null) {
//                                        thisData = data.poll();
//                                    }
//                                    right[i] = thisData;

                                }
//                                if(data.size() < 8820) {
//                                    continue;
//                                }
//                                while(dataUsageCheck == true) {
//                                    try {
//                                        Thread.sleep(1);
//                                    } catch (InterruptedException e) {
//                                        throw new RuntimeException(e);
//                                    }
//                                }

//                                List<Short> left = new ArrayList<>();
//                                List<Short> right = new ArrayList<>();
//                                for(int i = 0; i< 8820; i++) {
//                                    switch(i%2) {
//                                        case 0: left.add(data.get(0));
//                                            data.remove(0);
//                                            break;
//                                        case 1: right.add(data.get(0));
//                                            data.remove(0);
//                                            break;
//                                    }
//                                }

                                Log.d(TAG, "python: ");
                                PyObject FFT = python.getModule("fft").callAttr("fftDirect",left,right,threshold);
//                                left = null;
//                                right = null;


                                String output = textOutputStream.callAttr("getvalue").toString();
//                                offset = Integer.parseInt(  output.substring(0,output.indexOf('.')-1)  );
                                tvFrequency.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvFrequency.setText(output.substring(0,output.indexOf(']')+2));
                                    }
                                });
                                tvFrequency2.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvFrequency2.setText(output.substring(output.indexOf(']')+2));
                                    }
                                });
//                                offset += 8820;
//                                while(left.size() <blockSize || right.size() < blockSize) {
//                                    try {
//                                        Thread.sleep(100);
//                                    } catch (InterruptedException e) {
//                                        throw new RuntimeException(e);
//                                    }
//                                }
//
//                                List<Integer> fLeft = new ArrayList<>();
//                                List<Integer> fRight = new ArrayList<>();
//                                for (int i = 0; i < blockSize; i++) {
//                                    dLeft[i] = (double) left.get(i);
//                                    left.remove(i);
//                                    dRight[i] = (double) right.get(i);
//                                    right.remove(i);
//                                }
//                                for (int k = 0; k < blockSize; k++) {
//                                    summary[2 * k] = dLeft[k]; //실수부
//                                    summary[2 * k + 1] = y[k]; //허수부 0으로 채워넣음.
//                                }
//                                for (int k = blockSize; k < 44100; k++) {
//                                    summary[2 * k] = 0;
//                                    summary[2 * k + 1] = 0;
//                                }
//
//                                DoubleFFT_1D fft = new DoubleFFT_1D(44100);
//                                fft.complexForward(summary);
//                                for (int k = 0; k < blockSize / 2; k++) {
//                                    mag[k] = (float)Math.sqrt(Math.pow(summary[2 * k], 2) + Math.pow(summary[2 * k + 1], 2));
//                                }
//                                int threshold = Integer.parseInt(tvThreshold.getText().toString());
//                                for (int i = 0; i < 44100; i++) {
//                                    if (mag[i] > threshold)
//                                        fLeft.add(i);
//                                }
//
//
//                                for (int k = 0; k < blockSize; k++) {
//                                    summary[2 * k] = dRight[k]; //실수부
//                                    summary[2 * k + 1] = y[k]; //허수부 0으로 채워넣음.
//                                }
//                                for (int k = blockSize; k < 44100; k++) {
//                                    summary[2 * k] = 0;
//                                    summary[2 * k + 1] = 0;
//                                }
//                                fft.complexForward(summary);
//                                for (int k = 0; k < blockSize / 2; k++) {
//                                    mag[k] = Math.sqrt(Math.pow(summary[2 * k], 2) + Math.pow(summary[2 * k + 1], 2));
//                                }
//                                for (int i = 0; i < 44100; i++) {
//                                    if (mag[i] > threshold)
//                                        fRight.add(i);
//                                }
//                                tvFrequency.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        String frequencies = "Left : \n";
//                                        for (int f : fLeft) {
//                                            frequencies += "" + f + "Hz\n";
//                                        }
//                                        frequencies += "Right : \n";
//                                        for (int f : fRight) {
//                                            frequencies += "" + f + "Hz\n";
//                                        }
//                                        tvFrequency.setText(frequencies);
//                                    }
//                                });
//                                fft = null;

                            }

                        }

                    });

                    thread.start();
                    converter.start();
                }
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recorder.setContinueRecording(false);
                isPlay = false;
                try {
                    thread.join();
                    converter.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        });
    }

    private void checkPermission() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                (ContextCompat.checkSelfPermission(this, android.Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {

        } else {
            requestPermission();
        }
    }
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 30){
            if (!Environment.isExternalStorageManager()){
                Intent getpermission = new Intent();
                getpermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(getpermission);
            }
        }
        String[] permissions = new String[] {android.Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
        ActivityCompat.requestPermissions(this,permissions,1);
    }
}