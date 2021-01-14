package com.example.opencv10thtry.midi;



/**
 * Interface for {@link MidiDevice} receiver.
 *
 * @author K.Shoji
 */
public interface MidiDeviceReceiver extends Receiver {

    /**
     * Get the {@link com.example.opencv10thtry.midi.MidiDevice} associated with this instance.
     *
     * @return the {@link com.example.opencv10thtry.midi.MidiDevice} associated with this instance.
     */

    MidiDevice getMidiDevice();
}
