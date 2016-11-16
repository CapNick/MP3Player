package hird.nick.psynh1.mp3player;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


public class MusicService extends Service {

    //Binder
    public class MusicServiceBinder extends Binder {
        MusicService getService(){
            return MusicService.this;
        }
    }

    //The rest of the service
    private final IBinder musicServiceBinder = new MusicServiceBinder();
    private MP3Player mp3Player;

    public MusicService() {
        mp3Player = new MP3Player();
    }

    public MP3Player getMp3Player(){
        return mp3Player;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicServiceBinder;
    }



}
