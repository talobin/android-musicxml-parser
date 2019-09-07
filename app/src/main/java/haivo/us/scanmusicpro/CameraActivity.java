package haivo.us.scanmusicpro;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import haivo.us.scanmusicpro.util.CameraUtils;
import haivo.us.scanmusicpro.util.FNS;
import haivo.us.scanmusicpro.util.Helper;
import haivo.us.scanmusicpro.util.MediaManager;
import java.util.List;

import static android.graphics.PixelFormat.TRANSLUCENT;

public class CameraActivity extends Activity implements Callback, SensorEventListener {

    //region Property
    private float[] acceleration = new float[6];
    private ImageButton buttonBottom = null;
    private ImageButton buttonLeft = null;
    private ImageButton buttonRight = null;
    private ImageButton buttonTop = null;
    private Camera camera = null;
    private int cameraOrientation;
    private int currentAngle = -1;
    private float[] current_acceleration = new float[3];
    private float[] gravity = new float[3];
    private boolean hasChanged = false;
    private int imageAngle;
    private boolean isContinuousFocus = false;
    private boolean isPreview = false;
    private float[] linear_acceleration = new float[3];
    private Sensor mAccelerometer;
    private SensorManager mSensorManager;
    private OrientationEventListener orientationListener;
    private int portraitOrientation;
    private RelativeLayout root = null;
    private boolean safeToTakePhoto = true;
    private int screenHeight;
    private int screenWidth;
    private int side;
    private Boolean started = false;
    private int surfaceOrientation;
    private SurfaceView surface_view = null;
    //endregion

