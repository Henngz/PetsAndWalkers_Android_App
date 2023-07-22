package com.example.petsandwalkers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private NavController.OnDestinationChangedListener destinationChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Clear the login cache.
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("username");
        editor.apply();

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        final NavController navController = Navigation.findNavController(this, R.id.fragment);
        AppBarConfiguration configuration = new AppBarConfiguration.Builder(bottomNavigationView.getMenu()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, configuration);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Set the default selected item
        bottomNavigationView.setSelectedItemId(R.id.pets_walker_map);

        // Create destination change listener
        destinationChangedListener = (controller, destination, arguments) -> {
            int destinationId = destination.getId();
            Menu menu = bottomNavigationView.getMenu();
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                item.setChecked(item.getItemId() == destinationId);
            }
        };

        // Add destination change listener
        navController.addOnDestinationChangedListener(destinationChangedListener);
    }

    @Override
    protected void onDestroy() {
        // Remove destination change listener
        NavController navController = Navigation.findNavController(this, R.id.fragment);
        navController.removeOnDestinationChangedListener(destinationChangedListener);
        super.onDestroy();
    }
}
