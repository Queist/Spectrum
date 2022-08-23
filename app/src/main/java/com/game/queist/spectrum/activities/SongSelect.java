package com.game.queist.spectrum.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.game.queist.spectrum.BuildConfig;
import com.game.queist.spectrum.R;
import com.game.queist.spectrum.chart.Song;
import com.game.queist.spectrum.utils.DataManager;
import com.game.queist.spectrum.utils.Utility;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

public class SongSelect extends AppCompatActivity implements AbsListView.OnScrollListener {

    public static final int DIFF_NUM = 4;
    public static final int VIEW_NUM = 3;

    FrameLayout frameLayout;
    ListView nameListView;
    ImageButton backToSelect;
    LinearLayout layerInfo, layerInfo2, layerStart;
    TextView songInfo;
    TextView artistInfo;
    TextView bpmInfo;
    TextView prevScore;
    ImageButton start;
    ImageButton backToStart;
    ImageView rankImage;
    SeekBar offsetDetailed;
    SeekBar speed;
    TextView offsetDetailedValue;
    TextView speedValue;
    LinearLayout highlight;
    ImageButton[] play;
    TextView[] level;
    TextView[] name;
    TextView previewSongName, previewDiff, previewBPM;

    ArrayList<Song> songList;
    boolean isWaitForStart;
    boolean isSmoothScrollPerformed;
    boolean isInAnimation;
    float startPointX, startPointY;
    int prevFirstVisibleItem;
    boolean fromUser;
    boolean finishFlag;
    Song currentSong;

    public static MediaPlayer scrollSound;
    public static MediaPlayer seekSound;
    public static MediaPlayer thumbSound;
    public static MediaPlayer startSound;
    public static MediaPlayer tapSound;

    Handler handler;

    private void onDiffClicked(int diff) { //Error?
        tapSound.start();
        if (currentSong.getDifficulty(diff) == -1) return;
        if (currentSong.getCurrentDiff() != diff) {
            for (int i = 0; i < SongSelect.DIFF_NUM; i++) {
                level[i].setTextColor(Color.argb(200,235,235, 235));
                Utility.setMaskFilter(level[i], 0);
            }
            level[diff].setTextColor(Color.WHITE);
            Utility.setMaskFilter(level[diff], 4);
            currentSong.setCurrentDiff(diff);
            int bestScore = DataManager.getData(this).getScore(currentSong.getName(), diff);
            if (bestScore != -1) {
                Utility.startCountAnimation(bestScore, prevScore, 1000);
                int ID = getResources().getIdentifier(Utility.scoreToRank(bestScore), "drawable", this.getPackageName());
                rankImage.setImageResource(ID);
            }
            else {
                rankImage.setImageResource(R.drawable.rank_none);
                prevScore.setText("");
            }
            return;
        }
        int bgID = getResources().getIdentifier("preview_"+Utility.difficultyToString(diff),"drawable",this.getPackageName());
        previewDiff.setText(String.format(Locale.US,"%02d",currentSong.getDifficulty()));
        previewDiff.setBackground(ContextCompat.getDrawable(this, bgID));
        isWaitForStart = true;
        layerStart.setVisibility(View.VISIBLE);
        speed.setProgress((int)(DataManager.getData(this).getSpeed(currentSong.getName()) * 10 - 5));
        speedValue.setText(String.format(Locale.US,"%.1f",DataManager.getData(this).getSpeed(currentSong.getName())));
        offsetDetailed.setProgress((int)(DataManager.getData(this).getOffsetDetailed(currentSong.getName()) / 5 + 10));
        offsetDetailedValue.setText(String.format(Locale.US, "%+d",DataManager.getData(this).getOffsetDetailed(currentSong.getName())));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songselect);

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
            if(controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
        if (Build.VERSION.SDK_INT > 27) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        frameLayout = findViewById(R.id.frameLayoutSongList);
        ViewGroup.LayoutParams params = frameLayout.getLayoutParams();
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(size);
        params.height = Math.min(size.y, size.x * 9 / 16);
        params.width = 16 * params.height / 9;

        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(params));
        frameLayout.setX((float) size.x/2 - (float) params.width/2);
        frameLayout.setY((float) size.y/2 - (float) params.height/2);

