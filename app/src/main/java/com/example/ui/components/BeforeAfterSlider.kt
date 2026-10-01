package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandPurple
import kotlin.math.roundToInt

@Composable
fun BeforeAfterSlider(
    beforeBitmap: Bitmap,
    afterBitmap: Bitmap,
    modifier: Modifier = Modifier,
    isProcessing: Boolean = false
) {
    var splitFraction by remember { mutableFloatStateOf(0.5f) }
    var isHoldingOriginal by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF12111A))
            .testTag("before_after_slider_container")
    ) {
        val totalWidth = constraints.maxWidth.toFloat()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(totalWidth) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val newFraction = (change.position.x / totalWidth).coerceIn(0.05f, 0.95f)
                        splitFraction = newFraction
                    }
                }
        ) {
            val beforeImage = remember(beforeBitmap) { beforeBitmap.asImageBitmap() }
            val afterImage = remember(afterBitmap) { afterBitmap.asImageBitmap() }

            if (isHoldingOriginal) {
                // User is holding down button to see 100% original
                Image(
                    bitmap = beforeImage,
                    contentDescription = "Original Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Enhanced image on bottom (fills full canvas)
                Image(
                    bitmap = afterImage,
                    contentDescription = "Enhanced Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Before image clipped to the left of split line
                Image(
                    bitmap = beforeImage,
                    contentDescription = "Original Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            val clipWidth = size.width * splitFraction
                            clipRect(
                                left = 0f,
                                top = 0f,
                                right = clipWidth,
                                bottom = size.height
                            ) {
                                this@drawWithContent.drawContent()
                            }
                        }
                )

                // Draggable vertical divider line
                val splitPx = totalWidth * splitFraction
                Box(
                    modifier = Modifier
                        .offset { IntOffset(splitPx.roundToInt() - 1, 0) }
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color.White)
                )

                // Center handle knob
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset {
                            IntOffset(
                                (splitPx - 20.dp.toPx()).roundToInt(),
                                0
                            )
                        }
                        .size(40.dp)
                        .testTag("slider_handle")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = "Drag to compare",
                            tint = Color(0xFF1E1B2E),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // "BEFORE" badge on top left
            AnimatedVisibility(
                visible = !isHoldingOriginal && splitFraction > 0.15f,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "BEFORE",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // "AFTER" badge on top right
            AnimatedVisibility(
                visible = !isHoldingOriginal && splitFraction < 0.85f,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Surface(
                    color = BrandPurple.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandCyan.copy(alpha = 0.4f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "AI ENHANCED",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            // Bottom "Hold to Compare" pill button
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isHoldingOriginal = true
                                tryAwaitRelease()
                                isHoldingOriginal = false
                            }
                        )
                    }
                    .testTag("hold_compare_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Hold to view original",
                        tint = if (isHoldingOriginal) BrandCyan else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isHoldingOriginal) "Showing Original" else "Hold to Compare",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
    }
}
