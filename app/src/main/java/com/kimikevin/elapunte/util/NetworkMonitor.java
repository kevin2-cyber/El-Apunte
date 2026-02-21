package com.kimikevin.elapunte.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NetworkMonitor {
    private static final String TAG = "NetworkMonitor";
    private final ConnectivityManager connectivityManager;
    private final MutableLiveData<Boolean> isConnected = new MutableLiveData<>(false);
    // Thread-safe flag for background thread checks (LiveData.getValue() is unreliable off main thread)
    private final AtomicBoolean connected = new AtomicBoolean(false);

    @Inject
    public NetworkMonitor(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Synchronously determine current state BEFORE any async callbacks
        boolean currentlyConnected = checkCurrentConnection();
        connected.set(currentlyConnected);
        isConnected.postValue(currentlyConnected);
        Log.d(TAG, "Initial connectivity: " + currentlyConnected);
        registerCallback();
    }

    /**
     * Synchronously checks and returns the current network state.
     */
    private boolean checkCurrentConnection() {
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(network);
            return caps != null &&
                    (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                     caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        }
        return false;
    }

    private void registerCallback() {
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                Log.d(TAG, "Network available");
                connected.set(true);
                isConnected.postValue(true);
            }

            @Override
            public void onLost(@NonNull Network network) {
                Log.d(TAG, "Network lost");
                connected.set(false);
                isConnected.postValue(false);
            }
        });
    }

    /**
     * Observable for UI / ViewModel use (main-thread).
     */
    public LiveData<Boolean> getIsConnected() {
        return isConnected;
    }

    /**
     * Thread-safe check for background / executor threads.
     */
    public boolean isCurrentlyConnected() {
        return connected.get();
    }
}

