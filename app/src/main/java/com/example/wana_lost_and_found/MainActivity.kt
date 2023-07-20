package com.example.wana_lost_and_found

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.manager.Lifecycle
import com.example.wana_lost_and_found.databinding.ActivityMainBinding
import com.example.wana_lost_and_found.model.ReportType
import com.example.wana_lost_and_found.ui.add_view_report.ActivityAddReport
import com.example.wana_lost_and_found.ui.home.HomeFragmentDirections
import com.example.wana_lost_and_found.ui.login_signup.LoginActivity
import com.example.wana_lost_and_found.utils.REPORT_TYPE_EXTRA_KEY
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val intent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setLogo(R.drawable.ic_launcher_foreground)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.title = "My Reports"

        addMenuProvider(object : MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_options_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId){
                    R.id.action_sign_out ->{
                        firebaseAuth.signOut()
                        val loginIntent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(loginIntent)
                        finish()
                        return true
                    }

                }
                return true
            }

        }, this@MainActivity, androidx.lifecycle.Lifecycle.State.RESUMED)
        val navView: BottomNavigationView = binding.navView

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment

        navController = navHostFragment.navController
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_reports, R.id.navigation_pending
            )
        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener(this@MainActivity)
        navController.navigateUp(appBarConfiguration)

        binding.fabAddReport.setOnClickListener {
            /*val direction = HomeFragmentDirections.actionNavigationHomeToAddReportFragment()
            navController.navigate(direction)*/

            val reportTypeDialog = MaterialAlertDialogBuilder(this@MainActivity)
                .setTitle("Add Report")
                .setMessage("Report item you have...")
                .setPositiveButton("Found"){_, _ ->
                    val foundReportIntent = Intent(this@MainActivity,
                        ActivityAddReport::class.java)
                    foundReportIntent.putExtra(REPORT_TYPE_EXTRA_KEY, ReportType.FOUND.identity)
                    startActivity(foundReportIntent)

                }
                .setNegativeButton("Lost"){_, _ ->
                    val lostReportIntent = Intent(this@MainActivity,
                        ActivityAddReport::class.java)
                    lostReportIntent.putExtra(REPORT_TYPE_EXTRA_KEY, ReportType.LOST.identity)
                    startActivity(lostReportIntent)

                }
                .create()

            reportTypeDialog.show()
        }
    }


    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        when(destination.id){
            R.id.addReportFragment ->{
                hideBottomNavAndFab()
            }
            R.id.navigation_home ->{
                supportActionBar!!.title = "My Reports"
                showBottomNavAndFab()
            }
            R.id.navigation_reports ->{
                supportActionBar!!.title = "Global Reports"
                hideFab()
            }
            R.id.navigation_pending ->{
                supportActionBar!!.title = "Pending actions"
                hideFab()
            }
        }
    }

    private fun showBottomNavAndFab(){
        binding.fabAddReport.visibility = View.VISIBLE
        binding.navView.visibility = View.VISIBLE
    }

    private fun hideBottomNavAndFab(){
        binding.fabAddReport.visibility = View.GONE
        binding.navView.visibility = View.GONE
    }

    override fun onRestart() {
        super.onRestart()
    }
    private fun hideFab(){
        binding.fabAddReport.visibility = View.GONE
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onNavigateUp()
    }

    private fun checkAuthenticationState(){

        val user = FirebaseAuth.getInstance().currentUser
         if (user == null) {
             val intent = Intent(this@MainActivity, LoginActivity::class.java)
             intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
             startActivity(intent)
             finish()
            }
        else{
             Log.d("MainActivity", "checkAuthenticationState: Authenticated user")
         }
    }

    override fun onResume() {
        super.onResume()
        checkAuthenticationState()
    }
}