package com.imove.voipdemo.audioManager;

import android.net.LocalSocket;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.imove.voipdemo.config.CommonConfig;
import com.imove.voipdemo.dummy.DummyContent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import android.os.MemoryFile;

/**
 * Created by zhangyun on 14/12/10.
 */
public class ServerSocket {
    final String TAG="ServerSocket";
    private Socket mSocket;
    private String mIp;
    private int mPort;
    private int mPeerIp;
    private LocalSocket mLocalSocket;
    private ArrayAdapter mArrayAdapter;
    private Handler mUIHandler;
    private MemoryFile mMemoryFile=null;
    int sendnum=0;
    final int buffersize=5000;
    private PipedOutputStream pipedOutputStream=null;
   // private ArrayList<int> iplist;

    private static ServerSocket mServerSocket=null;



    public ServerSocket()
    {
        mSocket = new Socket();
    }

    public void SetPiedOutPutStream(PipedOutputStream out)
    {
        pipedOutputStream=out;
    }

    public synchronized static ServerSocket getServerSocketInstance()
    {
        if(mServerSocket==null)
            mServerSocket=new ServerSocket();

        return mServerSocket;
    }


    public void SetHost(String ip,int port)
    {
        mIp = ip;
        mPort = port;
    }

    public void ConnectHost()
    {
        new Thread() {
            public void run() {
                Log.d("aa", "ConnectHost:" + Thread.currentThread().getId());
                try {
                    mSocket.connect(new InetSocketAddress(mIp, mPort), 5000);
                } catch (IOException e) {
                    Log.e(TAG, "IOException:" + e);
                }
            }
        }.start();
    }

    public void GetUserList()
    {
        new Thread() {
            public void run() {

                try {
                    OutputStream os = null;
                    while(true) {

                        if (mSocket.isConnected() == true) {

                            os = mSocket.getOutputStream();
                            BufferedOutputStream bos = new BufferedOutputStream(os);
                            DataOutputStream dos = new DataOutputStream(bos);

                         //   Log.i(TAG, "GetUserList,sendnum:" + sendnum);
                            dos.writeInt(0x494d0180);

                            //长度为8(atom长度)
                            dos.writeInt(0x08000000);
                            dos.writeByte(sendnum & 0xff);
                            dos.writeByte((sendnum & 0xff00) >> 8);

                            //retcode
                            dos.writeShort(0);

                            //atom
                            dos.writeBytes("IPLS");
                            dos.writeInt(0x0);//STAT len =2
                            //dos.writeShort(0x0100);
                            dos.flush();
                            Log.d("aa","GetUserList,seq:"+sendnum);

                            sendnum++;
                            Thread.sleep(3000);
                        }
                        else
                        {
                            Thread.sleep(1000);
                        }

                    }

                } catch (Exception e) {
                    Log.e(TAG, "Exception:" + e);
                }
            }
        }.start();
    }

