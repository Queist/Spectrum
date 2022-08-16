package com.game.queist.spectrum.chart;

public class EffectFlag {

    public final static double TOTAL_LIFETIME = 0.5;
    public final static String MISS = "MISS", BAD = "BAD", GOOD = "GOOD", PERFECT = "PERFECT";
    private String effect;
    private int lifeTime;
    private double position1;
    private int quadrant;
    private double position2;

    private double rotateAngle;
    private double noteBit;
    private double currBit;

    public EffectFlag(String effect, double position1, int quadrant, double position2, double noteBit, double currBit, double rotateAngle) {
        this.effect = effect;
        this.position1 = position1;
        this.quadrant = quadrant;
        this.position2 = position2;
        this.lifeTime = 0;
        this.rotateAngle = rotateAngle;
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

    public int getLifeTime() {
        return lifeTime;
    }

    public String getEffect() {
        return effect;
    }

    public boolean checkTime() {
        return lifeTime >= TOTAL_LIFETIME;
    }

    public double getRotateAngle() {
        return rotateAngle;
    }
}
