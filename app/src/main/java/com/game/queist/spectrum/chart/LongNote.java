package com.game.queist.spectrum.chart;

public class LongNote extends Note {
    private double duration;

    public LongNote(String kind, double pos1, double bit, double pos2, int color, double duration) {
        super(kind, pos1, bit, pos2, color);
        this.duration = duration;
    }

    public double getDuration() {
        return duration;
    }
}
