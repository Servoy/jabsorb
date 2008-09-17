/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007-2008 The jabsorb team
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

import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.jabsorb.serializer.response.results.SuccessfulResult;

/**
 * Used by Serializers to hold state during marshalling and unmarshalling. It
 * keeps track of all Objects encountered during processing for the purpose of
 * detecting circular references and/or duplicates.
 */
public abstract class SerializerState
{
  /**
   * Creates a new SerializerState
   */
  protected SerializerState()
  {
    processedObjects = new IdentityHashMap();
    currentLocation = new LinkedList();
  }

  /**
   * The key is the processed object. The value is a ProcessedObject instance
   * which contains both the object that was processed, and other information
   * about the object used for generating fixups when marshalling.
   */
  private final Map processedObjects;

  /**
   * Represents the current json location that we are at during processing. Each
   * time we go one layer deeper in processing, the reference is pushed onto the
   * stack And each time we recurse out of that layer, it is popped off the
   * stack.
   */
  protected final LinkedList currentLocation;

  /**
   * Creates a result to be returned to the jabsorb client.
   * 
   * @param requestId The id of the request for which this result is to be mad e
   * @param json The data to send
   * @return Some kind of SuccessfulResult
   */
  public abstract SuccessfulResult createResult(Object requestId, Object json);

  /**
   * Checks whether the current object being parsed needs action taken upon it
   * or is ok to marshal. If any value is returned except null, it means that it
   * should not be marshalled.
   * 
   * @param parent The parent of the current object
   * @param currentObject The current object being marshalled
   * @param ref additional reference (String|Integer) to add on to the scope's
   *          current location.
   * @return The value to add to the json object being created, or null if the
   *         value to be added should be created by marshalling the current
   *         object.
   * @throws MarshallException if a scope error occurs (this won't normally
   *           occur.
   */
  public abstract Object checkObject(Object parent, Object currentObject,
      Object ref) throws MarshallException;

  /**
   * If the given object has already been processed, return the ProcessedObject
   * wrapper for that object which will indicate the original location from
   * where that Object was processed from.
   * 
   * @param object Object to check.
   * @return ProcessedObject wrapper for the given object or null if the object
   *         hasn't been processed yet in this SerializerState.
   */
  public ProcessedObject getProcessedObject(Object object)
  {
    // get unique key for this object
    // this is the basis for determining if we have already processed the object or not.
    return (ProcessedObject) processedObjects.get(object);
  }

  

  /**
   * Pop off one level from the scope stack of the current location during
   * processing. If we are already at the lowest level of scope, then this has
   * no action.
   * 
   * @throws MarshallException If called when currentLocation is empty
   */
  public void pop() throws MarshallException
  {
    if (currentLocation.size() == 0)
    {
      // this is a sanity check
      throw new MarshallException(
          "scope error, attempt to pop too much off the scope stack.");
    }
    currentLocation.removeLast();
  }

  /**
   * Record the given object as a ProcessedObject and push into onto the scope
   * stack. This is only used for marshalling. The store method should be used
   * for unmarshalling.
   * 
   * @param parent parent of object to process. Can be null if it's the root
   *          object being processed. it should be an object that was already
   *          processed via a previous call to processObject.
   * @param obj object being processed
   * @param ref reference to object within parent-- should be a String if parent
   *          is an object, and Integer if parent is an array. Can be null if
   *          this is the root object that is being pushed/processed.
   */
  public void push(Object parent, Object obj, Object ref)
  {
    ProcessedObject parentProcessedObject = null;

    if (parent != null)
    {
      parentProcessedObject = getProcessedObject(parent);

      if (parentProcessedObject == null)
      {
        // this is a sanity check-- it should never occur
        throw new IllegalArgumentException(
            "attempt to process an object with an unprocessed parent");
      }
    }

    ProcessedObject p = new ProcessedObject();
    p.setParent(parentProcessedObject);
    p.setObject(obj);

    processedObjects.put(obj, p);
    if (ref != null)
    {
      p.setRef(ref);
      currentLocation.add(ref);
    }
  }

  /**
   * Associate the incoming source object being serialized to it's serialized
   * representation. Currently only used within tryUnmarshall and unmarshall.
   * This MUST be called before a given unmarshall or tryUnmarshall recurses
   * into child objects to unmarshall them. The purpose is to stop the recursion
   * that can take place when circular references/duplicates are in the input
   * json being unmarshalled.
   * 
   * @param source source object being unmarshalled.
   * @param target target serialized representation of the object that the
   *          source object is being unmarshalled to.
   * @throws UnmarshallException if the source object is null, or is not already
   *           stored within a ProcessedObject.
   */
  public void setSerialized(Object source, Object target)
      throws UnmarshallException
  {
    if (source == null)
    {
      throw new UnmarshallException("source object may not be null");
    }
    ProcessedObject p = getProcessedObject(source);
    if (p == null)
    {
      // this should normally never happen- it's a sanity check.
      throw new UnmarshallException(
          "source object must be already registered as a ProcessedObject "
              + source);
    }
    p.setSerialized(target);
  }

  /**
   * Much simpler version of push to just account for the fact that an object
   * has been processed (used for unmarshalling where we just need to re-hook up
   * circ refs and duplicates and not generate fixups.)
   * 
   * @param obj Object to account for as being processed.
   * @return ProcessedObject wrapper for the accounted for object.
   */
  public ProcessedObject store(Object obj)
  {
    ProcessedObject p = new ProcessedObject();
    p.setObject(obj);

    processedObjects.put(obj, p);
    return p;
  }

  /**
   * Determine if a duplicate child object of the given parentis a circular
   * reference with the given ProcessedObject. We know it's a circular reference
   * if we can walk up the parent chain and find the ProcessedObject. If instead
   * we find null, then it's a duplicate instead of a circular ref.
   * 
   * @param dup the duplicate object that might also be the original reference
   *          in a circular reference.
   * @param parent the parent of an object that might be a circular reference.
   * @return true if the duplicate is a circular reference or false if it's a
   *         duplicate only.
   */
  protected boolean isAncestor(ProcessedObject dup, Object parent)
  {
    // walk up the ancestry chain until we either find the duplicate
    // (which would mean it's a circular ref)
    // or we find null (the end of the chain) which would mean it's a duplicate only.
    ProcessedObject ancestor = getProcessedObject(parent);
    while (ancestor != null)
    {
      if (dup == ancestor)
      {
        return true;
      }
      ancestor = ancestor.getParent();
    }
    return false;
  }
}
