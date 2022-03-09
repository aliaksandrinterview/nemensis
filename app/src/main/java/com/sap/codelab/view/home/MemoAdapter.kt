package com.sap.codelab.view.home

import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.sap.codelab.R
import com.sap.codelab.model.Memo

/**
 * Adapter containing a set of memos.
 */
internal class MemoAdapter(private val items: MutableList<Memo>,
                           private val onClick: View.OnClickListener,
                           private val onCheckboxChanged: CompoundButton.OnCheckedChangeListener) : RecyclerView.Adapter<MemoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewTypee: Int): MemoViewHolder {
        return MemoViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_memo, parent, false))
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        val memo = items[position]
        holder.update(memo, onClick, onCheckboxChanged)
    }

    override fun getItemCount(): Int = items.size

    /**
     * Updates the current list of items to the given list of items.
     */
    fun setItems(@NonNull newItems: List<Memo>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}