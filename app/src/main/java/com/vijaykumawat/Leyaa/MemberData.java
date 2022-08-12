package com.vijaykumawat.Leyaa;

public class MemberData {
    String avatar, fullname, email;


    public MemberData(String avatar, String fullname, String email) {
        this.avatar = avatar;
        this.fullname = fullname;
        this.email = email;
    }

    public MemberData(){}

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
