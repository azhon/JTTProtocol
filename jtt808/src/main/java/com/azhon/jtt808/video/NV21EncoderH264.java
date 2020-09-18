package com.azhon.jtt808.video;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt
 * 文件名:    NV21ToH264Encoder
 * 创建时间:  2020/2/26 on 15:06
 * 描述:     TODO 将摄像头采集到的视频帧编码为H264
 *
 * @author 阿钟
 */

public class NV21EncoderH264 {
    private int width, height;
    private int frameRate;
    private boolean rotate90;
    private MediaCodec mediaCodec;

    public NV21EncoderH264(int width, int height, int frameRate, boolean rotate90) {
        this.width = width;
        this.height = height;
        this.frameRate = frameRate;
        this.rotate90 = rotate90;
        initMediaCodec();
    }

    private void initMediaCodec() {
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
            //TODO 旋转90度 需要将宽高对调
            MediaFormat mediaFormat;
            if (rotate90) {
                mediaFormat = MediaFormat.createVideoFormat("video/avc", height, width);
            } else {
                mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
            }
            //描述平均位速率（以位/秒为单位）的键。 关联的值是一个整数
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 5);
            //描述视频格式的帧速率（以帧/秒为单位）的键。帧率，一般在15至30之内，太小容易造成视频卡顿。
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
            //色彩格式，具体查看相关API，不同设备支持的色彩格式不尽相同
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
            //关键帧间隔时间，单位是秒
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            //开始编码
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将NV21编码成H264
     */
    public void encoderH264(byte[] data, int channelNum, EncoderListener listener) {
        //将NV21编码成NV12
        byte[] nv12 = NV21ToNV12(data, width, height);
        //视频顺时针旋转90度
        if (rotate90) {
            nv12 = rotateNV290(nv12, width, height);
        }
        try {
            //拿到输入缓冲区,用于传送数据进行编码
            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
            //拿到输出缓冲区,用于取到编码后的数据
            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
            //当输入缓冲区有效时,就是>=0
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                //往输入缓冲区写入数据
                inputBuffer.put(nv12);
                //五个参数，第一个是输入缓冲区的索引，第二个数据是输入缓冲区起始索引，第三个是放入的数据大小，第四个是时间戳，保证递增就是
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, nv12.length, System.nanoTime() / 1000, 0);
            }
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            //拿到输出缓冲区的索引
            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            while (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);

                byte[] NALUB = findNALU(0, outData);
                byte NALU = NALUB[0];
                byte offset = NALUB[1];
                if ((NALU & 0x1F) == 7) {
                    byte[][] bytes = unPackPkg(outData, offset);
                    for (byte[] aByte : bytes) {
                        listener.h264(aByte, channelNum);
                    }
                } else {
                    //outData就是输出的h264数据
                    listener.h264(outData, channelNum);
                }
                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private byte[] NV21ToNV12(byte[] nv21, int width, int height) {
        byte[] nv12 = new byte[width * height * 3 / 2];
        int frameSize = width * height;
        int i, j;
        System.arraycopy(nv21, 0, nv12, 0, frameSize);
        for (i = 0; i < frameSize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < frameSize / 2; j += 2) {
            nv12[frameSize + j - 1] = nv21[j + frameSize];
        }
        for (j = 0; j < frameSize / 2; j += 2) {
            nv12[frameSize + j] = nv21[j + frameSize - 1];
        }
        return nv12;
    }

    /**
     * 此处为顺时针旋转旋转90度
     *
     * @param data        旋转前的数据
     * @param imageWidth  旋转前数据的宽
     * @param imageHeight 旋转前数据的高
     * @return 旋转后的数据
     */
    private byte[] rotateNV290(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
        return yuv;
    }

    /**
     * 拆包 sps pps
     *
     * @param data
     * @return
     */
    private byte[][] unPackPkg(byte[] data, byte offset) {
        byte[] NALUB = findNALU(offset, data);
        byte NALU = NALUB[0];
        offset = NALUB[1];
        if ((NALU & 0x1F) == 8) {
            byte[] sps = Arrays.copyOfRange(data, 0, offset - 4);
            byte[] pps = Arrays.copyOfRange(data, offset - 4, data.length);
            return new byte[][]{sps, pps};
        }
        return new byte[][]{data};
    }

    /**
     * 找H264包头
     */
    public static byte[] findNALU(int offset, byte[] data) {
        // 00 00 00 01 x
        for (int i = offset; i < data.length - 4; i++) {
            if (data[i] == 0x00 && data[i + 1] == 0x00
                    && data[i + 2] == 0x00 && data[i + 3] == 0x01) {
                return new byte[]{data[i + 4], (byte) (i + 4)};
            }
            // 00 00 01 x
            if (data[i] == 0x00 && data[i + 1] == 0x00
                    && data[i + 2] == 0x01) {
                return new byte[]{data[i + 3], (byte) (i + 3)};
            }
        }
        return new byte[2];
    }

    public interface EncoderListener {
        void h264(byte[] data, int channelNum);
    }

}
