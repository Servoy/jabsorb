/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: Serializer.java,v 1.2 2005/01/21 00:10:50 mclark Exp $
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

abstract class Serializer
{
    private JSONRPCBridge bridge;

    public void setBridge(JSONRPCBridge bridge) { this.bridge = bridge; }
    public JSONRPCBridge getBridge() { return bridge; }

    private static Class[] _serializableClasses = new Class[] { };
    private static Class[] _JSONClasses = new Class[] { };

    public Class[] getSerializableClasses() { return _serializableClasses; }
    public Class[] getJSONClasses() { return _JSONClasses; }

    public boolean canSerialize(Class clazz, Class jsonClazz)
    {
	boolean canJava = false, canJSON = false;

	Class serializableClasses[] = getSerializableClasses();
	for(int i=0; i < serializableClasses.length; i++)
	    if(clazz == serializableClasses[i]) canJava = true;

	if(jsonClazz == null) {
	    canJSON = true;
	} else {
	    Class jsonClasses[] = getJSONClasses();
	    for(int i=0; i < jsonClasses.length; i++)
		if(jsonClazz == jsonClasses[i]) canJSON = true;
	}

	return (canJava && canJSON);
    }

    public abstract ObjectMatch doTryToUnmarshall(Class clazz, Object jso)
	throws UnmarshallException;

    public abstract Object doUnmarshall(Class clazz, Object jso)
	throws UnmarshallException;

    public abstract Object doMarshall(Object o)
	throws MarshallException;

    public Object unmarshall(Class clazz, Object jso)
	throws UnmarshallException
    { return getBridge().unmarshall(clazz, jso); }

    public Object marshall(Object o)
	throws MarshallException
    { return getBridge().marshall(o); }

    public ObjectMatch tryToUnmarshall(Class clazz, Object jso)
	throws UnmarshallException
    { return getBridge().tryToUnmarshall(clazz, jso); }
    
    protected boolean isMarshalledObjectNull(Object o){
        { return getBridge().isMarshalledObjectNull(o); }
    }

}
