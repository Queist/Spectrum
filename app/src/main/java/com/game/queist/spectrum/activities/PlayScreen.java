package com.game.queist.spectrum.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.res.ResourcesCompat;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

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
    double[] startPointX, startPointY;
    ArrayList<Double>[] slidePosSet;
    LinkedList<Double>[] slideBuffer;
    int score;
    int totalNotes;
    int[] screenColor = new int[SIDE_NUM];
    int[] correctColor = new int[SIDE_NUM];
    public ArrayList<Integer> colorQueryList = new ArrayList<>();
    public ArrayList<EffectFlag> effectFlags = new ArrayList<>();
    boolean lockTouchEvent;
    boolean finishFlag;
    boolean[] upLeftToRight, upRightToLeft, downLeftToRight, downRightToLeft;
    boolean[] leftUpToDown, leftDownToUp, rightUpToDown, rightDownToUp;
    boolean colorFixFlag;
    char[] movePos;

    Thread thread;
    Handler handler;
    android.graphics.Canvas canvas;
    private long start;
    private long offset;
    private long pausedTime;
    private long pausedClock;
    private long now;
    private long dt;
    private double currentBit;
    private double bitInScreen;
    private double rotateAngle;

    ArrayList<Note>[] queryNotes;
    ArrayList<Note>[] screenNotes;
    LinkedList<Note>[] noteBuffer;
    ArrayList<Double> queryLines;
    ArrayList<Double> screenLines;
    LinkedList<EffectFlag> effectBuffer;
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
        if (gamePhase.getFlag() == GamePhase.START) return super.dispatchTouchEvent(ev);
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN || ev.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
            slideTouchDetect(ev);
            tabTouch(ev.getX(ev.getActionIndex()), ev.getY(ev.getActionIndex()), ev.getPointerId(ev.getActionIndex()));
        } else if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
            slideTouchDetect(ev);
            for (int i = 0; i < ev.getPointerCount(); i++) {
                swipe(ev.getX(i), ev.getY(i), ev.getPointerId(i));
            }
        } else if (ev.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
            slideTouchDetect(ev);
        }
        else if (ev.getActionMasked() == MotionEvent.ACTION_UP) {
            for (int i = 0; i < SIDE_NUM; i++) slideBuffer[i].addAll(slidePosSet[i]);
        }
        return super.dispatchTouchEvent(ev);
    }

    private void clearSwipeFlag(int i) {
        upLeftToRight[i] = upRightToLeft[i] = downLeftToRight[i] = downRightToLeft[i] = true;
        leftUpToDown[i] = leftDownToUp[i] = rightUpToDown[i] = rightDownToUp[i] = true;
    }

    private void blockSwipeFlag(int i) {
        upLeftToRight[i] = upRightToLeft[i] = downLeftToRight[i] = downRightToLeft[i] = false;
        leftUpToDown[i] = leftDownToUp[i] = rightUpToDown[i] = rightDownToUp[i] = false;
    }

    private void slideTouchDetect(MotionEvent ev) {
        for (int i = 0; i < SIDE_NUM; i++) slideBuffer[i].addAll(slidePosSet[i]);
        for (int i = 0; i < ev.getPointerCount(); i++) {
            if (i != ev.getActionIndex() || ev.getAction() != MotionEvent.ACTION_POINTER_UP) {
                double posX = ev.getX(i) - frameLayoutPlay.getX();
                double posY = ev.getY(i) - frameLayoutPlay.getY();
                int touchedLine = inJudgeLine(posX, posY);
                if (touchedLine >= 0) slidePosSet[touchedLine].add(rawPosToPos(posX, posY));
            }
        }
    }

    private double rawPosToPos(double posX, double posY) {
        posX = Math.abs(posX - width/2.0f);
        posY = Math.abs(posY - height/2.0f);
        return 10 * posY / (posY + Utility.screenRate * posX);
    }

    private void tabTouch(double rawPosX, double rawPosY, int i) {
        double posX = rawPosX - frameLayoutPlay.getX();
        double posY = rawPosY - frameLayoutPlay.getY();
        int touchedLine = inJudgeLine(posX, posY);
        if (touchedLine >= 0 && !screenNotes[touchedLine].isEmpty()) {
            ArrayList<Note> notes = Utility.getNextNotes(screenNotes[touchedLine]);
            updateCurrentBit();
            for (Note note : notes) {
                if (isCorrectPosition(note.getPosition1(), note.getPosition2(), posX, posY, touchedLine)) {
                    if (note.getKind().equals(Note.TAB)) {
                        if (Math.abs(chart.bitToNanos(note.getBit()) - chart.bitToNanos(currentBit)) <= AWARE_TIME * 1000000) {

                            if (Math.abs(chart.bitToNanos(note.getBit()) - chart.bitToNanos(currentBit)) > JUDGE_TIME_BAD * 1000000) {
                                makeJudge(note, touchedLine, MISS);
                            } else if (Math.abs(chart.bitToNanos(note.getBit()) - chart.bitToNanos(currentBit)) > JUDGE_TIME_GOOD * 1000000) {
                                makeJudge(note, touchedLine, BAD);
                            } else if (Math.abs(chart.bitToNanos(note.getBit()) - chart.bitToNanos(currentBit)) > JUDGE_TIME_PERFECT * 1000000) {
                                Thread thread = new Thread(() -> {
                                    long last = System.nanoTime();
                                    long now = 0;
                                    while (now - last < 1000000000.0 / FRAME_RATE) {
                                        now = System.nanoTime();
                                        if (screenColor[touchedLine] == note.getColor() || note.getColor() == -1) {
                                            makeJudge(note, touchedLine, GOOD);
                                            return;
                                        }
                                        try {
                                            Thread.sleep(1);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    makeJudge(note, touchedLine, BAD);
                                });
                                thread.start();
                            } else {
                                Thread thread = new Thread(() -> {
                                    long last = System.nanoTime();
                                    long now = 0;
                                    while (now - last < 1000000000.0 / FRAME_RATE) {
                                        now = System.nanoTime();
                                        if (screenColor[touchedLine] == note.getColor() || note.getColor() == -1) {
                                            makeJudge(note, touchedLine, PERFECT);
                                            return;
                                        }
                                        try {
                                            Thread.sleep(1);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    makeJudge(note, touchedLine, BAD);
                                });
                                thread.start();
                            }
                            noteBuffer[touchedLine].addLast(note);
                        }
                    }
                    /*else if (note.getKind().equals(Note.SLIDE)) {
                        if (Math.abs(chart.bitToNanos(note.getBit()) - chart.bitToNanos(currentBit)) <= AWARE_TIME * 1000000) {

                            if (chart.bitToNanos(currentBit) - chart.bitToNanos(note.getBit()) > JUDGE_TIME_BAD * 1000000) {
                                combo = 0;
                                effectFlags.add(new EffectFlag(EffectFlag.MISS, note.getPosition1(), touchedLine, note.getPosition2(), note.getBit(), currentBit));
                                handler.post(() -> Utility.startCountAnimation(score, getScore(), scoreText, 100));
                                comboText.setText(String.format(Locale.US, "%d", combo));
                                score = getScore();
                                if (maxCombo < combo) maxCombo = combo;
                            } else if (chart.bitToNanos(currentBit) - chart.bitToNanos(note.getBit()) > JUDGE_TIME_GOOD * 1000000) {
                                badCount++;
                                combo = 0;
                                effectFlags.add(new EffectFlag(EffectFlag.BAD, note.getPosition1(), touchedLine, note.getPosition2(), note.getBit(), currentBit));
                                handler.post(() -> Utility.startCountAnimation(score, getScore(), scoreText, 100));
                                comboText.setText(String.format(Locale.US, "%d", combo));
                                score = getScore();
                                if (maxCombo < combo) maxCombo = combo;
                            } else if (chart.bitToNanos(currentBit) - chart.bitToNanos(note.getBit()) > JUDGE_TIME_PERFECT * 1000000) {

                                Thread thread = new Thread(() -> {
                                    long last = System.nanoTime();
                                    long now = 0;
                                    while (now - last < 2 * 1000000000.0 / FRAME_RATE) {
                                        now = System.nanoTime();
                                        if (screenColor[touchedLine] == note.getColor() || note.getColor() == -1) {
                                            goodCount++;
                                            combo++;
                                            effectFlags.add(new EffectFlag(EffectFlag.GOOD, note.getPosition1(), touchedLine, note.getPosition2(), note.getBit(), currentBit));
                                            handler.post(() -> Utility.startCountAnimation(score, getScore(), scoreText, 100));
                                            handler.post(() -> comboText.setText(String.format(Locale.US, "%d", combo)));
                                            score = getScore();
                                            if (maxCombo < combo) maxCombo = combo;
                                            return;
                                        }
                                        try {
                                            Thread.sleep(1);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    badCount++;
                                    combo = 0;
                                    effectFlags.add(new EffectFlag(EffectFlag.BAD, note.getPosition1(), touchedLine, note.getPosition2(), note.getBit(), currentBit));
                                    handler.post(() -> Utility.startCountAnimation(score, getScore(), scoreText, 100));
                                    handler.post(() -> comboText.setText(String.format(Locale.US, "%d", combo)));
                                    score = getScore();
                                    if (maxCombo < combo) maxCombo = combo;
                                });
                                thread.start();
                            } else {
                                Thread thread = new Thread(() -> {
                                    long last = System.nanoTime();
                                    long now = 0;
                                    while (now - last < 1000000000.0 / FRAME_RATE) {
                                        now = System.nanoTime();
                                        if (screenColor[touchedLine] == note.getColor() || note.getColor() == -1) {
                                            perfectCount++;
                                            combo++;
                                            effectFlags.add(new EffectFlag(EffectFlag.PERFECT, note.getPosition1(), touchedLine, note.getPosition2(), note.getBit(), currentBit));
                                            handler.post(() -> Utility.startCountAnimation(score, getScore(), scoreText, 100));
                                            handler.post(() -> comboText.setText(String.format(Locale.US, "%d", combo)));
                                            score = getScore();
                                            if (maxCombo < combo) maxCombo = combo;
                                            return;
                                        }
                                        try {
                                            Thread.sleep(1);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    badCount++;
                                    combo = 0;
                                    effectFlags.add(new EffectFlag(EffectFlag.BAD, note.getPosition1(), touchedLine, note.getPosition2(), note.getBit(), currentBit));
                                    handler.post(() -> Utility.startCountAnimation(score, getScore(), scoreText, 100));
                                    handler.post(() -> comboText.setText(String.format(Locale.US, "%d", combo)));
                                    score = getScore();
                                    if (maxCombo < combo) maxCombo = combo;
                                });
                                thread.start();
                            }
                            screenNotes[touchedLine].remove(note);
                        }
                    }*/
                }
            }
        }

        /*if (touchedLine == -1) {
            clearSwipeFlag(i);
            movePos[i] = 'N';
            startPointX[i] = posX;
            startPointY[i] = posY;
        }
        else {
            blockSwipeFlag(i);
        }*/
        clearSwipeFlag(i);
        movePos[i] = 'N';
        startPointX[i] = posX;
        startPointY[i] = posY;
    }

    private void swipe(double rawPosX, double rawPosY, int i) {
        if (inJudgeLine(startPointX[i], startPointY[i]) != -1) return;
        double posX = rawPosX - frameLayoutPlay.getX();
        double posY = rawPosY - frameLayoutPlay.getY();
        double swipeDistanceX = (double) height/10;
        double swipeDistanceY = (double) height/10;
        int t = -1;
        if (movePos[i] != 'N') {
            switch (movePos[i]) {
                case 'R' :
                    if (posX > startPointX[i]) {
                        startPointX[i] = posX;
                        startPointY[i] = posY;
                        return;
                    }
                    break;
                case 'L' :
                    if (posX < startPointX[i]) {
                        startPointX[i] = posX;
                        startPointY[i] = posY;
                        return;
                    }
                    break;
                case 'D' :
                    if (posY > startPointY[i]) {
                        startPointX[i] = posX;
                        startPointY[i] = posY;
                        return;
                    }
                    break;
                case 'U' :
                    if (posY < startPointY[i]) {
                        startPointX[i] = posX;
                        startPointY[i] = posY;
                        return;
                    }
                    break;
                default :
                    System.out.println("\t\t\t!!!ERROR!!!");
            }
        }
        if (posX - startPointX[i] > swipeDistanceX) {
            movePos[i] = 'R';
            if (upLeftToRight[i] && 0 <= startPointY[i] && startPointY[i] < height/2.0) {
                t = 1;
                startPointX[i] = posX;
                startPointY[i] = posY;
                blockSwipeFlag(i);
                upRightToLeft[i] = true;
                rightUpToDown[i] = true;
            }
            else if (downLeftToRight[i] && height/2.0 <= startPointY[i] && startPointY[i] < height) {
                t = 3;
                startPointX[i] = posX;
                startPointY[i] = posY;
                blockSwipeFlag(i);
                downRightToLeft[i] = true;
                rightDownToUp[i] = true;
            }
        }
        else if (posX - startPointX[i] < -swipeDistanceX) {
            movePos[i] = 'L';
            if (upRightToLeft[i] && 0 <= startPointY[i] && startPointY[i] < height/2.0) {
                t = 1;
                startPointX[i] = posX;
                startPointY[i] = posY;
                blockSwipeFlag(i);
                upLeftToRight[i] = true;
                leftUpToDown[i] = true;
            }
            else if (downRightToLeft[i] && height/2.0 <= startPointY[i] && startPointY[i] < height) {
                t = 3;
                startPointX[i] = posX;
                startPointY[i] = posY;
                blockSwipeFlag(i);
                downLeftToRight[i] = true;
                leftDownToUp[i] = true;
            }
        }
        else if (posY - startPointY[i] > swipeDistanceY) {
            movePos[i] = 'D';
            if (leftUpToDown[i] && 0 <= startPointX[i] && startPointX[i] < width/2.0) {
                t = 0;
                startPointX[i] = posX;
                startPointY[i] = posY;
                blockSwipeFlag(i);
                leftDownToUp[i] = true;
                downLeftToRight[i] = true;
            }
            else if (rightUpToDown[i] && width/2.0 <= startPointX[i] && startPointX[i] < width) {
                t = 2;
                startPointX[i] = posX;
                startPointY[i] = posY;
                blockSwipeFlag(i);
                rightDownToUp[i] = true;
                downRightToLeft[i] = true;
            }
        }
        else if (posY - startPointY[i] < -swipeDistanceY) {
            movePos[i] = 'U';
            if (leftDownToUp[i] && 0 <= startPointX[i] && startPointX[i] < width/2.0) {
                t = 0;
                startPointX[i] = posX;
                startPointY[i] = posY;
                blockSwipeFlag(i);
                leftUpToDown[i] = true;
                upLeftToRight[i] = true;
            }
            else if (rightDownToUp[i] && width/2.0 <= startPointX[i] && startPointX[i] < width) {
                t = 2;
                startPointX[i] = posX;
                startPointY[i] = posY;
                blockSwipeFlag(i);
                rightUpToDown[i] = true;
                upRightToLeft[i] = true;
            }
        }
        if (t != -1) {
            colorQueryList.add(t);
            System.out.println("\t\t\tmove: "+movePos[i]);
        }
    }

    private void swap(int i, int j) {
        int t = screenColor[i];
        screenColor[i] = screenColor[j];
        screenColor[j] = t;
    }

    private int getScore() {
        long x = (long) 10000000 * (perfectCount * 3 + goodCount * 2 + badCount) / (3 * totalNotes);
        return (int) x;
    }

    private int inJudgeLine(double x, double y) {
        double eq = Math.abs(x - (double) width/2) / width + Math.abs(y - (double) height/2) / height;
        System.out.println("\t\t\teq:"+eq);
        if (7.0/16.0 < eq) {
            if (0 <= x && x < (double) width/2 && 0 <= y && y < (double) height/2) return 0;
            else if ((double) width/2 <= x && x < width && 0 <= y && y < (double) height/2) return 1;
            else if ((double) width/2 <= x && x < width && (double) height/2 <= y && y < height) return 2;
            else return 3;
        }
        else if (eq <= 7.0/16.0) return -1;
        else return -2;
    }

    private boolean isCorrectPosition(double pos1, double pos2, double x, double y, int line) {
        double[] coefficient;
        pos1 -= 0.5;
        pos2 += 0.5;
        coefficient = Utility.getCoefficients(line);
        coefficient[1] = (y - (double) height/2) - coefficient[0] * (x - (double) width/2);
        double y1 = coefficient[1] * pos1/10 + (double) height/2;
        double x1 = coefficient[1]/coefficient[0] * (pos1/10-1) + (double) width/2;
        double y2 = coefficient[1] * pos2/10 + (double) height/2;
        double x2 = coefficient[1]/coefficient[0] * (pos2/10-1) + (double) width/2;
        return  ((x-x1) * (x - x2) < 0 && (y - y1) * (y - y2) < 0);
    }

    private float[] screenPointToViewRay(float ex, float ey) {
        float x = 2 * (ex / width - 0.5f);
        float y = 2 * (ey / height - 0.5f);
        float[] result = new float[4];
        float[] tempMat = new float[16];
        float[] screenRay = new float[]{x, y, 0.f, 1.f};
        Matrix.invertM(tempMat, 0, Shape.getProj(), 0);
        Matrix.multiplyMV(result, 0, tempMat, 0, screenRay, 0);

        return result;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playscreen);

        finishFlag = false;
        colorFixFlag = false;
        slidePosSet = new ArrayList[SIDE_NUM];
        for (int i = 0; i < SIDE_NUM; i++) slidePosSet[i] = new ArrayList<>();
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
        effectBuffer = new LinkedList<>();
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

        queryNotes = chart.getNotes();
        queryLines = chart.getEquivalenceLines();
        totalNotes = chart.getTotalNotes();
        screenLines = new ArrayList<>();
        screenNotes = new ArrayList[SIDE_NUM];
        noteBuffer = new LinkedList[SIDE_NUM];
        slideBuffer = new LinkedList[SIDE_NUM];
        for (int i=0; i < SIDE_NUM; i++) {
            screenNotes[i] = new ArrayList<>();
            noteBuffer[i] = new LinkedList<>();
            slideBuffer[i] = new LinkedList<>();
        }
        startPointX = new double[10];
        startPointY = new double[10];
        upLeftToRight = new boolean[10];
        upRightToLeft = new boolean[10];
        downLeftToRight = new boolean[10];
        downRightToLeft = new boolean[10];
        leftUpToDown = new boolean[10];
        leftDownToUp = new boolean[10];
        rightUpToDown = new boolean[10];
        rightDownToUp = new boolean[10];
        movePos = new char[10];
        for (int i = 0; i < 10; i++) blockSwipeFlag(i);
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

    public void updateCurrentBit() {
        currentBit = chart.nanosToBit((long) (chart.bitToNanos(currentBit) + System.nanoTime() - time));
        time = System.nanoTime();
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
    private void makeJudge(Note note, int i, String judge) {

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
                effectBuffer.add(new EffectFlag(EffectFlag.MISS, note.getPosition1(), i, note.getPosition2(), note.getBit(), currentBit));
                if (note.getColor() != -1) colorFixFlag = true;
                break;

            case BAD :
                badCount++;
                combo = 0;
                handler.post(() -> Utility.startCountAnimation(score, getScore(), scoreText, 100));
                handler.post(() -> comboText.setText(String.format(Locale.US, "%d",combo)));
                score = getScore();
                effectBuffer.add(new EffectFlag(EffectFlag.BAD, note.getPosition1(), i, note.getPosition2(), note.getBit(), currentBit));
                if (note.getColor() != -1) colorFixFlag = true;
                break;

            case GOOD :
                goodCount++;
                combo++;
                handler.post(() -> Utility.startCountAnimation(score, getScore(), scoreText, 100));
                handler.post(() -> comboText.setText(String.format(Locale.US, "%d",combo)));
                score = getScore();
                if (maxCombo < combo) maxCombo = combo;
                effectBuffer.add(new EffectFlag(EffectFlag.GOOD, note.getPosition1(), i, note.getPosition2(), note.getBit(), currentBit));
                break;

            case PERFECT :
                perfectCount++;
                combo++;
                handler.post(() -> Utility.startCountAnimation(score, getScore(), scoreText, 100));
                handler.post(() -> comboText.setText(String.format(Locale.US, "%d",combo)));
                score = getScore();
                if (maxCombo < combo) maxCombo = combo;
                effectBuffer.add(new EffectFlag(EffectFlag.PERFECT, note.getPosition1(), i, note.getPosition2(), note.getBit(), currentBit));
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
        updateCurrentBit();

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);

        /*TODO : Cull And Render Note*/
        //noteShape.draw(3, new int[]{0, 0, 1}, new double[]{0.0, 0.0, 4.0}, new double[]{10.0, 10.0, 10.0}, new double[]{0.0, 5.0, t});
        cull();
        laneShape.draw();
        blankingShape.draw();
        for (int i = 0; i < SIDE_NUM; i++) {
            noteShape.draw(screenNotes[i].size(), i, screenNotes[i], getZ(i));
        }
    }

    private void cull() {
        int count;

        for (int i = 0; i < SIDE_NUM; i++) {
            count = noteBuffer[i].size();
            for (int j = 0; j < count; j++) {
                screenNotes[i].remove(noteBuffer[i].getFirst());
                noteBuffer[i].removeFirst();
            }
        }

        for (int i = 0; i < SIDE_NUM; i++) {
            count = slideBuffer[i].size();
            for (int j = 0; j < count; j++) {
                slidePosSet[i].remove(slideBuffer[i].getFirst());
                slideBuffer[i].removeFirst();
            }
        }

        for (Iterator<Double> iterator = queryLines.iterator(); iterator.hasNext();) {
            double bit = iterator.next();
            if (bit - bitInScreen < currentBit ) {
                screenLines.add(bit);
                iterator.remove();
            }
            else break;
        }

        screenLines.removeIf(bit -> chart.bitToNanos(currentBit) > chart.bitToNanos(bit));

        for (int i=0; i < SIDE_NUM; i++) {

            for (Iterator<Note> iterator = queryNotes[i].iterator(); iterator.hasNext();) {
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
                if (chart.bitToNanos(note.getBit()) - chart.bitToNanos(currentBit) < JUDGE_TIME_PERFECT * 1000000
                        && note.getKind().equals(Note.SLIDE)) { //슬라판정 레이트인 경우 고려하기.
                    for (Double pos : slidePosSet[i]) {
                        if (note.getPosition1() - 0.5 < pos && pos < note.getPosition2() + 0.5) {
                            if (chart.bitToNanos(currentBit) - chart.bitToNanos(note.getBit()) > JUDGE_TIME_GOOD * 1000000) {
                                makeJudge(note, i, BAD);
                                iterator.remove();
                                removed = true;
                                break;
                            }
                            else if ((note.getColor() == -1 || note.getColor() == screenColor[i])
                                    && chart.bitToNanos(currentBit) - chart.bitToNanos(note.getBit()) > JUDGE_TIME_PERFECT * 1000000) {
                                makeJudge(note, i, GOOD);
                                iterator.remove();
                                removed = true;
                                break;
                            }
                            else if (note.getColor() == -1 || note.getColor() == screenColor[i]) {
                                makeJudge(note, i, PERFECT);
                                iterator.remove();
                                removed = true;
                                break;
                            }
                        }
                    }
                    if (removed) continue;
                }
                if (chart.bitToNanos(currentBit) - chart.bitToNanos(note.getBit()) > -JUDGE_TIME_PERFECT //TODO: 나중에 수정
                        && note.getKind().equals(Note.AUTO) && (note.getColor() == -1 || note.getColor() == screenColor[i])) {
                    if (chart.bitToNanos(currentBit) - chart.bitToNanos(note.getBit()) < JUDGE_TIME_PERFECT * 1000000 ) {
                        makeJudge(note, i, PERFECT);
                    } else if (chart.bitToNanos(currentBit) - chart.bitToNanos(note.getBit()) < JUDGE_TIME_GOOD * 1000000 ) {
                        makeJudge(note, i, GOOD);
                    } else {
                        makeJudge(note, i, BAD);
                    }
                    iterator.remove();
                    continue;
                }
                if (chart.bitToNanos(currentBit) - chart.bitToNanos(note.getBit()) > JUDGE_TIME_BAD * 1000000 ) {
                    if (note.getKind().equals(Note.AUTO)) {
                        makeJudge(note, i, BAD);
                    } else makeJudge(note, i, MISS);
                    iterator.remove();
                    continue;
                }
            }
        }

        count = effectBuffer.size();
        for (int i = 0; i < count; i++) {
            effectFlags.add(effectBuffer.getFirst());
            effectBuffer.removeFirst();
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

