package com.example.viewmodel

import android.app.Application
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.model.CharacterProfile
import com.example.model.ChatMessage
import com.example.model.InteractiveObject
import com.example.model.StoryBook
import com.example.model.StoryPage
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.GenerationConfig
import com.example.network.GeminiClient
import com.example.network.ImageConfig
import com.example.network.Part
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

class StoryViewModel(application: Application) : AndroidViewModel(application) {

    var characterProfile by mutableStateOf(CharacterProfile())
        private set

    var currentStory by mutableStateOf<StoryBook?>(null)
        private set

    var imageSize by mutableStateOf("1K") // "1K", "2K", "4K"
        private set

    var isGenerating by mutableStateOf(false)
        private set

    var generationStatus by mutableStateOf("")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var chatMessages by mutableStateOf<List<ChatMessage>>(
        listOf(
            ChatMessage(role = "model", text = "Hello little explorer! I'm Sparkle the Storyteller ✨. Ready for a magical adventure?")
        )
    )
        private set

    var isChatting by mutableStateOf(false)
        private set

    val presetCharacters = listOf(
        CharacterProfile("Leo", "Brave Knight", "Golden Blonde", "Royal Blue Tunic & Silver Shield", "a tiny purple dragon named Spark"),
        CharacterProfile("Mia", "Space Explorer", "Bright Purple", "Silver Spacesuit with Glowing Stars", "a friendly robot puppy named Beep"),
        CharacterProfile("Sam", "Magic Wizard", "Fiery Red", "Starry Midnight Blue Robe", "a wise old owl named Professor Hoot"),
        CharacterProfile("Lily", "Forest Fairy", "Emerald Green", "Leaf Crown and Butterfly Wings", "a bouncy bunny named Clover")
    )

    fun updateCharacter(profile: CharacterProfile) {
        characterProfile = profile
    }

    fun updateImageSize(size: String) {
        imageSize = size
    }

    fun generateNewStory(theme: String) {
        viewModelScope.launch {
            isGenerating = true
            generationStatus = "Crafting magical story pages..."
            errorMessage = null

            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    errorMessage = "Gemini API Key is missing! Please configure it in the Secrets panel."
                    isGenerating = false
                    return@launch
                }

                val prompt = """
                    Create a delightful 3-page children's bedtime story about: "$theme".
                    The main hero is ${characterProfile.toPromptDescription()}.
                    Return a JSON object with:
                    - title: String
                    - pages: Array of 3 objects, each having:
                      - pageNumber: Int (1, 2, or 3)
                      - text: String (engaging, warm storytelling text for kids, 3-4 sentences)
                      - illustrationPrompt: String (vibrant, colorful, whimsical storybook illustration description featuring ${characterProfile.toPromptDescription()})
                      - interactiveObjects: Array of 2 objects, each having:
                        - id: String
                        - name: String (e.g. "Sparkling Star", "Magic Wand", "Butterfly")
                        - emoji: String (e.g. "⭐", "🪄", "🦋")
                        - soundType: String ("chime", "sparkle", "animal", "pop", "boing")
                        - xPercent: Float (0.2 to 0.8)
                        - yPercent: Float (0.2 to 0.8)
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json"
                    )
                )

                val response = withContext(Dispatchers.IO) {
                    GeminiClient.api.generateStory(apiKey, request)
                }

                val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (jsonText != null) {
                    val json = Json { ignoreUnknownKeys = true; isLenient = true }
                    val storyBookDto = json.decodeFromString<StoryBookDto>(jsonText)

                    val pages = storyBookDto.pages.map { p ->
                        StoryPage(
                            pageNumber = p.pageNumber,
                            text = p.text,
                            illustrationPrompt = p.illustrationPrompt,
                            interactiveObjects = p.interactiveObjects
                        )
                    }

                    val book = StoryBook(
                        title = storyBookDto.title,
                        theme = theme,
                        pages = pages,
                        characterProfile = characterProfile
                    )
                    currentStory = book

                    // Now generate illustrations for each page
                    generateAllIllustrations(book)
                } else {
                    errorMessage = "Failed to generate story content."
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
            } finally {
                isGenerating = false
            }
        }
    }

    private suspend fun generateAllIllustrations(book: StoryBook) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val updatedPages = book.pages.mapIndexed { index, page ->
            generationStatus = "Painting illustration for page ${index + 1} of ${book.pages.size} (${imageSize})..."
            try {
                val imagePrompt = "Children's storybook illustration style, highly detailed, vibrant colors, magical lighting. ${page.illustrationPrompt}"
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = imagePrompt)))),
                    generationConfig = GenerationConfig(
                        responseModalities = listOf("TEXT", "IMAGE"),
                        imageConfig = ImageConfig(aspectRatio = "1:1", imageSize = imageSize)
                    )
                )
                val resp = withContext(Dispatchers.IO) {
                    GeminiClient.api.generateIllustration(apiKey, request)
                }
                // Find image part in response
                val parts = resp.candidates?.firstOrNull()?.content?.parts
                val imagePart = parts?.firstOrNull { it.inlineData != null }
                if (imagePart?.inlineData != null) {
                    val mime = imagePart.inlineData.mimeType
                    val base64Data = imagePart.inlineData.data
                    page.copy(imageUrl = "data:$mime;base64,$base64Data")
                } else {
                    page
                }
            } catch (e: Exception) {
                page
            }
        }

        currentStory = book.copy(pages = updatedPages)
    }

    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val userMsg = ChatMessage(role = "user", text = userText)
        chatMessages = chatMessages + userMsg

        viewModelScope.launch {
            isChatting = true
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    chatMessages = chatMessages + ChatMessage(role = "model", text = "Please configure your Gemini API Key in the Secrets panel to chat with Sparkle!")
                    isChatting = false
                    return@launch
                }

                val history = chatMessages.map { Content(role = if (it.role == "user") "user" else "model", parts = listOf(Part(text = it.text))) }

                val request = GenerateContentRequest(
                    contents = history,
                    systemInstruction = Content(parts = listOf(Part(text = "You are Sparkle the Storyteller, a warm, magical, and friendly bedtime story guide for children. Keep answers short, cheerful, and imaginative.")))
                )

                val response = withContext(Dispatchers.IO) {
                    GeminiClient.api.chatWithStoryBuddy(apiKey, request)
                }

                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Let's imagine something wonderful together!"
                chatMessages = chatMessages + ChatMessage(role = "model", text = replyText)
            } catch (e: Exception) {
                chatMessages = chatMessages + ChatMessage(role = "model", text = "Oops! My magic wand flickered: ${e.localizedMessage}")
            } finally {
                isChatting = false
            }
        }
    }
}

@kotlinx.serialization.Serializable
data class StoryBookDto(
    val title: String,
    val pages: List<StoryPageDto>
)

@kotlinx.serialization.Serializable
data class StoryPageDto(
    val pageNumber: Int,
    val text: String,
    val illustrationPrompt: String,
    val interactiveObjects: List<InteractiveObject> = emptyList()
)
