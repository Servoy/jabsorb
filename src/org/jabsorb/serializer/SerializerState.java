/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007 The jabsorb team
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

import java.util.HashMap;

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
   * Maps classes to an instance of the class
   */
  private HashMap stateMap = null;

  /**
   * Instantiate (only if get hasn't been yet called for the requested type) or
   * get the previously instantiated instance of the specified type class.
   * 
   * @param clazz type to get/instatiate.
   * 
   * @return an instance of the given class. It will be instantiated if this is
   *         the first time the specified type is requested from the
   *         SerializerState, otherwise, the previously created instance will be
   *         returned.
   * 
   * @throws InstantiationException if an instance of the specified class type
   *           cannot be instantiated.
   * @throws IllegalAccessException if an instance of the specified class type
   *           cannot be instantiated.
   */
  public Object get(Class clazz) throws InstantiationException,
      IllegalAccessException
  {
    Object o;
    if (stateMap == null)
    {
      stateMap = new HashMap();
    }
    else if ((o = stateMap.get(clazz)) != null)
    {
      return o;
    }
    o = clazz.newInstance();
    stateMap.put(clazz, o);
    return o;
  }
}
