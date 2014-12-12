package com.imove.voipdemo.audioManager;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import org.apache.http.protocol.HttpService;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by zhangyun on 14/11/27.
 */
public class MediaPlayManager {
    String datasource;
    MediaPlayer mediaPlayer;
    LocalSocket receiver;
    FileDescriptor sockfd;

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

    public boolean isPlaying()
    {
        return mediaPlayer.isPlaying();
    }


    public LocalSocket GetLocalSocketForPlay()
    {
        int buffersize = 5000;
        LocalSocket sender=null;
        try
        {
            LocalServerSocket lss = new LocalServerSocket("play");
            receiver.connect(new LocalSocketAddress("play"));
            receiver.setReceiveBufferSize(buffersize);
            receiver.setSendBufferSize(buffersize);
            sender = lss.accept();
            // Log.i("", "sender filefd:" + sender.getFileDescriptor());
            sender.setReceiveBufferSize(buffersize);
            sender.setSendBufferSize(buffersize);

        } catch (IOException e1)
        {
            e1.printStackTrace();
            Log.e("", "localSocket error:" + e1.getMessage());
        }
        return sender;
    }

    public LocalSocket getReceiver()
    {
        return receiver;
    }

    public void StartPlay() {
        MediaPlayManager mediaPlayManager=null;
        final File mfile=new File("/sdcard/test.mp4");
        mediaPlayManager=new MediaPlayManager("/sdcard/test.mp4");
        try {
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
