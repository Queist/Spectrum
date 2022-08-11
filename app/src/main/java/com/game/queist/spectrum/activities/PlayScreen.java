package com.game.queist.spectrum.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.game.queist.spectrum.R;
import com.game.queist.spectrum.chart.Chart;
import com.game.queist.spectrum.chart.EffectFlag;
import com.game.queist.spectrum.chart.Note;
import com.game.queist.spectrum.shape.BlankingShape;
import com.game.queist.spectrum.shape.LaneShape;
import com.game.queist.spectrum.shape.NoteShape;
import com.game.queist.spectrum.shape.Shape;
import com.game.queist.spectrum.utils.DataManager;
import com.game.queist.spectrum.utils.GamePhase;
import com.game.queist.spectrum.utils.Utility;
import com.game.queist.spectrum.utils.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PlayScreen extends AppCompatActivity implements GLSurfaceView.Renderer {

    public final static int JUDGE_NUM = 4;
    public final static int SIDE_NUM = 4;
    public final static int FRAME_RATE = 60;
    public final static double AWARE_TIME = 360;
    public final static double JUDGE_TIME_BAD = 240;
    public final static double JUDGE_TIME_GOOD = 120;
    public final static double JUDGE_TIME_PERFECT = 40;
    public final static String MISS = "MISS";
    public final static String BAD = "BAD";
    public final static String GOOD = "GOOD";
    public final static String PERFECT = "PERFECT";

    private final static double BASE_Z = 20.0;

    public Chart chart;
    public static MediaPlayer bgm;
    public static MediaPlayer keySound;
    public long time;

    View difficultyView;
    GLSurfaceView surfaceView;
    FrameLayout frameLayoutPlay;
    LinearLayout layerPause;
    ImageButton pauseButton, resumeButton, quitButton, retryButton;
    TextView scoreText, comboText;
    TextView songNameText, comboNameText;

    String songName;
    String difficulty;
    String artist;
    int diffNum;
    int coverID;

    int perfectCount, goodCount, badCount, combo;
    int maxCombo;
    int score;
    int totalNotes;
    int[] screenColor = new int[SIDE_NUM];
    int[] correctColor = new int[SIDE_NUM];
    public ArrayList<EffectFlag> effectFlags = new ArrayList<>();
    boolean lockTouchEvent;
    boolean finishFlag;

    Handler handler;
    private long start;
    private long offset;
    private long pausedTime;
    private long pausedClock;
    private long now;
    private long dt;
    private double currentBit;
    private double bitInScreen;

    private double rotateAngle;
    double[] baseAngle;
    int dominantIndex; //TODO : Init in onCreate
    private boolean isRollBack;
    private double rollBackTime;

    ArrayList<Note>[] queriedNotes;
    ArrayList<Note>[] screenNotes;
    LinkedList<Note>[] deleteQueue;
    ArrayList<Double> queriedLines;
    ArrayList<Double> screenLines;
    LinkedList<EffectFlag> effectQueue;
    Bitmap background;
    Bitmap innerLine;
    Bitmap[] colorSide = new Bitmap[SIDE_NUM];
    int width;
    int height;

    GamePhase gamePhase;
    GamePhase prevGamePhase;

    NoteShape noteShape;
    LaneShape laneShape;
    BlankingShape blankingShape;


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (lockTouchEvent) return super.dispatchTouchEvent(ev);
        //if (gamePhase.getFlag() == GamePhase.START) return super.dispatchTouchEvent(ev);

        updateFrame();

        float[] viewRay = screenPointToViewRay(ev.getX(ev.getActionIndex()), ev.getY(ev.getActionIndex()));
        viewRay = Vector.multiply(11.0f / 10.5f, viewRay); //cam vs lane dist.

        viewRay[2] = 0.f; //project to xy-plane
        double radius = Vector.length(viewRay);
        double angle = Math.acos(Vector.dotProduct(viewRay, new float[]{-1.f, 0.f, 0.f}) / radius); //right-hand
        if (viewRay[1] > 0) angle = 2 * Math.PI - angle; //right-hand
        int pointerId = ev.getPointerId(ev.getActionIndex());

        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN || ev.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
            tabTouch(radius, angle, pointerId);
        } else if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
            if (dominantIndex == -1 || dominantIndex == pointerId && !isRollBack) {
                dominantIndex = pointerId;
                double cw = angle - baseAngle[pointerId];
                double ccw = cw > 0 ? cw - 2 * Math.PI : cw + 2 * Math.PI;
                rotateAngle += Math.abs(cw) <= Math.abs(ccw) ? cw : ccw;
                baseAngle[pointerId] = angle;
            }
        } else if (ev.getActionMasked() == MotionEvent.ACTION_UP || ev.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
            if (ev.getPointerId(ev.getActionIndex()) == dominantIndex) {
                dominantIndex = -1;
                isRollBack = true;
                rollBackTime = 0.5; //TODO : rollBackTime = min(nearestNoteTime, currentChartRollBackTimer);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void tabTouch(double radius, double angle, int pointerID) {
        int touchedLine = getTouchedLine(radius, angle);
        if (touchedLine >= 0 && !screenNotes[touchedLine].isEmpty()) {
            ArrayList<Note> notes = Utility.getNextNotes(screenNotes[touchedLine]);
            for (Note note : notes) {
                if (touchedCorrectly(note, angle, touchedLine)) {
                    if (note.getKind().equals(Note.TAB)) {
                        makeTabNoteJudge(note, touchedLine);
                    }
                }
            }
        }
        baseAngle[pointerID] = angle;
    }

    private int getScore() {
        long x = (long) 10000000 * (perfectCount * 3 + goodCount * 2 + badCount) / (3 * totalNotes);
        return (int) x;
    }

    private int getTouchedLine(double radius, double angle) {
        if (5 < radius && radius < 15) { //TODO
            return (int) Math.floor(angle / (Math.PI / 2));
        }
        else return -1;
    }

    private double getCurrentNoteAngleStart(Note note, int line) {
        double start = note.getPosition1();
        double end = note.getPosition2();
        if (start > end) {
            double t = start;
            start = end;
            end = t;
        }

        double min = line % 2 == 0 ? ((10 - end) / 10 + line) * (Math.PI / 2) : (start / 10 + line) * (Math.PI / 2);
        min += rotateAngle;
        double n = Math.ceil(-min / (2 * Math.PI));
        min += 2 * Math.PI * n;
        return min;
    }

    private double getCurrentNoteAngleRange(Note note) {
        double start = note.getPosition1();
        double end = note.getPosition2();
        if (start > end) {
            double t = start;
            start = end;
            end = t;
        }

        return (end - start) / 10 * (Math.PI / 2);
    }

    private boolean touchedCorrectly(Note note, double angle, int line) {
        double min = getCurrentNoteAngleStart(note, line);
        double range = getCurrentNoteAngleRange(note);
        return  (min <= angle && angle <= min + range) || (min <= angle + 2 * Math.PI && angle + 2 * Math.PI <= min + range);
    }

    private boolean matchColor(Note note, int line) {
        double min = getCurrentNoteAngleStart(note, line);
        double range = getCurrentNoteAngleRange(note);
        int color = note.getColor();
        if (min + range >= 2 * Math.PI) color += 4;

        if (min + range < color * Math.PI / 2 || min > (color + 1) * Math.PI / 2) return false;
        return (Math.min(min + range, (color + 1) * Math.PI / 2) - Math.max(min, color * Math.PI / 2)) / range > (2.0 / 3.0);
    }

    private float[] screenPointToViewRay(float ex, float ey) {
        ex -= frameLayoutPlay.getX();
        ey -= frameLayoutPlay.getY();
        float x = 2 * (ex / width - 0.5f);
        float y = 2 * (ey / height - 0.5f);
        float[] result = new float[4];
        float[] tempMat = new float[16];
        float[] screenRay = new float[]{x, y, -1.f, 1.f};
        Matrix.invertM(tempMat, 0, Shape.getProj(), 0);
        Matrix.multiplyMV(result, 0, tempMat, 0, screenRay, 0);
        result = Vector.multiply(1 / result[3], result);
        return result;
    }

    private void makeTabNoteJudge(Note note, int touchedLine) {
        if (Math.abs(chart.bitToNanos(note.getBit()) - chart.bitToNanos(currentBit)) <= AWARE_TIME * 1000000) {
            if (Math.abs(chart.bitToNanos(note.getBit()) - chart.bitToNanos(currentBit)) > JUDGE_TIME_BAD * 1000000) {
                makeJudgeEffect(note, touchedLine, MISS);
            } else if (Math.abs(chart.bitToNanos(note.getBit()) - chart.bitToNanos(currentBit)) > JUDGE_TIME_GOOD * 1000000) {
                makeJudgeEffect(note, touchedLine, BAD);
            } else if (Math.abs(chart.bitToNanos(note.getBit()) - chart.bitToNanos(currentBit)) > JUDGE_TIME_PERFECT * 1000000) {
                if (matchColor(note, touchedLine)) {
                    makeJudgeEffect(note, touchedLine, GOOD);
                } else {
                    makeJudgeEffect(note, touchedLine, BAD);
                }
                /*Thread thread = new Thread(() -> {
                    long last = System.nanoTime();
                    long now = 0;
                    while (now - last < 1000000000.0 / FRAME_RATE) {
                        now = System.nanoTime();
                        if (screenColor[touchedLine] == note.getColor() || note.getColor() == -1) {
                            makeJudgeEffect(note, touchedLine, GOOD);
                            return;
                        }
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    makeJudgeEffect(note, touchedLine, BAD);
                });
                thread.start();*/
            } else {
                if (matchColor(note, touchedLine)) {
                    makeJudgeEffect(note, touchedLine, PERFECT);
                } else {
                    makeJudgeEffect(note, touchedLine, BAD);
                }
                /*Thread thread = new Thread(() -> {
                    long last = System.nanoTime();
                    long now = 0;
                    while (now - last < 1000000000.0 / FRAME_RATE) {
                        now = System.nanoTime();
                        if (screenColor[touchedLine] == note.getColor() || note.getColor() == -1) {
                            makeJudgeEffect(note, touchedLine, PERFECT);
                            return;
                        }
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    makeJudgeEffect(note, touchedLine, BAD);
                });
                thread.start();*/
            }
            deleteQueue[touchedLine].addLast(note);
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playscreen);

        finishFlag = false;
        Intent intent = getIntent();
        songName = intent.getStringExtra("name");
        difficulty = intent.getStringExtra("difficulty");
        diffNum = intent.getIntExtra("diffNum", 0);
        coverID = intent.getIntExtra("coverID", 0);
        artist = intent.getStringExtra("artist");
        int fileID = this.getResources().getIdentifier(songName.toLowerCase().replace(" ","_") + "_" + difficulty, "raw", this.getPackageName());
        int songID = this.getResources().getIdentifier(songName.toLowerCase().replace(" ","_"), "raw", this.getPackageName());
        chart = new Chart(this.getResources().openRawResource(fileID));
        bgm = MediaPlayer.create(this,songID);
        keySound = MediaPlayer.create(this, R.raw.key_sound);
        float volume = DataManager.getData(this).getSystemVolume();
        float music = DataManager.getData(this).getMusicVolume();
        bgm.setVolume(music, music);
        keySound.setVolume(volume, volume);
        bitInScreen = chart.getCurrentBPM(0) / (60 * DataManager.getData(this).getSpeed(songName));
        offset = chart.getOffset() + DataManager.getData(this).getOffset() + DataManager.getData(this).getOffsetDetailed(songName);
        offset *= 1000000;

        if (Build.VERSION.SDK_INT < 30) {
            int uiOption = getWindow().getDecorView().getSystemUiVisibility();
            uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            uiOption |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            uiOption |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            uiOption |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            getWindow().getDecorView().setSystemUiVisibility(uiOption);
        }
        else {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if(controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
        if (Build.VERSION.SDK_INT > 27) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        gamePhase = new GamePhase();
        prevGamePhase = new GamePhase();
        handler = new Handler();
        effectQueue = new LinkedList<>();
        lockTouchEvent = false;

        surfaceView = findViewById(R.id.surfaceView);
        frameLayoutPlay = findViewById(R.id.frameLayoutPlay);
        layerPause = findViewById(R.id.layerPause);
        layerPause.setVisibility(View.INVISIBLE);
        pauseButton = findViewById(R.id.pauseButton);
        resumeButton = findViewById(R.id.resumeButton);
        quitButton = findViewById(R.id.quitButton);
        retryButton = findViewById(R.id.retryButton);
        scoreText = findViewById(R.id.scoreText);
        comboText = findViewById(R.id.comboText);
        difficultyView = findViewById(R.id.difficultyView);
        songNameText = findViewById(R.id.songNameText);
        comboNameText = findViewById(R.id.comboNameText);
        difficultyView.setBackgroundColor(Utility.difficultyToColor(difficulty));
        songNameText.setText(songName);
        ViewGroup.LayoutParams params = frameLayoutPlay.getLayoutParams();
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(size);
        height = params.height = Math.min(size.y, size.x * 9 / 16);
        width = params.width = 16 * params.height / 9;

        frameLayoutPlay.setLayoutParams(new FrameLayout.LayoutParams(params));
        frameLayoutPlay.setX((float) size.x/2 - (float) params.width/2);
        frameLayoutPlay.setY((float) size.y/2 - (float) params.height/2);

        songNameText.setShadowLayer(width/200.0f, 0, 0, Color.BLACK);
        scoreText.setShadowLayer(4 * width/200.0f, 0, 0, Color.BLACK);
        comboText.setShadowLayer(2 * width/200.0f, 0, 0, Color.BLACK);
        comboNameText.setShadowLayer(2 * width/200.0f, 0, 0, Color.BLACK);

        Utility.setScreenRate((double)height/(double)width);

        surfaceView.setEGLContextClientVersion(3);
        int redSize     =  8;
        int greenSize   =  8;
        int blueSize    =  8;
        int alphaSize   =  8;
        int depthSize   = 16;
        int stencilSize =  8;
        surfaceView.setEGLConfigChooser( redSize,greenSize, blueSize,alphaSize, depthSize, stencilSize );
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        surfaceView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);

        queriedNotes = chart.getNotes();
        queriedLines = chart.getEquivalenceLines();
        totalNotes = chart.getTotalNotes();
        screenLines = new ArrayList<>();
        screenNotes = new ArrayList[SIDE_NUM];
        deleteQueue = new LinkedList[SIDE_NUM];
        for (int i=0; i < SIDE_NUM; i++) {
            screenNotes[i] = new ArrayList<>();
            deleteQueue[i] = new LinkedList<>();
        }
        baseAngle = new double[10];
        rotateAngle = 0;
        isRollBack = false;
        for (int i = 0; i < SIDE_NUM; i++) {
            screenColor[i] = i;
            correctColor[i] = i;
        }
        pausedTime = 0;
        pausedClock = 0;
        now = 0;
        dt = 0;
        currentBit = chart.nanosToBit(-offset);
        perfectCount = 0;
        goodCount = 0;
        badCount = 0;
        combo = 0;
        maxCombo = 0;

        Bitmap coverBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.result_bg);
        coverBitmap = Bitmap.createScaledBitmap(coverBitmap, width, height, false);
        Bitmap backgroundRaw = BitmapFactory.decodeResource(getResources(), R.drawable.background_raw1);
        backgroundRaw = Bitmap.createScaledBitmap(backgroundRaw, width, height, false);
        background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(background);
        canvas.drawBitmap(coverBitmap, 0, 0, null);
        canvas.drawBitmap(backgroundRaw, 0, 0, null);
        coverBitmap.recycle();
        backgroundRaw.recycle();
        innerLine = BitmapFactory.decodeResource(getResources(), R.drawable.inner_line_raw);
        innerLine = Bitmap.createScaledBitmap(innerLine, width/5, height/5, false);
        Utility.initNoteColor(this);
        for (int i = 0; i < SIDE_NUM; i++) {
            int color = DataManager.getData(this).getColor(i);
            float saturation = DataManager.getData(this).getSaturationBG(i);
            int id = getResources().getIdentifier(String.format(Locale.US, "color%02d",color), "drawable", this.getPackageName());
            colorSide[i] = BitmapFactory.decodeResource(getResources(), id);
            colorSide[i] = SpectrumAlign.retouch(this, colorSide[i], saturation, i);
            colorSide[i] = Bitmap.createScaledBitmap(colorSide[i], width/2, height/2, false);
        }

        pauseButton.setOnClickListener((view) -> {
            prevGamePhase.setFlag(gamePhase.getFlag());
            gamePhase.setFlag(GamePhase.PAUSE);
            if (!finishFlag) bgm.pause();
            layerPause.setVisibility(View.VISIBLE);
            lockTouchEvent = true;
        });

        resumeButton.setOnClickListener((view) -> {
            layerPause.setVisibility(View.INVISIBLE);
            lockTouchEvent = false;
            if (!finishFlag) bgm.start();
            time = System.nanoTime();
            gamePhase.setFlag(prevGamePhase.getFlag());
        });

        quitButton.setOnClickListener((view) -> {
            Intent intentBack = new Intent(this, SongSelect.class);
            bgm.release();
            keySound.release();
            finishFlag  = true;
            gamePhase.setFlag(GamePhase.QUIT);
            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this,
                    android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
            startActivity(intentBack, bundle);
            finish();
        });

        retryButton.setOnClickListener((view) -> {
            Intent intentRetry = new Intent(this, PlayScreen.class);
            bgm.release();
            keySound.release();
            finishFlag = true;
            gamePhase.setFlag(GamePhase.QUIT);
            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation( this,
                    android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
            intentRetry.putExtra("name", songName);
            intentRetry.putExtra("difficulty", difficulty);
            intentRetry.putExtra("diffNum", diffNum);
            intentRetry.putExtra("coverID", coverID);
            intentRetry.putExtra("artist", artist);
            startActivity(intentRetry, bundle);
            finish();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gamePhase.getFlag() != GamePhase.END) {
            pauseButton.performClick();
        }
    }

    @Override
    protected void onResume() {
        if (Build.VERSION.SDK_INT < 30) {
            int uiOption = getWindow().getDecorView().getSystemUiVisibility();
            uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            uiOption |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            uiOption |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            uiOption |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            getWindow().getDecorView().setSystemUiVisibility(uiOption);
        }
        else {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if(controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
        if (Build.VERSION.SDK_INT > 27) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (Build.VERSION.SDK_INT < 30) {
            int uiOption = getWindow().getDecorView().getSystemUiVisibility();
            uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            uiOption |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            uiOption |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            uiOption |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            getWindow().getDecorView().setSystemUiVisibility(uiOption);
        }
        else {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if(controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
        if (Build.VERSION.SDK_INT > 27) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onBackPressed() {
        if (!lockTouchEvent) pauseButton.performClick();
    }

    /*private void start() {
        try {
            if (Build.VERSION.SDK_INT > 25) canvas = surfaceView.getHolder().lockHardwareCanvas();
            else canvas = surfaceView.getHolder().lockCanvas();
            synchronized (surfaceView.getHolder()) {
                drawBG(canvas);
            }
            surfaceView.getHolder().unlockCanvasAndPost(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            time = System.nanoTime();
            gamePhase.setFlag(GamePhase.PLAY);
        }
    }

    private void play() {
        try {
            long elapsedTime = System.nanoTime() - now;
            long sleepTime = (long) (1000000000.0/FRAME_RATE - elapsedTime);

            if (Build.VERSION.SDK_INT > 25) canvas = surfaceView.getHolder().lockHardwareCanvas();
            else canvas = surfaceView.getHolder().lockCanvas();
            if (canvas == null) {
                Thread.sleep(1);
                return;
            }

            if (sleepTime/1000000 > 0) Thread.sleep(sleepTime/1000000);

            synchronized (surfaceView.getHolder()) {
                now = System.nanoTime();
                surfaceUpdate();
                surfaceRender(canvas);
            }

            surfaceView.getHolder().unlockCanvasAndPost(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (chart.bitToNanos(currentBit) + offset > (long) bgm.getDuration()*1000000) {
                gamePhase.setFlag(GamePhase.END);
            }
        }
    }

    private void end() {
        try {
            if (Build.VERSION.SDK_INT > 25) canvas = surfaceView.getHolder().lockHardwareCanvas();
            else canvas = surfaceView.getHolder().lockCanvas();
            synchronized (surfaceView.getHolder()) {
                drawBG(canvas);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) surfaceView.getHolder().unlockCanvasAndPost(canvas);
        }
        try {
            Thread.sleep(1500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(this, Result.class);
        makeIntent(intent);
        bgm.release();
        keySound.release();
        finishFlag = true;
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this,
                android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        startActivity(intent, bundle);
        finish();
    }*/

    public void updateFrame() {
        long tick = System.nanoTime() - time;
        double tickSeconds = ((double) tick) / 1000000000.0;
        time = System.nanoTime();


        currentBit = chart.nanosToBit((long) (chart.bitToNanos(currentBit) + tick));

        if (isRollBack) {
            rotateAngle -= rotateAngle * tickSeconds / rollBackTime;
            rollBackTime -= tickSeconds;
            if (rollBackTime <= 0) {
                rotateAngle = 0;
                rollBackTime = 0;
                isRollBack = false;
            }
        }
        /*TODO : Lots of Things*/
    }

    private void makeIntent(Intent intent) {
        //가져가야할 것 : 난이도, 곡이름, 난이도(넘버), 맥스콤보, 퍼펙트, 굿, 배드, 스코어
        intent.putExtra("difficulty", difficulty.toUpperCase());
        intent.putExtra("song", songName);
        intent.putExtra("diffNum", diffNum);
        intent.putExtra("combo", maxCombo);
        intent.putExtra("perfect", perfectCount);
        intent.putExtra("good", goodCount);
        intent.putExtra("bad", badCount);
        intent.putExtra("miss", totalNotes - perfectCount - goodCount - badCount);
        intent.putExtra("score", score);
        intent.putExtra("coverID", coverID);
        intent.putExtra("artist",artist);
    }

    /**
     * @param i : side
     */
    private void makeJudgeEffect(Note note, int i, String judge) {
        if (note.getColor() != -1 && note.getColor() != correctColor[i]) {
            if (note.getColor() == correctColor[(i+3)%4]) {
                int t = correctColor[i];
                correctColor[i] = correctColor[(i+3)%4];
                correctColor[(i+3)%4] = t;
            }
            else if (note.getColor() == correctColor[(i+1)%4]) {
                int t = correctColor[i];
                correctColor[i] = correctColor[(i+1)%4];
                correctColor[(i+1)%4] = t;
            }
        }

        switch (judge) {
            case MISS :
                combo = 0;
                handler.post(() -> comboText.setText(String.format(Locale.US, "%d",combo)));
                effectQueue.add(new EffectFlag(EffectFlag.MISS, note.getPosition1(), i, note.getPosition2(), note.getBit(), currentBit));
                break;

            case BAD :
                badCount++;
                combo = 0;
                handler.post(() -> Utility.startCountAnimation(score, getScore(), scoreText, 100));
                handler.post(() -> comboText.setText(String.format(Locale.US, "%d",combo)));
                score = getScore();
                effectQueue.add(new EffectFlag(EffectFlag.BAD, note.getPosition1(), i, note.getPosition2(), note.getBit(), currentBit));
                break;

            case GOOD :
                goodCount++;
                combo++;
                handler.post(() -> Utility.startCountAnimation(score, getScore(), scoreText, 100));
                handler.post(() -> comboText.setText(String.format(Locale.US, "%d",combo)));
                score = getScore();
                if (maxCombo < combo) maxCombo = combo;
                effectQueue.add(new EffectFlag(EffectFlag.GOOD, note.getPosition1(), i, note.getPosition2(), note.getBit(), currentBit));
                break;

            case PERFECT :
                perfectCount++;
                combo++;
                handler.post(() -> Utility.startCountAnimation(score, getScore(), scoreText, 100));
                handler.post(() -> comboText.setText(String.format(Locale.US, "%d",combo)));
                score = getScore();
                if (maxCombo < combo) maxCombo = combo;
                effectQueue.add(new EffectFlag(EffectFlag.PERFECT, note.getPosition1(), i, note.getPosition2(), note.getBit(), currentBit));
                break;

            default :
                System.out.println("\t\t\tERROR!!!");
                break;
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES30.glClearDepthf(1.f);

        noteShape = new NoteShape(this);
        laneShape = new LaneShape(this);
        blankingShape = new BlankingShape(this);

        noteShape.initialize();
        laneShape.initialize();
        blankingShape.initialize();

        Shape.setCamara(new float[]{0.f, 0.f, -11.f}, new float[]{0.f, 0.f, 0.f});
        Shape.setProj(90.f, ((float) width)/height, 10.5f, 1000.f);

        Shape.setLight(new float[]{
                0.f, 0.f, -5.f,
                0.f, 0.f, 40.f,
                0.f, 0.f, 20.f,
        }, new float[]{
                00.f, 00.f, 00.f,
                0.f, 0.f, 0.f,
                80.f, 80.f, 80.f
        });
        time = System.nanoTime();
        bgm.start();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        this.width = width;
        this.height = height;
        GLES30.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        updateFrame();

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);

        /*TODO : Cull And Render Note*/
        cull();
        laneShape.draw();
        blankingShape.draw();
        for (int i = 0; i < SIDE_NUM; i++) {
            noteShape.draw(screenNotes[i].size(), i, screenNotes[i], getZ(i), rotateAngle);
        }
    }

    private void cull() {
        int count;

        for (int i = 0; i < SIDE_NUM; i++) {
            count = deleteQueue[i].size();
            for (int j = 0; j < count; j++) {
                screenNotes[i].remove(deleteQueue[i].getFirst());
                deleteQueue[i].removeFirst();
            }
        }

        for (Iterator<Double> iterator = queriedLines.iterator(); iterator.hasNext();) {
            double bit = iterator.next();
            if (bit - bitInScreen < currentBit ) {
                screenLines.add(bit);
                iterator.remove();
            }
            else break;
        }

        screenLines.removeIf(bit -> chart.bitToNanos(currentBit) > chart.bitToNanos(bit));

        for (int i=0; i < SIDE_NUM; i++) {

            for (Iterator<Note> iterator = queriedNotes[i].iterator(); iterator.hasNext();) {
                Note note = iterator.next();
                if (note.getBit() - bitInScreen < currentBit ) {
                    screenNotes[i].add(note);
                    iterator.remove();
                }
                else break;
            }

            for (Iterator<Note> iterator = screenNotes[i].iterator(); iterator.hasNext();) {
                boolean removed = false;
                Note note = iterator.next();
                if (chart.bitToNanos(currentBit) - chart.bitToNanos(note.getBit()) > -JUDGE_TIME_PERFECT //TODO: 나중에 수정
                        && note.getKind().equals(Note.SLIDE) && (note.getColor() == -1 || note.getColor() == screenColor[i])) {
                    if (chart.bitToNanos(currentBit) - chart.bitToNanos(note.getBit()) < JUDGE_TIME_PERFECT * 1000000 ) {
                        makeJudgeEffect(note, i, PERFECT);
                    } else if (chart.bitToNanos(currentBit) - chart.bitToNanos(note.getBit()) < JUDGE_TIME_GOOD * 1000000 ) {
                        makeJudgeEffect(note, i, GOOD);
                    } else {
                        makeJudgeEffect(note, i, BAD);
                    }
                    iterator.remove();
                    continue;
                }
                if (chart.bitToNanos(currentBit) - chart.bitToNanos(note.getBit()) > JUDGE_TIME_BAD * 1000000 ) {
                    if (note.getKind().equals(Note.SLIDE)) {
                        makeJudgeEffect(note, i, BAD);
                    } else makeJudgeEffect(note, i, MISS);
                    iterator.remove();
                    continue;
                }
            }
        }

        count = effectQueue.size();
        for (int i = 0; i < count; i++) {
            effectFlags.add(effectQueue.getFirst());
            effectQueue.removeFirst();
        }
    }

    private double[] getZ(int side) {
        double[] z = new double[screenNotes[side].size()];
        for (int i = 0; i < screenNotes[side].size(); i++) {
            Note note = screenNotes[side].get(i);
            if (note.getBit() < currentBit) z[i] = 0;
            else {
                z[i] = (note.getBit() - currentBit) / bitInScreen * BASE_Z;
            }
        }
        return z;
    }
}

