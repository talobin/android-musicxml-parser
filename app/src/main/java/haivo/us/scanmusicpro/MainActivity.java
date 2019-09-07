package haivo.us.scanmusicpro;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.MediaStore.Images.Media;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;
import haivo.us.scanmusicpro.model.BarIndexPlusInfo;
import haivo.us.scanmusicpro.model.BuildConfig;
import haivo.us.scanmusicpro.util.FNS;
import haivo.us.scanmusicpro.util.Helper;
import haivo.us.scanmusicpro.util.XMLUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import uk.co.dolphin_com.rscore.BarInfo;
import uk.co.dolphin_com.rscore.ConvertCallback;
import uk.co.dolphin_com.rscore.ConvertOptions;
import uk.co.dolphin_com.rscore.ProgressInfo;
import uk.co.dolphin_com.rscore.RScore;
import uk.co.dolphin_com.rscore.RScore.Orientation;
import uk.co.dolphin_com.rscore.ex.ConvertFailedException;
import uk.co.dolphin_com.rscore.ex.RScoreException;
import uk.co.dolphin_com.rscore.ex.TooManyStaffsException;

public class MainActivity extends Activity {

    public boolean afterLoop;
    Runnable allowCancelRunnable;
    public Bitmap bitmapFull;
    private Bitmap bitmapScaled;
    public ImageButton buttonCamera;
    private ImageButton buttonCrotchet;
    public ImageButton buttonGallery;
    public ImageButton buttonImage;
    private OnClickListener buttonListener;
    public ImageButton buttonMetronome;
    private ImageButton buttonNoNotes;
    public ImageButton buttonPlay;
    public ImageButton buttonProcess;
    private ImageButton buttonProgressBackground;
    private ImageButton buttonProgressBar;
    private ImageButton buttonProgressOutline;
    public ImageButton buttonSave;
    public ImageButton buttonSlider;
    public ImageButton buttonStop;
    public ImageButton buttonTrack;
    public ImageButton buttonVersion;
    public Date canCancelTime;
    private int currentProgress;
    private Point cursorPoint;

    public float dx;
    public float dy;
    public boolean filesNeedSaving;
    private float gap;
    public float imageHeight;
    public String imageName;
    public boolean imageNeedsSaving;
    public float imageWidth;
    private TextView labelSave;
    private TextView labelTempo;
    private TextView labelVersion;
    public boolean loopActive;
    public int loopEnd;
    public int loopEndWas;
    public int loopStart;
    public MediaPlayer mediaPlayer = new MediaPlayer();
    public boolean noNotesFlag;
    private Thread playThread;
    public boolean playerConfig;
    public boolean playing;
    public ProcessThread processThread;
    private LayoutParams progressRect;
    RelativeLayout root;
    private float scaleHeight = 1.0f;
    private float scaleWidth = 1.0f;
    private float screenHeight;
    public float screenWidth;
    public float shortest;
    public float side;
    public float sliderDrag;
    public float sliderFrom;
    public float sliderLevel;
    public State state;
    public boolean stopped;
    public float tempo;
    public boolean tempoMode;
    public File theFile;
    private float theRatio;
    public RScore theScore = null;
    public boolean tooManyStaffsDialog;
    public boolean tooManyStaffsFlag;
    private float totalHeight;
    private float totalWidth;
    public int touchBeat;
    public boolean useSystemCamera = false;
    public int versionButtonClickCount = 0;
    public ViewCursor viewCursor;
    private ViewCursor viewDebug;
    public WebView webView;

    private class PlayThread extends Thread {
        private RScore rscore;

        public PlayThread(RScore rscore2) {
            this.rscore = rscore2;
        }

