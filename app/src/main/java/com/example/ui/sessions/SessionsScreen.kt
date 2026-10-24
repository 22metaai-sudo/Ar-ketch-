package com.example.ui.sessions

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DrawingProject
import com.example.data.model.TimelapseSession
import com.example.data.repository.DrawingRepository
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldLocked
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.RubyRecording
import com.example.ui.theme.SurfaceElevated
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    repository: DrawingRepository,
    onNavigateBack: () -> Unit,
    onResumeProject: (DrawingProject) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val projects by repository.allProjects.collectAsState(initial = emptyList())
    val sessions by repository.allSessions.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) }
    var activePreviewSession by remember { mutableStateOf<TimelapseSession?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Historique : Projets & Vidéos",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("sessions_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightSurface,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = ObsidianDark
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MidnightSurface,
                contentColor = CyanNeon,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CyanNeon
                    )
                }
            ) {
                Tab(
                    selected = (selectedTab == 0),
                    onClick = { selectedTab = 0 },
                    text = { Text("Projets de Traçage (${projects.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = (selectedTab == 1),
                    onClick = { selectedTab = 1 },
                    text = { Text("Vidéos Time-Lapse (${sessions.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            if (selectedTab == 0) {
                if (projects.isEmpty()) {
                    EmptyHistoryPlaceholder(
                        title = "Aucun Projet Sauvegardé",
                        subtitle = "Chaque fois que vous commencez un tracé, vos réglages (zoom, filtres, position 3D) sont enregistrés automatiquement sans perte."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(projects) { project ->
                            ProjectCard(
                                project = project,
                                onResume = { onResumeProject(project) },
                                onDelete = {
                                    coroutineScope.launch { repository.deleteProject(project) }
                                }
                            )
                        }
                    }
                }
            } else {
                if (sessions.isEmpty()) {
                    EmptyHistoryPlaceholder(
                        title = "Aucune Vidéo Time-Lapse",
                        subtitle = "Appuyez sur [REC Vidéo] dans le Studio AR pour enregistrer réellement le processus de ce que vous dessinez sur la feuille."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sessions) { session ->
                            TimelapseSessionCard(
                                session = session,
                                onPlay = { activePreviewSession = session },
                                onDelete = {
                                    coroutineScope.launch { repository.deleteTimelapseSession(session) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    activePreviewSession?.let { session ->
        TimelapsePlaybackDialog(
            session = session,
            onDismiss = { activePreviewSession = null }
        )
    }
}

@Composable
fun ProjectCard(
    project: DrawingProject,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy • HH:mm", Locale.FRENCH) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onResume() }
            .testTag("project_card_${project.id}"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    project.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    dateFormat.format(Date(project.lastEditedAt)),
                    color = Color(0xFF94A3B8),
                    fontSize = 11.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (project.isLocked) EmeraldLocked.copy(alpha = 0.2f) else CyanNeon.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            if (project.isLocked) "VERROUILLÉ 6DOF" else "EN COURS",
                            color = if (project.isLocked) EmeraldLocked else CyanNeon,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Échelle ${(project.scale * 100).toInt()}% • Opacité ${(project.opacity * 100).toInt()}%",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_project_${project.id}")
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Supprimer",
                    tint = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
fun TimelapseSessionCard(
    session: TimelapseSession,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy • HH:mm", Locale.FRENCH) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .testTag("timelapse_card_${session.id}"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2B1218)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Videocam,
                    contentDescription = null,
                    tint = RubyRecording,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    session.projectTitle,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    dateFormat.format(Date(session.timestamp)),
                    color = Color(0xFF94A3B8),
                    fontSize = 11.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(RubyRecording.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "${session.frameCount} IMAGES",
                            color = RubyRecording,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${session.durationSeconds}s • Intervalle ${session.intervalSeconds}s",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }
            }
            IconButton(
                onClick = onPlay,
                modifier = Modifier.testTag("play_timelapse_${session.id}")
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Lire la vidéo",
                    tint = CyanNeon
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_timelapse_${session.id}")
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Supprimer",
                    tint = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
fun TimelapsePlaybackDialog(
    session: TimelapseSession,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var frames by remember { mutableStateOf<List<File>>(emptyList()) }
    var currentFrameIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }

    LaunchedEffect(session) {
        val folder = if (session.folderPath.isNotEmpty()) File(session.folderPath) else File(context.filesDir, "timelapses")
        val imageFiles = if (folder.isDirectory) {
            folder.listFiles { f -> f.extension.lowercase() == "jpg" }?.toList() ?: emptyList()
        } else emptyList()

        frames = if (imageFiles.isNotEmpty()) {
            imageFiles.sortedBy { it.name }
        } else {
            // Check all timelapses
            val root = File(context.filesDir, "timelapses")
            val dirs = root.listFiles { f -> f.isDirectory } ?: emptyArray()
            dirs.flatMap { dir ->
                dir.listFiles { f -> f.extension.lowercase() == "jpg" }?.toList() ?: emptyList()
            }.sortedBy { it.lastModified() }
        }
    }

    LaunchedEffect(isPlaying, frames) {
        if (frames.isNotEmpty() && isPlaying) {
            while (true) {
                delay(120L)
                currentFrameIndex = (currentFrameIndex + 1) % frames.size
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Vidéo Accélérée du Dessin",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (frames.isNotEmpty()) {
                    val frameFile = frames[currentFrameIndex]
                    val bitmap = remember(frameFile) {
                        BitmapFactory.decodeFile(frameFile.absolutePath)
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Image $currentFrameIndex",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Image ${currentFrameIndex + 1} sur ${frames.size} • Processus réel accéléré",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(SurfaceElevated, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Aucune image enregistrée dans cette session.", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_close_button")
            ) {
                Text("Fermer", color = CyanNeon)
            }
        },
        dismissButton = {
            if (frames.isNotEmpty()) {
                TextButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier.testTag("dialog_toggle_play_button")
                ) {
                    Text(if (isPlaying) "Pause" else "Lecture", color = Color.White)
                }
            }
        },
        containerColor = MidnightSurface
    )
}

@Composable
fun EmptyHistoryPlaceholder(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Videocam,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                title,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 17.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}
