package com.dmatsanganis.hardaliapp.Messages;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dmatsanganis.hardaliapp.Database.DatabaseConfiguration;
import com.dmatsanganis.hardaliapp.Database.DatabaseModel;
import com.dmatsanganis.hardaliapp.Messages.EditMessages;
import com.dmatsanganis.hardaliapp.MainActivity;
import com.dmatsanganis.hardaliapp.MySpeechRecognizer;
import com.dmatsanganis.hardaliapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.dmatsanganis.hardaliapp.R.string.*;
import static com.dmatsanganis.hardaliapp.R.string.AddMessage_database_success;

public class AddMessage extends AppCompatActivity {

    private static final int REC_RESULT = 777 ;
    private Button button, button1, back;
    private EditText editText, editText1, editText2;
    private MySpeechRecognizer speechRecognizer;
    private FloatingActionButton speechRecognitionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sms);

        // Initialize Components.
        editText = findViewById(R.id.edittext_code);
        editText1 = findViewById(R.id.edittext_title);
        editText2 = findViewById(R.id.edittext_data);
        button = findViewById(R.id.button_add);
        button1 = findViewById(R.id.button_view);
        back = findViewById(R.id.alerteditAlertButtonClose);

        // Init Mic (TexttoSpeech).
        speechRecognizer = new MySpeechRecognizer(this);
        speechRecognitionButton = findViewById(R.id.mic_add_message);

        // button lambda expression.
        button.setOnClickListener(v -> {
            String code = editText.getText().toString();
            String reason = editText1.getText().toString();
            String data = editText2.getText().toString();
            if(code.isEmpty()||reason.isEmpty()||data.isEmpty())
            {
                // Empty Fields Check and Toast Reminder.
                Toast.makeText(this, add_message_empty_fields,Toast.LENGTH_LONG).show();

                // Assistant Voice.
                speechRecognizer.speak("Παρακαλώ συμπληρώστε όλα τα υποχρεωτικά πεδία!");
            }
            else {
                DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration(getApplicationContext());
                DatabaseModel databaseModel = new DatabaseModel(code, reason, data);
                databaseConfiguration.addmessage(databaseModel);
                Toast.makeText(getApplicationContext(), AddMessage_database_success, Toast.LENGTH_LONG).show();
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(getApplicationContext(), EditMessages.class);
                startActivity(intent);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finishing activity
                finish();
            }
        });

        // Lambda Expression FAB listener (Microphone/TextToSpeech Tool).
        speechRecognitionButton.setOnClickListener((view) -> {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PERMISSION_GRANTED) {
                // Mic's Permission Check.
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 878);
            }
            else {
                // If permission is already given continue, with the initialization
                // of the Speech Recognizer function, initSpeechRecognizer().
                initSpeechRecognizer();
            }
        });
    }

    // Check activity results for Speech Recognition and Mic's Events.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REC_RESULT && resultCode == RESULT_OK)
        {
            // Results' Array.
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if ( matches.contains("έξοδος") || matches.contains("αποσύνδεση")
                    || matches.contains("logout") || matches.contains("exit") || matches.contains("sign out"))
            {
                // Go to MainActivity Activity.
                startActivity(new Intent(this, MainActivity.class));
            }
            else if (matches.contains("back") || matches.contains("πίσω"))
            {
                //finish Activity.
                finish();
            }
            else if (matches.contains("προσθήκη") || matches.contains("νέο") || matches.contains("πρόσθεσε") || matches.contains("νέο μήνυμα") || matches.contains("πρόσθεσε μήνημα") || matches.contains("νέο κωδικό")
                    || matches.contains("new") || matches.contains("add") || matches.contains("addition") || matches.contains("new message") || matches.contains("new code"))
            {
                // Add Message button.
                button.performClick();

                // Assistant's Voice Help.
                speechRecognizer.speak("προσθήκη μηνύματος!");
            }
            else if (matches.contains("επεξεργασία") || matches.contains("αλλαγή") || matches.contains("διαγραφή")
                    || matches.contains("edit") || matches.contains("change") || matches.contains("delete") )
            {
                // Edit Message Activity.
                Intent intent = new Intent(this, EditMessages.class);
                startActivity(intent);

                // Assistant's Voice Help.
                speechRecognizer.speak("Επεξεργασία Μηνυμάτων!");
            }
            else{
                // Toast Message.
                Toast.makeText(this, "Δεν σας καταλάβαμε! Παρακαλώ προσπαθήστε ξανά", Toast.LENGTH_LONG).show();
                // Assistant's Voice Help.
                speechRecognizer.speak("Δεν σας καταλάβαμε! Παρακαλώ προσπαθήστε ξανά!");
            }
        }
        else {
            // Toast Message.
            Toast.makeText(this, "Δεν σας καταλάβαμε! Παρακαλώ προσπαθήστε ξανά", Toast.LENGTH_LONG).show();
            // Assistant's Voice Help.
            speechRecognizer.speak("Δεν σας καταλάβαμε! Παρακαλώ προσπαθήστε ξανά!");
        }
    }

    // Speech Recognizer Void.
    public void initSpeechRecognizer()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Παρακαλω πείτε κάτι!");
        startActivityForResult(intent,REC_RESULT);
    }
}