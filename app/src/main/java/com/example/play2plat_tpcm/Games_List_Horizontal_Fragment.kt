package com.ddkric.play2plat

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ddkric.play2plat.adapters.Games_List_Grid_Adapter
import com.ddkric.play2plat.adapters.Games_List_Horizontal_Adapter
import com.ddkric.play2plat.api.ApiManager
import com.ddkric.play2plat.api.Collections
import com.ddkric.play2plat.api.Game
import com.ddkric.play2plat.api.GameFavorite
import com.ddkric.play2plat.api.ListFavoriteGames
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Games_List_Horizontal_Fragment : Fragment(), Games_List_Horizontal_Adapter.OnGameClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var gameCoverAdapter: Games_List_Horizontal_Adapter
    private lateinit var titleText: TextView
    private lateinit var arrowRight: ImageView
    private var filterType: String? = null
    private var paramater: String? = null
    private var paramaterInt: Int? = 0
    private var otherUser: Boolean = false
    private var paramaterUserId: Int = 0
    private var userId: Int = 0
    private val navigationViewModel: FragmentNavigationViewModel by viewModels()

    interface OnEmptyListListener {
        fun onListEmpty()
    }

    interface OnNotEmptyListListener {
        fun onListNotEmpty()
    }


    companion object {
        private const val ARG_FILTER_TYPE = "filter_type"
        private const val ARG_PARAMATER = "paramater"
        private const val ARG_PARAMATER_INT = "paramaterInt"
        private const val ARG_OTHER_USER = "otherUser"
        private const val ARG_PARAMATER_USER_ID = "paramaterUserId"


        fun newInstance(filterType: String, paramater: String, paramaterInt: Int, otherUser: Boolean, paramaterUserId: Int ): Games_List_Horizontal_Fragment {
            val fragment = Games_List_Horizontal_Fragment()
            val args = Bundle()
            args.putString(ARG_FILTER_TYPE, filterType)
            args.putString(ARG_PARAMATER, paramater)
            args.putInt(ARG_PARAMATER_INT, paramaterInt)
            args.putBoolean(ARG_OTHER_USER, otherUser)
            args.putInt(ARG_PARAMATER_USER_ID, paramaterUserId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            filterType = it.getString(ARG_FILTER_TYPE)
            paramater= it.getString(ARG_PARAMATER)
            paramaterInt = it.getInt(ARG_PARAMATER_INT)
            otherUser = it.getBoolean(ARG_OTHER_USER)
            paramaterUserId = it.getInt(ARG_PARAMATER_USER_ID)
        }

        // Retrieve userId from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("user_id", 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_games_list_horizontal, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_game_covers)
        arrowRight = view.findViewById(R.id.iconArrowRight)

        //recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        //gameCoverAdapter = Games_List_Horizontal_Adapter(emptyList(), this, filterType, otherUser)
        //recyclerView.adapter = gameCoverAdapter
        titleText = view.findViewById(R.id.text_view)

        if (filterType == "Companies" || filterType == "SameCompany") {
            //titleText.text = "From $paramater"
            val fromText = context?.getString(R.string.from)
            val parameter = paramater
            titleText.text = "$fromText $parameter"
        }
        else if(filterType == "Recent"){
            val recentGames = getString(R.string.recent_games)
            titleText.text = recentGames
        }
        else if(filterType == "Favorite"){
            val favouriteGames = getString(R.string.favourite_games)
            titleText.text = favouriteGames
        }
        else if(filterType == "Playing"){
            val playingGames = getString(R.string.playing_games)
            titleText.text = playingGames
        }
        else {
            val parameterGames = getString(R.string.paramaters_games, paramater)
            titleText.text = parameterGames
        }

        loadGames()


        arrowRight.setOnClickListener {
            if (isNetworkAvailable()) {
                redirectToViewMoreGames_Fragment(filterType, paramater)
            }
            else{
                redirectToNoConnectionFragment()
            }

        }

        return view
    }

    private fun redirectToViewMoreGames_Fragment(filterType: String?, paramater: String?) {
        var received_user = userId
        if (otherUser == true){
            received_user = paramaterUserId
        }
        val viewMoreGamesFragment = ViewMoreGames_Fragment.newInstance(filterType ?: "", paramater ?: "", null, received_user)
        navigationViewModel.addToStack(viewMoreGamesFragment)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.layout, viewMoreGamesFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        return networkInfo != null && networkInfo.isConnected
    }


    private fun loadGames() {
        when (filterType) {
            "Playing", "Wish List", "Paused", "Concluded" -> getStateCollection(filterType!!)
            "Favorite" -> getFavoriteGames()
            "Genres" -> getGamesByGenre(paramater!!)
            "Sequences"-> getGamesBySequence(paramater!!)
            "Platforms"-> getGamesByPlatform(paramater!!)
            "Companies"-> getGamesByCompany(paramater!!)
            "Recent" -> getRecentGames()
            "SameSequence" -> getGamesSameSequence(paramaterInt!!)
            "SameCompany" -> getGamesSameCompany(paramaterInt!!)
            else -> getStateCollection("Playing")
        }
    }

    private fun setupRecyclerView(games: List<Game>) {
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        gameCoverAdapter = Games_List_Horizontal_Adapter(games, this, filterType, otherUser)
        recyclerView.adapter = gameCoverAdapter
    }

    private fun getStateCollection(state: String) {
        //val sharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        //val userId = sharedPreferences.getInt("user_id", 0)
        ApiManager.apiService.getStateCollection(paramaterUserId, state).enqueue(object : Callback<List<Collections>> {
            override fun onResponse(
                call: Call<List<Collections>>,
                response: Response<List<Collections>>
            ) {
                if (response.isSuccessful) {
                    val games = response.body()?.map { it.toGame() } ?: emptyList()
                    Log.d("Games_List_Grid_Fragment", "Resposta da API: $games")
                    //gameCoverAdapter.updateGames(games)
                    setupRecyclerView(games)
                } else {
                    Log.e("Games_List_Grid_Fragment", "Erro na resposta: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<List<Collections>>, t: Throwable) {
                Log.e("Games_List_Grid_Fragment", "Falha na chamada da API: ${t.message}")
            }
        })
    }

    private fun getFavoriteGames() {
        ApiManager.apiService.getFavoritesByUserId(paramaterUserId).enqueue(object : Callback<List<ListFavoriteGames>> {
            override fun onResponse(
                call: Call<List<ListFavoriteGames>>,
                response: Response<List<ListFavoriteGames>>
            ) {
                if (response.isSuccessful) {
                    val games = response.body()?.map { it.game.toGame() } ?: emptyList()
                    Log.d("Games_List_Grid_Fragment", "Resposta da API: $games")
                    //gameCoverAdapter.updateGames(games)
                    setupRecyclerView(games)
                } else {
                    Log.e("Games_List_Grid_Fragment", "Erro na resposta: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<List<ListFavoriteGames>>, t: Throwable) {
                Log.e("Games_List_Grid_Fragment", "Falha na chamada da API: ${t.message}")
            }
        })
    }

    private fun getGamesByCompany(companyName: String) {
        ApiManager.apiService.getGamesByCompany(companyName).enqueue(object : Callback<List<Collections>> {
            override fun onResponse(
                call: Call<List<Collections>>,
                response: Response<List<Collections>>
            ) {
                if (response.isSuccessful) {
                    val games = response.body()?.map { it.toGame() } ?: emptyList()
                    Log.d("Games_List_Grid_Fragment", "Resposta da API: $games")
                    //gameCoverAdapter.updateGames(games)
                    setupRecyclerView(games)
                } else {
                    Log.e("Games_List_Grid_Fragment", "Erro na resposta: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<List<Collections>>, t: Throwable) {
                Log.e("Games_List_Grid_Fragment", "Falha na chamada da API: ${t.message}")
            }
        })
    }

    private fun getGamesByPlatform(platformName: String) {
        ApiManager.apiService.getGamesByPlatform(platformName).enqueue(object : Callback<List<Collections>> {
            override fun onResponse(
                call: Call<List<Collections>>,
                response: Response<List<Collections>>
            ) {
                if (response.isSuccessful) {
                    val games = response.body()?.map { it.toGame() } ?: emptyList()
                    Log.d("Games_List_Grid_Fragment", "Resposta da API: $games")
                    //gameCoverAdapter.updateGames(games)
                    setupRecyclerView(games)
                } else {
                    Log.e("Games_List_Grid_Fragment", "Erro na resposta: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<List<Collections>>, t: Throwable) {
                Log.e("Games_List_Grid_Fragment", "Falha na chamada da API: ${t.message}")
            }
        })
    }

    private fun getGamesBySequence(sequenceName: String) {
        ApiManager.apiService.getGamesBySequence(sequenceName).enqueue(object : Callback<List<Collections>> {
            override fun onResponse(
                call: Call<List<Collections>>,
                response: Response<List<Collections>>
            ) {
                if (response.isSuccessful) {
                    val games = response.body()?.map { it.toGame() } ?: emptyList()
                    Log.d("Games_List_Grid_Fragment", "Resposta da API: $games")
                    //gameCoverAdapter.updateGames(games)
                    setupRecyclerView(games)
                } else {
                    Log.e("Games_List_Grid_Fragment", "Erro na resposta: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<List<Collections>>, t: Throwable) {
                Log.e("Games_List_Grid_Fragment", "Falha na chamada da API: ${t.message}")
            }
        })
    }

    private fun getGamesByGenre(genreName: String) {
        ApiManager.apiService.getGamesByGenre(genreName).enqueue(object : Callback<List<Collections>> {
            override fun onResponse(
                call: Call<List<Collections>>,
                response: Response<List<Collections>>
            ) {
                if (response.isSuccessful) {
                    val games = response.body()?.map { it.toGame() } ?: emptyList()
                    Log.d("Games_List_Grid_Fragment", "Resposta da API: $games")
                    //gameCoverAdapter.updateGames(games)
                    setupRecyclerView(games)
                } else {
                    Log.e("Games_List_Grid_Fragment", "Erro na resposta: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<List<Collections>>, t: Throwable) {
                Log.e("Games_List_Grid_Fragment", "Falha na chamada da API: ${t.message}")
            }
        })
    }

    private fun getRecentGames() {
        ApiManager.apiService.getRecentGames().enqueue(object : Callback<List<Collections>> {
            override fun onResponse(
                call: Call<List<Collections>>,
                response: Response<List<Collections>>
            ) {
                if (response.isSuccessful) {
                    val games = response.body()?.map { it.toGame() } ?: emptyList()
                    Log.d("Games_List_Grid_Fragment", "Resposta da API: $games")
                    //gameCoverAdapter.updateGames(games)
                    setupRecyclerView(games)
                } else {
                    Log.e("Games_List_Grid_Fragment", "Erro na resposta: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<List<Collections>>, t: Throwable) {
                Log.e("Games_List_Grid_Fragment", "Falha na chamada da API: ${t.message}")
            }
        })
    }

    private fun getGamesSameSequence(gameId: Int) {
        ApiManager.apiService.getGamesSameSequence(gameId).enqueue(object : Callback<List<Collections>> {
            override fun onResponse(
                call: Call<List<Collections>>,
                response: Response<List<Collections>>
            ) {
                if (response.isSuccessful) {
                    val games = response.body()?.map { it.toGame() } ?: emptyList()
                    Log.d("Games_List_Grid_Fragment", "Resposta da API: $games")
                    //gameCoverAdapter.updateGames(games)
                    setupRecyclerView(games)
                } else {
                    Log.e("Games_List_Grid_Fragment", "Erro na resposta: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<List<Collections>>, t: Throwable) {
                Log.e("Games_List_Grid_Fragment", "Falha na chamada da API: ${t.message}")
            }
        })
    }


    private fun getGamesSameCompany(gameId: Int) {
        ApiManager.apiService.getGamesSameCompany(gameId).enqueue(object : Callback<List<Collections>> {
            override fun onResponse(
                call: Call<List<Collections>>,
                response: Response<List<Collections>>
            ) {
                if (response.isSuccessful) {
                    val games = response.body()?.map { it.toGame() } ?: emptyList()
                    Log.d("Games_List_Grid_Fragment", "Resposta da API: $games")
                    //gameCoverAdapter.updateGames(games)
                    setupRecyclerView(games)
                    if (games.isEmpty()) {
                        Log.d("empty", "empty")
                        emptyListListener?.onListEmpty()
                    } else {
                        Log.d("not empty", "not empty")
                        notEmptyListListener?.onListNotEmpty()
                    }
                } else {
                    Log.e("Games_List_Grid_Fragment", "Erro na resposta: ${response.errorBody()}")
                    emptyListListener?.onListEmpty()
                }
            }

            override fun onFailure(call: Call<List<Collections>>, t: Throwable) {
                Log.e("Games_List_Grid_Fragment", "Falha na chamada da API: ${t.message}")
                emptyListListener?.onListEmpty()
            }
        })
    }

    private var emptyListListener: OnEmptyListListener? = null
    private var notEmptyListListener: OnNotEmptyListListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnEmptyListListener) {
            emptyListListener = context
        }
        if (context is OnNotEmptyListListener) {
            notEmptyListListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        emptyListListener = null
        notEmptyListListener = null
    }



    // Handle game click event
    override fun onGameClick(gameId: Int) {
        if (isNetworkAvailable()) {
            val platforms = arrayListOf<String>()
            val viewGameFragment = View_Game_Fragment.newInstance(gameId, platforms)
            navigationViewModel.addToStack(viewGameFragment)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.layout, viewGameFragment)
                .addToBackStack(null)
                .commit()
        }
        else{
            redirectToNoConnectionFragment()
        }

    }

    // Inner class for the fragment
    private fun Collections.toGame(): Game {
        return Game(
            id = this.id,
            name = this.name,
            description = "", // Se não tiver essa informação, pode deixar em branco ou ajustar conforme necessário
            isFree = this.isFree,
            releaseDate = "", // Se não tiver essa informação, pode deixar em branco ou ajustar conforme necessário
            pegiInfo = 0, // Se não tiver essa informação, pode deixar 0 ou ajustar conforme necessário
            coverImage = this.coverImage,
            sequenceId = 0, // Se não tiver essa informação, pode deixar 0 ou ajustar conforme necessário
            companyId = 0 // Se não tiver essa informação, pode deixar 0 ou ajustar conforme necessário
        )
    }

    private fun GameFavorite.toGame(): Game {
        return Game(
            id = this.id,
            name = this.name,
            description = "", // Se não tiver essa informação, pode deixar em branco ou ajustar conforme necessário
            isFree = false,
            releaseDate = "", // Se não tiver essa informação, pode deixar em branco ou ajustar conforme necessário
            pegiInfo = 0, // Se não tiver essa informação, pode deixar 0 ou ajustar conforme necessário
            coverImage = this.coverImage,
            sequenceId = 0, // Se não tiver essa informação, pode deixar 0 ou ajustar conforme necessário
            companyId = 0
        )
    }

    private fun redirectToNoConnectionFragment() {
        val noConnectionFragment= NoConnectionFragment()
        navigationViewModel.addToStack(noConnectionFragment)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.layout, noConnectionFragment)
            .addToBackStack(null)
            .commit()
    }
}
