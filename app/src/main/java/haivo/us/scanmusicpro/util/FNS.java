package haivo.us.scanmusicpro.util;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout.LayoutParams;
import java.io.File;

public class FNS {
    public static final boolean AUTO_TEST = false;
    public static final String externalTempImageFilename = "playScoreImage";
    public static final String publicImageFilenameSuffix = "PlayScore_";
    private static String scoresDirectory = "/Camera/Scores";

    public static int min(int a, int b) {
        return a < b ? a : b;
    }

    public static int max(int a, int b) {
        return a > b ? a : b;
    }

    public static float square(float x) {
        return x * x;
    }

    public static LayoutParams rectMake(float x, float y, float w, float h) {
        LayoutParams p = new LayoutParams(1, 1);
        p.leftMargin = (int) x;
        p.topMargin = (int) y;
        p.width = (int) w;
        p.height = (int) h;
        return p;
    }

    public static void setRect(View v, LayoutParams rect) {
        v.setLayoutParams(rect);
    }

    public static void setRect(View v, float x, float y, float w, float h) {
        v.setLayoutParams(rectMake(x, y, w, h));
    }

    public static void setVisible(View v, boolean q) {
        v.setVisibility(q ? View.VISIBLE : View.GONE);
    }


    public static String getAbsolutePath(Activity activity, Uri contentUri) {
        String res = null;
        Cursor cursor = activity.getContentResolver().query(contentUri, new String[] { "_data" }, null, null, null);
        if (cursor.moveToFirst()) {
            res = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
        }
        cursor.close();
        return res;
    }

    public static boolean deleteFile(File f) {
        if (!f.exists()) {
            return false;
        }
        f.delete();
        return true;
    }

    public static File getImageFileLocalExternal(Context context) {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                                   .getAbsolutePath()
                                   .concat(scoresDirectory + "/" + externalTempImageFilename));
    }

    public static File getImageFileLocalInternal(Context context) {
        return new File(context.getFilesDir() + "/currentImage");
    }

    public static File getMidiFileLocal(Context context) {
        return new File(context.getFilesDir() + "/currentMidi");
    }

    public static File getXmlFileLocal(Context context) {
        return new File(context.getFilesDir() + "/currentXml");
    }

    public static File getImageFilePublic(String str) {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                                   .getAbsolutePath()
                                   .concat(scoresDirectory + "/" + publicImageFilenameSuffix + str + ".jpg"));
    }

    public static File getMidiFilePublic(String str) {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                                   .getAbsolutePath()
                                   .concat(scoresDirectory + "/" + publicImageFilenameSuffix + str + ".mid"));
    }

    public static File getXmlFilePublic(String str) {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                                   .getAbsolutePath()
                                   .concat(scoresDirectory + "/" + publicImageFilenameSuffix + str + ".xml"));
    }

    private static String getImageDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + scoresDirectory;
    }

    public static void ensureScoresDirectory() {
        File imagepath = new File(getImageDirectory());
        if (!imagepath.exists()) {
            imagepath.mkdirs();
        }
    }
}
