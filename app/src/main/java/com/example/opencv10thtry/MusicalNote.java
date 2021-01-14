package com.example.opencv10thtry;

public class MusicalNote {
    private int note;
    private int octave;
    private long startTime;
    private long playingTime;

    public MusicalNote(int note, int octave, long startTime, long playingTime){
        this.note = note;
        this.octave = octave;
        this.startTime = 3000 + startTime;
        this.playingTime = playingTime;
    }

    public int getNote() {
        return note;
    }

    public int getOctave() {
        return octave;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getPlayingTime() {
        return playingTime;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public void setOctave(int octave) {
        this.octave = octave;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setPlayingTime(long playingTime) {
        this.playingTime = playingTime;
    }

    @Override
    public String toString() {
        return "MusicalNote{" +
                "note=" + note +
                ", octave=" + octave +
                ", startTime=" + startTime +
                ", playingTime=" + playingTime +
                '}';
    }
}
