/*
 * JSON-RPC JavaScript client
 *
 * $Id: jsonrpc.js,v 1.25 2005/02/13 03:42:09 mclark Exp $
 *
 * Copyright (c) 2003-2004 Jan-Klaas Kollhof
 * Copyright (c) 2005 Michael Clark, Metaparadigm Pte Ltd
 *
 * This code is based on Jan-Klaas' JavaScript o lait library (jsolait).
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


// escape a character

function escapeJSONChar(c)
{
    if(c == "\"" || c == "\\") return "\\" + c;
    else if (c == "\b") return "\\b";
    else if (c == "\f") return "\\f";
    else if (c == "\n") return "\\n";
    else if (c == "\r") return "\\r";
    else if (c == "\t") return "\\t";
    var hex = c.charCodeAt(0).toString(16);
    if(hex.length == 1) return "\\u000" + hex;
    else if(hex.length == 2) return "\\u00" + hex;
    else if(hex.length == 3) return "\\u0" + hex;
    else return "\\u" + hex;
}


// encode a string into JSON format

function escapeJSONString(s)
{
    // The following should suffice but Safari's regex is b0rken
    // (doesn't support callback substitutions)
    //
    //   return "\"" + s.replace(/([^\u0020-\u007f]|[\\\"])/g,
    //                           escapeJSONChar) + "\"";

    // Rather inefficient way to do it
    var parts = s.split("");
    for(var i=0; i < parts.length; i++) {
	var c =parts[i];
	if(c == '"' ||
	   c == '\\' ||
	   c.charCodeAt(0) < 32 ||
	   c.charCodeAt(0) >= 128)
	    parts[i] = escapeJSONChar(parts[i]);
    }
    return "\"" + parts.join("") + "\"";
}


// Marshall objects to JSON format

function toJSON(o)
{
    if(o == null) {
	return "null";
    } else if(o.constructor == String) {
	return escapeJSONString(o);
    } else if(o.constructor == Number) {
	return o.toString();
    } else if(o.constructor == Boolean) {
	return o.toString();
    } else if(o.constructor == Date) {
	return o.valueOf().toString();
    } else if(o.constructor == Array) {
	var v = [];
	for(var i = 0; i < o.length; i++) v.push(toJSON(o[i]));
	return "[" + v.join(", ") + "]";
    } else {
	var v = [];
	for(attr in o) {
	    if(o[attr] == null) v.push("\"" + attr + "\": null");
	    else if(typeof o[attr] == "function"); // skip
	    else v.push(escapeJSONString(attr) + ": " + toJSON(o[attr]));
	}
	return "{" + v.join(", ") + "}";
    }
}


// JSONRpcClient constructor

JSONRpcClient = function JSONRpcClient_ctor(serverURL, user, pass, objectID)
{
    this.serverURL = serverURL;
    this.user = user;
    this.pass = pass;
    this.objectID = objectID;

    // Add standard methods
    this.addMethods(["system.listMethods"]);

    // Query the methods on the server and add them to this object
    var req = this.makeRequest("system.listMethods", []);
    var m = this.sendRequest(req);
    this.addMethods(m);
}


// JSONRpcCLient.Exception

JSONRpcClient.Exception =
function JSONRpcClient_Exception_ctor(code, message, javaStack)
{
    this.code = code;
    var name;
    if(javaStack) {
	this.javaStack = javaStack;
	var m = javaStack.match(/^([^:]*)/);
	if(m) name = m[0];
    }
    if(name) this.name = name;
    else this.name = "JSONRpcClientException";
    this.message = this.name + ": " + message;
}

JSONRpcClient.Exception.CODE_REMOTE_EXCEPTION = 490;
JSONRpcClient.Exception.CODE_ERR_PARSE = 590;
JSONRpcClient.Exception.CODE_ERR_NOMETHOD = 591;
JSONRpcClient.Exception.CODE_ERR_UNMARSHALL = 592;
JSONRpcClient.Exception.CODE_ERR_MARSHALL = 593;

JSONRpcClient.Exception.prototype = new Error();

JSONRpcClient.Exception.prototype.toString =
function JSONRpcClient_Exception_toString(code, msg)
{
    return this.message;
}


// Default top level exception handler

JSONRpcClient.default_ex_handler =
function JSONRpcClient_default_ex_handler(e) { alert(e); }


// Client settable variables

JSONRpcClient.toplevel_ex_handler = JSONRpcClient.default_ex_handler;
JSONRpcClient.profile_async = false;
JSONRpcClient.max_req_active = 1;
JSONRpcClient.old0point7proto = false; // Back compat


// JSONRpcClient implementation

JSONRpcClient.prototype.createMethod =
function JSONRpcClient_createMethod(methodName)
{
    var fn=function()
    {
	var args = [];
	var callback = null;
	for(var i=0;i<arguments.length;i++) args.push(arguments[i]);
	if(typeof args[0] == "function") callback = args.shift();
	var req = fn.client.makeRequest.call(fn.client, fn.methodName,
					     args, callback);
	if(callback == null) {
	    return fn.client.sendRequest.call(fn.client, req);
	} else {
	    JSONRpcClient.async_requests.push(req);
	    JSONRpcClient.kick_async();
	}
    }
    fn.client = this;
    fn.methodName = methodName;
    return fn;
}

JSONRpcClient.prototype.addMethods =
function JSONRpcClient_addMethods(methodNames)
{
    for(var i=0; i<methodNames.length; i++) {
	var obj = this;
	var names = methodNames[i].split(".");
	for(var n=0; n<names.length-1; n++){
	    var name = names[n];
	    if(obj[name]){
		obj = obj[name];
	    } else {
		obj[name]  = new Object();
		obj = obj[name];
	    }
	}
	var name = names[names.length-1];
	if(!obj[name]){
	    var method = this.createMethod(methodNames[i]);
	    obj[name] = method;
	}
    }
}

JSONRpcClient.getCharsetFromHeaders =
function JSONRpcClient_getCharsetFromHeaders(http)
{
    try {
	var contentType = http.getResponseHeader("Content-type");
	var parts = contentType.split(/\s*;\s*/);
	for(var i =0; i < parts.length; i++) {
	    if(parts[i].substring(0, 8) == "charset=")
		return parts[i].substring(8, parts[i].length);
	}
    } catch (e) {}
    return "UTF-8"; // default
}

