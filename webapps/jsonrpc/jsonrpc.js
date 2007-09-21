/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007 The jabsorb team
 * Copyright (c) 2005 Michael Clark, Metaparadigm Pte Ltd
 * Copyright (c) 2003-2004 Jan-Klaas Kollhof
 *
 * This code is based on original code from the json-rpc-java library
 * which was originally based on Jan-Klaas' JavaScript o lait library
 * (jsolait).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/* escape a character */

escapeJSONChar=function ()
{
  var escapeChars = ["\b","\t","\n","\f","\r"];

  return function(c){
    // Need to do these first as their ascii values are > 32 (34 & 92)
    if(c == "\"" || c == "\\")
    {
      return "\\"+c;
    }
    //Otherwise it doesn't need escaping
    if(c.charCodeAt(0)>=32)
    {
      return c;
    }
    // Otherwise it is has a code < 32 and may need escaping.
    for(var i=0;i<escapeChars.length;i++)
    {
      if(c==escapeChars[i])
      {
        return "\\" + c;
      }
    }
    // it was a character from 0-31 that wasn't one of the escape chars
    return c;  
  };
}();

/* encode a string into JSON format */

function escapeJSONString(s)
{
  /* The following should suffice but Safari's regex is b0rken
      (doesn't support callback substitutions)
      return "\"" + s.replace(/([^\u0020-\u007f]|[\\\"])/g,
      escapeJSONChar) + "\"";
   */

  /* Rather inefficient way to do it */
  var parts = s.split("");
  for (var i = 0; i < parts.length; i++)
  {
    parts[i] = escapeJSONChar(parts[i]);
  }
  return "\"" + parts.join("") + "\"";
}


/* Marshall objects to JSON format */

function toJSON(o)
{
  var v = [];
  if (o === null || o === undefined)
  {
    return "null";
  }
  else if (o.constructor == String)
  {
    return escapeJSONString(o);
  }
  else if (o.constructor == Number)
  {
    return o.toString();
  }
  else if (o.constructor == Boolean)
  {
    return o.toString();
  }
  else if (o.constructor == Date)
  {
    return '{javaClass: "java.util.Date", time: ' + o.valueOf() + '}';
  }
  else if (o.constructor == Array)
  {
    for (var i = 0; i < o.length; i++)
    {
      v.push(toJSON(o[i]));
    }
    return "[" + v.join(", ") + "]";
  }
  else
  {
    for (var attr in o)
    {
      if (!o[attr])
      {
        v.push("\"" + attr + "\": null");
      }
      else if (typeof o[attr] == "function")
      {
         /* skip */
      }
      else
      {
        v.push(escapeJSONString(attr) + ": " + toJSON(o[attr]));
      }
    }
    return "{" + v.join(", ") + "}";
  }
}


/* JSONRpcClient constructor */

function JSONRpcClient()
{
  var arg_shift = 0,
    req;

  //If a call back is being used grab it
  if (typeof arguments[0] == "function")
  {
    this.readyCB = arguments[0];
    arg_shift++;
  }
  //The next 3 args are passed to the http request
  this.serverURL = arguments[arg_shift + 0];
  this.user = arguments[arg_shift + 1];
  this.pass = arguments[arg_shift + 2];
  //A unique identifier which the identity hashcode of the object on the server, if this is a reference type
  this.objectID = arguments[arg_shift + 3];
  //The full package+classname of the object
  this.javaClass = arguments[arg_shift + 4];
  //The reference type this is: Reference or CallableReference
  this.JSONRPCType = arguments[arg_shift + 5];
  //if we have already made one of these classes before
  if(JSONRpcClient.knownClasses[this.javaClass]&&(this.JSONRPCType=="CallableReference"))
  {
    //Then add all the cached methods to it.
    for (var name in JSONRpcClient.knownClasses[this.javaClass]) 
    {
      var f = JSONRpcClient.knownClasses[this.javaClass][name];
      //Change the this to the object that will be calling it
      //Note: bind is JSONRPC.bind
      this[name]=JSONRpcClient.bind(f,this);
    }
  }
  else
  {
    //If we are here, it is either the first time an object of this type has 
    //been created or the bridge
    var req;
    // If it is an object list the methods for it
    if(this.objectID) 
    {
      this._addMethods(["listMethods"],this.javaClass);
      req = this._makeRequest("listMethods", []);
    } 
    //If it is the bridge get the bridge's methods
    else 
    {
      this._addMethods(["system.listMethods"],this.javaClass);
      req = this._makeRequest("system.listMethods", []);
    }
    var m = this._sendRequest(req);
    //Now add the methods to the object
    this._addMethods(m,this.javaClass);
  }
  //If a callback was added to the constructor, call it
  if (this.readyCB)
  {
    var self = this;
    req.cb = function (result, e)
    {
      if (!e)
      {
        self._addMethods(result);
      }
      self.readyCB(result, e);
    };
  }
}
//This is a static variable that maps className to a map of functions names to 
//calls, ie Map knownClasses<ClassName,Map<FunctionName,Function>>
JSONRpcClient.knownClasses = {};

