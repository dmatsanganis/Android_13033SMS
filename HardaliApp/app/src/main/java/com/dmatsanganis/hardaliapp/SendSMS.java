 package com.dmatsanganis.hardaliapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dmatsanganis.hardaliapp.Database.DatabaseConfiguration;
import com.dmatsanganis.hardaliapp.Database.DatabaseModel;
import com.dmatsanganis.hardaliapp.Messages.AddMessage;
import com.dmatsanganis.hardaliapp.Messages.ViewEditedMessages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import static android.content.pm.PackageManager.*;

public class SendSMS extends AppCompatActivity implements LocationListener {
    FirebaseUser currentUser;
    LocationManager locationManager;
    DatabaseConfiguration databaseConfiguration;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private CardView cardReason1, cardReason2, cardReason3, cardReason4, cardReason5, cardReason6, cardReason7;
    private MaterialButton info1, info2, info3, info4, info5, info6, info7, previewSMS;
    private MySpeechRecognizer speechRecognizer;
    private FloatingActionButton speechRecognitionButton;
    private static final int REC_RESULT = 489;
    double latitude;
    double longitude;
    private EditText nametxt;
    private EditText emailtxt;
    private EditText locationtxt;
    String [] reasonDataArray, reasonTitleArray, reasonIDArray;
    String reasonNumber;


    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendsms);
        mAuth = FirebaseAuth.getInstance();
        // Check if user is signed in (non-null).
        currentUser = mAuth.getCurrentUser();
        speechRecognitionButton = findViewById(R.id.mic_sendSMS);
        previewSMS = findViewById(R.id.previewSMSfab);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        nametxt = findViewById(R.id.textInputName);
        emailtxt = findViewById(R.id.textInputEmail);
        locationtxt = findViewById(R.id.textInputLocation);
        speechRecognizer = new MySpeechRecognizer(this);

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        if(bundle != null)
        {
            reasonNumber = bundle.getString("codeID");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.sendSMS_intro);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_round_keyboard_backspace);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); //finishing activity
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

        // Lambda Expression Button listener (Microphone/TextToSpeech Tool).
        previewSMS.setOnClickListener((view) -> {
            // Location's Permission check.
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Ask for Location's Permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 234);
            }
            else {

                // If permission is already given continue, with the initialization
                // of the proper functions, isLocationEnabled() and sendSMSExternal(),
                // while you request location updates.
                isLocationEnabled(this);

                // Checks if a Message's reason is selected.
                if(validateInput())
                {
                    sendSMSExternal();
                }
            }
        });

        // Get references from Firebase Database and set up the textViews Layouts properly.
        databaseReference = FirebaseDatabase.getInstance().getReference("Users/" + currentUser.getUid()).child("Personal Details");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                nametxt.setText(user.fullname);
                emailtxt.setText(user.email);
                locationtxt.setText(user.address);
                toolbar.setSubtitle(user.fullname);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), R.string.sendSMS_error_authentication,Toast.LENGTH_LONG).show();
            }
        });

        cardReason1 = findViewById(R.id.cardReason1);
        cardReason2 = findViewById(R.id.cardReason2);
        cardReason3 = findViewById(R.id.cardReason3);
        cardReason4 = findViewById(R.id.cardReason4);
        cardReason5 = findViewById(R.id.cardReason5);
        cardReason6 = findViewById(R.id.cardReason6);
        cardReason7 = findViewById(R.id.cardReason7);


        info1 = findViewById(R.id.buttonInfoReason1);
        info2 = findViewById(R.id.buttonInfoReason2);
        info3 = findViewById(R.id.buttonInfoReason3);
        info4 = findViewById(R.id.buttonInfoReason4);
        info5 = findViewById(R.id.buttonInfoReason5);
        info6 = findViewById(R.id.buttonInfoReason6);
        info7 = findViewById(R.id.buttonInfoReason7);

        cardClickHandler();
        cardLongClickHandler();


        databaseConfiguration = new DatabaseConfiguration(this);
        databaseConfiguration.getData();

        reasonDataArray = databaseConfiguration.getReasonDataArray();
        reasonTitleArray = databaseConfiguration.getReasonTitleArray();
        reasonIDArray = databaseConfiguration.getReasonIDArray();


    }

    private void cardLongClickHandler() {
        info1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertSMSInfo(0);
            }
        });
        info2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertSMSInfo(1);
            }
        });
        info3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertSMSInfo(2);
            }
        });
        info4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertSMSInfo(3);
            }
        });
        info5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertSMSInfo(4);
            }
        });
        info6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertSMSInfo(5);
            }
        });
        info7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertEditMessageInfo();
            }
        });
    }

    private void sendSMSExternal() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.sms_confirmation, null);
        builder.setCancelable(true)
                .setView(view);

        // Initialize Components.
        TextView textMessagePreview = view.findViewById(R.id.smsPreview);
        MaterialButton buttonSend = view.findViewById(R.id.smsButtonSend);
        MaterialButton buttonCancel = view.findViewById(R.id.smsButtonCancel);

        // Set properly the SMS text in order to be confirmed by the user.
        StringBuilder stringBuilder = new StringBuilder().append(reasonNumber).append(" ").append(nametxt.getText().toString()).append(" ").append(locationtxt.getText().toString());
        textMessagePreview.setText(stringBuilder.toString());

        final AlertDialog dialog = builder.create();

        buttonSend.setOnClickListener(v -> {
            // Checks for Location Permission.
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            // Checks for SMS Permission.
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!=
            PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},5435);
            }
            else{
                // If the Permission is already given.
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

                // Declare progressDialog, until GPS provides location..
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(getString(R.string.sendSMS_loading_progressDialog));
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();

                generateMessage();
                dialog.dismiss();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Objects.requireNonNull(dialog.getWindow()).
                setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

    }

    private void generateMessage() {
        StringBuilder stringBuilder = new StringBuilder().append(reasonNumber).append(" ").append(nametxt.getText().toString()).append(" ").append(locationtxt.getText().toString());
        SmsManager manager = SmsManager.getDefault();
        String phone_number = "13033";
        manager.sendTextMessage(phone_number,null, stringBuilder.toString(),null,null);
    }

    private boolean validateInput() {
        // No Message's Reason Check.
        if (reasonNumber == null || reasonNumber.isEmpty()) {
            //no reason number is not selected
            showSnackBar(getString(R.string.sendSMS_snackbar_error_empty_reasonNumber));
            return false;
        }
        // No errors so continue the process.
        return true;
    }

    private void alertSMSInfo(int numberOfReason) {

        ArrayList<String> data = new ArrayList<>();
        ArrayList<String> reasons = new ArrayList<>();

        initDataForDialog(data, reasons);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.sms_reason_info, null);
        builder.setCancelable(true)
                .setView(view);

        MaterialButton close;
        TextView title, message;

        close = view.findViewById(R.id.infoAlertButtonClose);
        title = view.findViewById(R.id.infoAlertTitle);
        message = view.findViewById(R.id.infoAlertName);

        title.setText(String.format("%s. %s", numberOfReason + 1 , reasons.get(numberOfReason)));
        message.setText(data.get(numberOfReason));

        final AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).
                setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }


    // Add - Edit Messages Alert Info.
    private void alertEditMessageInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_sms_info, null);
        builder.setCancelable(true)
                .setView(view);

        // Initialize about.xml 's components.
        MaterialButton close;
        close = view.findViewById(R.id.editAlertButtonClose);

        final AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).
                setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    // About - App's Info Alert.
    private void alertAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.about, null);
        builder.setCancelable(true)
                .setView(view);

        // Initialize about.xml 's components.
        MaterialButton close;
        close = view.findViewById(R.id.aboutAlertButtonClose);

        final AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).
                setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    private void initDataForDialog(ArrayList<String> data, ArrayList<String> reasons) {

        data.add(getResources().getString(R.string.summary_request_no1));
        data.add(getResources().getString(R.string.summary_request_no2));
        data.add(getResources().getString(R.string.summary_request_no3));
        data.add(getResources().getString(R.string.summary_request_no4));
        data.add(getResources().getString(R.string.summary_request_no5));
        data.add(getResources().getString(R.string.summary_request_no6));


        //init reason titles
        reasons.add(getResources().getString(R.string.title_request_no1));
        reasons.add(getResources().getString(R.string.title_request_no2));
        reasons.add(getResources().getString(R.string.title_request_no3));
        reasons.add(getResources().getString(R.string.title_request_no4));
        reasons.add(getResources().getString(R.string.title_request_no5));
        reasons.add(getResources().getString(R.string.title_request_no6));
    }

    private void cardClickHandler() {
        cardReason1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reasonNumber = "1";
                showSnackBar(" Έξοδος για: " + getResources().getString(R.string.title_request_no1));
            }
        });
        cardReason2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reasonNumber = "2";
                showSnackBar(" Έξοδος για: " + getResources().getString(R.string.title_request_no2));
            }
        });
        cardReason3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reasonNumber = "3";
                showSnackBar(" Έξοδος για: " + getResources().getString(R.string.title_request_no3));
            }
        });
        cardReason4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reasonNumber = "4";
                showSnackBar(" Έξοδος για: " + getResources().getString(R.string.title_request_no4));
            }
        });
        cardReason5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reasonNumber = "5";
                showSnackBar(" Έξοδος για: " + getResources().getString(R.string.title_request_no5));
            }
        });
        cardReason6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reasonNumber = "6";
                showSnackBar(" Έξοδος για: " + getResources().getString(R.string.title_request_no6));
            }
        });
        cardReason7.setOnClickListener((View view) -> {
            //Intent to Add new Message's code class.
            Intent intent = new Intent(this, AddMessage.class);
            this.startActivity(intent);
        });
    }

    private void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
        snackbar.setDuration(Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    // App's Navigation Menu Void.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // App's Navigation Menu Void.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // An Int type variable to identify the user's menu choice.
        int id = item.getItemId();

        if (id == R.id.action_info)
        {
            // Go to About Alert (about.xml).
            alertAbout();
            return true;
        }

        if (id == R.id.action_user_profile)
        {
            // Go to User_Profile Activity.
            startActivity(new Intent(this, UserProfile.class));
            return true;
        }

        if (id == R.id.action_newcodes)
        {
            // Go to ViewEditedMessages - custom created or edited messages - Activity.
            startActivity(new Intent(this, ViewEditedMessages.class));
            return true;
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

            if (matches.contains("back") || matches.contains("πίσω") || matches.contains("έξοδος") || matches.contains("αποσύνδεση")
                || matches.contains("logout") || matches.contains("exit") || matches.contains("sign out"))
            {
                // Go to MainActivity Activity.
                startActivity(new Intent(this, MainActivity.class));
            }
            else if (matches.contains("προφίλ") || matches.contains("προφίλ χρήστη") || matches.contains("στοιχεία")
                    || matches.contains("user profile") || matches.contains("profile") || matches.contains("details") || matches.contains("personal details"))
            {
                // Go to User_Profile Activity.
                startActivity(new Intent(this, UserProfile.class));
            }
            else if (matches.contains("about") || matches.contains("σχετικά"))
            {
                // Go to About custom Alert Dialog.
                alertAbout();
            }
            else if (matches.contains("το ένα") || matches.contains("ενα") || matches.contains("ένα") || matches.contains("1") || matches.contains("φαρμακείο") || matches.contains("γιατρό")
               || matches.contains("νοσοκομείο") || matches.contains("pharmacy") || matches.contains("doctor") || matches.contains("Dr") || matches.contains("hospital"))
            {
                cardReason1.performClick();

                // Assistant's Voice Help.
                speechRecognizer.speak("Μετακίνηση προς Φαρμακείο - Ιατρείο");
            }
            else if (matches.contains("το δύο") || matches.contains("δύο") || matches.contains("2") || matches.contains("σούπερ") || matches.contains("μάρκετ")
                    || matches.contains("ψώνια") || matches.contains("λαϊκή") || matches.contains("αγορά") || matches.contains("super") | matches.contains("market") | matches.contains("store"))
            {
                cardReason2.performClick();

                // Assistant's Voice Help.
                speechRecognizer.speak("Μετακίνηση προς Κατάστημα προμήθειας αγαθών");
            }
            else if (matches.contains("το τρία") || matches.contains("τρία") || matches.contains("3") || matches.contains("τράπεζα") || matches.contains("ATM")
                    || matches.contains("bank") || matches.contains("money") || matches.contains("λεφτά") || matches.contains("ανάληψη"))
            {
                cardReason3.performClick();

                // Assistant's Voice Help.
                speechRecognizer.speak("Μετακίνηση προς Τράπεζα");
            }
            else if (matches.contains("το τέσσερα") || matches.contains("τέσσερα") || matches.contains("4") || matches.contains("ανάγκη") || matches.contains("παροχή")
                    || matches.contains("help") || matches.contains("σχολείο") || matches.contains("μαθητή") || matches.contains("συνοδεία") || matches.contains("ηλικιωμένο"))
            {
                cardReason4.performClick();

                // Assistant's Voice Help.
                speechRecognizer.speak("Μετακίνηση για Παροχή βοήθειας");
            }
            else if (matches.contains("το πέντε") || matches.contains("πέντε") || matches.contains("5") || matches.contains("κηδεία") || matches.contains("τελετή")
                    || matches.contains("χωρισμένοι") || matches.contains("διαζευγμένων") || matches.contains("γονείς") || matches.contains("funeral"))
            {
                cardReason5.performClick();

                // Assistant's Voice Help.
                speechRecognizer.speak("Μετακίνηση για Τελετή - Γονείς σε διάσταση");
            }
            else if (matches.contains("το έξι") || matches.contains("έξι") || matches.contains("6") || matches.contains("τρέξιμο") || matches.contains("βόλτα")
                    || matches.contains("κατοικίδιο") || matches.contains("pet") || matches.contains("run") || matches.contains("άθληση") || matches.contains("sport") || matches.contains("ανάγκες"))
            {
                cardReason6.performClick();

                // Assistant's Voice Help.
                speechRecognizer.speak("Μετακίνηση για Άθληση - Ανάγκες κατοικίδιου");
            }
            else if (matches.contains("στείλε") || matches.contains("στείλτο") || matches.contains("13033") || matches.contains("send") || matches.contains("SMS")
                    || matches.contains("μήνυμα") || matches.contains("message"))
            {
                // Send SMS.
                previewSMS.performClick();
            }
            else{
                // Toast Message.
                Toast.makeText(this, R.string.sendSMS_mic_error1, Toast.LENGTH_LONG).show();
                // Assistant's Voice Help.
                speechRecognizer.speak("Δεν σας καταλάβαμε! Παρακαλώ προσπαθήστε ξανά!");
            }
        }
        else {
            // Toast Message.
            Toast.makeText(this, R.string.sendSMS_mic_error2, Toast.LENGTH_LONG).show();
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

    public void onLocationChanged(@NonNull Location location) {
        if(location!=null)
        {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            DateFormat dateFormat = new SimpleDateFormat("EEE dd-MM-yyyy HH-mm-ss");
            String date = dateFormat.format(Calendar.getInstance().getTime());
            User user = new User(reasonNumber, latitude, longitude, date);
            FirebaseDatabase.getInstance().getReference("Users/" + currentUser.getUid() + "/Location History").
                    child(date).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Close Progress Dialog.
                        progressDialog.dismiss();
                        // Success Message
                        Toast.makeText(getApplicationContext(), R.string.sendSMS_send_success, Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        else{
            //If location == null
            Toast.makeText(getApplicationContext(), R.string.sendSMS_error_no_location, Toast.LENGTH_LONG).show();
        }
        locationManager.removeUpdates(this);
    }

    // Checks if location service is enabled by the user.
    public void isLocationEnabled(Context context)
    {
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch(Exception ex) {}

        if(!gps_enabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle(R.string.sendSMS_locationisDisabled_title)
                    .setIcon(R.drawable.hardaliapp_logo)
                    .setMessage(R.string.sendSMS_locationisDisabled_message)
                    .setPositiveButton(R.string.sendSMS_locationisDisabled_positive_btn, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(R.string.sendSMS_locationisDisabled_negative_btn, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dialogInterface.cancel();
                        }
                    })
                    .show();
        }
    }

    // Ask to enable location if it is disabled.
    @Override
    public void onResume(){
        super.onResume();
        isLocationEnabled(this);
    }
}