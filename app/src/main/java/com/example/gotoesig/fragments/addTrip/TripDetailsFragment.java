package com.example.gotoesig.fragments.addTrip;

import static com.example.gotoesig.utils.Constants.*;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gotoesig.R;
import com.example.gotoesig.dao.TripDAO;
import com.example.gotoesig.models.Trip;
import com.example.gotoesig.utils.OpenRouteService;
import com.example.gotoesig.utils.RouteMatrixCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class TripDetailsFragment extends Fragment {

    private double distance = 0;
    private double duration = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_details, container, false);

        EditText inputDate = view.findViewById(R.id.input_date);
        EditText inputTime = view.findViewById(R.id.input_time);
        EditText inputDelay = view.findViewById(R.id.input_delay);
        EditText inputSeats = view.findViewById(R.id.input_seats);
        EditText inputPrice = view.findViewById(R.id.input_price);
        Button btnSave = view.findViewById(R.id.btn_save_trip);
        TripDAO tripDAO = new TripDAO();
        Trip trip;

        Bundle bundle = getArguments();
        if (bundle != null) {
            trip = (Trip) bundle.getSerializable(STR_TRIP.label);

            OpenRouteService openRouteService = new OpenRouteService();
            if (trip != null) {
                openRouteService.fetchRouteMatrix(
                        trip.getDepartureCoordinates(),
                        trip.getArrivalCoordinates(),
                        trip.getProfile(),
                        new RouteMatrixCallback() {
                            @Override
                            public void onSuccess(double dist, double dur) {
                                distance = dist;
                                duration = dur;

                                TextView distanceView = view.findViewById(R.id.route_distance);
                                TextView durationView = view.findViewById(R.id.route_duration);

                                distanceView.setText(String.format("Distance : %.2f km", distance));

                                durationView.setText(String.format("Durée : %.2f min", duration));
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                Toast.makeText(getContext(), "Erreur : " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                if (!trip.getProfile().contains("driving")) {
                    inputPrice.setVisibility(View.GONE);
                }
            }
        } else {
            trip = null;
        }

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

        //Selecteur d'heure
        inputTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                    (view1, hourOfDay, minute) -> {
                        String time = String.format("%02d:%02d", hourOfDay, minute);
                        inputTime.setText(time);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true);
            timePickerDialog.show();
        });

        // Enregistrer le trajet
        btnSave.setOnClickListener(v -> {
            String date = inputDate.getText().toString().trim();
            String time = inputTime.getText().toString().trim();
            String delayString = inputDelay.getText().toString().trim();
            String seatsString = inputSeats.getText().toString().trim();
            String priceString = inputPrice.getText().toString().trim();

            if (date.isEmpty() || time.isEmpty() || seatsString.isEmpty()) {
                Toast.makeText(getContext(), "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show();
                return;
            }

            int seats;
            try {
                seats = Integer.parseInt(seatsString);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Nombre de places invalide", Toast.LENGTH_SHORT).show();
                return;
            }

            int delay;
            try {
                delay = Integer.parseInt(delayString);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Delay invalide", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = priceString.isEmpty() ? 0 : Double.parseDouble(priceString);

            if (trip != null && distance > 0) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    trip.setOwnerID(user.getUid());
                    trip.setDate(date);
                    trip.setTime(time);
                    trip.setDelayMax(delay);
                    trip.setSeatsAvailable(seats);
                    trip.setPrice(price);
                    trip.setDistance(distance);
                    trip.setDuration(duration);
                    tripDAO.addTrip(trip, new TripDAO.TripCallback() {
                        @Override
                        public void onSuccess(Trip trip) {
                            Toast.makeText(getContext(), "Trajet enregistré avec succès", Toast.LENGTH_SHORT).show();
                            Log.d("TripDAO", "Trajet ajouté avec succès : " + trip.getId());
                            requireActivity().finish();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(getContext(), "Erreur sur la distance", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
}
