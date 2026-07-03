#pragma once

#ifdef __cplusplus
extern "C" {
#endif

// Returns heap-allocated UTF-8 JSON string. Free with ivd_free_string().
const char* ivd_analyze_strip_json(
    const char* image_path,
    const char* output_dir,
    int roi_x,
    int roi_y,
    int roi_w,
    int roi_h,
    int use_manual_roi
);

void ivd_free_string(const char* ptr);

#ifdef __cplusplus
}
#endif
