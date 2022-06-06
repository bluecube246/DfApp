package com.example.dfappnlp.api

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.*
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.dfappnlp.R
import java.net.URLEncoder

class DfApi(private val ApiKey:String, private val activity: Activity, private val context: Context) {

    fun setServer(init: Boolean = true, name: String = "검제큐브", info: String = "정보") {
        val serverView = activity.findViewById<Spinner>(R.id.spinner)

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(context)
        val url = "https://api.neople.co.kr/df/servers?apikey=$ApiKey"

        // Request a string response from the provided URL.
        val stringRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val servers = response.getJSONArray("rows")
                if (init) {
                    val serverItems = arrayOfNulls<String>(servers.length())

                    for (i in 0 until servers.length()) {
                        val server = servers.getJSONObject(i)
                        serverItems[i] = server.getString("serverName")
                    }

                    val adapter =
                        ArrayAdapter(context, android.R.layout.simple_list_item_1, serverItems)
                    serverView.adapter = adapter

                    val spinnerPoisition = adapter.getPosition("힐더")
                    serverView.setSelection(spinnerPoisition)
                    Log.d("server_info", servers.toString())
                }

                Log.d("selected server", serverView.selectedItem.toString())
                for (i in 0 until servers.length()) {
                    val server = servers.getJSONObject(i)
                    if (server.getString("serverName") == serverView.selectedItem.toString()) {
                        Log.d("selected server id", server.getString("serverId"))
                        updateResults(name, server.getString("serverId"), info)
                    }
                }

            },
            { Log.d("error", "error in set server") })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)

    }

    private fun updateResults(
        name: String = "검제큐브",
        serverId: String = "hilder",
        info: String = "정보"
    ) {
        val textView = activity.findViewById<TextView>(R.id.text)

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(context)
        val url_name: String = URLEncoder.encode(name, "utf-8")
        val url =
            "https://api.neople.co.kr/df/servers/$serverId/characters?characterName=$url_name&apikey=$ApiKey"

        // Request a string response from the provided URL.
        val stringRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                if (response.getJSONArray("rows").length() == 0) {
                    textView.text = "캐릭터: 정보없음"
                } else if (info == "정보") {
                    val server = response.getJSONArray("rows").getJSONObject(0)
                    Log.d("server", server.getString("characterName"))
                    textView.text = "캐릭터: ${server.getString("characterName")}"
                    loadImage(server.getString("characterId"), serverId)
                    loadInfo(server.getString("characterId"), serverId)
                } else {
                    Log.d("no info match", "No info that matches")
                }
            },
            { textView.text = "That didn't work!" })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)

    }

    fun loadImage(
        characterId: String = "9683cde24572f230266aa74964139bcc",
        serverId: String = "hilder"
    ) {
        val queue = Volley.newRequestQueue(context)
        val imageUrl =
            "https://img-api.neople.co.kr/df/servers/$serverId/characters/$characterId?zoom=3&apikey=$ApiKey"
        val mImageView = activity.findViewById<ImageView>(R.id.imageView2)

        val imageRequest = ImageRequest(imageUrl,
            { bitmap ->
                mImageView.setImageBitmap(bitmap)
            }, 0, 0,
            ImageView.ScaleType.CENTER_CROP,
            Bitmap.Config.ARGB_8888,
            {
                Log.d("error", "cannot load image")
            }
        )

        queue.add(imageRequest)
    }

    private fun loadInfo(
        characterId: String = "9683cde24572f230266aa74964139bcc",
        serverId: String = "hilder"
    ) {
        val info_list = activity.findViewById<ListView>(R.id.listview)

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(context)
        val url =
            "https://api.neople.co.kr/df/servers/$serverId/characters/$characterId?apikey=$ApiKey"

        // Request a string response from the provided URL.
        val stringRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val listItems = arrayOfNulls<String>(4)
                listItems[0] = "직업: " + response.getString("jobGrowName")
                listItems[1] = "모험단: " + response.getString("adventureName")
                listItems[2] = "길드: " + response.getString("guildName")
                listItems[3] = "레벨: " + response.getString("level")

                val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, listItems)

                info_list.adapter = adapter

                val server = response.getString("guildName")
                Log.d("guild", server)

            },
            { Log.d("error", "cannot import info") })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

}