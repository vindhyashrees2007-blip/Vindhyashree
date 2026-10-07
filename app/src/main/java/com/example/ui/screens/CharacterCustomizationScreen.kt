package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CharacterProfile
import com.example.viewmodel.StoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCustomizationScreen(
    viewModel: StoryViewModel,
    onBack: () -> Unit
) {
    val current = viewModel.characterProfile
    var name by remember { mutableStateOf(current.name) }
    var heroType by remember { mutableStateOf(current.heroType) }
    var hairColor by remember { mutableStateOf(current.hairColor) }
    var clothingColor by remember { mutableStateOf(current.clothingColor) }
    var companion by remember { mutableStateOf(current.companion) }

    val hairColors = listOf("Golden Blonde", "Fiery Red", "Dark Brown", "Midnight Black", "Neon Purple", "Ocean Blue")
    val heroTypes = listOf("Brave Knight", "Space Explorer", "Magic Wizard", "Forest Fairy", "Super Superhero", "Clever Pirate")
    val companions = listOf("a tiny purple dragon named Spark", "a friendly robot puppy named Beep", "a wise old owl named Hoot", "a bouncy bunny named Clover", "a magical star sprite named Twinkle")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFEDE9FE), Color(0xFFFCE7F3), Color(0xFFFEF3C7))
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Customize Your Hero 🦸", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Presets Section
                item {
                    Text(text = "Quick Pre-set Heroes:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        viewModel.presetCharacters.forEach { preset ->
                            val isSelected = preset.name == name
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        name = preset.name
                                        heroType = preset.heroType
                                        hairColor = preset.hairColor
                                        clothingColor = preset.clothingColor
                                        companion = preset.companion
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = if (preset.name == "Leo") "🛡️" else if (preset.name == "Mia") "🚀" else if (preset.name == "Sam") "🪄" else "🧚", fontSize = 28.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = preset.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = preset.heroType, fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                // Customization Fields
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = "Hero Details", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Hero Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = heroType,
                                onValueChange = { heroType = it },
                                label = { Text("Hero Type / Role") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Text("Hair Color:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            ScrollableRowOptions(options = hairColors, selected = hairColor) { hairColor = it }

                            OutlinedTextField(
                                value = clothingColor,
                                onValueChange = { clothingColor = it },
                                label = { Text("Clothing Style & Outfit") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Text("Magical Companion:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            ScrollableRowOptions(options = companions, selected = companion) { companion = it }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            val profile = CharacterProfile(
                                name = name.ifBlank { "Hero" },
                                heroType = heroType.ifBlank { "Adventurer" },
                                hairColor = hairColor,
                                clothingColor = clothingColor,
                                companion = companion
                            )
                            viewModel.updateCharacter(profile)
                            onBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save & Adventure! ✨", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ScrollableRowOptions(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options.size) { idx ->
            val opt = options[idx]
            val isSelected = opt == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(opt) },
                label = { Text(opt) }
            )
        }
    }
}
