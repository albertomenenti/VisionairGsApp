package it.visionair.gsapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import it.visionair.gsapp.ProgramSchedule
import it.visionair.gsapp.databinding.FragmentSpeakersBinding

class SpeakersFragment : Fragment() {

    private var _binding: FragmentSpeakersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpeakersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val schedule = ProgramSchedule(requireContext().applicationContext)
        val speakers = schedule.allSpeakers()

        binding.speakersList.adapter = SpeakerAdapter(speakers)

        // Se il fragment è stato aperto con uno scrollToId, scorri al conduttore giusto.
        arguments?.getString(ARG_SCROLL_TO)?.let { targetId ->
            val idx = speakers.indexOfFirst { it.id == targetId }
            if (idx >= 0) {
                (binding.speakersList.layoutManager as? LinearLayoutManager)
                    ?.scrollToPositionWithOffset(idx, 0)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_SCROLL_TO = "scrollToId"

        fun newInstance(scrollToId: String? = null) = SpeakersFragment().apply {
            if (scrollToId != null) {
                arguments = Bundle().apply { putString(ARG_SCROLL_TO, scrollToId) }
            }
        }
    }
}
