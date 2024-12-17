package com.example.gotoesig.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gotoesig.models.Profile;

import java.util.List;

public class ProfilesAdapter extends ArrayAdapter<Profile> {

    public ProfilesAdapter(@NonNull Context context, @NonNull List<Profile> profiles) {
        super(context, android.R.layout.simple_spinner_item, profiles);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Récupérer l'objet Profile
        Profile profile = getItem(position);

        // Créer ou réutiliser une vue
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        // Configurer le texte avec la traduction
        TextView textView = (TextView) convertView;
        if (profile != null) {
            textView.setText(profile.getTranslation());
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Même logique pour les éléments de la liste déroulante
        return getView(position, convertView, parent);
    }
}

