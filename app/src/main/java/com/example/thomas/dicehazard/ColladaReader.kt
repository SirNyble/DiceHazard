package com.example.thomas.dicehazard

import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory


class ColladaReader {

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

      //mesh now has buffers so now we construct the VBO
      mesh.update()
      entries.add(mesh)
    } catch (e: Exception) {
      e.printStackTrace()
    }

    return entries
  }
}