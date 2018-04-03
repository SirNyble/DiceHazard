package com.example.thomas.dicehazard

import android.content.Context
import android.opengl.GLSurfaceView

internal class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private var mRenderer: MyGLRenderer

    init {

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        mRenderer = MyGLRenderer(context)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer)
    }
}