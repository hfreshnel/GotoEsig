package com.example.gotoesig.utils;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class CustomParser {

    // Méthode pour parser une chaîne de coordonnées
    public static double[] parseCoordinates(String coordinates) {
        String[] parts = coordinates.split(",");
        return new double[]{Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
    }

    public static GeoPoint parseGeoPointCoordinates(String coordinates) {
        String[] parts = coordinates.split(",");
        return new GeoPoint(Double.parseDouble(parts[1]), Double.parseDouble(parts[0]));
    }

    public static List<GeoPoint> decodePolyline(String encoded) {
        List<GeoPoint> polyline = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            polyline.add(new GeoPoint(lat / 1E5, lng / 1E5));
        }

        return polyline;
    }
}
