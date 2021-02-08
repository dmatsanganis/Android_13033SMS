package com.dmatsanganis.hardaliapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static com.dmatsanganis.hardaliapp.R.string.*;


public class Register extends AppCompatActivity {
    private MySpeechRecognizer speechRecognizer;
    private FloatingActionButton speechRecognitionButton;
    private static final int REC_RESULT = 489;
    private FirebaseAuth mAuth;
    private Button registerbtn;
    FirebaseUser currentUser;
    private EditText nametxt;
    private EditText emailtxt;
    private EditText locationtxt;
    private EditText passtxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_round_keyboard_backspace);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); //finishing activity
            }
        });

        nametxt = findViewById(R.id.textInputName);
        emailtxt = findViewById(R.id.textInputEmail);
        locationtxt = findViewById(R.id.textInputLocation);
        passtxt = findViewById(R.id.textInputPassword);
        registerbtn = findViewById(R.id.buttonSavePersonalInfo);
        speechRecognitionButton = findViewById(R.id.mic_register);
        speechRecognizer = new MySpeechRecognizer(this);

        mAuth = FirebaseAuth.getInstance();
        // Check if user is signed in (non-null).
        currentUser = mAuth.getCurrentUser();

        // Lambda Expression FAB listener (Microphone/TextToSpeech Tool).
        speechRecognitionButton.setOnClickListener((view) -> {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            {
                // Mic's Permission Check.
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 878);
            }
            else {

                // If permission is already given continue, with the initialization
                //  of the Speech Recognizer function, initSpeechRecognizer().
                initSpeechRecognizer();
            }
        });

    }

    // Sing Up - Register Void.
    public void signup(View view){
        if(emailtxt.getText().toString().isEmpty())
        {
            // Empty Fields Error Message Appears (Email's Field).
            Toast.makeText(getApplicationContext(), register_empty_email, Toast.LENGTH_LONG).show();
        }
        else if(nametxt.getText().toString().isEmpty())
        {
            // Empty Fields Error Message Appears (Password's Field).
            Toast.makeText(getApplicationContext(), register_empty_name, Toast.LENGTH_LONG).show();
        }
        else if(passtxt.getText().toString().isEmpty())
        {
            // Empty Fields Error Message Appears (Password's Field).
            Toast.makeText(getApplicationContext(), register_empty_password, Toast.LENGTH_LONG).show();
        }
        else if(locationtxt.getText().toString().isEmpty())
        {
            // Empty Fields Error Message Appears (Address's Field).
            Toast.makeText(getApplicationContext(), register_empty_address, Toast.LENGTH_LONG).show();
        }
        else {
            // If All Fields are Filled continue the Register Process.
            mAuth.createUserWithEmailAndPassword(
                    emailtxt.getText().toString(),
                    passtxt.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                User user = new User(nametxt.getText().toString(), emailtxt.getText().toString(), locationtxt.getText().toString());
                                currentUser = mAuth.getCurrentUser();

                                //Send Registration's Email Verification.
                                currentUser.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    FirebaseDatabase.getInstance().getReference("Users/" + currentUser.getUid())
                                                            .child("Personal Details")
                                                            .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                // Success Message and Email Verification Reminder.
                                                                Toast.makeText(getApplicationContext(), register_success, Toast.LENGTH_LONG).show();
                                                            } else {
                                                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                savename(nametxt.getText().toString(), currentUser);
                            } else {
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    // SaveName Helper Void.
    private void savename(String fullname, FirebaseUser user) {
        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullname)
                .build();
        user.updateProfile(profileChangeRequest);
    }

    // Login Intent.
    public void gologin(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // Check activity results for Speech Recognition and Mic's Events.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REC_RESULT && resultCode == RESULT_OK)
        {
            // Results' Array.
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (matches.contains("back") || matches.contains("πίσω") || matches.contains("έξοδος") || matches.contains("αποσύνδεση")
                    || matches.contains("logout") || matches.contains("exit") || matches.contains("sign out"))
            {
                // Go to MainActivity Activity.
                startActivity(new Intent(this, MainActivity.class));
            }
            else if (matches.contains("εγγραφή") || matches.contains("εγγραφείτε") || matches.contains("register")
                    || matches.contains("sign up") || matches.contains("registration") || matches.contains("signup") || matches.contains("γίνε μέλος"))
            {
                // Register User.
                registerbtn.performClick();
            }
            else{
                // Toast Message.
                Toast.makeText(this, R.string.register, Toast.LENGTH_LONG).show();
                // Assistant's Voice Help.
                speechRecognizer.speak("Δεν σας καταλάβαμε! Παρακαλώ προσπαθήστε ξανά!");
            }
        }
        else {
            // Toast Message.
            Toast.makeText(this, register_mic, Toast.LENGTH_LONG).show();
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