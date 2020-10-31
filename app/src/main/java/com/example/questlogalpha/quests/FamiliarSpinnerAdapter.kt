package com.example.questlogalpha.quests

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.questlogalpha.R
import com.example.questlogalpha.Util


class FamiliarSpinnerAdapter(context: Context, images: Array<Int>) :
    ArrayAdapter<Int>(context, R.layout.spinner_item_fixed_width, images) {

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup) =
        getImageForPosition(position)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup) =
        getImageForPosition(position)

    private fun getImageForPosition(position: Int) = ImageView(context).apply {
        setBackgroundResource(getItem(position)!!)
        background.setTint(ContextCompat.getColor(context, R.color.colorAccent))

        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Util.dpToPixels(80f, resources))
    }
}
