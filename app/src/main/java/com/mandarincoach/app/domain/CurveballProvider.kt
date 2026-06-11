package com.mandarincoach.app.domain

import java.time.LocalDate
import kotlin.random.Random

/**
 * The 20% "exploration hook": the first session of each day opens with a
 * surprise challenge to break the native-language autopilot before
 * routine practice begins.
 */
object CurveballProvider {

    private val CURVEBALLS = listOf(
        "A nightclub bouncer says the club is full. The student must talk their way (or charm their way) inside in a few short sentences.",
        "The student's phone is at 1% and they must tell you — a hotel clerk — their room problem before it dies.",
        "You are a taxi driver who misheard the destination and is driving the wrong way. The student must correct you, fast.",
        "You are a street-food vendor with only one portion left and another customer eyeing it. The student must claim it.",
        "The student just got on the wrong high-speed train. You are the conductor. They must explain and ask what to do.",
        "You are a neighbor knocking because the student's music is too loud. They must apologize and win you over.",
        "The student left their wallet at the bar last night. You are the bartender answering the phone.",
        "You are a market stall owner quoting triple the fair price. The student must haggle you down.",
        "The student's boss (you) just asked them to summarize, in Mandarin, what they did this week — on the spot.",
        "You are airport staff: the gate closes in five minutes and the student is at the wrong terminal. They must ask the fastest way.",
        "A friend's grandmother (you) keeps refilling the student's plate. They must refuse politely — but firmly.",
        "You are a delivery rider with the wrong order at the door. The student must sort it out before you ride off.",
        "The student must convince you — a skeptical friend — to lend them 100块 by tonight, no explanations in English allowed.",
        "You are a hairdresser holding the scissors and proposing something drastic. The student must steer the haircut to safety.",
        "It's pouring rain and the last umbrella in the shop has no price tag. You are the shopkeeper. The student must close the deal."
    )

    /**
     * Returns a curveball brief for the model on the first session of the
     * day, null afterwards. Seeding with the day keeps the pick stable
     * across re-opens on the same date.
     */
    fun curveball(
        today: LocalDate,
        lastCurveballEpochDay: Long,
        random: Random = Random(today.toEpochDay())
    ): String? {
        if (today.toEpochDay() <= lastCurveballEpochDay) return null
        return CURVEBALLS[random.nextInt(CURVEBALLS.size)]
    }
}
