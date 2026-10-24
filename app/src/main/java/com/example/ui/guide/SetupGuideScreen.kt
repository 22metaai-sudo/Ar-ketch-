package com.example.ui.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldLocked
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SurfaceElevated

data class GuideStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val tip: String,
    val icon: ImageVector,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupGuideScreen(
    onNavigateBack: () -> Unit,
    onStartTracing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        GuideStep(
            stepNumber = 1,
            title = "Installation Physique (L'Astuce de la Tasse / Mug)",
            description = "Posez une tasse ou un verre lourd à côté de votre feuille de dessin. Placez votre téléphone à l'horizontale, la caméra orientée vers le papier.",
            tip = "Activez le mode Grand Angle (0.5x) dans l'application pour cadrer toute la feuille même à faible hauteur !",
            icon = Icons.Default.Coffee,
            accentColor = CyanNeon
        ),
        GuideStep(
            stepNumber = 2,
            title = "Étalonnage & Niveau à Bulle Numérique",
            description = "Regardez la cible gyroscopique en haut à droite. Lorsque la bulle se centre et devient verte émeraude, votre objectif est parfaitement parallèle à la feuille.",
            tip = "Élimine toute déformation de perspective (anamorphose).",
            icon = Icons.Default.GridView,
            accentColor = CyanNeon
        ),
        GuideStep(
            stepNumber = 3,
            title = "Mise au Point Tactile (Tap-to-Focus) & Torche",
            description = "Touchez l'écran à l'endroit où se trouve la pointe de votre crayon pour faire une mise au point ultra-nette. Allumez la torche flash pour supprimer l'ombre de votre main.",
            tip = "Le focus continu garantit une lisibilité chirurgicale de la mine de plomb.",
            icon = Icons.Default.FlashOn,
            accentColor = Color(0xFFF59E0B)
        ),
        GuideStep(
            stepNumber = 4,
            title = "VERROUILLAGE [LOCK CONFIRMATION 6DOF]",
            description = "Ajustez la taille et la rotation du modèle, puis appuyez sur [🔒 LOCK CONFIRMATION]. Les gestes tactiles sont immédiatement figés pour éviter tout faux mouvement en dessinant.",
            tip = "L'ancrage spatial 6DOF compense les micro-vibrations de votre bureau.",
            icon = Icons.Default.Lock,
            accentColor = EmeraldLocked
        ),
        GuideStep(
            stepNumber = 5,
            title = "Loupe Macro & Zoom Écran (Quand Verrouillé)",
            description = "Une fois l'image verrouillée, utilisez l'outil Loupe ou zoomez sur l'écran avec deux doigts. Vous pouvez inspecter les moindres détails sans jamais décaler le modèle !",
            tip = "Parfait pour les cils, les pupilles et les textures hyper-réalistes.",
            icon = Icons.Default.Search,
            accentColor = Color(0xFFA855F7)
        ),
        GuideStep(
            stepNumber = 6,
            title = "Enregistrement Vidéo du Processus Réel (Time-Lapse)",
            description = "Appuyez sur [REC Vidéo] pour enregistrer une image toutes les 2 secondes pendant que vous tracez. À la fin, rejouez votre création accélérée prête pour TikTok / Reels !",
            tip = "La sauvegarde automatique garde tout en mémoire même si vous quittez l'app.",
            icon = Icons.Default.Videocam,
            accentColor = Color(0xFFEF4444)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Guide d'Installation & Étalonnage",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("guide_back_button")
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
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldLocked,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Conseils de Pro — Rich'Art Dely",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "Guide officiel de traçage assisté par réalité augmentée",
                                    color = CyanNeon,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Tracer sur papier en regardant l'écran de son téléphone offre une précision incomparable. Suivez cette méthode en 6 étapes pour réussir vos dessins sans tremblement ni décalage.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            items(steps) { step ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(step.accentColor.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = step.icon,
                                contentDescription = null,
                                tint = step.accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    step.title,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontSize = 14.5.sp
                                )
                                Text(
                                    "0${step.stepNumber}",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = step.accentColor,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                step.description,
                                color = Color(0xFF94A3B8),
                                fontSize = 12.5.sp,
                                lineHeight = 17.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceElevated, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "💡 Astuce : ${step.tip}",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onStartTracing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("guide_launch_studio_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanNeon,
                        contentColor = ObsidianDark
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Ouvrir le Studio AR Trace",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
