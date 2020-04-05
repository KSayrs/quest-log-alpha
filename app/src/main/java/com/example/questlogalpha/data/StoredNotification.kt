package com.example.questlogalpha.data

import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.room.TypeConverter
import com.example.questlogalpha.R
import org.json.JSONObject
import java.math.BigInteger
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/** POJO (or POKO I guess) for holding data necessary to build a notification. */
data class StoredNotification(
    var channelId:String = "",
    var channelPriority: Int = NotificationCompat.PRIORITY_DEFAULT,
    var contentTitle:String = "",
    var contentText:String = "",
    var deleteIntent: StoredPendingIntent? = null,
    var icon:Int = R.drawable.ic_scroll_quill,
    var autoCancel:Boolean = false,
    var actions:ArrayList<StoredAction> = arrayListOf(),
    var notificationTime:Long = 1,
    val id: Int = -1
)

/** POJO for holding stored action data for notifications. */
data class StoredAction(
    var iconPath:Int,
    var title:String = "",
    var intent:StoredPendingIntent,
    // this is not actually used to build a notification, but is used to keep the JSON object from merging
    // actions. I KNOW I could use accumulate() instead of put(), but that would take refactoring that
    // I am too tired to do for the time being. It's alpha.
    val id: String = UUID.randomUUID().toString()
)

/** POJO for holding pending intent data. */
data class StoredPendingIntent(
    var requestCode:Int,
    var intent:StoredIntent,
    var flags:Int
) {
    /** Converts a [StoredPendingIntent] to a [JSONObject]. */
    fun toJSON() : JSONObject {
        val pendingIntentObj: JSONObject = JSONObject(
            """{"${StoredPendingIntent::requestCode.name}":${this.requestCode},
                        |"${StoredPendingIntent::flags.name}":${this.flags}}""".trimMargin())

        val intentExtras: JSONObject = JSONObject()
        for((k,v) in this.intent.extras)
        {
            intentExtras.put(k, v)
        }

        // add intent to pending intent
        pendingIntentObj.put(StoredPendingIntent::intent.name, JSONObject(
            """{"${StoredIntent::action.name}":"${this.intent.action}",
                        |"${StoredIntent::extras.name}": $intentExtras,
                        |"${StoredIntent::id.name}": "${this.intent.id}"}""".trimMargin()
        ))

        return pendingIntentObj
    }
}

/** POJO for holding intent data. */
data class StoredIntent(
    var action:String = "",
    var extras:HashMap<String, Int>,
    val id: Int = -1
)

// -------------------------------------------------------------------------------------------------

/** Converts our [StoredNotification] object and its sub-objects to and from a string that can be entered into and read from the database. */
class StoredNotificationArrayConverter {

    @TypeConverter
    fun stringToNotification(value: String): ArrayList<StoredNotification> = value.let {
        val notificationArray = ArrayList<StoredNotification>()
        val obj: JSONObject = JSONObject(value)
        if (obj == {})
        {
            Log.e(TAG, "Empty object in database! Notification will not be returned")
            return notificationArray
        }

        for (id in obj.keys()) {
            val storedNotificationObject = obj.getJSONObject(id)
            val storedNotification = StoredNotification(
                storedNotificationObject.getString(StoredNotification::channelId.name),
                storedNotificationObject.getInt(StoredNotification::channelPriority.name),
                storedNotificationObject.getString(StoredNotification::contentTitle.name),
                storedNotificationObject.getString(StoredNotification::contentText.name),
                storedNotificationObject.getStoredPendingIntent(StoredNotification::deleteIntent.name),
                storedNotificationObject.getInt(StoredNotification::icon.name),
                storedNotificationObject.getBoolean(StoredNotification::autoCancel.name),
                id = id.toInt()
            )
            storedNotification.notificationTime = storedNotificationObject.getLong(StoredNotification::notificationTime.name)

            val actionsObject = storedNotificationObject.getJSONObject(StoredNotification::actions.name)
            val storedActions: ArrayList<StoredAction> = getStoredActions(actionsObject)
            storedNotification.actions = storedActions

            Log.d(TAG, "storedNotification: $storedNotification")
            notificationArray.add(storedNotification)
        }

        return notificationArray
    }

