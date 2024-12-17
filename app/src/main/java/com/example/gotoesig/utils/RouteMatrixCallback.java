package com.example.gotoesig.utils;

public interface RouteMatrixCallback {
    void onSuccess(double distance, double duration);
    void onFailure(Throwable t);
}

