package com.example.data.model

enum class FilterMode(val displayName: String, val description: String) {
    NORMAL("Original", "Couleurs naturelles et contrastes réels"),
    SOBEL_EDGES("Contours Sobel", "Croquis net au trait de crayon noir"),
    NEGATIVE_INVERTED("Négatif Luminescent", "Contours blancs lumineux pour papier sombre"),
    BINARIZED("Encre de Chine", "Seuil de contraste net sans nuances"),
    GRAYSCALE("Noir & Blanc Pro", "Monochrome classique haute dynamique"),
    BELLE_VUE("Belle Vue / Hyper-Clarté", "Rehausse les contrastes et la netteté pour un traçage facile"),
    SKETCH_PENCIL("Esquisse Graphite", "Effet mine de plomb douce et estompe")
}

data class TraceTemplate(
    val id: String,
    val title: String,
    val category: String,
    val drawableResName: String,
    val imageUrl: String = "",
    val difficulty: String = "Tous niveaux",
    val description: String = "",
    val tags: List<String> = emptyList()
)

data class AnchorPose6DOF(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val qx: Float = 0f,
    val qy: Float = 0f,
    val qz: Float = 0f,
    val qw: Float = 1f,
    val scale: Float = 1f,
    val rotationDegrees: Float = 0f,
    val isLocked: Boolean = false
)

data class TutorialStep(
    val stepIndex: Int,
    val stepTitle: String,
    val instruction: String,
    val overlayDrawableResName: String,
    val imageUrl: String = "",
    val tip: String
)

data class StepByStepTutorial(
    val id: String,
    val title: String,
    val category: String,
    val difficulty: String,
    val estimatedMinutes: Int,
    val steps: List<TutorialStep>
)

data class InspirationPost(
    val id: String,
    val title: String,
    val artistName: String,
    val category: String,
    val drawableResName: String,
    val imageUrl: String = "",
    val advice: String,
    val challengePrompt: String,
    val likesCount: Int = 142
)
