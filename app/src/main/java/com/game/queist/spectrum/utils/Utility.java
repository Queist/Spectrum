package com.game.queist.spectrum.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.opengl.GLES30;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.game.queist.spectrum.chart.EffectFlag;
import com.game.queist.spectrum.activities.PlayScreen;
import com.game.queist.spectrum.chart.Note;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Locale;

public class Utility {
    public static double screenRate = 0;
    private static int[] color = new int[PlayScreen.SIDE_NUM];
    private static float[] saturation = new float[PlayScreen.SIDE_NUM];

    public static void initNoteColor(Context context) {
        for (int i = 0; i < PlayScreen.SIDE_NUM; i++) {
            color[i] = DataManager.getData(context).getColor(i);
            saturation[i] = DataManager.getData(context).getSaturationNote(i);
        }
    }

    public static void setScreenRate(double screenRate) {
        Utility.screenRate = screenRate;
    }

    public static double[] getCoefficients(int line) {
        double[] coefficient = new double[2];
        coefficient[0] = (line%2==0) ? -screenRate : screenRate;
        coefficient[1] = (line/2==0) ? -1 : 1;
        return coefficient;
    }

    public static int getRGB(int side) {
        if (side == -1) return Color.WHITE;
        return Color.HSVToColor(new float[]{color[side] * 30, saturation[side], 1.0f});
    }

    public static int[] getRGB(int color, int white) {
        int[] RGB = new int[3];
        switch (color) {
            case 0 :
                RGB[0] = 255;
                RGB[1] = white;
                RGB[2] = white;
                break;
            case 1 :
                RGB[0] = 255;
                RGB[1] = 255;
                RGB[2] = white;
                break;
            case 2 :
                RGB[0] = white;
                RGB[1] = 255;
                RGB[2] = white;
                break;
            case 3 :
                RGB[0] = white;
                RGB[1] = white;
                RGB[2] = 255;
                break;
            default :
                RGB[0] = 255;
                RGB[1] = 255;
                RGB[2] = 255;
        }
        return RGB;
    }

    public static int getEquivalenceLineColor() {
        return Color.argb(128, 128, 128, 128);
    }

