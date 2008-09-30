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
 * Call this to create a jabsorb object which can handle circular references. 
 */
var jabsorb_circrefs = function()
{
  /** Assign public variables to this */
  var pub=jabsorb.apply(this,arguments);

  /** Assign protected variables (set by jabsorb) to this */
  var pro=this.pro;

  /** Assign private variables to this */
  var prv={};
  
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

  /* **************************** PUBLIC METHODS **************************** */
  
  /**
   * Marshall an object to JSON format.
   * Circular references can be handled if the parameter 
   * jabsorb.fixupCircRefs is true.  If the parameter is false,
   * an exception will be thrown if a circular reference is detected.
   *
   * if the parameter, jabsorb.fixupDuplicates is true then duplicate objects in
   * the object graph are also combined except for Strings
   *
   * (TODO: it wouldn't be too hard to optimize strings as well, but probably a threshold
   *  should be provided, so that only strings over a certain length would be optimized)
   * This would be worth doing on the upload (on the download it's not so important, because
   * gzip handles this)
   * if it's false, then duplicate objects are "re-serialized"
   *
   * @param o The object being converted to json
   * @param resultKey With what key the result should be stored 
   * @return an object, { "<resultKey>": jsonString, 'fixups': fixupString } or
   *            just { "<resultKey>" : jsonString }  if there were no fixups found.
   */
  pub.toJSON=function (o,resultKey)
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
  
    //temp variable to create result
    var toReturn;
    var helper={};
    helper.pre=function(o){};
    helper.post=function(o,v)
    {
      return "{" + v.join(", ") + "}";
    };
    helper.markerNotFound=function(o,p,ref)
    {
      // mark this object as visited/processed and set up the parent link 
      // and prev link
      o[pro.simpleToJSON.marker] = {parent:p, prev:markerHead, ref:ref};

      // adjust the "marker" head pointer so the prev pointer on the next 
      // object processed can be set
      markerHead = o;
    };
    helper.markerFound=function(o,p,ref)
    {
          // list of references to get to the fixup entry
      var fixup,
          // list of reference to get to the original location
          original,
          parent,
          circRef
        
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
          original.unshift (parent[pro.simpleToJSON.marker].ref);
        }

        // if we find ourself, then we found a circular reference!
        if (parent===o)
        {
          circRef=parent;
          original = [circRef[pro.simpleToJSON.marker].ref];
        }

        fixup.unshift(parent[pro.simpleToJSON.marker].ref);
        parent = parent[pro.simpleToJSON.marker].parent;
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

          //TODO: (LATER) if multiple fixups go to the same original, this 
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
          original = [o[pro.simpleToJSON.marker].ref];
          parent = o[pro.simpleToJSON.marker].parent;
          while (parent)
          {
            original.unshift(parent[pro.simpleToJSON.marker].ref);
            parent = parent[pro.simpleToJSON.marker].parent;
          }
          //TODO: (LATER) if multiple fixups go to the same original, this 
          // could be optimized somewhat

          // remove last redundant unshifted reference
          fixup.shift();
          original.shift();

          fixups.push([fixup, original]);
          return omitCircRefOrDuplicate;
        }
      }
    };
  
    json = pro.simpleToJSON(o,helper);
    removeMarkers();
  
    toReturn={};
    toReturn[resultKey]=json;
    // only return the fixups if one or more were found
    if (fixups.length)
    {
      toReturn.fixups=pro.simpleToJSON(fixups);
    }
    return toReturn;
  }


  /* ************************** PROTECTED METHODS *************************** */
  
  /**
   * Applies circular references to the json value
   * 
   * @param value The value to apply the circular references
   * @param data  The data which contains fixups
   * @return A value which may contain circular references.
   */
  pro.postJSON=function(value,data)
  {
    if (data.fixups)
    {
      prv.applyFixups(value,data.fixups);
    }
    return value;
  }
  
  /* *************************** PRIVATE METHODS **************************** */
  
  /**
   * Apply fixups.
   * 
   * @param obj    root object to apply fixups against.
   * @param fixups array of fixups to apply. Each element of the array is a 2 
   *               element array, containing the array with the fixup location 
   *               followed by an array with the original location to fix up 
   *               into the fixup location.
   */
  prv.applyFixups=function(obj, fixups)
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
  
  /* *********************** OBJECT INITIALISATION ************************** */
  
  // Give access to public variables
  return pub;
};