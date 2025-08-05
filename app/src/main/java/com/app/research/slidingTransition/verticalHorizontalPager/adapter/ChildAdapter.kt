package com.app.research.slidingTransition.verticalHorizontalPager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.app.research.R
import com.app.research.slidingTransition.verticalHorizontalPager.ParentData

class ChildAdapter(
    private val children: List<ParentData.ChildData>
) : RecyclerView.Adapter<ChildAdapter.ChildViewHolder>() {

    inner class ChildViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val playerView: PlayerView = view.findViewById(R.id.childPlayerView)
        var player: ExoPlayer? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_horizontal_pager, parent, false)
        return ChildViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        val child = children[position]

        // Setup ExoPlayer
        holder.player = ExoPlayer.Builder(holder.view.context).build().also { player ->
            holder.playerView.player = player
            val mediaItem = MediaItem.fromUri(child.cUrl.toUri())
//            val mediaItem = MediaItem.fromUri(child.cUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true
        }
    }

    override fun onViewRecycled(holder: ChildViewHolder) {
        holder.player?.release()
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int = children.size
}