// Async queue globals
JSONRpcClient.async_requests = [];
JSONRpcClient.async_responses = [];
JSONRpcClient.async_timeout = null;
JSONRpcClient.num_req_active = 0;

JSONRpcClient.async_handler = function JSONRpcClient_async_handler()
{
    JSONRpcClient.async_timeout = null;

    while(JSONRpcClient.async_responses.length > 0) {
	var res = JSONRpcClient.async_responses.shift();
	try {
	    if(res.profile) res.profile.dispatch = new Date();
	    res.cb(res.result, res.ex, res.profile);
	} catch(e) {
	    JSONRpcClient.toplevel_ex_handler(e);
	}
    }

    while(JSONRpcClient.async_requests.length > 0 &&
	  JSONRpcClient.num_req_active < JSONRpcClient.max_req_active) {
	var req = JSONRpcClient.async_requests.shift();
	req.client.sendRequest.call(req.client, req);
    }
}

JSONRpcClient.kick_async = function JSONRpcClient_kick_async()
{
    if(JSONRpcClient.async_timeout == null)
	setTimeout(JSONRpcClient.async_handler, 0);
}

JSONRpcClient.prototype.makeRequest =
function JSONRpcClient_makeRequest(methodName, args, cb)
{
    var req = {};
    req.client = this;
    var obj;
    if(JSONRpcClient.old0point7proto) {
	obj = {"methodName" : methodName, "arguments" : args};
    } else {
	obj = {"method" : methodName, "params" : args};
    }
    if (this.objectID) obj.objectID = this.objectID;
    if (cb) req.cb = cb;
    if (JSONRpcClient.profile_async)
	req.profile = { "submit": new Date() };
    req.data = toJSON(obj);
    return req;
}

