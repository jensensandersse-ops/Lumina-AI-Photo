package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.HdrEnhancedSelect
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.processing.ImageProcessor
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandPink
import com.example.ui.theme.BrandPurple
import com.example.ui.viewmodel.StudioViewModel

data class ToolCard(
    val title: String,
    val subtitle: String,
    val badge: String,
    val icon: ImageVector,
    val accentColor: Color,
    val preset: ImageProcessor.Preset,
    val sampleId: String
)

@Composable
fun ToolsScreen(
    viewModel: StudioViewModel,
    onNavigateToStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tools = listOf(
        ToolCard(
            title = "4K Ultra Clarity",
            subtitle = "Deblur, sharpen soft focus and reconstruct micro edge details",
            badge = "POPULAR",
            icon = Icons.Default.HighQuality,
            accentColor = BrandCyan,
            preset = ImageProcessor.Preset.ULTRA_CLARITY,
            sampleId = "sample_landscape"
        ),
        ToolCard(
            title = "Face Glow Retouch",
            subtitle = "Airbrush skin smoothing, brighten eyes, enhance warm tones",
            badge = "BEAUTY",
            icon = Icons.Default.Face,
            accentColor = BrandPink,
            preset = ImageProcessor.Preset.PORTRAIT_GLOW,
            sampleId = "sample_portrait"
        ),
        ToolCard(
            title = "Vintage Colorize",
            subtitle = "Restore faded archival B&W photos with realistic vibrant tones",
            badge = "RESTORE",
            icon = Icons.Default.ColorLens,
            accentColor = BrandAmber,
            preset = ImageProcessor.Preset.COLORIZE_RESTORE,
            sampleId = "sample_vintage"
        ),
        ToolCard(
            title = "Studio Bokeh Blur",
            subtitle = "DSLR f/1.4 aperture depth of field with focus preservation",
            badge = "PRO LENS",
            icon = Icons.Default.BlurOn,
            accentColor = BrandPurple,
            preset = ImageProcessor.Preset.BOKEH_STUDIO,
            sampleId = "sample_portrait"
        ),
        ToolCard(
            title = "Cyberpunk Neon",
            subtitle = "Electrifying hyper-contrast cyan, magenta and deep blacks",
            badge = "ARTISTIC",
            icon = Icons.Default.Lightbulb,
            accentColor = Color(0xFFE040FB),
            preset = ImageProcessor.Preset.CYBER_NEON,
            sampleId = "sample_cyber"
        ),
        ToolCard(
            title = "HDR Landscape Pop",
            subtitle = "Punchy dynamic range expansion for skies, foliage and water",
            badge = "VIVID",
            icon = Icons.Default.HdrEnhancedSelect,
            accentColor = BrandEmerald,
            preset = ImageProcessor.Preset.HDR_VIVID,
            sampleId = "sample_landscape"
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0C13))
            .padding(16.dp)
            .testTag("tools_screen")
    ) {
        // Screen Header
        Text(
            text = "AI Magic Toolbox",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Choose a neural workflow to enhance, restore or stylize instantly.",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(tools) { tool ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF161522),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2B283E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.loadSample(tool.sampleId)
                            viewModel.selectPreset(tool.preset)
                            onNavigateToStudio()
                        }
                        .testTag("tool_card_${tool.preset.name}")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = tool.accentColor.copy(alpha = 0.2f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = tool.icon,
                                        contentDescription = null,
                                        tint = tool.accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = tool.accentColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = tool.badge,
                                    color = tool.accentColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = tool.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tool.subtitle,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            maxLines = 3,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
