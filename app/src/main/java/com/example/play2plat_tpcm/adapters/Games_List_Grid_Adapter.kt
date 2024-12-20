package com.ddkric.play2plat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ddkric.play2plat.R
import com.ddkric.play2plat.api.Game
import com.ddkric.play2plat.databinding.ItemGameGridBinding
import com.squareup.picasso.Picasso

class Games_List_Grid_Adapter(
    private var games: List<Game>,
    private val listener: OnGameClickListener
) : RecyclerView.Adapter<Games_List_Grid_Adapter.GameCoverViewHolder>() {

    inner class GameCoverViewHolder(private val binding: ItemGameGridBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(game: Game) {
            Picasso.get()
                .load(game.coverImage).into(binding.gameCoverImage)
            binding.gameCoverImage.setOnClickListener {
                game.id?.let { id ->
                    listener.onGameClick(id)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameCoverViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemGameGridBinding.inflate(inflater, parent, false)
        return GameCoverViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameCoverViewHolder, position: Int) {
        holder.bind(games[position])


    }

    override fun getItemCount() = games.size

    fun updateGames(newGames: List<Game>) {
        games = newGames
        notifyDataSetChanged()
    }

    interface OnGameClickListener {
        fun onGameClick(gameId: Int)
    }
}
