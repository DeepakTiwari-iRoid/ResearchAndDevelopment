package com.app.research.slidingTransition.verticalHorizontalPager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.app.research.R
import com.app.research.slidingTransition.verticalHorizontalPager.ParentData

class ParentAdapter(
    private val parents: List<ParentData>
) : RecyclerView.Adapter<ParentAdapter.ParentViewHolder>() {

    inner class ParentViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val horizontalPager: ViewPager2 = view.findViewById(R.id.horizontalPager)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vertical_pager, parent, false)
        return ParentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val parent = parents[position]
        holder.horizontalPager.adapter = ChildAdapter(parent.children)
    }

    override fun getItemCount(): Int = parents.size
}