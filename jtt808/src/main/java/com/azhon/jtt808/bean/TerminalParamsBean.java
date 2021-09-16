package com.azhon.jtt808.bean;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.bean
 * 文件名:    TerminalParamsBean
 * 创建时间:  2020/2/25 on 19:05
 * 描述:     TODO
 *
 * @author 阿钟
 */

public class TerminalParamsBean {
    //参数 ID
    private int id;
    private byte[] data;

    public TerminalParamsBean(int id, byte[] data) {
        this.id = id;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
