package com.app.rakubaru.service

import android.Manifest
import android.app.Activity
import android.content.*
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.app.rakubaru.commons.Commons
import com.app.rakubaru.main.LoginActivity
import com.app.rakubaru.models.RPoint
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

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestBackgroundPermission(activity:Activity) {
        if(ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
            val backPermList = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            AlertDialog.Builder(activity)
                .setTitle("バックグラウンドロケーション許可")
                .setMessage("バックグラウンドで位置情報の更新を取得するための位置情報の許可を許可します。")
                .setPositiveButton("許可する") { _, _ ->
                    requestPermissions(
                        activity,
                        backPermList,
                        34
                    )
                }
                .setNegativeButton("キャンセル") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    fun notify8hoursExceed() {
        foregroundOnlyLocationService?.makeWorkExceedNotification()
    }

}


























