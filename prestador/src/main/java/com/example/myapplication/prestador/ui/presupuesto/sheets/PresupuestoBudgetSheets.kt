package com.example.myapplication.prestador.ui.presupuesto.sheets

import android.graphics.drawable.Icon
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import java.util.UUID
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.myapplication.prestador.data.local.entity.ClienteEntity
import com.example.myapplication.prestador.ui.presupuesto.components.*
import com.example.myapplication.prestador.ui.presupuesto.*


// --- SHEET CONTENT COMPOSABLES ---

@Composable
fun AddArticleSheetContent(
    itemToEdit: BudgetItem?,
    suggestionItems: List<BudgetItem> = emptyList(),
    currentItems: List<BudgetItem> = emptyList(),
    onAddItem: (BudgetItem) -> Unit,
    onUpdateItem: (BudgetItem) -> Unit,
    onDeleteCurrentItem: ((Int) -> Unit)? = null,
    onDeleteSaved: ((BudgetItem) -> Unit)? = null,
    onSaveToSuggestions: ((BudgetItem) -> Unit)? = null,
    onAddComplete: () -> Unit = {}
) {
    val colors = getPrestadorColors()
    val isEditMode = itemToEdit != null
    var currentItem by remember { mutableStateOf(itemToEdit ?: BudgetItem()) }
    var searchQuery by remember { mutableStateOf("") }

    val baseAmount = currentItem.quantity * currentItem.unitPrice
    val taxAmountValue = baseAmount * (currentItem.taxPercentage / 100)
    val baseWithTax = baseAmount + taxAmountValue

    var taxPercentStr by remember { mutableStateOf(if (currentItem.taxPercentage > 0) currentItem.taxPercentage.toString() else "") }
    var taxAmountStr by remember { mutableStateOf(if (currentItem.taxPercentage > 0) "%.2f".format(taxAmountValue) else "") }
    var discountPercentStr by remember { mutableStateOf(if (currentItem.discountPercentage > 0) currentItem.discountPercentage.toString() else "") }
    var discountAmountStr by remember { mutableStateOf(if (currentItem.discountPercentage > 0) "%.2f".format(baseWithTax * currentItem.discountPercentage / 100) else "") }
    var pendingItemToSave by remember { mutableStateOf<BudgetItem?>(null) }

    LaunchedEffect(baseAmount) {
         if (baseAmount > 0) {
            val taxP = taxPercentStr.toDoubleOrNull() ?: 0.0
            if (taxP > 0) taxAmountStr = "%.2f".format(baseAmount * taxP / 100)

            val discP = discountPercentStr.toDoubleOrNull() ?: 0.0
            val currentTaxAmt = baseAmount * (taxP / 100)
            val currentBaseWithTax = baseAmount + currentTaxAmt
            if (discP > 0) discountAmountStr = "%.2f".format(currentBaseWithTax * discP / 100)
        }
    }

    Column(modifier = Modifier
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
        .imePadding()
        .navigationBarsPadding()
        .background(colors.backgroundColor)) {
        // Handle pill
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 12.dp)
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFCBD5E1))
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.primaryOrange.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Inventory2, contentDescription = null,
                    tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
            }
            Text(
                if (isEditMode) "Editar Artículo" else "Agregar Nuevo Artículo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        BudgetItemRow(item = currentItem, suggestionItems = suggestionItems, onUpdate = { currentItem = it })

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = colors.border)
        Text(
            "Impuestos y Descuentos", 
            style = MaterialTheme.typography.titleSmall, 
            color = colors.primaryOrange,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CompactTextField(
                value = taxPercentStr,
                onValueChange = {
                    taxPercentStr = it
                    val p = it.toDoubleOrNull()
                    if (p != null && baseAmount > 0) {
                         val tAmount = baseAmount * p / 100
                         taxAmountStr = "%.2f".format(tAmount)
                         currentItem = currentItem.copy(taxPercentage = p)
                         val newBaseWithTax = baseAmount + tAmount
                         val currentDiscP = discountPercentStr.toDoubleOrNull() ?: 0.0
                         if (currentDiscP > 0) discountAmountStr = "%.2f".format(newBaseWithTax * currentDiscP / 100)
                    } else if (it.isEmpty()) {
                        taxAmountStr = ""
                        currentItem = currentItem.copy(taxPercentage = 0.0)
                    }
                },
                label = { Text("Imp. (%)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
            )
            CompactTextField(
                value = taxAmountStr,
                onValueChange = {
                    taxAmountStr = it
                    val a = it.toDoubleOrNull()
                    if (a != null && baseAmount > 0) {
                        val p = (a / baseAmount) * 100
                        taxPercentStr = "%.2f".format(p)
                        currentItem = currentItem.copy(taxPercentage = p)
                         val newBaseWithTax = baseAmount + a
                         val currentDiscP = discountPercentStr.toDoubleOrNull() ?: 0.0
                         if (currentDiscP > 0) discountAmountStr = "%.2f".format(newBaseWithTax * currentDiscP / 100)
                    } else if (it.isEmpty()) {
                        taxPercentStr = ""
                        currentItem = currentItem.copy(taxPercentage = 0.0)
                    }
                },
                label = { Text("Imp. ($)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CompactTextField(
                value = discountPercentStr,
                onValueChange = {
                    discountPercentStr = it
                    val p = it.toDoubleOrNull()
                    if (p != null && baseWithTax > 0) {
                         discountAmountStr = "%.2f".format(baseWithTax * p / 100)
                         currentItem = currentItem.copy(discountPercentage = p)
                    } else if (it.isEmpty()) {
                        discountAmountStr = ""
                        currentItem = currentItem.copy(discountPercentage = 0.0)
                    }
                },
                label = { Text("Desc. (%)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
            )
            CompactTextField(
                value = discountAmountStr,
                onValueChange = {
                    discountAmountStr = it
                    val a = it.toDoubleOrNull()
                    if (a != null && baseWithTax > 0) {
                        val p = (a / baseWithTax) * 100
                        discountPercentStr = "%.2f".format(p)
                        currentItem = currentItem.copy(discountPercentage = p)
                    } else if (it.isEmpty()) {
                        discountPercentStr = ""
                        currentItem = currentItem.copy(discountPercentage = 0.0)
                    }
                },
                label = { Text("Desc. ($)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (isEditMode) onUpdateItem(currentItem)
                else {
                    val added = currentItem
                    onAddItem(added)
                    // Si es artículo nuevo (no está en el catálogo), preguntar si guardar
                    if (onSaveToSuggestions != null && suggestionItems.none { it.description.equals(added.description, ignoreCase = true) }) {
                        pendingItemToSave = added
                    }
                    currentItem = BudgetItem()
                    searchQuery = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = currentItem.description.isNotBlank() && currentItem.unitPrice > 0 && currentItem.quantity > 0,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryOrange,
                contentColor = Color.White
            )
        ) {
            Text(
                if (isEditMode) "Guardar Cambios" else "Agregar Artículo",
                fontWeight = FontWeight.Bold
            )
        }

        // Lista de artículos ya agregados al presupuesto actual
        if (currentItems.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.border)
            Text(
                "Artículos en este presupuesto (${currentItems.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            currentItems.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.primaryOrange.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            buildString { if (item.code.isNotBlank()) append("[${item.code}] "); append(item.description) },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Text(
                            "${item.quantity} u.  •  \$${"%.2f".format(item.unitPrice)}  =  \$${"%.2f".format(item.quantity * item.unitPrice)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                    IconButton(onClick = { currentItem = item }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = colors.primaryOrange, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { onDeleteCurrentItem?.invoke(index) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Diálogo: ¿guardar artículo nuevo en catálogo?
        pendingItemToSave?.let { newArt ->
            AlertDialog(
                onDismissRequest = { pendingItemToSave = null; onAddComplete() },
                title = { Text("Artículo nuevo", fontWeight = FontWeight.Bold) },
                text = { Text("\"${newArt.description}\" no está en tu lista.\n¿Deseas guardarlo para usarlo en futuros presupuestos?") },
                confirmButton = {
                    TextButton(onClick = {
                        onSaveToSuggestions?.invoke(newArt)
                        pendingItemToSave = null
                        onAddComplete()
                    }) {
                        Text("Guardar", color = colors.primaryOrange, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingItemToSave = null; onAddComplete() }) { Text("No, gracias") }
                }
            )
        }

        // Catálogo de artículos guardados
        if (!isEditMode && suggestionItems.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = colors.border)
            Text(
                "Artículos guardados (${suggestionItems.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar artículo...") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                },
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
            val filtered = if (searchQuery.isBlank()) suggestionItems
                           else suggestionItems.filter { it.description.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true) }
            filtered.forEach { saved ->
                key(saved.description) {
                    var showConfirmDelete by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.primaryOrange.copy(alpha = 0.06f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                buildString {
                                    if (saved.code.isNotBlank()) append("[${saved.code}] ")
                                    append(saved.description)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Text(
                                "${saved.quantity} u.  •  \$${"%.2f".format(saved.unitPrice)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                        }
                        IconButton(
                            onClick = { currentItem = saved.copy(id = currentItem.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = colors.primaryOrange, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = { showConfirmDelete = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                    if (showConfirmDelete) {
                        AlertDialog(
                            onDismissRequest = { showConfirmDelete = false },
                            title = { Text("¿Eliminar?", fontWeight = FontWeight.Bold) },
                            text = { Text("Se eliminará \"${saved.description}\" de la lista.") },
                            confirmButton = {
                                TextButton(onClick = { onDeleteSaved?.invoke(saved); showConfirmDelete = false }) {
                                    Text("Eliminar", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = { TextButton(onClick = { showConfirmDelete = false }) { Text("Cancelar") } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddServiceSheetContent(
    itemToEdit: BudgetService?,
    onAddItem: (BudgetService) -> Unit,
    onUpdateItem: (BudgetService) -> Unit,
    currentItems: List<BudgetService> = emptyList(),
    onDeleteCurrentItem: ((Int) -> Unit)? = null,
    suggestionItems: List<BudgetService> = emptyList(),
    onDeleteSaved: ((BudgetService) -> Unit)? = null,
    onSaveToSuggestions: ((BudgetService) -> Unit)? = null,
    onAddComplete: () -> Unit = {}
) {
    val colors = getPrestadorColors()
    val isEditMode = itemToEdit != null
    var currentItem by remember { mutableStateOf(itemToEdit ?: BudgetService()) }
    var pendingItemToSave by remember { mutableStateOf<BudgetService?>(null) }

    Column(modifier = Modifier
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
        .imePadding()
        .navigationBarsPadding()
        .background(colors.backgroundColor)) {
        // Handle pill
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 12.dp)
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFCBD5E1))
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.primaryOrange.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Build, contentDescription = null,
                    tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
            }
            Text(
                if (isEditMode) "Editar Servicio" else "Agregar Nuevo Servicio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
        }
        BudgetServiceRow(service = currentItem, suggestionItems = suggestionItems, onUpdate = { currentItem = it })
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (isEditMode) onUpdateItem(currentItem)
                else {
                    val added = currentItem
                    onAddItem(added)
                    if (onSaveToSuggestions != null && suggestionItems.none { it.description.equals(added.description, ignoreCase = true) }) {
                        pendingItemToSave = added
                    }
                    currentItem = BudgetService()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = currentItem.description.isNotBlank() && currentItem.total > 0,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryOrange,
                contentColor = Color.White
            )
        ) {
            Text(
                if (isEditMode) "Guardar Cambios" else "Agregar Servicio",
                fontWeight = FontWeight.Bold
            )
        }

        // Lista de servicios ya agregados al presupuesto actual
        if (currentItems.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.border)
            Text(
                "Servicios en este presupuesto (${currentItems.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            currentItems.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.primaryOrange.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            buildString { if (item.code.isNotBlank()) append("[${item.code}] "); append(item.description) },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Text(
                            "\$${"%.2f".format(item.total)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                    IconButton(onClick = { currentItem = item }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = colors.primaryOrange, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { onDeleteCurrentItem?.invoke(index) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        pendingItemToSave?.let { newItem ->
            AlertDialog(
                onDismissRequest = { pendingItemToSave = null; onAddComplete() },
                title = { Text("Servicio nuevo", fontWeight = FontWeight.Bold) },
                text = { Text("\"${newItem.description}\" no está en tu lista.\n¿Deseas guardarlo para futuros presupuestos?") },
                confirmButton = {
                    TextButton(onClick = { onSaveToSuggestions?.invoke(newItem); pendingItemToSave = null; onAddComplete() }) {
                        Text("Guardar", color = colors.primaryOrange, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = { TextButton(onClick = { pendingItemToSave = null; onAddComplete() }) { Text("No, gracias") } }
            )
        }
        if (!isEditMode && suggestionItems.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = colors.border)
            Text("Servicios guardados (${suggestionItems.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = colors.textSecondary, modifier = Modifier.padding(bottom = 8.dp))
            var searchQuery by remember { mutableStateOf("") }
            OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = { Text("Buscar servicio...") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), singleLine = true, leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) }, trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) } }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primaryOrange, unfocusedBorderColor = colors.border, focusedLabelColor = colors.primaryOrange, cursorColor = colors.primaryOrange, focusedTextColor = colors.textPrimary, unfocusedTextColor = colors.textPrimary), shape = RoundedCornerShape(8.dp))
            val filtered = if (searchQuery.isBlank()) suggestionItems else suggestionItems.filter { it.description.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true) }
            filtered.forEach { saved ->
                key(saved.description) {
                    var showConfirmDelete by remember { mutableStateOf(false) }
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF3B82F6).copy(alpha = 0.06f)).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(buildString { if (saved.code.isNotBlank()) append("[${saved.code}] "); append(saved.description) }, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                            Text("\$${"%.2f".format(saved.total)}", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                        }
                        IconButton(onClick = { currentItem = saved.copy(id = currentItem.id) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = colors.primaryOrange, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { showConfirmDelete = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                    if (showConfirmDelete) {
                        AlertDialog(onDismissRequest = { showConfirmDelete = false }, title = { Text("¿Eliminar?", fontWeight = FontWeight.Bold) }, text = { Text("Se eliminará \"${saved.description}\" de la lista.") }, confirmButton = { TextButton(onClick = { onDeleteSaved?.invoke(saved); showConfirmDelete = false }) { Text("Eliminar", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(onClick = { showConfirmDelete = false }) { Text("Cancelar") } })
                    }
                }
            }
        }
    }
}

@Composable
fun AddProfessionalFeeSheetContent(
    itemToEdit: BudgetProfessionalFee?,
    onAddItem: (BudgetProfessionalFee) -> Unit,
    onUpdateItem: (BudgetProfessionalFee) -> Unit,
    currentItems: List<BudgetProfessionalFee> = emptyList(),
    onDeleteCurrentItem: ((Int) -> Unit)? = null,
    suggestionItems: List<BudgetProfessionalFee> = emptyList(),
    onDeleteSaved: ((BudgetProfessionalFee) -> Unit)? = null,
    onSaveToSuggestions: ((BudgetProfessionalFee) -> Unit)? = null,
    onAddComplete: () -> Unit = {}
) {
    val colors = getPrestadorColors()
    val isEditMode = itemToEdit != null
    var currentItem by remember { mutableStateOf(itemToEdit ?: BudgetProfessionalFee()) }
    var pendingItemToSave by remember { mutableStateOf<BudgetProfessionalFee?>(null) }

    Column(modifier = Modifier
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
        .imePadding()
        .navigationBarsPadding()
        .background(colors.backgroundColor)) {
        // Handle pill
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 12.dp)
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFCBD5E1))
        )
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.primaryOrange.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AttachMoney, contentDescription = null,
                    tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
            }
            Text(
                if (isEditMode) "Editar Honorario" else "Honorarios Profesionales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
        }
        BudgetProfessionalFeeRow(fee = currentItem, suggestionItems = suggestionItems, onUpdate = { updatedItem -> currentItem = updatedItem })
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onAddComplete() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, colors.border)
            ) {
                Text("Cancelar", color = colors.textPrimary)
            }
            Button(
                onClick = {
                    if (isEditMode) onUpdateItem(currentItem)
                    else {
                        val added = currentItem
                        onAddItem(added)
                        if (onSaveToSuggestions != null && suggestionItems.none { it.description.equals(added.description, ignoreCase = true) }) {
                            pendingItemToSave = added
                        }
                        currentItem = BudgetProfessionalFee()
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = currentItem.description.isNotBlank() && currentItem.total > 0,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryOrange,
                    contentColor = Color.White
                )
            ) {
                Text(
                    if (isEditMode) "Guardar Cambios" else "Guardar Ítem",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Lista de honorarios ya agregados al presupuesto actual
        if (currentItems.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.border)
            Text(
                "Honorarios en este presupuesto (${currentItems.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            currentItems.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.primaryOrange.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            buildString { if (item.code.isNotBlank()) append("[${item.code}] "); append(item.description) },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Text(
                            "\$${"%.2f".format(item.total)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                    IconButton(onClick = { currentItem = item }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = colors.primaryOrange, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { onDeleteCurrentItem?.invoke(index) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        pendingItemToSave?.let { newItem ->
            AlertDialog(
                onDismissRequest = { pendingItemToSave = null; onAddComplete() },
                title = { Text("Honorario nuevo", fontWeight = FontWeight.Bold) },
                text = { Text("\"${newItem.description}\" no está en tu lista.\n¿Deseas guardarlo para futuros presupuestos?") },
                confirmButton = {
                    TextButton(onClick = { onSaveToSuggestions?.invoke(newItem); pendingItemToSave = null; onAddComplete() }) {
                        Text("Guardar", color = colors.primaryOrange, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = { TextButton(onClick = { pendingItemToSave = null; onAddComplete() }) { Text("No, gracias") } }
            )
        }

        if (!isEditMode && suggestionItems.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = colors.border)
            Text("Honorarios guardados (${suggestionItems.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = colors.textSecondary, modifier = Modifier.padding(bottom = 8.dp))
            var searchQuery by remember { mutableStateOf("") }
            OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = { Text("Buscar honorario...") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), singleLine = true, leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) }, trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) } }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primaryOrange, unfocusedBorderColor = colors.border, focusedLabelColor = colors.primaryOrange, cursorColor = colors.primaryOrange, focusedTextColor = colors.textPrimary, unfocusedTextColor = colors.textPrimary), shape = RoundedCornerShape(8.dp))
            val filtered = if (searchQuery.isBlank()) suggestionItems else suggestionItems.filter { it.description.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true) }
            filtered.forEach { saved ->
                key(saved.description) {
                    var showConfirmDelete by remember { mutableStateOf(false) }
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF8B5CF6).copy(alpha = 0.06f)).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(buildString { if (saved.code.isNotBlank()) append("[${saved.code}] "); append(saved.description) }, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                            Text("\$${"%.2f".format(saved.total)}", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                        }
                        IconButton(onClick = { currentItem = saved.copy(id = currentItem.id) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = colors.primaryOrange, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { showConfirmDelete = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                    if (showConfirmDelete) {
                        AlertDialog(onDismissRequest = { showConfirmDelete = false }, title = { Text("¿Eliminar?", fontWeight = FontWeight.Bold) }, text = { Text("Se eliminará \"${saved.description}\" de la lista.") }, confirmButton = { TextButton(onClick = { onDeleteSaved?.invoke(saved); showConfirmDelete = false }) { Text("Eliminar", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(onClick = { showConfirmDelete = false }) { Text("Cancelar") } })
                    }
                }
            }
        }
    }
}

data class TempMiscExpense(
    val id: String = java.util.UUID.randomUUID().toString(),
    var description: String = "",
    var amount: String = ""
)

@Composable
fun AddMiscExpenseSheetContent(
    itemToEdit: BudgetMiscExpense?,
    existingItems: List<BudgetMiscExpense> = emptyList(),
    savedGastos: List<Pair<String, Double>> = emptyList(),
    onAddItem: (List<BudgetMiscExpense>) -> Unit,
    onUpdateItem: (BudgetMiscExpense) -> Unit,
    onDeleteItem: ((BudgetMiscExpense) -> Unit)? = null,
    onDeleteSaved: ((String) -> Unit)? = null,
    onUpdateSaved: ((String, String, Double) -> Unit)? = null
) {
    val colors = getPrestadorColors()
    val isEditMode = itemToEdit != null

    var description by remember { mutableStateOf(itemToEdit?.description ?: "") }
    var amountStr by remember { mutableStateOf(if ((itemToEdit?.amount ?: 0.0) > 0) itemToEdit!!.amount.toString() else "") }
    var showSuggestions by remember { mutableStateOf(false) }

    val enteredAmount = amountStr.toDoubleOrNull() ?: 0.0
    val filteredSuggestions = if (showSuggestions && description.length >= 2 && savedGastos.isNotEmpty()) {
        savedGastos.filter { it.first.contains(description, ignoreCase = true) }.take(5)
    } else emptyList()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .background(colors.backgroundColor)
    ) {
        // Handle pill
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 12.dp)
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFCBD5E1))
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.primaryOrange.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null,
                    tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
            }
            Text(
                if (isEditMode) "Editar Gasto" else "Agregar Gasto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Text("DESCRIPCIÓN", style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold, color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 4.dp))
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    showSuggestions = it.isNotBlank()
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(if (savedGastos.isNotEmpty()) "Descripción (${savedGastos.size} sugerencias)" else "...", style = MaterialTheme.typography.bodySmall) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryOrange,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    cursorColor = colors.primaryOrange
                )
            )
            // Sugerencias inline
            if (showSuggestions && filteredSuggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F5)),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column {
                        filteredSuggestions.forEachIndexed { index, (desc, amt) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        description = desc
                                        amountStr = if (amt > 0) amt.toString() else ""
                                        showSuggestions = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(desc, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                                    Text("\$${"%.2f".format(amt)}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                                }
                                Icon(Icons.Default.NorthWest, contentDescription = "Usar", tint = colors.primaryOrange, modifier = Modifier.size(16.dp))
                            }
                            if (index < filteredSuggestions.size - 1) HorizontalDivider(color = Color(0xFFE2E8F0))
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(bottom = 20.dp)) {
            Text("IMPORTE ($)", style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold, color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 4.dp))
            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it.filter { c -> c.isDigit() || c == '.' } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("0.00", style = MaterialTheme.typography.bodySmall) },
                prefix = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryOrange,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    cursorColor = colors.primaryOrange
                )
            )
        }

        Button(
            onClick = {
                val expense = BudgetMiscExpense(
                    id = itemToEdit?.id ?: System.currentTimeMillis(),
                    description = description,
                    amount = enteredAmount
                )
                if (isEditMode) {
                    onUpdateItem(expense)
                } else {
                    onAddItem(listOf(expense))
                    description = ""
                    amountStr = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = description.isNotBlank() && enteredAmount > 0,
            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange, contentColor = Color.White)
        ) {
            Text(if (isEditMode) "Guardar Cambios" else "Agregar Gasto", fontWeight = FontWeight.Bold)
        }

        // Lista de gastos existentes (solo en modo agregar)
        if (!isEditMode) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = colors.border)
            Text(
                "Gastos agregados",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (existingItems.isEmpty()) {
                Text(
                    "Sin gastos agregados aún",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            existingItems.forEach { expense ->
                key(expense.id) {
                var editingThis by remember { mutableStateOf(false) }
                var editDesc by remember { mutableStateOf(expense.description) }
                var editAmt by remember { mutableStateOf(expense.amount.toString()) }
                var showConfirmDelete by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.primaryOrange.copy(alpha = 0.06f))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(expense.description, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            if (!editingThis) Text("$ ${"%.2f".format(expense.amount)}", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                        }
                        IconButton(onClick = { editingThis = !editingThis }, modifier = Modifier.size(32.dp)) {
                            Icon(if (editingThis) Icons.Default.Close else Icons.Default.Edit, contentDescription = null, tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { showConfirmDelete = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                    }
                    if (editingThis) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editDesc,
                            onValueChange = { editDesc = it },
                            label = { Text("Descripción") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primaryOrange, unfocusedBorderColor = colors.border, focusedLabelColor = colors.primaryOrange, cursorColor = colors.primaryOrange, focusedTextColor = colors.textPrimary, unfocusedTextColor = colors.textPrimary),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = editAmt,
                            onValueChange = { editAmt = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Importe") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            singleLine = true,
                            prefix = { Text("$") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primaryOrange, unfocusedBorderColor = colors.border, focusedLabelColor = colors.primaryOrange, cursorColor = colors.primaryOrange, focusedTextColor = colors.textPrimary, unfocusedTextColor = colors.textPrimary),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Button(
                            onClick = {
                                val v = editAmt.toDoubleOrNull() ?: 0.0
                                if (editDesc.isNotBlank() && v > 0) {
                                    onUpdateItem(expense.copy(description = editDesc, amount = v))
                                    editingThis = false
                                }
                            },
                            enabled = editDesc.isNotBlank() && (editAmt.toDoubleOrNull() ?: 0.0) > 0,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange, contentColor = Color.White)
                        ) { Text("Guardar cambio", fontWeight = FontWeight.Bold) }
                    }
                }
                if (showConfirmDelete) {
                    AlertDialog(
                        onDismissRequest = { showConfirmDelete = false },
                        title = { Text("¿Eliminar?", fontWeight = FontWeight.Bold) },
                        text = { Text("Se eliminará \"${expense.description}\".") },
                        confirmButton = {
                            TextButton(onClick = { onDeleteItem?.invoke(expense); showConfirmDelete = false }) {
                                Text("Eliminar", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = { TextButton(onClick = { showConfirmDelete = false }) { Text("Cancelar") } }
                    )
                }
                } // key
            }

            // Gastos guardados (del catálogo / presupuestos anteriores)
            if (savedGastos.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = colors.border)
                Text(
                    "Guardados",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                savedGastos.forEach { (desc, amount) ->
                    key(desc) {
                        var editingThis by remember { mutableStateOf(false) }
                        var editDesc by remember { mutableStateOf(desc) }
                        var editAmt by remember { mutableStateOf(amount.toString()) }
                        var showConfirmDelete by remember { mutableStateOf(false) }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(desc, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                    if (!editingThis) Text("$ ${"%.2f".format(amount)}", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                                }
                                IconButton(onClick = { editingThis = !editingThis }, modifier = Modifier.size(32.dp)) {
                                    Icon(if (editingThis) Icons.Default.Close else Icons.Default.Edit, contentDescription = null, tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { showConfirmDelete = true }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                            if (editingThis) {
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = editDesc,
                                    onValueChange = { editDesc = it },
                                    label = { Text("Descripción") },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primaryOrange, unfocusedBorderColor = colors.border, focusedLabelColor = colors.primaryOrange, cursorColor = colors.primaryOrange, focusedTextColor = colors.textPrimary, unfocusedTextColor = colors.textPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                OutlinedTextField(
                                    value = editAmt,
                                    onValueChange = { editAmt = it.filter { c -> c.isDigit() || c == '.' } },
                                    label = { Text("Importe") },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    singleLine = true,
                                    prefix = { Text("$") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primaryOrange, unfocusedBorderColor = colors.border, focusedLabelColor = colors.primaryOrange, cursorColor = colors.primaryOrange, focusedTextColor = colors.textPrimary, unfocusedTextColor = colors.textPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Button(
                                    onClick = {
                                        val v = editAmt.toDoubleOrNull() ?: 0.0
                                        if (editDesc.isNotBlank() && v > 0) {
                                            onUpdateSaved?.invoke(desc, editDesc, v)
                                            editingThis = false
                                        }
                                    },
                                    enabled = editDesc.isNotBlank() && (editAmt.toDoubleOrNull() ?: 0.0) > 0,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange, contentColor = Color.White)
                                ) { Text("Guardar cambio", fontWeight = FontWeight.Bold) }
                            }
                        }
                        if (showConfirmDelete) {
                            AlertDialog(
                                onDismissRequest = { showConfirmDelete = false },
                                title = { Text("¿Eliminar?", fontWeight = FontWeight.Bold) },
                                text = { Text("Se eliminará \"$desc\" de todos los presupuestos guardados.") },
                                confirmButton = {
                                    TextButton(onClick = { onDeleteSaved?.invoke(desc); showConfirmDelete = false }) {
                                        Text("Eliminar", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = { TextButton(onClick = { showConfirmDelete = false }) { Text("Cancelar") } }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaxSheetContent(
    itemToEdit: BudgetTax?,
    subtotal: Double = 0.0,
    savedCustomTaxes: List<Pair<String, Double>> = emptyList(),
    onDeleteSaved: ((String) -> Unit)? = null,
    onUpdateSaved: ((String, Double) -> Unit)? = null,
    onAddItem: (List<BudgetTax>) -> Unit,
    onUpdateItem: (BudgetTax) -> Unit
) {
    val colors = getPrestadorColors()
    val isEditMode = itemToEdit != null

    var description by remember { mutableStateOf(itemToEdit?.description ?: "") }
    var valueStr by remember { mutableStateOf("") }
    var isPercentage by remember { mutableStateOf(true) }

    // Pre-fill when editing
    LaunchedEffect(itemToEdit) {
        if (itemToEdit != null) {
            description = itemToEdit.description
            // Try to detect if it was a percentage (description ends with %)
            val pctFromDesc = Regex("(\\d+(?:\\.\\d+)?)%").find(itemToEdit.description)?.groupValues?.get(1)?.toDoubleOrNull()
            if (pctFromDesc != null && subtotal > 0) {
                isPercentage = true
                valueStr = pctFromDesc.toString()
            } else {
                isPercentage = false
                valueStr = if (itemToEdit.amount > 0) itemToEdit.amount.toString() else ""
            }
        }
    }

    val computedAmount = remember(valueStr, isPercentage, subtotal) {
        val v = valueStr.toDoubleOrNull() ?: 0.0
        if (isPercentage) subtotal * v / 100.0 else v
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .background(colors.backgroundColor)
    ) {
        // Handle pill
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 12.dp)
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFCBD5E1))
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.primaryOrange.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Percent, contentDescription = null,
                    tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
            }
            Text(
                if (isEditMode) "Editar Impuesto" else "Impuesto Personalizado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            singleLine = true,
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

        // Value + toggle
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = valueStr,
                onValueChange = { valueStr = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text(if (isPercentage) "Porcentaje" else "Importe") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                suffix = { Text(if (isPercentage) "%" else "$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
            // % / $ toggle
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
            ) {
                listOf("%" to true, "$" to false).forEachIndexed { idx, (label, isPct) ->
                    val selected = isPercentage == isPct
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(if (selected) colors.primaryOrange else Color.Transparent)
                            .clickable { isPercentage = isPct }
                            .width(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else colors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (idx == 0) Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(colors.border))
                }
            }
        }

        val enteredValue = valueStr.toDoubleOrNull() ?: 0.0
        val finalAmount = if (isPercentage) subtotal * enteredValue / 100.0 else enteredValue

        // Preview
        if (enteredValue > 0 && subtotal > 0) {
            val previewText = if (isPercentage)
                "${"%.1f".format(enteredValue)}% de ${"%.2f".format(subtotal)} = ${"%.2f".format(finalAmount)}"
            else
                "${"%.2f".format(enteredValue)} sobre subtotal ${"%.2f".format(subtotal)}"
            Text(
                previewText,
                style = MaterialTheme.typography.bodySmall,
                color = colors.primaryOrange,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Personalizados guardados
        if (savedCustomTaxes.isNotEmpty() && !isEditMode) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = colors.border)
            Text(
                "Guardados",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            savedCustomTaxes.forEach { (savedDesc, savedAmt) ->
                var editMode by remember { mutableStateOf(false) }
                var editValueStr by remember { mutableStateOf(savedAmt.toString()) }
                var editIsPercent by remember { mutableStateOf(Regex("\\d+%").containsMatchIn(savedDesc)) }
                var showDelete by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.primaryOrange.copy(alpha = 0.06f))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(savedDesc, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            if (!editMode) Text("${"%.2f".format(savedAmt)}", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                        }
                        IconButton(onClick = { editMode = !editMode }, modifier = Modifier.size(32.dp)) {
                            Icon(if (editMode) Icons.Default.Close else Icons.Default.Edit, contentDescription = null, tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { showDelete = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                    }
                    if (editMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = editValueStr,
                                onValueChange = { editValueStr = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text(if (editIsPercent) "Porcentaje" else "Importe") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                suffix = { Text(if (editIsPercent) "%" else "$") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                            Row(
                                modifier = Modifier
                                    .height(56.dp)
                                    .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                listOf("%" to true, "$" to false).forEachIndexed { idx, (lbl, isPct) ->
                                    val sel = editIsPercent == isPct
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .background(if (sel) colors.primaryOrange else Color.Transparent)
                                            .clickable { editIsPercent = isPct }
                                            .width(44.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(lbl, fontWeight = FontWeight.Bold, color = if (sel) Color.White else colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (idx == 0) Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(colors.border))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val v = editValueStr.toDoubleOrNull() ?: 0.0
                                val newAmt = if (editIsPercent) subtotal * v / 100.0 else v
                                onUpdateSaved?.invoke(savedDesc, newAmt)
                                editMode = false
                            },
                            enabled = editValueStr.toDoubleOrNull() != null && editValueStr.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange, contentColor = Color.White)
                        ) { Text("Guardar cambio", fontWeight = FontWeight.Bold) }
                    }
                }
                if (showDelete) {
                    AlertDialog(
                        onDismissRequest = { showDelete = false },
                        title = { Text("¿Eliminar?", fontWeight = FontWeight.Bold) },
                        text = { Text("Se eliminará \"$savedDesc\" de las sugerencias.") },
                        confirmButton = {
                            TextButton(onClick = { onDeleteSaved?.invoke(savedDesc); showDelete = false }) {
                                Text("Eliminar", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancelar") } }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {},
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, colors.border)
            ) {
                Text("Cancelar", color = colors.textPrimary)
            }
            Button(
                onClick = {
                    val autoDesc = if (isPercentage) "${valueStr}%" else "$$valueStr"
                    val tax = BudgetTax(
                        id = itemToEdit?.id ?: System.currentTimeMillis(),
                        description = description.ifBlank { autoDesc },
                        amount = finalAmount
                    )
                    if (isEditMode) onUpdateItem(tax) else onAddItem(listOf(tax))
                },
                modifier = Modifier.weight(1f),
                enabled = enteredValue > 0,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange, contentColor = Color.White)
            ) {
                Text(if (isEditMode) "Guardar Cambios" else "Guardar Ítem", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AddAttachmentSheetContent(
    itemToEdit: BudgetAttachment?,
    onAddItem: (BudgetAttachment) -> Unit,
    onUpdateItem: (BudgetAttachment) -> Unit
) {
    val colors = getPrestadorColors()
    val isEditMode = itemToEdit != null
    var currentItem by remember { mutableStateOf(itemToEdit ?: BudgetAttachment()) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            currentItem = currentItem.copy(uri = uri, type = if (uri.toString().endsWith("pdf")) AttachmentType.PDF else AttachmentType.IMAGE)
        }
    }

    Column(modifier = Modifier
        .padding(16.dp)
        .imePadding()
        .navigationBarsPadding()
        .background(colors.backgroundColor)) {
        Text(
            "Adjuntar Archivo", 
            style = MaterialTheme.typography.titleLarge, 
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color(0xFFFFE5D9), RoundedCornerShape(12.dp))
                .clickable { launcher.launch("*/*") },
            contentAlignment = Alignment.Center
        ) {
            if (currentItem.uri != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (currentItem.type == AttachmentType.PDF) Icons.Default.PictureAsPdf else Icons.Default.Image, 
                        contentDescription = null, 
                        modifier = Modifier.size(48.dp), 
                        tint = colors.primaryOrange
                    )
                    Text(
                        "Archivo seleccionado", 
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textPrimary
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AttachFile, 
                        contentDescription = null, 
                        modifier = Modifier.size(48.dp), 
                        tint = colors.primaryOrange
                    )
                    Text(
                        "Toque para seleccionar (Imagen/PDF)", 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = colors.textSecondary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        CompactTextField(value = currentItem.description, onValueChange = { currentItem = currentItem.copy(description = it) }, label = { Text("Descripción / Detalle") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { if (isEditMode) onUpdateItem(currentItem) else onAddItem(currentItem) }, 
            modifier = Modifier.fillMaxWidth(), 
            enabled = currentItem.uri != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryOrange,
                contentColor = Color.White
            )
        ) {
            Text("Adjuntar", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SectionsSheetContent(
    isProfessional: Boolean = false,
    showArticles: Boolean,
    showServices: Boolean,
    showProfessionalFees: Boolean,
    showMisc: Boolean,
    showTaxes: Boolean,
    showAttachments: Boolean,
    onShowArticlesChange: (Boolean) -> Unit,
    onShowServicesChange: (Boolean) -> Unit,
    onShowProfessionalFeesChange: (Boolean) -> Unit,
    onShowMiscChange: (Boolean) -> Unit,
    onShowTaxesChange: (Boolean) -> Unit,
    onShowAttachmentsChange: (Boolean) -> Unit
) {
    val colors = getPrestadorColors()
    Column(modifier = Modifier
        .padding(16.dp)
        .navigationBarsPadding()
        .background(colors.backgroundColor)) {
        Text(
            "Mostrar/Ocultar Secciones", 
            style = MaterialTheme.typography.titleLarge, 
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (!isProfessional) {
            SectionSwitch(title = "Artículos", checked = showArticles, onCheckedChange = onShowArticlesChange)
            SectionSwitch(title = "Mano de Obra / Servicios", checked = showServices, onCheckedChange = onShowServicesChange)
            SectionSwitch(title = "Gastos Varios", checked = showMisc, onCheckedChange = onShowMiscChange)
            SectionSwitch(title = "Impuestos", checked = showTaxes, onCheckedChange = onShowTaxesChange)
        }
        SectionSwitch(title = "Honorarios del Profesional", checked = showProfessionalFees, onCheckedChange = onShowProfessionalFeesChange)
        SectionSwitch(title = "Archivos Adjuntos", checked = showAttachments, onCheckedChange = onShowAttachmentsChange)
    }
}

@Composable
fun SectionSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = getPrestadorColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title, 
            modifier = Modifier.weight(1f),
            color = colors.textPrimary
        )
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.primaryOrange,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = colors.border
            )
        )
    }
}

@Composable
fun ClientDetailsSheetContent(
    clienteNombre: String,
    onClienteNombreChange: (String) -> Unit,
    clienteEmail: String,
    onClienteEmailChange: (String) -> Unit,
    clienteTelefono: String,
    onClienteTelefonoChange: (String) -> Unit,
    clienteDireccion: String,
    onClienteDireccionChange: (String) -> Unit,
    onClose: () -> Unit,
    onSave: () -> Unit
) {
    val colors = getPrestadorColors()

    val fieldShape = RoundedCornerShape(12.dp)
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.primaryOrange,
        unfocusedBorderColor = colors.border,
        focusedLabelColor = colors.primaryOrange,
        unfocusedLabelColor = colors.textSecondary,
        cursorColor = colors.primaryOrange,
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .navigationBarsPadding()
            .background(colors.backgroundColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Datos del Cliente",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = colors.textSecondary)
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = clienteNombre,
            onValueChange = onClienteNombreChange,
            label = { Text("Nombre") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = colors.textSecondary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            colors = textFieldColors,
            shape = fieldShape
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = clienteEmail,
            onValueChange = onClienteEmailChange,
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = colors.textSecondary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            colors = textFieldColors,
            shape = fieldShape
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = clienteTelefono,
            onValueChange = { value ->
                onClienteTelefonoChange(value.filter { c -> c.isDigit() || c == '+' || c == ' ' })
            },
            label = { Text("Teléfono") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = colors.textSecondary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            colors = textFieldColors,
            shape = fieldShape
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = clienteDireccion,
            onValueChange = onClienteDireccionChange,
            label = { Text("Dirección") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = colors.textSecondary) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            colors = textFieldColors,
            shape = fieldShape
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    onClienteNombreChange("")
                    onClienteEmailChange("")
                    onClienteTelefonoChange("")
                    onClienteDireccionChange("")
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryOrange),
                border = BorderStroke(1.dp, colors.primaryOrange.copy(alpha = 0.5f)),
                shape = fieldShape
            ) {
                Text("Limpiar")
            }

            Button(
                onClick = onSave,
                enabled = clienteNombre.isNotBlank(),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryOrange,
                    contentColor = Color.White
                ),
                shape = fieldShape
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ClientPickerSheetContent(
    clientes: List<ClienteEntity>,
    selectedClienteId: String?,
    onSelectCliente: (ClienteEntity) -> Unit,
    onClose: () -> Unit
) {
    val colors = getPrestadorColors()
    var query by remember { mutableStateOf("") }

    val filtered = remember(clientes, query) {
        val q = query.trim().lowercase()
        if (q.isBlank()) clientes
        else clientes.filter { c ->
            c.nombre.lowercase().contains(q) ||
                (c.email ?: "").lowercase().contains(q) ||
                (c.telefono ?: "").lowercase().contains(q)
        }
    }

    val fieldShape = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .padding(16.dp)
            .navigationBarsPadding()
            .background(colors.backgroundColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Buscar cliente",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = colors.textSecondary)
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Buscar") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.textSecondary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primaryOrange,
                unfocusedBorderColor = colors.border,
                focusedLabelColor = colors.primaryOrange,
                unfocusedLabelColor = colors.textSecondary,
                cursorColor = colors.primaryOrange,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            shape = fieldShape
        )

        Spacer(Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Text(
                "No se encontraron clientes.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.id }) { cliente ->
                    val isSelected = cliente.id == selectedClienteId
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) colors.primaryOrange.copy(alpha = 0.10f) else colors.surfaceColor
                        ),
                        shape = RoundedCornerShape(14.dp),
                        onClick = { onSelectCliente(cliente) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(cliente.nombre, color = colors.textPrimary, fontWeight = FontWeight.SemiBold)
                            val line2 = listOfNotNull(
                                cliente.telefono?.takeIf { it.isNotBlank() },
                                cliente.email?.takeIf { it.isNotBlank() }
                            ).joinToString(" • ")
                            if (line2.isNotBlank()) {
                                Text(line2, color = colors.textSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                            val addr = cliente.direccion?.takeIf { it.isNotBlank() }
                            if (addr != null) {
                                Text(addr, color = colors.textSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }


    }
}