package com.azhon.jtt;

import com.azhon.jtt808.JTT808Manager;

/**
 * createDate: 2023/10/18 on 11:52
 * desc:
 *
 * @author azhon
 */


class LocationThread extends Thread {
    private JTT808Manager manager;

    LocationThread(JTT808Manager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(10 * 1000);
                manager.uploadLocation(MainActivity.LAT, MainActivity.LNG);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
