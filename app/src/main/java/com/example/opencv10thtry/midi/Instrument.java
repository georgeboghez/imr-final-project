package com.example.opencv10thtry.midi;



/**
 * Abstract Class for MIDI Instrument
 *
 * @author K.Shoji
 */
public abstract class Instrument extends SoundbankResource {
    private final Patch patch;

    /**
     * Constructor
     *
     * @param soundbank the soundbank
     * @param patch the patch
     * @param name the name
     * @param dataClass the dataClass
     */
    protected Instrument( final Soundbank soundbank,  final Patch patch,  final String name,  final Class<?> dataClass) {
        super(soundbank, name, dataClass);
        this.patch = patch;
    }

    /**
     * Get the patch of the {@link Instrument}
     *
     * @return the patch
     */

    public Patch getPatch() {
        return patch;
    }
}
