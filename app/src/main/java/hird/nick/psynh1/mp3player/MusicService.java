package hird.nick.psynh1.mp3player;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;


public class MusicService extends Service {

    private final IBinder musicServiceBinder = new MusicServiceBinder();
    private final static int notificationID = 20442244;
    NotificationCompat.Builder notification;
    private MP3Player mp3Player;

    //Binder
    public class MusicServiceBinder extends Binder {
        MusicService getService(){
            return MusicService.this;
        }
    }

    public MusicService() {
        mp3Player = new MP3Player();
        notification = new NotificationCompat.Builder(this);
        setupMusicNotification();
    }

    public MP3Player getMp3Player(){
        return mp3Player;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicServiceBinder;
    }

    private void setupMusicNotification () {
        notification.setSmallIcon(R.mipmap.ic_launcher);
        notification.setTicker("");
    }


}
