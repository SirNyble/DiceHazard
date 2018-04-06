package com.example.thomas.dicehazard

import android.opengl.GLES20
import android.util.Log
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Mesh {
  var buffers = HashMap<String, FloatArray>()
  var triangleBuffers: MutableList<IntArray> = arrayListOf()
  var positionSourceID: String? = null

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
      this.positionSourceID = inputElement.getAttribute("source")
    }

  }

  fun updateGLBuffers() {
    //TODO: use appropriate opengl calls to fill the buffers
    // initialize vertex byte buffer for shape coordinates
//        val bb = ByteBuffer.allocateDirect(
//                // (number of coordinate values * 4 bytes per float)
//                triangleCoords.size * 4)
//        // use the device hardware's native byte order
//        bb.order(ByteOrder.nativeOrder())
//
//        // create a floating point buffer from the ByteBuffer
//        vertexBuffer = bb.asFloatBuffer()
//        // add the coordinates to the FloatBuffer
//        vertexBuffer?.put(triangleCoords)
//        // set the buffer to read the first coordinate
//        vertexBuffer?.position(0)
//
//        val vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
//                vertexShaderCode)
//        val fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
//                fragmentShaderCode)
//
//        // create empty OpenGL ES Program
//        mProgram = GLES20.glCreateProgram()
//
//        // add the vertex shader to program
//        GLES20.glAttachShader(mProgram, vertexShader)
//
//        // add the fragment shader to program
//        GLES20.glAttachShader(mProgram, fragmentShader)
//
//        // creates OpenGL ES program executables
//        GLES20.glLinkProgram(mProgram)
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