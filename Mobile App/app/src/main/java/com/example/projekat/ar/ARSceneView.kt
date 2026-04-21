@file:Suppress("SpellCheckingInspection")
package com.example.projekat.ar
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.projekat.data.PoiDto
import com.example.projekat.data.RetrofitInstance
import com.google.ar.core.ArCoreApk
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import kotlinx.coroutines.delay
import com.example.projekat.orientation.OrientationViewModel
import com.example.projekat.location.LocationViewModel
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.example.projekat.data.CategoryDto
import com.example.projekat.data.SimplePasswordTransformation
import com.example.projekat.data.UpdateDescriptionDto
import com.example.projekat.data.VerifyAdminCommand
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.Node
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARSceneScreen(
    locationViewModel: LocationViewModel,
    orientationViewModel: OrientationViewModel,
    modifier: Modifier = Modifier
) {
    //region VARIJABLE
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var secretClickCount by remember { mutableIntStateOf(0) }
    var showAdminPanel by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableLongStateOf(0L) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    val arSceneView = remember { ArSceneView(context) }
    val markerNodes = remember { mutableMapOf<String, Node>() }
    var worldAnchorNode by remember { mutableStateOf<AnchorNode?>(null) }
    var selectedPoi by remember { mutableStateOf<PoiDto?>(null) }
    val loadingMarkers = remember { mutableSetOf<String>() }
    var poiList by remember { mutableStateOf<List<PoiDto>>(emptyList()) }
    var isInitializing by remember { mutableStateOf(true) }
    var isArSessionReady by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf("Sve") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var referenceAzimut by remember { mutableStateOf<Float?>(null) }
    var isMapVisible by remember { mutableStateOf(false) }
    var isMapExpanded by remember { mutableStateOf(false) }
    val mapWidth by animateDpAsState(targetValue = if (isMapExpanded) 320.dp else 120.dp)
    val mapHeight by animateDpAsState(targetValue = if (isMapExpanded) 400.dp else 120.dp)
    var showDetails by remember { mutableStateOf(false) }
    var fullDescription by remember { mutableStateOf("Učitavam...") }
    var categories by remember { mutableStateOf<List<CategoryDto>>(emptyList()) }
    var gpsToastShown by remember { mutableStateOf(false) }
    val location by locationViewModel.location.collectAsState()
    val azimut by orientationViewModel.azimut.collectAsState()
    val isVertical by orientationViewModel.isVertical.collectAsState()
    val camera = arSceneView.scene.camera
    camera.farClipPlane = 70.0f
    camera.nearClipPlane = 0.1f
    //endregion
    //region AR CORE SETUP
    LaunchedEffect(isInitializing) {
        if (!isInitializing) {
            try {
                val availability = ArCoreApk.getInstance().checkAvailability(context)
                if (availability.isSupported) {
                    val installStatus = ArCoreApk.getInstance().requestInstall(context as Activity, false)
                    if (installStatus == ArCoreApk.InstallStatus.INSTALLED) {
                        setupARCore(context, arSceneView)
                        arSceneView.resume()
                        isArSessionReady = true
                    }
                }
            } catch (e: Exception) {
                Log.e("AR_DEBUG", "AR Error: ${e.message}")
            }
        }
    }
    //endregion
    //region UCITAVANJE PODATAKA
    fun refreshPois() {
        scope.launch {
            errorMessage = null
            try {
                withContext(Dispatchers.Main) {
                    markerNodes.values.forEach { it.setParent(null) }
                    markerNodes.clear()
                    loadingMarkers.clear()
                    isProcessingModel = false
                }
                val response = RetrofitInstance.api.getAllPois()
                if (response.isSuccessful && response.body() != null) {
                    val newPois = response.body()!!.sortedBy { it.categoryId }
                    delay(400)
                    poiList = newPois
                } else {
                    errorMessage = "Greška na serveru: ${response.code()}"
                }
                val categoriesResponse = RetrofitInstance.api.getCategories()
                categories = categoriesResponse.sortedBy { it.id }
            } catch (_: Exception) {
                errorMessage = "Problem sa mrežom."
            }
        }
    }
    //endregion
    //region UCITAVANJE OPISA
    LaunchedEffect(showDetails, selectedPoi) {
        if (showDetails && selectedPoi != null) {
            fullDescription = "Učitavam..."
            try {
                val body = RetrofitInstance.api.getPoiDescription(selectedPoi!!.id)
                fullDescription = body.string()
            } catch (_: Exception) {
                fullDescription = "Opis trenutno nije dostupan."
            }
        }
    }
    //endregion
    //region INICIJALIZACIJA
    LaunchedEffect(Unit) {
        locationViewModel.startLocationUpdates(context)
        orientationViewModel.start(context)
        refreshPois()
        delay(4000)
        isInitializing = false
    }
    //endregion
    //region KREIRANJE GLOBALNOG SIDRA
    LaunchedEffect(isArSessionReady) {
        if (!isArSessionReady) return@LaunchedEffect

        if (worldAnchorNode == null) {
            val session = arSceneView.session ?: return@LaunchedEffect
            var retries = 0
            var anchor: Anchor? = null
            while (retries < 10) {
                val frame = arSceneView.arFrame
                val trackingState = frame?.camera?.trackingState
                if (trackingState == TrackingState.TRACKING) {
                    anchor = session.createAnchor(Pose.IDENTITY)
                    break
                }
                retries++
                delay(200)
            }
            worldAnchorNode = AnchorNode(anchor).apply {
                setParent(arSceneView.scene)
            }
        }
    }
    //endregion
    //region DISPOSE
    DisposableEffect(arSceneView) {
        onDispose {
            locationViewModel.stopLocationUpdates()
            orientationViewModel.stop()
            arSceneView.destroy()
        }
    }
    //endregion
    //region LIFECYCLE I TOUCHLISTENER
    DisposableEffect(arSceneView, lifecycleOwner) {
        val scene = arSceneView.scene
        val touchListener = Scene.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val touchX = event.x
                val touchY = event.y
                val hitRadius = 200
                var foundPoi: PoiDto? = null
                markerNodes.forEach { (poiId, node) ->
                    val screenPos = scene.camera.worldToScreenPoint(node.worldPosition)
                    val dx = touchX - screenPos.x
                    val dy = touchY - screenPos.y
                    if (dx.absoluteValue < hitRadius && dy.absoluteValue < hitRadius) {
                        foundPoi = poiList.find { it.id == poiId }
                        return@forEach
                    }
                }
                selectedPoi = foundPoi
                return@OnTouchListener true
            }
            false
        }
        scene.setOnTouchListener(touchListener)
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                try {
                    arSceneView.resume()
                    arSceneView.session?.resume()
                } catch (e: Exception) {
                    Log.e("AR_DEBUG", "onResume error: ${e.message}")
                }
            }
            override fun onPause(owner: LifecycleOwner) {
                try {
                    arSceneView.session?.pause()
                    arSceneView.pause()
                } catch (e: Exception) {
                    Log.e("AR_DEBUG", "onPause error: ${e.message}")
                }
            }

            override fun onDestroy(owner: LifecycleOwner) {
                arSceneView.destroy()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            scene.setOnTouchListener(null)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    //endregion
    //region FIKSIRANJE AZIMUTA
    LaunchedEffect(isInitializing) {
        if (!isInitializing && referenceAzimut == null) {
            referenceAzimut = azimut
            //Toast.makeText(context, "Sever postavljen na: ${azimut.toInt()}°", Toast.LENGTH_LONG).show()
        }
    }
    //endregion
    //region BRISANJE HUD KAD SE PROMENI FILTER
    LaunchedEffect(selectedType) {
        selectedPoi?.let { poi ->
            if (selectedType != "Sve" && poi.categoryName != selectedType) {
                selectedPoi = null
            }
        }
    }
    //endregion
    //region MARKER LOGIKA
    LaunchedEffect(azimut,location, isVertical, isArSessionReady, selectedType, poiList, showAdminPanel) {
        // 1. Osnovne provere
        if (showAdminPanel || !isArSessionReady || !isVertical || location == null) return@LaunchedEffect
        val worldAnchor = worldAnchorNode ?: return@LaunchedEffect
        val currentLoc = location!!
        val camera = arSceneView.scene.camera
        val gpsAccuracy = currentLoc.accuracy
        val cameraPos = camera.localPosition
        val cameraRotation = camera.worldRotation
        val fixedAzimut = referenceAzimut ?: azimut
        if (gpsAccuracy > 8.0f) {
            if (!gpsToastShown) {
                Toast.makeText(context, "Slab GPS signal (${"%.1f".format(gpsAccuracy)}m)", Toast.LENGTH_SHORT).show()
                gpsToastShown = true
            }
        } else {
            gpsToastShown = false
        }
        poiList.forEach { poi ->
            val results = FloatArray(1)
            Location.distanceBetween(currentLoc.latitude, currentLoc.longitude, poi.latitude, poi.longitude, results)
            val distance = results[0]

            val existingNode = markerNodes[poi.id]
            val matchesFilter = selectedType == "Sve" || poi.categoryName == selectedType
            if (distance < 50f) {
                val bearing = bearingToPOI(currentLoc.latitude, currentLoc.longitude, poi.latitude, poi.longitude)
                val (targetPos, _) = calculateMarkerPosition(fixedAzimut, bearing, distance)
                val absoluteTargetPos = Vector3.add(cameraPos, targetPos)
                val scale = (0.5f + (distance / 100f) * 1.5f).coerceIn(0.5f, 2.0f)
                if (existingNode == null) {
                    addMarker(
                        context = context,
                        arSceneView = arSceneView,
                        worldAnchorNode = worldAnchor,
                        poi = poi,
                        position = absoluteTargetPos,
                        scale = scale,
                        markerNodes = markerNodes,
                        loadingMarkers = loadingMarkers
                    )
                } else {
                    val wasDisabled = !existingNode.isEnabled
                    val shouldBeEnabled = (distance < 20f) && matchesFilter
                    existingNode.isEnabled = shouldBeEnabled
                    if (existingNode.isEnabled) {
                        val currentMarkerPos = existingNode.localPosition
                        val diff = Vector3.subtract(absoluteTargetPos, currentMarkerPos).length()
                        if (wasDisabled || diff > 5.0f) {
                            existingNode.localPosition = absoluteTargetPos
                        } else {
                            existingNode.localPosition = Vector3.lerp(currentMarkerPos, absoluteTargetPos, 0.15f)
                        }
                        existingNode.localScale = Vector3(scale, scale, scale)
                        existingNode.worldRotation = cameraRotation
                    }
                }
            } else {
                existingNode?.isEnabled = false
                if (selectedPoi?.id == poi.id) selectedPoi = null
            }
        }
    }
    //endregion
    //region UI LAYOUT
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(factory = { arSceneView }, modifier = Modifier.fillMaxSize())
        if (!isInitializing && !isVertical) VerticalPhoneOverlay()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 16.dp, start = 20.dp, end = 20.dp)
        ) {
            //region LOGIKA TAJNOG KLIKA
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastClickTime > 2000) {
                            secretClickCount = 1
                        } else {
                            secretClickCount++
                        }
                        lastClickTime = currentTime

                        if (secretClickCount >= 5) {
                            showPasswordDialog = true
                            secretClickCount = 0
                        }
                    }
            ) {
                Text(
                    text = "AR",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = "VODIČ",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 2.dp)
                )
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .height(20.dp)
                        .width(1.dp)
                        .background(Color.White.copy(alpha = 0.3f))
                )
            }
            //endregion
            //region FILTER
            if (!isInitializing && !showAdminPanel) {
                CategoryFilterBar(
                    poiList = poiList,
                    selectedType = selectedType,
                    onTypeSelected = { selectedType = it }
                )
            }
            //endregion
        }
        //region MAPA
        val filteredPois = remember(poiList, selectedType) {
            if (selectedType == "Sve") poiList
            else poiList.filter { it.categoryName == selectedType }
        }
        if (isMapExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { isMapExpanded = false })
                    }
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            AnimatedContent(
                targetState = isMapVisible,
                transitionSpec = {
                    if (targetState) {
                        (fadeIn(tween(200)) + scaleIn(initialScale = 0.8f)).togetherWith(fadeOut(tween(100)))
                    } else {
                        fadeIn(tween(300, delayMillis = 300)).togetherWith(
                            fadeOut(tween(300)) + scaleOut(targetScale = 0.8f)
                        )
                    }
                }
            ) { visible ->
                if (visible) {
                    Box(
                        modifier = Modifier.pointerInput(Unit) { detectTapGestures {} }
                    ) {
                        Card(
                            modifier = Modifier.size(width = mapWidth, height = mapHeight),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                MiniMapOSM(
                                    currentLocation = location,
                                    poiList = filteredPois,
                                    modifier = Modifier.fillMaxSize()
                                )

                                IconButton(
                                    onClick = { isMapExpanded = !isMapExpanded },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                        .size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isMapExpanded) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { isMapVisible = false },
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                        .size(32.dp)
                                ) {
                                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                } else {
                    FloatingActionButton(
                        onClick = { isMapVisible = true },
                        containerColor = Color.Black.copy(alpha = 0.7f),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "MAPA",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = Color.White,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    }
                }
            }
        }
        //endregion
        //region PROVERA SIFRE ZA ADMINPANEL
        if (showPasswordDialog) {
            AlertDialog(
                onDismissRequest = {
                    showPasswordDialog = false
                    passwordInput = ""
                },
                title = { Text("Admin Pristup") },
                text = {
                    TextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Unesite lozinku") },
                        visualTransformation = SimplePasswordTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val response = RetrofitInstance.api.verifyAdmin(
                                        VerifyAdminCommand(passwordInput)
                                    )
                                    if (response.isSuccessful) {
                                        showAdminPanel = true
                                        showPasswordDialog = false
                                        passwordInput = ""
                                        Toast.makeText(context, "Pristup odobren", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Pogrešna lozinka!", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Server nedostupan", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Text("Potvrdi")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showPasswordDialog = false
                        passwordInput = ""
                    }) {
                        Text("Otkaži")
                    }
                }
            )
        }
        //endregion
        //region HUD
        selectedPoi?.let { poi ->
            SelectedPoiHud(
                poi = poi,
                location = location,
                currentAzimut = azimut,
                onOpenDetails = {
                    showDetails = true },
                onClose = {
                    selectedPoi = null
                    showDetails = false
                }
            )
        }
        //endregion
        //region DUGACAK OPIS
        if (showDetails && selectedPoi != null) {
            ModalBottomSheet(
                onDismissRequest = { showDetails = false },
                containerColor = Color(0xFF121212),
                scrimColor = Color.Black.copy(alpha = 0.6f),
            ) {
                val context = LocalContext.current
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(selectedPoi!!.name, fontSize = 24.sp, color = Color.Cyan, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = fullDescription,
                        color = Color.White,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.padding(16.dp)
                    )
                    if (!selectedPoi!!.websiteUrl.isNullOrBlank()) {
                        Spacer(Modifier.height(30.dp))
                        Button(
                            onClick = {
                                val intent =
                                    Intent(Intent.ACTION_VIEW, selectedPoi!!.websiteUrl!!.toUri())
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("POSETITE VEBSAJT", color = Color.White)
                        }
                    }
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
        //endregion
        //region ADMIN PANEL
        if (showAdminPanel) {
            key(poiList.size) {
                AdminPanelScreen(
                    categories = categories,
                    poiList = poiList,
                    currentLocation = location,
                    onFullUpdate = { updatedPoi ->
                        scope.launch {
                            markerNodes[updatedPoi.id]?.setParent(null)
                            markerNodes.remove(updatedPoi.id)
                            val res = RetrofitInstance.api.updatePoi(updatedPoi.id, updatedPoi)
                            if (res.isSuccessful) {
                                Toast.makeText(context, "Ažurirano!", Toast.LENGTH_SHORT).show()
                                refreshPois()
                            }
                        }
                    },
                    onCreate = { newPoi ->
                        scope.launch {
                            val res = RetrofitInstance.api.createPoi(newPoi)
                            if (res.isSuccessful) {
                                Toast.makeText(context, "Kreirano!", Toast.LENGTH_SHORT).show()
                                refreshPois()
                            }
                        }
                    },
                    onDelete = { id ->
                        scope.launch {
                            try {
                                markerNodes[id]?.setParent(null)
                                markerNodes.remove(id)
                                val res = RetrofitInstance.api.deletePoi(id)
                                if (res.isSuccessful) {
                                    Toast.makeText(context, "POI obrisan", Toast.LENGTH_SHORT).show()
                                    refreshPois()
                                }
                            } catch (e: Exception) {
                                Log.e("ADMIN", "Delete error: ${e.message}")
                            }
                        }
                    },
                    onLocationUpdate = { updatedPoiLocation ->
                        scope.launch {
                            try {
                                markerNodes[updatedPoiLocation.id]?.setParent(null)
                                markerNodes.remove(updatedPoiLocation.id)
                                val res = RetrofitInstance.api.updatePoiLocation(updatedPoiLocation.id, updatedPoiLocation)
                                if(res.isSuccessful) {
                                    Toast.makeText(context, "Uspešno ažurirana lokacija!", Toast.LENGTH_SHORT).show()
                                    refreshPois()
                                }
                            } catch (e: Exception) {
                                Log.e("ADMIN", "Update error: ${e.message}")
                            }
                        }
                    },
                    onDescriptionUpdate = { id, content ->
                        scope.launch {
                            try {
                                val dto = UpdateDescriptionDto(content = content)
                                val res = RetrofitInstance.api.updatePoiDescription(id, dto)
                                if (res.isSuccessful) {
                                    Toast.makeText(context, "Uspešno ažuriran opis!", Toast.LENGTH_SHORT).show()
                                } else {
                                    val errorMsg = res.errorBody()?.string() ?: "Nepoznata greška"
                                    Log.e("ADMIN", "Server Error ($id): $errorMsg")
                                }
                            } catch (e: Exception) {
                                Log.e("ADMIN", "Android Exception: ${e.stackTraceToString()}")
                            }
                        }
                    },
                    onClose = { showAdminPanel = false }
                )
            }
        }
        //endregion
        //region GRESKA INTERNET/BAZA
        if (errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding(),
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp
                    )
                    TextButton(
                        onClick = {
                            refreshPois()
                        }
                    ) {
                        Text("POKUŠAJ PONOVO", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        //endregion
    }
    //region KALIBRACIJA SENZORA
    if (isInitializing) CalibrationOverlay()
    //endregion
    //endregion
}