    public void KeepAliveToServer()
    {
        new Thread() {
            public void run() {
                try {
                    OutputStream os = null;
                    while (true) {

                        if(mSocket.isConnected()==true) {
                            os = mSocket.getOutputStream();
                            BufferedOutputStream bos = new BufferedOutputStream(os);
                            DataOutputStream dos = new DataOutputStream(bos);

                            //Log.i(TAG, "KeepAliveToServer,sendnum:" + sendnum);
                            dos.writeInt(0x494d0100);

                            //长度为10(atom长度)
                            dos.writeInt(0x0a000000);

                            dos.writeByte(sendnum & 0xff);
                            dos.writeByte((sendnum & 0xff00) >> 8);

                            //retcode
                            dos.writeShort(0);

                            //atom
                            dos.writeBytes("STAT");
                            dos.writeInt(0x02000000);//STAT len =2
                            dos.writeShort(0x0100);
                            dos.flush();
                            sendnum++;
                        }
                        Thread.sleep(10000);
                    }
                }
                catch (Exception e)
                {
                    Log.e(TAG, "IOException:" + e);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void setLocalSocket(LocalSocket receiver)
    {
        mLocalSocket=receiver;
    }

    public void SetPeerIp(int ip)
    {
        mPeerIp=ip;
    }
    public void SendAudioToServer() {
        new Thread() {
            public void run() {
                OutputStream os = null;
                Log.d("aa","SendToServer:"+Thread.currentThread().getId());
                try {
                    InputStream stream = mLocalSocket.getInputStream();
                    //    BufferedOutputStream bos = new BufferedOutputStream(os);

                    byte[] buffer = new byte[buffersize];
                    int bufferReadResult;

                    // mSocket.connect(new InetSocketAddress(mIp, mPort), 5000);
                    os = mSocket.getOutputStream();
                    BufferedOutputStream bos = new BufferedOutputStream(os);
                    DataOutputStream dos = new DataOutputStream(bos);

                    byte[] header = new byte[32];
                    int sendlen=0;

                    while ((bufferReadResult = stream.read(buffer, 0, buffersize)) > 0) {
                        Log.i(TAG, "SendToServer,buflen:" +bufferReadResult + ",seq:"+sendnum);

                        int bodylen=bufferReadResult+20;

                        dos.writeInt(0x494d0400);

                        dos.writeByte(bodylen&0xff);
                        dos.writeByte((bodylen & 0xff00) >> 8);
                        dos.writeByte((bodylen & 0xff0000) >> 16);
                        dos.writeByte((bodylen & 0xff000000) >> 24);

                        dos.writeByte(sendnum & 0xff);
                        dos.writeByte((sendnum & 0xff00) >> 8);

                        dos.writeShort(0);

                        dos.writeBytes("DIPS");
                        dos.writeInt(0x04000000);//dips len =4


                        //172.20.7.23 大htc
                        // 172.20.7.83 小htc
                        //ip
                        /*
                        dos.writeByte(172);
                        dos.writeByte(20);
                        dos.writeByte(7);
                        dos.writeByte(23);
                        */
                        dos.writeInt(mPeerIp);
                        Log.i("aa","mPeerIp:"+Integer.toHexString(mPeerIp));

                        dos.writeBytes("MDAT");

                        //dos.writeInt(bufferReadResult);
                        dos.writeByte(bufferReadResult & 0xff);
                        dos.writeByte((bufferReadResult & 0xff00) >> 8);
                        dos.writeByte((bufferReadResult & 0xff0000) >> 16);
                        dos.writeByte((bufferReadResult & 0xff000000) >> 24);

                        dos.write(buffer,0,bufferReadResult);

                        sendlen+=bufferReadResult;
                        dos.flush();
                        Log.i(TAG, "SendToServer,time:" +System.currentTimeMillis() + ",seq:"+sendnum);

                        /*
                        if(sendlen>1000) {
                            dos.flush();
                            sendlen=0;
                        }
                        */
                        sendnum++;
                        Log.i("aa","has sended");

                    }

                    Log.i(TAG, " stopped,read buff size:" + bufferReadResult);
                    // dos.close();
                    mSocket.close();

                } catch (Exception e) {
                    Log.e(TAG, "IOException:" + e);
                    e.printStackTrace();
                }
            }
        }.start();
    }



    public void ReceiveFromServer() {
        new Thread() {
            public void run() {
                InputStream is = null;
                Log.d("aa","ReceiveFromServer:"+Thread.currentThread().getId());
                try {
                    while(true)
                    {
                        if(mSocket.isConnected())
                        {
                            break;
                        }
                        Thread.sleep(100);
                    }

                    is = mSocket.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    DataInputStream dis = new DataInputStream(bis);

                   // File file=new File(CommonConfig.FILEPATH);
                    //FileOutputStream os = new FileOutputStream(file);

                    //MemoryFile memoryFile=new MemoryFile(CommonConfig.MEMORYFILE,1000000);
                   // OutputStream os=mMemoryFile.getOutputStream();

                    byte[] body = new byte[buffersize];
                    int bufferread;



                    while(true) {
                        int retcode=0;
                        int length = 0;
                        int seq=0;
                        int head = dis.readInt();
                        Log.i(TAG, "head :" + Integer.toHexString(head));

                        switch (head)
                        {
                            case 0x494d0100:
                                dis.skipBytes(6);
                                retcode+=dis.readByte()&0xff;
                                retcode+=dis.readByte()&0xff;
                             //   Log.i(TAG, "retcode :" + Integer.toHexString(retcode));
                                break;

                            case 0x494d0200:
                                break;

                            case 0x494d0300:
                                break;

                            case 0x494d0400:
                                // dis.skipBytes(6);
                                length+=dis.readByte()&0xff;
                                length+=(dis.readByte()<<8)&0xff00;
                                length+=(dis.readByte()<<16)&0xff0000;
                                length+=(dis.readByte()<<24)&0xff000000;
                                //length-=20;

                                seq+=dis.readByte()&0xff;
                                seq+=(dis.readByte()&0xff)<<8;

                                retcode+=dis.readByte()&0xff;
                                retcode+=(dis.readByte()&0xff)<<8;

                               // Log.i(TAG, "length :" + Integer.toHexString(length)+",seq:"+seq+",retcode:"+retcode);
                                Log.i(TAG, "RespFromServer,time:" +System.currentTimeMillis() + ",seq:"+seq +",length :" + Integer.toString(length));

                                if(retcode==1)
                                    break;

                                length-=20;
                                dis.skipBytes(20);

                                while (true) {
                                    if ((bufferread = dis.read(body, 0, length)) > 0) {
                                        pipedOutputStream.write(body, 0, bufferread);
                                        pipedOutputStream.flush();

                                        if (bufferread < length)
                                            length -= bufferread;
                                        else
                                            break;
                                    } else
                                        Thread.sleep(10);
                                }
                                break;
                            case 0x494d0500:
                                break;
                            case 0x494d0600:
                                break;

                            case 0x494d0700:
                                break;

                            case 0x494d0180:

                                length+=dis.readByte()&0xff;
                                length+=(dis.readByte()<<8)&0xff00;
                                length+=(dis.readByte()<<16)&0xff0000;
                                length+=(dis.readByte()<<24)&0xff000000;
                                //length-=20
                                seq+=dis.readByte()&0xff;
                                seq+=(dis.readByte()&0xff)<<8;
                                Log.i(TAG, "resp ip list xxxxxx,seq:"+seq);
                                retcode+=dis.readByte()&0xff;
                                retcode+=(dis.readByte()&0xff)<<8;

                                if(length==0)
                                    break;

                                int id = dis.readInt();
                                int atomsize=0;

                                atomsize+=dis.readByte()&0xff;
                                atomsize+=(dis.readByte()<<8)&0xff00;
                                atomsize+=(dis.readByte()<<16)&0xff0000;
                                atomsize+=(dis.readByte()<<24)&0xff000000;

                              //  Log.i(TAG, "ip list size:" + atomsize);

                                int[] ip=new int[atomsize];
                                for(int i=0;i<atomsize;i+=4) {
                                    ip[i] = dis.readInt();
                                    Log.i(TAG, "resp ip list:" + Integer.toHexString(ip[i]));
                                    DummyContent.DummyItem item=new DummyContent.DummyItem(Integer.toString(i) ,Integer.toString(ip[i]),ip[i]);
                                    DummyContent.addItem(item);
                                }

                                //更新列表
                                Runnable mRunnable = new Runnable()
                                {
                                    @Override
                                    public void run() {
                                        mArrayAdapter.notifyDataSetChanged();
                                    }
                                };
                                mUIHandler.post(mRunnable);

                                break;

                            default:
                                break;
                        }

                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void setUserListUpdate(ArrayAdapter mAdapter,Handler handler)
    {
        mArrayAdapter=mAdapter;
        mUIHandler=handler;
    }
}
