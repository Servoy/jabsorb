/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007-2009 The jabsorb team
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
var jabsorb_flat = function()
{
  /** Assign public variables to this */
  var pub = jabsorb.apply(this, arguments);

  /** Assign protected variables (set by jabsorb) to this */
  var pro = this.pro;

  /** Assign private variables to this */
  var prv = {};

  /* ************************** PRIVATE VARIABLES *************************** */

  prv.INDEX_PREFIX = "_$Inx$_";

  /* **************************** PUBLIC METHODS **************************** */

  /**
   * Marshall an object to JSON format.
   *
   * @param o
   *          The object being converted to json
   * @param resultKey
   *          The field in which the json is to be stored
   *
   * @return an object, { '<resultKey>': jsonString }
   */
  pub.toJSON = function(o, resultKey)
  {
    // temp variable to hold json while processing
    var json, index = 1;

    if (!resultKey)
    {
      resultKey = "json";
    }

    var nextIndex = function()
    {
      var x = prv.INDEX_PREFIX + index;
      index++;
      return x;
    }
    var objects = {};
    var actualObjects = {};

    var helper = {};
    helper.pre = function(o)
    {
    };
    helper.markerFound = function(o)
    {
      return "\"" + o[pro.simpleToJSON.marker] + "\"";
    }
    helper.markerNotFound = function(o)
    {
      if ((o.constructor !== Array) && (typeof o === "object"))
      {
        o[pro.simpleToJSON.marker] = nextIndex();
      }
    }
    helper.post = function(o, v)
    {
      objects[o[pro.simpleToJSON.marker]] = "{" + v.join(", ") + "}";
      actualObjects[o[pro.simpleToJSON.marker]] = o;
      return "\"" + o[pro.simpleToJSON.marker] + "\"";
    }

    json = pro.simpleToJSON(o, helper);
    objects[resultKey] = json;
    for (i in actualObjects)
    {
      delete actualObjects[i][pro.simpleToJSON.marker];
    }
    return objects;
  }

  /**
   * Parses json into an object tree
   *
   * @param data
   *          the object which contains the data
   * @param index
   *          the index in "data" which contains the object to parse
   */
  pub.fromJSON = function(data, index)
  {
    /**
     * Makes a callable reference
     *
     * @param value
     *          The value to make a callable reference out of
     * @return The callable reference if it was made, otherwise value.
     */
    function makeCR(value)
    {
      var x = pro.makeCallableReference(value);
      if (x)
      {
        return x;
      }
      return value;
    }

    /**
     * Whether the given value is an index
     *
     * @return Whether it is an index
     */
    function isIndex(value)
    {
      if (typeof value === 'string')
      {
        if (value.substring(0, prv.INDEX_PREFIX.length) === prv.INDEX_PREFIX)
        {
          return true;
        }
      }
      return false;
    }

    /**
     * Parses an unmarshalled value into an object
     *
     * @param data
     *          The object which contains all the index->object mappings
     * @param obj
     *          The object to unmarshall
     * @param index
     *          The index within data of obj
     * @param processedObjects
     *          Maps indexes to all the objects that are currently being
     *          processed. If an object is found in here it is a circular
     *          reference.
     */
    function parse(data, obj, index, processedObjects)
    {
      var k, v;

      for (k in obj)
      {
        v = obj[k];
        if ((v) && (v.constructor === Array))
        {
          parse(data, obj[k], k, processedObjects);
        }
        else if (isIndex(v))
        {
          if (!processedObjects[v])
          {
            // here we transform obj[k], so don't use v!
            obj[k] = makeCR(data[v]);
            processedObjects[v] = obj[k];
            if (typeof obj[k] === "object")
            {
              parse(data, obj[k], k, processedObjects);
            }
            processedObjects[v] = 0;
          }
          else
          {
            obj[k] = processedObjects[v];
          }
        }
      }
      return data[index];
    }
    if (isIndex(data[index]))
    {
      data[index] = makeCR(data[data[index]]);
    }
    return parse(data, data[index], index, {});
  }

  /* *************************** PRIVATE METHODS **************************** */

  /* *********************** OBJECT INITIALISATION ************************** */

  // Call the constructor
  // prv.init.apply(this,arguments);
  // Give access to public variables
  return pub;
};