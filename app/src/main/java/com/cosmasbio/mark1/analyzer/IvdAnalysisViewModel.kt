package com.cosmasbio.mark1.analyzer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosmasbio.mark1.model.AnalysisReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class IvdAnalysisUiState(
    val loading: Boolean = false,
    val report: AnalysisReport? = null,
    val error: String? = null,
)

class IvdAnalysisViewModel(
    private val repository: IvdAnalyzerRepository = IvdAnalyzerRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(IvdAnalysisUiState())
    val uiState: StateFlow<IvdAnalysisUiState> = _uiState.asStateFlow()

    fun analyze(
        imagePath: String,
        outputDir: String,
        manualRoi: ManualRoi? = null,
    ) {
        viewModelScope.launch {
            _uiState.value = IvdAnalysisUiState(loading = true)

            try {
                val report = withContext(Dispatchers.Default) {
                    repository.analyzeStrip(
                        imagePath = imagePath,
                        outputDir = outputDir,
                        manualRoi = manualRoi,
                    )
                }

                _uiState.value = IvdAnalysisUiState(
                    loading = false,
                    report = report,
                    error = null,
                )
            } catch (e: Exception) {
                _uiState.value = IvdAnalysisUiState(
                    loading = false,
                    report = null,
                    error = e.message ?: "분석 실패",
                )
            }
        }
    }

    fun clear() {
        _uiState.value = IvdAnalysisUiState()
    }
}