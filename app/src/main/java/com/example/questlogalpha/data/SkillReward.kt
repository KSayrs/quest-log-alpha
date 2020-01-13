package com.example.questlogalpha.data

import android.util.Log
import androidx.room.TypeConverter
import org.json.JSONObject

// todo change this from string to actual type. Maybe do an interface that SkillReward and ItemReward can inherit from.


class SkillReward(rewardId: String, amt: Double = 0.0, rewardName: String) {
    var id = rewardId
    var amount: Double = amt
    var name: String = rewardName
}

class RewardArrayConverter {
    @TypeConverter
    fun stringToReward(value: String): ArrayList<SkillReward> = value.let {
        val rewardArray = ArrayList<SkillReward>()

        val obj = JSONObject(value)
        for (id in obj.keys()) {
            val objObj = obj.getJSONObject(id)
            val r = SkillReward(id, objObj.getDouble("amount"), objObj.getString("name"))
            rewardArray.add(r)

            Log.d(TAG, "stringToSkillReward: id: $id, amount: ${objObj.getDouble("amount")}, name: ${objObj.getString("name")}")
        }

        return rewardArray
    }

    @TypeConverter
    fun RewardToString(rewards: ArrayList<SkillReward>): String? {
        val jsonObject: JSONObject = JSONObject()
        for (reward in rewards) {
            val obj = JSONObject()
            obj.put("amount", reward.amount)
            obj.put("name", reward.name)

            jsonObject.put(reward.id, obj)
           // jsonObject.put(reward.id, reward.amount)
        }

        Log.d(TAG, "RewardArrayConverter: skillRewardToString: jsonObject: \n $jsonObject")

        return jsonObject.toString()
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: Reward.kt"
    }
}
