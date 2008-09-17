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

/**
 * Call this to create a jabsorb object. 
 */
var jabsorb = function()
{
  /** Assign private variables to this */
  var prv={};
  
  /** Assign public variables to this */
  var pub={};
  
  /* *************************** PUBLIC VARIABLES *************************** */
  
  /**
   * if this is true, circular references in the object graph are fixed up
   * if this is false, circular references cause an exception to be thrown
   */
  pub.fixupCircRefs = true;
  
  /**
   * if this is true, duplicate objects in the object graph are optimized
   * if it's false, then duplicate objects are "re-serialized"
   */
  pub.fixupDuplicates = true;

  /** The number of requests that can occur at once*/
  pub.max_req_active = 1;
  
  /** Whether the time that each async call takes should be measured*/
  pub.profile_async = false;

  /** The id of the next request*/
  pub.requestId = 1;
  
  /**
   * if true, java.util.Date object are unmarshalled to javascript dates
   * if false, no customized unmarshalling for dates is done
   */
  pub.transformDates = false;
  
  /* ************************** PRIVATE VARIABLES *************************** */
  
  /**Private static final variables */
  prv.CALLABLE_REFERENCE_METHOD_PREFIX = ".ref";
  
  /**
   * This is a static variable that maps className to a map of functions names 
   * to calls, ie Map knownClasses<ClassName,Map<FunctionName,Function>>
   */
  prv.knownClasses = {};
  
  /* **************************** INNER CLASSES ***************************** */
  /** 
   * Callable Proxy constructor
   * 
   * @param objectID A unique identifier which the identity hashcode of the 
   *        object on the server, if this is a reference type
   * @param javaclass The full package and classname of the object 
   */
  prv.JSONRPCCallableProxy=function(objectID,javaclass)
  {
    this.prv=prv;
    this.pub=pub;
    
    this.prv.objectID = objectID;
    this.pub.javaClass = javaclass;
    this.prv.JSONRPCType = "CallableReference";
  }
    
  /**
   * Exception Constructor
   * 
   * @param errorObject An object which describes the error. 
   */
  prv.Exception = function (errorObject)
  {
    var m;
    for( var prop in errorObject) 
    {
      if (errorObject.hasOwnProperty(prop)) 
      {
        this[prop] = errorObject[prop];
      }
    }
    if (this.trace)
    {
      m = this.trace.match(/^([^:]*)/);
      if (m)
      {
        this.name = m[0];
      }
    }
    if (!this.name)
    {
      this.name = "JSONRpcClientException";
    }
  };
  //Error codes that are the same as on the bridge
  prv.Exception.CODE_REMOTE_EXCEPTION = 490;
  prv.Exception.CODE_ERR_CLIENT = 550;
  prv.Exception.CODE_ERR_PARSE = 590;
  prv.Exception.CODE_ERR_NOMETHOD = 591;
  prv.Exception.CODE_ERR_UNMARSHALL = 592;
  prv.Exception.CODE_ERR_MARSHALL = 593;
  
  prv.Exception.prototype = new Error();
  
  prv.Exception.prototype.toString = function (code, msg)
  {
    var str="";
    if(this.name)
    {
      str+=this.name;
    }
    if(this.message)
    {
      str+=": "+this.message;
    }
    if(str.length==0)
    {
      str="no exception information given";
    }
    return str;
  };
  
  /* **************************** CONSTRUCTORS ****************************** */
  /** 
   * Jabsorb constructor
   * 
   * @param callback|methods 
   *                   The function to call once the rpc list methods 
   *                   has completed.  If this argument is omitted completely, 
   *                   then jabsorb is constructed synchronously. If this 
   *                   argument is an array then it is the list of methods that 
   *                   can be invoked on the server (and the server will not be 
   *                   queried for that information)
   * @param serverURL  Path to JSONRpcServlet on server.
   * @param user       The user name to connect to the server (if necessary)
   * @param pass       The password to connect to the server (if necessary)
   */
  prv.init=function()
  {
    var argShift = 0,
        callbackPos=0,
        req,
        callbackArgType= (typeof arguments[callbackPos]),
        doListMethods=true,
        readyCB;
  
    //If a call back is being used grab it
    if (callbackArgType === "function")
    {
      readyCB = arguments[callbackPos];
      argShift++;
    }
    // if it's an array then just do add methods directly
    else if (
                arguments[callbackPos] 
             && callbackArgType === "object" 
             && arguments[0].length)
    {
      // go ahead and add the methods directly
      prv.addMethods(pub,arguments[callbackPos]); 
      argShift++;
      doListMethods=false;
    }
  
    //The next 3 args are passed to the http request
    prv.serverURL = arguments[argShift];
    prv.user = arguments[argShift + 1];
    prv.pass = arguments[argShift + 2];
    prv.objectID=0;
  
    if (doListMethods)
    {
      //Add the listMethods system methods
      prv.addMethods(pub,["system.listMethods"]);
      //Make the call to list the methods
      req = comms.makeRequest("system.listMethods", []);
      //If a callback was added to the constructor, call it
      if(readyCB) 
      {
        req.cb = function(result, e) 
        {
           if(!e) 
           {
             prv.addMethods(pub,result);
           }
           readyCB(pub, e);
        };
        comms.addRequest(req);
      }
      else
      {
        prv.addMethods(pub,comms.sendRequest(req));
      }
    }
  }
  
  /* **************************** PUBLIC METHODS **************************** */
  
  /**
   * Cancels a request that is currentlyin progress
   * 
   * @param requestId The id of the request to cancel
   */
  pub.cancelRequest=function(requestId)
  {
    comms.cancelRequest(requestId);
  };

  /**
   * Creates a new object from the bridge. A callback may optionally be given as
   * the first argument to make this an async call.
   *
   * @param callback (optional)
   * @param constructorName The name of the class to create, which should be 
   *   registered with JSONRPCBridge.registerClass()
   * @param constructorArgs The arguments the constructor takes
   * @return the new object if sync, the request id if async.
   */
  pub.createObject = function ()
  {
    var args = [],
        callback = null,
        constructorName,
        constructorArgs,
        req;
    for(var i=0;i<arguments.length;i++)
    {
      args.push(arguments[i]);
    }
    if(typeof args[0] == "function")
    {
      callback = args.shift();
    }
    constructorName=args[0]+".$constructor";
    constructorArgs=args[1];      
          
    req = comms.makeRequest(constructorName, constructorArgs, 0,callback);
    if(callback === null) 
    {
      return comms.sendRequest(req);
    }
    else 
    {
      comms.addRequest(req);
      return req.requestId;
    }
  };

  /**
   * Default top level exception handler 
   */
  pub.default_ex_handler = function (e)
  {
    var str="";
    for(a in e)
    {
      str+=a +"\t"+e[a]+"\n";
    }
    alert(str);
  };    

  /**
   * Marshall an object to JSON format.
   * Circular references can be handled if the parameter 
   * jabsorb.fixupCircRefs is true.  If the parameter is false,
   * an exception will be thrown if a circular reference is detected.
   *
   * if the parameter, jabsorb.fixupDuplicates is true then duplicate objects in
   * the object graph are also combined except for Strings
   *
   * (todo: it wouldn't be too hard to optimize strings as well, but probably a threshold
   *  should be provided, so that only strings over a certain length would be optimized)
   * This would be worth doing on the upload (on the download it's not so important, because
   * gzip handles this)
   * if it's false, then duplicate objects are "re-serialized"
   *
   * @param o The object being converted to json
   *
   * @return an object, { 'json': jsonString, 'fixups': fixupString } or
   *            just { 'json' : jsonString }  if there were no fixups found.
   */
  pub.toJSON=function (o)
  {
    // to detect circular references and duplicate objects, each object has a special marker
    // added to it as we go along.
  
    // therefore we know if the object is either a duplicate or circular ref if the object
    // already has this marker in it before we process it.
  
    // the marker object itself contains two pointers-- one to the last object processed
    // and another to the parent object
    // therefore we can rapidly detect if an object is a circular reference
    // by following the chain of parent pointer objects to see if we find the same object again.
  
    // if we don't find the same object again in the parent recursively, then we know that it's a
    // duplicate instead of a circular reference
  
    // the pointer to the last object processed is used to link all processed objects together
    // so that the marker objects can be removed when the operation is complete
  
    // once all objects are processed, we can traverse the linked list, removing all the markers
  
    // the special name for the marker object
    // try to pick a name that would never be used for any other purpose, so
    // it won't conflict with anything else
    var marker="$_$jabsorbed$813492";
  
    // the head of the marker object chain
    var markerHead;
  
    // fixups detected as we go along, both for circular references and duplicates
    var fixups = [];
  
    // unlink the whole chain of marker objects that were added to objects when processing
    function removeMarkers()
    {
      var next;
      while (markerHead)
      {
        next = markerHead[marker].prev;
        delete markerHead[marker];
        markerHead = next;
      }
    }
  
    // special object used to indicate that an object should be omitted
    // because it was found to be a circular reference or duplicate
    var omitCircRefOrDuplicate = {};
  
    // temp variable to hold json while processing
    var json;
  
    /**
     * Do the work of converting an individual "sub" object to JSON.
     * Each object that is processed has a special marker object attached to it, 
     * to quickly detect if it has already been processed and thus handle 
     * circular references and duplicates.
     *
     * @param o   object being converted into JSON.
     * 
     * @param p   the parent of the object being processed, it should be null or
     *            undefined if it's the root object.
     * 
     * @param ref the "reference" of the object in the parent that is being 
     *            converted such that p[ref] === o.
     * 
     * @return A string containing the JSON representation of the object o, but 
     *         with duplicates and circular references removed (according to the 
     *         option settings jabsorb.fixupCircRefs and 
     *         jabsorb.fixupDuplicates. 
     */
    function subObjToJSON(o,p,ref)
    {
      var v = [],
          // list of references to get to the fixup entry
          fixup,
          // list of reference to get to the original location
          original,
          parent,
          circRef,
          i;
          
      if (o === null || o === undefined)
      {
        return "null";  // it's null or undefined, so serialize it as null
      }
      else if (typeof o === 'string')
      {
        //todo: handle duplicate strings!  but only if they are over a certain threshold size...
        return prv.escapeJSONString(o);
      }
      else if (typeof o === 'number')
      {
        return o.toString();
      }
      else if (typeof o === 'boolean')
      {
        return o.toString();
      }
      else
      {
        // must be an object type
  
        // look for an already existing marker which would mean this object has already been processed
        // at least once and therefore either a circular ref or dup has been found!!
        if (o[marker])
        {
          // determine if it's a circular reference
          fixup = [ref];
          parent = p;
  
          // walk up the parent chain till we find null
          while (parent)
          {
            // if a circular reference was found somewhere along the way,
            // calculate the path to it as we are going
            if (original)
            {
              original.unshift (parent[marker].ref);
            }
  
            // if we find ourself, then we found a circular reference!
            if (parent===o)
            {
              circRef=parent;
              original = [circRef[marker].ref];
            }
  
            fixup.unshift(parent[marker].ref);
            parent = parent[marker].parent;
          }
  
          // if we found ourselves in the parent chain then this is a circular 
          // reference
          if (circRef)
          {
            //either save off the circular reference or throw an exception, 
            //depending on the client setting
            if (pub.fixupCircRefs)
            {
              // remove last redundant unshifted reference
              fixup.shift();
              original.shift();
  
              //todo: (LATER) if multiple fixups go to the same original, this 
              // could be optimized somewhat
              fixups.push([fixup, original]);
              return omitCircRefOrDuplicate;
            }
            else
            {
              removeMarkers();
              throw new Error("circular reference detected!");
            }
          }
          else
          {
            // otherwise it's a dup!
            if (pub.fixupDuplicates)
            {
              // find the original path of the dup
              original = [o[marker].ref];
              parent = o[marker].parent;
              while (parent)
              {
                original.unshift(parent[marker].ref);
                parent = parent[marker].parent;
              }
              //todo: (LATER) if multiple fixups go to the same original, this 
              // could be optimized somewhat
  
              // remove last redundant unshifted reference
              fixup.shift();
              original.shift();
  
              fixups.push([fixup, original]);
              return omitCircRefOrDuplicate;
            }
          }
        }
        else
        {
          // mark this object as visited/processed and set up the parent link 
          // and prev link
          o[marker] = {parent:p, prev:markerHead, ref:ref};
  
          // adjust the "marker" head pointer so the prev pointer on the next 
          // object processed can be set
          markerHead = o;
        }
  
        if (o.constructor === Date)
        {
          return '{javaClass: "java.util.Date", time: ' + o.valueOf() + '}';
        }
        else if (o.constructor === Array)
        {
          for (i = 0; i < o.length; i++)
          {
            json = subObjToJSON(o[i], o, i);
  
            // if it's a dup/circ ref, put a slot where the object would have 
            // been, otherwise put the json data here
            v.push(json===omitCircRefOrDuplicate?null:json);
          }
          return "[" + v.join(", ") + "]";
        }
        else
        {
          for (var attr in o)
          {
            if (attr === marker)
            {
               /* skip */
            }
            else if (o[attr] === null || o[attr] === undefined)
            {
              v.push("\"" + attr + "\": null");
            }
            else if (typeof o[attr] == "function")
            {
               /* skip */
            }
            else
            {
              json = subObjToJSON(o[attr], o, attr);
              if (json !== omitCircRefOrDuplicate)
              {
                v.push(prv.escapeJSONString(attr) + ": " + json);
              }
            }
          }
          return "{" + v.join(", ") + "}";
        }
      }
    }
  
    json = subObjToJSON(o, null, "root");
  
    removeMarkers();
  
    // only return the fixups if one or more were found
    if (fixups.length)
    {
      return {json: json, fixups: fixups};
    }
    else
    {
      return {json: json};
    }
  }

  /**
   * Converts a json string into jabsorb data types.
   * 
   * @param data The json string to parse. It should contain a value in the 
   *             result field.
   * @return If data contains any Callable Proxies these will be extracted, 
   *         otherwise, it returns the string converted into javascript objects.
   */
  pub.unmarshallResponse=function(data)
  {
    /**
     * Apply fixups.
     * 
     * @param obj    root object to apply fixups against.
     * @param fixups array of fixups to apply. Each element of the array is a 2 
     *               element array, containing the array with the fixup location 
     *               followed by an array with the original location to fix up 
     *               into the fixup location.
     */
    function applyFixups(obj, fixups)
    {
      function findOriginal(ob, original)
      {
        for (var i=0,j=original.length;i<j;i++)
        {
          ob = ob[original[i]];
        }
        return ob;
      }
      function applyFixup(ob, fixups, value)
      {
        var j=fixups.length-1;
        for (var i=0;i<j;i++)
        {
          ob = ob[fixups[i]];
        }
        ob[fixups[j]] = value;
      }
      for (var i = 0,j = fixups.length; i < j; i++)
      {
        applyFixup(obj,fixups[i][0],findOriginal(obj,fixups[i][1]));
      }
    }
    
    /**
     * Traverse the resulting object graph and replace serialized date objects with javascript dates. An object is 
     * replaced with a JS date when any of the following conditions is true:
     *   The object has a class hint, and the value of the hint is 'java.util.Date'
     *   The object does not have a class hint, and the ONE AND ONLY property is 'time'
     * Note that the traversal creates an infinite loop if the object graph is not a DAG, so do not call this function 
     * after fixing up circular refs.
     * 
     * @param obj root of the object graph where dates should be replaces.
     * @return object graph where serialized date objects are replaced by javascript dates.
     */
    function transformDate(obj) 
    {
      var hint,foo,num,i,jsDate
      if (obj && typeof obj === 'object')
      {
        hint = obj.hasOwnProperty('javaClass');
        foo = hint ? obj.javaClass === 'java.util.Date' : obj.hasOwnProperty('time');
        num = 0;
        // if there is no class hint but the object has 'time' property, count its properties
        if (!hint && foo)
        {
          for (i in obj) 
          {
            if (obj.hasOwnProperty(i)) 
            {
              num++;
            }
          }
        }
        // if class hint is java.util.Date or no class hint set, but the only property is named 'time', we create jsdate
        if (hint && foo || foo && num === 1) 
        {
          jsDate = new Date(obj.time);
          return jsDate;
        } 
        else
        {
          for (i in obj) 
          { 
            if (obj.hasOwnProperty(i))
            {
              obj[i] = transformDate(obj[i]);
            }
          }
          return obj;
        }
      } 
      else 
      {
        return obj;
      }
    } 
  
    var obj;
    try
    {
      eval("obj = " + data);
    }
    catch(e)
    {
      throw new prv.Exception({ code: 550, message: "error parsing result" });
    } 
    if (obj.error)
    {
      throw new prv.Exception (obj.error);
    }
    console.log(data)
    console.log(obj.result)
    var x = obj;
    var r = x.result;
    console.log(r)
    
    // look for circular reference/duplicates fixups and execute them 
    // if they are there
    
    var i,tmp;
      
    /* Handle CallableProxy */
    if (r) 
    {
      if(r.objectID && r.JSONRPCType == "CallableReference")
      {  
        return prv.createCallableProxy(r.objectID,r.javaClass);
      }
      else
      {
        r=prv.extractCallableReferences(pub.transformDates ? transformDate(r) : r);
        if (obj.fixups)
        {
          applyFixups(r,obj.fixups);
        }
      }
    }
    return r;
  };
  
  /* *************************** PRIVATE METHODS **************************** */
  
  /**
   * This is used to add a list of methods to this.
   * 
   * @param the object that methods should be added to
   * @param methodNames a list containing the names of the methods to add
   * @param dontAdd If this is set, methods wont actually added
   * @return the methods that were created
   */
  prv.addMethods = function (toAddTo,methodNames,dontAdd)
  {
    var name,
        names,
        n,
        method,
        methods=[],
        javaClass,
        tmpNames,
        startIndex,
        endIndex,
        tmp;
    
    for (var i = 0; i < methodNames.length; i++)
    {
      names = methodNames[i].split(".");
      startIndex=methodNames[i].indexOf("[");
      endIndex=methodNames[i].indexOf("]");
      if(
          (methodNames[i].substring(0,
            prv.CALLABLE_REFERENCE_METHOD_PREFIX.length)==
            prv.CALLABLE_REFERENCE_METHOD_PREFIX)
        &&(startIndex!=-1)&&(endIndex!=-1)&&(startIndex<endIndex))        
      {
        javaClass=methodNames[i].substring(startIndex+1,endIndex);
      }
      else
      {
        //Create intervening objects in the path to the method name.
        //For example with the method name "system.listMethods", we first
        //create a new object called "system" and then add the "listMethod"
        //function to that object.
        tmp=toAddTo;
        for (n = 0; n < names.length - 1; n++)
        {
          name = names[n];
          if (tmp[name])
          {
            tmp = tmp[name];
          }
          else
          {
            tmp[name] = {};
            tmp = tmp[name];
          }
        }
      }
      //The last part of the name is the actual functionName
      name = names[names.length - 1];
  
      //Create the method
      
      if(javaClass)
      {
        method = prv.createMethod(name);
        if(!prv.knownClasses[javaClass])
        {
          prv.knownClasses[javaClass]={};
        }
        prv.knownClasses[javaClass][name]=method;
      }
      else
      {
        method = prv.createMethod(methodNames[i]);
        //If it doesn't yet exist and it is to be added to this
        if ((!toAddTo[name])&&(!dontAdd))
        {
          tmp[name]=prv.bind(method,this);
        }
        //maintain a list of all methods created so that methods[i]==methodNames[i]
        methods.push(method);
      }
      javaClass=null;
    }
  
    return methods;
  };
  
  /**
   * Used to bind the this of the serverMethodCaller() (see below) which is to be
   * bound to the right object. This is needed as the serverMethodCaller is
   * called only once in createMethod and is then assigned to multiple
   * CallableReferences are created.
   */
  prv.bind=function(functionName,context)
  {
    return function() {
      return functionName.apply(context, arguments);
    };
  };
  
  
  /**
   * Creates a new callable proxy (reference). 
   *
   * @param objectID The id of the object as determined by the server
   * @param javaClass The package+classname of the object
   * @return a new callable proxy object
   */
  prv.createCallableProxy=function(objectID,javaClass)
  {
    var cp,req,methodNames,name,i;
    
    cp = new prv.JSONRPCCallableProxy(objectID,javaClass);
    //Then add all the cached methods to it.
    for (name in prv.knownClasses[javaClass])
    {
      //Change the this to the object that will be calling it
      cp.pub[name]=prv.bind(
        prv.knownClasses[javaClass][name],cp);
    }
    return cp.pub;
  };

  /**
   * This creates a method that points to the serverMethodCaller and binds it
   * with the correct methodName.
   * 
   * @param methodName The name of the method to create.
   */
  prv.createMethod = function (methodName)
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
      var req = comms.makeRequest(methodName, args, prv.objectID,callback);
      if (!callback)
      {
        return comms.sendRequest(req);
      }
      else
      {
        //when there is a callback, add the req to the list 
        comms.addRequest(req);
        return req.requestId;
      }
    };
  
    return serverMethodCaller;
  };
  
  /** 
   * Escapes a character, ensuring any character with a code < 32 is escaped
   * 
   * @param c The character to escape
   * @return The escaped character 
   */
  prv.escapeJSONChar=function ()
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
  
  /** 
   * Encodes a string into JSON format
   * 
   * @param s the string to escape
   * @return The escaped json string 
   */
  prv.escapeJSONString=function(s)
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
      parts[i] = prv.escapeJSONChar(parts[i]);
    }
    return "\"" + parts.join("") + "\"";
  }  
  
  
  /**
   * Recursivly extracts objects in the form:
   *
   * {"objectID":x "javaClass":y "JSONRPCType":"CallableReference"}
   * 
   * and replaces it with a real callabe proxy.
   * 
   * @param root the top level element to start processing from. The values in 
   *             this will change.
   * @return     The same root parameter, with the values fixed up.  
   */
  prv.extractCallableReferences = function(root)
  {
    var i,tmp,value;
    for (i in root)
    {
      if(typeof(root[i])=="object")
      {
        tmp=prv.makeCallableReference(root[i]);
        if(tmp)
        {
          root[i]=tmp;
        }
        else
        {
          tmp=prv.extractCallableReferences(root[i]);
          root[i]=tmp;
        }
      }
      if(typeof(i)=="object")
      {
        tmp=prv.makeCallableReference(i);
        if(tmp)
        {
          value=root[i];
          delete root[i];
          root[tmp]=value;
        }
        else
        {
          tmp=prv.extractCallableReferences(i);
          value=root[i];
          delete root[i];
          root[tmp]=value;
        }
      }
    }
    return root;
  };

  /**
   * Creates a callable reference on an object if it fits the form:
   * 
   * {"objectID":x "javaClass":y "JSONRPCType":"CallableReference"}
   * 
   * @param Value The object to make the callable reference from
   * @return The callable reference or null, if it couldn't be created.
   */
  prv.makeCallableReference = function(value)
  {
    if(value && value.objectID && value.javaClass && value.JSONRPCType == "CallableReference")
    {
      return prv.createCallableProxy(value.objectID,value.javaClass);
    }
    return null;
  };
  
  /* **************************** HIDDEN MODELES **************************** */
  
  /**
   * This object holds functions that deal with data communications with the server
   */
  var comms=function()
  {
    
    /** Private variables*/
    comms_pub={};
    /** Public variables*/
    comms_prv={};
    
    /* Async queue globals */
    
    /* ************************* PRIVATE VARIABLES ************************** */
    
    /** 
     * Maps the id of the request to the request of requests that have been sent
     * but have not got a response.
     */
    comms_prv.asyncInflight = {};
  
    /** Requests that have not been sent yet. */
    comms_prv.asyncRequests = [];
    
    /** Responses that have been returned but not yet been dealt with*/
    comms_prv.asyncResponses = [];
    
    /** A time out for the asyncHandler function so it runs regularly*/
    comms_prv.asyncTimeout = null;
    
    /**
     * The names with which an MS ActiveXObject should be created.
     * 
     * The search order here may seem strange, but it's actually what Microsoft 
     * recommends 
     */
    comms_prv.msxmlNames = [
      "MSXML2.XMLHTTP.6.0",
      "MSXML2.XMLHTTP.3.0",
      "MSXML2.XMLHTTP",
      "MSXML2.XMLHTTP.5.0",
      "MSXML2.XMLHTTP.4.0",
      "Microsoft.XMLHTTP" ];
  
    /** The number of requests currently active. */
    comms_prv.numReqActive = 0;
    
    /* ************************** PUBLIC METHODS **************************** */
    
    /**
     * Add a request to be sent to the server.
     * 
     * @param req The request (which should have been created by makeRequest())
     *            to send.
     */
    comms_pub.addRequest = function(req)
    {
      comms_prv.asyncRequests.push(req);
      comms_prv.kickAsync();
    }
  
    /**
     * Ensures that the response for the given request will be no longer 
     * handled. This must be called before the response is acted upon, but can 
     * be called after it is received.
     * 
     * @param requestId The id of the request to cancel.  
     */
    comms_pub.cancelRequest = function (requestId)
    {
      /* If it is in flight then mark it as canceled in the inflight map
          and the XMLHttpRequest callback will discard the reply. */
      if (comms_prv.asyncInflight[requestId])
      {
        comms_prv.asyncInflight[requestId].canceled = true;
        return true;
      }
      var i;
    
      /* If its not in flight yet then we can just mark it as canceled in
          the the request queue and it will get discarded before being sent. */
      for (i in comms_prv.asyncRequests)
      {
        if (comms_prv.asyncRequests[i].requestId == requestId)
        {
          comms_prv.asyncRequests[i].canceled = true;
          return true;
        }
      }
    
      /* It may have returned from the network and be waiting for its callback
          to be dispatched, so mark it as canceled in the response queue
          and the response will get discarded before calling the callback. */
      for (i in comms_prv.asyncResponses)
      {
        if (comms_prv.asyncResponses[i].requestId == requestId)
        {
          comms_prv.asyncResponses[i].canceled = true;
          return true;
        }
      }
    
      return false;
    };
  
    /* ************************** PRIVATE METHODS *************************** */
    
    /**
     * Calls the asyncHandler right away.
     */
    comms_prv.kickAsync = function ()
    {
      if (!comms_prv.asyncTimeout)
      {
        comms_prv.asyncTimeout = setTimeout(comms_prv.asyncHandler, 0);
      }
    };
  
    /**
     * Makes a request to send to the server.
     * @param methodName    The method to call on the server
     * @param args          The arguments to send with the method
     * @param objectID      (optional) The id of the proxy to call this upon. 
     * @param cb            (optional) A callback which will be called when the 
     *                      response is received.
     */
    comms_pub.makeRequest = function (methodName, args,objectID,cb)
    {
      var req = {};
      req.requestId = pub.requestId++;
    
      var obj = "{id:"+req.requestId+",method:";
    
      if ((objectID)&&(objectID>0))
      {
        obj += "\".obj[" + objectID + "]." + methodName +"\"";
      }
      else
      {
        obj += "\"" + methodName + "\"";
      }
    
      if (cb)
      {
        req.cb = cb;
      }
      if (pub.profile_async)
      {
        req.profile = {submit: new Date() };
      }
    
      // use p as an alias for params to save space in the fixups
      var j= pub.toJSON(args);
    
      obj += ",params:" + j.json;
     
      // only attach duplicates/fixups if they are found
      // this is to provide graceful backwards compatibility to the json-rpc spec.
      if (j.fixups)
      {
        // todo: the call to toJSON here to turn the fixups into json is a bit 
        // inefficient, since there will never be fixups in the fixups... but
        // it saves us from writing some additional code at this point...
        obj += ",fixups:" + pub.toJSON(j.fixups).json;  
      }
    
      req.data = obj + "}";
    
      return req;
    };
    /**
     * Sends a request to the server.
     * 
     * @param req A request created with makeRequest().
     */
    comms_pub.sendRequest = function (req)
    {
      var http;
      if (req.profile)
      {
        req.profile.start = new Date();
      }
    
      /* Get free http object from the pool */
      http = xhr.poolGetHTTPRequest();
      comms_prv.numReqActive++;
    
      /* Send the request */
      http.open("POST", prv.serverURL, !!req.cb, prv.user, prv.pass);
    
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
        http.onreadystatechange = function()
        {
          var res;
          if (http.readyState == 4)
          {
            http.onreadystatechange = function ()
            {
            };
            res = {cb: req.cb, result: null, ex: null};
            if (req.profile)
            {
              res.profile = req.profile;
              res.profile.end = new Date();
            }
            else
            {
              res.profile = false;
            }
            try
            {
              res.result = comms_prv.handleResponse(http);
            }
            catch(e)
            {
              res.ex = e;
            }
            if (!comms_prv.asyncInflight[req.requestId].canceled)
            {
              comms_prv.asyncResponses.push(res);
            }
            delete comms_prv.asyncInflight[req.requestId];
            comms_prv.kickAsync();
          }
        };
      }
      else
      {
        http.onreadystatechange = function()
        {
        };
      }
    
      comms_prv.asyncInflight[req.requestId] = req;
    
      try
      {
        http.send(req.data);
      }
      catch(e)
      {
        xhr.poolReturnHTTPRequest(http);
        comms_prv.numReqActive--;
        throw new prv.Exception(
          { 
            code: prv.Exception.CODE_ERR_CLIENT, 
            message: "Connection failed" 
          } );
      }
    
      if (!req.cb)
      {
        delete comms_prv.asyncInflight[req.requestId]; 
        return comms_prv.handleResponse(http);
      }
      return null;
    };
    
    /**
     * Responds to requests being received.
     */
    comms_prv.asyncHandler = function ()
    {
      var res,
          req;
      comms_prv.asyncTimeout = null;
    
      while (comms_prv.asyncResponses.length > 0)
      {
        res = comms_prv.asyncResponses.shift();
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
          pub.toplevel_ex_handler(e);
        }
      }
    
      while (comms_prv.asyncRequests.length > 0 &&
             comms_prv.numReqActive < pub.max_req_active)
      {
        req = comms_prv.asyncRequests.shift();
        if (req.canceled)
        {
          continue;
        }
        comms.sendRequest(req);
      }
    };
  
    /**
     * Tries to determine the correct character set for a server connection.
     * 
     * @param http The connection to the server.
     */
    comms_prv.getCharsetFromHeaders = function (http)
    {
      var contentType,
          parts,
          i;
      try
      {
        contentType = http.getResponseHeader("Content-type");
        parts = contentType.split(/\s*;\s*/);
        for (i = 0; i < parts.length; i++)
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
      return "UTF-8"; // default
    };
      
    /**
     * Deals with a response from the server.
     *
     * @param http The connection to the server
     */
    comms_prv.handleResponse = function (http)
    {
      /* Get the charset */
      if (!this.charset)
      {
        this.charset = comms_prv.getCharsetFromHeaders(http);
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
        xhr.poolReturnHTTPRequest(http);
        comms_prv.numReqActive--;
        comms_prv.kickAsync();
        throw new prv.Exception(
          { 
            code: prv.Exception.CODE_ERR_CLIENT, 
            message: "Connection failed" 
          });
      }
    
      /* Return http object to the pool; */
      xhr.poolReturnHTTPRequest(http);
      comms_prv.numReqActive--;
    
      /* Unmarshall the response */
      if (status != 200)
      {
        throw new prv.Exception({ code: status, message: statusText });
      };
      return pub.unmarshallResponse(data);
    };
  
    /**
     * XMLHttpRequest wrapper
     */
    xhr=function()
    {
      /** Public data */
      xhr_pub={};
      
      /** Private data */
      xhr_prv={};
      
      /* ************************ PRIVATE VARIABLES ************************* */
      
      /**
       * The most http connections that will be kept
       */
      xhr_prv.httpMaxSpare = 8;
  
      /**
       * Unused server connections
       */
      xhr_prv.httpSpare = [];
      
      /* ************************** PUBLIC METHODS ************************** */
      
      /**
       * Gets a http request from the cache.
       */
      xhr_pub.poolGetHTTPRequest = function ()
      {
        // atomic test and fetch spare
        // (pop returns undefined if httpSpare is empty)
        var http = xhr_prv.httpSpare.pop();
        if(http)
        {
          return http;
        }
        return xhr_prv.getHTTPRequest();
      };
      
      /**
       * Returns a server connection to the cache.
       * 
       * @param http The connection to return
       */
      xhr_pub.poolReturnHTTPRequest = function (http)
      {
        if (xhr_prv.httpSpare.length >= xhr_prv.httpMaxSpare)
        {
          delete http;
        }
        else
        {
          xhr_prv.httpSpare.push(http);
        }
      };
      
      /* ************************* PRIVATE METHODS ************************** */
      
      /**
       * Gets a fresh http connection
       * @return A new http connection
       */
      xhr_prv.getHTTPRequest = function ()
      {
        /* Look for a browser native XMLHttpRequest implementation (Mozilla/IE7/Opera/Safari, etc.) */
        try
        {
          xhr_prv.httpObjectName = "XMLHttpRequest";
          return new XMLHttpRequest();
        }
        catch(e)
        {
        }
      
        /* Microsoft MSXML ActiveX for IE versions < 7 */
        for (var i = 0; i < comms_prv.msxmlNames.length; i++)
        {
          try
          {
            xhr_prv.httpObjectName = comms_prv.msxmlNames[i];
            return new ActiveXObject(comms_prv.msxmlNames[i]);
          }
          catch (e)
          {
          }
        }
      
        /* None found */
        xhr.httpObjectName = null;
        throw new prv.Exception(
          { 
            code: 0, 
            message: "Can't create XMLHttpRequest object" 
          });
      }
      return xhr_pub;
    }();
    
    return comms_pub;
  }();
  
  /* *********************** OBJECT INITIALISATION ************************** */
  
  // Set the exception handler to something. 
  // The user can reset this to something else.
  pub.toplevel_ex_handler = pub.default_ex_handler;
  
  // Call the constructor
  prv.init.apply(this,arguments);
  
  // Give access to public variables
  return pub;
};