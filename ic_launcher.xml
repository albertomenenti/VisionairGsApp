package it.visionair.gsapp

import android.content.Context
import it.visionair.gsapp.model.Program
import it.visionair.gsapp.model.TimeSlot
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

/**
 * Carica il palinsesto da res/raw/programs.json e fornisce
 * il programma attualmente in onda.
 *
 * Tutti gli orari nel JSON sono interpretati nel fuso orario Europe/Rome.
 */
class ProgramSchedule(private val context: Context) {

    private val timeZone: ZoneId = ZoneId.of("Europe/Rome")

    private val programs: List<Program> by lazy { loadFromRaw() }

    private val defaultProgram: Program = Program(
        id = "default",
        title = "Visionair Golden Stream",
        host = "Programmazione musicale continua",
        description = "Musica selezionata 24 ore su 24.",
        hostBio = "",
        slots = emptyList()
    )

    /**
     * Programma in onda all'istante [now] (default: adesso, fuso Europe/Rome).
     * Se nessuna fascia oraria copre il momento attuale, ritorna il programma di default.
     */
    fun nowPlaying(now: ZonedDateTime = ZonedDateTime.now(timeZone)): Program {
        val day = now.dayOfWeek
        val time = now.toLocalTime()
        return programs.firstOrNull { program ->
            program.slots.any { it.contains(day, time) }
        } ?: defaultProgram
    }

    private fun loadFromRaw(): List<Program> {
        val raw = context.resources.openRawResource(R.raw.programs)
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(raw)
        val arr = root.getJSONArray("programs")
        val out = mutableListOf<Program>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            out += Program(
                id = obj.getString("id"),
                title = obj.getString("title"),
                host = obj.getString("host"),
                description = obj.optString("description", ""),
                hostBio = obj.optString("hostBio", ""),
                slots = parseSlots(obj.optJSONArray("slots"))
            )
        }
        return out
    }

    private fun parseSlots(arr: JSONArray?): List<TimeSlot> {
        if (arr == null) return emptyList()
        val out = mutableListOf<TimeSlot>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val day = DayOfWeek.valueOf(obj.getString("day").uppercase(Locale.ROOT))
            val start = LocalTime.parse(obj.getString("start"))
            val duration = obj.getInt("durationMinutes")
            out += TimeSlot(day, start, duration)
        }
        return out
    }
}
