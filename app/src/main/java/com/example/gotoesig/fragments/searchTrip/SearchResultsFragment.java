package com.example.gotoesig.fragments.searchTrip;

import static com.example.gotoesig.utils.Constants.STR_VALIDATION_BTN;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.List;

public class SearchResultsFragment extends Fragment {

    private List<Trip> trips;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_results, container, false);

        RecyclerView resultsRecyclerView = view.findViewById(R.id.results_recycler_view);

        // Initialiser RecyclerView
        trips = new ArrayList<>();
        TripsRecyclerAdapter adapter = new TripsRecyclerAdapter(requireContext(), trips, trip -> {
            // Gestion du clic sur un trajet
            Intent intent = new Intent(getContext(), TripDetailsOSMActivity.class);
            intent.putExtra("trip", trip);
            intent.putExtra(STR_VALIDATION_BTN.label, false);
            startActivity(intent);
        });

        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        resultsRecyclerView.setAdapter(adapter);

        // Charger les trajets via DAO
        Bundle args = getArguments();
        if (args != null) {
            String departure = args.getString("departure");
            String date = args.getString("date");

            if (departure != null && date != null) {
                TripDAO tripDAO = new TripDAO();
                tripDAO.getTripsByDate(date, new TripDAO.TripsCallback() {
                    @Override
                    public void onSuccess(List<Trip> result) {
                        trips.clear();
                        for (Trip trip : result) {
                            if (trip.getDepartureAddress().equals(departure)) {
                                trips.add(trip);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Erreur lors du chargement des trajets", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        return view;
    }

}
