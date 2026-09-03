package com.example

import com.example.network.HostRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {
    private lateinit var repository: HostRepository

    @Before
    fun setup() {
        repository = HostRepository()
    }

    @Test
    fun testDoubleVerificationMismatchRejection() = runBlocking {
        val roomRes = repository.submitOmbRoomDetails(
            ombId = "OMB-1042",
            firstRoomId = "551122",
            firstPassword = "pass",
            secondRoomId = "551123", // mismatch
            secondPassword = "pass"
        )
        assertTrue(roomRes.isFailure)
        assertTrue(roomRes.exceptionOrNull()?.message?.contains("entered information correctly") == true)
    }

    @Test
    fun testPositionDoubleVerificationMismatchRejection() = runBlocking {
        val posRes = repository.assignParticipantPosition(
            ombId = "OMB-1042",
            participantId = "P-101",
            firstPosition = 1,
            secondPosition = 2 // mismatch
        )
        assertTrue(posRes.isFailure)
        assertTrue(posRes.exceptionOrNull()?.message?.contains("entered information correctly") == true)
    }

    @Test
    fun testTournamentMetricDoubleVerificationMismatchRejection() = runBlocking {
        val metricRes = repository.saveTournamentInitialValue(
            tournamentId = "T-2001",
            participantId = "TP-101",
            firstVal = 10,
            secondVal = 12 // mismatch
        )
        assertTrue(metricRes.isFailure)
        assertTrue(metricRes.exceptionOrNull()?.message?.contains("entered information correctly") == true)
    }

    @Test
    fun testReleaseExactWordRequirement() = runBlocking {
        val failRelease = repository.releaseAssignment("OMB-1042", "OMB", "wrong_text")
        assertTrue(failRelease.isFailure)
        assertTrue(failRelease.exceptionOrNull()?.message?.contains("type 'release' exactly") == true)
    }
}


