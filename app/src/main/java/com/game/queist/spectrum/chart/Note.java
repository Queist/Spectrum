package com.game.queist.spectrum.chart;

public class Note extends BitObject {
    private String kind;
    private double start;
    private double range;
    private int color; //Color color;
    public final static String TAB = "Tab";
    public final static String SLIDE = "Slide";
    public final static String LONG = "Long";

    public Note(String kind, double start, double range, double bit, int color) {
        super(bit);
        this.kind = kind;
        this.start = start;
        this.range = range;
        this.color = color;
    }

    public String getKind() {
        return kind;
    }

    public double getStart() {
        return start;
    }

    public double getRange() {
        return range;
    }

    public int getColor() {
        return color;
    }

    public boolean equals(Note note) {
        return (super.equals(note) && note.kind.equals(this.kind) && note.start == this.start && note.range == this.range && note.color == this.color);
    }
}
