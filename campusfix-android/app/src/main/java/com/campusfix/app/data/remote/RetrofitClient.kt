package com.campusfix.app.data.remote

import com.campusfix.app.data.remote.api.AuthApiService
import com.campusfix.app.data.remote.api.BuildingAdminApiService
import com.campusfix.app.data.remote.api.CampusAdminApiService
import com.campusfix.app.data.remote.api.StaffApiService
import com.campusfix.app.data.remote.api.StudentApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Base URL WITH trailing slash - Required by Retrofit when using absolute paths
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApiService: AuthApiService = retrofit.create(AuthApiService::class.java)
    val campusAdminApiService: CampusAdminApiService = retrofit.create(CampusAdminApiService::class.java)
    val buildingAdminApiService: BuildingAdminApiService = retrofit.create(BuildingAdminApiService::class.java)
    val staffApiService: StaffApiService = retrofit.create(StaffApiService::class.java)
    val studentApiService: StudentApiService = retrofit.create(StudentApiService::class.java)
}

