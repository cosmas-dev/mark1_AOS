package com.cosmasbio.mark1.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

// --- DTO (서버 스키마와 snake_case 로 일치) ---

@Serializable
data class LoginRequestDto(val username: String, val password: String)

@Serializable
data class TokenResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Int = 0,
)

@Serializable
data class PersonCreateDto(
    @SerialName("person_id") val personId: String,
    val name: String,
    @SerialName("date_of_birth") val dateOfBirth: String = "",
    val email: String = "",
    @SerialName("phone_number") val phoneNumber: String = "",
    val organization: String = "",
)

@Serializable
data class PersonOutDto(@SerialName("person_id") val personId: String)

@Serializable
data class CaptureCreateDto(
    @SerialName("capture_id") val captureId: String,
    @SerialName("person_id") val personId: String? = null,
    @SerialName("device_id") val deviceId: String? = null,
    @SerialName("captured_at") val capturedAt: String,
    @SerialName("image_sha256") val imageSha256: String? = null,
    @SerialName("image_content_type") val imageContentType: String = "image/jpeg",
    @SerialName("test_type") val testType: String = "",
)

@Serializable
data class CaptureOutDto(
    @SerialName("capture_id") val captureId: String,
    @SerialName("sync_status") val syncStatus: String = "",
)

@Serializable
data class AnalysisUploadDto(
    @SerialName("test_type") val testType: String = "",
    @SerialName("test_info") val testInfo: String = "",
    @SerialName("image_width") val imageWidth: Int = 0,
    @SerialName("image_height") val imageHeight: Int = 0,
    @SerialName("roi_x") val roiX: Int = 0,
    @SerialName("roi_y") val roiY: Int = 0,
    @SerialName("roi_w") val roiW: Int = 0,
    @SerialName("roi_h") val roiH: Int = 0,
    @SerialName("channel_name") val channelName: String = "",
    @SerialName("noise_sigma") val noiseSigma: Double = 0.0,
    @SerialName("c_position") val cPosition: Int? = null,
    @SerialName("c_snr") val cSnr: Double? = null,
    @SerialName("t_position") val tPosition: Int? = null,
    @SerialName("t_snr") val tSnr: Double? = null,
    @SerialName("t_detected") val tDetected: Boolean = false,
    @SerialName("t_weak") val tWeak: Boolean = false,
    @SerialName("h1_split_valid") val h1SplitValid: Boolean = false,
    @SerialName("peak_separation_px") val peakSeparationPx: Double = 0.0,
    @SerialName("num_peaks") val numPeaks: Int = 0,
    @SerialName("raw_json") val rawJson: String = "",
)

@Serializable
data class AnalysisOutDto(
    @SerialName("analysis_id") val analysisId: String,
    val result: String = "",
)

// --- Retrofit API ---

interface Mark1Api {
    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequestDto): TokenResponseDto

    @POST("api/v1/persons")
    suspend fun createPerson(@Body body: PersonCreateDto): PersonOutDto

    @POST("api/v1/captures")
    suspend fun createCapture(@Body body: CaptureCreateDto): CaptureOutDto

    @Multipart
    @POST("api/v1/captures/{captureId}/image")
    suspend fun uploadImage(
        @Path("captureId") captureId: String,
        @Part file: MultipartBody.Part,
    ): CaptureOutDto

    @POST("api/v1/captures/{captureId}/analysis")
    suspend fun uploadAnalysis(
        @Path("captureId") captureId: String,
        @Body body: AnalysisUploadDto,
    ): AnalysisOutDto

    @GET("api/v1/auth/me")
    suspend fun me(): PersonOutDto
}

object ApiClient {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    /**
     * 동기화 1회 실행마다 로그인 토큰을 새로 받아 사용한다.
     * (액세스 토큰 수명이 짧으므로 저장하지 않는다)
     */
    fun create(baseUrl: String, accessToken: String? = null): Mark1Api {
        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)

        if (accessToken != null) {
            clientBuilder.addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer $accessToken")
                        .build()
                )
            }
        }

        return Retrofit.Builder()
            .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
            .client(clientBuilder.build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(Mark1Api::class.java)
    }
}
