package com.example.opencv10thtry.midi.spi;

import com.example.opencv10thtry.midi.MidiDevice;

import org.jetbrains.annotations.Nullable;

/**
 * Abstract class for MIDI Device Provider
 *
 * @author K.Shoji
 */
public abstract class MidiDeviceProvider {

    /**
     * Constructor
     */
    public MidiDeviceProvider() {
    }

    /**
     * Get the {@link MidiDevice} from the specified information
     *
     * @param info the information
     * @return the MidiDevice
     * @throws IllegalArgumentException
     */

    public abstract MidiDevice getDevice( MidiDevice.Info info) throws IllegalArgumentException;

    /**
     * Get the all of {@link MidiDevice.Info}
     *
     * @return the array of {@link MidiDevice.Info}
     */

    public abstract MidiDevice.Info[] getDeviceInfo();

    /**
     * Check if the specified Device is supported
     *
     * @param info the information
     * @return true if the Device is supported
     */
    public boolean isDeviceSupported( MidiDevice.Info info) {
        MidiDevice.Info[] informationArray = getDeviceInfo();

        for (MidiDevice.Info information : informationArray) {
            if (info.equals(information)) {
                return true;
            }
        }
        
        return false;
    }
}
