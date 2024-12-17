package com.example.gotoesig.utils;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.gotoesig.api.PhotonApi;
import com.example.gotoesig.api.RetrofitClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotonAutoCompleteAddress implements TextWatcher {

    private final PhotonApi photonApi;
    private final AutoCompleteTextView inputDeparture;
    private final android.content.Context context;

    public PhotonAutoCompleteAddress(AutoCompleteTextView inputDeparture, Context context) {
        this.photonApi = RetrofitClient.getPhotonApi();
        this.inputDeparture = inputDeparture;
        this.context = context;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 2) {
            photonApi.getSuggestions(s.toString(), 49.3729, 1.0922, 5)
                    .enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Log.d("PHOTON_RESPONSE", response.body().toString());

                                List<String> suggestions = new ArrayList<>();
                                JsonArray features = response.body().getAsJsonArray("features");

                                if (features != null) {
                                    for (JsonElement element : features) {
                                        try {
                                            JsonObject properties = element.getAsJsonObject().getAsJsonObject("properties");
                                            if (properties != null && properties.has("name") && !properties.get("name").isJsonNull()) {
                                                suggestions.add(properties.get("name").getAsString());
                                            } else {
                                                Log.e("PHOTON_ERROR", "Champ 'name' absent ou null dans : " + properties);
                                            }
                                        } catch (Exception e) {
                                            Log.e("PHOTON_ERROR", "Erreur lors du traitement d'un élément : ", e);
                                        }
                                    }
                                }

                                if (!suggestions.isEmpty()) {
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                            context,
                                            android.R.layout.simple_dropdown_item_1line,
                                            suggestions);
                                    inputDeparture.setAdapter(adapter);
                                    inputDeparture.showDropDown();
                                } else {
                                    Toast.makeText(context, "Aucune suggestion trouvée", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("PHOTON_ERROR", "Réponse invalide ou vide : " + response.errorBody());
                            }
                        }


                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(context, "Erreur de connexion à Photon", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void afterTextChanged(Editable s) {}
}