/* JSONRpcCLient.Exception */
JSONRpcClient.Exception = function (code, message, javaStack)
{
  this.code = code;
  var name,m;
  if (javaStack)
  {
    this.javaStack = javaStack;
    m = javaStack.match(/^([^:]*)/);
    if (m)
    {
      name = m[0];
    }
  }
  if (name)
  {
    this.name = name;
  }
  else
  {
    this.name = "JSONRpcClientException";
  }
  this.message = message;
};

//Error codes that are the same as on the bridge
JSONRpcClient.Exception.CODE_REMOTE_EXCEPTION = 490;
JSONRpcClient.Exception.CODE_ERR_CLIENT = 550;
JSONRpcClient.Exception.CODE_ERR_PARSE = 590;
JSONRpcClient.Exception.CODE_ERR_NOMETHOD = 591;
JSONRpcClient.Exception.CODE_ERR_UNMARSHALL = 592;
JSONRpcClient.Exception.CODE_ERR_MARSHALL = 593;

JSONRpcClient.Exception.prototype = new Error();

JSONRpcClient.Exception.prototype.toString = function (code, msg)
{
  return this.name + ": " + this.message;
};


/* Default top level exception handler */

JSONRpcClient.default_ex_handler = function (e)
{
  alert(e);
};


/* Client settable variables */

JSONRpcClient.toplevel_ex_handler = JSONRpcClient.default_ex_handler;
JSONRpcClient.profile_async = false;
JSONRpcClient.max_req_active = 1;
JSONRpcClient.requestId = 1;

/**
 * Used to bind the this of the serverMethodCaller() (see below) which is to be
 * bound to the right object. This is needed as the serverMethodCaller is 
 * called only once in createMethod and is then assigned to multiple 
 * CallableReferences are created.
 */
JSONRpcClient.bind=function(functionName,context)
{
  return function() {
    return functionName.apply(context, arguments);
  }
}

/* 
 * This creates a method that points to the serverMethodCaller and binds it 
 * with the correct methodName.
 */
JSONRpcClient.prototype._createMethod = function (methodName)
{
  //This function is what the user calls.
  //This function uses a closure on methodName to ensure that the function 
  //always has the same name, but can take different arguments each call.
  //Each time it is added to an object this should be set with bind()
  var serverMethodCaller= function()
  {
    var args = [], 
      callback;
    for (var i = 0; i < arguments.length; i++)
    {
      args.push(arguments[i]);
    }
    if (typeof args[0] == "function")
    {
      callback = args.shift();
    }
    var req = this._makeRequest.call(this, methodName, args, callback);
    if (!callback) 
    {
      return this._sendRequest.call(this, req);
    } 
    else 
    {
      JSONRpcClient.async_requests.push(req);
      JSONRpcClient.kick_async();
      return req.requestId;
    }
  };

  return serverMethodCaller;
};

/**
 * This is used to add a list of methods to this.
 * @param methodNames a list containing the names of the methods to add
 * @param javaClass If here it signifies that the function is part of the class
 *   and should be cached.
 */
JSONRpcClient.prototype._addMethods = function (methodNames,javaClass)
{ 
  //Aha! It is a class, so create a entry for it.
  //This shouldn't get called twice on the same class so we can happily
  //overwrite it
  if(javaClass){
    JSONRpcClient.knownClasses[javaClass]={};
  }
  var name;
  for (var i = 0; i < methodNames.length; i++)
  {
  
    var obj = this;
    var names = methodNames[i].split(".");
    //In the case of system.listMethods create a new object in this called
    //system and and the listMethod function to that object.
    for (var n = 0; n < names.length - 1; n++)
    {
      name = names[n];
      if (obj[name])
      {
        obj = obj[name];
      }
      else
      {
        obj[name] = {};
        obj = obj[name];
      }
    }
    //The last part of the name is the actual functionName
    name = names[names.length - 1];
    //If it doesn't yet exist (why would it??)
    if (!obj[name])
    {
      //Then create the method
      var method = this._createMethod(methodNames[i]);
      //Bind it to the current this
      obj[name]=JSONRpcClient.bind(method,this);
      //And if this is adding it to an object, then
      //add it to the cache
      if(javaClass&&(name!="listMethods")){
        JSONRpcClient.knownClasses[javaClass][name]=method;
      }
    }
  }
};

