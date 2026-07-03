//package com.cosmasbio.mark1.analyzer
//
//import org.json.JSONObject
//import java.io.File
//
//class IvdAnalyzerRepository(
//    private val nativeAnalyzer: NativeIvdAnalyzer = NativeIvdAnalyzer(),
//) {
//    suspend fun analyzeStrip(
//        imagePath: String,
//        outputDir: String,
//        manualRoi: ManualRoi? = null,
//    ): JSONObject {
//        File(outputDir).mkdirs()
//        return nativeAnalyzer.analyze(
//            imagePath = imagePath,
//            outputDir = outputDir,
//            manualRoi = manualRoi,
//        )
//    }
//}

package com.cosmasbio.mark1.analyzer

import android.util.Log
import com.cosmasbio.mark1.model.AnalysisReport
import org.json.JSONObject
import java.io.File

class IvdAnalyzerRepository(
    private val nativeAnalyzer: NativeIvdAnalyzer = NativeIvdAnalyzer(),
) {
    suspend fun analyzeStrip(
        imagePath: String,
        outputDir: String,
        manualRoi: ManualRoi? = null,
    ): AnalysisReport {
        File(outputDir).mkdirs()

        val json = nativeAnalyzer.analyze(
            imagePath = imagePath,
            outputDir = outputDir,
            manualRoi = manualRoi,
        )
        Log.d("ANALYSIS", "native json = ${json.toString(2)}")

//        return parseReport(json)
        try {
            return parseReport(json)
        } catch (e: Exception) {
            throw IllegalStateException(
                "parseReport failed: ${e.message}\njson=${json.toString(2)}",
                e
            )
        }
    }

    private fun parseReport(json: JSONObject): AnalysisReport {
        if (json.has("ok") && !json.optBoolean("ok")) {
            throw IllegalStateException(json.optString("error", "analysis failed"))
        }

        val image = json.optString("image", "")
        val imageSize = json.optJSONArray("image_size")
        val imageWidth = imageSize?.optInt(0) ?: 0
        val imageHeight = imageSize?.optInt(1) ?: 0

        val roi = json.optJSONObject("roi")
        val roiX = roi?.optInt("x") ?: 0
        val roiY = roi?.optInt("y") ?: 0
        val roiW = roi?.optInt("w") ?: 0
        val roiH = roi?.optInt("h") ?: 0

        val channels = json.optJSONObject("channels")
            ?: throw IllegalStateException("channels not found in analysis result")

        val firstChannelName = channels.keys().asSequence().firstOrNull()
            ?: throw IllegalStateException("channel result not found")

        val channel = channels.optJSONObject(firstChannelName)
            ?: throw IllegalStateException("channel payload not found")

        val cLine = channel.optJSONObject("c_line")
        val tLine = channel.optJSONObject("t_line")

        return AnalysisReport(
            rawJson = json.toString(2),
            imagePath = image,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            roiX = roiX,
            roiY = roiY,
            roiW = roiW,
            roiH = roiH,
            channelName = firstChannelName,
            noiseSigma = channel.optDouble("noise_sigma", 0.0),
            cPosition = cLine?.takeIf { !it.isNull("position") }?.optInt("position"),
            cSnr = cLine?.takeIf { !it.isNull("snr") }?.optDouble("snr"),
            tPosition = tLine?.takeIf { !it.isNull("position") }?.optInt("position"),
            tSnr = tLine?.takeIf { !it.isNull("snr") }?.optDouble("snr"),
            tDetected = tLine?.optBoolean("detected", false) ?: false,
            tWeak = tLine?.optBoolean("weak", false) ?: false,
            h1SplitValid = channel.optBoolean("h1_split_valid", false),
            peakSeparationPx = channel.optDouble("peak_separation_px", 0.0),
            numPeaks = channel.optInt("num_peaks", 0),
        )
    }
}