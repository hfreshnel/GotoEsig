package com.example.gotoesig.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface OpenRouteServiceApi {

    @Headers({
            "Authorization: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
            "Content-Type: application/json"
    })
    @POST
    Call<JsonObject> getRoute(@Url String url, @Body JsonObject body);

    @Headers({
            "Authorization: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
            "Content-Type: application/json"
    })
    @POST
    Call<JsonObject> getMatrix(@Url String url, @Body JsonObject body);
}
