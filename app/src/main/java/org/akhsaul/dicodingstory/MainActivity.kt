package org.akhsaul.dicodingstory

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import org.akhsaul.dicodingstory.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private var _navController: NavController? = null
    private val navController get() = _navController!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
    }

    override fun onStart() {
        Log.i(TAG, "onCreate: Activity starting")
        super.onStart()
        _navController = findNavController(binding.fragmentContainerView.id)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.settingsFragment -> {
                    binding.root.fitsSystemWindows = true
                    binding.appBarLayout.visibility = View.VISIBLE
                    binding.fragmentContainerView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        topMargin = getActionBarHeightPx(this@MainActivity)
                    }
                }

                else -> {
                    binding.root.fitsSystemWindows = false
                    binding.appBarLayout.visibility = View.GONE
                    binding.fragmentContainerView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        topMargin = 0
                    }
                }
            }
        }
        val topLevelDestination = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.loginFragment,
                R.id.registerFragment,
            )
        )
        setupActionBarWithNavController(navController, topLevelDestination)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        _navController = null
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    companion object {
        private const val TAG = "MainActivity"
    }

    private fun getActionBarHeightPx(context: Context): Int {
        val tv = TypedValue()
        if (context.theme.resolveAttribute(
                com.google.android.material.R.attr.actionBarSize,
                tv,
                true
            )
        ) {
            return TypedValue.complexToDimensionPixelSize(tv.data, context.resources.displayMetrics)
        }
        return 0
    }
}