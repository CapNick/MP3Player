package hird.nick.psynh1.mp3player;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.mpatric.mp3agic.*;

import java.io.File;
import java.io.IOException;


class MusicAdapter extends ArrayAdapter {


    MusicAdapter(Context context, File[] tracks) {
        super(context, R.layout.music_list ,tracks);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater musicInflater = LayoutInflater.from(getContext());
        View musicView = musicInflater.inflate(R.layout.music_list, parent, false);

        ImageView trackImage = (ImageView)musicView.findViewById(R.id.trackImage);
        TextView trackTitle = (TextView) musicView.findViewById(R.id.trackTitle);
        TextView trackArtist = (TextView) musicView.findViewById(R.id.trackArtist);
        TextView trackDuration = (TextView) musicView.findViewById(R.id.trackLength);

        try {
            Mp3File mp3File = new Mp3File((File) getItem(position));
            if (mp3File.hasId3v2Tag()) {
                trackTitle.setText(mp3File.getId3v2Tag().getTitle());
                trackArtist.setText(mp3File.getId3v2Tag().getArtist());
                try {
                    trackImage.setImageBitmap(getImage(mp3File));
                }catch (NullPointerException e){
                    Log.e("No Image found!: ", e.toString());
                }
            }else if(mp3File.hasId3v1Tag()){
                trackTitle.setText(mp3File.getId3v1Tag().getTitle());
                trackArtist.setText(mp3File.getId3v1Tag().getArtist());
            }else{
                Log.d("Error", "not found any meta data going with defaults");
                trackTitle.setText("Unkown");
                trackArtist.setText("Unkown");
            }
            int seconds = (int)mp3File.getLengthInSeconds() % 60;
            int minutes = (int)mp3File.getLengthInSeconds() / 60;

            trackDuration.setText(String.format("%02d:%02d",minutes,seconds));
        } catch (IOException | InvalidDataException | UnsupportedTagException e) {
            e.printStackTrace();
        }
        return musicView;
    }

    private Bitmap getImage(Mp3File track){
        byte[] byteImage = track.getId3v2Tag().getAlbumImage();
        return BitmapFactory.decodeByteArray(byteImage,0,byteImage.length);
    }

}
