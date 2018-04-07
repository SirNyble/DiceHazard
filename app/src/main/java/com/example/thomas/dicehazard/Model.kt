package com.example.thomas.dicehazard

import android.content.Context
import android.opengl.GLES20
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Model(inputStream: InputStream) {
  private var mMeshes: MutableList<Mesh> = emptyList<Mesh>().toMutableList()

  init {

    val colladaReader = ColladaReader()
    mMeshes = colladaReader.parse(inputStream) as MutableList<Mesh>
  }

  fun draw(mvpMatrix: FloatArray) {
    mMeshes.forEach {
      it.draw(mvpMatrix)
    }
  }

}