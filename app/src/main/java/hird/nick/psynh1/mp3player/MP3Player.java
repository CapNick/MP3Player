package hird.nick.psynh1.mp3player;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import java.io.IOException;
import com.mpatric.mp3agic.*;

/**
 * Created by pszmdf on 06/11/16.
 * Modifiec by psynh1 on 10/11/16
 */
public class MP3Player {

    //lets make this a singleton shall we
//    private static final MP3Player instance = new MP3Player();


    protected MediaPlayer mediaPlayer;
    protected MP3PlayerState state;
    protected String filePath;
    protected Mp3File mp3File;

    public enum MP3PlayerState {
        ERROR,
        PLAYING,
        PAUSED,
        STOPPED
    }

//    public static MP3Player getInstance(){
//        return instance;
//    }

    public MP3Player() {
        this.state = MP3PlayerState.STOPPED;
    }

    public MP3PlayerState getState() {
        return this.state;
    }

    public void load(String filePath) {
        this.filePath = filePath;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mp3File = new Mp3File(filePath);
        } catch (InvalidDataException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedTagException e) {
            e.printStackTrace();
        }

        try{
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.e("MP3Player", e.toString());
            e.printStackTrace();
            this.state = MP3PlayerState.ERROR;
            return;
        } catch (IllegalArgumentException e) {
            Log.e("MP3Player", e.toString());
            e.printStackTrace();
            this.state = MP3PlayerState.ERROR;
            return;
        }

        this.state = MP3PlayerState.PLAYING;
        mediaPlayer.start();
    }

    public String getFilePath() {
        return this.filePath;
    }

    public int getProgress() {
        if(mediaPlayer!=null) {
            if(this.state == MP3PlayerState.PAUSED || this.state == MP3PlayerState.PLAYING)
                return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void play() {
        if(this.state == MP3PlayerState.PAUSED) {
            mediaPlayer.start();
            this.state = MP3PlayerState.PLAYING;
        }
    }

    public void pause() {
        if(this.state == MP3PlayerState.PLAYING) {
            mediaPlayer.pause();
            state = MP3PlayerState.PAUSED;
        }
    }
    //for a seekbar to get to a specific part of the song
    public void setProgress(int progress){
        if(mediaPlayer!=null) {
            if (this.state == MP3PlayerState.PLAYING || this.state == MP3PlayerState.PAUSED) {
                mediaPlayer.seekTo(progress);
            }
        }
    }

    public void stop() {
        if(mediaPlayer!=null) {
            if(mediaPlayer.isPlaying())
                mediaPlayer.stop();
            state = MP3PlayerState.STOPPED;
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

//    //--------------------------------------Expanded Features---------------------------------------
//
//    public long getLength(){
//        return mp3File.getLengthInSeconds();
//    }
//
//    public String getTitle(){
//        if (mp3File.hasId3v1Tag()){
//            return mp3File.getId3v1Tag().getTitle();
//        }
//        return "";
//    }
//
//    public String getArtist(){
//        if (mp3File.hasId3v1Tag()){
//            return mp3File.getId3v1Tag().getArtist();
//        }
//        return "";
//    }
//
//    public String getAlbum(){
//        if (mp3File.hasId3v1Tag()){
//            return mp3File.getId3v1Tag().getAlbum();
//        }
//        return "";
//    }
//
//    public byte[] getArtwork(){
//        if (mp3File.hasId3v2Tag()){
//            byte [] imageData = mp3File.getId3v2Tag().getAlbumImage();
//            if (imageData != null){
//                return imageData;
//            }
//        }
//        return null;
//    }
}