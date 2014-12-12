package com.imove.voipdemo.audioManager;
import android.media.MediaPlayer;
import android.net.LocalSocket;
import android.util.Log;
import com.imove.voipdemo.config.CommonConfig;
import java.io.File;
import java.io.IOException;


/**
 * Created by zhangyun on 14/11/27.
 */
public class MediaPlayManager {
    String datasource;
    MediaPlayer mediaPlayer;
    LocalSocket receiver;

    public MediaPlayManager(String mdatasource)
    {
        mediaPlayer=new MediaPlayer();
        datasource=mdatasource;
        receiver=new LocalSocket();
    }

    public void mediaPlay()
    {
        Log.i("MediaPlayManager","mediaPlay");
        try {
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.i("MediaPlayManager","complete："+mp.getCurrentPosition()+"duration:"+mp.getDuration());
                    int seekto=mp.getDuration();

                    try {
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(datasource);
                        mediaPlayer.prepare();
                        Log.i("MediaPlayManager","complete,befor seek："+mediaPlayer.getCurrentPosition());
                        mediaPlayer.seekTo(seekto);
                        mediaPlayer.start();
                        Log.i("MediaPlayManager","complete,after seek："+mediaPlayer.getCurrentPosition()+"seek to:"+seekto);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                }
            });
            mediaPlayer.setDataSource(datasource);

            mediaPlayer.prepare();
            mediaPlayer.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return;
    }


    public void StartPlay() {
        final File mfile=new File(CommonConfig.FILEPATH);
        try {
            mfile.delete();
            mfile.createNewFile();
        }
        catch (Exception e)
        {
            Log.e("", "createNewFile ");
        }

        new Thread() {
            public void run() {
                while(true)
                {
                    if(mfile.length()>2000)
                    {
                        mediaPlay();
                        break;
                    }
                    else
                    {
                        try{
                            Thread.sleep(1000);
                            Log.e("", "StartPlay sleep");
                        }
                        catch (Exception e)
                        {
                            Log.e("", "StartPlay sleep error");
                        }
                    }
                }
            }
        }.start();
    }
}
