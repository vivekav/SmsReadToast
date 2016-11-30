package vivek.felight.com.smsreadandtoast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private final int CHECK_CODE = 0x1;
    private final int LONG_DURATION = 5000;
    private final int SHORT_DURATION = 1200;

    private Speaker speaker;
    private ToggleButton toggle;
    private CompoundButton.OnCheckedChangeListener toggleListener;
    private TextView smsText;
    private TextView smsSender;
    private BroadcastReceiver smsReceiver;

    private void checkTTS(){
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, CHECK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                speaker = new Speaker(this);
            } else {
                Intent install = new Intent();
                install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(install);
            }
        }
    }


        public void initializeSMSReceiver(){
            smsReceiver = new BroadcastReceiver(){
                @Override
                public void onReceive(Context context, Intent intent) {

                    Bundle bundle = intent.getExtras();

                    if(bundle!=null){
                        Object[] pdus = (Object[])bundle.get("pdus");
                        for(int i=0;i<pdus.length;i++){
                            byte[] pdu = (byte[])pdus[i];
                            SmsMessage message = SmsMessage.createFromPdu(pdu);
                            String text = message.getDisplayMessageBody();
                            String sender = getContactName(message.getOriginatingAddress());
                            intent.putExtras(bundle);
                            speaker.pause(SHORT_DURATION);
                            speaker.speak("You have a new message from" + sender + "!");
                            speaker.pause(SHORT_DURATION);
                            speaker.speak(text);
                            smsSender.setText("Message from " + sender);
                            smsText.setText(text);
                            Toast.makeText(getBaseContext(),text,Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            };
        }


    private String getContactName(String phone){
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        String projection[] = new String[]{ContactsContract.Data.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if(cursor.moveToFirst()){
            return cursor.getString(0);
        }else {
            return "unknown number";
        }
    }


    private void registerSMSReceiver() {
        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, intentFilter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggle = (ToggleButton) findViewById(R.id.speechToggle);
        smsText = (TextView) findViewById(R.id.sms_text);
        smsSender = (TextView) findViewById(R.id.sms_sender);

        toggleListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                if (isChecked) {
                    speaker.allow(true);
                    speaker.speak(getString(R.string.start_speaking));
                    Toast.makeText(getBaseContext(), R.string.start_speaking, Toast.LENGTH_SHORT).show();
                } else {
                    speaker.speak(getString(R.string.stop_speaking));
                    speaker.allow(false);
                }
            }
        };
        toggle.setOnCheckedChangeListener(toggleListener);

        checkTTS();
        initializeSMSReceiver();
        registerSMSReceiver();

            }


                @Override
                protected void onDestroy () {
                    super.onDestroy();
                    unregisterReceiver(smsReceiver);
                    speaker.destroy();
                }

            }