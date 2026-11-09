package com.mobilecomputing.applock

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.withResumed
import kotlinx.coroutines.delay

private enum class Screen { Home, Settings, ChangePin }

/** Hangi ekranın gösterileceğine karar verir: PIN kurulumu → kilit ekranı → içerik. */
@Composable
fun AppRoot(store: PinStore, biometric: BiometricHelper) {
    var hasPin by remember { mutableStateOf(store.hasPin) }
    var screen by rememberSaveable { mutableStateOf(Screen.Home) }

    when {
        !hasPin -> PinSetupScreen(
            title = "Hoş geldiniz",
            onPinSet = { pin ->
                store.setPin(pin)
                hasPin = true
                AppLock.unlock()
            },
        )

        AppLock.isLocked -> LockScreen(store, biometric, onUnlocked = { AppLock.unlock() })

        else -> when (screen) {
            Screen.Home -> HomeScreen(
                store = store,
                onOpenSettings = { screen = Screen.Settings },
                onLockNow = { AppLock.lock() },
            )

            Screen.Settings -> SettingsScreen(
                store = store,
                biometricAvailable = remember { biometric.isAvailable() },
                onChangePin = { screen = Screen.ChangePin },
                onBack = { screen = Screen.Home },
            )

            Screen.ChangePin -> PinSetupScreen(
                title = "PIN'i değiştir",
                verifyCurrent = store::matches,
                onPinSet = { pin ->
                    store.setPin(pin)
                    screen = Screen.Settings
                },
                onCancel = { screen = Screen.Settings },
            )
        }
    }
}

@Composable
fun LockScreen(store: PinStore, biometric: BiometricHelper, onUnlocked: () -> Unit) {
    var message by remember { mutableStateOf<String?>(null) }
    var lockoutUntil by remember { mutableLongStateOf(store.lockoutUntilMs) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val lockedOut = now < lockoutUntil

    val canUseBiometric = remember { store.biometricEnabled && biometric.isAvailable() }
    val askBiometric: () -> Unit = {
        biometric.authenticate(
            onSuccess = {
                store.resetFailures()
                onUnlocked()
            },
            onError = { message = it },
        )
    }

    // Bekleme süresi geri sayımı.
    LaunchedEffect(lockoutUntil) {
        while (System.currentTimeMillis() < lockoutUntil) {
            now = System.currentTimeMillis()
            delay(500)
        }
        now = System.currentTimeMillis()
    }

    // Kilit ekranı açılınca biyometrik diyaloğu otomatik göster
    // (Activity tamamen ön plana geldikten sonra).
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(Unit) {
        if (canUseBiometric && !lockedOut) lifecycle.withResumed { askBiometric() }
    }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🔒", fontSize = 48.sp)
        Spacer(Modifier.height(8.dp))
        Text("Uygulama kilitli", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        val status = when {
            lockedOut -> {
                val seconds = (lockoutUntil - now + 999) / 1000
                "Çok fazla hatalı deneme.\n$seconds sn sonra tekrar deneyin."
            }
            else -> message ?: "PIN'inizi girin"
        }
        Text(
            status,
            textAlign = TextAlign.Center,
            color = if (lockedOut || message != null) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))

        PinPad(
            enabled = !lockedOut,
            extraKey = if (canUseBiometric) "👆" to askBiometric else null,
            onComplete = { pin ->
                when (val result = store.checkPin(pin, System.currentTimeMillis())) {
                    PinCheckResult.Success -> onUnlocked()
                    is PinCheckResult.Wrong ->
                        message = "Hatalı PIN. Kalan deneme hakkı: ${result.attemptsLeft}"
                    is PinCheckResult.LockedOut -> {
                        message = null
                        now = System.currentTimeMillis()
                        lockoutUntil = result.untilMs
                    }
                }
            },
        )
    }
}

private enum class SetupStage { Current, New, Confirm }

/**
 * Yeni PIN oluşturma: (isteğe bağlı mevcut PIN) → yeni PIN → tekrar.
 */
