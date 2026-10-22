package net.everycord.sample

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import net.everycord.sdk.ChatClient
import net.everycord.sdk.Destination
import net.everycord.sdk.EveryCord
import net.everycord.sdk.EveryCordException
import net.everycord.sdk.EveryCordPlayerView
import net.everycord.sdk.Facecam
import net.everycord.sdk.EveryCordFeature
import net.everycord.sdk.LiveOptions
import net.everycord.sdk.LiveSession
import net.everycord.sdk.ScreenCaptureContract

/**
 * One screen that walks through the SDK: the partner config and its tier, sign-in with a partner userToken, going live
 * (with a destination, co-hosts, facecam and overlays), chat, Sparks and the player. Each part shows only when the
 * partner's tier includes it ([EveryCord.isEnabled]).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    if (EveryCord.isConfigured) SampleScreen()
                    else Text("Set everycord.publishableKey in gradle.properties to your ecpk_test_ key.", Modifier.padding(24.dp))
                }
            }
        }
    }
}

@Composable
private fun SampleScreen() {
    val scope = rememberCoroutineScope()
    val config by EveryCord.configUpdates.collectAsState()
    val user by EveryCord.auth.currentUser.collectAsState()
    val live by EveryCord.live.current.collectAsState()
    var message by remember { mutableStateOf<String?>(null) }
    // Why a stream ended or an output failed, kept after the live panel goes away.
    LaunchedEffect(live) { live?.events?.collect { if (it.isError) message = it.message } }

    fun run(block: suspend () -> Unit) {
        scope.launch {
            message = null
            try { block() } catch (e: EveryCordException) { message = "${e.code}: ${e.message}" } catch (e: Exception) { message = e.message }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("EveryCord SDK ${EveryCord.VERSION}", style = MaterialTheme.typography.titleLarge)
        message?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        // The partner config: who the key belongs to and what the tier includes.
        Section("Partner") {
            val c = config
            if (c == null) {
                Text("Loading the partner config...")
                OutlinedButton(onClick = { run { EveryCord.loadConfig() } }) { Text("Retry") }
            } else {
                Text("${c.partner.name}, ${c.tier.key} tier, ${c.mode.name.lowercase()} mode")
                Text("Enabled: " + EveryCordFeature.entries.filter { c.isEnabled(it) }.joinToString { it.key }, style = MaterialTheme.typography.bodySmall)
            }
        }

        // Accounts: the partner's server makes a single-use userToken (POST /v1/partner/users/:id/token with the secret
        // key) and hands it to the app.
        Section("Account") {
            val u = user
            if (u == null) {
                var token by remember { mutableStateOf("") }
                OutlinedTextField(token, { token = it }, label = { Text("userToken from your server") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(onClick = { run { EveryCord.auth.signIn(token) } }, enabled = token.isNotBlank()) { Text("Sign in") }
            } else {
                Text("Signed in as ${u.displayName.ifBlank { u.username }}")
                OutlinedButton(onClick = { run { EveryCord.auth.signOut() } }) { Text("Sign out") }
            }
        }

        if (user != null && EveryCord.isEnabled(EveryCordFeature.CAPTURE)) {
            val session = live
            if (session == null) GoLive(::run) else LivePanel(session, ::run)
        }

        var watchId by remember { mutableStateOf("") }
        val sessionId = live?.id ?: watchId.trim()
        if (live == null && (EveryCord.isEnabled(EveryCordFeature.CHAT) || EveryCord.isEnabled(EveryCordFeature.SPARKS) || EveryCord.isEnabled(EveryCordFeature.LIVE_DELIVERY))) {
            Section("Watch a session") {
                OutlinedTextField(watchId, { watchId = it }, label = { Text("Live session ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        }
        if (sessionId.isNotEmpty() && EveryCord.isEnabled(EveryCordFeature.CHAT)) Chat(sessionId)
        if (sessionId.isNotEmpty() && user != null && EveryCord.isEnabled(EveryCordFeature.SPARKS)) Sparks(sessionId, ::run)
        if (sessionId.isNotEmpty() && live == null && EveryCord.isEnabled(EveryCordFeature.LIVE_DELIVERY)) Player(sessionId) { message = it }
    }
}

@Composable
private fun GoLive(run: (suspend () -> Unit) -> Unit) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    var cohosts by remember { mutableStateOf(false) }
    var facecam by remember { mutableStateOf(false) }
    var overlays by remember { mutableStateOf(false) }

    fun options() = LiveOptions(
        title = title,
        destinations = if (url.isNotBlank()) listOf(Destination.Rtmp(url.trim(), key.trim())) else emptyList(),
        cohostsEnabled = cohosts,
        facecam = if (facecam) Facecam() else null,
        overlays = overlays,
    )

    // Android's screen capture prompt, then the stream.
    val capture = rememberLauncherForActivityResult(ScreenCaptureContract()) { consent ->
        if (consent != null) run { EveryCord.live.start(options(), consent) }
    }
    // The microphone, camera and notification permissions first (each optional: the stream starts without them).
    val permissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { capture.launch(Unit) }

    Section("Go live") {
        OutlinedTextField(title, { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (EveryCord.isEnabled(EveryCordFeature.MULTISTREAM)) {
            OutlinedTextField(url, { url = it }, label = { Text("RTMP(S) server URL (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(key, { key = it }, label = { Text("Stream key") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        if (EveryCord.isEnabled(EveryCordFeature.COHOSTS)) Toggle("Co-hosts", cohosts) { cohosts = it }
        Toggle("Facecam", facecam) { facecam = it }
        if (EveryCord.isEnabled(EveryCordFeature.OVERLAYS)) Toggle("My EveryCord overlays", overlays) { overlays = it }
        val problems = EveryCord.live.problems(options())
        problems.forEach { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        Button(enabled = problems.isEmpty(), onClick = {
            val wanted = buildList {
                add(Manifest.permission.RECORD_AUDIO)
                if (facecam) add(Manifest.permission.CAMERA)
                if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissions.launch(wanted.toTypedArray())
        }) { Text("Go live") }
    }
}

@Composable
private fun LivePanel(session: LiveSession, run: (suspend () -> Unit) -> Unit) {
    val state by session.state.collectAsState()
    val viewers by session.viewerCount.collectAsState()
    val bitrate by session.bitrate.collectAsState()
    val outputs by session.outputs.collectAsState()
    var event by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(session) { session.events.collect { event = it.message } }

    Section("Live") {
        Text("${state.name.lowercase()}, $viewers watching, $bitrate kbps", fontWeight = FontWeight.SemiBold)
        session.shareUrl?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        outputs.forEach { Text("${it.name}: ${it.state.name.lowercase()}${it.detail?.let { d -> " ($d)" } ?: ""}", style = MaterialTheme.typography.bodySmall) }
        event?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        Button(onClick = { run { session.stop() } }) { Text("Stop") }
    }

    session.cohosts?.let { cohosts ->
        val list by cohosts.list.collectAsState()
        var link by remember { mutableStateOf<String?>(null) }
        Section("Co-hosts") {
            list.forEach { Text("${it.displayName.ifBlank { it.username }}: ${it.status}${if (it.hasVideo) ", on camera" else ""}") }
            link?.let { Text(it) }
            OutlinedButton(onClick = { run { link = cohosts.inviteLink(maxUses = 1) } }) { Text("Make an invite link") }
        }
    }
}

@Composable
private fun Chat(sessionId: String) {
    val chat: ChatClient = remember(sessionId) { EveryCord.chat(sessionId) }
    DisposableEffect(chat) {
        chat.connect()
        onDispose { chat.close() }
    }
    val messages by chat.messages.collectAsState()
    var text by remember { mutableStateOf("") }
    Section("Chat") {
        messages.takeLast(12).forEach { m ->
            // displayText carries "Sparks sent via EveryCord" on gifts when the tier requires it.
            Text("${m.user.name}: ${m.displayText}", style = MaterialTheme.typography.bodySmall)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(text, { text = it }, label = { Text("Message") }, singleLine = true, modifier = Modifier.weight(1f))
            Spacer(Modifier.height(4.dp))
            OutlinedButton(onClick = { if (chat.send(text)) text = "" }) { Text("Send") }
        }
    }
}

@Composable
private fun Sparks(sessionId: String, run: (suspend () -> Unit) -> Unit) {
    var balance by remember { mutableStateOf<Int?>(null) }
    var sent by remember { mutableStateOf<String?>(null) }
    Section("Sparks") {
        balance?.let { Text("Balance: $it Sparks") }
        sent?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { run { balance = EveryCord.sparks.balance().balance } }) { Text("Balance") }
            Button(onClick = {
                run {
                    val result = EveryCord.sparks.send(sessionId, 10, "Great stream!")
                    balance = result.balance
                    sent = listOfNotNull("Sent 10 Sparks.", result.credit).joinToString(" ")
                }
            }) { Text("Send 10") }
        }
    }
}

@Composable
private fun Player(sessionId: String, onError: (String) -> Unit) {
    Section("Player") {
        AndroidView(
            factory = { context -> EveryCordPlayerView(context).apply { errorListener = EveryCordPlayerView.ErrorListener { onError(it.message.orEmpty()) } } },
            // Play once per session ID, not on every recomposition.
            update = { if (it.tag != sessionId) { it.tag = sessionId; it.play(sessionId) } },
            modifier = Modifier.fillMaxWidth().height(220.dp),
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    HorizontalDivider()
    Text(title, style = MaterialTheme.typography.titleMedium)
    content()
}

@Composable
private fun Toggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked, onChange)
        Text(label)
    }
}
