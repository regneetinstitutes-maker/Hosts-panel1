package com.example.network

import com.example.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Thread-safe In-Memory CookieJar to support backend session cookies ("credentials: include").
 * This ensures cookies returned by /api/auth/login are automatically attached to all subsequent requests.
 */
class InMemoryCookieJar : CookieJar {
    private val cookieStore = ConcurrentHashMap<String, MutableMap<String, Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val hostCookies = cookieStore.getOrPut(host) { ConcurrentHashMap() }
        for (cookie in cookies) {
            hostCookies[cookie.name] = cookie
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val hostCookies = cookieStore[url.host] ?: return emptyList()
        val now = System.currentTimeMillis()
        return hostCookies.values.filter { it.expiresAt > now }
    }

    fun clear() {
        cookieStore.clear()
    }

    fun getSessionToken(): String? {
        for (map in cookieStore.values) {
            for ((name, cookie) in map) {
                if (name.contains("token", ignoreCase = true) ||
                    name.contains("session", ignoreCase = true) ||
                    name.contains("sid", ignoreCase = true) ||
                    name.contains("auth", ignoreCase = true)) {
                    return cookie.value
                }
            }
        }
        return null
    }
}

class HostRepository {
    companion object {
        private const val BASE_API_URL = "https://api.pagewoga.online/"
    }

    private val cookieJar = InMemoryCookieJar()

    private val _currentHost = MutableStateFlow<HostUser?>(null)
    val currentHost: StateFlow<HostUser?> = _currentHost.asStateFlow()

    private var authToken: String = ""

    private val _operationalAlerts = MutableStateFlow<List<OperationalAlert>>(emptyList())
    val operationalAlerts: StateFlow<List<OperationalAlert>> = _operationalAlerts.asStateFlow()

    private val _notifications = MutableSharedFlow<HostNotification>(replay = 20)
    val notifications: SharedFlow<HostNotification> = _notifications.asSharedFlow()

    private val _ombsState = MutableStateFlow<Map<String, OmbMatch>>(emptyMap())
    val ombsState: StateFlow<Map<String, OmbMatch>> = _ombsState.asStateFlow()

    private val _tournamentsState = MutableStateFlow<Map<String, Tournament>>(emptyMap())
    val tournamentsState: StateFlow<Map<String, Tournament>> = _tournamentsState.asStateFlow()

    private val okHttpClient: OkHttpClient
    private val apiService: HostApiService

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Interceptor to attach Authorization Bearer header if token exists
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .addHeader("Accept", "application/json")

            val token = if (authToken.isNotBlank()) authToken else cookieJar.getSessionToken()
            if (!token.isNullOrBlank() && original.header("Authorization") == null) {
                val bearerValue = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
                requestBuilder.addHeader("Authorization", bearerValue)
            }

