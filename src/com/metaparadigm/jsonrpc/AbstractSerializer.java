/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: AbstractSerializer.java,v 1.1 2005/02/24 03:05:51 mclark Exp $
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

/**
 * Convenience class for implementing Serializers providing default
 * setOwner and canSerialize implementations.
 */

public abstract class AbstractSerializer implements Serializer
{
    protected JSONSerializer ser;

    public void setOwner(JSONSerializer ser) { this.ser = ser; }

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
}
