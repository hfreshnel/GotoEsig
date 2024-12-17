package com.example.gotoesig.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PhotonApi {
    @GET("api/")
    Call<JsonObject> getSuggestions(
            @Query("q") String query,
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("limit") int limit
    );
}
