package com.imove.voipdemo;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.imove.voipdemo.audioManager.FdManager;
import com.imove.voipdemo.audioManager.MediaPlayManager;
import com.imove.voipdemo.audioManager.RecorderManager;
import com.imove.voipdemo.audioManager.ServerSocket;

import java.lang.String;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatFragment.OnChatFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class ChatFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private VoiceTask mVoiceTask = null;

    private boolean longClicked=false;

    private OnChatFragmentInteractionListener mListener;
    public Button mVoiceBtn;
    public Button mStopBtn;
    private RecorderManager mRecorderManager;
    private FdManager mFdManager;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public ChatFragment() {
        mRecorderManager=new RecorderManager();
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_chat, container, false);

        mFdManager=new FdManager();
       // mFdManager.SetHost("172.16.2.32",9000);
        mVoiceBtn=(Button)view.findViewById(R.id.sayButton);
        /*
        mVoiceBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d("aa","ACTION_DOWN");
                   // mVoiceTask = new VoiceTask();
                   // mVoiceTask.execute((Void) null);
                    Log.d("aa","doInbackground:"+Thread.currentThread().getId());
                    String filepath="/sdcard/";
                    String filename="aaa.mp4";
                    mFdManager.SetFilePath(filepath,filename);
                    mRecorderManager.recorder(mFdManager.GetStreamSocket());

                    mFdManager.SendToServer();
                    mFdManager.ReceiveFromServer();

                    MediaPlayManager mediaPlayManager=new MediaPlayManager(mFdManager.GetFilePath());
                    mediaPlayManager.mediaPlay();
                }

                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    Log.d("aa","ACTION_UP");
                    //mVoiceTask.cancel(true);
                    mRecorderManager.stopRecorder();
                }
                return true;
            }
        });
        */

        mVoiceBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Log.d("aa","doInbackground:"+Thread.currentThread().getId());

                mRecorderManager.recorder(mFdManager.GetStreamSocket());

                ServerSocket ss=ServerSocket.getServerSocketInstance();
                ss.setLocalSocket(mFdManager.getReceiver());

                ss.SendAudioToServer();

            }
        });

        mStopBtn = (Button)view.findViewById(R.id.stopButton);
        mStopBtn.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v)
                {
                    mRecorderManager.stopRecorder();
                }
            }
        );


        Button mPlayBtn=(Button)view.findViewById(R.id.playButton);
        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onChatFragmentInteraction();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnChatFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnChatFragmentInteractionListener {
        // TODO: Update argument type and name

        public void onChatFragmentInteraction();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class VoiceTask extends AsyncTask<Void, Void, Boolean> {

        VoiceTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d("aa","doInbackground:"+Thread.currentThread().getId());

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Log.d("aa","onPostExecute,thread id:"+Thread.currentThread().getId());
        }

        @Override
        protected void onCancelled()
        {
            Log.d("aa","onCancelled:"+Thread.currentThread().getId());
        }
    }
}
 