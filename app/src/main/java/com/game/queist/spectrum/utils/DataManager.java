package com.game.queist.spectrum.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class DataManager {

    public static Data getData(Context context) {
        return new Data(context);
    }

    public static class Data {

        private SharedPreferences sharedPreferences;


        public Data(Context context) {
            sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        }

        public void setRecentPlayed(String value) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("recent", value);
            editor.apply();
        }

        public void setSystemVolume(float value) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("system_volume", value);
            editor.apply();
        }

        public void setMusicVolume(float value) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("music_volume", value);
            editor.apply();
        }

        public void setScore(String songName, int value, int difficulty) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(songName + "_" + difficulty, value);
            editor.apply();
        }

        public void setOffset(long value) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("offset", value);
            editor.apply();
        }

        public void setOffsetDetailed(String songName, long value) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("offsetDetailed"+songName, value);
            editor.apply();
        }

        public void setSpeed(String songName, float value) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("speed"+songName, value);
            editor.apply();
        }

        public void setRecentPlayedDiff(String songName, int value) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("diff"+songName, value);
            editor.apply();
        }

        public void setColor(int side, int value) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("color"+side, value);
            editor.apply();
        }

        public void setSaturationBG(int side, float value) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("saturation"+"BG"+side, value);
            editor.apply();
        }

        public void setSaturationNote(int side, float value) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("saturation"+"note"+side, value);
            editor.apply();
        }

        public long getOffset() {
            return sharedPreferences.getLong("offset", 0);
        }

        public long getOffsetDetailed(String songName) {
            if (songName == null) return 0;
            if (songName.equals(getRecentPlayed()))
                return sharedPreferences.getLong("offsetDetailed"+songName,0);
            return sharedPreferences.getLong("offsetDetailed"+songName, getOffsetDetailed(getRecentPlayed()));
        }

        public String getRecentPlayed() {
            return sharedPreferences.getString("recent", null);
        }

        public float getSystemVolume() {
            return sharedPreferences.getFloat("system_volume", 1);
        }

        public float getMusicVolume() {
            return sharedPreferences.getFloat("music_volume", 1);
        }

        public int getScore(String songName, int difficulty) {
            return sharedPreferences.getInt(songName + "_" + difficulty, -1);
        }

        public float getSpeed(String songName) {
            if (songName == null) return 1;
            if (songName.equals(getRecentPlayed()))
                return sharedPreferences.getFloat("speed"+songName,1);
            return sharedPreferences.getFloat("speed"+songName, getSpeed(getRecentPlayed()));
        }

        public int getRecentPlayedDiff(String songName) {
            return sharedPreferences.getInt("diff"+songName, 0);
        }

        public int getColor(int side) {
            return sharedPreferences.getInt("color"+side, 3 * side);
        }

        public float getSaturationBG(int side) {
            return sharedPreferences.getFloat("saturation"+"BG"+side, 1.0f);
        }

        public float getSaturationNote(int side) {
            return sharedPreferences.getFloat("saturation"+"note"+side, 1.0f);
        }

        public void removeKey(String key) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(key);
            editor.apply();
        }

        public void clear() {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
        }

    }
}
