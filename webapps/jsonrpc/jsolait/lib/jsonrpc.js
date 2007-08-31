/*
  Copyright (c) 2003-2004 Jan-Klaas Kollhof
  
  This file is part of the JavaScript o lait library(jsolait).
  
  jsolait is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; either version 2.1 of the License, or
  (at your option) any later version.
 
  This software is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this software; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

/**
    Provides a JSON-RPC imlementation.
    It is vary similar to  xmlrpc module.
*/
Module("jsonrpc", "0.1.4", function(thisMod){
    var lang = importModule("lang");
    var urllib = importModule("urllib");
    
    /**
        Thrown if a  server did not respond with response status 200 (OK).
    */
    thisMod.InvalidServerResponse = Class("InvalidServerResponse", Exception, function(thisClass, Super){
        /**
            Initializes the Exception.
            @param status       The status returned by the server.
        */
        thisClass.prototype.init= function(status){
            Super.prototype.init.call(this,thisMod, "The server did not respond with a status 200 (OK) but with: " + status);
            this.status = status;
        }
         ///The status returned by the server.
        thisClass.prototype.status;
    })
    
    /**
        Thrown if a JSON-RPC response is not well formed.
    */
    thisMod.MalformedJSONRPC = Class("MalformedJSONRPC", Exception, function(thisClass, Super){
        /**
            Initializes the Exception.
            @param msg          The error message of the user.
            @param json          The json source.
            @param trace=null  The error causing this exception
        */
        thisClass.prototype.init= function(msg, json, trace){
            Super.prototype.init.call(this,thisMod,msg,trace);
            this.json = json;
        }
         ///The json source which was malformed.
        thisClass.prototype.json;
    })
       
    /**
        Thrown if the RPC response is an Error.        
    */
    thisMod.Error = Class("Exception", Exception, function(thisClass, Super){
        /**
            Initializes the Exception.
            @param nr    The error code returned by the rpc call.
            @param s     The error string returned by the rpc call.
        */
        thisClass.prototype.init= function(nr, s){
            Super.prototype.init.call(this, thisMod,"JSON-RPC Error: " +  nr + "\n\n" + s);
            this.nr = nr;
            this.s = s;
        }
        ///The error code returned from the rpc call.
        thisClass.prototype.nr;
        ///The error msg  returned from the rpc call.
        thisClass.prototype.s;
    })
    
    /**
        Class for creating JSON-RPC methods.
        Calling the created method will result in an JSON-RPC call to the service.
        The return value of this call will be the return value of the RPC call.
        RPC-Errors will be raised as Exceptions.
        
        Asynchronous operation:
        If the last parameter passed to the method is an JSONRPCAsyncCallback object, 
        then the remote method will be called asynchronously. 
        The results and errors are passed to the callback.
        This is the preffered way for asynchronous operation.
        callAsync is deprecated and will be removed in future versions.
    */
    thisMod.JSONRPCMethod =Class("JSONRPCMethod", function(thisClass){
        
        var postData = function(url, user, pass, data, callback){
            var rslt = urllib.postURL(url, user, pass, data, [["Content-Type", "text/plain"]], callback);
	    return rslt;
        }
        
        thisMod.unmarshall = function(s){
            var obj = lang.jsonToObj(s);
            if(obj.error){
                throw new thisMod.Error(obj.error.code, obj.error.msg);
            }else{
                return obj.result;
            }
        }
        
        var handleResponse=function(self, resp){
            var status=null;
            try{//see if the server responded with a response code 200 OK.
                status = resp.status;
            }catch(e){
            }
            if(status == 200){
                var respTxt = "";
                try{
                    respTxt=resp.responseText;
                }catch(e){
                }
                if(respTxt == null || respTxt == ""){
                    throw new thisMod.MalformedJSONRPC("The server responded with an empty document.", "");
                }else{
                    var o = thisMod.unmarshall(respTxt);
                    // Handle CallableProxy
                    if(o.objectID && o.JSONRPCType == "CallableReference") {
                        var callableRef = new thisMod.ServerProxy(self.url, self.user, self.pass, o.objectID);
                        return callableRef;
                    }
                    return o;
                }
            }else{
                throw new thisMod.InvalidServerResponse(status);
            }
        }
        
	    var createRequest=function(objectID, methodName, args){
	    if(objectID)
                return lang.objToJson({"objectID" : objectID, "methodName" : methodName, "arguments" : args});
	    else
	    	return lang.objToJson({"methodName" : methodName, "arguments" : args});
	    }
        
        /**
            Class for creating JSON-RPC methods.
            Calling the created method will result in an JSON-RPC call to the service.
            The return value of this call will be the return value of the RPC call.
            RPC-Errors will be raised as Exceptions.
            
            Initializes the JSON-RPC method.
            @param url                 The URL of the service providing the method.
            @param methodName   The name of the method to invoke.
            @param user=null             The user name to use for HTTP authentication.
            @param pass=null             The password to use for HTTP authentication.
        */
        thisClass.prototype.init = function(url, methodName, user, pass, objectID){
            this.methodName = methodName;
            this.url = url;
            this.user = user;
            this.password = pass;
            this.objectID = objectID;
            //this is pretty much a hack.
            //we create a function which mimics this class and return it instead of instanciating an object really. 
            var fn=function(){
                var args = [];
                for(var i=0;i<arguments.length;i++){
                    args.push(arguments[i]);
                }
                //sync or async call
                var data = createRequest(fn.objectID, fn.methodName, args);
                if(typeof arguments[arguments.length-1] != "function"){
                    var resp = postData(fn.url, fn.user, fn.password, data);
                    return handleResponse(fn, resp);
                }else{
                    args.pop();
                    args = arguments;
                    //callback for async postData
                    var cllbck = function(resp){
                        var rslt = null;
                        var exc =null;
                        try{
                            rslt = handleResponse(fn, resp);
                        }catch(e){
                            exc = e;
                        }
                        try{//call the callback for the async call.
                            args[args.length-1](rslt,exc);
                        }catch(e){
                        }
                    }
                    postData(this.url, this.user, this.password, data, cllbck);
                }
            }
            //make sure the function has the same property as an object created from this class.
            fn.methodName = this.methodName;
            fn.url = this.url;
            fn.user = this.user;
            fn.password = this.password;
            fn.objectID = this.objectID;
            fn.callAsync = this.callAsync;
            fn.toMulticall = this.toMulticall;
            fn.toString = this.toString;
            fn.setAuthentication = this.setAuthentication;
            fn.constructor = thisClass;
            return fn;
        }
        
        /**
            Sets username and password for HTTP Authentication.
            @param user    The user name.
            @param pass    The password.
        */
        thisClass.prototype.setAuthentication = function(user, pass){
            this.user = user;
            this.password = pass;
        }
        ///The name of the remote method.
        thisClass.prototype.methodName;
        ///The url of the remote service containing the method.
        thisClass.prototype.url;
        ///The user name used for HTTP authorization.
        thisClass.prototype.user;
        ///The password used for HTTP authorization.
        thisClass.prototype.password;
        ///The objectID to invoke on
        thisClass.prototype.objectID;
    })
    
    /**
        Creates proxy objects which resemble the remote service.
        Method calls of this proxy will result in calls to the service.
    */
    thisMod.ServerProxy=Class("ServerProxy", function(thisClass){
        /**
            Initializes a new ServerProxy.
            The arguments are interpreted as shown in the examples:
            ServerProxy("url")
            ServerProxy("url", ["methodName1",...])
            ServerProxy("url", ["methodName1",...], "user", "pass")
            ServerProxy("url", "user", "pass")
            
            @param url                     The url of the service.
            @param methodNames=[]  Array of names of methods that can be called on the server.
                                                If no methods are given then introspection is used to get the methodnames from the server.
            @param user=null             The user name to use for HTTP authentication.
            @param pass=null             The password to use for HTTP authentication.
        */
        thisClass.prototype.init = function(url, methodNames, user, pass, objectID){
            if(methodNames instanceof Array){
                if(methodNames.length > 0){
                    var tryIntrospection=false;
                }else{
                    var tryIntrospection=true;
                }
            }else{
                objectID=pass;
                pass=user;
                user=methodNames;
                methodNames=[];
                var tryIntrospection=true;
            }
            this.url = url;
            this.user = user;
            this.password = pass;
            this.objectID = objectID;
            this.add(methodNames);
            if(tryIntrospection){
                try{//it's ok if it fails.
                    this.introspect();
                }catch(e){
		    reportException(e);
                }
            }
        }
        
        /**
            Adds new JSONRPCMethods to the proxy server which can then be invoked.
            @param methodNames   Array of names of methods that can be called on the server.
        */
        thisClass.prototype.add = function(methodNames){
            for(var i=0;i<methodNames.length;i++){
                var obj = this;
                //setup obj.childobj...method
                var names = methodNames[i].split(".");
                for(var n=0;n<names.length-1;n++){
                    var name = names[n];
                    if(obj[name]){
                        obj = obj[name];
                    }else{
                        obj[name]  = new Object();
                        obj = obj[name];
                    }
                }
                var name = names[names.length-1];
                if(obj[name]){
                }else{
                    var mth = new thisMod.JSONRPCMethod(this.url, methodNames[i], this.user, this.password, this.objectID);
                    obj[name] = mth;
                    this.methods.push(mth);
                }
            }
        }
        
        /**
            Sets username and password for HTTP Authentication for all methods of this service.
            @param user    The user name.
            @param pass    The password.
        */
        thisClass.prototype.setAuthentication = function(user, pass){
            this.user = user;
            this.password = pass;
            for(var i=0;i<this.methods.length;i++){
                this.methods[i].setAuthentication(user, pass);;
            }
        }
        
        /**
            Initiate JSON-RPC introspection to retrieve methodnames from the server
            and add them to the server proxy.
        */
        thisClass.prototype.introspect = function(){
            this.add(["system.listMethods","system.methodHelp", "system.methodSignature"]);
            var m = this.system.listMethods();
            this.add(m);
        }
        ///The url of the service to resemble.
        thisClass.prototype.url;
        ///The user used for HTTP authentication.
        thisClass.prototype.user;
        ///The password used for HTTP authentication.
        thisClass.prototype.password;
        ///The object to invoke on
        thisClass.prototype.objectID;
        ///All methods.
        thisClass.prototype.methods=new Array();
    })
})
