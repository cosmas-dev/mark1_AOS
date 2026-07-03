#include "ivd_strip_analyzer.hpp"

#include <iostream>
#include <optional>
#include <string>

int main(int argc, char** argv) {
    if (argc < 2) {
        std::cerr << "Usage: ivd_strip_analyzer_cpp <image_path> [output_dir] [x y w h]\n";
        return 1;
    }

    std::string image_path = argv[1];
    std::string output_dir = argc >= 3 ? argv[2] : ".";
    std::optional<cv::Rect> roi;
    if (argc == 7) {
        roi = cv::Rect{std::stoi(argv[3]), std::stoi(argv[4]), std::stoi(argv[5]), std::stoi(argv[6])};
    }

    try {
        ivd::StripAnalyzer analyzer;
        auto report = analyzer.analyze(image_path, roi, output_dir);
        std::cout << report.report_json << std::endl;
        return 0;
    } catch (const std::exception& e) {
        std::cerr << e.what() << std::endl;
        return 2;
    }
}
