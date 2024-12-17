package com.example.gotoesig.utils;

public enum Constants {
    STR_TRIP("trip"),
    ESIGELEC_COORDINATES("1.07756,49.38349"),
    STR_VALIDATION_BTN("isValidated");

    public final String label;

   private Constants(String label) {
       this.label = label;
    }
}