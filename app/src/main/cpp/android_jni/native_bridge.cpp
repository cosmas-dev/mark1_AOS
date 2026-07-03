#include <jni.h>
#include <string>
#include <optional>
#include <opencv2/opencv.hpp>
#include "include/ivd_strip_analyzer.hpp"

static std::string jstringToStdString(JNIEnv* env, jstring value) {
    if (value == nullptr) return std::string();
    const char* chars = env->GetStringUTFChars(value, nullptr);
    std::string result = chars ? chars : "";
    if (chars) env->ReleaseStringUTFChars(value, chars);
    return result;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_cosmasbio_mark1_analyzer_NativeIvdAnalyzer_nativeAnalyzeStrip(
    JNIEnv* env,
    jobject /* this */,
    jstring imagePath,
    jstring outputDir,
    jint roiX,
    jint roiY,
    jint roiW,
    jint roiH,
    jboolean useManualRoi
) {
    try {
        const std::string image_path = jstringToStdString(env, imagePath);
        const std::string output_dir = jstringToStdString(env, outputDir);

        ivd::StripAnalyzer analyzer;
        std::optional<cv::Rect> manual_roi = std::nullopt;
        if (useManualRoi) {
            manual_roi = cv::Rect((int)roiX, (int)roiY, (int)roiW, (int)roiH);
        }

        const auto report = analyzer.analyze(image_path, manual_roi, output_dir);
        return env->NewStringUTF(report.report_json.c_str());
    } catch (const std::exception& e) {
        std::string error = std::string("{\"ok\":false,\"error\":\"") + e.what() + "\"}";
        return env->NewStringUTF(error.c_str());
    } catch (...) {
        return env->NewStringUTF("{\"ok\":false,\"error\":\"unknown native error\"}");
    }
}
