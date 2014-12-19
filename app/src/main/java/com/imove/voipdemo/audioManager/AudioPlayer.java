package com.imove.voipdemo.audioManager;
import android.os.MemoryFile;
import android.util.Log;
import com.imove.voipdemo.config.CommonConfig;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;


import com.spoledge.aacdecoder.AACPlayer;

/**
 * Created by zhangyun on 14/11/27.
 */
public class AudioPlayer {
    InputStream inputStream;

    public  AudioPlayer(BufferedInputStream is)
    {
        inputStream=is;
    }

    public void StartPlay() {

        try {
            new Thread() {
                public void run() {
                    try {
                        AACPlayer aacPlayer = new AACPlayer(null ,10,300);
                        aacPlayer.play(inputStream,CommonConfig.AUDIO_BITRATE);

                    }
                    catch (Exception e) {
                        Log.e("", "StartPlay sleep error");
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        catch (Exception e)
        {
            Log.e("", "createNewFile ");
        }

    }
}
