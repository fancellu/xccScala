package com.felstar.xccScala.example

import xml.Attribute
import xml.Elem
import xml.PrettyPrinter
import com.felstar.xccScala.XCC._
import com.felstar.xccScala.XCC.AllImplicits._
import java.net.URI

import com.marklogic.xcc.ContentSourceFactory

object Test1 {

  def main(args: Array[String]): Unit = {

    val session = ContentSourceFactory.newContentSource(new URI(args(0))).newSession()

    {
      println("----Simple query returned as a string--------")
      val rs = session("concat('Hello World:',current-date())")
      println(rs.asString())
    }
    {
      println("----3 to 8 as ints-----------")
      val ints: Seq[Int] = session("3 to 8")
      ints.foreach(println)
    }
    {
      println("----DB query returned as sequence of strings-----------")
      val strings: Seq[String] = 
        session("/PLAY[TITLE='The Tragedy of Antony and Cleopatra']//SPEECH[SPEAKER='DOLABELLA']")
      strings foreach (str => println("\nSPEECH=" + str))
    }
    {
      println("----DB query returned as sequence of XML Elems-----------")
      val elems: Seq[scala.xml.Elem] = 
        session("/PLAY[TITLE='The Tragedy of Antony and Cleopatra']//SPEECH[SPEAKER='DOLABELLA']")
      val pp = new PrettyPrinter(80, 2)
      elems foreach (xml => println(pp.format(xml)))
    }    
    {
      println("----a mix of decimals, double, ints etc as decimals-----------")
      val decimals: Seq[BigDecimal] =
        session("""1.0,2.0,3.1415926536,xs:double(123.2),xs:integer(12),
			xs:byte(120),xs:long(11111),xs:short(44),xs:int(-5),xs:negativeInteger(-44),
			xs:nonNegativeInteger(45)""")
      decimals.foreach(println)
    }
    {
      println("----complex sequence-----------")
      val refs = toSeqAnyRef(session("""(1 to  5,44.444,<thing>{10 to 12}</thing>,
	    'xxx',<root attr='hello'>somet<mixed>MIX</mixed>hing</root>,1.0,2.0,
	    3.1415926536,xs:double(123.2),xs:integer(12),
			xs:byte(120),xs:long(11111),xs:short(44),xs:int(-5),xs:negativeInteger(-44),
			xs:nonNegativeInteger(45),
	    <thing attr='alone'/>/@*)"""))
      refs.foreach(_ match {
        case x: java.lang.Number => println(x.doubleValue + 1000)
        case x: Elem => println("Element " + x)
        case x: Attribute => println("Attribute " + x)
        case x => println(x + " " + x.getClass)
      })
    }
    {
      println("----adhoc with binding-----------") 
      val refs=toSeqAnyRef(session.submitRequestWith(
          """declare variable $x as item() external:=<no_variable_x/>;
    		  (concat('Hello world adhoc ',current-time()),1 to 3,$x)  """)
    		  {_.int("x",123)}) 
      refs.foreach(x=>println(x+" "+x.getClass)) 
    }
    {
      println("----adhoc with binding-of-scala-xml---") 
      val refs=toSeqAnyRef(session.submitRequestWith(
          """declare variable $x as item() external:=<no_variable_x/>;             
    		  (concat('Hello world adhoc ',current-time()),1 to 3,$x)  """)
    		  {_.document("x", <root>someelem</root> )}) 
      refs.foreach(x=>println(x+" "+x.getClass)) 
    }
    {
	  println("----adhoc many fluent bindings-------")
	  
	  val str="my text!!"
	 
	  val ret2=toSeqAnyRef(session.submitRequestWith(""" 
	 		declare variable $x as xs:integer external;
	 		declare variable $y as xs:integer external;
	 		declare variable $name as xs:string external;
	 		declare variable $mydoc as node() external;	 		
	        declare variable $bool as xs:boolean external;
	        declare variable $pi as xs:double external;
	        declare variable $float as xs:float external;
	        declare variable $date as xs:date external;
	        declare variable $datetime as xs:dateTime external;
	 		($name,' ',$x+$y,'list=',
			  $mydoc,$bool,$pi,$float,$date,$datetime)""")
	 		{_.int("x",1234).int("y",9999).string("name","Dino").
	         document("mydoc", <somedoc>{str}</somedoc>).
	         boolean("bool",true).double("pi",Math.PI).float("float",1.2f).	         
	         date("date",new java.util.Date()).
	         datetime("datetime",new java.util.Date())
	       //  p.sequence("list",Seq(1,"some text",99))//not supported in XCC
	 		} 
	    )
	
	  ret2 foreach(x=>println(x+"\n\t"+x.getClass))	  
	}
    {
     println("----invoking a module, no binding-----------") 
      // hello.xqy is as above example
     val refs=toSeqAnyRef(session.invoke("hello.xqy")) 
     refs.foreach(x=>println(x+" "+x.getClass))     
    }
    {
     println("----invoking a module with binding-----------") 
     val refs=toSeqAnyRef(session.invokeWith("hello.xqy"){_.int("x",123)}) 
     refs.foreach(x=>println(x+" "+x.getClass))     
    }    
    session.close()
  }

}