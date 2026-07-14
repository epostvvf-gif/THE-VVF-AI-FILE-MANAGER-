package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.FileEntity

@Composable
fun StorageDashboard(files: List<FileEntity>) {
    if (files.isEmpty()) return

    val totalSize = files.sumOf { it.size }.coerceAtLeast(1)
    val imagesSize = files.filter { it.categoryId == "IMAGES" }.sumOf { it.size }
    val videosSize = files.filter { it.categoryId == "VIDEOS" }.sumOf { it.size }
    val docsSize = files.filter { it.categoryId == "DOCUMENTS" }.sumOf { it.size }
    val otherSize = totalSize - (imagesSize + videosSize + docsSize)

    val colorImages = Color(0xFF4CAF50)
    val colorVideos = Color(0xFF2196F3)
    val colorDocs = Color(0xFFFFC107)
    val colorOther = Color(0xFF9E9E9E)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Storage Distribution",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pie Chart
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(80.dp)) {
                        var startAngle = -90f
                        val strokeWidth = 24.dp.toPx()
                        
                        // Helper to draw arc
                        fun drawSlice(size: Long, color: Color) {
                            if (size == 0L) return
                            val sweepAngle = (size.toFloat() / totalSize) * 360f
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                            startAngle += sweepAngle
                        }
                        
                        drawSlice(imagesSize, colorImages)
                        drawSlice(videosSize, colorVideos)
                        drawSlice(docsSize, colorDocs)
                        drawSlice(otherSize, colorOther)
                    }
                    Text(
                        text = formatSize(totalSize),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                
                Spacer(modifier = Modifier.width(24.dp))
                
                // Legend
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LegendItem("Images", imagesSize, colorImages)
                    LegendItem("Videos", videosSize, colorVideos)
                    LegendItem("Documents", docsSize, colorDocs)
                    LegendItem("Other", otherSize, colorOther)
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, size: Long, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ${formatSize(size)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
