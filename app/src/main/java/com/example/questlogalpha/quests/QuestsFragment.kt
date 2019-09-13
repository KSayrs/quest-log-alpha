package com.example.questlogalpha.quests

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

import com.example.questlogalpha.R
import com.example.questlogalpha.ViewModelFactory
import com.example.questlogalpha.data.QuestLogDatabase
import com.example.questlogalpha.databinding.FragmentQuestsBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [QuestsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [QuestsFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class QuestsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        val binding: FragmentQuestsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_quests, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource = QuestLogDatabase.getInstance(application).questLogDatabaseDao
        val viewModelFactory = ViewModelFactory(dataSource, application)
        val questsViewModel = ViewModelProviders.of(this, viewModelFactory).get(QuestsViewModel::class.java)

        binding.questsViewModel = questsViewModel
        binding.lifecycleOwner = this

        return binding.root
    }
}
