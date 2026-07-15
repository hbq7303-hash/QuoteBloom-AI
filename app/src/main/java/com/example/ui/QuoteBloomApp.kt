package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.SavedQuote
import com.example.ui.theme.CardTheme
import com.example.ui.theme.QuoteThemes
import com.example.util.QuoteImageExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteBloomApp(
    application: Application,
    viewModel: QuoteViewModel = viewModel(
        factory = QuoteViewModelFactory(
            application,
            com.example.data.QuoteRepository(
                com.example.data.QuoteBloomDatabase.getDatabase(application).savedQuoteDao()
            )
        )
    )
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf("Generate") }

    // Toast Feedback Handler
    LaunchedEffect(viewModel.feedbackMessage) {
        viewModel.feedbackMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "QuoteBloom AI",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(
                        onClick = { /* Menu placeholder or simple info */ },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                modifier = Modifier.shadow(1.dp)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .testTag("bottom_nav")
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
            ) {
                NavigationBarItem(
                    selected = currentTab == "Generate",
                    onClick = { currentTab = "Generate" },
                    icon = { Icon(Icons.Outlined.AutoAwesome, contentDescription = "Generate") },
                    label = { Text("Generate", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "Explore",
                    onClick = { currentTab = "Explore" },
                    icon = { Icon(Icons.Outlined.Explore, contentDescription = "Explore") },
                    label = { Text("Explore", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "Saved",
                    onClick = { currentTab = "Saved" },
                    icon = { Icon(Icons.Outlined.Bookmarks, contentDescription = "Saved") },
                    label = { Text("Saved", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "How It Works",
                    onClick = { currentTab = "How It Works" },
                    icon = { Icon(Icons.Outlined.HelpOutline, contentDescription = "How It Works") },
                    label = { Text("How-To", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "TabTransition"
            ) { targetTab ->
                when (targetTab) {
                    "Generate" -> GenerateTab(viewModel)
                    "Explore" -> ExploreTab(viewModel) { tab -> currentTab = tab }
                    "Saved" -> SavedTab(viewModel)
                    "How It Works" -> HowItWorksTab()
                }
            }
        }
    }
}

@Composable
fun QuoteDropdownSelector(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueSelected: (String) -> Unit,
    icon: ImageVector,
    testTag: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                enabled = false, // Makes the whole field clickable to show dropdown
                shape = RoundedCornerShape(16.dp),
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Expand dropdown",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                    disabledLeadingIconColor = MaterialTheme.colorScheme.primary,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                ),
                textStyle = TextStyle(fontSize = 13.sp)
            )

            // Transparent overlay button to capture click since text field is disabled/readOnly
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = true }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontSize = 13.sp,
                                fontWeight = if (option == selectedValue) FontWeight.Bold else FontWeight.Normal,
                                color = if (option == selectedValue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onValueSelected(option)
                            expanded = false
                        },
                        leadingIcon = {
                            if (option == selectedValue) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

// --- GENERATE TAB ---
@Composable
fun GenerateTab(viewModel: QuoteViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    var showCustomizer by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Card Preview if no quote is generated yet
        if (viewModel.generatedQuoteText == null && !viewModel.isGenerating) {
            HeroSection(onGenerateClick = {
                viewModel.generateQuote()
            })
        }

        // Active Quote Card Result
        if (viewModel.isGenerating || viewModel.generatedQuoteText != null || viewModel.generationError != null) {
            QuoteResultSection(
                viewModel = viewModel,
                onCustomizeClick = { showCustomizer = !showCustomizer }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Customize Panel (Collapsible Drawer-like Card)
        AnimatedVisibility(
            visible = showCustomizer && viewModel.generatedQuoteText != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(32.dp)
                    ),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Customize Design",
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Theme picker
                    Text("Theme Style", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(QuoteThemes.list) { theme ->
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(theme.backgroundColors)
                                    )
                                    .border(
                                        width = if (viewModel.activeTheme.id == theme.id) 3.dp else 1.dp,
                                        color = if (viewModel.activeTheme.id == theme.id) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .shadow(
                                        elevation = if (viewModel.activeTheme.id == theme.id) 4.dp else 0.dp,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { viewModel.activeTheme = theme }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = theme.name.take(1),
                                        color = theme.textColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 2. Font Style picker
                    Text("Font Family", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Serif", "Sans", "Monospace").forEach { font ->
                            val isSelected = viewModel.activeFontStyle == font
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .clickable { viewModel.activeFontStyle = font }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = font,
                                    fontFamily = when(font) {
                                        "Monospace" -> FontFamily.Monospace
                                        "Sans" -> FontFamily.SansSerif
                                        else -> FontFamily.Serif
                                    },
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3. Text Alignment picker
                    Text("Text Alignment", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Left", "Center", "Right").forEach { align ->
                            val isSelected = viewModel.activeTextAlignment == align
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .clickable { viewModel.activeTextAlignment = align }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = when(align) {
                                            "Left" -> Icons.Default.FormatAlignLeft
                                            "Right" -> Icons.Default.FormatAlignRight
                                            else -> Icons.Default.FormatAlignCenter
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = align,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Generator Config Form Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Create Your Quote",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "Choose what you need, then turn your moment into meaningful words.",
                    style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Category/Niche Selection via Dropdown
                val categories = listOf(
                    "Morning Motivation", "Success", "Self-Love", "Confidence",
                    "Productivity", "Discipline", "Healing", "Love", "Gratitude",
                    "Mindfulness", "Fitness", "Study", "Career", "Spirituality",
                    "Creativity", "Happiness"
                )
                QuoteDropdownSelector(
                    label = "Category / Niche",
                    selectedValue = viewModel.selectedCategory,
                    options = categories,
                    onValueSelected = { viewModel.selectedCategory = it },
                    icon = getCategoryIcon(viewModel.selectedCategory),
                    testTag = "niche_dropdown"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mood Selection via Dropdown
                val tones = listOf(
                    "Motivational", "Calm", "Deep", "Powerful", "Gentle",
                    "Poetic", "Funny", "Bold", "Spiritual", "Romantic"
                )
                QuoteDropdownSelector(
                    label = "Mood of Quote",
                    selectedValue = viewModel.selectedTone,
                    options = tones,
                    onValueSelected = { viewModel.selectedTone = it },
                    icon = Icons.Outlined.SentimentSatisfied,
                    testTag = "mood_dropdown"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quote Length Selection
                Text("Length", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Short", "Medium", "Long").forEach { length ->
                        val isSelected = viewModel.selectedLength == length
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .clickable { viewModel.selectedLength = length }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$length lines",
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Audience Selection
                Text("Target Audience (Optional)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val audiences = listOf("For Me", "For a Friend", "For Students", "For Entrepreneurs", "For a Team", "For Social Media", "For a Loved One")
                    items(audiences) { aud ->
                        val isSelected = viewModel.selectedAudience == aud
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .clickable { viewModel.selectedAudience = aud }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = aud,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Prompt Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Custom Prompt / Idea (Optional)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        text = "${viewModel.customIdea.length}/500",
                        fontSize = 11.sp,
                        color = if (viewModel.customIdea.length > 450) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = viewModel.customIdea,
                    onValueChange = { if (it.length <= 500) viewModel.customIdea = it },
                    placeholder = { Text("Example: Write a calm quote for anxiety before an exam...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("custom_idea_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    maxLines = 4,
                    textStyle = TextStyle(fontSize = 13.sp)
                )
                Text(
                    text = "The more detail you add, the more personal your quote can be.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Generate Button
                Button(
                    onClick = { viewModel.generateQuote() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("generate_button"),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !viewModel.isGenerating
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (viewModel.isGenerating) "Creating Your Inspiration..." else "Generate My Quote",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// --- EXPLORE TAB ---
@Composable
fun ExploreTab(viewModel: QuoteViewModel, navigateToTab: (String) -> Unit) {
    val categoriesList = listOf(
        ExploreCategory("Morning Motivation", "Start softly. Even a small step can change the shape of your day.", Icons.Outlined.LightMode, "lavender_dream"),
        ExploreCategory("Self-Love", "You do not have to earn the kindness you deserve.", Icons.Outlined.FavoriteBorder, "blush_bloom"),
        ExploreCategory("Productivity", "Focus on the next meaningful step, not the entire mountain.", Icons.Outlined.HourglassEmpty, "sunrise_gold"),
        ExploreCategory("Healing", "Healing is not forgetting; it is learning to breathe without the weight.", Icons.Outlined.Spa, "ocean_calm"),
        ExploreCategory("Success", "Success is not a destination, but the courage to keep flowing.", Icons.Outlined.TrendingUp, "sunset_energy"),
        ExploreCategory("Mindfulness", "Breathe in pure presence, release everything else.", Icons.Outlined.SelfImprovement, "forest_growth"),
        ExploreCategory("Happiness", "Joy resides not in the size of the bloom, but in the depth of appreciation.", Icons.Outlined.SentimentSatisfied, "sunrise_gold"),
        ExploreCategory("Creativity", "Art is your heart speaking in a language words cannot limit.", Icons.Outlined.Brush, "lavender_dream")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Explore Inspiration",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = "Tap any category to load a beautifully tailored visual preset and begin crafting.",
            style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(categoriesList) { cat ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clickable {
                            viewModel.usePremadeQuote(cat.sampleQuote, cat.title, cat.themeId)
                            navigateToTab("Generate")
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "“${cat.sampleQuote}”",
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            ),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Button(
                            onClick = {
                                viewModel.usePremadeQuote(cat.sampleQuote, cat.title, cat.themeId)
                                navigateToTab("Generate")
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Generate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

data class ExploreCategory(
    val title: String,
    val sampleQuote: String,
    val icon: ImageVector,
    val themeId: String
)

// --- HOW IT WORKS TAB ---
@Composable
fun HowWorksStep(number: String, title: String, desc: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun HowItWorksTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "How QuoteBloom Works",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Fast, unlimited, completely free, premium quote creation in seconds.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        Divider(color = Color.LightGray.copy(alpha = 0.3f))

        HowWorksStep(
            number = "1",
            title = "Choose Your Mood",
            desc = "Pick a tailored quote category, set the ideal emotional tone, and specify your preferred line length.",
            icon = Icons.Outlined.EmojiEmotions
        )

        Divider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

        HowWorksStep(
            number = "2",
            title = "Make It Personal",
            desc = "Add your own custom idea, challenge, or moment (e.g. 'motivate my study group') to tailor the quote's relevance perfectly.",
            icon = Icons.Outlined.Create
        )

        Divider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

        HowWorksStep(
            number = "3",
            title = "Customize & Share",
            desc = "Select from 8 breathtaking visual gradients, pick font styling and text alignment, and download or copy instantly for social sharing.",
            icon = Icons.Outlined.Share
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Premium Brand Info Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("100% Free, Private local storage", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "All generated and saved quotes are kept strictly on your local device. We never sell your personal ideas or compromise your workflow. No sign-ups required ever.",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// --- SAVED TAB ---
@Composable
fun SavedTab(viewModel: QuoteViewModel) {
    val context = LocalContext.current
    val savedQuotes by viewModel.filteredSavedQuotes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterCategory by viewModel.filterCategory.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()

    var showDeleteConfirmDialog by remember { mutableStateOf<SavedQuote?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Saved Inspiration",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = "Saved quotes are stored locally in your browser/app context.",
            style = TextStyle(fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Search & Filters Panel
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search saved quotes...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_saved_quotes"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            ),
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Sort & Filter selectors Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Category Filter Dropdown
            var showCategoryMenu by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { showCategoryMenu = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Filter: $filterCategory",
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                DropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false }
                ) {
                    val categories = listOf("All", "Morning Motivation", "Success", "Self-Love", "Confidence", "Productivity", "Healing", "Love", "Gratitude", "Mindfulness")
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                viewModel.filterCategory.value = cat
                                showCategoryMenu = false
                            }
                        )
                    }
                }
            }

            // Sort Dropdown
            var showSortMenu by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { showSortMenu = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Sort: $sortBy",
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    val sorts = listOf("Newest", "Oldest", "Favorites First")
                    sorts.forEach { sort ->
                        DropdownMenuItem(
                            text = { Text(sort) },
                            onClick = {
                                viewModel.sortBy.value = sort
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (savedQuotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Bookmarks,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your inspiration collection is waiting.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Generate a quote and save the words that matter to you.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                items(savedQuotes) { quote ->
                    SavedQuoteCardItem(
                        quote = quote,
                        onLoadIntoCustomizer = { viewModel.loadQuoteIntoCustomizer(quote) },
                        onToggleFavorite = { viewModel.toggleFavorite(quote) },
                        onDeleteClick = { showDeleteConfirmDialog = quote }
                    )
                }
            }
        }
    }

    // Confirmation Modal for Deletion
    showDeleteConfirmDialog?.let { quoteToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete saved quote?") },
            text = { Text("Are you sure you want to delete this quote from your local saved collection?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteQuote(quoteToDelete)
                        showDeleteConfirmDialog = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SavedQuoteCardItem(
    quote: SavedQuote,
    onLoadIntoCustomizer: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val theme = QuoteThemes.getByName(quote.themeName)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                shape = RoundedCornerShape(32.dp)
            )
            .shadow(2.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Visual Card Background Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Brush.linearGradient(theme.backgroundColors))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "“${quote.text}”",
                    style = TextStyle(
                        fontFamily = when (quote.fontStyle.lowercase()) {
                            "monospace" -> FontFamily.Monospace
                            "sans" -> FontFamily.SansSerif
                            else -> FontFamily.Serif
                        },
                        fontStyle = FontStyle.Italic,
                        fontSize = 14.sp,
                        color = theme.textColor,
                        textAlign = when (quote.textAlignment.lowercase()) {
                            "left" -> TextAlign.Left
                            "right" -> TextAlign.Right
                            else -> TextAlign.Center
                        }
                    ),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Actions and Category Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = quote.category,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Load to customizer
                    IconButton(onClick = onLoadIntoCustomizer) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Card",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Favorite toggler
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (quote.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (quote.isFavorite) Color.Red else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete button
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- ACTIVE QUOTE CARD RESULT WITH CUSTOM ACTIONS ---
@Composable
fun QuoteResultSection(viewModel: QuoteViewModel, onCustomizeClick: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                shape = RoundedCornerShape(32.dp)
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Preview Watermark Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Card Preview",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = viewModel.selectedCategory.uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // The main card preview box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.8f) // Clean Minimalism aspect-[4/5]
                    .clip(RoundedCornerShape(32.dp))
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(viewModel.activeTheme.backgroundColors)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .drawBehind {
                        // Soft decorative gradient circles on corners
                        drawCircle(
                            color = Color(0xFFC0A9F8).copy(alpha = 0.25f),
                            radius = 120.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x = this.size.width + 30.dp.toPx(), y = -20.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFFFFC0CB).copy(alpha = 0.22f),
                            radius = 130.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x = -40.dp.toPx(), y = this.size.height + 30.dp.toPx())
                        )
                    }
                    .testTag("quote_card"),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.isGenerating) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = viewModel.activeTheme.textColor,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = viewModel.loadingText,
                            color = viewModel.activeTheme.textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (viewModel.generationError != null) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.generationError ?: "An error occurred",
                            color = viewModel.activeTheme.textColor,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (viewModel.generatedQuoteText != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top watermark quotes symbol
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "“",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 72.sp,
                                color = viewModel.activeTheme.textColor.copy(alpha = 0.15f),
                                lineHeight = 40.sp
                            )
                        }

                        // Main quote text in center
                        Text(
                            text = viewModel.generatedQuoteText ?: "",
                            style = TextStyle(
                                fontFamily = when (viewModel.activeFontStyle.lowercase()) {
                                    "monospace" -> FontFamily.Monospace
                                    "sans" -> FontFamily.SansSerif
                                    else -> FontFamily.Serif
                                },
                                fontStyle = FontStyle.Italic,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 28.sp,
                                color = viewModel.activeTheme.textColor,
                                textAlign = when (viewModel.activeTextAlignment.lowercase()) {
                                    "left" -> TextAlign.Left
                                    "right" -> TextAlign.Right
                                    else -> TextAlign.Center
                                }
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Bottom watermark divider & branding
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(1.dp)
                                    .background(viewModel.activeTheme.textColor.copy(alpha = 0.2f))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Created with QuoteBloom AI",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp,
                                color = viewModel.activeTheme.textColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Option details tag pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(viewModel.selectedTone, viewModel.selectedLength + " lines", viewModel.selectedAudience).forEach { valTag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(valTag, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick action controls (Copy, Customize, Save)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Customize card styles
                IconButtonWithLabel(
                    icon = Icons.Outlined.Palette,
                    label = "Style",
                    onClick = onCustomizeClick,
                    enabled = viewModel.generatedQuoteText != null
                )

                // Copy plain text
                IconButtonWithLabel(
                    icon = Icons.Outlined.ContentCopy,
                    label = "Copy",
                    onClick = {
                        viewModel.generatedQuoteText?.let { text ->
                            clipboardManager.setText(AnnotatedString(text))
                            viewModel.showFeedback("Copied to clipboard!")
                        }
                    },
                    enabled = viewModel.generatedQuoteText != null
                )

                // Save to Room SQLite db
                IconButtonWithLabel(
                    icon = if (viewModel.isActiveQuoteSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    label = if (viewModel.isActiveQuoteSaved) "Saved" else "Save",
                    onClick = {
                        if (!viewModel.isActiveQuoteSaved) {
                            viewModel.saveActiveQuote()
                        } else {
                            viewModel.showFeedback("Quote already saved locally!")
                        }
                    },
                    enabled = viewModel.generatedQuoteText != null,
                    tint = if (viewModel.isActiveQuoteSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )

                // Share Quote (Standard Android intent chooser)
                IconButtonWithLabel(
                    icon = Icons.Outlined.Share,
                    label = "Share",
                    onClick = {
                        viewModel.generatedQuoteText?.let { text ->
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "“$text”\n\n— Generated via QuoteBloom AI")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        }
                    },
                    enabled = viewModel.generatedQuoteText != null
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Premium Image Download Section (Square & Portrait exports)
            if (viewModel.generatedQuoteText != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.generatedQuoteText?.let { text ->
                                val bitmap = QuoteImageExporter.generateQuoteBitmap(
                                    quoteText = text,
                                    category = viewModel.selectedCategory,
                                    theme = viewModel.activeTheme,
                                    fontStyle = viewModel.activeFontStyle,
                                    alignment = viewModel.activeTextAlignment,
                                    isPortrait = false
                                )
                                val savedUri = QuoteImageExporter.saveToGallery(context, bitmap)
                                if (savedUri != null) {
                                    viewModel.showFeedback("Saved Square image to gallery!")
                                } else {
                                    viewModel.showFeedback("Failed to save image")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Square (1:1)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.generatedQuoteText?.let { text ->
                                val bitmap = QuoteImageExporter.generateQuoteBitmap(
                                    quoteText = text,
                                    category = viewModel.selectedCategory,
                                    theme = viewModel.activeTheme,
                                    fontStyle = viewModel.activeFontStyle,
                                    alignment = viewModel.activeTextAlignment,
                                    isPortrait = true
                                )
                                val savedUri = QuoteImageExporter.saveToGallery(context, bitmap)
                                if (savedUri != null) {
                                    viewModel.showFeedback("Saved Portrait image to gallery!")
                                } else {
                                    viewModel.showFeedback("Failed to save image")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Instagram (4:5)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun IconButtonWithLabel(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (enabled) tint.copy(alpha = 0.08f) else Color.LightGray.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) tint else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray
        )
    }
}

@Composable
fun HeroSection(onGenerateClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                shape = RoundedCornerShape(32.dp)
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Find the words your heart needs today.",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Choose a mood, a moment, or your own idea — and let AI create an original quote made just for you. Completely free, no sign-up needed.",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Sample Preview Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = QuoteThemes.list[0].backgroundColors[0].copy(alpha = 0.7f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "“Some days, progress is simply choosing to begin again.”",
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontSize = 15.sp,
                            color = QuoteThemes.list[0].textColor,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Created with QuoteBloom AI",
                        fontSize = 10.sp,
                        color = QuoteThemes.list[0].subtitleColor.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onGenerateClick,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Generate My Quote", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

// --- CATEGORY ICON UTILS ---
fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "morning motivation" -> Icons.Outlined.LightMode
        "success" -> Icons.Outlined.TrendingUp
        "self-love" -> Icons.Outlined.FavoriteBorder
        "confidence" -> Icons.Outlined.EmojiPeople
        "productivity" -> Icons.Outlined.HourglassEmpty
        "discipline" -> Icons.Outlined.Lock
        "healing" -> Icons.Outlined.Spa
        "love" -> Icons.Outlined.VolunteerActivism
        "gratitude" -> Icons.Outlined.CardGiftcard
        "mindfulness" -> Icons.Outlined.SelfImprovement
        "fitness" -> Icons.Outlined.FitnessCenter
        "study" -> Icons.Outlined.School
        "career" -> Icons.Outlined.WorkOutline
        "spirituality" -> Icons.Outlined.Brightness7
        "creativity" -> Icons.Outlined.Brush
        else -> Icons.Outlined.SentimentSatisfied
    }
}
