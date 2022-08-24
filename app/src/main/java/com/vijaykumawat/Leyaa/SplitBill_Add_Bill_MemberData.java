package com.vijaykumawat.Leyaa;


public class SplitBill_Add_Bill_MemberData {
    String avatar;
    String fullname;

    public void setUid(String uid) {
        this.uid = uid;
    }

    String uid;


    public SplitBill_Add_Bill_MemberData(String avatar, String fullname, String uid) {
        this.avatar = avatar;
        this.fullname = fullname;
        this.uid = uid;

    }



    public SplitBill_Add_Bill_MemberData(){}

    public String getAvatar() {
        return avatar;
    }

    public String getUid() {
        return uid;
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

}

