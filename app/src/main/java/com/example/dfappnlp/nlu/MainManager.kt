package com.example.dfappnlp.nlu

import android.content.Context
import android.util.Log
import com.example.dfappnlp.tokenization.Tokenizer

class MainManager (val context:Context, val model_path: String, val tokenizer_file: String){

    val inference_helper = Inference(context, model_path)
    val tokenizer = Tokenizer(context, tokenizer_file)

    fun predict(utterance: String){
        val tokens = tokenizer.tokenize(utterance)
        val (input_ids, attention_mask, token_type_ids, intent_id, slot_id)  = tokenizer.encode(utterance)
        Log.v("input_ids", input_ids.contentToString())
        Log.v("attention_mask", attention_mask.contentToString())
        Log.v("token_type_ids", token_type_ids.contentToString())
        Log.v("input_ids", intent_id.contentToString())
        Log.v("slot_id", slot_id.contentToString())
        Log.d("model path", model_path)

        inference_helper.nlu_inference(input_ids, attention_mask, token_type_ids, intent_id, slot_id, tokens!!.size)

    }

}