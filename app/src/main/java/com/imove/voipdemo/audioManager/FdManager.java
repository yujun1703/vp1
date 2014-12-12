package com.imove.voipdemo.audioManager;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.DataInputStream;

import java.io.OutputStream;
import java.net.SocketImpl;
import java.io.BufferedOutputStream;
import java.nio.ByteBuffer;

/**
 * Created by zhangyun on 14/11/26.
 */
public class FdManager {

    static final String TAG="FdManager";
    LocalSocket receiver=new LocalSocket();
    String mPath;
    String mName;
    FileDescriptor sockfd;
    FileDescriptor filefd;
    private Socket mSocket;
    private SocketImpl mSocketImpl;
    private BufferedReader br;
   // String mFilepath;
   // String mFilename;

    private static byte[] sendData;
    private  File mFile;




    public FdManager()
    {
       // mSocket = new Socket();
    }

    public FileDescriptor GetStreamSocket()
    {
        int buffersize = 5000;
        try
        {
            LocalServerSocket lss = new LocalServerSocket("amr");
            receiver.connect(new LocalSocketAddress("amr"));
            receiver.setReceiveBufferSize(buffersize);
            receiver.setSendBufferSize(buffersize);
            LocalSocket sender = lss.accept();
           // Log.i("", "sender filefd:" + sender.getFileDescriptor());
            sender.setReceiveBufferSize(buffersize);
            sender.setSendBufferSize(buffersize);
            sockfd=sender.getFileDescriptor();
            return sockfd;
        } catch (IOException e1)
        {
            e1.printStackTrace();
            Log.e("", "localSocket error:" + e1.getMessage());
        }
        return sockfd;
    }

    public LocalSocket getReceiver()
    {
        return receiver;
    }



    public FileDescriptor GetFileFd(String filepath,String filename)
    {
         mFile=new File(filepath,filename);
        try{
            mFile.createNewFile();
            FileOutputStream os = new FileOutputStream(mFile);


        //    BufferedOutputStream bos = new BufferedOutputStream(os);
         //   DataOutputStream dos = new DataOutputStream(bos);
            filefd=os.getFD();
            Log.i(TAG, "##initializeVideo....");
        }
        catch (IOException e)
        {
            Log.d("aa", "IOException",e);
        }
        return filefd;
    }

    public String GetFilePath()
    {
        return mFile.getPath();
    }




    public void SetFilePath(String path,String name)
    {
       // return mFile.getPath();
        mPath=path;
        mName=name;
    }




}