    private fun getStoredActions(actionsObject: JSONObject): ArrayList<StoredAction> {
        val storedActions: ArrayList<StoredAction> = arrayListOf()
        for (actionId in actionsObject.keys()) {
            val actionObject = actionsObject.getJSONObject(actionId)
            val pendingIntent = actionObject.getStoredPendingIntent(StoredAction::intent.name)

            assert(pendingIntent != null) { "$TAG getStoredActions: Assert fail: pending intent is null" }

            val storedAction = StoredAction(
                actionObject.getInt(StoredAction::iconPath.name),
                actionObject.getString(StoredAction::title.name),
                pendingIntent!!
            )

            storedActions.add(storedAction)
        }
        return storedActions
    }

    /**
     * Converts an [ArrayList] of [StoredNotification]s to a json string format that looks like this:
     * LONGJAVAID1 {
     *    "channelId": "channelid",
     *    "channelPriority": 2
     *    "actions": {
     *         "longActionID": {
     *              "iconPath": 143
     *              "title": "text"
     *              "intent": {
     *                   "action": "stringForAction"
     *                   "extras": {
     *                       "SomeExtraNameProbablyAnId": 0,
     *                       "SomeOtherExtraName": 3
     *                    }
     *               }
     *         }
     *     }
     * }
     * etc...
     */
    @TypeConverter
    fun notificationToString(notifications: ArrayList<StoredNotification>): String? {

        if(notifications.size <= 0) {
            Log.d(TAG, "Empty notification array passed. Empty object will be stored")
            return "{}"
        }

        val jsonObject: JSONObject = JSONObject()

        for (notification in notifications) {
            val obj: JSONObject = JSONObject()
            obj.put(StoredNotification::channelId.name, notification.channelId)
            obj.put(StoredNotification::channelPriority.name, notification.channelPriority)
            obj.put(StoredNotification::contentTitle.name, notification.contentTitle)
            obj.put(StoredNotification::contentText.name,  notification.contentText)
            obj.put(StoredNotification::icon.name, notification.icon)
            obj.put(StoredNotification::autoCancel.name, notification.autoCancel)
            obj.put(StoredNotification::notificationTime.name, notification.notificationTime)

            if(notification.deleteIntent == null) obj.put(StoredNotification::deleteIntent.name, JSONObject("{}")) // we store an empty object because JSON really can't deal with nulls
            else obj.put(StoredNotification::deleteIntent.name, notification.deleteIntent!!.toJSON())

            val actionsObject: JSONObject = JSONObject()
            for (action in notification.actions)
            {
                val actionObject: JSONObject = JSONObject()

                actionObject.put(StoredAction::iconPath.name, action.iconPath)
                actionObject.put(StoredAction::title.name, action.title)

                val pendingIntentObj: JSONObject = action.intent.toJSON()

                actionObject.put(StoredAction::intent.name, pendingIntentObj)
                actionsObject.put(action.id, actionObject)
            }

            obj.put(StoredNotification::actions.name, actionsObject)

            jsonObject.put(notification.id.toString(), obj)
        }

        Log.d(TAG, "notificationToString: jsonObject: \n $jsonObject")
        return jsonObject.toString()
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: StoredNotificationArrayConverter"
    }
}

// -------------------------------------------------------------------------------------------------
/**
 * Gets a [StoredPendingIntent] from the [JSONObject] property [name].
 *
 * Extension for JSONObject.
 * */
fun JSONObject.getStoredPendingIntent(name: String) : StoredPendingIntent? {

    val pendingIntentObject = this.getJSONObject(name)
    if(pendingIntentObject == {}) return null

    val intentObject = pendingIntentObject.getJSONObject(StoredPendingIntent::intent.name)
    val extrasObject = intentObject.getJSONObject(StoredIntent::extras.name)

    val extras: HashMap<String, Int> = HashMap()
    for (extraString in extrasObject.keys()) {
        extras[extraString] = extrasObject.getInt(extraString)
    }

    val intent = StoredIntent(
        intentObject.getString(StoredIntent::action.name),
        extras,
        intentObject.getInt(StoredIntent::id.name)
    )

    return StoredPendingIntent(
        pendingIntentObject.getInt(StoredPendingIntent::requestCode.name),
        intent,
        pendingIntentObject.getInt(StoredPendingIntent::flags.name)
    )
}
