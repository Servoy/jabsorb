/*
  Copyright (c) 2004 Jan-Klaas Kollhof
  
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
    Module providing language services like tokenizing JavaScript code
    or converting JavaScript objects to and from JSON (json.org).
    To customize JSON serialization of Objects just overwrite the _toJSON method in your class.
*/
Module("lang", "0.1.0", function(thisMod){
    
    /**
        Turns JSON code into JavaScript objects.
        @param src  The source as a String.
    */
    thisMod.jsonToObj=function(src){
        var obj;
        eval("obj = " + src);
        return obj;
    }
    
    /**
        Turns an object into JSON.
        This is the same as calling obj._toJSON();
        @param obj  The object to marshall.
    */
    thisMod.objToJson=function(obj){
        return obj._toJSON();
    }
    
    ///Token constants for the tokenizer.
    thisMod.tokens = {};
    thisMod.tokens.WSP = 0;
    thisMod.tokens.OP =1;
    thisMod.tokens.STR = 2;
    thisMod.tokens.NAME = 3;
    thisMod.tokens.NUM = 4;
    thisMod.tokens.ERR = 5;
    thisMod.tokens.NL = 6;
    thisMod.tokens.COMMENT = 7;
    thisMod.tokens.DOCCOMMENT = 8;
    thisMod.tokens.REGEXP = 9;
    
    /**
        Tokenizer Class which incrementally parses JavaScript code and returns the language tokens.
    */
    thisMod.Tokenizer=Class("Tokenizer", function(thisClass, Super){
        thisClass.prototype.init=function(s){
            this._working = s;
        }
        
        /**
            Returns weather or not the code was parsed.
            @return True if the complete code was parsed, false otherwise.
        */
        thisClass.prototype.finished=function(){
            return this._working.length == 0;
        }
        
        /**
            Returns the next token.
            @return The next token.
        */
        thisClass.prototype.next = function(){
            if(this._working ==""){
                throw "Empty";
            } 
            //match tokens using regex:    ' " { } [ ] ( ) , ; : . + - * ! <> <= < >= > == != = \n \r /// // /** /* /  FloatNum DecNum Name WhiteSpace
            var s = this._working.match(/'|"|\{|\}|\[|\]|\(|\)|,|;|:|\.|\+|\-|\*|!|<>|<=|<|>=|>|==|!=|=|\n|\r|\/\/\/|\/\/|\/\*\*|\/\*|\/|\d+\.\d+|\d+|\w+|\s+/)[0];
            this._working = this._working.slice(s.length);
            switch(s){
                case "\n": case "\r":
                    return [thisMod.tokens.NL, s];
                case "//":case "///":
                    var r = extractSLComment(this._working);
                    this._working = this._working.slice(r.length);
                    return [s.charAt(2) != "/" ? thisMod.tokens.COMMENT:thisMod.tokens.DOCCOMMENT, s+r];
                case "/*":case "/**":
                    try{
                        var r = extractMLComment(this._working);
                        this._working = this._working.slice(r.length);
                        return [s.charAt(2) !="*" ? thisMod.tokens.COMMENT: thisMod.tokens.DOCCOMMENT, s+r];
                    }catch(e){
                        return [thisMod.tokens.ERR, s, e];
                    }
                case '"': case "'":
                    try{
                        s += extractQString(this._working, s);
                        this._working = this._working.slice(s.length-1);
                        return [thisMod.tokens.STR, s];
                    }catch(e){
                        return [thisMod.tokens.ERR, s, e];
                    }
                case "/":
                    try{
                        s += extractRegExp(this._working);
                        this._working = this._working.slice(s.length-1);
                        return [thisMod.tokens.REGEXP, s];
                    }catch(e){
                        return [thisMod.tokens.ERR, s, e];
                    }
                case "{": case "}": case "[": case "]": case "(": case ")":
                case ":": case ",": case ".": case ";":
                case "*": case "/": case "-": case "+":
                case "=": case "<=": case ">=": case "<": case ">": case "==": case "!=": case "!": case "<>": 
                    return [thisMod.tokens.OP, s];
                default:
                    if(s.replace(/\s+/g,"") == ""){ //whitespace
                        return [thisMod.tokens.WSP, s];
                    }else if(/\d|\d\.\d/.test(s)){//number
                        return [thisMod.tokens.NUM, s];
                    }else{//name
                        return [thisMod.tokens.NAME, s];
                    }
            }
            return s;
        }
        
        var searchQoute = function(s, q){
            if(q=="'"){
                return s.search(/[\\']/);
            }else{
                return s.search(/[\\"]/);
            }
        }
        
        var extractQString=function(s, q){
            var rs="";
            var p= searchQoute(s, q);
            while(p >= 0){
                if(p >=0){
                    if(s.charAt(p) == q){
                        rs += s.slice(0, p+1);
                        s = s.slice(p+1);
                        return rs;
                    }else{
                        rs+=s.slice(0, p+2);
                        s = s.slice(p+2);
                    }
                }
                p = searchQoute(s, q);
            }
            throw "End of String expected.";
        }
        
        var extractSLComment=function(s){
            var p = s.search(/\n/);
            if(p>=0){
                return s.slice(0,p+1);
            }else{
                return s;
            }
        }
        
        var extractMLComment=function(s){
            var p = s.search(/\*\//);
            if(p>=0){
                return s.slice(0,p+2);
            }else{
                throw "End of comment expected.";
            }
        }
        
        var extractRegExp=function(s){
            var rs="";
            var p= s.search(/[\\\/]/);
            while(p >= 0){
                if(p >=0){
                    if(s.charAt(p) == "/"){
                        rs += s.slice(0, p+1);
                        s = s.slice(p+1);
                        return rs;
                    }else{
                        rs+=s.slice(0, p+2);
                        s = s.slice(p+2);
                    }
                }
                p = s.search(/[\\\/]/);
            }
            throw "End of String expected.";  
        }
    })
    
    /**
        Converts an object to JSON.
    */
    Object.prototype._toJSON = function(){
        var v=[];
        for(attr in this){
            if(typeof this[attr] != "function"){
                v.push('"' + attr + '": ' + this[attr]._toJSON());
            }
        }
        return "{" + v.join(", ") + "}";
    }
    
    /**
        Converts a String to JSON.
    */
    String.prototype._toJSON = function(){
        return '"' + this.replace(/(["\\])/g, '\\$1') + '"';
    }
    
    /**
        Converts a Number to JSON.
    */
    Number.prototype._toJSON = function(){
        return this.toString();
    }
    
    /**
        Converts a Boolean to JSON.
    */
    Boolean.prototype._toJSON = function(){
        return this.toString();
    }
    
    /**
        Converts a Date to JSON.
        Date representation is not defined in JSON.
    */
    Date.prototype._toJSON= function(){
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
        
        return '"' + isodate + '"';
    }
    
    /**
        Converts an Array to JSON.
    */
    Array.prototype._toJSON = function(){
        var v = [];
        for(var i=0;i<this.length;i++){
            v.push(this[i]._toJSON()) ;
        }
        return "[" + v.join(", ") + "]";
    }
})
