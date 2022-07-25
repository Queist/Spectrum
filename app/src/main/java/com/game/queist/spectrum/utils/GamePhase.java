package com.game.queist.spectrum.utils;

public class GamePhase {
    public static final int START = 0;
    public static final int PAUSE = 1;
    public static final int END = 2;
    public static final int PLAY = 3;
    public static final int QUIT = 4;
    private int flag;

    public GamePhase() {
        this.flag = START;
    }

    public int getFlag() {
        return this.flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
