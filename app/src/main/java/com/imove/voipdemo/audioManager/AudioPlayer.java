package com.imove.voipdemo.audioManager;
import android.util.Log;
import com.imove.voipdemo.config.CommonConfig;
import java.io.File;
import java.io.FileInputStream;

import com.spoledge.aacdecoder.AACPlayer;

/**
 * Created by zhangyun on 14/11/27.
 */
public class AudioPlayer {
    public void StartPlay() {
        final File mfile=new File(CommonConfig.FILEPATH);
        try {
            mfile.delete();
            mfile.createNewFile();
            final FileInputStream fis = new FileInputStream(mfile);
            new Thread() {
                public void run() {
                    try {
                        AACPlayer aacPlayer = new AACPlayer();


                        while (true) {
                            if (mfile.length() > 2000) {
                              aacPlayer.play(fis);
                                break;
                            } else {
                                try {
                                    Thread.sleep(1000);
                                    Log.e("", "StartPlay sleep");
                               } catch (Exception e) {
                                    Log.e("", "StartPlay sleep error");
                                }
                            }
                        }
                    }
                    catch (Exception e) {
                        Log.e("", "StartPlay sleep error");
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