JSONRpcClient.prototype.sendRequest =
function JSONRpcClient_sendRequest(req)
{
    if(req.profile) req.profile.start = new Date();

    // Get free http object from the pool
    var http = JSONRpcClient.poolGetHTTPRequest();
    JSONRpcClient.num_req_active++;

    // Send the request
    http.open("POST", this.serverURL, (req.cb != null), this.user, this.pass);

    // setRequestHeader is missing in Opera 8 Beta
    try { http.setRequestHeader("Content-type", "text/plain"); } catch(e) {}

    // Construct call back if we have one
    if(req.cb) {
	var self = this;
	http.onreadystatechange = function() {
	    if(http.readyState == 4) {
		var res = { "cb": req.cb, "result": null, "ex": null};
		if (req.profile) {
		    res.profile = req.profile;
		    res.profile.end = new Date();
		}
		try { res.result = self.handleResponse(http); }
		catch(e) { res.ex = e; }
		JSONRpcClient.async_responses.push(res);
		JSONRpcClient.kick_async();
	    }
	};
    } else {
	http.onreadystatechange = function() {};
    }

    http.send(req.data);

    if(!req.cb) return this.handleResponse(http);
}

JSONRpcClient.prototype.handleResponse =
function JSONRpcClient_handleResponse(http)
{
    // Get the charset
    if(!this.charset) {
	this.charset = JSONRpcClient.getCharsetFromHeaders(http);
    }

    // Get request results
    var status = http.status;
    var statusText = http.statusText;
    var data = http.responseText;

    // Return http object to the pool;
    JSONRpcClient.poolReturnHTTPRequest(http)
    JSONRpcClient.num_req_active--;

    // Unmarshall the response
    if(status != 200) {
	throw new JSONRpcClient.Exception(status, statusText);
    }
    var obj;
    try {
	eval("obj = " + data);
    } catch(e) {
	throw new JSONRpcClient.Exception(550, "error parsing result");
    }
    if(obj.error)
	throw new JSONRpcClient.Exception(obj.error.code, obj.error.msg,
					  obj.error.trace);
    var res = obj.result;

    // Handle CallableProxy
    if(res && res.objectID && res.JSONRPCType == "CallableReference")
	return new JSONRpcClient(this.serverURL, this.user,
				 this.pass, res.objectID);

    return res;
}


// XMLHttpRequest wrapper code

// XMLHttpRequest pool globals
JSONRpcClient.http_spare = [];
JSONRpcClient.http_max_spare = 8;

JSONRpcClient.poolGetHTTPRequest =
function JSONRpcClient_pool_getHTTPRequest()
{
    if(JSONRpcClient.http_spare.length > 0) {
	return JSONRpcClient.http_spare.pop();
    }
    return JSONRpcClient.getHTTPRequest();
}

JSONRpcClient.poolReturnHTTPRequest =
function JSONRpcClient_poolReturnHTTPRequest(http)
{
    if(JSONRpcClient.http_spare.length >= JSONRpcClient.http_max_spare)
	delete htpp;
    JSONRpcClient.http_spare.push(http);
}

JSONRpcClient.msxmlNames = [ "MSXML2.XMLHTTP.5.0",
			     "MSXML2.XMLHTTP.4.0",
			     "MSXML2.XMLHTTP.3.0",
			     "MSXML2.XMLHTTP",
			     "Microsoft.XMLHTTP" ];

JSONRpcClient.getHTTPRequest =
function JSONRpcClient_getHTTPRequest()
{
    // Mozilla XMLHttpRequest
    try {
	JSONRpcClient.httpObjectName = "XMLHttpRequest";
	return new XMLHttpRequest();
    } catch(e) {}

    // Microsoft MSXML ActiveX
    for (var i=0;i < JSONRpcClient.msxmlNames.length; i++) {
	try {
	    JSONRpcClient.httpObjectName = JSONRpcClient.msxmlNames[i];
	    return new ActiveXObject(JSONRpcClient.msxmlNames[i]);
	} catch (e) {}
    }

    // None found
    JSONRpcClient.httpObjectName = null;
    throw new JSONRpcClient.Exception(0, "Can't create XMLHttpRequest object");
}

