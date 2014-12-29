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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.BlockingDeque;


/**
 * Created by zhangyun on 14/12/10.
 */
public class StreamManager implements  SendDataListener{
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
    private RecieveDataListener rl;

    private static StreamManager mServerSocket=null;

    public StreamManager()
    {
        mSocket = new Socket();
    }

    public void SetOnRecieveDataListener(RecieveDataListener l)
    {
        rl=l;
    }

    public synchronized static StreamManager getServerSocketInstance()
    {
        if(mServerSocket==null)
            mServerSocket=new StreamManager();


        return mServerSocket;
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


    static final short USERLIST_HEADER=(short)0x8001;
    static final short ALIVE_HEADER=(short)0x0001;
    static final short SESSION_HEADER=(short)0x0002;
    static final short AUDIO_HEADER=(short)0x0004;


    private int write_atom_byte(ByteBuffer buffer,String id,int len,byte[] data)
    {
        buffer.put(id.getBytes());
        buffer.putInt(len);

        if(len!=0)
            buffer.put(data,0,len);

        return 8+len;
    }

    private int write_atom_short(ByteBuffer buffer,String id,short data)
    {
        buffer.put(id.getBytes());
        buffer.putInt(2);
        buffer.putShort(data);
        return 10;
    }

    private int write_atom_int(ByteBuffer buffer,String id,int data)
    {
        buffer.put(id.getBytes());
        buffer.putInt(4);
        buffer.putInt(data);
        return 12;
    }

    static final  int HEADLEN=12;
    private int write_header(ByteBuffer buffer,short header,int len)
    {
        buffer.putShort((short)0x4d49);
        buffer.putShort((short)header);
        buffer.putInt(len);
        buffer.putShort((short)sendnum);
        buffer.putShort((short)0);//retcode
        return  0;
    }





    public synchronized void GetUserList()
    {
        new Thread() {
            public void run() {

                try {

                    while(true) {

                        if (mSocket.isClosed()==false){
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            buffer.order(ByteOrder.LITTLE_ENDIAN);

                            buffer.position(HEADLEN);
                            int len = write_atom_byte(buffer,"IPLS",0,null);
                            Log.i(TAG, "GetUserList list atom len :"+len);
                            int pos=buffer.position();
                            buffer.position(0);
                            write_header(buffer,USERLIST_HEADER,len);

                            for(int i=0;i<pos;++i)
                            {
                                dos.write(buffer.get(i));
                            }
                            dos.flush();
                            Log.i(TAG, "GetUserList list xxxxxx,seq:"+sendnum+",time:" +System.currentTimeMillis());
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



    public synchronized void KeepAliveToServer()
    {
        new Thread() {
            public void run() {
                try {

                    while (true) {

                        if(mSocket.isClosed()==false) {
                            Log.i(TAG, "KeepAliveToServer,sendnum:" + sendnum);
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            buffer.order(ByteOrder.LITTLE_ENDIAN);


                            buffer.position(12);
                            int len=write_atom_short(buffer,"STAT",(short)2);
                            len+=write_atom_byte(buffer,"NAME",mname.length(),mname.getBytes());
                            int pos=buffer.position();
                            buffer.position(0);
                            write_header(buffer,ALIVE_HEADER,len);

                            for(int i=0;i<pos;++i)
                            {
                                dos.write(buffer.get(i));
                            }
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
    //int length=10+mname.length()+4+4;


    public void SetPeerIp(int ip)
    {

        mPeerIp=ip;
        //
        // ReverseInt(ip);
    }

    private int ReverseInt(int in)
    {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(in);
        int out=0;
        for(int  i=0;i<buffer.position();i++)
        {
            out=out|(((buffer.get(i)&0xff)<<(3-i)*8));
        }
        return out;
    }

    private int ReverseShort(short in)
    {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort(in);
        int out=0;
        for(int  i=0;i<buffer.position();i++)
        {
            out=out|(((buffer.get(i)&0xff)<<(1-i)*8));
        }
        return out;
    }



    public synchronized void CreateSession(short action)
    {
        try {
            if(mSocket.isClosed()==false) {
                Log.i(TAG, "CreateSession,sendnum:" + sendnum);
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                buffer.order(ByteOrder.LITTLE_ENDIAN);

                buffer.position(HEADLEN);
                int len=write_atom_int(buffer,"IPDE",mPeerIp);
                len+=write_atom_short(buffer,"UACT",action);
                Log.i(TAG, "CreateSession list atom len :"+len);
                int pos=buffer.position();
                buffer.position(0);
                write_header(buffer,SESSION_HEADER,len);

                for(int i=0;i<pos;++i)
                {
                    dos.write(buffer.get(i));
                }
                dos.flush();
                Log.i(TAG, "CreateSession list xxxxxx,seq:"+sendnum+",time:" +System.currentTimeMillis());
                sendnum++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }



    public void OnSendDataCallback(byte[] sendbuf,int sendbufLen) {
        Log.i("RecoderByMediaCodec", "SendToServer,mSocket.isClosed:" + mSocket.isClosed() );

        if(mSocket.isClosed()==true&&mSocket.isConnected()==true)
        {
            ConnectHost();
        }
        try {

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.position(HEADLEN);
            int len=write_atom_int(buffer,"IPDE",mPeerIp);
            len+=write_atom_byte(buffer,"MDAT",sendbufLen,sendbuf);
            Log.i(TAG, "OnSendDataCallback  atom len :"+len);
            int pos=buffer.position();
            buffer.position(0);
            write_header(buffer,AUDIO_HEADER,len);

            for(int i=0;i<pos;++i)
            {
                dos.write(buffer.get(i));
            }
            dos.flush();
            Log.i(TAG, "OnSendDataCallback  xxxxxx,seq:"+sendnum+",time:" +System.currentTimeMillis());
            sendnum++;

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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

    private void HandleAction(int action)
    {
        switch (action)
        {
            case CommonConfig.USER_ACTION_REQUEST:
                if (mUIHandler != null) {
                    Log.i(TAG, "RespFromServer ,0x494d0200 ,sendmassage to list");

                    Message msg = new Message();
                    msg.what = 1;
                    mUIHandler.sendMessage(msg);
                }
                break;
            case CommonConfig.USER_ACTION_AGREE:
                Log.i(TAG, "RespFromServer ,0x494d0200 ,USER_ACTION_AGREE");
                //    AudioDecoderPlayer audioPlayer=new AudioDecoderPlayer();
                //    audioPlayer.setPlayer();
                Message msg = new Message();
                msg.what = 2;
                mUIHandler.sendMessage(msg);


                break;
            case CommonConfig.USER_ACTION_REJECT:
                break;
            case CommonConfig.USER_ACTION_QUIT:
                break;
        }
    }

    private int ReadBody(DataInputStream dataInputStream)
    {
        try {
            int length = ReverseInt(dataInputStream.readInt());
            int seq = ReverseShort(dataInputStream.readShort());
            int retcode = ReverseShort(dataInputStream.readShort());
            if(retcode==0)
                return length;
            else if(retcode == 1)
                return -11;
            else
                return -1;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    private void ReadUserList(DataInputStream dataInputStream) {
        int retcode = ReadBody(dataInputStream);
        try {
            if (retcode == -11) {

                String atomid = ReadStringFromStream(dataInputStream, 4);
                if (atomid.compareTo("IPLS") == 0) {
                    int atomsize = ReverseInt(dataInputStream.readInt());
                    int[] ip = new int[atomsize];
                    for (int i = 0; i < atomsize; i += 4) {
                        ip[i] = ReverseInt(dataInputStream.readInt());
                        Log.i(TAG, "resp ip list:" + Integer.toHexString(ip[i]));
                    }

                    dataInputStream.skipBytes(8);

                    //更新用户名
                    for (int i = 0; i < atomsize; i += 4) {
                        int nameleng = 0;
                        //StringBuffer sb = new StringBuffer();
                        atomid = ReadStringFromStream(dataInputStream, 4);
                        if (atomid.compareTo("NAME") == 0) {
                            nameleng = ReverseInt(dataInputStream.readInt());
                        }

                        String sb = ReadStringFromStream(dataInputStream, nameleng);

                        Log.i(TAG, "resp ip list name:" + sb);

                        DummyContent.DummyItem item = new DummyContent.DummyItem(Integer.toString(i), sb, ip[i]);

                        if (DummyContent.haveItem(item) == false) {
                            DummyContent.addItem(item);
                        }
                    }

                    //更新列表
                    Runnable mRunnable = new Runnable() {
                        @Override
                        public void run() {
                            mArrayAdapter.notifyDataSetChanged();
                        }
                    };

                    if (mUIHandler != null)
                        mUIHandler.post(mRunnable);

                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private int ReadAlive(DataInputStream dataInputStream)
    {
        return ReadBody(dataInputStream);

    }

    private void ReadSession(DataInputStream dataInputStream)
    {
        try {
            int bodylen = ReadBody(dataInputStream);
            Log.d("ReadSession","body len:"+bodylen);
            if (bodylen > 0) {
                int sip;
                String atomid = ReadStringFromStream(dataInputStream, 4);
                Log.d("ReadSession","in ipsr:"+atomid);
                if(atomid.compareTo("IPSR")==0)
                {
                    Log.d("ReadSession","in ipsr");
                    int len = ReverseInt(dataInputStream.readInt());
                    sip=ReverseInt(dataInputStream.readInt());
                    SetPeerIp(sip);
                }
                atomid = ReadStringFromStream(dataInputStream, 4);

                if(atomid.compareTo("IPDE")==0){
                    Log.d("ReadSession","in ipde");
                    int len = ReverseInt(dataInputStream.readInt());
                    int dip=ReverseInt(dataInputStream.readInt());
                }
                atomid = ReadStringFromStream(dataInputStream, 4);
                if(atomid.compareTo("UACT")==0){
                    Log.d("ReadSession","in uact");
                    int len = ReverseInt(dataInputStream.readInt());
                    int action = ReverseShort(dataInputStream.readShort());
                    HandleAction(action);
                }
            }
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void ReadAudio(DataInputStream dataInputStream)
    {
        try {
            byte[] body=new byte[1000];
            int bufferread=0;
            int length = ReadBody(dataInputStream);
            Log.d("ReadAudio","body len:"+length);
            if (length > 0) {
                length -= 20;
                dataInputStream.skipBytes(20);

                while (true) {
                    if ((bufferread = dataInputStream.read(body, 0, length)) > 0) {
                        Log.d("ReadAudio","buffer read:"+bufferread);
                        try {
                            rl.OnRecieveDataCallback(body, bufferread);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        if (bufferread < length)
                            length -= bufferread;
                        else
                            break;
                    } else
                        Thread.sleep(10);
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }



    public void ReceiveFromServer() {
        new Thread() {
            public void run() {
                InputStream is = null;
                Log.d("aa","ReceiveFromServer:"+Thread.currentThread().getId());
                try {
                    while(true)
                    {
                        if(mSocket.isClosed()==false&&mSocket.isConnected()==true)
                        {
                            break;
                        }
                        Thread.sleep(100);
                    }

                    is = mSocket.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    DataInputStream dis = new DataInputStream(bis);


                    while(true) {

                        int head = dis.readInt();


                        switch (head)
                        {
                            case 0x494d0100:
                                ReadAlive(dis);
                                break;

                            case 0x494d0200:
                                ReadSession(dis);
                            case 0x494d0300:
                                break;

                            case 0x494d0400:
                                ReadAudio(dis);
                                break;
                            case 0x494d0500:
                                break;
                            case 0x494d0600:
                                break;
                            case 0x494d0700:
                                break;

                            case 0x494d0180:

                                ReadUserList(dis);
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
