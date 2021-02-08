package com.dmatsanganis.hardaliapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dmatsanganis.hardaliapp.Database.DatabaseConfiguration;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

import static com.dmatsanganis.hardaliapp.R.string.*;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private MySpeechRecognizer speechRecognizer;
    private FirebaseAuth mAuth;
    private EditText emailtxt, passtxt;
    private TextView resetlbl, registerlbl;
    FirebaseUser currentUser;
    private FloatingActionButton speechRecognitionButton;
    private static final int REC_RESULT = 489;
    private CheckBox pass_checkBox;
    private Boolean stayIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailtxt = findViewById(R.id.textInputEmail);
        passtxt = findViewById(R.id.textInputPassword);
        resetlbl = findViewById(R.id.goto_resetpass_lbl);
        registerlbl = findViewById(R.id.gotologinlbl);
        pass_checkBox = findViewById(R.id.checkBox1);
        speechRecognitionButton = findViewById(R.id.mic_main);
        speechRecognizer = new MySpeechRecognizer(this);

        mAuth = FirebaseAuth.getInstance();
        // Check if user is signed in (non-null).
        FirebaseUser currentUser = mAuth.getCurrentUser();

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

        // Login credentials save to Shared Preferences.
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        stayIn = preferences.getBoolean("stayIn",false);
        if (stayIn)
        {
            emailtxt.setText(preferences.getString("username", ""));
            passtxt.setText(preferences.getString("password", ""));
        }
    }

    // Sign In - Authentication Void.
    public void signin(View view){
        if(emailtxt.getText().toString().isEmpty())
        {
            // Empty Fields Error Message Appears (Email's Field).
            Toast.makeText(getApplicationContext(), R.string.mainActivity_empty_email, Toast.LENGTH_LONG).show();
        }
        else if(passtxt.getText().toString().isEmpty())
        {
            // Empty Fields Error Message Appears (Password's Field).
            Toast.makeText(getApplicationContext(), R.string.mainActivity_empty_password, Toast.LENGTH_LONG).show();
        }
        else {
            // When all fields are properly filled, start the Authentication Process.
            mAuth.signInWithEmailAndPassword(
                    emailtxt.getText().toString(),
                    passtxt.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful())
                            {

                                // Save Password Check.
                                if (pass_checkBox.isChecked())
                                {
                                    // Saving data locally in Shared Preference.
                                    editor= preferences.edit();
                                    editor.putBoolean("stayIn", true);
                                    editor.putString("username", emailtxt.getText().toString());
                                    editor.putString("password", passtxt.getText().toString());
                                    editor.apply();
                                }
                                else {
                                    // Don't save anything
                                }
                                currentUser = mAuth.getCurrentUser();

                                // Success Message Appears.
                                Toast.makeText(getApplicationContext(), R.string.mainActivity_success, Toast.LENGTH_LONG).show();

                                // Go to SendSMS Activity.
                                Intent intent = new Intent(getApplicationContext(), SendSMS.class);
                                startActivity(intent);
                            }
                            else {
                                // Login Error Message - Authentication Error.
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    // Register Intent Void.
    public void gotoregister(View view){
        Intent intent = new Intent(getApplicationContext(),Register.class);
        startActivity(intent);
    }

    // Reset password void.
    public void resetpassword(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        view = inflater.inflate(R.layout.reset_password, null);
        EditText submitEmail = view.findViewById(R.id.changepassword_email);
        builder.setCancelable(true)
                .setIcon(R.drawable.hardaliapp_logo)
                .setTitle(R.string.mainActivity_password_reset)
                .setMessage("Συμπληρώστε το e-mail σας, ώστε να μπορέσετε να επαναφέρετε τον κωδικό σας")
                .setView(view)
                .setNegativeButton("Ακύρωση", (dialogInterface, i) -> dialogInterface.cancel())
                .setPositiveButton("Αποστολή Email", (dialog, whichButton) -> {
                    if(submitEmail.getText().toString().isEmpty() || submitEmail.getText().length() <= 0){
                        // Empty Field Checker via if statement.
                        Toast.makeText(this, R.string.mainActivity_passwordreset_empty_fields, Toast.LENGTH_LONG).show();
                        //Assistant Voice.
                        speechRecognizer.speak("Παρακαλώ συμπληρώστε όλα τα απαραίτητα πεδία!");
                        // Debug Console's Message.
                        Log.d("Password Reset", "Empty Field.");
                        builder.create();
                    }
                    else {
                        mAuth.sendPasswordResetEmail(submitEmail.getText().toString())
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(this, R.string.mainActivity_passwordreset_success, Toast.LENGTH_LONG).show();
                                        // Debug Console's Message.
                                        Log.d("Password Reset", "Email sent.");
                                    }
                                    else {
                                        Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        // Debug Console's Message.
                                        Log.d("Password Reset", "Error during password reset email");
                                    }
                                });
                    }
                })
                .show();
    }

    // Check activity results for Speech Recognition and Mic's Events.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REC_RESULT && resultCode == RESULT_OK)
        {
            // Results' Array.
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (matches.contains("σύνδεση") || matches.contains("είσοδος") || matches.contains("μπες") || matches.contains("sign in")
                    || matches.contains("login") || matches.contains("access") || matches.contains("SMS"))
            {
                // Go to SendSMS Activity.
                startActivity(new Intent(this, SendSMS.class));
            }
            else if (matches.contains("επαναφορά") || matches.contains("ξέχασα") || matches.contains("κωδικό") || matches.contains("forget")
                    || matches.contains("νέο") || matches.contains("reset") || matches.contains("password") || matches.contains("new"))
            {
                // Reset Password Alert Dialog Appears.
                resetlbl.performClick();
            }
            else if (matches.contains("exit") || matches.contains("έξοδος") || matches.contains("close") || matches.contains("κλείσιμο"))
            {
                // Exits App
                this.finish();
                System.exit(0);
            }
            else if (matches.contains("εγγραφή") || matches.contains("εγγραφείτε") || matches.contains("register")
                    || matches.contains("sign up") || matches.contains("registration") || matches.contains("signup") || matches.contains("γίνε μέλος"))
            {
                // Go to Register Activity, in order to register the user.
                registerlbl.performClick();
            }
            else{
                // Toast Message.
                Toast.makeText(this, R.string.mainActivity_mic1, Toast.LENGTH_LONG).show();
                // Assistant's Voice Help.
                speechRecognizer.speak("Δεν σας καταλάβαμε! Παρακαλώ προσπαθήστε ξανά!");
            }
        }
        else {
            // Toast Message.
            Toast.makeText(this, mainActivity_mic2, Toast.LENGTH_LONG).show();
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