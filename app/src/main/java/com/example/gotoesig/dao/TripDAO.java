package com.example.gotoesig.dao;

import com.example.gotoesig.models.Trip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TripDAO {

    private static final String COLLECTION_NAME = "trips";
    private final FirebaseFirestore db;

    public TripDAO() {
        db = FirebaseFirestore.getInstance();
    }

    // Ajouter un nouveau trajet
    public void addTrip(Trip trip, TripCallback callback) {
        db.collection(COLLECTION_NAME)
                .add(trip)
                .addOnSuccessListener(documentReference -> {
                    String tripId = documentReference.getId();

                    // Ajouter l'ID au trip et mettre à jour dans la base
                    documentReference.update("id", tripId)
                            .addOnSuccessListener(aVoid -> {
                                // Associer le trajet à l'utilisateur dans user_trips
                                addUserTripAssociation(trip.getOwnerID(), tripId, new UserTripCallback() {
                                    @Override
                                    public void onSuccess() {
                                        callback.onSuccess(trip);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        callback.onFailure(e);
                                    }
                                });
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void addUserTripAssociation(String userId, String tripId, UserTripCallback callback) {
        HashMap<String, Object> userTripData = new HashMap<>();
        userTripData.put("user_id", userId);
        userTripData.put("trip_id", tripId);

        db.collection("user_trips")
                .add(userTripData)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    // Récupérer un trajet par son ID
    public void getTripById(String tripId, TripCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(tripId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Trip trip = documentSnapshot.toObject(Trip.class);
                        callback.onSuccess(trip);
                    } else {
                        callback.onFailure(new Exception("Trip not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    // Récupérer les trajets par date
    public void getTripsByDate(String date, TripsCallback callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Trip> trips = new ArrayList<>();

                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Trip trip = documentSnapshot.toObject(Trip.class);
                        trips.add(trip);
                    }
                    callback.onSuccess(trips);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getTripsByUserId(String userId, TripsCallback callback) {
        db.collection("user_trips")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Trip> trips = new ArrayList<>();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String tripId = document.getString("trip_id");
                        // Charger les détails du trajet via son ID
                        db.collection("trips").document(tripId)
                                .get()
                                .addOnSuccessListener(tripDoc -> {
                                    if (tripDoc.exists()) {
                                        Trip trip = tripDoc.toObject(Trip.class);
                                        trips.add(trip);
                                        callback.onSuccess(trips); // Retourne les trajets dès qu'ils sont chargés
                                    }
                                })
                                .addOnFailureListener(callback::onFailure);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }


    // Callbacks pour les résultats des requêtes
    public interface TripCallback {
        void onSuccess(Trip trip);
        void onFailure(Exception e);
    }

    public interface TripsCallback {
        void onSuccess(List<Trip> trips);
        void onFailure(Exception e);
    }

    public interface UserTripCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}
