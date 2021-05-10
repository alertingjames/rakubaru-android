package com.app.rakubaru.service

import android.content.*
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.app.rakubaru.commons.Commons
import com.google.android.gms.maps.model.LatLng

public class ForegroundServiceInitialize() {
    var foregroundOnlyLocationServiceBound = false
    // Provides location updates for while-in-use feature.
    var foregroundOnlyLocationService: ForegroundOnlyLocationService? = null
    // Listens for location broadcasts from ForegroundOnlyLocationService.
    private lateinit var foregroundOnlyBroadcastReceiver: ForegroundOnlyBroadcastReceiver
    lateinit var sharedPreferences: SharedPreferences

    public val foregroundOnlyServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundOnlyLocationService.LocalBinder
            foregroundOnlyLocationService = binder.service
            foregroundOnlyLocationServiceBound = true
        }
        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }

    fun initialize() {
        // Monitors connection to the while-in-use service.
        foregroundOnlyBroadcastReceiver = ForegroundOnlyBroadcastReceiver()
    }

    fun onResume() {
        LocalBroadcastManager.getInstance(Commons.homeActivity).registerReceiver(
                foregroundOnlyBroadcastReceiver,
                IntentFilter(
                        ForegroundOnlyLocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
        )
    }

    fun onPause(){
        LocalBroadcastManager.getInstance(Commons.homeActivity).unregisterReceiver(
                foregroundOnlyBroadcastReceiver
        )
    }

    /**
     * Receiver for location broadcasts from [ForegroundOnlyLocationService].
     */
    private inner class ForegroundOnlyBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(
                    ForegroundOnlyLocationService.EXTRA_LOCATION
            )
            if (location != null) {
                Log.d("FOREGROUND LOCATION : ", location.toString());
//                Commons.homeActivity.myLatLng = LatLng(location.latitude, location.longitude)
//                Commons.homeActivity.refreshMyMarker(Commons.homeActivity.myLatLng)
            }
        }
    }

}


























