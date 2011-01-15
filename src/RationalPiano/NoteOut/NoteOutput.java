package RationalPiano.NoteOut;

import java.util.concurrent.ConcurrentHashMap;

import processing.core.*;

/**
 * Manages note output for MIDI and OSC
 * Allows to turn on/off notes/voices on a midi channel / osc port and to de/activate sustain
 * 
 * @author Fabian Ehrentraud
 * @date 2010-10-10
 * @version 1.05
 * @licence Licensed under the Open Software License (OSL 3.0)
 */
public class NoteOutput implements INoteOutput {

	/**
	 * Possible modes of note message outputs: either only via MIDI, only via OSC or both
	 */
	public enum outputModes {MIDI_ONLY, OSC_ONLY, MIDI_AND_OSC};
	
	private boolean oscOn = false;
	private boolean midiOn = false;
	
	private SendMidi sendmidi = null;
	private SendOsc sendosc = null;
	
	private ConcurrentHashMap<Integer, Double> activeNotes = new ConcurrentHashMap<Integer, Double>();
	
	/**
	 * Initializes the MIDI/OSC output
	 * @param papplet The processing applet to send the MIDI/OSC messages from
	 * @param outputMode allows to either activate midi output or osc output or both
	 * @param oscport UDP port to send the OSC note messages to
	 * @param midiOutputDevice PART of the name of the MIDI device to send the note messages to
	 * @param midiChannel MIDI channel to send the note messages to; 0<=midiChannel<=15
	 */
	public NoteOutput(PApplet papplet, outputModes outputMode, int oscport, String midiDevice, int midiChannel) {
		if(outputMode == outputModes.OSC_ONLY || outputMode == outputModes.MIDI_AND_OSC ){
			oscOn = true;
			sendosc = new SendOsc(papplet, oscport);
		}
		if(outputMode == outputModes.MIDI_ONLY || outputMode == outputModes.MIDI_AND_OSC ){
			midiOn = true;
			sendmidi = new SendMidi(papplet, midiDevice, midiChannel);
		}
	}

	/* (non-Javadoc)
	 * @see RationalPiano.NoteOut.INoteOutput#noteOn(int, double)
	 */
	public void noteOn(int midiNoteNumber, double velocity){
		if(activeNotes.containsKey(midiNoteNumber)){
			noteOff(midiNoteNumber); //note is already active, turn off before
		}
		
		activeNotes.put(midiNoteNumber, velocity);
		
		if(oscOn == true){
			sendosc.voiceOn(midiNoteNumber, (float)velocity);
		}
		if(midiOn == true){
			sendmidi.noteOn(midiNoteNumber, (int)(127*velocity));
		}
	}
	
	/* (non-Javadoc)
	 * @see RationalPiano.NoteOut.INoteOutput#noteOff(int)
	 */
	public void noteOff(int midiNoteNumber){
		if(!activeNotes.containsKey(midiNoteNumber)){
			return; //note is not active, no need to turn it off
		}
		
		activeNotes.remove(midiNoteNumber);
		
		if(oscOn == true){
			sendosc.voiceOff(midiNoteNumber);
		}
		if(midiOn == true){
			sendmidi.noteOff(midiNoteNumber);
		}
	}

	/* (non-Javadoc)
	 * @see RationalPiano.NoteOut.INoteOutput#sustain(double)
	 */
	public void sustain(double sustain) {
		if(oscOn == true){
			sendosc.sustain(sustain);
		}
		if(midiOn == true){
			sendmidi.sustain((int)(127 * sustain));
		}
	}
}
