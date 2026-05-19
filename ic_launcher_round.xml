package it.visionair.gsapp.model

import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Un programma del palinsesto.
 *
 * @param id            identificativo univoco interno
 * @param title         titolo del programma (es. "Frequenze Nomadi")
 * @param host          conduttore o conduttori (es. "Alberto Menenti")
 * @param description   breve descrizione del programma
 * @param hostBio       bio del/dei conduttori
 * @param slots         fasce orarie settimanali in cui va in onda
 */
data class Program(
    val id: String,
    val title: String,
    val host: String,
    val description: String,
    val hostBio: String,
    val slots: List<TimeSlot>
)

/**
 * Una fascia oraria settimanale: giorno della settimana + orario di inizio + durata in minuti.
 */
data class TimeSlot(
    val day: DayOfWeek,
    val start: LocalTime,
    val durationMinutes: Int
) {
    fun contains(weekDay: DayOfWeek, time: LocalTime): Boolean {
        if (weekDay != day) return false
        val end = start.plusMinutes(durationMinutes.toLong())
        // Slot che attraversano la mezzanotte non sono gestiti qui per semplicità;
        // se serve, si spezzano in due slot in palinsesto.
        return !time.isBefore(start) && time.isBefore(end)
    }
}
