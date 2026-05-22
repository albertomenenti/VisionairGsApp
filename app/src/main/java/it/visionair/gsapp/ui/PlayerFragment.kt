package it.visionair.gsapp.ui

import android.content.ComponentName
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import it.visionair.gsapp.PlaybackService
import it.visionair.gsapp.ProgramSchedule
import it.visionair.gsapp.R
import it.visionair.gsapp.databinding.FragmentPlayerBinding
import it.visionair.gsapp.model.NowPlaying
import it.visionair.gsapp.model.Speaker
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
    private var currentNowPlaying: NowPlaying? = null

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

        // Titolo programma → tab Programmi scrollata al programma corrente
        binding.programTitle.setOnClickListener {
            currentNowPlaying?.program?.id?.let { id ->
                (requireActivity() as? NavigationCallback)?.navigateToProgram(id)
            }
        }

        // I conduttori vengono resi cliccabili singolarmente in renderNowPlaying()
        binding.programHost.movementMethod = LinkMovementMethod.getInstance()

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

    private fun connectToService() {
        val token = SessionToken(
            requireContext(),
            ComponentName(requireContext(), PlaybackService::class.java)
        )
        val future = MediaController.Builder(requireContext(), token).buildAsync()
        future.addListener({
            if (future.isDone && !future.isCancelled && _binding != null) {
                mediaController = future.get()
                mediaController?.addListener(playerListener)
                setLoadingState(false)
                updatePlayPauseIcon()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun togglePlayback() {
        val c = mediaController ?: return
        if (c.isPlaying) c.pause()
        else { if (c.playbackState == Player.STATE_IDLE) c.prepare(); c.play() }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) = updatePlayPauseIcon()
        override fun onPlaybackStateChanged(state: Int) {
            _binding?.bufferingIndicator?.visibility =
                if (state == Player.STATE_BUFFERING) View.VISIBLE else View.GONE
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
            while (true) { renderNowPlaying(); delay(30_000L) }
        }
    }

    private fun renderNowPlaying() {
        if (_binding == null) return
        val now = schedule.nowPlaying()
        currentNowPlaying = now

        binding.programTitle.text = now.program.title

        if (now.speakers.isEmpty()) {
            binding.programHost.visibility = View.GONE
        } else {
            binding.programHost.visibility = View.VISIBLE
            binding.programHost.text = buildClickableHosts(now.speakers)
        }

        val desc = now.program.description
        binding.programDescription.text = desc
        binding.programDescription.visibility = if (desc.isBlank()) View.GONE else View.VISIBLE
    }

    /**
     * Costruisce una SpannableString "Con Nome1 · Nome2 · Nome3" dove ogni nome
     * è un ClickableSpan in oro che naviga al conduttore nella tab Conduttori.
     */
    private fun buildClickableHosts(speakers: List<Speaker>): SpannableString {
        val separator = " · "
        val prefix = getString(R.string.host_prefix, "").trimEnd() + " "   // "Con "
        val sb = StringBuilder(prefix)
        speakers.forEachIndexed { i, s ->
            sb.append(s.name)
            if (i < speakers.lastIndex) sb.append(separator)
        }
        val span = SpannableString(sb)

        val goldColor = requireContext().getColor(R.color.visionair_gold)

        var cursor = prefix.length
        speakers.forEach { speaker ->
            val nameEnd = cursor + speaker.name.length
            span.setSpan(
                object : ClickableSpan() {
                    override fun onClick(v: View) {
                        (requireActivity() as? NavigationCallback)
                            ?.navigateToSpeaker(speaker.id)
                    }
                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = goldColor
                        ds.isUnderlineText = false   // link oro senza sottolineatura
                    }
                },
                cursor, nameEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            cursor = nameEnd + separator.length
        }
        return span
    }
}
