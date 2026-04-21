package com.example.myapplication.presentation.components.Utilidades

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.copy
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme

// ==========================================================================================
// --- SECCIÓN: ARBOLES DE DIRECTORIO Y MENÚS ---
// ==========================================================================================

data class FileNode(
    val name: String,
    val isDirectory: Boolean,
    val children: List<FileNode> = emptyList(),
    val icon: ImageVector? = null
)

@Composable
fun DirectoryTree(
    nodes: List<FileNode>,
    modifier: Modifier = Modifier,
    onNodeClick: (FileNode) -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        nodes.forEach { node ->
            FileNodeItem(node, onNodeClick = onNodeClick)
        }
    }
}

@Composable
private fun FileNodeItem(
    node: FileNode,
    level: Int = 0,
    onNodeClick: (FileNode) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (node.isDirectory) isExpanded = !isExpanded
                    onNodeClick(node)
                }
                .padding(start = (level * 16).dp, top = 8.dp, bottom = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (node.isDirectory) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(if (isExpanded) 90f else 0f),
                    tint = MaverickColors.TextMuted
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaverickColors.NeonCyan
                )
            } else {
                Spacer(modifier = Modifier.width(20.dp))
                Icon(
                    imageVector = node.icon ?: Icons.Default.InsertDriveFile,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaverickColors.TextMuted
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = node.name,
                color = if (node.isDirectory) Color.White else MaverickColors.TextMain,
                fontSize = 14.sp,
                fontWeight = if (node.isDirectory) FontWeight.Bold else FontWeight.Normal
            )
        }

        if (node.isDirectory && isExpanded) {
            node.children.forEach { child ->
                FileNodeItem(child, level + 1, onNodeClick)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun DirectoryTreePreview() {
    val sampleTree = listOf(
        FileNode(
            "app", true, listOf(
                FileNode("src", true, listOf(
                    FileNode("main", true, listOf(
                        FileNode("java", true),
                        FileNode("res", true)
                    ))
                )),
                FileNode("build.gradle", false)
            )
        ),
        FileNode("gradle", true),
        FileNode("settings.gradle", false)
    )

    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            DirectoryTree(nodes = sampleTree)
        }
    }
}

// ==========================================================================================
// --- SECCIÓN: MENU CP (HÍBRIDO ROG/GHOST) ---
// ==========================================================================================

/**
 * MenuCP: Contenedor de menú estilo Cyberpunk.
 * Mezcla la geometría de ChatBubbleRogElite con el estilo visual de ChatBubbleGhost.
 */
@Composable
fun MenuCP(
    isVisible: Boolean,
    title: String,
    headerEmoji: String = "",
    headerColor: Color = CyberColorsV3.ElectricCyan,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = CutCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(modifier = modifier.padding(start = 24.dp, top = 12.dp)) {
            Box(
                modifier = Modifier
                    .widthIn(min = 240.dp)
                    .drawBehind {
                        val path = Path().apply {
                            moveTo(10.dp.toPx(), 0f)
                            lineTo(20.dp.toPx(), (-10).dp.toPx())
                            lineTo(30.dp.toPx(), 0f)
                            close()
                        }
                        drawPath(path, headerColor.copy(alpha = 0.1f))
                    }
                    .background(
                        color = MaverickColors.CyberBackground,
                        shape = shape
                    )
                    .border(
                        width = 1.dp,
                        color = headerColor.copy(alpha = 0.3f),
                        shape = shape
                    )
            ) {
                Column(
                    modifier = Modifier
                        .clip(shape)
                        .padding(12.dp)
                ) {
                    // HEADER CON TÍTULO Y BOTÓN DE CIERRE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (headerEmoji.isNotEmpty()) {
                                Text(headerEmoji, modifier = Modifier.padding(end = 8.dp))
                            }
                            Text(
                                text = title.uppercase(),
                                color = headerColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        }

                        // BOTÓN DE CIERRE (X) - Centrado a la derecha de la cabecera
                        BtnCancelStealth(
                            onClick = onDismiss
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = headerColor.copy(alpha = 0.2f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    content()
                }
            }
        }
    }
}









@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun MenuCPPreview() {
    MyApplicationTheme {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            MenuCP(
                isVisible = true,
                title = "SISTEMA MAVERICK",
                headerEmoji = "⚡",
                onDismiss = {}
            ) {
                HorizontalDivider(color = CyberColorsV3.ElectricCyan.copy(alpha = 0.2f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = ">> ACCEDER A LA RED",
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(vertical = 6.dp)
                )
                Text(
                    text = ">> CONFIGURACIÓN NEURAL",
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(vertical = 6.dp)
                )
                Text(
                    text = ">> DESCONECTARSE",
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(vertical = 6.dp)
                )
            }
        }
    }
}

