package it.visionair.gsapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.visionair.gsapp.ProgramSchedule
import it.visionair.gsapp.databinding.ItemProgramBinding
import it.visionair.gsapp.model.Program

class ProgramAdapter(
    private val items: List<Program>,
    private val schedule: ProgramSchedule
) : RecyclerView.Adapter<ProgramAdapter.VH>() {

    class VH(val b: ItemProgramBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemProgramBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]

        // Pill orario: mostra il primo slot (la maggior parte dei programmi ne ha uno solo)
        holder.b.programSlot.text = p.slots.firstOrNull()?.toReadable() ?: ""

        holder.b.programTitle.text = p.title

        val hostNames = schedule.speakersOf(p).joinToString(" · ") { it.name }
        if (hostNames.isBlank()) {
            holder.b.programHosts.visibility = View.GONE
        } else {
            holder.b.programHosts.visibility = View.VISIBLE
            holder.b.programHosts.text = "Con $hostNames"
        }

        if (p.description.isBlank()) {
            holder.b.programDescription.visibility = View.GONE
        } else {
            holder.b.programDescription.visibility = View.VISIBLE
            holder.b.programDescription.text = p.description
        }
    }
}
