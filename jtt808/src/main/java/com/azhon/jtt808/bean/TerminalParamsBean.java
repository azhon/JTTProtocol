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
    //数据类型
    private Class clz;
    //参数的值
    private Object value;

    public TerminalParamsBean(int id, Class clz, Object value) {
        this.id = id;
        this.clz = clz;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Class getClz() {
        return clz;
    }

    public void setClz(Class clz) {
        this.clz = clz;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
