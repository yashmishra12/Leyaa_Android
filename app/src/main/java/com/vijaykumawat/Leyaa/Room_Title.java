package com.vijaykumawat.Leyaa;

import androidx.annotation.Keep;

@Keep
public class Room_Title {
    private String title;

    public Room_Title() {}

    public Room_Title(String title) {
        this.title = title;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
