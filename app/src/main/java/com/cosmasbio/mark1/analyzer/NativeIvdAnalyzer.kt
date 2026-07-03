package com.cosmasbio.mark1.analyzer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class NativeIvdAnalyzer {
    external fun nativeAnalyzeStrip(
        imagePath: String,
        outputDir: String,
        roiX: Int,
        roiY: Int,
        roiW: Int,
        roiH: Int,
        useManualRoi: Boolean,
    ): String

    suspend fun analyze(
        imagePath: String,
        outputDir: String,
        manualRoi: ManualRoi? = null,
    ): JSONObject = withContext(Dispatchers.Default) {
        val json = nativeAnalyzeStrip(
            imagePath = imagePath,
            outputDir = outputDir,
            roiX = manualRoi?.x ?: 0,
            roiY = manualRoi?.y ?: 0,
            roiW = manualRoi?.w ?: 0,
            roiH = manualRoi?.h ?: 0,
            useManualRoi = manualRoi != null,
        )
        JSONObject(json)
    }

    companion object {
        init {
            System.loadLibrary("ivd_analyzer")
        }
    }
}

data class ManualRoi(
    val x: Int,
    val y: Int,
    val w: Int,
    val h: Int,
)
