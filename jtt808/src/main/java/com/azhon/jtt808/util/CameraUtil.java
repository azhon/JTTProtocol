package com.azhon.jtt808.util;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.azhon.jtt808.video.NV21EncoderH264;

import java.io.IOException;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.video
 * 文件名:    CameraUtil
 * 创建时间:  2020/2/26 on 17:39
 * 描述:     TODO
 *
 * @author 阿钟
 */

public class CameraUtil {
    private Camera camera;
    private final int channelNum;
    //是否需要旋转90度
    private boolean rotate90 = true;
    private final SurfaceHolder holder;
    private NV21EncoderH264 h264Encoder;
    private final NV21EncoderH264.EncoderListener listener;

    public CameraUtil(int width, int height, SurfaceHolder holder, int channelNum, NV21EncoderH264.EncoderListener listener) {
        this.holder = holder;
        this.channelNum = channelNum;
        this.listener = listener;
        init(width, height);
    }

    private void init(int width, int height) {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                h264Encoder.encoderH264(data, channelNum, listener);
            }
        });
        Camera.Parameters parameters = camera.getParameters();
        h264Encoder = new NV21EncoderH264(width, height, 30, rotate90);

        //设置预览格式
        parameters.setPreviewFormat(ImageFormat.NV21);
        //设置预览图像分辨率
        parameters.setPreviewSize(width, height);
        camera.setDisplayOrientation(90);
        //配置camera参数
        camera.setParameters(parameters);
        //没有surface的话，相机不会开启preview预览
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //调用startPreview()用以更新preview的surface，必须要在拍照之前start Preview
        camera.startPreview();
    }


    public void release() {
        if (null != camera) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
