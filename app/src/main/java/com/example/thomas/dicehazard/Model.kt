package com.example.thomas.dicehazard

import android.content.Context
import java.io.InputStream

class Model(context: Context, inputStream: InputStream) {
  private var mMeshes: MutableList<Mesh> = emptyList<Mesh>().toMutableList()
  private var mContext: Context? = null

  init {
    mContext = context
    val colladaReader = ColladaReader(context)
    mMeshes = colladaReader.parse(inputStream) as MutableList<Mesh>
  }

  fun draw(mvpMatrix: FloatArray) {
    mMeshes.forEach {
      it.draw(mvpMatrix)
    }
  }

}