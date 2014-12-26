package com.imove.voipdemo.audioManager;

import android.net.LocalSocket;
import android.os.Handler;
import android.os.Message;
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
import java.util.concurrent.BlockingDeque;

import android.os.MemoryFile;
import junit.framework.TestCase;



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
    int sendnum=0;
    final int buffersize=5000;
    String mname;
    private  AudioDecoderPlayer mAudioDecoderPlayer;
    private DataOutputStream dos;
    ConnectionFsm connectionFsm;

    private static ServerSocket mServerSocket=null;

    public ServerSocket()
    {
        mSocket = new Socket();
        connectionFsm=new ConnectionFsm();
    }
    public  ConnectionFsm getConnectionFsm()
    {
        return connectionFsm;
    }


    public synchronized static ServerSocket getServerSocketInstance()
    {
        if(mServerSocket==null)
            mServerSocket=new ServerSocket();

        return mServerSocket;
    }

    public void SetAudiaPlayer(AudioDecoderPlayer player)
    {
        mAudioDecoderPlayer=player;
    }


    public void SetHost(String ip,int port)
    {
        mIp = ip;
        mPort = port;
    }


    public void SetUserName(String name)
    {
        mname=name;
    }

    public void ConnectHost()
    {
        new Thread() {
            public void run() {

                try {

                    mSocket.connect(new InetSocketAddress(mIp, mPort), 5000);
                    OutputStream os = mSocket.getOutputStream();
                    BufferedOutputStream bos = new BufferedOutputStream(os);
                    dos = new DataOutputStream(bos);
                    KeepAliveToServer();
                    GetUserList();
                    Log.d("aa", "ConnectHost:" + Thread.currentThread().getId());
                } catch (IOException e) {
                    Log.e(TAG, "IOException:" + e);
                }
            }
        }.start();
    }

    public synchronized void GetUserList()
    {
        new Thread() {
            public void run() {

                try {

                    while(true) {

                        if (mSocket.isConnected() == true){
                          //  if (mSocket.isConnected() == true&&DummyContent.isEmpty()==false) {
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

                        //    Log.i(TAG, "GetUserList list xxxxxx,seq:"+sendnum+",time:" +System.currentTimeMillis());

                            sendnum++;
                            //Thread.sleep(30000);
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


    public synchronized void KeepAliveToServer()
    {
        new Thread() {
            public void run() {
                try {

                    while (true) {

                        if(mSocket.isConnected()==true) {

                            //Log.i(TAG, "KeepAliveToServer,sendnum:" + sendnum);
                            dos.writeInt(0x494d0100);

                            //长度为10(atom长度)
                            //dos.writeInt(0x0a000000);
                            int length=10+mname.length()+4+4;

                            dos.writeByte(length & 0xff);
                            dos.writeByte((length & 0xff00) >> 8);
                            dos.writeByte((length & 0xff0000) >> 16);
                            dos.writeByte((length & 0xff000000) >> 24);


                            dos.writeByte(sendnum & 0xff);
                            dos.writeByte((sendnum & 0xff00) >> 8);

                            //retcode
                            dos.writeShort(0);

                            //atom
                            dos.writeBytes("STAT");
                            dos.writeInt(0x02000000);//STAT len =2
                            dos.writeShort(0x0100);

                            //user name
                            dos.writeBytes("NAME");
                            //dos.writeInt(name.length());//STAT len =2
                            dos.writeByte(mname.length() & 0xff);
                            dos.writeByte((mname.length() & 0xff00) >> 8);
                            dos.writeByte((mname.length() & 0xff0000) >> 16);
                            dos.writeByte((mname.length() & 0xff000000) >> 24);

                            dos.writeBytes(mname);

                            dos.flush();
                            sendnum++;
                        }
                        //Thread.sleep(20000);
                        Thread.sleep(2000);
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

    public void SetPeerIp(int ip)
    {
        mPeerIp=ip;
    }





    public synchronized void CreateSession(short action)
    {
        try {
            if(mSocket.isConnected()==true) {
                //Log.i(TAG, "KeepAliveToServer,sendnum:" + sendnum);
                dos.writeInt(0x494d0200);

                //长度为10(atom长度)
                //dos.writeInt(0x0a000000);
               // int length=10+mname.length();
               // int length=12+mname.length()+4+4+10;
                int length=12+10;

                dos.writeByte(length & 0xff);
                dos.writeByte((length & 0xff00) >> 8);
                dos.writeByte((length & 0xff0000) >> 16);
                dos.writeByte((length & 0xff000000) >> 24);

                dos.writeByte(sendnum & 0xff);
                dos.writeByte((sendnum & 0xff00) >> 8);

                //retcode
                dos.writeShort(0);

                //////////atom//////

                //12bytes
                dos.writeBytes("IPDE");
                dos.writeInt(0x04000000);//dips len =4
                dos.writeInt(mPeerIp);

/*
                //user name
                dos.writeBytes("NAME");
                dos.writeByte(mname.length() & 0xff);
                dos.writeByte((mname.length() & 0xff00) >> 8);
                dos.writeByte((mname.length() & 0xff0000) >> 16);
                dos.writeByte((mname.length() & 0xff000000) >> 24);
                dos.writeBytes(mname);
*/

                //command 10bytes
                dos.writeBytes("UACT");
                dos.writeInt(0x02000000);
                dos.writeByte(action & 0xff);
                dos.writeByte((action & 0xff00) >> 8);

                dos.flush();
                sendnum++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }

    public synchronized void SendAudioToServer(int bufferLen,byte[] buffer) {
        Log.i("RecoderByMediaCodec", "SendToServer,buflen:" + bufferLen );

        if(mSocket.isConnected()==false)
        {
            ConnectHost();
        }

        int bodylen = bufferLen + 20;
        try {

            dos.writeInt(0x494d0400);

            dos.writeByte(bodylen & 0xff);
            dos.writeByte((bodylen & 0xff00) >> 8);
            dos.writeByte((bodylen & 0xff0000) >> 16);
            dos.writeByte((bodylen & 0xff000000) >> 24);

            dos.writeByte(sendnum & 0xff);
            dos.writeByte((sendnum & 0xff00) >> 8);

            dos.writeShort(0);

            dos.writeBytes("IPDE");
            dos.writeInt(0x04000000);//dips len =4
            dos.writeInt(mPeerIp);
            Log.i("aa", "mPeerIp:" + Integer.toHexString(mPeerIp));

            dos.writeBytes("MDAT");

            //dos.writeInt(bufferReadResult);
            dos.writeByte(bufferLen & 0xff);
            dos.writeByte((bufferLen & 0xff00) >> 8);
            dos.writeByte((bufferLen & 0xff0000) >> 16);
            dos.writeByte((bufferLen & 0xff000000) >> 24);

            dos.write(buffer, 0, bufferLen);
            // sendlen += bufferReadResult;
            dos.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Log.i(TAG, "SendToServer,time:" + System.currentTimeMillis() + ",seq:" + sendnum);
    }

    private void readAtom(DataInputStream dis)
    {

    }

    private String ReadStringFromStream(DataInputStream dis,int len)
    {
        StringBuffer sb = new StringBuffer();
        for(int j=0;j<len;j++)
        {
            // sb.append(dis.read());
            try {
                sb.append(String.format("%c", dis.read()));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return sb.toString();
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
                                Log.i(TAG, "retcode :" + Integer.toHexString(retcode));
                                break;

                            case 0x494d0200:
                                Log.d("aa","dd");
                                length+=dis.readByte()&0xff;
                                length+=(dis.readByte()<<8)&0xff00;
                                length+=(dis.readByte()<<16)&0xff0000;
                                length+=(dis.readByte()<<24)&0xff000000;
                                //length-=20;

                                seq+=dis.readByte()&0xff;
                                seq+=(dis.readByte()&0xff)<<8;

                                retcode+=dis.readByte()&0xff;
                                retcode+=(dis.readByte()&0xff)<<8;

                             //   if(retcode==1)
                              //      break;

                                if(retcode==0) {

                                    Log.i(TAG, "RespFromServer ,0x494d0200 retcode" + Integer.toHexString(retcode));

/*
                                    dos.writeBytes("UACT");
                                    dos.writeInt(0x02000000);
                                    dos.writeByte(action & 0xff);
                                    dos.writeByte((action & 0xff00) >> 8);
*/

                                //"IPSR" LEN=4
                                    dis.skipBytes(8);
                                    int sip=dis.readInt();
                                    Log.i(TAG, "RespFromServer ,ssip:"+sip);

                                //"IPDE" len=4
                                    dis.skipBytes(12);


/*
                                    dos.writeBytes("UACT");
                                    dos.writeInt(0x02000000);
                                    dos.writeByte(action & 0xff);
                                    dos.writeByte((action & 0xff00) >> 8);
*/

                                    dis.skipBytes(8);
                                    int action=dis.readByte()&0xff;
                                    action+=(dis.readByte()&0xff)<<8;

                                    switch (action)
                                    {
                                        case CommonConfig.USER_ACTION_REQUEST:
                                            if (mUIHandler != null) {
                                                Log.i(TAG, "RespFromServer ,sendmassage to list");
                                                Message msg = new Message();
                                                msg.what = 1;
                                                mUIHandler.sendMessage(msg);
                                            }
                                            break;
                                        case CommonConfig.USER_ACTION_AGREE:
                                            break;
                                        case CommonConfig.USER_ACTION_REJECT:
                                            break;
                                        case CommonConfig.USER_ACTION_QUIT:
                                            break;
                                    }
                                }

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
                                Log.i(TAG, "RespFromServer,time:" + System.currentTimeMillis() + ",seq:" + seq + ",length :" + Integer.toString(length));

                                if(retcode==1)
                                    break;

                                length-=20;
                                dis.skipBytes(20);

                                while (true) {
                                    if ((bufferread = dis.read(body, 0, length)) > 0) {

                                        //pipedOutputStream.write(body, 0, bufferread);
                                        //pipedOutputStream.flush();
                                        //feedandplay(body);
                                        Log.i(TAG, "RespFromServer,write to pipe,len:" +bufferread);
                                        mAudioDecoderPlayer.FeedAndPlay(body,bufferread);

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
                               // Log.i(TAG, "resp ip list xxxxxx,seq:"+seq+",time:" +System.currentTimeMillis());
                                retcode+=dis.readByte()&0xff;
                                retcode+=(dis.readByte()&0xff)<<8;
                                Log.i(TAG, "ip list length:" + length);
                                if(length==0)
                                    break;

                                int id = dis.readInt();//"IPLS"
                                int atomsize=0;

                                atomsize+=dis.readByte()&0xff;
                                atomsize+=(dis.readByte()<<8)&0xff00;
                                atomsize+=(dis.readByte()<<16)&0xff0000;
                                atomsize+=(dis.readByte()<<24)&0xff000000;

                                Log.i(TAG, "ip list size:" + atomsize);
                                int[] ip=new int[atomsize];
                                for(int i=0;i<atomsize;i+=4) {
                                    ip[i] = dis.readInt();
                                    Log.i(TAG, "resp ip list:" + Integer.toHexString(ip[i]));
                                    /*
                                    DummyContent.DummyItem item=new DummyContent.DummyItem(Integer.toString(i) ,Integer.toString(ip[i]),ip[i]);

                                    if(DummyContent.haveItem(item)==false) {
                                        DummyContent.addItem(item);
                                    }
                                    */
                                }

                                dis.skipBytes(8);
                                //更新用户名
                                for(int i=0;i<atomsize;i+=4)
                                {
                                    int nameleng=0;
                                    StringBuffer sb = new StringBuffer();
                                    dis.skipBytes(4);//"NAME"

                                    nameleng+=dis.readByte()&0xff;
                                    nameleng+=(dis.readByte()<<8)&0xff00;
                                    nameleng+=(dis.readByte()<<16)&0xff0000;
                                    nameleng+=(dis.readByte()<<24)&0xff000000;
                                    Log.i(TAG, "resp ip list len:" + Integer.toHexString(nameleng));

                                    for(int j=0;j<nameleng;j++)
                                    {
                                       // sb.append(dis.read());
                                        sb.append(String.format("%c",dis.read()));
                                    }

                                    Log.i(TAG, "resp ip list name:" + sb.toString());

                                    DummyContent.DummyItem item=new DummyContent.DummyItem(Integer.toString(i) ,sb.toString(),ip[i]);

                                    if(DummyContent.haveItem(item)==false) {
                                        DummyContent.addItem(item);
                                    }
                                    //name[i]=dis.readChars(nameleng);
                                }

                                //更新列表
                                Runnable mRunnable = new Runnable()
                                {
                                    @Override
                                    public void run() {
                                        mArrayAdapter.notifyDataSetChanged();
                                    }
                                };

                                if(mUIHandler!=null)
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
