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

    /*
    public void SetAudiaPlayer(AudioDecoderPlayer player)
    {
        mAudioDecoderPlayer=player;
    }
*/

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
      //  mPeerIp=ip;

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(ip);
        mPeerIp=0;
        for(int  i=0;i<buffer.position();i++)
        {
            mPeerIp=mPeerIp|(((buffer.get(i)&0xff)<<(3-i)*8));
            //mPeerIp=mPeerIp<<8 + buffer.get(i);
        }
        Log.d("aa","ip:"+Integer.toHexString(mPeerIp)+" org ip:"+Integer.toHexString(ip));

    }

/*
    public synchronized void CreateSession(short action)
    {
        try {
            if(mSocket.isConnected()==true) {
                dos.writeInt(0x494d0200);

                int length=12+10;

                dos.writeByte(length & 0xff);
                dos.writeByte((length & 0xff00) >> 8);
                dos.writeByte((length & 0xff0000) >> 16);
                dos.writeByte((length & 0xff000000) >> 24);

                dos.writeByte(sendnum & 0xff);
                dos.writeByte((sendnum & 0xff00) >> 8);

                dos.writeShort(0);

                dos.writeBytes("IPDE");
                dos.writeInt(0x04000000);//dips len =4
                dos.writeInt(mPeerIp);

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
*/


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

/*
    public synchronized void OnSendDataCallback(byte[] sendbuf,int sendbufLen) {
        Log.i("RecoderByMediaCodec", "SendToServer,buflen:" + sendbufLen );

        if(mSocket.isConnected()==false)
        {
            ConnectHost();
        }

        int bodylen = sendbufLen + 20;
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
            dos.writeByte(sendbufLen & 0xff);
            dos.writeByte((sendbufLen & 0xff00) >> 8);
            dos.writeByte((sendbufLen & 0xff0000) >> 16);
            dos.writeByte((sendbufLen & 0xff000000) >> 24);

            dos.write(sendbuf, 0, sendbufLen);
            // sendlen += bufferReadResult;
            dos.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Log.i(TAG, "SendToServer,time:" + System.currentTimeMillis() + ",seq:" + sendnum);
    }
*/

    public void OnSendDataCallback(byte[] sendbuf,int sendbufLen) {
  //  public synchronized void SendAudioToServer(int bufferLen,byte[] buffer) {
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
       // Log.i(TAG, "SendAudioToServer,body len : "+bodylen);
    }

    /*
    private void readAtom(DataInputStream dis)
    {

        String id=ReadStringFromStream(dis,4);
        if(id=="STAT")
        else if(id=="IMTY")
        else if(id=="IPSR")
        else if(id=="IPDE")
        else if(id=="IPLS")
        else if(id=="MDAT")
        else if(id=="CVER")
        else if(id=="NAME")
        else if(id=="NALS")
        else if(id=="UACT")
    }
*/
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

                //  RecoderByMediaCodec recoderByMediaCodec=new RecoderByMediaCodec();
                //  recoderByMediaCodec.prepare();
                //  recoderByMediaCodec.startRecord();

                break;
            case CommonConfig.USER_ACTION_REJECT:
                break;
            case CommonConfig.USER_ACTION_QUIT:
                break;
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
                                Log.i(TAG, "RespFromServer ,0x494d0200 retcode " + Integer.toHexString(retcode));
                                Log.i(TAG, "RespFromServer ,0x494d0200 length " + Integer.toHexString(length));

                                if(retcode==0) {
/*
                                    dos.writeBytes("UACT");
                                    dos.writeInt(0x02000000);
                                    dos.writeByte(action & 0xff);
                                    dos.writeByte((action & 0xff00) >> 8);
*/
                                //"IPSR" LEN=4
                                    dis.skipBytes(8);
                                    int sip=dis.readInt();
                                    Log.i(TAG, "RespFromServer ,0x494d0200，ssip:"+Integer.toHexString(sip));

                                //"IPDE" len=4
                                    dis.skipBytes(12);

/*
                                    dos.writeBytes("UACT");
                                    dos.writeInt(0x02000000);
                                    dos.writeByte(action & 0xff);
                                    dos.writeByte((action & 0xff00) >> 8);
*/
                                    SetPeerIp(sip);
                                    //mPeerIp=sip;

                                    dis.skipBytes(8);
                                    int action=dis.readByte()&0xff;
                                    action+=(dis.readByte()&0xff)<<8;


                                    HandleAction(action);

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

                                /*
                                if(retcode==1)
                                    break;
*/
                                if(retcode==0) {
                                    length -= 20;
                                    dis.skipBytes(20);

                                    while (true) {
                                        if ((bufferread = dis.read(body, 0, length)) > 0) {

                                            //pipedOutputStream.write(body, 0, bufferread);
                                            //pipedOutputStream.flush();
                                            //feedandplay(body);
                                            Log.i(TAG, "RespFromServer,write to pipe,len:" + bufferread);
                                            Log.i(TAG, "RespFromServer,write to pipe,length:" + length);
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
                                    //StringBuffer sb = new StringBuffer();

                                    dis.skipBytes(4);//"NAME"

                                    nameleng+=dis.readByte()&0xff;
                                    nameleng+=(dis.readByte()<<8)&0xff00;
                                    nameleng+=(dis.readByte()<<16)&0xff0000;
                                    nameleng+=(dis.readByte()<<24)&0xff000000;
                                    Log.i(TAG, "resp ip list len:" + Integer.toHexString(nameleng));

                                    String sb = ReadStringFromStream(dis,nameleng);

                                    /*
                                    for(int j=0;j<nameleng;j++)
                                    {
                                       // sb.append(dis.read());
                                        sb.append(String.format("%c",dis.read()));
                                    }
                                    */

                                    Log.i(TAG, "resp ip list name:" + sb);

                                    DummyContent.DummyItem item=new DummyContent.DummyItem(Integer.toString(i) ,sb,ip[i]);

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
