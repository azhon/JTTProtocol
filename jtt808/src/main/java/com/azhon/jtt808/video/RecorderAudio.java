package com.azhon.jtt808.video;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.audio
 * 文件名:    RecorderAudio
 * 创建时间:  2020/3/2 on 21:16
 * 描述:     TODO AudioRecord 录制pcm转G711A
 * 博客地址：https://www.jianshu.com/p/33cba1a821d0
 * GitHub地址：https://github.com/liulixu/AudioRecord
 *
 * @author 阿钟
 */

public class RecorderAudio {

    private static final String TAG = "AudioStream";
    //采样率
    private static int SAMPLE_RATE_HZ = 8000;
    private AudioRecord audioRecord;
    private boolean isRecording;
    private int channelNum;
    private RecorderListener recordListener;


    public RecorderAudio(int channelNum, RecorderListener recordListener) {
        this.channelNum = channelNum;
        this.recordListener = recordListener;

    }

    /**
     * 编码
     */
    public void start() {
        isRecording = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                recordAudio();
            }
        }).start();
    }

    /**
     * 录制音频并转码G711A
     */
    private void recordAudio() {
        try {
            int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_HZ, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_HZ,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);

            int readBufferSize = minBufferSize;
            //每次读取800个字节
            if (readBufferSize > 800) {
                readBufferSize = 800;
            }
            Log.e(TAG, "readBufferSize：" + readBufferSize);
            short[] inG711Buffer = new short[readBufferSize];
            byte[] outG711Buffer = new byte[readBufferSize];

            audioRecord.startRecording();

            while (isRecording) {
                int read = audioRecord.read(inG711Buffer, 0, inG711Buffer.length);
                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    //调用G711A编码
                    G711Code.G711aEncoder(inG711Buffer, outG711Buffer, read);
                    if (recordListener != null) {
                        recordListener.audioData(outG711Buffer, channelNum);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * 停止录音
     */
    public void stop() {
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    public interface RecorderListener {
        void audioData(byte[] data, int channelNum);
    }
}

