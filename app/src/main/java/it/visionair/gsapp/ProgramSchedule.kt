package it.visionair.gsapp

import android.content.Context
import it.visionair.gsapp.model.NowPlaying
import it.visionair.gsapp.model.Program
import it.visionair.gsapp.model.Speaker
import it.visionair.gsapp.model.TimeSlot
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

class ProgramSchedule(private val context: Context) {

    private val zone: ZoneId = ZoneId.of("Europe/Rome")

    private val speakersMap: Map<String, Speaker> by lazy { loadSpeakers() }
    private val programsList: List<Program> by lazy { loadPrograms() }

    private val defaultProgram = Program(
        id = "default",
        title = "Visionair Golden Stream",
        speakerIds = emptyList(),
        description = "La programmazione musicale di Visionair continua.",
        slots = emptyList()
    )

    /** Cosa è in onda ora (default = adesso, fuso Europe/Rome). */
    fun nowPlaying(now: ZonedDateTime = ZonedDateTime.now(zone)): NowPlaying {
        val day = now.dayOfWeek
        val time = now.toLocalTime()
        val program = programsList.firstOrNull { p ->
            p.slots.any { it.contains(day, time) }
        } ?: defaultProgram
        val speakerList = program.speakerIds.mapNotNull { speakersMap[it] }
        return NowPlaying(program, speakerList)
    }

    /** Tutti gli speaker, nell'ordine di dichiarazione del JSON. */
    fun allSpeakers(): List<Speaker> = speakersMap.values.toList()

    /** Tutti i programmi, nell'ordine di dichiarazione del JSON. */
    fun allPrograms(): List<Program> = programsList

    /** Risolve gli speaker per un programma (nell'ordine dichiarato negli speakerIds). */
    fun speakersOf(program: Program): List<Speaker> =
        program.speakerIds.mapNotNull { speakersMap[it] }

    // --- Caricamento ---

    private fun loadSpeakers(): Map<String, Speaker> {
        val text = readRaw(R.raw.speakers)
        val arr = JSONObject(text).getJSONArray("speakers")
        val map = linkedMapOf<String, Speaker>()  // preserva ordine
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val id = obj.getString("id")
            map[id] = Speaker(
                id = id,
                name = obj.getString("name"),
                bio = obj.optString("bio", ""),
                photo = obj.optString("photo", "")
            )
        }
        return map
    }

    private fun loadPrograms(): List<Program> {
        val text = readRaw(R.raw.programs)
        val arr = JSONObject(text).getJSONArray("programs")
        val out = mutableListOf<Program>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            out += Program(
                id = obj.getString("id"),
                title = obj.getString("title"),
                speakerIds = jsonStringList(obj.optJSONArray("speakerIds")),
                description = obj.optString("description", ""),
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
            out += TimeSlot(
                days = parseDays(obj),
                start = LocalTime.parse(obj.getString("start")),
                durationMinutes = obj.getInt("durationMinutes")
            )
        }
        return out
    }

    private fun parseDays(obj: JSONObject): List<DayOfWeek> {
        if (obj.has("day")) {
            val s = obj.getString("day").uppercase(Locale.ROOT)
            return if (s == "DAILY") DayOfWeek.values().toList()
                   else listOf(DayOfWeek.valueOf(s))
        }
        return when (val v = obj.opt("days")) {
            is String -> {
                val s = v.uppercase(Locale.ROOT)
                if (s == "DAILY") DayOfWeek.values().toList()
                else listOf(DayOfWeek.valueOf(s))
            }
            is JSONArray -> jsonStringList(v).map {
                DayOfWeek.valueOf(it.uppercase(Locale.ROOT))
            }
            else -> emptyList()
        }
    }

    private fun jsonStringList(arr: JSONArray?): List<String> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).map { arr.getString(it) }
    }

    private fun readRaw(resId: Int): String =
        context.resources.openRawResource(resId)
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
}

/** Helper per risolvere R.drawable.{photo} a runtime; ritorna 0 se non trovato. */
fun Context.resolveDrawable(name: String): Int =
    if (name.isBlank()) 0 else resources.getIdentifier(name, "drawable", packageName)
