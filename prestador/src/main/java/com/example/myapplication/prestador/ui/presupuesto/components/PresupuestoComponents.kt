package com.example.myapplication.prestador.ui.presupuesto.components

import android.accessibilityservice.GestureDescription
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import com.example.myapplication.prestador.utils.PrestadorProfile

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
//import com.example.myapplication.prestador.data.PPrestadorProfileFalso
import com.example.myapplication.prestador.data.local.entity.ProviderEntity
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.myapplication.prestador.ui.presupuesto.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    textStyle: TextStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
) {
    val colors = getPrestadorColors()
    val interactionSource = remember { MutableInteractionSource() }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle.copy(color = colors.textPrimary),
        keyboardOptions = keyboardOptions,
        singleLine = true,
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = true,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                label = label,
                placeholder = placeholder,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryOrange,
                    unfocusedBorderColor = colors.border,
                    focusedLabelColor = colors.primaryOrange,
                    unfocusedLabelColor = colors.textSecondary,
                    cursorColor = colors.primaryOrange,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledBorderColor = colors.border,
                    disabledLabelColor = Color(0xFF9CA3AF),
                    disabledTextColor = Color(0xFF9CA3AF),
                    errorBorderColor = Color(0xFFEF4444),
                    errorLabelColor = Color(0xFFEF4444),
                    errorCursorColor = Color(0xFFEF4444)
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                container = {
                    OutlinedTextFieldDefaults.ContainerBox(
                        enabled = true,
                        isError = false,
                        interactionSource = interactionSource,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF6B35),
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            )
        }
    )
}

