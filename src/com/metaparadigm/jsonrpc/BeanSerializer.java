/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: BeanSerializer.java,v 1.1.1.1 2004/03/31 14:21:00 mclark Exp $
 *
 * Copyright Metaparadigm Pte. Ltd. 2004.
 * Michael Clark <michael@metaparadigm.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public (LGPL)
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details: http://www.gnu.org/
 *
 */

package com.metaparadigm.jsonrpc;

import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.BeanInfo;
import org.json.JSONObject;
import org.json.JSONArray;

class BeanSerializer extends Serializer
{
    private static HashMap beanCache = new HashMap();

    public boolean canSerialize(Class clazz, Class jsonClazz)
    {
	return (!clazz.isArray() &&
		!clazz.isPrimitive() &&
		!clazz.isInterface() &&
		(jsonClazz == null || jsonClazz == JSONObject.class));
    }

    private static class BeanData
    {
	private BeanInfo beanInfo;
	private HashMap readableProps;
	private HashMap writableProps;	
    }

    public static BeanData analyzeBean(Class clazz)
	throws IntrospectionException
    {
	System.out.println("BeanSerializer.analyzeBean analyzing " +
			   clazz.getName());
	BeanData bd = new BeanData();
	bd.beanInfo = Introspector.getBeanInfo(clazz, Object.class);
	PropertyDescriptor props[] = bd.beanInfo.getPropertyDescriptors();
	bd.readableProps = new HashMap();
	bd.writableProps = new HashMap();
	for(int i=0; i < props.length; i++) {
	    if(props[i].getWriteMethod() != null) {
		bd.writableProps.put(props[i].getName(),
				     props[i].getWriteMethod());
	    }
	    if(props[i].getReadMethod() != null) {
		bd.readableProps.put(props[i].getName(),
				     props[i].getReadMethod());
	    }
	}
	return bd;
    }

    public static BeanData getBeanData(Class clazz)
	throws IntrospectionException
    {
	BeanData bd;
	synchronized (beanCache) {
	    bd = (BeanData)beanCache.get(clazz);
	    if(bd == null) {
		bd = analyzeBean(clazz);
		beanCache.put(clazz, bd);
	    }
	}
	return bd;
    }

    public ObjectMatch doTryToUnmarshall(Class clazz, Object o)
	throws UnmarshallException
    {
	JSONObject jso = (JSONObject)o;
	BeanData bd = null;
	try {
	    bd = getBeanData(clazz);
	} catch (IntrospectionException e) {
	    throw new UnmarshallException(clazz.getName() + " is not a bean");
	}

	int match = 0, mismatch = 0;
	Iterator i = bd.writableProps.entrySet().iterator();
	while(i.hasNext()) {
	    Map.Entry ent = (Map.Entry)i.next();
	    String prop = (String)ent.getKey();
	    Method method = (Method)ent.getValue();
	    if(jso.has(prop)) match++;
	    else mismatch++;
	}
	if(match == 0) throw new UnmarshallException("bean has no matches");

	ObjectMatch m = null, tmp = null;
	i = jso.keys();
	while(i.hasNext()) {
	    String field = (String)i.next();
	    Method setMethod = (Method)bd.writableProps.get(field);
	    if(setMethod != null) {
		try {
		    Class param[] = setMethod.getParameterTypes();
		    if(param.length != 1)
			throw new UnmarshallException
			    ("bean " + clazz.getName() +
			     " method " + setMethod.getName() +
			     " does not have one arg");
		    tmp = tryToUnmarshall(param[0], jso.get(field));
		    if(m == null) m = tmp;
		    else m = m.max(tmp);
		} catch (UnmarshallException e) {
		    throw new UnmarshallException("bean " + clazz.getName() +
					     " " + e.getMessage());
		}
	    } else {
		mismatch++;
	    }
	}
	return m.max(new ObjectMatch(mismatch));
    }

    public Object doUnmarshall(Class clazz, Object o)
	throws UnmarshallException
    {
	JSONObject jso = (JSONObject)o;
	BeanData bd = null;
	try {
	    bd = getBeanData(clazz);
	} catch (IntrospectionException e) {
	    throw new UnmarshallException(clazz.getName() + " is not a bean");
	}
	if(getBridge().isDebug())
	    System.out.println("BeanSerializer.unmarshall instantiating " +
			       clazz.getName());
	Object instance = null;
	try {
	    instance = clazz.newInstance();
	} catch (Exception e) {
	    throw new UnmarshallException
		("can't instantiate bean " +
		 clazz.getName() + ": " + e.getMessage());
	}
	Object invokeArgs[] = new Object[1];
	Object fieldVal;
	Iterator i = jso.keys();
	while(i.hasNext()) {
	    String field = (String)i.next();
	    Method setMethod = (Method)bd.writableProps.get(field);
	    if(setMethod != null) {
		try {
		    Class param[] = setMethod.getParameterTypes();
		    fieldVal = unmarshall(param[0], jso.get(field));
		} catch (UnmarshallException e) {
		    throw new UnmarshallException("bean " + clazz.getName() +
						  " " + e.getMessage());
		}
		if(getBridge().isDebug())
		    System.out.println
			("BeanSerializer.unmarshall invoking " +
			 setMethod.getName() + "(" + fieldVal + ")");
		invokeArgs[0] = fieldVal;
		try {
		    setMethod.invoke(instance, invokeArgs);
		} catch (Throwable e) {
		    if(e instanceof InvocationTargetException)
			e = ((InvocationTargetException)e).
			    getTargetException();
		    throw new UnmarshallException
			("bean " + clazz.getName() + "can't invoke " +
			 setMethod.getName() + ": " + e.getMessage());
		}
	    }
	}
	return instance;
    }

    public Object doMarshall(Object o)
	throws MarshallException
    {
	BeanData bd = null;
	try {
	    bd = getBeanData(o.getClass());
	} catch (IntrospectionException e) {
	    throw new MarshallException
		(o.getClass().getName() + " is not a bean");
	}
	JSONObject val = new JSONObject();
	val.put("java_class", o.getClass().getName());
	Iterator i = bd.readableProps.entrySet().iterator();
	Object args[] = new Object[0];
	Object result = null;
	while(i.hasNext()) {
	    Map.Entry ent = (Map.Entry)i.next();
	    String prop = (String)ent.getKey();
	    Method getMethod = (Method)ent.getValue();
	    if(getBridge().isDebug())
		System.out.println
		    ("BeanSerializer.marshall invoking " +
		     getMethod.getName() + "()");
	    try {
		result = getMethod.invoke(o, args);
	    } catch (Throwable e) {
		if(e instanceof InvocationTargetException)
		    e = ((InvocationTargetException)e).
			getTargetException();
		throw new MarshallException
		    ("bean " + o.getClass().getName() + "can't invoke " +
		     getMethod.getName() + ": " + e.getMessage());
	    }
	    try {
		val.put(prop, marshall(result));
	    } catch (MarshallException e) {
		throw new MarshallException
		    ("bean " + o.getClass().getName() + " " + e.getMessage());
	    }
	}
	return val;
    }

}