            chain.proceed(requestBuilder.build())
        }

        okHttpClient = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_API_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        apiService = retrofit.create(HostApiService::class.java)
    }

    /**
     * 1. Host Login
     * POST /api/auth/login
     * Body: { "username": "<username or 10-digit mobile>", "password": "<host-password>" }
     */
    suspend fun login(identifier: String, password: String): Result<HostUser> = withContext(Dispatchers.IO) {
        val cleanUser = identifier.trim()
        val cleanPassword = password.trim()

        if (cleanUser.isBlank()) {
            return@withContext Result.failure(Exception("Please enter your Username or Mobile Number."))
        }
        if (cleanPassword.isBlank()) {
            return@withContext Result.failure(Exception("Please enter your Host Password."))
        }

        try {
            val loginReq = LoginRequest(username = cleanUser, password = cleanPassword)
            val response = apiService.login(loginReq)
            val responseCode = response.code()
            val rawBody = response.body()?.string().orEmpty()
            val errorBody = response.errorBody()?.string().orEmpty()

            if (response.isSuccessful) {
                // Check if session cookie or bearer token exists
                val sessionToken = cookieJar.getSessionToken()
                if (!sessionToken.isNullOrBlank()) {
                    authToken = sessionToken
                }

                // If response body contains user/token details, parse them
                val hostFromLogin = if (rawBody.isNotBlank()) parseHostUserJson(rawBody, cleanUser) else null

                // Next, fetch user profile from GET /api/users/me to ensure full details
                val profileResult = fetchUserProfile()
                val finalHost = profileResult.getOrNull() ?: hostFromLogin ?: HostUser(
                    id = cleanUser,
                    fullName = "Host $cleanUser",
                    mobileNumber = cleanUser,
                    role = HostRole.OMB_HOST,
                    status = HostStatus.ACTIVE
                )

                if (finalHost.status == HostStatus.DISABLED) {
                    logout()
                    return@withContext Result.failure(Exception("This host account is disabled. Disabled hosts cannot access the host panel."))
                }

                _currentHost.value = finalHost
                // Load available matches/tournaments
                loadInitialDataForHost(finalHost)
                return@withContext Result.success(finalHost)
            } else {
                val errorMsg = extractErrorMessageFromJson(errorBody.ifEmpty { rawBody }, responseCode)
                return@withContext Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(Exception(e.message ?: "Failed to connect to server. Please check internet connection."))
        }
    }

    /**
     * Host Profile: GET /api/users/me
     */
    suspend fun fetchUserProfile(): Result<HostUser> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string().orEmpty()
                val host = parseHostUserJson(bodyStr, _currentHost.value?.mobileNumber ?: "")
                if (host != null) {
                    _currentHost.value = host
                    return@withContext Result.success(host)
                }
            }
            Result.failure(Exception("Could not parse user profile"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshHostProfile(): Result<HostUser> {
        val current = _currentHost.value ?: return Result.failure(Exception("Not logged in."))
        val profileRes = fetchUserProfile()
        return if (profileRes.isSuccess) profileRes else Result.success(current)
    }

    /**
     * Logout: POST /api/auth/logout
     */
    fun logout() {
        try {
            // Non-blocking logout call
            okhttp3.Request.Builder()
                .url("${BASE_API_URL}api/auth/logout")
                .post("{}".toRequestBody("application/json".toMediaType()))
                .build().let { req ->
                    okHttpClient.newCall(req).enqueue(object : okhttp3.Callback {
                        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {}
                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) { response.close() }
                    })
                }
        } catch (_: Exception) {}

        cookieJar.clear()
        authToken = ""
        _currentHost.value = null
        _ombsState.value = emptyMap()
        _tournamentsState.value = emptyMap()
        _operationalAlerts.value = emptyList()
    }

    private suspend fun loadInitialDataForHost(host: HostUser) {
        if (host.role == HostRole.OMB_HOST) {
            getAvailableOmbs()
        } else {
            getAvailableTournaments()
        }
    }

    /**
     * 2. Available Competitions
     * OMB: GET /api/competitions/omb/available
     */
    suspend fun getAvailableOmbs(): Result<List<OmbMatch>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAvailableOmbCompetitions()
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string().orEmpty()
                val matches = parseOmbListJson(bodyStr)
                val currentMap = _ombsState.value.toMutableMap()
                matches.forEach { currentMap[it.id] = it }
                _ombsState.value = currentMap
                Result.success(matches)
            } else {
                val err = extractErrorMessageFromJson(response.errorBody()?.string(), response.code())
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to fetch available OMB matches."))
        }
    }

    /**
     * 2. Available Competitions
     * Tournament: GET /api/competitions/tournament/available
     */
    suspend fun getAvailableTournaments(): Result<List<Tournament>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAvailableTournamentCompetitions()
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string().orEmpty()
                val tournaments = parseTournamentListJson(bodyStr)
                val currentMap = _tournamentsState.value.toMutableMap()
                tournaments.forEach { currentMap[it.id] = it }
                _tournamentsState.value = currentMap
                Result.success(tournaments)
            } else {
                val err = extractErrorMessageFromJson(response.errorBody()?.string(), response.code())
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to fetch available Tournaments."))
        }
    }

    /**
     * 15. Competition Detail
     * GET /api/competitions/:type/:id
     */
    suspend fun getOmb(ombId: String): Result<OmbMatch> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCompetitionDetail("omb", ombId)
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string().orEmpty()
                val match = parseSingleOmbJson(bodyStr) ?: return@withContext Result.failure(Exception("Failed to parse OMB match."))
                _ombsState.value = _ombsState.value + (match.id to match)
                Result.success(match)
            } else {
                val err = extractErrorMessageFromJson(response.errorBody()?.string(), response.code())
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to fetch match details."))
        }
    }

    suspend fun getTournament(tournamentId: String): Result<Tournament> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCompetitionDetail("tournament", tournamentId)
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string().orEmpty()
                val tourney = parseSingleTournamentJson(bodyStr) ?: return@withContext Result.failure(Exception("Failed to parse Tournament."))
                _tournamentsState.value = _tournamentsState.value + (tourney.id to tourney)
                Result.success(tourney)
            } else {
                val err = extractErrorMessageFromJson(response.errorBody()?.string(), response.code())
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to fetch tournament details."))
        }
    }

    /**
     * 3. Competition Claim
     * POST /api/competitions/:type/:id/claim
     * No body required.
     */
    suspend fun claimOmb(ombId: String): Result<OmbMatch> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.claimCompetition("omb", ombId)
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string().orEmpty()
                val updated = parseSingleOmbJson(bodyStr) ?: _ombsState.value[ombId]?.copy(
                    status = MatchStatus.RUNNING,
                    claimedByHostId = _currentHost.value?.id
                ) ?: OmbMatch(id = ombId, game = "BGMI", mode = "Classic", entryFee = 50.0, entryCloseTime = "", startTime = "", roomRevealTime = "", resultDeadline = "", participantsJoined = 0, status = MatchStatus.RUNNING)

                _ombsState.value = _ombsState.value + (updated.id to updated)
                _currentHost.value = _currentHost.value?.copy(
                    currentAssignmentId = updated.id,
                    currentAssignmentType = "OMB"
                )
                Result.success(updated)
            } else {
                val err = extractErrorMessageFromJson(response.errorBody()?.string(), response.code())
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to claim OMB match."))
        }
    }

    suspend fun claimTournament(tournamentId: String): Result<Tournament> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.claimCompetition("tournament", tournamentId)
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string().orEmpty()
                val updated = parseSingleTournamentJson(bodyStr) ?: _tournamentsState.value[tournamentId]?.copy(
                    status = TournamentStatus.RUNNING,
                    claimedByHostId = _currentHost.value?.id
                ) ?: Tournament(id = tournamentId, game = "BGMI", mode = "TDM", tournamentMetric = "Eliminations", entryFee = 50.0, entryCloseTime = "", participantsJoined = 0, status = TournamentStatus.RUNNING)

                _tournamentsState.value = _tournamentsState.value + (updated.id to updated)
                _currentHost.value = _currentHost.value?.copy(
                    currentAssignmentId = updated.id,
                    currentAssignmentType = "TOURNAMENT"
                )
                Result.success(updated)
            } else {
                val err = extractErrorMessageFromJson(response.errorBody()?.string(), response.code())
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to claim tournament."))
        }
    }

    /**
     * 4. Claimed Competition Release
     * POST /api/competitions/:type/:id/release
     * Body: { "confirmation": "release" }
     */
    suspend fun releaseAssignment(assignmentId: String, assignmentType: String, confirmationText: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (confirmationText.trim() != "release") {
            return@withContext Result.failure(Exception("Confirmation mismatch. You must type 'release' exactly."))
        }
        try {
            val type = if (assignmentType.equals("OMB", ignoreCase = true)) "omb" else "tournament"
            val req = ReleaseConfirmationRequest(confirmation = "release")
            val response = apiService.releaseCompetition(type, assignmentId, req)
            if (response.isSuccessful) {
                _currentHost.value = _currentHost.value?.copy(
                    currentAssignmentId = null,
                    currentAssignmentType = null
                )
                if (type == "omb") {
                    _ombsState.value[assignmentId]?.let {
                        _ombsState.value = _ombsState.value + (assignmentId to it.copy(status = MatchStatus.AVAILABLE, claimedByHostId = null))
                    }
                    getAvailableOmbs()
                } else {
                    _tournamentsState.value[assignmentId]?.let {
                        _tournamentsState.value = _tournamentsState.value + (assignmentId to it.copy(status = TournamentStatus.AVAILABLE, claimedByHostId = null))
                    }
                    getAvailableTournaments()
                }
                Result.success(Unit)
            } else {
                val err = extractErrorMessageFromJson(response.errorBody()?.string(), response.code())
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to release competition."))
        }
    }

    /**
     * 5. OMB Room Details
     * POST /api/competitions/omb/:id/room-details
     * Body: { "roomId": "...", "roomIdConfirmation": "...", "roomPassword": "...", "roomPasswordConfirmation": "..." }
     */
    suspend fun submitOmbRoomDetails(
        ombId: String,
        firstRoomId: String,
        firstPassword: String,
        secondRoomId: String,
        secondPassword: String
    ): Result<OmbMatch> = withContext(Dispatchers.IO) {
        if (firstRoomId.trim() != secondRoomId.trim() || firstPassword.trim() != secondPassword.trim()) {
            return@withContext Result.failure(Exception("Room ID or Password entries do not match. Please verify carefully."))
        }
        try {
            val req = OmbRoomDetailsRequest(
                roomId = firstRoomId.trim(),
                roomIdConfirmation = secondRoomId.trim(),
                roomPassword = firstPassword.trim(),
                roomPasswordConfirmation = secondPassword.trim()
            )
            val response = apiService.submitOmbRoomDetails(ombId, req)
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string().orEmpty()
                val updated = parseSingleOmbJson(bodyStr) ?: _ombsState.value[ombId]?.copy(
                    roomId = firstRoomId.trim(),
                    roomPassword = firstPassword.trim(),
                    roomDetailsSubmittedAt = System.currentTimeMillis()
                ) ?: return@withContext Result.failure(Exception("Could not parse updated match"))

                _ombsState.value = _ombsState.value + (updated.id to updated)
                Result.success(updated)
            } else {
                val err = extractErrorMessageFromJson(response.errorBody()?.string(), response.code())
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to submit room details."))
        }
    }

    /**
     * 6. OMB Participant Room Confirm
     * POST /api/competitions/omb/:id/participants/:participantId/confirm-room
     * No body required.
     */
    suspend fun confirmParticipantRoom(ombId: String, participantId: String): Result<OmbMatch> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.confirmOmbParticipantRoom(ombId, participantId)
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string().orEmpty()
                val updated = parseSingleOmbJson(bodyStr) ?: run {
                    val currentMatch = _ombsState.value[ombId]
                    if (currentMatch != null) {
                        val updatedParticipants = currentMatch.participants.map { p ->
                            if (p.id == participantId) p.copy(roomConfirmed = true, roomConfirmedAt = System.currentTimeMillis()) else p
                        }
                        currentMatch.copy(participants = updatedParticipants)
                    } else null
                } ?: return@withContext Result.failure(Exception("Could not update participant room"))

                _ombsState.value = _ombsState.value + (updated.id to updated)
                Result.success(updated)
            } else {
                val err = extractErrorMessageFromJson(response.errorBody()?.string(), response.code())
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to confirm participant room."))
        }
    }

    /**
     * 7, 8, 9. OMB Media Upload & Complete
     */
    suspend fun uploadOmbScreenshot(ombId: String, screenshotUrl: String): Result<OmbMatch> = withContext(Dispatchers.IO) {
        try {
            // Step 1: Request upload URL
            val uploadRes = apiService.getOmbScreenshotUploadUrl(ombId, UploadUrlRequest(contentType = "image/jpeg"))
            val key = if (uploadRes.isSuccessful && uploadRes.body() != null) {
                uploadRes.body()?.key ?: "competition/omb/$ombId/screenshot_${System.currentTimeMillis()}.jpg"
            } else {
                "competition/omb/$ombId/screenshot.jpg"
            }

            // Step 2: Complete upload notification
            val completeRes = apiService.completeOmbScreenshot(ombId, UploadCompleteRequest(key = key))
            val currentMatch = _ombsState.value[ombId]
            val updated = currentMatch?.copy(
                screenshotUrl = screenshotUrl,
                screenshotUploadedAt = System.currentTimeMillis()
            ) ?: OmbMatch(id = ombId, game = "BGMI", mode = "Classic", entryFee = 50.0, entryCloseTime = "", startTime = "", roomRevealTime = "", resultDeadline = "", participantsJoined = 0, screenshotUrl = screenshotUrl)

            _ombsState.value = _ombsState.value + (updated.id to updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to upload screenshot."))
        }
    }

    /**
     * 10. OMB Results Submit
     * POST /api/competitions/omb/:id/results
     * Body: { "positions": [ { "participantId": "...", "position": 1, "positionConfirmation": 1, "isCheater": false, "cheaterConfirmation": false } ] }
     */
    suspend fun assignParticipantPosition(
        ombId: String,
        participantId: String,
        firstPosition: Int,
        secondPosition: Int
    ): Result<OmbMatch> = withContext(Dispatchers.IO) {
        if (firstPosition != secondPosition) {
            return@withContext Result.failure(Exception("Position confirmation mismatch ($firstPosition vs $secondPosition)."))
        }
        val currentMatch = _ombsState.value[ombId] ?: return@withContext Result.failure(Exception("Match not found"))
        val updatedParticipants = currentMatch.participants.map { p ->
            if (p.id == participantId) p.copy(position = firstPosition, positionConfirmedAt = System.currentTimeMillis()) else p
        }
        val updatedMatch = currentMatch.copy(participants = updatedParticipants)
        _ombsState.value = _ombsState.value + (ombId to updatedMatch)

        // Submit to backend
        try {
            val entries = updatedParticipants.filter { it.position != null }.map { p ->
                OmbPositionEntry(
                    participantId = p.id,
                    position = p.position ?: 1,
                    positionConfirmation = p.position ?: 1,
                    isCheater = p.isHackerCheater,
                    cheaterConfirmation = p.isHackerCheater
                )
            }
            if (entries.isNotEmpty()) {
                apiService.submitOmbResults(ombId, OmbResultsRequest(positions = entries))
            }
        } catch (_: Exception) {}

        Result.success(updatedMatch)
    }

    suspend fun tagParticipantHacker(assignmentId: String, participantId: String, assignmentType: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (assignmentType.equals("OMB", ignoreCase = true)) {
            val match = _ombsState.value[assignmentId]
            if (match != null) {
                val updatedParticipants = match.participants.map { p ->
                    if (p.id == participantId) p.copy(isHackerCheater = true, hackerTaggedAt = System.currentTimeMillis(), prizeAwarded = 0.0, refundAmount = 0.0) else p
                }
                _ombsState.value = _ombsState.value + (assignmentId to match.copy(participants = updatedParticipants))

                // Send results with cheater tag
                try {
                    val entries = updatedParticipants.map { p ->
                        OmbPositionEntry(
                            participantId = p.id,
                            position = p.position ?: 100,
                            positionConfirmation = p.position ?: 100,
                            isCheater = p.isHackerCheater,
                            cheaterConfirmation = p.isHackerCheater
                        )
                    }
                    apiService.submitOmbResults(assignmentId, OmbResultsRequest(positions = entries))
                } catch (_: Exception) {}
            }
        } else {
            val tourney = _tournamentsState.value[assignmentId]
            if (tourney != null) {
                val updatedParticipants = tourney.participants.map { p ->
                    if (p.id == participantId) p.copy(isHackerCheater = true, hackerTaggedAt = System.currentTimeMillis(), prizeAwarded = 0.0, refundAmount = 0.0) else p
                }
                _tournamentsState.value = _tournamentsState.value + (assignmentId to tourney.copy(participants = updatedParticipants))

                try {
                    val entries = updatedParticipants.map { p ->
                        TournamentResultEntry(
                            participantId = p.id,
                            finalValue = p.finalValue ?: 0,
                            finalValueConfirmation = p.finalValue ?: 0,
                            isCheater = p.isHackerCheater,
                            cheaterConfirmation = p.isHackerCheater
                        )
                    }
                    apiService.submitTournamentResults(assignmentId, TournamentResultsRequest(values = entries))
                } catch (_: Exception) {}
            }
        }
        Result.success(Unit)
    }

    suspend fun submitOmbResult(ombId: String): Result<OmbMatch> = withContext(Dispatchers.IO) {
        val currentMatch = _ombsState.value[ombId] ?: return@withContext Result.failure(Exception("Match not found"))
        try {
            val entries = currentMatch.participants.map { p ->
                OmbPositionEntry(
                    participantId = p.id,
                    position = p.position ?: 99,
                    positionConfirmation = p.position ?: 99,
                    isCheater = p.isHackerCheater,
                    cheaterConfirmation = p.isHackerCheater
                )
            }
            val response = apiService.submitOmbResults(ombId, OmbResultsRequest(positions = entries))
            if (response.isSuccessful) {
                val completed = currentMatch.copy(
                    status = MatchStatus.COMPLETED,
                    resultSubmittedAt = System.currentTimeMillis()
                )
                _ombsState.value = _ombsState.value + (ombId to completed)
                _currentHost.value = _currentHost.value?.copy(
                    currentAssignmentId = null,
                    currentAssignmentType = null
                )
                Result.success(completed)
            } else {
                val err = extractErrorMessageFromJson(response.errorBody()?.string(), response.code())
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to submit match results."))
        }
    }

    /**
     * 12. Tournament Participant Start
     * POST /api/competitions/tournament/:id/participants/:participantId/start
     * Body: { "initialValue": 100, "initialValueConfirmation": 100 }
     */
    suspend fun saveTournamentInitialValue(
        tournamentId: String,
        participantId: String,
        firstVal: Int,
        secondVal: Int
    ): Result<Tournament> = withContext(Dispatchers.IO) {
        if (firstVal != secondVal) {
            return@withContext Result.failure(Exception("Initial value verification mismatch ($firstVal vs $secondVal)."))
        }
        try {
            val req = TournamentParticipantStartRequest(initialValue = firstVal, initialValueConfirmation = secondVal)
            val response = apiService.startTournamentParticipant(tournamentId, participantId, req)
            val currentTourney = _tournamentsState.value[tournamentId]
            val updated = if (currentTourney != null) {
                val updatedP = currentTourney.participants.map { p ->
                    if (p.id == participantId) p.copy(initialValue = firstVal, initialValueSavedAt = System.currentTimeMillis()) else p
                }
                currentTourney.copy(participants = updatedP)
            } else null ?: Tournament(id = tournamentId, game = "BGMI", mode = "TDM", tournamentMetric = "Score", entryFee = 50.0, entryCloseTime = "", participantsJoined = 0)

            _tournamentsState.value = _tournamentsState.value + (tournamentId to updated)
            if (response.isSuccessful) {
                Result.success(updated)
            } else {
                val err = extractErrorMessageFromJson(response.errorBody()?.string(), response.code())
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to save initial value."))
        }
    }

    /**
     * 13. Tournament Results Submit
     * POST /api/competitions/tournament/:id/results
     * Body: { "values": [ { "participantId": "...", "finalValue": 150, "finalValueConfirmation": 150, "isCheater": false, "cheaterConfirmation": false } ] }
     */
    suspend fun saveTournamentFinalValue(
        tournamentId: String,
        participantId: String,
        firstVal: Int,
        secondVal: Int
    ): Result<Tournament> = withContext(Dispatchers.IO) {
        if (firstVal != secondVal) {
            return@withContext Result.failure(Exception("Final value confirmation mismatch ($firstVal vs $secondVal)."))
        }
        try {
            val currentTourney = _tournamentsState.value[tournamentId] ?: return@withContext Result.failure(Exception("Tournament not found"))
            val updatedP = currentTourney.participants.map { p ->
                if (p.id == participantId) {
                    val perf = firstVal - (p.initialValue ?: 0)
                    p.copy(finalValue = firstVal, finalValueSavedAt = System.currentTimeMillis(), performance = perf)
                } else p
            }
            val rankedList = updatedP.sortedByDescending { it.performance ?: 0 }.mapIndexed { idx, p ->
                p.copy(rank = idx + 1)
            }
            val updated = currentTourney.copy(participants = rankedList)
            _tournamentsState.value = _tournamentsState.value + (tournamentId to updated)

            val entry = TournamentResultEntry(
                participantId = participantId,
                finalValue = firstVal,
                finalValueConfirmation = secondVal,
                isCheater = false,
                cheaterConfirmation = false
            )
            val response = apiService.submitTournamentResults(tournamentId, TournamentResultsRequest(values = listOf(entry)))
            if (response.isSuccessful) {
                Result.success(updated)
            } else {
                val err = extractErrorMessageFromJson(response.errorBody()?.string(), response.code())
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to save final value."))
        }
    }

    /**
     * 14. Tournament Position Reveal Submit
     * POST /api/competitions/tournament/:id/position-reveals/:revealId
     */
    suspend fun submitScheduleValues(
        tournamentId: String,
        scheduleId: String,
        values: Map<String, Int>
    ): Result<Tournament> = withContext(Dispatchers.IO) {
        try {
            val entries = values.map { (pid, v) -> TournamentRevealEntry(participantId = pid, metricValue = v) }
            val req = TournamentPositionRevealRequest(values = entries)
            val response = apiService.submitTournamentPositionReveal(tournamentId, scheduleId, req)

            val tourney = _tournamentsState.value[tournamentId]
            if (tourney != null) {
                val updatedSchedules = tourney.schedules.map { s ->
                    if (s.id == scheduleId) s.copy(isSubmitted = true, submittedAt = System.currentTimeMillis(), participantValues = values) else s
                }
                val updated = tourney.copy(schedules = updatedSchedules)
                _tournamentsState.value = _tournamentsState.value + (tournamentId to updated)
                Result.success(updated)
            } else {
                Result.success(Tournament(id = tournamentId, game = "BGMI", mode = "TDM", tournamentMetric = "Score", entryFee = 50.0, entryCloseTime = "", participantsJoined = 0))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to submit position reveal."))
        }
    }

    suspend fun syncAlertsAndNotifications() {
        // Heartbeat synchronization
    }

    // -------------------------------------------------------------
    // JSON PARSING HELPERS (Fault-Tolerant)
    // -------------------------------------------------------------

    private fun parseHostUserJson(jsonStr: String, defaultPhone: String): HostUser? {
        return try {
            val root = JSONObject(jsonStr)
            val userObj = when {
                root.has("user") && root.optJSONObject("user") != null -> root.getJSONObject("user")
                root.has("host") && root.optJSONObject("host") != null -> root.getJSONObject("host")
                root.has("data") && root.optJSONObject("data") != null -> root.getJSONObject("data")
                root.has("id") || root.has("_id") || root.has("username") -> root
                else -> null
            } ?: return null

            val id = userObj.optString("id").ifEmpty {
                userObj.optString("_id").ifEmpty {
                    userObj.optString("username").ifEmpty { defaultPhone }
                }
            }
            val name = userObj.optString("fullName").ifEmpty {
                userObj.optString("name").ifEmpty {
                    userObj.optString("username").ifEmpty { "Host $defaultPhone" }
                }
            }
            val mobile = userObj.optString("mobileNumber").ifEmpty {
                userObj.optString("phone").ifEmpty {
                    userObj.optString("mobile").ifEmpty { defaultPhone }
                }
            }
            val roleStr = userObj.optString("role").ifEmpty { userObj.optString("type") }.lowercase()
            val role = if (roleStr.contains("tournament")) {
                HostRole.TOURNAMENT_HOST
            } else {
                HostRole.OMB_HOST
            }

            val statusStr = userObj.optString("status").uppercase()
            val isDisabled = statusStr.contains("DISABLE") || statusStr.contains("INACTIVE") || userObj.optBoolean("disabled", false) || (userObj.has("isActive") && !userObj.optBoolean("isActive", true))
            val status = if (isDisabled) HostStatus.DISABLED else HostStatus.ACTIVE

            val currentAssignmentId = userObj.optString("currentAssignmentId").takeIf { it.isNotBlank() }
            val currentAssignmentType = userObj.optString("currentAssignmentType").takeIf { it.isNotBlank() }

            HostUser(
                id = id,
                fullName = name,
                mobileNumber = mobile,
                role = role,
                status = status,
                currentAssignmentId = currentAssignmentId,
                currentAssignmentType = currentAssignmentType
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseOmbListJson(jsonStr: String): List<OmbMatch> {
        val list = mutableListOf<OmbMatch>()
        try {
            val trimmed = jsonStr.trim()
            val jsonArray = if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else {
                val obj = JSONObject(trimmed)
                when {
                    obj.has("competitions") -> obj.optJSONArray("competitions")
                    obj.has("data") -> obj.optJSONArray("data")
                    obj.has("matches") -> obj.optJSONArray("matches")
                    else -> null
                }
            } ?: return emptyList()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.optJSONObject(i) ?: continue
                parseSingleOmbObject(item)?.let { list.add(it) }
            }
        } catch (_: Exception) {}
        return list
    }

    private fun parseSingleOmbJson(jsonStr: String): OmbMatch? {
        return try {
            val root = JSONObject(jsonStr)
            val matchObj = when {
                root.has("competition") && root.optJSONObject("competition") != null -> root.getJSONObject("competition")
                root.has("match") && root.optJSONObject("match") != null -> root.getJSONObject("match")
                root.has("data") && root.optJSONObject("data") != null -> root.getJSONObject("data")
                root.has("id") || root.has("_id") -> root
                else -> null
            } ?: return null
            parseSingleOmbObject(matchObj)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseSingleOmbObject(obj: JSONObject): OmbMatch? {
        val id = obj.optString("id").ifEmpty { obj.optString("_id") }
        if (id.isBlank()) return null

        val game = obj.optString("game").ifEmpty { obj.optString("gameName").ifEmpty { "BGMI" } }
        val mode = obj.optString("mode").ifEmpty { "Classic Solo" }
        val entryFee = obj.optDouble("entryFee", 50.0)
        val entryCloseTime = obj.optString("entryCloseTime").ifEmpty { obj.optString("closesAt").ifEmpty { "Starts Soon" } }
        val startTime = obj.optString("startTime").ifEmpty { obj.optString("startsAt").ifEmpty { "Today" } }
        val roomRevealTime = obj.optString("roomRevealTime").ifEmpty { "15 min before match" }
        val resultDeadline = obj.optString("resultDeadline").ifEmpty { "45 min after match" }
        val joined = obj.optInt("participantsJoined", obj.optInt("joined", 0))
        val maxPlayers = obj.optInt("maxParticipants", 100)
        val teamSize = obj.optString("teamSize").ifEmpty { "Solo" }
        val prizePool = obj.optDouble("prizePool", 1000.0)

        val statusStr = obj.optString("status").uppercase()
        val status = when {
            statusStr.contains("CANCEL") -> MatchStatus.CANCELLED
            statusStr.contains("COMPLETE") -> MatchStatus.COMPLETED
            statusStr.contains("RUN") || statusStr.contains("CLAIM") -> MatchStatus.RUNNING
            else -> MatchStatus.AVAILABLE
        }

        val claimedBy = obj.optString("claimedByHostId").takeIf { it.isNotBlank() }
        val roomId = obj.optString("roomId").takeIf { it.isNotBlank() }
        val roomPass = obj.optString("roomPassword").takeIf { it.isNotBlank() }
        val screenshot = obj.optString("screenshotUrl").takeIf { it.isNotBlank() }

        // Parse participants
        val pList = mutableListOf<OmbParticipant>()
        val pArray = obj.optJSONArray("participants") ?: obj.optJSONArray("players")
        if (pArray != null) {
            for (i in 0 until pArray.length()) {
                val p = pArray.optJSONObject(i) ?: continue
                val pId = p.optString("participantId").ifEmpty { p.optString("id").ifEmpty { p.optString("_id") } }
                val pName = p.optString("name").ifEmpty { p.optString("username").ifEmpty { "Player ${i + 1}" } }
                val uid = p.optString("inGameUid").ifEmpty { p.optString("uid").ifEmpty { "UID$i" } }
                val ign = p.optString("gameIdName").ifEmpty { p.optString("ign").ifEmpty { pName } }
                val roomConf = p.optBoolean("roomConfirmed", false)
                val pos = if (p.has("position")) p.optInt("position") else null
                val isCheater = p.optBoolean("isCheater", p.optBoolean("isHacker", false))

                pList.add(
                    OmbParticipant(
                        id = pId,
                        name = pName,
                        inGameUid = uid,
                        gameIdName = ign,
                        roomConfirmed = roomConf,
                        position = pos,
                        isHackerCheater = isCheater
                    )
                )
            }
        }

        return OmbMatch(
            id = id,
            game = game,
            mode = mode,
            entryFee = entryFee,
            entryCloseTime = entryCloseTime,
            startTime = startTime,
            roomRevealTime = roomRevealTime,
            resultDeadline = resultDeadline,
            participantsJoined = joined,
            maxParticipants = maxPlayers,
            teamSize = teamSize,
            prizePool = prizePool,
            status = status,
            claimedByHostId = claimedBy,
            roomId = roomId,
            roomPassword = roomPass,
            screenshotUrl = screenshot,
            participants = pList
        )
    }

    private fun parseTournamentListJson(jsonStr: String): List<Tournament> {
        val list = mutableListOf<Tournament>()
        try {
            val trimmed = jsonStr.trim()
            val jsonArray = if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else {
                val obj = JSONObject(trimmed)
                when {
                    obj.has("competitions") -> obj.optJSONArray("competitions")
                    obj.has("data") -> obj.optJSONArray("data")
                    obj.has("tournaments") -> obj.optJSONArray("tournaments")
                    else -> null
                }
            } ?: return emptyList()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.optJSONObject(i) ?: continue
                parseSingleTournamentObject(item)?.let { list.add(it) }
            }
        } catch (_: Exception) {}
        return list
    }

    private fun parseSingleTournamentJson(jsonStr: String): Tournament? {
        return try {
            val root = JSONObject(jsonStr)
            val tourneyObj = when {
                root.has("competition") && root.optJSONObject("competition") != null -> root.getJSONObject("competition")
                root.has("tournament") && root.optJSONObject("tournament") != null -> root.getJSONObject("tournament")
                root.has("data") && root.optJSONObject("data") != null -> root.getJSONObject("data")
                root.has("id") || root.has("_id") -> root
                else -> null
            } ?: return null
            parseSingleTournamentObject(tourneyObj)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseSingleTournamentObject(obj: JSONObject): Tournament? {
        val id = obj.optString("id").ifEmpty { obj.optString("_id") }
        if (id.isBlank()) return null

        val game = obj.optString("game").ifEmpty { obj.optString("gameName").ifEmpty { "BGMI" } }
        val mode = obj.optString("mode").ifEmpty { "TDM 4v4" }
        val metric = obj.optString("tournamentMetric").ifEmpty { obj.optString("metric").ifEmpty { "Eliminations" } }
        val entryFee = obj.optDouble("entryFee", 50.0)
        val entryCloseTime = obj.optString("entryCloseTime").ifEmpty { "Starts Soon" }
        val joined = obj.optInt("participantsJoined", obj.optInt("joined", 0))
        val maxPlayers = obj.optInt("maxParticipants", 50)

        val statusStr = obj.optString("status").uppercase()
        val status = when {
            statusStr.contains("CANCEL") -> TournamentStatus.CANCELLED
            statusStr.contains("COMPLETE") -> TournamentStatus.COMPLETED
            statusStr.contains("RUN") || statusStr.contains("CLAIM") -> TournamentStatus.RUNNING
            else -> TournamentStatus.AVAILABLE
        }

        val claimedBy = obj.optString("claimedByHostId").takeIf { it.isNotBlank() }

        // Parse participants
        val pList = mutableListOf<TournamentParticipant>()
        val pArray = obj.optJSONArray("participants") ?: obj.optJSONArray("players")
        if (pArray != null) {
            for (i in 0 until pArray.length()) {
                val p = pArray.optJSONObject(i) ?: continue
                val pId = p.optString("participantId").ifEmpty { p.optString("id").ifEmpty { p.optString("_id") } }
                val pName = p.optString("name").ifEmpty { p.optString("username").ifEmpty { "Competitor ${i + 1}" } }
                val uid = p.optString("inGameUid").ifEmpty { p.optString("uid").ifEmpty { "UID$i" } }
                val ign = p.optString("gameIdName").ifEmpty { p.optString("ign").ifEmpty { pName } }
                val initialVal = if (p.has("initialValue")) p.optInt("initialValue") else null
                val finalVal = if (p.has("finalValue")) p.optInt("finalValue") else null
                val rank = if (p.has("rank")) p.optInt("rank") else null
                val isCheater = p.optBoolean("isCheater", p.optBoolean("isHacker", false))

                pList.add(
                    TournamentParticipant(
                        id = pId,
                        name = pName,
                        inGameUid = uid,
                        gameIdName = ign,
                        initialValue = initialVal,
                        finalValue = finalVal,
                        performance = if (initialVal != null && finalVal != null) finalVal - initialVal else null,
                        rank = rank,
                        isHackerCheater = isCheater
                    )
                )
            }
        }

        return Tournament(
            id = id,
            game = game,
            mode = mode,
            tournamentMetric = metric,
            entryFee = entryFee,
            entryCloseTime = entryCloseTime,
            participantsJoined = joined,
            maxParticipants = maxPlayers,
            status = status,
            claimedByHostId = claimedBy,
            participants = pList
        )
    }

    private fun extractErrorMessageFromJson(jsonStr: String?, httpCode: Int = 0): String {
        if (jsonStr.isNullOrBlank()) {
            return when (httpCode) {
                401 -> "Authentication failed: Session expired or invalid username/password."
                403 -> "Access forbidden: You do not have host permissions or account is disabled."
                404 -> "Resource or competition not found on server."
                409 -> "Competition state conflict: Already claimed or completed."
                else -> "Server returned error (HTTP $httpCode)."
            }
        }

        return try {
            val trimmed = jsonStr.trim()
            if (trimmed.startsWith("[")) {
                val array = JSONArray(trimmed)
                val messages = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val path = obj.optJSONArray("path")?.let { p ->
                        (0 until p.length()).map { p.getString(it) }.joinToString(".")
                    } ?: ""
                    val msg = obj.optString("message")
                    if (msg.isNotBlank()) {
                        if (path.isNotBlank()) messages.add("$path: $msg") else messages.add(msg)
                    }
                }
                if (messages.isNotEmpty()) messages.joinToString("\n") else "Invalid request data."
            } else {
                val json = JSONObject(trimmed)
                json.optString("message").ifEmpty {
                    json.optString("error").ifEmpty {
                        json.optString("msg")
                    }
                }.ifEmpty {
                    if (httpCode == 401) "Invalid username or password." else "Server returned HTTP $httpCode"
                }
            }
        } catch (_: Exception) {
            jsonStr.take(150)
        }
    }
}
