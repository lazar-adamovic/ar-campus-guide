package com.example.projekat.ar

import android.content.Context
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ArSceneView

fun setupARCore(context: Context, arSceneView: ArSceneView) {
    val session = Session(context)

    val config = Config(session).apply {
        planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
        updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        lightEstimationMode = Config.LightEstimationMode.AMBIENT_INTENSITY
        focusMode = Config.FocusMode.AUTO
    }

    session.configure(config)

    arSceneView.apply {
        setupSession(session)
        planeRenderer.isVisible = true
        planeRenderer.isEnabled = true
        scene.addOnUpdateListener { frameTime ->
            val frame = arFrame ?: return@addOnUpdateListener
            val pointCloud = frame.acquirePointCloud()
            pointCloud.release()
        }
    }
}

