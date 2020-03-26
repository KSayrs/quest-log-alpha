package com.example.questlogalpha.vieweditquest

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context.ALARM_SERVICE
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.questlogalpha.*
import com.example.questlogalpha.data.QuestLogDatabase
import com.example.questlogalpha.databinding.FragmentViewEditQuestBinding
import com.example.questlogalpha.databinding.QuestObjectiveViewBinding
import com.example.questlogalpha.quests.AddRewardDialogFragment
import com.example.questlogalpha.quests.Difficulty
import kotlinx.android.synthetic.main.quest_objective_view.view.*

/** ************************************************************************************************
 * [Fragment] to display all data relating to a quest.
 * ********************************************************************************************** */
class ViewEditQuestFragment : Fragment() {

    private var viewModel: ViewEditQuestViewModel? = null

    private var questId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentViewEditQuestBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_view_edit_quest, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource = QuestLogDatabase.getInstance(application).questLogDatabaseDao
        val arguments = ViewEditQuestFragmentArgs.fromBundle(arguments)

        questId = arguments.questId

        val viewModelFactory = ViewModelFactory(arguments.questId, dataSource, null, application)
        val viewEditQuestViewModel =
            ViewModelProvider(this, viewModelFactory).get(ViewEditQuestViewModel::class.java)

        viewModel = viewEditQuestViewModel

        binding.viewEditQuestViewModel = viewEditQuestViewModel
        binding.lifecycleOwner = this

        // set up spinner
        val spinner: Spinner = binding.difficultySpinner
        spinner.adapter = ArrayAdapter<Difficulty>(
            application,
            android.R.layout.simple_spinner_item,
            Difficulty.values()
        )
        spinner.onItemSelectedListener = viewEditQuestViewModel

        // set up rewards adapter
        val rewardsAdapter = RewardsAdapter()
        rewardsAdapter.onItemRemoved = {
            viewModel!!.onRemoveReward(it)
        }
        binding.rewardsList.adapter = rewardsAdapter

        // show the action bar on this page
        val actionbar = (activity as AppCompatActivity).supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.show()

        setHasOptionsMenu(true)

        // set the ordinal on data load
        viewEditQuestViewModel.difficulty.observeOnce {
            if (viewEditQuestViewModel.difficulty.value != null) {
                spinner.setSelection(viewEditQuestViewModel.difficulty.value!!.ordinal)
            } else {
                spinner.setSelection(Difficulty.MEDIUM.ordinal)
            }
        }

