package com.example.dfappnlp.tokenization

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.IOException

data class Inputs(
    val tokens: LongArray,
    val attention_mask: LongArray,
    val token_type_ids: LongArray,
    val intent_id: LongArray,
    val slot_id: LongArray
)

class Tokenizer (context: Context, assetName: String){

    val jsonObject = JSONObject(loadJSONFromAsset(context, "kor_tokenizer.json"))
    val iterator :Iterator<String> =jsonObject.keys()
    val data = HashMap<String, Int>()
    var tokenizer: FullTokenizer? = null

    init {
        while (iterator.hasNext()) {
            val key = iterator.next()
            data.put(key, jsonObject.get(key) as Int)
        }

        tokenizer = FullTokenizer(data,false)
    }

    private fun loadJSONFromAsset(context: Context, filename : String): String? {
        var json: String?
        try {
            val inputStream = context!!.assets.open(filename)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer)
        }
        catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    fun tokenize(utterance: String): MutableList<String>? {
        val tokens = tokenizer?.tokenize(utterance)
        Log.d("tokens", tokens.toString())
        return tokens
    }

    fun encode(utterance: String): Inputs {
        val tokens = tokenizer?.tokenize(utterance)
        Log.d("tokens", tokens.toString())
        val ids = tokenizer?.convertTokensToIds(tokens)
        val input_ids_temp = longArrayOf(2, 4982, 6812, 1408,  517, 7470, 5689,    3,    1,    1,    1,    1,
            1,    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
            1,    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
            1,    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
            1,    1)

        val input_ids_short = ids!!.map{it.toLong()}.toLongArray()

        val input_ids = LongArray(50){ _ -> 1}
        input_ids[0] = 2
        for (i in input_ids_short.indices){
            input_ids[i+1] = input_ids_short[i]
        }

        input_ids[input_ids_short.size + 1] = 3

        val attention_mask = LongArray(50){ _ -> 0}
        for (i in 0 .. tokens!!.size + 1){
            attention_mask[i] = 1
        }

        val token_type_ids = LongArray(50){ _ -> 0}

        val intent_id = longArrayOf(1)

        val slot_id = longArrayOf(0, 5,  0, 5, 5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0)

        val inputs = Inputs(input_ids, attention_mask, token_type_ids, intent_id, slot_id)

        return inputs
    }

    //        val basic = BasicTokenizer(false)
//        Log.d("tokens", basic.tokenize("Hello how hilder are you").toString())
//        val jsonObject = JSONObject(loadJSONFromAsset(this ,"tokenizer.json")).getJSONObject("model").getJSONObject("vocab")
}