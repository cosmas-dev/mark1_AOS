#include "ivd_strip_analyzer.hpp"

#include <algorithm>
#include <cmath>
#include <fstream>
#include <iomanip>
#include <numeric>
#include <set>
#include <sstream>
#include <stdexcept>

namespace ivd {
namespace {

std::vector<double> matRowColumnToVector(const cv::Mat& m, int col) {
    std::vector<double> out(static_cast<size_t>(m.rows));
    for (int r = 0; r < m.rows; ++r) out[static_cast<size_t>(r)] = m.at<double>(r, col);
    return out;
}

std::vector<double> toDoubleVector(const cv::Mat& m) {
    std::vector<double> out;
    out.reserve(static_cast<size_t>(m.total()));
    for (int r = 0; r < m.rows; ++r) {
        for (int c = 0; c < m.cols; ++c) out.push_back(m.at<double>(r, c));
    }
    return out;
}

double percentileMedian(std::vector<double> vals) {
    if (vals.empty()) return 0.0;
    const size_t n = vals.size();
    const size_t mid = n / 2;
    std::nth_element(vals.begin(), vals.begin() + static_cast<long>(mid), vals.end());
    double med = vals[mid];
    if (n % 2 == 0) {
        std::nth_element(vals.begin(), vals.begin() + static_cast<long>(mid - 1), vals.end());
        med = 0.5 * (med + vals[mid - 1]);
    }
    return med;
}

cv::Rect clampRect(const cv::Rect& rect, int max_w, int max_h) {
    int x = std::max(0, rect.x);
    int y = std::max(0, rect.y);
    int w = std::min(rect.width, max_w - x);
    int h = std::min(rect.height, max_h - y);
    return {x, y, std::max(0, w), std::max(0, h)};
}

std::string stemOf(const std::string& path) {
    const auto slash = path.find_last_of("/\\");
    std::string file = slash == std::string::npos ? path : path.substr(slash + 1);
    const auto dot = file.find_last_of('.');
    return dot == std::string::npos ? file : file.substr(0, dot);
}

} // namespace

StripAnalyzer::StripAnalyzer(AnalysisConfig config) : cfg_(std::move(config)) {}

int StripAnalyzer::ensureOdd(int n, int minimum) {
    n = std::max(n, minimum);
    return (n % 2 == 1) ? n : n + 1;
}

double StripAnalyzer::madSigma(const std::vector<double>& data) {
    if (data.empty()) return 0.0;
    const double med = percentileMedian(data);
    std::vector<double> abs_dev;
    abs_dev.reserve(data.size());
    for (double v : data) abs_dev.push_back(std::abs(v - med));
    return percentileMedian(abs_dev) * 1.4826;
}

cv::Rect StripAnalyzer::detectStripWindow(const cv::Mat& gray) const {
    cv::Mat gray_u8;
    gray.convertTo(gray_u8, CV_8U);

    cv::Mat thresh;
    cv::threshold(gray_u8, thresh, cfg_.strip_thresh_value, 255, cv::THRESH_BINARY);

    cv::Mat kernel = cv::getStructuringElement(cv::MORPH_RECT, cv::Size(5, 5));
    cv::morphologyEx(thresh, thresh, cv::MORPH_CLOSE, kernel, cv::Point(-1, -1), 3);
    cv::morphologyEx(thresh, thresh, cv::MORPH_OPEN, kernel, cv::Point(-1, -1), 2);

    std::vector<std::vector<cv::Point>> contours;
    cv::findContours(thresh, contours, cv::RETR_EXTERNAL, cv::CHAIN_APPROX_SIMPLE);

    double best_area = 0.0;
    cv::Rect best;
    for (const auto& c : contours) {
        const double area = cv::contourArea(c);
        const cv::Rect rect = cv::boundingRect(c);
        const double aspect = rect.width > 0 ? static_cast<double>(rect.height) / rect.width : 0.0;
        if (area > best_area && aspect > cfg_.strip_min_aspect && rect.width > cfg_.strip_min_width && rect.height > cfg_.strip_min_height) {
            best_area = area;
            best = rect;
        }
    }

    if (best_area > 0.0) return best;
    return {gray.cols / 4, gray.rows / 4, gray.cols / 2, gray.rows / 2};
}

cv::Rect StripAnalyzer::applyRoiMargins(const cv::Rect& roi) const {
    int mx = static_cast<int>(std::round(roi.width * cfg_.margin_x_ratio));
    int my = static_cast<int>(std::round(roi.height * cfg_.margin_y_ratio));
    return {roi.x + mx, roi.y + my, roi.width - 2 * mx, roi.height - 2 * my};
}

std::vector<double> StripAnalyzer::medianFilter1D(const std::vector<double>& data, int window) {
    const int half = window / 2;
    std::vector<double> out(data.size());
    std::vector<double> tmp;
    tmp.reserve(static_cast<size_t>(window));

    for (int i = 0; i < static_cast<int>(data.size()); ++i) {
        tmp.clear();
        for (int j = std::max(0, i - half); j <= std::min(static_cast<int>(data.size()) - 1, i + half); ++j) {
            tmp.push_back(data[static_cast<size_t>(j)]);
        }
        out[static_cast<size_t>(i)] = percentileMedian(tmp);
    }
    return out;
}

std::vector<double> StripAnalyzer::savitzkyGolay1D(const std::vector<double>& data, int window, int polyorder) {
    if (data.empty()) return {};
    window = ensureOdd(window, polyorder + 2);
    if (window > static_cast<int>(data.size())) {
        window = ensureOdd(static_cast<int>(data.size()) - 1, 3);
        if (window <= polyorder) return data;
    }

    const int half = window / 2;
    cv::Mat A(window, polyorder + 1, CV_64F);
    for (int i = -half; i <= half; ++i) {
        for (int j = 0; j <= polyorder; ++j) {
            A.at<double>(i + half, j) = std::pow(static_cast<double>(i), j);
        }
    }

    cv::Mat ATA = A.t() * A;
    cv::Mat ATA_inv;
    if (!cv::invert(ATA, ATA_inv, cv::DECOMP_SVD)) return data;
    cv::Mat coeffs = ATA_inv * A.t();
    cv::Mat smooth_coeff_row = coeffs.row(0);

    std::vector<double> kernel(static_cast<size_t>(window));
    for (int i = 0; i < window; ++i) kernel[static_cast<size_t>(i)] = smooth_coeff_row.at<double>(0, i);

    std::vector<double> out(data.size());
    for (int i = 0; i < static_cast<int>(data.size()); ++i) {
        double acc = 0.0;
        for (int k = -half; k <= half; ++k) {
            int idx = i + k;
            if (idx < 0) idx = 0;
            if (idx >= static_cast<int>(data.size())) idx = static_cast<int>(data.size()) - 1;
            acc += data[static_cast<size_t>(idx)] * kernel[static_cast<size_t>(k + half)];
        }
        out[static_cast<size_t>(i)] = acc;
    }
    return out;
}

std::vector<double> StripAnalyzer::meanProfileByRows(const cv::Mat& src) {
    std::vector<double> out(static_cast<size_t>(src.rows), 0.0);
    for (int r = 0; r < src.rows; ++r) {
        const double* ptr = src.ptr<double>(r);
        double sum = 0.0;
        for (int c = 0; c < src.cols; ++c) sum += ptr[c];
        out[static_cast<size_t>(r)] = sum / static_cast<double>(src.cols);
    }
    return out;
}

std::vector<int> StripAnalyzer::findPeaksWithProminence(const std::vector<double>& signal, double min_prominence, int min_width) {
    std::vector<int> peaks;
    const int n = static_cast<int>(signal.size());
    if (n < 3) return peaks;

    for (int i = 1; i < n - 1; ++i) {
        if (!(signal[static_cast<size_t>(i)] > signal[static_cast<size_t>(i - 1)] && signal[static_cast<size_t>(i)] >= signal[static_cast<size_t>(i + 1)])) continue;

        int left = i;
        while (left > 0 && signal[static_cast<size_t>(left - 1)] < signal[static_cast<size_t>(left)]) left--;
        int right = i;
        while (right < n - 1 && signal[static_cast<size_t>(right + 1)] <= signal[static_cast<size_t>(right)]) right++;

        const int width = right - left + 1;
        if (width < min_width) continue;

        double left_min = signal[static_cast<size_t>(i)];
        for (int j = 0; j <= i; ++j) left_min = std::min(left_min, signal[static_cast<size_t>(j)]);
        double right_min = signal[static_cast<size_t>(i)];
        for (int j = i; j < n; ++j) right_min = std::min(right_min, signal[static_cast<size_t>(j)]);
        double prominence = signal[static_cast<size_t>(i)] - std::max(left_min, right_min);
        if (prominence >= min_prominence) peaks.push_back(i);
    }

    return peaks;
}

ChannelResult StripAnalyzer::analyzeChannel(const std::vector<double>& profile, const std::string& name) const {
    ChannelResult result;
    result.name = name;
    result.profile = profile;
    if (profile.empty()) return result;

    const int n = static_cast<int>(profile.size());
    const int win = ensureOdd(std::max(cfg_.baseline_min_window, static_cast<int>(n * cfg_.baseline_window_ratio)));
    result.baseline = medianFilter1D(profile, win);

    result.signal.resize(profile.size());
    for (size_t i = 0; i < profile.size(); ++i) result.signal[i] = profile[i] - result.baseline[i];

    result.sigma = madSigma(result.signal);
    if (result.sigma < 1e-6) {
        double mean = std::accumulate(result.signal.begin(), result.signal.end(), 0.0) / static_cast<double>(result.signal.size());
        double var = 0.0;
        for (double v : result.signal) var += (v - mean) * (v - mean);
        result.sigma = std::sqrt(var / std::max<size_t>(1, result.signal.size())) + 1e-9;
    }

    int sg = std::min(cfg_.sg_window, std::max(3, (n / 4) * 2 - 1));
    sg = ensureOdd(sg, 3);
    result.smooth = savitzkyGolay1D(result.signal, sg, cfg_.sg_polyorder);

    auto peaks = findPeaksWithProminence(result.smooth, cfg_.peak_prominence_sigma * result.sigma, cfg_.peak_min_width);
    std::vector<std::pair<int, double>> ranked;
    ranked.reserve(peaks.size());
    for (int p : peaks) ranked.emplace_back(p, result.smooth[static_cast<size_t>(p)] / result.sigma);
    std::sort(ranked.begin(), ranked.end(), [](const auto& a, const auto& b) { return a.second > b.second; });

    for (const auto& [p, snr] : ranked) {
        result.peaks.push_back(p);
        result.snrs.push_back(snr);
    }

    if (result.peaks.size() >= 2) {
        int p1 = result.peaks[0], p2 = result.peaks[1];
        double s1 = result.snrs[0], s2 = result.snrs[1];
        if (p1 < p2) {
            result.c_line = PeakInfo{p1, s1};
            result.t_line = PeakInfo{p2, s2};
        } else {
            result.c_line = PeakInfo{p2, s2};
            result.t_line = PeakInfo{p1, s1};
        }
        result.t_detected = result.t_line->snr >= cfg_.detection_threshold;
        result.t_weak = result.t_line->snr >= cfg_.weak_threshold && result.t_line->snr < cfg_.detection_threshold;
    } else if (result.peaks.size() == 1) {
        result.c_line = PeakInfo{result.peaks[0], result.snrs[0]};
        auto retry = findPeaksWithProminence(result.smooth, 1.0 * result.sigma, 2);
        int best_t = -1;
        double best_t_snr = -1e18;
        for (int p : retry) {
            if (std::abs(p - result.peaks[0]) <= 10) continue;
            const double snr = result.smooth[static_cast<size_t>(p)] / result.sigma;
            if (snr > best_t_snr) {
                best_t = p;
                best_t_snr = snr;
            }
        }
        if (best_t >= 0) {
            result.t_line = PeakInfo{best_t, best_t_snr};
            result.t_weak = true;
        }
    }

    return result;
}

H1SplitInfo StripAnalyzer::validateH1Split(const ChannelResult& result) const {
    H1SplitInfo info;
    info.midpoint = static_cast<int>(result.profile.size()) / 2;
    if (result.c_line && result.t_line) {
        info.valid = result.c_line->position < info.midpoint && result.t_line->position > info.midpoint;
        info.separation_px = std::abs(result.t_line->position - result.c_line->position);
        if (!info.valid) {
            info.c_region = result.c_line->position < info.midpoint ? "upper" : "lower";
            info.t_region = result.t_line->position < info.midpoint ? "upper" : "lower";
        }
    }
    return info;
}

std::string StripAnalyzer::escapeJson(const std::string& s) {
    std::ostringstream oss;
    for (char ch : s) {
        switch (ch) {
            case '\\': oss << "\\\\"; break;
            case '"': oss << "\\\""; break;
            case '\n': oss << "\\n"; break;
            case '\r': oss << "\\r"; break;
            case '\t': oss << "\\t"; break;
            default: oss << ch; break;
        }
    }
    return oss.str();
}

std::string StripAnalyzer::toJson(const StripReport& report) {
    std::ostringstream oss;
    oss << std::fixed << std::setprecision(4);
    oss << "{\n";
    oss << "  \"image\": \"" << escapeJson(report.image_path) << "\",\n";
    oss << "  \"image_size\": [" << report.image_width << ", " << report.image_height << "],\n";
    oss << "  \"roi\": {\"x\": " << report.roi.x << ", \"y\": " << report.roi.y << ", \"w\": " << report.roi.width << ", \"h\": " << report.roi.height << "},\n";
    oss << "  \"channels\": {\n";
    const auto& res = report.target_channel;
    oss << "    \"" << escapeJson(res.name) << "\": {\n";
    oss << "      \"noise_sigma\": " << res.sigma << ",\n";
    oss << "      \"c_line\": {\"position\": ";
    if (res.c_line) oss << res.c_line->position << ", \"snr\": " << res.c_line->snr;
    else oss << "null, \"snr\": null";
    oss << "},\n";
    oss << "      \"t_line\": {\"position\": ";
    if (res.t_line) oss << res.t_line->position << ", \"snr\": " << res.t_line->snr;
    else oss << "null, \"snr\": null";
    oss << ", \"detected\": " << (res.t_detected ? "true" : "false") << ", \"weak\": " << (res.t_weak ? "true" : "false") << "},\n";
    oss << "      \"h1_split_valid\": " << (report.h1.valid ? "true" : "false") << ",\n";
    oss << "      \"peak_separation_px\": " << report.h1.separation_px << ",\n";
    oss << "      \"num_peaks\": " << res.peaks.size() << "\n";
    oss << "    }\n";
    oss << "  }\n";
    oss << "}\n";
    return oss.str();
}

StripReport StripAnalyzer::analyze(const std::string& image_path, const std::optional<cv::Rect>& manual_roi, const std::string& output_dir, const std::string& output_suffix) const {
    cv::Mat img_bgr = cv::imread(image_path, cv::IMREAD_COLOR);
    if (img_bgr.empty()) throw std::runtime_error("이미지 로드 실패: " + image_path);

    cv::Mat img_gray_u8;
    cv::cvtColor(img_bgr, img_gray_u8, cv::COLOR_BGR2GRAY);
    cv::Mat img_gray;
    img_gray_u8.convertTo(img_gray, CV_64F);

    cv::Rect raw_roi = manual_roi ? *manual_roi : detectStripWindow(img_gray);
    cv::Rect roi = clampRect(applyRoiMargins(raw_roi), img_gray.cols, img_gray.rows);
    if (roi.width <= 0 || roi.height <= 0) throw std::runtime_error("ROI가 유효하지 않습니다");

    cv::Mat roi_gray = img_gray(roi).clone();
    cv::Mat roi_bgr = img_bgr(roi).clone();

    std::vector<cv::Mat> bgr_channels;
    cv::split(roi_bgr, bgr_channels);
    cv::Mat b, g, r;
    bgr_channels[0].convertTo(b, CV_64F);
    bgr_channels[1].convertTo(g, CV_64F);
    bgr_channels[2].convertTo(r, CV_64F);

    double max_gray = 0.0;
    cv::minMaxLoc(roi_gray, nullptr, &max_gray);
    cv::Mat gray_inv = max_gray - roi_gray;
    cv::Mat color_diff = -( (g + b) / 2.0 - r );

    auto profile_gray = meanProfileByRows(gray_inv);
    auto profile_color = meanProfileByRows(color_diff);
    const int center_col = roi.width / 2;
    auto single_gray = matRowColumnToVector(gray_inv, center_col);
    auto single_color = matRowColumnToVector(color_diff, center_col);

    std::vector<std::pair<std::string, std::vector<double>>> channels = {
        {"단일라인_그레이", single_gray},
        {"단일라인_컬러", single_color},
        {"면적평균_그레이", profile_gray},
        {"면적평균_컬러", profile_color},
    };

    std::string best_name;
    double best_t_snr = -1.0;
    ChannelResult best_result;
    H1SplitInfo best_h1;

    for (const auto& [name, profile] : channels) {
        ChannelResult res = analyzeChannel(profile, name);
        H1SplitInfo h1 = validateH1Split(res);
        double t_snr = res.t_line ? res.t_line->snr : -1.0;
        if (t_snr > best_t_snr) {
            best_t_snr = t_snr;
            best_name = name;
            best_result = std::move(res);
            best_h1 = h1;
        }
    }

    StripReport report;
    report.image_path = image_path;
    report.image_width = img_bgr.cols;
    report.image_height = img_bgr.rows;
    report.roi = roi;
    report.best_channel = best_name;
    report.target_channel = best_result;
    report.h1 = best_h1;
    report.report_json = toJson(report);

    std::string base = stemOf(image_path);
    std::string suffix = output_suffix.empty() ? "" : "_" + output_suffix;
    std::ofstream json_file(output_dir + "/" + base + suffix + "_report.json", std::ios::binary);
    json_file << report.report_json;

    return report;
}

} // namespace ivd
