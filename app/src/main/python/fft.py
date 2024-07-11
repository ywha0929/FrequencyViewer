import numpy as np;
import time;

def fft(offset, threshold) :
    # print("start")
    targetlen = 441
    # xC = np.fromfile('/storage/emulated/0/FrequencyViewer/buffer.C','h')


    # while(len(xC) < offset + targetlen*2) :
    #     xC = np.fromfile('/storage/emulated/0/FrequencyViewer/buffer.C','h')
    #
    # left = np.empty(targetlen,dtype=np.short)
    # right = np.empty(targetlen,dtype=np.short)
    # countL = 0;
    # countR = 0;
    # for i in range(targetlen*2) :
    #     if i%2 == 0 :
    #         right[countL] = xC[offset+i];
    #         countL +=1
    #     else :
    #         left[countR] = xC[offset+i]
    #         countR +=1
    first = time.time_ns()
    left = np.fromfile('/storage/emulated/0/FrequencyViewer/buffer.L','h')
    right = np.fromfile('/storage/emulated/0/FrequencyViewer/buffer.R','h')
    while(len(left) < offset + targetlen) :
        left = np.fromfile('/storage/emulated/0/FrequencyViewer/buffer.L','h')
        right = np.fromfile('/storage/emulated/0/FrequencyViewer/buffer.R','h')
    second = time.time_ns()


    # while len(left) > offset + targetlen :
    interval = targetlen
    xLzp = np.zeros(44100);
    xRzp = np.zeros(44100);
    # xLzp[0:interval] = left;
    # xRzp[0:interval] = right;
    xLzp[0:interval] = left[offset:offset+targetlen];
    xRzp[0:interval] = right[offset:offset+targetlen];
    third = time.time_ns()
    XLk = np.fft.fft(xLzp,44100);
    XRk = np.fft.fft(xRzp,44100);
    magXLk = np.abs(XLk);
    magXRk = np.abs(XRk);
    fourth = time.time_ns()
    print("file read : " , (second-first)/1000000)
    print("zero padding : " , (third-second)/1000000)
    print("fft : " , (fourth-third)/1000000)
    # result = np.asarray(np.where(magXLk[17000:22100]>threshold))+17000;
    # offset+=targetlen
    # print(targetlen,".");

    # print("left : ",findPeekFrequencies(magXLk,threshold));

    # result = np.asarray(np.where(magXRk[17000:22100]>threshold))+17000;

    # print("right : ",findPeekFrequencies(magXRk,threshold));

        # max_index = np.argmax(magXLk[:22100])
        # print("max : " , max_index);
        # print("amplitude : " ,magXLk[max_index])

def fftDirect(left,right, threshold) :
    # print("start")

    first = time.time_ns()
    targetlen = len(left)
    # xC = np.fromfile('/storage/emulated/0/FrequencyViewer/buffer.C','h')


    # while(len(xC) < offset + targetlen*2) :
    #     xC = np.fromfile('/storage/emulated/0/FrequencyViewer/buffer.C','h')
    #
    # left = np.empty(targetlen,dtype=np.short)
    # right = np.empty(targetlen,dtype=np.short)
    # countL = 0;
    # countR = 0;
    # for i in range(targetlen*2) :
    #     if i%2 == 0 :
    #         right[countL] = xC[offset+i];
    #         countL +=1
    #     else :
    #         left[countR] = xC[offset+i]
    #         countR +=1




    # while len(left) > offset + targetlen :
    interval = targetlen
    xLzp = np.zeros(44100);
    xRzp = np.zeros(44100);
    # xLzp[0:interval] = left;
    # xRzp[0:interval] = right;
    xLzp[0:interval] = left[0:0+targetlen];
    xRzp[0:interval] = right[0:0+targetlen];
    second = time.time_ns()
    XLk = np.fft.fft(xLzp,44100);
    XRk = np.fft.fft(xRzp,44100);
    magXLk = np.abs(XLk);
    magXRk = np.abs(XRk);
    third = time.time_ns()
    # print("zero padding : " , (second-first)/1000000)
    # print("fft : " , (third-second)/1000000)
    # result = np.asarray(np.where(magXLk[17000:22100]>threshold))+17000;
    # offset+=targetlen
    # print(targetlen,".");

    print("left : ",findPeekFrequencies(magXLk,threshold));

    # result = np.asarray(np.where(magXRk[17000:22100]>threshold))+17000;

    print("right : ",findPeekFrequencies(magXRk,threshold));

    # max_index = np.argmax(magXLk[:22100])
    # print("max : " , max_index);
    # print("amplitude : " ,magXLk[max_index])



def findPeekFrequencies(arr,threshold) :
    result = []
    for i in range(17000,22000,200) :
        array = arr[i-100:i+100]
        arrayOverThreshold = array[array>threshold]
        if(len(arrayOverThreshold) != 0) :
            result.append(np.where(array==np.max(arrayOverThreshold))[0][0]+i-100)
    return result;