        layerInfo = findViewById(R.id.layerInfo);
        layerInfo2 = findViewById(R.id.layerInfo2);
        layerStart = findViewById(R.id.layerStart);
        layerStart.setVisibility(View.INVISIBLE);
        isWaitForStart = false;
        isSmoothScrollPerformed = true;
        isInAnimation = true;
        fromUser = false;

        nameListView = findViewById(R.id.nameList);
        highlight = findViewById(R.id.highlight);
        nameListView.setOnScrollListener(this);
        songList = makeSongList();
        int songOffset = 0;
        String recentPlayed = DataManager.getData(this).getRecentPlayed();
        if (recentPlayed != null)
            for (int i = 0; i < songList.size(); i++)
                if (recentPlayed.equals(songList.get(i).getName()))
                    songOffset = i;

        nameListView.setAdapter(new SelectionAdaptor(this, R.layout.songlist, songList, songOffset-VIEW_NUM/2, params.width, (int) (params.height/4.5)));
        nameListView.setFriction((float) (ViewConfiguration.getScrollFriction() * 2.5));
        nameListView.setSmoothScrollbarEnabled(true);
        nameListView.setClickable(false);
        songInfo = findViewById(R.id.songInfo);
        artistInfo = findViewById(R.id.artistInfo);
        prevScore = findViewById(R.id.prevScore);
        start = findViewById(R.id.start);
        backToStart = findViewById(R.id.backToStart);
        backToSelect = findViewById(R.id.backToSelect);
        offsetDetailed = findViewById(R.id.offsetDetailed);
        offsetDetailedValue = findViewById(R.id.offsetDetailedValue);
        bpmInfo = findViewById(R.id.bpmInfo);
        play = new ImageButton[4];
        play[0] = findViewById(R.id.softPlay);
        play[1] = findViewById(R.id.normalPlay);
        play[2] = findViewById(R.id.hardPlay);
        play[3] = findViewById(R.id.extremPlay);
        level = new TextView[4];
        level[0] = findViewById(R.id.softLevel);
        level[1] = findViewById(R.id.normalLevel);
        level[2] = findViewById(R.id.hardLevel);
        level[3] = findViewById(R.id.extremeLevel);
        name = new TextView[4];
        name[0] = findViewById(R.id.softName);
        name[1] = findViewById(R.id.normalName);
        name[2] = findViewById(R.id.hardName);
        name[3] = findViewById(R.id.extremeName);
        previewSongName = findViewById(R.id.previewSongName);
        previewDiff = findViewById(R.id.previewDiff);
        previewBPM = findViewById(R.id.previewBPM);
        speed = findViewById(R.id.speed);
        speedValue = findViewById(R.id.speedValue);
        scrollSound = MediaPlayer.create(this, R.raw.scroll_sound);
        seekSound = MediaPlayer.create(this,R.raw.seek_sound);
        tapSound = MediaPlayer.create(this, R.raw.tap);
        startSound = MediaPlayer.create(this, R.raw.start_sound);
        rankImage = findViewById(R.id.rankImage);
        float volume = DataManager.getData(this).getSystemVolume();
        scrollSound.setVolume(volume, volume);
        seekSound.setVolume(volume, volume);
        tapSound.setVolume(volume, volume);
        startSound.setVolume(volume, volume);
        handler = new Handler();
        currentSong = (Song) nameListView.getItemAtPosition(nameListView.getFirstVisiblePosition()+VIEW_NUM/2);

