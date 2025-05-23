package com.ddkric.play2plat

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ddkric.play2plat.api.ApiManager
import com.ddkric.play2plat.api.Filters
import com.ddkric.play2plat.api.ListFavoriteGames
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewMoreGames_Fragment : Fragment(), FavoritesAdapter.OnGamePictureClickListener {

    private lateinit var favoritesAdapter: FavoritesAdapter
    private var favoriteGamesList: MutableList<ListFavoriteGames> = mutableListOf()
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var titleTextView: TextView
    private lateinit var backImageView: ImageView
    private var filterType: String? = null
    private var paramater: String? = null
    private var filters: Filters? = null
    private var user_two_id: Int = 0
    private lateinit var TitleLayout: ConstraintLayout

    companion object {
        private const val ARG_FILTER_TYPE = "filter_type"
        private const val ARG_PARAMETER = "parameter"
        private const val ARG_FILTERS = "filters" // Novo argumento para Filters
        private const val ARG_USER_TWO_ID = "user_two_id"

        fun newInstance(filterType: String, paramater: String, filters: Filters?, user_two_id: Int): ViewMoreGames_Fragment {
            val fragment = ViewMoreGames_Fragment()
            val args = Bundle()
            args.putString(ARG_FILTER_TYPE, filterType)
            args.putString(ARG_PARAMETER, paramater)
            args.putParcelable(ARG_FILTERS, filters) // Adiciona os Filters ao Bundle
            args.putInt(ARG_USER_TWO_ID, user_two_id)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            filterType = it.getString(ARG_FILTER_TYPE)
            paramater = it.getString(ARG_PARAMETER)
            filters = it.getParcelable(ARG_FILTERS) // Obtém os Filters do Bundle
            user_two_id = it.getInt(ARG_USER_TWO_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_more_games_, container, false)

        favoritesAdapter = FavoritesAdapter(favoriteGamesList, this)
        fragmentContainer = view.findViewById(R.id.fragment_container)
        titleTextView = view.findViewById(R.id.title)
        backImageView = view.findViewById(R.id.back_icon)
        TitleLayout = view.findViewById(R.id.container)

        //loadFavoriteGames()
        backImageView.setOnClickListener {
            if (isNetworkAvailable()) {
                requireActivity().onBackPressed()
            }
            else{
                redirectToNoConnectionFragment()
            }
        }


        if (filterType == "Companies" || filterType == "SameCompany") {
            //titleText.text = "From $paramater"
            val fromText = context?.getString(R.string.from)
            val parameter = paramater
            titleTextView.text = "$fromText $parameter"
        }
        else if(filterType == "Recent"){
            val recentGames = getString(R.string.recent_games)
            titleTextView.text = recentGames
        }
        else if(filterType == "Favorite"){
            val favouriteGames = getString(R.string.favourite_games)
            titleTextView.text = favouriteGames
        }
        else if(filterType == "Playing"){
            val playingGames = getString(R.string.playing_games)
            titleTextView.text = playingGames
        }
        else if(filterType == "Filtered"){
            val foundedGames = getString(R.string.founded_games)
            titleTextView.text = foundedGames
        }
        else {
            val parameterGames = getString(R.string.paramaters_games, paramater)
            titleTextView.text = parameterGames
        }

//        val textViewHeight = TitleLayout.layoutParams.height
//
//        // Obtém a altura da tela
//        val displayMetrics = resources.displayMetrics
//        val screenHeight = displayMetrics.heightPixels
//
//        // Calcula a altura disponível para o FrameLayout
//        val availableHeight = screenHeight - textViewHeight
//
//        // Define a altura do FrameLayout
//        val layoutParams = fragmentContainer.layoutParams
//        layoutParams.height = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) availableHeight - 200 else availableHeight
//        fragmentContainer.layoutParams = layoutParams

        Log.d("Filters", "o seus filtros $filters")


        val fragment = Games_List_Grid_Fragment.newInstance(filterType!!, paramater!!, filters, user_two_id)

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()


        /*
        val fragment = Games_List_Grid_Fragment.newInstance(filterType!!, paramater!!)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

         */

        return view
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        return networkInfo != null && networkInfo.isConnected
    }


    private fun redirectToNoConnectionFragment() {
        val noConnectionFragment= NoConnectionFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.layout, noConnectionFragment)
            .addToBackStack(null)
            .commit()

    }

    private fun redirectToViewGame(gameId: Int) {
        val platforms = arrayListOf<String>()

        val viewGameFragment = View_Game_Fragment.newInstance(gameId, platforms)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.layout, viewGameFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onGamePictureClick(gameId: Int) {
        redirectToViewGame(gameId)
    }


    private fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

    private fun getUserIdFromSession(): Int {
        val sharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", 0)
    }
}

