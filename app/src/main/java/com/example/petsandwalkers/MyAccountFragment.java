package com.example.petsandwalkers;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.net.ParseException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class MyAccountFragment extends Fragment {

    private Spinner identitySpinner;
    private EditText serviceTimeRangeEditText;
    private EditText serviceLocationEditText;
    private EditText priceEditText;
    private EditText phoneNumberEditText;
    private EditText emailAddressEditText;
    private EditText additionalInfoEditText;
    private Button saveButton;
    private DBOpenHelper DB;
    private String username;
    private TimePicker serviceTimeRangeFrom;
    private TimePicker serviceTimeRangeTo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_account, container, false);

        // Hide the back arrow
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        DB = new DBOpenHelper(requireContext());

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username", null);

        // Initialize views
        identitySpinner = rootView.findViewById(R.id.identity_spinner);
        serviceTimeRangeEditText = rootView.findViewById(R.id.service_time_range_edit_text);
        serviceLocationEditText = rootView.findViewById(R.id.service_location_edit_text);
        priceEditText = rootView.findViewById(R.id.price_edit_text);
        phoneNumberEditText = rootView.findViewById(R.id.phone_number_edit_text);
        emailAddressEditText = rootView.findViewById(R.id.email_address_edit_text);
        additionalInfoEditText = rootView.findViewById(R.id.additional_info_edit_text);
        saveButton = rootView.findViewById(R.id.save_button);

        serviceTimeRangeFrom = rootView.findViewById(R.id.service_time_range_from);
        serviceTimeRangeTo = rootView.findViewById(R.id.service_time_range_to);

        Button logoutButton = rootView.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> logout());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.identity_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        identitySpinner.setAdapter(adapter);

        if (username != null) {
            loadAccountInfo(username);
        }

        saveButton.setOnClickListener(v -> saveAccountInfo());

        return rootView;
    }

    private void logout() {
        // Clear user information in SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Navigate to the AccountFragment
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.accountFragment);
    }

    private void loadAccountInfo(String username) {
        Cursor cursor = DB.getAccountInfo(username);

        if (cursor != null && cursor.moveToFirst()) {
            int identityIndex = cursor.getColumnIndex("identity");
            int serviceTimeRangeIndex = cursor.getColumnIndex("service_time_range");
            int serviceLocationIndex = cursor.getColumnIndex("service_location");
            int priceIndex = cursor.getColumnIndex("price");
            int phoneNumberIndex = cursor.getColumnIndex("phone_number");
            int emailAddressIndex = cursor.getColumnIndex("email_address");
            int additionalInfoIndex = cursor.getColumnIndex("additional_info");

            if (identityIndex != -1 && serviceTimeRangeIndex != -1 && serviceLocationIndex != -1 &&
                    phoneNumberIndex != -1 && emailAddressIndex != -1 && additionalInfoIndex != -1) {
                String identity = cursor.getString(identityIndex);
                String serviceTimeRange = cursor.getString(serviceTimeRangeIndex);
                String serviceLocation = cursor.getString(serviceLocationIndex);
                String phoneNumber = cursor.getString(phoneNumberIndex);
                String emailAddress = cursor.getString(emailAddressIndex);
                String additionalInfo = cursor.getString(additionalInfoIndex);

                // Set values in the UI
                int identityArrayIndex = 0;
                String[] identityArray = getResources().getStringArray(R.array.identity_array);
                for (int i = 0; i < identityArray.length; i++) {
                    if (identityArray[i].equals(identity)) {
                        identityArrayIndex = i;
                        break;
                    }
                }

                identitySpinner.setSelection(identityArrayIndex);
                serviceTimeRangeEditText.setText(serviceTimeRange);
                serviceLocationEditText.setText(serviceLocation);
                phoneNumberEditText.setText(phoneNumber);
                emailAddressEditText.setText(emailAddress);
                additionalInfoEditText.setText(additionalInfo);
            }
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private void saveAccountInfo() {
        // Collect form data
        String identity = identitySpinner.getSelectedItem().toString();
        String serviceTimeRange = serviceTimeRangeEditText.getText().toString();
        String serviceLocation = serviceLocationEditText.getText().toString();
        String price = priceEditText.getText().toString();
        double latitude = 0;
        double longitude = 0;

        int fromHour = serviceTimeRangeFrom.getCurrentHour();
        int fromMinute = serviceTimeRangeFrom.getCurrentMinute();
        int toHour = serviceTimeRangeTo.getCurrentHour();
        int toMinute = serviceTimeRangeTo.getCurrentMinute();

        SimpleDateFormat sdf24Hour = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat sdf12Hour = new SimpleDateFormat("h:mm a", Locale.getDefault());

        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocationName(serviceLocation, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                latitude = address.getLatitude();
                longitude = address.getLongitude();
            } else {
                Toast.makeText(requireContext(), "Cannot find location.", Toast.LENGTH_SHORT).show();
                return;
            }

            Date fromTime = sdf24Hour.parse(String.format(Locale.getDefault(), "%02d:%02d", fromHour, fromMinute));
            Date toTime = sdf24Hour.parse(String.format(Locale.getDefault(), "%02d:%02d", toHour, toMinute));

            serviceTimeRange = String.format(Locale.getDefault(), "%s - %s",
                    sdf12Hour.format(fromTime), sdf12Hour.format(toTime));

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error finding location.", Toast.LENGTH_SHORT).show();
            return;
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error formatting time range.", Toast.LENGTH_SHORT).show();
            return;
        }

        String phoneNumber = phoneNumberEditText.getText().toString();
        String emailAddress = emailAddressEditText.getText().toString();
        String additionalInfo = additionalInfoEditText.getText().toString();

        // Update the data in the database
        DBOpenHelper dbHelper = new DBOpenHelper(requireContext());
        boolean isUpdated = dbHelper.updateAccountInfo(username, identity, serviceTimeRange, serviceLocation, price, latitude, longitude, phoneNumber, emailAddress, additionalInfo);

        UserViewModel userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        userViewModel.setUserLatitude(latitude);
        userViewModel.setUserLongitude(longitude);

        if (isUpdated) {
            // Display a success message or perform other actions
            Toast.makeText(requireContext(), "Account info saved.", Toast.LENGTH_SHORT).show();
        } else {
            // Display an error message or perform other actions
            Toast.makeText(requireContext(), "Error saving account info.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username", null);
        if (username != null) {
            loadAccountInfo(username);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Restore the back arrow
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
