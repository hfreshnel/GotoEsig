package com.example.gotoesig.fragments.addTrip;

import static com.example.gotoesig.utils.Constants.*;

import android.content.pm.PackageManager;
import android.location.*;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.gotoesig.R;
import com.example.gotoesig.adapters.ProfilesAdapter;
import com.example.gotoesig.dao.ProfileDAO;
import com.example.gotoesig.models.Profile;
import com.example.gotoesig.models.Trip;
import com.example.gotoesig.utils.NominatimAutoCompleteAddress;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DepartureFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private String departureCoordinates; // Format : "longitude,latitude"
    private String selectedTransportMode; // Mode de transport sélectionné

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_departure, container, false);

        ProfileDAO profileDAO = new ProfileDAO();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Bundle bundle = new Bundle();
        Trip trip = new Trip();

        // Initialiser les vues
        Spinner transportModeSpinner = view.findViewById(R.id.transport_mode_spinner);
        AutoCompleteTextView inputDeparture = view.findViewById(R.id.input_departure);
        Button btnNext = view.findViewById(R.id.btn_next);

        // Initialiser le client de localisation
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Charger les modes de transport depuis Firestore
        profileDAO.getProfiles(new ProfileDAO.ProfilesCallback() {
            @Override
            public void onSuccess(List<Profile> profiles) {
                ProfilesAdapter adapter = new ProfilesAdapter(requireContext(), profiles);
                transportModeSpinner.setAdapter(adapter);

                // Gérer la sélection du mode de transport
                transportModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Profile selectedProfile = profiles.get(position);
                        trip.setProfile(selectedProfile.getName());
                        //selectedTransportMode =selectedProfile.getName();
                        Log.d("Selected Profile", "Mode : " + selectedProfile.getName());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Erreur lors du chargement des modes de transport", Toast.LENGTH_SHORT).show();
            }
        });


        //Selecte address
        inputDeparture.addTextChangedListener(new NominatimAutoCompleteAddress(inputDeparture, getContext()));

        // Bouton pour passer au fragment suivant
        btnNext.setOnClickListener(v -> {
            String departureAddress = inputDeparture.getText().toString().trim();
            Log.d("TEST", departureAddress);
            if (departureAddress.isEmpty()) {
                Toast.makeText(getContext(), "Veuillez entrer une adresse de départ", Toast.LENGTH_SHORT).show();
                return;
            }
            // Si l'utilisateur a saisi une adresse, on l'utilise pour calculer les coordonnées
            // Convertir l'adresse en coordonnées
            departureCoordinates = getCoordinatesFromAddress(departureAddress);
            if (departureCoordinates == null) {
                Toast.makeText(getContext(), "Impossible de convertir l'adresse en coordonnées", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("Long,Lat", departureCoordinates);
            // Passer au fragment suivant
            trip.setDepartureAddress(departureAddress);
            trip.setDepartureCoordinates(departureCoordinates);
            trip.setArrivalCoordinates(ESIGELEC_COORDINATES.label);
            bundle.putSerializable(STR_TRIP.label, trip);
//            bundle.putString(STR_DEPARTURE_COORDINATES.label, departureCoordinates);
//            bundle.putString(STR_DEPARTURE_ADDRESS.label, departureAddress);
//            bundle.putString(STR_ESIGELEC_COORDINATES.label, ESIGELEC_COORDINATES.label);
//            bundle.putString(STR_PROFILE.label, selectedTransportMode);

            TripDetailsFragment tripDetailsFragment = new TripDetailsFragment();
            tripDetailsFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, tripDetailsFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private String getCoordinatesFromAddress(String address) {
        Log.d("TEST", address);
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                return location.getLongitude() + "," + location.getLatitude();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Gérer les permissions de localisation
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permission accordée", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
