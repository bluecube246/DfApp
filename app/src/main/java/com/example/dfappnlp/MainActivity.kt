package com.example.dfappnlp

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dfappnlp.api.DfApi
import com.example.dfappnlp.nlu.MainManager
import android.app.Application

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val temp_tokenizer = Tokenizer(this, "kor_tokenizer.json")
//        val utt = "함안 날씨 춥냐 궤귀슠 한국어 모델을 공유합니다. 검제큐브"
//        temp_tokenizer.tokenize(utt)
//        temp_tokenizer.encode(utt)

//        val inference_helper = Inference(this, "ko_bert.pt")
//        inference_helper.inference_test_example()
//        inference_helper.nlu_inference_korean_test()

//        val predict = MainManager(this, "ko_bert.pt","kor_tokenizer.json")
//        predict.predict("함안 날씨 춥냐")

//        val predict = MainManager(this, this, "dnf_bert_v3_1.pt","kor_tokenizer.json")
//        predict.predict("노란색 큐브 정보 보여 줘")

        //BuildConfig.D

        //BuildConfig.DN


        //Initialize Front screen
        val apiHelper = DfApi(BuildConfig.DNF_KEY, this, this)
        apiHelper.setServer(true, "검제큐브", "정보")

        // Button to input question
        val btnSpeak = findViewById<ImageView>(R.id.btnSpeak)
        btnSpeak.setOnClickListener(View.OnClickListener {
            getSpeechInput()
        })
    }

    private fun getSpeechInput() {
        val intent = Intent(
            RecognizerIntent
                .ACTION_RECOGNIZE_SPEECH
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            "ko-KR"
        )

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, 10)
        } else {
            Toast.makeText(
                this,
                "Your Device Doesn't Support Speech Input",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int, data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode, data
        )
        when (requestCode) {
            10 -> if (resultCode == RESULT_OK &&
                data != null
            ) {
                val result =
                    data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS
                    )
                val txvResult = findViewById<TextView>(R.id.utt)
                txvResult.text = result!![0]
                val predict = MainManager(BuildConfig.DNF_KEY, this, this, "dnf_bert_v3_1.pt", "kor_tokenizer.json")
                predict.predict(result[0])

            }
        }
    }

}