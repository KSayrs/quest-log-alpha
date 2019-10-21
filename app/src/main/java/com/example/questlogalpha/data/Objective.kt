package com.example.questlogalpha.data

import android.util.Log
import androidx.room.TypeConverter
import org.json.JSONObject
import java.util.*

data class Objective(
    var description: String = "",
    var completed: Boolean = false,
    val id: String = UUID.randomUUID().toString()
)

class ObjectiveArrayConverter {
    @TypeConverter
    fun stringToObjective(value: String): ArrayList<Objective> = value.let {
        val objArray = ArrayList<Objective>()

        val obj: JSONObject = JSONObject(value)
        for (id in obj.keys()) {
            val objObj = obj.getJSONObject(id)
            val o = Objective(objObj.getString("description"), objObj.getBoolean("completed"), id)
            objArray.add(o)

            Log.d(TAG, "stringToObjective: id: $id, description: ${objObj.getString("description")}")
        }

        return objArray
    }

    /* This creates a json that looks like this:
        LONGASJAVASTRINGID {
            "description": "some text here",
            "completed": true
        }
        LONGASJAVASTRINGID2 {
            "description": "some other text here",
            "completed": false
        }
        etc...
     */

    @TypeConverter
    fun objectiveToString(objectives: ArrayList<Objective>): String? {
        val jsonObject: JSONObject = JSONObject()

        for (objective in objectives) {
            val obj: JSONObject = JSONObject()
            obj.put("description", objective.description)
            obj.put("completed", objective.completed)

            jsonObject.put(objective.id, obj)
        }

        Log.d(TAG, "objectiveToString: jsonObject: \n $jsonObject")

        return jsonObject.toString()
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: Objective.kt"
    }
}
