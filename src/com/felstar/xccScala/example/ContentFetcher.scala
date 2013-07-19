package com.felstar.xccScala.example

import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URI

import com.felstar.xccScala.XCC._
import com.felstar.xccScala.XCC.AllImplicits._

import com.marklogic.xcc.ContentSourceFactory
import com.marklogic.xcc.RequestOptions

class ContentFetcher(serverUri:URI)
{
   val  session = ContentSourceFactory.newContentSource(serverUri).newSession()

   var  options= new RequestOptions()

   options.setCacheResult(false)
   
   def  fetch(docUri:String, outStream:OutputStream ) = {

        val  item = session("doc (\"" + docUri + "\")", options).next()

        if (item == null) {
            throw new IllegalArgumentException("No document found with URI '" + docUri + "'")
        }

        item.writeTo(outStream)
    }
   
     def setRequestOptions(options:RequestOptions) {
        this.options = options
    }
}

object ContentFetcher {

    def main(args: Array[String]): Unit = {
        if (args.length < 2) {
            usage()
            return
        }

        val serverUri = new URI(args(0))
        val docUri = args(1)
        var outStream:OutputStream=null

        if (args.length == 4) {
            if (args(2).equals("-o")) {
                outStream = new BufferedOutputStream(new FileOutputStream(args(3)))
            } else {
                usage()
                return
            }
        } else {
            outStream = System.out
        }

        val fetcher  = new ContentFetcher(serverUri)
        val start = System.currentTimeMillis()

        fetcher.fetch(docUri, outStream)

        if (outStream != System.out) {
            outStream.close()
        }

        System.err.println("Fetched " + docUri + " in " + QueryHelper.formatTime(System.currentTimeMillis() - start))
    }

    // -----------------------------------------------------------------

    def usage() {
        System.err.println("usage: serveruri docuri [-o outfilename]")
    }


}
