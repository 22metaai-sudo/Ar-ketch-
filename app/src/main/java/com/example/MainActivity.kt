package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.local.AppDatabase
import com.example.data.model.DrawingProject
import com.example.data.repository.DrawingRepository
import com.example.ui.ar.ARTraceScreen
import com.example.ui.drawer.SettingsDrawerContent
import com.example.ui.feed.FeedInspirationScreen
import com.example.ui.gallery.TemplatesScreen
import com.example.ui.guide.SetupGuideScreen
import com.example.ui.sessions.SessionsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.tutorials.TutorialsScreen
import kotlinx.coroutines.launch

sealed class Screen(val routeName: String) {
    object Templates : Screen("templates")
    data class ARStudio(
        val imageSource: String,
        val title: String,
        val existingProject: DrawingProject? = null
    ) : Screen("ar_studio")
    object Tutorials : Screen("tutorials")
    object Feed : Screen("feed")
    object Sessions : Screen("sessions")
    object Guide : Screen("guide")
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = DrawingRepository(database.drawingDao())

        setContent {
            MyApplicationTheme {
                MainAppNavHost(
                    repository = repository,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun MainAppNavHost(
    repository: DrawingRepository,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Templates) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SettingsDrawerContent(
                currentRoute = currentScreen.routeName,
                onNavigate = { route ->
                    when (route) {
                        "templates" -> currentScreen = Screen.Templates
                        "ar_studio" -> currentScreen = Screen.ARStudio("res:template_anime_hero", "Héros Shōnen Manga")
                        "tutorials" -> currentScreen = Screen.Tutorials
                        "feed" -> currentScreen = Screen.Feed
                        "sessions" -> currentScreen = Screen.Sessions
                        "guide" -> currentScreen = Screen.Guide
                    }
                },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        when (val screen = currentScreen) {
            is Screen.Templates -> {
                TemplatesScreen(
                    repository = repository,
                    onSelectTemplate = { imageSource, title ->
                        currentScreen = Screen.ARStudio(imageSource, title)
                    },
                    onResumeProject = { project: DrawingProject ->
                        currentScreen = Screen.ARStudio(project.imageSource, project.title, project)
                    },
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    },
                    onNavigateToGuide = {
                        currentScreen = Screen.Guide
                    },
                    onNavigateToSessions = {
                        currentScreen = Screen.Sessions
                    },
                    onNavigateToTutorials = {
                        currentScreen = Screen.Tutorials
                    },
                    onNavigateToFeed = {
                        currentScreen = Screen.Feed
                    },
                    modifier = modifier
                )
            }
            is Screen.ARStudio -> {
                ARTraceScreen(
                    imageSource = screen.imageSource,
                    initialTitle = screen.title,
                    repository = repository,
                    existingProject = screen.existingProject,
                    onNavigateBack = {
                        currentScreen = Screen.Templates
                    },
                    onNavigateToGuide = {
                        currentScreen = Screen.Guide
                    },
                    modifier = modifier
                )
            }
            is Screen.Tutorials -> {
                TutorialsScreen(
                    onNavigateBack = {
                        currentScreen = Screen.Templates
                    },
                    onStartTutorialTrace = { drawableRes, title ->
                        currentScreen = Screen.ARStudio("res:$drawableRes", title)
                    },
                    modifier = modifier
                )
            }
            is Screen.Feed -> {
                FeedInspirationScreen(
                    onNavigateBack = {
                        currentScreen = Screen.Templates
                    },
                    onTraceInspiration = { drawableRes, title ->
                        currentScreen = Screen.ARStudio("res:$drawableRes", title)
                    },
                    modifier = modifier
                )
            }
            is Screen.Guide -> {
                SetupGuideScreen(
                    onNavigateBack = {
                        currentScreen = Screen.Templates
                    },
                    onStartTracing = {
                        currentScreen = Screen.ARStudio("res:template_anime_hero", "Héros Shōnen Manga")
                    },
                    modifier = modifier
                )
            }
            is Screen.Sessions -> {
                SessionsScreen(
                    repository = repository,
                    onNavigateBack = {
                        currentScreen = Screen.Templates
                    },
                    onResumeProject = { project: DrawingProject ->
                        currentScreen = Screen.ARStudio(project.imageSource, project.title, project)
                    },
                    modifier = modifier
                )
            }
        }
    }
}
