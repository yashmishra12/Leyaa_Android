package com.vijaykumawat.Leyaa;

import java.util.Date;

public class Message_Data {
    private String id;
    private String senderID;
    private String text;
    private Date timestamp;

    public Message_Data(String id, String senderID, String text, Date timestamp) {
        this.id = id;
        this.senderID = senderID;
        this.text = text;
        this.timestamp = timestamp;
    }

    public Message_Data(){}

    public String getId() {
        return id;
    }

    public String getSenderID() {
        return senderID;
    }

    public String getText() {
        return text;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
