package com.vijaykumawat.Leyaa;



public class SplitMemberData {
    String avatar, fullname, uid;
    private String firebaseId;


    public SplitMemberData(String avatar, String fullname, String uid) {
        this.avatar = avatar;
        this.fullname = fullname;
        this.uid = uid;

    }

    public String getUid() {
        return uid;
    }

    public SplitMemberData(){}

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }
}
