package com.wgsoft.appviwifinal

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView

class MyUserAdapter(val context: Context, val userList:List<BeaconsItem>): RecyclerView.Adapter<MyUserAdapter.ViewHolder>()  {

    private lateinit var mListener : onItemClickListener

    interface onItemClickListener {
            fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }


    class ViewHolder(itemView: View, listener: onItemClickListener):RecyclerView.ViewHolder(itemView) {
        var tvName : TextView
        var tvusername : TextView
        var tvId : TextView

        init {
            tvName = itemView.findViewById(R.id.tvName)
            tvusername = itemView.findViewById(R.id.tvusername)
            tvId = itemView.findViewById(R.id.tvId)
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var itemView = LayoutInflater.from(context).inflate(R.layout.row_items,parent,false)
        return ViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.tvName.text = userList[position].BeaconNombre
        holder.tvId.text = userList[position].BeaconTipo
        holder.tvusername.text = userList[position].BeaconUUID


    }

    override fun getItemCount(): Int {
        return userList.size
    }


}



