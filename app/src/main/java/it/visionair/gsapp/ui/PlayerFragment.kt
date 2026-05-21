package it.visionair.gsapp.ui

import android.content.ComponentName
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.util.concurrent.MoreExecutors
import it.visionair.gsapp.PlaybackService
import it.visionair.gsapp.ProgramSchedule
import it.visionair.gsapp.R
import it.visionair.gsapp.databinding.FragmentPlayerBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@UnstableApi
class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private var mediaController: MediaController? = null
    private lateinit var schedule: ProgramSchedule
    private var refreshJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        schedule = ProgramSchedule(requireContext().applicationContext)

        binding.playPauseButton.setOnClickListener { togglePlayback() }

        // Titolo programma → vai alla tab Programmi
        binding.programTitle.setOnClickListener {
            navigateTo(R.id.nav_programs)
        }

        // Nomi conduttori → vai alla tab Conduttori
        binding.programHost.setOnClickListener {
            navigateTo(R.id.nav_speakers)
        }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** Seleziona una tab della bottom navigation dall'esterno del fragment. */
    private fun navigateTo(navItemId: Int) {
        activity?.findViewById<BottomNavigationView>(R.id.bottomNav)
            ?.selectedItemId = navItemId
    }

    private fun connectToService() {
        val token = SessionToken(
            requireContext(),
            ComponentName(requireContext(), PlaybackService::class.java)
        )
        val future = MediaController.Builder(requireContext(), token).buildAsync()
        future.addListener(
            {
                if (future.isDone && !future.isCancelled && _binding != null) {
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
            if (controller.playbackState == Player.STATE_IDLE) controller.prepare()
            controller.play()
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlayPauseIcon()
        }
        override fun onPlaybackStateChanged(playbackState: Int) {
            _binding?.bufferingIndicator?.visibility =
                if (playbackState == Player.STATE_BUFFERING) View.VISIBLE else View.GONE
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
        if (_binding == null) return
        val now = schedule.nowPlaying()
        binding.programTitle.text = now.program.title

        val hosts = now.speakerNames
        if (hosts.isBlank()) {
            binding.programHost.visibility = View.GONE
        } else {
            binding.programHost.visibility = View.VISIBLE
            binding.programHost.text = getString(R.string.host_prefix, hosts)
        }

        val desc = now.program.description
        binding.programDescription.text = desc
        binding.programDescription.visibility = if (desc.isBlank()) View.GONE else View.VISIBLE
    }
}
