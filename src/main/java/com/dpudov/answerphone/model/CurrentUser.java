package com.dpudov.answerphone.model;

/**
 * Created by DPudov on 05.07.2016.
 */
public class CurrentUser {
    private int id;
    private String first_name;
    private String last_name;
    private String photo_50;

    public CurrentUser(int id, String first_name, String last_name, String photo_50) {
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.photo_50 = photo_50;
    }

    public int getId() {
        return id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getPhoto_50() {
        return photo_50;
    }
}
