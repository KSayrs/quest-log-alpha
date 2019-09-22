package com.example.questlogalpha.quests

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.questlogalpha.QuestsAdapter

import com.example.questlogalpha.R
import com.example.questlogalpha.ViewModelFactory
import com.example.questlogalpha.data.QuestLogDatabase
import com.example.questlogalpha.databinding.FragmentQuestsBinding
import com.example.questlogalpha.ui.main.MainViewFragmentDirections

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

        var supportActionBar = (activity as AppCompatActivity).supportActionBar
        if(supportActionBar == null){
            Log.e("QuestsFragment.kt: onCreate: ", "supportActionBar is null.")
        }
        else {
            supportActionBar.hide()
        }
      //  (activity as AppCompatActivity).supportActionBar!!.hide()
    }

    // Hide the navigation bar when we're on this activity
    override fun onResume() {
        super.onResume()
        Log.d("QuestsFragment.kt: onResume", " called")

        val supportActionBar = (activity as AppCompatActivity).supportActionBar
        if(supportActionBar == null) Log.e("MainActivity.kt: onResume: ", "supportActionBar is null.")
        else if(supportActionBar.isShowing) supportActionBar.hide()
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        val binding: FragmentQuestsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_quests, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource = QuestLogDatabase.getInstance(application).questLogDatabaseDao
        val viewModelFactory = ViewModelFactory("", dataSource, application)
        val questsViewModel = ViewModelProviders.of(this, viewModelFactory).get(QuestsViewModel::class.java)
        val adapter = QuestsAdapter()
        binding.questList.adapter = adapter


        binding.questsViewModel = questsViewModel
        binding.lifecycleOwner = this

        // Add an Observer on the state variable for Navigating when add quest button is pressed.
        questsViewModel.navigateToViewEditQuest.observe(this, Observer { quest ->
            quest?.let {
                // We need to get the navController from this, because button is not ready, and it
                // just has to be a view. For some reason, this only matters if we hit stop again
                // after using the back button, not if we hit stop and choose a quality.
                // Also, in the Navigation Editor, for Quality -> Tracker, check "Inclusive" for
                // popping the stack to get the correct behavior if we press stop multiple times
                // followed by back.
                // Also: https://stackoverflow.com/questions/28929637/difference-and-uses-of-oncreate-oncreateview-and-onactivitycreated-in-fra

                if (this.findNavController().currentDestination?.id == R.id.mainViewFragment) {
                    this.findNavController().navigate(
                        MainViewFragmentDirections.actionMainViewFragmentToViewEditQuestFragment(quest.id))
                }
                else {
                    Log.e(TAG,"Current destination is " + this.findNavController().currentDestination?.label + " instead of R.id.mainViewFragment!")
                }
                // Reset state to make sure we only navigate once, even if the device
                // has a configuration change.
                questsViewModel.doneNavigating()
            }
        })

        return binding.root
    }

    // -------------------------- log tag ------------------------------ //

    companion object {
        const val TAG: String = "KSLOG: QuestsFragment"
    }
}
