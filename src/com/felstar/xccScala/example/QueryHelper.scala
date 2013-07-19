package com.felstar.xccScala.example

import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.text.NumberFormat


object QueryHelper {
    val MILLIS = 1000
    val SECONDS = MILLIS
    val MINUTES = 60 * SECONDS
    val HOURS = 60 * MINUTES


    def bytesPerSecond(totalByteCount:Long, elapsed:Long)= {
        ((totalByteCount.toDouble / elapsed.toDouble) * MILLIS).toLong
    }

    def formatInteger(n:Long)= {
         NumberFormat.getIntegerInstance().format(n)
    }

    def formatTime(millis:Long)= {
        val  sb = new StringBuffer()
        var n:Long = millis / HOURS

        if (n != 0) {
            sb.append(n).append("h")
        }

        n = (millis % HOURS) / MINUTES

        if ((n) != 0) {
            sb.append(n).append("m")
        }

        sb.append((millis % MINUTES) / SECONDS).append(".")

        n = millis % MILLIS

        if (n < 100)
            sb.append("0")
        if (n < 10)
            sb.append("0")

        sb.append(n).append("s")

        sb.toString()
    }

    def loadQueryFromFile(queryFile:String) = {
      scala.io.Source.fromFile(queryFile).mkString
    }
}
