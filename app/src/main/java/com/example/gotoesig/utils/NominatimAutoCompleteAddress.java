package com.example.gotoesig.utils;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.gotoesig.api.NominatimApi;
import com.example.gotoesig.api.RetrofitClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NominatimAutoCompleteAddress implements TextWatcher {
    private final NominatimApi nominatimApi;
    private final AutoCompleteTextView inputDeparture;
    private final android.content.Context context;

    public NominatimAutoCompleteAddress(AutoCompleteTextView inputDeparture, Context context) {
        this.nominatimApi = RetrofitClient.getNominatimApi();
        this.inputDeparture = inputDeparture;
        this.context = context;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 2) { // Rechercher seulement si 3 caractères ou plus
            nominatimApi.searchAddresses(s.toString(), "json", 1, 5)
                    .enqueue(new Callback<JsonArray>() {
                        @Override
                        public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<String> suggestions = new ArrayList<>();
                                JsonArray results = response.body();

                                for (JsonElement element : results) {
                                    String displayName = element.getAsJsonObject().get("display_name").getAsString();
                                    suggestions.add(displayName);
                                }

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        context,
                                        android.R.layout.simple_dropdown_item_1line,
                                        suggestions
                                );
                                inputDeparture.setAdapter(adapter);
                                inputDeparture.showDropDown();
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonArray> call, Throwable t) {
                            Toast.makeText(context, "Erreur lors de la recherche d'adresse", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void afterTextChanged(Editable s) {}
}
