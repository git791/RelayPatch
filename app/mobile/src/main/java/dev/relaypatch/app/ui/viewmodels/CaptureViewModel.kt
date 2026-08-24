package dev.relaypatch.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.relaypatch.app.data.model.CaptureBundle
import dev.relaypatch.app.domain.PatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val repository: PatchRepository
) : ViewModel() {

    private val _errorText = MutableStateFlow("")
    val errorText = _errorText.asStateFlow()

    private val _spokenIntent = MutableStateFlow("")
    val spokenIntent = _spokenIntent.asStateFlow()

    fun updateErrorText(text: String) {
        _errorText.value = text
    }

    fun updateSpokenIntent(text: String) {
        _spokenIntent.value = text
    }

    fun submitCapture(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val bundle = CaptureBundle(
                errorText = _errorText.value,
                spokenIntent = _spokenIntent.value,
                optionalCodeSnippet = null
            )
            repository.createPatch(bundle)
            
            // Clear state for next time
            _errorText.value = ""
            _spokenIntent.value = ""
            
            onSuccess()
        }
    }
}
