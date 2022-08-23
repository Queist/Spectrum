package com.game.queist.spectrum.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.game.queist.spectrum.R;
import com.game.queist.spectrum.chart.Song;
import com.game.queist.spectrum.utils.Utility;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class LoadingScreen extends AppCompatActivity {

    LinearLayout linearLayout;
    ImageView loadingIcon;
    ArrayList<Song> songList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);

        setContentView(R.layout.loading);

        linearLayout = findViewById(R.id.loadingLayout);
        loadingIcon = findViewById(R.id.loadingIcon);

        Glide.with(this).load(R.drawable.loading).into(loadingIcon);

        ViewGroup.LayoutParams params = linearLayout.getLayoutParams();
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(size);
        params.height = Math.min(size.y, size.x * 9 / 16);
        params.width = 16 * params.height / 9;

        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(params));
        linearLayout.setX((float) size.x / 2 - (float) params.width / 2);
        linearLayout.setY((float) size.y / 2 - (float) params.height / 2);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0 :
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        finishAndRemoveTask();
                    }
                }

                AsyncTask<Context, Long, Boolean> asyncTask = new AsyncTask<Context, Long, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Context... contexts) {
                        Utility.writeFile(contexts[0], String.format("song_list"), "raw");
                        try {
                            InputStream inputStream = getResources().openRawResource(R.raw.song_list);
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

                            String readLine;
                            while ((readLine = bufferedReader.readLine()) != null) {
                                String[] attr = readLine.split("\t");
                                String name = attr[0].toLowerCase().replace(" ", "_");

                                Utility.writeFile(contexts[0], String.format("%s", name), "raw");
                                Utility.writeFile(contexts[0], String.format("%s_thumb", name), "raw");
                                Utility.writeFile(contexts[0], String.format("%s", name), "drawable");

                                String[] difficultyValue = attr[4].split(",");
                                for (int i = 0; i < SongSelect.DIFF_NUM; i++) {
                                    if (Integer.parseInt(difficultyValue[i]) != -1) {
                                        Utility.writeFile(contexts[0],
                                                String.format("%s_%s", name, Utility.difficultyToString(i)), "raw");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }


                };

                asyncTask.execute(this);

                break;
            default:
                finishAndRemoveTask();
                break;
        }
    }
}
