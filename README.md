![XCCscala Logo](http://felstar.com/projects/xccScala/img/xcc-scala-cliff.png)
# Marklogic XCC for Scala (XCCscala)

*Author: [Dino Fancellu](http://dinofancellu.com)*

**XCCscala** is a Scala Library to invoke XQuery against a **MarkLogic** database via XCC.

Requires Scala 2.10+

Firstly, make sure that your **XCC** java driver jars are included and are working.
Perhaps run some java to make sure its all up and running.

Then add

> com.felstar.xccScala.XCC.scala 

to your code base. XCCscala is implemented in one file!

Then in your Scala include in the following:

	import com.felstar.xccScala.XCC._
    import com.felstar.xccScala.XCC.AllImplicits._

The next few steps are very familiar to any XCC Developer:

Create your session, e.g.
```scala
val session = ContentSourceFactory.newContentSource(new URI(args(0))).newSession()
```
The above uri should be formed like this
	
	xcc://NAME:PASSWORD@HOST:PORT
# Some example code: #

## Simple query returned as a string ##
```scala
val rs = session("concat('Hello World:',current-date())")
println(rs.asString())
```

>     Hello World:2013-07-19+01:00
  
## 3 to 8 as ints ##
```scala
val ints: Seq[Int] = session("3 to 8")
ints.foreach(println)
```
>     3
>     4
>     5
>     6
>     7
>     8

## DB query returned as sequence of strings ##
```scala
val strings: Seq[String] = 
    session("/PLAY[TITLE='The Tragedy of Antony and Cleopatra']//SPEECH[SPEAKER='DOLABELLA']")
    strings foreach (str => println("\nSPEECH=" + str))
```

>     SPEECH=<SPEECH>
>     <SPEAKER>DOLABELLA</SPEAKER>
>     <LINE>Caesar, 'tis his schoolmaster:</LINE>
>     <LINE>An argument that he is pluck'd, when hither</LINE>
>     <LINE>He sends so poor a pinion off his wing,</LINE>
>     <LINE>Which had superfluous kings for messengers</LINE>
>     <LINE>Not many moons gone by.</LINE>
>     </SPEECH>
>     etc...

## DB query returned as sequence of XML Elems ##
```scala
val elems: Seq[scala.xml.Elem] = 
        session("/PLAY[TITLE='The Tragedy of Antony and Cleopatra']//SPEECH[SPEAKER='DOLABELLA']")
    val pp = new PrettyPrinter(80, 2)
    elems foreach (xml => println(pp.format(xml)))
```

>     <SPEECH>
>       <SPEAKER>DOLABELLA</SPEAKER>
>       <LINE>Caesar, 'tis his schoolmaster:</LINE>
>       <LINE>An argument that he is pluck'd, when hither</LINE>
>       <LINE>He sends so poor a pinion off his wing,</LINE>
>       <LINE>Which had superfluous kings for messengers</LINE>
>       <LINE>Not many moons gone by.</LINE>
>     </SPEECH>
>     etc...
  
## A mix of decimals, double, ints etc as decimals ##
```scala
val decimals: Seq[BigDecimal] =
    session("""1.0,2.0,3.1415926536,xs:double(123.2),xs:integer(12),
		xs:byte(120),xs:long(11111),xs:short(44),xs:int(-5),xs:negativeInteger(-44),
		xs:nonNegativeInteger(45)""")
  decimals.foreach(println)
```
>     1
>     2
>     3.1415926536
>     123.2
>     12
>     120
>     11111
>     44
>     -5
>     -44
>     45  
  
## Complex sequence##
```scala
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
```
>     1001.0
>     1002.0
>     1003.0
>     1004.0
>     1005.0
>     1044.444
>     Element <thing>10 11 12</thing>
>     xxx class java.lang.String
>     Element <root attr="hello">somet<mixed>MIX</mixed>hing</root>
>     1001.0
>     1002.0
>     1003.1415926536
>     1123.2
>     1012.0
>     1120.0
>     12111.0
>     1044.0
>     995.0
>     956.0
>     1045.0   
>     Attribute  attr="alone"


## Adhoc with binding ##
```scala
val refs=toSeqAnyRef(session.submitRequestWith(
  """declare variable $x as item() external:=<no_variable_x/>;
	  (concat('Hello world adhoc ',current-time()),1 to 3,$x)  """)
	  {_.int("x",123)}) 
 refs.foreach(x=>println(x+" "+x.getClass))
```

>    Hello world adhoc 19:09:44+01:00 class java.lang.String
>    1 class java.lang.Integer
>    2 class java.lang.Integer
>    3 class java.lang.Integer
>    123 class java.lang.Integer

## Adhoc with binding of scala xml ##
```scala
val refs=toSeqAnyRef(session.submitRequestWith(
  """declare variable $x as item() external:=<no_variable_x/>;             
	  (concat('Hello world adhoc ',current-time()),1 to 3,$x)  """)
	  {_.document("x", <root>someelem</root> )}) 
  refs.foreach(x=>println(x+" "+x.getClass))
```

>    Hello world adhoc 19:09:44+01:00 class java.lang.String
>    1 class java.lang.Integer
>    2 class java.lang.Integer
>    3 class java.lang.Integer
>    <root>someelem</root> class scala.xml.Elem

##Many fluent Bindings
```scala
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
	{p=>
     p.int("x",1234).int("y",9999).string("name","Dino").
     document("mydoc", <somedoc>{str}</somedoc>).
     boolean("bool",true).double("pi",Math.PI).float("float",1.2f).	         
     date("date",new java.util.Date()).
     datetime("datetime",new java.util.Date())
   //  p.sequence("list",Seq(1,"some text",99))//not supported in XCC
	} 
  )

  ret2 foreach(x=>println(x+"\n\t"+x.getClass))	
```
>     Dino
>       class java.lang.String	 
>     	
> 		class java.lang.String
>     11233
>       class java.lang.Integer
>     list=
>       class java.lang.String
>     <somedoc>my text!!</somedoc>
>       class scala.xml.Elem
>     true
>       class java.lang.Boolean
>     3.14159265358979
>       class java.lang.Double
>     1.2
>     	class java.lang.Float
>     Fri Jul 19 00:00:00 BST 2013
>       class java.util.Date
>     Fri Jul 19 19:37:23 BST 2013
>       class java.util.Date

###hello.qxy : contents###
```xquery
declare variable $x as item() external:=<no_variable_x/>;
(concat('Hello world ',current-time()),1 to 3,$x) 
```

## Invoking a module, no binding ##
```scala
val refs=toSeqAnyRef(session.invoke("hello.xqy")) 
refs.foreach(x=>println(x+" "+x.getClass))
```

>    Hello world 19:37:23+01:00 class java.lang.String
>    1 class java.lang.Integer
>    2 class java.lang.Integer
>    3 class java.lang.Integer
>    <no_variable_x/> class scala.xml.Elem

## Invoking a module with binding ##
```scala
val refs=toSeqAnyRef(session.invokeWith("hello.xqy"){_.int("x",123)}) 
refs.foreach(x=>println(x+" "+x.getClass))
```

>    Hello world 19:37:23+01:00 class java.lang.String
>    1 class java.lang.Integer
>    2 class java.lang.Integer
>    3 class java.lang.Integer
>    123 class java.lang.Integer

##A few items of note

Any feedback is appreciated. I understand that I may well not currently cover all use cases and look forward to improving XCCscala.
You can find some examples of XCCscala being used in the com.felstar.xccScala.example package

## *A big thank you to Charles Foster of [XQJ.net](http://xqj.net) for the inspiration* ##
