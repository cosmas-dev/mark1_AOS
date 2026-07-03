#include "ivd_strip_analyzer_c_api.h"
#include "ivd_strip_analyzer.hpp"

#include <cstring>
#include <new>
#include <optional>

const char* ivd_analyze_strip_json(
    const char* image_path,
    const char* output_dir,
    int roi_x,
    int roi_y,
    int roi_w,
    int roi_h,
    int use_manual_roi
) {
    try {
        ivd::StripAnalyzer analyzer;
        std::optional<cv::Rect> roi;
        if (use_manual_roi) roi = cv::Rect{roi_x, roi_y, roi_w, roi_h};
        auto report = analyzer.analyze(image_path ? image_path : "", roi, output_dir ? output_dir : ".");
        char* out = new char[report.report_json.size() + 1];
        std::memcpy(out, report.report_json.c_str(), report.report_json.size() + 1);
        return out;
    } catch (const std::exception& e) {
        std::string err = std::string{"{\"error\":\""} + e.what() + "\"}";
        char* out = new char[err.size() + 1];
        std::memcpy(out, err.c_str(), err.size() + 1);
        return out;
    }
}

void ivd_free_string(const char* ptr) {
    delete[] ptr;
}
