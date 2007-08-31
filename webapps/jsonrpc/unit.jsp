<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
 "http://www.w3.org/TR/html4/loose.dtd">
<%@
page contentType="text/html; charset=UTF-8" %><%@
page language="java" %><%@
page import="com.metaparadigm.jsonrpc.JSONRPCBridge" %><%@
page import="com.metaparadigm.jsonrpc.test.Test"
%><jsp:useBean id="JSONRPCBridge" scope="session"
     class="com.metaparadigm.jsonrpc.JSONRPCBridge"
/><jsp:useBean id="testObject" scope="session"
     class="com.metaparadigm.jsonrpc.test.Test"
/><%
   response.setDateHeader ("Expires", 0);
   //JSONRPCBridge.setDebug(true);
   JSONRPCBridge.registerObject("test", testObject);
   JSONRPCBridge.registerReference(Test.RefTest.class);
   JSONRPCBridge.registerCallableReference(Test.CallableRefTest.class);
%>
<html>
  <head>
    <link rel="stylesheet" type="text/css" href="css/site.css">
    <link rel="stylesheet" type="text/css" href="css/unit.css">
    <script type="text/javascript" src="jsonrpc.js"></script>
    <script type="text/javascript" src="unit.js"></script>
    <title>JSON-RPC-Java Unit Tests</title>
   </head>
   <body bgcolor="#ffffff" onLoad="onLoad()">

    <h1><img align="left" src="images/json.png" width="55" height="55" hspace="6" vspace="0" alt="JSON logo"/>JSON-RPC-Java</h1>
    <div class="tagline">JavaScript to Java remote communication.</div>
    <hr />
    <div class="menu"><a href="index.html">Home</a> | <a href="tutorial.html">Tutorial</a> | <a href="manual.html">Manual</a> | <a href="demos.html">Demos</a> | <a href="docs/">API Documentation</a> | <a href="http://oss.metaparadigm.com/mailman/listinfo/json-rpc-java">Mailing List</a> | <a href="CHANGES.txt">Changelog</a></div>

    <h2>JSON-RPC-Java Unit Tests</h2>

    <table cellpadding="0" cellspacing="0" border="0" width="100%">
      <tr>
        <td align="left">
          <input type="button" value="Run Tests" onclick="runTests()" />
        </td>
        <td align="right">
          (
          <input type="checkbox" id="profile" />
          Profile
	  |
          <input type="checkbox" id="async" checked />
          Asynchronous
          )
          &nbsp;
          Max parallel async requests
          <input type="text" id="max_requests" value="8" size="2" />
        </td> 
      <tr>
    </table>

    <p></p>

    <table class="test_table">
      <thead>
       <tr>
        <th class="test_th" width="260"><div class="code_heading">Code</div></th>
        <th class="test_th" width="100%"><div class="result_heading">Result</div></th>
        <th class="test_th" width="32"><div class="pass_heading">Pass</div></th>
       </tr>
      </thead>
      <tbody id="tests"></tbody>
    </table>

    <br>
    <hr>
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
      <tr>
	<td><code>$Id: unit.jsp,v 1.3 2005/02/13 01:26:47 mclark Exp $</code></td>
	<td><div class="copyright">Copyright 2005 <a href="http://www.metaparadigm.com/">Metaparadigm Pte Ltd</a></div></td>
      </tr>
    </table>
  </body>
</html>
