package com.example.gotoesig.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gotoesig.R;
import com.example.gotoesig.dao.UserDAO;
import com.example.gotoesig.models.Trip;
import com.example.gotoesig.models.User;
import com.example.gotoesig.utils.BitmapHandler;

import java.util.List;

public class TripsRecyclerAdapter extends RecyclerView.Adapter<TripsRecyclerAdapter.TripViewHolder> {

    private final List<Trip> trips;
    private final Context context;
    private final OnTripClickListener onTripClickListener;
    private final UserDAO userDAO;

    public interface OnTripClickListener {
        void onTripClick(Trip trip);
    }

    public TripsRecyclerAdapter(Context context, List<Trip> trips, OnTripClickListener onTripClickListener) {
        this.context = context;
        this.trips = trips;
        this.onTripClickListener = onTripClickListener;
        this.userDAO = new UserDAO();
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = trips.get(position);

        holder.tripDeparture.setText(trip.getDepartureAddress());
        holder.tripDate.setText("Date : " + trip.getDate() + ", " + trip.getTime());

        // Calcul de l'état
        String status = trip.getStatus();
        holder.tripStatus.setVisibility(View.VISIBLE); // Affiche la pastille
        holder.tripStatus.setText(status);

        int statusColor;
        switch (status) {
            case "Terminé":
                statusColor = ContextCompat.getColor(context, R.color.red);
                break;
            case "En cours":
                statusColor = ContextCompat.getColor(context, R.color.green);
                break;
            case "À venir":
            default:
                statusColor = ContextCompat.getColor(context, R.color.blue);
                break;
        }
        holder.tripStatus.setBackgroundTintList(ColorStateList.valueOf(statusColor));

        if (trip.getPrice() > 0) {
            holder.tripPrice.setVisibility(View.VISIBLE);
            holder.tripPrice.setText("Prix : " + trip.getPrice() + " €");
        } else {
            holder.tripPrice.setVisibility(View.GONE);
        }

        userDAO.getUserById(trip.getOwnerID(), new UserDAO.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user.getDisplay_name() != null) {
                    holder.tripCreatorName.setText("Créé par : " + user.getDisplay_name());
                } else {
                    holder.tripCreatorName.setText("Créé par : " + user.getFirst_name() + " " + user.getLast_name());
                }

                if (user.getProfile_image() != null) {
                    BitmapHandler bitmapHandler = new BitmapHandler();
                    holder.tripCreatorPhoto.setImageBitmap(bitmapHandler.decodeBase64ToImage(user.getProfile_image()));
                } else {
                    holder.tripCreatorPhoto.setImageResource(R.drawable.icon_profil);
                }

            }

            @Override
            public void onFailure(Exception e) {

            }
        });

        holder.itemView.setOnClickListener(v -> onTripClickListener.onTripClick(trip));
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public static class TripViewHolder extends RecyclerView.ViewHolder {

        ImageView tripCreatorPhoto;
        TextView tripCreatorName, tripDeparture, tripDate, tripPrice, tripStatus;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tripCreatorPhoto = itemView.findViewById(R.id.trip_creator_photo);
            tripCreatorName = itemView.findViewById(R.id.trip_creator_name);
            tripDeparture = itemView.findViewById(R.id.trip_departure);
            tripDate = itemView.findViewById(R.id.trip_date);
            tripPrice = itemView.findViewById(R.id.trip_price);
            tripStatus = itemView.findViewById(R.id.trip_status);
        }
    }
}