        Utility.setMaskFilter(prevScore);
        Utility.setMaskFilter(songInfo);
        Utility.setMaskFilter(artistInfo);
        Utility.setMaskFilter(bpmInfo);
        Utility.setMaskFilter(speedValue);
        Utility.setMaskFilter(offsetDetailedValue);
        Utility.setMaskFilter(previewSongName);
        Utility.setMaskFilter(previewBPM);
        Utility.setMaskFilter(previewDiff);
        for (int i = 0; i < SongSelect.DIFF_NUM; i++) {
            Utility.setMaskFilter(level[i]);
        }

        nameListView.setSelection(songList.size() * (500000/songList.size()));
        init((Song) nameListView.getItemAtPosition(nameListView.getFirstVisiblePosition()+VIEW_NUM/2));

        start.setOnClickListener((view) -> {
            int select = currentSong.getCurrentDiff();
            if (select != -1) {
                frameLayout.setEnabled(false); //Error???
                startSound.start();
                Thread thread = new Thread(() -> {
                    for (int i = 0; i < 500; i++) {
                        int finalI = i;
                        handler.post(() -> frameLayout.setAlpha((float)(500- finalI)/500));
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    handler.post(() -> {
                        Intent intent = new Intent(this, PlayScreen.class);
                        DataManager.getData(this).setRecentPlayed(currentSong.getName());
                        DataManager.getData(this).setOffsetDetailed(currentSong.getName(), (offsetDetailed.getProgress() - 10) * 5);
                        DataManager.getData(this).setSpeed(currentSong.getName(), (float) ((speed.getProgress()+5)/10.0));
                        DataManager.getData(this).setRecentPlayedDiff(currentSong.getName(), select);
                        String diffSelect = Utility.difficultyToString(select);
                        //제목과 난이도 intent 전달
                        intent.putExtra("name", currentSong.getName());
                        intent.putExtra("difficulty", diffSelect);
                        intent.putExtra("diffNum", currentSong.getDifficulty(select));
                        intent.putExtra("coverID", currentSong.getCoverID());
                        intent.putExtra("artist", currentSong.getArtist());
                        scrollSound.release();
                        tapSound.release();
                        seekSound.release();
                        startSound.release();
                        thumbSound.release();
                        finishFlag = true;
                        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this,
                                android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                        startActivity(intent, bundle);
                        finish();
                    });
                });
                thread.start();
            }
        });

        backToStart.setOnClickListener((view) -> onBackPressed());
        backToSelect.setOnClickListener((view) -> onBackPressed());

        offsetDetailed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (seekSound.isPlaying()) {
                        seekSound.pause();
                        seekSound.seekTo(0);
                    }
                    seekSound.start();
                }
                offsetDetailedValue.setText(String.format(Locale.US,"%+d",(progress-10) * 5));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (seekSound.isPlaying()) {
                        seekSound.pause();
                        seekSound.seekTo(0);
                    }
                    seekSound.start();
                }
                speedValue.setText(String.format(Locale.US,"%.1f",(progress+5)/10.0));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        for (int i = 0; i < SongSelect.DIFF_NUM; i++) {
            int finalI = i;
            play[i].setOnClickListener((view) -> onDiffClicked(finalI));
        }
    }

    private void init(Song song) {
        currentSong = song;
        thumbSound = MediaPlayer.create(this, currentSong.getThumbID());
        float volume = DataManager.getData(this).getMusicVolume();
        thumbSound.setVolume(volume, volume);
        thumbSound.setOnCompletionListener(MediaPlayer::start);
        thumbSound.start();
        songInfo.setText(currentSong.getName());
        artistInfo.setText(currentSong.getArtist());
        bpmInfo.setText(currentSong.toStringBPM());
        previewBPM.setText(currentSong.toStringBPM());
        for (int i = 0; i < SongSelect.DIFF_NUM; i++) {
            int t = currentSong.getDifficulty(i);
            if (t != -1) {
                name[i].setVisibility(View.VISIBLE);
                level[i].setVisibility(View.VISIBLE);
                level[i].setText(String.format(Locale.US, "%02d", currentSong.getDifficulty(i)));
                play[i].setImageResource(getResources().getIdentifier(Utility.difficultyToString(i)+"_button", "drawable", this.getPackageName()));
            }
            else {
                name[i].setVisibility(View.INVISIBLE);
                level[i].setVisibility(View.INVISIBLE);
                play[i].setImageResource(R.drawable.blocked_button);
            }
        }
        previewSongName.setText(currentSong.getName());
        currentSong.setCurrentDiff(-1);
        int recentDiff = DataManager.getData(this).getRecentPlayedDiff(currentSong.getName());
        while (currentSong.getDifficulty(recentDiff) == -1) recentDiff++;
        onDiffClicked(recentDiff);
    }

    @Override
    public void onBackPressed() {
        tapSound.start();
        if (isWaitForStart) {
            layerStart.setVisibility(View.INVISIBLE);
            isWaitForStart = false;
        }
        else {
            Intent it = new Intent(this, StartScreen.class);
            scrollSound.release();
            tapSound.setOnCompletionListener(MediaPlayer::release);
            seekSound.release();
            startSound.release();
            thumbSound.release();
            finishFlag = true;
            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this,
                    android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
            startActivity(it, bundle);
            finish();
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
        thumbSound.start();
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
        if (!finishFlag) thumbSound.pause();
        super.onStop();
    }

    // Attribute of SongList : 1. Name / 2. Artist / 3. MinBPM / 4. MaxBPM / 5. Difficulty Sets
    private ArrayList<Song> makeSongList() {
        songList = new ArrayList<>();
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.song_list);
            BufferedReader bufferedReader;

            /*if (BuildConfig.DEV_MODE) bufferedReader = Utility.readFile("song_list");
            else*/ bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            String readLine;
            while ((readLine = bufferedReader.readLine()) != null) {
                System.out.println("\t\t\t"+readLine);
                String[] attr = readLine.split("\t");
                String[] difficultyValue = attr[4].split(",");
                int[] difficulty = new int[DIFF_NUM];
                for (int i = 0; i < DIFF_NUM; i++) difficulty[i] = Integer.parseInt(difficultyValue[i]);
                int coverID = this.getResources().getIdentifier(attr[0].replace(" ","_").toLowerCase(), "drawable", this.getPackageName());
                int thumbID = this.getResources().getIdentifier(attr[0].replace(" ","_").toLowerCase() + "_thumb", "raw", this.getPackageName());
                songList.add(new Song(attr[0], attr[1], coverID, thumbID, Double.parseDouble(attr[2]), Double.parseDouble(attr[3]), difficulty));
            }
            return songList;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return songList;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int state) {

        if (state == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            isWaitForStart = true;
            layerInfo.setVisibility(View.INVISIBLE);
            layerInfo2.setVisibility(View.INVISIBLE);
            isInAnimation = false;
            isSmoothScrollPerformed = false;
            thumbSound.stop();
        }
        else if (!isSmoothScrollPerformed && state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            isSmoothScrollPerformed = true;
            isInAnimation = true;
            int index = absListView.getFirstVisiblePosition();
            int top = absListView.getChildAt(0).getTop();
            int height = absListView.getChildAt(0).getHeight();
            if (top < -height/2) {
                index += 1;
                absListView.scrollListBy(top + height);
            }
            else absListView.scrollListBy(top);
            /*absListView.setSelection(index);
            System.out.println("\t\t\tfirst : "+absListView.getFirstVisiblePosition());
            System.out.println("\t\t\tind : "+index);
            System.out.println(absListView.getChildAt(0).getTop());
            System.out.println(absListView.getChildAt(0).getHeight());
            System.out.println(((Song) absListView.getItemAtPosition(absListView.getFirstVisiblePosition())).getName());
            if (absListView.getChildAt(0).getTop() < 0) {
                absListView.scrollListBy(absListView.getChildAt(0).getHeight() + absListView.getChildAt(0).getTop());
                onScroll(absListView, absListView.getFirstVisiblePosition(), 3, absListView.getCount());
            }*/
            init((Song) absListView.getItemAtPosition(index+VIEW_NUM/2));
            layerInfo.setVisibility(View.VISIBLE);
            layerInfo2.setVisibility(View.VISIBLE);
            isWaitForStart = false;
        }
    }

    @Override
    public void onScroll (AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (!fromUser && firstVisibleItem != prevFirstVisibleItem) fromUser = true;
        else if (fromUser && firstVisibleItem != prevFirstVisibleItem) {
            System.out.println("\t\t\titem "+firstVisibleItem);
            prevFirstVisibleItem = firstVisibleItem;
            if (scrollSound.isPlaying()) {
                scrollSound.pause();
                scrollSound.seekTo(0);
            }
            scrollSound.start();
        }

        float centerY = view.getHeight()/2;

        //Judging the validity of the midpoint
        if(centerY <= 0){
            return;
        }
        double part = -1;
        int count = -1;
        //Start to zoom the currently displayed View
        for(int i = 0; i < visibleItemCount; i++){
            //Get item
            View temp = view.getChildAt(i);
            if (temp.getBottom() >= 0 && part == -1) {
                part = ((double) temp.getBottom()) / ((double) (view.getHeight()/4.5));
                count = 0;
                continue;
            }
            if (count != -1) {
                if (count == 0) {
                    FrameLayout frameLayout = temp.findViewById(R.id.songListFrameLayout);
                    ViewGroup.LayoutParams params = frameLayout.getLayoutParams();
                    params.height = (int) (view.getHeight() * (1 + 1.5 * part) / 4.5);
                    AbsListView.LayoutParams myParams = new AbsListView.LayoutParams(params);
                    frameLayout.setLayoutParams(myParams);
                    part = 1 + 1.5 * part;
                }
                else if (count == 1) {
                    FrameLayout frameLayout = temp.findViewById(R.id.songListFrameLayout);
                    ViewGroup.LayoutParams params = frameLayout.getLayoutParams();
                    params.height = (int) (view.getHeight() * (3.5 - part) / 4.5);
                    AbsListView.LayoutParams myParams = new AbsListView.LayoutParams(params);
                    frameLayout.setLayoutParams(myParams);
                }
                else {
                    break;
                }
                count++;
            }
        }
    }
}

