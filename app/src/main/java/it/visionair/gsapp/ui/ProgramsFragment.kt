package it.visionair.gsapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import it.visionair.gsapp.ProgramSchedule
import it.visionair.gsapp.databinding.FragmentProgramsBinding

class ProgramsFragment : Fragment() {

    private var _binding: FragmentProgramsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgramsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val schedule = ProgramSchedule(requireContext().applicationContext)
        // Ordino i programmi per orario di inizio del primo slot, così la lista
        // segue il flusso della giornata.
        val sorted = schedule.allPrograms().sortedBy {
            it.slots.firstOrNull()?.start?.toSecondOfDay() ?: Int.MAX_VALUE
        }
        binding.programsList.adapter = ProgramAdapter(sorted, schedule)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
