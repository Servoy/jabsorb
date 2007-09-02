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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.jabsorb.json.JSONObject;
import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serialises java beans that are known to have readable and writable properties
 */
public class BeanSerializer extends AbstractSerializer
{
  /**
   * Used to check for circular reference detection.
   * 
   * TODO: Why not just use a HashSet?
   */
  public static class BeanSerializerState
  {
    // TODO: Legacy comment. WTF?
    // in absence of getters and setters, these fields are
    // public to allow subclasses to access.

    /**
     * Used for Circular reference detection
     */
    public HashSet beanSet = new HashSet();
  }

  /**
   * Stores the readable and writable properties for the Bean.
   */
  protected static class BeanData
  {
    // TODO: Legacy comment. WTF?
    // in absence of getters and setters, these fields are
    // public to allow subclasses to access.
    /**
     * The bean info for a certain bean
     */
    public BeanInfo beanInfo;

    /**
     * The readable properties of the bean.
     */
    public HashMap readableProps;

    /**
     * The writable properties of the bean.
     */
    public HashMap writableProps;
  }

  /**
   * Unique serialisation id.
   * 
   * TODO: should this number be generated?
   */
  private final static long serialVersionUID = 2;

  /**
   * The logger for this class
   * 
   * TODO: should logging happen only when debug mode is set (need to add debug
   * mode as well). If so we can get rid of this object.
   */
  private final static Logger log = LoggerFactory
      .getLogger(BeanSerializer.class);

  /**
   * Caches analysed beans
   */
  private static HashMap beanCache = new HashMap();

  /**
   * Classes that this can serialise.
   * 
   * TODO: Yay for bloat!
   */
  private static Class[] _serializableClasses = new Class[] {};

  /**
   * Classes that this can serialise to.
   * 
   * TODO: Yay for bloat!
   */
  private static Class[] _JSONClasses = new Class[] {};

  /**
   * Analyses a bean, returning a BeanData with the data extracted from it.
   * 
   * @param clazz
   *          The class of the bean to analyse
   * @return A populated BeanData
   * @throws IntrospectionException
   *           If a problem occurs during getting the bean info.
   */
  public static BeanData analyzeBean(Class clazz) throws IntrospectionException
  {
    log.info("analyzing " + clazz.getName());
    BeanData bd = new BeanData();
    bd.beanInfo = Introspector.getBeanInfo(clazz, Object.class);
    PropertyDescriptor props[] = bd.beanInfo.getPropertyDescriptors();
    bd.readableProps = new HashMap();
    bd.writableProps = new HashMap();
    for (int i = 0; i < props.length; i++)
    {
      if (props[i].getWriteMethod() != null)
      {
        bd.writableProps.put(props[i].getName(), props[i].getWriteMethod());
      }
      if (props[i].getReadMethod() != null)
      {
        bd.readableProps.put(props[i].getName(), props[i].getReadMethod());
      }
    }
    return bd;
  }

  /**
   * Gets the bean data from cache if possible, otherwise analyses the bean.
   * 
   * @param clazz
   *          The class of the bean to analyse
   * @return A populated BeanData
   * @throws IntrospectionException
   *           If a problem occurs during getting the bean info.
   */
  public static BeanData getBeanData(Class clazz) throws IntrospectionException
  {
    BeanData bd;
    synchronized (beanCache)
    {
      bd = (BeanData) beanCache.get(clazz);
      if (bd == null)
      {
        bd = analyzeBean(clazz);
        beanCache.put(clazz, bd);
      }
    }
    return bd;
  }

  public boolean canSerialize(Class clazz, Class jsonClazz)
  {
    return (!clazz.isArray() && !clazz.isPrimitive() && !clazz.isInterface() && (jsonClazz == null || jsonClazz == JSONObject.class));
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
    BeanSerializerState beanState;
    try
    {
      beanState = (BeanSerializerState) state.get(BeanSerializerState.class);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw new MarshallException("bean serializer internal error");
    }
    Integer identity = new Integer(System.identityHashCode(o));
    if (beanState.beanSet.contains(identity))
    {
      throw new MarshallException("circular reference");
    }
    beanState.beanSet.add(identity);

    BeanData bd = null;
    try
    {
      bd = getBeanData(o.getClass());
    }
    catch (IntrospectionException e)
    {
      throw new MarshallException(o.getClass().getName() + " is not a bean");
    }

    JSONObject val = new JSONObject();
    if (ser.getMarshallClassHints())
    {
      val.put("javaClass", o.getClass().getName());
    }
    Iterator i = bd.readableProps.entrySet().iterator();
    Object args[] = new Object[0];
    Object result = null;
    while (i.hasNext())
    {
      Map.Entry ent = (Map.Entry) i.next();
      String prop = (String) ent.getKey();
      Method getMethod = (Method) ent.getValue();
      if (ser.isDebug())
      {
        log.trace("invoking " + getMethod.getName() + "()");
      }
      try
      {
        result = getMethod.invoke(o, args);
      }
      catch (Throwable e)
      {
        if (e instanceof InvocationTargetException)
        {
          e = ((InvocationTargetException) e).getTargetException();
        }
        throw new MarshallException("bean " + o.getClass().getName()
            + " can't invoke " + getMethod.getName() + ": " + e.getMessage());
      }
      try
      {
        if (result != null || ser.getMarshallNullAttributes())
        {
          val.put(prop, ser.marshall(state, result));
        }
      }
      catch (MarshallException e)
      {
        throw new MarshallException("bean " + o.getClass().getName() + " "
            + e.getMessage());
      }
    }

    beanState.beanSet.remove(identity);
    return val;
  }