@Composable
fun BudgetItemRow(item: BudgetItem, suggestionItems: List<BudgetItem> = emptyList(), onUpdate: (BudgetItem) -> Unit) {
    val colors = getPrestadorColors()
    val base = item.unitPrice * item.quantity
    val taxAmount = base * (item.taxPercentage / 100)
    val withTax = base + taxAmount
    val discountAmount = withTax * (item.discountPercentage / 100)
    val total = withTax - discountAmount

    var showSuggestions by remember { mutableStateOf(false) }
    val searchTerm = if (item.code.length >= 2) item.code else item.description
    val filtered = if (showSuggestions && searchTerm.length >= 2 && suggestionItems.isNotEmpty()) {
        suggestionItems.filter {
            it.description.contains(searchTerm, ignoreCase = true) ||
            it.code.contains(searchTerm, ignoreCase = true)
        }.take(5)
    } else emptyList()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Column(modifier = Modifier.weight(0.25f)) {
                Text("CÓDIGO", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    fontWeight = FontWeight.SemiBold, color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp))
                CompactTextField(
                    value = item.code,
                    onValueChange = { v: String ->
                        if (v.all { c: Char -> c.isDigit() }) {
                            onUpdate(item.copy(code = v))
                            showSuggestions = v.isNotBlank()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    placeholder = { Text("...", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                )
            }
            Column(modifier = Modifier.weight(0.75f)) {
                Text("DESCRIPCIÓN", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    fontWeight = FontWeight.SemiBold, color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp))
                CompactTextField(
                    value = item.description,
                    onValueChange = { v: String ->
                        onUpdate(item.copy(description = v))
                        showSuggestions = v.isNotBlank()
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    placeholder = { Text(if (suggestionItems.isNotEmpty()) "Descripción (${suggestionItems.size} sugerencias)" else "Descripción", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                )
            }
        }

        // Sugerencias inline
        if (showSuggestions && filtered.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F5)),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column {
                    filtered.forEachIndexed { index, suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdate(suggestion.copy(id = item.id))
                                    showSuggestions = false
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (suggestion.code.isNotBlank()) {
                                        Text(
                                            suggestion.code,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.primaryOrange
                                        )
                                        Text("•", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                                    }
                                    Text(
                                        suggestion.description,
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textPrimary
                                    )
                                }
                                Text(
                                    "Cant: ${suggestion.quantity}  •  \$${"%.2f".format(suggestion.unitPrice)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                            Icon(
                                Icons.Default.NorthWest,
                                contentDescription = "Usar",
                                tint = colors.primaryOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (index < filtered.size - 1) {
                            HorizontalDivider(color = colors.border)
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("CANT.", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    fontWeight = FontWeight.SemiBold, color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp))
                CompactTextField(
                    value = if (item.quantity == 0) "" else item.quantity.toString(),
                    onValueChange = { v: String ->
                        if (v.all { c: Char -> c.isDigit() }) onUpdate(item.copy(quantity = v.toIntOrNull() ?: 0))
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    placeholder = { Text("0", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("P. UNIT.",style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    fontWeight = FontWeight.SemiBold, color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp))
                CompactTextField(
                    value = if (item.unitPrice == 0.0) "" else item.unitPrice.toString(),
                    onValueChange = { v: String ->
                        if (v.isEmpty() || v.matches(Regex("^\\d*\\.?\\d*$"))) onUpdate(item.copy(unitPrice = v.toDoubleOrNull() ?: 0.0))
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    placeholder = { Text("0.00", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                )
            }
            Text(
                "Total: \$${"%.2f".format(total)}",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.primaryOrange
            )
        }
    }
}

@Composable
fun BudgetServiceRow(service: BudgetService, suggestionItems: List<BudgetService> = emptyList(), onUpdate: (BudgetService) -> Unit) {
    val colors = getPrestadorColors()
    var showSuggestions by remember { mutableStateOf(false) }
    val searchTerm = if (service.code.length >= 2) service.code else service.description
    val filtered = if (showSuggestions && searchTerm.length >= 2 && suggestionItems.isNotEmpty()) {
        suggestionItems.filter {
            it.description.contains(searchTerm, ignoreCase = true) ||
            it.code.contains(searchTerm, ignoreCase = true)
        }.take(5)
    } else emptyList()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Column(modifier = Modifier.weight(0.25f)) {
                Text("CÓDIGO", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    fontWeight = FontWeight.SemiBold, color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp))
                CompactTextField(
                    value = service.code,
                    onValueChange = {
                        onUpdate(service.copy(code = it))
                        showSuggestions = it.isNotBlank()
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    placeholder = { Text("...", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                )
            }
            Column(modifier = Modifier.weight(0.75f)) {
                Text("DESCRIPCIÓN", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    fontWeight = FontWeight.SemiBold, color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp))
                CompactTextField(
                    value = service.description,
                    onValueChange = {
                        onUpdate(service.copy(description = it))
                        showSuggestions = it.isNotBlank()
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    placeholder = { Text(if (suggestionItems.isNotEmpty()) "Descripción (${suggestionItems.size} sugerencias)" else "Descripción", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                )
            }
        }

        // Sugerencias inline
        if (showSuggestions && filtered.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column {
                    filtered.forEachIndexed { index, suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdate(suggestion.copy(id = service.id))
                                    showSuggestions = false
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (suggestion.code.isNotBlank()) {
                                        Text(suggestion.code, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = colors.primaryOrange)
                                        Text("•", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                                    }
                                    Text(suggestion.description, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                                }
                                Text("\$${"%.2f".format(suggestion.total)}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            }
                            Icon(Icons.Default.NorthWest, contentDescription = "Usar", tint = colors.primaryOrange, modifier = Modifier.size(16.dp))
                        }
                        if (index < filtered.size - 1) HorizontalDivider(color = colors.border)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("TOTAL ($)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    fontWeight = FontWeight.SemiBold, color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp))
                CompactTextField(
                    value = if (service.total == 0.0) "" else service.total.toString(),
                    onValueChange = { onUpdate(service.copy(total = it.toDoubleOrNull() ?: 0.0)) },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    placeholder = { Text("0.00", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                )
            }
        }
    }
}

@Composable
fun BudgetProfessionalFeeRow(fee: BudgetProfessionalFee, suggestionItems: List<BudgetProfessionalFee> = emptyList(), onUpdate: (BudgetProfessionalFee) -> Unit) {
    val colors = getPrestadorColors()
    var showSuggestions by remember { mutableStateOf(false) }
    val searchTerm = if (fee.code.length >= 2) fee.code else fee.description
    val filtered = if (showSuggestions && searchTerm.length >= 2 && suggestionItems.isNotEmpty()) {
        suggestionItems.filter {
            it.description.contains(searchTerm, ignoreCase = true) ||
            it.code.contains(searchTerm, ignoreCase = true)
        }.take(5)
    } else emptyList()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(0.35f)) {
                Text("CÓDIGO", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    fontWeight = FontWeight.SemiBold, color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp))
                CompactTextField(
                    value = fee.code,
                    onValueChange = {
                        onUpdate(fee.copy(code = it))
                        showSuggestions = it.isNotBlank()
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    placeholder = { Text("011", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                )
            }
            Column(modifier = Modifier.weight(0.65f)) {
                Text("TOTAL ($)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    fontWeight = FontWeight.SemiBold, color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp))
                CompactTextField(
                    value = if (fee.total == 0.0) "" else fee.total.toString(),
                    onValueChange = { onUpdate(fee.copy(total = it.toDoubleOrNull() ?: 0.0)) },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    placeholder = { Text("Ej. 25000", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                )
            }
        }
        Column {
            Text("DESCRIPCIÓN", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                fontWeight = FontWeight.SemiBold, color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 4.dp))
            CompactTextField(
                value = fee.description,
                onValueChange = {
                    onUpdate(fee.copy(description = it))
                    showSuggestions = it.isNotBlank()
                },
                modifier = Modifier.fillMaxWidth().height(40.dp),
                placeholder = { Text(if (suggestionItems.isNotEmpty()) "Descripción (${suggestionItems.size} sugerencias)" else "Ej. Consulta Especialista", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
            )
        }

        // Sugerencias inline
        if (showSuggestions && filtered.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F0FF)),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column {
                    filtered.forEachIndexed { index, suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdate(suggestion.copy(id = fee.id))
                                    showSuggestions = false
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (suggestion.code.isNotBlank()) {
                                        Text(suggestion.code, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = colors.primaryOrange)
                                        Text("•", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                                    }
                                    Text(suggestion.description, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                                }
                                Text("\$${"%.2f".format(suggestion.total)}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            }
                            Icon(Icons.Default.NorthWest, contentDescription = "Usar", tint = colors.primaryOrange, modifier = Modifier.size(16.dp))
                        }
                        if (index < filtered.size - 1) HorizontalDivider(color = colors.border)
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleSummaryRow(
    modifier: Modifier = Modifier,
    item: BudgetItem,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val colors = getPrestadorColors()
    val base = item.unitPrice * item.quantity
    val taxAmount = base * (item.taxPercentage / 100)
    val withTax = base + taxAmount
    val discountAmount = withTax * (item.discountPercentage / 100)
    val total = withTax - discountAmount

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .shadow(2.dp, RoundedCornerShape(8.dp))
            .background(colors.surfaceColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(colors.primaryOrange))
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.description.ifBlank { "(Sin desc.)" }, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall, color = colors.textPrimary, maxLines = 1)
            Text("${item.quantity} × \$${"%.2f".format(item.unitPrice)}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = colors.textSecondary)
        }
        Text("\$${"%.2f".format(total)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = colors.primaryOrange)
        if (onEdit != null || onDelete != null) {
            IconButton(onClick = { onEdit?.invoke() }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = colors.primaryOrange, modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = { onDelete?.invoke() }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.width(2.dp))
        } else {
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
fun ServiceSummaryRow(modifier: Modifier = Modifier, item: BudgetService, onEdit: (() -> Unit)? = null, onDelete: (() -> Unit)? = null) {
    val colors = getPrestadorColors()
    val accentColor = Color(0xFF3B82F6)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .shadow(2.dp, RoundedCornerShape(8.dp))
            .background(colors.surfaceColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(accentColor))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            item.description.ifBlank { "(Sin descripción)" },
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textPrimary,
            maxLines = 1
        )
        Text(
            "\$${"%.2f".format(item.total)}",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            color = accentColor
        )
        if (onEdit != null || onDelete != null) {
            IconButton(onClick = { onEdit?.invoke() }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = colors.primaryOrange, modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = { onDelete?.invoke() }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.width(2.dp))
        } else {
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
fun ProfessionalFeeSummaryRow(modifier: Modifier = Modifier, item: BudgetProfessionalFee, onEdit: (() -> Unit)? = null, onDelete: (() -> Unit)? = null) {
    val colors = getPrestadorColors()
    val accentColor = Color(0xFF8B5CF6)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .shadow(2.dp, RoundedCornerShape(8.dp))
            .background(colors.surfaceColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(accentColor))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            item.description.ifBlank { "(Sin descripción)" },
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textPrimary,
            maxLines = 1
        )
        Text(
            "\$${"%.2f".format(item.total)}",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            color = accentColor
        )
        if (onEdit != null || onDelete != null) {
            IconButton(onClick = { onEdit?.invoke() }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = colors.primaryOrange, modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = { onDelete?.invoke() }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.width(2.dp))
        } else {
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
fun TaxSummaryRow(modifier: Modifier = Modifier, item: BudgetTax, onEdit: (() -> Unit)? = null, onDelete: (() -> Unit)? = null) {
    val colors = getPrestadorColors()
    val accentColor = Color(0xFFEF4444)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .shadow(2.dp, RoundedCornerShape(8.dp))
            .background(colors.surfaceColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(accentColor))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            item.description.ifBlank { "(Sin descripción)" },
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textPrimary,
            maxLines = 1
        )
        Text(
            "\$${"%.2f".format(item.amount)}",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            color = accentColor
        )
        if (onEdit != null || onDelete != null) {
            IconButton(onClick = { onEdit?.invoke() }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = colors.primaryOrange, modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = { onDelete?.invoke() }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.width(2.dp))
        } else {
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
fun AttachmentSummaryRow(modifier: Modifier = Modifier, item: BudgetAttachment) {
    val colors = getPrestadorColors()
    Row(modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (item.type == AttachmentType.PDF) Icons.Default.PictureAsPdf else Icons.Default.Image, 
            contentDescription = null, 
            tint = colors.primaryOrange,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            item.description.ifBlank { "(Sin descripción)" }, 
            modifier = Modifier.weight(1f), 
            fontWeight = FontWeight.SemiBold, 
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary
        )
        Icon(
            Icons.Default.CheckCircle, 
            contentDescription = "Adjunto", 
            tint = Color(0xFF10B981),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun MiscExpenseSummaryRow(modifier: Modifier = Modifier, item: BudgetMiscExpense, onEdit: (() -> Unit)? = null, onDelete: (() -> Unit)? = null) {
    val colors = getPrestadorColors()
    val accentColor = Color(0xFF10B981)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .shadow(2.dp, RoundedCornerShape(8.dp))
            .background(colors.surfaceColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(accentColor))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            item.description.ifBlank { "(Sin descripción)" },
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textPrimary,
            maxLines = 1
        )
        Text(
            "\$${"%.2f".format(item.amount)}",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            color = accentColor
        )
        if (onEdit != null || onDelete != null) {
            IconButton(onClick = { onEdit?.invoke() }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = colors.primaryOrange, modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = { onDelete?.invoke() }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.width(2.dp))
        } else {
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
fun TotalsSummary(modifier: Modifier = Modifier, isExpanded: Boolean, grandTotal: Double) {
    val priceTextStyle = if (isExpanded) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge
    val verticalPadding = if (isExpanded) 14.dp else 8.dp
    val gradient = Brush.linearGradient(colors = listOf(Color(0xFF1E293B), Color(0xFF334155)))
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp)
            .background(gradient)
            .animateContentSize()
            .padding(horizontal = 20.dp, vertical = verticalPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "TOTAL GENERAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "\$${"%.2f".format(grandTotal)}",
                    style = priceTextStyle,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF6B35)
                )
            }
            Icon(
                Icons.Default.Receipt,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PrestadorHeader(prestador: PrestadorProfile, numeroPresupuesto: String, onFilterClick: () -> Unit) {
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy", java.util.Locale("es")))
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF6B35), Color(0xFFE64A19))
    )
    val nombreCompleto = prestador.companyName ?: "${prestador.name} ${prestador.lastName}"
    val iniciales = nombreCompleto.split(" ")
        .filter { it.isNotBlank() }.take(2)
        .joinToString("") {
            it.first().uppercaseChar().toString()
        }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    iniciales,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    nombreCompleto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    prestador.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "# $numeroPresupuesto",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text("•", color = Color.White.copy(alpha = .6f),
                        style = MaterialTheme.typography.bodySmall)
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        currentDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetMetaBox(title: String, value: String) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, Color.Black, RoundedCornerShape(4.dp))) {
        Text(text = title, modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1976D2))
            .padding(2.dp), color = Color.White, fontSize = 9.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
        Text(text = value, modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(4.dp), fontSize = 10.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SectionHeader(text: String) {
    Surface(color = Color(0xFF1976D2), modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}

@Composable
fun <T> CollapsibleSection(
    title: String,
    items: List<T>,
    sectionTotal: Double,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAddClick: () -> Unit,
    quickAddSlot: (@Composable () -> Unit)? = null,
    showDefaultContent: Boolean = true,
    itemContent: @Composable (item: T, index: Int) -> Unit
) {
    val colors = getPrestadorColors()
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrowRotation")

    Box(modifier = Modifier.padding(top = 18.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggleExpand)
                ) {
                    Badge(
                        containerColor = colors.primaryOrange,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = "${items.size}",
                            modifier = Modifier.padding(horizontal = 6.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        title, 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                        modifier = Modifier.rotate(rotationAngle),
                        tint = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = colors.border
                        )
                        // Quick-add slot (search from saved items)
                        if (quickAddSlot != null) {
                            quickAddSlot()
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (showDefaultContent) {
                            if (items.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .border(
                                            width = 1.dp,
                                            color = colors.border,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.AddCircleOutline,
                                            contentDescription = null,
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            "Sin ítems. Tocá + para agregar.",
                                            color = colors.textSecondary,
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                Column {
                                    items.forEachIndexed { index, item ->
                                        itemContent(item, index)
                                        if (index < items.size - 1) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 4.dp),
                                                color = colors.border
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        SmallFloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-18).dp),
            shape = CircleShape,
            containerColor = colors.primaryOrange,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir ${title.substringBefore(' ')}")
        }

        if (items.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 12.dp)
                    .shadow(4.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFF8C42), Color(0xFFFF4500))
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Subtotal: \$${"%.2f".format(sectionTotal)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}



/**
 * A quick-add search field that shows saved suggestions.
 * The user types to filter; tapping a suggestion calls onSelect.
 */
@Composable
fun <T> QuickAddField(
    suggestions: List<T>,
    getLabel: (T) -> String,
    getSubLabel: (T) -> String = { "" },
    placeholder: String = "Buscar guardados...",
    onSelect: (T) -> Unit
) {
    if (suggestions.isEmpty()) return

    val colors = getPrestadorColors()
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, suggestions) {
        if (query.isBlank()) suggestions
        else suggestions.filter { getLabel(it).contains(query, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = colors.primaryOrange,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = if (query.isNotEmpty()) {
                { IconButton(onClick = { query = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpiar", modifier = Modifier.size(16.dp))
                }}
            } else null,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primaryOrange,
                unfocusedBorderColor = colors.border,
                focusedLabelColor = colors.primaryOrange,
                cursorColor = colors.primaryOrange,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary
            ),
            shape = RoundedCornerShape(8.dp)
        )

        if (filtered.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    filtered.take(5).forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(item)
                                    query = ""
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    getLabel(item),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary,
                                    maxLines = 1
                                )
                                val sub = getSubLabel(item)
                                if (sub.isNotBlank()) {
                                    Text(
                                        sub,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.AddCircleOutline,
                                contentDescription = "Agregar",
                                tint = colors.primaryOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (index < filtered.take(5).lastIndex) {
                            HorizontalDivider(color = colors.border)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ArticleAutoCompleteFields(
    suggestions: List<BudgetItem>,
    onAdd: (BudgetItem) -> Unit,
    items: List<BudgetItem> = emptyList(),
    onEdit: (BudgetItem) -> Unit = {},
    onDelete: (Int) -> Unit = {}
) {
    val colors = getPrestadorColors()
    var codeText by remember { mutableStateOf("") }
    var codeExpanded by remember { mutableStateOf(false) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    val codeFiltered = remember(codeText, suggestions) {
        if (codeText.isBlank()) emptyList()
        else suggestions.filter {
            it.code.contains(codeText, ignoreCase = true) ||
            it.description.contains(codeText, ignoreCase = true)
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.primaryOrange,
        unfocusedBorderColor = colors.border,
        focusedLabelColor = colors.primaryOrange,
        cursorColor = colors.primaryOrange,
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary
    )

    Row(
        modifier = Modifier.fillMaxWidth().bringIntoViewRequester(bringIntoViewRequester),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = codeExpanded && codeFiltered.isNotEmpty(),
            onExpandedChange = { codeExpanded = it },
            modifier = Modifier.weight(0.22f)
        ) {
            CompactTextField(
                value = codeText,
                onValueChange = { codeText = it; codeExpanded = true },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryEditable)
                    .fillMaxWidth()
                    .height(40.dp)
                    .onFocusChanged { if (it.isFocused) scope.launch { delay(500); bringIntoViewRequester.bringIntoView() } },
                label = { Text("Buscar", style = MaterialTheme.typography.labelSmall) },
                placeholder = { Text("Cód. o descripción", style = MaterialTheme.typography.labelSmall) }
            )
            ExposedDropdownMenu(
                expanded = codeExpanded && codeFiltered.isNotEmpty(),
                onDismissRequest = { codeExpanded = false },
                modifier = Modifier.widthIn(min = 200.dp)
            ) {
                codeFiltered.take(5).forEach { item ->
                    DropdownMenuItem(
                        text = {
                            SuggestionsCodeRow(
                                code = item.code,
                                description = item.description,
                                icon = Icons.Default.Inventory2
                            )
                        },
                        onClick = {
                            onAdd(item.copy(id = System.currentTimeMillis()))
                            codeText = ""
                            codeExpanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Column(modifier = Modifier.weight(0.78f)) {
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Sin ítems. Tocá + para agregar.",
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items.forEachIndexed { index, item ->
                    ArticleSummaryRow(
                        item = item,
                        onEdit = { onEdit(item) },
                        onDelete = { onDelete(index) }
                    )
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ServiceAutoCompleteFields(
    suggestions: List<BudgetService>,
    onAdd: (BudgetService) -> Unit,
    items: List<BudgetService> = emptyList(),
    onEdit: (BudgetService) -> Unit = {},
    onDelete: (Int) -> Unit = {}
) {
    val colors = getPrestadorColors()
    var codeText by remember { mutableStateOf("") }
    var codeExpanded by remember { mutableStateOf(false) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    val codeFiltered = remember(codeText, suggestions) {
        if (codeText.isBlank()) emptyList()
        else suggestions.filter {
            it.code.contains(codeText, ignoreCase = true) ||
            it.description.contains(codeText, ignoreCase = true)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().bringIntoViewRequester(bringIntoViewRequester),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = codeExpanded && codeFiltered.isNotEmpty(),
            onExpandedChange = { codeExpanded = it },
            modifier = Modifier.weight(0.22f)
        ) {
            CompactTextField(
                value = codeText,
                onValueChange = { codeText = it; codeExpanded = true },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryEditable)
                    .fillMaxWidth()
                    .height(40.dp)
                    .onFocusChanged { if (it.isFocused) scope.launch { delay(500); bringIntoViewRequester.bringIntoView() } },
                label = { Text("Buscar", style = MaterialTheme.typography.labelSmall) },
                placeholder = { Text("Cód. o descripción", style = MaterialTheme.typography.labelSmall) }
            )
            ExposedDropdownMenu(
                expanded = codeExpanded && codeFiltered.isNotEmpty(),
                onDismissRequest = { codeExpanded = false },
                modifier = Modifier.widthIn(min = 200.dp)
            ) {
                codeFiltered.take(5).forEach { item ->
                    DropdownMenuItem(
                        text = {
                            SuggestionsCodeRow(
                                code = item.code,
                                description = item.description,
                                icon = Icons.Default.Build
                            )
                        },
                        onClick = {
                            onAdd(item.copy(id = System.currentTimeMillis()))
                            codeText = ""
                            codeExpanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Column(modifier = Modifier.weight(0.78f)) {
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Sin ítems. Tocá + para agregar.",
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items.forEachIndexed { index, item ->
                    ServiceSummaryRow(
                        item = item,
                        onEdit = { onEdit(item) },
                        onDelete = { onDelete(index) }
                    )
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FeeAutoCompleteFields(
    suggestions: List<BudgetProfessionalFee>,
    onAdd: (BudgetProfessionalFee) -> Unit,
    items: List<BudgetProfessionalFee> = emptyList(),
    onEdit: (BudgetProfessionalFee) -> Unit = {},
    onDelete: (Int) -> Unit = {}
) {
    val colors = getPrestadorColors()
    var codeText by remember { mutableStateOf("") }
    var codeExpanded by remember { mutableStateOf(false) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    val codeFiltered = remember(codeText, suggestions) {
        if (codeText.isBlank()) emptyList()
        else suggestions.filter {
            it.code.contains(codeText, ignoreCase = true) ||
            it.description.contains(codeText, ignoreCase = true)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().bringIntoViewRequester(bringIntoViewRequester),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = codeExpanded && codeFiltered.isNotEmpty(),
            onExpandedChange = { codeExpanded = it },
            modifier = Modifier.weight(0.22f)
        ) {
            CompactTextField(
                value = codeText,
                onValueChange = { codeText = it; codeExpanded = true },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryEditable)
                    .fillMaxWidth()
                    .height(40.dp)
                    .onFocusChanged { if (it.isFocused) scope.launch { delay(500); bringIntoViewRequester.bringIntoView() } },
                label = { Text("Buscar", style = MaterialTheme.typography.labelSmall) },
                placeholder = { Text("Cód. o descripción", style = MaterialTheme.typography.labelSmall) }
            )
            ExposedDropdownMenu(
                expanded = codeExpanded && codeFiltered.isNotEmpty(),
                onDismissRequest = { codeExpanded = false },
                modifier = Modifier.widthIn(min = 200.dp)
            ) {
                codeFiltered.take(5).forEach { item ->
                    DropdownMenuItem(
                        text = {
                            SuggestionsCodeRow(
                                code = item.code,
                                description = item.description,
                                icon = Icons.Default.Person
                            )
                        },
                        onClick = {
                            onAdd(item.copy(id = System.currentTimeMillis()))
                            codeText = ""
                            codeExpanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Column(modifier = Modifier.weight(0.78f)) {
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Sin ítems. Tocá + para agregar.",
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items.forEachIndexed { index, item ->
                    ProfessionalFeeSummaryRow(
                        item = item,
                        onEdit = { onEdit(item) },
                        onDelete = { onDelete(index) }
                    )
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DescriptionAutoCompleteField(
    label: String,
    suggestions: List<String>,
    onSelect: (String) -> Unit
) {
    val colors = getPrestadorColors()
    var text by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val filtered = remember(text, suggestions) {
        if (text.isBlank()) emptyList() else suggestions.filter { it.contains(text, ignoreCase = true) }
    }
    ExposedDropdownMenuBox(expanded = expanded && filtered.isNotEmpty(), onExpandedChange = { expanded = it }, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(value = text, onValueChange = { text = it; expanded = true },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
            label = { Text(label, style = MaterialTheme.typography.labelSmall) }, singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
            trailingIcon = if (text.isNotEmpty()) { { IconButton(onClick = { onSelect(text); text = "" }) { Icon(Icons.Default.AddCircle, null, tint = colors.primaryOrange) } } } else null,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primaryOrange, unfocusedBorderColor = colors.border, focusedLabelColor = colors.primaryOrange, cursorColor = colors.primaryOrange, focusedTextColor = colors.textPrimary, unfocusedTextColor = colors.textPrimary),
            shape = RoundedCornerShape(8.dp))
        ExposedDropdownMenu(expanded = expanded && filtered.isNotEmpty(), onDismissRequest = { expanded = false }) {
            filtered.take(5).forEach { desc ->
                DropdownMenuItem(text = { Text(desc, style = MaterialTheme.typography.bodySmall, color = colors.textPrimary) },
                    onClick = { onSelect(desc); text = ""; expanded = false })
            }
        }
    }
}

@Composable
private fun SuggetionRow(
    description: String,
    code: String = "",
    price: Double = 0.0,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.CheckCircle
) {
    val colors = getPrestadorColors()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.primaryOrange.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
        }

        Column {
            Text(description, fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (code.isNotBlank()) Text("#$code", fontSize = 11.sp, color = colors.textSecondary)
                if (price > 0) Text("$${"%.2f".format(price)}", fontSize = 11.sp,
                    color = colors.primaryOrange, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun SuggestionsCodeRow(
    code: String,
    description: String = "",
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Tag
) {
    val colors = getPrestadorColors()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.primaryOrange.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(code, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            if (description.isNotEmpty())
                Text(description, fontSize = 11.sp, color = colors.textSecondary, maxLines = 1)
        }
    }
}
