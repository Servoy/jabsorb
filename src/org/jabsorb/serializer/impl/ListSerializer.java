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

package org.jabsorb.serializer.impl;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Serialises lists
 * 
 * TODO: if this serialises a superclass does it need to also specify the
 * subclasses?
 */
public class ListSerializer extends AbstractSerializer
{
  /**
   * Unique serialisation id.
   */
  private final static long serialVersionUID = 2;

  /**
   * Classes that this can serialise.
   */
  private static Class[] _serializableClasses = new Class[] { List.class,
      ArrayList.class, LinkedList.class, Vector.class };

  /**
   * Classes that this can serialise to.
   */
  private static Class[] _JSONClasses = new Class[] { JSONObject.class };

  public boolean canSerialize(Class clazz, Class jsonClazz)
  {
    return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || jsonClazz == JSONObject.class) && List.class
        .isAssignableFrom(clazz)));
  }

  public Class[] getJSONClasses()
  {
    return _JSONClasses;
  }

  public Class[] getSerializableClasses()
  {
    return _serializableClasses;
  }

  public Object marshall(SerializerState state, Object o)
      throws MarshallException
  {
    List list = (List) o;
    JSONObject obj = new JSONObject();
    JSONArray arr = new JSONArray();
    // TODO: this same block is done everywhere.
    // Have a single function to do it.
    if (ser.getMarshallClassHints())
    {
      try
      {
        obj.put("javaClass", o.getClass().getName());
      }
      catch (JSONException e)
      {
        throw new MarshallException("javaClass not found!");
      }
    }
    try
    {
      obj.put("list", arr);
    }
    catch (JSONException e)
    {
      throw new MarshallException("Error setting list: " + e);
    }
    int index = 0;
    try
    {
      Iterator i = list.iterator();
      while (i.hasNext())
      {
        arr.put(ser.marshall(state, i.next()));
        index++;
      }
    }
    catch (MarshallException e)
    {
      throw new MarshallException("element " + index + " " + e.getMessage());
    }
    return obj;
  }

  // TODO: try unMarshall and unMarshall share 90% code. Put in into an
  // intermediate function.
  // TODO: Also cache the result somehow so that an unmarshall
  // following a tryUnmarshall doesn't do the same work twice!
  public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object o)
      throws UnmarshallException
  {
    JSONObject jso = (JSONObject) o;
    String java_class;
    try
    {
      java_class = jso.getString("javaClass");
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("Could not read javaClass");
    }
    if (java_class == null)
    {
      throw new UnmarshallException("no type hint");
    }
    if (!(java_class.equals("java.util.List")
        || java_class.equals("java.util.AbstractList")
        || java_class.equals("java.util.LinkedList")
        || java_class.equals("java.util.ArrayList") || java_class
        .equals("java.util.Vector")))
    {
      throw new UnmarshallException("not a List");
    }
    JSONArray jsonlist;
    try
    {
      jsonlist = jso.getJSONArray("list");
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("Could not read list: " + e.getMessage());
    }
    if (jsonlist == null)
    {
      throw new UnmarshallException("list missing");
    }
    int i = 0;
    ObjectMatch m = new ObjectMatch(-1);
    try
    {
      for (; i < jsonlist.length(); i++)
      {
        m = ser.tryUnmarshall(state, null, jsonlist.get(i)).max(m);
      }
    }
    catch (UnmarshallException e)
    {
      throw new UnmarshallException("element " + i + " " + e.getMessage());
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("element " + i + " " + e.getMessage());
    }
    return m;
  }

  public Object unmarshall(SerializerState state, Class clazz, Object o)
      throws UnmarshallException
  {
    JSONObject jso = (JSONObject) o;
    String java_class;
    try
    {
      java_class = jso.getString("javaClass");
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("Could not read javaClass");
    }
    if (java_class == null)
    {
      throw new UnmarshallException("no type hint");
    }
    AbstractList al = null;
    if (java_class.equals("java.util.List")
        || java_class.equals("java.util.AbstractList")
        || java_class.equals("java.util.ArrayList"))
    {
      al = new ArrayList();
    }
    else if (java_class.equals("java.util.LinkedList"))
    {
      al = new LinkedList();
    }
    else if (java_class.equals("java.util.Vector"))
    {
      al = new Vector();
    }
    else
    {
      throw new UnmarshallException("not a List");
    }
    JSONArray jsonlist;
    try
    {
      jsonlist = jso.getJSONArray("list");
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("Could not read list: " + e.getMessage());
    }
    if (jsonlist == null)
    {
      throw new UnmarshallException("list missing");
    }
    int i = 0;
    try
    {
      for (; i < jsonlist.length(); i++)
      {
        al.add(ser.unmarshall(state, null, jsonlist.get(i)));
      }
    }
    catch (UnmarshallException e)
    {
      throw new UnmarshallException("element " + i + " " + e.getMessage());
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("element " + i + " " + e.getMessage());
    }
    return al;
  }

}
