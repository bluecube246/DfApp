package com.example.dfappnlp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.dfappnlp.api.DfApi
import com.example.dfappnlp.nlu.Inference
import com.example.dfappnlp.nlu.MainManager
import com.example.dfappnlp.tokenization.Tokenizer
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val temp_tokenizer = Tokenizer(this, "kor_tokenizer.json")
//        val utt = "함안 날씨 춥냐 궤귀슠 한국어 모델을 공유합니다. 검제큐브"
//        temp_tokenizer.tokenize(utt)
//        temp_tokenizer.encode(utt)

        val api_helper = DfApi(this, this)
        api_helper.loadImage()
        api_helper.loadData()

//        val inference_helper = Inference(this, "ko_bert.pt")
//        inference_helper.inference_test_example()
//        inference_helper.nlu_inference_korean_test()

//        val predict = MainManager(this, "ko_bert.pt","kor_tokenizer.json")
//        predict.predict("함안 날씨 춥냐")

        val predict = MainManager(this, "dnf_bert_v2.pt","kor_tokenizer.json")
        predict.predict("검제큐브 정보 보여줘")

        //inference_2()
    }

    private fun inference_2() {
        fun assetFilePath(context: Context, assetName: String): String {
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
                return file.absolutePath
            }
        }
        val module = Module.load(assetFilePath(this, "dnf_bert.pt"))

        val input_ids = longArrayOf(2, 4982, 6812, 1408,  517, 7470, 5689,    3,    1,    1,    1,    1,
            1,    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
            1,    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
            1,    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
            1,    1)

        val attention_mask = longArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0)

        val token_type_ids = longArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0)

        val intent_id = longArrayOf(2)

        val slot_id = longArrayOf(0, 14,  0, 12, 12,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0)

        val input_ids_tensor = Tensor.fromBlob(input_ids, longArrayOf(1, 50))
        val attention_mask_tensor = Tensor.fromBlob(attention_mask, longArrayOf(1, 50))
        val token_type_ids_tensor = Tensor.fromBlob(token_type_ids, longArrayOf(1, 50))
        val intent_id_tensor = Tensor.fromBlob(intent_id, longArrayOf(1))
        val slot_id_tensor = Tensor.fromBlob(slot_id, longArrayOf(1, 50))

        Log.d("input_ids", input_ids_tensor.toString())
        Log.d("attention_mask", attention_mask_tensor.toString())
        Log.d("token", token_type_ids_tensor.toString())
        Log.d("intent_id", intent_id_tensor.toString())
        Log.d("slot_id", slot_id_tensor.toString())

        val output = module.forward(IValue.from(input_ids_tensor), IValue.from(attention_mask_tensor),
            IValue.from(token_type_ids_tensor), IValue.from(intent_id_tensor), IValue.from(slot_id_tensor)).toTuple()

        Log.d("output 0 shape", output[0].toTensor().dataAsFloatArray.toList().toString())
        Log.d("intent_logits", output[1].toTuple()[0].toTensor().dataAsFloatArray.toList().toString())
        Log.d("slot_logits", output[1].toTuple()[1].toTensor().dataAsFloatArray.toList().toString())

        val arr = Array(50) {FloatArray(17)}
        for (i in output[1].toTuple()[1].toTensor().dataAsFloatArray.toList().indices){
            arr[i/17][i%17] = output[1].toTuple()[1].toTensor().dataAsFloatArray.toList()[i]
        }

        val slot_preds = IntArray(50)

        for (row in arr.indices){
            //Log.d("slot_out", arr[row].contentToString())
            //Log.d("max", arr[row].asList().indexOf(arr[row].toList().maxOrNull()).toString())
            slot_preds[row] = arr[row].asList().indexOf(arr[row].toList().maxOrNull())
        }

        Log.d("slot_preds", slot_preds.contentToString())

    }


}