package bumblebees.hobee.objects;

import android.util.Log;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;


public class EventDetails {

    EventDetails(){
        super();
    }

    private String event_name;
    private String host_id;
    private String host_name;
    private int age_min;
    private int age_max;
    private String gender;
    private String timestamp;
    private int maximum_people;
    private String location;
    private String description;
    private List<SimpleUser> users_pending;
    private List<SimpleUser> users_accepted;

    private Hobby hobby;

    public EventDetails(String event_name, String host_id, String host_name, int age_min, int age_max,
                        String gender, String timestamp, int maximum_people, String location, String description,
                        List<SimpleUser> users_pending, List<SimpleUser> users_accepted, Hobby hobby) {
        this.event_name = event_name;
        this.host_id = host_id;
        this.host_name = host_name;
        this.age_min = age_min;
        this.age_max = age_max;
        this.gender = gender;
        this.timestamp = timestamp;
        this.maximum_people = maximum_people;
        this.location = location;
        this.description = description;
        this.users_pending = users_pending;
        this.users_accepted = users_accepted;
        this.hobby = hobby;
    }

    public String toString(){
        return "Event name: " + event_name + " Host ID " + host_id + " Host name: " + host_name +
                "\nAge Range " + age_min + "-" + age_max + " Gender " + gender + " Timestamp " +
                timestamp + "\n Ammount of People " + maximum_people + " Location + " + location +
                "\n Description " + description + " Users pending: " + users_pending + " Users accepted: +" +
                users_accepted;
    }

    public void confirmUser(SimpleUser user) {
        if (users_pending.contains(user)) {
            users_accepted.add(user);
            users_pending.remove(user);
        }
    }

    /**
     * Add a user to the list of pending users of the event.
     * @param user - user to be added
     */
    public void addUser(SimpleUser user){
        users_pending.add(user);
    }

    /**
     * Check if the User exists in the list of accepted or pending users.
     * @param user - user to be checked
     * @return true if the user exists, false otherwise
     */
    public boolean checkUser(SimpleUser user){
         if(users_pending.contains(user)){
            return true;
        }
        if(users_accepted.contains(user)){
            return true;
        }
        return false;
    }



    public List<SimpleUser> getUsers_pending(){
        return users_pending;
    }

    public List<SimpleUser> getUsers_accepted(){
        return users_accepted;
    }
    public String getEvent_name() {
        return event_name;
    }

    public String getHost_id() {return host_id; }

    public String getHost_name() {
        return host_name;
    }

    public int getAge_min() {
        return age_min;
    }

    public int getAge_max() {
        return age_max;
    }

    public String getGender() {
        return gender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getMaximum_people() {
        return maximum_people;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

}
