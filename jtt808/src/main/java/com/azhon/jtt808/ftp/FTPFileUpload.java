package com.azhon.jtt808.ftp;

import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.ftp
 * 文件名:    FTPFileUpload
 * 创建时间:  2020/2/20 on 12:02
 * 描述:     TODO
 *
 * @author 阿钟
 */

public class FTPFileUpload {
    private static final String TAG = "FTPFileUpload";
    private String ip;
    private int port;
    private String username;
    private String password;
    private String remotePath;

    public FTPFileUpload(String ip, int port, String username, String password) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public boolean uploadFile(File file) {
        Log.d(TAG, "开始上传：" + file);
        //创建FTPClient对象
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip, port);
            boolean loginResult = ftpClient.login(username, password);
            int returnCode = ftpClient.getReplyCode();
            // 登录成功
            if (loginResult && FTPReply.isPositiveCompletion(returnCode)) {
                // 设置上传目录
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                FileInputStream fis = new FileInputStream(file);
                //写入文件
                ftpClient.storeFile(file.getName(), fis);
                fis.close();
                Log.d(TAG, "文件上传成功：" + file);
                return true;
            } else {
                Log.e(TAG, "FTP服务器登录失败...loginResult=" + loginResult + " returnCode=" + returnCode);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "FTP服务器登录失败..." + e.getMessage());
            return false;
        }
    }
}
