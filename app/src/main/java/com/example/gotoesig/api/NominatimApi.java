package com.example.gotoesig.api;

import com.google.gson.JsonArray;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NominatimApi {
    @GET("search")
    Call<JsonArray> searchAddresses(
            @Query("q") String query,
            @Query("format") String format,
            @Query("addressdetails") int addressDetails,
            @Query("limit") int limit
    );
}
