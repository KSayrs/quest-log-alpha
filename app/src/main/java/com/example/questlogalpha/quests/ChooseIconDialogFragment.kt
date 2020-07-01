package com.example.questlogalpha.quests

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.questlogalpha.R
import com.example.questlogalpha.Util
import com.example.questlogalpha.data.Icon
import com.example.questlogalpha.data.QuestLogDatabase
import com.example.questlogalpha.databinding.DialogFragmentSelectIconBinding
import com.example.questlogalpha.databinding.IconItemViewBinding
import kotlinx.android.synthetic.main.icon_item_view.view.*

/** ************************************************************************************************
 * [DialogFragment] to display when choosing an icon for a quest, item, or skill.
 * ********************************************************************************************** */
class ChooseIconDialogFragment : DialogFragment() {

    var onIconTapped: ((iconResourceId: Int) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding: DialogFragmentSelectIconBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_fragment_select_icon, null, false)
        binding.lifecycleOwner = this
        val dataSource = QuestLogDatabase.getInstance(activity!!.application).iconsDatabaseDao

        // set up recyclerview
        val mRecyclerView = binding.iconGrid
        mRecyclerView.layoutManager = GridLayoutManager(context, 5)
        val adapter = IconsAdapter()
        mRecyclerView.adapter = adapter

        // todo replace with proper viewmodel call
        adapter.data = listOf(
            Icon(R.drawable.ic_scroll_quill, listOf("tag")),
            Icon(R.drawable.ic_cog, listOf("tag")),
            Icon(R.drawable.ic_clock, listOf("tag"))
        )

        // there's almost certainly a better way to do this than just making delegates call delegates but it's alpha and it works
        adapter.onIconTapped = { onIconTapped?.invoke(it) }

        return AlertDialog.Builder(activity)
            .setView(binding.root)
            .setNegativeButton(
                getString(R.string.cancel),
                DialogInterface.OnClickListener { _, _ ->
                    Util.showShortToast(context!!, "negative")
                })
            .create()
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: ChooseIconDialogFragment"
    }
}

class IconItemViewHolder(val constraintLayout: ConstraintLayout): RecyclerView.ViewHolder(constraintLayout)

/** ************************************************************************************************
 * A [RecyclerView.Adapter] to show all skills in the database.
 * ********************************************************************************************** */
class IconsAdapter: RecyclerView.Adapter<IconItemViewHolder>() {

    var onIconTapped: ((iconResourceId: Int) -> Unit)? = null

    var data = listOf<Icon>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: IconItemViewBinding = DataBindingUtil.inflate(layoutInflater, R.layout.icon_item_view, parent, false)

        return IconItemViewHolder(binding.root.icon_container)
    }

    override fun onBindViewHolder(holder: IconItemViewHolder, position: Int) {
        val binding = DataBindingUtil.getBinding<IconItemViewBinding>(holder.itemView)
        val icon = data[position]

        binding!!.iconSquare.setImageResource(icon.drawableResource)

        binding.iconSquare.setOnClickListener { onIconTapped?.invoke(icon.drawableResource) }
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: IconsAdapter"
    }
}
