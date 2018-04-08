package com.example.thomas.dicehazard

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.lang.Math.cos
import java.lang.Math.sin
import javax.microedition.khronos.opengles.GL10


class MyGLRenderer(context: Context) : GLSurfaceView.Renderer {

  private var mContext: Context? = null
  private var mTriangle: Triangle? = null
  private var mModel: Model? = null

  // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
  private val mMVPMatrix = FloatArray(16)
  private val mProjectionMatrix = FloatArray(16)
  private val mViewMatrix = FloatArray(16)

  private var mStartTime: Long = 0

  init {
    mContext = context
    mStartTime = System.currentTimeMillis()
  }

  override fun onSurfaceCreated(gl: GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
    GLES20.glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
    GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    mTriangle = Triangle()

    val inputStream = mContext?.assets?.open("astroBoy_walk_Max.dae")
    mModel = Model(mContext!!, inputStream!!)
  }

  override fun onDrawFrame(unused: GL10) {
    // Redraw background color
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT )


    val tEnd = System.currentTimeMillis()
    val tDelta = tEnd - mStartTime
    val elapsedSeconds = tDelta / 1000.0


    val radius = 20.0f
    val camX = sin(elapsedSeconds) * radius;
    val camZ = cos(elapsedSeconds) * radius;

    // Set the camera position (View matrix)
    Matrix.setLookAtM(mViewMatrix, 0, camX.toFloat(), 10.0f, camZ.toFloat(),
        0f, 0f, 0f, 0f, 1.0f, 0.0f)

    // Calculate the projection and view transformation
    Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)

    mTriangle?.draw(mMVPMatrix)
    mModel?.draw(mMVPMatrix)
  }

  override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
    GLES20.glViewport(0, 0, width, height)
    val ratio = width.toFloat() / height.toFloat()

    // this projection matrix is applied to object coordinates
    // in the onDrawFrame() method
    Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1.0f, 1.0f,
        3.0f, 100.0f)
  }

  companion object {
    fun loadShader(type: Int, shaderCode: String): Int {

      // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
      // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
      val shader = GLES20.glCreateShader(type)

      // add the source code to the shader and compile it
      GLES20.glShaderSource(shader, shaderCode)
      GLES20.glCompileShader(shader)

      return shader
    }
  }
}