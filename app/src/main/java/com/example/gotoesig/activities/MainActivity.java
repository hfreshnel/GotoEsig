package com.example.gotoesig.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.osmdroid.config.Configuration;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Installe le Splash Screen si Android 12 ou supérieur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SplashScreen.installSplashScreen(this);
        }

        EdgeToEdge.enable(this);

        // Configurer le répertoire de cache des tuiles
        Configuration.getInstance().setUserAgentValue(getPackageName());

        // Vérifie si un utilisateur est déjà connecté
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent;
        if (currentUser != null) {
            // Utilisateur authentifié : diriger vers HomeActivity
            intent = new Intent(this, HomeActivity.class);
        } else {
            // Aucun utilisateur : diriger vers AuthActivity
            intent = new Intent(this, AuthActivity.class);
        }
        startActivity(intent);

        // Termine MainActivity pour éviter de revenir dessus avec le bouton retour
        finish();
    }
}
