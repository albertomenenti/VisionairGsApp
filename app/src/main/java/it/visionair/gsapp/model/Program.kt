package it.visionair.gsapp.model

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

/** Singolo conduttore della radio. */
data class Speaker(
    val id: String,
    val name: String,
    val bio: String = "",
    /** Nome del drawable della foto (senza prefisso path). Vuoto = nessuna foto. */
    val photo: String = ""
)

/** Programma del palinsesto. */
data class Program(
    val id: String,
    val title: String,
    val speakerIds: List<String>,
    val description: String,
    val slots: List<TimeSlot>
)

/**
 * Fascia oraria settimanale: gruppo di giorni + orario di inizio + durata in minuti.
 * Supporta slot che attraversano la mezzanotte (es. 23:00–01:00).
 */
data class TimeSlot(
    val days: List<DayOfWeek>,
    val start: LocalTime,
    val durationMinutes: Int
) {
    fun contains(weekDay: DayOfWeek, time: LocalTime): Boolean {
        val curMin = time.hour * 60 + time.minute
        val startMin = start.hour * 60 + start.minute
        val endMin = startMin + durationMinutes
        val dayMin = 24 * 60

        return if (endMin <= dayMin) {
            weekDay in days && curMin in startMin until endMin
        } else {
            if (weekDay in days && curMin >= startMin) return true
            val prevDay = weekDay.minus(1)
            prevDay in days && curMin < (endMin - dayMin)
        }
    }

    /** Rappresentazione leggibile per UI, es. "Tutti i giorni 09:00–11:00" o "Lun, Mer 14:00–15:30". */
    fun toReadable(locale: Locale = Locale.ITALIAN): String {
        val endTime = LocalTime.of(
            ((start.hour * 60 + start.minute + durationMinutes) / 60) % 24,
            (start.minute + durationMinutes) % 60
        )
        val daysLabel = when {
            days.size == 7 -> "Tutti i giorni"
            days.size == 5 && DayOfWeek.SATURDAY !in days && DayOfWeek.SUNDAY !in days -> "Lun-Ven"
            days.size == 2 && DayOfWeek.SATURDAY in days && DayOfWeek.SUNDAY in days -> "Sab-Dom"
            else -> days.sorted().joinToString(", ") {
                it.getDisplayName(TextStyle.SHORT, locale).replaceFirstChar { c -> c.uppercase() }
            }
        }
        return "$daysLabel  %02d:%02d–%02d:%02d".format(
            start.hour, start.minute, endTime.hour, endTime.minute
        )
    }
}

/** Snapshot del "cosa è in onda ora", con conduttori risolti. */
data class NowPlaying(
    val program: Program,
    val speakers: List<Speaker>
) {
    val speakerNames: String
        get() = speakers.joinToString(" · ") { it.name }
}
