package com.game.queist.spectrum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;

import com.game.queist.spectrum.utils.DataManager;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

public class SpectrumAlign extends AppCompatActivity {

    public static final int COLOR_NUM = 12;

    public static int[] color = new int[PlayScreen.SIDE_NUM];
    float[] saturationBG;
    float[] saturationNote;
    float posX, posY;
    boolean swapped;
    boolean needToRefresh;

    View isDisabled;
    LinearLayout linearLayout;
    ListView colorList;
    SeekBar noteSeekBar, bgSeekBar;
    ImageView[] colorSide;
    ImageView[] note;
    ImageView[] isClicked;
    ImageButton spectrumBack;
    ImageButton refreshButton;
    public static MediaPlayer tapSound;
    boolean finishFlag;

    public static int currentSide = -1;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getX() < linearLayout.getX() || ev.getX() > linearLayout.getX() + linearLayout.getWidth() ||
                ev.getY() < linearLayout.getY() || ev.getY() > linearLayout.getY() + linearLayout.getHeight())
            return super.dispatchTouchEvent(ev);
        if (ev.getX() - linearLayout.getX() > linearLayout.getWidth() * 3.0f / 4) return super.dispatchTouchEvent(ev);
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (getTouchedSide(ev.getX(), ev.getY()) != -1) {
                swapped = false;
                posX = ev.getX() - linearLayout.getX();
                posY = ev.getY() - linearLayout.getY();
            }
            else swapped = true;
        }
        else if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
            swipe(ev.getX(), ev.getY());
        }
        else if (ev.getActionMasked() == MotionEvent.ACTION_UP) {
            if (Math.abs(ev.getX() - linearLayout.getX() - posX) + Math.abs(ev.getY() - linearLayout.getY() - posY) < 10) {
                swapped = true;
                currentSide = getTouchedSide(ev.getX(), ev.getY());
                if (currentSide == -1) {
                    colorList.setAdapter(new ColorAdaptor(this, R.layout.colorcontainer, linearLayout.getWidth()/12, getBlock()));
                    bgSeekBar.setProgress(10);
                    noteSeekBar.setProgress(10);
                    bgSeekBar.setProgressDrawable(ContextCompat.getDrawable(this, R.drawable.colorbar_gray));
                    noteSeekBar.setProgressDrawable(ContextCompat.getDrawable(this, R.drawable.colorbar_gray));
                    isDisabled.setVisibility(View.VISIBLE);
                    colorList.setEnabled(false);
                    bgSeekBar.setEnabled(false);
                    noteSeekBar.setEnabled(false);
                }
                else {
                    if (needToRefresh) refresh();
                    isDisabled.setVisibility(View.INVISIBLE);
                    colorList.setEnabled(true);
                    bgSeekBar.setEnabled(true);
                    noteSeekBar.setEnabled(true);
                    colorList.setAdapter(new ColorAdaptor(this, R.layout.colorcontainer, linearLayout.getWidth()/12, getBlock()));
                    colorList.setSelection(color[currentSide]);
                    bgSeekBar.setProgressDrawable(getGradientDrawable(color[currentSide], true));
                    noteSeekBar.setProgressDrawable(getGradientDrawable(color[currentSide], true));
                    bgSeekBar.setProgress((int) (10 * saturationBG[currentSide]));
                    noteSeekBar.setProgress((int) (10 * saturationNote[currentSide]));
                    /*for (int i = 0; i < PlayScreen.SIDE_NUM; i++) {
                        if (i != currentSide) isClicked[i].setVisibility(View.INVISIBLE);
                        else isClicked[i].setVisibility(View.VISIBLE);
                    }*/
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private int getTouchedSide(float x, float y) {
        x -= linearLayout.getX();
        y -= linearLayout.getY();
        float width = linearLayout.getWidth() * 3.0f / 4;
        float height = linearLayout.getHeight();
        if (Math.abs(x - width/2) / width + Math.abs(y - height/2) / height < 0.5) {
            if (x - width/2 < 0 && y - height/2 < 0) return 0;
            else if (x - width/2 >= 0 && y - height/2 < 0) return 1;
            else if (x - width/2 >= 0 && y - height/2 >= 0) return 2;
            else return 3;
        }
        else return -1;
    }

    private void swipe(float x, float y) {
        x -= linearLayout.getX();
        y -= linearLayout.getY();
        float width = linearLayout.getWidth() * 3.0f / 4;
        float height = linearLayout.getHeight();
        double swipeDistanceX = (double) width/10;
        double swipeDistanceY = (double) height/10;
        if (!swapped) {
            if (0 <= posX && posX < (double) width/2 && 0 <= posY && posY < (double) height/2) {
                if (x - posX > swipeDistanceX) {
                    swapped = true;
                    swap(1);
                }
                else if (y - posY > swipeDistanceY) {
                    swapped = true;
                    swap(0);
                }
            } else if (width/2 <= posX && posX < (double) width && 0 <= posY && posY < (double) height/2) {
                if (x - posX < -swipeDistanceX) {
                    swapped = true;
                    swap(1);
                }
                else if (y - posY > swipeDistanceY) {
                    swapped = true;
                    swap(2);
                }
            } else if (width/2 <= posX && posX < (double) width && height/2 <= posY && posY < (double) height) {
                if (x - posX < -swipeDistanceX) {
                    swapped = true;
                    swap(3);
                }
                else if (y - posY < -swipeDistanceY) {
                    swapped = true;
                    swap(2);
                }
            } else if (0 <= posX && posX < (double) width/2 && height/2 <= posY && posY < (double) height) {
                if (x - posX > swipeDistanceX) {
                    swapped = true;
                    swap(3);
                } else if (y - posY < -swipeDistanceY) {
                    swapped = true;
                    swap(0);
                }
            }
        }
        if (swapped) needToRefresh = true;
    }

    private void swap(int i) {
        Matrix matrix = new Matrix();
        if (i%2==1) matrix.setScale(-1,1);
        else matrix.setScale(1,-1);
        BitmapDrawable drawable = (BitmapDrawable) colorSide[i].getDrawable();
        Bitmap bitmap1 = drawable.getBitmap();
        drawable = (BitmapDrawable) colorSide[(i+PlayScreen.SIDE_NUM-1)%PlayScreen.SIDE_NUM].getDrawable();
        Bitmap bitmap2 = drawable.getBitmap();
        bitmap1 = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(), bitmap1.getHeight(), matrix, false);
        bitmap2 = Bitmap.createBitmap(bitmap2, 0, 0, bitmap2.getWidth(), bitmap2.getHeight(), matrix, false);
        colorSide[i].setImageBitmap(bitmap2);
        colorSide[(i+PlayScreen.SIDE_NUM-1)%PlayScreen.SIDE_NUM].setImageBitmap(bitmap1);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spectrum);

        saturationBG = new float[PlayScreen.SIDE_NUM];
        saturationNote = new float[PlayScreen.SIDE_NUM];
        finishFlag = false;
        needToRefresh = false;

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

        linearLayout = findViewById(R.id.spectrumLayout);

        ViewGroup.LayoutParams params = linearLayout.getLayoutParams();
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(size);
        params.height = Math.min(size.y, size.x * 9 / 16);
        params.width = 16 * params.height / 9;

        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(params));
        linearLayout.setX((float) size.x/2 - (float) params.width/2);
        linearLayout.setY((float) size.y/2 - (float) params.height/2);

        colorSide = new ImageView[PlayScreen.SIDE_NUM];
        note = new ImageView[PlayScreen.SIDE_NUM];
        isClicked = new ImageView[PlayScreen.SIDE_NUM];

        isDisabled = findViewById(R.id.isDisabled);
        colorList = findViewById(R.id.colorList);
        noteSeekBar = findViewById(R.id.noteSeekBar);
        bgSeekBar = findViewById(R.id.bgSeekBar);
        colorSide[0] = findViewById(R.id.colorSide0);
        colorSide[1] = findViewById(R.id.colorSide1);
        colorSide[2] = findViewById(R.id.colorSide2);
        colorSide[3] = findViewById(R.id.colorSide3);
        note[0] = findViewById(R.id.note0);
        note[1] = findViewById(R.id.note1);
        note[2] = findViewById(R.id.note2);
        note[3] = findViewById(R.id.note3);
        refreshButton = findViewById(R.id.refreshButton);
        spectrumBack = findViewById(R.id.spectrumBack);
        float volume = DataManager.getData(this).getSystemVolume();
        tapSound = MediaPlayer.create(this, R.raw.tap);
        tapSound.setVolume(volume, volume);

        for (int i = 0; i < PlayScreen.SIDE_NUM; i++) {
            color[i] = DataManager.getData(this).getColor(i);
            saturationBG[i] = DataManager.getData(this).getSaturationBG(i);
            saturationNote[i] = DataManager.getData(this).getSaturationNote(i);
        }

        for (int i = 0; i < PlayScreen.SIDE_NUM; i++) {
            colorSide[i].setImageBitmap(retouchBG(color[i], saturationBG[i], i));
            note[i].setImageBitmap(retouchNote(color[i], saturationNote[i], i));
        }

        colorList = findViewById(R.id.colorList);
        colorList.setAdapter(new ColorAdaptor(this, R.layout.colorcontainer, params.width/12, getBlock()));
        colorList.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            tapSound.start();
            if (currentSide == -1 || color[currentSide] == position) return;
            for (int i = 0; i < PlayScreen.SIDE_NUM; i++)
                if (i != currentSide && color[i] ==  position) return;
            for (int i = 0; i < parent.getChildCount(); i++)
                parent.getChildAt(i).findViewById(R.id.isSelected).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.isSelected).setVisibility(View.VISIBLE);
            bgSeekBar.setProgressDrawable(getGradientDrawable(position, true));
            noteSeekBar.setProgressDrawable(getGradientDrawable(position, true));
            color[currentSide] = position;
            colorSide[currentSide].setImageBitmap(retouchBG(color[currentSide], saturationBG[currentSide], currentSide));
            note[currentSide].setImageBitmap(retouchNote(color[currentSide], saturationNote[currentSide], currentSide));
        });

        bgSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (currentSide != -1) {
                    saturationBG[currentSide] = (float) progress / 10;
                    colorSide[currentSide].setImageBitmap(retouchBG(color[currentSide], saturationBG[currentSide], currentSide));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        noteSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (currentSide != -1) {
                    saturationNote[currentSide] = (float) progress / 10;
                    note[currentSide].setImageBitmap(retouchNote(color[currentSide], saturationNote[currentSide], currentSide));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        bgSeekBar.setProgressDrawable(getGradientDrawable(-1, false));
        noteSeekBar.setProgressDrawable(getGradientDrawable(-1, false));

        refreshButton.setOnClickListener((view) -> {
            tapSound.start();
            for (int i = 0; i < PlayScreen.SIDE_NUM; i++) {
                color[i] = DataManager.getData(this).getColor(i);
                saturationBG[i] = DataManager.getData(this).getSaturationBG(i);
                saturationNote[i] = DataManager.getData(this).getSaturationNote(i);
                colorSide[i].setImageBitmap(retouchBG(color[i], saturationBG[i], i));
                note[i].setImageBitmap(retouchNote(color[i], saturationNote[i], i));
            }
            bgSeekBar.setProgress((int) (10 * saturationBG[currentSide]));
            noteSeekBar.setProgress((int) (10 * saturationNote[currentSide]));
        });

        spectrumBack.setOnClickListener((view) -> {
            tapSound.start();
            for (int i = 0; i < PlayScreen.SIDE_NUM; i++) {
                DataManager.getData(this).setColor(i, color[i]);
                DataManager.getData(this).setSaturationBG(i, saturationBG[i]);
                DataManager.getData(this).setSaturationNote(i, saturationNote[i]);
            }
            tapSound.setOnCompletionListener(MediaPlayer::release);
            finishFlag = true;
            Intent it = new Intent(this, StartScreen.class);
            it.putExtra("NewStart", false);
            it.putExtra("BackFromSpectrum", true);
            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this,
                    android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
            startActivity(it, bundle);
            finish();
        });

        bgSeekBar.setProgress(10);
        noteSeekBar.setProgress(10);
        colorList.setEnabled(false);
        bgSeekBar.setEnabled(false);
        noteSeekBar.setEnabled(false);
    }

    private Bitmap retouchBG(int color, float saturation, int flip) {
        Bitmap it = BitmapFactory.decodeResource(this.getResources(), this.getResources().getIdentifier(String.format(Locale.US, "color%02d", color), "drawable", this.getPackageName()));
        return retouch(this, it, saturation, flip);
    }

    private Bitmap retouchNote(int color, float saturation, int flip) {
        Bitmap it = BitmapFactory.decodeResource(this.getResources(), this.getResources().getIdentifier(String.format(Locale.US, "note%02d", color), "drawable", this.getPackageName()));
        return retouch(this, it, saturation, flip);
    }

    public static Bitmap retouch(Context context, Bitmap bitmap, float saturation, int flip) {
        if (flip > 1) {
            Matrix matrix = new Matrix();
            matrix.setScale(1, -1);
            bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix, false);
        }
        if (0 < flip && flip < 3) {
            Matrix matrix = new Matrix();
            matrix.setScale(-1, 1);
            bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix, false);
        }
        RenderScript renderScript = RenderScript.create(context);
        ScriptC_retouch scriptC = new ScriptC_retouch(renderScript);
        scriptC.set_saturation(saturation);
        Allocation allocation = Allocation.createFromBitmap(renderScript, bitmap);
        scriptC.forEach_retouch(allocation, allocation);
        allocation.syncAll(Allocation.USAGE_SHARED);
        allocation.destroy();
        scriptC.destroy();
        renderScript.destroy();
        return bitmap;
    }

    private void refresh() {
        needToRefresh = false;
        for (int i = 0; i < PlayScreen.SIDE_NUM; i++) {
            colorSide[i].setImageBitmap(retouchBG(color[i], saturationBG[i], i));
            note[i].setImageBitmap(retouchNote(color[i], saturationNote[i], i));
        }
    }

    private ArrayList<Integer> getBlock() {
        ArrayList<Integer> it = new ArrayList<>();
        if (currentSide == -1) return it;
        for (int i = 0; i < PlayScreen.SIDE_NUM; i++) if (i != currentSide) it.add(color[i]);
        return it;
    }

    private Drawable getGradientDrawable(int color, boolean flag) {
        int start;
        int end;
        if (flag) {
            start = Color.WHITE;
            end = Color.HSVToColor(new float[]{color * 30, 1, 1});
        }
        else {
            start = Color.LTGRAY;
            end = Color.LTGRAY;
        }
        return new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{start, end});
    }

    @Override
    protected void onResume() {
        StartScreen.bgm.start();
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
    protected void onStop() {
        if (!finishFlag) StartScreen.bgm.pause();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        spectrumBack.performClick();
    }
}

