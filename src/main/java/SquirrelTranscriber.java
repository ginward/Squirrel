/**
 * Author: Jinhua Wang
 * The script to transcribe audio into text
 * License MIT
 */
import java.io.IOException;
import java.io.InputStream;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

public class SquirrelTranscriber {
    /*
     * Function to transcribe audio into text
     * */
    public String transcribe(InputStream stream) throws IOException {
        Configuration configuration = new Configuration();
        configuration
                .setAcousticModelPath("file:res/cmusphinx-en-us-5.2");
        configuration
                .setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration
                .setLanguageModelPath("file:res/en-70k-0.2.lm.bin");
        StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(
                configuration);
        System.out.println("starting...");
        recognizer.startRecognition(stream);
        SpeechResult result;
        String txt = "";
        System.out.println("ended...");
        while ((result = recognizer.getResult()) != null) {
            System.out.format("Hypothesis: %s\n", result.getHypothesis());
            txt+=result.getHypothesis();
            txt+=" ";
        }
        recognizer.stopRecognition();
        return txt;
    }

}