        // add/update objectives
        viewEditQuestViewModel.modifiedObjective.observe(viewLifecycleOwner, Observer {
            if (viewEditQuestViewModel.objectives.value == null) {
                Log.e(TAG, "viewEditQuestViewModel.objectives is null. Ending observation early.")
                return@Observer
            }
            // todo extend view to allow findInChildren
            val views = binding.objectives.children
            for (objective in viewEditQuestViewModel.objectives.value!!) {
                var objectiveBound = false
                for (view in views) {
                    val objBinding: QuestObjectiveViewBinding? = DataBindingUtil.getBinding(view)
                    if (objBinding != null && objBinding.objective?.id == objective.id) {
                        objectiveBound = true
                        // todo figure out why this doesn't auto-update
                        objBinding.questObjectiveCheckbox.isChecked = objective.completed
                        break
                    }
                }
                if (objectiveBound) continue

                val objectiveBinding: QuestObjectiveViewBinding = DataBindingUtil.inflate(
                    inflater,
                    R.layout.quest_objective_view,
                    container,
                    false
                )
                val objView = objectiveBinding.root
                objectiveBinding.objective = objective
                objectiveBinding.viewModel = viewEditQuestViewModel
                binding.objectives.addView(objView)

                objView.delete_objective_icon.setOnClickListener {
                    viewEditQuestViewModel.onObjectiveDeleted(objective)
                    binding.objectives.removeView(objView)
                }

                objView.edit_objective_icon.setOnClickListener {
                    objView.quest_objective_edit_text.isFocusableInTouchMode = true
                    objView.quest_objective_edit_text.requestFocus()
                    val imm =
                        application.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm!!.showSoftInput(
                        objView.quest_objective_edit_text,
                        InputMethodManager.SHOW_IMPLICIT
                    )
                }

                objView.quest_objective_edit_text.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        try {
                            viewEditQuestViewModel.onObjectiveEdit(
                                objective,
                                objView.quest_objective_edit_text.text.toString()
                            )
                            objView.quest_objective_edit_text.isFocusableInTouchMode = false
                            objView.quest_objective_edit_text.isCursorVisible = false
                            objView.clearFocus()

                            val imm =
                                application.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
                            imm!!.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)

                            binding.viewEditQuestTitleEditText.clearFocus()
                            binding.viewEditQuestDescriptionEditText.clearFocus()
                        } catch (e: Exception) {
                            Log.e(TAG, "onEditorAction: $e")
                        }
                    }
                    false
                }
            }
        })

        // we need to observe both of these because without the full list it won't initialize
        // and without the individual it won't add new ones
        viewEditQuestViewModel.rewards.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "observe rewards full list")
            if (viewEditQuestViewModel.rewards.value == null) {
                Log.e(
                    TAG,
                    "viewEditQuestViewModel.rewards is null. Ending observation (rewards) early."
                )
                return@Observer
            }

            rewardsAdapter.data = viewEditQuestViewModel.rewards.value!!
        })

        viewEditQuestViewModel.modifiedReward.observe(viewLifecycleOwner, Observer {
            if (viewEditQuestViewModel.rewards.value == null) {
                Log.w(
                    TAG,
                    "viewEditQuestViewModel.rewards is null. Ending observation (modifiedReward) early."
                )
                return@Observer
            }

            rewardsAdapter.data = viewEditQuestViewModel.rewards.value!!
        })

        // add reward button
        binding.addRewardsButton.setOnClickListener {
            val dialog = AddRewardDialogFragment()
            dialog.onPositiveButtonClicked = {
                viewEditQuestViewModel.onPositiveButtonClicked(it)
            }
            dialog.show(childFragmentManager, "addRewards")
        }

        // add notification button
        binding.addFamiliarNotificationButton.setOnClickListener {
            val dialog = DatePickerDialogFragment()
            val timeDialog = TimePickerDialogFragment(true)

            val alarm: Calendar = Calendar.getInstance()

            dialog.onDateSet = { year, month, day ->
                Log.d(TAG, "Date picked: $year $month $day")

                alarm.add(Calendar.YEAR, (year - alarm.get(Calendar.YEAR)))
                alarm.add(Calendar.MONTH, (month - alarm.get(Calendar.MONTH)))
                alarm.add(Calendar.DATE, (day - alarm.get(Calendar.DATE)))

                timeDialog.show(childFragmentManager, "addTime")
            }
            timeDialog.onTimeSet = { hour, minute ->
                Log.d(TAG, "Time picked: $hour:$minute ")

                alarm.add(Calendar.HOUR_OF_DAY, (hour - alarm.get(Calendar.HOUR_OF_DAY)))
                alarm.add(Calendar.MINUTE, (minute - alarm.get(Calendar.MINUTE)))

                // thanks https://gist.github.com/BrandonSmith/6679223
                scheduleNotification(getNotification(
                    binding.viewEditQuestTitleEditText.text.toString(),
                    binding.viewEditQuestDescriptionEditText.text.toString()),
                    alarm.timeInMillis)

                Log.d(TAG, "CurrentTime: ${System.currentTimeMillis()}")
                Log.d(TAG, "Alarm set for ${alarm.timeInMillis}")

                // todo call viewmodel onSaveNotificationTime / onSetNotificationTime
            }
            timeDialog.onDismiss = {
                Log.d(TAG, "timeDialog dismissing")
                // todo call viewModel onSaveNotificationTime / onSetNotificationTime
            }

            dialog.show(childFragmentManager, "addDate")
        }

        // navigation
        viewEditQuestViewModel.navigateToQuestsViewModel.observe(viewLifecycleOwner, Observer {
            // todo test with activity!!.onBackPressed()
            if (this.findNavController().currentDestination?.id == R.id.viewEditQuestFragment) {
                this.findNavController().navigate(
                    ViewEditQuestFragmentDirections.actionViewEditQuestFragmentToMainViewFragment()
                )
            } else {
                Log.e(
                    TAG,
                    "Current destination is " + this.findNavController().currentDestination?.label + " instead of R.id.viewEditQuestFragment!"
                )
                return@Observer // this is a hack, otherwise this becomes an infinite loop
            }

            viewEditQuestViewModel.doneNavigating()
        })

        Log.d(TAG, "Current destination is " + this.findNavController().currentDestination?.label)

        // improving EditText UX
        binding.viewEditQuestTitleEditText.setRawInputType(InputType.TYPE_CLASS_TEXT)
        binding.viewEditQuestTitleEditText.setClearFocusOnDone()
        binding.viewEditQuestDescriptionEditText.setClearFocusOnDone()

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity!!.menuInflater.inflate(R.menu.menu_actionbar, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (questId == "") menu.removeItem(R.id.action_abandon_quest)
        super.onPrepareOptionsMenu(menu)
    }

    // handle button activities
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (viewModel == null) {
            Toast.makeText(this.context, "Please try again.", Toast.LENGTH_SHORT).show()
            Log.e("ViewEditQuestFragment: onOptionsItemSelected", "viewModel is null!!")
            return false
        }

        val id = item.itemId

        // save/delete quest
        if (id == R.id.action_done_editing) {

            viewModel!!.onSaveQuest()
        }
        if (id == R.id.action_abandon_quest) {
            viewModel!!.onDeleteQuest()
        }
        return super.onOptionsItemSelected(item)
    }

    // private
    // -------------------------------------------------------------------

    /** Schedule a notification to happen later in time. */
    private fun scheduleNotification(notification: Notification, notificationTime: Long)
    {
        val notificationIntent = Intent(context, NotificationReceiver::class.java)
        notificationIntent.putExtra(NotificationIntentService.NOTIFICATION_ID, NotificationIntentService.NotificationId)
        notificationIntent.putExtra(NotificationIntentService.NOTIFICATION, notification)
        //val pendingIntent = PendingIntent.getService(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = context!!.getSystemService(ALARM_SERVICE) as AlarmManager?
        alarmManager!!.set(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent)
    }

    /** Build and return a [Notification]. */
    private fun getNotification(title:String, content: String) : Notification
    {
        // build notification channel
        val notificationChannelId = NotificationUtil.createNotificationChannel(
            context!!,
            channelId,
            getString(R.string.familiar_quest_reminders),
            getString(R.string.familiar_quest_reminders_description),
            NotificationManager.IMPORTANCE_DEFAULT,
            true,
            Notification.VISIBILITY_PUBLIC
        )

        // to make a new notification id, add
        // builder.addExtras(NOTIFICATION_ID, number)
        // and then get it in the receiver.

        val dismissIntent = Intent(context, NotificationIntentService::class.java)
        dismissIntent.action = NotificationIntentService.ACTION_DISMISS
        dismissIntent.putExtra(NotificationIntentService.NOTIFICATION_ID, NotificationIntentService.NotificationId)

    //    val dismissPendingIntent = PendingIntent.getBroadcast(context, 0, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT )
        val dismissPendingIntent = PendingIntent.getService(context, 0, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT )

        val builder = NotificationCompat.Builder(context!!, notificationChannelId!!)
        builder.setContentTitle(title)
        builder.setContentText(content)
        builder.setSmallIcon(R.drawable.ic_scroll_quill)
        builder.priority = NotificationCompat.PRIORITY_DEFAULT
        builder.setAutoCancel(true)
        builder.addAction(R.drawable.ic_scroll_quill, "Dismiss", dismissPendingIntent)
        builder.addAction(
            NotificationUtil.createSnoozeAction(context!!)
        )
     //  builder.addAction(
     //      NotificationUtil.createDismissAction(context!!)
     //  )
        return builder.build()
    }

    companion object {
        const val channelId:String = "familiar_channel"

        // -------------------------- log tag ------------------------------ //
        private const val TAG: String = "KSLOG: ViewEditQuestFragment"
    }

}