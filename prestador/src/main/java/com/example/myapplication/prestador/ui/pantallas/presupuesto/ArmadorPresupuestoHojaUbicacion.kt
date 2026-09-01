package com.example.myapplication.prestador.ui.pantallas.presupuesto

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.prestador.datos.local.entidades.BorradorPresupuestoEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetDireccionManual(
    borrador: BorradorPresupuestoEntity,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String) -> Unit
) {
    var calle by remember { mutableStateOf(borrador.calleManual ?: "") }
    var numero by remember { mutableStateOf(borrador.numeroManual ?: "") }
    //var piso by remember { mutableStateOf(borrador.pisoManual ?: "") }
    //var depto by remember { mutableStateOf(borrador.deptoManual ?: "") }
    var localidad by remember { mutableStateOf(borrador.localidadManual ?: "") }
  //  var provincia by remember { mutableStateOf(borrador.provinciaManual ?: "") }
    var cp by remember { mutableStateOf(borrador.cpManual ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ArmadorPresupuestoTema.SurfaceCardSolid,
        dragHandle = { BottomSheetDefaults.DragHandle(color = ArmadorPresupuestoTema.TextMuted) },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UBICACIÓN MANUAL",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, null, tint = ArmadorPresupuestoTema.TextMuted)
                }
            }

            FilaInputDetalle(label = "CALLE", value = calle, onValueChange = { calle = it }, placeholder = "Ej: Av. Rivadavia")
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilaInputDetalle(label = "NÚMERO", value = numero, onValueChange = { numero = it }, placeholder = "1234", modifier = Modifier.weight(1f))
             //   FilaInputDetalle(label = "PISO", value = piso, onValueChange = { piso = it }, placeholder = "4", modifier = Modifier.weight(0.5f))
           //     FilaInputDetalle(label = "DEPTO", value = depto, onValueChange = { depto = it }, placeholder = "B", modifier = Modifier.weight(0.5f))
            }

            FilaInputDetalle(label = "LOCALIDAD / BARRIO", value = localidad, onValueChange = { localidad = it }, placeholder = "Ej: Caballito")
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
             //   FilaInputDetalle(label = "PROVINCIA", value = provincia, onValueChange = { provincia = it }, placeholder = "Ej: Tucumán", modifier = Modifier.weight(1.5f))
                FilaInputDetalle(label = "C. POSTAL", value = cp, onValueChange = { cp = it }, placeholder = "4000", modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                 //   onConfirm(calle, numero, piso, depto, localidad, provincia, cp)
                },
                enabled = calle.isNotBlank() && numero.isNotBlank() && localidad.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ArmadorPresupuestoTema.BrandOrange,
                    disabledContainerColor = ArmadorPresupuestoTema.BrandOrange.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { 
                Text(
                    text = "CONFIRMAR UBICACIÓN", 
                    color = if (calle.isNotBlank()) Color.Black else ArmadorPresupuestoTema.TextMuted, 
                    fontWeight = FontWeight.Black, 
                    fontSize = 14.sp
                ) 
            }
        }
    }
}
