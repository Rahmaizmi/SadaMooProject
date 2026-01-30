package com.example.sadamoo.users.helper

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.sadamoo.users.data.Response
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.InputStream
import java.lang.reflect.Type
import java.nio.MappedByteBuffer
import kotlin.collections.find

class Classifier(private val context: Context) {
    private var interpreter: Interpreter
    private val inputImageSize = 224
    private val confidenceThreshold = 0.6f

    private val modelPath = "model2.tflite"
    private val labelPath = "labels.txt"
    private val jsonPath = "description.json"

    private var labels: List<String>
    private var resultResponse: List<Response>

    init {
        val model: MappedByteBuffer = FileUtil.loadMappedFile(context, modelPath)
        val options = Interpreter.Options().apply { numThreads = 4 }
        interpreter = Interpreter(model, options)

        Log.d("Classifier", "Model berhasil dimuat.")

        // Load label dari file dan JSON penyakit
        labels = loadLabels()
        resultResponse = loadDiseaseDataFromJson()
    }

    data class Recognition(val title: String, val confidence: Float)

    fun classify(bitmap: Bitmap): List<Recognition> {
        val tensorImage = preprocessImage(bitmap)
        val outputArray = Array(1) { FloatArray(labels.size) }
        interpreter.run(tensorImage.buffer, outputArray)

        val probabilities = outputArray[0]

        // mapping hasil ke Recognition
        return labels.mapIndexed { index, label ->
            Recognition(label, probabilities[index])
        }.filter { it.confidence >= confidenceThreshold }
    }

    private fun preprocessImage(bitmap: Bitmap): TensorImage {
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputImageSize, inputImageSize, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f))
            .build()
        return imageProcessor.process(tensorImage)
    }

    // get info penyakit berdasarkan nama
    fun getDiseaseInfoByName(diseaseName: String): Response? {
        return resultResponse.find {
            it.name.equals(diseaseName, ignoreCase = true)
        }
    }

    // Memuat label dari file teks
    private fun loadLabels(): List<String> {
        return FileUtil.loadLabels(context, labelPath)
    }

    // Memuat data penyakit dari file JSON
    private fun loadDiseaseDataFromJson(): List<Response> {
        val inputStream: InputStream = context.assets.open(jsonPath)
        val json = inputStream.bufferedReader().use { it.readText() }
        val type: Type = object : TypeToken<List<Response>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun close() {
        interpreter.close()
    }
}