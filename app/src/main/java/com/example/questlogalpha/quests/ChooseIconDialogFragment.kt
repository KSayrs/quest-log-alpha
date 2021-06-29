package com.example.questlogalpha.quests

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.questlogalpha.R
import com.example.questlogalpha.Util
import com.example.questlogalpha.ViewModelFactory
import com.example.questlogalpha.data.Icon
import com.example.questlogalpha.data.Quest
import com.example.questlogalpha.data.QuestLogDatabase
import com.example.questlogalpha.databinding.DialogFragmentSelectIconBinding
import com.example.questlogalpha.databinding.IconItemViewBinding

/** ************************************************************************************************
 * [DialogFragment] to display when choosing an icon for a quest, item, or skill.
 * ********************************************************************************************** */
class ChooseIconDialogFragment : DialogFragment() {

    var onIconTapped: ((iconResourceId: Int) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding: DialogFragmentSelectIconBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_fragment_select_icon, null, false)
        binding.lifecycleOwner = this
        val dataSource = QuestLogDatabase.getInstance(requireActivity().application).iconsDatabaseDao
        val questsDataSource = QuestLogDatabase.getInstance(requireActivity().application).questLogDatabaseDao
        val globalVariables = QuestLogDatabase.getInstance(requireActivity().application).globalVariableDatabaseDao
        val familiarsDataSource = QuestLogDatabase.getInstance(requireActivity().application).familiarsDatabaseDao
        val viewModelFactory = ViewModelFactory("", questsDataSource, globalVariableDataSource = globalVariables, iconsDataSource = dataSource, familiarsDataSource = familiarsDataSource, application = requireActivity().application)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(QuestsViewModel::class.java)

        // set up recyclerview
        val mRecyclerView = binding.iconGrid
        mRecyclerView.layoutManager = GridLayoutManager(context, 5)
        val adapter = IconsAdapter()
        mRecyclerView.adapter = adapter

        // observe icons
        viewModel.icons.observe(this, Observer {
            it?.let {
                adapter.data = it
                Log.d(TAG, "it: $it")
            }
        })

        // implement search logic
        binding.iconSearchView.setOnQueryTextListener(object: androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    // observe icons, again I guess
                    viewModel.onIconSearch(query).observe(this@ChooseIconDialogFragment, Observer { list ->
                        list?.let {
                            adapter.data = it
                        }
                    })
                    adapter.notifyDataSetChanged()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    // observe icons
                    viewModel.searchIcons(newText).observe(this@ChooseIconDialogFragment, Observer { list ->
                        list?.let {
                            adapter.data = it
                        }
                    })
                    adapter.notifyDataSetChanged()
                }
                return true
            }

        })

        // there's almost certainly a better way to do this than just making delegates call delegates but it's alpha and it works
        adapter.onIconTapped = { onIconTapped?.invoke(it) }

        return AlertDialog.Builder(activity)
            .setView(binding.root)
            .setNegativeButton(
                getString(R.string.cancel),
                DialogInterface.OnClickListener { _, _ ->
                    Util.showShortToast(requireContext(), "negative")
                })
            .create()
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: ChooseIconDialogFragment"
    }
}

// ---------------------------- Adapter & View Holder ------------------------------ //

class IconItemViewHolder(val constraintLayout: ConstraintLayout, private val binding: IconItemViewBinding): RecyclerView.ViewHolder(constraintLayout) {
    fun bind(icon: Icon) {
        binding.iconSquare.setImageResource(icon.drawableResource)
    }
}

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
        val holder = IconItemViewHolder(binding.iconContainer, binding)

        binding.iconSquare.setOnClickListener {
            val pos = holder.absoluteAdapterPosition
            onIconTapped?.invoke(data[pos].drawableResource)
        }

        return holder
    }

    override fun onBindViewHolder(holder: IconItemViewHolder, position: Int) {
        val icon = data[position]

        holder.bind(icon)
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: IconsAdapter"
    }
}
