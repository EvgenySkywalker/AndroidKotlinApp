package com.application.expertnewdesign.statistic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.expertnewdesign.R
import android.content.Context
import android.view.View.GONE
import kotlinx.android.synthetic.main.recycler_view_item.view.*
import java.lang.StringBuilder

class CustomAdapter(context: Context, private val items: List<Statistic>) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    private var mOnItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    fun setOnItemClickListener(onEntryClickListener: OnItemClickListener) {
        mOnItemClickListener = onEntryClickListener
    }

    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.recycler_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        if(item is Subject){
            holder.title.text = item.name
            holder.subtitle.text = StringBuilder().append("Количество тем: ").append(item.topicList.size).toString()
        }
        if(item is Topic){
            holder.title.text = item.name
            holder.subtitle.text = StringBuilder().append("Количество тем: ").append(item.lessonList.size).toString()
        }
        if(item is Lesson){
            holder.title.text = item.name
            holder.subtitle.visibility = GONE
            //holder.subtitle.text = StringBuilder().append(item.description).toString()
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        //val image = view.image
        val title = view.title
        val subtitle = view.subtitle

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener!!.onItemClick(v, layoutPosition)
            }
        }
    }
}