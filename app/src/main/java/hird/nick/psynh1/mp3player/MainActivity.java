package hird.nick.psynh1.mp3player;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
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


    //Bound Service
    MusicService musicService;
    boolean isBound = false;

    //Music Tracks
    private ListView trackList;
    private ProgressBar trackProgress;
    private TextView trackSelectedTitle;
    private TextView trackSelectedArtist;
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
        if (!isBound){
            bindService(intent, musicConnection, Context.BIND_AUTO_CREATE);
        }

        setupProgressBarThread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    //Play Button
    public void playTrackButton(View v){
        musicService.getMp3Player().play();
    }
    //Pause Button
    public void pauseTrackButton(View v){
        musicService.getMp3Player().pause();
    }

    public void stopTrackButton(View v){
        stopTrack();
    }

    //Stop Button
    private void stopTrack(){
        musicService.getMp3Player().stop();
//        clearing the playing track from the itemlist
        trackSelectedTitle.setText("");
        trackSelectedArtist.setText("");
        timeProgressed.setText("00:00");
        timeTotal.setText("00:00");
    }

    private void initializeWidgets(){
        File musicDir;
        File[] list;
        trackList = (ListView) findViewById(R.id.trackList);
        trackProgress = (ProgressBar) findViewById(R.id.trackProgress);
        trackSelectedTitle = (TextView) findViewById(R.id.trackTitle);
        trackSelectedArtist = (TextView) findViewById(R.id.currentArtist);
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
                trackProgress.setMax((int) track.getLengthInMilliseconds());

                int seconds = (int) track.getLengthInSeconds() % 60;
                int minutes = (int) track.getLengthInSeconds() / 60;
                timeTotal.setText(String.format("%02d:%02d", minutes, seconds));

                if (track.hasId3v1Tag()){
                    trackSelectedTitle.setText(track.getId3v1Tag().getTitle());
                    trackSelectedArtist.setText(track.getId3v1Tag().getArtist());
                }

                else if (track.hasId3v2Tag()){
                    trackSelectedTitle.setText(track.getId3v2Tag().getTitle());
                    trackSelectedArtist.setText(track.getId3v2Tag().getArtist());
                }

                else {
                    trackSelectedTitle.setText("Unknown");
                }
            } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                e.printStackTrace();
            }
        }
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
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(musicService.getMp3Player().getProgress() < trackProgress.getMax()){
                trackProgress.setProgress(musicService.getMp3Player().getProgress());
                int seconds = (musicService.getMp3Player().getProgress()/1000) % 60;
                int minutes = (musicService.getMp3Player().getProgress()/1000) / 60;
                timeProgressed.setText(String.format("%02d:%02d", minutes, seconds));
            }else{
                stopTrack();
            }
            return true;
        }
    });

    private void setupProgressBarThread(){
        Runnable runnableThread = new Runnable() {
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
            trackThread = new Thread(runnableThread);
            trackThread.start();
        }
        Log.d("Thread:","Track Progress: "+trackThread.getName());
    }
}
