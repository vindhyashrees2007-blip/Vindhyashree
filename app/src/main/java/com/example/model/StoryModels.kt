package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class CharacterProfile(
    val name: String = "Leo",
    val heroType: String = "Brave Knight",
    val hairColor: String = "Golden Blonde",
    val clothingColor: String = "Royal Blue Tunic & Silver Shield",
    val companion: String = "a tiny purple dragon named Spark"
) {
    fun toPromptDescription(): String {
        return "Main character named $name, who is a $heroType with $hairColor hair, wearing $clothingColor, accompanied by $companion."
    }
}

@Serializable
data class InteractiveObject(
    val id: String,
    val name: String,
    val emoji: String,
    val soundType: String, // "chime", "sparkle", "animal", "pop", "boing"
    val xPercent: Float,   // 0.0 to 1.0 position on image
    val yPercent: Float
)

@Serializable
data class StoryPage(
    val pageNumber: Int,
    val text: String,
    val illustrationPrompt: String,
    var imageUrl: String? = null,
    val interactiveObjects: List<InteractiveObject> = emptyList()
)

@Serializable
data class StoryBook(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val theme: String,
    val pages: List<StoryPage>,
    val characterProfile: CharacterProfile
)

@Serializable
data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
