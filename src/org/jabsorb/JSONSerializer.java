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

package org.jabsorb;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jabsorb.json.JSONArray;
import org.jabsorb.json.JSONObject;
import org.jabsorb.json.JSONTokener;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.Serializer;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.jabsorb.serializer.impl.ArraySerializer;
import org.jabsorb.serializer.impl.BeanSerializer;
import org.jabsorb.serializer.impl.BooleanSerializer;
import org.jabsorb.serializer.impl.DateSerializer;
import org.jabsorb.serializer.impl.DictionarySerializer;
import org.jabsorb.serializer.impl.ListSerializer;
import org.jabsorb.serializer.impl.MapSerializer;
import org.jabsorb.serializer.impl.NumberSerializer;
import org.jabsorb.serializer.impl.PrimitiveSerializer;
import org.jabsorb.serializer.impl.RawJSONArraySerializer;
import org.jabsorb.serializer.impl.RawJSONObjectSerializer;
import org.jabsorb.serializer.impl.SetSerializer;
import org.jabsorb.serializer.impl.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the public entry point to the serialization code and provides
 * methods for marshalling Java objects into JSON objects and unmarshalling JSON
 * objects into Java objects.
 */
public class JSONSerializer implements Serializable
{

  private final static long serialVersionUID = 2;

  private final static Logger log = LoggerFactory.getLogger(JSONSerializer.class);

  //  Debugging enabled on this serializer.
  private boolean debug = false;

  /**
   * Enable or disable debugging message from this serializer instance.
   *
   * @param debug flag to enable or disable debugging messages
   */
  public void setDebug(boolean debug)
  {
    this.debug = debug;
  }

  /**
   * Are debugging messages enabled on this serializer instance.
   *
   * @return true or false depending on whether debugging messages are enabled.
   */
  public boolean isDebug()
  {
    return debug;
  }

