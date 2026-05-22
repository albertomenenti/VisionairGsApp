package it.visionair.gsapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import it.visionair.gsapp.R
import it.visionair.gsapp.databinding.ActivityMainBinding

/**
 * Interfaccia di navigazione: i fragment interni (es. PlayerFragment)
 * la usano per richiedere di spostarsi a un altro tab con scroll a un elemento specifico.
 */
interface NavigationCallback {
    fun navigateToProgram(programId: String)
    fun navigateToSpeaker(speakerId: String)
}

@UnstableApi
class MainActivity : AppCompatActivity(), NavigationCallback {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            showFragment(PlayerFragment(), "player")
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_player   -> { showFragment(PlayerFragment(),   "player");   true }
                R.id.nav_speakers -> { showFragment(SpeakersFragment(), "speakers"); true }
                R.id.nav_programs -> { showFragment(ProgramsFragment(), "programs"); true }
                else -> false
            }
        }
    }

    // --- NavigationCallback ---

    override fun navigateToProgram(programId: String) {
        binding.bottomNav.selectedItemId = R.id.nav_programs
        showFragment(ProgramsFragment.newInstance(scrollToId = programId), "programs")
    }

    override fun navigateToSpeaker(speakerId: String) {
        binding.bottomNav.selectedItemId = R.id.nav_speakers
        showFragment(SpeakersFragment.newInstance(scrollToId = speakerId), "speakers")
    }

    // ---

    private fun showFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.navHost, fragment, tag)
            .commit()
    }
}
