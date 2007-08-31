<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
 "http://www.w3.org/TR/html4/loose.dtd">
<%@
page contentType="text/html; charset=UTF-8" %><%@
page language="java" %><%@
page import="com.metaparadigm.jsonrpc.test.Test"
%><jsp:useBean id="JSONRPCBridge" scope="session"
     class="com.metaparadigm.jsonrpc.JSONRPCBridge"
/><jsp:useBean id="testObject" scope="session"
     class="com.metaparadigm.jsonrpc.test.Test"
/><%
   response.setDateHeader ("Expires", 0);
   JSONRPCBridge.enableReferences();
   JSONRPCBridge.registerObject("test", testObject);
   JSONRPCBridge.registerReference(Test.RefTest.class);
   JSONRPCBridge.registerCallableReference(Test.CallableRefTest.class);
%>
<html>
  <head>
    <link rel="stylesheet" type="text/css" href="css/site.css">
    <script type="text/javascript" src="jsonrpc.js"></script>
    <script type="text/javascript" src="test.js"></script>
    <title>JSON-RPC-Java Tests</title>
   </head>
   <body bgcolor="#ffffff" onLoad="onLoad()">

    <h1><img align="left" src="images/json.png" width="55" height="55" hspace="6" vspace="0" alt="JSON logo"/>JSON-RPC-Java</h1>
    <div class="tagline">JavaScript to Java remote communication.</div>
    <hr />
    <div class="menu"><a href="index.html">Home</a> | <a href="tutorial.html">Tutorial</a> | <a href="manual.html">Manual</a> | <a href="demos.html">Demos</a> | <a href="docs/">API Documentation</a> | <a href="http://oss.metaparadigm.com/mailman/listinfo/json-rpc-java">Mailing List</a> | <a href="CHANGES.txt">Changelog</a></div>

    <h2>JSON-RPC-Java Tests</h2>

    <table cellpadding="2" cellspacing="0" border="0">
      <tr>
       <td>
        <input type="text" id="eval" size="80"
	 value="jsonrpc.test.echo({bang: 'foo', baz: 9})" />
       </td>
       <td><input type="button" value="Eval" onclick="doEval()" /></td>
      </tr>
      <tr><td></td></tr>
      <tr>
       <td>
        <textarea wrap="off" id="result" cols="80" rows="24"></textarea>
       </td>
       <td valign="top">
        <h3>Tests</h3>
        <p><a href="javascript:doListMethods();">List Methods</a><br>
        <a href="javascript:doBasicTests();">Basic Tests</a><br>
        <a href="javascript:doReferenceTests();">Reference Tests</a><br>
        <a href="javascript:doContainerTests();">Container Tests</a><br>
        <a href="javascript:doExceptionTest();">Exception Test</a></p>
        <h3>Debug</h3>
        <p><a href="javascript:setDebug(true);">Debug On</a><br>
        <a href="javascript:setDebug(false);">Debug Off</a></p>
        <h3>Callbacks</h3>
        <p><a href="javascript:setCallback(true);">Callback On</a><br>
        <a href="javascript:setCallback(false);">Callback Off</a></p>
	<p><em><strong>Note:</strong> the debug and callback controls only affect debug output on the server side.</em></p>
       </td>
      </tr>
    </table>

    <br>
    <hr>
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
      <tr>
	<td><code>$Id: test.jsp,v 1.34 2005/04/24 19:52:08 mclark Exp $</code></td>
	<td><div class="copyright">Copyright 2005 <a href="http://www.metaparadigm.com/">Metaparadigm Pte Ltd</a></div></td>
      </tr>
    </table>
  </body>
</html>
