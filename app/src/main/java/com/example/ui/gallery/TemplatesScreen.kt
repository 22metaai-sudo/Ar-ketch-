package com.example.ui.gallery

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DrawingProject
import com.example.data.model.TraceTemplate
import com.example.data.repository.DrawingRepository
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldLocked
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SurfaceElevated
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    repository: DrawingRepository,
    onSelectTemplate: (imageSource: String, title: String) -> Unit,
    onResumeProject: (DrawingProject) -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToGuide: () -> Unit,
    onNavigateToSessions: () -> Unit,
    onNavigateToTutorials: () -> Unit,
    onNavigateToFeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tous (500+)") }
    val categories = remember { DrawingRepository.getCategories() }
    val allTemplates = remember { DrawingRepository.getCuratedTemplates() }

    // Check for last auto-saved project
    var lastSavedProject by remember { mutableStateOf<DrawingProject?>(null) }
    LaunchedEffect(Unit) {
        lastSavedProject = repository.getLatestProject()
    }

    val filteredTemplates = remember(selectedCategory, searchQuery) {
        allTemplates.filter { template ->
            val matchesCategory = (selectedCategory == "Tous (500+)") || (template.category == selectedCategory)
            val matchesSearch = searchQuery.isBlank() ||
                template.title.contains(searchQuery, ignoreCase = true) ||
                template.category.contains(searchQuery, ignoreCase = true) ||
                template.tags.any { it.contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesSearch
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onSelectTemplate(uri.toString(), "Photo Personnalisée Importée")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanNeon),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Brush,
                                contentDescription = null,
                                tint = ObsidianDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "AR Trace Studio",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "Par Rich'Art Dely",
                                color = CyanNeon,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("nav_drawer_open_button")
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu Principal", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToTutorials) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Tutos Pas à Pas", tint = Color.White)
                    }
                    IconButton(onClick = onNavigateToFeed) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Feed Inspiration", tint = CyanNeon)
                    }
                    IconButton(onClick = onNavigateToSessions) {
                        Icon(Icons.Default.History, contentDescription = "Historique & Vidéos", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightSurface,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                containerColor = CyanNeon,
                contentColor = ObsidianDark,
                modifier = Modifier.testTag("import_custom_image_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Importer Image")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Importer Photo", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = ObsidianDark
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Auto-Save Resume Banner (if a previous project exists)
            lastSavedProject?.let { project ->
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clickable { onResumeProject(project) }
                            .testTag("resume_last_project_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF064E3B), Color(0xFF0F2B48))
                                    )
                                )
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(EmeraldLocked, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, tint = ObsidianDark)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "✨ REPRENDRE LE DERNIER TRACÉ",
                                    color = EmeraldLocked,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                                Text(
                                    project.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1
                                )
                                Text(
                                    "Position 3D, filtre et zoom restaurés automatiquement.",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.5.sp
                                )
                            }
                            Button(
                                onClick = { onResumeProject(project) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldLocked),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Continuer", color = ObsidianDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Search Bar for 500+ Models
            item(span = { GridItemSpan(2) }) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .testTag("models_search_field"),
                    placeholder = {
                        Text("Rechercher parmi 500+ modèles (oeil, loup, manga, fleur...)", color = Color(0xFF64748B), fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = CyanNeon)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Effacer", tint = Color.White)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MidnightSurface,
                        unfocusedContainerColor = MidnightSurface,
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = Color(0xFF1E293B),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            // Categories horizontal filter chips
            item(span = { GridItemSpan(2) }) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = (selectedCategory == category),
                            onClick = { selectedCategory = category },
                            label = { Text(category, fontSize = 12.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanNeon,
                                selectedLabelColor = ObsidianDark,
                                containerColor = SurfaceElevated,
                                labelColor = Color.White
                            ),
                            border = null
                        )
                    }
                }
            }

            // Results count header
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (selectedCategory == "Tous (500+)") "500+ Références d'Artistes" else selectedCategory,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        "${filteredTemplates.size} disponibles",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }

            // 500+ Grid Item Cards
            items(filteredTemplates) { template ->
                TemplateCard(
                    template = template,
                    onSelect = {
                        val source = if (template.imageUrl.isNotEmpty()) template.imageUrl else "res:${template.drawableResName}"
                        onSelectTemplate(source, template.title)
                    }
                )
            }
        }
    }
}

@Composable
fun TemplateCard(
    template: TraceTemplate,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resId = remember(template.drawableResName) {
        if (template.drawableResName.isNotEmpty()) {
            context.resources.getIdentifier(template.drawableResName, "drawable", context.packageName)
        } else 0
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("template_card_${template.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.0f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(Color(0xFF1E293B))
            ) {
                if (template.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(template.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = template.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = template.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Difficulty badge
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                        .background(Color(0xCC000000), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        template.difficulty,
                        color = CyanNeon,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xDD090D16))
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(EmeraldLocked, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Tracer",
                            tint = ObsidianDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Tracer",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    template.title,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    template.category,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }
    }
}
