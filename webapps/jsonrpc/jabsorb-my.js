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
var jabsorb_my = function()
{
  /** Assign public variables to this */
  var pub=jabsorb.apply(this,arguments);

  /** Assign protected variables (set by jabsorb) to this */
  var pro=this.pro;

  /** Assign private variables to this */
  var prv={};
  
  /* **************************** PUBLIC METHODS **************************** */
  
  /**
   * Marshall an object to JSON format.
   * 
   * @param o The object being converted to json
   * @param resultKey The field in which the json is to be stored
   *
   * @return an object, { '<resultKey>': jsonString }
   */
  pub.toJSON=function (o,resultKey)
  {  
    // temp variable to hold json while processing
    var json,
        index=1;
    
    if(!resultKey)
    {
      resultKey="json";
    }
    
    var nextIndex=function()
    {
      var x=prv.OBJECT_PREFIX+index;
      index++;
      return x;
    }
    var objects ={};
    var actualObjects={};
    
    var helper={};
    helper.pre=function(o){};
    helper.markerFound=function(o)
    {
      return "\""+o[pro.simpleToJSON.marker]+"\"";
    }
    helper.markerNotFound=function(o)
    {
      if ((o.constructor !== Array)&&(typeof o === "object"))
      {
        o[pro.simpleToJSON.marker]=nextIndex();
      }
    }
    helper.post=function(o,v)
    {
      objects[o[pro.simpleToJSON.marker]]="{" + v.join(", ") + "}";
      actualObjects[o[pro.simpleToJSON.marker]]=o;
      return "\""+o[pro.simpleToJSON.marker]+"\"";
    }

    json = pro.simpleToJSON(o,helper);
    objects[resultKey]=json;
    for(i in actualObjects)
    {
      delete actualObjects[i][pro.simpleToJSON.marker];
    }
    return objects;
  }
  prv.OBJECT_PREFIX="_";
  
  pub.fromJSON=function(data,el)
  {
    function _unwrangle(data,obj,el,processedEls)
    {
      function isObject(value)
      {
        if(typeof value === 'string')
        {
          if(value.substring(0,prv.OBJECT_PREFIX.length)===prv.OBJECT_PREFIX)
          {
            return true;
          }
        }
        return false;
      }
      function makeCR(value)
      {
        var x = pro.makeCallableReference(value);
        if(x)
        {
          return x;
        }
        return value;
      }
        
      var k,v;
      if(isObject(obj[el]))
      {
        obj[el]=makeCR(data[obj[el]]);
      }
      for(k in obj[el])
      {
        v=obj[el][k];
        //if is array...
        if((v)&&(v.constructor.toString().indexOf("Array") != -1))
        {
          _unwrangle(data,obj[el],k,processedEls);
        }
        else if(isObject(v))
        {
          if(!processedEls[v])
          {
            //here we transform obj[el][k], so don't use v!
            obj[el][k]=makeCR(data[v]);
            processedEls[v]=obj[el][k];
            if(typeof obj[el][k] === "object")
            {
              _unwrangle(data,obj[el],k,processedEls);
            }
            processedEls[v]=0;
          }
          else
          {
            obj[el][k]=processedEls[v];
          }
        }
      }
      return data[el];
    }
    return _unwrangle(data,data,el,{});
  }
  
  /* *************************** PRIVATE METHODS **************************** */
  
  /* *********************** OBJECT INITIALISATION ************************** */
  
  
  // Call the constructor
  //prv.init.apply(this,arguments);
  
  // Give access to public variables
  return pub;
};