package com.example.fooddex

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object NotificationSender {
    private val key = "AAAA2aoRqwk:APA91bFGtBQyYLp6CgBqZYyfSXJt4AR2nAs7_zQpz14lWykfvdadyw3yAWgFC5zxA" +
            "-wnCbKBSSejSM1h07EYaEaOdAig5p1DZ32c8C-Rj5p3KmIKBp900Sanf53ggJCWcUQ11AdrH5Tz"

    private val client = OkHttpClient()

    fun sendNotificationToDevice(token: String, title:String, body: String, data: Map<String, String> = emptyMap()){
        val url = "https://fcm.googleapis.com/fcm/send"

        val bodyJson = JSONObject()
        bodyJson.put("to", token)
        bodyJson.put("notification",
            JSONObject().also {
                it.put("title", title)
                it.put("body", body)
            }
        )
        bodyJson.put("data", JSONObject(data))

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "key=$key")
            .post(
                bodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            )
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FCM API", e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("FCM API","Received data: ${response.body?.string()}")
            }

        })
    }

    fun notifyFamily(title: String, body:String) {
        val auth = Firebase.auth
        val dbReference = Firebase.database.reference

        val currentUser = auth.currentUser!!.uid
        val userRef = dbReference.child("users").child(currentUser)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChild("familyId")) {
                    val familyId = snapshot.child("familyId").value as String
                    if (familyId.isNotEmpty()) {

                        val familyRef = dbReference.child("families").child(familyId)

                        familyRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val family = snapshot.getValue(Family::class.java)

                                if (family != null){
                                    for (member in family.members){

                                        //Don't send Notification if to the user who made the meal
                                        if(member == currentUser){
                                            continue
                                        }

                                        val tokensRef = dbReference.child("fcm_tokens").child(member)

                                        tokensRef.addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                for (tokenSnapshot in dataSnapshot.children) {
                                                    val token = tokenSnapshot.getValue(String::class.java)
                                                    if (token != null) {
                                                        sendNotificationToDevice(token, title, body)
                                                    }
                                                }
                                            }
                                            override fun onCancelled(databaseError: DatabaseError) {
                                            }
                                        })

                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                            }

                        })

                    } else {
                        // Handle the case where familyId is empty or not available
                    }
                } else {
                    // Handle the case where familyId is not available in the database
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })


    }
}