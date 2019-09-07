package haivo.us.scanmusicpro.util;

import android.app.Activity;
import android.graphics.drawable.StateListDrawable;
import android.widget.ImageButton;
import android.widget.ImageView;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Helper {
    public static ImageButton createButton(Activity context, boolean enabled, int down, int up) {
        ImageButton button = new ImageButton(context);
        setImages(context, button, down, up);
        button.setBackgroundColor(0);
        button.setScaleType(ImageView.ScaleType.FIT_CENTER);
        button.setPadding(0, 0, 0, 0);
        button.setEnabled(enabled);
        return button;
    }

    public static void setImages(Activity activity, ImageButton button, int down, int up) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[] { 16842919 }, activity.getResources().getDrawable(down));
        states.addState(new int[0], activity.getResources().getDrawable(up));
        button.setImageDrawable(states);
    }

    public static void setImage(ImageButton button, int image) {
        button.setImageResource(image);
    }


    public static String getTimeString() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }
}
