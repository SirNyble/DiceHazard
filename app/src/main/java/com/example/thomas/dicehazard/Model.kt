package com.example.thomas.dicehazard

import java.io.InputStream

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