package com.example.questlogalpha.ui.main

import android.os.Bundle
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import com.example.questlogalpha.R
import com.example.questlogalpha.quests.QuestsFragment

// todo this is unused.... delete
class RootQuestViewerFragment : Fragment() {

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        /* Inflate the layout for this fragment */
        val root = inflater.inflate(R.layout.root_quest_viewer_fragment, container, false)

        val transaction = fragmentManager?.beginTransaction()
        /*
         * When this container fragment is created, we fill it with our first
         * "real" fragment
         */
      //  transaction?.replace(R.id.root_quest_frame, QuestsFragment())

        transaction?.commit()

        return root
    }

    companion object {

        private val TAG = "RootFragment"
    }
}
