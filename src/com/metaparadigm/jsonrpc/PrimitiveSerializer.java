/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: PrimitiveSerializer.java,v 1.1.1.1 2004/03/31 14:21:01 mclark Exp $
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

class PrimitiveSerializer extends Serializer
{
    private static Class[] _serializableClasses = new Class[]
	{ int.class, byte.class, short.class, long.class,
	  float.class, double.class };

    private static Class[] _JSONClasses = new Class[]
	{ Integer.class, Byte.class, Short.class, Long.class,
	  Float.class, Double.class };

    public Class[] getSerializableClasses() { return _serializableClasses; }
    public Class[] getJSONClasses() { return _JSONClasses; }


    public ObjectMatch doTryToUnmarshall(Class clazz, Object jso)
	throws UnmarshallException
    {
	return ObjectMatch.OKAY;
    }

    public Object doUnmarshall(Class clazz, Object jso)
	throws UnmarshallException
    {
	if(clazz == int.class) {
	    return new Integer(((Number)jso).intValue());
	} else  if(clazz == long.class) {
	    return new Long(((Number)jso).longValue());
	} else  if(clazz == short.class) {
	    return new Short(((Number)jso).shortValue());
	} else  if(clazz == byte.class) {
	    return new Byte(((Number)jso).byteValue());
	} else  if(clazz == float.class) {
	    return new Float(((Number)jso).floatValue());
	} else  if(clazz == double.class) {
	    return new Double(((Number)jso).doubleValue());
	}
	return null;
    }

    public Object doMarshall(Object o)
	throws MarshallException
    {
	return o;
    }

}
