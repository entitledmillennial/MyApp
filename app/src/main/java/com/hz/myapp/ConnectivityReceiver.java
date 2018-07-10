package com.hz.myapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by zeee on 07-03-2018.
 */

public class ConnectivityReceiver extends BroadcastReceiver {

    private ConnectivityReceiverListener connectivityReceiverListener;

    private ConnectivityReceiver(){
        super();
    }

    public static ConnectivityReceiver getInstance(){
        return new ConnectivityReceiver();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (connectivityReceiverListener != null) {
            connectivityReceiverListener.onNetworkConnectionChanged(isConnected);
        }
    }

    public boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    public interface ConnectivityReceiverListener{
        void onNetworkConnectionChanged(boolean isConnected);
    }

    public void setConnectivityReceiverListener(ConnectivityReceiver.ConnectivityReceiverListener connectivityReceiverListener){
        this.connectivityReceiverListener = connectivityReceiverListener;
    }
}
