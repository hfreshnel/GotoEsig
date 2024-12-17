package com.example.gotoesig.models;

import android.os.Build;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Trip implements Serializable {

    private String id;
    private String arrivalCoordinates;   // Coordonnées GPS de l'arrivée (fixe : ESIGELEC)
    private String date;      // Date du trajet
    private String departureAddress; // Adresse de départ
    private String departureCoordinates; // Coordonnées GPS du départ
    private double distance;
    private double duration;
    private String ownerID;
    private double price;     // Prix par personne
    private String profile;
    private int delayMax;
    private int seatsAvailable; // Nombre de places disponibles
    private String time;      // Heure du trajet

    // Constructeur sans argument (nécessaire pour Firestore)
    public Trip() { }

    // Constructeur complet

    public Trip(String id, String arrivalCoordinates, String date, String departureAddress, String departureCoordinates, double distance, double duration, String ownerID, double price, String profile, int delayMax, int seatsAvailable, String time) {
        this.id = id;
        this.arrivalCoordinates = arrivalCoordinates;
        this.date = date;
        this.departureAddress = departureAddress;
        this.departureCoordinates = departureCoordinates;
        this.distance = distance;
        this.duration = duration;
        this.ownerID = ownerID;
        this.price = price;
        this.profile = profile;
        this.delayMax = delayMax;
        this.seatsAvailable = seatsAvailable;
        this.time = time;
    }


    // Getters et setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTripId() {
        return id;
    }

    public void setTripId(String id) {
        this.id = id;
    }

    public String getArrivalCoordinates() {
        return arrivalCoordinates;
    }

    public void setArrivalCoordinates(String arrivalCoordinates) {
        this.arrivalCoordinates = arrivalCoordinates;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDepartureAddress() {
        return departureAddress;
    }

    public void setDepartureAddress(String departureAddress) {
        this.departureAddress = departureAddress;
    }

    public String getDepartureCoordinates() {
        return departureCoordinates;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public void setDepartureCoordinates(String departureCoordinates) {
        this.departureCoordinates = departureCoordinates;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public int getDelayMax() {
        return delayMax;
    }

    public void setDelayMax(int delayMax) {
        this.delayMax = delayMax;
    }

    public int getSeatsAvailable() {
        return seatsAvailable;
    }

    public void setSeatsAvailable(int seatsAvailable) {
        this.seatsAvailable = seatsAvailable;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Combinez la date et l'heure pour créer un objet LocalDateTime
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime tripDateTime = LocalDateTime.parse(getDate() + " " + getTime(), formatter);

                // Date et heure actuelles
                LocalDateTime now = LocalDateTime.now();

                if (tripDateTime.isBefore(now)) {
                    return "Terminé";
                } else if (tripDateTime.isAfter(now.minusHours(1)) && tripDateTime.isBefore(now.plusHours(1))) {
                    return "En cours";
                } else {
                    return "À venir";
                }
            } else {
                return "À venir";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "À venir"; // Par défaut si erreur
        }
    }
}
