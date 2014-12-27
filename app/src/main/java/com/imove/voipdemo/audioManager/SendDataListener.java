package com.imove.voipdemo.audioManager;

/**
 * Created by zhangyun on 14/12/27.
 */
public interface SendDataListener {
    void OnSendDataCallback(byte[] data,int len);
}
