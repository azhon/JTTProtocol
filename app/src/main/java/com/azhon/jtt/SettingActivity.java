package com.azhon.jtt;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;


public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SettingActivity";
    private EditText etIp;
    private EditText etPort;
    private EditText etPhone;
    private EditText etManufacturer;
    private EditText etModel;
    private EditText etTerminalId;
    private EditText etPlate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setTitle("服务器设置");
        initView();
        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.btn_reset).setOnClickListener(this);
    }

    private void initView() {
        etIp = findViewById(R.id.et_ip);
        etPort = findViewById(R.id.et_port);
        etPhone = findViewById(R.id.et_phone);
        etManufacturer = findViewById(R.id.et_manufacturer);
        etModel = findViewById(R.id.et_terminal_model);
        etTerminalId = findViewById(R.id.et_terminal_id);
        Spinner spinner = findViewById(R.id.spinner);
        etPlate = findViewById(R.id.et_plate);

        String ip = SharePreUtil.getString(this, "IP", Constants.IP);
        int port = SharePreUtil.getInt(this, "PORT", Constants.PORT);
        String phone = SharePreUtil.getString(this, "PHONE", Constants.PHONE);
        String manufacturerId = SharePreUtil.getString(this, "MANUFACTURER_ID", Constants.MANUFACTURER_ID);
        String terminalModel = SharePreUtil.getString(this, "TERMINAL_MODEL", Constants.TERMINAL_MODEL);
        String terminalId = SharePreUtil.getString(this, "TERMINAL_ID", Constants.TERMINAL_ID);
        String plate = SharePreUtil.getString(this, "PLATE", Constants.PLATE);
        etIp.setText(ip);
        etPort.setText(String.valueOf(port));
        etPhone.setText(phone);
        etManufacturer.setText(manufacturerId);
        etModel.setText(terminalModel);
        etTerminalId.setText(terminalId);
        etPlate.setText(plate);

        initSpinner(spinner);
    }

    private void initSpinner(Spinner spinner) {
        int color = SharePreUtil.getInt(this, "PLATE_COLOR", 1);
        List<String> items = new ArrayList<>();
        items.add("蓝色-1");
        items.add("黄色-2");
        items.add("黑色-3");
        items.add("白色-4");
        items.add("其他-9");
        //
        int position = 0;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).contains(String.valueOf(color))) {
                position = i;
                break;
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(position);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] names = items.get(position).split("-");
                int color = Integer.parseInt(names[1]);
                SharePreUtil.putInt(SettingActivity.this, "PLATE_COLOR", color);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                SharePreUtil.putString(this, "IP", etIp.getText().toString());
                SharePreUtil.putInt(this, "PORT", Integer.parseInt(etPort.getText().toString()));
                SharePreUtil.putString(this, "PHONE", etPhone.getText().toString());
                SharePreUtil.putString(this, "MANUFACTURER_ID", etManufacturer.getText().toString());
                SharePreUtil.putString(this, "TERMINAL_MODEL", etModel.getText().toString());
                SharePreUtil.putString(this, "TERMINAL_ID", etTerminalId.getText().toString());
                SharePreUtil.putString(this, "PLATE", etPlate.getText().toString());
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_reset:
                SharePreUtil.deleShareAll(this);
                Toast.makeText(this, "恢复成功", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

    }
}
