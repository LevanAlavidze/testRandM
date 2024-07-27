package com.example.testrickmorty

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.testrickmorty.databinding.ActivityMainBinding
import com.example.testrickmorty.feature.characters.di.CharacterViewModelFactory
import com.example.testrickmorty.feature.characters.vm.CharacterViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: CharacterViewModel
    private lateinit var navController: NavController

    var currentSearchQuery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up ViewModel
        val repository = (application as MyApplication).repository
        val viewModelFactory = CharacterViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[CharacterViewModel::class.java]

        setSupportActionBar(binding.toolbar)

        // Set up NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set up AppBarConfiguration
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.characterFragment, R.id.locationsFragment, R.id.episodesFragment)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Set up BottomNavigationView
        val bottomNavigationView: BottomNavigationView = binding.bottomNavigation
        bottomNavigationView.setupWithNavController(navController)

        // Handle back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentDestination = navController.currentDestination
                if (currentDestination?.id == R.id.characterFragment) {
                    finish() // Close the app
                } else {
                    navController.navigateUp() // Handle default back navigation
                }
            }
        })

        // Listen for destination changes to control back arrow visibility
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.characterFragment) {
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            } else {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
