package com.shayan.playbackmaster.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.shayan.playbackmaster.R
import com.shayan.playbackmaster.services.UsbProximityService
import com.shayan.playbackmaster.ui.fragments.ExitPlaybackListener

class MainActivity : AppCompatActivity(), ExitPlaybackListener {

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_READ_STORAGE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize navigation
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Enable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        // Handle permissions & start USB service
        checkAndRequestPermissions()
    }

    /**
     * Checks if required permissions are granted. If not, requests them.
     */
    private fun checkAndRequestPermissions() {
        if (hasStoragePermission()) {
            handlePlaybackIntent()
            startUsbService()
        } else {
            requestStoragePermission()
        }
    }

    /**
     * Checks if the app has storage permissions.
     */
    private fun hasStoragePermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Requests storage permissions if not already granted.
     */
    private fun requestStoragePermission() {
        val permission =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_VIDEO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

        ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_CODE_READ_STORAGE)
    }

    /**
     * Handles intent data when launching the app to play a video.
     */
    private fun handlePlaybackIntent() {
        val videoUri = intent.getStringExtra("VIDEO_URI")
        val startTime = intent.getStringExtra("START_TIME")
        val endTime = intent.getStringExtra("END_TIME")

        Log.d(
            TAG,
            "Playback Intent Data - VIDEO_URI: $videoUri, START_TIME: $startTime, END_TIME: $endTime"
        )

        if (!videoUri.isNullOrEmpty() && !startTime.isNullOrEmpty() && !endTime.isNullOrEmpty()) {
            navigateToVideoFragment(videoUri, startTime, endTime)
        }
    }

    /**
     * Navigates to the VideoFragment with the provided playback details.
     */
    private fun navigateToVideoFragment(videoUri: String, startTime: String, endTime: String) {
        val bundle = Bundle().apply {
            putString("VIDEO_URI", videoUri)
            putString("START_TIME", startTime)
            putString("END_TIME", endTime)
        }

        navController.setGraph(R.navigation.nav_graph, bundle)
        navController.navigate(R.id.videoFragment, bundle)
    }

    /**
     * Starts the USB proximity service.
     */
    private fun startUsbService() {
        val usbServiceIntent = Intent(this, UsbProximityService::class.java)
        startService(usbServiceIntent)
        Log.d(TAG, "USB Proximity Service started")
    }

    /**
     * Handles permission request results.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_READ_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Storage permission granted")
                handlePlaybackIntent()
                startUsbService()
            } else {
                Log.w(TAG, "Storage permission denied")
                Toast.makeText(this, "Permission required to play video", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Handles navigation up action.
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    /**
     * Handles exit from playback mode, returning to the home fragment.
     */
    override fun onExitPlayback() {
        navController.navigate(R.id.homeFragment)
    }
}