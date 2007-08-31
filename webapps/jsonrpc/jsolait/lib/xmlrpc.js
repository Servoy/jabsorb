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
    Provides an XML-RPC imlementation.
    It is similar to python's xmlrpclib module.
*/
Module("xmlrpc","1.2.0", function(thisMod){
    var xmlext = importModule("xml");
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
        Thrown if an XML-RPC response is not well formed.
    */
    thisMod.MalformedXmlRpc = Class("MalformedXmlRpc", Exception, function(thisClass, Super){
        /**
            Initializes the Exception.
            @param msg          The error message of the user.
            @param xml           The xml document's source.
            @param trace=null  The error causing this exception
        */
        thisClass.prototype.init= function(msg, xml, trace){
            Super.prototype.init.call(this,thisMod,msg,trace);
            this.xml = xml;
        }
         ///The xml source which was mal formed.
        thisClass.prototype.xml;
    })
    /**
        Thrown if the RPC response is a Fault.        
    */
    thisMod.Fault = Class("Fault", Exception, function(thisClass, Super){
        /**
            Initializes the Exception.
            @param faultCode       The fault code returned by the rpc call.
            @param faultString      The fault string returned by the rpc call.
        */
        thisClass.prototype.init= function(faultCode, faultString){
            Super.prototype.init.call(this, thisMod,"XML-RPC Fault: " +  faultCode + "\n\n" + faultString);
            this.faultCode = faultCode;
            this.faultString = faultString;
        }
        ///The fault code returned from the rpc call.
        thisClass.prototype.faultCode;
        ///The fault string returned from the rpc call.
        thisClass.prototype.faultString;
    })

    /**
        Marshalls an object to XML-RPC.(Converts an object into XML-RPC conforming xml.)
        It just calls the _toXmlRpc function of the objcect.
        So, to customize serialization of objects one just needs to specify/override the _toXmlRpc method 
        which should return an xml string conforming with XML-RPC spec.
        @param obj    The object to marshall
        @return         An xml representation of the object.
    */
    thisMod.marshall = function(obj){
        return obj._toXmlRpc()
    }
    /**
        Unmarshalls an XML document to a JavaScript object. (Converts xml to JavaScript object.)
        It parses the xml source and creates a JavaScript object.
        @param xml    The xml document source to unmarshall.
        @return         The JavaScript object created from the XML.
    */
    thisMod.unmarshall = function(xml){
        try {//try to parse xml ... this will throw an exception if failed
            try{
                var doc = xmlext.parseXML(xml);
            }catch(e){
                throw new thisMod.MalformedXmlRpc("The server's response could not be parsed.", xml, e);
            }
            var node = doc.documentElement;
            if(node==null){//just in case parse xml didn't throw an exception but returned nothing usefull.
                throw new thisMod.MalformedXmlRpc("No documentElement found.", xml);
            }
            switch(node.tagName){
                case "methodResponse":
                    return parseMethodResponse(node);
                case "methodCall":
                    return parseMethodCall(node);
                default://nothing usefull returned by parseXML.
                    throw new thisMod.MalformedXmlRpc("'methodCall' or 'methodResponse' element expected.\nFound: '" + node.tagName + "'", xml);
            }
        }catch(e){
            if(e instanceof thisMod.Fault){//just rethrow the fault.
                throw e;
            }else {
                throw new thisMod.MalformedXmlRpc("Unmarshalling of XML failed.", xml, e);    
            }
        }
    }
    /**
        Parses a methodeResponse element.
        @param node  The methodResponse element.
        @return          The return value of the XML-RPC.
    */
    var parseMethodResponse=function(node){
        try{
            for(var i=0;i<node.childNodes.length;i++){
                var child = node.childNodes.item(i);
                if (child.nodeType == 1){
                    switch (child.tagName){
                        case "fault": //a fault is thrown as an exception
                            throw parseFault(child);
                        case "params":
                            var params = parseParams(child);
                            if(params.length == 1){//params should only have one param
                                return params[0];
                            }else{
                                throw new thisMod.MalformedXmlRpc("'params' element inside 'methodResponse' must have exactly ONE 'param' child element.\nFound: " + params.length);
                            }
                        default:
                            throw new thisMod.MalformedXmlRpc("'fault' or 'params' element expected.\nFound: '" + child.tagName + "'");                        
                    }
                }
            }
            //no child elements found
            throw new thisMod.MalformedXmlRpc("No child elements found.");    
        }catch(e){
            if(e instanceof thisMod.Fault){
                throw e;
            }else{
                throw new thisMod.MalformedXmlRpc("'methodResponse' element could not be parsed.",null,e);    
            }
        }
    }
    /**
        Parses a methodCall element.
        @param node  The methodCall element.
        @return          Array [methodName,params]. 
    */        
    var parseMethodCall = function(node){
        try{
            var methodName = null;
            var params = new Array();//default is no parameters
            for(var i=0;i<node.childNodes.length;i++){
                var child = node.childNodes.item(i);
                if (child.nodeType == 1){
                    switch (child.tagName){
                        case "methodName":
                            methodName = new String(child.firstChild.nodeValue);
                            break;
                        case "params":
                            params = parseParams(child);
                            break;
                        default:
                            throw new thisMod.MalformedXmlRpc("'methodName' or 'params' element expected.\nFound: '" + child.tagName + "'");                        
                    }
                }
            }
            if(methodName==null){
                throw new thisMod.MalformedXmlRpc("'methodName' element expected.");
            }else{
                return new Array(methodName, params);
            }
        }catch(e){
            throw new thisMod.MalformedXmlRpc("'methodCall' element could not be parsed.",null,e);    
        }
    }
    /**
        Parses a params element.
        @param node  The params element.
        @return          Array of params values. 
    */
    var parseParams = function(node){
        try{
            var params=new Array();
            for(var i=0;i<node.childNodes.length;i++){
                var child = node.childNodes.item(i);
                if (child.nodeType == 1){
                    switch (child.tagName){
                        case "param":
                            params.push(parseParam(child));
                            break;
                        default:
                            throw new thisMod.MalformedXmlRpc("'param' element expected.\nFound: '" + child.tagName + "'");                        
                    }
                }
            }
            //the specs say a 'params' element can contain any number of 'param' elements. That includes 0 ?!
            return params;
        }catch(e){
            throw new thisMod.MalformedXmlRpc("'params' element could not be parsed.",null,e);    
        }
    }
    /**
        Parses a param element.
        @param node  The param node.
        @return          The value of the param.
    */
    var parseParam = function(node){
        try{
            for(var i=0;i<node.childNodes.length;i++){
                var child = node.childNodes.item(i);
                if (child.nodeType == 1){
                    switch (child.tagName){
                        case "value":
                            return parseValue(child);
                        default:
                            throw new thisMod.MalformedXmlRpc("'value' element expected.\nFound: '" + child.tagName + "'");                        
                    }
                }
            }
            //no child elements found, that's an error
            throw new thisMod.MalformedXmlRpc("'value' element expected.But none found.");
        }catch(e){
            throw new thisMod.MalformedXmlRpc("'param' element could not be parsed.",null,e);    
        }
    }
    /**
        Parses a value element.
        @param node  The value element.
        @return         The value.
    */
    var parseValue = function(node){
        try{
            for(var i=0;i<node.childNodes.length;i++){
                var child = node.childNodes.item(i);
                if (child.nodeType == 1){
                    switch (child.tagName){
                        case "string":
                            var s="" 
                            //Mozilla has many textnodes with a size of 4096 chars each instead of one large one.
                            //They all need to be concatenated.
                            for(var j=0;j<child.childNodes.length;j++){
                                s+=new String(child.childNodes.item(j).nodeValue);
                            }
                            return s;
                        case "int":
                        case "i4":
                        case "double":
                            return (child.firstChild) ? new Number(child.firstChild.nodeValue) : 0;
                        case "boolean":
                            return Boolean(isNaN(parseInt(child.firstChild.nodeValue)) ? (child.firstChild.nodeValue == "true") : parseInt(child.firstChild.nodeValue));
                        case "base64":
                            return parseBase64(child);
                        case "dateTime.iso8601":
                            return parseDateTime(child);
                        case "array":
                            return parseArray(child);
                        case "struct":
                            return parseStruct(child);
                        case "nil": //for python None todo: ??? is this valid XML-RPC
                            return null;
                        default:
                            throw new thisMod.MalformedXmlRpc("'string','int','i4','double','boolean','base64','dateTime.iso8601','array' or 'struct' element expected.\nFound: '" + child.tagName + "'");                        
                    }
                }
            }
            //no type specified, assume it is a string
            return (node.firstChild) ? new String(node.firstChild.nodeValue) : "";
        }catch(e){
            throw new thisMod.MalformedXmlRpc("'value' element could not be parsed.",null,e);    
        }
    }
    /**
        Parses a base64 element.
        @param node   The base64 element.
        @return          A string with the decoded base64.
    */
    var parseBase64=function(node){
        try{
            var s = node.firstChild.nodeValue;
            return s.decode("base64");
        }catch(e){
            throw new thisMod.MalformedXmlRpc("'base64' element could not be parsed.",null,e);    
        }
    }
    /**
        Parses a dateTime.iso8601 element.
        @param node   The dateTime.iso8601 element.
        @return           A JavaScript date.
    */
    var parseDateTime=function(node){
        try{
            if(/^(\d{4})(\d{2})(\d{2})T(\d{2}):(\d{2}):(\d{2})/.test(node.firstChild.nodeValue)){
                return new Date(Date.UTC(RegExp.$1, RegExp.$2-1, RegExp.$3, RegExp.$4, RegExp.$5, RegExp.$6));
            }else{ //todo error message
                throw new thisMod.MalformedXmlRpc("Could not convert the given date.");
            }
        }catch(e){
            throw new thisMod.MalformedXmlRpc("'dateTime.iso8601' element could not be parsed.",null,e);    
        }
    }
    /**
        Parses an array element.
        @param node   The array element.
        @return           An Array.
    */
    var parseArray=function(node){
        try{
            for(var i=0;i<node.childNodes.length;i++){
                var child = node.childNodes.item(i);
                if (child.nodeType == 1){
                    switch (child.tagName){
                        case "data":
                            return parseData(child);
                        default:
                            throw new thisMod.MalformedXmlRpc("'data' element expected.\nFound: '" + child.tagName + "'");                        
                    }
                }
            }
            throw new thisMod.MalformedXmlRpc("'data' element expected. But not found.");   
        }catch(e){
            throw new thisMod.MalformedXmlRpc("'array' element could not be parsed.",null,e);    
        }
    }
    /**
        Parses a data element.
        @param node   The data element.
        @return           The value of a data element.
    */
    var parseData=function(node){
        try{
            var rslt = new Array();
            for(var i=0;i<node.childNodes.length;i++){
                var child = node.childNodes.item(i);
                if (child.nodeType == 1){
                    switch (child.tagName){
                        case "value":
                            rslt.push(parseValue(child));
                            break;
                        default:
                            throw new thisMod.MalformedXmlRpc("'value' element expected.\nFound: '" + child.tagName + "'");                        
                    }
                }
            }
            return rslt;
        }catch(e){
            throw new thisMod.MalformedXmlRpc("'data' element could not be parsed.",null,e);    
        }
    }
    /**
        Parses a struct element.
        @param node   The struct element.
        @return           A JavaScript object. Struct memembers are properties of the object.
    */
    var parseStruct=function(node){
        try{
            var struct = new Object();
            for(var i=0;i<node.childNodes.length;i++){
                var child = node.childNodes.item(i);
                if (child.nodeType == 1){
                    switch (child.tagName){
                        case "member":
                            var member = parseMember(child); //returns [name, value]
                            struct[member[0]] = member[1];
                            break;
                        default:
                            throw new thisMod.MalformedXmlRpc("'data' element expected.\nFound: '" + child.tagName + "'");                        
                    }
                }
            }
            return struct;
        }catch(e){
            throw new thisMod.MalformedXmlRpc("'struct' element could not be parsed.",null,e);    
        }
    }
    /**
        Parses a member element.
        @param node  The member element.
        @return          Array containing [memberName, value].
    */
    var parseMember=function(node){
        try{
            var name="";
            var value=null;
            for(var i=0;i<node.childNodes.length;i++){
                var child = node.childNodes.item(i);
                if (child.nodeType == 1){
                    switch (child.tagName){
                        case "value":
                            value = parseValue(child); 
                            break;
                        case "name":
                            name = new String(child.firstChild.nodeValue);
                            break;
                        default:
                            throw new thisMod.MalformedXmlRpc("'value' or 'name' element expected.\nFound: '" + child.tagName + "'");                        
                    }
                }
            }
            if(name == ""){
                throw new thisMod.MalformedXmlRpc("Name for member not found/convertable.");
            }else{
                return new Array(name, value);
            }
        }catch(e){
            throw new thisMod.MalformedXmlRpc("'member' element could not be parsed.",null,e);    
        }
    }
    /**
        Parses a fault element.
        @param node  The fault element.
        @return          A Fault exception object.
    */
    var parseFault = function(node){
        try{
            for(var i=0;i<node.childNodes.length;i++){
                var child = node.childNodes.item(i);
                if (child.nodeType == 1){
                    switch (child.tagName){
                        case "value":
                            var flt = parseValue(child); 
                            return new thisMod.Fault(flt.faultCode, flt.faultString);
                        default:
                            throw new thisMod.MalformedXmlRpc("'value' element expected.\nFound: '" + child.tagName + "'");                        
                    }
                }
            }
            throw new thisMod.MalformedXmlRpc("'value' element expected. But not found.");                        
        }catch(e){
            throw new thisMod.MalformedXmlRpc("'fault' element could not be parsed.",null,e);    
        }
    }

    /**
        Class for creating XML-RPC methods.
        Calling the created method will result in an XML-RPC call to the service.
        The return value of this call will be the return value of the RPC call.
        RPC-Faults will be raised as Exceptions.
        
        Asynchronous operation:
        If the last parameter passed to the method is an XMLRPCAsyncCallback object, 
        then the remote method will be called asynchronously. 
        The results and errors are passed to the callback.
        This is the preffered way for asynchronous operation.
        callAsync is deprecated and will be removed in future versions.
    */
    thisMod.XMLRPCMethod =Class("XMLRPCMethod", function(thisClass){
        
        var postData = function(url, user, pass, data, callback){
            var rslt = urllib.postURL(url, user, pass, data, [["Content-Type", "text/xml"]], callback);
            return rslt;
        }
        
        var handleResponse=function(resp){
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
                    throw new thisMod.MalformedXmlRpc("The server responded with an empty document.", "");
                }else{
                    return thisMod.unmarshall(respTxt);
                }
            }else{
                throw new thisMod.InvalidServerResponse(status);
            }
        }
        
        var getXML = function(methodName, args){
            var data='<?xml version="1.0"?><methodCall><methodName>' + methodName + '</methodName>';
            if (args.length>0){
                data += "<params>";
                for(var i=0;i<args.length;i++){
                    data += '<param><value>' + thisMod.marshall(args[i]) + '</value></param>';
                }
                data += '</params>';
            }
            data += '</methodCall>';
            return data;
        }
        /**
            Initializes the XML-RPC method.
            @param url                 The URL of the service providing the method.
            @param methodName   The name of the method to invoke.
            @param user=null             The user name to use for HTTP authentication.
            @param pass=null             The password to use for HTTP authentication.
        */
        thisClass.prototype.init = function(url, methodName, user, pass){
            this.methodName = methodName;
            this.url = url;
            this.user = user;
            this.password = pass;
            //this is pretty much a hack.
            //we create a function which mimics this class and return it instead of instanciating an object really. 
            var fn=function(){
                //sync or async call
                if(typeof arguments[arguments.length-1] != "function"){
                    var data=getXML(fn.methodName,arguments);
                    var resp = postData(fn.url, fn.user, fn.password, data);
                    return handleResponse(resp);
                }else{
                    fn.callAsync.apply(fn, arguments);
                }
            }
            //make sure the function has the same property as an object created from this class.
            fn.methodName = this.methodName;
            fn.url = this.url;
            fn.user = this.user;
            fn.password=this.password;
            fn.callAsync = this.callAsync;
            fn.toMulticall = this.toMulticall;
            fn.toString = this.toString;
            fn.setAuthentication=this.setAuthentication;
            fn.constructor = thisClass;
            return fn;
        }
        
        /**
            Deprecated and will be removed in future versions!
            Calls the remote method asynchr.
            @param               The first parameters will be passed to the remote method.
            @param callback   The last param is a function to call on compeltion. The result and a possible exception will be passed: callback(result, exception)
        */
        thisClass.prototype.callAsync = function(callback){
            var args=new Array();
            for(var i=0;i<arguments.length-1;i++){
                args.push(arguments[i]);
            }
            var data=getXML(this.methodName, args);
            args=arguments;
            //callback for async postData
            var cllbck = function(resp){
                var rslt = null;
                var exc =null;
                try{
                    rslt = handleResponse(resp);
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
        /**
            Returns the method representation for system.multicall.
            @param   All params will be passed to the remote method.
            @return   An object containing a member methodName and a member params(As required by system.multicall).
        */
        thisClass.prototype.toMulticall = function(){
            var multiCallable = new Object();
            multiCallable.methodName = this.methodName;
            multiCallable.params = arguments;
            return multiCallable;
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
        thisClass.prototype.init = function(url, methodNames, user, pass){
            if(methodNames instanceof Array){
                if(methodNames.length > 0){
                    var tryIntrospection=false;
                }else{
                    var tryIntrospection=true;
                }
            }else{
                pass=user;
                user=methodNames;
                methodNames=[];
                var tryIntrospection=true;
            }
            this.url = url;
            this.user = user;
            this.password = pass;
            this.add(methodNames);
            if(tryIntrospection){
                try{//it's ok if it fails.
                    this.introspect();
                }catch(e){
                }
            }
        }
        
        /**
            Adds new XMLRPCMethods to the proxy server which can then be invoked.
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
                    var mth = new thisMod.XMLRPCMethod(this.url, methodNames[i], this.user, this.password);
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
            Initiate XML-RPC introspection to retrieve methodnames from the server
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
        ///All methods.
        thisClass.prototype.methods=new Array();
    })
})

/**
    Returns the XML-RPC representation of an object.
    A struct is used. Each property of an object is a member of that struct.
    @return   A string containing the object's representation in XML.
*/
Object.prototype._toXmlRpc = function(){
    var s = "<struct>";
    for(var attr in this){
        if(typeof this[attr] != "function"){
            s += "<member><name>" + attr + "</name><value>" + this[attr]._toXmlRpc() + "</value></member>";
        }
    }
    s += "</struct>";
    return s
}
/**
    XML-RPC representation of a string.
    All '&' and '<' are replaced with the '&amp;'  and  '&lt'.
    @return  A string containing the String's representation in XML.
*/
String.prototype._toXmlRpc = function(){
    return "<string>" + this.replace(/&/g, "&amp;").replace(/</g, "&lt;") + "</string>";
}
/**
    XML-RPC representation of a number.
    @return A string containing the Number's representation in XML.
*/
Number.prototype._toXmlRpc = function(){
    if(this == parseInt(this)){
        return "<int>" + this + "</int>";
    }else if(this == parseFloat(this)){
        return "<double>" + this + "</double>";
    }else{
        return false._toXmlRpc();
    }
}
/**
    XML-RPC representation of a boolean.
    @return A string containing the Boolean's representation in XML.
*/
Boolean.prototype._toXmlRpc = function(){
    if(this == true) {
        return "<boolean>1</boolean>";
    }else{
        return "<boolean>0</boolean>";
    }
}
/**
    XML-RPC representation of a date(iso 8601).
    @return A string containing the Date's representation in XML.
*/
Date.prototype._toXmlRpc = function(){
    var padd=function(s, p){
        s=p+s
        return s.substring(s.length - p.length)
    }
    var y = padd(this.getUTCFullYear(), "0000");
    var m = padd(this.getUTCMonth() + 1, "00");
    var d = padd(this.getUTCDate(), "00");
    var h = padd(this.getUTCHours(), "00");
    var min = padd(this.getUTCMinutes(), "00");
    var s = padd(this.getUTCSeconds(), "00");
    
    var isodate = y +  m  + d + "T" + h +  ":" + min + ":" + s

    return "<dateTime.iso8601>" + isodate + "</dateTime.iso8601>";
}
/**
    XML-RPC representation of an array.
    Each entry in the array is a value in the XML-RPC.
    @return A string containing the Array's representation in XML.
*/
Array.prototype._toXmlRpc = function(){
    var retstr = "<array><data>";
    for(var i=0;i<this.length;i++){
        retstr += "<value>" + this[i]._toXmlRpc() + "</value>";
    }
    return retstr + "</data></array>";
}
