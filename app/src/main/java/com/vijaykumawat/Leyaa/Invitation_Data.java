package com.vijaykumawat.Leyaa;

public class Invitation_Data {

    private String senderName;
    private String roomName;
    private Invitation_Data(){}

    private Invitation_Data(String senderName,String roomName){
        this.senderName=senderName;
        this.roomName = roomName;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getRoomName() {
        return roomName;
    }
}
