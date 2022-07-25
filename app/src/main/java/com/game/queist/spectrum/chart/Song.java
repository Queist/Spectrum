package com.game.queist.spectrum.chart;

import com.game.queist.spectrum.activities.SongSelect;

public class Song {
    private String name;
    private String artist;
    private int coverID;
    private int thumbID;
    private double minBPM, maxBPM;
    private int[] difficulty;
    private boolean[] isClicked;
    private int currentDiff;

    public Song(String name, String artist, int coverID, int thumbID, double minBPM, double maxBPM, int[] difficulty) {
        this.name = name;
        this.artist = artist;
        this.coverID = coverID;
        this.thumbID = thumbID;
        this.maxBPM = maxBPM;
        this.minBPM = minBPM;
        this.difficulty = difficulty;
        this.isClicked = new boolean[SongSelect.DIFF_NUM];
        this.currentDiff = -1;
        for (int i = 0; i < SongSelect.DIFF_NUM; i++) {
            isClicked[i] = false;
        }
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public int getCoverID() {
        return coverID;
    }

    public int getThumbID() {
        return thumbID;
    }

    public int getDifficulty(int i) {
        return difficulty[i];
    }

    public int getDifficulty() {
        return difficulty[currentDiff];
    }

    public int getDifficultyLength() {
        return difficulty.length;
    }

    public String toStringBPM() {
        return minBPM == maxBPM ? "BPM " + (int) minBPM : "BPM " + (int) minBPM + "~" + (int) maxBPM;
    }

    public boolean isClicked(int i) {
        return isClicked[i];
    }

    /*public void clicked(int i) {
        isClicked[i] = !isClicked[i];
    }

    public void resetClicked() {
        for (int i = 0; i<SongSelect.DIFF_NUM; i++) {
            isClicked[i] = false;
        }
    }

    public int getClickedDifficulty() {
        for (int i = 0; i<SongSelect.DIFF_NUM; i++) {
            if (isClicked[i]) {
                return i;
            }
        }
        return -1;
    }*/

    public int getCurrentDiff() {
        return currentDiff;
    }

    public void changeDiff() {
        currentDiff = (currentDiff + 1) % SongSelect.DIFF_NUM;
        if (getDifficulty() == -1) changeDiff();
    }

    public void setCurrentDiff(int i) {
        currentDiff = i;
    }

}
