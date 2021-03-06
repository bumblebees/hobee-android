package bumblebees.hobee;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import bumblebees.hobee.fragments.PlacePickerFragment;
import bumblebees.hobee.objects.Event;
import bumblebees.hobee.objects.EventDetails;
import bumblebees.hobee.objects.Hobby;
import bumblebees.hobee.objects.PublicUser;
import bumblebees.hobee.objects.User;
import bumblebees.hobee.utilities.DatePickerFragment;
import bumblebees.hobee.utilities.MQTTService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import bumblebees.hobee.utilities.SessionManager;
import bumblebees.hobee.utilities.TimePickerFragment;
import io.apptik.widget.MultiSlider;


public class NewEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, PlacePickerFragment.OnActivityResultListener {

    private Button btnAddEvent;
    private Button setDateBtn;
    private Button setTimeBtn;
    private Button setLocationBtn;
    private TextView maxAge;
    private TextView minAge;
    private TextView inputEventName;
    private TextView inputEventLocation;
    private TextView inputEventDescription;
    private TextView inputEventDate;
    private TextView inputEventTime;
    private Spinner inputEventGender;
    private Spinner eventHobbyChoice;
    private Spinner spinnerLocation;
    private Spinner spinnerHobbySkillChoice;
    private TextView inputEventNumber;
    private MultiSlider ageRangeSlider;
    private Place place;
    private User loggedInUser;


    private HashMap<String, String> areas;

    @Override
    public void updateEvent(Place place) {
        inputEventLocation.setText(place.getAddress());
        this.place = place;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);
        View v = findViewById(android.R.id.content);
        
        SessionManager session = new SessionManager(this);
        loggedInUser = session.getUser();

        inputEventName = (TextView) findViewById(R.id.inputEventName);
        inputEventDescription = (TextView) findViewById(R.id.inputEventDescription);
        inputEventLocation = (TextView) findViewById(R.id.inputEventLocation);

        inputEventDate = (TextView) findViewById(R.id.inputEventDate);
        inputEventTime = (TextView) findViewById(R.id.inputEventTime);
        inputEventGender = (Spinner) findViewById(R.id.inputEventGender);
        inputEventNumber = (TextView) findViewById(R.id.inputEventNumber);
        ageRangeSlider = (MultiSlider) v.findViewById(R.id.age_range_slider);
        ageRangeSlider.setMin(16);
        ageRangeSlider.setMax(96);
        maxAge = (TextView) findViewById(R.id.maxAge);
        minAge = (TextView) findViewById(R.id.minAge);
        minAge.setText(String.valueOf(ageRangeSlider.getThumb(0).getValue()));
        maxAge.setText(String.valueOf(ageRangeSlider.getThumb(1).getValue()));
        eventHobbyChoice = (Spinner) findViewById(R.id.eventHobbyChoice);
        spinnerLocation = (Spinner) findViewById(R.id.spinnerLocation);
        spinnerHobbySkillChoice = (Spinner) findViewById(R.id.spinnerSkillChoice);
        setDateBtn = (Button) findViewById(R.id.setDateBtn);
        setTimeBtn = (Button) findViewById(R.id.setTimeBtn);
        setLocationBtn = (Button) findViewById(R.id.setLctBtn);