        public void run() {
            while (true) {
                if (playing) {
                    MainActivity.this.updateCursor(true);
                    try {
                        Thread.sleep(20, 0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void finalize() {
            this.rscore = null;
        }
    }

    private class ProcessThread extends Thread {

        Bitmap bm;
        public boolean cancelled = false;
        Context context;

        ProcessThread(Context c, Bitmap bm) {
            this.context = c;
            this.bm = bm;
            setPriority(1);
        }

        public void cancel() {
            this.cancelled = true;
        }

        public void run() {
            try {
                int bitmapWidth = bm.getWidth();
                int bitmapHeight = bm.getHeight();
                int[] pixels = new int[(bitmapWidth * bitmapHeight)];
                bm.getPixels(pixels, 0, this.bm.getWidth(), 0, 0, this.bm.getWidth(), this.bm.getHeight());
                File midiFile = FNS.getMidiFileLocal(this.context);
                File xmlFile = FNS.getXmlFileLocal(this.context);
                midiFile.delete();
                xmlFile.delete();
                ConvertOptions rscore_opt = new ConvertOptions("PlayScore", false, false);
                theScore = RScore.Convert(pixels,
                                          bitmapWidth,
                                          bitmapHeight,
                                          bm.getWidth(),
                                          Orientation.ROT0,
                                          new ConvertCallback() {
                                              public boolean progress(final ProgressInfo info) {
                                                  runOnUiThread(new Runnable() {
                                                      public void run() {
                                                          if (!ProcessThread.this.cancelled) {
                                                              setCurrentProgress(info.progress_percent);
                                                          }
                                                      }
                                                  });
                                                  if (cancelled) {
                                                      return false;
                                                  }
                                                  try {
                                                      Thread.sleep(10);
                                                      return true;
                                                  } catch (InterruptedException e) {
                                                      return false;
                                                  }
                                              }
                                          },
                                          midiFile,
                                          xmlFile,
                                          rscore_opt);
                if (midiFile.exists()) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            processingComplete();
                        }
                    });
                }
                if (theScore.getNumBars() < 0) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            noNotesFlag = true;
                            processingFailed();
                        }
                    });
                }
            } catch (ConvertFailedException e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        processingCancelled();
                    }
                });
            } catch (TooManyStaffsException e2) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        tooManyStaffsFlag = true;
                        tooManyStaffsDialog = true;
                        processingFailed();
                    }
                });
            } catch (RScoreException e3) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        processingCancelled();
                    }
                });
            }
        }

        public void finalize() {
            this.bm = null;
        }
    }

    /* renamed from: haivo.us.scanmusicpro.MainActivity$State */
    private enum State {
        SPLASH, INIT, IMAGE, PROCESSING, READY
    }

    public class ViewBracket extends View {
        public Point bottomLeft = new Point(0, 20);
        public Point bottomRight = new Point(20, 20);
        public int colour = Color.parseColor("#aa008800");
        public float fraction;
        public ViewGroup.LayoutParams layoutParams;
        public Rect rect = new Rect(10, 10, 50, 50);
        public Point topLeft = new Point(0, 0);
        public Point topRight = new Point(20, 0);

        public ViewBracket(Context context) {
            super(context);
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Paint paint = new Paint();
            paint.setStrokeWidth(6.0f);
            paint.setStyle(Style.STROKE);
            paint.setColor(this.colour);
            canvas.drawLine((float) this.topLeft.x,
                            (float) this.topLeft.y,
                            (float) this.topRight.x,
                            (float) this.topRight.y,
                            paint);
            canvas.drawLine((float) this.topRight.x,
                            (float) this.topRight.y,
                            (float) this.bottomRight.x,
                            (float) this.bottomRight.y,
                            paint);
            canvas.drawLine((float) this.bottomRight.x,
                            (float) this.bottomRight.y,
                            (float) this.bottomLeft.x,
                            (float) this.bottomLeft.y,
                            paint);
        }
    }

    public class ViewCursor extends View {
        public Point bottomLeft = new Point(0, 20);
        public Point bottomRight = new Point(20, 20);
        public int colour = Color.parseColor("#ffff3333");
        public float fraction;
        public ViewGroup.LayoutParams layoutParams;
        public Rect rect = new Rect(10, 10, 50, 50);
        public Point topLeft = new Point(0, 0);
        public Point topRight = new Point(20, 0);

        public ViewCursor(Context context) {
            super(context);
        }

        public void setCoords(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
            this.topLeft.x = (int) x1;
            this.topLeft.y = (int) y1;
            this.topRight.x = (int) x2;
            this.topRight.y = (int) y2;
            this.bottomRight.x = (int) x3;
            this.bottomRight.y = (int) y3;
            this.bottomLeft.x = (int) x4;
            this.bottomLeft.y = (int) y4;
            Point start = new Point(this.topLeft.x, this.topLeft.y);
            if (this.topRight.y < start.y) {
                start.y = this.topRight.y;
            }
            Point end = new Point(this.bottomRight.x, this.bottomLeft.y);
            if (this.bottomRight.y > end.y) {
                end.y = MainActivity.this.viewCursor.bottomRight.y;
            }
            this.layoutParams = FNS.rectMake(x1, y1, (x2 - x1) + 2.0f, (float) ((end.y - start.y) + 2));
            this.topLeft.x -= start.x - 1;
            this.topLeft.y -= start.y - 1;
            this.topRight.x -= start.x;
            this.topRight.y -= start.y - 1;
            this.bottomRight.x -= start.x;
            this.bottomRight.y -= start.y;
            this.bottomLeft.x -= start.x - 1;
            this.bottomLeft.y -= start.y;
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Paint paint = new Paint();
            paint.setStrokeWidth(2.0f);
            paint.setStyle(Style.STROKE);
            paint.setColor(this.colour);
            canvas.drawLine((float) this.topLeft.x,
                            (float) this.topLeft.y,
                            (float) this.topRight.x,
                            (float) this.topRight.y,
                            paint);
            canvas.drawLine((float) this.topRight.x,
                            (float) this.topRight.y,
                            (float) this.bottomRight.x,
                            (float) this.bottomRight.y,
                            paint);
            canvas.drawLine((float) this.bottomRight.x,
                            (float) this.bottomRight.y,
                            (float) this.bottomLeft.x,
                            (float) this.bottomLeft.y,
                            paint);
            canvas.drawLine((float) this.bottomLeft.x,
                            (float) this.bottomLeft.y,
                            (float) this.topLeft.x,
                            (float) this.topLeft.y,
                            paint);
            float x = ((float) (this.topRight.x - this.topLeft.x)) * this.fraction;
            canvas.drawLine(x, (float) this.bottomLeft.y, x, (float) this.topLeft.y, paint);
        }
    }

    static int access$2704(MainActivity x0) {
        int i = x0.versionButtonClickCount + 1;
        x0.versionButtonClickCount = i;
        return i;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(1024, 1024);
        System.loadLibrary("stlport_shared");
        if (BuildConfig.FLAVOR.equals("Lite")) {
            System.loadLibrary("ReadScoreLib_Lite");
        } else {
            System.loadLibrary("ReadScoreLib");
        }
        this.useSystemCamera = getSharedPreferences("PlayScorePrefs", 0).getBoolean("useSystemCamera", false);
        setContentView(R.layout.activity_main);
        this.root = (RelativeLayout) findViewById(R.id.mainLayout);
        setButtonAction();
        int med = BuildConfig.FLAVOR.equals("Lite") ? R.drawable.medlite : R.drawable.medpro;
        this.buttonVersion = createButton(true, med, med);
        this.labelVersion = new TextView(this);
        this.root.addView(this.labelVersion);
        this.buttonSave = createButton(true, R.drawable.button_save_down, R.drawable.button_save_up);
        this.labelSave = new TextView(this);
        this.labelSave.setTextSize(20.0f);
        this.labelSave.setText("Saved");
        this.labelSave.setTextColor(Color.parseColor("#ccffffff"));
        this.root.addView(this.labelSave);
        this.buttonCamera = createButton(true, R.drawable.button_camera_down, R.drawable.button_camera_up);
        this.buttonGallery = createButton(true, R.drawable.button_gallery_down, R.drawable.button_gallery_up);
        this.buttonImage = createButton(true, R.drawable.app_logo, R.drawable.app_logo);
        this.buttonProcess = createButton(true, R.drawable.button_process_down, R.drawable.button_process_up);
        this.buttonNoNotes = createButton(false, R.drawable.nomusic, R.drawable.nomusic);
        this.buttonPlay = createButton(true, R.drawable.button_play_down, R.drawable.button_play_up);
        this.buttonStop = createButton(true, R.drawable.button_stop_down, R.drawable.button_stop_up);
        this.buttonImage.setSoundEffectsEnabled(false);
        this.buttonCamera.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                showCameraInterfaceDialog();
                return true;
            }
        });
        this.webView = new WebView(this);
        this.webView.loadDataWithBaseURL("", getTooManyStavesHtml(this), "text/html", "utf-8", "");
        this.webView.getSettings().setLoadWithOverviewMode(true);
        this.webView.getSettings().setUseWideViewPort(true);
        this.root.addView(this.webView);
        this.labelTempo = new TextView(this);
        this.root.addView(this.labelTempo);
        this.labelTempo.setBackgroundColor(Color.parseColor("#ccffffff"));
        this.labelTempo.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        this.labelTempo.setGravity(16);
        this.buttonMetronome = createButton(true, R.drawable.button_metronome_down, R.drawable.button_metronome_up);
        this.buttonCrotchet = createButton(false, R.drawable.crotchet, R.drawable.crotchet);
        this.buttonProgressBackground =
            createButton(false, R.drawable.progressbar_background, R.drawable.progressbar_background);
        this.buttonProgressBar = createButton(false, R.drawable.progressbar_bar, R.drawable.progressbar_bar);
        this.buttonProgressOutline =
            createButton(false, R.drawable.progressbar_outline, R.drawable.progressbar_outline);
        this.buttonProgressBackground.setScaleType(ScaleType.FIT_XY);
        this.buttonProgressBar.setScaleType(ScaleType.FIT_XY);
        this.buttonProgressOutline.setScaleType(ScaleType.FIT_XY);
        this.viewCursor = new ViewCursor(this);
        this.viewCursor.setBackgroundColor(0);
        FNS.setRect(this.viewCursor, -3.0f, -3.0f, 1.0f, 1.0f);
        this.cursorPoint = new Point(-5, -5);
        this.root.addView(this.viewCursor);
        this.buttonTrack = createButton(false, R.drawable.slider_background, R.drawable.slider_background);
        this.buttonTrack.setScaleType(ScaleType.FIT_XY);
        this.buttonTrack.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (v == MainActivity.this.buttonTrack && event.getAction() == 0) {
                    float centre =
                        ((float) MainActivity.this.buttonSlider.getLeft()) + (MainActivity.this.side * 0.75f);
                    if (event.getRawX() < centre) {
                        MainActivity.this.updateTempo(MainActivity.this.tempo - 1.0f);
                    } else {
                        MainActivity.this.updateTempo(MainActivity.this.tempo + 1.0f);
                    }
                    MainActivity.this.updateTempoLabel(MainActivity.this.tempo);
                    MainActivity.this.sliderLevel =
                        (centre - (0.5f * (MainActivity.this.screenWidth - (MainActivity.this.shortest * 0.7f)))) / (
                            MainActivity.this.shortest
                                * 0.7f);
                    if (MainActivity.this.sliderLevel < 0.0f) {
                        MainActivity.this.sliderLevel = 0.0f;
                    } else if (MainActivity.this.sliderLevel > 1.0f) {
                        MainActivity.this.sliderLevel = 1.0f;
                    }
                    MainActivity.this.layoutSlider();
                }
                return false;
            }
        });
        this.buttonSlider = createButton(true, R.drawable.slider_marker, R.drawable.slider_marker);
        this.buttonSlider.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (v == MainActivity.this.buttonSlider) {
                    if (event.getAction() == 0) {
                        MainActivity.this.sliderDrag = event.getRawX();
                        MainActivity.this.sliderFrom = (float) MainActivity.this.buttonSlider.getLeft();
                    } else if (event.getAction() == 2) {
                        MainActivity.this.sliderLevel =
                            (((MainActivity.this.sliderFrom + ((float) ((int) (event.getRawX()
                                - MainActivity.this.sliderDrag)))) + (MainActivity.this.side * 0.75f))
                                - ((MainActivity.this.screenWidth - (MainActivity.this.shortest * 0.7f)) * 0.5f)) / (
                                MainActivity.this.shortest
                                    * 0.7f);
                        if (MainActivity.this.sliderLevel < 0.0f) {
                            MainActivity.this.sliderLevel = 0.0f;
                        } else if (MainActivity.this.sliderLevel > 1.0f) {
                            MainActivity.this.sliderLevel = 1.0f;
                        }
                        MainActivity.this.updateTempoLabel(MainActivity.this.sliderProgressToTempo(MainActivity.this.sliderLevel
                                                                                                       * 100.0f));
                        MainActivity.this.layoutSlider();
                    } else if (event.getAction() == 1) {
                        MainActivity.this.sliderLevel =
                            (((MainActivity.this.sliderFrom + ((float) ((int) (event.getRawX()
                                - MainActivity.this.sliderDrag)))) + (MainActivity.this.side * 0.75f))
                                - ((MainActivity.this.screenWidth - (MainActivity.this.shortest * 0.7f)) * 0.5f)) / (
                                MainActivity.this.shortest
                                    * 0.7f);
                        if (MainActivity.this.sliderLevel < 0.0f) {
                            MainActivity.this.sliderLevel = 0.0f;
                        } else if (MainActivity.this.sliderLevel > 1.0f) {
                            MainActivity.this.sliderLevel = 1.0f;
                        }
                        MainActivity.this.updateTempo(MainActivity.this.sliderProgressToTempo(MainActivity.this.sliderLevel
                                                                                                  * 100.0f));
                        MainActivity.this.updateTempoLabel(MainActivity.this.tempo);
                        MainActivity.this.layoutSlider();
                    }
                }
                return false;
            }
        });
        FNS.setVisible(this.labelVersion, false);
        FNS.setVisible(this.labelSave, false);
        FNS.setVisible(this.labelTempo, false);
        this.webView.bringToFront();
        sendViewToBack(this.buttonProgressOutline);
        sendViewToBack(this.buttonProgressBar);
        sendViewToBack(this.buttonProgressBackground);
        sendViewToBack(this.viewCursor);
        sendViewToBack(this.buttonImage);
        setImageAction();
        this.mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer md) {
                float beat = (float) ((int) MainActivity.this.getCurrentBeat());
                if (!MainActivity.this.loopActive || ((float) MainActivity.this.loopStart) != beat) {
                    MainActivity.this.rewindAndStop();
                    MainActivity.this.setState(MainActivity.this.state);
                    return;
                }
                MainActivity.this.mediaPlayer.seekTo((int) (((((float) MainActivity.this.loopStart) * 60.0f)
                    / MainActivity.this.tempo) * 1.0f));
                MainActivity.this.mediaPlayer.start();
            }
        });
        this.mediaPlayer.reset();
        this.root.post(new Runnable() {
            public void run() {
                MainActivity.this.calculateOffsets();
                MainActivity.this.layoutUI();
                MainActivity.this.setState(State.SPLASH);
                MainActivity.this.buttonImage.setImageResource(R.drawable.app_logo);
                FNS.setVisible(MainActivity.this.buttonImage, true);
                Animation fade = new AlphaAnimation(1.0f, 0.0f);
                fade.setInterpolator(new DecelerateInterpolator());
                fade.setStartOffset(2500);
                fade.setDuration(550);
                MainActivity.this.buttonImage.startAnimation(fade);
            }
        });
        this.root.postDelayed(new Runnable() {
            public void run() {
                MainActivity.this.setState(State.INIT);
            }
        }, 3000);

        XMLUtils.parseTestXML();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.mediaPlayer != null
            && this.mediaPlayer.isPlaying()
            && !((PowerManager) getSystemService(POWER_SERVICE)).isScreenOn()) {
            stopPlaying();
            setState(this.state);
            resetCursor();
            updateCursor(false);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.buttonImage.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            public void onGlobalLayout() {
                MainActivity.this.calculateOffsets();
                MainActivity.this.resetCursor();
                MainActivity.this.updateCursor(true);
                MainActivity.this.updateBrackets();
                if (MainActivity.this.buttonImage.getViewTreeObserver().isAlive()) {
                    MainActivity.this.buttonImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
        calculateOffsets();
        layoutUI();
        resetCursor();
        updateCursor(true);
        updateBrackets();
        if (this.state == State.PROCESSING) {
            setCurrentProgress(this.currentProgress);
        }
        setState(this.state);
    }

    /* access modifiers changed from: protected */
    public void calculateOffsets() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        this.totalWidth = (float) size.x;
        this.totalHeight = (float) size.y;
        this.screenWidth = this.totalWidth * this.scaleWidth;
        this.screenHeight = this.totalHeight * this.scaleHeight;
        if (this.viewDebug != null) {
            this.viewDebug.setCoords(0.0f,
                                     0.0f,
                                     this.screenWidth,
                                     0.0f,
                                     this.screenWidth,
                                     this.screenHeight,
                                     0.0f,
                                     this.screenHeight);
        }
        float w = this.screenWidth;
        float h = this.screenHeight;
        if (this.theRatio > h / w) {
            this.imageWidth = h / this.theRatio;
            this.imageHeight = h;
        } else {
            this.imageWidth = w;
            this.imageHeight = this.theRatio * w;
        }
        this.dx = (w - this.imageWidth) * 0.5f;
        this.dy = (h - this.imageHeight) * 0.5f;
    }

    public void layoutUI() {
        this.shortest = this.screenHeight < this.screenWidth ? this.screenHeight : this.screenWidth;
        getWindowManager().getDefaultDisplay().getMetrics(new DisplayMetrics());
        this.side = 128.0f;
        this.gap = 0.0f;
        float num = this.shortest / this.side;
        if (((double) num) < 5.0d) {
            this.side = this.shortest / 5.0f;
        } else if (((double) num) > 5.0d) {
            this.gap = 0.25f * (this.shortest - (this.side * 5.0f));
        }
        float bot = this.gap;
        float top = (this.side * 0.6f) + (this.gap * 0.5f);
        if (this.viewDebug != null) {
            FNS.setRect(this.viewDebug, 0.0f, 0.0f, this.screenWidth, this.screenHeight);
            this.viewDebug.invalidate();
        }
        FNS.setRect(this.buttonVersion,
                    (this.screenWidth * 0.5f) - (this.side * 1.5f),
                    this.screenHeight - (this.side * 0.8f),
                    this.side * 3.0f,
                    this.side * 0.7f);
        FNS.setRect(this.labelVersion,
                    (this.screenWidth * 0.5f) - (this.side * 0.75f),
                    this.screenHeight - (this.side * 0.46f),
                    this.side * 1.25f,
                    this.side * 0.2f);
        this.labelVersion.setText("version " + getVersionName());
        this.labelVersion.setTextColor(Color.parseColor("#ccffffff"));
        this.labelVersion.setTextSize(0, this.side * 0.16f);
        this.labelVersion.setGravity(19);
        FNS.setRect(this.buttonSave, (this.screenWidth - this.side) - this.gap, this.gap, this.side, this.side);
        FNS.setRect(this.labelSave, (this.screenWidth - this.side) - this.gap, this.gap, this.side, this.side);
        FNS.setRect(this.labelSave,
                    (this.screenWidth * 0.5f) - (this.side * 0.75f),
                    this.screenHeight * 0.1f,
                    this.side * 1.25f,
                    this.side * 0.5f);
        this.labelSave.setGravity(49);
        FNS.setRect(this.buttonCamera, this.gap, (this.screenHeight - (this.side * 1.1f)) - bot, this.side, this.side);
        FNS.setRect(this.buttonGallery,
                    (this.screenWidth - this.side) - this.gap,
                    (this.screenHeight - (this.side * 1.1f)) - bot,
                    this.side,
                    this.side);
        float margin = this.side * 0.5f;
        FNS.setRect(this.webView, margin, 0.0f, this.screenWidth - (2.0f * margin), this.screenHeight);
        FNS.setRect(this.buttonImage, 0.0f, 0.0f, this.screenWidth, this.screenHeight);
        FNS.setRect(this.buttonProcess,
                    (this.screenWidth * 0.5f) - (this.side * 0.5f),
                    (this.screenHeight - (this.side * 1.1f)) - top,
                    this.side,
                    this.side);
        FNS.setRect(this.buttonNoNotes,
                    (this.screenWidth * 0.5f) - (this.side * 1.0f),
                    (this.screenHeight * 0.5f) - (this.side * 1.0f),
                    this.side * 2.0f,
                    this.side * 2.0f);
        FNS.setRect(this.buttonPlay,
                    (this.screenWidth * 0.5f) - (this.side * 1.07f),
                    (this.screenHeight - (this.side * 1.1f)) - top,
                    this.side,
                    this.side);
        FNS.setRect(this.buttonStop,
                    (this.screenWidth * 0.5f) + (this.side * 0.07f),
                    (this.screenHeight - (this.side * 1.1f)) - top,
                    this.side,
                    this.side);
        FNS.setRect(this.buttonMetronome,
                    (this.screenWidth - this.side) - this.gap,
                    (this.screenHeight - (this.side * 1.1f)) - top,
                    this.side,
                    this.side);
        float w = this.shortest * 0.92f;
        float h = w / 7.125f;
        this.progressRect = FNS.rectMake((this.screenWidth * 0.5f) - (this.shortest * 0.46f),
                                         (this.screenHeight * 0.5f) - (0.5f * h),
                                         w,
                                         h);
        FNS.setRect(this.buttonProgressOutline, this.progressRect);
        FNS.setRect(this.buttonProgressBar, this.progressRect);
        FNS.setRect(this.buttonProgressBackground, this.progressRect);
        FNS.setRect(this.buttonTrack,
                    (this.screenWidth * 0.5f) - ((this.shortest * 0.7f) * 0.5f),
                    (this.screenHeight * 0.5f) - (this.side * 0.75f),
                    this.shortest * 0.7f,
                    this.side * 1.5f);
        FNS.setRect(this.labelTempo,
                    (this.screenWidth * 0.5f) + (this.shortest * 0.37f),
                    (this.screenHeight * 0.5f) - (this.side * 0.2f),
                    this.shortest * 0.12f,
                    this.side * 0.4f);
        FNS.setRect(this.buttonCrotchet,
                    (this.screenWidth * 0.5f) + (this.shortest * 0.38f),
                    (this.screenHeight * 0.5f) - (this.side * 0.15f),
                    this.shortest * 0.03f,
                    this.side * 0.3f);
        this.labelTempo.setTextSize(0, this.shortest * 0.024f);
        layoutSlider();
    }

    public void setState(State newState) {
        boolean z;
        boolean z2;
        boolean z3;
        boolean z4;
        boolean gallery;
        boolean z5;
        boolean z6;
        boolean z7;
        boolean z8;
        boolean z9;
        boolean z10;
        boolean z11;
        boolean z12;
        boolean z13;
        boolean z14;
        boolean z15 = true;
        boolean isLite = BuildConfig.FLAVOR.equals("Lite");
        this.state = newState;
        switch (this.state) {
            case INIT:
                clearProcessedData();
                FNS.setRect(this.viewCursor, -3.0f, -3.0f, 1.0f, 1.0f);
                clearBitmap();
                break;
            case IMAGE:
                clearProcessedData();
                break;
        }
        Helper.setImages(this,
                         this.buttonPlay,
                         this.playing ? R.drawable.button_pause_down : R.drawable.button_play_down,
                         this.playing ? R.drawable.button_pause_up : R.drawable.button_play_up);
        Helper.setImages(this,
                         this.buttonStop,
                         this.stopped ? R.drawable.button_stop_grey : R.drawable.button_stop_up,
                         this.stopped ? R.drawable.button_stop_grey : R.drawable.button_stop_up);
        Helper.setImages(this,
                         this.buttonMetronome,
                         R.drawable.button_metronome_down,
                         this.tempoMode ? R.drawable.button_metronome_down : R.drawable.button_metronome_up);
        if (this.noNotesFlag || this.tooManyStaffsFlag) {
            Helper.setImage(this.buttonProcess, R.drawable.button_process_grey);
        } else {
            Helper.setImages(this,
                             this.buttonProcess,
                             this.state == State.IMAGE ? R.drawable.button_process_down : R.drawable.button_cancel_down,
                             this.state == State.IMAGE ? R.drawable.button_process_up : R.drawable.button_cancel_up);
        }
        ImageButton imageButton = this.buttonImage;
        if (this.state == State.READY || this.state == State.IMAGE || this.tooManyStaffsDialog) {
            z = true;
        } else {
            z = false;
        }
        imageButton.setEnabled(z);
        ImageButton imageButton2 = this.buttonStop;
        if (!this.stopped) {
            z2 = true;
        } else {
            z2 = false;
        }
        imageButton2.setEnabled(z2);
        ImageButton imageButton3 = this.buttonProcess;
        if (this.noNotesFlag || this.tooManyStaffsFlag) {
            z3 = false;
        } else {
            z3 = true;
        }
        imageButton3.setEnabled(z3);
        FNS.setVisible(this.webView, this.tooManyStaffsDialog);
        if (this.tooManyStaffsDialog) {
            this.root.postDelayed(new Runnable() {
                public void run() {
                    MainActivity.this.webView.scrollTo(0, 0);
                }
            }, 10);
        }
        FNS.setVisible(this.buttonVersion, this.state != State.SPLASH);
        TextView textView = this.labelVersion;
        if (this.state != State.SPLASH) {
            z4 = true;
        } else {
            z4 = false;
        }
        FNS.setVisible(textView, z4);
        FNS.setVisible(this.buttonSave, !isLite && this.state == State.READY && this.filesNeedSaving);
        FNS.setVisible(this.labelSave, !isLite && this.state == State.READY && !this.filesNeedSaving);
        if (this.tooManyStaffsFlag || this.tooManyStaffsDialog || !(this.state == State.INIT
            || this.state == State.IMAGE)) {
            gallery = false;
        } else {
            gallery = true;
        }
        if (isLite) {
        }
        ImageButton imageButton4 = this.buttonCamera;
        if (this.tooManyStaffsDialog || !(this.state == State.INIT || this.state == State.IMAGE)) {
            z5 = false;
        } else {
            z5 = true;
        }
        FNS.setVisible(imageButton4, z5);
        ImageButton imageButton5 = this.buttonProcess;
        if (this.state == State.IMAGE || this.state == State.PROCESSING) {
            z6 = true;
        } else {
            z6 = false;
        }
        FNS.setVisible(imageButton5, z6);
        ImageButton imageButton6 = this.buttonNoNotes;
        if (this.state != State.IMAGE || !this.noNotesFlag) {
            z7 = false;
        } else {
            z7 = true;
        }
        FNS.setVisible(imageButton6, z7);
        ImageButton imageButton7 = this.buttonGallery;
        if (isLite || !gallery) {
            z8 = false;
        } else {
            z8 = true;
        }
        FNS.setVisible(imageButton7, z8);
        ImageButton imageButton8 = this.buttonPlay;
        if (this.state == State.READY) {
            z9 = true;
        } else {
            z9 = false;
        }
        FNS.setVisible(imageButton8, z9);
        ImageButton imageButton9 = this.buttonStop;
        if (this.state == State.READY) {
            z10 = true;
        } else {
            z10 = false;
        }
        FNS.setVisible(imageButton9, z10);
        ImageButton imageButton10 = this.buttonMetronome;
        if (this.state == State.READY) {
            z11 = true;
        } else {
            z11 = false;
        }
        FNS.setVisible(imageButton10, z11);
        ImageButton imageButton11 = this.buttonProgressBackground;
        if (this.state == State.PROCESSING) {
            z12 = true;
        } else {
            z12 = false;
        }
        FNS.setVisible(imageButton11, z12);
        ImageButton imageButton12 = this.buttonProgressBar;
        if (this.state == State.PROCESSING) {
            z13 = true;
        } else {
            z13 = false;
        }
        FNS.setVisible(imageButton12, z13);
        ImageButton imageButton13 = this.buttonProgressOutline;
        if (this.state == State.PROCESSING) {
            z14 = true;
        } else {
            z14 = false;
        }
        FNS.setVisible(imageButton13, z14);
        FNS.setVisible(this.buttonSlider, this.tempoMode);
        FNS.setVisible(this.buttonTrack, this.tempoMode);
        FNS.setVisible(this.buttonCrotchet, this.tempoMode);
        FNS.setVisible(this.labelTempo, this.tempoMode);
        ViewCursor viewCursor2 = this.viewCursor;
        if (this.state != State.READY || this.stopped) {
            z15 = false;
        }
        FNS.setVisible(viewCursor2, z15);
    }
    public void layoutSlider() {
        FNS.setRect(this.buttonSlider,
                    (((this.screenWidth * 0.5f) - ((this.shortest * 0.5f) * 0.7f)) + ((this.sliderLevel * this.shortest)
                        * 0.7f)) - (this.side * 0.75f),
                    (this.screenHeight * 0.5f) - (this.side * 0.75f),
                    this.side * 1.5f,
                    this.side * 1.5f);
    }
    public void resetCursor() {
        this.cursorPoint.x = -5;
        this.cursorPoint.y = -5;
    }
    public void updateCursor(boolean useMainThread) {
        if (this.theScore != null && this.mediaPlayer != null && !this.playerConfig) {
            int totalBars = this.theScore.getNumBars();
            BarInfo lastBarInfo = this.theScore.getBarInfo(totalBars - 1);
            try {
                int beat = (int) (((float) (lastBarInfo.startBeat + lastBarInfo.numBeats))
                    * (((float) this.mediaPlayer.getCurrentPosition()) / ((float) this.mediaPlayer.getDuration())));
                BarIndexPlusInfo info = BarIndexPlusInfo.forBeat(beat, totalBars, this.theScore);
                if (info != null) {
                    if (!(info.barInfo.leftBarline.base.x == this.cursorPoint.x
                        && info.barInfo.leftBarline.base.y == this.cursorPoint.y)) {
                        this.cursorPoint.x = info.barInfo.leftBarline.base.x;
                        this.cursorPoint.y = info.barInfo.leftBarline.base.y;
                        BarInfo b = info.barInfo;
                        this.viewCursor.setCoords(bitmapxToScreen((float) b.leftBarline.base.x),
                                                  bitmapyToScreen(((float) (b.leftBarline.base.y
                                                      + b.leftBarline.height)) - 20.0f),
                                                  bitmapxToScreen((float) b.rightBarline.base.x),
                                                  bitmapyToScreen(((float) (b.rightBarline.base.y
                                                      + b.rightBarline.height)) - 20.0f),
                                                  bitmapxToScreen((float) b.rightBarline.base.x),
                                                  bitmapyToScreen(((float) b.rightBarline.base.y) - 20.0f),
                                                  bitmapxToScreen((float) b.leftBarline.base.x),
                                                  bitmapyToScreen(((float) b.leftBarline.base.y) - 20.0f));
                        if (useMainThread) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    MainActivity.this.viewCursor.setLayoutParams(MainActivity.this.viewCursor.layoutParams);
                                    MainActivity.this.viewCursor.fraction = 0.0f;
                                    MainActivity.this.viewCursor.invalidate();
                                }
                            });
                        } else {
                            this.viewCursor.setLayoutParams(this.viewCursor.layoutParams);
                            this.viewCursor.fraction = 0.0f;
                            this.viewCursor.invalidate();
                        }
                    }
                    final float barFraction =
                        ((float) (beat - info.barInfo.startBeat)) / ((float) info.barInfo.numBeats);
                    if (useMainThread) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                MainActivity.this.viewCursor.fraction = barFraction;
                                MainActivity.this.viewCursor.invalidate();
                            }
                        });
                        return;
                    }
                    this.viewCursor.fraction = barFraction;
                    this.viewCursor.invalidate();
                }
            } catch (Exception e) {
            }
        }
    }
    public void setCurrentProgress(int percent) {
        this.currentProgress = percent;
        FNS.setRect(this.buttonProgressBar,
                    ((float) this.progressRect.leftMargin) + (((float) this.progressRect.width) * 0.0789f),
                    (float) this.progressRect.topMargin,
                    ((float) this.progressRect.width) * (1.0f - (2.2f * 0.0789f)) * (((float) percent) / 100.0f),
                    (float) this.progressRect.height);
    }

    private void showProDialog() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0);
        scroll.setPadding(0, 0, 0, 0);
        FNS.setRect(scroll,
                    0.0f,
                    this.screenWidth,
                    (this.screenHeight * 0.5f) - (this.screenHeight * 0.25f),
                    this.screenHeight * 0.5f);
        Builder builder = new Builder(scroll.getContext());
        String str =
            "Too many staffs for Diana Lite.<br><br>For more staffs and other great features please upgrade to <a href=\"http://www.google.com\">PlayScore Pro</a>.<br><br>";
        builder.setTitle("Diana Lite")
               .setMessage(Html.fromHtml(
                   "Too many staffs for Diana Lite.<br><br>For more staffs and other great features please upgrade to <a href=\"http://www.google.com\">PlayScore Pro</a>.<br><br>"))
               .setCancelable(true)
               .setPositiveButton("View", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                   }
               })
               .setNegativeButton("Not now", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                   }
               });
        builder.create().show();
    }
    public void showCameraInterfaceDialog() {
        ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("simple text",
                                                                                                      "\n\n{device="
                                                                                                          + (Build.MANUFACTURER
                                                                                                          + ","
                                                                                                          + Build.MODEL)
                                                                                                          .toUpperCase()
                                                                                                          + "}\n\n"));
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0);
        scroll.setPadding(0, 0, 0, 0);
        FNS.setRect(scroll,
                    0.0f,
                    this.screenWidth,
                    (this.screenHeight * 0.5f) - (this.screenHeight * 0.25f),
                    this.screenHeight * 0.5f);
        Builder builder = new Builder(scroll.getContext());
        builder.setTitle("")
               .setMessage("\nChoose Camera\n")
               .setCancelable(true)
               .setPositiveButton("System Camera", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       MainActivity.this.useSystemCamera = true;
                       Editor editor = MainActivity.this.getSharedPreferences("PlayScorePrefs", 0).edit();
                       editor.putBoolean("useSystemCamera", MainActivity.this.useSystemCamera);
                       editor.commit();
                       MainActivity.this.dispatchSystemCamera();
                   }
               })
               .setNegativeButton("Diana Camera", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       MainActivity.this.useSystemCamera = false;
                       Editor editor = MainActivity.this.getSharedPreferences("PlayScorePrefs", 0).edit();
                       editor.putBoolean("useSystemCamera", MainActivity.this.useSystemCamera);
                       editor.commit();
                       MainActivity.this.dispatchPlayscoreCamera();
                   }
               });
        AlertDialog alert = builder.create();
        alert.show();
        float textSize = this.shortest * 0.026f;
        Button b1 = alert.getButton(-1);
        Button b2 = alert.getButton(-2);
        if (!(b1 == null || b2 == null)) {
            b1.setTransformationMethod(null);
            b2.setTransformationMethod(null);
            b1.setAllCaps(false);
            b2.setAllCaps(false);
            b1.setTextSize(0, textSize);
            b2.setTextSize(0, textSize);
        }
        TextView messageView = (TextView) alert.findViewById(16908299);
        messageView.setTextSize(0, textSize);
        messageView.setGravity(17);
        Button b = alert.getButton(this.useSystemCamera ? -1 : -2);
        if (b != null) {
            b.setTextColor(Color.parseColor("#0000ff"));
        }
    }
    public void showVersionsDialog() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0);
        scroll.setPadding(0, 0, 0, 0);
        FNS.setRect(scroll,
                    0.0f,
                    this.screenWidth,
                    (this.screenHeight * 0.5f) - (this.screenHeight * 0.25f),
                    this.screenHeight * 0.5f);
        Builder builder = new Builder(scroll.getContext());
        builder.setTitle("Diana" + getVersionName() + " (build " + "11" + ") ReadScoreLib " + RScore.getVersion()
                                                                                                    .toString())
               .setMessage("deviceStr = '" + (Build.MANUFACTURER + "," + Build.MODEL).toLowerCase() + "'")
               .setCancelable(false)
               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                   }
               });
        builder.create().show();
    }
    File xmlTo;
    private void setButtonAction() {
        this.buttonListener = new OnClickListener() {
            public void onClick(View v) {
                boolean z = true;
                if (v == MainActivity.this.buttonVersion) {
                    MainActivity.access$2704(MainActivity.this);
                    if (MainActivity.this.versionButtonClickCount >= 5) {
                        MainActivity.this.versionButtonClickCount = 0;
                        MainActivity.this.showVersionsDialog();
                    }
                } else if (v == MainActivity.this.buttonGallery) {
                    //MainActivity.this.dispatchGallery();
                    XMLUtils.parseXML(xmlTo);
                } else if (v == MainActivity.this.buttonCamera) {
                    if (MainActivity.this.useSystemCamera) {
                        MainActivity.this.dispatchSystemCamera();
                    } else {
                        MainActivity.this.dispatchPlayscoreCamera();
                    }
                } else if (v == MainActivity.this.buttonSave) {
                    if (MainActivity.this.theFile != null && MainActivity.this.filesNeedSaving) {
                        if (MainActivity.this.imageNeedsSaving) {
                            try {
                                File imageFrom = FNS.getImageFileLocalInternal(MainActivity.this);
                                File imageTo = FNS.getImageFilePublic(MainActivity.this.imageName);
                                MainActivity.this.copyFile(imageFrom, imageTo);
                                MainActivity.this.galleryAddPic(imageTo);
                                MainActivity.this.imageNeedsSaving = false;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            File midiFrom = FNS.getMidiFileLocal(MainActivity.this);
                            File xmlFrom = FNS.getXmlFileLocal(MainActivity.this);
                            File midiTo = FNS.getMidiFilePublic(MainActivity.this.imageName);
                             xmlTo = FNS.getXmlFilePublic(MainActivity.this.imageName);
                            MainActivity.this.copyFile(midiFrom, midiTo);
                            MainActivity.this.copyFile(xmlFrom, xmlTo);
                            MediaScannerConnection.scanFile(MainActivity.this,
                                                            new String[] { midiTo.toString(), xmlTo.toString() },
                                                            null,
                                                            null);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                        MainActivity.this.filesNeedSaving = false;
                        MainActivity.this.setState(MainActivity.this.state);
                    }
                } else if (v == MainActivity.this.buttonProcess) {
                    if (MainActivity.this.processThread == null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(13, 1);
                        MainActivity.this.canCancelTime = calendar.getTime();
                        MainActivity.this.setCurrentProgress(0);
                        MainActivity.this.setState(State.PROCESSING);
                        MainActivity.this.startProcessing();
                    } else if (Calendar.getInstance().getTime().after(MainActivity.this.canCancelTime)) {
                        FNS.setVisible(MainActivity.this.buttonProcess, false);
                        MainActivity.this.cancelProcessing();
                    }
                } else if (v == MainActivity.this.buttonPlay) {
                    if (MainActivity.this.mediaPlayer.isPlaying()) {
                        MainActivity.this.stopPlaying();
                    } else {
                        MainActivity.this.startPlaying();
                    }
                    MainActivity.this.setState(MainActivity.this.state);
                    MainActivity.this.resetCursor();
                    MainActivity.this.updateCursor(false);
                } else if (v == MainActivity.this.buttonStop) {
                    MainActivity.this.loopActive = false;
                    MainActivity.this.updateBrackets();
                    MainActivity.this.rewindAndStop();
                    MainActivity.this.setState(MainActivity.this.state);
                } else if (v == MainActivity.this.buttonMetronome) {
                    MainActivity mainActivity = MainActivity.this;
                    if (MainActivity.this.tempoMode) {
                        z = false;
                    }
                    mainActivity.tempoMode = z;
                    MainActivity.this.setState(MainActivity.this.state);
                }
            }
        };
    }

    private void setImageAction() {
        this.buttonImage.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (MainActivity.this.tooManyStaffsDialog) {
                    MainActivity.this.tooManyStaffsDialog = false;
                    MainActivity.this.setState(MainActivity.this.state);
                    return false;
                } else if (MainActivity.this.theScore == null
                    && v == MainActivity.this.buttonImage
                    && event.getAction() == 0) {
                    return false;
                } else {
                    if (!(v == MainActivity.this.buttonImage && event.getAction() == 1)
                        && v == MainActivity.this.buttonImage
                        && event.getAction() == 0) {
                        if (MainActivity.this.tempoMode) {
                            MainActivity.this.tempoMode = false;
                            MainActivity.this.setState(MainActivity.this.state);
                            return false;
                        }
                        float tx = event.getX();
                        int beat =
                            MainActivity.this.findBeatForPoint(new Point((int) (((float) MainActivity.this.bitmapFull.getWidth())
                                * ((tx - MainActivity.this.dx) / MainActivity.this.imageWidth)),
                                                                         (int) ((1.0f - ((event.getY()
                                                                             - MainActivity.this.dy)
                                                                             / MainActivity.this.imageHeight))
                                                                             * ((float) MainActivity.this.bitmapFull.getHeight()))));
                        MainActivity.this.touchBeat = beat;
                        if (MainActivity.this.loopActive) {
                            if (!MainActivity.this.mediaPlayer.isPlaying()) {
                                int fromBeat = beat;
                                if (fromBeat < 0) {
                                    fromBeat = 0;
                                }
                                if (fromBeat > MainActivity.this.loopEnd) {
                                    MainActivity.this.afterLoop = true;
                                }
                                MainActivity.this.mediaPlayer.seekTo((int) (((((float) fromBeat) * 60.0f)
                                    / MainActivity.this.tempo) * 1.0f));
                                MainActivity.this.startPlaying();
                                MainActivity.this.setState(MainActivity.this.state);
                                return false;
                            } else if (beat == MainActivity.this.loopStart) {
                                MainActivity.this.loopActive = false;
                                MainActivity.this.updateBrackets();
                                return false;
                            } else {
                                if (beat > MainActivity.this.loopStart) {
                                    MainActivity.this.loopEnd = beat;
                                }
                                MainActivity.this.loopEndWas = MainActivity.this.loopEnd;
                                MainActivity.this.updateBrackets();
                                return false;
                            }
                        } else if (beat > -1) {
                            MainActivity.this.loopActive = false;
                            MainActivity.this.loopStart = beat;
                            MainActivity.this.updateBrackets();
                            MainActivity.this.playFromBeat(beat);
                            MainActivity.this.setState(MainActivity.this.state);
                        }
                    }
                    return false;
                }
            }
        });
    }
    public void playFromBeat(int beat) {
        BarInfo lastBarInfo = this.theScore.getBarInfo(this.theScore.getNumBars() - 1);
        this.mediaPlayer.seekTo(((int) (((float) this.mediaPlayer.getDuration()) * (((float) beat) / ((float) (
            lastBarInfo.startBeat
                + lastBarInfo.numBeats))))) - 20);
        startPlaying();
    }

    public void onBackPressed() {
        switch (this.state) {
            case PROCESSING:
                cancelProcessing();
                return;
            case READY:
                setState(State.INIT);
                return;
            default:
                Intent i = new Intent();
                i.setAction("android.intent.action.MAIN");
                i.addCategory("android.intent.category.HOME");
                startActivity(i);
                return;
        }
    }

    public void updateBrackets() {
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void dispatchPlayscoreCamera() {
        int degrees = 0;
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case 0:
                degrees = 0;
                break;
            case 1:
                degrees = 270;
                break;
            case 2:
                degrees = 180;
                break;
            case 3:
                degrees = 90;
                break;
        }
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("startAngle", degrees);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 1);
        }
    }
    public void dispatchSystemCamera() {
        Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            FNS.deleteFile(FNS.getImageFileLocalExternal(this));
            File file = FNS.getImageFileLocalExternal(this);
            if (file != null) {
                takePictureIntent.putExtra("output", Uri.fromFile(file));
                startActivityForResult(takePictureIntent, 2);
            }
        }
    }
    public void dispatchGallery() {
        Intent intent = new Intent("android.intent.action.PICK", Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 3);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != -1) {
            return;
        }
        if (requestCode == 1 || requestCode == 2 || requestCode == 3) {
            this.noNotesFlag = false;
            this.tooManyStaffsFlag = false;
            this.tooManyStaffsDialog = false;
            clearBitmap();
            FNS.ensureScoresDirectory();
            String path = "";
            if (requestCode == 2) {
                this.imageNeedsSaving = true;
                this.imageName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File fileExt = FNS.getImageFileLocalExternal(this);
                File fileInt = FNS.getImageFileLocalInternal(this);
                try {
                    copyFile(fileExt, fileInt);
                } catch (Exception e) {
                }
                FNS.deleteFile(fileExt);
                path = fileInt.getAbsolutePath();
            } else if (requestCode == 1) {
                if (data != null) {
                    this.imageNeedsSaving = true;
                    String str = data.getStringExtra("timeString");
                    if (str != null && str.length() != 0) {
                        this.imageName = str;
                        path = FNS.getImageFileLocalInternal(this).getAbsolutePath();
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            } else if (requestCode == 3) {
                if (data != null) {
                    this.imageNeedsSaving = false;
                    Uri imageUri = data.getData();
                    if (imageUri != null) {
                        boolean useDateFormat = true;
                        path = FNS.getAbsolutePath(this, imageUri);
                        if (path != null) {
                            this.imageName = stripExtension(new File(path).getName());
                            if (this.imageName.startsWith(FNS.publicImageFilenameSuffix)) {
                                this.imageName = this.imageName.substring(FNS.publicImageFilenameSuffix.length());
                            }
                            if (this.imageName.length() > 0) {
                                useDateFormat = false;
                            }
                        }
                        if (useDateFormat) {
                            this.imageName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            File localFile = FNS.getImageFileLocalInternal(this);
                            path = localFile.getAbsolutePath();
                            try {
                                copyFileStream(getContentResolver().openInputStream(imageUri),
                                               new FileOutputStream(localFile));
                            } catch (Exception e2) {
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
            Options options = new Options();
            options.inJustDecodeBounds = true;
            this.bitmapScaled = BitmapFactory.decodeFile(path, options);
            options.inSampleSize = calculateInSampleSize(options, (int) this.screenWidth, (int) this.screenHeight);
            options.inJustDecodeBounds = false;
            this.bitmapScaled = BitmapFactory.decodeFile(path, options);
            this.bitmapFull = BitmapFactory.decodeFile(path);
            int degrees = getImageFileDegrees(path);
            this.bitmapFull = rotateBitmap(this.bitmapFull, degrees);
            this.bitmapScaled = rotateBitmap(this.bitmapScaled, degrees);
            this.buttonImage.setImageBitmap(this.bitmapScaled);
            if (this.bitmapFull != null) {
                this.theRatio = ((float) this.bitmapFull.getHeight()) / ((float) this.bitmapFull.getWidth());
                this.theFile = new File(path);
            }
            setState(State.IMAGE);
        }
    }

    private void copyFileStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        while (true) {
            int read = in.read(buffer);
            if (read != -1) {
                out.write(buffer, 0, read);
            } else {
                return;
            }
        }
    }

    static String stripExtension(String str) {
        if (str == null) {
            return null;
        }
        int pos = str.lastIndexOf(".");
        return pos != -1 ? str.substring(0, pos) : str;
    }

    public void startProcessing() {
        if (this.processThread == null) {
            if (this.allowCancelRunnable != null) {
                this.root.removeCallbacks(this.allowCancelRunnable);
            }
            this.buttonProcess.setEnabled(false);
            this.processThread = new ProcessThread(this, this.bitmapFull);
            this.processThread.start();
            this.allowCancelRunnable = new Runnable() {
                public void run() {
                    MainActivity.this.buttonProcess.setEnabled(true);
                }
            };
            this.root.postDelayed(this.allowCancelRunnable, 1000);
        }
    }
    public void cancelProcessing() {
        setState(State.IMAGE);
        if (this.processThread != null) {
            this.processThread.cancel();
            this.processThread.interrupt();
            this.processThread = null;
        }
    }
    public void processingComplete() {
        if (this.processThread != null) {
            this.processThread.finalize();
            this.processThread = null;
        }
        this.filesNeedSaving = true;
        setState(State.READY);
        calculateOffsets();
        try {
            this.mediaPlayer.reset();
            this.mediaPlayer.setDataSource(FNS.getMidiFileLocal(this).getCanonicalPath());
            this.mediaPlayer.prepare();
            setTempo(readMidiTempo());
        } catch (IOException e) {
        }
        if (this.playThread == null) {
            this.playThread = new PlayThread(this.theScore);
            this.playThread.start();
        }
    }

    public void processingCancelled() {
    }
    public void processingFailed() {
        setState(State.IMAGE);
    }

    private void clearBitmap() {
        this.buttonImage.setImageBitmap(null);
        if (this.bitmapFull != null) {
            this.bitmapFull.recycle();
            this.bitmapFull = null;
        }
        System.runFinalization();
        System.gc();
    }

    private void clearProcessedData() {
        this.loopActive = false;
        if (this.mediaPlayer.isPlaying()) {
            rewindAndStop();
            this.mediaPlayer.reset();
        }
        this.playing = false;
        this.stopped = true;
        this.tempoMode = false;
        this.theScore = null;
        updateBrackets();
        System.runFinalization();
        System.gc();
    }
    public void rewindAndStop() {
        this.playing = false;
        this.stopped = true;
        this.mediaPlayer.seekTo(0);
        if (this.mediaPlayer.isPlaying()) {
            this.mediaPlayer.pause();
        }
    }
    public void startPlaying() {
        this.playing = true;
        this.stopped = false;
        this.mediaPlayer.start();
    }
    public void stopPlaying() {
        if (this.mediaPlayer != null) {
            this.playing = false;
            this.stopped = false;
            if (this.mediaPlayer.isPlaying()) {
                this.mediaPlayer.pause();
            }
        }
    }
    public int findBeatForPoint(Point p) {
        if (this.theScore == null) {
            return -1;
        }
        for (int barIndex = 0; barIndex < this.theScore.getNumBars(); barIndex++) {
            BarInfo b = this.theScore.getBarInfo(barIndex);
            int x1 = b.leftBarline.base.x;
            int x2 = b.rightBarline.base.x;
            int y1 = FNS.min(b.leftBarline.base.y, b.rightBarline.base.y);
            int y2 =
                FNS.max(b.leftBarline.base.y + b.leftBarline.height, b.rightBarline.base.y + b.rightBarline.height);
            if (p.x >= x1 && p.x <= x2 && p.y >= y1 && p.y <= y2) {
                return b.startBeat;
            }
        }
        float mind = -1.0f;
        int beat = -1;
        for (int barIndex2 = 0; barIndex2 < this.theScore.getNumBars(); barIndex2++) {
            BarInfo b2 = this.theScore.getBarInfo(barIndex2);
            int x12 = b2.leftBarline.base.x;
            int x22 = b2.rightBarline.base.x;
            float d = FNS.square(((float) p.x) - (((float) (x12 + x22)) * 0.5f)) + FNS.square(((float) p.y)
                                                                                                  - (((float) (FNS.min(
                b2.leftBarline.base.y,
                b2.rightBarline.base.y) + FNS.max(b2.leftBarline.base.y + b2.leftBarline.height,
                                                  b2.rightBarline.base.y + b2.rightBarline.height))) * 0.5f));
            if (beat == -1 || d < mind) {
                beat = b2.startBeat;
                mind = d;
            }
        }
        return beat;
    }

    public void setTempo(float value) {
        this.tempo = value;
        this.sliderLevel = ((float) sliderTempoToProgress(this.tempo)) / 100.0f;
        layoutSlider();
        updateTempoLabel(this.tempo);
    }
    public float getCurrentBeat() {
        float f = -1.0f;
        if (this.theScore == null || this.mediaPlayer == null) {
            return f;
        }
        BarInfo lastBarInfo = this.theScore.getBarInfo(this.theScore.getNumBars() - 1);
        try {
            return ((float) (lastBarInfo.startBeat + lastBarInfo.numBeats))
                * (((float) this.mediaPlayer.getCurrentPosition()) / ((float) this.mediaPlayer.getDuration()));
        } catch (Exception e) {
            return f;
        }
    }

    public void updateTempo(float newTempo) {
        final float currentBeat = getCurrentBeat();
        if (currentBeat >= 0.0f) {
            final float theNewTempo = newTempo;
            final boolean wasPlaying = this.playing;
            this.mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
                public void onPrepared(MediaPlayer player) {
                    MainActivity.this.mediaPlayer.setOnPreparedListener(null);
                    try {
                        MainActivity.this.mediaPlayer.seekTo((int) ((currentBeat * (60.0f / theNewTempo) * 1.0f)
                            + 1.0f));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (wasPlaying) {
                        MainActivity.this.mediaPlayer.start();
                        MainActivity.this.playing = true;
                        MainActivity.this.stopped = false;
                        MainActivity.this.resetCursor();
                    }
                    MainActivity.this.playerConfig = false;
                }
            });
            this.tempo = newTempo;
            this.playerConfig = true;
            int tempoToWrite = (int) ((this.tempo * 6.0f) + 0.5f);
            try {
                RandomAccessFile raf = new RandomAccessFile(FNS.getMidiFileLocal(this), "rw");
                try {
                    raf.seek(12);
                    raf.write(tempoToWrite >>> 8);
                    raf.seek(13);
                    raf.write(tempoToWrite);
                    this.mediaPlayer.reset();
                    this.mediaPlayer.setDataSource(raf.getFD());
                    raf.close();
                    this.mediaPlayer.setDisplay(null);
                    this.mediaPlayer.prepare();
                    RandomAccessFile randomAccessFile = raf;
                } catch (FileNotFoundException e) {
                    e = e;
                    RandomAccessFile randomAccessFile2 = raf;
                    e.printStackTrace();
                } catch (IOException e2) {

                    RandomAccessFile randomAccessFile3 = raf;
                    e2.printStackTrace();
                } catch (IllegalStateException e3) {

                    RandomAccessFile randomAccessFile4 = raf;
                    e3.printStackTrace();
                }
            } catch (FileNotFoundException e4) {

                e4.printStackTrace();
            } catch (Exception e5) {

                e5.printStackTrace();
            }
        }
    }

    private float readMidiTempo() {
        int msb = 0;
        int lsb = 0;
        try {
            RandomAccessFile raf = new RandomAccessFile(FNS.getMidiFileLocal(this), "r");
            try {
                raf.seek(12);
                msb = raf.read();
                raf.seek(13);
                lsb = raf.read();
                raf.close();
                RandomAccessFile randomAccessFile = raf;
            } catch (FileNotFoundException e) {
                RandomAccessFile randomAccessFile2 = raf;
            } catch (IOException e2) {
                RandomAccessFile randomAccessFile3 = raf;
            }
        } catch (IOException e3) {
        }
        return ((float) ((msb << 8) + lsb)) / 6.0f;
    }

    public void updateTempoLabel(float value) {
        this.labelTempo.setText("      = " + Integer.toString((int) (0.5f + value)));
    }

    private ImageButton createButton(boolean enabled, int down, int up) {
        ImageButton button = new ImageButton(this);
        Helper.setImages(this, button, down, up);
        button.setBackgroundColor(0);
        button.setScaleType(ScaleType.FIT_CENTER);
        button.setOnClickListener(this.buttonListener);
        button.setPadding(0, 0, 0, 0);
        button.setEnabled(enabled);
        FNS.setVisible(button, false);
        this.root.addView(button);
        return button;
    }

    private static void sendViewToBack(View child) {
        ViewGroup parent = (ViewGroup) child.getParent();
        if (parent != null) {
            parent.removeView(child);
            parent.addView(child, 0);
        }
    }

    private float bitmapxToScreen(float x) {
        return this.dx + ((this.imageWidth * x) / ((float) this.bitmapFull.getWidth()));
    }

    private float bitmapyToScreen(float y) {
        return this.dy + (this.imageHeight * (1.0f - (y / ((float) this.bitmapFull.getHeight()))));
    }
    public float sliderProgressToTempo(float value) {
        return ((380.0f * value) / 100.0f) + 20.0f;
    }

    private int sliderTempoToProgress(float value) {
        return (int) ((((value - 20.0f) * 100.0f) / 380.0f) + 0.5f);
    }

    public static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while (true) {
                if (halfHeight / inSampleSize <= reqHeight && halfWidth / inSampleSize <= reqWidth) {
                    break;
                }
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static int calculateInSampleSizePrevious(Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        int side2 = reqWidth;
        if (reqHeight > side2) {
            side2 = reqHeight;
        }
        if (height > side2 || width > side2) {
            while ((height / 2) / inSampleSize > side2 && (width / 2) / inSampleSize > side2) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private String getVersionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "Version Name Not Found";
        }
    }

    private String getVersionCode() {
        try {
            return Integer.toString(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
        } catch (Exception e) {
            e.printStackTrace();
            return "Build Name Not Found";
        }
    }

    private String getTooManyStavesHtml(Context context) {
        AssetManager assets = context.getAssets();
        try {
            BufferedReader r =
                new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.toomanystaves)));
            StringBuilder total = new StringBuilder();
            while (true) {
                String line = r.readLine();
                if (line == null) {
                    return total.toString();
                }
                total.append(line);
                total.append("\n");
            }
        } catch (IOException e) {
            return e.toString();
        }
    }

    private int getImageOrientation(String path) {
        try {
            return new ExifInterface(path).getAttributeInt("Orientation", 1);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static int getImageFileDegrees(String path) {
        int degrees = 0;
        try {
            return exifToDegrees(new ExifInterface(path).getAttributeInt("Orientation", 1));
        } catch (Exception e) {
            e.printStackTrace();
            return degrees;
        }
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == 6) {
            return 90;
        }
        if (exifOrientation == 3) {
            return 180;
        }
        if (exifOrientation == 8) {
            return 270;
        }
        return 0;
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int rotationInDegrees) {
        if (rotationInDegrees == 0) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate((float) rotationInDegrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
    public void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }
    public void galleryAddPic(File f) {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        mediaScanIntent.setData(Uri.fromFile(f));
        sendBroadcast(mediaScanIntent);
    }

    private Orientation exifToRscoreOrientation(int ori) {
        switch (ori) {
            case 3:
                return Orientation.ROT180;
            case 6:
                return Orientation.ROT90;
            case 8:
                return Orientation.ROT270;
            default:
                return Orientation.ROT0;
        }
    }
}