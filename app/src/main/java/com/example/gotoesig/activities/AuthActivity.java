package com.example.gotoesig.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.example.gotoesig.R;
import com.example.gotoesig.fragments.auth.LoginFragment;

public class AuthActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Charger LoginFragment au démarrage
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        }
    }
}
