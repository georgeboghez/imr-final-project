package com.example.opencv10thtry.midi;



import java.util.EventListener;

/**
 * {@link EventListener} for MIDI Meta messages.
 * 
 * @author K.Shoji
 */
public interface MetaEventListener extends EventListener {

	/**
	 * Called at {@link MetaMessage} event has fired
	 * 
	 * @param meta the source event
	 */
	void meta( MetaMessage meta);
}
