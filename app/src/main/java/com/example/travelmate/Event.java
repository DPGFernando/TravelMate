package com.example.travelmate;

import java.io.Serializable;

public class Event implements Serializable {
    private String documentId;
    private String eventName;
    private String date;
    private String startsAt;
    private String endsAt;
    private String photoimg;
    private String venue;
    private String contact;
    private String entranceFee;

    public Event() {
    }

    // Constructor including documentId
    public Event(String documentId, String photoimg, String eventName, String date, String startsAt, String endsAt,  String entranceFee, String venue, String contact) {
        this.documentId = documentId;
        this.photoimg = photoimg;
        this.eventName = eventName;
        this.date = date;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.venue = venue;
        this.contact = contact;
        this.entranceFee = entranceFee;

    }

    // Getters and setters
    public String geteventName() { return eventName; }

    public String getDate() { return date; }
    public String getStartsAt() { return startsAt; }
    public String getEndsAt() { return endsAt; }
    public String getPhotoimg() { return photoimg; }
    public String getVenue() { return venue; }

    public String getContact() { return contact; }
    public String getEntranceFee() { return entranceFee; }
    public String getDocumentId() { return documentId; }

    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void seteventName(String eventName) { this.eventName = eventName; }

    public void setDate(String date) { this.date = date; }
    public void setStartsAt(String startsAt) { this.startsAt = startsAt; }
    public void setEndsAt(String endsAt) { this.startsAt = endsAt; }
    public void setPhotoimg(String photoimg) { this.photoimg = photoimg; }
    public void setVenue(String venue) { this.venue = venue; }

    public void setContact(String contact) { this.contact = contact; }
    public void setEntranceFee(String entranceFee) { this.entranceFee = entranceFee; }
}