  private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    serializableMap = new HashMap();
    Iterator i = serializerList.iterator();
    while (i.hasNext())
    {
      Serializer s = (Serializer) i.next();
      Class classes[] = s.getSerializableClasses();
      for (int j = 0; j < classes.length; j++)
      {
        serializableMap.put(classes[j], s);
      }
    }
  }

  // Key Serializer
  private HashSet serializerSet = new HashSet();

  // key Class, val Serializer
  private transient HashMap serializableMap = null;

  // List for reverse registration order search
  private ArrayList serializerList = new ArrayList();

  private boolean marshallClassHints = true;
  private boolean marshallNullAttributes = true;

  /**
   * Register all of the provided standard serializers.
   */
  public void registerDefaultSerializers() throws Exception
  {

    // the order of registration is important:
    // when trying to marshall java objects into json, first,
    // a direct match (by Class) is looked for in the serializeableMap
    // if a direct match is not found, all serializers are
    // searched in the reverse order that they were registered here (via the serializerList)
    // for the first serializer that canSerialize the java class type.

    registerSerializer(new RawJSONArraySerializer());
    registerSerializer(new RawJSONObjectSerializer());
    registerSerializer(new BeanSerializer());
    registerSerializer(new ArraySerializer());
    registerSerializer(new DictionarySerializer());
    registerSerializer(new MapSerializer());
    registerSerializer(new SetSerializer());
    registerSerializer(new ListSerializer());
    registerSerializer(new DateSerializer());
    registerSerializer(new StringSerializer());
    registerSerializer(new NumberSerializer());
    registerSerializer(new BooleanSerializer());
    registerSerializer(new PrimitiveSerializer());
  }

  /**
   * Register a new type specific serializer.
   * The order of registration is important.  More specific serializers should be added after less
   * specific serializers.  This is because when the JSONSerializer is trying to find
   * a serializer, if it can't find the serializer by a direct match, it will search for a serializer
   * in the reverse order that they were registered.
   *
   * @param s A class implementing the Serializer interface
   *          (usually derived from AbstractSerializer).
   */
  public void registerSerializer(Serializer s) throws Exception
  {
    Class classes[] = s.getSerializableClasses();
    Serializer exists;
    synchronized (serializerSet)
    {
      if (serializableMap == null)
      {
        serializableMap = new HashMap();
      }
      for (int i = 0; i < classes.length; i++)
      {
        exists = (Serializer) serializableMap.get(classes[i]);
        if (exists != null && exists.getClass() != s.getClass())
        {
          throw new Exception(
            "different serializer already registered for "
              + classes[i].getName());
        }
      }
      if (!serializerSet.contains(s))
      {
        if (isDebug())
        {
          log.info("registered serializer " + s.getClass().getName());
        }
        s.setOwner(this);
        serializerSet.add(s);
        serializerList.add(0, s);
        for (int j = 0; j < classes.length; j++)
        {
          serializableMap.put(classes[j], s);
        }
      }
    }
  }

  /**
   * Find the serializer for the given Java type and/or JSON type.
   *
   * @param clazz    The Java class to lookup.
   * @param jsoClazz The JSON class type to lookup (may be null in
   *                 the marshalling case in which case only the
   *                 class is used to lookup the serializer).
   * @return The found Serializer for the types specified
   *         or null if none could be found.
   */
  private Serializer getSerializer(Class clazz, Class jsoClazz)
  {
    if (isDebug())
    {
      log.trace("looking for serializer - java:"
        + (clazz == null ? "null" : clazz.getName()) + " json:"
        + (jsoClazz == null ? "null" : jsoClazz.getName()));
    }

    Serializer s = null;
    synchronized (serializerSet)
    {
      s = (Serializer) serializableMap.get(clazz);
      if (s != null && s.canSerialize(clazz, jsoClazz))
      {
        if (isDebug())
        {
          log.trace("direct match serializer "
            + s.getClass().getName());
        }
        return s;
      }
      Iterator i = serializerList.iterator();
      while (i.hasNext())
      {
        s = (Serializer) i.next();
        if (s.canSerialize(clazz, jsoClazz))
        {
          if (isDebug())
          {
            log.trace("search found serializer "
              + s.getClass().getName());
          }
          return s;
        }
      }
    }
    return null;
  }

  /**
   * Find the corresponding java Class type from json (as represented by a JSONObject or JSONArray,)
   * using the javaClass hinting mechanism.
   * <p/>
   * If the Object is a JSONObject, the simple javaClass property is looked for.
   * If it is a JSONArray then this method is invoked recursively on the first element of the array.
   * <p/>
   * then the Class is returned as an array type for the type of class hinted by the first Object
   * in the array.
   * <p/>
   * If the object is neither a JSONObject or JSONArray, return the Class of the object directly.
   * (this implies a primitive type, such as String, Integer or Boolean)
   *
   * @param o a JSONObject or JSONArray object to get the Class type from the javaClass hint.
   * @return the Class of javaClass hint found, or null if the passed in Object is null, or the
   *         Class of the Object passed in, if that object is not a JSONArray or JSONObject.
   * @throws UnmarshallException if javaClass hint was not found (except for null case or primitive object case),
   *                             or the javaClass hint is not a valid java class.
   *                             <p/>
   * todo: the name of this method is a bit misleading because it doesn't actually get the class from
   * todo: the javaClass hint if the type of Object passed in is not JSONObject|JSONArray.
   */
  private Class getClassFromHint(Object o) throws UnmarshallException
  {
    if (o == null)
    {
      return null;
    }
    if (o instanceof JSONObject)
    {
      try
      {
        String class_name = ((JSONObject) o).getString("javaClass");
        Class clazz = Class.forName(class_name);
        return clazz;
      }
      catch (NoSuchElementException e)
      {
      }
      catch (Exception e)
      {
        throw new UnmarshallException("class in hint not found");
      }
    }
    if (o instanceof JSONArray)
    {
      JSONArray arr = (JSONArray) o;
      if (arr.length() == 0)
      {
        throw new UnmarshallException("no type for empty array");
      }
      // return type of first element
      Class compClazz = getClassFromHint(arr.get(0));
      try
      {
        if (compClazz.isArray())
        {
          return Class.forName("[" + compClazz.getName());
        }
        else
        {
          return Class.forName("[L" + compClazz.getName() + ";");
        }
      }
      catch (ClassNotFoundException e)
      {
        throw new UnmarshallException("problem getting array type");
      }
    }
    return o.getClass();
  }

  public ObjectMatch tryUnmarshall(SerializerState state, Class clazz,
                                   Object json) throws UnmarshallException
  {
    /*
    * If we have a JSON object class hint that is a sub class of the
    * signature 'clazz', then override 'clazz' with the hint class.
    */
    if (clazz != null && json instanceof JSONObject
      && ((JSONObject) json).has("javaClass")
      && clazz.isAssignableFrom(getClassFromHint(json)))
    {
      clazz = getClassFromHint(json);
    }

    if (clazz == null)
    {
      clazz = getClassFromHint(json);
    }
    if (clazz == null)
    {
      throw new UnmarshallException("no class hint");
    }
    if (json == null || json == JSONObject.NULL)
    {
      if (!clazz.isPrimitive())
      {
        return ObjectMatch.NULL;
      }
      else
      {
        throw new UnmarshallException("can't assign null primitive");
      }
    }
    Serializer s = getSerializer(clazz, json.getClass());
    if (s != null)
    {
      return s.tryUnmarshall(state, clazz, json);
    }

    throw new UnmarshallException("no match");
  }

  /**
   * Unmarshall json into an equivalent java object.
   * <p/>
   * This involves finding the correct Serializer to use and then
   * delegating to that Serializer to unmarshall for us.  This method will be invoked
   * recursively as Serializers unmarshall complex object graphs.
   *
   * @param state can be used by the underlying Serializer objects
   *              to hold state while unmarshalling.
   * @param clazz optional java class to unmarshall to- if set to null
   *              then it will be looked for via the javaClass hinting mechanism.
   * @param json  JSONObject or JSONArray that contains the json to unmarshall.
   * @return the java object representing the json that was unmarshalled.
   * @throws UnmarshallException if there is a problem unmarshalling json to java.
   */
  public Object unmarshall(SerializerState state, Class clazz, Object json)
    throws UnmarshallException
  {

    // If we have a JSON object class hint that is a sub class of the
    // signature 'clazz', then override 'clazz' with the hint class.
    if (clazz != null && json instanceof JSONObject
      && ((JSONObject) json).has("javaClass")
      && clazz.isAssignableFrom(getClassFromHint(json)))
    {
      clazz = getClassFromHint(json);
    }

    // if no clazz type was passed in, look for the javaClass hint
    if (clazz == null)
    {
      clazz = getClassFromHint(json);
    }

    if (clazz == null)
    {
      throw new UnmarshallException("no class hint");
    }
    if (json == null || json == JSONObject.NULL)
    {
      if (!clazz.isPrimitive())
      {
        return null;
      }
      else
      {
        throw new UnmarshallException("can't assign null primitive");
      }
    }
    Serializer s = getSerializer(clazz, json.getClass());
    if (s != null)
    {
      return s.unmarshall(state, clazz, json);
    }

    throw new UnmarshallException("can't unmarshall");
  }

  /**
   * Marshall java into an equivalent json representation (JSONObject or JSONArray.)
   * <p/>
   * This involves finding the correct Serializer for the class of the given java object
   * and then invoking it to marshall the java object into json.
   * <p/>
   * The Serializer will invoke this method recursively while marshalling complex object graphs.
   *
   * @param state can be used by the underlying Serializer objects to hold state while marshalling.
   * @param o     java object to convert into json.
   * @return the JSONObject or JSONArray containing the json for the marshalled java object.
   * @throws MarshallException if there is a problem marshalling java to json.
   */
  public Object marshall(SerializerState state, Object o)
    throws MarshallException
  {
    if (o == null)
    {
      if (isDebug())
      {
        log.trace("marshall null");
      }
      return JSONObject.NULL;
    }
    if (isDebug())
    {
      log.trace("marshall class " + o.getClass().getName());
    }
    Serializer s = getSerializer(o.getClass(), null);
    if (s != null)
    {
      return s.marshall(state, o);
    }
    throw new MarshallException("can't marshall " + o.getClass().getName());
  }

  /**
   * Convert a Java objects (or tree of Java objects) into a string in JSON
   * format
   *
   * @param obj the object to be converted to JSON.
   * @return the JSON format string representing the data in the the Java
   *         object.
   */
  public String toJSON(Object obj) throws MarshallException
  {
    SerializerState state = new SerializerState();
    Object json = marshall(state, obj);
    return json.toString();
  }

  /**
   * Convert a string in JSON format into Java objects.
   *
   * @param jsonString the JSON format string.
   * @return an object (or tree of objects) representing the data in the JSON
   *         format string.
   */
  public Object fromJSON(String jsonString) throws UnmarshallException
  {
    JSONTokener tok = new JSONTokener(jsonString);
    Object json;
    try
    {
      json = tok.nextValue();
    }
    catch (ParseException e)
    {
      throw new UnmarshallException("couldn't parse JSON");
    }
    SerializerState state = new SerializerState();
    return unmarshall(state, null, json);
  }

  /**
   * Should serializers defined in this object include the fully
   * qualified class name of objects being serialized?  This can
   * be helpful when unmarshalling, though if not needed can
   * be left out in favor of increased performance and smaller
   * size of marshalled String.  Default is true.
   *
   * @return whether Java Class hints are included in the serialised
   *         JSON objects
   */
  public boolean getMarshallClassHints()
  {
    return marshallClassHints;
  }

  /**
   * Should serializers defined in this object include the fully
   * qualified class name of objects being serialized?  This can
   * be helpful when unmarshalling, though if not needed can
   * be left out in favor of increased performance and smaller
   * size of marshalled String.  Default is true.
   *
   * @param marshallClassHints flag to enable/disable inclusion
   *                           of Java class hints in the serialized JSON objects
   */
  public void setMarshallClassHints(boolean marshallClassHints)
  {
    this.marshallClassHints = marshallClassHints;
  }

  /**
   * Returns true if attributes will null values should still be included
   * in the serialized JSON object.  Defaults to true.  Set to false for
   * performance gains and small JSON serialized size.  Useful because null and
   * undefined for JSON object attributes is virtually the same thing.
   *
   * @return boolean value as to whether null attributes will be
   *         in the serialized JSON objects
   */
  public boolean getMarshallNullAttributes()
  {
    return marshallNullAttributes;
  }

  /**
   * Returns true if attributes will null values should still be included
   * in the serialized JSON object.  Defaults to true.  Set to false for
   * performance gains and small JSON serialized size.  Useful because null and
   * undefined for JSON object attributes is virtually the same thing.
   *
   * @param marshallNullAttributes flag to enable/disable marshalling of
   *                               null attributes in the serialized JSON objects
   */
  public void setMarshallNullAttributes(boolean marshallNullAttributes)
  {
    this.marshallNullAttributes = marshallNullAttributes;
  }
}
