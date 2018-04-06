package com.example.thomas.dicehazard

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.opengl.GLSurfaceView


class MainActivity : AppCompatActivity() {

  private var mGLView: GLSurfaceView? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    //setContentView(R.layout.activity_main)

    // Create a GLSurfaceView instance and set it
    // as the ContentView for this Activity.
    mGLView = MyGLSurfaceView(this)
    setContentView(mGLView)
  }
}
