package bumblebees.hobee;

import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;


import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.OrientationHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import bumblebees.hobee.fragments.CancelEventDialogFragment;
import bumblebees.hobee.objects.Event;
import bumblebees.hobee.objects.PublicUser;
import bumblebees.hobee.utilities.MQTTService;
import bumblebees.hobee.utilities.SessionManager;
import bumblebees.hobee.utilities.SocketIO;

import static bumblebees.hobee.R.color.Bee_color_1;


public class EventViewActivity extends AppCompatActivity {
   private TextView eventName;
    private TextView eventDescription;
    TextView eventLocation;
    private TextView eventDate;
    private TextView eventTime;
    private TextView eventPeople;
    private TextView eventGender;
    private TextView eventAge;
    private TextView eventHostName;
    private TextView eventHobbySkill;
    private TextView eventHobby;
    private MapFragment map;
    private Gson g;
    private String eventString;
    private LinearLayout containerUsers;
    private LinearLayout containerPending;
    private LinearLayout locationContainer;
    private LinearLayout mapContainer;
    private Event event;
    private Button btnJoinEvent;
    private SessionManager session;



    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            GoogleMap gMap = googleMap;
            String unparsedLocation = event.getEvent_details().getLocation();
            try {
                unparsedLocation = unparsedLocation.substring(unparsedLocation.indexOf("(")+1,unparsedLocation.indexOf(")"));
                String[] latlong = unparsedLocation.split(",");
                double lat = Double.parseDouble(latlong[0]);
                double lng = Double.parseDouble(latlong[1]);
                LatLng position = new LatLng(lat,lng);
                gMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)));
                CameraPosition cameraPosition = new CameraPosition.Builder().target(position).zoom(14.0f).build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                gMap.moveCamera(cameraUpdate);

            } catch (Exception e){
                Log.d("Error creating map",e.toString());
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);
        eventName = (TextView) findViewById(R.id.eventName);
        eventDescription = (TextView) findViewById(R.id.eventDescription);
        eventDate = (TextView) findViewById(R.id.eventDate);
        eventTime = (TextView) findViewById(R.id.eventTime);
        eventPeople = (TextView) findViewById(R.id.eventPeople);
        eventGender = (TextView) findViewById(R.id.eventGender);
        eventAge = (TextView) findViewById(R.id.eventAge);
        eventHostName = (TextView) findViewById(R.id.eventHostName);
        eventHobby = (TextView) findViewById(R.id.eventHobby);
        eventHobbySkill = (TextView)findViewById(R.id.eventHobbySkill);
        mapContainer =(LinearLayout)findViewById(R.id.mapContainer);
        containerUsers = (LinearLayout) findViewById(R.id.containerUsers);
        containerPending = (LinearLayout) findViewById(R.id.containerPending);
        btnJoinEvent = (Button) findViewById(R.id.btnJoinEvent);
        locationContainer = (LinearLayout) findViewById(R.id.locationContainer);

        session = new SessionManager(this);

        Intent intent = getIntent();
        g = new Gson();
        eventString = intent.getStringExtra("event");
        event = g.fromJson(eventString, Event.class);

        if(event.getEvent_details().getLocation().contains("lat/lng:")) {
            map = MapFragment.newInstance();
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().replace(R.id.mapContainer, map).commit();
            map.getMapAsync(callback);
        }
        else {
            locationContainer.removeView(mapContainer);
            locationContainer.setOrientation(OrientationHelper.HORIZONTAL);
            TextView location = new TextView(getApplicationContext());
            location.setTextSize(18);
            location.setTextColor(getResources().getColor(Bee_color_1));
            location.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT));
            location.setText(event.getEvent_details().getLocation());
            locationContainer.addView(location);
        }


        PublicUser currentUser = session.getUser().getSimpleUser();


        //check if the event is still active
        //if not, disable all interactions with the event
        if(event.isEventActive()) {

            //check if the user is also the host of the event
            //if so, turn the button into one that cancels the event
            if (event.getEvent_details().getHost_id().equals(currentUser.getUserID())) {
                btnJoinEvent.setText("Host: cancel event");
                //btnJoinEvent.setEnabled(false);

                btnJoinEvent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cancelEvent(event);
                    }
                });

                containerPending.setVisibility(View.VISIBLE);

                if (event.getEvent_details().getUsers_pending().size() == 0) {
                    TextView pendingUsers = new TextView(this.getApplicationContext());
                    pendingUsers.setText("No users found.");
                    pendingUsers.setPadding(0, 10, 0, 10);
                    pendingUsers.setTextSize(16);
                    pendingUsers.setTypeface(null, Typeface.ITALIC);
                    pendingUsers.setTextColor(getResources().getColor(R.color.Bee_color_1));
                    containerPending.addView(pendingUsers);
                } else {
                    for (final PublicUser user : event.getEvent_details().getUsers_pending()) {
                        LayoutInflater inflater = LayoutInflater.from(this);
                        final View row = inflater.inflate(R.layout.user_accept_item, containerPending, false);


                        TextView pendingUser = (TextView) row.findViewById(R.id.userAcceptName);
                        pendingUser.setPadding(0, 10, 0, 0);
                        pendingUser.setText(user.getName());
                        pendingUser.setTextSize(16);
                        pendingUser.setTextColor(getResources().getColor(R.color.Bee_color_1));
                        pendingUser.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                viewUserProfile(user.getUserID());
                            }
                        });

                        Button acceptUser = (Button) row.findViewById(R.id.userAcceptButton);
                        acceptUser.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                event = session.findUpdatedHostEvent(event);
                                event.getEvent_details().confirmUser(user);
                                updateEvent(event);
                                finish();
                                Intent updatedIntent = getIntent();
                                updatedIntent.putExtra("event", g.toJson(event));
                                startActivity(updatedIntent);
                            }
                        });
                        Button rejectUser = (Button) row.findViewById(R.id.userRejectButton);
                        rejectUser.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                event = session.findUpdatedHostEvent(event);
                                event.getEvent_details().rejectUser(user);
                                updateEvent(event);
                                finish();
                                Intent updatedIntent = getIntent();
                                updatedIntent.putExtra("event", g.toJson(event));
                                startActivity(updatedIntent);
                            }
                        });

                        containerPending.addView(row);

                    }
                }


            }
            //check if the user has already joined the event
            else if (event.getEvent_details().checkUser(currentUser)) {
                //disable the button so that the user cannot join the event again
                btnJoinEvent.setText("You have already joined this event");
                btnJoinEvent.setEnabled(false);
            } else {
                //add the on-click listener so that the user can join the event

                btnJoinEvent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        joinEvent(event);
                    }
                });
            }
        }
        else{
            //event is inactive, remove all interaction with it
            btnJoinEvent.setText("You cannot join a past event.");
            btnJoinEvent.setEnabled(false);
        }

        eventName.setText(event.getEvent_details().getEvent_name());
        eventDescription.setText(event.getEvent_details().getDescription());

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Long.parseLong(event.getEvent_details().getTimestamp())*1000L);
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

        eventDate.setText(String.valueOf(sdfDate.format(cal.getTime())));
        eventTime.setText(String.valueOf(sdfTime.format(cal.getTime())));

        eventGender.setText(event.getEvent_details().getGender());
        eventPeople.setText(String.valueOf(event.getEvent_details().getMaximum_people()));
        eventAge.setText(event.getEvent_details().getAge_min()+"-"+event.getEvent_details().getAge_max());
        eventHostName.setText(event.getEvent_details().getHost_name());

        eventHobbySkill.setText(event.getEvent_details().getHobbySkill());
        eventHobby.setText(event.getEvent_details().getHobbyName());

        for(final PublicUser publicUser : event.getEvent_details().getUsers_accepted()){
            TextView acceptedUser = new TextView(this.getApplicationContext());
            acceptedUser.setPadding(0, 10, 0, 0);
            acceptedUser.setText(publicUser.getName());
            acceptedUser.setTextSize(30);
            acceptedUser.setTextColor(getResources().getColor(R.color.Bee_color_1));
            acceptedUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewUserProfile(publicUser.getUserID());
                }
            });
            containerUsers.addView(acceptedUser);
        }

        eventHostName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewUserProfile(event.getEvent_details().getHost_id());
            }
        });
    }



    /**
     * Adds the currently logged in user to the event, publishes it to the MQTT topic.
     * Returns to the home activity with a confirmation Toast.
     * @param event - event to be joined
     */

    private void joinEvent(Event event){

        event = session.findUpdatedPendingEvent(event);
        PublicUser currentUser = session.getUser().getSimpleUser();
        event.getEvent_details().addUser(currentUser);

        updateEvent(event);

        Context context = getApplicationContext();
        CharSequence text = "Event joined!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        Intent homeIntent = new Intent(EventViewActivity.this, HomeActivity.class);
        EventViewActivity.this.startActivity(homeIntent);

    }

    /**
     * Updates the event on the MQTT broker.
     * @param event - event to be updated
     */
    private void updateEvent(final Event event) {
        Intent intent = new Intent(this, MQTTService.class);
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                MQTTService.MQTTBinder binder = (MQTTService.MQTTBinder) iBinder;
                MQTTService service = binder.getInstance();
                service.addOrUpdateEvent(event);
                service.getEvents().removeEligibleEvent(event.getType(), event);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

    }

    /**
     * Cancels the current event.
     * @param event - current event
     */
    private void cancelEvent(Event event){

        Gson gson = new GsonBuilder().setVersion(0.3).create();
        DialogFragment cancelEventDialog = new CancelEventDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("event", gson.toJson(event));
        cancelEventDialog.setArguments(bundle);
        cancelEventDialog.show(getSupportFragmentManager(), "cancelEvent");

    }

    private void viewUserProfile(String userID){
        SocketIO.getInstance().getUserAndOpenProfile(userID,getApplicationContext());
    }
}
