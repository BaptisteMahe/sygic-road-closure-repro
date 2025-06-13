package com.unico.dev.appmobile

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sygic.aura.ResourceManager
import com.sygic.aura.ResourceManager.OnResultListener
import com.sygic.aura.embedded.IApiCallback
import com.sygic.aura.embedded.SygicFragmentSupportV4
import com.sygic.sdk.api.ApiNavigation
import com.sygic.sdk.api.ApiOnline
import com.sygic.sdk.api.events.ApiEvents
import com.sygic.sdk.api.model.WayPoint
import com.unico.dev.appmobile.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

const val SYGIC_GEO_COEF = 100_000.0

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkSygicResources {
            val fgm = SygicNavigationFragment(this::onSygicStart)

            supportFragmentManager
                .beginTransaction()
                .replace(R.id.sygicmap, fgm)
                .commitAllowingStateLoss()
        }


        findViewById<View>(R.id.load_button)
            .setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    ApiOnline.addMapCorrectionEvents(MAP_CORRECTIONS, 0)
                    toast("Map corrections loaded")
                }
            }

        findViewById<View>(R.id.clear_button)
            .setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    ApiOnline.clearMapCorrectionEvents(0)
                    toast("Map corrections cleared")
                }
            }

        findViewById<View>(R.id.navigate_button)
            .setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    startNavigation()
                    toast("Navigation started")
                }
            }

        findViewById<View>(R.id.break_button)
            .setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    listOf(
                        async { clearAndLoadMapCorrections() },
                        async { startNavigation() }
                    ).awaitAll()
                    toast("Map corrections loaded & navigation started IN PARALLEL")
                }
            }
    }

    private fun checkSygicResources(onSuccess: () -> Unit) {
        val resourceManager = ResourceManager(this, null)
        if (resourceManager.shouldUpdateResources()) {
            Toast.makeText(this, "Please wait while Sygic resources are being updated", Toast.LENGTH_LONG).show()
            resourceManager.updateResources(object : OnResultListener {
                override fun onError( errorCode: Int, message: String) {
                    toast("Failed to update resources: $message")
                }

                override fun onSuccess() {
                    onSuccess()
                }
            })
        }
        else onSuccess()
    }

    private fun onSygicStart() {
        CoroutineScope(Dispatchers.IO).launch {
            ApiOnline.clearMapCorrectionEvents(0)
            runOnUiThread {
                findViewById<View>(R.id.load_button).isEnabled = true
                findViewById<View>(R.id.clear_button).isEnabled = true
                findViewById<View>(R.id.navigate_button).isEnabled = true
                findViewById<View>(R.id.break_button).isEnabled = true
            }
        }
    }

    private fun clearAndLoadMapCorrections() {
        ApiOnline.clearMapCorrectionEvents(0)
        ApiOnline.addMapCorrectionEvents(MAP_CORRECTIONS, 0)
    }

    private fun startNavigation() {
        runCatching { ApiNavigation.stopNavigation(0) }
        ApiNavigation.startNavigation(
            WayPoint(
                "",
                (-0.135820 * SYGIC_GEO_COEF).toInt(),
                (45.002110 * SYGIC_GEO_COEF).toInt()
            ),
            0,
            false,
            0
        )
    }
}

fun AppCompatActivity.toast(message: String) = runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_LONG).show() }


class SygicNavigationFragment(
    private val onSygicStart: () -> Unit
) : SygicFragmentSupportV4() {
    override fun onResume() {
        startNavi()
        setCallback(
            SygicNaviCallback(onSygicStart)
        )
        super.onResume()
    }
}

class SygicNaviCallback(
    private val onSygicStart: () -> Unit
) : IApiCallback {
    override fun onEvent(event: Int, data: String?) {
        Log.d("SygicNaviCallback", "onEvent: $event")
        if (event == ApiEvents.EVENT_APP_STARTED)
            onSygicStart()
    }

    override fun onServiceConnected() {
        Log.d("SygicNaviCallback", "onServiceConnected")

    }

    override fun onServiceDisconnected() {
        Log.d("SygicNaviCallback", "onServiceDisconnected")
    }
}