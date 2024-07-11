package com.example.frequencyviewer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Recorder {
    static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC; // for raw audio, use MediaRecorder.AudioSource.UNPROCESSED, see note in MediaRecorder section
    static final int SAMPLE_RATE = 44100;
    static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    static final int BUFFER_SIZE_RECORDING = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    private final String TAG = "Recorder";
    protected AudioRecord audioRecord;
    private int offset=0;



    private boolean continueRecording = false;
    public boolean getContinueRecording() {
        return continueRecording;
    }

    public void setContinueRecording(boolean continueRecording) {
        this.continueRecording = continueRecording;
        if(continueRecording == false) {
            audioRecord.stop();
        }
    }
    public int startRecording(Context context) {

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "startRecording: inside checkSelfPermission");
            return -1;
        }
        audioRecord = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE_RECORDING);

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) { // check for proper initialization
            Log.e(TAG, "error initializing ");
            return -1;
        }

        audioRecord.startRecording();
        return 0;

    }
    public void processData(Queue<Short> store) throws InterruptedException {
        byte[] data = new byte[BUFFER_SIZE_RECORDING];

        while(continueRecording) {
            int read = audioRecord.read(data, 0, BUFFER_SIZE_RECORDING);
            short[] shortData = new short[read/2];
            ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortData);
            for(int i = 0; i< read/2; i++) {
//                Log.d(TAG, "processData: " + data[2*i]* 256 + data[2*i+1]);
//                store.add((short) (data[2*i]* 256 + data[2*i+1]));
                store.add(shortData[i]);
            }
//            while(MainActivity.dataUsageCheck == true) {
//                Thread.sleep(1);
//            }
//            MainActivity.dataUsageCheck = true;
//            store.addAll(toAdd);
//            MainActivity.dataUsageCheck = false;

//                outputStreamL.write(dataL,0,read/2);
//                outputStreamR.write(dataR,0,read/2);


            Log.d(TAG, "writeAudioData: " + "Recording..."+offset);
            offset += read;
//                offset += dataL.length;


        }
    }

//    public void processAudioData() {
//        byte[] data = new byte[BUFFER_SIZE_RECORDING];
//        while(continueRecording) {
//            int read = audioRecord.read(data, 0, BUFFER_SIZE_RECORDING);
//            short[] dataL = new short[read];
//            short[] dataR = new short[read];
//            int count=0;
//            for(int i = 0; i < read/4; i = i+4)
//            {
//                dataL[count] = (short) (data[2*i] + data[2*i+1]);
//                dataR[count] = (short) (data[2*i+3] + data[2*i+4]);
//                count++;
//            }
//            Python python = Python.getInstance();
//            PyObject sys = python.getModule("sys");
//            PyObject io = python.getModule("io");
//            PyObject textOutputStream = io.callAttr("StringIO");
//            sys.put("stdout", textOutputStream);
//            Log.d(TAG, "python: ");
//            PyObject FFT = python.getModule("fft").callAttr("fft",offset,threshold);
//            String output = textOutputStream.callAttr("getvalue").toString();
//            offset = Integer.parseInt(  output.substring(0,output.indexOf('.')-1)  );
//            tvFrequency.post(new Runnable() {
//                @Override
//                public void run() {
//                    tvFrequency.setText(output.substring(0,output.indexOf(']')+2));
//                }
//            });
//            tvFrequency2.post(new Runnable() {
//                @Override
//                public void run() {
//                    tvFrequency2.setText(output.substring(output.indexOf(']')+2));
//                }
//            });
//
////            while(MainActivity.dataUsageCheck == true) {
////                Thread.sleep(1);
////            }
////            MainActivity.dataUsageCheck = true;
////            store.addAll(toAdd);
////            MainActivity.dataUsageCheck = false;
//
////                outputStreamL.write(dataL,0,read/2);
////                outputStreamR.write(dataR,0,read/2);
//
//
//
//
//
//        }
//    }

    public void writeAudioData(String fileName) { // to be called in a Runnable for a Thread created after call to startRecording()
        Log.d(TAG, "writeAudioData: " + BUFFER_SIZE_RECORDING);
        byte[] data = new byte[BUFFER_SIZE_RECORDING]; // assign size so that bytes are read in in chunks inferior to AudioRecord internal buffer size

        FileOutputStream outputStreamL = null;
        FileOutputStream outputStreamR = null;
//        FileOutputStream outputStreamC = null;

        try {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() +"/FrequencyViewer";
            File dir = new File(path);
            if(!dir.exists()) {
                dir.mkdir();
            }
            path = path +"/"+ fileName;
            File file1 = new File(path+".L");
            File file2 = new File(path + ".R");
            File file3 = new File(path+".C");
            if(!file1.exists()) {
                file1.createNewFile();
            }
            else {
                file1.delete();
                file1.createNewFile();
            }
            if(!file2.exists()) {
                file2.createNewFile();
            }
            else {
                file2.delete();
                file2.createNewFile();
            }
//            if(!file3.exists()) {
//                file3.createNewFile();
//            }
//            else {
//                file3.delete();
//                file3.createNewFile();
//            }
            Log.d(TAG, "writeAudioData: path - " + path);
            outputStreamL = new FileOutputStream(path+".L",true); //fileName is path to a file, where audio data should be written
            outputStreamR = new FileOutputStream(path+".R",true);
//            outputStreamC = new FileOutputStream(path+".C");
        } catch (FileNotFoundException e) {
            Log.d(TAG, "writeAudioData: " + "no such file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int offset = 0;
        while (continueRecording) { // continueRecording can be toggled by a button press, handled by the main (UI) thread


            int read = audioRecord.read(data, 0, BUFFER_SIZE_RECORDING);
            try {
                byte[] dataL = new byte[read];
                byte[] dataR = new byte[read];
                for(int i = 0; i < read/2; i = i + 2)
                {
                    dataL[i] = data[2*i];
                    dataL[i+1] = data[2*i+1];
                    dataR[i] =  data[2*i+2];
                    dataR[i+1] = data[2*i+3];
                }

                outputStreamL.write(dataL,0,read/2);
                outputStreamR.write(dataR,0,read/2);
//                outputStreamC.write(data,0,read);
//                outputStreamC.flush();
                offset += read;
                Log.d(TAG, "writeAudioData: " + "Recording..."+offset);

//                offset += dataL.length;
            }
            catch (IOException e) {
                Log.d(TAG, "exception while writing to file");
                e.printStackTrace();
            }
        }

        try {
            outputStreamL.flush();
            outputStreamL.close();
            outputStreamR.flush();
            outputStreamR.close();
//            outputStreamC.flush();
//            outputStreamC.close();
        }
        catch (IOException e) {
            Log.d(TAG, "exception while closing output stream " + e.toString());
            e.printStackTrace();
        }

        // Clean up
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;

    }
}
