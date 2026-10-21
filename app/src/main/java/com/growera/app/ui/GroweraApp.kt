package com.growera.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.growera.app.data.ApiClient
import com.growera.app.data.ApiException
import com.growera.app.data.SessionStore
import kotlinx.coroutines.launch
import org.json.JSONObject

private enum class Page(val label: String) { HOME("Home"), PROFILE("Profile"), REWARDS("Rewards"), STORE("Store"), MORE("More") }

@Composable
fun GroweraApp(sessionStore: SessionStore, apiClient: ApiClient) {
    var token by remember { mutableStateOf(sessionStore.token()) }
    var page by remember { mutableStateOf(Page.HOME) }
    var player by remember { mutableStateOf<JSONObject?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun refreshProfile(activeToken: String) {
        scope.launch {
            loading = true
            error = ""
            try {
                player = apiClient.getPlayer(activeToken)
            } catch (e: Exception) {
                error = e.message ?: "Gagal mengambil data pemain."
                if (e is ApiException && e.statusCode == 401) {
                    sessionStore.clear(); token = null; player = null
                }
            } finally { loading = false }
        }
    }

    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) refreshProfile(token!!)
    }

    if (token.isNullOrBlank()) {
        LoginScreen(
            sessionStore = sessionStore,
            apiClient = apiClient,
            onLoggedIn = { newToken -> token = newToken; page = Page.HOME }
        )
    } else {
        Scaffold(
            containerColor = Color(0xFF080D19),
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF0E1626), tonalElevation = 0.dp) {
                    Page.values().forEach { item ->
                        val icon = when (item) {
                            Page.HOME -> Icons.Filled.Home
                            Page.PROFILE -> Icons.Filled.Person
                            Page.REWARDS -> Icons.Filled.EmojiEvents
                            Page.STORE -> Icons.Filled.Diamond
                            Page.MORE -> Icons.Filled.GridView
                        }
                        NavigationBarItem(
                            selected = page == item,
                            onClick = { page = item },
                            icon = { Icon(icon, contentDescription = item.label) },
                            label = { Text(item.label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFB7A8FF),
                                selectedTextColor = Color(0xFFB7A8FF),
                                indicatorColor = Color(0xFF292447),
                                unselectedIconColor = Color(0xFF8793AF),
                                unselectedTextColor = Color(0xFF8793AF)
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                Header()
                if (loading && player == null) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (error.isNotBlank()) {
                    Card(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF321B2B))) {
                        Column(Modifier.padding(14.dp)) {
                            Text(error, color = Color(0xFFFFB7C4))
                            TextButton(onClick = { token?.let(::refreshProfile) }) { Text("Coba lagi") }
                        }
                    }
                }
                when (page) {
                    Page.HOME -> HomePage(player, loading, onRefresh = { token?.let(::refreshProfile) }, onOpen = { page = it })
                    Page.PROFILE -> GenericPage("MY PROFILE", "Player Account", "Informasi akun pemainmu dari server GROWERA.", player?.toString(2) ?: "Data profil belum tersedia.")
                    Page.REWARDS -> GenericPage("CRYSTAL REWARDS", "Claim Your Glory", "Milestone • Daily Login • Daily Quest • Weekly Quest • Monthly Quest", "Sistem rewards akan dihubungkan setelah struktur respons API diverifikasi.")
                    Page.STORE -> GenericPage("CRYSTAL STORE", "Store", "Gems • Assets • Items", "Tampilan store native dasar. Data paket dan transaksi belum diaktifkan.")
                    Page.MORE -> GenericPage(
                        "EXPLORE", "More", "Pengaturan akun dan informasi aplikasi.", "GROWERA Crystal Era • Android Native",
                        action = {
                            Button(
                                onClick = {
                                    val oldToken = token
                                    scope.launch { apiClient.logout(oldToken) }
                                    sessionStore.clear(); token = null; player = null; page = Page.HOME
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A1D31), contentColor = Color(0xFFFFA6B9))
                            ) { Icon(Icons.Filled.Logout, null); Spacer(Modifier.width(8.dp)); Text("Logout") }
                        }
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun Header() {
    Row(
        Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0xFF191A3D), Color(0xFF101B2D), Color(0xFF0B1322))))
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(46.dp).background(Brush.linearGradient(listOf(Color(0xFF8D7CFF), Color(0xFF4DDDF6))), RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Diamond, null, tint = Color(0xFF090E1A), modifier = Modifier.size(27.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("GROWERA", fontSize = 21.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Text("CRYSTAL ERA  •  PLAYER PORTAL", fontSize = 9.sp, color = Color(0xFF9DAAC8), letterSpacing = 1.1.sp)
        }
        Text("BETA", color = Color(0xFF77E7F5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LoginScreen(sessionStore: SessionStore, apiClient: ApiClient, onLoggedIn: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF17163A), Color(0xFF080D19), Color(0xFF080D19))))) {
        Column(
            Modifier.fillMaxWidth().align(Alignment.Center).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(82.dp).background(Brush.linearGradient(listOf(Color(0xFFA79AFF), Color(0xFF4BDCF3))), RoundedCornerShape(27.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Diamond, null, tint = Color(0xFF0A1020), modifier = Modifier.size(47.dp))
            }
            Spacer(Modifier.height(18.dp))
            Text("GROWERA", fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
            Text("CRYSTAL ERA  •  PLAYER PORTAL", color = Color(0xFF9DAAC8), fontSize = 10.sp, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(30.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xE6111A2B)), shape = RoundedCornerShape(25.dp)) {
                Column(Modifier.fillMaxWidth().padding(22.dp)) {
                    Text("Welcome back", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp))
                    Text("Masuk untuk melanjutkan perjalananmu.", color = Color(0xFF9DAAC8), fontSize = 13.sp)
                    Spacer(Modifier.height(22.dp))
                    OutlinedTextField(
                        value = username, onValueChange = { username = it; message = "" },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        label = { Text("Username") }, leadingIcon = { Icon(Icons.Filled.Person, null) },
                        shape = RoundedCornerShape(14.dp), colors = fieldColors()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password, onValueChange = { password = it; message = "" },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        label = { Text("Password") }, leadingIcon = { Icon(Icons.Filled.Lock, null) },
                        trailingIcon = { IconButton(onClick = { showPassword = !showPassword }) { Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, "Tampilkan/sembunyikan password") } },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp), colors = fieldColors()
                    )
                    if (message.isNotBlank()) {
                        Spacer(Modifier.height(12.dp)); Text(message, color = Color(0xFFFF9CAC), fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) { message = "Masukkan username dan password."; return@Button }
                            if (!sessionStore.isAvailable()) { message = "Penyimpanan sesi aman tidak tersedia di perangkat ini."; return@Button }
                            scope.launch {
                                busy = true; message = ""
                                try {
                                    val newToken = apiClient.login(username.trim(), password)
                                    sessionStore.saveToken(newToken)
                                    if (sessionStore.token() != newToken) throw IllegalStateException("Token tidak dapat disimpan dengan aman.")
                                    onLoggedIn(newToken)
                                } catch (e: Exception) {
                                    message = e.message ?: "Login gagal. Periksa koneksi dan coba lagi."
                                } finally { busy = false }
                            }
                        }, modifier = Modifier.fillMaxWidth().height(52.dp), enabled = !busy,
                        shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B8CFF), contentColor = Color(0xFF100C24))
                    ) {
                        if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF100C24))
                        else { Text("SIGN IN", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.4.sp); Spacer(Modifier.width(8.dp)); Icon(Icons.Filled.ArrowForward, null) }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("SECURE PLAYER ACCESS", color = Color(0xFF687795), fontSize = 10.sp, letterSpacing = 1.5.sp)
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF9B8CFF), unfocusedBorderColor = Color(0xFF35415B),
    focusedLabelColor = Color(0xFFB7A8FF), cursorColor = Color(0xFFB7A8FF)
)

