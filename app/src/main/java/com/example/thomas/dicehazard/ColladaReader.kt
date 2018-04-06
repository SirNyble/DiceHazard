package com.example.thomas.dicehazard

import android.util.Log
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory


class ColladaReader {

  @Throws(XmlPullParserException::class, IOException::class)
  fun parse(inputStream: InputStream): List<*> {
    var entries: MutableList<Mesh> = emptyList<Mesh>().toMutableList()
    try {
      Log.d("Greetings", "HELLO")
      val dbFactory = DocumentBuilderFactory.newInstance()
      val dBuilder = dbFactory.newDocumentBuilder()
      val doc = dBuilder.parse(inputStream)

      val element = doc.documentElement
      element.normalize()

      val meshElement = doc.getElementsByTagName("mesh").item(0)

      val length = meshElement.childNodes.length
      var mesh: Mesh = Mesh()
      for (i in 0 until length) {
        val node = meshElement.childNodes.item(i)
        if (node.nodeType.equals(Node.ELEMENT_NODE)) {
          val sourceElement = node as Element
          Log.d("Collada element name: ", sourceElement.tagName)
          Log.d("Collada element i", i.toString())
          when (sourceElement.tagName) {
            "source" -> {
              Log.d("Collada file element", "SOURCE WOO")
              Log.d("Collada Source Element", sourceElement.getAttribute("id"))
              mesh.addBufferFromSource(sourceElement)
            }
            "vertices" -> {
              Log.d("Collada file element", "Vertices")
              mesh.setPositionSourceID(sourceElement)
            }
            "triangles" -> {
              Log.d("Collada file element", "TRIANGLES")
              mesh.addTriangleBuffersFromSource(sourceElement)
            }
          }
        }
      }
      //mesh now has buffers so now we construct the VBO
      mesh.updateGLBuffers()

    } catch (e: Exception) {
      e.printStackTrace()
    }

    return entries
  }

  @Throws(XmlPullParserException::class, IOException::class)
  private fun readFeed(parser: XmlPullParser): List<*> {
    var entries: MutableList<Mesh> = emptyList<Mesh>().toMutableList()

    //parser.require(XmlPullParser.START_TAG, null, "feed")
    var next = parser.next()
    while (next != XmlPullParser.END_TAG) {
      if (parser.eventType != XmlPullParser.START_TAG) {
        next = parser.next()
        continue

      }
      val name = parser.name
      // Starts by looking for the entry tag
      if (name == "library_geometries") {

        next = parser.next()
        entries.add(readMesh(parser))
      } else {
        skip(parser)
      }
      next = parser.next()
    }
    return entries
  }

  // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
  // to their respective "read" methods for processing. Otherwise, skips the tag.
  @Throws(XmlPullParserException::class, IOException::class)
  private fun readMesh(parser: XmlPullParser): Mesh {
    //parser.require(XmlPullParser.START_TAG, null, "entry")
    var title: String? = null
    var summary: String? = null
    var link: String? = null
    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.eventType != XmlPullParser.START_TAG) {
        continue
      }
      val name = parser.name
      if (name == "title") {
        val to = true
      }
    }
    val mesh = Mesh()
    return mesh
  }

  @Throws(XmlPullParserException::class, IOException::class)
  private fun skip(parser: XmlPullParser) {
    if (parser.eventType != XmlPullParser.START_TAG) {
      throw IllegalStateException()
    }
    var depth = 1
    while (depth != 0) {
      when (parser.next()) {
        XmlPullParser.END_TAG -> depth--
        XmlPullParser.START_TAG -> depth++
      }
    }
  }

}