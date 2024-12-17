package com.example.gotoesig.activities;

import android.graphics.Bitmap;
import android.os.Bundle;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.gotoesig.R;
import com.example.gotoesig.fragments.home.MyTripsFragment;
import com.example.gotoesig.fragments.home.SearchTripsFragment;
import com.example.gotoesig.fragments.home.StatisticsFragment;
import com.example.gotoesig.utils.BitmapHandler;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private TextView userName;
    private TextView userFirstName;
    private ImageView profileIcon;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private BitmapHandler bitmapHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        bitmapHandler = new BitmapHandler();

        // Initialisation des vues
        userName = findViewById(R.id.user_name);
        userFirstName = findViewById(R.id.user_firstname);
        profileIcon = findViewById(R.id.profile_icon);

        // Récupérer l'utilisateur connecté
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            fetchUserDataFromFirestore(uid);
        } else {
            Toast.makeText(this, "Aucun utilisateur connecté", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Ajouter des listeners pour ouvrir ProfileActivity
        View.OnClickListener openProfileListener = v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        };

        profileIcon.setOnClickListener(openProfileListener);
        userName.setOnClickListener(openProfileListener);
        userFirstName.setOnClickListener(openProfileListener);

        // Initialisation de la Bottom Navigation Bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Charger le fragment par défaut (ex. Mes Trajets)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SearchTripsFragment())
                    .commit();
        }

        // Gestion des clics sur la Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            Intent intent = null;

            switch (item.getItemId()) {
                case R.id.nav_search:
                    //intent = new Intent(this, SearchTripsActivity.class);
                    selectedFragment = new SearchTripsFragment();
                    break;
                case R.id.nav_my_trips:
                    selectedFragment = new MyTripsFragment();
                    break;
                case R.id.nav_add:
                    intent = new Intent(this, AddTripActivity.class);
                    break;
                case R.id.nav_statistics:
                    selectedFragment = new StatisticsFragment();
                    break;
                case R.id.nav_quit:
                    finish(); // Fermer l'application
                    return true;
            }

            if (intent != null) {
                startActivity(intent);
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });
    }

    private void fetchUserDataFromFirestore(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Récupérer les données utilisateur
                        String firstName = documentSnapshot.getString("first_name");
                        String lastName = documentSnapshot.getString("last_name");
                        String profileImageBase64 = documentSnapshot.getString("profile_image");

                        // Mettre à jour les vues
                        userName.setText(lastName != null ? lastName : "Utilisateur");
                        userFirstName.setText(firstName != null ? firstName : "Inconnu");

                        if (profileImageBase64 != null) {
                            Bitmap decodedBitmap = bitmapHandler.decodeBase64ToImage(profileImageBase64);
                            profileIcon.setImageBitmap(decodedBitmap);
                        }
                    } else {
                        Log.d("TAG", "Document utilisateur introuvable");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors du chargement des données utilisateur", Toast.LENGTH_SHORT).show();
                    Log.e("TAG", "Erreur Firestore : ", e);
                });
    }
}
