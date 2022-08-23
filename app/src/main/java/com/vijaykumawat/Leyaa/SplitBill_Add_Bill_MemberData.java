package com.vijaykumawat.Leyaa;


public class SplitBill_Add_Bill_MemberData {
    String avatar, fullname;
    private String firebaseId;


    public SplitBill_Add_Bill_MemberData(String avatar, String fullname) {
        this.avatar = avatar;
        this.fullname = fullname;


    }



    public SplitBill_Add_Bill_MemberData(){}

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

}

