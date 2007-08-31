<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
 "http://www.w3.org/TR/html4/loose.dtd">
<%@
page contentType="text/html; charset=UTF-8" %><%@
page language="java" %><%@
page import="com.metaparadigm.jsonrpc.JSONRPCBridge" %><%@
page import="com.metaparadigm.dict.DictClient"
%><jsp:useBean id="JSONRPCBridge" scope="session"
     class="com.metaparadigm.jsonrpc.JSONRPCBridge"
/><jsp:useBean id="dict" scope="session"
     class="com.metaparadigm.dict.DictClient"
/> <%
   response.setDateHeader ("Expires", 0);
   //JSONRPCBridge.setDebug(true);
   JSONRPCBridge.registerObject("dict", dict);
%>
<html>
  <head>
    <link rel="stylesheet" type="text/css" href="css/site.css">
    <link rel="stylesheet" type="text/css" href="css/dict.css">
    <script type="text/javascript" src="jsonrpc.js"></script>
    <script type="text/javascript" src="dict.js"></script>
    <title>JSON-RPC-Java Dictionary Client</title>
   </head>
   <body bgcolor="#ffffff" onLoad="onLoad()">

    <h1><img align="left" src="images/json.png" width="55" height="55" hspace="6" vspace="0" alt="JSON logo"/>JSON-RPC-Java</h1>
    <div class="tagline">JavaScript to Java remote communication.</div>
    <hr />
    <div class="menu"><a href="index.html">Home</a> | <a href="tutorial.html">Tutorial</a> | <a href="manual.html">Manual</a> | <a href="demos.html">Demos</a> | <a href="docs/">API Documentation</a> | <a href="http://oss.metaparadigm.com/mailman/listinfo/json-rpc-java">Mailing List</a> | <a href="CHANGES.txt">Changelog</a></div>

    <h2>JSON-RPC-Java Dictionary Client</h2>

    <p>
      <strong>Word:</strong>
      <input class="dict_word_input" id="word" type="text" size="25" />
      &nbsp;
      <strong>Strategy:</strong>
      <select class="dict_strategy_select" id="strategy">
        <option value=".">default</option>
      </select>
      &nbsp;
      <strong>Database:</strong>
      <select class="dict_database_select" id="database">
        <option value="*">all</option>
      </select> 
      &nbsp;
      <input id="auto" type="checkbox" /> Auto
      &nbsp;
      <input id="lookup" type="button" value="Lookup"
             onclick="matchWord()" />
    </p>
    <p><strong>Strategy:</strong> <span id="strategy_desc">Default</span></p>
    <p><strong>Database:</strong> <span id="database_desc">All</span></p>

    <table cellpadding="0" cellspacing="0" width="100%" style="border: 1px solid #c0c0c0; border-collapse: collapse;">
      <tr>
        <td width="150">
	  <select id="matches" size="24" class="dict_match_select">
            <option></option>
          </select>
        </td>
        <td id="definitions"><div></div></td>
      </tr>
    </table>

    <br>
    <hr>
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
      <tr>
	<td><code>$Id: dict.jsp,v 1.17 2005/02/13 01:26:47 mclark Exp $</code></td>
	<td><div class="copyright">Copyright 2005 <a href="http://www.metaparadigm.com/">Metaparadigm Pte Ltd</a></div></td>
      </tr>
    </table>
  </body>
</html>