class SelectionAdaptor extends BaseAdapter {

    ArrayList<Song> songList;
    int songOffset;
    int resource;
    int width, height;
    LayoutInflater inflater;
    Context context;



    public SelectionAdaptor(@NonNull Context context, int resource, ArrayList<Song> songList, int songOffset, int width, int height) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.resource = resource;
        this.songList = songList;
        this.songOffset = songOffset;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getCount() {
        return songList.size()*(1000000/songList.size());
    }

    @Override
    public Song getItem(int position) { return songList.get((position+songOffset+songList.size())%songList.size()); }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables"})
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View view;
        if (convertView == null) view = inflater.inflate(resource, viewGroup, false);
        else view = convertView;

        Song song = getItem(position);
        FrameLayout songListFrameLayout = view.findViewById(R.id.songListFrameLayout);
        ImageView cover = view.findViewById(R.id.cover);
        ViewGroup.LayoutParams params = songListFrameLayout.getLayoutParams();
        params.height = position == 1 ? (int) (height * 2.5) : height;
        params.width = (int) (1.5 * width/5);
        FrameLayout.LayoutParams myParams = new FrameLayout.LayoutParams(params);
        songListFrameLayout.setLayoutParams(myParams);
        cover.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.MATCH_PARENT));
        System.out.println("\t\t\t?!?!?!?!"+cover.getWidth());
        System.out.println("\t\t\t?!?!?!?!?!?!?!"+params.width);
        cover.setImageResource(song.getCoverID());
        cover.setScaleType(ImageView.ScaleType.FIT_CENTER);
        return view;
    }
}

