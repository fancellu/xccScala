package com.felstar.xccScala

import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date

import scala.xml.Attribute
import scala.xml.Node
import scala.xml.Null
import scala.xml.Text
import scala.xml.XML
import scala.xml.parsing.NoBindingFactoryAdapter

import org.w3c.dom.Document
import org.xml.sax.InputSource

import com.marklogic.xcc.Request
import com.marklogic.xcc.RequestOptions
import com.marklogic.xcc.ResultSequence
import com.marklogic.xcc.Session

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.sax.SAXResult

/**
 * XCCscala is a Scala API that sits atop Marklogic XCC and provides Scala interfaces and metaphors
 * Comments and suggestions are welcome. Use this file as you will.
 * Would be nice if I got attribution. Thanks. 
 * @author Dino Fancellu (Felstar Ltd)
 * @version 0.80
 * 
 */

object XCC {

  import com.marklogic.xcc.types._
  
  val tFactory = javax.xml.transform.TransformerFactory.newInstance
  val builderFactory= DocumentBuilderFactory.newInstance();
  
   implicit def toDom(node: scala.xml.Node) = {
    val str=node.buildString(false)
    builderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(str)))
  }
  
   implicit def toScala(dom: _root_.org.w3c.dom.Node): Node = {
    val adapter = new NoBindingFactoryAdapter
    tFactory.newTransformer().transform(new DOMSource(dom), new SAXResult(adapter))
    adapter.rootElem
  }
   
   implicit def toScala(attr: org.w3c.dom.Attr): Attribute 
  			= Attribute(None, attr.getName, Text(attr.getValue), Null)    		
  			
   implicit def toScala(xdm: XdmElement): Node = {
    toScala(xdm.asW3cElement)
  }
   
   implicit def toScala(xdm: XdmNode): Node = {
    toScala(xdm.asW3cNode)
  }
   
   implicit def toScala(xdm: XdmAttribute): Attribute 
  			= toScala(xdm.asW3cAttr)  			
  
  def closeResultSequence(seq:ResultSequence){ 
    if (!seq.isCached())
	 seq.close	
  }

   implicit def toSeqString(s: ResultSequence):Seq[String] = {
      var seq = Seq[String]()
      try { while (s.hasNext()) seq +:= s.next().asString()} 
       finally {closeResultSequence(s)}
      seq reverse
    }
   
    def toSeqAnyRef(s: ResultSequence):Seq[AnyRef] = {
           
      var seq = Seq[AnyRef]()
      try {
       while (s.hasNext()) {
         val item=s.next().getItem()
        seq +:= (item match {
          case x:XdmElement=>  toScala(x)
          case x:XdmAttribute=>  toScala(x)
          case x:XdmNode=>  toScala(x)
          case x:XSString =>  x.asString
          case x:XSInteger=>  x.asInteger
          case x:XSDouble=>  x.asDouble
          case x:XSFloat=>  x.asFloat
          case x:XSDecimal=>  x.asBigDecimal
          case x:XSBoolean=>  x.asBoolean
          case x:XSDate=>  x.asDate
          case x:XSDateTime=>  x.asDate
          case x =>  x.asString
        })
       }
      } finally { closeResultSequence(s) }
      seq reverse
    }
   
    implicit def toSeqInt(s: ResultSequence):Seq[Int] = {
      toSeqString(s).map(Integer.parseInt(_))
    }
    
    implicit def toSeqDecimal(s: ResultSequence):Seq[scala.math.BigDecimal] = {
       toSeqString(s).map(scala.math.BigDecimal(_))
    }
   
  implicit def toSeqXML(seq: Seq[String]):Seq[scala.xml.Elem] = seq.map(XML.loadString)
  implicit def toSeqXML(s: ResultSequence):Seq[scala.xml.Elem] = toSeqXML(toSeqString(s))  
 
   trait ImplicitSession{
    implicit class MySession(val session:Session) {
      def submitRequest(adhoc:String)={
        session.submitRequest(session.newAdhocQuery(adhoc))
      }
      def submitRequest(adhoc:String,options:RequestOptions)={
        session.submitRequest(session.newAdhocQuery(adhoc,options))
      }
      
      def submitRequestWith(adhoc:String)(f: Request=> Unit)={
        val req=session.newAdhocQuery(adhoc)
        f(req)
        session.submitRequest(req)
      }
      
      def submitRequestWith(adhoc:String,options:RequestOptions)(f: Request=> Unit)={
        val req=session.newAdhocQuery(adhoc,options)
        f(req)
        session.submitRequest(req)
      }
      
      def invoke(moduleUri:String)={
        session.submitRequest(session.newModuleInvoke(moduleUri))
      }
      def invoke(moduleUri:String,options:RequestOptions)={
        session.submitRequest(session.newModuleInvoke(moduleUri,options))
      }
      
      def invokeWith(moduleUri:String)(f: Request=> Unit)={
        val req=session.newModuleInvoke(moduleUri)
        f(req)
        session.submitRequest(req)
      }
      
      def invokeWith(moduleUri:String,options:RequestOptions)(f: Request=> Unit)={
        val req=session.newModuleInvoke(moduleUri,options)
        f(req)
        session.submitRequest(req)
      }
      
      def apply(query:String)=submitRequest(query)
      def apply(query:String,options:RequestOptions)=submitRequest(query,options)
    } 
  }
  
   trait ImplicitRequest  {    
    
   implicit class  MyImplicitRequest[A <:Request](val request:A) {    
     
    import com.marklogic.xcc.types.ValueType._ 
     
    def document(varName:String, value:String)= {
      request.setNewVariable(varName, DOCUMENT,value);request
    }
    def document(varName:String, 
        value:java.io.InputStream )= {
      request.setNewVariable(varName, DOCUMENT,value);request
    }
    def document(varName:String, 
        value:Document )= {
      request.setNewVariable(varName, DOCUMENT,value);request
    }    
    def int(varName:String,value:Long)= {
        request.setNewIntegerVariable(varName,value);request
     }
    
     def boolean(varName:String, 
        value:Boolean )= {
      request.setNewVariable(varName, XS_BOOLEAN,value);request
    }
     
      def double(varName:String,value:Double)= {
        request.setNewVariable(varName, XS_DOUBLE,value);request
     }
    
    def float(varName:String,value:Float)= {
        request.setNewVariable(varName, XS_FLOAT,value);request
     }  
    
    def date(varName:String,value:Date):A= {
        val df = new SimpleDateFormat("yyyy-MM-dd");
        date(varName,df.format(value))
     }  
    
    def date(varName:String,value:String):A= {        
        request.setNewVariable(varName, XS_DATE,value);request
     }
     
    def datetime(varName:String,value:Date):A= {
        val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        datetime(varName,df.format(value))
     }
    
    def datetime(varName:String,value:String):A= {        
        request.setNewVariable(varName, XS_DATE_TIME,value);request
     }
      
    // no sequence binding, not supported in XCC, it is in XQJ/XQS
    
    def string(varName:String,value:String)= {
        request.setNewStringVariable(varName,value);request
     }
   }
  }
  
  object AllImplicits extends ImplicitSession with ImplicitRequest  
}