JSONRpcClient._getCharsetFromHeaders = function (http)
{
  try
  {
    var contentType = http.getResponseHeader("Content-type");
    var parts = contentType.split(/\s*;\s*/);
    for (var i = 0; i < parts.length; i++)
    {
      if (parts[i].substring(0, 8) == "charset=")
      {
        return parts[i].substring(8, parts[i].length);
      }
    }
  }
  catch (e)
  {
  }

  return "UTF-8";
  /* default */
};

/* Async queue globals */
JSONRpcClient.async_requests = [];
JSONRpcClient.async_inflight = {};
JSONRpcClient.async_responses = [];
JSONRpcClient.async_timeout = null;
JSONRpcClient.num_req_active = 0;

JSONRpcClient._async_handler = function ()
{
  JSONRpcClient.async_timeout = null;

  while (JSONRpcClient.async_responses.length > 0)
  {
    var res = JSONRpcClient.async_responses.shift();
    if (res.canceled)
    {
      continue;
    }
    if (res.profile)
    {
      res.profile.dispatch = new Date();
    }
    try
    {
      res.cb(res.result, res.ex, res.profile);
    }
    catch(e)
    {
      JSONRpcClient.toplevel_ex_handler(e);
    }
  }

  while (JSONRpcClient.async_requests.length > 0 &&
         JSONRpcClient.num_req_active < JSONRpcClient.max_req_active)
  {
    var req = JSONRpcClient.async_requests.shift();
    if (req.canceled)
    {
      continue;
    }
    req.client._sendRequest.call(req.client, req);
  }
};

JSONRpcClient.kick_async = function ()
{
  if (!JSONRpcClient.async_timeout)
  {
    JSONRpcClient.async_timeout = setTimeout(JSONRpcClient._async_handler, 0);
  }
};

JSONRpcClient.cancelRequest = function (requestId)
{
  /* If it is in flight then mark it as canceled in the inflight map
      and the XMLHttpRequest callback will discard the reply. */
  if (JSONRpcClient.async_inflight[requestId])
  {
    JSONRpcClient.async_inflight[requestId].canceled = true;
    return true;
  }
  var i;

  /* If its not in flight yet then we can just mark it as canceled in
      the the request queue and it will get discarded before being sent. */
  for (i in JSONRpcClient.async_requests)
  {
    if (JSONRpcClient.async_requests[i].requestId == requestId)
    {
      JSONRpcClient.async_requests[i].canceled = true;
      return true;
    }
  }

  /* It may have returned from the network and be waiting for its callback
      to be dispatched, so mark it as canceled in the response queue
      and the response will get discarded before calling the callback. */
  for (i in JSONRpcClient.async_responses)
  {
    if (JSONRpcClient.async_responses[i].requestId == requestId)
    {
      JSONRpcClient.async_responses[i].canceled = true;
      return true;
    }
  }

  return false;
};

JSONRpcClient.prototype._makeRequest = function (methodName, args, cb)
{
  var req = {};
  req.client = this;
  req.requestId = JSONRpcClient.requestId++;

  var obj = {};
  obj.id = req.requestId;
  if (this.objectID)
  {
    obj.method = ".obj#" + this.objectID + "." + methodName;
  }
  else
  {
    obj.method = methodName;
  }
  obj.params = args;

  if (cb)
  {
    req.cb = cb;
  }
  if (JSONRpcClient.profile_async)
  {
    req.profile = { "submit": new Date() };
  }
  req.data = toJSON(obj);

  return req;
};

JSONRpcClient.prototype._sendRequest = function (req)
{
  if (req.profile)
  {
    req.profile.start = new Date();
  }

  /* Get free http object from the pool */
  var http = JSONRpcClient.poolGetHTTPRequest();
  JSONRpcClient.num_req_active++;

  /* Send the request */
  http.open("POST", this.serverURL, !!req.cb, this.user, this.pass);

  /* setRequestHeader is missing in Opera 8 Beta */
  try
  {
    http.setRequestHeader("Content-type", "text/plain");
  }
  catch(e)
  {
  }

  /* Construct call back if we have one */
  if (req.cb)
  {
    var self = this;
    http.onreadystatechange = function()
    {
      if (http.readyState == 4)
      {
        http.onreadystatechange = function ()
        {
        };
        var res = { "cb": req.cb, "result": null, "ex": null};
        if (req.profile)
        {
          res.profile = req.profile;
          res.profile.end = new Date();
        }
        try
        {
          res.result = self._handleResponse(http);
        }
        catch(e)
        {
          res.ex = e;
        }
        if (!JSONRpcClient.async_inflight[req.requestId].canceled)
        {
          JSONRpcClient.async_responses.push(res);
        }
        delete JSONRpcClient.async_inflight[req.requestId];
        JSONRpcClient.kick_async();
      }
    };
  }
  else
  {
    http.onreadystatechange = function()
    {
    };
  }

  JSONRpcClient.async_inflight[req.requestId] = req;

  try
  {
    http.send(req.data);
  }
  catch(e)
  {
    JSONRpcClient.poolReturnHTTPRequest(http);
    JSONRpcClient.num_req_active--;
    throw new JSONRpcClient.Exception(JSONRpcClient.Exception.CODE_ERR_CLIENT, "Connection failed");
  }

  if (!req.cb)
  {
    return this._handleResponse(http);
  }
};

