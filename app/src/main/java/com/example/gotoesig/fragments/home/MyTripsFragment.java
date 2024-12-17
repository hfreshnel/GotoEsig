package com.example.gotoesig.fragments.home;

import static com.example.gotoesig.utils.Constants.STR_VALIDATION_BTN;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gotoesig.R;
import com.example.gotoesig.activities.TripDetailsOSMActivity;
import com.example.gotoesig.adapters.TripsRecyclerAdapter;
import com.example.gotoesig.dao.TripDAO;
import com.example.gotoesig.models.Trip;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MyTripsFragment extends Fragment {

    private RecyclerView myTripsList;
    private TextView emptyMessage;
    private TripsRecyclerAdapter adapter;
    private List<Trip> trips;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_trips, container, false);

        // Initialisation des vues
        myTripsList = view.findViewById(R.id.my_trips_list);
        emptyMessage = view.findViewById(R.id.empty_message);

        // Initialiser RecyclerView et l'adaptateur
        trips = new ArrayList<>();
        adapter = new TripsRecyclerAdapter(requireContext(), trips, trip -> {
            // Gestion du clic sur un trajet
            Intent intent = new Intent(getContext(), TripDetailsOSMActivity.class);
            intent.putExtra("trip", trip);
            intent.putExtra(STR_VALIDATION_BTN.label, true);
            startActivity(intent);
        });

        myTripsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        myTripsList.setAdapter(adapter);

        // Charger les trajets depuis la DAO
        loadTrips();

        return view;
    }

    private void loadTrips() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        TripDAO tripDAO = new TripDAO();
        tripDAO.getTripsByUserId(userId, new TripDAO.TripsCallback() {
            @Override
            public void onSuccess(List<Trip> result) {
                if (result.isEmpty()) {
                    emptyMessage.setVisibility(View.VISIBLE);
                    myTripsList.setVisibility(View.GONE);
                } else {
                    emptyMessage.setVisibility(View.GONE);
                    myTripsList.setVisibility(View.VISIBLE);

                    trips.clear();
                    trips.addAll(result);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Erreur lors du chargement des trajets", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
