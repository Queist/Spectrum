package com.game.queist.spectrum.chart;

public class EffectFlag {

    public final static int TOTAL_FRAME = 40;
    public final static String MISS = "MISS", BAD = "BAD", GOOD = "GOOD", PERFECT = "PERFECT";
    private String effect;
    private int frame;
    private double position1;
    private int quadrant;
    private double position2;
    private double noteBit;
    private double currBit;

    public EffectFlag(String effect, double position1, int quadrant, double position2, double noteBit, double currBit) {
        this.effect = effect;
        this.position1 = position1;
        this.quadrant = quadrant;
        this.position2 = position2;
        this.frame = 0;
        this.noteBit = noteBit;
        this.currBit = currBit;
    }

    public int getQuadrant() {
        return quadrant;
    }

    public double getPosition1() {
        return position1;
    }

    public double getPosition2() {
        return position2;
    }

    public double getNoteBit() {
        return noteBit;
    }

    public double getCurrBit() {
        return currBit;
    }

    public int getFrame() {
        return frame;
    }

    public String getEffect() {
        return effect;
    }

    public void update() {
        frame++;
    }

    public boolean checkFrame() {
        return frame >= TOTAL_FRAME;
    }
}