class ColorAdaptor extends BaseAdapter {

    LayoutInflater inflater;
    Context context;
    int resource;
    int size;
    ArrayList<Integer> block;



    public ColorAdaptor(@NonNull Context context, int resource, int size, ArrayList<Integer> block) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.resource = resource;
        this.size = size;
        this.block = block;
    }

    @Override
    public int getCount() {
        return SpectrumAlign.COLOR_NUM;
    }

    @Override
    public float[] getItem(int position) {
        return new float[] {position * 30,1,1};
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View view;
        if (convertView == null) view = inflater.inflate(resource, viewGroup, false);
        else view = convertView;

        ConstraintLayout constraintLayout = view.findViewById(R.id.constraintLayout);
        if (SpectrumAlign.currentSide != -1 && position == SpectrumAlign.color[SpectrumAlign.currentSide])
            view.findViewById(R.id.isSelected).setVisibility(View.VISIBLE);
        else view.findViewById(R.id.isSelected).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.layoutColor).setBackgroundColor(Color.HSVToColor(getItem(position)));
        view.findViewById(R.id.isBlocked).setVisibility(View.INVISIBLE);
        for (Integer it : block) if (it == position) view.findViewById(R.id.isBlocked).setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams params = constraintLayout.getLayoutParams();
        params.height = size;
        params.width = size;
        constraintLayout.setLayoutParams(new ConstraintLayout.LayoutParams(params));
        return view;
    }
}
