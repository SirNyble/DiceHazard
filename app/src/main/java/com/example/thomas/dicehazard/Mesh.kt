package com.example.thomas.dicehazard

import android.opengl.GLES20
import android.util.Log
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Mesh {
  var buffers = HashMap<String, FloatArray>()
  var triangleBuffers: MutableList<IntArray> = arrayListOf()
  var positionSourceID: String? = null

  //Vertex Shader
  private val vertexShaderCode = (
      // This matrix member variable provides a hook to manipulate
      // the coordinates of the objects that use this vertex shader
      "uniform mat4 uMVPMatrix;" +
          "attribute vec4 vPosition;" +
          "void main() {" +
          // the matrix must be included as a modifier of gl_Position
          // Note that the uMVPMatrix factor *must be first* in order
          // for the matrix multiplication product to be correct.
          "  gl_Position = uMVPMatrix * vPosition;" +
          "}")

  //Fragment Shader
  private val fragmentShaderCode = (
      "precision mediump float;" +
          "uniform vec4 vColor;" +
          "void main() {" +
          "  gl_FragColor = vColor;" +
          "}")

  //Shader variable handles
  private var mPositionHandle: Int = -1
  private var mColorHandle: Int = -1
  // Use to access and set the view transformation
  private var mMVPMatrixHandle: Int = -1

  //shader program
  private var mProgram: Int = -1

  //Buffer information
  private var vertexBuffer: FloatBuffer? = null
  private val COORDS_PER_VERTEX: Int = 3

  private var vertexCount = 0
  private var vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

  //Color
  private val color: FloatArray = floatArrayOf(1f,1f, 1f, 1.0f)


  var triangleCoords: FloatArray? = null

  init {

  }

  fun addBufferFromSource(sourceElement: Element) {
    for (i in 0 until sourceElement.childNodes.length) {
      val node = sourceElement.childNodes.item(i)
      if (node.nodeType.equals(Node.ELEMENT_NODE)) {
        val childElement = node as Element
        when (childElement.tagName) {
          "float_array" -> {
            val id = childElement.getAttribute("id")
            val bufferString = childElement.firstChild.nodeValue
            val count = childElement.getAttribute("count")
            buffers[id] = getFloatArrayFromString(bufferString, count.toInt())
          }
          "technique_common" -> {
            Log.d("Collada file element", "Technique Common")
          }
        }
      }
    }
  }

  fun addTriangleBuffersFromSource(sourceElement: Element) {
    val indicesCount = sourceElement.getAttribute("count")

    for (i in 0 until sourceElement.childNodes.length) {
      val node = sourceElement.childNodes.item(i)
      if (node.nodeType.equals(Node.ELEMENT_NODE)) {
        val childElement = node as Element
        when (childElement.tagName) {
          "p" -> {
            val bufferString = childElement.firstChild.nodeValue
            triangleBuffers.add(getIntArrayFromString(bufferString, indicesCount.toInt()))
          }
        }
      }
    }
  }

  fun setPositionSourceID(sourceElement: Element) {
    val inputElementList= sourceElement.getElementsByTagName("input")
    if(inputElementList.length == 1) {
      val inputElement = inputElementList.item(0) as Element
      this.positionSourceID = inputElement.getAttribute("source").removeRange(startIndex = 0, endIndex = 1)
      this.positionSourceID += "-array"
    }

  }

  fun updateGLBuffers() {

    var numVertices: Int = 0
    for (i in 0 until triangleBuffers.size) {
      numVertices += triangleBuffers[i].size / 3
    }
    triangleCoords = FloatArray(numVertices * 3)

    var count = 0
    for (i in 0 until triangleBuffers.size) {
      for (j in 0 until triangleBuffers[i].size step 3) {
        var buffer: FloatArray = buffers[positionSourceID]!!
        val vertIndex = triangleBuffers[i][j] * 3
        var triangleCoord: Float = buffer[ vertIndex ]
        triangleCoords!![count * 3] = triangleCoord
        triangleCoords!![count * 3 + 1] =  buffer[ vertIndex + 1 ]
        triangleCoords!![count * 3  +2] = buffer[vertIndex + 2]
        count++
      }
    }

    val rhys = true

    vertexCount = numVertices /// COORDS_PER_VERTEX

    //TODO: use appropriate opengl calls to fill the buffers
    // initialize vertex byte buffer for shape coordinates
        val bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                numVertices * 3 * 4)
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder())

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer()
        // add the coordinates to the FloatBuffer
        vertexBuffer?.put(triangleCoords!!)
        // set the buffer to read the first coordinate
        vertexBuffer?.position(0)

        val vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode)
        val fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode)

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram()

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader)

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader)

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram)
  }

  fun draw(mvpMatrix: FloatArray) {
    // Add program to OpenGL ES environment
    GLES20.glUseProgram(mProgram)

    // get handle to vertex shader's vPosition member
    mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")

    // Enable a handle to the triangle vertices
    GLES20.glEnableVertexAttribArray(mPositionHandle)

    // Prepare the triangle coordinate data
    GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
        GLES20.GL_FLOAT, false,
        vertexStride, vertexBuffer)

    // get handle to fragment shader's vColor member
    mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")

    // Set color for drawing the triangle
    GLES20.glUniform4fv(mColorHandle, 1, color, 0)

    // get handle to shape's transformation matrix
    mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")

    // Pass the projection and view transformation to the shader
    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)

    // Draw the triangle
    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

    // Disable vertex array
    GLES20.glDisableVertexAttribArray(mPositionHandle)
  }

  fun getFloatArrayFromString(floatArrString: String, count: Int): FloatArray {
    var floatArr: FloatArray = FloatArray(count)

    var tokenizedStr = floatArrString.split(" ")
    val strCount = tokenizedStr.count()
    if(strCount.equals(count)) {
      for (i in 0 until count) {
        floatArr[i] = tokenizedStr[i].toFloat()
      }
    }
    else {
      Log.d("ERROR", "Collada float array count does not match the count id value!")
      //TODO: THROW ERROR, not sure how in kotlin yet ¯\_(ツ)_/¯
    }
    return floatArr
  }

  fun getIntArrayFromString(bufferString: String, count: Int): IntArray {
    val trianglesToElements: Int = count * buffers.count() * 3
    var intArray = IntArray(trianglesToElements)

    var tokenizedStr = bufferString.split(" ")
    val strCount = tokenizedStr.count()

    if(strCount.equals(trianglesToElements)) {
      for (i in 0 until strCount) {
        intArray[i] = tokenizedStr[i].toInt()
      }
    }
    else {
      Log.d("ERROR", "Collada int array count does not match the count id value!")
      //TODO: THROW ERROR, not sure how in kotlin yet ¯\_(ツ)_/¯
    }

    return intArray
  }
}