    public static ArrayList<Note> getNextNotes (ArrayList<Note> notes) {
        ArrayList<Note> temp = new ArrayList<>();
        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);
            if (temp.size() == 0) temp.add(note);
            else if (note.getBit() == temp.get(0).getBit()) temp.add(note);
            else break;
        }
        return temp;
    }

    public static String difficultyToString(int select) {
        switch (select) {
            case 0 :
                return "easy";
            case 1 :
                return "normal";
            case 2 :
                return "hard";
            case 3 :
                return "extreme";
            default :
                return null;
        }
    }

    public static int difficultyToColor(String difficulty) {
        switch (difficulty) {
            case "easy" :
                return Color.rgb(0, 224, 0);
            case "normal" :
                return Color.rgb(224, 224, 0);
            case "hard" :
                return Color.rgb(224, 0, 0);
            case "extreme" :
                return Color.rgb(0, 224, 224);
            default :
                return -1;
        }
    }

    public static int[] nameToInteger(String[] difficultyName) {
        int[] difficulty = new int[difficultyName.length];
        for (int i = 0; i < difficultyName.length; i++)
            difficulty[i] = difficultyToInteger(difficultyName[i]);
        return difficulty;
    }

    public static int difficultyToInteger(String difficulty) {
        switch (difficulty) {
            case "easy" :
                return 0;
            case "normal" :
                return 1;
            case "hard" :
                return 2;
            case "extreme" :
                return 3;
            default :
                return -1;
        }
    }

    public static int judgeToInteger(String judge) {
        switch (judge) {
            case EffectFlag.MISS :
                return 0;
            case EffectFlag.BAD :
                return 1;
            case EffectFlag.GOOD :
                return 2;
            case EffectFlag.PERFECT :
                return 3;
            default :
                return -1;
        }
    }

    public static void startCountAnimation(int value, TextView textView, long duration) {
        ValueAnimator animator = ValueAnimator.ofInt(0, value);
        animator.setDuration(duration);
        animator.addUpdateListener((animation) -> textView.setText(String.format(Locale.US,"%08d", (int) animation.getAnimatedValue())));
        animator.start();
    }

    public static void startCountAnimation(int start, int end, TextView textView, long duration) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(duration);
        animator.addUpdateListener((animation) -> textView.setText(String.format(Locale.US,"%08d", (int) animation.getAnimatedValue())));
        animator.start();
    }

    public static void startCountAnimation(int value, TextView textView, long duration, MediaPlayer mediaPlayer) {
        ValueAnimator animator = ValueAnimator.ofInt(0, value);
        animator.setDuration(duration);
        animator.addUpdateListener((animation) -> {
            if (Integer.parseInt((String) textView.getText()) != (int) animation.getAnimatedValue()) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    mediaPlayer.seekTo(0);
                }
                mediaPlayer.start();
            }
            textView.setText(String.format(Locale.US,"%08d", (int) animation.getAnimatedValue()));
        });
        animator.start();
    }

    public static void setMaskFilter(TextView textView) {
        textView.getPaint().setMaskFilter(new BlurMaskFilter(textView.getTextSize()/5, BlurMaskFilter.Blur.SOLID));
    }

    public static void setMaskFilter(Button button) {
        button.getPaint().setMaskFilter(new BlurMaskFilter(button.getTextSize()/5, BlurMaskFilter.Blur.SOLID));
    }

    public static void setMaskFilter(TextView textView, int rate) {
        if (rate == 0) textView.getPaint().setMaskFilter(null);
        else textView.getPaint().setMaskFilter(new BlurMaskFilter(textView.getTextSize()/rate, BlurMaskFilter.Blur.SOLID));
    }

    public static String scoreToRank(int score) {
        //360*324
        //2700->1152->810->360
        if (score == 10000000) {
            return "rank_ss";
        } else if (score >= 9750000) {
            return "rank_s";
        } else if (score >= 9500000) {
            return "rank_a";
        } else if (score >= 9000000) {
            return "rank_b";
        } else if (score >= 8000000) {
            return "rank_c";
        } else if (score >= 7000000) {
            return "rank_d";
        } else if (score >= 5000000) {
            return "rank_e";
        } else {
            return "rank_f";
        }
    }

    public static int loadShader(int type, String shaderCode) {
        // 빈 쉐이더를 생성하고 그 인덱스를 할당.
        int shader = GLES30.glCreateShader(type);

        // *컴파일 결과를 받을 공간을 생성.
        IntBuffer compiled = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
        String shaderType;

        // *컴파일 결과를 출력하기 위해 쉐이더를 구분.
        if(type == GLES30.GL_VERTEX_SHADER)
            shaderType = "Vertex";
        else if(type == GLES30.GL_FRAGMENT_SHADER)
            shaderType = "Fragment";
        else
            shaderType = "Unknown";

        // 빈 쉐이더에 소스코드를 할당.
        GLES30.glShaderSource(shader, shaderCode);
        // 쉐이더에 저장 된 소스코드를 컴파일
        GLES30.glCompileShader(shader);

        // *컴파일 결과 오류가 발생했는지를 확인.
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled);
        // *컴파일 에러가 발생했을 경우 이를 출력.
        if(compiled.get(0) == 0) {
            GLES30.glGetShaderiv(shader,GLES30.GL_INFO_LOG_LENGTH,compiled);
            if (compiled.get(0) > 1){
                Log.e("Shader", shaderType + " shader: " + GLES30.glGetShaderInfoLog(shader));
            }
            GLES30.glDeleteShader(shader);
            Log.e("Shader", shaderType + " shader compile error.");
        }

        // 완성된 쉐이더의 인덱스를 리턴.
        return shader;
    }
}
