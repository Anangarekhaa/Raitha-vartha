package com.example.raitha_vartha.ui.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.raitha_vartha.R
import com.example.raitha_vartha.model.Tip
import com.example.raitha_vartha.viewmodel.TipViewModel

private val DarkGreen = Color(0xFF0B3D2E)
private val LightGreenBg = Color(0xFFEAF3EC)
private val SuccessGold = Color(0xFFFFD700)
private val MitraPrimary = Color(0xFF2E7D32)
private val MitraSecondary = Color(0xFFFFC107)
private val MitraBotBg = Color(0xFF1B5E20)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TipViewModel = viewModel()) {
    val tips by viewModel.tips.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val selectedCrop by viewModel.selectedCrop.collectAsState()
    val onlySuccessStories by viewModel.onlySuccessStories.collectAsState()
    val regionInfo by viewModel.regionInfo.collectAsState()

    var showFilterMenu by remember { mutableStateOf(false) }
    var showChatSheet by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.fetchCurrentLocation()
            showLocationDialog = false
        }
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { if (tips.isEmpty()) 0 else Int.MAX_VALUE }
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val fabScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fabScale"
    )

    // Initial scroll to middle for infinite effect
    LaunchedEffect(tips) {
        if (tips.isNotEmpty() && pagerState.currentPage == 0) {
            val middlePage = Int.MAX_VALUE / 2
            val startPage = middlePage - (middlePage % tips.size)
            pagerState.scrollToPage(startPage)
        }
    }
    
    // Mark welcome shown when user swipes away
    LaunchedEffect(pagerState.currentPage) {
        if (tips.isNotEmpty()) {
            val currentTip = tips[pagerState.currentPage % tips.size]
            if (currentTip.cropType.lowercase() != "general") {
                viewModel.markWelcomeShown()
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = DarkGreen, 
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Raitha-Varta", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                modifier = Modifier.clickable { 
                                    viewModel.setLanguage(if (currentLanguage == "en") "kn" else "en")
                                }
                            ) {
                                Text(
                                    if (currentLanguage == "en") "ಕನ್ನಡ" else "English",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = DarkGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Box {
                                IconButton(
                                    onClick = { showFilterMenu = true },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                ) {
                                    Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                
                                DropdownMenu(
                                    expanded = showFilterMenu,
                                    onDismissRequest = { showFilterMenu = false },
                                    modifier = Modifier.background(Color.White).width(220.dp)
                                ) {
                                    FilterMenuItem("All", "📁", selectedCrop == null) { viewModel.setCropFilter("All"); showFilterMenu = false }
                                    FilterMenuItem("Coconut", "🌴", selectedCrop == "Coconut") { viewModel.setCropFilter("Coconut"); showFilterMenu = false }
                                    FilterMenuItem("Tomato", "🍅", selectedCrop == "Tomato") { viewModel.setCropFilter("Tomato"); showFilterMenu = false }
                                    FilterMenuItem("Paddy", "🌾", selectedCrop == "Paddy") { viewModel.setCropFilter("Paddy"); showFilterMenu = false }
                                    FilterMenuItem("Areca nut", "🥜", selectedCrop == "Areca nut") { viewModel.setCropFilter("Areca nut"); showFilterMenu = false }
                                    
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("⭐", fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text("Success", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                                Text("Stories", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                            }
                                        }
                                        Switch(
                                            checked = onlySuccessStories,
                                            onCheckedChange = { viewModel.toggleSuccessStories(it) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = DarkGreen,
                                                checkedTrackColor = LightGreenBg,
                                                uncheckedThumbColor = Color.Gray,
                                                uncheckedTrackColor = Color.LightGray
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Location Bar
                    Surface(
                        onClick = { showLocationDialog = true },
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MitraSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (regionInfo.locationName == "Detecting...") "Detecting location..." else "${regionInfo.locationName} • ${regionInfo.weather}",
                                color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Filled.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            Box(modifier = Modifier.scale(fabScale)) {
                FloatingActionButton(
                    onClick = { showChatSheet = true },
                    containerColor = Color.Transparent,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                    modifier = Modifier.size(85.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            shape = CircleShape,
                            color = MitraBotBg,
                            modifier = Modifier.size(76.dp).border(4.dp, Color.White, CircleShape).shadow(8.dp, CircleShape)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("👨‍🌾", fontSize = 32.sp)
                                Text("MITRA", color = MitraSecondary, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        // Online indicator dot
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                                .border(2.5.dp, Color.White, CircleShape)
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White)) {
            if (tips.isNotEmpty()) {
                val currentIndex = pagerState.currentPage % tips.size
                val currentTip = tips[currentIndex]
                
                // Background Image - Top portion
                Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                    val imageResId = remember(currentTip.image) {
                        val id = context.resources.getIdentifier(currentTip.image, "drawable", context.packageName)
                        if (id == 0) R.drawable.farm else id
                    }
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(imageResId).crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Subtle gradient to blend
                    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.1f), Color.White),
                        startY = 600f
                    )))
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1
                ) { page ->
                    val tip = tips[page % tips.size]
                    TipCardContent(tip, onBookmarkClick = { viewModel.toggleBookmark(tip) })
                }

                // Indicators and Navigation Help - REDUCED SIZES AS REQUESTED
                Column(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                        Text("👈", fontSize = 10.sp)
                        Text("👆", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 14.dp))
                        Text("👉", fontSize = 10.sp)
                    }
                    Text(
                        if (currentLanguage == "en") "Swipe for next tip" else "ಮುಂದಿನ ಸಲಹೆಗಾಗಿ ಸ್ವೈಪ್ ಮಾಡಿ",
                        color = Color(0xFF1B5E20), fontSize = 9.sp, fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(minOf(tips.size, 5)) { i ->
                            Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(
                                if ((currentIndex % 5) == i) Color(0xFF1B5E20) else Color.LightGray.copy(alpha = 0.5f)
                            ))
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DarkGreen)
                }
            }
        }

        // Location Dialog
        if (showLocationDialog) {
            var tempLocation by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showLocationDialog = false },
                title = { Text(if (currentLanguage == "en") "Set Location" else "ಸ್ಥಳವನ್ನು ಹೊಂದಿಸಿ", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            if (currentLanguage == "en") "Choose how you want to set your farming location for personalized tips." 
                            else "ವೈಯಕ್ತಿಕ ಸಲಹೆಗಳಿಗಾಗಿ ನಿಮ್ಮ ಕೃಷಿ ಸ್ಥಳವನ್ನು ಹೇಗೆ ಹೊಂದಿಸಬೇಕೆಂದು ಆರಿಸಿ.",
                            fontSize = 14.sp, color = Color.Gray
                        )
                        
                        OutlinedTextField(
                            value = tempLocation,
                            onValueChange = { tempLocation = it },
                            label = { Text(if (currentLanguage == "en") "Enter District/Town" else "ಜಿಲ್ಲೆ/ಪಟ್ಟಣ ನಮೂದಿಸಿ") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = DarkGreen) }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        
                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MitraPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(if (currentLanguage == "en") "Use GPS Location" else "GPS ಸ್ಥಳ ಬಳಸಿ")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (tempLocation.isNotBlank()) {
                            viewModel.updateRegionByLocation(tempLocation)
                            showLocationDialog = false
                        }
                    }, enabled = tempLocation.isNotBlank()) {
                        Text("Update Manually", fontWeight = FontWeight.Bold, color = DarkGreen)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLocationDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }

        if (showChatSheet) {
            ModalBottomSheet(
                onDismissRequest = { showChatSheet = false },
                containerColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle(width = 40.dp, color = Color.LightGray) }
            ) {
                AgriChatContent(viewModel, currentLanguage)
            }
        }
    }
}

