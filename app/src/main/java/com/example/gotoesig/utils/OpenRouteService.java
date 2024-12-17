package com.example.gotoesig.utils;

import static com.example.gotoesig.utils.CustomParser.parseCoordinates;

import com.example.gotoesig.api.OpenRouteServiceApi;
import com.example.gotoesig.api.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OpenRouteService {

    public void fetchRouteMatrix(String departureCoordinates, String esigelecCoordinates, String profile, RouteMatrixCallback callback) {
        OpenRouteServiceApi api = RetrofitClient.getClient().create(OpenRouteServiceApi.class);

        // Construire le corps de la requête
        JsonObject body = new JsonObject();
        body.add("locations", new Gson().toJsonTree(new double[][]{
                parseCoordinates(departureCoordinates),
                parseCoordinates(esigelecCoordinates)
        }));
        body.add("metrics", new Gson().toJsonTree(new String[]{"distance", "duration"}));

        // Appeler l'API avec le profil sélectionné
        api.getMatrix("v2/matrix/" + profile, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Extraire les données de la réponse
                        JsonObject matrix = response.body();
                        double distance = matrix.getAsJsonArray("distances")
                                .get(0).getAsJsonArray().get(1).getAsDouble() / 1000; // Convertir en km
                        double duration = matrix.getAsJsonArray("durations")
                                .get(0).getAsJsonArray().get(1).getAsDouble() / 60; // Convertir en minutes

                        // Appeler le callback avec les résultats
                        callback.onSuccess(distance, duration);
                    } catch (Exception e) {
                        callback.onFailure(e);
                    }
                } else {
                    callback.onFailure(new Exception("Réponse invalide ou vide"));
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }
}

