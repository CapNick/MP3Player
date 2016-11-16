package hird.nick.psynh1.mp3player;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import java.io.File;
import java.io.IOException;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import hird.nick.psynh1.mp3player.MusicService.MusicServiceBinder;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;


public class MainActivity extends AppCompatActivity {


    //Service
    MusicService musicService;
    boolean isBound = false;

    //Music Tracks
    private ListView trackList;
    private ProgressBar trackProgrss;
    private TextView trackSelectedName;
    private TextView timeProgressed;
    private TextView timeTotal;

    //Private thread
    private Thread trackThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeWidgets();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, musicConnection, Context.BIND_AUTO_CREATE);
        setupProgressBar();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(isBound){
            musicService.getMp3Player().stop();
            unbindService(musicConnection);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //Play Button
    public void playTrack(View v){
        musicService.getMp3Player().play();
        if(trackThread != null && musicService.getMp3Player().getState() == MP3Player.MP3PlayerState.PAUSED){
            Log.d("Thread: ","resuming for play");
            trackThread.notify();
        }
    }
    //Pause Button
    public void pauseTrack(View v){
        musicService.getMp3Player().pause();
        if(trackThread != null && musicService.getMp3Player().getState() == MP3Player.MP3PlayerState.PLAYING){
            try {
                Log.d("Thread: ","waiting due to stop");
                trackThread.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
    //Stop Button
    public void stopTrack(View v){
        trackSelectedName.setText("None");
        musicService.getMp3Player().stop();
        if(trackThread != null && musicService.getMp3Player().getState() == MP3Player.MP3PlayerState.PLAYING) {
            //checking if the payer is either playing  so we dont try pausing the
            // thread while its already paused for nothing
            try {
                Log.d("Thread:"," waiting due to stop");
                trackThread.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void initializeWidgets(){
        File musicDir;
        File[] list;
        trackList = (ListView) findViewById(R.id.trackList);
        trackProgrss = (ProgressBar) findViewById(R.id.trackProgress);
        trackSelectedName = (TextView) findViewById(R.id.trackTitle);
        timeProgressed = (TextView) findViewById(R.id.timeProgressed);
        timeTotal = (TextView) findViewById(R.id.timeTotal);

        try {
            musicDir = new File(Environment.getExternalStorageDirectory().getPath()+"/Music/");
            Log.d("Getting Music From", Environment.getExternalStorageDirectory().getPath()+"/Music/");

            list = musicDir.listFiles();
            ListAdapter musicAdapter = new MusicAdapter(this, list);

            trackList.setAdapter(musicAdapter);
            trackList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                    File selectedFromList =(File) (trackList.getItemAtPosition(myItemInt));
                    Log.d("g53mdp", selectedFromList.getAbsolutePath());
                    loadTrack(selectedFromList.getAbsolutePath());
                }
            });

        }catch (NullPointerException e){
            Log.e("Error", e.toString());
        }
    }

    private void loadTrack(String trackPath) {
        //Get the mp3's length in milliseconds and set the track progress maximum to it

        musicService.getMp3Player().stop();
        musicService.getMp3Player().load(trackPath);
        if (musicService.getMp3Player().getState() != MP3Player.MP3PlayerState.ERROR){
            try {
                Mp3File track = new Mp3File(trackPath);
                trackProgrss.setMax((int) track.getLengthInMilliseconds());
                timeTotal.setText(""+track.getLengthInMilliseconds()/1000);
                if (track.hasId3v1Tag()){
                    trackSelectedName.setText(track.getId3v1Tag().getTitle());
                }
                else if (track.hasId3v2Tag()){
                    trackSelectedName.setText(track.getId3v2Tag().getTitle());
                }
                else {
                    trackSelectedName.setText("Unknown");
                }
            } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                e.printStackTrace();
            }
        }
        trackThread.start();
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicServiceBinder binder = (MusicServiceBinder) service;
            musicService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    //Thread related stuff
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            trackProgrss.setProgress(musicService.getMp3Player().getProgress());
            timeProgressed.setText(""+musicService.getMp3Player().getProgress()/1000);
        }
    };

    private void setupProgressBar(){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(500);
                        handler.sendEmptyMessage(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        if(trackThread == null){
            trackThread = new Thread(r);
            Log.d("Thread:","Track Progress: "+trackThread.getName());
        }
    }
}
