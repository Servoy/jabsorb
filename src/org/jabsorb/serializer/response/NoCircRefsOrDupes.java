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

package org.jabsorb.serializer.response;

import java.util.List;

import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ProcessedObject;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.response.results.SuccessfulResult;

/**
 * Serializer State which can handle neither circular references or duplicates.
 * An exception is thrown when circular references are found and duplicates are
 * ignored.
 * 
 * @author William Becker
 */
public class NoCircRefsOrDupes extends SerializerState implements
    CircularReferenceHandler, DuplicateReferenceHandler
{
  public Object circularReferenceFound(List<Object> originalLocation, Object ref,
      Object java) throws MarshallException
  {
    throw new MarshallException("Circular Reference");
  }

  @Override
  public SuccessfulResult createResult(Object requestId, Object json)
  {
    return new SuccessfulResult(requestId, json);
  }

  public Object duplicateFound(List<Object> originalLocation, Object ref, Object java)
      throws MarshallException
  {
    return null;
  }

  @Override
  public Object checkObject(Object parent, Object java, Object ref)
      throws MarshallException
  {
    {
      // check for duplicate objects or circular references
      ProcessedObject p = this.getProcessedObject(java);
      final Object returnValue;

      // if this object hasn't been seen before, mark it as seen and continue forth
      if (p != null)
      {
        //TODO: make test cases to explicitly handle all 4 combinations of the 2 option
        //settings (both on the client and server)

        // handle throwing of circular reference exception and/or serializing duplicates, depending
        // on the options set in the serializer!
        final boolean foundCircRef = this.isAncestor(p, parent);
        if (foundCircRef)
        {
          returnValue = this.circularReferenceFound(p.getLocation(), ref, java);
        }
        else
        {
          returnValue = this.duplicateFound(p.getLocation(), ref, java);
        }
      }
      else
      {
        returnValue = null;
      }
      if (returnValue == null)
      {
        this.push(parent, java, ref);
      }
      return returnValue;
    }
  }
}
