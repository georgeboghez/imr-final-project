package com.example.opencv10thtry.midi.spi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.example.opencv10thtry.midi.InvalidMidiDataException;
import com.example.opencv10thtry.midi.Soundbank;

/**
 * Abstract class for Soundbank Reader
 *
 * @author K.Shoji
 */
public abstract class SoundbankReader {

    /**
     * Constructor
     */
    public SoundbankReader() {
    }

    /**
     * Get the Soundbank from the specified {@link File}
     *
     * @param file the file
     * @return Soundbank
     * @throws InvalidMidiDataException
     * @throws IOException
     */

    public abstract Soundbank getSoundbank( File file) throws InvalidMidiDataException, IOException;

    /**
     * Get the Soundbank from the specified {@link InputStream}
     *
     * @param stream the InputStream
     * @return Soundbank
     * @throws InvalidMidiDataException
     * @throws IOException
     */

    public abstract Soundbank getSoundbank( InputStream stream) throws InvalidMidiDataException, IOException;

    /**
     * Get the Soundbank from the specified {@link URL}
     *
     * @param url the URL
     * @return Soundbank
     * @throws InvalidMidiDataException
     * @throws IOException
     */

    public abstract Soundbank getSoundbank( URL url) throws InvalidMidiDataException, IOException;
}
