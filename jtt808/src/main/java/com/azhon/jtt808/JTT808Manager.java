package com.azhon.jtt808;

import android.util.Log;

import com.azhon.jtt808.bean.JTT808Bean;
import com.azhon.jtt808.listener.OnConnectionListener;
import com.azhon.jtt808.netty.JTT808Client;
import com.azhon.jtt808.util.JTT808Util;

import java.io.File;
import java.util.List;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808
 * 文件名:    JTT808Manager
 * 创建时间:  2020/1/4 on 16:35
 * 描述:     TODO
 *
 * @author 阿钟
 */

public class JTT808Manager {
    private static final String TAG = "JTT808Manager";

    private static JTT808Manager manager = new JTT808Manager();
    //标记是否初始化
    private boolean isInit = false;
    //终端手机号
    private String phone;
    //终端ID
    private String terminalId;
    //服务器地址和端口
    private String ip;
    private int port;
    private OnConnectionListener listener;

    public static JTT808Manager getInstance() {
        return manager;
    }

    /**
     * 设置监听
     *
     * @param listener
     * @return
     */
    public JTT808Manager setOnConnectionListener(OnConnectionListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 初始化
     *
     * @param phone 终端手机号 12位
     * @param ip    服务器地址
     * @param port  服务器端口
     */
    public void init(String phone, String terminalId, String ip, int port) {
        if (isInit) return;
        this.phone = phone;
        this.terminalId = terminalId;
        this.ip = ip;
        this.port = port;
        if (this.phone.length() != 12) {
            Log.e(TAG, "终端手机号的长度必须为12位");
        }
        isInit = true;
        connectServer();
    }

    /**
     * 连接服务器
     */
    private void connectServer() {
        JTT808Client client = JTT808Client.getInstance();
        client.setServerInfo(ip, port);
        client.setConnectionListener(listener);
        client.connect();
    }

    /**
     * 主动断开与服务器的连接
     */
    public void disconnect() {
        isInit = false;
        JTT808Client client = JTT808Client.getInstance();
        client.disconnect();
    }
    //========================协议方法===========================================

    /**
     * 注册
     *
     * @param manufacturerId 制造商 ID
     * @param terminalModel  终端型号
     */
    public void register(String manufacturerId, String terminalModel) {
        JTT808Bean register = JTT808Util.register(manufacturerId, terminalModel, terminalId);
        Log.d(TAG, "发送注册: " + register.toString());
        JTT808Client.getInstance().writeAndFlush(register);
    }

    /**
     * 上传经纬度
     *
     * @param lat 纬度，乘以10的6次方
     * @param lng 经度，乘以10的6次方
     */
    public void uploadLocation(long lat, long lng) {
        JTT808Bean location = JTT808Util.uploadLocation(lat, lng);
        Log.d(TAG, "上传位置信息: " + location.toString());
        JTT808Client.getInstance().writeAndFlush(location);
    }


    /**
     * 上传报警信息（渝标）
     * 重庆车检院平台通讯协议（3.4.2驾驶员行为监测功能报警）
     *
     * @param lat       纬度，乘以10的6次方
     * @param lng       经度，乘以10的6次方
     * @param alarmType 1：抽烟，2：打电话，3：未注视前方，4：疲劳驾驶，5：未在驾驶位
     * @param level     1：一级报警，2：二级报警
     * @param degree    范围 1~10。数值越大表示疲劳程度越严重
     * @param files     附件集合
     */
    public void uploadAlarmInfoYB(long lat, long lng, int alarmType, int level, int degree, List<File> files) {
        JTT808Bean alarm = JTT808Util.uploadAlarmInfoYB(lat, lng, alarmType, level, degree, files, terminalId);
        Log.d(TAG, "上传报警信息（渝标）: " + alarm.toString() + "\n" + alarmType
                + "(1：抽烟，2：打电话，3：未注视前方，4：疲劳驾驶，5：未在驾驶位)，"
                + degree + "(1~10,数值越大表示疲劳程度越严重)，附件数量：" + files.size());
        JTT808Client.getInstance().writeAndFlush(alarm);
    }

    //========================get set===========================================


    public String getPhone() {
        return phone;
    }

    public String getTerminalId() {
        return terminalId;
    }
}
