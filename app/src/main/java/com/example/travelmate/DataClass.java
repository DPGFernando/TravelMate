package com.example.travelmate;

public class DataClass {

    private String imageURL, caption, documentId;

    public DataClass(){

    }

    public DataClass(String imageURL, String caption, String documentId) {
        this.imageURL = imageURL;
        this.caption = caption;
        this.documentId = documentId;

    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }


    public String getDocumentId() {
        return documentId;
    }
}
