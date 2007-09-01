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

package org.jabsorb.serializer.impl;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.jabsorb.json.JSONArray;
import org.jabsorb.json.JSONObject;
import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;

public class ListSerializer extends AbstractSerializer
{
  private final static long serialVersionUID = 2;

  private static Class[] _serializableClasses = new Class[]{List.class,
    ArrayList.class, LinkedList.class, Vector.class};

  private static Class[] _JSONClasses = new Class[]{JSONObject.class};

  public Class[] getSerializableClasses()
  {
    return _serializableClasses;
  }

  public Class[] getJSONClasses()
  {
    return _JSONClasses;
  }

  public boolean canSerialize(Class clazz, Class jsonClazz)
  {
    return (super.canSerialize(clazz, jsonClazz) ||
      ((jsonClazz == null || jsonClazz == JSONObject.class) &&
        List.class.isAssignableFrom(clazz)));
  }

  public ObjectMatch tryUnmarshall(SerializerState state, Class clazz,
                                   Object o) throws UnmarshallException
  {
    JSONObject jso = (JSONObject) o;
    String java_class = jso.getString("javaClass");
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
    JSONArray jsonlist = jso.getJSONArray("list");
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
    return m;
  }

  public Object unmarshall(SerializerState state, Class clazz, Object o)
    throws UnmarshallException
  {
    JSONObject jso = (JSONObject) o;
    String java_class = jso.getString("javaClass");
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
    JSONArray jsonlist = jso.getJSONArray("list");
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
    return al;
  }

  public Object marshall(SerializerState state, Object o)
    throws MarshallException
  {
    List list = (List) o;
    JSONObject obj = new JSONObject();
    JSONArray arr = new JSONArray();
    if (ser.getMarshallClassHints())
    {
      obj.put("javaClass", o.getClass().getName());
    }
    obj.put("list", arr);
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
      throw new MarshallException("element " + index + " "
        + e.getMessage());
    }
    return obj;
  }

}
