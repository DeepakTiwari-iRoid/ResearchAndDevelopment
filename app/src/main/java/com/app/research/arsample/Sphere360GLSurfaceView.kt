package com.app.research.arsample

import android.content.Context
import android.opengl.GLSurfaceView

class Sphere360GLSurfaceView(
    context: Context,
    val renderer: Sphere360Renderer
) : GLSurfaceView(context) {

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}