package com.example.dfappnlp.nlu

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.TextView
import com.example.dfappnlp.R
import com.example.dfappnlp.api.DfApi
import com.example.dfappnlp.tokenization.Tokenizer
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.ArithmeticException

class MainManager (private val ApiKey:String, private val context:Context, private val activity: Activity, private val model_path: String, private val tokenizer_file: String){

    private val inferenceHelper = Inference(context, model_path)
    private val tokenizer = Tokenizer(context, tokenizer_file)
    private val apiHelper = DfApi(ApiKey, activity, context)

    private fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }
        context.assets.open(assetName).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
            Log.d("path", file.absolutePath)
            return file.absolutePath
        }
    }

    fun predict(utterance: String){
        val tokens = tokenizer.tokenize(utterance)
        val (input_ids, attention_mask, token_type_ids, intent_id, slot_id)  = tokenizer.encode(utterance)
        Log.v("input_ids", input_ids.contentToString())
        Log.v("attention_mask", attention_mask.contentToString())
        Log.v("token_type_ids", token_type_ids.contentToString())
        Log.v("input_ids", intent_id.contentToString())
        Log.v("slot_id", slot_id.contentToString())
        Log.d("model path", model_path)

        val (intent_logits, slot_predictions) = inferenceHelper.nluInference(input_ids, attention_mask, token_type_ids, intent_id, slot_id, tokens!!.size)

        Log.d("MainManager1", intent_logits.toString())
        Log.d("MainManager2", slot_predictions.toString())

        val inputStreamSlot: InputStream = File(assetFilePath(context, "slot_label_v3_1.txt")).inputStream()
        val inputStringSlot = inputStreamSlot.bufferedReader().readLines()

        val inputStreamIntent: InputStream = File(assetFilePath(context, "intent_label_v3_1.txt")).inputStream()
        val inputStringIntent = inputStreamIntent.bufferedReader().readLines()

        Log.d("Intent List", inputStringIntent.toString())
        Log.d("Slot List", inputStringSlot.toString())

        if (tokens.size != slot_predictions.size){
            throw ArithmeticException("tokens and predictions not equal")
        }

        val slotBio = mutableListOf<String>()

        for (i in tokens.indices){
            if (tokens[i].contains("▁")){
                slotBio.add(inputStringSlot[slot_predictions[i]])
            }
        }

        Log.d("Slot tokens", utterance.split("\\s".toRegex()).toString())
        Log.d("Slot predictions", slotBio.toString())

        evaluate(utterance.split("\\s".toRegex()), slotBio)

    }

    private fun evaluate(tokens: List<String>, predictions: MutableList<String>){

        val name = StringBuilder()
        val bio = StringBuilder()
        val info = StringBuilder()

        for (i in predictions.indices){
            if (predictions[i].contains("-character_name")){
                name.append(tokens[i])
            }
            if (predictions[i].contains("-info_type")){
                info.append(tokens[i])
            }
            bio.append(tokens[i] + ":" + predictions[i] + " ")
        }

        Log.d("name", name.toString())
        Log.d("bio", bio.toString())
        Log.d("info", info.toString())
        val infoView = activity.findViewById<TextView>(R.id.txvResult)
        infoView.text = bio.toString()
        apiHelper.setServer(false, name.toString(), info.toString())
        //api_helper.update_results(name.toString())


    }

}