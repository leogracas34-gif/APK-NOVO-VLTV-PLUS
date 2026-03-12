package com.vltv.plus

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vltv.plus.databinding.ActivityMainBinding
import com.vltv.plus.ui.login.LoginFragment // Import necessário para resolver o erro 'Unresolved reference'

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Carregar LoginFragment por padrão no início do VLTV+
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, LoginFragment())
                .commit()
        }
    }
}
