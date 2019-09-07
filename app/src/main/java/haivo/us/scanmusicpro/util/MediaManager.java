package haivo.us.scanmusicpro.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.SystemClock;

public class MediaManager {
    private static final MediaManager ourInstance = new MediaManager();
    private Context context;
    private MediaPlayer mediaPlayer;

    public static MediaManager getInstance() {
        return ourInstance;
    }

    private MediaManager() {
    }

    public void init(Context context){
        this.context = context;
    }
    public void destroy(){
        context = null;
    }

    public void playAutofocusSound() {
        try {
            releasePlayer();
            mediaPlayer = MediaPlayer.create(context, Uri.parse("/system/media/audio/ui/Auto_focus.ogg"));
            if (mediaPlayer != null) {
                int d = mediaPlayer.getDuration();
                mediaPlayer.start();
                SystemClock.sleep((long) d);
                releasePlayer();
            }
        } catch (Exception e) {
            releasePlayer();
        }
    }

    public void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}
