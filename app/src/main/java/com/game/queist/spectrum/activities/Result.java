package com.game.queist.spectrum.activities;

import android.content.Intent;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.game.queist.spectrum.BuildConfig;
import com.game.queist.spectrum.R;
import com.game.queist.spectrum.utils.DataManager;
import com.game.queist.spectrum.utils.Utility;

import java.io.FileNotFoundException;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

public class Result extends AppCompatActivity {

    //TODO : 난이도 이미지 만들어서 적용.

    FrameLayout frameLayoutResult;
    LinearLayout judgeLayer;
    LinearLayout buttonLayer;
    ImageView resultDifficulty, resultRank, resultIsNew;
    TextView resultSongName, resultArtistName;
    ImageView resultCover;
    TextView resultComboText, resultPerfectText, resultGoodText, resultBadText, resultMissText;
    TextView resultScoreName, resultBestName;
    TextView resultScore, resultBestScore;
    ImageButton retryButton, resultBackButton;

    int score, bestScore;
    String songName;
    String difficulty;
    String artist;
    int diffNum;
    int coverID;
    boolean finishFlag;

    Handler handler;

    public static MediaPlayer startSound;
    public static MediaPlayer tapSound;
    public static MediaPlayer bgm;
    public static MediaPlayer scrollSound;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resultscreen);

        finishFlag = false;
        handler = new Handler();
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

        frameLayoutResult = findViewById(R.id.frameLayoutResult);
        judgeLayer = findViewById(R.id.judgeLayer);
        buttonLayer = findViewById(R.id.buttonLayer);
        resultSongName = findViewById(R.id.resultSongName);
        resultCover = findViewById(R.id.resultCover);
        resultArtistName = findViewById(R.id.resultArtistName);
        resultDifficulty = findViewById(R.id.resultDifficulty);
        resultComboText = findViewById(R.id.resultComboText);
        resultPerfectText = findViewById(R.id.resultPerfectText);
        resultGoodText = findViewById(R.id.resultGoodText);
        resultBadText = findViewById(R.id.resultBadText);
        resultMissText = findViewById(R.id.resultMissText);
        resultScore = findViewById(R.id.resultScore);
        resultBestScore = findViewById(R.id.resultBestScore);
        resultIsNew = findViewById(R.id.resultIsNew);
        retryButton = findViewById(R.id.resultRetryButton);
        resultBackButton = findViewById(R.id.resultBackButton);
        resultScoreName = findViewById(R.id.resultScoreName);
        resultBestName = findViewById(R.id.resultBestName);
        resultRank = findViewById(R.id.resultRank);

        startSound = MediaPlayer.create(this, R.raw.start_sound);
        tapSound = MediaPlayer.create(this, R.raw.tap);
        scrollSound = MediaPlayer.create(this, R.raw.scroll_sound);
        float volume = DataManager.getData(this).getSystemVolume();
        startSound.setVolume(volume, volume);
        tapSound.setVolume(volume, volume);
        scrollSound.setVolume(volume, volume);

        ViewGroup.LayoutParams params = frameLayoutResult.getLayoutParams();
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(size);
        params.height = Math.min(size.y, size.x * 9 / 16);
        params.width = 16 * params.height / 9;

        frameLayoutResult.setLayoutParams(new FrameLayout.LayoutParams(params));
        frameLayoutResult.setX((float) size.x/2 - (float) params.width/2);
        frameLayoutResult.setY((float) size.y/2 - (float) params.height/2);

        resultIsNew.setVisibility(View.INVISIBLE);
        judgeLayer.setVisibility(View.INVISIBLE);
        buttonLayer.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        score = intent.getIntExtra("score",0);
        songName = intent.getStringExtra("song");
        difficulty = intent.getStringExtra("difficulty");
        diffNum = intent.getIntExtra("diffNum", 0);
        coverID = intent.getIntExtra("coverID", 0);
        artist = intent.getStringExtra("artist");
        resultSongName.setText(songName);
        int ID = getResources().getIdentifier("result_"+difficulty.toLowerCase(), "drawable", this.getPackageName());
        resultDifficulty.setImageResource(ID);
        ID = getResources().getIdentifier("result_"+ Utility.scoreToRank(score), "drawable", this.getPackageName());
        System.out.println("\t\t\t"+Utility.scoreToRank(score));
        resultRank.setImageResource(ID);
        if (BuildConfig.DEV_MODE) {
            try {
                resultCover.setImageURI(Uri.fromFile(Utility.getExternalStorageFile(songName.replace(" ", "_").toLowerCase(), "image")));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else resultCover.setImageResource(coverID);
        resultComboText.setText(String.format(Locale.US,"%04d",intent.getIntExtra("combo",0)));
        resultPerfectText.setText(String.format(Locale.US,"%04d",intent.getIntExtra("perfect",0)));
        resultGoodText.setText(String.format(Locale.US,"%04d",intent.getIntExtra("good",0)));
        resultBadText.setText(String.format(Locale.US,"%04d",intent.getIntExtra("bad",0)));
        resultMissText.setText(String.format(Locale.US,"%04d",intent.getIntExtra("miss",0)));
        resultArtistName.setText(artist);

        Utility.setMaskFilter(resultScore);
        Utility.setMaskFilter(resultSongName);
        Utility.setMaskFilter(resultBestScore);
        Utility.setMaskFilter(resultComboText);
        Utility.setMaskFilter(resultPerfectText);
        Utility.setMaskFilter(resultGoodText);
        Utility.setMaskFilter(resultBadText);
        Utility.setMaskFilter(resultMissText);
        Utility.setMaskFilter(resultScoreName);
        Utility.setMaskFilter(resultBestName);
        Utility.setMaskFilter(resultArtistName);

        int prevBestScore = DataManager.getData(this).getScore(songName, Utility.difficultyToInteger(difficulty.toLowerCase()));
        if (prevBestScore < score) {
            DataManager.getData(this).setScore(songName, score, Utility.difficultyToInteger(difficulty.toLowerCase()));
            System.out.println("\t\t\tdiff"+Utility.difficultyToInteger(difficulty.toLowerCase()));
            bestScore = score;
            handler.postDelayed(() -> resultIsNew.setVisibility(View.VISIBLE), 2500);
        }
        else bestScore = prevBestScore;
        handler.postDelayed(() -> {
            TranslateAnimation animationJudge = new TranslateAnimation(judgeLayer.getWidth(), 0, 0, 0);
            TranslateAnimation animationButton = new TranslateAnimation(0,0,buttonLayer.getHeight(),0);
            animationJudge.setDuration(250);
            animationJudge.setFillAfter(true);
            animationButton.setDuration(500);
            animationButton.setFillAfter(true);
            judgeLayer.startAnimation(animationJudge);
            judgeLayer.setVisibility(View.VISIBLE);
            buttonLayer.startAnimation(animationButton);
            buttonLayer.setVisibility(View.VISIBLE);
        }, 2000);
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            bgm = MediaPlayer.create(this,R.raw.result_theme);
            float musicVolume = DataManager.getData(this).getMusicVolume();
            bgm.setVolume(musicVolume, musicVolume);
            if (!bgm.isPlaying()) bgm.start();
            bgm.setOnCompletionListener(MediaPlayer::start);
        });
        thread.start();
        Utility.startCountAnimation(score, resultScore, 2000, scrollSound);
        Utility.startCountAnimation(bestScore, resultBestScore, 2000);

        retryButton.setOnClickListener((view) -> {
            startSound.start();
            Thread threadStart = new Thread(() -> {
                for (int i = 0; i < 500; i++) {
                    int finalI = i;
                    handler.post(() -> frameLayoutResult.setAlpha((float) (500 - finalI) / 500));
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                handler.post(() -> {
                    Intent it = new Intent(this, PlayScreen.class);
                    it.putExtra("name", songName);
                    it.putExtra("difficulty", difficulty.toLowerCase());
                    it.putExtra("diffNum", diffNum);
                    it.putExtra("coverID", coverID);
                    it.putExtra("artist", artist);
                    bgm.release();
                    startSound.release();
                    tapSound.release();
                    finishFlag = true;
                    Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this,
                            android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                    startActivity(it, bundle);
                    finish();
                });
            });
            threadStart.start();
        });

        resultBackButton.setOnClickListener((view) -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        tapSound.start();
        Intent it = new Intent(this, SongSelect.class);
        bgm.stop();
        bgm.release();
        startSound.release();
        tapSound.setOnCompletionListener(MediaPlayer::release);
        finishFlag = true;
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this,
                android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        startActivity(it, bundle);
        finish();
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
        if (bgm != null && !bgm.isPlaying()) bgm.start();
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
        if (bgm != null && !finishFlag) bgm.pause();
        super.onStop();
    }
}