    //region Life-Cycle
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFormat(TRANSLUCENT);
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        if (this.screenWidth > screenHeight) {
            screenWidth = size.y;
            screenHeight = size.x;
        }
        float shortest = screenHeight < screenWidth ? (float) screenHeight : (float) screenWidth;
        getWindowManager().getDefaultDisplay().getMetrics(new DisplayMetrics());
        side = 128;
        if (((double) (shortest / ((float) side))) < 5.0d) {
            side = (int) (shortest / 5.0f);
        }
        surface_view = new SurfaceView(this);
        root = new RelativeLayout(this);
        root.addView(this.surface_view, new LayoutParams(-1, -1));
        setContentView(this.root);
        surface_view.getHolder().addCallback(this);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(1);
        orientationListener = new OrientationEventListener(this) {
            public void onOrientationChanged(int orientation) {
                int angle;
                if (orientation != -1) {
                    if (orientation >= 330 || orientation <= 30) {
                        angle = 0;
                    } else if (orientation >= 60 && orientation <= 120) {
                        angle = 90;
                    } else if (orientation >= 150 && orientation <= 210) {
                        angle = 180;
                    } else if (orientation >= 240 && orientation <= 300) {
                        angle = 270;
                    } else {
                        return;
                    }
                    if (CameraActivity.this.currentAngle != angle) {
                        CameraActivity.this.currentAngle = angle;
                        CameraActivity.this.imageAngle =
                            ((CameraActivity.this.currentAngle + 360) + CameraActivity.this.cameraOrientation) % 360;
                        CameraActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                CameraActivity.this.layoutUI();
                            }
                        });
                    }
                }
            }
        };
        if (this.orientationListener.canDetectOrientation()) {
            orientationListener.enable();
        }
        buttonLeft = Helper.createButton(this, true, R.drawable.take_picture_down_r, R.drawable.take_picture_up_r);
        buttonRight = Helper.createButton(this, true, R.drawable.take_picture_down_l, R.drawable.take_picture_up_l);
        buttonTop = Helper.createButton(this, true, R.drawable.take_picture_down_t, R.drawable.take_picture_up_t);
        buttonBottom = Helper.createButton(this, true, R.drawable.take_picture_down_b, R.drawable.take_picture_up_b);
        setFrame(this.buttonLeft, 90);
        setFrame(this.buttonRight, 270);
        setFrame(this.buttonTop, 180);
        setFrame(this.buttonBottom, 0);
        root.addView(this.buttonLeft);
        root.addView(this.buttonRight);
        root.addView(this.buttonTop);
        root.addView(this.buttonBottom);
        OnClickListener listener = new OnClickListener() {
            public void onClick(View v) {
                hideButtons();
                autoFocus();
            }
        };
        buttonLeft.setOnClickListener(listener);
        buttonRight.setOnClickListener(listener);
        buttonTop.setOnClickListener(listener);
        buttonBottom.setOnClickListener(listener);
    }

    @Override
    public void onPause() {
        MediaManager.getInstance().releasePlayer();
        setPreview(false);
        if (this.camera != null) {
            camera.release();
            camera = null;
            setResult(0, new Intent());
            finish();
        }
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        MediaManager.getInstance().init(this);
        mSensorManager.registerListener(this, mAccelerometer, 3);
        if (started) {
            try {
                OpenCamera();
                camera.setPreviewDisplay(this.surface_view.getHolder());
                setPreview(true);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder != null) {
            try {
                OpenCamera();
                if (this.camera != null) {
                    camera.setPreviewDisplay(holder);
                    started = Boolean.valueOf(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (!this.hasChanged) {
            hasChanged = true;
            setCameraOrientation();
            Parameters p = camera.getParameters();
            p.setPictureFormat(256);
            p.setRotation(this.surfaceOrientation);
            Size psize = getLargestPictureSize(p);
            p.setPictureSize(psize.width, psize.height);
            List<String> flash = p.getSupportedFlashModes();
            if (flash != null && flash.contains("auto")) {
                p.setFlashMode("auto");
            }
            List<String> modes = p.getSupportedFocusModes();
            if (modes != null) {
                if (modes.contains("continuous-picture")) {
                    isContinuousFocus = true;
                    p.setFocusMode("continuous-picture");
                } else if (modes.contains("auto")) {
                    isContinuousFocus = false;
                    p.setFocusMode("auto");
                }
            }
            p.setJpegQuality(100);
            camera.setParameters(p);
            setPreview(true);
        }
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        current_acceleration[0] = event.values[0];
        current_acceleration[1] = event.values[1];
        current_acceleration[2] = event.values[2];
        gravity[0] = (this.gravity[0] * 0.8f) + (event.values[0] * 0.19999999f);
        gravity[1] = (this.gravity[1] * 0.8f) + (event.values[1] * 0.19999999f);
        gravity[2] = (this.gravity[2] * 0.8f) + (event.values[2] * 0.19999999f);
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    //endregion

    //region UI Helper
    public void showButton(int angle) {
        ImageButton b;
        hideButtons();
        switch (angle) {
            case 90:
                b = buttonRight;
                break;
            case 180:
                b = buttonTop;
                break;
            case 270:
                b = buttonLeft;
                break;
            default:
                b = buttonBottom;
                break;
        }
        if (b != null) {
            b.setEnabled(true);
            b.clearAnimation();
            Animation fade = new AlphaAnimation(0.0f, 1.0f);
            fade.setInterpolator(new DecelerateInterpolator());
            fade.setStartOffset(0);
            fade.setDuration(1200);
            b.startAnimation(fade);
            b.setAlpha(1.0f);
        }
    }

    public void hideButtons() {
        buttonLeft.setAlpha(0.0f);
        buttonRight.setAlpha(0.0f);
        buttonTop.setAlpha(0.0f);
        buttonBottom.setAlpha(0.0f);
        buttonLeft.setEnabled(false);
        buttonRight.setEnabled(false);
        buttonTop.setEnabled(false);
        buttonBottom.setEnabled(false);
    }

    public void layoutUI() {
        if (safeToTakePhoto) {
            showButton(((720 - currentAngle) - portraitOrientation) % 360);
        }
    }
    //endregion

    //region Camera Settings Helper
    private void setCameraOrientation() {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(0, info);
        cameraOrientation = info.orientation;
        portraitOrientation = getDeviceRotation();
        surfaceOrientation = (info.orientation + portraitOrientation) % 360;
        int displayOrientation = ((info.orientation - portraitOrientation) + 360) % 360;
        if (this.camera != null) {
            camera.setDisplayOrientation(displayOrientation);
        }
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentAngle = extras.getInt("startAngle");
            imageAngle = ((this.currentAngle + 360) + cameraOrientation) % 360;
            layoutUI();
        }
        runOnUiThread(new Runnable() {
            public void run() {
                CameraActivity.this.layoutUI();
            }
        });
    }

    private void setPreview(boolean set) {
        if (set) {
            if (!isPreview) {
                camera.startPreview();
                isPreview = true;
                safeToTakePhoto = true;
            }
        } else if (this.isPreview) {
            camera.stopPreview();
            isPreview = false;
        }
    }

    private void setFrame(ImageButton b, int angle) {
        float w = ((float) side) * 2.0f;
        float h = ((float) side) * 2.0f;
        switch (angle) {
            case 90:
                FNS.setRect(b, ((float) screenWidth) - h, (((float) screenHeight) * 0.5f) - (0.5f * w), h, w);
                return;
            case 180:
                FNS.setRect(b, (((float) screenWidth) * 0.5f) - (w * 0.5f), 0.0f, w, h);
                return;
            case 270:
                FNS.setRect(b, 0.0f, (((float) screenHeight) * 0.5f) - (w * 0.5f), h, w);
                return;
            default:
                FNS.setRect(b, (((float) screenWidth) * 0.5f) - (w * 0.5f), ((float) screenHeight) - (1.2f * h), w, h);
                return;
        }
    }

    private int getDeviceRotation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case 0:
                return 0;
            case 1:
                return 90;
            case 2:
                return 180;
            case 3:
                return 270;
            default:
                return 0;
        }
    }

    private Size getBestPreviewSize(int width, int height, Parameters parameters) {
        Size result = null;
        for (Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else if (size.width * size.height > result.width * result.height) {
                    result = size;
                }
            }
        }
        return result;
    }

    private Size getLargestPictureSize(Parameters parameters) {
        Size result = null;
        int area = 0;
        for (Size size : parameters.getSupportedPictureSizes()) {
            if (result == null) {
                result = size;
                area = size.width * size.height;
            } else if (result.width * result.height > area) {
                result = size;
                area = size.width * size.height;
            }
        }
        return result;
    }
    //endregion

    //region Camera Action
    private void OpenCamera() {
        try {
            camera = Camera.open();
        } catch (Exception e) {
            Log.i("camera", "OpenCamera exception: " + e.getMessage());
        }
    }

    private void autoFocus() {
        if (this.camera != null && safeToTakePhoto) {
            safeToTakePhoto = false;
            if (this.isContinuousFocus) {
                MediaManager.getInstance().playAutofocusSound();
                takePhoto();
                return;
            }
            camera.autoFocus(new AutoFocusCallback() {
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        MediaManager.getInstance().playAutofocusSound();
                        takePhoto();
                        return;
                    }
                    safeToTakePhoto = true;
                    layoutUI();
                }
            });
        }
    }

    private void takePhoto() {
        Parameters p = camera.getParameters();
        p.setRotation(((this.currentAngle + 360) + cameraOrientation) % 360);
        camera.setParameters(p);
        camera.takePicture(new ShutterCallback() {
            public void onShutter() {
                acceleration[0] = current_acceleration[0];
                acceleration[1] = current_acceleration[1];
                acceleration[2] = current_acceleration[2];
                acceleration[3] = gravity[0];
                acceleration[4] = gravity[1];
                acceleration[5] = gravity[2];
            }
        }, null, null, new PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                if (data == null) {
                    safeToTakePhoto = true;
                    showButton(currentAngle);
                    return;
                }
                try {
                    String timeString = Helper.getTimeString();
                    CameraUtils.writeImageToFile(CameraActivity.this, data);
                    Intent result = new Intent();
                    result.putExtra("timeString", timeString);
                    result.putExtra("orientation", CameraActivity.this.imageAngle);
                    result.putExtra("acceleration", CameraActivity.this.acceleration);
                    CameraActivity.this.setResult(-1, result);
                    CameraActivity.this.finish();
                } catch (Exception e) {
                }
            }
        });
    }
    //endregion
}
