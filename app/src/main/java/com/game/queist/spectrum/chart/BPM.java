package com.game.queist.spectrum.chart;

public class BPM extends BitObject {
    private double value;

    public BPM(double value, double bit) {
        super(bit);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

}
