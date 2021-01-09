package idn.faza.instagramClone

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import idn.faza.instagramClone.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    //medeklarasika Binding
    private lateinit var binding: ActivityMainBinding
    //medeklarasika apparcofiguratio utuk ottom avigatio
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //viewBinding inflater
        val inflater = layoutInflater
        //set viewBinding utuk activity_main.xml
        binding = ActivityMainBinding.inflate(inflater)
        setContentView(binding.root)

        //definisikan id NavHostFragment yang ada di activity_main.xml
        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as  NavHostFragment? ?:return
        val navController = host.navController

        //navView adalah id dari bottomNavigation
        binding.navView.setupWithNavController(navController)
    }
}