/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: SerializerState.java,v 1.1 2005/02/24 03:05:51 mclark Exp $
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

import java.util.HashMap;

/**
 * This class is used by Serializers to hold state during marshalling and
 * unmarshalling.
 */

public class SerializerState
{
    private HashMap stateMap = null;

    public Object get(Class clazz)
	throws InstantiationException, IllegalAccessException
    {
	Object o;
	if(stateMap == null) stateMap = new HashMap();
	else if((o = stateMap.get(clazz)) != null) return o;
	o = clazz.newInstance();
	stateMap.put(clazz, o);
	return o;
    }
}
