package com.example.petsandwalkers;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {
    private String googlePlacesData;
    private GoogleMap mMap;
    private String url;

    @Override
    protected String doInBackground(Object... params) {
        mMap = (GoogleMap) params[0];
        url = (String) params[1];

        DownloadUrl downloadUrl = new DownloadUrl();
        try {
            googlePlacesData = downloadUrl.ReadTheURL(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String result) {
        List<HashMap<String, String>> nearbyPlacesList = null;
        DataParser dataParser = new DataParser();
        nearbyPlacesList = dataParser.parse(result);
        showNearbyPlaces(nearbyPlacesList);
    }

    private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList) {
        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlacesList.get(i);

            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));

            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            markerOptions.title(placeName + " : " + vicinity);
            mMap.addMarker(markerOptions);
        }
    }
}

