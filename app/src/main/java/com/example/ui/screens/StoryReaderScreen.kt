package com.example.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.util.SoundHelper
import com.example.viewmodel.StoryViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryReaderScreen(
    viewModel: StoryViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val story = viewModel.currentStory
    val pagerState = rememberPagerState(pageCount = { story?.pages?.size ?: 1 })
    val soundHelper = remember { SoundHelper(context) }

    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(0.85f) // Friendly slow speed for kids
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
            soundHelper.release()
        }
    }

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
                    title = { Text(story?.title ?: "Magical Story", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ) {
                            Text(text = viewModel.imageSize, modifier = Modifier.padding(4.dp), fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            if (story == null || story.pages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No story loaded yet!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onBack) {
                            Text("Go Back & Create Story")
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Page indicator
                    Text(
                        text = "Page ${pagerState.currentPage + 1} of ${story.pages.size}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) { pageIndex ->
                        val page = story.pages[pageIndex]

                        Card(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Illustration Box with Interactive Hotspots
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFF3F4F6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (page.imageUrl != null) {
                                        AsyncImage(
                                            model = page.imageUrl,
                                            contentDescription = "Story Illustration",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Painting illustration (${viewModel.imageSize})...", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }

                                    // Interactive Hotspots overlayed on illustration
                                    page.interactiveObjects.forEach { obj ->
                                        var isClicked by remember { mutableStateOf(false) }
                                        val scale by animateFloatAsState(
                                            targetValue = if (isClicked) 1.4f else 1.0f,
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                            finishedListener = { isClicked = false }
                                        )

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(
                                                    start = (obj.xPercent * 300).dp.coerceAtLeast(0.dp),
                                                    top = (obj.yPercent * 250).dp.coerceAtLeast(0.dp)
                                                )
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .scale(scale)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.85f))
                                                    .clickable {
                                                        isClicked = true
                                                        soundHelper.playSound(obj.soundType, context)
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = obj.emoji, fontSize = 24.sp)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Story Text
                                Text(
                                    text = page.text,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF1F2937),
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Read Aloud Button
                                Button(
                                    onClick = {
                                        isSpeaking = true
                                        tts?.speak(page.text, TextToSpeech.QUEUE_FLUSH, null, null)
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = "Read Aloud")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Read Aloud 🔊", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
