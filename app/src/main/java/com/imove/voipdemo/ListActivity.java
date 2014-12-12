package com.imove.voipdemo;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.net.LocalSocketAddress;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.imove.voipdemo.ChatFragment;

import android.os.Build;

import com.imove.voipdemo.R;
import com.imove.voipdemo.audioManager.ServerSocket;
import com.imove.voipdemo.dummy.DummyContent;

public class ListActivity extends Activity implements ItemListFragment.OnFragmentInteractionListener,ChatFragment.OnChatFragmentInteractionListener
{
    ItemListFragment itemListFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        itemListFragment=new ItemListFragment();
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, itemListFragment)
                    .commit();
        }
    }

    @Override
    public void onFragmentInteraction(String id)
    {
       // int ip= DummyContent
        DummyContent.DummyItem item=DummyContent.getItem(id);
        int ip=item.ip;
        Log.d("aa","onFragmentInteraction,ip:"+ip);
        ServerSocket ss=ServerSocket.getServerSocketInstance();
        ss.SetPeerIp(ip);

        getFragmentManager().beginTransaction()
                .hide(itemListFragment)
                .add(R.id.container,new ChatFragment(),id)
                .commit();

    }
    @Override
    public void onChatFragmentInteraction()
    {
        Log.d("aa","cc:");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_itemlist_list, container, false);
            return rootView;
        }
    }
}
