package haivo.us.scanmusicpro.util;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraUtils {
    private void LogCameraParameters(Camera.Parameters p) {
        Log.i("cameraParameters", "CameraActivity.LogCameraParameters");
        Log.i("cameraParameters", "Antibanding               " + p.getAntibanding());
        Log.i("cameraParameters", "Auto Exposure Lock        " + p.getAutoExposureLock());
        Log.i("cameraParameters", "Auto White Balance Lock   " + p.getAutoWhiteBalanceLock());
        Log.i("cameraParameters", "Color Effect              " + p.getColorEffect());
        Log.i("cameraParameters", "Exposure Comp             " + p.getExposureCompensation());
        Log.i("cameraParameters", "Exposure Comp Step        " + p.getExposureCompensationStep());
        Log.i("cameraParameters", "Flash Mode                " + p.getFlashMode());
        Log.i("cameraParameters", "Focal Length              " + p.getFocalLength());
        Log.i("cameraParameters", "Focus Areas               " + PrintCameraAreas(p.getFocusAreas()));
        Log.i("cameraParameters", "Focus Mode                " + p.getFocusMode());
        Log.i("cameraParameters", "Jpeg Quality              " + p.getJpegQuality());
        Log.i("cameraParameters", "Max Exposure Compensation " + p.getMaxExposureCompensation());
        Log.i("cameraParameters", "Max Num Focus Areas       " + p.getMaxNumFocusAreas());
        Log.i("cameraParameters", "Max Num Metering Areas    " + p.getMaxNumMeteringAreas());
        Log.i("cameraParameters", "Max Zoom                  " + p.getMaxZoom());
        Log.i("cameraParameters", "MeteringAreas             " + PrintCameraAreas(p.getMeteringAreas()));
        Log.i("cameraParameters", "Min Exposure Compensation " + p.getMinExposureCompensation());
        Camera.Size s = p.getPictureSize();
        Log.i("cameraParameters", "Picture Size              (" + s.width + "," + s.height + ")");
        Log.i("cameraParameters", "Scene mode                " + p.getSceneMode());
        Log.i("cameraParameters", "Supported Antibanding     " + PrintStringList(p.getSupportedAntibanding()));
        Log.i("cameraParameters", "Supported Color Effects   " + PrintStringList(p.getSupportedColorEffects()));
        Log.i("cameraParameters", "Supported Flash Modes     " + PrintStringList(p.getSupportedFlashModes()));
        Log.i("cameraParameters", "Supported Focus Modes     " + PrintStringList(p.getSupportedFocusModes()));
        Log.i("cameraParameters", "Supported Scene Modes     " + PrintStringList(p.getSupportedSceneModes()));
        Log.i("cameraParameters", "Supported White Balance   " + PrintStringList(p.getSupportedWhiteBalance()));
        Log.i("cameraParameters", "White Balance             " + p.getWhiteBalance());
        Log.i("cameraParameters", "Zoom                      " + p.getZoom());
    }

    public String PrintCameraAreas(List<Camera.Area> l) {
        if (l == null) {
            return "<null>";
        }
        String result = "";
        for (Camera.Area a : l) {
            result = result
                + ""
                + a.weight
                + "("
                + a.rect.left
                + ","
                + a.rect.bottom
                + ")("
                + a.rect.right
                + ","
                + a.rect.top
                + ")";
        }
        return result;
    }

    public String PrintStringList(List<String> l) {
        if (l == null) {
            return "<null>";
        }
        String result = "";
        for (String s : l) {
            result = result + (result == "" ? "" : ",") + s;
        }
        return result;
    }

    public static void writeImageToFile(Context context, byte[] data) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(FNS.getImageFileLocalInternal(context).getName(), 0);
            fos.write(data, 0, data.length);
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e2) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
            }
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
            }
        }
    }
}
