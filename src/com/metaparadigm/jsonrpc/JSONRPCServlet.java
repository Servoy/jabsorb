/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: JSONRPCServlet.java,v 1.12 2005/02/11 09:41:49 mclark Exp $
 *
 * Copyright Metaparadigm Pte. Ltd. 2004.
 * Michael Clark <michael@metaparadigm.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public (LGPL)
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details: http://www.gnu.org/
 *
 */

package com.metaparadigm.jsonrpc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.CharArrayWriter;
import java.util.NoSuchElementException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * This servlet handles JSON-RPC requests over HTTP and hands them to
 * a JSONRPCBridge instance registered in the HttpSession.
 * </p>
 * An instance of the JSONRPCBridge object is automatically placed in the
 * HttpSession object registered under the attribute "JSONRPCBridge" by
 * the JSONRPCServlet.
 * <p />
 * The following can be added to your web.xml to export the servlet
 * under the URI &quot;<code>/JSON-RPC</code>&quot;
 * <p />
 * <code>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;com.metaparadigm.jsonrpc.JSONRPCServlet&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;com.metaparadigm.jsonrpc.JSONRPCServlet&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;com.metaparadigm.jsonrpc.JSONRPCServlet&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;/JSON-RPC&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 * </code>
 */

public class JSONRPCServlet extends HttpServlet
{
    private final static int buf_size = 4096;

    public void service(HttpServletRequest request,
			HttpServletResponse response)
	throws IOException, ClassCastException
    {
	// Find the JSONRPCBridge for this session or create one
	// if it doesn't exist
	HttpSession session = request.getSession();
	JSONRPCBridge json_bridge = null;
	json_bridge = (JSONRPCBridge)
	    session.getAttribute("JSONRPCBridge");
	if(json_bridge == null) {
	    json_bridge = new JSONRPCBridge();
	    session.setAttribute("JSONRPCBridge", json_bridge);
	}

	// Encode using UTF-8, although We are actually ASCII clean as
	// all unicode data is JSON escaped using backslash u. This is
	// less data efficient for foreign character sets but it is
	// needed to support naughty browsers such as Konqueror and Safari
	// which do not honour the charset set in the response
	response.setContentType("text/plain;charset=utf-8");
	OutputStream out = response.getOutputStream();

	// Decode using the charset in the request if it exists otherwise
	// use UTF-8 as this is what all browser implementations use.
	// The JSON-RPC-Java JavaScript client is ASCII clean so it
	// although here we can correctly handle data from other clients
	// that do not escape non ASCII data
	String charset = request.getCharacterEncoding();
	if(charset == null) charset = "UTF-8";
	BufferedReader in = new BufferedReader
	    (new InputStreamReader(request.getInputStream(), charset));

	// Read the request
        CharArrayWriter data = new CharArrayWriter();
        char buf[] = new char[buf_size];
        int ret;
        while((ret = in.read(buf, 0, buf_size)) != -1) {
            data.write(buf, 0, ret);
        }

	if(json_bridge.isDebug())
	    System.out.println("JSONRPCServlet.service recv: " +
			       data.toString());

	// Process the request
	JSONObject json_req = null;
	Object json_res = null;
	try {
	    json_req = new JSONObject(data.toString());

	    // Get method name and arguments
	    String methodName = null;
	    JSONArray arguments = null;

	    try { methodName = json_req.getString("method");
	    } catch (NoSuchElementException ne) {}

	    // Back compatibility for <= 0.7 clients
	    if (methodName != null) {
		arguments = json_req.getJSONArray("params");
	    } else {
		methodName = json_req.getString("methodName");
		arguments = json_req.getJSONArray("arguments");
		System.err.println("JSONRPCServlet.service: " +
				   "methodName in request deprecated, " +
				   "please update your JSON-RPC client.");
	    }

	    // Is this a CallableReference it will have a non-zero objectID
	    int object_id = json_req.optInt("objectID");
	    if(json_bridge.isDebug())
		if(object_id != 0)
		    System.out.println("JSONRPCServlet.service call " +
				       "objectID=" + object_id + " " +
				       methodName + "(" + arguments + ")");
		else
		    System.out.println("JSONRPCServlet.service call " +
				       methodName + "(" + arguments + ")");
	    json_res = json_bridge.call(session,
					object_id, methodName, arguments);
	} catch (ParseException e) {
	    System.err.println
		("JSONRPCServlet.service can't parse call: " + data);
	    json_res = JSONRPCResult.ERR_PARSE;
	} catch (NoSuchElementException e) {
	    System.err.println
		("JSONRPCServlet.service no method in request");
	    json_res = JSONRPCResult.ERR_NOMETHOD;
	}

	// Write the response
	if(json_bridge.isDebug())
	    System.out.println("JSONRPCServlet.service send: " +
			       json_res.toString());
	byte[] bout = json_res.toString().getBytes("UTF-8");
	response.setIntHeader("Content-Length", bout.length);
	response.setHeader("Connection", "keep-alive");

	out.write(bout);
	out.flush();
	out.close();
    }
}
