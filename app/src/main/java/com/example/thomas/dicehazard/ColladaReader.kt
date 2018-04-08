package com.example.thomas.dicehazard

import android.content.Context
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.graphics.BitmapFactory
import android.graphics.Bitmap




class ColladaReader(context: Context) {
  private var mContext: Context? = null

  init {
    mContext = context
  }

  @Throws(XmlPullParserException::class, IOException::class)
  fun parse(inputStream: InputStream): List<*> {
    val entries: MutableList<Mesh> = emptyList<Mesh>().toMutableList()
    try {
      val dbFactory = DocumentBuilderFactory.newInstance()
      val dBuilder = dbFactory.newDocumentBuilder()
      val doc = dBuilder.parse(inputStream)

      val element = doc.documentElement
      element.normalize()

      val meshElement = doc.getElementsByTagName("mesh").item(0)

      val length = meshElement.childNodes.length
      val mesh = Mesh()
      for (i in 0 until length) {
        val node = meshElement.childNodes.item(i)
        if (node.nodeType.equals(Node.ELEMENT_NODE)) {
          val sourceElement = node as Element
          when (sourceElement.tagName) {
            "source" -> {
              mesh.addBufferFromSource(sourceElement)
            }
            "vertices" -> {
              mesh.setPositionSourceID(sourceElement)
            }
            "triangles" -> {
              mesh.addTriangleBuffersFromSource(sourceElement)
            }
          }
        }
      }
      //TODO: Actually load the appropriate texture and not hardcoded value
      loadTexture(mesh, "boy_10.JPG")

      //mesh now has buffers so now we construct the VBO
      mesh.update()
      entries.add(mesh)
    } catch (e: Exception) {
      e.printStackTrace()
    }

    return entries
  }

  fun loadTexture(mesh: Mesh, imageFileName: String) {
    val textureHandle = IntArray(1)

    GLES20.glGenTextures(1, textureHandle, 0)

    if (textureHandle[0] != 0) {
      val options = BitmapFactory.Options()
      options.inScaled = false   // No pre-scaling

      // Read in the resource
      val inputStream: InputStream = mContext?.assets?.open(imageFileName)!!
      val bitmap = BitmapFactory.decodeStream(inputStream )

      // Bind to the texture in OpenGL
      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

      // Set filtering
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)

      // Load the bitmap into the bound texture.
      GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

      // Recycle the bitmap, since its data has been loaded into OpenGL.
      bitmap.recycle()
    }

    if (textureHandle[0] == 0) {
      throw RuntimeException("Error loading texture.")
    }

    mesh.setTextureHandle(textureHandle[0])
  }
}