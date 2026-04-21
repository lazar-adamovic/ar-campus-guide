@file:Suppress("SpellCheckingInspection")

package com.example.projekat.ar

import android.location.Location
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.projekat.R
import com.example.projekat.data.PoiDto
import com.example.projekat.data.CategoryDto
import com.example.projekat.data.RetrofitInstance
import com.example.projekat.data.getIconResId
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
//region FILTERI
@Composable
fun CategoryFilterBar(
    poiList: List<PoiDto>,
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    val categories = remember(poiList) {
        listOf("Sve") + poiList.map { it.categoryName }.distinct()
    }
    var isExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isExpanded) {
            LazyRow(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedType == category
                    Surface(
                        modifier = Modifier.clickable {
                            onTypeSelected(category)
                            isExpanded = false
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFF1976D2) else Color.Black.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = category,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            containerColor = if (isExpanded) Color(0xFF1976D2) else Color.Black.copy(alpha = 0.7f),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.FilterList,
                contentDescription = "Filter",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
//endregion
//region KALIBRACIJA
@Composable
fun CalibrationOverlay() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = Color.Black)
            Spacer(modifier = Modifier.height(20.dp))
            Text("Kalibracija sistema...", color = Color.DarkGray, fontWeight = FontWeight.Bold)
            Text("Priprema senzora i ARCore okruženja", color = Color.DarkGray, fontSize = 12.sp,fontWeight = FontWeight.Bold)
        }
    }
}
//endregion
//region UPOZORENJE NA POLOZAJ TELEFONA
@Composable
fun VerticalPhoneOverlay() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.PhoneAndroid, null, tint = Color.White, modifier = Modifier.size(100.dp))
            Text("USPRAVITE TELEFON", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
//endregion
//region HUD
@Composable
fun SelectedPoiHud(
    poi: PoiDto?,
    location: Location?,
    currentAzimut: Float,
    onOpenDetails: () -> Unit,
    onClose: () -> Unit
) {
    if (poi == null || location == null) return

    val distance = remember(location, poi) {
        val results = FloatArray(1)
        Location.distanceBetween(
            location.latitude, location.longitude,
            poi.latitude, poi.longitude, results
        )
        results[0]
    }
    val poiBearing = bearingToPOI(location.latitude, location.longitude, poi.latitude, poi.longitude)
    var relativeAngle: Double = (poiBearing - currentAzimut)
    if (relativeAngle > 180) relativeAngle -= 360
    if (relativeAngle < -180) relativeAngle += 360
    Box(
        modifier = Modifier.fillMaxSize().padding(20.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color.Cyan.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = poi.name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.CenterEnd)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Zatvori", tint = Color.White)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if(distance > 3f) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_up),
                            contentDescription = "Arrow",
                            tint = Color.Cyan,
                            modifier = Modifier
                                .size(48.dp)
                                .rotate(relativeAngle.toFloat())
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "${distance.toInt()} m",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onOpenDetails,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color.Cyan)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.Cyan)
                    Spacer(Modifier.width(8.dp))
                    Text("DETALJNIJE", color = Color.Cyan, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
//endregion
//region ADMIN PANEL
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    categories: List<CategoryDto>,
    poiList: List<PoiDto>,
    currentLocation: Location?,
    onFullUpdate: (PoiDto) -> Unit,
    onLocationUpdate: (PoiDto) -> Unit,
    onDelete: (String) -> Unit,
    onCreate: (PoiDto) -> Unit,
    onDescriptionUpdate: (String, String) -> Unit,
    onClose: () -> Unit
) {
    var editingPoi by remember { mutableStateOf<PoiDto?>(null) }
    var editingDescriptionPoi by remember { mutableStateOf<PoiDto?>(null) }
    var isCreating by remember { mutableStateOf(false) }
    var poiToDelete by remember { mutableStateOf<PoiDto?>(null) }
    var showExitConfirmation by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredPois = remember(searchQuery, poiList) {
        poiList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.categoryName.contains(searchQuery, ignoreCase = true)
        }
    }
    BackHandler(enabled = true) {
        showExitConfirmation = true
    }
    val containerColor = MaterialTheme.colorScheme.surface
    val contentColor = MaterialTheme.colorScheme.onSurface
    Scaffold(
        containerColor = containerColor,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor.copy(alpha = 1f),
                    titleContentColor = contentColor,
                    actionIconContentColor = contentColor
                ),
                title = {
                    if (!isSearchExpanded) {
                        Text("Admin Panel")
                    } else {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Pretraži...", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (isSearchExpanded) {
                            searchQuery = ""
                            isSearchExpanded = false
                        } else {
                            isSearchExpanded = true
                        }
                    }) {
                        Icon(
                            if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            null
                        )
                    }
                    if (!isSearchExpanded) {
                        IconButton(onClick = { showExitConfirmation = true }) {
                            Icon(Icons.Default.Close, "Zatvori")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreating = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, "Novi POI")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(filteredPois, key = { it.id }) { poi ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(poi.name, style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Tip: ${poi.categoryName}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    currentLocation?.let {
                                        onLocationUpdate(
                                            poi.copy(
                                                latitude = it.latitude,
                                                longitude = it.longitude
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF4CAF50
                                    )
                                )
                            ) {
                                Text("Popravi GPS", fontSize = 10.sp, maxLines = 1)
                            }
                            Button(
                                onClick = { editingPoi = poi },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text("Izmeni", fontSize = 10.sp, maxLines = 1)
                            }
                            Button(
                                onClick = { editingDescriptionPoi = poi },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF673AB7
                                    )
                                )
                            ) {
                                Text("Opis", fontSize = 10.sp, maxLines = 1)
                            }
                            IconButton(
                                onClick = { poiToDelete = poi },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Delete, "Obriši", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
        if (isCreating) {
            PoiFormDialog(
                poi = null,
                categories = categories,
                currentLocation = currentLocation,
                onDismiss = { isCreating = false },
                onSave = {
                    onCreate(it)
                    isCreating = false
                }
            )
        }
        if (editingPoi != null) {
            PoiFormDialog(
                poi = editingPoi,
                categories = categories,
                currentLocation = currentLocation,
                onDismiss = { editingPoi = null },
                onSave = {
                    onFullUpdate(it)
                    editingPoi = null
                }
            )
        }
        if (poiToDelete != null) {
            AlertDialog(
                onDismissRequest = { poiToDelete = null },
                title = { Text("Potvrda brisanja") },
                text = { Text("Da li ste sigurni da želite da obrišete \"${poiToDelete?.name}\"?") },
                confirmButton = {
                    Button(
                        onClick = {
                            poiToDelete?.let { onDelete(it.id) }
                            poiToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("Obriši") }
                },
                dismissButton = {
                    TextButton(onClick = { poiToDelete = null }) { Text("Otkaži") }
                }
            )
        }
        if (showExitConfirmation) {
            AlertDialog(
                onDismissRequest = { showExitConfirmation = false },
                title = { Text("Zatvori Admin Panel?") },
                text = { Text("Vratićete se na AR prikaz.") },
                confirmButton = {
                    Button(onClick = {
                        showExitConfirmation = false
                        onClose()
                    }) { Text("Zatvori") }
                },
                dismissButton = {
                    TextButton(onClick = { showExitConfirmation = false }) { Text("Otkaži") }
                }
            )
        }
        if (editingDescriptionPoi != null) {
            DescriptionEditDialog(
                poi = editingDescriptionPoi!!,
                onDismiss = { editingDescriptionPoi = null },
                onSave = { id, newText ->
                    editingDescriptionPoi = null
                    onDescriptionUpdate(id, newText)
                }
            )
        }
    }
}
//endregion
//region IZMENA OPISA
@Composable
fun DescriptionEditDialog(
    poi: PoiDto,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var text by remember { mutableStateOf("Učitavam...") }
    LaunchedEffect(poi.id) {
        try {
            val response = RetrofitInstance.api.getPoiDescription(poi.id)
            text = response.string()
        } catch (_: Exception) {
            text = ""
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Uredi dugački opis") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().height(200.dp),
                label = { Text("Fajl: ${poi.name}.txt") }
            )
        },
        confirmButton = {
            Button(onClick = {
                onSave(poi.id, text)
            }) {
                Text("Sačuvaj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Otkaži") }
        }
    )
}
//endregion
//region DODAVANJA/AZURIRANJE POI
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PoiFormDialog(
    poi: PoiDto?,
    categories: List<CategoryDto>,
    currentLocation: Location?,
    onDismiss: () -> Unit,
    onSave: (PoiDto) -> Unit
) {
    var name by remember { mutableStateOf(poi?.name ?: "") }
    var selectedCategoryId by remember {
        mutableIntStateOf(poi?.categoryId ?: categories.firstOrNull()?.id ?: 1)
    }
    var categoryName by remember {
        mutableStateOf(poi?.categoryName ?: categories.firstOrNull()?.name ?: "Fakultet")
    }
    var webUrl by remember { mutableStateOf(poi?.websiteUrl ?: "") }
    var latText by remember { mutableStateOf(poi?.latitude?.toString() ?: "0.0") }
    var lngText by remember { mutableStateOf(poi?.longitude?.toString() ?: "0.0") }
    val accuracy = currentLocation?.accuracy ?: 0f
    val (color, label) = when {
        accuracy == 0f -> Color.Gray to "Nema signala"
        accuracy < 5f -> Color.Green to "Stabilan GPS"
        accuracy < 15f -> Color.Yellow to "Srednje stabilan GPS"
        else -> Color.Red to "Nestabilan GPS"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val finalId = if (poi?.id.isNullOrBlank()) "00000000-0000-0000-0000-000000000000" else poi.id
                val finalPoi = PoiDto(
                    id = finalId,
                    name = name,
                    categoryId = selectedCategoryId,
                    categoryName = categoryName,
                    latitude = latText.toDoubleOrNull() ?: 0.0,
                    longitude = lngText.toDoubleOrNull() ?: 0.0,
                    description = "",
                    websiteUrl = webUrl,
                    modelFileName = "",
                    iconName = ""
                )
                onSave(finalPoi)
            }) {
                Text("Sačuvaj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Otkaži") }
        },
        title = { Text(if (poi == null) "Novi POI" else "Izmeni POI") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Ime") })
                Text("Tip objekta:", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = {
                                selectedCategoryId = cat.id
                                categoryName = cat.name
                            },
                            label = { Text(cat.name) }
                        )
                    }
                }
                TextField(value = webUrl, onValueChange = { webUrl = it }, label = { Text("Website URL") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text("GPS Status: $label (${accuracy.toInt()}m)", color = Color.White, fontSize = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(value = latText, onValueChange = { latText = it }, label = { Text("Lat") }, modifier = Modifier.weight(1f))
                    TextField(value = lngText, onValueChange = { lngText = it }, label = { Text("Lng") }, modifier = Modifier.weight(1f))
                }
                Button(onClick = {
                    currentLocation?.let {
                        latText = it.latitude.toString()
                        lngText = it.longitude.toString()
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Moja lokacija")
                }
            }
        }
    )
}
//endregion
//region PODESAVANJA MAPE
@Composable
fun MiniMapOSM(
    currentLocation: Location?,
    poiList: List<PoiDto>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val poiMarkers = remember { mutableMapOf<String, Marker>() }
    var hasCenteredOnce by remember { mutableStateOf(false) }
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            Configuration.getInstance().userAgentValue = context.packageName
            val mReceiver = object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                    overlays.forEach { overlay ->
                        if (overlay is Marker) {
                            overlay.closeInfoWindow()
                        }
                    }
                    return true
                }
                override fun longPressHelper(p: GeoPoint?): Boolean = false
            }
            overlays.add(0, MapEventsOverlay(mReceiver))
        }
    }
    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc ->
            if (loc.accuracy < 100f) {
                val userPoint = GeoPoint(loc.latitude, loc.longitude)
                val mapCenter = mapView.mapCenter as GeoPoint
                val distance = userPoint.distanceToAsDouble(mapCenter)

                if (!hasCenteredOnce || distance > 10000.0) {
                    mapView.controller.animateTo(userPoint)
                    mapView.controller.setZoom(17.5)
                    hasCenteredOnce = true
                }
            }
        }
    }
    val userMarker = remember(mapView) {
        Marker(mapView).apply {
            title = "Ti"
            icon = androidx.core.content.ContextCompat.getDrawable(
                context,
                org.osmdroid.library.R.drawable.person
            )
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        }
    }

    LaunchedEffect(poiList, currentLocation) {
        val currentPoiIds = poiList.map { it.id }
        val idsToRemove = poiMarkers.keys.filter { it !in currentPoiIds }
        idsToRemove.forEach { id ->
            mapView.overlays.remove(poiMarkers[id])
            poiMarkers.remove(id)
        }
        poiList.forEach { poi ->
            val marker = poiMarkers.getOrPut(poi.id) {
                Marker(mapView).apply {
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(this)
                }
            }
            marker.title = poi.name
            currentLocation?.let { loc ->
                val results = FloatArray(1)
                Location.distanceBetween(
                    loc.latitude, loc.longitude,
                    poi.latitude, poi.longitude,
                    results
                )
                val distance = results[0]
                val distanceText = if (distance < 1000) {
                    "${distance.toInt()} m"
                } else {
                    "%.1f km".format(distance / 1000f)
                }
                marker.snippet = "Razdaljina: $distanceText"
            }
            val iconResId = getIconResId(poi.iconName)
            marker.icon = androidx.core.content.ContextCompat.getDrawable(context, iconResId)
            marker.position = GeoPoint(poi.latitude, poi.longitude)
        }
        mapView.invalidate()
    }
    Box(modifier = modifier) {
        AndroidView(
            factory = { mapView },
            update = { view ->
                currentLocation?.let { loc ->
                    userMarker.position = GeoPoint(loc.latitude, loc.longitude)
                    if (!view.overlays.contains(userMarker)) {
                        view.overlays.add(userMarker)
                    }
                }
                view.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )
        FloatingActionButton(
            onClick = {
                currentLocation?.let {
                    mapView.controller.animateTo(GeoPoint(it.latitude, it.longitude))
                    mapView.controller.setZoom(17.5)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .size(44.dp),
            containerColor = Color.White.copy(alpha = 0.9f),
            contentColor = Color(0xFF1A73E8),
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Center Me",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
//endregion