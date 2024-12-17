package com.example.gotoesig.activities;

import static com.example.gotoesig.utils.Constants.STR_VALIDATION_BTN;
import static com.example.gotoesig.utils.CustomParser.*;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gotoesig.R;
import com.example.gotoesig.api.OpenRouteServiceApi;
import com.example.gotoesig.api.RetrofitClient;
import com.example.gotoesig.dao.TripDAO;
import com.example.gotoesig.models.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripDetailsOSMActivity extends AppCompatActivity {

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details_osm);

        // Initialiser la carte
        mapView = findViewById(R.id.osm_map);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        Button btnValidateTrip = findViewById(R.id.btn_validate_trip);

        // Coordonnées de départ et d'arrivée (exemple)
        Trip trip = (Trip) getIntent().getSerializableExtra("trip");
        boolean isValidated = getIntent().getBooleanExtra(STR_VALIDATION_BTN.label, false);


        if (isValidated) {
            btnValidateTrip.setVisibility(View.GONE);
        }

        if (trip == null) {
            finish();
            return;
        }
        if (trip.getDepartureCoordinates() == null || trip.getArrivalCoordinates() == null) {
            finish();
            return;
        }

        // Convertir les coordonnées en GeoPoint
        GeoPoint startPoint = parseGeoPointCoordinates(trip.getDepartureCoordinates());
        GeoPoint endPoint = parseGeoPointCoordinates(trip.getArrivalCoordinates());

        // Centrer la carte sur le point de départ
        mapView.getController().setZoom(12.0);
        mapView.getController().setCenter(startPoint);

        // Ajouter les marqueurs et dessiner l'itinéraire
        //addRouteToMap(startPoint, endPoint);
        fetchAndDrawRoute(startPoint, endPoint, trip.getProfile());

        btnValidateTrip.setOnClickListener(v -> validateTrip(trip));
    }

    private void validateTrip(Trip trip) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // Réduire le nombre de places disponibles
        db.collection("trips").document(trip.getId())
                .update("seatsAvailable", trip.getSeatsAvailable() - 1)
                .addOnSuccessListener(aVoid -> {
                    // Ajouter l'utilisateur à la liste des trajets
                    TripDAO tripDAO = new TripDAO();
                    tripDAO.addUserTripAssociation(userId, trip.getId(), new TripDAO.UserTripCallback() {
                        @Override
                        public void onSuccess() {
                            // Ajouter le prix au solde du créateur si applicable
                            if (trip.getPrice() > 0) {
                                updateCreatorBalance(trip.getOwnerID(), trip.getPrice());
                            }

                            Toast.makeText(TripDetailsOSMActivity.this, "Trajet ajouté avec succès", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(TripDetailsOSMActivity.this, "Erreur lors de l'ajout du trajet à vos trajets", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors de la mise à jour des places disponibles", Toast.LENGTH_SHORT).show();
                });
    }

    private void addRouteToMap(GeoPoint start, GeoPoint end) {
        // Exemple simple : tracer une ligne directe entre les deux points
        List<GeoPoint> routePoints = new ArrayList<>();
        routePoints.add(start);
        routePoints.add(end);

        Polyline line = new Polyline();
        line.setPoints(routePoints);
        line.setColor(Color.BLUE); // Couleur de la ligne
        line.setWidth(5f); // Épaisseur de la ligne

        mapView.getOverlayManager().add(line);
        mapView.invalidate(); // Rafraîchir la carte
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDetach(); // Libérer les ressources de la carte
    }

    private void fetchAndDrawRoute(GeoPoint start, GeoPoint end, String profile) {
        OpenRouteServiceApi api = RetrofitClient.getClient().create(OpenRouteServiceApi.class);

        JsonObject body = new JsonObject();
        body.add("coordinates", new Gson().toJsonTree(new double[][]{
                {start.getLongitude(), start.getLatitude()},
                {end.getLongitude(), end.getLatitude()}
        }));
        body.addProperty("instructions", false);

        Log.d("API_DEBUG", "Profile : " + profile + " Requete : " + new Gson().toJson(body));

        api.getRoute("v2/directions/" + profile, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonArray features = response.body().getAsJsonArray("routes");
                        if (features == null || features.size() == 0) {
                            Log.e("API_ERROR", "Aucun itinéraire trouvé");
                            addRouteToMap(start, end);
                            Toast.makeText(TripDetailsOSMActivity.this, "Aucun itinéraire trouvé", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Décoder la géométrie encodée
                        String encodedGeometry = features.get(0)
                                .getAsJsonObject()
                                .get("geometry")
                                .getAsString();

                        List<GeoPoint> routePoints = decodePolyline(encodedGeometry);

                        // Tracer l'itinéraire sur la carte
                        Polyline line = new Polyline();
                        line.setPoints(routePoints);
                        line.setColor(Color.BLUE);
                        line.setWidth(5f);

                        mapView.getOverlayManager().add(line);
                        mapView.invalidate();
                    } catch (Exception e) {
                        Log.e("API_ERROR", "Erreur lors du traitement de la réponse", e);
                        Toast.makeText(TripDetailsOSMActivity.this, "Erreur dans la réponse", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("API_ERROR", "Réponse invalide : " + response.errorBody());
                    Toast.makeText(TripDetailsOSMActivity.this, "Erreur lors de la requête API", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("API_ERROR", "Échec de la requête", t);
                Toast.makeText(TripDetailsOSMActivity.this, "Erreur lors du calcul de l'itinéraire", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateCreatorBalance(String ownerId, double price) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Récupérer le solde actuel
        db.collection("users").document(ownerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        double currentBalance = documentSnapshot.getDouble("balance") != null ?
                                documentSnapshot.getDouble("balance") : 0.0;

                        // Mettre à jour le solde
                        db.collection("users").document(ownerId)
                                .update("balance", currentBalance + price)
                                .addOnSuccessListener(aVoid -> {
                                    // Solde mis à jour avec succès
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Erreur lors de la mise à jour du solde du créateur", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors de la récupération du solde du créateur", Toast.LENGTH_SHORT).show();
                });
    }
}
