package com.ddkric.play2plat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ddkric.play2plat.api.Game
import com.ddkric.play2plat.api.ListFavoriteGames

import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class FavoritesAdapter(
    private val games: List<ListFavoriteGames>,
    private val onGamePictureClickListener: OnGamePictureClickListener
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteGameViewHolder>() {
    interface OnGamePictureClickListener {
        fun onGamePictureClick(gameId: Int)
    }

    class FavoriteGameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageGame: ImageView = itemView.findViewById(R.id.gameImageView)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteGameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_game_collection, parent, false)
        return FavoriteGameViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteGameViewHolder, position: Int) {
        val game = games[position]
        Picasso.get().load(game.game.coverImage).into(holder.imageGame)

        holder.imageGame.setOnClickListener {
            onGamePictureClickListener.onGamePictureClick(game.gameId)
        }
    }

    override fun getItemCount(): Int = games.size





    }


