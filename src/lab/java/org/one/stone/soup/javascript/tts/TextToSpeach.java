package org.one.stone.soup.javascript.tts;

import java.beans.PropertyVetoException;
import java.util.Locale;

import javax.speech.AudioException;
import javax.speech.EngineCreate;
import javax.speech.EngineException;
import javax.speech.EngineList;
import javax.speech.EngineStateError;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;

import com.sun.speech.freetts.jsapi.FreeTTSEngineCentral;

public class TextToSpeach {

	private String voiceName = "kevin16";
	private Synthesizer synthesizer;
	
	public TextToSpeach() throws EngineStateError, Exception {
		
		SynthesizerModeDesc desc = 
                new SynthesizerModeDesc(null, 
                                        "general", /* use "time" or "general" */
                                        Locale.US, 
                                        Boolean.FALSE,
                                        null);

            FreeTTSEngineCentral central = new FreeTTSEngineCentral();
            EngineList list = central.createEngineList(desc); 
            
            if (list.size() > 0) { 
                EngineCreate creator = (EngineCreate) list.get(0); 
                synthesizer = (Synthesizer) creator.createEngine(); 
            } 
            if (synthesizer == null) {
                System.err.println("Cannot create synthesizer");
                System.exit(1);
            }
            synthesizer.allocate();
            synthesizer.resume();
            
            synthesizer.speakPlainText("Text to speach installed", null);
            synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
	}
	
	public void say(String text) throws IllegalArgumentException, InterruptedException {
        synthesizer.speakPlainText(text, null);
        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
	}
}
