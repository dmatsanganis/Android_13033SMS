package com.dmatsanganis.hardaliapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import static com.dmatsanganis.hardaliapp.R.string.*;

public class UserProfile extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private MySpeechRecognizer speechRecognizer;
    private DatabaseReference databaseReference;
    FirebaseUser currentUser;
    private EditText nametxt;
    private EditText emailtxt;
    private EditText locationtxt;
    private EditText passtxt;
    private TextView password_change;
    private FloatingActionButton speechRecognitionButton;
    private Button updatebtn;
    private static final int REC_RESULT = 489;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //Toolbar BackArrow.
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
        password_change = findViewById(R.id.goto_resetpass_lbl2);
        updatebtn = findViewById(R.id.buttonUpdatePersonalInfo);
        speechRecognitionButton = findViewById(R.id.mic_user_profile);
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

        // Get references from Firebase Database and set up the textViews Layouts properly.
        databaseReference = FirebaseDatabase.getInstance().getReference("Users/" + currentUser.getUid()).child("Personal Details");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                locationtxt.setText(user.address);
                nametxt.setText(user.fullname);
                emailtxt.setText(user.email);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), user_profile_error1,Toast.LENGTH_LONG).show();
            }
        });
    }

    public void update(View view) {
        if(nametxt.getText().toString().isEmpty())
        {
            // Empty Fields Error Message Appears (Email's Field).
            Toast.makeText(getApplicationContext(), user_profile_empty_name, Toast.LENGTH_LONG).show();
        }
        else if(locationtxt.getText().toString().isEmpty())
        {
            // Empty Fields Error Message Appears (Address's Field).
            Toast.makeText(getApplicationContext(), user_profile_empty_address, Toast.LENGTH_LONG).show();
        }
        else {
            // Update User's Name and Location.
            User user = new User(nametxt.getText().toString(), emailtxt.getText().toString(), locationtxt.getText().toString());
            FirebaseDatabase.getInstance().getReference("Users/" + currentUser.getUid())
                    .child("Personal Details")
                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Success Message and Email Verification Reminder.
                        Toast.makeText(getApplicationContext(), R.string.user_profile_success_update, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.user_profile_error_update, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    // In - App Change Password via Alert Builder Layout.
    public void changepassword(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        view = inflater.inflate(R.layout.change_password, null);
        EditText oldPassword = view.findViewById(R.id.changepassword_old);
        EditText newPassword = view.findViewById(R.id.changepassword_newpass);
        EditText newPassword2 = view.findViewById(R.id.changepassword_newpass2);
        builder.setCancelable(true)
                .setIcon(R.drawable.hardaliapp_logo)
                .setTitle("Αλλαγή Κωδικόυ Πρόσβασης")
                .setMessage("Συμπληρώστε το e-mail και τον κωδικό σας, ώστε να μπορέσετε να αλλάξετε τον κωδικό σας")
                .setView(view)
                .setNegativeButton("Ακύρωση", (dialogInterface, i) -> dialogInterface.cancel())
                .setPositiveButton("Αλλαγή Κωδικού", (dialog, whichButton) -> {
                    // Empty Fields Check.
                    if ((oldPassword.getText().toString().equals("") && oldPassword.getText().length() <= 0) ||
                            (newPassword.getText().toString().equals("") && newPassword.getText().length() <= 0) ||
                            (newPassword2.getText().toString().equals("") && newPassword2.getText().length() <= 0))
                    {
                        // Error Toast Appears.
                        Toast.makeText(getApplicationContext(), R.string.user_profile_empty_fields, Toast.LENGTH_LONG).show();
                        // Debug Console's Message.
                        Log.d("Password Change", "Empty Fields");
                        builder.create();
                    }
                    else {
                        mAuth.signInWithEmailAndPassword(
                                emailtxt.getText().toString(),
                                oldPassword.getText().toString())
                                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            if (newPassword.getText().toString().equals(newPassword2.getText().toString())) {
                                                currentUser = mAuth.getCurrentUser();

                                                if (currentUser != null) {
                                                    currentUser.updatePassword(newPassword.getText().toString());
                                                    Toast.makeText(getApplicationContext(), R.string.user_profile_success_password_change, Toast.LENGTH_LONG).show();
                                                    // Debug Console's Message.
                                                    Log.d("Password Change", "Password's Change Success!");
                                                } else {
                                                    Toast.makeText(getApplicationContext(), R.string.user_profile_error_authentication, Toast.LENGTH_LONG).show();
                                                    // Debug Console's Message.
                                                    Log.d("Password Change", "Getting User Error");
                                                }
                                            } else {
                                                Toast.makeText(getApplicationContext(), R.string.user_profile_error_password_matching, Toast.LENGTH_LONG).show();
                                                // Debug Console's Message.
                                                Log.d("Password Change", "New Passwords Matching Problem");
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            // Debug Console's Message.
                                            Log.d("Password Change", "Task Unsuccessful");
                                        }
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

            if (matches.contains("έξοδος") || matches.contains("αποσύνδεση") || matches.contains("logout")
                    || matches.contains("exit") || matches.contains("sign out"))
            {
                // Go to MainActivity Activity.
                startActivity(new Intent(this, MainActivity.class));
            }
            else if (matches.contains("ενημέρωση") || matches.contains("update") ||  matches.contains("details") || matches.contains("personal details"))
            {
                // Update Personal Details.
                updatebtn.performClick();
            }
            else if (matches.contains("back") || matches.contains("πίσω"))
            {
                // Go to SendSMS Activity.
                startActivity(new Intent(this, SendSMS.class));
            }
            else if (matches.contains("αλλαγή") || matches.contains("κωδικού") || matches.contains("code") || matches.contains("password")
                    || matches.contains("change"))
            {
                // Change Password Button.
                password_change.performClick();
            }
            else{
                // Toast Message.
                Toast.makeText(this, R.string.user_profile_mic_error, Toast.LENGTH_LONG).show();
                // Assistant's Voice Help.
                speechRecognizer.speak("Δεν σας καταλάβαμε! Παρακαλώ προσπαθήστε ξανά!");
            }
        }
        else {
            // Toast Message.
            Toast.makeText(this, R.string.user_profile_mic_error_2, Toast.LENGTH_LONG).show();
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

