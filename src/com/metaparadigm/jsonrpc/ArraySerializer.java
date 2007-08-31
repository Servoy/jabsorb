/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: ArraySerializer.java,v 1.5.2.2 2006/03/06 12:39:21 mclark Exp $
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

package com.metaparadigm.jsonrpc;

import java.lang.reflect.Array;
import org.json.JSONArray;

public class ArraySerializer extends AbstractSerializer
{
    private final static long serialVersionUID = 1;

    private static Class[] _serializableClasses = new Class[]
	{ int[].class, short[].class, long[].class,
	  float[].class, double[].class, boolean[].class,
	  Integer[].class, Short[].class, Long[].class,
	  Float[].class, Double[].class, Boolean[].class,
	  String[].class };

    private static Class[] _JSONClasses = new Class[]
	{ JSONArray.class };

    public Class[] getSerializableClasses() { return _serializableClasses; }
    public Class[] getJSONClasses() { return _JSONClasses; }

    public boolean canSerialize(Class clazz, Class jsonClazz)
    {
	Class cc = clazz.getComponentType();
	return (super.canSerialize(clazz, jsonClazz) ||
		((jsonClazz == null || jsonClazz == JSONArray.class) &&
		 (clazz.isArray() && !cc.isPrimitive())));
    }

    public ObjectMatch tryUnmarshall(SerializerState state,
				     Class clazz, Object o)
	throws UnmarshallException
    {
	JSONArray jso = (JSONArray)o;
 	Class cc = clazz.getComponentType();
	int i = 0;
	ObjectMatch m = new ObjectMatch(-1);
	try {
	    for(; i< jso.length(); i++)
		m = ser.tryUnmarshall(state, cc, jso.get(i)).max(m);
	} catch (UnmarshallException e) {
	    throw new UnmarshallException
		("element " + i + " " + e.getMessage());
	}
	return m;
    }

    public Object unmarshall(SerializerState state, Class clazz, Object o)
	throws UnmarshallException
    {
	JSONArray jso = (JSONArray)o;
	Class cc = clazz.getComponentType();
	int i = 0;
	try {
	    if(clazz == int[].class) {
		int arr[] = new int[jso.length()];
		for(; i< jso.length(); i++)
		    arr[i] = ((Number)ser.unmarshall
			      (state, cc, jso.get(i))).intValue();
		return (Object)arr;
	    } else if (clazz == byte[].class) {
		byte arr[] = new byte[jso.length()];
		for(; i< jso.length(); i++)
		    arr[i] = ((Number)ser.unmarshall
			      (state, cc, jso.get(i))).byteValue();
		return (Object)arr;
	    } else if (clazz == short[].class) {
		short arr[] = new short[jso.length()];
		for(; i< jso.length(); i++)
		    arr[i] = ((Number)ser.unmarshall
			      (state, cc, jso.get(i))).shortValue();
		return (Object)arr;
	    } else if (clazz == long[].class) {
		long arr[] = new long[jso.length()];
		for(; i< jso.length(); i++)
		    arr[i] = ((Number)ser.unmarshall
			      (state, cc, jso.get(i))).longValue();
		return (Object)arr;
	    } else if (clazz == float[].class) {
		float arr[] = new float[jso.length()];
		for(; i< jso.length(); i++)
		    arr[i] = ((Number)ser.unmarshall
			      (state, cc, jso.get(i))).floatValue();
		return (Object)arr;
	    } else if (clazz == double[].class) {
		double arr[] = new double[jso.length()];
		for(; i< jso.length(); i++)
		    arr[i] =((Number)ser.unmarshall
			     (state, cc, jso.get(i))).doubleValue();
		return (Object)arr;
	    } else if (clazz == char[].class) {
		char arr[] = new char[jso.length()];
		for(; i< jso.length(); i++)
		    arr[i] = ((String)ser.unmarshall
			      (state, cc, jso.get(i))).charAt(0);
		return (Object)arr;
	    } else if (clazz == boolean[].class) {
		boolean arr[] = new boolean[jso.length()];
		for(; i< jso.length(); i++)
		    arr[i] = ((Boolean)ser.unmarshall
			      (state, cc, jso.get(i))).booleanValue();
		return (Object)arr;
	    } else {
		Object arr[] = (Object[])Array.newInstance
		    (clazz.getComponentType(), jso.length());
		for(; i< jso.length(); i++)
		    arr[i] = ser.unmarshall(state, cc, jso.get(i));
		return (Object)arr;
	    }
	} catch (UnmarshallException e) {
	    throw new UnmarshallException
		("element " + i + " " + e.getMessage());
	}
    }

    public Object marshall(SerializerState state, Object o)
	throws MarshallException
    {
	JSONArray arr = new JSONArray();
	if(o instanceof int[]) {
	    int a[] = (int[])o;
	    for(int i=0; i < a.length; i++) arr.put(a[i]);
	} else if(o instanceof long[]) {
	    long a[] = (long[])o;
	    for(int i=0; i < a.length; i++) arr.put(a[i]);
	} else if(o instanceof short[]) {
	    short a[] = (short[])o;
	    for(int i=0; i < a.length; i++) arr.put(a[i]);
	} else if(o instanceof byte[]) {
	    byte a[] = (byte[])o;
	    for(int i=0; i < a.length; i++) arr.put(a[i]);
	} else if(o instanceof float[]) {
	    float a[] = (float[])o;
	    for(int i=0; i < a.length; i++) arr.put(a[i]);
	} else if(o instanceof double[]) {
	    double a[] = (double[])o;
	    for(int i=0; i < a.length; i++) arr.put(a[i]);
	} else if(o instanceof char[]) {
	    char a[] = (char[])o;
	    for(int i=0; i < a.length; i++) arr.put(a[i]);
	} else if(o instanceof boolean[]) {
	    boolean a[] = (boolean[])o;
	    for(int i=0; i < a.length; i++) arr.put(a[i]);
	} else if(o instanceof Object[]) {
	    Object a[] = (Object[])o;
	    for(int i=0; i < a.length; i++) arr.put(ser.marshall(state, a[i]));
	}
	return arr;
    }

}
