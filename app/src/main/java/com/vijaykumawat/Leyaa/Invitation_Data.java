package com.vijaykumawat.Leyaa;

public class Invitation_Data {

    private String senderName;
    private String roomName;
    private String documentId;

    private Invitation_Data(){}

    private Invitation_Data(String senderName,String roomName){
        this.senderName=senderName;
        this.roomName = roomName;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getRoomName() {
        return roomName;
    }
}
