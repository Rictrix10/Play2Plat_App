package com.ddkric.play2plat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import com.ddkric.play2plat.api.ApiManager
import com.ddkric.play2plat.api.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class User_Profile_Fragment : Fragment() {

    private lateinit var usernameTextView: TextView
    private lateinit var profileImageView: ImageView
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getInt(ARG_USER_ID)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usernameTextView = view.findViewById(R.id.username)
        profileImageView = view.findViewById(R.id.profile_picture)

        ApiManager.apiService.getUserById(userId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        // Atualize as views com os dados do utilizador
                        usernameTextView.text = user.username
                        loadImage(user.avatar)
                    } else {
                        Log.e("User_Profile_Fragment", "Resposta da API não retornou dados do usuário.")
                    }
                } else {
                    // Manipular erro de resposta da API
                    Log.e("User_Profile_Fragment", "Erro na resposta da API: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                // Lidar com falha na solicitação
                Log.e("User_Profile_Fragment", "Erro na solicitação: ${t.message}")
            }
        })
    }

    private fun loadImage(avatarUrl: String?) {
        if (!avatarUrl.isNullOrEmpty()) {
            Picasso.get().load(avatarUrl).into(profileImageView)
        } else {
            // Se o avatar do usuário for nulo ou vazio, carregue a imagem padrão
            profileImageView.setImageResource(R.drawable.icon_noimageuser)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user__profile_, container, false)
    }

    companion object {
        private const val ARG_USER_ID = "user_id"
        @JvmStatic
        fun newInstance(userId: Int) =
            User_Profile_Fragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_USER_ID, userId)
                }
            }
    }
}
