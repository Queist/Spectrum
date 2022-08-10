package com.game.queist.spectrum.chart;

public class Note extends BitObject {
    private String kind;
    private double pos1;
    private double pos2;
    private int color; //Color color;
    public final static String TAB = "Tab";
    public final static String SLIDE = "Slide";

    public Note(String kind, double pos1, double bit, double pos2, int color) {
        super(bit);
        this.kind = kind;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.color = color;
    }

    public String getKind() {
        return kind;
    }

    public double getPosition1() {
        return pos1;
    }

    public double getPosition2() {
        return pos2;
    }

    public int getColor() {
        return color;
    }

    public boolean equals(Note note) {
        return (super.equals(note) && note.kind.equals(this.kind) && note.pos1 == this.pos1 && note.pos2 == this.pos2 && note.color == this.color);
    }
}
