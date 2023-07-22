package com.example.petsandwalkers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PetsWalkerFragment extends Fragment {

    private RecyclerView recyclerView;
    private PetsWalkerAdapter adapter;
    private List<PetWalker> petWalkerList;
    private DBOpenHelper dbOpenHelper;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location userCurrentLocation;

    public PetsWalkerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        getLastKnownLocation();
    }

    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
        } else {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        userCurrentLocation = location;
                        adapter.setUserCurrentLocation(location);

                        sortPetWalkersByDistance();
                    }
                }
            });
        }
    }

    private void sortPetWalkersByDistance() {
        if (userCurrentLocation == null) {
            return;
        }

        Collections.sort(petWalkerList, new Comparator<PetWalker>() {
            @Override
            public int compare(PetWalker petWalker1, PetWalker petWalker2) {
                float[] result1 = new float[1];
                float[] result2 = new float[1];

                Location.distanceBetween(userCurrentLocation.getLatitude(), userCurrentLocation.getLongitude(), petWalker1.getLatitude(), petWalker1.getLongitude(), result1);
                Location.distanceBetween(userCurrentLocation.getLatitude(), userCurrentLocation.getLongitude(), petWalker2.getLatitude(), petWalker2.getLongitude(), result2);

                return Float.compare(result1[0], result2[0]);
            }
        });

        adapter.updateData(petWalkerList);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pets_walker, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        dbOpenHelper = new DBOpenHelper(getActivity());
        petWalkerList = dbOpenHelper.getAllPetWalkers();

        adapter = new PetsWalkerAdapter(getActivity(), petWalkerList);
        recyclerView.setAdapter(adapter);

        Spinner spinnerFilter = view.findViewById(R.id.spinner_filter);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.filter_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filterOption = parent.getItemAtPosition(position).toString();
                filterResults(filterOption);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Pets information is displayed by default.
        filterResults("Pets");

        return view;
    }

    private void filterResults(String filterOption) {
        if (filterOption.equals("Find a Pet")) {
            petWalkerList = dbOpenHelper.getUsersByType("Pet Owner");
        } else if (filterOption.equals("Find a Walker")) {
            petWalkerList = dbOpenHelper.getUsersByType("Pet Walker");
        }

        sortPetWalkersByDistance();
        adapter.updateData(petWalkerList);
    }
}