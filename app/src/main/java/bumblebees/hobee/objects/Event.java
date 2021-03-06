package bumblebees.hobee.objects;

import java.util.Calendar;
import java.util.UUID;


public class Event {
    private UUID eventID;
    private String type;
    private String timestamp;
    private EventDetails event_details;
    private String location;

    public Event(){

    }

    public Event(UUID eventID, String type, String timestamp, EventDetails event_details, String location) {
        this.eventID = eventID;
        this.type = type;
        this.timestamp = timestamp;
        this.event_details = event_details;
        this.location = location;
    }

    public String getType(){
        return type;
    }

    public String getTimestamp(){
        return timestamp;
    }

    public EventDetails getEvent_details() {
        return event_details;
    }

    public UUID getEventID() {
        return eventID;
    }

    public String toString(){
        return "Event ID " + eventID.toString() + " Event Type " + type + " Event timestamp " +
                timestamp + event_details;
    }

    /**
     * Retrieves the topic where the event is posted, including the "geo" prefix and the "event/hobby/" prefix.
     * @return topic string
     */
    public String getTopic(){
        return "geo/"+location+"/event/hobby/"+type+"/"+eventID;
    }

    /**
     * Retrieves the topic where the event will be found when subscribing with a wildcard (#) instead of the ID.
     * @return topic string
     */
    public String getSubscribeTopic(){
        return "geo/"+location+"/event/hobby/"+type+"/#";
    }

    //equals method overwritten for use when adding and removing events from ArrayLists
    //an event is considered equal with another when its ID, type and timestamp match
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (eventID != null ? !eventID.equals(event.eventID) : event.eventID != null) return false;
        if (type != null ? !type.equals(event.type) : event.type != null) return false;
        return timestamp != null ? timestamp.equals(event.timestamp) : event.timestamp == null;

    }

    @Override
    public int hashCode() {
        int result = eventID != null ? eventID.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        return result;
    }





    /**
     * Checks if the event is still active: the time when the event takes place has not passed yet.
     * @return true - event is active
     *          false - event is not active, event has already happened
     */
    public boolean isEventActive(){
        long currentTime = Calendar.getInstance().getTimeInMillis() / 1000L;
        return currentTime < Long.parseLong(event_details.getTimestamp());
    }

    /**
     * Check if the event is full (the number of accepted people is equal to the number of slots).
     * @return true - event is full
     *          false - event is not full
     */
    public boolean isFull(){
        return event_details.getUsers_accepted().size() == event_details.getMaximum_people();
    }


}