@Composable
fun FilterMenuItem(label: String, icon: String, isSelected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = label, 
                    fontSize = 16.sp, 
                    color = if (isSelected) DarkGreen else Color.Black, 
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        },
        onClick = onClick
    )
}

@Composable
fun TipCardContent(tip: Tip, onBookmarkClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 100.dp, bottom = 140.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with icon and crop name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF0F4F1),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(when (tip.cropType.lowercase()) {
                                    "coconut" -> "🥥"
                                    "tomato" -> "🍅"
                                    "areca nut" -> "🥜"
                                    "paddy" -> "🌾"
                                    else -> "👨‍🌾"
                                }, fontSize = 26.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            tip.cropType.replaceFirstChar { it.uppercase() },
                            color = Color(0xFF424242),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    IconButton(onClick = onBookmarkClick) {
                        Icon(
                            if (tip.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = Color(0xFF1B5E20),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(modifier = Modifier.width(44.dp), thickness = 2.dp, color = Color.LightGray.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = tip.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 26.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Description box with leaf icon
                Surface(
                    color = Color(0xFFF1F8F3),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
                        Surface(
                            color = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp),
                            border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.2f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null, 
                                    tint = Color(0xFF2E7D32), 
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(18.dp))
                        Text(
                            text = tip.description,
                            fontSize = 17.sp,
                            lineHeight = 26.sp,
                            color = Color(0xFF455A64),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                if (tip.isSuccessStory) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = SuccessGold, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("SUCCESS STORY", color = SuccessGold, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AgriChatContent(viewModel: TipViewModel, language: String) {
    var userQuery by remember { mutableStateOf("") }
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (!results.isNullOrEmpty()) {
                    userQuery = results[0]
                }
            }
        }
    )

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .padding(horizontal = 20.dp)
    ) {
        // Chat Header with status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Box {
                Surface(shape = CircleShape, color = MitraBotBg, modifier = Modifier.size(56.dp), border = BorderStroke(2.dp, Color.White), shadowElevation = 4.dp) {
                    Box(contentAlignment = Alignment.Center) { Text("👨‍🌾", fontSize = 28.sp) }
                }
                Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(Color(0xFF4CAF50)).border(2.dp, Color.White, CircleShape).align(Alignment.TopEnd))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Mitra AI", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = DarkGreen)
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(color = MitraPrimary, shape = RoundedCornerShape(6.dp)) {
                        Text("EXPERT", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Text(
                    if (language == "kn") "ಆನ್‌ಲೈನ್ • ನಿಮ್ಮ ಕೃಷಿ ಸ್ನೇಹಿತ" else "Online • Your Farming Friend", 
                    fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium
                )
            }
        }
        
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))

        // Message List
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            item {
                BotMessageBubble(
                    if (language == "kn") "ನಮಸ್ಕಾರ! ನಾನು ಮಿತ್ರ, ನಿಮ್ಮ ವೈಯಕ್ತಿಕ ಕೃಷಿ ತಜ್ಞ. ನಾನು ನಿಮಗೆ ಹೇಗೆ ಸಹಾಯ ಮಾಡಲಿ?"
                    else "Namaste! I am Mitra, your personal agriculture expert. How can I help you today?"
                )
            }
            items(chatMessages) { message ->
                if (message.isUser) UserMessageBubble(message.text) else BotMessageBubble(message.text)
            }
            if (isLoading) {
                item {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MitraPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Mitra is thinking...", fontSize = 14.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                }
            }
        }

        // Improved Input Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userQuery,
                onValueChange = { userQuery = it },
                placeholder = { Text(if (language == "kn") "ಮಿತ್ರನ ಬಳಿ ಕೇಳಿ..." else "Ask Mitra anything...", fontSize = 15.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MitraPrimary,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                maxLines = 3,
                trailingIcon = {
                    IconButton(onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (language == "kn") "kn-IN" else "en-US")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, if (language == "kn") "ಮಾತನಾಡಿ..." else "Speak now...")
                        }
                        speechRecognizerLauncher.launch(intent)
                    }) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = MitraPrimary)
                    }
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            FloatingActionButton(
                onClick = { 
                    if (userQuery.isNotBlank()) {
                        viewModel.askChatbot(userQuery)
                        userQuery = ""
                    }
                },
                containerColor = if (userQuery.isNotBlank()) MitraPrimary else Color.LightGray,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(52.dp),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun UserMessageBubble(text: String) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Surface(
            color = MitraPrimary,
            shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp), // Pointed bottom right
            modifier = Modifier.widthIn(max = 280.dp),
            shadowElevation = 2.dp
        ) {
            Text(text = text, color = Color.White, modifier = Modifier.padding(14.dp), fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun BotMessageBubble(text: String) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp), // Pointed bottom left
            modifier = Modifier.widthIn(max = 300.dp),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.6f)),
            shadowElevation = 1.dp
        ) {
            Text(text = text, color = Color.Black, modifier = Modifier.padding(14.dp), fontSize = 15.sp, lineHeight = 22.sp)
        }
    }
}
