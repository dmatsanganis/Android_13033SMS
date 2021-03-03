package com.dmatsanganis.hardaliapp.Messages;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.dmatsanganis.hardaliapp.Adapters.MessagesAdapter;
import com.dmatsanganis.hardaliapp.Database.DatabaseConfiguration;
import com.dmatsanganis.hardaliapp.Database.DatabaseModel;
import com.dmatsanganis.hardaliapp.MainActivity;
import com.dmatsanganis.hardaliapp.MySpeechRecognizer;
import com.dmatsanganis.hardaliapp.R;
import com.dmatsanganis.hardaliapp.SendSMS;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class EditMessages extends AppCompatActivity {

    private static final int REC_RESULT = 777 ;
    private MySpeechRecognizer speechRecognizer;
    private FloatingActionButton speechRecognitionButton;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_messages);

        // Initialize Toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_round_keyboard_backspace);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finishing activity
                finish();
            }
        });

        // Init Mic (TexttoSpeech).
        speechRecognizer = new MySpeechRecognizer(this);

        // Init Recycle view.
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration(this);
        List<DatabaseModel> messages = databaseConfiguration.getmessages();

        if(messages.size()>0)
        {
            MessagesAdapter messagesAdapter = new MessagesAdapter(messages, EditMessages.this);
            recyclerView.setAdapter(messagesAdapter);
        }
    }

    // App's Navigation Menu Void.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_edited_message, menu);
        return true;
    }

    // App's Navigation Menu Void.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // An Int type variable to identify the user's menu choice.
        int id = item.getItemId();

        if (id == R.id.action_addcode)
        {
            // Go to Add Message Void.
            startActivity(new Intent(this, AddMessage.class));
            return true;
        }

        if (id == R.id.action_see_edited)
        {
            // Go to View Edited Messages Void.
            startActivity(new Intent(this, ViewEditedMessages.class));
            return true;
        }

        if (id == R.id.microphone)
        {
            // Checks for Permission.
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PERMISSION_GRANTED) {
                // Mic's Permission Check.
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 878);
            }
            else {
                // If permission is already given continue, with the initialization
                // of the Speech Recognizer function, initSpeechRecognizer().
                initSpeechRecognizer();
            }
        }

        return super.onOptionsItemSelected(item);
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
                // Add Message Activity.
                Intent intent = new Intent(this, AddMessage.class);
                startActivity(intent);

            }
            else if (matches.contains("προβολή") || matches.contains("custom") || matches.contains("see")
                    || matches.contains("view") || matches.contains("send custom"))
            {
                // Edit Message Activity.
                Intent intent = new Intent(this, ViewEditedMessages.class);
                startActivity(intent);

                // Assistant's Voice Help.
                speechRecognizer.speak("Προβολή Μηνυμάτων!");
            }
            else if (matches.contains("στείλε") || matches.contains("στείλτο") || matches.contains("13033") || matches.contains("send") || matches.contains("SMS")
                    || matches.contains("μήνυμα") || matches.contains("message"))
            {
                // Send SMS Activity.
                Intent intent = new Intent(this, SendSMS.class);
                startActivity(intent);
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