package id.anantyan.tfliteaudio.helper

import android.content.Context
import android.util.Log
import id.anantyan.tfliteaudio.model.Recognition
import id.anantyan.tfliteaudio.utils.MFCC
import id.anantyan.tfliteaudio.utils.WavFile
import id.anantyan.tfliteaudio.utils.WavFileException
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.IOException
import java.math.RoundingMode
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.text.DecimalFormat
import java.util.PriorityQueue

/**
 * Created by Arya Rezza Anantya on 19/03/2024.
 */
class AudioInterpreterHelper(
    private val context: Context
) : AudioInterpreter {
    private var outputResult: String? = null

    private fun getTopKProbability(labelProb: Map<String, Float>): List<Recognition> {
        val MAX_RESULTS = 1
        val pq: PriorityQueue<Recognition> = PriorityQueue(MAX_RESULTS) { lhs, rhs ->
            (rhs.confidence ?: 0f).compareTo(lhs.confidence ?: 0f)
        }
        for (entry in labelProb.entries) {
            pq.add(Recognition(entry.key, entry.key, entry.value))
        }
        val recognitions: ArrayList<Recognition> = ArrayList()
        val recognitionsSize: Int = pq.size.coerceAtMost(MAX_RESULTS)
        for (i in 0 until recognitionsSize) {
            recognitions.add(pq.poll() ?: Recognition())
        }
        return recognitions
    }

    private fun getPredictedValue(predictedList:List<Recognition>): String? {
        return predictedList[0].title
    }

    private fun loadModelAndMakePredictions(meanMFCCValues : FloatArray) : String? {
        var predictedResult: String? = "Tidak bisa di klasifikasi!"
        val tfliteModel: MappedByteBuffer = FileUtil.loadMappedFile(context, NOISE)
        val tfliteOptions = Interpreter.Options()
        tfliteOptions.setNumThreads(4)

        val imageTensorIndex = 0
        val probabilityTensorIndex = 0
        val tflite = Interpreter(tfliteModel, tfliteOptions)
        val imageShape = tflite.getInputTensor(imageTensorIndex).shapeSignature()
        val imageDataType: DataType = tflite.getInputTensor(imageTensorIndex).dataType()
        val probabilityShape = tflite.getOutputTensor(probabilityTensorIndex).shape()
        val probabilityDataType: DataType = tflite.getOutputTensor(probabilityTensorIndex).dataType()
        val inBuffer: TensorBuffer = TensorBuffer.createDynamic(imageDataType)
        inBuffer.loadArray(meanMFCCValues, imageShape)

        val inpBuffer: ByteBuffer = inBuffer.buffer
        val outputTensorBuffer: TensorBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType)
        tflite.run(inpBuffer, outputTensorBuffer.buffer)

        var associatedAxisLabels: List<String?>? = null
        try {
            associatedAxisLabels = FileUtil.loadLabels(context, LABEL_NOISE)
        } catch (e: IOException) {
            Log.e("tfliteSupport", "Error membaca label!", e)
        }

        val probabilityProcessor: TensorProcessor = TensorProcessor.Builder().add(NormalizeOp(0.0f, 255.0f)).build()
        if (null != associatedAxisLabels) {
            val labels = TensorLabel(
                associatedAxisLabels,
                probabilityProcessor.process(outputTensorBuffer)
            )
            val floatMap: Map<String, Float> = labels.mapWithFloatValue
            val resultPrediction: List<Recognition> = getTopKProbability(floatMap)
            predictedResult = getPredictedValue(resultPrediction)
        }
        return predictedResult
    }

    override fun onResult(listener: (String) -> Unit) {
        outputResult?.let { result -> listener.invoke(result) }
    }

    override fun onClassification(outputFile: String) {
        val mNumFrames: Int
        val mSampleRate: Int
        val mChannels: Int
        val meanMFCCValues: FloatArray
        val wavFile: WavFile

        try {
            wavFile = WavFile.openWavFile(File(outputFile))
            mNumFrames = wavFile.numFrames.toInt()
            mSampleRate = wavFile.sampleRate.toInt()
            mChannels = wavFile.numChannels

            val buffer = Array(mChannels) { DoubleArray(mNumFrames) }
            var frameOffset = 0
            val loopCounter: Int = mNumFrames * mChannels / 4096 + 1
            for (i in 0 until loopCounter) {
                frameOffset = wavFile.readFrames(buffer, mNumFrames, frameOffset)
            }

            val df = DecimalFormat("#.#####")
            df.setRoundingMode(RoundingMode.CEILING)

            val meanBuffer = DoubleArray(mNumFrames)
            for (q in 0 until mNumFrames) {
                var frameVal = 0.0
                for (p in 0 until mChannels) {
                    frameVal += buffer[p][q]
                }
                meanBuffer[q] = df.format(frameVal / mChannels).toDouble()
            }

            val mfccConvert = MFCC()
            mfccConvert.setSampleRate(mSampleRate)

            val nMFCC = 40
            mfccConvert.setN_mfcc(nMFCC)

            val mfccInput = mfccConvert.process(meanBuffer)
            val nFFT = mfccInput.size / nMFCC
            val mfccValues = Array(nMFCC) { DoubleArray(nFFT) }
            for (i in 0 until nFFT) {
                var indexCounter = i * nMFCC
                val rowIndexValue = i % nFFT
                for (j in 0 until nMFCC) {
                    mfccValues[j][rowIndexValue] = mfccInput[indexCounter].toDouble()
                    indexCounter++
                }
            }

            meanMFCCValues = FloatArray(nMFCC)

            for (p in 0 until nMFCC) {
                var fftValAcrossRow = 0.0
                for (q in 0 until nFFT) {
                    fftValAcrossRow += mfccValues[p][q]
                }
                val fftMeanValAcrossRow = fftValAcrossRow / nFFT
                meanMFCCValues[p] = fftMeanValAcrossRow.toFloat()
            }

            outputResult = loadModelAndMakePredictions(meanMFCCValues)
        } catch (e: Exception) {
            Log.d("DEBUGGING", e.message.toString(), e)
        } catch (e: WavFileException) {
            Log.d("DEBUGGING", e.message.toString(), e)
        }
    }

    companion object {
        const val NOISE = "model.tflite"
        const val LABEL_NOISE = "labels.txt"
    }
}