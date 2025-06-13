package com.unicofrance.sygic_road_closure_repro

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sygic.aura.ResourceManager
import com.sygic.aura.ResourceManager.OnResultListener
import com.sygic.aura.embedded.IApiCallback
import com.unicofrance.sygic_road_closure_repro.databinding.ActivityMainBinding
import com.sygic.aura.embedded.SygicFragmentSupportV4
import com.sygic.sdk.api.ApiNavigation
import com.sygic.sdk.api.ApiOnline
import com.sygic.sdk.api.events.ApiEvents
import com.sygic.sdk.api.model.WayPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

        findViewById<View>(R.id.button)
            .setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
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
    }

    private fun checkSygicResources(onSuccess: () -> Unit) {
        val resourceManager = ResourceManager(this, null)
        if (resourceManager.shouldUpdateResources()) {
            Toast.makeText(this, "Please wait while Sygic resources are being updated", Toast.LENGTH_LONG).show()
            resourceManager.updateResources(object : OnResultListener {
                override fun onError( errorCode: Int, message: String) {
                    Toast.makeText(this@MainActivity, "Failed to update resources: $message", Toast.LENGTH_LONG).show()
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
            ApiOnline.addMapCorrectionEvents(MAP_CORRECTIONS, 0)
            runOnUiThread {
                findViewById<View>(R.id.button).isEnabled = true
            }
        }
    }
}


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