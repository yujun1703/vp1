package com.imove.voipdemo.audioManager;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.util.Log;

import com.imove.voipdemo.config.CommonConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.net.DatagramPacket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by zhangyun on 14/12/18.
 */
public class AudioDecoderPlayer {
    MediaCodec decoder=null;
    //InputStream inputStream;
    PipedInputStream inputStream;

    static int sampleRateInHz = CommonConfig.sampleRateInHz;
    static int channelConfig = CommonConfig.channeloutConfig;
    static int audioFormat = CommonConfig.audioFormat;
    static int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)*2;
    static int bitrate=CommonConfig.bitrate;

    static int channelCount=CommonConfig.getChannels(channelConfig);



    AudioTrack player;
    public AudioDecoderPlayer(PipedInputStream is)
    {
        Log.d("AudioDecoderPlayer", "aaaa");
        inputStream=is;

        decoder = MediaCodec.createDecoderByType(CommonConfig.mediaType);
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, CommonConfig.mediaType);//aac raw
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channelCount);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRateInHz);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);//AAC-HE 64kbps
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectHE);
        decoder.configure(format, null, null, 0);
        decoder.start();
    }

    public boolean setPlayer()
    {

        int bufferSizePlayer = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)*2;
        player= new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, channelConfig, audioFormat, bufferSizePlayer, AudioTrack.MODE_STREAM);

        Log.d("AudioDecoderPlayer", "bufferSizePlayer:"+bufferSizePlayer+",mono:"+bufferSizeInBytes);
     //  player= new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, AudioFormat.CHANNEL_OUT_MONO, audioFormat, bufferSizePlayer, AudioTrack.MODE_STREAM);
        if (player.getState() == AudioTrack.STATE_INITIALIZED)
        {
            //设置回调，收到数据立即解码并播放
            ServerSocket.getServerSocketInstance().SetAudiaPlayer(this);
            player.play();
            return true;
        }
        else
        {
            return false;
        }
    }

    public void FeedAndPlay(byte[] data,int len)
    {

        ByteBuffer inputBuffer;
        ByteBuffer outputBuffer;

        ByteBuffer[] inputBuffers = decoder.getInputBuffers();
        ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
        int inputBufferIndex = decoder.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(data);
            decoder.queueInputBuffer(inputBufferIndex, 0, len, 0, 0);
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 0);

        while (outputBufferIndex >= 0) {

            outputBuffer = outputBuffers[outputBufferIndex];
            byte[] outData = new byte[bufferInfo.size];//pcm
            outputBuffer.get(outData);
            outputBuffer.clear();

            if (outData.length > 0) {
                player.write(outData, 0, outData.length);
            }
            //   Log.d("AudioDecoder", bufferInfo.presentationTimeUs + " bufferInfo.presentationTimeUs");

            decoder.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 0);

        }
    }

}
