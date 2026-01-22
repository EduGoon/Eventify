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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import services.data.Event
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.text.format
import kotlin.text.uppercase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: EventViewModel = hiltViewModel()
) {
    val events by viewModel.events.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Music", "Tech", "Sports", "Art", "Food", "Business")
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // Filter sheet
    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            FilterSheetContent(onDismiss = { showFilterSheet = false })
        }
    }

    // Event detail sheet
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

    // Full screen image dialog
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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 0.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Eventify",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-1).sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Nairobi, Kenya",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalIconButton(onClick = { }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                            FilledTonalIconButton(onClick = { }) {
                                Icon(Icons.Default.Person, contentDescription = "Profile")
                            }
                        }
                    }

                    // Category chips
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = {
                                    Text(
                                        category,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Medium
                                        )
                                    )
                                },
                                leadingIcon = if (selectedCategory == category) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (events.isEmpty()) {
                item { HomeSkeleton() }
            } else {
                // Hero Event (Biggest featured event)
                item {
                    HeroEventCard(
                        event = events.firstOrNull() ?: return@item,
                        onClick = { selectedEvent = events.firstOrNull() }
                    )
                }

                // Quick stats or promotional banner
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            icon = Icons.Default.Event,
                            count = "${events.size}+",
                            label = "Events",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            icon = Icons.Default.LocationOn,
                            count = "5",
                            label = "Cities",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            icon = Icons.Default.Star,
                            count = "4.8",
                            label = "Rating",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Trending Events - Horizontal scroll
                item {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "🔥 Trending Now",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    "Don't miss these hot events",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            items(events.take(5)) { event ->
                                TrendingEventCard(event = event, onClick = { selectedEvent = event })
                            }
                        }
                    }
                }

                // This Weekend Section
                item {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "This Weekend",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    "Plan your perfect weekend",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { }) {
                                Text("See all")
                                Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Two column grid
                        val weekendEvents = events.drop(1).take(4)
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            weekendEvents.chunked(2).forEach { rowEvents ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowEvents.forEach { event ->
                                        CompactEventCard(
                                            event = event,
                                            onClick = { selectedEvent = event },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rowEvents.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                // All Events List
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "All Events",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        TextButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.FilterList, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Filter")
                        }
                    }
                }

                items(events.drop(5)) { event ->
                    ModernEventListItem(event = event, onClick = { selectedEvent = event })
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun HeroEventCard(event: Event, onClick: () -> Unit) {
    val formattedDate = remember(event.date) { formatIsoDate(event.date) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .height(400.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(event.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.9f)
                            ),
                            startY = 200f
                        )
                    )
            )

            // Featured Badge
            Surface(
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.TopStart),
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        "FEATURED",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Text(
                    text = event.title ?: "",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    ),
                    color = Color.White,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            formattedDate,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (!event.location.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                event.location!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(icon: ImageVector, count: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                count,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun TrendingEventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(event.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 200f
                        )
                    )
            )

            // Price
            if (!event.price.isNullOrBlank()) {
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        event.price!!,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    event.title ?: "",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!event.location.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        event.location!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun CompactEventCard(event: Event, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val dateParts = remember(event.date) { parseIsoDateParts(event.date) }

    Card(
        modifier = modifier
            .height(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(event.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                            startY = 100f
                        )
                    )
            )

            // Date badge
            if (dateParts != null) {
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.95f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            dateParts.first,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp
                        )
                        Text(
                            dateParts.second,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Text(
                event.title ?: "",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ModernEventListItem(event: Event, onClick: () -> Unit) {
    val formattedDate = remember(event.date) { formatIsoDate(event.date) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(90.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(event.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    event.title ?: "",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!event.location.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            event.location!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            FilledTonalIconButton(
                onClick = onClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun FilterSheetContent(onDismiss: () -> Unit) {
    val locations = listOf("All", "Nairobi", "Mombasa", "Kisumu", "Nakuru", "International")
    val eventTypes = listOf("All", "Music", "Tech", "Sports", "Art", "Food", "Business")
    val priceRanges = listOf("All", "Free", "Under KES 1000", "KES 1000-5000", "Above KES 5000")

    var selectedLocation by remember { mutableStateOf("All") }
    var selectedType by remember { mutableStateOf("All") }
    var selectedPrice by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            "Filter Events",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text("Location", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(locations) { loc ->
                FilterChip(
                    selected = selectedLocation == loc,
                    onClick = { selectedLocation = loc },
                    label = { Text(loc) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Category", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(eventTypes) { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = { Text(type) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Price Range", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(priceRanges) { price ->
                FilterChip(
                    selected = selectedPrice == price,
                    onClick = { selectedPrice = price },
                    label = { Text(price) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = {
                    selectedLocation = "All"
                    selectedType = "All"
                    selectedPrice = "All"
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset")
            }
            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Apply Filters")
            }
        }
    }
}

// Keep the existing helper functions and EventDetailContent
@Composable
fun EventDetailContent(event: Event, onImageClick: (String) -> Unit) {
    val uriHandler = LocalUriHandler.current
    val formattedDate = remember(event.date) { formatIsoDate(event.date) }
    val dateParts = remember(event.date) { parseIsoDateParts(event.date) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
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
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 500f
                        )
                    )
            )

            if (dateParts != null) {
                Surface(
                    modifier = Modifier.padding(20.dp).align(Alignment.TopStart),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(dateParts.first, fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 14.sp)
                        Text(dateParts.second, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "UPCOMING",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.title ?: "",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.price ?: "Free",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Row {
                    FilledTonalIconButton(onClick = { }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalIconButton(onClick = { }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            InfoRow(icon = Icons.Default.CalendarMonth, title = formattedDate, subtitle = "Starting time")
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(icon = Icons.Default.LocationOn, title = event.location ?: "Online", subtitle = "Event venue")

            Spacer(modifier = Modifier.height(32.dp))

            Text(text = "About this event", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (event.about.isNullOrBlank()) {
                    "Join us for this amazing event at ${event.location}. Experience a night of wonder and excitement. Don't miss out on this opportunity to connect and enjoy!"
                } else {
                    event.about!!
                },
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 26.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    event.url?.let {
                        try {
                            uriHandler.openUri(it)
                        } catch (e: Exception) { }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !event.url.isNullOrBlank(),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("Reserve Your Spot", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun formatIsoDate(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "TBA"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("EEE, d MMM • h:mm a", Locale.ENGLISH)
        val date = inputFormat.parse(isoString)
        date?.let { outputFormat.format(it) } ?: isoString
    } catch (e: Exception) {
        try {
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(isoString)
            val outputFormat = SimpleDateFormat("EEE, d MMM • h:mm a", Locale.ENGLISH)
            date?.let { outputFormat.format(it) } ?: isoString
        } catch (e2: Exception) {
            isoString
        }
    }
}

fun parseIsoDateParts(isoString: String?): Pair<String, String>? {
    if (isoString.isNullOrBlank()) return null
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH)
        val date = inputFormat.parse(isoString) ?: return null

        val monthFormat = SimpleDateFormat("MMM", Locale.ENGLISH)
        val dayFormat = SimpleDateFormat("dd", Locale.ENGLISH)

        val month = monthFormat.format(date).uppercase()
        val day = dayFormat.format(date)
        Pair(month, day)
    } catch (e: Exception) {
        null
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

    Column(modifier = Modifier.padding(20.dp)) {
        Box(Modifier.fillMaxWidth().height(400.dp).background(Color.LightGray.copy(alpha = alpha), RoundedCornerShape(28.dp)))

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) {
                Box(Modifier.weight(1f).height(90.dp).background(Color.LightGray.copy(alpha = alpha), RoundedCornerShape(20.dp)))
            }
        }

        Spacer(Modifier.height(32.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(3) {
                Box(Modifier.width(280.dp).height(180.dp).background(Color.LightGray.copy(alpha = alpha), RoundedCornerShape(20.dp)))
            }
        }

        Spacer(Modifier.height(32.dp))

        repeat(4) {
            Box(Modifier.fillMaxWidth().height(110.dp).padding(vertical = 6.dp).background(Color.LightGray.copy(alpha = alpha), RoundedCornerShape(20.dp)))
        }
    }
}