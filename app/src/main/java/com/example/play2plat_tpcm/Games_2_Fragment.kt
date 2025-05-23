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
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ddkric.play2plat.adapters.CollectionsAdapter
import com.ddkric.play2plat.adapters.Collections_2_Adapter
import com.ddkric.play2plat.adapters.PegyAdapter
import com.ddkric.play2plat.api.ApiManager
import com.ddkric.play2plat.api.Collections
import com.ddkric.play2plat.api.GameCommentsResponse
import com.ddkric.play2plat.api.UserGame
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.text.Editable
import android.text.TextWatcher
import android.widget.FrameLayout
import android.widget.Toast

class Games_2_Fragment : Fragment(), GamesAdapter.OnGamePictureClickListener {

    private lateinit var collectionAccordion: LinearLayout
    private lateinit var collectionTitle: TextView
    private lateinit var collectionList: ListView
    private lateinit var collectionInfoValues: Array<String>
    private lateinit var collectionAdapter: Collections_2_Adapter
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var TitleLayout: ConstraintLayout

    private val gameStateTranslationMap = mapOf(
        "Wish List" to "Lista de Desejos",
        "Playing" to "A jogar",
        "Paused" to "Pausado",
        "Concluded" to "Concluído"
    )

    private val gameStateReverseTranslationMap = mapOf(
        "Lista de Desejos" to "Wish List",
        "A jogar" to "Playing",
        "Pausado" to "Paused",
        "Concluído" to "Concluded"
    )



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_games_2, container, false)

        collectionAccordion = view.findViewById(R.id.collection_accordion)
        collectionTitle = view.findViewById(R.id.collection_title)
        collectionList = view.findViewById(R.id.collection_list)
        fragmentContainer = view.findViewById(R.id.fragment_container)
        TitleLayout = view.findViewById(R.id.title_container)

        loadCollections(view.context)
        showFilteredGames(collectionTitle.text.toString())

        collectionAccordion.setOnClickListener {
            if(isNetworkAvailable()){
                toggleListVisibility(collectionList, collectionTitle)
            }
            else{
                Toast.makeText(
                    requireContext(),
                    getString(R.string.no_net_collections),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        collectionTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Não precisamos fazer nada aqui
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Quando o texto mudar, atualizamos a lista de jogos
                showFilteredGames(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // Não precisamos fazer nada aqui
            }
        })

        if (!childFragmentManager.isStateSaved()) {
            val englishFilterType = getEnglishGameState(collectionTitle.text.toString())
            val fragment = Games_List_Grid_Fragment.newInstance(englishFilterType, "", null, 0)
            childFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        } else {
            // Lidar com o caso em que o estado da instância já foi salvo
        }

        return view
    }

    private fun getEnglishGameState(gameState: String): String {
        return gameStateReverseTranslationMap[gameState] ?: gameState
    }


    private fun showFilteredGames(filterType: String) {
        if (!childFragmentManager.isStateSaved()) {
            val englishFilterType = getEnglishGameState(filterType)
            val fragment = Games_List_Grid_Fragment.newInstance(englishFilterType, "", null, 0)
            childFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        } else {
            // Lidar com o caso em que o estado da instância já foi salvo
        }
    }



    private fun loadCollections(context: Context) {
        collectionInfoValues = context.resources.getStringArray(R.array.collections_names)
        collectionAdapter = Collections_2_Adapter(context, collectionInfoValues, collectionTitle)
        collectionList.adapter = collectionAdapter

        // Retrieve SharedPreferences
        val sharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        // Get the last collection ID and selected locale
        val lastCollectionId = sharedPreferences.getInt("last_collection_id", 1) // Default to 1 if not found
        val selectedLocale = sharedPreferences.getString("selected_locale", "en") ?: "en"
        // Determine the string array resource based on the selected locale
        val collectionNamesArray = when (selectedLocale) {
            "pt" -> context.resources.getStringArray(R.array.collections_names) // Portuguese
            else -> context.resources.getStringArray(R.array.collections_names) // Default to English
        }
        // Check if the ID is within bounds and retrieve the collection name
        val collectionName = if (lastCollectionId in collectionNamesArray.indices) {
            collectionNamesArray[lastCollectionId]
        } else {
            // Handle out of bounds or default case if necessary
            collectionNamesArray.getOrElse(0) { "Unknown" } // Default to the first item or another fallback
        }
        // Set the text of the collectionTitle
        collectionTitle.text = collectionName

        setListViewHeightBasedOnItems(collectionList)
    }

    private fun toggleListVisibility(listView: ListView, titleView: TextView) {
        val heightInPx = 50.dpToPx()

        if (listView.visibility == View.VISIBLE) {
            listView.visibility = View.GONE
            collectionAccordion.layoutParams.height = heightInPx
            titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_spinner_down, 0)
        } else {
            listView.visibility = View.VISIBLE
            setListViewHeightBasedOnItems(listView)
            collectionAccordion.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_spinner_up, 0)
        }
        collectionAccordion.requestLayout()
    }

    fun setListViewHeightBasedOnItems(listView: ListView) {
        val listAdapter = listView.adapter ?: return
        var totalHeight = 0
        for (i in 0 until listAdapter.count) {
            val listItem = listAdapter.getView(i, null, listView)
            listItem.measure(
                View.MeasureSpec.makeMeasureSpec(listView.width, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            totalHeight += listItem.measuredHeight
        }
        val params = listView.layoutParams
        params.height = totalHeight + (listView.dividerHeight * (listAdapter.count - 1))
        listView.layoutParams = params
        listView.requestLayout()
    }

    private fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
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
}

