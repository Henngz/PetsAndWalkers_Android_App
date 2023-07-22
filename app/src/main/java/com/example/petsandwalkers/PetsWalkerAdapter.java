package com.example.petsandwalkers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PetsWalkerAdapter extends RecyclerView.Adapter<PetsWalkerAdapter.PetsWalkerViewHolder> {

    private Context context;
    private List<PetWalker> petWalkerList;
    private Location userCurrentLocation;

    public PetsWalkerAdapter(Context context, List<PetWalker> petWalkerList) {
        this.context = context;
        this.petWalkerList = petWalkerList;
    }

    @NonNull
    @Override
    public PetsWalkerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pets_walker_list_item, parent, false);
        return new PetsWalkerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetsWalkerViewHolder holder, int position) {
        PetWalker petWalker = petWalkerList.get(position);
        holder.usernameTextView.setText("Username: " + petWalker.getUsername());
        holder.identityTextView.setText("Identity: " + petWalker.getIdentity());
        holder.serviceTimeRangeTextView.setText("Service Time Range: " + petWalker.getServiceTimeRange());
        holder.serviceLocationTextView.setText("Service Location: " + petWalker.getServiceLocation());
        holder.priceTextView.setText("Price: " + petWalker.getPrice());

        if (userCurrentLocation != null) {
            float[] results = new float[1];
            Location.distanceBetween(userCurrentLocation.getLatitude(), userCurrentLocation.getLongitude(), petWalker.getLatitude(), petWalker.getLongitude(), results);
            float distanceInMeters = results[0];
            holder.distanceTextView.setText("Distance: " + String.format(Locale.getDefault(), "%.2f km", distanceInMeters / 1000));
        } else {
            holder.distanceTextView.setText("Distance: Distance unavailable");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isUserLoggedIn()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(petWalker.getUsername());

                    SpannableStringBuilder messageBuilder = new SpannableStringBuilder();
                    messageBuilder.append("Identity: ").append(petWalker.getIdentity())
                            .append("\nService Time Range: ").append(petWalker.getServiceTimeRange())
                            .append("\nService Location: ").append(petWalker.getServiceLocation())
                            .append("\nPrice: ").append(String.valueOf(petWalker.getPrice()))
                            .append("\n").append(holder.distanceTextView.getText())
                            .append("\nPhone Number: ");

                    int phoneNumberStart = messageBuilder.length();
                    messageBuilder.append(petWalker.getPhoneNumber());
                    int phoneNumberEnd = messageBuilder.length();

                    ClickableSpan phoneNumberClickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            dialPhoneNumber(petWalker.getPhoneNumber());
                        }
                    };

                    messageBuilder.setSpan(phoneNumberClickableSpan, phoneNumberStart, phoneNumberEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    messageBuilder.append("\nEmail Address: ").append(petWalker.getEmailAddress())
                            .append("\nAdditional Info: ").append(petWalker.getAdditionalInfo());

                    TextView messageTextView = new TextView(context);
                    messageTextView.setText(messageBuilder);
                    messageTextView.setMovementMethod(LinkMovementMethod.getInstance());

                    builder.setView(messageTextView);
                    builder.setPositiveButton("OK", null);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    Toast.makeText(context, "Please log in to view detailed information", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.openMapButton.setOnClickListener(v -> {
            openMap(holder.serviceLocationTextView.getText().toString());
        });
    }

    private void openMap(String address) {
        // Get the location from the address
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Create an implicit intent to open the map application
                Uri geoUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + Uri.encode(address));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                // Verify that there is a map application that can handle the intent
                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                } else {
                    Toast.makeText(context, "No map application found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Address not found.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error opening map.", Toast.LENGTH_SHORT).show();
        }
    }

    private void dialPhoneNumber(String phoneNumber) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:" + phoneNumber));

        if (dialIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(dialIntent);
        } else {
            Toast.makeText(context, "No phone application found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return petWalkerList.size();
    }

    public static class PetsWalkerViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, identityTextView, serviceTimeRangeTextView,
                serviceLocationTextView, priceTextView, distanceTextView;
        Button openMapButton;

        public PetsWalkerViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.username);
            identityTextView = itemView.findViewById(R.id.identity);
            serviceTimeRangeTextView = itemView.findViewById(R.id.service_time_range);
            serviceLocationTextView = itemView.findViewById(R.id.service_location);
            priceTextView = itemView.findViewById(R.id.price);
            distanceTextView = itemView.findViewById(R.id.distance);
            openMapButton = itemView.findViewById(R.id.open_map_button);
        }
    }

    public void updateData(List<PetWalker> newPetWalkerList) {
        this.petWalkerList = newPetWalkerList;
        notifyDataSetChanged();
    }

    public void setUserCurrentLocation(Location location) {
        this.userCurrentLocation = location;
        notifyDataSetChanged();
    }

    public interface UserLoginChecker {
        boolean isUserLoggedIn();
    }

    private boolean isUserLoggedIn() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        Log.d("SharedPreferences", sharedPreferences.toString());

        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("SharedPreferences", entry.getKey() + ": " + entry.getValue().toString());
        }

        return sharedPreferences.contains("username");
    }

}