@Composable
fun PinSetupScreen(
    title: String,
    onPinSet: (String) -> Unit,
    verifyCurrent: ((String) -> Boolean)? = null,
    onCancel: (() -> Unit)? = null,
) {
    var stage by remember { mutableStateOf(if (verifyCurrent != null) SetupStage.Current else SetupStage.New) }
    var firstPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    if (onCancel != null) BackHandler(onBack = onCancel)

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            when (stage) {
                SetupStage.Current -> "Mevcut PIN'inizi girin"
                SetupStage.New -> "$PIN_LENGTH haneli yeni bir PIN belirleyin"
                SetupStage.Confirm -> "Onaylamak için PIN'i tekrar girin"
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(error ?: " ", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))

        PinPad(onComplete = { pin ->
            when (stage) {
                SetupStage.Current ->
                    if (verifyCurrent?.invoke(pin) == true) {
                        error = null
                        stage = SetupStage.New
                    } else {
                        error = "Mevcut PIN hatalı"
                    }

                SetupStage.New ->
                    if (PinRules.isTooSimple(pin)) {
                        error = "Çok kolay bir PIN (1111, 1234 gibi). Başka bir PIN seçin."
                    } else {
                        error = null
                        firstPin = pin
                        stage = SetupStage.Confirm
                    }

                SetupStage.Confirm ->
                    if (pin == firstPin) {
                        onPinSet(pin)
                    } else {
                        error = "PIN'ler eşleşmedi, baştan deneyin."
                        firstPin = ""
                        stage = SetupStage.New
                    }
            }
        })

        if (onCancel != null) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onCancel) { Text("Vazgeç") }
        }
    }
}

/** Kilidin koruduğu örnek içerik: gizli bir not. */
@Composable
fun HomeScreen(store: PinStore, onOpenSettings: () -> Unit, onLockNow: () -> Unit) {
    var note by remember { mutableStateOf(store.secretNote) }

    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)) {
        Text("🔓 Gizli Kasa", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Bu not yalnızca kilit açıkken görünür. Uygulamadan çıkınca " +
                "(${store.lockTimeout.label.lowercase()}) yeniden kilitlenir.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = note,
            onValueChange = {
                note = it
                store.secretNote = it
            },
            label = { Text("Gizli notum") },
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onLockNow, modifier = Modifier.weight(1f)) { Text("Şimdi kilitle") }
            OutlinedButton(onClick = onOpenSettings, modifier = Modifier.weight(1f)) { Text("Ayarlar") }
        }
    }
}

@Composable
fun SettingsScreen(
    store: PinStore,
    biometricAvailable: Boolean,
    onChangePin: () -> Unit,
    onBack: () -> Unit,
) {
    var biometricOn by remember { mutableStateOf(store.biometricEnabled) }
    var timeout by remember { mutableStateOf(store.lockTimeout) }

    BackHandler(onBack = onBack)

    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Geri") }
            Text("Ayarlar", style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Parmak izi / yüz tanıma", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (biometricAvailable) "Kilidi biyometrik doğrulamayla aç"
                    else "Bu cihazda kayıtlı parmak izi veya yüz yok",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = biometricOn && biometricAvailable,
                enabled = biometricAvailable,
                onCheckedChange = {
                    biometricOn = it
                    store.biometricEnabled = it
                },
            )
        }

        HorizontalDivider(Modifier.padding(vertical = 16.dp))

        Text("Otomatik kilitleme", style = MaterialTheme.typography.titleMedium)
        Text(
            "Uygulama arka plana alındıktan ne kadar sonra kilitlensin?",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LockTimeout.entries.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = timeout == option, onClick = {
                        timeout = option
                        store.lockTimeout = option
                    }),
            ) {
                RadioButton(selected = timeout == option, onClick = null)
                Text(option.label, Modifier.padding(start = 8.dp, top = 12.dp, bottom = 12.dp))
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 16.dp))

        OutlinedButton(onClick = onChangePin, modifier = Modifier.fillMaxWidth()) {
            Text("PIN'i değiştir")
        }
    }
}
