package com.example.myapplication.uishared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * --- INSTAGRAM PROMO SKELETON (ELITE v2026.FINAL) ---
 * [ELITE]: Reserva de espacio de alta fidelidad para el feed de promociones.
 */
@Composable
fun InstagramPromoSkeleton(
    modifier: Modifier = Modifier
) {
    val skeletonColor = Color.White.copy(alpha = 0.08f)
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF020408))
    ) {
        // 1. Header Skeleton
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(skeletonColor)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(140.dp, 10.dp).clip(RoundedCornerShape(2.dp)).background(skeletonColor))
                Spacer(Modifier.height(6.dp))
                Box(modifier = Modifier.size(80.dp, 8.dp).clip(RoundedCornerShape(2.dp)).background(skeletonColor))
            }
        }
        
        // 2. Media Skeleton (Perfect Square)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(skeletonColor)
        )
        
        // 3. Actions Bar Skeleton
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(skeletonColor))
            Spacer(Modifier.width(16.dp))
            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(skeletonColor))
            Spacer(Modifier.weight(1f))
            Box(modifier = Modifier.size(100.dp, 32.dp).clip(RoundedCornerShape(16.dp)).background(skeletonColor))
        }
        
        // 4. Content Skeleton
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Box(modifier = Modifier.fillMaxWidth(0.7f).height(12.dp).clip(RoundedCornerShape(2.dp)).background(skeletonColor))
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.9f).height(8.dp).clip(RoundedCornerShape(2.dp)).background(skeletonColor))
            Spacer(Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth(0.4f).height(8.dp).clip(RoundedCornerShape(2.dp)).background(skeletonColor))
        }
        
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
    }
}

































