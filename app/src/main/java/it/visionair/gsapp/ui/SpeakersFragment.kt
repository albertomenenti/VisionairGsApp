package it.visionair.gsapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
        binding.speakersList.adapter = SpeakerAdapter(schedule.allSpeakers())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
