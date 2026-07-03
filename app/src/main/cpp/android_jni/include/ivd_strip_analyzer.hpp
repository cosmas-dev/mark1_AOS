#pragma once

#include <opencv2/opencv.hpp>
#include <optional>
#include <string>
#include <tuple>
#include <vector>

namespace ivd {

struct AnalysisConfig {
    double margin_x_ratio = 0.12;
    double margin_y_ratio = 0.03;

    double baseline_window_ratio = 0.15;
    int baseline_min_window = 51;

    int sg_window = 21;
    int sg_polyorder = 3;

    double peak_prominence_sigma = 1.5;
    int peak_min_width = 2;
    double detection_threshold = 3.0;
    double weak_threshold = 2.0;

    int strip_thresh_value = 180;
    double strip_min_aspect = 1.5;
    int strip_min_width = 30;
    int strip_min_height = 100;
};

struct PeakInfo {
    int position = -1;
    double snr = 0.0;
};

struct H1SplitInfo {
    int midpoint = 0;
    bool valid = false;
    std::string c_region = "upper";
    std::string t_region = "lower";
    int separation_px = 0;
};

struct ChannelResult {
    std::string name;
    std::vector<double> profile;
    std::vector<double> baseline;
    std::vector<double> signal;
    std::vector<double> smooth;
    double sigma = 0.0;
    std::vector<int> peaks;
    std::vector<double> snrs;
    std::optional<PeakInfo> c_line;
    std::optional<PeakInfo> t_line;
    bool t_detected = false;
    bool t_weak = false;
};

struct StripReport {
    std::string image_path;
    int image_width = 0;
    int image_height = 0;
    cv::Rect roi;
    std::string best_channel;
    ChannelResult target_channel;
    H1SplitInfo h1;
    std::string report_json;
};

class StripAnalyzer {
public:
    explicit StripAnalyzer(AnalysisConfig config = {});

    StripReport analyze(
        const std::string& image_path,
        const std::optional<cv::Rect>& manual_roi = std::nullopt,
        const std::string& output_dir = ".",
        const std::string& output_suffix = ""
    ) const;

private:
    AnalysisConfig cfg_;

    static int ensureOdd(int n, int minimum = 5);
    static double madSigma(const std::vector<double>& data);

    cv::Rect detectStripWindow(const cv::Mat& gray) const;
    cv::Rect applyRoiMargins(const cv::Rect& roi) const;

    ChannelResult analyzeChannel(const std::vector<double>& profile, const std::string& name) const;
    H1SplitInfo validateH1Split(const ChannelResult& result) const;

    static std::vector<double> medianFilter1D(const std::vector<double>& data, int window);
    static std::vector<double> savitzkyGolay1D(const std::vector<double>& data, int window, int polyorder);
    static std::vector<double> meanProfileByRows(const cv::Mat& src);

    static std::vector<int> findPeaksWithProminence(
        const std::vector<double>& signal,
        double min_prominence,
        int min_width
    );

    static std::string toJson(const StripReport& report);
    static std::string escapeJson(const std::string& s);
};

} // namespace ivd