JSONRpcClient.prototype._handleResponse = function (http)
{
  /* Get the charset */
  if (!this.charset)
  {
    this.charset = JSONRpcClient._getCharsetFromHeaders(http);
  }

  /* Get request results */
  var status, statusText, data;
  try
  {
    status = http.status;
    statusText = http.statusText;
    data = http.responseText;
  }
  catch(e)
  {
/*
    todo:   don't throw away the original error information here!!
    todo:   and everywhere else, as well!
    if (e instanceof Error)
    {
      alert (e.name + ": " + e.message);
    }
*/
    JSONRpcClient.poolReturnHTTPRequest(http);
    JSONRpcClient.num_req_active--;
    JSONRpcClient.kick_async();
    throw new JSONRpcClient.Exception(JSONRpcClient.Exception.CODE_ERR_CLIENT, "Connection failed");
  }

  /* Return http object to the pool; */
  JSONRpcClient.poolReturnHTTPRequest(http);
  JSONRpcClient.num_req_active--;

  /* Unmarshall the response */
  if (status != 200)
  {
    throw new JSONRpcClient.Exception(status, statusText);
  }
  var obj;
  try
  {
    eval("obj = " + data);
  }
  catch(e)
  {
    throw new JSONRpcClient.Exception(550, "error parsing result");
  }
  if (obj.error)
  {
    throw new JSONRpcClient.Exception (obj.error.code, obj.error.msg, obj.error.trace);
  }
  var res = obj.result;

  /* Handle CallableProxy */
  if (res && res.objectID && res.JSONRPCType == "CallableReference")
  {
    return new JSONRpcClient(this.serverURL, this.user, this.pass, 
      res.objectID,res.javaClass,res.JSONRPCType);
  }

  return res;
};

/* XMLHttpRequest wrapper code */

/* XMLHttpRequest pool globals */
JSONRpcClient.http_spare = [];
JSONRpcClient.http_max_spare = 8;

JSONRpcClient.poolGetHTTPRequest = function ()
{
  if (JSONRpcClient.http_spare.length > 0)
  {
    return JSONRpcClient.http_spare.pop();
  }
  return JSONRpcClient.getHTTPRequest();
};

JSONRpcClient.poolReturnHTTPRequest = function (http)
{
  if (JSONRpcClient.http_spare.length >= JSONRpcClient.http_max_spare)
  {
    delete http;
  }
  else
  {
    JSONRpcClient.http_spare.push(http);
  }
};

/* the search order here may seem strange, but it's
   actually what Microsoft recommends */
JSONRpcClient.msxmlNames = [
  "MSXML2.XMLHTTP.6.0",
  "MSXML2.XMLHTTP.3.0",
  "MSXML2.XMLHTTP",
  "MSXML2.XMLHTTP.5.0",
  "MSXML2.XMLHTTP.4.0",
  "Microsoft.XMLHTTP" ];

JSONRpcClient.getHTTPRequest = function ()
{
  /* Look for a browser native XMLHttpRequest implementation (Mozilla/IE7/Opera/Safari, etc.) */
  try
  {
    JSONRpcClient.httpObjectName = "XMLHttpRequest";
    return new XMLHttpRequest();
  }
  catch(e)
  {
  }

  /* Microsoft MSXML ActiveX for IE versions < 7 */
  for (var i = 0; i < JSONRpcClient.msxmlNames.length; i++)
  {
    try
    {
      JSONRpcClient.httpObjectName = JSONRpcClient.msxmlNames[i];
      return new ActiveXObject(JSONRpcClient.msxmlNames[i]);
    }
    catch (e)
    {
    }
  }

  /* None found */
  JSONRpcClient.httpObjectName = null;
  throw new JSONRpcClient.Exception(0, "Can't create XMLHttpRequest object");
};
