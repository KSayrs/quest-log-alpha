package com.example.questlogalpha.vieweditquest

import android.app.*
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
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.questlogalpha.*
import com.example.questlogalpha.data.*
import com.example.questlogalpha.databinding.FragmentViewEditQuestBinding
import com.example.questlogalpha.databinding.QuestObjectiveViewBinding
import com.example.questlogalpha.notifications.AlarmData
import com.example.questlogalpha.notifications.NotificationIntentService
import com.example.questlogalpha.notifications.NotificationReceiver
import com.example.questlogalpha.notifications.NotificationUtil
import com.example.questlogalpha.quests.AddRewardDialogFragment
import com.example.questlogalpha.quests.Difficulty
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.collections.HashMap
import kotlin.math.abs

/** ************************************************************************************************
 * [Fragment] to display all data relating to a quest.
 * ********************************************************************************************** */
class ViewEditQuestFragment : Fragment() {

    private var viewModel: ViewEditQuestViewModel? = null

    private var questId: String = ""
    private var _binding: FragmentViewEditQuestBinding? = null
    private val binding get() = _binding!!
    private var nAdapter: NotificationsAdapter? = null
    private var rAdapter: RewardsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentViewEditQuestBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_edit_quest, container, false)
        _binding = binding
        val application = requireNotNull(this.activity).application
        val dataSource = QuestLogDatabase.getInstance(application).questLogDatabaseDao
        val globalVariables = QuestLogDatabase.getInstance(application).globalVariableDatabaseDao
        val arguments = ViewEditQuestFragmentArgs.fromBundle(arguments)

        questId = arguments.questId

        val viewModelFactory = ViewModelFactory(arguments.questId, dataSource, globalVariableDataSource = globalVariables, application = application)
        val viewEditQuestViewModel = ViewModelProvider(this, viewModelFactory).get(ViewEditQuestViewModel::class.java)

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
        rAdapter = rewardsAdapter

        // set up notification adapter
        val notificationsAdapter = NotificationsAdapter()
        nAdapter = notificationsAdapter
        binding.familiarNotificationsList.adapter = notificationsAdapter

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

        // update date on load
        viewEditQuestViewModel.date.observeOnce {
            if(viewEditQuestViewModel.date.value != null) setDueDate()
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

                objectiveBinding.deleteObjectiveIcon.setOnClickListener {
                    viewEditQuestViewModel.onObjectiveDeleted(objective)
                    binding.objectives.removeView(objView)
                }

                objectiveBinding.editObjectiveIcon.setOnClickListener {
                    objectiveBinding.questObjectiveEditText.focus(application)
                }

                objectiveBinding.questObjectiveEditText.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        try {
                            viewEditQuestViewModel.onObjectiveEdit(
                                objective,
                                objectiveBinding.questObjectiveEditText.text.toString()
                            )
                            objectiveBinding.questObjectiveEditText.isFocusableInTouchMode = false
                            objectiveBinding.questObjectiveEditText.isCursorVisible = false
                            objView.clearFocus()

                            val imm = application.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
                            imm!!.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)

                            binding.viewEditQuestTitleEditText.clearFocus()
                            binding.viewEditQuestDescriptionEditText.clearFocus()
                        } catch (e: Exception) {
                            Log.e(TAG, "onEditorAction: $e")
                        }
                    }
                    false
                }

                // if this is a new objective we just added, focus it
                if(viewEditQuestViewModel.isNewObjective && viewEditQuestViewModel.modifiedObjective != null) {
                    objectiveBinding.questObjectiveEditText.focus(application)
                }
            }
        })

        // populate notifications
        viewEditQuestViewModel.storedNotifications.observe(viewLifecycleOwner, Observer {
            it?.let {
                notificationsAdapter.data = it
                notificationsAdapter.onItemRemoved = {
                    viewModel!!.onRemoveStoredNotification(it)
                }
                notificationsAdapter.questDueDate = viewEditQuestViewModel.date.value
                Log.d(TAG, "observenotifications: notificationsAdapter.questDueDate: ${notificationsAdapter.questDueDate}")
                Log.d(TAG, "observenotifications: notificationsAdapter.itemCount: ${notificationsAdapter.itemCount}")
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

        // add reward button
        binding.addRewardsButton.setOnClickListener {
            val dialog = AddRewardDialogFragment()
            dialog.onPositiveButtonClicked = {
                viewEditQuestViewModel.onPositiveButtonClicked(it)
                rAdapter!!.notifyDataSetChanged()
            }
            dialog.show(childFragmentManager, "addRewards")
        }

        // add notification button
        binding.addFamiliarNotificationButton.setOnClickListener {
            if(viewModel!!.date.value == null) {
                Util.showShortToast(requireContext(), "Set a due date first!")
                return@setOnClickListener
            }

            val notificationDialogFragment = PickNotificationTimeDialogFragment()
            notificationDialogFragment.questDueDate = viewModel!!.date.value
            Log.d(TAG, "System: ${System.currentTimeMillis()} | Quest: ${viewModel!!.date.value!!.toInstant().toEpochMilli()}")

            notificationDialogFragment.show(childFragmentManager, "notificationDialogFragment")
            notificationDialogFragment.onPositiveButtonClicked = onPositiveButtonClicked@{ chosenTime ->
                if (System.currentTimeMillis() > chosenTime) {
                    Util.showShortToast(requireContext(), "Can't set an alarm for the past!")
                    return@onPositiveButtonClicked
                }

                val textStoredNotification: StoredNotification = StoredNotification(channelId, id = viewModel!!.getNotificationId())

                textStoredNotification.notificationTime = chosenTime
                textStoredNotification.channelPriority = NotificationManager.IMPORTANCE_DEFAULT

                val extras = HashMap<String, Int>()
                val storedDismissIntent = StoredIntent(NotificationIntentService.ACTION_DISMISS, extras, viewModel!!.getNotificationId())
                val storedDismissPendingIntent = StoredPendingIntent(textStoredNotification.id, storedDismissIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                val actionList = arrayListOf<StoredAction>()

                actionList.add(NotificationUtil.createStoredSnoozeAction())

                actionList.add(StoredAction(R.drawable.ic_scroll_quill, getString(R.string.dismiss), storedDismissPendingIntent))

                textStoredNotification.contentText = this.binding.viewEditQuestDescriptionEditText.text.toString()
                textStoredNotification.contentTitle = this.binding.viewEditQuestTitleEditText.text.toString()
                textStoredNotification.autoCancel = false
                textStoredNotification.icon = R.drawable.ic_scroll_quill
                textStoredNotification.bigIcon = if (viewEditQuestViewModel.currentFamiliar.value != null) viewEditQuestViewModel.currentFamiliar.value!!.value else R.drawable.ic_scroll_quill
                textStoredNotification.actions = actionList

                val storedDeleteIntent = StoredIntent(NotificationIntentService.ACTION_DISMISS_SWIPE, extras, viewModel!!.getNextNotificationId())
                val storedDeletePendingIntent = StoredPendingIntent(textStoredNotification.id, storedDeleteIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                textStoredNotification.deleteIntent = storedDeletePendingIntent

                viewModel!!.onAddStoredNotification(textStoredNotification)
                nAdapter!!.notifyDataSetChanged()
            }
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

    // Show the navigation bar when we're on this fragment
    override fun onResume() {
        super.onResume()
        Log.d("$TAG: onResume", " called")

        val actionbar = (activity as AppCompatActivity).supportActionBar
        if(actionbar == null){
            Log.e(TAG, "onResume: supportActionBar is null.")
        }
        else {
            if(!actionbar.isShowing) actionbar.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        requireActivity().menuInflater.inflate(R.menu.menu_actionbar, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (questId == "") menu.removeItem(R.id.action_abandon_quest)
        if (viewModel == null) Log.e(TAG, "onPrepareOptionsMenu: viewmodel is not ready yet")
        else {
            if (viewModel!!.date.value != null) {
                menu.findItem(R.id.action_add_due_date).isVisible = false
                menu.findItem(R.id.action_remove_due_date).isVisible = true
            }
            else {
                menu.findItem(R.id.action_remove_due_date).isVisible = false
                menu.findItem(R.id.action_add_due_date).isVisible = true
            }
        }

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
            saveQuest()
        }
        if (id == R.id.action_abandon_quest) {
            viewModel!!.onDeleteQuest()
        }
        // add/delete due date
        if (id == R.id.action_add_due_date) {
            handleAddDueDate()
        }
        if (id == R.id.action_remove_due_date) {
            viewModel!!.onRemoveDate()
            nAdapter!!.notifyDataSetChanged()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveQuest() {
        if(viewModel!!.date.value != null) {
            val notifications = arrayListOf<StoredNotification>()
            notifications.addAll(viewModel!!.storedNotifications.value!!)
            val alarms = arrayListOf<AlarmData>()
            for (notification in notifications) {

                // check for past alarms and remove them
                if (notification.notificationTime == 1L || notification.id == -1) {
                    Log.d(TAG, "*****************************************")
                    Log.d(TAG, "storedNotification: notification ${notification.id} is invalid. Deleting it")
                    viewModel!!.onRemoveStoredNotification(notification)
                    continue
                }
                if (System.currentTimeMillis() > notification.notificationTime) {
                    Log.d(TAG, "storedNotification: notification ${notification.id} is in the past. Deleting it")
                    viewModel!!.onRemoveStoredNotification(notification)
                    continue
                }

                Log.d(TAG, "*****************************************")
                Log.d(TAG, "storedNotification: $notification")
                Log.d(TAG, "CurrentTime: ${System.currentTimeMillis()}")
                Log.d(TAG, "Alarm set for ${notification.notificationTime}")

                // thanks https://gist.github.com/BrandonSmith/6679223
                if (questId == "") questId = viewModel!!.id.value!!
                val pendingIntent = scheduleNotification(
                    NotificationUtil.getNotification(
                        notification,
                        questId,
                        context
                    ),
                    notification.notificationTime,
                    notification.id
                )

                alarms.add(AlarmData(pendingIntent, notification.notificationTime))
            }

            viewModel!!.onSaveQuest(requireContext().getSystemService(ALARM_SERVICE) as AlarmManager?, alarms)
        }
        else {
            viewModel!!.onSaveQuest()
        }
    }

    // private
    // -------------------------------------------------------------------

    private fun handleAddDueDate() {
        val dialog = DatePickerDialogFragment()
        val timeDialog = TimePickerDialogFragment(true)

        val calendar: Calendar = Calendar.getInstance()

        dialog.onDateSet = { year, month, day ->
            Log.d(TAG, "handleAddDueDate: Date picked: $year $month $day")

            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)

            timeDialog.show(childFragmentManager, "addTime")
        }

        // don't check for the past because sometimes we want to purposefully acknowledge a quest is overdue
        timeDialog.onTimeSet = { hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)

            viewModel!!.onSetDate(
                ZonedDateTime.of(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH)+1,
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    0, // we're not getting that specific lol
                    0,
                    ZoneId.systemDefault()
                )
            )

            Log.d(TAG, "onTimeSet: duedate: ${viewModel!!.date.value} | now: ${ZonedDateTime.now()}")
            nAdapter!!.questDueDate = viewModel!!.date.value

            setDueDate()
        }
        dialog.show(childFragmentManager, "addDate")
    }

    /** Set the text for the due date flag */
    private fun setDueDate() {
        if(viewModel!!.date.value != null) {
            var remainingTime = (viewModel!!.date.value!!).toEpochSecond() - ZonedDateTime.now().toEpochSecond()

            val weeksDifference = remainingTime / NotificationUtil.SECONDS_PER_WEEK
            Log.d(TAG, "weeksDifference: + $weeksDifference")

            if(weeksDifference >= 1) { remainingTime %= NotificationUtil.SECONDS_PER_WEEK  }

            val daysDifference = remainingTime / NotificationUtil.SECONDS_PER_DAY
            Log.d(TAG, "daysInDifference: + $daysDifference")

            if(daysDifference >= 1) { remainingTime %= NotificationUtil.SECONDS_PER_DAY }

            val hoursDifference = remainingTime / NotificationUtil.SECONDS_PER_HOUR
            Log.d(TAG, "hoursDifference: + $hoursDifference")

            if(hoursDifference >= 1) { remainingTime %= NotificationUtil.SECONDS_PER_HOUR  }

            val minutesDifference = remainingTime / NotificationUtil.SECONDS_PER_MINUTE
            Log.d(TAG, "minutesDifference: + $minutesDifference")

            val text = buildCountdownText(0, 0, weeksDifference, daysDifference, hoursDifference, minutesDifference)
            binding!!.viewEditQuestBookmarkText.text = text
        }
        else {
            Log.e(TAG, "viewModel!!.date.value is null -- cannot set due date flag")
        }
    }

    /** Formats the display string for dates as such: 99y 99m 99w 99d 99h 99m */
    private fun buildCountdownText(years: Long, months: Long, weeks: Long, days: Long, hours: Long, minutes: Long): String {
        var hitNegative = false
        var isFirst = true
        var text = ""

        if(years != 0L) {
            isFirst = false
            if(years < 0) hitNegative = true
            text += "${years}y "
            if(text.length >= 8) {
                text =  text.substring(0,7) + "+y"
                return text
            }
        }
        if(months != 0L) {
            text += "${if(hitNegative || !isFirst){abs(months)} else {months}}m "
            if(months < 0 && !hitNegative) hitNegative = true
            if(isFirst) isFirst = false
        }
        if(weeks != 0L) {
            text += "${if(hitNegative || !isFirst){abs(weeks)} else {weeks}}w "
            if(weeks < 0 && !hitNegative) hitNegative = true
            if(isFirst) isFirst = false
        }
        if(days != 0L) {
            text += "${if(hitNegative || !isFirst){abs(days)} else {days}}d "
            if(days < 0 && !hitNegative) hitNegative = true
            if(isFirst) isFirst = false
        }
        if(hours != 0L) {
            text += "${if(hitNegative || !isFirst){abs(hours)} else {hours}}h "
            if(hours < 0 && !hitNegative) hitNegative = true
            if(isFirst) isFirst = false
        }
        if(minutes != 0L) {
            text += "${if(hitNegative || !isFirst){abs(minutes)} else {minutes}}m "
        }

        if(text.length > 8) {
            val substrings = text.split(" ")
            var currentLength = 0
            for(substring in substrings) {
                if(substring.length + currentLength + 1 <= 8) {  // +1 for the space at the end that's removed for delimiting
                    currentLength += substring.length + 1;
                } else {
                    return text.substring(0, currentLength)
                }
            }
            return text
        }

        return text
    }

    /** Schedule a [notification] with [notificationId] to happen at [notificationTime] (milliseconds). */
    private fun scheduleNotification(notification: Notification, notificationTime: Long, notificationId: Int) : PendingIntent
    {
        Log.d(TAG, "scheduleNotification ************************ ")
        Log.d(TAG, " notificationid: $notificationId")
        Log.d(TAG, " notification: $notification")
        Log.d(TAG, " notificationTime: $notificationTime")
        val notificationIntent = Intent(context, NotificationReceiver::class.java)
        notificationIntent.putExtra(NotificationIntentService.NOTIFICATION_ID, notificationId)
        notificationIntent.putExtra(NotificationIntentService.NOTIFICATION, notification)
        notificationIntent.putExtra(NotificationIntentService.QUEST_ID, questId)
        return PendingIntent.getBroadcast(context, notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /** Make and return an [Intent] from a [storedPendingIntent]s data. */
    private fun makeGenericIntent(storedPendingIntent: StoredPendingIntent?): Intent? {
        if(storedPendingIntent == null) return null
        val intent = Intent(context, NotificationIntentService::class.java)
        intent.action = storedPendingIntent.intent.action
        intent.putExtra(NotificationIntentService.NOTIFICATION_ID, storedPendingIntent.intent.id)
        intent.putExtra(NotificationIntentService.QUEST_ID, questId)
        for(extra in storedPendingIntent.intent.extras)
        {
            Log.d(TAG, "makeGenericIntent: extra: $extra")
            intent.putExtra(extra.key, extra.value)
        }
        return intent
    }

    companion object {
        const val channelId:String = "familiar_channel"

        // -------------------------- log tag ------------------------------ //
        private const val TAG: String = "KSLOG: ViewEditQuestFragment"
    }
}