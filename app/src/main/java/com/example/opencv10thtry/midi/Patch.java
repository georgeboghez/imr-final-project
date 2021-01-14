package com.example.opencv10thtry.midi;

/**
 * Represents MIDI Patch
 *
 * @author K.Shoji
 */
public class Patch {
    private final int bank;
    private final int program;

    /**
     * Constructor
     *
     * @param bank the bank of {@link com.example.opencv10thtry.midi.Patch}
     * @param program the program of {@link com.example.opencv10thtry.midi.Patch}
     */
    public Patch(final int bank, final int program) {
        this.bank = bank;
        this.program = program;
    }

    /**
     * Get the bank of {@link com.example.opencv10thtry.midi.Patch}
     *
     * @return the bank of {@link com.example.opencv10thtry.midi.Patch}, 0-16383
     */
    public int getBank() {
        return bank;
    }

    /**
     * Get the program of {@link com.example.opencv10thtry.midi.Patch}
     *
     * @return the program of {@link com.example.opencv10thtry.midi.Patch}, 0-127
     */
    public int getProgram() {
        return program;
    }
}
