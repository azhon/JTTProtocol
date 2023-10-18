package com.azhon.jtt;

import com.azhon.jtt808.JTT808Manager;

/**
 * createDate: 2023/10/18 on 11:52
 * desc:
 *
 * @author azhon
 */


class LocationThread extends Thread {
    private final JTT808Manager manager;
    private boolean loop = true;

    LocationThread(JTT808Manager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        while (loop) {
            try {
                Thread.sleep(5 * 1000);
                manager.uploadLocation(Constants.LAT, Constants.LNG);
                Thread.sleep(5 * 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void release() {
        loop = false;
        interrupt();
    }
}
