package com.example.play2plat

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var infoTextView: TextView
    private lateinit var emailEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var backIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        imageView = findViewById(R.id.imageView)
        infoTextView = findViewById(R.id.tv_info)
        emailEditText = findViewById(R.id.recover_email)
        submitButton = findViewById(R.id.btn_sign_in)
        backIcon = findViewById(R.id.back_icon)

        submitButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                // Chama a função para enviar a solicitação de recuperação de senha
                submitForgetPasswordRequest(email)
            } else {
                Toast.makeText(this, R.string.enter_email_warning, Toast.LENGTH_SHORT).show()
            }
        }

        backIcon.setOnClickListener {
            onBackPressed()
        }
    }

    private fun submitForgetPasswordRequest(email: String) {
        // Define a linguagem com base nas preferências do usuário
        val language = getPreferredLanguage()

        // Monta o JSON para enviar na requisição
        val json = JSONObject().apply {
            put("recipient", email)
            put("language", language)
        }

        Log.d("ForgetPasswordActivity", "JSON Enviado: $json")

        // Configura o cliente e a requisição HTTP
        val client = OkHttpClient()
        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        // Verifica o conteúdo do requestBody
        Log.d("ForgetPasswordActivity", "RequestBody Enviado: $requestBody")

        val request = Request.Builder()
            .url("https://api-flask-play2plat.vercel.app/send-email")  // Substitua pelo URL correto
            .post(requestBody)
            .build()

        Log.d("ForgetPasswordActivity", "URL da Requisição: ${request.url}")

        // Faz a requisição de maneira assíncrona
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ForgetPasswordActivity", "Falha na requisição: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@ForgetPasswordActivity, R.string.email_send_failure, Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Loga o código e o corpo da resposta para verificar o que está acontecendo
                Log.d("ForgetPasswordActivity", "Código da Resposta: ${response.code}")
                Log.d("ForgetPasswordActivity", "Corpo da Resposta: ${response.body?.string()}")

                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@ForgetPasswordActivity, R.string.forget_password_request_submitted, Toast.LENGTH_LONG).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ForgetPasswordActivity, R.string.email_send_failure, Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun getPreferredLanguage(): String {
        // Obtém o idioma selecionado nas preferências
        val sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val selectedLocale = sharedPreferences.getString("selected_locale", "auto") ?: "auto"

        return when (selectedLocale) {
            "en" -> "en"
            "pt" -> "pt"
            "auto" -> {
                // Define o idioma com base na configuração do sistema
                val systemLocale = resources.configuration.locales[0].language
                if (systemLocale == "pt") "pt" else "en"
            }
            else -> "en" // Padrão para inglês se o valor for inesperado
        }
    }
}
