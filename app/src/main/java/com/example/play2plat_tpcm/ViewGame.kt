package com.ddkric.play2plat

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ddkric.play2plat.api.ApiManager
import com.ddkric.play2plat.api.GameInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.squareup.picasso.Picasso

class ViewGame : AppCompatActivity() {
    private lateinit var nameTextView: TextView
    private lateinit var companyTextView: TextView
    private lateinit var genresTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var gameImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_game)

        // Inicialize as views
        nameTextView = findViewById(R.id.name)
        companyTextView = findViewById(R.id.company)
        genresTextView = findViewById(R.id.genres)
        descriptionTextView = findViewById(R.id.api_description)
        gameImageView = findViewById(R.id.game)

        // Obtenha o ID do jogo da intenção que iniciou esta atividade
        val gameId = intent.getIntExtra("gameId", 19)
        if (gameId == 19) {
            ApiManager.apiService.getGameById(gameId).enqueue(object : Callback<GameInfo> {
                override fun onResponse(call: Call<GameInfo>, response: Response<GameInfo>) {
                    if (response.isSuccessful) {
                        val game = response.body()
                        if (game != null) {

                            // Atualize as views com os dados do jogo
                            nameTextView.text = game.name
                            companyTextView.text = game.company
                            genresTextView.text = game.genres.joinToString(", ")
                            descriptionTextView.text = game.description
                            Picasso.get()
                                .load(game.coverImage)
                                .into(gameImageView)

                        } else {
                            Log.e("ViewGame", "Resposta da API não retornou dados do jogo.")
                        }
                    } else {
                        // Manipular erro de resposta da API
                        Log.e("ViewGame", "Erro na resposta da API: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<GameInfo>, t: Throwable) {
                    // Lidar com falha na solicitação
                }
            })
        }
    }
}
