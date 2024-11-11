package com.example.play2plat_tpcm.api

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date
import com.auth0.android.jwt.JWT
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ApiManager {
    // Adicione a sua chave API
    private const val API_KEY = "a75cfc9e4102e2830343787f2e0f3b939f877b8d7b1f2c1a9fdd07d0d3e0542c5beed6c0e5b80eb7cd8b57ab8cbcb9dbb3b4f8b06ad86ad4fe2b7b302d907d7e"
    private var jwtToken: String? = null
    private var refreshToken: String? = null

    // Define o interceptor de forma estática
    private val interceptor = Interceptor { chain ->
        var request = chain.request()

        request = request.newBuilder()
            .header("x-api-key", API_KEY)  // Adiciona a chave da API
            .build()

        // Verifique se o JWT expirou e, se sim, faça o refresh
        if (isTokenExpired(jwtToken)) {
            refreshToken?.let {
                refreshAccessToken(it) // Faz a renovação do token
            }
        }

        // Agora adicione o header de autenticação
        jwtToken?.let {
            request = request.newBuilder()
                .header("Authorization", "Bearer $it")
                .build()
        }

        chain.proceed(request)
    }

    // Configura o OkHttpClient com o interceptor
    private val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    private const val BASE_URL = "https://play2-plat-tpcm.vercel.app/api/"
    //private const val BASE_URL = "http://10.0.2.2:3001/api/"

    private val gson = GsonBuilder()
        .serializeNulls()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    // Função para definir o token JWT
    fun setJwtToken(token: String) {
        jwtToken = token
    }

    // Função para definir o refresh token
    fun setRefreshToken(token: String) {
        refreshToken = token
    }
    fun isTokenExpired(jwt: String?): Boolean {
        jwt?.let {
            val decoded = JWT(it)
            val expiration = decoded.expiresAt
            val now = Date()
            return expiration!!.before(now)
        }
        return true
    }

    fun refreshAccessToken(refreshToken: String) {
        val refreshTokenBody = RefreshTokenBody(refreshToken)

        apiService.refreshToken(refreshTokenBody).enqueue(object : Callback<RefreshTokenResponse> {
            override fun onResponse(call: Call<RefreshTokenResponse>, response: Response<RefreshTokenResponse>) {
                if (response.isSuccessful) {
                    val newAccessToken = response.body()?.accessToken
                    if (newAccessToken != null) {
                        // Atualiza o JWT com o novo token
                        setJwtToken(newAccessToken)
                    }
                } else {
                    // Lide com a falha na renovação do token, por exemplo, fazer logout ou pedir novo login
                    Log.e("ApiManager", "Refresh token failed")
                }
            }

            override fun onFailure(call: Call<RefreshTokenResponse>, t: Throwable) {
                Log.e("ApiManager", "Error refreshing token", t)
            }
        })
    }
}
