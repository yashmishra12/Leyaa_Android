package com.vijaykumawat.Leyaa;

public class Invitation_Data {

    private String senderName;
    private String roomName;
    private String roomID;
    private String message;
    private String receiverEmail;
    private int pos;

    public Invitation_Data(){}

    public Invitation_Data(int pos,String roomID){
        this.pos =pos;
        this.roomID =roomID;

    }

    public int getPos() {
        return pos;
    }

    private Invitation_Data(String senderName, String roomName,String receiverEmail,String message){
        this.senderName=senderName;
        this.roomName = roomName;
        this.message=message;

    }

    public String getMessage() {
        return message;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public String getRoomID() {
        return roomID;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getRoomName() {
        return roomName;
    }
}