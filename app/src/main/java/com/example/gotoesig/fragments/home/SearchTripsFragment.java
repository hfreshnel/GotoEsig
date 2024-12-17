package com.example.gotoesig.fragments.home;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.gotoesig.R;
import com.example.gotoesig.fragments.searchTrip.SearchResultsFragment;
import com.example.gotoesig.utils.NominatimAutoCompleteAddress;

import java.util.Calendar;

public class SearchTripsFragment extends Fragment {

    //private EditText inputDeparture, inputDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_trips, container, false);

        //inputDeparture = view.findViewById(R.id.input_departure);
        AutoCompleteTextView inputDeparture = view.findViewById(R.id.input_departure);
        EditText inputDate = view.findViewById(R.id.input_date);
        Button btnSearch = view.findViewById(R.id.btn_search);

        inputDeparture.addTextChangedListener(new NominatimAutoCompleteAddress(inputDeparture, getContext()));

        //Selecteur de date
        inputDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        String date = year + "-" + (month + 1) + "-" + dayOfMonth;
                        inputDate.setText(date);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        // Bouton de recherche
        btnSearch.setOnClickListener(v -> {
            String departure = inputDeparture.getText().toString().trim();
            String date = inputDate.getText().toString().trim();

            if (departure.isEmpty() || date.isEmpty()) {
                Toast.makeText(getContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            // Passer la requête au fragment
            Bundle bundle = new Bundle();
            bundle.putString("departure", departure);
            bundle.putString("date", date);

            Fragment searchResultsFragment = new SearchResultsFragment();
            searchResultsFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, searchResultsFragment)
                    .commit();
        });

        return view;
    }
}