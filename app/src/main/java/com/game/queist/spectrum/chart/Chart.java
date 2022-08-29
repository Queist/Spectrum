package com.game.queist.spectrum.chart;

import android.util.Pair;

import com.game.queist.spectrum.utils.Utility;

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

    private ArrayList<Note> notes;
    private ArrayList<Pair<Double, Double>> helper;
    private ArrayList<RotateSpeed> rotateSpeeds;
    private ArrayList<EquivalenceLine> equivalenceLines;
    private ArrayList<BPM> bpms;
    private int totalNotes;
    private int offset;
    private boolean isLegacy;
    static Comparator<BitObject> sortToBit = (b1, b2) -> Double.compare(b1.getBit(), b2.getBit());

    public Chart(InputStream chartFile) {
        totalNotes = 0;
        notes = new ArrayList<>();
        bpms = new ArrayList<>();
        helper = new ArrayList<>();
        equivalenceLines = new ArrayList<>();
        rotateSpeeds = new ArrayList<>();
        offset = 0;
        readChart(chartFile);
    }

    public Chart(BufferedReader chartFile) {
        totalNotes = 0;
        notes = new ArrayList<>();
        bpms = new ArrayList<>();
        helper = new ArrayList<>();
        equivalenceLines = new ArrayList<>();
        rotateSpeeds = new ArrayList<>();
        offset = 0;
        readChart(chartFile);
    }

    private void readChart(InputStream chartFile) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(chartFile, StandardCharsets.UTF_8));
        readChart(bufferedReader);
    }

    private void readChart(BufferedReader bufferedReader) {
        try {
            String readLine;
            readLine = bufferedReader.readLine();
            String[] hAttr = readLine.split("\t");
            if (!(hAttr[0].equals("Header") && hAttr[1].equals("Spectrum"))) {
                throw new Exception("Error in Parsing Spectrum Chart Header");
            }
            isLegacy = hAttr[2].equals("Legacy");

            rotateSpeeds.add(new RotateSpeed(1.0, -100000.0));

            while ((readLine = bufferedReader.readLine()) != null) {
                String[] attr = readLine.split("\t");
                System.out.println(attr[0]);
                switch (attr[0]) {
                    case "Offset":
                        offset = Integer.parseInt(attr[1]);
                        break;

                    case "BPM":
                        bpms.add(new BPM(Double.parseDouble(attr[1]), Double.parseDouble(attr[2])));
                        break;

                    case "Note":
                        Double meanPos = isLegacy ?
                                Double.parseDouble(attr[2]) : (Double.parseDouble(attr[2]) + Double.parseDouble(attr[3]))/2;
                        Note note;
                        if (!checkNoteKind(attr[1])) {
                            throw new Exception("Error in Parsing Spectrum Chart Body");
                        }
                        if (isLegacy) {
                            note = Utility.getNoteFromLegacyFormat(
                                    attr[1],
                                    Integer.parseInt(attr[2]),
                                    Double.parseDouble(attr[3]),
                                    Double.parseDouble(attr[4]),
                                    Double.parseDouble(attr[5]),
                                    Integer.parseInt(attr[6]));
                            totalNotes += 1;
                        }
                        else {
                            if (attr[1].equals(Note.LONG)) {
                                note = new LongNote(attr[1],
                                        Double.parseDouble(attr[2]),
                                        Double.parseDouble(attr[3]),
                                        Double.parseDouble(attr[4]),
                                        Integer.parseInt(attr[5]),
                                        Double.parseDouble(attr[6]));
                                totalNotes += 2;
                            }
                            else {
                                note = new Note(attr[1],
                                        Double.parseDouble(attr[2]),
                                        Double.parseDouble(attr[3]),
                                        Double.parseDouble(attr[4]),
                                        Integer.parseInt(attr[5]));
                                totalNotes += 1;
                            }
                        }
                        notes.add(note);
                        if (note.getKind().equals(Note.TAB) || note.getKind().equals(Note.LONG))
                            helper.add(new Pair<>(meanPos, note.getBit()));
                        break;

                    case "Rotate":
                        if (rotateSpeeds.get(rotateSpeeds.size() - 1).getValue() != Double.parseDouble(attr[1]))
                            rotateSpeeds.add(new RotateSpeed(Double.parseDouble(attr[1]), Double.parseDouble(attr[2])));
                        break;

                    default:
                        throw new Exception("Error in Parsing Spectrum Chart Body");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            double recentBit = -8000;
            double recentMeanPos = -1;
            for (int i = 0; i < helper.size(); i++) {
                if (recentBit != helper.get(i).second) {
                    recentBit = helper.get(i).second;
                    recentMeanPos = helper.get(i).first;
                }
                else if (isLegacy) {
                    if (equivalenceLines.isEmpty() ||
                            equivalenceLines.get(equivalenceLines.size() - 1).getBit() != recentBit)
                        equivalenceLines.add(new EquivalenceLine(recentBit));
                }
                else {
                    if (equivalenceLines.isEmpty() ||
                            equivalenceLines.get(equivalenceLines.size() - 1).getBit() != recentBit)
                        equivalenceLines.add(new EquivalenceLine(recentBit));
                }
            }
        }
    }

    public int getTotalNotes() { return totalNotes; }

    public ArrayList<EquivalenceLine> getEquivalenceLines() { return equivalenceLines; }

    public double bitToNanos(double bit) {
        double time = 0;
        if (bit < 0) return bit * 60000.0 * 1000000 / bpms.get(0).getValue();
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

    public double getCurrentRotateSpeed(double bit) {
        if (bit <= 0) {
            return rotateSpeeds.get(0).getValue();
        }
        for (int i=0; i<rotateSpeeds.size(); i++) {
            if (rotateSpeeds.get(i).getBit() > bit) {
                return rotateSpeeds.get(i-1).getValue();
            }
        }
        return 1;
    }

    public ArrayList<Note> getNotes() { return notes; }

    public ArrayList<RotateSpeed> getRotateSpeeds() {
        return rotateSpeeds;
    }

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

    private boolean checkNoteKind(String kind) {
        return kind.equals(Note.TAB) || kind.equals(Note.LONG) || kind.equals(Note.SLIDE) || kind.equals(Note.AUTO);
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