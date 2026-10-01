package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Crop169
import androidx.compose.material.icons.filled.Crop32
import androidx.compose.material.icons.filled.Crop54
import androidx.compose.material.icons.filled.Crop75
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.processing.ImageProcessor
import com.example.processing.SamplePhotos
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandPink
import com.example.ui.theme.BrandPurple
import com.example.ui.viewmodel.StudioViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun StudioScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val originalBitmap: Bitmap? by viewModel.originalBitmap.collectAsState()
    val enhancedBitmap: Bitmap? by viewModel.enhancedBitmap.collectAsState()
    val selectedFilter: ImageProcessor.HypicFilter by viewModel.selectedFilter.collectAsState()
    val adjustments: ImageProcessor.EditAdjustments by viewModel.adjustments.collectAsState()
    val isProcessing: Boolean by viewModel.isProcessing.collectAsState()
    val activeSampleId: String? by viewModel.activeSampleId.collectAsState()
    val canUndo: Boolean by viewModel.canUndo.collectAsState()
    val canRedo: Boolean by viewModel.canRedo.collectAsState()

    // Hypic editor bottom tabs: 0: Auto, 1: Adjust, 2: Filters, 3: Retouch, 4: Crop/Rotate
    var activeHypicTab by remember { mutableIntStateOf(0) }

    // Active manual adjustment sub-tool in "Adjust" tab:
    // 0: Brightness, 1: Contrast, 2: Highlights, 3: Shadows, 4: Saturation, 5: Warmth, 6: Sharpness, 7: Vignette, 8: Film Grain
    var activeAdjustSubTool by remember { mutableIntStateOf(0) }

    // Active Retouch sub-tool in "Retouch" tab: 0: Skin Smooth, 1: Eye Brighten
    var activeRetouchSubTool by remember { mutableIntStateOf(0) }

    // Photo picker launcher (zero-permission)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.loadImageFromUri(context, uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0C13))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
            .testTag("studio_screen")
    ) {
        // Hypic-Style Top Action Bar: Picker, Undo, Redo, Reset, Save/Export
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // New Photo Picker
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1C1929),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332F4B)),
                modifier = Modifier
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .testTag("upload_custom_photo_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Pick Photo",
                        tint = BrandCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "New Photo",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 5.dp)
                    )
                }
            }

            // Undo / Redo / Reset Control Cluster
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.undo() },
                    enabled = canUndo,
                    modifier = Modifier.size(34.dp).testTag("undo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) Color.White else Color(0xFF475569),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.redo() },
                    enabled = canRedo,
                    modifier = Modifier.size(34.dp).testTag("redo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) Color.White else Color(0xFF475569),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.resetAllAdjustments() },
                    modifier = Modifier.size(34.dp).testTag("reset_adjustments_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset all changes",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Save & Export HD Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        val path = viewModel.saveAndExport(context)
                        if (path != null) {
                            Toast.makeText(context, "Saved HD Photo to Gallery!", Toast.LENGTH_SHORT).show()
                            val file = File(path)
                            val fileUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/jpeg"
                                putExtra(Intent.EXTRA_STREAM, fileUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Photo"))
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp).testTag("export_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Save HD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        // Quick Sample Photos Carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SamplePhotos.sampleList.forEach { sample ->
                val isSelected = activeSampleId == sample.id
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(0xFF261D42) else Color(0xFF161522),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) BrandPurple else Color(0xFF262335)
                    ),
                    modifier = Modifier
                        .clickable { viewModel.loadSample(sample.id) }
                        .testTag("sample_chip_${sample.id}")
                ) {
                    Text(
                        text = sample.title,
                        color = if (isSelected) BrandCyan else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Center Main Canvas: Interactive Before/After Split View
        val currentOriginal = originalBitmap
        val currentEnhanced = enhancedBitmap
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .aspectRatio(0.92f),
            contentAlignment = Alignment.Center
        ) {
            if (currentOriginal != null && currentEnhanced != null) {
                BeforeAfterSlider(
                    beforeBitmap = currentOriginal,
                    afterBitmap = currentEnhanced,
                    isProcessing = isProcessing,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                CircularProgressIndicator(color = BrandPurple)
            }

            if (isProcessing) {
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = BrandCyan,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Processing...",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hypic-Style Floating Instant AI Magic Bar
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (adjustments.autoEnhance > 0f) Color(0xFF22173D) else Color(0xFF151422),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (adjustments.autoEnhance > 0f) BrandPurple else Color(0xFF29263C)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .clickable { viewModel.toggleAutoEnhance() }
                .testTag("auto_enhance_pill")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (adjustments.autoEnhance > 0f) BrandPurple else Color(0xFF252238),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (adjustments.autoEnhance > 0f) BrandCyan else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Auto AI Magic",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = BrandEmerald.copy(alpha = 0.2f),
                                modifier = Modifier.padding(start = 6.dp)
                            ) {
                                Text(
                                    text = "FREE",
                                    color = BrandEmerald,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = if (adjustments.autoEnhance > 0f) "One-tap AI intelligent balance ON" else "Tap for instant lighting & clarity fix",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (adjustments.autoEnhance > 0f) BrandPurple else Color(0xFF232036)
                ) {
                    Text(
                        text = if (adjustments.autoEnhance > 0f) "${(adjustments.autoEnhance * 100).toInt()}%" else "AUTO",
                        color = if (adjustments.autoEnhance > 0f) BrandCyan else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hypic Tool Editor Panel (Contextual to active tab)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .background(Color(0xFF141320), RoundedCornerShape(18.dp))
                .border(1.dp, Color(0xFF28253A), RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            when (activeHypicTab) {
                // Tab 0: Auto Enhance Controls
                0 -> {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Auto AI Intensity",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${(adjustments.autoEnhance * 100).toInt()}%",
                                color = BrandCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = adjustments.autoEnhance,
                            onValueChange = { viewModel.setAutoEnhanceIntensity(it) },
                            valueRange = 0f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = BrandCyan,
                                activeTrackColor = BrandPurple,
                                inactiveTrackColor = Color(0xFF28253A)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Hypic smart algorithm balances exposure, clears haze, lifts shadows, and enhances skin color automatically.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                // Tab 1: Adjust (Manual Tools)
                1 -> {
                    val adjustSubTools = listOf(
                        "Brightness", "Contrast", "Highlights", "Shadows",
                        "Saturation", "Warmth", "Sharpness", "Vignette", "Grain"
                    )

                    // Sub-tools horizontal selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        adjustSubTools.forEachIndexed { idx, name ->
                            val isSubSelected = activeAdjustSubTool == idx
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSubSelected) BrandPurple else Color(0xFF1E1C2B),
                                modifier = Modifier
                                    .clickable { activeAdjustSubTool = idx }
                                    .testTag("adjust_sub_${name.lowercase()}")
                            ) {
                                Text(
                                    text = name,
                                    color = if (isSubSelected) Color.White else Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Active Slider
                    when (activeAdjustSubTool) {
                        0 -> {
                            AdjustmentSliderRow(
                                label = "Brightness",
                                value = adjustments.brightness,
                                range = -80f..80f,
                                displayValue = "${adjustments.brightness.toInt()}",
                                onValueChange = { viewModel.updateAdjustments(adjustments.copy(brightness = it)) }
                            )
                        }
                        1 -> {
                            AdjustmentSliderRow(
                                label = "Contrast",
                                value = adjustments.contrast,
                                range = 0.5f..1.8f,
                                displayValue = "${((adjustments.contrast - 1f) * 100).toInt()}",
                                onValueChange = { viewModel.updateAdjustments(adjustments.copy(contrast = it)) }
                            )
                        }
                        2 -> {
                            AdjustmentSliderRow(
                                label = "Highlights",
                                value = adjustments.highlights,
                                range = -50f..50f,
                                displayValue = "${adjustments.highlights.toInt()}",
                                onValueChange = { viewModel.updateAdjustments(adjustments.copy(highlights = it)) }
                            )
                        }
                        3 -> {
                            AdjustmentSliderRow(
                                label = "Shadows",
                                value = adjustments.shadows,
                                range = -50f..50f,
                                displayValue = "${adjustments.shadows.toInt()}",
                                onValueChange = { viewModel.updateAdjustments(adjustments.copy(shadows = it)) }
                            )
                        }
                        4 -> {
                            AdjustmentSliderRow(
                                label = "Saturation",
                                value = adjustments.saturation,
                                range = 0f..2.0f,
                                displayValue = "${((adjustments.saturation - 1f) * 100).toInt()}",
                                onValueChange = { viewModel.updateAdjustments(adjustments.copy(saturation = it)) }
                            )
                        }
                        5 -> {
                            AdjustmentSliderRow(
                                label = "Warmth (Color Temp)",
                                value = adjustments.warmth,
                                range = -0.5f..0.5f,
                                displayValue = "${(adjustments.warmth * 100).toInt()}°",
                                onValueChange = { viewModel.updateAdjustments(adjustments.copy(warmth = it)) }
                            )
                        }
                        6 -> {
                            AdjustmentSliderRow(
                                label = "Sharpness & Clarity",
                                value = adjustments.sharpness,
                                range = 0f..2.0f,
                                displayValue = "${(adjustments.sharpness * 50).toInt()}%",
                                onValueChange = { viewModel.updateAdjustments(adjustments.copy(sharpness = it)) }
                            )
                        }
                        7 -> {
                            AdjustmentSliderRow(
                                label = "Vignette Framing",
                                value = adjustments.vignette,
                                range = 0f..1.0f,
                                displayValue = "${(adjustments.vignette * 100).toInt()}%",
                                onValueChange = { viewModel.updateAdjustments(adjustments.copy(vignette = it)) }
                            )
                        }
                        8 -> {
                            AdjustmentSliderRow(
                                label = "Film Grain Texture",
                                value = adjustments.grain,
                                range = 0f..1.0f,
                                displayValue = "${(adjustments.grain * 100).toInt()}%",
                                onValueChange = { viewModel.updateAdjustments(adjustments.copy(grain = it)) }
                            )
                        }
                    }
                }

                // Tab 2: Filters (Hypic Aesthetic Film & Portrait Presets)
                2 -> {
                    Column {
                        Text(
                            text = "Hypic Aesthetic Filters",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(ImageProcessor.HypicFilter.entries) { filter ->
                                val isFilterActive = selectedFilter == filter
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isFilterActive) Color(0xFF281E48) else Color(0xFF1A1828),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isFilterActive) 2.dp else 1.dp,
                                        color = if (isFilterActive) BrandCyan else Color(0xFF2C283F)
                                    ),
                                    modifier = Modifier
                                        .width(105.dp)
                                        .clickable { viewModel.selectFilter(filter) }
                                        .testTag("filter_chip_${filter.name}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = filter.category,
                                            color = BrandCyan,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = filter.displayName,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Tab 3: Retouch (Hypic Portrait Beauty)
                3 -> {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Skin Smooth", "Eye Sparkle").forEachIndexed { idx, name ->
                                val isSub = activeRetouchSubTool == idx
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSub) BrandPink else Color(0xFF1E1C2B),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { activeRetouchSubTool = idx }
                                ) {
                                    Text(
                                        text = name,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 12.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (activeRetouchSubTool == 0) {
                            AdjustmentSliderRow(
                                label = "Skin Softening & Glow",
                                value = adjustments.skinSmooth,
                                range = 0f..1.0f,
                                displayValue = "${(adjustments.skinSmooth * 100).toInt()}%",
                                onValueChange = { viewModel.updateAdjustments(adjustments.copy(skinSmooth = it)) }
                            )
                        } else {
                            AdjustmentSliderRow(
                                label = "Eye Brighten & Contrast",
                                value = adjustments.eyeBrighten,
                                range = 0f..1.0f,
                                displayValue = "${(adjustments.eyeBrighten * 100).toInt()}%",
                                onValueChange = { viewModel.updateAdjustments(adjustments.copy(eyeBrighten = it)) }
                            )
                        }
                    }
                }

                // Tab 4: Crop / Transform
                4 -> {
                    Column {
                        Text(
                            text = "Rotate & Flip",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.rotate90() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF221F32)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Rotate90DegreesCw,
                                    contentDescription = "Rotate",
                                    tint = BrandCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Rotate 90°", fontSize = 11.sp, modifier = Modifier.padding(start = 6.dp))
                            }

                            Button(
                                onClick = { viewModel.flipHorizontal() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF221F32)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flip,
                                    contentDescription = "Flip",
                                    tint = BrandPink,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Flip", fontSize = 11.sp, modifier = Modifier.padding(start = 6.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Aspect Ratio Crop",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Pair("1:1 Square", 1.0f),
                                Pair("4:5 Feed", 0.8f),
                                Pair("9:16 Story", 0.5625f),
                                Pair("16:9 Cinema", 1.777f),
                                Pair("3:4 Portrait", 0.75f)
                            ).forEach { (label, ratio) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1E1C2B),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332F4B)),
                                    modifier = Modifier.clickable { viewModel.cropToRatio(ratio) }
                                ) {
                                    Text(
                                        text = label,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hypic Primary Category Navigation Bar (Bottom dock)
        val hypicCategories = listOf(
            Triple(0, "Auto AI", Icons.Default.AutoAwesome),
            Triple(1, "Adjust", Icons.Default.Tune),
            Triple(2, "Filters", Icons.Default.ColorLens),
            Triple(3, "Retouch", Icons.Default.Face),
            Triple(4, "Crop", Icons.Default.Crop)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            hypicCategories.forEach { (index, title, icon) ->
                val isSelected = activeHypicTab == index
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) BrandPurple else Color(0xFF171524),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) BrandCyan.copy(alpha = 0.5f) else Color(0xFF262335)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeHypicTab = index }
                        .testTag("hypic_tab_$index")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (isSelected) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = title,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdjustmentSliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    displayValue: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = displayValue,
                color = BrandCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = BrandCyan,
                activeTrackColor = BrandPurple,
                inactiveTrackColor = Color(0xFF28253A)
            ),
            modifier = Modifier.fillMaxWidth().testTag("adjustment_slider")
        )
    }
}
