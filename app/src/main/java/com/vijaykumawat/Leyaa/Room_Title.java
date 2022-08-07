package com.vijaykumawat.Leyaa;

public class Room_Title {

    private String title;
    private Room_Title(){}

    private Room_Title(String title){
        this.title=title;
    }

    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title=title;

    }
}
