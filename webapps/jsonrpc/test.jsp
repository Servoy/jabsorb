<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="1.2">
<!-- 
  - JSON-RPC-Java - JSP test page for JSON-RPC-Java
  -
  - Author: Michael Clark <michael@metaparadigm.com>
  - Copyright 2004 Metaparadigm Pte Ltd.
  - $Id: test.jsp,v 1.4 2004/04/04 12:29:14 mclark Exp $
  -->
 <jsp:directive.page contentType="text/html;charset=UTF-8" language="java" />
 <jsp:directive.page import="com.metaparadigm.jsonrpc.JSONRPCBridge" />
 <jsp:directive.page import="com.metaparadigm.jsonrpc.test.Test" />
 <jsp:useBean id="JSONRPCBridge" scope="session"
	class="com.metaparadigm.jsonrpc.JSONRPCBridge" />
 <jsp:useBean id="testObject" scope="session"
	class="com.metaparadigm.jsonrpc.test.Test" />
 <jsp:scriptlet>
   response.setDateHeader ("Expires", 0);
   JSONRPCBridge.setDebug(true);
   JSONRPCBridge.registerObject("test", testObject);
   JSONRPCBridge.registerReference(Test.RefTest.class);
   JSONRPCBridge.registerCallableReference(Test.CallableRefTest.class);
 </jsp:scriptlet>
 <jsp:text>
  <html>
   <head>
    <link rel="stylesheet" type="text/css" href="css/site.css" />
    <script type="text/javascript" src="jsolait/init.js"><jsp:text /></script>
    <script type="text/javascript" src="test.js"><jsp:text /></script>
    <title>JSONRPC tester</title>
   </head>
    <body bgcolor="#ffffff" onLoad="onLoad()">
     <h2>JSON-RPC-Java Tester</h2>
      <form id="testform">
       <table cellpadding="2" cellspacing="0" border="0"><tr>
         <td valign="top">Eval:</td>
         <td colspan="6">
          <input type="text" id="txEval" size="80"
	         value="jsonserver.test.echo({bang: 'foo', baz: 9})" />
         </td>
        </tr><tr>
         <td valign="top">Result:</td>
         <td colspan="6">
          <textarea wrap="off" id="txResult" cols="80" rows="24">
           <jsp:text /></textarea>
         </td>
        </tr><tr>
         <td></td>
         <td><input type="button" value="Eval"
                         onclick="doEval()" />
         </td><td><input type="button" value="List Methods"
                         onclick="doListMethods()" />
         </td><td><input type="button" value="Basic Tests"
                         onclick="doBasicTests()" />
         </td><td><input type="button" value="Reference Tests"
                         onclick="doReferenceTests()" />
         </td><td><input type="button" value="Container Tests"
                         onclick="doContainerTests()" />
         </td><td><input type="button" value="Exception Test"
                         onclick="doExceptionTest()" />
         </td>
        </tr>
      </table>
     </form>
    </body>
   </html>
  </jsp:text>
</jsp:root>
