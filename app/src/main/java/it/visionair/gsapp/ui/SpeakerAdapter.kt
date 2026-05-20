package it.visionair.gsapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.visionair.gsapp.R
import it.visionair.gsapp.databinding.ItemSpeakerBinding
import it.visionair.gsapp.model.Speaker
import it.visionair.gsapp.resolveDrawable

class SpeakerAdapter(private val items: List<Speaker>) :
    RecyclerView.Adapter<SpeakerAdapter.VH>() {

    class VH(val b: ItemSpeakerBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemSpeakerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]
        val ctx = holder.itemView.context

        holder.b.speakerName.text = s.name

        if (s.bio.isBlank()) {
            holder.b.speakerBio.visibility = View.GONE
        } else {
            holder.b.speakerBio.visibility = View.VISIBLE
            holder.b.speakerBio.text = s.bio
        }

        val resId = ctx.resolveDrawable(s.photo)
        if (resId != 0) {
            holder.b.speakerPhoto.setImageResource(resId)
        } else {
            holder.b.speakerPhoto.setImageResource(R.drawable.speaker_placeholder)
        }
    }
}
