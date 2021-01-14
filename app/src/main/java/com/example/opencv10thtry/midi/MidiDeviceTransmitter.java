package com.example.opencv10thtry.midi;


/**
 * Interface for {@link MidiDevice} transmitter.
 *
 * @author K.Shoji
 */
public interface MidiDeviceTransmitter extends Transmitter {

    /**
     * Get the {@link com.example.opencv10thtry.midi.MidiDevice} associated with this instance.
     *
     * @return the {@link com.example.opencv10thtry.midi.MidiDevice} associated with this instance.
     */

    MidiDevice getMidiDevice();
}
