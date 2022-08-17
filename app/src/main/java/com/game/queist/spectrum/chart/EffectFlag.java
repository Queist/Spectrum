package com.game.queist.spectrum.chart;

public class EffectFlag {

    public final static double TOTAL_LIFETIME = 0.5;
    public final static String MISS = "MISS", BAD = "BAD", GOOD = "GOOD", PERFECT = "PERFECT";
    private String effect;
    private double lifeTime;
    private double start;
    private double range;

    private double rotateAngle;

    public EffectFlag(String effect, double start, double range, double rotateAngle) {
        this.effect = effect;
        this.start = start;
        this.range = range;
        this.lifeTime = 0;
        this.rotateAngle = rotateAngle;
    }

    public double getStart() {
        return start;
    }

    public double getRange() {
        return range;
    }

    public double getLifeTime() {
        return lifeTime;
    }

    public String getEffect() {
        return effect;
    }

    public boolean lifeOver() {
        return lifeTime >= TOTAL_LIFETIME;
    }

    public double getRotateAngle() {
        return rotateAngle;
    }

    public void update(double tickSeconds) { lifeTime += tickSeconds; }
}
