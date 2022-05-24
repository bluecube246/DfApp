package com.example.dfappnlp.api

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.dfappnlp.R
import java.net.URLEncoder

class DfApi (val activity: Activity, val context: Context){

    fun loadImage(){
        val queue = Volley.newRequestQueue(context)
        val query_image: String = URLEncoder.encode("검제큐브", "utf-8")
        //var query = "검제큐브"
        val characterId = "9683cde24572f230266aa74964139bcc"
        val imageUrl = "https://img-api.neople.co.kr/df/servers/hilder/characters/" + characterId + "?zoom=3&apikey=H1rZasWGFhiLpJNGB7fQsKGiWnsASu0F"
        val mImageView = activity.findViewById<ImageView>(R.id.imageView2)

        val imageRequest = ImageRequest(imageUrl,
            Response.Listener{ bitmap ->
                mImageView.setImageBitmap(bitmap)
            }, 0, 0,
            ImageView.ScaleType.CENTER_CROP,
            Bitmap.Config.ARGB_8888,
            Response.ErrorListener {

            }
        )

        queue.add(imageRequest)
    }

    fun loadData(){
        val textView = activity.findViewById<TextView>(R.id.text)

// Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(context)
        //val url = "https://www.google.com"
        //val url = "https://api.neople.co.kr/df/servers?apikey=H1rZasWGFhiLpJNGB7fQsKGiWnsASu0F"
        val query: String = URLEncoder.encode("검제큐브", "utf-8")
        //var query = "검제큐브"
        val url = "https://api.neople.co.kr/df/servers/hilder/characters?characterName=" + query + "&apikey=H1rZasWGFhiLpJNGB7fQsKGiWnsASu0F"
        Log.d("query", query)

// Request a string response from the provided URL.
        val stringRequest = JsonObjectRequest(
            Request.Method.GET, url,null,
            Response.Listener{ response ->
                // Display the first 500 characters of the response string.
                Log.d("sucess", response.toString())
//                val url = response.getString("rows")

                val server = response.getJSONArray("rows").getJSONObject(0)
                Log.d("server", server.getString("characterName"))
                textView.text = "캐릭터: ${server.getString("characterName")}"
//                Log.d("server", server.toString())
//                Log.d("sucess", url)
            },
            Response.ErrorListener { textView.text = "That didn't work!" })

// Add the request to the RequestQueue.
        queue.add(stringRequest)

    }
}