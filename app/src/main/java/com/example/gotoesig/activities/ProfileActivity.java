package com.example.gotoesig.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gotoesig.R;
import com.example.gotoesig.utils.BitmapHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialisation Firebase
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Récupérer les vues
        ImageView profileImage = findViewById(R.id.profile_image);
        TextView profileName = findViewById(R.id.profile_name);
        TextView profileFirstname = findViewById(R.id.profile_firstname);
        TextView profilePhone = findViewById(R.id.profile_phone);
        TextView profileCity = findViewById(R.id.profile_city);
        Button btnEditProfile = findViewById(R.id.btn_edit_profile);
        Button btnLogout = findViewById(R.id.btn_logout);
        Button btnDeleteAccount = findViewById(R.id.btn_delete_account);

        if (currentUser != null) {
            String uid = currentUser.getUid();

            // Récupérer les données utilisateur depuis Firestore
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Récupérer les champs
                            String firstName = documentSnapshot.getString("first_name");
                            String lastName = documentSnapshot.getString("last_name");
                            String phone = documentSnapshot.getString("phone");
                            String city = documentSnapshot.getString("city");
                            String profileImageBase64 = documentSnapshot.getString("profile_image");

                            // Mettre à jour les vues avec les valeurs récupérées ou des placeholders
                            profileName.setText(lastName != null ? lastName : "Nom non renseigné");
                            profileFirstname.setText(firstName != null ? firstName : "Prénom non renseigné");
                            profilePhone.setText(phone != null ? phone : "Téléphone non renseigné");
                            profileCity.setText(city != null ? city : "Ville non renseignée");

                            // Charger la photo de profil si elle existe
                            if (profileImageBase64 != null) {
                                BitmapHandler bitmapHandler = new BitmapHandler();
                                profileImage.setImageBitmap(bitmapHandler.decodeBase64ToImage(profileImageBase64));
                            }
                        } else {
                            Toast.makeText(this, "Données utilisateur introuvables", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show();
                        Log.e("ProfileActivity", "Erreur Firestore : ", e);
                    });
        } else {
            Toast.makeText(this, "Aucun utilisateur connecté", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Boutons d'action
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Déconnecté", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            finish();
        });

        btnDeleteAccount.setOnClickListener(v -> {
            if (currentUser != null) {
                currentUser.delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Compte supprimé", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, AuthActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Erreur lors de la suppression du compte", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}
