package com.example.ui.drawer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldLocked
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SurfaceElevated

@Composable
fun SettingsDrawerContent(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var autoSaveEnabled by remember { mutableStateOf(true) }
    var hapticsEnabled by remember { mutableStateOf(true) }

    val hasCameraPerm = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(MidnightSurface)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Author Profile Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0F2B48), Color(0xFF132035))
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(CyanNeon, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "RD",
                            color = ObsidianDark,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            "AR Trace Studio",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            "Créé par Rich'Art Dely",
                            color = CyanNeon,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            "Version 2.0 Pro • Moteur 6DOF",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            "NAVIGATION PRINCIPALE",
            color = Color(0xFF64748B),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        DrawerNavigationItem(
            icon = Icons.Default.Collections,
            label = "Galerie (500+ Modèles)",
            isSelected = currentRoute == "templates",
            onClick = {
                onNavigate("templates")
                onCloseDrawer()
            }
        )

        DrawerNavigationItem(
            icon = Icons.Default.ViewInAr,
            label = "Studio AR Trace",
            isSelected = currentRoute == "ar_studio",
            onClick = {
                onNavigate("ar_studio")
                onCloseDrawer()
            }
        )

        DrawerNavigationItem(
            icon = Icons.Default.MenuBook,
            label = "Apprendre Pas à Pas (Leçons)",
            isSelected = currentRoute == "tutorials",
            onClick = {
                onNavigate("tutorials")
                onCloseDrawer()
            }
        )

        DrawerNavigationItem(
            icon = Icons.Default.AutoAwesome,
            label = "Feed Inspiration & Conseils",
            isSelected = currentRoute == "feed",
            onClick = {
                onNavigate("feed")
                onCloseDrawer()
            }
        )

        DrawerNavigationItem(
            icon = Icons.Default.History,
            label = "Mes Projets & Timelapses",
            isSelected = currentRoute == "sessions",
            onClick = {
                onNavigate("sessions")
                onCloseDrawer()
            }
        )

        DrawerNavigationItem(
            icon = Icons.Default.Coffee,
            label = "Guide : La Tasse & Trépied",
            isSelected = currentRoute == "guide",
            onClick = {
                onNavigate("guide")
                onCloseDrawer()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(16.dp))

        // System Settings & Database
        Text(
            "PARAMÈTRES & SYSTÈME",
            color = Color(0xFF64748B),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Auto-Save Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Sauvegarde Automatique", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                Text("Enregistre chaque coup de crayon & position 3D", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
            Switch(
                checked = autoSaveEnabled,
                onCheckedChange = { autoSaveEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ObsidianDark,
                    checkedTrackColor = CyanNeon
                )
            )
        }

        // Unlimited DB Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Storage, contentDescription = null, tint = EmeraldLocked, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("Base de Données Locale", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Stockage Room Illimité & Hors-ligne actif", color = EmeraldLocked, fontSize = 11.sp)
            }
        }

        // Permissions Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = if (hasCameraPerm) EmeraldLocked else Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("Autorisations Système", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    if (hasCameraPerm) "Caméra & Torche autorisées" else "Caméra en attente d'approbation",
                    color = if (hasCameraPerm) Color(0xFF94A3B8) else Color(0xFFF59E0B),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Footer Rich'Art Dely
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceElevated, RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Column {
                Text("Signature Auteur :", color = CyanNeon, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "Développé et optimisé pour les créateurs d'art par Rich'Art Dely.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DrawerNavigationItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) SurfaceElevated else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) CyanNeon else Color(0xFF94A3B8),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            color = if (isSelected) CyanNeon else Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.5.sp
        )
    }
}
