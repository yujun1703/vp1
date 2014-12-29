package com.imove.voipdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioStream;
import android.net.rtp.RtpStream;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.imove.voipdemo.R;
import com.imove.voipdemo.audioManager.AudioDecoderPlayer;
import com.imove.voipdemo.audioManager.AudioEncoder;
import com.imove.voipdemo.audioManager.StreamManager;

import com.imove.voipdemo.config.CommonConfig;

public class SessionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        Intent intent = this.getIntent();
       // AudioCodec
       // RtpStream
      //  AudioStream

        String a=(String)intent.getSerializableExtra("send");
        Log.d("aa","intent ddd:"+a);
        if(intent.getSerializableExtra("send")!=null)
        {
            //ServerSocket.getServerSocketInstance().getConnectionFsm().
            StreamManager.getServerSocketInstance().CreateSession(CommonConfig.USER_ACTION_REQUEST);


            FrameLayout frameLayout=new FrameLayout(this);


            setContentView(frameLayout);
            Button btn1 = new Button(this);
            btn1.setText("正在呼叫xxx");
            FrameLayout.LayoutParams pp=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
          //  btn1.setId(1);

            frameLayout.addView(btn1,pp);
        }
        else {

            Dialog dialog = new AlertDialog.Builder(this).setTitle("新的来电").setMessage("是否接受xxx的来电？").setPositiveButton("接受", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Log.d("aa", "bb");
                            StreamManager.getServerSocketInstance().CreateSession(CommonConfig.USER_ACTION_AGREE);


                          //  AudioDecoderPlayer audioPlayer=new AudioDecoderPlayer();
                          //  audioPlayer.setPlayer();


                            AudioEncoder recoderByMediaCodec=new AudioEncoder();
                            recoderByMediaCodec.SetOnSendDataListener(StreamManager.getServerSocketInstance());
                            recoderByMediaCodec.prepare();
                            recoderByMediaCodec.startRecord();

                        }
                    }
            ).setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    StreamManager.getServerSocketInstance().CreateSession(CommonConfig.USER_ACTION_REJECT);
                }
            }).create();
            dialog.show();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
