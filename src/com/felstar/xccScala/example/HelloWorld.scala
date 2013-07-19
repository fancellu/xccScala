package com.felstar.xccScala.example

import java.net.URI

import com.felstar.xccScala.XCC._
import com.felstar.xccScala.XCC.AllImplicits._
import com.marklogic.xcc.ContentSourceFactory

object HelloWorld {
     def main(args: Array[String]): Unit = {
        if (args.length != 1) {
            System.err.println("usage: xcc://user:password@host:port/contentbase")
            return
        }
      
        val  uri = new URI(args(0))
        val session = ContentSourceFactory.newContentSource(uri).newSession()
        println(session("\"Hello World\"").asString())

        session.close()
    }
}
