package com.example.petsandwalkers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class StoreFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private GoogleMap mMap;
    private PlacesClient mPlacesClient;
    private String apiKey;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_store, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            ApplicationInfo app = requireContext().getPackageManager().getApplicationInfo(requireContext().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;
            apiKey = bundle.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("StoreFragment", "Failed to load API key from manifest", e);
            apiKey = ""; // Fallback to an empty string
        }

        // Initialize the Places client.
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), apiKey);
        }
        mPlacesClient = Places.createClient(requireContext());

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // +++++++++++++++++++++++++++++++++++++++++++
        Log.d("StoreFragment", "onMapReady: API Key: " + apiKey);

        mMap.setOnMarkerClickListener(marker -> {
            if (marker.getTag() instanceof Place) {
                showBottomSheetDialog((Place) marker.getTag());
            }
            return false;
        });

    }

    private void enableUserLocation() {
        if (mMap != null) {
            try {
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(Location location) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                        mMap.setOnMyLocationChangeListener(null);

                        showNearbyPetStores(new LatLng(location.getLatitude(), location.getLongitude()));
                    }
                });
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private BitmapDescriptor getMarkerIconWithBackground() {
        View markerView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_marker_layout, null);

        // Measure and layout the custom marker view
        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());

        // Create a bitmap from the custom marker view
        Bitmap bitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerView.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    private void showNearbyPetStores(LatLng currentLatLng) {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                currentLatLng.latitude + "," + currentLatLng.longitude +
                "&radius=5000&type=pet_store&key=" + apiKey;

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject result = results.getJSONObject(i);
                                String placeId = result.getString("place_id");
                                String name = result.getString("name");
                                String address = result.getString("vicinity");
                                JSONObject geometry = result.getJSONObject("geometry");
                                JSONObject location = geometry.getJSONObject("location");
                                double lat = location.getDouble("lat");
                                double lng = location.getDouble("lng");


                                BitmapDescriptor storeIcon = getMarkerIconWithBackground();

                                LatLng latLng = new LatLng(lat, lng);
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(latLng)
                                        .title(name)
                                        .snippet(address)
                                        .icon(storeIcon);

                                Marker marker = mMap.addMarker(markerOptions);

                                // Fetch the Place object using the placeId and set it as the tag of the marker
                                fetchPlace(placeId, new OnFetchPlaceCompleteListener() {
                                    @Override
                                    public void onComplete(Place place) {
                                        marker.setTag(place);
                                    }
                                });

                                Log.d("StoreFragment", "Pet store found: " + name + ", Address: " + address);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("StoreFragment", "Error fetching pet stores: " + error.getMessage());
            }
        });

        queue.add(jsonObjectRequest);
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
            } else {
                Toast.makeText(requireContext(), "Location permission is required to show pet stores", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showBottomSheetDialog(Place place) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_dialog, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Set up views in the bottom sheet with the place information
        TextView placeName = bottomSheetView.findViewById(R.id.place_name);
        TextView placeAddress = bottomSheetView.findViewById(R.id.place_address);
        Button openInMaps = bottomSheetView.findViewById(R.id.open_in_maps);

        placeName.setText(place.getName());
        placeAddress.setText(place.getAddress());

        // Set up the "Open in Maps" button
        openInMaps.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("geo:" + place.getLatLng().latitude + "," + place.getLatLng().longitude + "?q=" + Uri.encode(place.getName()));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        });

        bottomSheetDialog.show();
    }

    private void fetchPlace(String placeId, OnFetchPlaceCompleteListener listener) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        mPlacesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            listener.onComplete(response.getPlace());
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                Log.e("StoreFragment", "Place not found: " + exception.getMessage());
            }
        });
    }

    interface OnFetchPlaceCompleteListener {
        void onComplete(Place place);
    }



}

