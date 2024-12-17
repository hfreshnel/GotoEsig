package com.example.gotoesig.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Profile implements Serializable {
    private String name;
    private String translation;

    public Profile() {}

    public Profile(String name, String translation) {
        this.name = name;
        this.translation = translation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    @NonNull
    @Override
    public String toString() {
        return translation;
    }
}
