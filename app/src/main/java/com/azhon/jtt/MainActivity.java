package com.azhon.jtt;

import android.Manifest;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.azhon.jtt808.JTT808Manager;
import com.azhon.jtt808.bean.JTT808Bean;
import com.azhon.jtt808.bean.TerminalParamsBean;
import com.azhon.jtt808.listener.OnConnectionListener;
import com.azhon.jtt808.netty.live.LiveClient;
import com.azhon.jtt808.util.CameraUtil;
import com.azhon.jtt808.video.NV21EncoderH264;
import com.azhon.jtt808.video.RecorderAudio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements OnConnectionListener,
        View.OnClickListener, SurfaceHolder.Callback, NV21EncoderH264.EncoderListener, RecorderAudio.RecorderListener {

    private static final String TAG = "MainActivity";

    public static String IP;
    public static Integer PORT;
    //终端手机号
    public static String PHONE;
    //制造商ID
    public static String MANUFACTURER_ID;
    //终端型号
    public static String TERMINAL_MODEL;
    //终端ID
    public static String TERMINAL_ID;
    //经纬度
    public static long LAT = 31228068;
    public static long LNG = 121481323;
    private static int DEGREE = 1;
    //视频宽高
    private static int WIDTH;
    private static int HEIGHT;

    private JTT808Manager manager;
    private SurfaceHolder holder;
    //实时监控
    private LiveClient liveClient;
    private CameraUtil cameraUtil;
    private RecorderAudio recorderAudio;
    private Spinner spinner;
    private List<Camera.Size> sizeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("部标JTT808,JTT1078,渝标协议封装");
        initView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 0x23);
        }
        initCamera();
    }

    private void initCamera() {
        Camera camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        Camera.Parameters parameters = camera.getParameters();
        sizeList = parameters.getSupportedPreviewSizes();
        List<String> items = new ArrayList<>();
        for (Camera.Size size : sizeList) {
            items.add(size.width + "x" + size.height);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                WIDTH = sizeList.get(position).width;
                HEIGHT = sizeList.get(position).height;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        camera.release();
    }

    private void init() {
        IP = SharePreUtil.getString(this, "IP", Constants.IP);
        PORT = SharePreUtil.getInt(this, "PORT", Constants.PORT);
        PHONE = SharePreUtil.getString(this, "PHONE", Constants.PHONE);
        MANUFACTURER_ID = SharePreUtil.getString(this, "MANUFACTURER_ID", Constants.MANUFACTURER_ID);
        TERMINAL_MODEL = SharePreUtil.getString(this, "TERMINAL_MODEL", Constants.TERMINAL_MODEL);
        TERMINAL_ID = SharePreUtil.getString(this, "TERMINAL_ID", Constants.TERMINAL_ID);

        manager = JTT808Manager.getInstance();
        manager.setOnConnectionListener(this).init(PHONE, TERMINAL_ID, IP, PORT);
    }

    private void initView() {
        spinner = findViewById(R.id.spinner);
        findViewById(R.id.btn_connect).setOnClickListener(this);
        findViewById(R.id.btn_location).setOnClickListener(this);
        findViewById(R.id.btn_cy_alarm).setOnClickListener(this);
        findViewById(R.id.btn_call_alarm).setOnClickListener(this);
        findViewById(R.id.btn_zsqf_alarm).setOnClickListener(this);
        findViewById(R.id.btn_pljs_alarm).setOnClickListener(this);
        findViewById(R.id.btn_wzjsw_alarm).setOnClickListener(this);
        SeekBar seekBar = findViewById(R.id.sb);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DEGREE = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //初始化SurfaceView
        SurfaceView surfaceView = findViewById(R.id.sfv);
        holder = surfaceView.getHolder();
        holder.addCallback(this);
    }


    @Override
    public void onConnectionSateChange(int state) {
        switch (state) {
            case OnConnectionListener.CONNECTED:
                manager.register(MANUFACTURER_ID, TERMINAL_MODEL);
                break;
            case OnConnectionListener.DIS_CONNECT:
                Log.d(TAG, "断开连接");
                break;
            case OnConnectionListener.RE_CONNECT:
                Log.d(TAG, "重连");
                break;
            default:
                break;
        }
    }

    @Override
    public void receiveData(JTT808Bean jtt808Bean) {

    }

    @Override
    public void terminalParams(List<TerminalParamsBean> params) {
        for (TerminalParamsBean param : params) {
            int id = param.getId();
            if (Integer.class.equals(param.getClz())) {
                int value = (int) param.getValue();
            } else if (String.class.equals(param.getClz())) {
                String value = (String) param.getValue();
            } else if (Byte.class.equals(param.getClz())) {
                Byte value = (Byte) param.getValue();
            }
            switch (id) {
                //最高速度，单位为公里每小时(km/h)
                case 0x0055:
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void audioVideoLive(String ip, int port, int channelNum, int dataType) {
        if (liveClient != null) return;
        liveClient = new LiveClient(ip, port);
        startLive(channelNum);
    }

    @Override
    public void audioVideoLiveControl(int channelNum, int control, int closeAudio, int switchStream) {
        if (control == 0 || control == 2) {
            stopLive();
        } else if (control == 3) {
            startLive(channelNum);
        }
    }

    /**
     * 开始实时视频
     */
    private void startLive(int channelNum) {
        if (liveClient == null) return;
        cameraUtil = new CameraUtil(WIDTH, HEIGHT, holder, channelNum, MainActivity.this);
        recorderAudio = new RecorderAudio(channelNum, MainActivity.this);
        recorderAudio.start();
    }

    /**
     * 停止实时视频
     */
    private void stopLive() {
        if (liveClient != null) {
            liveClient.release();
            liveClient = null;
        }
        if (cameraUtil != null) {
            cameraUtil.release();
            cameraUtil = null;
        }
        if (recorderAudio != null) {
            recorderAudio.stop();
        }
    }

    @Override
    public void h264(byte[] data, int channelNum) {
        manager.videoLive(data, channelNum, liveClient);
    }

    @Override
    public void audioData(byte[] data, int channelNum) {
        manager.audioLive(data, channelNum, liveClient);
    }

    @Override
    public void onClick(View v) {
        //附件列表
        List<File> files = new ArrayList<>();
        switch (v.getId()) {
            case R.id.btn_connect:
                init();
                break;
            case R.id.btn_location:
                if (manager == null) return;
                manager.uploadLocation(LAT, LNG);
                break;
            case R.id.btn_cy_alarm:
                if (manager == null) return;
                files.add(new File(getExternalCacheDir() + "/2.png"));
                files.add(new File(getExternalCacheDir() + "/3.png"));
                manager.uploadAlarmInfoYB(LAT, LNG, 1, 1, 0, files);
                break;
            case R.id.btn_call_alarm:
                if (manager == null) return;
                manager.uploadAlarmInfoYB(LAT, LNG, 2, 1, 0, files);
                break;
            case R.id.btn_zsqf_alarm:
                if (manager == null) return;
                manager.uploadAlarmInfoYB(LAT, LNG, 3, 1, 0, files);
                break;
            case R.id.btn_pljs_alarm:
                if (manager == null) return;
                manager.uploadAlarmInfoYB(LAT, LNG, 4, 1, DEGREE, files);
                break;
            case R.id.btn_wzjsw_alarm:
                if (manager == null) return;
                manager.uploadAlarmInfoYB(LAT, LNG, 5, 1, 0, files);
                break;
            default:
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(this, SettingActivity.class));
        return super.onOptionsItemSelected(item);
    }
}
