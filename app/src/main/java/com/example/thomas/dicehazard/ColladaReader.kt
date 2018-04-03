package com.example.thomas.dicehazard

import org.xmlpull.v1.XmlPullParser
import android.util.Xml
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream


class ColladaReader {

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): List<*> {
        try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()
            return readFeed(parser)
        } finally {
            inputStream.close()
        }
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