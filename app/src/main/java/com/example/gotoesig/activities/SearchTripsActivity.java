package com.example.gotoesig.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.gotoesig.R;
import com.example.gotoesig.fragments.searchTrip.SearchResultsFragment;
import com.google.firebase.firestore.FirebaseFirestore;

public class SearchTripsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText inputDeparture, inputDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_trips);

        // Initialisation
        db = FirebaseFirestore.getInstance();
        inputDeparture = findViewById(R.id.input_departure);
        inputDate = findViewById(R.id.input_date);
        Button btnSearch = findViewById(R.id.btn_search);

        // Bouton de recherche
        btnSearch.setOnClickListener(v -> {
            String departure = inputDeparture.getText().toString().trim();
            String date = inputDate.getText().toString().trim();

            if (departure.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            // Passer la requête au fragment
            Bundle bundle = new Bundle();
            bundle.putString("departure", departure);
            bundle.putString("date", date);

            Fragment searchResultsFragment = new SearchResultsFragment();
            searchResultsFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, searchResultsFragment)
                    .commit();
        });
    }
}
