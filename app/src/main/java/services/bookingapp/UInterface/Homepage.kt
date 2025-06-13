package services.bookingapp.UInterface

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yourapp.booking.viewmodel.BookingViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(navController: NavController, viewModel: BookingViewModel = viewModel()) {
    var name by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val scrollState = rememberScrollState()

    LaunchedEffect(uid) {
        if (uid != null) {
            viewModel.checkIfFirstTime(uid)
            viewModel.fetchCurrentUserName { resultName ->
                name = resultName ?: ""
            }
        }
    }

    Scaffold(
        topBar = {
            // Modern top bar with gradient and search
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Column {
                        // Header with greeting and notifications
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (name.isNotBlank()) "$name"  else "",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Discover amazing events near you",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Profile Avatar
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .clickable { /* Navigate to profile */ },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Profile",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Modern search bar
                        ModernSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onSearch = { /* Handle search */ }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                var selected = ""
                // Quick filters
                QuickFiltersSection(onSelect = {
                    selectedFilter -> selected = selectedFilter
                })

                Spacer(modifier = Modifier.height(24.dp))

                // Featured Events with enhanced design
                SectionHeader(
                    title = "🌟 Suggested Events",
                    subtitle = "Don't miss out on these amazing experiences"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    items(4) { index ->
                        EnhancedSuggestedEventCard(index)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Upcoming Events with better design
                SectionHeader(
                    title = "📅 Upcoming ${selected} Near You",
                    subtitle = "Events happening in your area"
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    repeat(6) { index ->
                        EnhancedEventCard(index)
                    }
                }

                Spacer(modifier = Modifier.height(100.dp)) // Bottom padding
            }

            val showOnboarding = viewModel.isFirstTime.collectAsState().value
            FirstTimePopupForm(
                show = showOnboarding,
                onDismiss = { viewModel.setFirstTime(false) },
                onSubmit = { area, budget ->
                    uid?.let {
                        FirebaseFirestore.getInstance().collection("users").document(it).update(
                            mapOf(
                                "area" to area,
                                "budget" to budget,
                                "isFirstLogin" to false
                            )
                        )
                    }
                }
            )

        }
    }
}

@Composable
fun ModernSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                "Search events, venues, artists...",
                color = Color.White.copy(alpha = 0.7f)
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White.copy(alpha = 0.8f)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White.copy(alpha = 0.5f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() })
    )
}

@Composable
fun QuickFiltersSection(onSelect: (String) -> Unit) {
    val filters = listOf("🎵 Music", "🏃 Sports", "🍕 Food", "🎨 Art", "💼 Business", "🎭 Theater")
    var selectedFilter by remember { mutableStateOf<String?>(null) }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        items(filters) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = {
                    selectedFilter = if (selectedFilter == filter) null else filter
                    onSelect(selectedFilter.toString())
                },
                label = {
                    Text(
                        text = filter,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == filter,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun EnhancedSuggestedEventCard(index: Int) {
    val eventTitles = listOf("Summer Music Festival", "Tech Conference 2024", "Food & Wine Expo", "Art Gallery Opening")
    val eventLocations = listOf("Central Park", "Convention Center", "Downtown Plaza", "Art District")
    val eventDates = listOf("Dec 25", "Jan 15", "Feb 20", "Mar 10")
    val eventPrices = listOf("$35", "$120", "$25", "Free")

    Card(
        modifier = Modifier
            .width(320.dp)
            .height(280.dp)
            .clickable { /* Navigate to event details */ },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box {
            // Background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                            )
                        )
                    )
            )

            // Content overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text("Featured", style = MaterialTheme.typography.bodySmall)
                        },
                        leadingIcon = null,
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            labelColor = Color.White
                        )
                    )

                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // Bottom section
                Column {
                    Text(
                        text = eventTitles[index % eventTitles.size],
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = eventLocations[index % eventLocations.size],
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = eventDates[index % eventDates.size],
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Text(
                            text = eventPrices[index % eventPrices.size],
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedEventCard(index: Int) {
    val eventTitles = listOf(
        "Jazz Night at Blue Note",
        "Marathon Training Session",
        "Wine Tasting Evening",
        "Contemporary Art Workshop",
        "Startup Networking Event",
        "Comedy Show Tonight"
    )
    val eventLocations = listOf("Blue Note Club", "City Stadium", "Wine Bar", "Art Studio", "Co-working Space", "Comedy Club")
    val eventDates = listOf("Tonight 8PM", "Tomorrow 6AM", "Fri 7PM", "Sat 2PM", "Mon 6PM", "Sun 9PM")
    val eventPrices = listOf("$20", "Free", "$45", "$30", "$15", "$25")
    val eventIcons = listOf(
        Icons.Default.MusicNote,
        Icons.Default.DirectionsWalk,
        Icons.Default.LocalBar,
        Icons.Default.Palette,
        Icons.Default.Business,
        Icons.Default.TheaterComedy
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {},
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event icon with gradient background
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            ),
                            start = Offset.Zero,
                            end = Offset.Infinite
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    eventIcons[index % eventIcons.size],
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Event details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = eventTitles[index % eventTitles.size],
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${eventLocations[index % eventLocations.size]} • ${(1..5).random()}km away",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = eventDates[index % eventDates.size],
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = eventPrices[index % eventPrices.size],
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FirstTimePopupForm(
    show: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (area: String, budget: String) -> Unit
) {
    if (!show) return

    var area by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Complete Your Profile to improve our suggestions") },
        text = {
            Column {
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Area of Residence") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("Budget") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        confirmButton = {
            Button(onClick = {
                onSubmit(area, budget)
                onDismiss()
            }) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
