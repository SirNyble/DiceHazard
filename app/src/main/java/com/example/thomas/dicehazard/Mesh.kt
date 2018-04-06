package com.example.thomas.dicehazard

import android.opengl.GLES20
import org.w3c.dom.Element
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Mesh {
    var buffers = HashMap<String, FloatArray>()
    var triangleBuffers: MutableList<Int> = arrayListOf()
    var positionSourceID: String? = null

    init {

    }

    fun addBufferFromSource(sourceElement: Element) {

    }

    fun addTriangleBuffersFromSource(sourceElement: Element) {

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
}