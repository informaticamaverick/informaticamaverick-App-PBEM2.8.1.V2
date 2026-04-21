
package com.example.myapplication.prestador.ui.promotion

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.prestador.data.model.PromotionType
import com.example.myapplication.prestador.data.model.ProviderPromotion
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.CreatePromotionViewModel
import com.example.myapplication.prestador.viewmodel.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.ProfileState
import kotlinx.coroutines.flow.flowOf

private const val TITLE_MAX = 60
private const val DESC_MAX  = 300

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePromotionScreen(
    onBack: () -> Unit,
    onPublish: (ProviderPromotion) -> Unit,
    promotionId: String? = null,            // null = crear, non-null = editar
    viewModel: CreatePromotionViewModel = hiltViewModel(),
    editProfileViewModel: EditProfileViewModel = hiltViewModel()
) {
    val isEditMode = promotionId != null
    val colors = getPrestadorColors()
    val context = LocalContext.current

    val profileState by editProfileViewModel.profileState.collectAsState()
    val provider = (profileState as? ProfileState.Success)?.provider
    val providerId = provider?.id.orEmpty()
    val providerName = when {
        provider?.tieneEmpresa == true && !provider.nombreEmpresa.isNullOrBlank() -> provider.nombreEmpresa!!
        else -> "${provider?.name.orEmpty()} ${provider?.apellido.orEmpty()}".trim().ifBlank { "Prestador" }
    }
    val providerImageUrl = provider?.imageUrl

    var selectedType       by remember { mutableStateOf(PromotionType.PROMOTION) }
    var title              by remember { mutableStateOf("") }
    var description        by remember { mutableStateOf("") }
    var selectedImages     by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var existingImageUrls  by remember { mutableStateOf<List<String>>(emptyList()) }
    var existingPromotion  by remember { mutableStateOf<ProviderPromotion?>(null) }
    var discount           by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var errorMessage       by remember { mutableStateOf<String?>(null) }
    var showPreview        by remember { mutableStateOf(false) }

    // Cargar datos existentes cuando se abre en modo edición
    val existingPromotionFlow = if (isEditMode) viewModel.getPromotionByIdAsModel(promotionId!!) else null
    val loadedPromotion by (existingPromotionFlow ?: kotlinx.coroutines.flow.flowOf(null)).collectAsState(initial = null)
    LaunchedEffect(loadedPromotion) {
        loadedPromotion?.let { promo ->
            if (existingPromotion == null) {
                existingPromotion  = promo
                title              = promo.title
                description        = promo.description
                discount           = promo.discount?.toString() ?: ""
                selectedCategories = promo.categories.toSet()
                selectedType       = promo.type
                existingImageUrls  = promo.imageUrls
            }
        }
    }

    val isLoading       by viewModel.isLoading.collectAsState()
    val successMessage  by viewModel.successMessage.collectAsState()
    val vmErrorMessage  by viewModel.errorMessage.collectAsState()

    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(vmErrorMessage) {
        vmErrorMessage?.let { errorMessage = it; viewModel.clearMessages() }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val totalAfterAdd = existingImageUrls.size + selectedImages.size + uris.size
        if (totalAfterAdd <= 3) { selectedImages = selectedImages + uris; errorMessage = null }
        else errorMessage = "Máximo 3 imágenes en total"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        // ── HEADER GRADIENTE ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(colors.primaryOrange, colors.primaryOrangeDark)
                    ),
                    shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                )
                .padding(top = 48.dp, start = 8.dp, end = 16.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBackIosNew,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isEditMode) "Editar publicación" else "Nueva publicación",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        if (isEditMode) "Actualizá tu oferta" else "Llegá a más clientes con tu oferta",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                // Botón preview
                Surface(
                    onClick = { showPreview = !showPreview },
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = if (showPreview) 0.35f else 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "Preview",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Preview", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── PREVIEW EN VIVO ───────────────────────────────────────
            AnimatedVisibility(
                visible = showPreview,
                enter = expandVertically(tween(300)) + fadeIn(),
                exit = shrinkVertically(tween(300)) + fadeOut()
            ) {
                PromoPreviewCard(
                    title = title.ifBlank { "Título de tu publicación" },
                    description = description.ifBlank { "Descripción de tu oferta..." },
                    imageUri = selectedImages.firstOrNull(),
                    discount = discount.toIntOrNull(),
                    type = selectedType,
                    providerName = providerName,
                    colors = colors
                )
            }

            // ── SELECTOR DE TIPO ──────────────────────────────────────
            SectionCard(colors = colors) {
                SectionTitle("Tipo de publicación", Icons.Default.Campaign, colors)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PromotionType.values().forEach { type ->
                        TypeCard(
                            type = type,
                            isSelected = selectedType == type,
                            onClick = { selectedType = type },
                            colors = colors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── TÍTULO ────────────────────────────────────────────────
            SectionCard(colors = colors) {
                SectionTitle("Título", Icons.Default.Title, colors)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= TITLE_MAX) title = it },
                    placeholder = { Text("Ej: Instalación eléctrica con 20% OFF", color = colors.textSecondary.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primaryOrange,
                        unfocusedBorderColor = colors.border,
                        focusedLabelColor = colors.primaryOrange,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        "${title.length}/$TITLE_MAX",
                        fontSize = 11.sp,
                        color = if (title.length > TITLE_MAX * 0.9) colors.primaryOrange else colors.textSecondary.copy(alpha = 0.6f)
                    )
                }
            }

            // ── DESCRIPCIÓN ───────────────────────────────────────────
            SectionCard(colors = colors) {
                SectionTitle("Descripción", Icons.Default.Description, colors)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= DESC_MAX) description = it },
                    placeholder = { Text("Contá qué ofrecés, por qué elegirte...", color = colors.textSecondary.copy(alpha = 0.6f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primaryOrange,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        "${description.length}/$DESC_MAX",
                        fontSize = 11.sp,
                        color = if (description.length > DESC_MAX * 0.9) colors.primaryOrange else colors.textSecondary.copy(alpha = 0.6f)
                    )
                }
            }

            // ── IMÁGENES ──────────────────────────────────────────────
            SectionCard(colors = colors) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SectionTitle("Imágenes", Icons.Default.PhotoLibrary, colors)
                    Text(
                        "${existingImageUrls.size + selectedImages.size}/3",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(10.dp))
                // Imágenes ya guardadas (modo edición)
                if (existingImageUrls.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        items(existingImageUrls) { url: String ->
                            Box {
                                AsyncImage(

                                    model = url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                                IconButton(
                                    onClick = { existingImageUrls = existingImageUrls.filter { it != url } },
                                    modifier = Modifier
                                        .size(22.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color(0xFFE53935), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                ImageSelector(
                    selectedImages = selectedImages,
                    onAddImages = { imagePickerLauncher.launch("image/*") },
                    onRemoveImage = { uri -> selectedImages = selectedImages.filter { it != uri } },
                    colors = colors
                )
            }

            // ── CATEGORÍAS ────────────────────────────────────────────
            SectionCard(colors = colors) {
                SectionTitle("Categorías", Icons.Default.Category, colors)
                Spacer(Modifier.height(10.dp))
                CategorySelector(
                    selectedCategories = selectedCategories,
                    onShowDialog = { showCategoryDialog = true },
                    colors = colors
                )
            }

            // ── DESCUENTO ─────────────────────────────────────────────
            SectionCard(colors = colors) {
                SectionTitle("Descuento (opcional)", Icons.Default.LocalOffer, colors)
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = discount,
                        onValueChange = {
                            if (it.isEmpty() || (it.toIntOrNull() in 1..100)) discount = it
                        },
                        placeholder = { Text("Ej: 30", color = colors.textSecondary.copy(alpha = 0.6f)) },
                        suffix = { Text("%", color = colors.primaryOrange, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primaryOrange,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                    if (discount.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.primaryOrange.copy(alpha = 0.12f)
                        ) {
                            Text(
                                "${discount}% OFF",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.primaryOrange
                            )
                        }
                    }
                }
            }

            // ── ERROR ─────────────────────────────────────────────────
            AnimatedVisibility(visible = errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.error.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, colors.error.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, null, tint = colors.error, modifier = Modifier.size(18.dp))
                        Text(errorMessage ?: "", color = colors.error, fontSize = 13.sp)
                    }
                }
            }

            // ── BOTONES ───────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Botón izquierdo: "Borrador" (crear) / "Cancelar" (editar)
                OutlinedButton(
                    onClick = {
                        if (isEditMode) {
                            onBack()
                        } else {
                            if (providerId.isBlank()) { errorMessage = "No se pudo cargar tu perfil"; return@OutlinedButton }
                            viewModel.createPromotion(
                                providerId = providerId, providerName = providerName,
                                providerImageUrl = providerImageUrl, type = selectedType,
                                title = title, description = description,
                                imageUrls = selectedImages, discount = discount,
                                categories = selectedCategories, onSuccess = { onBack() }
                            )
                        }
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, colors.primaryOrange),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryOrange)
                ) {
                    Icon(
                        if (isEditMode) Icons.Default.Close else Icons.Default.BookmarkBorder,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isEditMode) "Cancelar" else "Borrador",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Botón principal: "Publicar" (crear) / "Guardar cambios" (editar)
                Button(
                    onClick = {
                        errorMessage = null
                        val allImages = existingImageUrls + selectedImages.map { it.toString() }
                        if (isEditMode) {
                            val promo = existingPromotion ?: return@Button
                            viewModel.updatePromotion(
                                existing = promo,
                                type = selectedType,
                                title = title,
                                description = description,
                                imageUrls = allImages,
                                discount = discount,
                                categories = selectedCategories,
                                onSuccess = { onBack() }
                            )
                        } else {
                            if (providerId.isBlank()) { errorMessage = "No se pudo cargar tu perfil"; return@Button }
                            viewModel.createPromotion(
                                providerId = providerId, providerName = providerName,
                                providerImageUrl = providerImageUrl, type = selectedType,
                                title = title, description = description,
                                imageUrls = selectedImages, discount = discount,
                                categories = selectedCategories, onSuccess = { onBack() }
                            )
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.weight(2f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(
                            if (isEditMode) Icons.Default.Save else Icons.Default.Send,
                            null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isEditMode) "Guardar cambios" else "Publicar ahora",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showCategoryDialog) {
        CategoryDialog(
            selectedCategories = selectedCategories,
            onCategoryToogle = { category ->
                selectedCategories = if (selectedCategories.contains(category))
                    selectedCategories - category else selectedCategories + category
            },
            onDismiss = { showCategoryDialog = false },
            colors = colors
        )
    }
}

// ── SECCIÓN CARD ──────────────────────────────────────────────────
@Composable
private fun SectionCard(
    colors: PrestadorColors,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceColor,
        border = BorderStroke(0.8.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

// ── TÍTULO DE SECCIÓN ─────────────────────────────────────────────
@Composable
private fun SectionTitle(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, colors: PrestadorColors) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(colors.primaryOrange.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = colors.primaryOrange, modifier = Modifier.size(16.dp))
        }
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
    }
}

// ── PREVIEW EN VIVO ───────────────────────────────────────────────
@Composable
private fun PromoPreviewCard(
    title: String,
    description: String,
    imageUri: Uri?,
    discount: Int?,
    type: PromotionType,
    providerName: String,
    colors: PrestadorColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceColor,
        border = BorderStroke(1.5.dp, colors.primaryOrange.copy(alpha = 0.4f)),
        shadowElevation = 4.dp
    ) {
        Column {
            // Banner "Vista previa"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(colors.primaryOrange, colors.primaryOrangeDark)),
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Visibility, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text("Así se verá tu publicación", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Imagen
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.primaryOrange.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.Image, null, tint = colors.primaryOrange.copy(alpha = 0.4f), modifier = Modifier.size(32.dp))
                    }
                    // Badge descuento
                    if (discount != null && discount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .background(colors.primaryOrange, RoundedCornerShape(6.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text("$discount%", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    // Badge tipo
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (type == PromotionType.STORY) Color(0xFF7B1FA2).copy(alpha = 0.12f)
                                else colors.primaryOrange.copy(alpha = 0.12f)
                    ) {
                        Text(
                            if (type == PromotionType.STORY) "📖 Historia · 24h" else "📢 Promo · 7 días",
                            fontSize = 10.sp,
                            color = if (type == PromotionType.STORY) Color(0xFF7B1FA2) else colors.primaryOrange,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(3.dp))
                    Text(description, fontSize = 11.sp, color = colors.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 15.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("👤 $providerName", fontSize = 11.sp, color = colors.textSecondary)
                }
            }
        }
    }
}

// ── SELECTOR DE TIPO ──────────────────────────────────────────────
@Composable
fun PromotionTypeSelector(
    selectedType: PromotionType,
    onTypeSelected: (PromotionType) -> Unit,
    colors: PrestadorColors
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PromotionType.values().forEach { type ->
            TypeCard(
                type = type,
                isSelected = selectedType == type,
                onClick = { onTypeSelected(type) },
                colors = colors,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TypeCard(
    type: PromotionType,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: PrestadorColors,
    modifier: Modifier = Modifier
) {
    val (icon, label, sublabel, tipeColor) = when (type) {
        PromotionType.STORY     -> arrayOf(Icons.Default.FlashOn, "Historia", "Dura 24 horas", Color(0xFF7B1FA2))
        PromotionType.PROMOTION -> arrayOf(Icons.Default.Campaign, "Promoción", "Dura 7 días",  colors.primaryOrange)
    }
    val cardColor = (tipeColor as Color)

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) cardColor else colors.surfaceColor,
        border = BorderStroke(
            width = if (isSelected) 0.dp else 1.dp,
            color = if (isSelected) Color.Transparent else colors.border
        ),
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.2f) else cardColor.copy(alpha = 0.12f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon as androidx.compose.ui.graphics.vector.ImageVector,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else cardColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                label as String,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else colors.textPrimary
            )
            Text(
                sublabel as String,
                fontSize = 11.sp,
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else colors.textSecondary
            )
        }
    }
}

// ── SELECTOR DE IMÁGENES ──────────────────────────────────────────
@Composable
fun ImageSelector(
    selectedImages: List<Uri>,
    onAddImages: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    colors: PrestadorColors
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (selectedImages.size < 3) {
            item {
                Surface(
                    onClick = onAddImages,
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = colors.primaryOrange.copy(alpha = 0.06f),
                    border = BorderStroke(1.5.dp, colors.primaryOrange.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, null, tint = colors.primaryOrange, modifier = Modifier.size(30.dp))
                        Spacer(Modifier.height(4.dp))
                        Text("Agregar", fontSize = 11.sp, color = colors.primaryOrange, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        itemsIndexed(selectedImages) { index, uri ->
            Box(modifier = Modifier.size(100.dp)) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
                // Indicador posición
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text("${index + 1}/3", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                // Botón eliminar
                IconButton(
                    onClick = { onRemoveImage(uri) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(22.dp)
                        .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

// ── SELECTOR DE CATEGORÍAS ────────────────────────────────────────
@Composable
fun CategorySelector(
    selectedCategories: Set<String>,
    onShowDialog: () -> Unit,
    colors: PrestadorColors
) {
    Surface(
        onClick = onShowDialog,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.backgroundColor,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedCategories.isEmpty()) "Seleccionar categorías"
                       else "${selectedCategories.size} seleccionada(s)",
                color = if (selectedCategories.isEmpty()) colors.textSecondary else colors.textPrimary,
                fontSize = 14.sp
            )
            Icon(Icons.Default.ChevronRight, null, tint = colors.textSecondary)
        }
    }
    if (selectedCategories.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(selectedCategories.toList()) { category ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = colors.primaryOrange.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, colors.primaryOrange.copy(alpha = 0.3f))
                ) {
                    Text(
                        category,
                        fontSize = 12.sp,
                        color = colors.primaryOrange,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

// ── DIÁLOGO DE CATEGORÍAS ─────────────────────────────────────────
@Composable
fun CategoryDialog(
    selectedCategories: Set<String>,
    onCategoryToogle: (String) -> Unit,
    onDismiss: () -> Unit,
    colors: PrestadorColors
) {
    val categories = listOf(
        "Plomería", "Electricidad", "Carpintería", "Pintura",
        "Albañilería", "Jardinería", "Limpieza", "Cerrajería",
        "Aire Acondicionado", "Reparaciones Generales"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccioná categorías", fontWeight = FontWeight.Bold, color = colors.textPrimary) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                categories.forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategoryToogle(category) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedCategories.contains(category),
                            onCheckedChange = { onCategoryToogle(category) },
                            colors = CheckboxDefaults.colors(checkedColor = colors.primaryOrange)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(category, color = colors.textPrimary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Listo") }
        },
        containerColor = colors.surfaceColor,
        shape = RoundedCornerShape(20.dp)
    )
}