@Composable
private fun HomePage(player: JSONObject?, loading: Boolean, onRefresh: () -> Unit, onOpen: (Page) -> Unit) {
    val data = player?.optJSONObject("data") ?: player
    val username = data?.let { firstValue(it, listOf("username", "name", "playerName")) } ?: "Player"
    val level = data?.let { firstValue(it, listOf("level", "playerLevel")) } ?: "—"
    val gems = data?.let { firstValue(it, listOf("gems", "gemsBalance", "balance")) } ?: "—"
    Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(20.dp))
        Text("PLAYER DASHBOARD", color = Color(0xFF9D91FF), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
        Spacer(Modifier.height(5.dp))
        Text("Welcome to Crystal Era", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
        Text("Your journey, your legacy.", color = Color(0xFF9DAAC8), fontSize = 13.sp)
        Spacer(Modifier.height(18.dp))
        Card(shape = RoundedCornerShape(23.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.background(Brush.linearGradient(listOf(Color(0xFF35316B), Color(0xFF1A2948), Color(0xFF123140)))).padding(20.dp)) {
                Text("PLAYER OVERVIEW", color = Color(0xFFB9C4E7), fontSize = 10.sp, letterSpacing = 1.4.sp)
                Spacer(Modifier.height(13.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(54.dp).background(Color(0xFFB5A9FF), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                        Text(username.take(1).uppercase(), color = Color(0xFF15112F), fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text(username, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Crystal Era Player", color = Color(0xFFB8C4E1), fontSize = 12.sp)
                    }
                    IconButton(onClick = onRefresh, enabled = !loading) { Icon(Icons.Filled.Refresh, "Refresh data", tint = Color.White) }
                }
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("LEVEL", level, Icons.Filled.Star, Modifier.weight(1f))
                    StatTile("GEMS", gems, Icons.Filled.Diamond, Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(23.dp))
        Text("QUICK ACCESS", color = Color(0xFF9D91FF), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.7.sp)
        Spacer(Modifier.height(12.dp))
        val shortcuts = listOf(Triple("My Profile", "Account details", Icons.Filled.Person) to Page.PROFILE, Triple("Crystal Rewards", "Your reward journey", Icons.Filled.EmojiEvents) to Page.REWARDS, Triple("Crystal Store", "Gems & items", Icons.Filled.Diamond) to Page.STORE, Triple("More", "Settings & info", Icons.Filled.GridView) to Page.MORE)
        shortcuts.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                row.forEach { (info, destination) ->
                    Card(onClick = { onOpen(destination) }, modifier = Modifier.weight(1f).padding(bottom = 10.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF111A2B))) {
                        Column(Modifier.fillMaxWidth().padding(15.dp)) {
                            Icon(info.third, null, tint = Color(0xFF9B8CFF), modifier = Modifier.size(25.dp))
                            Spacer(Modifier.height(12.dp)); Text(info.first, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(Modifier.height(3.dp)); Text(info.second, color = Color(0xFF9DAAC8), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        if (player == null && !loading) Text("Data pemain belum dimuat. Tekan refresh atau login kembali jika sesi sudah kedaluwarsa.", color = Color(0xFF9DAAC8), fontSize = 12.sp)
    }
}

@Composable
private fun StatTile(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color(0x333F4B75)), shape = RoundedCornerShape(15.dp)) {
        Column(Modifier.fillMaxWidth().padding(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Color(0xFFB6A9FF), modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text(title, color = Color(0xFFB8C4E1), fontSize = 9.sp, letterSpacing = 1.sp) }
            Spacer(Modifier.height(7.dp)); Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun GenericPage(eyebrow: String, title: String, subtitle: String, body: String, action: (@Composable () -> Unit)? = null) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 22.dp)) {
        Text(eyebrow, color = Color(0xFF9D91FF), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.7.sp)
        Spacer(Modifier.height(6.dp)); Text(title, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(8.dp)); Text(subtitle, color = Color(0xFF9DAAC8), fontSize = 13.sp)
        Spacer(Modifier.height(20.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF111A2B)), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Text("CRYSTAL ERA", color = Color(0xFFB7A8FF), fontSize = 10.sp, letterSpacing = 1.3.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp)); Text(body, color = Color(0xFFD6DDF0), fontSize = 13.sp, lineHeight = 20.sp)
                if (action != null) { Spacer(Modifier.height(18.dp)); action() }
            }
        }
    }
}

private fun firstValue(json: JSONObject, keys: List<String>): String? {
    for (key in keys) {
        val value = json.opt(key)
        if (value != null && value != JSONObject.NULL && value.toString().isNotBlank() && value.toString() != "0") return value.toString()
    }
    return null
}
