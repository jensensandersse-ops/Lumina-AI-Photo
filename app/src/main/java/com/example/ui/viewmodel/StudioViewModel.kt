package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.EnhancedProject
import com.example.data.repository.ProjectRepository
import com.example.processing.ImageProcessor
import com.example.processing.SamplePhotos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.Stack

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository
    init {
        val db = AppDatabase.getDatabase(application)
        repository = ProjectRepository(db.projectDao())
    }

    val historyProjects: StateFlow<List<EnhancedProject>> = repository.allProjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _originalBitmap = MutableStateFlow<Bitmap?>(null)
    val originalBitmap: StateFlow<Bitmap?> = _originalBitmap.asStateFlow()

    private val _enhancedBitmap = MutableStateFlow<Bitmap?>(null)
    val enhancedBitmap: StateFlow<Bitmap?> = _enhancedBitmap.asStateFlow()

    private val _selectedPreset = MutableStateFlow(ImageProcessor.Preset.ULTRA_CLARITY)
    val selectedPreset: StateFlow<ImageProcessor.Preset> = _selectedPreset.asStateFlow()

    private val _selectedFilter = MutableStateFlow(ImageProcessor.HypicFilter.ORIGINAL)
    val selectedFilter: StateFlow<ImageProcessor.HypicFilter> = _selectedFilter.asStateFlow()

    private val _adjustments = MutableStateFlow(ImageProcessor.EditAdjustments())
    val adjustments: StateFlow<ImageProcessor.EditAdjustments> = _adjustments.asStateFlow()

    private val _rotationDegrees = MutableStateFlow(0f)
    val rotationDegrees: StateFlow<Float> = _rotationDegrees.asStateFlow()

    private val _isFlippedHorizontally = MutableStateFlow(false)
    val isFlippedHorizontally: StateFlow<Boolean> = _isFlippedHorizontally.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // 100% Free & Unlimited edits policy!
    private val _isUnlimitedFree = MutableStateFlow(true)
    val isUnlimitedFree: StateFlow<Boolean> = _isUnlimitedFree.asStateFlow()

    private val _credits = MutableStateFlow(25)
    val credits: StateFlow<Int> = _credits.asStateFlow()

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _showRewardedAd = MutableStateFlow(false)
    val showRewardedAd: StateFlow<Boolean> = _showRewardedAd.asStateFlow()

    private val _showProSheet = MutableStateFlow(false)
    val showProSheet: StateFlow<Boolean> = _showProSheet.asStateFlow()

    private val _activePhotoTitle = MutableStateFlow("Studio Portrait")
    val activePhotoTitle: StateFlow<String> = _activePhotoTitle.asStateFlow()

    private val _activeSampleId = MutableStateFlow<String?>("sample_portrait")
    val activeSampleId: StateFlow<String?> = _activeSampleId.asStateFlow()

    // Undo / Redo history stacks
    data class EditSnapshot(
        val adjustments: ImageProcessor.EditAdjustments,
        val filter: ImageProcessor.HypicFilter,
        val preset: ImageProcessor.Preset,
        val rotation: Float,
        val flipped: Boolean
    )

    private val undoStack = Stack<EditSnapshot>()
    private val redoStack = Stack<EditSnapshot>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    init {
        loadSample("sample_portrait")
    }

    private fun captureSnapshot() {
        val snapshot = EditSnapshot(
            adjustments = _adjustments.value,
            filter = _selectedFilter.value,
            preset = _selectedPreset.value,
            rotation = _rotationDegrees.value,
            flipped = _isFlippedHorizontally.value
        )
        undoStack.push(snapshot)
        redoStack.clear()
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = false
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = EditSnapshot(
            adjustments = _adjustments.value,
            filter = _selectedFilter.value,
            preset = _selectedPreset.value,
            rotation = _rotationDegrees.value,
            flipped = _isFlippedHorizontally.value
        )
        redoStack.push(current)
        val previous = undoStack.pop()
        _adjustments.value = previous.adjustments
        _selectedFilter.value = previous.filter
        _selectedPreset.value = previous.preset
        _rotationDegrees.value = previous.rotation
        _isFlippedHorizontally.value = previous.flipped
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
        triggerReprocess()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = EditSnapshot(
            adjustments = _adjustments.value,
            filter = _selectedFilter.value,
            preset = _selectedPreset.value,
            rotation = _rotationDegrees.value,
            flipped = _isFlippedHorizontally.value
        )
        undoStack.push(current)
        val next = redoStack.pop()
        _adjustments.value = next.adjustments
        _selectedFilter.value = next.filter
        _selectedPreset.value = next.preset
        _rotationDegrees.value = next.rotation
        _isFlippedHorizontally.value = next.flipped
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
        triggerReprocess()
    }

    fun loadSample(sampleId: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            _activeSampleId.value = sampleId
            val sampleInfo = SamplePhotos.sampleList.find { it.id == sampleId }
            _activePhotoTitle.value = sampleInfo?.title ?: "Sample Photo"
            _selectedPreset.value = sampleInfo?.defaultPreset ?: ImageProcessor.Preset.ULTRA_CLARITY
            _selectedFilter.value = ImageProcessor.HypicFilter.ORIGINAL
            _adjustments.value = ImageProcessor.EditAdjustments()
            _rotationDegrees.value = 0f
            _isFlippedHorizontally.value = false
            undoStack.clear()
            redoStack.clear()
            _canUndo.value = false
            _canRedo.value = false

            val rawSample = SamplePhotos.createSampleBitmap(sampleId)
            _originalBitmap.value = rawSample

            val processed = ImageProcessor.processImage(
                src = rawSample,
                preset = _selectedPreset.value,
                filter = _selectedFilter.value,
                adjustments = _adjustments.value
            )
            _enhancedBitmap.value = processed
            _isProcessing.value = false
        }
    }

    fun loadImageFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isProcessing.value = true
            _activeSampleId.value = null
            _activePhotoTitle.value = "Custom Photo"
            _adjustments.value = ImageProcessor.EditAdjustments()
            _selectedFilter.value = ImageProcessor.HypicFilter.ORIGINAL
            _rotationDegrees.value = 0f
            _isFlippedHorizontally.value = false
            undoStack.clear()
            redoStack.clear()
            _canUndo.value = false
            _canRedo.value = false

            try {
                val bitmap = withContext(Dispatchers.IO) {
                    var input: InputStream? = null
                    try {
                        input = context.contentResolver.openInputStream(uri)
                        BitmapFactory.decodeStream(input)
                    } finally {
                        input?.close()
                    }
                }

                if (bitmap != null) {
                    val scaledBitmap = scaleBitmapIfNeeded(bitmap, 1200)
                    _originalBitmap.value = scaledBitmap

                    val processed = ImageProcessor.processImage(
                        src = scaledBitmap,
                        preset = _selectedPreset.value,
                        filter = _selectedFilter.value,
                        adjustments = _adjustments.value
                    )
                    _enhancedBitmap.value = processed
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isProcessing.value = false
            }
        }
    }

    private fun scaleBitmapIfNeeded(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxDimension && h <= maxDimension) return bitmap

        val ratio = w.toFloat() / h.toFloat()
        val targetW = if (w > h) maxDimension else (maxDimension * ratio).toInt()
        val targetH = if (h >= w) maxDimension else (maxDimension / ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    fun selectPreset(preset: ImageProcessor.Preset) {
        captureSnapshot()
        _selectedPreset.value = preset
        _selectedFilter.value = ImageProcessor.HypicFilter.ORIGINAL
        triggerReprocess()
    }

    fun selectFilter(filter: ImageProcessor.HypicFilter) {
        captureSnapshot()
        _selectedFilter.value = filter
        triggerReprocess()
    }

    fun setAutoEnhanceIntensity(intensity: Float) {
        captureSnapshot()
        _adjustments.value = _adjustments.value.copy(autoEnhance = intensity)
        triggerReprocess()
    }

    fun toggleAutoEnhance() {
        captureSnapshot()
        val current = _adjustments.value.autoEnhance
        val next = if (current > 0f) 0f else 0.85f // Instant one-tap Hypic AI magic
        _adjustments.value = _adjustments.value.copy(autoEnhance = next)
        triggerReprocess()
    }

    fun updateAdjustments(newAdjustments: ImageProcessor.EditAdjustments) {
        captureSnapshot()
        _adjustments.value = newAdjustments
        triggerReprocess()
    }

    fun rotate90() {
        captureSnapshot()
        _rotationDegrees.value = (_rotationDegrees.value + 90f) % 360f
        triggerReprocess()
    }

    fun flipHorizontal() {
        captureSnapshot()
        _isFlippedHorizontally.value = !_isFlippedHorizontally.value
        triggerReprocess()
    }

    fun cropToRatio(ratio: Float) {
        val raw = _originalBitmap.value ?: return
        captureSnapshot()
        val cropped = ImageProcessor.cropToRatio(raw, ratio)
        _originalBitmap.value = cropped
        triggerReprocess()
    }

    fun resetAllAdjustments() {
        captureSnapshot()
        _adjustments.value = ImageProcessor.EditAdjustments()
        _selectedFilter.value = ImageProcessor.HypicFilter.ORIGINAL
        _rotationDegrees.value = 0f
        _isFlippedHorizontally.value = false
        triggerReprocess()
    }

    fun triggerReprocess() {
        val raw = _originalBitmap.value ?: return
        viewModelScope.launch {
            _isProcessing.value = true
            val result = ImageProcessor.processImage(
                src = raw,
                preset = _selectedPreset.value,
                filter = _selectedFilter.value,
                adjustments = _adjustments.value,
                rotationDegrees = _rotationDegrees.value,
                isFlippedHorizontally = _isFlippedHorizontally.value
            )
            _enhancedBitmap.value = result
            _isProcessing.value = false
        }
    }

    // 100% Free & Unlimited edits - always succeeds without blocking!
    fun applyAiMagic() {
        toggleAutoEnhance()
    }

    fun triggerWatchRewardedAd() {
        _showRewardedAd.value = true
    }

    fun onRewardedAdDismiss() {
        _showRewardedAd.value = false
    }

    fun onRewardEarned(earnedCredits: Int) {
        _credits.value = _credits.value + earnedCredits
        _showRewardedAd.value = false
    }

    fun triggerShowProSheet() {
        _showProSheet.value = true
    }

    fun onProSheetDismiss() {
        _showProSheet.value = false
    }

    fun activatePro() {
        _isPro.value = true
        _showProSheet.value = false
    }

    suspend fun saveAndExport(context: Context): String? {
        val enhanced = _enhancedBitmap.value ?: return null
        return withContext(Dispatchers.IO) {
            val fileName = "Lumina_${System.currentTimeMillis()}.jpg"
            val file = ImageProcessor.saveBitmapToFile(context, enhanced, fileName)
            val path = file.absolutePath

            val project = EnhancedProject(
                title = _activePhotoTitle.value,
                sampleId = _activeSampleId.value,
                localCachedPath = path,
                appliedPreset = if (_adjustments.value.autoEnhance > 0f) "Auto AI Magic" else _selectedPreset.value.displayName,
                brightness = _adjustments.value.brightness,
                contrast = _adjustments.value.contrast,
                saturation = _adjustments.value.saturation,
                sharpness = _adjustments.value.sharpness,
                vignette = _adjustments.value.vignette,
                warmth = _adjustments.value.warmth
            )
            repository.insertProject(project)

            path
        }
    }

    fun deleteProject(project: EnhancedProject) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }
}
