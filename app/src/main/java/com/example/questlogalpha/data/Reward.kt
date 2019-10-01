package com.example.questlogalpha.data

import android.util.Log
import androidx.room.TypeConverter
import org.json.JSONObject

class Reward(rewardId: String, amt: Double = 0.0) {
    var id = rewardId
    var amount: Double = amt
}

class RewardArrayConverter {
    @TypeConverter
    fun stringToReward(value: String): Array<Reward> = value.let {
        val rewardArray = mutableListOf<Reward>()

        val obj: JSONObject = JSONObject(value)
        for (id in obj.keys()) {
            val r = Reward(id, obj.getDouble(id))
            rewardArray.add(r)
            Log.d(TAG, "stringToSkillReward: id: $id, amount: ${obj.getDouble(id)}")
        }

        return rewardArray.toTypedArray()
    }

    @TypeConverter
    fun RewardToString(rewards: Array<Reward>): String? {
        val jsonObject: JSONObject = JSONObject()
        for (reward in rewards) {
            jsonObject.put(reward.id, reward.amount)
        }

        Log.d(TAG, "RewardArrayConverter: skillRewardToString: jsonObject: \n $jsonObject")

        return jsonObject.toString()
    }

    // -------------------------- log tag ------------------------------ //
    companion object {
        const val TAG: String = "KSLOG: Reward.kt"
    }
}
