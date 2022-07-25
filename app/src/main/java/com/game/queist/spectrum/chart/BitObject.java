package com.game.queist.spectrum.chart;

public abstract class BitObject {
    private double bit;

    public BitObject(double bit) {
        this.bit = bit;
    }

    public double getBit() {
        return bit;
    }

    public boolean equals(BitObject bo) {
        return bo.bit == this.bit;
    }
}
