package com.imove.voipdemo.audioManager;

/**
 * Created by zhangyun on 14/12/27.
 */
public interface RecieveDataListener {
    void OnRecieveDataCallback(byte[] data,int len);
}
