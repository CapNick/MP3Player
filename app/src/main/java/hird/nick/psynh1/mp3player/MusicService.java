package hird.nick.psynh1.mp3player;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;


public class MusicService extends Service {

    private final IBinder musicServiceBinder = new MusicServiceBinder();
    private static String LOG_TAG = "BoundMusicService";
    private final static int notificationID = 20442244;
    NotificationCompat.Builder notification;
    NotificationManager notificationManager;
    private MP3Player mp3Player;

    //Binder
    public class MusicServiceBinder extends Binder {
        MusicService getService(){
            return MusicService.this;
        }
    }

    public MusicService() {
        mp3Player = new MP3Player();
    }

    public MP3Player getMp3Player(){
        return mp3Player;
    }

    public NotificationManager getNotificationManager(){
        return notificationManager;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        setupMusicNotification();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationID, notification.build());
        return musicServiceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.v(LOG_TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "in onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "in onDestroy");
    }

    private void setupMusicNotification () {
        Intent resultIntent = new Intent(this, MainActivity.class);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this,0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        notification = new NotificationCompat.Builder(this);
        notification.setSmallIcon(R.mipmap.ic_launcher);
        notification.setTicker("MP3P");
        notification.setContentTitle("Mp3Player");

        notification.setContentText("Touch this to return to the app");

        notification.setContentIntent(resultPendingIntent);

    }


}
