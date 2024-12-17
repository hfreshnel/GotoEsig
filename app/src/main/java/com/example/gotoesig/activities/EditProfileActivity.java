package com.example.gotoesig.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gotoesig.R;
import com.example.gotoesig.utils.BitmapHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    private ImageView profileImage;
    private EditText editFirstName, editLastName, editPhone, editCity;
    private Button btnSave, btnCancel, btnChangeImage;
    private String encodedImage; // Contient l'image encodée en Base64
    private BitmapHandler bitmapHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialisation Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();
        bitmapHandler = new BitmapHandler();

        // Initialisation des vues
        profileImage = findViewById(R.id.edit_profile_image);
        editFirstName = findViewById(R.id.edit_first_name);
        editLastName = findViewById(R.id.edit_last_name);
        editPhone = findViewById(R.id.edit_phone);
        editCity = findViewById(R.id.edit_city);
        btnSave = findViewById(R.id.btn_save_profile);
        btnCancel = findViewById(R.id.btn_cancel);
        btnChangeImage = findViewById(R.id.btn_change_image);

        // Charger les données existantes
        if (user != null) {
            String uid = user.getUid();
            loadUserData(uid);
        } else {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Changer l'image de profil
        btnChangeImage.setOnClickListener(v -> openImagePicker());

        // Enregistrer les modifications
        btnSave.setOnClickListener(v -> saveUserData());

        // Annuler l'édition
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadUserData(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Charger les champs existants
                        String firstName = documentSnapshot.getString("first_name");
                        String lastName = documentSnapshot.getString("last_name");
                        String phone = documentSnapshot.getString("phone");
                        String city = documentSnapshot.getString("city");
                        String profileImageBase64 = documentSnapshot.getString("profile_image");

                        editFirstName.setText(firstName != null ? firstName : "");
                        editLastName.setText(lastName != null ? lastName : "");
                        editPhone.setText(phone != null ? phone : "");
                        editCity.setText(city != null ? city : "");

                        // Charger l'image si elle existe
                        if (profileImageBase64 != null) {
                            Bitmap decodedBitmap = bitmapHandler.decodeBase64ToImage(profileImageBase64);
                            profileImage.setImageBitmap(decodedBitmap);
                            encodedImage = profileImageBase64; // Conserver l'image existante
                        }
                    } else {
                        Toast.makeText(this, "Données utilisateur introuvables", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show();
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
                encodedImage = bitmapHandler.encodeImageToBase64(bitmap); // Encoder l'image
            } catch (IOException e) {
                Toast.makeText(this, "Erreur lors de la sélection de l'image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveUserData() {
        if (user != null) {
            String uid = user.getUid();

            // Récupérer les nouvelles données saisies
            String firstName = editFirstName.getText().toString().trim();
            String lastName = editLastName.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String city = editCity.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || city.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            // Préparer les données pour la sauvegarde
            HashMap<String, Object> updates = new HashMap<>();
            updates.put("first_name", firstName);
            updates.put("last_name", lastName);
            updates.put("phone", phone);
            updates.put("city", city);
            if (encodedImage != null) {
                updates.put("profile_image", encodedImage);
            }

            // Mettre à jour Firestore
            db.collection("users").document(uid).update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erreur lors de la mise à jour du profil", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
