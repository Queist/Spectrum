package com.game.queist.spectrum;

import android.util.Pair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * >> BPM 구성 요소 : 1. Symbol "BPM" / 2. 값 / 3. 박자
 * >> Note 구성 요소 : 1. Symbol "Note" / 2. 노트 종류 / 3. 사분면 / 4.시작 위치 / 5. 끝 위치 / 6. 박자 / 7. 색깔
 *  가능한 색 종류 : White : -1 / Red : 0 / YellowGreen : 1 / Cyan : 2 / Violet : 3
 *  가능한 노트 종류 : Auto, Slide, Tab
 *  위치는 0~10 사이의 값
 *  박자는 비트 값을 기록하면 됨. (주어진 bpm의 4분음표가 한박)
 * >> 사분면 별 위치 값 분포
 *  0사분면(초기 빨강색 사분면의 위치) : 0 -> 10
 *  1사분면(초기 잔디색 사분면의 위치) : 10 <- 0
 *  2사분면(초기 하늘색 사분면의 위치) : 10 <- 0
 *  3사분면(초기 보라색 사분면의 위치) : 0 -> 10
 *  예시 : 2사분면에 살짝 "왼쪽"에 노트를 배치하고 싶으면 시작위치 5, 끝 위치 8 정도로 설정하면 됨 (가장 왼쪽이 10이므로)
 * >> 각 속성들을 Tab 키를 눌러서 구분하여 작성하시면 됨.
 * >> 채보 txt 파일 열어서 보시면 이해가 쉬우실 것
 *
 */

public class Chart {

    private ArrayList<Note>[] notes;
    private ArrayList<Pair<Integer, Double>> helper;
    private ArrayList<Double> equivalenceLines;
    private ArrayList<BPM> bpms;
    private int totalNotes;
    private int offset;
    static Comparator<BitObject> sortToBit = (b1, b2) -> Double.compare(b1.getBit(), b2.getBit());

    Chart(InputStream chartFile) {
        totalNotes = 0;
        notes = new ArrayList[PlayScreen.SIDE_NUM];
        for (int i=0; i<PlayScreen.SIDE_NUM; i++) notes[i] = new ArrayList<>();
        bpms = new ArrayList<>();
        helper = new ArrayList<>();
        equivalenceLines = new ArrayList<>();
        offset = 0;
        readChart(chartFile);
    }

    private void readChart(InputStream chartFile) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(chartFile, StandardCharsets.UTF_8));
            String readLine;
            while ((readLine = bufferedReader.readLine()) != null) {
                String[] attr = readLine.split("\t");
                switch (attr[0]) {
                    case "Offset" :
                        offset = Integer.parseInt(attr[1]);
                        break;

                    case "BPM" :
                        bpms.add(new BPM(Double.parseDouble(attr[1]), Double.parseDouble(attr[2])));
                        break;

                    case "Note" :
                        Integer side = Integer.parseInt(attr[2]);
                        Note note = new Note(attr[1], Double.parseDouble(attr[3]), Double.parseDouble(attr[5]),
                                Double.parseDouble(attr[4]), Integer.parseInt(attr[6]));
                        notes[side].add(note);
                        if (note.getKind().equals(Note.TAB)) helper.add(new Pair<>(side, note.getBit()));
                        break;

                    default :
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            double recentBit = -8000;
            int recentSide = -1;
            for (int i = 0; i < PlayScreen.SIDE_NUM; i++) { totalNotes += notes[i].size(); }
            for (int i = 0; i < helper.size(); i++) {
                if (recentBit != helper.get(i).second) {
                    recentBit = helper.get(i).second;
                    recentSide = helper.get(i).first;
                }
                else if (recentSide != helper.get(i).first && (equivalenceLines.isEmpty() ||
                        equivalenceLines.get(equivalenceLines.size() -1) != recentBit))
                    equivalenceLines.add(recentBit);
            }
        }

    }

    public int getTotalNotes() { return totalNotes; }

    public ArrayList<Double> getEquivalenceLines() { return equivalenceLines; }

    public double bitToNanos(double bit) {
        double time = 0;
        if (bit < 0) return (double) bit * 60000.0 * 1000000 / bpms.get(0).getValue();
        if (bpms.size() == 1) return 60000.0 * 1000000 / bpms.get(0).getValue() * bit;
        for (int i=0; i<bpms.size(); i++) {
            BPM current = bpms.get(i);
            BPM before = bpms.get(Math.max(i-1,0));
            if (current.getBit() < bit) {
                time += 60000.0 * 1000000 / before.getValue() * (current.getBit() - before.getBit());
            }
            else {
                time += 60000.0 * 1000000 / before.getValue() * (bit - before.getBit());
                return time;
            }
        }
        time += 60000.0 * 1000000 / bpms.get(bpms.size()-1).getValue() * (bit - bpms.get(bpms.size()-1).getBit());
        return time;
    }

    public ArrayList[] getNotes() { return notes; }

    public double getCurrentBPM (double bit) {
        if (bit <= 0) {
            return bpms.get(0).getValue();
        }
        for (int i=0; i<bpms.size(); i++) {
            if (bpms.get(i).getBit() > bit) {
                return bpms.get(i-1).getValue();
            }
        }
        return 0;
    }

    public double nanosToBit(long time) {
        if (time < 0) return (double) time / 60000.0 / 1000000 * bpms.get(0).getValue();
        double bit = 0;
        for (int i = 0; i < bpms.size(); i++) {
            if (i == bpms.size()-1) {
                bit += (double) time / 60000.0 / 1000000 * bpms.get(i).getValue();
            }
            else {
                double period = bpms.get(i+1).getBit() - bpms.get(i).getBit();
                if ((double) time > period * 60000.0 * 1000000 / bpms.get(i).getValue()) {
                    bit += period;
                    time -= period * 60000.0 * 1000000 / bpms.get(i).getValue();
                }
                else {
                    bit += (double) time / 60000.0 / 1000000 * bpms.get(i).getValue();
                    return bit;
                }
            }
        }
        return bit;
    }

    public int getOffset() { return offset; }


}

abstract class BitObject {
    private double bit;

    public BitObject(double bit) {
        this.bit = bit;
    }

    double getBit() { return bit; }

    boolean equals(BitObject bo) {
        return bo.bit == this.bit;
    }
}

class BPM extends BitObject {
    private double value;

    BPM (double value, double bit) {
        super(bit);
        this.value = value;
    }

    public double getValue() { return value; }

}

class Note extends BitObject {
    private String kind;
    private double pos1;
    private double pos2;
    private int color; //Color color;
    public final static String SLIDE = "Slide";
    public final static String TAB = "Tab";
    public final static String AUTO = "Auto";

    Note (String kind, double pos1, double bit, double pos2, int color) {
        super(bit);
        this.kind = kind;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.color = color;
    }

    public String getKind() { return kind; }

    public double getPosition1() { return pos1; }

    public double getPosition2() { return pos2; }

    public int getColor() { return color; }

    public boolean equals(Note note) {
        return (super.equals(note) && note.kind.equals(this.kind) && note.pos1 == this.pos1 && note.pos2 == this.pos2 && note.color == this.color);
    }
}

/*class Flip extends Note {
    final static int LEFT = 0;
    final static int RIGHT = 1;
    final static int UP = 2;
    final static int DOWN = 3;
}

class Slide extends Note {

}*/