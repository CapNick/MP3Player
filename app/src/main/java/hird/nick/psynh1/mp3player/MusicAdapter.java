package hird.nick.psynh1.mp3player;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.mpatric.mp3agic.*;

import java.io.File;
import java.io.IOException;


public class MusicAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private File [] trackss;
    private Activity activity;

    private class Track {
        ImageView image;
        TextView title;
        TextView artist;
        TextView length;
    }

    public MusicAdapter(Activity activity, File[] tracks){
        this.activity = activity;
        trackss = tracks;
    }

    @Override
    public int getCount() {
        return trackss.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Track track = new Track();
        Mp3File file;


        if(convertView==null){
            view = inflater.inflate(R.layout.music_list, parent);
        }
        // initializing each of the track parts
        track.title = (TextView)activity.findViewById(R.id.trackTitle);
        track.artist = (TextView)activity.findViewById(R.id.trackArtist);
        track.length = (TextView)activity.findViewById(R.id.trackLength);


        try {
            file = new Mp3File(trackss[position].getAbsolutePath());
            track.title.setText(file.getId3v2Tag().getTitle());
            track.artist.setText(file.getId3v2Tag().getArtist());
            track.length.setText(String.format("%d's",file.getLength()));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedTagException e) {
            e.printStackTrace();
        } catch (InvalidDataException e) {
            e.printStackTrace();
        }

        // setting all of the parts

        return view;
    }



    private Bitmap getAlbumImage(Mp3File file){
        if(file.hasId3v2Tag()){
            byte[] image = file.getId3v2Tag().getAlbumImage();
            if(image != null){
                return BitmapFactory.decodeByteArray(image,0,32);
            }
        }
        return null;
    }

}
