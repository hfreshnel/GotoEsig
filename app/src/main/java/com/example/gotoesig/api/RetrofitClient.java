package com.example.gotoesig.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://api.openrouteservice.org/";
    private static final String NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/";
    private static final String PHOTON_BASE_URL = "https://photon.komoot.io/";

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static NominatimApi getNominatimApi() {
        return new Retrofit.Builder()
                .baseUrl(NOMINATIM_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NominatimApi.class);
    }

    public static PhotonApi getPhotonApi() {
        return new Retrofit.Builder()
                .baseUrl(PHOTON_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PhotonApi.class);
    }
}
