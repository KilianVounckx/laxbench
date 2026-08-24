package io.github.kilianvounckx.laxbench.domain

import kotlin.time.Duration

/**
 * One foul's own countdown duration, tracked as a fully separate entry even when queued behind
 * another foul for the same player (see [PlayerFoulTimers]) -- required so a future feature can
 * still see each foul's individual timer, even though the UI only ever shows one combined total.
 * [id] uniquely identifies this entry within its player's queue (e.g. to cancel one specific queued
 * foul without touching the others); assigning unique, ever-increasing ids is the responsibility of
 * whoever creates entries (see `FoulTimerViewModel`) -- this type does not enforce uniqueness
 * itself, the same way [PlayerNumber] does not enforce that no two players share a number.
 */
data class FoulTimerEntry(val id: Long, val duration: Duration)
