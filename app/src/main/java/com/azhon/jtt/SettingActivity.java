package com.azhon.jtt;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SettingActivity";
    private EditText etIp;
    private EditText etPort;
    private EditText etPhone;
    private EditText etManufacturer;
    private EditText etModel;
    private EditText etTerminalId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setTitle("服务器设置");
        initView();
        findViewById(R.id.btn_save).setOnClickListener(this);
    }

    private void initView() {
        etIp = findViewById(R.id.et_ip);
        etPort = findViewById(R.id.et_port);
        etPhone = findViewById(R.id.et_phone);
        etManufacturer = findViewById(R.id.et_manufacturer);
        etModel = findViewById(R.id.et_terminal_model);
        etTerminalId = findViewById(R.id.et_terminal_id);

        String ip = SharePreUtil.getString(this, "IP", Constants.IP);
        int port = SharePreUtil.getInt(this, "PORT", Constants.PORT);
        String phone = SharePreUtil.getString(this, "PHONE", Constants.PHONE);
        String manufacturerId = SharePreUtil.getString(this, "MANUFACTURER_ID", Constants.MANUFACTURER_ID);
        String terminalModel = SharePreUtil.getString(this, "TERMINAL_MODEL", Constants.TERMINAL_MODEL);
        String terminalId = SharePreUtil.getString(this, "TERMINAL_ID", Constants.TERMINAL_ID);
        etIp.setText(ip);
        etPort.setText(String.valueOf(port));
        etPhone.setText(phone);
        etManufacturer.setText(manufacturerId);
        etModel.setText(terminalModel);
        etTerminalId.setText(terminalId);
    }

    @Override
    public void onClick(View v) {
        SharePreUtil.putString(this, "IP", etIp.getText().toString());
        SharePreUtil.putInt(this, "PORT", Integer.parseInt(etPort.getText().toString()));
        SharePreUtil.putString(this, "PHONE", etPhone.getText().toString());
        SharePreUtil.putString(this, "MANUFACTURER_ID", etManufacturer.getText().toString());
        SharePreUtil.putString(this, "TERMINAL_MODEL", etModel.getText().toString());
        SharePreUtil.putString(this, "TERMINAL_ID", etTerminalId.getText().toString());
        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
    }
}
