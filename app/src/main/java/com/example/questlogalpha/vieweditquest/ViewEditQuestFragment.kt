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
import androidx.core.app.NotificationCompat
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
import com.example.questlogalpha.quests.AddRewardDialogFragment
import com.example.questlogalpha.quests.Difficulty
import kotlinx.android.synthetic.main.fragment_view_edit_quest.*
import kotlinx.android.synthetic.main.quest_objective_view.view.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs

/** ************************************************************************************************
 * [Fragment] to display all data relating to a quest.
 * ********************************************************************************************** */
class ViewEditQuestFragment : Fragment() {

    private var viewModel: ViewEditQuestViewModel? = null

    private var questId: String = ""
    private var bind: FragmentViewEditQuestBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentViewEditQuestBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_edit_quest, container, false)
        bind = binding
        val application = requireNotNull(this.activity).application
        val dataSource = QuestLogDatabase.getInstance(application).questLogDatabaseDao
        val globalVariables = QuestLogDatabase.getInstance(application).globalVariableDatabaseDao
        val arguments = ViewEditQuestFragmentArgs.fromBundle(arguments)

        questId = arguments.questId

        val viewModelFactory = ViewModelFactory(arguments.questId, dataSource, null, globalVariables, application)
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

        // update date on load
        viewEditQuestViewModel.date.observeOnce {
            if(viewEditQuestViewModel.date.value != null) {
                val date = viewEditQuestViewModel.date.value!!
                val calendar: Calendar = Calendar.getInstance()
                val years = date.year - calendar.get(Calendar.YEAR)
                val months = date.monthValue - calendar.get(Calendar.MONTH)
                val days = date.dayOfMonth - calendar.get(Calendar.DATE)
                val hours = date.hour - calendar.get(Calendar.HOUR_OF_DAY)
                val minutes = date.minute - calendar.get(Calendar.MINUTE)

                val text = buildCountdownText(years, months, days, hours, minutes)
                binding.viewEditQuestBookmarkText.text = text
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
            timeDialog.onTimeSet = onTimeSet@{ hour, minute ->
                alarm.add(Calendar.HOUR_OF_DAY, (hour - alarm.get(Calendar.HOUR_OF_DAY)))
                alarm.add(Calendar.MINUTE, (minute - alarm.get(Calendar.MINUTE)))

                Log.d(TAG, "Time picked: $hour:$minute " +
                        "\n timeInIMillis: ${alarm.timeInMillis}")

                if(System.currentTimeMillis() > alarm.timeInMillis) {
                    Util.showShortToast(context!!, "Can't set an alarm for the past!")
                    return@onTimeSet
                }

                val textStoredNotification: StoredNotification = StoredNotification(channelId, id=viewEditQuestViewModel.getNotificationId())

                textStoredNotification.notificationTime = alarm.timeInMillis
                textStoredNotification.channelPriority = NotificationManager.IMPORTANCE_DEFAULT

                val extras = HashMap<String, Int>()
                val storedDismissIntent = StoredIntent(NotificationIntentService.ACTION_DISMISS, extras, viewEditQuestViewModel.getNotificationId())
                val storedDismissPendingIntent = StoredPendingIntent(textStoredNotification.id, storedDismissIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                val actionList = arrayListOf<StoredAction>()
                actionList.add(NotificationUtil.createStoredSnoozeAction())

                actionList.add(StoredAction(R.drawable.ic_scroll_quill, getString(R.string.dismiss), storedDismissPendingIntent))

                textStoredNotification.contentText = binding.viewEditQuestDescriptionEditText.text.toString()
                textStoredNotification.contentTitle = binding.viewEditQuestTitleEditText.text.toString()
                textStoredNotification.autoCancel = false
                textStoredNotification.icon = R.drawable.ic_scroll_quill
                textStoredNotification.actions = actionList

                val storedDeleteIntent = StoredIntent(NotificationIntentService.ACTION_DISMISS_SWIPE, extras, viewEditQuestViewModel.getNextNotificationId())
                val storedDeletePendingIntent = StoredPendingIntent(textStoredNotification.id, storedDeleteIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                textStoredNotification.deleteIntent = storedDeletePendingIntent

                viewModel!!.onAddStoredNotification(textStoredNotification)
            }
            timeDialog.onDismiss = {
                Log.d(TAG, "timeDialog dismissing")
                // todo set notification for just the day
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
        activity!!.menuInflater.inflate(R.menu.menu_actionbar, menu)
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
                if(System.currentTimeMillis() > notification.notificationTime) {
                    Log.d(TAG, "storedNotification: notification ${notification.id} is in the past. Deleting it")
                    viewModel!!.onRemoveStoredNotification(notification)
                    continue
                }

                Log.d(TAG, "*****************************************")
                Log.d(TAG, "storedNotification: $notification")
                Log.d(TAG, "CurrentTime: ${System.currentTimeMillis()}")
                Log.d(TAG, "Alarm set for ${notification.notificationTime}")

                // thanks https://gist.github.com/BrandonSmith/6679223
                if(questId == "") questId = viewModel!!.id.value!!
                val pendingIntent = scheduleNotification(getNotification(
                    notification),
                    notification.notificationTime,
                    notification.id)

                alarms.add(AlarmData(pendingIntent, notification.notificationTime))
            }

            viewModel!!.onSaveQuest(context!!.getSystemService(ALARM_SERVICE) as AlarmManager?, alarms)
        }
        if (id == R.id.action_abandon_quest) {
            viewModel!!.onDeleteQuest()
        }
        // add/delete due date
        if (id == R.id.action_add_due_date) {
            val dialog = DatePickerDialogFragment()
            val timeDialog = TimePickerDialogFragment(true)

            val calendar: Calendar = Calendar.getInstance()
            val differenceCalendar: Calendar = Calendar.getInstance()

            var years = 0
            var months = 0
            var days = 0
            var hours = 0
            var minutes = 0

            dialog.onDateSet = { year, month, day ->
                Log.d(TAG, "Date picked: $year $month $day")

                years = year - calendar.get(Calendar.YEAR)
                months = month - calendar.get(Calendar.MONTH)
                days = day - calendar.get(Calendar.DATE)

                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DATE, day)

                timeDialog.show(childFragmentManager, "addTime")
            }

            // don't check for the past because sometimes we want to purposefully acknowledge a quest is overdue
            timeDialog.onTimeSet = { hour, minute ->
                hours = hour - differenceCalendar.get(Calendar.HOUR_OF_DAY)
                minutes = minute - differenceCalendar.get(Calendar.MINUTE)

                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)

                viewModel!!.onSetDate(ZonedDateTime.of(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DATE),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    0, // we're not getting that specific lol
                    0,
                    ZoneId.systemDefault()
                ))

                if(bind != null) {
                    val text = buildCountdownText(years, months, days, hours, minutes)
                    bind!!.viewEditQuestBookmarkText.text = text
                }
                else Log.e(TAG, "bind is null!")
            }
            dialog.show(childFragmentManager, "addDate")
        }
        if (id == R.id.action_remove_due_date) {
            viewModel!!.onRemoveDate()
        }
        return super.onOptionsItemSelected(item)
    }

    // private
    // -------------------------------------------------------------------

    /** Formats the display string for the due date. */
    private fun buildCountdownText(years: Int, months: Int, days: Int, hours: Int, minutes: Int): String {
        var hitNegative = false
        var text = ""

        if(years != 0) {
            if(years < 0) hitNegative = true
            text += "${years}y "
            if(text.length >= 8) {
                text =  text.substring(0,7) + "+y"
                return text
            }
        }
        if(months != 0) {
            text += "${if(hitNegative){abs(months)} else {months}}m "
            if(months < 0 && !hitNegative) hitNegative = true
        }
        if(days != 0) {
            text += "${if(hitNegative){abs(days)} else {days}}d "
            if(days < 0 && !hitNegative) hitNegative = true
        }
        if(hours != 0) {
            text += "${if(hitNegative){abs(hours)} else {hours}}h "
            if(hours < 0 && !hitNegative) hitNegative = true
        }
        if(minutes != 0) {
            text += "${if(hitNegative){abs(minutes)} else {minutes}}m "
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

    /** Build and return a [Notification] out of a stored [notification]. */
    private fun getNotification(notification: StoredNotification) : Notification
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

        val builder = NotificationCompat.Builder(context!!, notificationChannelId!!)

        // iterate through actions and build them
        for (action in notification.actions){
            val intent = makeGenericIntent(action.intent)
            assert(intent != null) { "intent for action ${action.id} is null" }
            val pendingIntent = PendingIntent.getService(context, action.intent.requestCode, intent!!, action.intent.flags)
            builder.addAction(action.iconPath, action.title, pendingIntent)
        }
        builder.setContentTitle(notification.contentTitle)
        builder.setContentText(notification.contentText)
        builder.setSmallIcon(notification.icon)
        builder.priority = notification.channelPriority
        builder.setAutoCancel(notification.autoCancel)

        // make the delete intent
        val deleteIntent = makeGenericIntent(notification.deleteIntent)
        if(deleteIntent != null) {
            val deletePendingIntent = PendingIntent.getService(
                context,
                notification.deleteIntent!!.requestCode,
                deleteIntent,
                notification.deleteIntent!!.flags
            )
            builder.setDeleteIntent(deletePendingIntent)
        }

        Log.d(TAG, "getNotification ************************ " +
            "\n contentTitle: ${notification.contentTitle}" +
            "\n contentText: ${notification.contentText}" +
            "\n priority: ${notification.channelPriority}" +
            "\n icon path: ${notification.icon}" +
            "\n setAutoCancel: ${notification.autoCancel}" +
            "\n deleteIntent: ${notification.deleteIntent}" +
            "\n actions: ${notification.actions}")

        return builder.build()
    }

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