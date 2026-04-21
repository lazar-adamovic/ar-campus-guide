@file:Suppress("SpellCheckingInspection")
package com.example.projekat.ar

import com.google.ar.sceneform.math.Vector3
import kotlin.math.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.rendering.ModelRenderable
import java.lang.Math.toRadians
import androidx.core.net.toUri
import com.example.projekat.data.PoiDto
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.assets.RenderableSource

//region RACUNANJE PRAVCA
fun bearingToPOI(userLat: Double, userLon: Double, poiLat: Double, poiLon: Double): Double {
    val lat1 = toRadians(userLat)
    val lat2 = toRadians(poiLat)
    val dLon = toRadians(poiLon - userLon)

    val y = sin(dLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)

    return (Math.toDegrees(atan2(y, x)) + 360) % 360
}
//endregion
//region RACUNANJE POZICIJE MARKERA
fun calculateMarkerPosition(
    referenceAzimut: Float,
    poiBearing: Double,
    distanceMeters: Float
): Pair<Vector3, Float> {
    var relativeAngle = (poiBearing - referenceAzimut).toFloat()

    if (relativeAngle > 180) {
        relativeAngle -= 360
    } else if (relativeAngle < -180) {
        relativeAngle += 360
    }

    val angleToRadians = toRadians(relativeAngle.toDouble())
    val x = sin(angleToRadians) * distanceMeters
    val z = -cos(angleToRadians) * distanceMeters
    val y = -0.5f
    val position = Vector3(x.toFloat(), y, z.toFloat())

    return position to 0.8f
}
//endregion
//region UCITAVANJE MODELA I DODAVANJE MARKERA
var isProcessingModel = false
val renderablesCache = mutableMapOf<String, ModelRenderable>()
private val handler = Handler(Looper.getMainLooper())
fun addModel(
    context: Context,
    arSceneView: ArSceneView,
    modelFileName: String,
    onNodeCreated: (Node) -> Unit,
    onError: (Throwable) -> Unit
) {
    val frame = arSceneView.arFrame ?: run {
        onError(Exception("Frame is null"))
        return
    }
    if (frame.camera.trackingState != TrackingState.TRACKING) {
        onError(Exception("Camera not tracking"))
        return
    }
    val existingRenderable = renderablesCache[modelFileName]
    if (existingRenderable != null) {
        val node = Node().apply {
            renderable = existingRenderable.makeCopy()
        }
        onNodeCreated(node)
        return
    }
    val modelUri = "models/$modelFileName".toUri()
    val renderableSource = RenderableSource.builder()
        .setSource(context, modelUri, RenderableSource.SourceType.GLB)
        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
        .build()
    ModelRenderable.builder()
        .setSource(context, renderableSource)
        .setRegistryId(modelFileName)
        .build()
        .thenAccept { renderable ->
            renderablesCache[modelFileName] = renderable
            val node = Node().apply {
                this.renderable = renderable.makeCopy()
            }
            onNodeCreated(node)
        }
        .exceptionally { throwable ->
            onError(throwable)
            null
        }
}
fun addMarker(
    context: Context,
    arSceneView: ArSceneView,
    worldAnchorNode: AnchorNode,
    poi: PoiDto,
    position: Vector3,
    scale: Float,
    markerNodes: MutableMap<String, Node>,
    loadingMarkers: MutableSet<String>
) {
    if (arSceneView.arFrame?.camera?.trackingState != TrackingState.TRACKING) return
    if (markerNodes.containsKey(poi.id)) return
    if (loadingMarkers.contains(poi.id)) return

    loadingMarkers.add(poi.id)
    isProcessingModel = true

    val safetyTimeout = Runnable {
        if (loadingMarkers.contains(poi.id)) {
            loadingMarkers.remove(poi.id)
            isProcessingModel = false
        }
    }
    handler.postDelayed(safetyTimeout, 10000)

    addModel(
        context = context,
        arSceneView = arSceneView,
        modelFileName = poi.modelFileName,
        onNodeCreated = { node ->
            handler.removeCallbacks(safetyTimeout)
            node.setParent(worldAnchorNode)
            node.localPosition = position
            node.localScale = Vector3(scale, scale, scale)
            node.worldRotation = arSceneView.scene.camera.worldRotation
            markerNodes[poi.id] = node
            node.name = poi.modelFileName
            loadingMarkers.remove(poi.id)
            handler.postDelayed({
                isProcessingModel = false
            }, 500)
        },
        onError = { throwable ->
            handler.removeCallbacks(safetyTimeout)
            loadingMarkers.remove(poi.id)
            isProcessingModel = false
        }
    )
}
//endregion