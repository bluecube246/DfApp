package com.example.dfappnlp.nlu

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

data class Outputs(
    val intent_preds: List<Float>,
    val slot_preds: List<Int>,
)

class Inference(private val context: Context, private val model_path: String) {

    val path = assetFilePath(context, model_path)
    val module = Module.load(path)
    //val path = context.assets.
    //val list = context.assets.list("df_bert_model")
    //val module = Module.load(getFileFromAssets(context, model_path).absolutePath)

    @Throws(IOException::class)
    fun getFileFromAssets(context: Context, fileName: String): File =
        File(context.cacheDir, fileName)
            .also {
                if (!it.exists()) {
                    it.outputStream().use { cache ->
                        context.assets.open(fileName).use { inputStream ->
                            inputStream.copyTo(cache)
                        }
                    }
                }
            }

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
            return file.absolutePath
        }
    }

    fun nluInference(
        input_ids: LongArray, attention_mask: LongArray, token_type_ids: LongArray,
        intent_id: LongArray, slot_id: LongArray, token_size: Int
    ): Outputs {

        val inputIdsTensor = Tensor.fromBlob(input_ids, longArrayOf(1, 50))
        val attentionMaskTensor = Tensor.fromBlob(attention_mask, longArrayOf(1, 50))
        val tokenTypeIdsTensor = Tensor.fromBlob(token_type_ids, longArrayOf(1, 50))
        val intentIdTensor = Tensor.fromBlob(intent_id, longArrayOf(1))
        val slotIdTensor = Tensor.fromBlob(slot_id, longArrayOf(1, 50))

        val output = module.forward(
            IValue.from(inputIdsTensor), IValue.from(attentionMaskTensor),
            IValue.from(tokenTypeIdsTensor), IValue.from(intentIdTensor), IValue.from(slotIdTensor)
        ).toTuple()

        val intentLogits = output[1].toTuple()[0].toTensor().dataAsFloatArray.toList()

        Log.d("Loss", output[0].toTensor().dataAsFloatArray.toList().toString())
        Log.d(
            "intent_logits",
            output[1].toTuple()[0].toTensor().dataAsFloatArray.toList().toString()
        )
        Log.d("slot_logits", output[1].toTuple()[1].toTensor().dataAsFloatArray.toList().toString())

        Log.d("num", output[1].toTuple()[1].toTensor().dataAsFloatArray.toList().size.toString())

        val slotDim = 6
        Log.d("slot len", output[1].toTuple()[1].toTensor().dataAsFloatArray.size.toString())
        val arr = Array(50) { FloatArray(slotDim) }
        for (i in output[1].toTuple()[1].toTensor().dataAsFloatArray.toList().indices) {
            arr[i / slotDim][i % slotDim] =
                output[1].toTuple()[1].toTensor().dataAsFloatArray.toList()[i]
        }

        val slotPreds = IntArray(50)

        for (row in arr.indices) {
            //Log.d("slot_out", arr[row].contentToString())
            //Log.d("max", arr[row].asList().indexOf(arr[row].toList().maxOrNull()).toString())
            slotPreds[row] = arr[row].asList().indexOf(arr[row].toList().maxOrNull())
        }

        val slotPredsFilter = slotPreds.toMutableList()
            .filterIndexed { index, s -> (index != 0) && (index <= token_size) }
        Log.d("slot_preds_filter", slotPredsFilter.toString())
        Log.d("slot_preds", slotPreds.contentToString())

        return Outputs(intentLogits, slotPredsFilter)

    }

    fun inference_test_example() {

        val bitmap = BitmapFactory.decodeStream(context.assets.open("image.jpg"))
        val module = Module.load(assetFilePath(context, "resnet.pt"))

        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            bitmap,
            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB
        )

        val outputTensor = module.forward(IValue.from(inputTensor)).toTensor()
        val scores = outputTensor.dataAsFloatArray

        var maxScore: Float = 0F
        var maxScoreIdx = -1
        var maxSecondScore: Float = 0F
        var maxSecondScoreIdx = -1

        for (i in scores.indices) {
            if (scores[i] > maxScore) {
                maxSecondScore = maxScore
                maxSecondScoreIdx = maxScoreIdx
                maxScore = scores[i]
                maxScoreIdx = i
            }
        }

        Log.d("maxScore", maxScore.toString())
        Log.d("scores", scores.toList().toString())

    }

    fun nlu_inference_korean_test() {
        val module = Module.load(assetFilePath(context, "ko_bert.pt"))

        val input_ids = longArrayOf(
            2, 4982, 6812, 1408, 517, 7470, 5689, 3, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1
        )

        val attention_mask = longArrayOf(
            1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0
        )

        val token_type_ids = longArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0
        )

        val intent_id = longArrayOf(2)

        val slot_id = longArrayOf(
            0, 14, 0, 12, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        )

        val input_ids_tensor = Tensor.fromBlob(input_ids, longArrayOf(1, 50))
        val attention_mask_tensor = Tensor.fromBlob(attention_mask, longArrayOf(1, 50))
        val token_type_ids_tensor = Tensor.fromBlob(token_type_ids, longArrayOf(1, 50))
        val intent_id_tensor = Tensor.fromBlob(intent_id, longArrayOf(1))
        val slot_id_tensor = Tensor.fromBlob(slot_id, longArrayOf(1, 50))

        Log.d("input_ids", input_ids_tensor.dataAsLongArray.toList().toString())
        Log.d("attention_mask", attention_mask_tensor.dataAsLongArray.toList().toString())
        Log.d("token", token_type_ids_tensor.dataAsLongArray.toList().toString())
        Log.d("intent_id", intent_id_tensor.dataAsLongArray.toList().toString())
        Log.d("slot_id", slot_id_tensor.dataAsLongArray.toList().toString())

        val output = module.forward(
            IValue.from(input_ids_tensor),
            IValue.from(attention_mask_tensor),
            IValue.from(token_type_ids_tensor),
            IValue.from(intent_id_tensor),
            IValue.from(slot_id_tensor)
        ).toTuple()

        Log.d("Loss", output[0].toTensor().dataAsFloatArray.toList().toString())
        Log.d(
            "intent_logits",
            output[1].toTuple()[0].toTensor().dataAsFloatArray.toList().toString()
        )
        Log.d("slot_logits", output[1].toTuple()[1].toTensor().dataAsFloatArray.toList().toString())

        val arr = Array(50) { FloatArray(17) }
        for (i in output[1].toTuple()[1].toTensor().dataAsFloatArray.toList().indices) {
            arr[i / 17][i % 17] = output[1].toTuple()[1].toTensor().dataAsFloatArray.toList()[i]
        }

        val slot_preds = IntArray(50)

        for (row in arr.indices) {
            //Log.d("slot_out", arr[row].contentToString())
            //Log.d("max", arr[row].asList().indexOf(arr[row].toList().maxOrNull()).toString())
            slot_preds[row] = arr[row].asList().indexOf(arr[row].toList().maxOrNull())
        }
        Log.d("slot_preds", slot_preds.contentToString())
    }


}