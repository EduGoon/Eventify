package services.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import services.data.Event

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: EventViewModel = hiltViewModel()
) {
    val events by viewModel.events.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    
    val locations = listOf("All", "Nairobi", "Mombasa", "Kisumu", "Nakuru", "International")
    val eventTypes = listOf("All", "Music", "Tech", "Sports", "Art", "Food", "Business")
    
    var selectedLocation by remember { mutableStateOf("All") }
    var selectedType by remember { mutableStateOf("All") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                Text("Filters", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Location", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(locations) { loc ->
                        FilterChip(
                            selected = selectedLocation == loc,
                            onClick = { selectedLocation = loc },
                            label = { Text(loc) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Event Type", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(eventTypes) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type) }
                        )
                    }
                }
            }
        }
    }

    if (selectedEvent != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedEvent = null },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxHeight(0.95f)
        ) {
            EventDetailContent(
                event = selectedEvent!!,
                onImageClick = { url -> fullScreenImageUrl = url }
            )
        }
    }

    if (fullScreenImageUrl != null) {
        Dialog(
            onDismissRequest = { fullScreenImageUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(fullScreenImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Full Screen Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { fullScreenImageUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eventify", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)) },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.Search, contentDescription = "Search") }
                    IconButton(onClick = { }) { Icon(Icons.Default.Notifications, contentDescription = "Notifications") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
        ) {
            if (events.isEmpty()) {
                item { HomeSkeleton() }
            } else {
                item {
                    Text("Suggested for you", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(events.take(3)) { event -> 
                            SuggestedEventCard(event = event, onClick = { selectedEvent = event }) 
                        }
                    }
                }

                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Events near you", style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Filter", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                items(events) { event ->
                    NearEventCard(event = event, onClick = { selectedEvent = event })
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun EventDetailContent(
    event: Event,
    onImageClick: (String) -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clickable { event.imageUrl?.let { onImageClick(it) } }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(event.imageUrl).crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 400f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = event.title ?: "",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = event.location ?: "", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                }
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Date & Time", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(text = event.date ?: "", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = event.price ?: "",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "About Event", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (event.about.isNullOrBlank()) {
                    "Join us for this amazing event at ${event.location}. Experience a night of wonder and excitement. Don't miss out on this opportunity to connect and enjoy!"
                } else {
                    event.about!!
                },
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(icon = Icons.Default.Notifications, label = "Remind Me")
                ActionButton(icon = Icons.Default.ThumbUp, label = "Like")
                ActionButton(icon = Icons.Default.ThumbDown, label = "Dislike")
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    event.url?.let { 
                        try {
                            uriHandler.openUri(it)
                        } catch (e: Exception) {
                            // Handle invalid URI if necessary
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !event.url.isNullOrBlank()
            ) {
                Text("Get Tickets", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(
            onClick = { },
            modifier = Modifier.size(56.dp),
            shape = CircleShape
        ) {
            Icon(icon, contentDescription = label)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun HomeSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse),
        label = "alpha"
    )

    Column {
        Box(Modifier.fillMaxWidth(0.5f).height(24.dp).padding(vertical = 4.dp).background(Color.LightGray.copy(alpha = alpha), RoundedCornerShape(4.dp)))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(vertical = 16.dp)) {
            items(2) {
                Box(Modifier.width(280.dp).height(180.dp).background(Color.LightGray.copy(alpha = alpha), RoundedCornerShape(16.dp)))
            }
        }
        
        Box(Modifier.fillMaxWidth(0.4f).height(24.dp).padding(vertical = 4.dp).background(Color.LightGray.copy(alpha = alpha), RoundedCornerShape(4.dp)))
        repeat(5) {
            Box(Modifier.fillMaxWidth().height(100.dp).padding(vertical = 6.dp).background(Color.LightGray.copy(alpha = alpha), RoundedCornerShape(12.dp)))
        }
    }
}

@Composable
fun SuggestedEventCard(event: Event, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.width(280.dp).height(180.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(event.imageUrl).crossfade(true).build(),
                contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Color.Black.copy(alpha = 0.5f)).padding(12.dp)) {
                Text(text = event.title ?: "", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                Text(text = "${event.date} • ${event.location}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun NearEventCard(event: Event, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().height(100.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(event.imageUrl).crossfade(true).build(),
                contentDescription = null, modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp, 0.dp, 0.dp, 12.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp).weight(1f)) {
                Text(text = event.title ?: "", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1)
                Text(text = event.location ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Text(text = event.price ?: "", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
