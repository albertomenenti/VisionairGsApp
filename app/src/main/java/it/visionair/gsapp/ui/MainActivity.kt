package it.visionair.gsapp.ui

import android.content.ComponentName
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import it.visionair.gsapp.PlaybackService
import it.visionair.gsapp.ProgramSchedule
import it.visionair.gsapp.R
import it.visionair.gsapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mediaController: MediaController? = null
    private lateinit var schedule: ProgramSchedule
    private var refreshJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        schedule = ProgramSchedule(applicationContext)

        binding.playPauseButton.setOnClickListener { togglePlayback() }

        // Stato iniziale dell'UI
        setLoadingState(true)
        renderNowPlaying()
    }

    override fun onStart() {
        super.onStart()
        connectToService()
        startNowPlayingRefresh()
    }

    override fun onStop() {
        super.onStop()
        refreshJob?.cancel()
        mediaController?.release()
        mediaController = null
    }

    private fun connectToService() {
        val token = SessionToken(
            this,
            ComponentName(this, PlaybackService::class.java)
        )
        val future = MediaController.Builder(this, token).buildAsync()
        future.addListener(
            {
                if (future.isDone && !future.isCancelled) {
                    mediaController = future.get()
                    mediaController?.addListener(playerListener)
                    setLoadingState(false)
                    updatePlayPauseIcon()
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun togglePlayback() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            // Se siamo in stato IDLE (es. dopo errore di rete), riprepariamo
            if (controller.playbackState == Player.STATE_IDLE) {
                controller.prepare()
            }
            controller.play()
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlayPauseIcon()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val buffering = playbackState == Player.STATE_BUFFERING
            binding.bufferingIndicator.visibility =
                if (buffering) View.VISIBLE else View.GONE
        }
    }

    private fun updatePlayPauseIcon() {
        val playing = mediaController?.isPlaying == true
        binding.playPauseButton.setImageResource(
            if (playing) R.drawable.ic_pause else R.drawable.ic_play
        )
        binding.playPauseButton.contentDescription =
            getString(if (playing) R.string.action_pause else R.string.action_play)
    }

    private fun setLoadingState(loading: Boolean) {
        binding.playPauseButton.isEnabled = !loading
        binding.bufferingIndicator.visibility = if (loading) View.VISIBLE else View.GONE
    }

    /** Aggiorna "in onda ora" e bio conduttore ogni 30 secondi. */
    private fun startNowPlayingRefresh() {
        refreshJob?.cancel()
        refreshJob = lifecycleScope.launch {
            while (true) {
                renderNowPlaying()
                delay(30_000L)
            }
        }
    }

    private fun renderNowPlaying() {
        val program = schedule.nowPlaying()
        binding.programTitle.text = program.title
        binding.programHost.text = if (program.host.isBlank()) "" else
            getString(R.string.host_prefix, program.host)
        binding.programDescription.text = program.description
        binding.hostBio.text = program.hostBio
        binding.hostBio.visibility =
            if (program.hostBio.isBlank()) View.GONE else View.VISIBLE
    }
}
