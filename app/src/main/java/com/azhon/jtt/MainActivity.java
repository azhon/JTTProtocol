package com.azhon.jtt;

import android.Manifest;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.azhon.jtt808.JTT808Manager;
import com.azhon.jtt808.bean.JTT808Bean;
import com.azhon.jtt808.bean.TerminalParamsBean;
import com.azhon.jtt808.listener.OnConnectionListener;
import com.azhon.jtt808.netty.live.LiveClient;
import com.azhon.jtt808.util.CameraUtil;
import com.azhon.jtt808.video.NV21EncoderH264;
import com.azhon.jtt808.video.RecorderAudio;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements OnConnectionListener, View.OnClickListener, SurfaceHolder.Callback, NV21EncoderH264.EncoderListener, RecorderAudio.RecorderListener {

    private static final String TAG = "MainActivity";

    //制造商ID
    public static String MANUFACTURER_ID;
    //终端型号
    public static String TERMINAL_MODEL;
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
    private LocationThread locationThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("部标JTT808,JTT1078,渝标协议封装");
        initView();
        checkPermission();
    }

    private void initView() {
        spinner = findViewById(R.id.spinner);
        findViewById(R.id.btn_location).setOnClickListener(this);
        findViewById(R.id.btn_cy_alarm).setOnClickListener(this);
        findViewById(R.id.btn_call_alarm).setOnClickListener(this);
        findViewById(R.id.btn_zsqf_alarm).setOnClickListener(this);
        findViewById(R.id.btn_pljs_alarm).setOnClickListener(this);
        findViewById(R.id.btn_wzjsw_alarm).setOnClickListener(this);
        ToggleButton toggleButton = findViewById(R.id.btn_connect);
        SeekBar seekBar = findViewById(R.id.sb);
        //初始化SurfaceView
        SurfaceView surfaceView = findViewById(R.id.sfv);
        holder = surfaceView.getHolder();
        holder.addCallback(this);

        int heightPixels = getResources().getDisplayMetrics().heightPixels;
        ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
        layoutParams.height = heightPixels;
        surfaceView.setLayoutParams(layoutParams);
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                connectService();
            } else {
                disconnectService();
            }
        });
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
    }

    private void checkPermission() {
        PermissionX.init(this).permissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO).request(new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                if (allGranted) {
                    initCamera();
                } else {
                    Toast.makeText(MainActivity.this, "请允许所有权限", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
            case R.id.btn_location:
                if (manager == null) return;
                manager.uploadLocation(Constants.LAT, Constants.LNG);
                break;
            case R.id.btn_cy_alarm:
                if (manager == null) return;
                files.add(new File(getExternalCacheDir() + "/2.png"));
                files.add(new File(getExternalCacheDir() + "/3.png"));
                manager.uploadAlarmInfoYB(Constants.LAT, Constants.LNG, 1, 1, 0, files);
                break;
            case R.id.btn_call_alarm:
                if (manager == null) return;
                manager.uploadAlarmInfoYB(Constants.LAT, Constants.LNG, 2, 1, 0, files);
                break;
            case R.id.btn_zsqf_alarm:
                if (manager == null) return;
                manager.uploadAlarmInfoYB(Constants.LAT, Constants.LNG, 3, 1, 0, files);
                break;
            case R.id.btn_pljs_alarm:
                if (manager == null) return;
                manager.uploadAlarmInfoYB(Constants.LAT, Constants.LNG, 4, 1, DEGREE, files);
                break;
            case R.id.btn_wzjsw_alarm:
                if (manager == null) return;
                manager.uploadAlarmInfoYB(Constants.LAT, Constants.LNG, 5, 1, 0, files);
                break;
            default:
                break;
        }
    }

    private void connectService() {
        if (manager != null) {
            return;
        }
        String phone = SharePreUtil.getString(this, "PHONE", Constants.PHONE);
        String ip = SharePreUtil.getString(this, "IP", Constants.IP);
        int port = SharePreUtil.getInt(this, "PORT", Constants.PORT);
        String terminalId = SharePreUtil.getString(this, "TERMINAL_ID", Constants.TERMINAL_ID);
        MANUFACTURER_ID = SharePreUtil.getString(this, "MANUFACTURER_ID", Constants.MANUFACTURER_ID);
        TERMINAL_MODEL = SharePreUtil.getString(this, "TERMINAL_MODEL", Constants.TERMINAL_MODEL);

        manager = JTT808Manager.getInstance();
        manager.setOnConnectionListener(this).init(phone, terminalId, ip, port);
        locationThread = new LocationThread(manager);
        locationThread.start();
    }

    private void disconnectService() {
        manager.disconnect();
        stopLive();
        manager = null;
        locationThread.release();
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
