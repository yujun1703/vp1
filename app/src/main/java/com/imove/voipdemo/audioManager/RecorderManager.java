package com.imove.voipdemo.audioManager;

import android.media.MediaRecorder;
import android.util.Log;

import com.imove.voipdemo.config.CommonConfig;

import java.io.FileDescriptor;

/**
 * Created by zhangyun on 14/11/26.
 */
public class RecorderManager {

    private MediaRecorder mMediaRecorder;
    private final String TAG="RecorderManager";

    public RecorderManager()
    {
        mMediaRecorder=new MediaRecorder();
    }

    public boolean recorder(FileDescriptor  fileDescriptor)
    {
        try
        {
            if(mMediaRecorder == null)
                mMediaRecorder = new MediaRecorder();
            else
                mMediaRecorder.reset();

            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);

           // Log.i(TAG, "Audio：Current container format: " + "3GP\n");
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setAudioEncodingBitRate(CommonConfig.AUDIO_BITRATE);


          //  Log.i(TAG, "Audio：Current encoding format: "+"aac\n");

            mMediaRecorder.setOutputFile(fileDescriptor);
          //  Log.i(TAG, "start send into sender~");

            mMediaRecorder.setMaxDuration(0);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            Log.i(TAG, "start recorder");
            return true;
        }
        catch (Exception e)
        {
            // TODO: handle exception
            e.printStackTrace();
            return false;
        }
    }

    public  boolean stopRecorder()
    {
        mMediaRecorder.stop();
        return true;
    }


}
