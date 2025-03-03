package com.jetflex

import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jetflex.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {
    
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = checkNotNull(_binding) { "Activity has been destroyed" }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.apply {
        systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }
        binding.surfaceView.holder.addCallback(this)
    }
    
    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d("MainActivity", "Surface Created")
        // TODO
    }
    
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d("MainActivity", "Surface Changed: width=$width, height=$height")
        // TODO
    }
    
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d("MainActivity", "Surface Destroyed")
        // TODO
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
