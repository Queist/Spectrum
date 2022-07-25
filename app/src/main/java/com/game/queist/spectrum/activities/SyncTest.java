package com.game.queist.spectrum;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.game.queist.spectrum.chart.Note;
import com.game.queist.spectrum.utils.DataManager;
import com.game.queist.spectrum.utils.Utility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

public class SyncTest extends AppCompatActivity implements SurfaceHolder.Callback {

    private final static long DEFAULT_OFFSET = 200 * 1000000;

    LinearLayout linearLayout;
    SurfaceView surfaceSync;
    SurfaceHolder surfaceHolder;
    ImageButton syncTestBack;
    TextView recentDT, averageDT;
    TextView recentValue, averageValue;
    ImageButton plus10, plus1, minus10, minus1;
    TextView syncTestValue;
    Canvas canvas;
    public static MediaPlayer tapSound;
    public static MediaPlayer bgm;
    boolean finishFlag;
    Thread thread;
    long syncValue;
    long averageValueRaw, recentValueRaw;
    int count;
    int width, height, offsetX, offsetY;
    GamePhase gamePhase;
    long start, now, pausedTime;
    double bit;
    int noteCount;

    ArrayList<Note> metronom;

    Bitmap background;
    Bitmap innerLine;
    Bitmap colorSide;
    Bitmap theme;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getX() < linearLayout.getX() || ev.getX() > linearLayout.getX() + linearLayout.getWidth() ||
                ev.getY() < linearLayout.getY() || ev.getY() > linearLayout.getY() + linearLayout.getHeight())
            return super.dispatchTouchEvent(ev);
        if (ev.getX(ev.getActionIndex()) - linearLayout.getX() > linearLayout.getWidth() * 3.0 / 4) return super.dispatchTouchEvent(ev);
        if (gamePhase.getFlag() == GamePhase.START) return super.dispatchTouchEvent(ev);
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN || ev.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN)
            tabTouch(ev.getX(ev.getActionIndex()), ev.getY(ev.getActionIndex()));
        return super.dispatchTouchEvent(ev);
    }

    private void tabTouch(double rawPosX, double rawPosY) {
        double posX = rawPosX - linearLayout.getX() - offsetX;
        double posY = rawPosY - linearLayout.getY() - offsetY;
        if (inJudgeLine(posX, posY) && !metronom.isEmpty()) {
            Note note = metronom.get(0);
            if (isCorrectPosition(posX, posY) && (Math.abs(note.getBit() - bit) <= PlayScreen.AWARE_TIME/500)) {
                recentValueRaw = (long) ((note.getBit() - bit) * 500);
                recentValue.setText(String.format(Locale.US, "%d", recentValueRaw));
                averageValueRaw = Math.round((averageValueRaw * count + recentValueRaw) / (double)(++count));
                averageValue.setText(String.format(Locale.US, "%d", averageValueRaw));
                metronom.remove(0);
            }
        }
    }

    private boolean inJudgeLine(double x, double y) {
        double eq = x / width + y / height;
        return 14.0 / 16.0 < eq && eq < 24.0 / 16.0;
    }

    private boolean isCorrectPosition(double x, double y) {
        x /= (x / width + y / height);
        return 0.275 < x/width && x/width < 0.525;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.synctest);

        if (Build.VERSION.SDK_INT < 30) {
            int uiOption = getWindow().getDecorView().getSystemUiVisibility();
            uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            uiOption |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            uiOption |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            uiOption |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            getWindow().getDecorView().setSystemUiVisibility(uiOption);
        } else {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }

        if (Build.VERSION.SDK_INT > 27) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        linearLayout = findViewById(R.id.linearLayoutSync);

        ViewGroup.LayoutParams params = linearLayout.getLayoutParams();
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(size);
        params.height = Math.min(size.y, size.x * 9 / 16);
        params.width = 16 * params.height / 9;

        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(params));
        linearLayout.setX((float) size.x/2 - (float) params.width/2);
        linearLayout.setY((float) size.y/2 - (float) params.height/2);

        this.width = (int) (params.width * 3 / 6.5);
        this.height = this.width * 9 / 16;
        this.offsetX = (int) (params.width * 0.4 / 6.5);
        this.offsetY = (int) (params.height * 2.5 / 6.5);
        Utility.setScreenRate(9.0/16.0);

        noteCount = 0;
        metronom = new ArrayList<>();

        finishFlag = false;
        gamePhase = new GamePhase();
        surfaceSync = findViewById(R.id.surfaceSync);
        syncTestBack = findViewById(R.id.syncTestBack);
        recentDT = findViewById(R.id.recentDT);
        averageDT = findViewById(R.id.averageDT);
        recentValue = findViewById(R.id.recentValue);
        averageValue = findViewById(R.id.averageValue);
        plus10 = findViewById(R.id.plus10);
        plus1 = findViewById(R.id.plus1);
        minus1 = findViewById(R.id.minus1);
        minus10 = findViewById(R.id.minus10);
        syncTestValue = findViewById(R.id.syncTestValue);
        float volume = DataManager.getData(this).getSystemVolume();
        float music = DataManager.getData(this).getMusicVolume();
        tapSound = MediaPlayer.create(this, R.raw.tap);
        bgm = MediaPlayer.create(this, R.raw.bpm120);
        tapSound.setVolume(volume, volume);
        bgm.setVolume(music, music);
        background = BitmapFactory.decodeResource(this.getResources(), R.drawable.background_sync);
        background = Bitmap.createScaledBitmap(background, width, height, false);
        innerLine = BitmapFactory.decodeResource(this.getResources(), R.drawable.inner_line_sync);
        innerLine = Bitmap.createScaledBitmap(innerLine, width/5, height/5, false);
        colorSide = BitmapFactory.decodeResource(this.getResources(), R.drawable.color_sync);
        colorSide = Bitmap.createScaledBitmap(colorSide, width, height, false);
        theme = BitmapFactory.decodeResource(this.getResources(), R.drawable.sync_bg);
        theme = Bitmap.createScaledBitmap(theme, params.width, params.height, false);
        theme = Bitmap.createBitmap(theme, offsetX, offsetY, this.width, this.height);
        surfaceHolder = surfaceSync.getHolder();
        surfaceHolder.addCallback(this);

        Utility.setMaskFilter(recentDT);
        Utility.setMaskFilter(recentValue);
        Utility.setMaskFilter(averageDT);
        Utility.setMaskFilter(averageValue);
        Utility.setMaskFilter(syncTestValue);

        syncValue = DataManager.getData(this).getOffset();
        syncTestValue.setText(String.format(Locale.US,"%+d",syncValue));

        plus10.setOnClickListener((view) -> {
            syncValue += 10;
            syncTestValue.setText(String.format(Locale.US,"%+d",syncValue));
        });
        plus1.setOnClickListener((view) -> {
            syncValue += 1;
            syncTestValue.setText(String.format(Locale.US,"%+d",syncValue));
        });
        minus1.setOnClickListener((view) -> {
            syncValue -= 1;
            syncTestValue.setText(String.format(Locale.US,"%+d",syncValue));
        });
        minus10.setOnClickListener((view) -> {
            syncValue -= 10;
            syncTestValue.setText(String.format(Locale.US,"%+d",syncValue));
        });

        syncTestBack.setOnClickListener((view) -> {
            gamePhase.setFlag(GamePhase.QUIT);
            tapSound.start();
            DataManager.getData(this).setOffset(syncValue);
            bgm.release();
            tapSound.setOnCompletionListener(MediaPlayer::release);
            finishFlag = true;
            Intent it = new Intent(this, StartScreen.class);
            it.putExtra("NewStart", false);
            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this,
                    android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
            startActivity(it, bundle);
            finish();
        });
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
        bgm.start();
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
    protected void onStop() {
        if (!finishFlag) bgm.pause();
        super.onStop();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        syncTestBack.performClick();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        thread = new Thread(() -> {
            surfaceSync.getHolder().setFormat(PixelFormat.RGBA_8888);
            while (gamePhase.getFlag() != GamePhase.QUIT) {
                switch (gamePhase.getFlag()) {
                    case GamePhase.START:
                        start();
                        start = System.nanoTime();
                        bgm.start();
                        break;
                    case GamePhase.PLAY:
                        play();
                        break;
                    default:
                        break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    private void start() {
        try {
            canvas = surfaceSync.getHolder().lockCanvas(null);
            synchronized (surfaceSync.getHolder()) {
                canvas.drawBitmap(theme, 0, 0, null);
                canvas.drawBitmap(background, 0, 0, null);
                canvas.drawBitmap(colorSide, 0, 0, null);
                canvas.drawBitmap(innerLine, 0, 0, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) surfaceSync.getHolder().unlockCanvasAndPost(canvas);
        }
        gamePhase.setFlag(GamePhase.PLAY);
    }

    private void play() {
        try {
            canvas = surfaceSync.getHolder().lockCanvas(null);
            synchronized (surfaceSync.getHolder()) {
                now = System.nanoTime();
                bit = 2 * (now - start - syncValue - pausedTime - DEFAULT_OFFSET) / 1000000000.0;
                surfaceUpdate();
                surfaceRender(canvas);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) surfaceSync.getHolder().unlockCanvasAndPost(canvas);
        }
        if (gamePhase.getFlag() != GamePhase.QUIT && bit * 1000000000 / 2 > (long) bgm.getDuration()*1000000) {
            gamePhase.setFlag(GamePhase.END);
        }
    }

    private void surfaceUpdate() {
        if (bit > noteCount) {
            metronom.add(new Note(Note.TAB, 4, noteCount+2, 6, -1));
            noteCount++;
        }
        for (Iterator<Note> iterator = metronom.iterator(); iterator.hasNext();) {
            Note note = iterator.next();
            if (bit - note.getBit() > PlayScreen.JUDGE_TIME_BAD/500) {
                iterator.remove();
            }
        }
    }

    private void surfaceRender(Canvas canvas) {

        canvas.drawBitmap(theme, 0, 0, null);
        canvas.drawBitmap(background, 0, 0, null);
        canvas.drawBitmap(colorSide, 0, 0, null);
        canvas.drawBitmap(innerLine, 0, 0, null);

        for (Note note : metronom) {
            double[] info = getInfoForDraw(note.getBit(), bit);
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth((float) info[4]);
            paint.setStyle(Paint.Style.STROKE);
            paint.setMaskFilter(new BlurMaskFilter((float) (info[4] / 2), BlurMaskFilter.Blur.OUTER));
            paint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawLine((float) info[0], (float) info[1], (float) info[2], (float) info[3], paint);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            paint.setMaskFilter(null);
            paint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawLine((float) info[0], (float) info[1], (float) info[2], (float) info[3], paint);
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth((float) info[4] / (float) 3);
            paint.setStyle(Paint.Style.FILL);
            paint.setMaskFilter(null);
            paint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawLine((float) info[0], (float) info[1], (float) info[2], (float) info[3], paint);
        }
    }

    private double[] getInfoForDraw(double bit1, double bit2) {
        double[] it = new double[5];
        double x = (bit1 - bit2) / 2;
        double distance = bit1 > bit2 ? -1.0/3 * (x - 2) * (x - 2) + 4.0/3 :
                0.1 * (bit1 - bit2) / (PlayScreen.JUDGE_TIME_BAD/500);
        double[] coefficient = Utility.getCoefficients(2);
        coefficient[1] *= height*(0.2+0.8*(1-distance))*26/27;
        double y1 = coefficient[1] * 0.5;
        double x1 = coefficient[1]/coefficient[0] * -0.5;
        double y2 = coefficient[1] * 0.7;
        double x2 = coefficient[1]/coefficient[0] * -0.3;
        it[0] = x1;
        it[1] = y1;
        it[2] = x2;
        it[3] = y2;
        it[4] = ((double)2*height/27*Math.sqrt((double)256/337)*(0.2+0.8*(1-distance)));
        return it;
    }
}
