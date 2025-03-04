package com.jetflex

import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jetflex.databinding.ActivityJetflexBinding

public class JetFlexActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private var _binding: ActivityJetflexBinding? = null
    private val binding: ActivityJetflexBinding
        get() = checkNotNull(_binding) { "Activity has been destroyed" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityJetflexBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.apply {
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
        binding.jetflexSurfaceView.holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d("JetFlexActivity", "Surface Created")
        // TODO
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d("JetFlexActivity", "Surface Changed: width=$width, height=$height")
        // TODO
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d("JetFlexActivity", "Surface Destroyed")
        // TODO
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
