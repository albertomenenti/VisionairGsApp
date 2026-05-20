package it.visionair.gsapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import it.visionair.gsapp.R
import it.visionair.gsapp.databinding.ActivityMainBinding

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Frammento iniziale: il player
        if (savedInstanceState == null) {
            showFragment(PlayerFragment(), "player")
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_player   -> { showFragment(PlayerFragment(),    "player");   true }
                R.id.nav_speakers -> { showFragment(SpeakersFragment(),  "speakers"); true }
                R.id.nav_programs -> { showFragment(ProgramsFragment(),  "programs"); true }
                else -> false
            }
        }
    }

    private fun showFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.navHost, fragment, tag)
            .commit()
    }
}
