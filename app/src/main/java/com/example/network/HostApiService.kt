package com.example.network

import com.example.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

// 1. Auth Models
data class LoginRequest(
    val username: String,
    val password: String
)

// 4. Release Model
data class ReleaseConfirmationRequest(
    val confirmation: String = "release"
)

// 5. OMB Room Details
data class OmbRoomDetailsRequest(
    val roomId: String,
    val roomIdConfirmation: String,
    val roomPassword: String,
    val roomPasswordConfirmation: String
)

// 7. Media Upload URL
data class UploadUrlRequest(
    val contentType: String
)

data class UploadUrlResponse(
    val uploadUrl: String? = null,
    val url: String? = null,
    val key: String? = null
)

// 8. Media Upload Complete
data class UploadCompleteRequest(
    val key: String
)

// 9. Download URL Response
data class DownloadUrlResponse(
    val downloadUrl: String? = null,
    val url: String? = null
)

// 10. OMB Results
data class OmbPositionEntry(
    val participantId: String,
    val position: Int,
    val positionConfirmation: Int,
    val isCheater: Boolean = false,
    val cheaterConfirmation: Boolean = false
)

data class OmbResultsRequest(
    val positions: List<OmbPositionEntry>
)

// 12. Tournament Participant Start
data class TournamentParticipantStartRequest(
    val initialValue: Int,
    val initialValueConfirmation: Int
)

// 13. Tournament Results
data class TournamentResultEntry(
    val participantId: String,
    val finalValue: Int,
    val finalValueConfirmation: Int,
    val isCheater: Boolean = false,
    val cheaterConfirmation: Boolean = false
)

data class TournamentResultsRequest(
    val values: List<TournamentResultEntry>
)

// 14. Tournament Position Reveal
data class TournamentRevealEntry(
    val participantId: String,
    val metricValue: Int
)

data class TournamentPositionRevealRequest(
    val values: List<TournamentRevealEntry>
)

interface HostApiService {

    // 1. Host Login
    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): Response<ResponseBody>

    // Host Profile
    @GET("api/users/me")
    suspend fun getCurrentUser(): Response<ResponseBody>

    // Logout
    @POST("api/auth/logout")
    suspend fun logout(): Response<ResponseBody>

    // 2. Available Competitions
    @GET("api/competitions/omb/available")
    suspend fun getAvailableOmbCompetitions(): Response<ResponseBody>

    @GET("api/competitions/tournament/available")
    suspend fun getAvailableTournamentCompetitions(): Response<ResponseBody>

    // 15. Competition Detail
    @GET("api/competitions/{type}/{id}")
    suspend fun getCompetitionDetail(
        @Path("type") type: String,
        @Path("id") id: String
    ): Response<ResponseBody>

    // 3. Competition Claim
    @POST("api/competitions/{type}/{id}/claim")
    suspend fun claimCompetition(
        @Path("type") type: String,
        @Path("id") id: String
    ): Response<ResponseBody>

    // 4. Claimed Competition Release
    @POST("api/competitions/{type}/{id}/release")
    suspend fun releaseCompetition(
        @Path("type") type: String,
        @Path("id") id: String,
        @Body req: ReleaseConfirmationRequest
    ): Response<ResponseBody>

    // 5. OMB Room Details
    @POST("api/competitions/omb/{id}/room-details")
    suspend fun submitOmbRoomDetails(
        @Path("id") id: String,
        @Body req: OmbRoomDetailsRequest
    ): Response<ResponseBody>

    // 6. OMB Participant Room Confirm
    @POST("api/competitions/omb/{id}/participants/{participantId}/confirm-room")
    suspend fun confirmOmbParticipantRoom(
        @Path("id") id: String,
        @Path("participantId") participantId: String
    ): Response<ResponseBody>

    // 7. OMB Screenshot / Voice-note Upload URL
    @POST("api/competitions/omb/{id}/screenshot/upload-url")
    suspend fun getOmbScreenshotUploadUrl(
        @Path("id") id: String,
        @Body req: UploadUrlRequest
    ): Response<UploadUrlResponse>

    @POST("api/competitions/omb/{id}/voice-note/upload-url")
    suspend fun getOmbVoiceNoteUploadUrl(
        @Path("id") id: String,
        @Body req: UploadUrlRequest
    ): Response<UploadUrlResponse>

    // 8. OMB File Upload Complete
    @POST("api/competitions/omb/{id}/screenshot/complete")
    suspend fun completeOmbScreenshot(
        @Path("id") id: String,
        @Body req: UploadCompleteRequest
    ): Response<ResponseBody>

    @POST("api/competitions/omb/{id}/voice-note/complete")
    suspend fun completeOmbVoiceNote(
        @Path("id") id: String,
        @Body req: UploadCompleteRequest
    ): Response<ResponseBody>

    // 9. OMB Media Download URL
    @GET("api/competitions/omb/{id}/screenshot/download-url")
    suspend fun getOmbScreenshotDownloadUrl(
        @Path("id") id: String
    ): Response<DownloadUrlResponse>

    @GET("api/competitions/omb/{id}/voice-note/download-url")
    suspend fun getOmbVoiceNoteDownloadUrl(
        @Path("id") id: String
    ): Response<DownloadUrlResponse>

    // 10. OMB Results Submit
    @POST("api/competitions/omb/{id}/results")
    suspend fun submitOmbResults(
        @Path("id") id: String,
        @Body req: OmbResultsRequest
    ): Response<ResponseBody>

    // 11. Tournament Participants
    @GET("api/competitions/tournament/{id}/participants")
    suspend fun getTournamentParticipants(
        @Path("id") id: String
    ): Response<ResponseBody>

    // 12. Tournament Participant Start
    @POST("api/competitions/tournament/{id}/participants/{participantId}/start")
    suspend fun startTournamentParticipant(
        @Path("id") id: String,
        @Path("participantId") participantId: String,
        @Body req: TournamentParticipantStartRequest
    ): Response<ResponseBody>

    // 13. Tournament Results Submit
    @POST("api/competitions/tournament/{id}/results")
    suspend fun submitTournamentResults(
        @Path("id") id: String,
        @Body req: TournamentResultsRequest
    ): Response<ResponseBody>

    // 14. Tournament Position Reveal Submit
    @POST("api/competitions/tournament/{id}/position-reveals/{revealId}")
    suspend fun submitTournamentPositionReveal(
        @Path("id") id: String,
        @Path("revealId") revealId: String,
        @Body req: TournamentPositionRevealRequest
    ): Response<ResponseBody>
}
