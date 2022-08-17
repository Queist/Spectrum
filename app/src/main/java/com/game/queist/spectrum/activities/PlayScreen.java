package com.game.queist.spectrum.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.opengl.EGL14;
import android.opengl.EGL15;
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
import com.game.queist.spectrum.chart.LongNote;
import com.game.queist.spectrum.chart.Note;
import com.game.queist.spectrum.shape.BlankingShape;
import com.game.queist.spectrum.shape.EffectShape;
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

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PlayScreen extends AppCompatActivity implements GLSurfaceView.Renderer {

    public final static int JUDGE_NUM = 4;
    public final static int SIDE_NUM = 4;
    public final static double AWARE_TIME = 360;
    public final static double JUDGE_TIME_BAD = 240;
    public final static double JUDGE_TIME_GOOD = 120;
    public final static double JUDGE_TIME_PERFECT = 40;
    public final static String MISS = "MISS";
    public final static String BAD = "BAD";
    public final static String GOOD = "GOOD";
    public final static String PERFECT = "PERFECT";

    public final static double NEAR = 10.5;
    public final static double FAR = 1000.0;
    public final static double CAM_Z = -11.0;
    public final static double RADIUS = 10.0;
    private final static double BASE_Z = 20.0;
    public final static double THICKNESS = 1.0;
    public final static double BLEND_RATE = 0.1666667;
    public final static double OUTER = 11.0;

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

    ArrayList<Note> queriedNotes;
    ArrayList<Note> screenNotes;
    LinkedList<Note> deleteQueue;
    ArrayList<Double> queriedLines;
    ArrayList<Double> screenLines;
    ArrayList<EffectFlag> effectFlags = new ArrayList<>();
    LinkedList<EffectFlag> effectQueue;

    int width;
    int height;

    GamePhase gamePhase;
    GamePhase prevGamePhase;

    NoteShape noteShape;
    LaneShape laneShape;
    BlankingShape blankingShape;
    EffectShape effectShape;


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (lockTouchEvent) return super.dispatchTouchEvent(ev);
        //if (gamePhase.getFlag() == GamePhase.START) return super.dispatchTouchEvent(ev);

        //updateFrame();

        float[] viewRay = screenPointToViewRay(ev.getX(ev.getActionIndex()), ev.getY(ev.getActionIndex()));
        viewRay = Vector.multiply((float) (RADIUS / NEAR), viewRay); //cam vs lane dist.

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
        if (hasTouchLine(radius) && !screenNotes.isEmpty()) {
            ArrayList<Note> notes = Utility.getNextNotes(screenNotes);
            for (Note note : notes) {
                if (touchedCorrectly(note, angle)) {
                    if (note.getKind().equals(Note.TAB)) {
                        makeTabNoteJudge(note);
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

    private boolean hasTouchLine(double radius) {
        //TODO
        return 5 < radius && radius < 15;
    }

    private boolean touchedCorrectly(Note note, double angle) {
        double min = Utility.plusAngle(Math.toRadians(note.getStart()), rotateAngle);
        double range = Math.toRadians(note.getRange());
        return  (min <= angle && angle <= min + range) || (min <= angle + 2 * Math.PI && angle + 2 * Math.PI <= min + range);
    }

    private boolean matchColor(Note note) {
        if (note.getColor() == -1) return true;
        double min = Utility.plusAngle(Math.toRadians(note.getStart()), rotateAngle);
        double range = Math.toRadians(note.getRange());
        int color = note.getColor();

        ArrayList<Double[]> rangeList = new ArrayList<>();
        while (true) {
            if (min + range > 2 * Math.PI) {
                rangeList.add(new Double[]{min, 2 * Math.PI});
                range -= 2 * Math.PI - min;
                min = 0;
            }
            else {
                rangeList.add(new Double[]{min, min + range});
                break;
            }
        }

        double result = 0;
        for (Double[] interval: rangeList) {
            result += Math.max(0, Math.min(interval[1], (color + 1) * Math.PI / 2) - Math.max(interval[0], color * Math.PI / 2));
        }
        return result >= Math.min(Math.PI / 2, note.getRange() * (2.0 / 3.0));
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

    private void makeTabNoteJudge(Note note) {
        double diff = chart.bitToNanos(note.getBit()) - chart.bitToNanos(currentBit);
        if (Math.abs(diff) <= AWARE_TIME * 1000000) {
            if (Math.abs(diff) > JUDGE_TIME_BAD * 1000000) {
                makeJudgeEffect(note, MISS);
            } else if (Math.abs(diff) > JUDGE_TIME_GOOD * 1000000) {
                makeJudgeEffect(note, BAD);
            } else if (Math.abs(diff) > JUDGE_TIME_PERFECT * 1000000) {
                if (matchColor(note)) {
                    makeJudgeEffect(note, GOOD);
                } else {
                    makeJudgeEffect(note, BAD);
                }
            } else {
                if (matchColor(note)) {
                    makeJudgeEffect(note, PERFECT);
                } else {
                    makeJudgeEffect(note, BAD);
                }
            }
            deleteQueue.addLast(note);
        }
    }

    private void makeSlideNoteJudge(Note note) {
        boolean dontRemove = false;
        double diff = chart.bitToNanos(note.getBit()) - chart.bitToNanos(currentBit);
        if (diff <= JUDGE_TIME_PERFECT * 1000000) {
            if (diff < -JUDGE_TIME_GOOD * 1000000) {
                makeJudgeEffect(note, BAD);
            } else if (diff < -JUDGE_TIME_PERFECT * 1000000 && matchColor(note)) {
                makeJudgeEffect(note,  GOOD);
            } else if (Math.abs(diff) <= JUDGE_TIME_PERFECT * 1000000 && matchColor(note)) {
                makeJudgeEffect(note, PERFECT);
            }
            else dontRemove = true;

            if (!dontRemove) deleteQueue.addLast(note);
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
        surfaceView.setEGLConfigChooser((egl, display) -> {
            int[] attrs = {
                    EGL10.EGL_LEVEL, 0,
                    EGL10.EGL_RENDERABLE_TYPE, android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q ?
                                                    EGL15.EGL_OPENGL_ES3_BIT : EGL14.EGL_OPENGL_ES2_BIT,
                    EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_ALPHA_SIZE, 8,
                    EGL10.EGL_DEPTH_SIZE, 16,
                    EGL10.EGL_STENCIL_SIZE, 8,
                    EGL10.EGL_SAMPLE_BUFFERS, 1,
                    EGL10.EGL_SAMPLES, 4,  // This is for 4x MSAA.
                    EGL10.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] configCounts = new int[1];
            egl.eglChooseConfig(display, attrs, configs, 1, configCounts);

            if (configCounts[0] == 0) {
                // Failed! Error handling.
                return null;
            } else {
                return configs[0];
            }
        } );
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        surfaceView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);

        queriedNotes = chart.getNotes();
        queriedLines = chart.getEquivalenceLines();
        totalNotes = chart.getTotalNotes();
        screenLines = new ArrayList<>();
        screenNotes = new ArrayList<>();
        deleteQueue = new LinkedList<>();

        baseAngle = new double[10];
        rotateAngle = 0;
        isRollBack = false;

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

        Utility.initNoteColor(this);

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

        for (Iterator<EffectFlag> iterator = effectFlags.iterator(); iterator.hasNext();) {
            EffectFlag effect = iterator.next();
            effect.update(tickSeconds);
            if (effect.lifeOver()) iterator.remove();
        }
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

    private void makeJudgeEffect(Note note, String judge) {
        switch (judge) {
            case MISS :
                combo = 0;
                handler.post(() -> comboText.setText(String.format(Locale.US, "%d",combo)));
                effectQueue.add(new EffectFlag(EffectFlag.MISS, note.getStart(), note.getRange(), rotateAngle));
                break;

            case BAD :
                badCount++;
                combo = 0;
                handler.post(() -> Utility.startCountAnimation(score, getScore(), scoreText, 100));
                handler.post(() -> comboText.setText(String.format(Locale.US, "%d",combo)));
                score = getScore();
                effectQueue.add(new EffectFlag(EffectFlag.BAD, note.getStart(), note.getRange(), rotateAngle));
                break;

            case GOOD :
                goodCount++;
                combo++;
                handler.post(() -> Utility.startCountAnimation(score, getScore(), scoreText, 100));
                handler.post(() -> comboText.setText(String.format(Locale.US, "%d",combo)));
                score = getScore();
                if (maxCombo < combo) maxCombo = combo;
                effectQueue.add(new EffectFlag(EffectFlag.GOOD, note.getStart(), note.getRange(), rotateAngle));
                break;

            case PERFECT :
                perfectCount++;
                combo++;
                handler.post(() -> Utility.startCountAnimation(score, getScore(), scoreText, 100));
                handler.post(() -> comboText.setText(String.format(Locale.US, "%d",combo)));
                score = getScore();
                if (maxCombo < combo) maxCombo = combo;
                effectQueue.add(new EffectFlag(EffectFlag.PERFECT, note.getStart(), note.getRange(), rotateAngle));
                break;

            default :
                System.out.println("\t\t\tERROR!!!");
                break;
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES30.glClearColor(0.5f, 0.0f, 0.0f, 1.0f);
        GLES30.glClearDepthf(1.f);

        for (Note note : queriedNotes) {
            if (note.getKind().equals(Note.LONG)) {
                LongNote longNote = (LongNote) note;
                longNote.setWorldWidth((longNote.getEndBit() - longNote.getBit()) / bitInScreen * BASE_Z);
            }
        }

        noteShape = new NoteShape(this, (float) RADIUS, (float) THICKNESS);
        laneShape = new LaneShape(this, (float) RADIUS, (float) BASE_Z, (float) BLEND_RATE);
        blankingShape = new BlankingShape(this, (float) RADIUS, (float) BASE_Z, (float) BLEND_RATE);
        effectShape = new EffectShape(this, (float) OUTER, (float) RADIUS, 0.f);

        noteShape.initialize();
        laneShape.initialize();
        blankingShape.initialize();
        effectShape.initialize();

        Shape.setCamara(new float[]{0.f, 0.f, (float) CAM_Z}, new float[]{0.f, 0.f, 0.f});
        Shape.setProj(90.f, ((float) width)/height, (float) NEAR, (float) FAR);

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
        reduceNote();
        gatherRenderObjects();
        cull();
        laneShape.draw();
        blankingShape.draw();
        effectShape.draw(effectFlags.size(), effectFlags);
        noteShape.draw(screenNotes.size(), screenNotes, getZ(), rotateAngle);
    }

    private void reduceNote() {
        for (Note note: screenNotes) {
            if (note.getKind().equals(Note.SLIDE)) makeSlideNoteJudge(note);
            if (note.getKind().equals(Note.TAB) &&
                    chart.bitToNanos(note.getBit()) - chart.bitToNanos(currentBit) < -JUDGE_TIME_BAD * 1000000) {
                makeJudgeEffect(note, MISS);
                deleteQueue.add(note);
            }
        }

        int count = deleteQueue.size();
        for (int j = 0; j < count; j++) {
            screenNotes.remove(deleteQueue.getFirst());
            deleteQueue.removeFirst();
        }
    }

    private void gatherRenderObjects() {
        int count = effectQueue.size();

        for (int i = 0; i < count; i++) {
            effectFlags.add(effectQueue.getFirst());
            effectQueue.removeFirst();
        }
    }

    private void cull() {
        for (Iterator<Double> iterator = queriedLines.iterator(); iterator.hasNext();) {
            double bit = iterator.next();
            if (bit - bitInScreen < currentBit ) {
                screenLines.add(bit);
                iterator.remove();
            }
            else break;
        }

        screenLines.removeIf(bit -> chart.bitToNanos(currentBit) > chart.bitToNanos(bit));

        for (Iterator<Note> iterator = queriedNotes.iterator(); iterator.hasNext();) {
            Note note = iterator.next();
            if (note.getBit() - bitInScreen < currentBit) {
                screenNotes.add(note);
                iterator.remove();
            }
            else break;
        }
    }

    private double[] getZ() {
        double[] z = new double[screenNotes.size()];
        for (int i = 0; i < screenNotes.size(); i++) {
            Note note = screenNotes.get(i);
            if (note.getBit() < currentBit) {
                z[i] = 0;
                if (note.getKind().equals(Note.LONG)) {
                    LongNote longNote = (LongNote) note;
                    longNote.setWorldWidth((longNote.getEndBit() - currentBit) / bitInScreen * BASE_Z);
                }
            }
            else {
                z[i] = (note.getBit() - currentBit) / bitInScreen * BASE_Z;
            }
        }
        return z;
    }
}