  public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object o)
      throws UnmarshallException
  {
    JSONObject jso = (JSONObject) o;
    BeanData bd = null;
    try
    {
      bd = getBeanData(clazz);
    }
    catch (IntrospectionException e)
    {
      throw new UnmarshallException(clazz.getName() + " is not a bean");
    }

    int match = 0;
    int mismatch = 0;
    Iterator i = bd.writableProps.entrySet().iterator();
    while (i.hasNext())
    {
      Map.Entry ent = (Map.Entry) i.next();
      String prop = (String) ent.getKey();
      if (jso.has(prop))
      {
        match++;
      }
      else
      {
        mismatch++;
      }
    }
    if (match == 0)
    {
      throw new UnmarshallException("bean has no matches");
    }

    ObjectMatch m = null;
    ObjectMatch tmp = null;
    i = jso.keys();
    while (i.hasNext())
    {
      String field = (String) i.next();
      Method setMethod = (Method) bd.writableProps.get(field);
      if (setMethod != null)
      {
        try
        {
          Class param[] = setMethod.getParameterTypes();
          if (param.length != 1)
          {
            throw new UnmarshallException("bean " + clazz.getName()
                + " method " + setMethod.getName() + " does not have one arg");
          }
          tmp = ser.tryUnmarshall(state, param[0], jso.get(field));
          if (m == null)
          {
            m = tmp;
          }
          else
          {
            m = m.max(tmp);
          }
        }
        catch (UnmarshallException e)
        {
          throw new UnmarshallException("bean " + clazz.getName() + " "
              + e.getMessage());
        }
      }
      else
      {
        mismatch++;
      }
    }
    if (m != null)
    {
      return m.max(new ObjectMatch(mismatch));
    }
    return new ObjectMatch(mismatch);
  }

  public Object unmarshall(SerializerState state, Class clazz, Object o)
      throws UnmarshallException
  {
    JSONObject jso = (JSONObject) o;
    BeanData bd = null;
    try
    {
      bd = getBeanData(clazz);
    }
    catch (IntrospectionException e)
    {
      throw new UnmarshallException(clazz.getName() + " is not a bean");
    }
    if (ser.isDebug())
    {
      log.trace("instantiating " + clazz.getName());
    }
    Object instance = null;
    try
    {
      instance = clazz.newInstance();
    }
    catch (Exception e)
    {
      throw new UnmarshallException("can't instantiate bean " + clazz.getName()
          + ": " + e.getMessage());
    }
    Object invokeArgs[] = new Object[1];
    Object fieldVal;
    Iterator i = jso.keys();
    while (i.hasNext())
    {
      String field = (String) i.next();
      Method setMethod = (Method) bd.writableProps.get(field);
      if (setMethod != null)
      {
        try
        {
          Class param[] = setMethod.getParameterTypes();
          fieldVal = ser.unmarshall(state, param[0], jso.get(field));
        }
        catch (UnmarshallException e)
        {
          throw new UnmarshallException("bean " + clazz.getName() + " "
              + e.getMessage());
        }
        if (ser.isDebug())
        {
          log.trace("invoking " + setMethod.getName() + "(" + fieldVal + ")");
        }
        invokeArgs[0] = fieldVal;
        try
        {
          setMethod.invoke(instance, invokeArgs);
        }
        catch (Throwable e)
        {
          if (e instanceof InvocationTargetException)
          {
            e = ((InvocationTargetException) e).getTargetException();
          }
          throw new UnmarshallException("bean " + clazz.getName()
              + "can't invoke " + setMethod.getName() + ": " + e.getMessage());
        }
      }
    }
    return instance;
  }
}
