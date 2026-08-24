package dev.relaypatch.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.relaypatch.app.data.model.Patch
import dev.relaypatch.app.domain.PatchRepository
import dev.relaypatch.app.domain.diff.DiffLine
import dev.relaypatch.app.domain.diff.DiffParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiffInspectorViewModel @Inject constructor(
    private val repository: PatchRepository
) : ViewModel() {

    private val _patch = MutableStateFlow<Patch?>(null)
    val patch = _patch.asStateFlow()

    private val _parsedDiffLines = MutableStateFlow<List<DiffLine>>(emptyList())
    val parsedDiffLines = _parsedDiffLines.asStateFlow()

    fun loadPatch(patchId: String) {
        viewModelScope.launch {
            val loadedPatch = repository.getPatchById(patchId)
            _patch.value = loadedPatch
            
            loadedPatch?.let {
                if (it.diffText.isNotBlank()) {
                    var currentHunkIndex = -1
                    val mappedLines = DiffParser.parse(it.diffText).mapIndexed { i, parsedLine ->
                        if (parsedLine.lineType == DiffParser.LineType.HUNK_HEADER) {
                            currentHunkIndex++
                        }
                        DiffLine(
                            index = i,
                            hunkIndex = Math.max(0, currentHunkIndex),
                            type = when(parsedLine.lineType) {
                                DiffParser.LineType.ADDITION -> dev.relaypatch.app.domain.diff.DiffLineType.ADDITION
                                DiffParser.LineType.DELETION -> dev.relaypatch.app.domain.diff.DiffLineType.DELETION
                                DiffParser.LineType.HUNK_HEADER -> dev.relaypatch.app.domain.diff.DiffLineType.HUNK_HEADER
                                else -> dev.relaypatch.app.domain.diff.DiffLineType.CONTEXT
                            },
                            oldLineNumber = parsedLine.oldLineNum,
                            newLineNumber = parsedLine.newLineNum,
                            content = parsedLine.content
                        )
                    }
                    _parsedDiffLines.value = mappedLines
                }
            }
        }
    }
}
