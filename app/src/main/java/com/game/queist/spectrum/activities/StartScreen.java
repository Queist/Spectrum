package com.game.queist.spectrum;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.game.queist.spectrum.utils.DataManager;

public class StartScreen extends AppCompatActivity {

    FrameLayout frameLayout;
    FrameLayout layerMenu;
    ImageView circle;
    ImageButton back;
    ImageButton startButton, menuButton;
    ImageButton syncButton, volumeButton;
    LinearLayout spectrumButton;
    View[] side;
    public static MediaPlayer tapSound;
    public static MediaPlayer bgm;
    boolean finishFlag;
    Handler handler;
    boolean backFromSpectrum;
    float angle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startscreen);
        finishFlag = false;
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

        Intent it = getIntent();
        boolean newStart = it.getBooleanExtra("NewStart", true);
        backFromSpectrum = it.getBooleanExtra("BackFromSpectrum", false);
        handler = new Handler();

        frameLayout = findViewById(R.id.frameLayoutStart);

        ViewGroup.LayoutParams params = frameLayout.getLayoutParams();
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(size);
        params.height = Math.min(size.y, size.x * 9 / 16);
        params.width = 16 * params.height / 9;

        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(params));
        frameLayout.setX((float) size.x/2 - (float) params.width/2);
        frameLayout.setY((float) size.y/2 - (float) params.height/2);
        frameLayout.setVisibility(View.VISIBLE);

        side = new View[PlayScreen.SIDE_NUM];
        side[0] = findViewById(R.id.side0);
        side[1] = findViewById(R.id.side1);
        side[2] = findViewById(R.id.side2);
        side[3] = findViewById(R.id.side3);
        layerMenu = findViewById(R.id.layerMenu);
        if (newStart) layerMenu.setVisibility(View.INVISIBLE);
        else layerMenu.setVisibility(View.VISIBLE);
        back = findViewById(R.id.back);
        startButton = findViewById(R.id.startButton);
        menuButton = findViewById(R.id.menuButton);
        syncButton = findViewById(R.id.syncButton);
        spectrumButton = findViewById(R.id.spectrumButton);
        volumeButton = findViewById(R.id.volumeButton);
        circle = findViewById(R.id.circle);

        for (int i = 0; i < PlayScreen.SIDE_NUM; i++)
            side[i].setBackgroundColor(Color.HSVToColor(new float[]{30 * DataManager.getData(this).getColor(i), DataManager.getData(this).getSaturationBG(i), 1}));

        handler.post(new Runnable() {
                @Override
                public void run() {
                    circle.setRotation(angle);
                    angle += 0.816;
                    if (angle >= 360) {
                        angle -= 360;
                    }
                    handler.postDelayed(this, 17);
                }
            });

        startButton.setOnClickListener((view) -> {
            tapSound.start();
            Intent intent = new Intent(this, SongSelect.class);
            bgm.release();
            tapSound.setOnCompletionListener(MediaPlayer::release);
            finishFlag = true;
            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this,
                    android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
            startActivity(intent, bundle);
            finish();
        });
        menuButton.setOnClickListener((view) -> {
            tapSound.start();
            /*TranslateAnimation[] animationIn = new TranslateAnimation[3];
            for (int i = 0; i < 3; i++) {
                animationIn[i] = new TranslateAnimation(layerMenu.getWidth(), 0, 0, 0);
                animationIn[i].setDuration(250);
                animationIn[i].setStartOffset(25 * i);
                animationIn[i].setFillAfter(true);
                configRows[i].setAnimation(animationIn[i]);
            }*/
            layerMenu.setVisibility(View.VISIBLE);
        });
        syncButton.setOnClickListener((view) -> {
            tapSound.start();
            Intent intent = new Intent(this, SyncTest.class);
            bgm.release();
            tapSound.setOnCompletionListener(MediaPlayer::release);
            finishFlag = true;
            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this,
                    android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
            startActivity(intent, bundle);
            finish();
        });
        spectrumButton.setOnClickListener((view) -> {
            tapSound.start();
            Intent intent = new Intent(this, SpectrumAlign.class);
            tapSound.setOnCompletionListener(MediaPlayer::release);
            finishFlag = true;
            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this,
                    android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
            startActivity(intent, bundle);
            finish();
        });
        volumeButton.setOnClickListener((view) -> {
            tapSound.start();
            Intent intent = new Intent(this, Volume.class);
            tapSound.setOnCompletionListener(MediaPlayer::release);
            finishFlag = true;
            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this,
                    android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
            startActivity(intent, bundle);
            finish();
        });
        back.setOnClickListener((view) -> onBackPressed());
        /*volume.setProgress((int)(DataManager.getData(this).getVolume() * 100));
        volumeValue.setText(String.format(Locale.US,"%d",(int) (DataManager.getData(this).getVolume() * 100)));
        syncValue.setText(String.format(Locale.US, "%+d", DataManager.getData(this).getOffset()));
        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volumeValue.setText(String.format(Locale.US, "%d", progress));
                float realVolume = (float)progress/100;
                bgm.setVolume(realVolume, realVolume);
                tapSound.setVolume(realVolume, realVolume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });*/

        if (!backFromSpectrum) bgm = MediaPlayer.create(this, R.raw.start_theme);
        tapSound = MediaPlayer.create(this, R.raw.tap);
        float systemVolume = DataManager.getData(this).getSystemVolume();
        float musicVolume = DataManager.getData(this).getMusicVolume();
        bgm.setVolume(musicVolume, musicVolume);
        tapSound.setVolume(systemVolume, systemVolume);
        bgm.setOnCompletionListener(MediaPlayer::start);
        bgm.start();
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
    }

    @Override
    public void onBackPressed() {
        tapSound.start();
        if (layerMenu.getVisibility() == View.VISIBLE) {
            //DataManager.getData(this).setVolume((float)volume.getProgress()/100);
            /*TranslateAnimation[] animationOut = new TranslateAnimation[3];
            for (int i = 0; i < 3; i++) {
                animationOut[i] = new TranslateAnimation(0, layerMenu.getWidth(), 0, 0);
                animationOut[i].setDuration(250);
                animationOut[i].setStartOffset(75 * i);
                animationOut[i].setFillAfter(true);
                configRows[i].setAnimation(animationOut[i]);
                configRows[i].setVisibility(View.INVISIBLE);
            }
             handler.postDelayed(() -> {
                 for (int i = 0; i < 3; i++) {
                     configRows[i].setVisibility(View.VISIBLE);
                 }
                 layerMenu.setVisibility(View.INVISIBLE);
             }, 400);*/
            layerMenu.setVisibility(View.INVISIBLE);
        }
        else ActivityCompat.finishAffinity(this);
    }
}
