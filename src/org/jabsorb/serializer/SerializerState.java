/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007 Arthur Blake and William Becker
 *
 * based on original code from
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * Copyright Metaparadigm Pte. Ltd. 2004.
 * Michael Clark <michael@metaparadigm.com>
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

package org.jabsorb.serializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class is used by Serializers to hold state during marshalling and
 * unmarshalling. At this time, the BeanSerializer is the only standard
 * Serializer that makes use of SerializerState, but any custom Serializer could
 * use this to store and retrieve state while processing through recursive
 * levels.
 */
public class SerializerState
{
  /**
   * The key is the identity hash code of a processed object wrapped in an Integer object.
   * The value is a ProcessedObject instance which contains both the object that was processed, and
   * other information about the object.
   */
  private Map processedObjects = new HashMap();

  /**
   * A List of FixUp objects that are generated during processing for circular references
   * and/or duplicate references.
   */
  private List fixups = new ArrayList();

  /**
   * If the given object has already been processed, return the ProcessedObject wrapper for
   * that object which will indicate the original location from where that Object was processed from.
   *
   * @param  object Object to check.
   * @return ProcessedObject wrapper for the given object or null if the object hasn't been processed yet
   *         in this SerializerState.
   */
  public ProcessedObject getProcessedObject(Object object)
  {
    // get unique key for this object
    // this is the basis for determining if we have already processed the object or not.
    return (ProcessedObject) processedObjects.get(new Integer(System.identityHashCode(object)));
  }

  /**
   * Represents the current json location that we are at during processing.
   * Each time we go one layer deeper in processing, the reference is pushed onto the stack
   * And each time we recurse out of that layer, it is popped off the stack.
   * A freeze dried copy of the currentLocation can be produced at any point by
   * calling getCurrentLocation.
   */
  private LinkedList currentLocation = new LinkedList();

  /**
   * Pop off one level from the scope stack of the current location during processing.
   */
  public void pop()
  {
    currentLocation.removeLast();
  }

  /**
   * Record the given object as a ProcessedObject and push into onto the scope stack.
   *
   * @param parent parent of object to process.  Can be null if it's the root object being processed.
   *               it should be an object that was already processed via a previous call to processObject.
   *
   * @param obj    object being processed
   * @param ref    reference to object within parent-- should be a String if parent is an object, and Integer
   *               if parent is an array.
   */
  public void push(Object parent, Object obj, Object ref)
  {
    ProcessedObject parentProcessedObject = null;

    if (parent!=null)
    {
      parentProcessedObject = getProcessedObject(parent);

      if (parentProcessedObject==null)
      {
        // this is a sanity check-- it should never occur
        throw new IllegalArgumentException("attempt to process an object with an unprocessed parent");
      }
    }

    ProcessedObject p = new ProcessedObject();
    p.setParent(parentProcessedObject);
    p.setObject(obj);
    p.setRef(ref);

    processedObjects.put(p.getUniqueId(),p);

    currentLocation.add(ref);
  }

  /**
   * Get the List of all FixUp objects created during processing.
   * @return List of FixUps to circular references and duplicates found during processing.
   */
  public List getFixUps()
  {
    return fixups;
  }

  /**
   * Add a fixup entry.  Assumes that the SerializerState is in the correct scope for the
   * fix up location.
   *
   * @param originalLocation original json path location where the object was first encountered.
   * @param ref additional reference (String|Integer) to add on to the scope's current location.
   */
  public void addFixUp(List originalLocation, Object ref)
  {
    currentLocation.add(ref);
    fixups.add(new FixUp(currentLocation, originalLocation));
    pop();
  }
}
