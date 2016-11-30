package vivek.felight.com.smsreadandtoast;

/**
 * Created by Vivek on 11/15/2016.
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Vivek on 11/15/2016.
 */

public class Speaker extends Activity implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private boolean ready = false;
    private boolean allowed = false;

    public Speaker(Context context){
        tts = new TextToSpeech(context, this);
    }

    public boolean isAllowed(){
        return allowed;
    }

    public void allow(boolean allowed){
        this.allowed = allowed;
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            // Change this to match your
            // locale
            tts.setLanguage(Locale.US);
            ready = true;
        }else{
            ready = false;
        }
    }

    public void speak(String text){
        // Speak only if the TTS is ready
        // and the user has allowed speech
        if(ready && allowed) {
            HashMap<String, String> hash = new HashMap<String,String>();
            hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void pause(int duration){
        tts.playSilence(duration, TextToSpeech.QUEUE_ADD, null);
    }

    // Free up resources
    public void destroy(){
        tts.shutdown();
    }
}