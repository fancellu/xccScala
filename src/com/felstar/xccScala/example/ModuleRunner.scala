package com.felstar.xccScala.example

import java.net.URI

import com.felstar.xccScala.XCC._
import com.felstar.xccScala.XCC.AllImplicits._

import com.marklogic.xcc.ContentSourceFactory

class ModuleRunner(serverUri:URI) {
    val  session = ContentSourceFactory.newContentSource(serverUri).newSession()

    def invoke(moduleUri:String) = session.invoke(moduleUri)

    def invokeToStringArray(moduleUri:String) = invoke(moduleUri).asStrings()

    def invokeToSingleString(moduleUri:String , separator:String) = {        
        invoke(moduleUri).asString(separator)
    }

}

object ModuleRunner{

    def main(args: Array[String]): Unit = {
        if (args.length < 2) {
            usage()
            return
        }

        val serverUri = new URI(args(0))        
        val moduleUri = args(1)
        val runner = new ModuleRunner(serverUri)
        val result = runner.invokeToSingleString(moduleUri, System.getProperty("line.separator"))

        println(result)
    }

    def usage() {
        System.err.println("usage: serveruri docuri [-o outfilename]")
    }
}