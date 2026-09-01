package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.uishared.ui.components.shimmerApp

/**
 * --- SKELETONS PARA ITEMS DEL ARCHIVERO (v2026.ELITE) ---
 */

@Composable
fun ItemProductoSkeletonMav() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.03f)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).shimmerApp())
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(120.dp, 12.dp).clip(RoundedCornerShape(2.dp)).shimmerApp())
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.size(80.dp, 10.dp).clip(RoundedCornerShape(2.dp)).shimmerApp())
            }
        }
    }
}

@Composable
fun ItemEventoSkeletonMav() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(72.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.03f)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)).shimmerApp())
            Spacer(Modifier.width(16.dp))
            Column {
                Box(modifier = Modifier.size(140.dp, 12.dp).clip(RoundedCornerShape(2.dp)).shimmerApp())
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.size(100.dp, 10.dp).clip(RoundedCornerShape(2.dp)).shimmerApp())
            }
        }
    }
}

@Composable
fun ItemUbicacionSkeletonMav() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(68.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.03f)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).shimmerApp())
            Spacer(Modifier.width(16.dp))
            Box(modifier = Modifier.fillMaxWidth(0.8f).height(12.dp).clip(RoundedCornerShape(2.dp)).shimmerApp())
        }
    }
}
