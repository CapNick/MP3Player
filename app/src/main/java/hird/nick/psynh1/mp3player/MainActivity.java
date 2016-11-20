package hird.nick.psynh1.mp3player;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.IOException;

import hird.nick.psynh1.mp3player.MusicService.MusicServiceBinder;


public class MainActivity extends AppCompatActivity {

    //Bound Service
    MusicService musicService;
    boolean isBound = false;

    //Music Tracks
    private ListView trackList;
    private SeekBar trackProgress;
    private TextView trackSelectedTitle;
    private TextView trackSelectedArtist;
    private TextView timeProgressed;
    private TextView timeTotal;

    //Private thread
    private Thread trackThread;
    boolean isSeekBarSelected = false;
    boolean isActivityActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeWidgets();
        Intent intent = new Intent(this, MusicService.class);
        if (!isBound){
            this.startService(intent);
            bindService(intent, musicConnection, Context.BIND_AUTO_CREATE);
        }
        Log.d("MainActivity", "onCreate"+musicService);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound){
            unbindService(musicConnection);
        }
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
        trackProgress = (SeekBar) findViewById(R.id.trackProgress);
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
//            trackList.setAdapter(new ArrayAdapter<File>(this, android.R.layout.simple_list_item_1, list)  );
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

        trackProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTimeText();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBarSelected = true;
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicService.getMp3Player().setProgress(seekBar.getProgress()*1000);
                isSeekBarSelected = false;
            }
        });

    }

    private void loadTrack(String trackPath) {
        musicService.getMp3Player().stop();
        musicService.getMp3Player().load(trackPath);
        if (musicService.getMp3Player().getState() != MP3Player.MP3PlayerState.ERROR){
            setupTrackInfo();
        }
    }

    private void setupTrackInfo() {
        try {
            Mp3File track = new Mp3File(musicService.getMp3Player().getFilePath());
            //Get the mp3's length in milliseconds and set the track progress maximum to it

            trackProgress.setMax((int) track.getLengthInSeconds());

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
                trackSelectedArtist.setText("Unknown");
            }
        } catch (IOException | UnsupportedTagException | NullPointerException | InvalidDataException e) {
            Log.e("Setup Track",e.toString());
        }
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicServiceBinder binder = (MusicServiceBinder) service;
            musicService = binder.getService();
            Log.d("MusicConnection", musicService.toString()+" Starting");
            isBound = true;
            setupProgressBarThread();
            setupTrackInfo();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    //Thread related stuff/////////////////////////////////////////////////////////////////////
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(!isSeekBarSelected){
                trackProgress.setProgress(musicService.getMp3Player().getProgress() / 1000);
                updateTimeText();
            }
            return true;
        }
    });

    private void updateTimeText(){
        int seconds = (trackProgress.getProgress()) % 60;
        int minutes = (trackProgress.getProgress()) / 60;
        timeProgressed.setText(String.format("%02d:%02d", minutes, seconds));
    }


    private void setupProgressBarThread(){
        Runnable runnableThread = new Runnable() {
            @Override
            public void run() {
                while (isActivityActive){
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
        Log.d("Thread:","Track Progress: "+trackThread.isAlive());
    }

}