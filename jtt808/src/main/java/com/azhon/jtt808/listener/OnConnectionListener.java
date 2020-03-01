package com.azhon.jtt808.listener;

import com.azhon.jtt808.bean.JTT808Bean;
import com.azhon.jtt808.bean.TerminalParamsBean;

import java.util.List;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.listener
 * 文件名:    OnConnectionListener
 * 创建时间:  2020/1/2 on 22:45
 * 描述:     TODO
 *
 * @author 阿钟
 */

public interface OnConnectionListener {
    //连接成功
    int CONNECTED = 1000;
    //断开连接
    int DIS_CONNECT = 1001;
    //重连
    int RE_CONNECT = 1002;

    void onConnectionSateChange(int state);

    //收到服务器的数据
    void receiveData(JTT808Bean jtt808Bean);


    //平台设置终端参数
    void terminalParams(List<TerminalParamsBean> params);
}
