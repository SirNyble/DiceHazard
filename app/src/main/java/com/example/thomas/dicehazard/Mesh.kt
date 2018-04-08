package com.example.thomas.dicehazard

import android.opengl.GLES20
import android.opengl.Matrix
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
          "uniform mat4 uModelMatrix;" +
          "attribute vec4 vPosition;" +
          "attribute vec3 vNormal;" +
          "attribute vec3 vTextureCoord;" +
          "varying vec2 textureCoord;" +
          "void main() {" +
          // the matrix must be included as a modifier of gl_Position
          // Note that the uMVPMatrix factor *must be first* in order
          // for the matrix multiplication product to be correct.
          "  textureCoord = vTextureCoord.xy;" +
          "  gl_Position =  uMVPMatrix * uModelMatrix *  vPosition;" +
          "}")

  //Fragment Shader
  private val fragmentShaderCode = (
      "precision mediump float;" +
          "varying vec2 textureCoord;" +
          "uniform sampler2D textureImage; " +
          "uniform vec4 vColor;" +
          "void main() {" +
          "  vec2 flipped_texcoord = vec2(textureCoord.x, 1.0 - textureCoord.y); " +
          "  gl_FragColor = texture2D(textureImage, flipped_texcoord);" +
          "}")

  //Shader variable handles
  private var mPositionHandle: Int = -1
  private var mNormalHandle: Int = -1
  private var mTextureCoordHandle: Int = -1

  private var mTextureUniformHandle: Int = -1
  private var mTextureDataHandle: Int = -1

  private var mColorHandle: Int = -1
  // Use to access and set the view transformation
  private var mMVPMatrixHandle: Int = -1

  //shader program
  private var mProgram: Int = -1

  //Buffer information
  private var vertexBuffer: FloatBuffer? = null
  private val COORDS_PER_VERTEX: Int = 9

  private var vertexCount = 0
  private var vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

  //Color
  private val color: FloatArray = floatArrayOf(1f, 1f, 1f, 1.0f)

  private var mModelMatrixHandle: Int = -1
  private val mModelMatrix: FloatArray = FloatArray(16)

  var triangleCoords: FloatArray? = null

  init { }

  fun addBufferFromSource(sourceElement: Element) {
    for (i in 0 until sourceElement.childNodes.length) {
      val node = sourceElement.childNodes.item(i)
      if (node.nodeType == Node.ELEMENT_NODE) {
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
      if (node.nodeType == Node.ELEMENT_NODE) {
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
    val inputElementList = sourceElement.getElementsByTagName("input")
    if (inputElementList.length == 1) {
      val inputElement = inputElementList.item(0) as Element
      this.positionSourceID = inputElement.getAttribute("source").removeRange(startIndex = 0, endIndex = 1)
      this.positionSourceID += "-array"
    }
  }

  fun setTextureHandle(textureHandle: Int) {
    mTextureDataHandle = textureHandle
  }

  fun update() {
    //First we need to update the number of vertices
    updateVertexCount()
    //Now that we have the vertex count we can fill the triangle float array
    updateTriangleCoords()
    //Update GL Buffers with the updated triangl float array
    updateGLBuffers()
  }

  private fun updateVertexCount() {
    //Right now make assumption triangle has position/normal/texture so every third element is
    //position data
    vertexCount = 0
    for (i in 0 until triangleBuffers.size) {
      vertexCount += triangleBuffers[i].size / 3
    }

  }

  private fun updateTriangleCoords() {
    //Each triangle has X,Y,Z so multiply the number of position vertices by 3
    triangleCoords = FloatArray(vertexCount * 9)

    //triangleBuffers contains the INDEX of the position in the floatbuffer buffers var
    //each triangle has 3 elements (XYZ), Normal, Texture so we step by 3 to only get XYZ for now
    //TODO: Get and fill the Normal and Texture
    val positionBuffer: FloatArray = buffers[positionSourceID]!!
    val normalBuffer: FloatArray = buffers["boyShape-skin-normals-array"]!!
    val textureBuffer: FloatArray = buffers["boyShape-skin-map-channel1-array"]!!
    var currentTriangle = 0
    for (i in 0 until triangleBuffers.size) {
      for (j in 0 until triangleBuffers[i].size step 3) {
        //We have to multiply the index in triangleBuffers by 3 because each element is XYZ data
        val vertIndex = triangleBuffers[i][j] * 3
        val normalIndex: Int = triangleBuffers[i][j + 1] * 3
        val textureIndex: Int = triangleBuffers[i][j + 2] * 3

        //Set the XYZ data now that we have the base float element which is the X Val.
        //To get Y and Z elements we just add by offset
        triangleCoords!![currentTriangle * 9] = positionBuffer[vertIndex]
        triangleCoords!![currentTriangle * 9 + 1] = positionBuffer[vertIndex + 1]
        triangleCoords!![currentTriangle * 9 + 2] = positionBuffer[vertIndex + 2]

        //normals
        triangleCoords!![currentTriangle * 9 + 3] = normalBuffer[normalIndex]
        triangleCoords!![currentTriangle * 9 + 4] = normalBuffer[normalIndex + 1]
        triangleCoords!![currentTriangle * 9 + 5] = normalBuffer[normalIndex + 2]

        //texture
        triangleCoords!![currentTriangle * 9 + 6] = textureBuffer[textureIndex]
        triangleCoords!![currentTriangle * 9 + 7] = textureBuffer[textureIndex + 1]
        triangleCoords!![currentTriangle * 9 + 8] = textureBuffer[textureIndex + 2]
        currentTriangle++
      }
    }
  }

  private fun updateGLBuffers() {
    val bb = ByteBuffer.allocateDirect(
        // (number of coordinate values * 3 elements per vertex (XYZ) * 4 bytes per float)
        vertexCount * 9 * 4)
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

    GLES20.glBindAttribLocation(mProgram, 0, "vPosition");
    GLES20.glBindAttribLocation(mProgram, 1, "vNormal");

    // creates OpenGL ES program executables
    GLES20.glLinkProgram(mProgram)
  }

  fun draw(mvpMatrix: FloatArray) {
    // Add program to OpenGL ES environment
    GLES20.glUseProgram(mProgram)

    // get handle to vertex shader's vPosition member
    mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
    mNormalHandle = GLES20.glGetAttribLocation(mProgram, "vNormal")
    mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "vTextureCoord")

   //GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, sizeof(gCubeVertexData), gCubeVertexData, GL_STATIC_DRAW);
    // Enable a handle to the triangle vertices
    vertexBuffer?.position(0)
    GLES20.glEnableVertexAttribArray(mPositionHandle)

    // Prepare the triangle coordinate data
    GLES20.glVertexAttribPointer(mPositionHandle, 3,
        GLES20.GL_FLOAT, false,
        vertexStride, vertexBuffer)

    if(mNormalHandle > 0) {
      // Enable a handle to the triangle normals
      vertexBuffer?.position(3)
      GLES20.glEnableVertexAttribArray(mNormalHandle)

      // Prepare the triangle coordinate data
      GLES20.glVertexAttribPointer(mNormalHandle, 3,
          GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)
    }

    if(mTextureCoordHandle > 0) {
      // Enable a handle to the triangle normals
      vertexBuffer?.position(6)
      GLES20.glEnableVertexAttribArray(mTextureCoordHandle)

      // Prepare the triangle coordinate data
      GLES20.glVertexAttribPointer(mTextureCoordHandle, 3,
          GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)
    }

    mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "textureImage");

    // Set the active texture unit to texture unit 0.
    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

    // Bind the texture to this unit.
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

    // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
    GLES20.glUniform1i(mTextureUniformHandle, 0);

//    // Enable a handle to the triangle normals
//    GLES20.glEnableVertexAttribArray(mTextureCoordHandle)
//
//    // Prepare the triangle coordinate data
//    GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX,
//        GLES20.GL_FLOAT, false, vertexStride, 24)

    // get handle to fragment shader's vColor member
//    mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
//
//    // Set color for drawing the triangle
//    GLES20.glUniform4fv(mColorHandle, 1, color, 0)

    // get handle to shape's transformation matrix
    mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")

    // Pass the projection and view transformation to the shader
    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)

    mModelMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uModelMatrix")
    Matrix.setIdentityM(mModelMatrix, 0)
    Matrix.rotateM(mModelMatrix, 0, -90f, 1f, 0f, 0f)
    GLES20.glUniformMatrix4fv(mModelMatrixHandle, 1, false, mModelMatrix, 0)

    // Draw the triangle
    try {
      GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
    }
    catch(e: Exception) {
      Log.d("ERROR", e.toString())
    }


    // Disable vertex array
    GLES20.glDisableVertexAttribArray(mPositionHandle)
    if(mNormalHandle > 0)
      GLES20.glDisableVertexAttribArray(mNormalHandle)
    if(mTextureCoordHandle > 0)
      GLES20.glDisableVertexAttribArray(mTextureCoordHandle)

  }

  private fun getFloatArrayFromString(floatArrString: String, count: Int): FloatArray {
    val floatArr = FloatArray(count)

    val tokenizedStr = floatArrString.split(" ")
    val strCount = tokenizedStr.count()
    if (strCount == count) {
      for (i in 0 until count) {
        floatArr[i] = tokenizedStr[i].toFloat()
      }
    } else {
      Log.d("ERROR", "Collada float array count does not match the count id value!")
      //TODO: THROW ERROR, not sure how in kotlin yet ¯\_(ツ)_/¯
    }
    return floatArr
  }

  private fun getIntArrayFromString(bufferString: String, count: Int): IntArray {
    val trianglesToElements: Int = count * buffers.count() * 3
    val intArray = IntArray(trianglesToElements)

    val tokenizedStr = bufferString.split(" ")
    val strCount = tokenizedStr.count()

    if (strCount == trianglesToElements) {
      for (i in 0 until strCount) {
        intArray[i] = tokenizedStr[i].toInt()
      }
    } else {
      Log.d("ERROR", "Collada int array count does not match the count id value!")
      //TODO: THROW ERROR, not sure how in kotlin yet ¯\_(ツ)_/¯
    }

    return intArray
  }
}