package com.example.gotoesig.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gotoesig.R;
import com.example.gotoesig.fragments.addTrip.DepartureFragment;

public class AddTripActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        // Charger le premier fragment (choix de départ)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DepartureFragment())
                    .commit();
        }
    }
}