        //set gender spinner options
        String[] genderOptions = getResources().getStringArray(R.array.eventGenderOptions);
        ArrayAdapter<String> genderChoice = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genderOptions);
        inputEventGender.setAdapter(genderChoice);

        //set skill spinner options
        String[] hobbySkill = getResources().getStringArray(R.array.hobbySkillOptions);
        ArrayAdapter<String> hobbySkillChoice = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, hobbySkill);
        spinnerHobbySkillChoice.setAdapter(hobbySkillChoice);

        String[] hobbyChoices = loggedInUser.getHobbyNames().toArray(new String[loggedInUser.getHobbyNames().size()]);

        ArrayAdapter<String> hobbyChoice = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, hobbyChoices);

        eventHobbyChoice.setAdapter(hobbyChoice);

        btnAddEvent = (Button) findViewById(R.id.eventAddNew);

        btnAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewEvent();
            }
        });

        setTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });

        setDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        ageRangeSlider.setOnThumbValueChangeListener(new MultiSlider.OnThumbValueChangeListener() {
            @Override
            public void onValueChanged(MultiSlider multiSlider, MultiSlider.Thumb thumb, int thumbIndex, int value) {
                if (thumbIndex == 0) {
                    minAge.setText(String.valueOf(value));
                } else {
                    maxAge.setText(String.valueOf(value));
                }
            }
        });

        setLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getSupportFragmentManager();
                PlacePickerFragment fragment = (PlacePickerFragment) fm.findFragmentByTag("PlacePickerFragment");
                FragmentTransaction transaction = fm.beginTransaction();
                fragment = new PlacePickerFragment();
                transaction.add(fragment,"PlacePickerFragment");
                transaction.commit();
            }
        });

    }

    private void openMap() {
        PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
        try {
            Intent intent = intentBuilder.build(NewEventActivity.this);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //set location spinner options by matching them with the selected locations in the preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        areas = new HashMap<>();
        String[] locations = getResources().getStringArray(R.array.locationTopicSpinner);
        String[] topics = getResources().getStringArray(R.array.locationTopicValues);

        Set<String> emptyLocation = new HashSet<>(); //to prevent null pointer exception

        Set<String> preferencesStringSet = preferences.getStringSet("location_topics", emptyLocation);

        //check if the preferences have been set and show an error if not
        if (preferencesStringSet.isEmpty()) {
            btnAddEvent.setEnabled(false);
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "No location selected in preferences", Snackbar.LENGTH_INDEFINITE)
                    .setAction("go", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent settingsIntent = new Intent(NewEventActivity.this, SettingsActivity.class);
                            startActivity(settingsIntent);
                        }
                    });
            snackbar.show();
        } else {
            btnAddEvent.setEnabled(true);
            ArrayList finalLocations = new ArrayList();
            for (int i = 0; i < locations.length; i++) {
                if (preferencesStringSet.contains(topics[i])) {
                    //only add the values to the spinner that also exist in the user selected preferences
                    areas.put(locations[i], topics[i]);
                    finalLocations.add(locations[i]);
                }
                ArrayAdapter<String> locationChoice = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, finalLocations);
                spinnerLocation.setAdapter(locationChoice);
            }
        }
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        month = month + 1;
        inputEventDate.setText(year + "-" + month + "-" + day);
        if (month < 10 && day < 10)
            inputEventDate.setText(year + "-0" + month + "-0" + day);
        else {
            if (month < 10)
                inputEventDate.setText(year + "-0" + month + "-" + day);
            if (day < 10)
                inputEventDate.setText(year + "-" + month + "-0" + day);
        }
    }


    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        inputEventTime.setText(hourOfDay + ":" + minute);
        if (hourOfDay < 10 && minute < 10)
            inputEventTime.setText("0" + hourOfDay + ":0" + minute);
        else {
            if (hourOfDay < 10)
                inputEventTime.setText("0" + hourOfDay + ":" + minute);
            if (minute < 10)
                inputEventTime.setText(hourOfDay + ":0" + minute);
        }
    }

    /**
     * Creates the JSON that will be sent over MQTT using the completed fields in the form.
     */
    private void addNewEvent() {
        long timeCreated = Calendar.getInstance().getTimeInMillis() / 1000L;
        String eventCategory = eventHobbyChoice.getSelectedItem().toString();
        String hostID = loggedInUser.getUserID();

        UUID uuid = UUID.randomUUID();

        if(inputEventDate.getText().toString().equals("") || inputEventTime.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "Please fill in the date and time.", Toast.LENGTH_SHORT).show();
        }
        if(inputEventLocation.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "Please select a location", Toast.LENGTH_SHORT).show();
        }
        else {
            String timestamp = "";
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            try {
                Date date = sdf.parse(inputEventDate.getText().toString() + " " + inputEventTime.getText().toString());
                cal.setTime(date);
                timestamp = String.valueOf(cal.getTimeInMillis() / 1000L);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //check if the event time is in the future
            if (Long.parseLong(timestamp) > timeCreated && Integer.parseInt(inputEventNumber.getText().toString())>1) {
                ArrayList<PublicUser> acceptedUsers = new ArrayList<>();
                PublicUser currentUser = loggedInUser.getSimpleUser();
                acceptedUsers.add(currentUser);
                ArrayList<String> users_unranked = new ArrayList<>();
                users_unranked.add(loggedInUser.getUserID());

                try {
                    Hobby hobby = new Hobby(eventHobbyChoice.getSelectedItem().toString(), spinnerHobbySkillChoice.getSelectedItem().toString());
                    EventDetails eventDetails = new EventDetails(inputEventName.getText().toString(), hostID, currentUser.getName(),
                            Integer.parseInt(minAge.getText().toString()), Integer.parseInt(maxAge.getText().toString()), inputEventGender.getSelectedItem().toString(),
                            timestamp, Integer.parseInt(inputEventNumber.getText().toString()), place.getLatLng().toString(), inputEventDescription.getText().toString(),
                            new ArrayList<PublicUser>(), acceptedUsers, hobby, users_unranked);

                    final Event event = new Event(uuid, eventCategory, String.valueOf(timeCreated), eventDetails, areas.get(spinnerLocation.getSelectedItem().toString()));


                    Intent intent = new Intent(this, MQTTService.class);
                    ServiceConnection serviceConnection = new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                            MQTTService.MQTTBinder binder = (MQTTService.MQTTBinder) iBinder;
                            MQTTService service = binder.getInstance();
                            service.addOrUpdateEvent(event);
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName componentName) {

                        }
                    };
                    bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

                    Context context = getApplicationContext();
                    CharSequence text = "Event created!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                    Intent homeIntent = new Intent(NewEventActivity.this, HomeActivity.class);
                    NewEventActivity.this.startActivity(homeIntent);
                } catch (NullPointerException e) {

                    Toast.makeText(getApplicationContext(), "Please fill in all the fields.", Toast.LENGTH_SHORT).show();

                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Please fill in all the fields.", Toast.LENGTH_SHORT).show();
                }

            }
            else {
                if(Long.parseLong(timestamp)<timeCreated){
                    Toast.makeText(getApplicationContext(), "Events cannot be created in the past.", Toast.LENGTH_SHORT).show();
                }
                if(!(Integer.parseInt(inputEventNumber.getText().toString())>1)) {
                    Toast.makeText(getApplicationContext(), "Invalid ammount of people", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}



