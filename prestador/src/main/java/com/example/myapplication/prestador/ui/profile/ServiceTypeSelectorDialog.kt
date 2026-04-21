package com.example.myapplication.prestador.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.prestador.data.model.ServiceType

@Composable
fun ServiceTypeSelectorDialog(
    currentServiceType: ServiceType,
    onDismiss: () -> Unit,
    onServiceTypeSelected: (ServiceType) -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    var selectedType by remember { mutableStateOf(currentServiceType) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.backgroundColor,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Text(
                    text = "Tipo de Servicio",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Seleccioná el tipo que mejor describe tu servicio",
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Service Type Options
                ServiceTypeCard(
                    serviceType = ServiceType.TECHNICAL,
                    icon = Icons.Default.Build,
                    isSelected = selectedType == ServiceType.TECHNICAL,
                    onClick = { selectedType = ServiceType.TECHNICAL },
                    colors = colors
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ServiceTypeCard(
                    serviceType = ServiceType.PROFESSIONAL,
                    icon = Icons.Default.CalendarMonth,
                    isSelected = selectedType == ServiceType.PROFESSIONAL,
                    onClick = { selectedType = ServiceType.PROFESSIONAL },
                    colors = colors
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ServiceTypeCard(
                    serviceType = ServiceType.RENTAL,
                    icon = Icons.Default.Stadium,
                    isSelected = selectedType == ServiceType.RENTAL,
                    onClick = { selectedType = ServiceType.RENTAL },
                    colors = colors
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ServiceTypeCard(
                    serviceType = ServiceType.OTHER,
                    icon = Icons.Default.MoreHoriz,
                    isSelected = selectedType == ServiceType.OTHER,
                    onClick = { selectedType = ServiceType.OTHER },
                    colors = colors
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.textSecondary
                        )
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            onServiceTypeSelected(selectedType)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryOrange
                        )
                    ) {
                        Text("Guardar", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceTypeCard(
    serviceType: ServiceType,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    val borderColor = if (isSelected) colors.primaryOrange else colors.textSecondary.copy(alpha = 0.2f)
    val backgroundColor = if (isSelected) colors.primaryOrange.copy(alpha = 0.1f) else Color.Transparent
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(backgroundColor)
            .clickable(onClick = onClick),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) colors.primaryOrange else colors.textSecondary.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else colors.textSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = serviceType.displayName,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) colors.primaryOrange else colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = serviceType.description,
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    lineHeight = 18.sp
                )
            }
            
            // Check icon
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Seleccionado",
                    tint = colors.primaryOrange,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
