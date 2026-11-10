package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audio.DubbingAudioEngine
import com.example.data.SampleVideosRepository
import com.example.model.AudioMixMode
import com.example.model.DubbingProject
import com.example.model.EmotionalTone
import com.example.model.ScriptCue
import com.example.model.SpeakerAcousticProfile
import com.example.model.SupportedLanguage
import com.example.model.VoicePersona
import com.example.model.VoiceSettings
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DubbingStudioScreen
import com.example.ui.screens.VoiceLabScreen
import com.example.ui.theme.MyApplicationTheme
import java.util.UUID

sealed interface AppScreen {
    data object Dashboard : AppScreen
    data class Studio(val project: DubbingProject) : AppScreen
    data object VoiceLab : AppScreen
}

class MainActivity : ComponentActivity() {

    private lateinit var audioEngine: DubbingAudioEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        audioEngine = DubbingAudioEngine(this)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VoxDubApp(audioEngine = audioEngine)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.shutdown()
    }
}

@Composable
fun VoxDubApp(audioEngine: DubbingAudioEngine) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Dashboard) }
    var projects by remember { mutableStateOf(SampleVideosRepository.getSampleProjects()) }
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showVideoPickerModal by remember { mutableStateOf(false) }

    when (val screen = currentScreen) {
        is AppScreen.Dashboard -> {
            DashboardScreen(
                projects = projects,
                onSelectProject = { selectedProject ->
                    currentScreen = AppScreen.Studio(selectedProject)
                },
                onOpenVoiceLab = {
                    currentScreen = AppScreen.VoiceLab
                },
                onOpenVideoPicker = {
                    showVideoPickerModal = true
                },
                onNewProject = {
                    showNewProjectDialog = true
                }
            )
        }
        is AppScreen.Studio -> {
            DubbingStudioScreen(
                initialProject = screen.project,
                audioEngine = audioEngine,
                onNavigateBack = {
                    audioEngine.stop()
                    currentScreen = AppScreen.Dashboard
                }
            )
        }
        is AppScreen.VoiceLab -> {
            VoiceLabScreen(
                audioEngine = audioEngine,
                onNavigateBack = {
                    audioEngine.stop()
                    currentScreen = AppScreen.Dashboard
                }
            )
        }
    }

    // Video File Picker Interface
    if (showVideoPickerModal) {
        com.example.ui.components.VideoFilePickerModal(
            onDismissRequest = { showVideoPickerModal = false },
            onProjectCreated = { newProject ->
                projects = listOf(newProject) + projects
                showVideoPickerModal = false
                currentScreen = AppScreen.Studio(newProject)
                Toast.makeText(
                    context,
                    "Imported \"${newProject.title}\" ready for multi-language dubbing!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // New Project Dialog
    if (showNewProjectDialog) {
        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Start New Video Dubbing")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Choose how you'd like to import and dub your video:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Card(
                        onClick = {
                            showNewProjectDialog = false
                            showVideoPickerModal = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_custom_video_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Import Device Video",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Auto-transcribe & dub any video file",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Card(
                        onClick = {
                            val newProject = SampleVideosRepository.getSampleProjects().random().copy(
                                id = "sample_${UUID.randomUUID()}",
                                title = "Creative Masterclass Dub"
                            )
                            projects = listOf(newProject) + projects
                            showNewProjectDialog = false
                            currentScreen = AppScreen.Studio(newProject)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("load_sample_template_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Load Studio Masterclass",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "High-definition multi-track template",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
