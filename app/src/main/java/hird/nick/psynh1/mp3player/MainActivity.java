package hird.nick.psynh1.mp3player;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Debug;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.mpatric.mp3agic.*;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ListView musicList;
    private File musicDir;
    private File[] list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initalizeWidgits();

    }



    private void initalizeWidgits(){
        musicList = (ListView) findViewById(R.id.musicList);
        try {
            musicDir = new File(Environment.getExternalStorageDirectory().getPath()+"/Music/");
            Log.d("Getting Music From", Environment.getExternalStorageDirectory().getPath()+"/Music/");
            list = musicDir.listFiles();

            musicList.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, list));
        }catch (NullPointerException e){
            Log.e("Error", e.toString());
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

}
