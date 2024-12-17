package com.example.gotoesig.fragments.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gotoesig.R;
import com.example.gotoesig.models.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.concurrent.atomic.AtomicInteger;

public class StatisticsFragment extends Fragment {

    private TextView totalTrips, totalDistance, totalTime, userBalance;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        // Initialiser les vues
        totalTrips = view.findViewById(R.id.total_trips);
        totalDistance = view.findViewById(R.id.total_distance);
        totalTime = view.findViewById(R.id.total_time);
        userBalance = view.findViewById(R.id.user_balance);

        // Charger les statistiques
        loadStatistics();

        return view;
    }

    private void loadStatistics() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Récupérer les trajets validés
        db.collection("user_trips")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("Stats", "Trips count : " + queryDocumentSnapshots.size());
                    AtomicInteger totalTripsCount = new AtomicInteger(queryDocumentSnapshots.size());
                    AtomicInteger terminatedTrip = new AtomicInteger();
                    Log.d("Stats", "Trips count : " + totalTripsCount);
                    double[] totals = {0, 0}; // [0] = distance, [1] = durée

                    if (totalTripsCount.get() == 0) {
                        // Si aucun trajet, afficher les valeurs par défaut
                        totalTrips.setText("Trajets validés : 0");
                        totalDistance.setText("Distance totale parcourue : 0 km");
                        totalTime.setText("Temps total : 0 h 0 min");
                        loadUserBalance(userId);
                        return;
                    }

                    // Parcourir chaque trajet validé
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String tripId = document.getString("trip_id");
                        Log.d("Stats", "Trip : " + tripId);
                        if (tripId != null) {
                            // Récupérer les détails du trajet depuis la collection "trips"
                            db.collection("trips").document(tripId)
                                    .get()
                                    .addOnSuccessListener(tripDoc -> {
                                        if (tripDoc.exists()) {
                                            Trip trip = tripDoc.toObject(Trip.class);
                                            Log.d("Stats", "Trips dist : " + trip.getDistance());
                                            Log.d("Stats", "Trips dist : " + trip.getDuration());

                                            if (trip.getStatus() == "Terminé"){
                                                // Ajouter la distance et la durée aux totaux
                                                if (trip != null) {
                                                    terminatedTrip.getAndIncrement();
                                                    totals[0] += trip.getDistance();
                                                    totals[1] += trip.getDuration();
                                                }
                                            }

                                            // Une fois toutes les données chargées, afficher les statistiques
                                            if (totalTripsCount.decrementAndGet() == 0) {
                                                updateUI(totals[0], totals[1], terminatedTrip);
                                                loadUserBalance(userId);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Erreur lors de la récupération des trajets", Toast.LENGTH_SHORT).show());
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Erreur lors du chargement des statistiques", Toast.LENGTH_SHORT).show());
    }

    private void updateUI(double totalDistanceValue, double totalDurationValue, AtomicInteger totalTripsCount) {
        totalTrips.setText("Trajets validés : " + totalTripsCount);
        totalDistance.setText("Distance totale parcourue : " + String.format("%.2f km", totalDistanceValue));
        totalTime.setText("Temps total : " + String.format("%.2f min", totalDurationValue));
    }

    private void loadUserBalance(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        double balance = documentSnapshot.getDouble("balance") != null ?
                                documentSnapshot.getDouble("balance") : 0.0;
                        userBalance.setText("Solde : " + String.format("%.2f €", balance));
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Erreur lors du chargement du solde utilisateur", Toast.LENGTH_SHORT).show());
    }
}
