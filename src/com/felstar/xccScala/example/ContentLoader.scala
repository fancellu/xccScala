package com.felstar.xccScala.example

import java.io.File
import java.net.URI

import com.marklogic.xcc.Content
import com.marklogic.xcc.ContentCreateOptions
import com.marklogic.xcc.ContentFactory
import com.marklogic.xcc.ContentSourceFactory

 class ContentLoader(serverUri:URI) {
      val  session = ContentSourceFactory.newContentSource(serverUri).newSession()

   var  options:ContentCreateOptions= null
      
    def load(uris:Array[String], files:Array[File]) {
        val contents = new Array[Content](files.length)

        for (i<-0 to files.length-1) {
            contents(i) = ContentFactory.newContent(uris(i), files(i), options)
        }

        session.insertContent(contents)
    }

    def load(files:Array[File]) {
      val uris=files.map(_.getAbsolutePath)
      load(uris, files)
    }

    def setOptions(options:ContentCreateOptions ) {
        this.options = options
    }
}

object ContentLoader
{    
   def main(args: Array[String]): Unit = {
        if (args.length < 2) {
            usage()
            return
        }

        val serverUri = new URI(args(0))        
        val files = args.tail.map(new File(_))        
        val totalByteCount:Long = files.map(_.length).sum
        val loader = new ContentLoader(serverUri)
        val start = System.currentTimeMillis()
        loader.load(files)

        val elapsed = System.currentTimeMillis() - start

        println("Loaded " + files.length + " files (" + QueryHelper.formatInteger(totalByteCount)
                + " bytes) in " + QueryHelper.formatTime(elapsed) + " ("
                + QueryHelper.formatInteger(QueryHelper.bytesPerSecond(totalByteCount, elapsed)) + " bytes/second)")
    }

    def usage() {
        System.err.println("usage: serveruri docpath ...")
    }
}
