package com.example.petsandwalkers;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserViewModel extends ViewModel {
    private MutableLiveData<String> userAddress;
    private MutableLiveData<Double> userLatitude;
    private MutableLiveData<Double> userLongitude;

    public UserViewModel() {
        userAddress = new MutableLiveData<>();
        userLatitude = new MutableLiveData<>();
        userLongitude = new MutableLiveData<>();
    }

    public MutableLiveData<String> getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String address) {
        userAddress.setValue(address);
    }

    public MutableLiveData<Double> getUserLatitude() {
        return userLatitude;
    }

    public void setUserLatitude(Double latitude) {
        userLatitude.setValue(latitude);
    }

    public MutableLiveData<Double> getUserLongitude() {
        return userLongitude;
    }

    public void setUserLongitude(Double longitude) {
        userLongitude.setValue(longitude);
    }
}

