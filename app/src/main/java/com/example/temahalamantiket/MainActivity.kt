package com.example.temahalamantiket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF3F4F6)
                ) {
                    // Parent yang mengelola semua state (State Hoisting)
                    PemesananTiketScreen()
                }
            }
        }
    }
}

enum class StatusType { IDLE, PROCESSING, SUCCESS, ERROR }

@Composable
fun PemesananTiketScreen() {

    var namaPembeli by rememberSaveable { mutableStateOf("") }
    var jumlahTiket by rememberSaveable { mutableStateOf(1) }
    var hargaTiket by rememberSaveable { mutableStateOf(50000) } // harga per tiket

    var orderTrigger by rememberSaveable { mutableStateOf(0) }

    var statusType by rememberSaveable { mutableStateOf(StatusType.IDLE) }


    LaunchedEffect(orderTrigger) {
        if (orderTrigger > 0) {
            statusType = StatusType.PROCESSING
            delay(5000) // ganti angka ini jika ingin durasi berbeda (mis. 2000 = 2 detik)
            statusType = StatusType.SUCCESS
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        TiketHeader()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            NamaInputField(
                nama = namaPembeli,
                isError = statusType == StatusType.ERROR,
                onNamaChange = { newValue ->
                    namaPembeli = newValue
                    // Reset status error begitu user mulai mengetik lagi
                    if (statusType == StatusType.ERROR) {
                        statusType = StatusType.IDLE
                    }
                }
            )

            JumlahTiketSelector(
                jumlah = jumlahTiket,
                onIncrement = { jumlahTiket++ },
                onDecrement = { if (jumlahTiket > 1) jumlahTiket-- }
            )

            Text(
                text = "Total: Rp ${hargaTiket * jumlahTiket}",
                fontSize = 14.sp,
                color = Color(0xFF4B5563)
            )

            PesanTiketButton(
                enabled = statusType != StatusType.PROCESSING,
                onClick = {
                    if (namaPembeli.isBlank()) {
                        // ----- Status: Nama Masih Kosong -----
                        statusType = StatusType.ERROR
                    } else {
                        // Menaikkan trigger -> LaunchedEffect di atas berjalan
                        orderTrigger++
                    }
                }
            )

            StatusCard(statusType)
        }
    }
}

/**
 * ==========================================================
 *  HEADER (stateless)
 * ==========================================================
 */
@Composable
fun TiketHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E4FD8))
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Pemesanan Tiket",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * ==========================================================
 *  INPUT NAMA (stateless)
 *  Hanya menerima value & callback dari parent -> reusable & testable
 * ==========================================================
 */
@Composable
fun NamaInputField(
    nama: String,
    isError: Boolean,
    onNamaChange: (String) -> Unit
) {
    Column {
        Text(text = "Nama", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = nama,
            onValueChange = onNamaChange,
            placeholder = { Text("Masukkan nama Anda") },
            isError = isError,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )
    }
}


@Composable
fun JumlahTiketSelector(
    jumlah: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column {
        Text(text = "Jumlah Tiket", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CounterBox(symbol = "-", onClick = onDecrement)

            Text(
                text = "$jumlah",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            CounterBox(symbol = "+", onClick = onIncrement)
        }
    }
}

@Composable
fun CounterBox(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFE5E7EB))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = symbol, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * ==========================================================
 *  TOMBOL PESAN TIKET (stateless)
 * ==========================================================
 */
@Composable
fun PesanTiketButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1E4FD8),
            disabledContainerColor = Color(0xFFB9C3DA)
        )
    ) {
        Text(text = "Pesan Tiket", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatusCard(status: StatusType) {
    val (bgColor, textColor, label) = when (status) {
        StatusType.IDLE -> Triple(Color(0xFFF3F4F6), Color(0xFF374151), "Silakan pesan tiket")
        StatusType.PROCESSING -> Triple(Color(0xFFEFF6FF), Color(0xFF1D4ED8), "Memproses pesanan.........")
        StatusType.SUCCESS -> Triple(Color(0xFFECFDF3), Color(0xFF15803D), "Tiket berhasil dipesan!")
        StatusType.ERROR -> Triple(Color(0xFFFEF2F2), Color(0xFFB91C1C), "Nama harus diisi")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (status) {
            StatusType.PROCESSING -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = textColor
                )
            }
            StatusType.SUCCESS -> Text("✅", fontSize = 16.sp)
            StatusType.ERROR -> Text("⚠️", fontSize = 16.sp)
            StatusType.IDLE -> Text("ℹ️", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            buildString {
                append("Status: ")
            },
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827),
            fontSize = 14.sp
        )
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


