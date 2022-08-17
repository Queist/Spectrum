package com.game.queist.spectrum.chart;

public class LongNote extends Note {
    private double endBit;

    public double getWorldWidth() {
        return worldWidth;
    }

    public void setWorldWidth(double worldWidth) {
        this.worldWidth = worldWidth;
    }

    private double worldWidth;

    public LongNote(String kind, double pos1, double bit, double pos2, int color, double endBit) {
        super(kind, pos1, bit, pos2, color);
        this.endBit = endBit;
    }

    public double getEndBit() {
        return endBit;
    }
}
