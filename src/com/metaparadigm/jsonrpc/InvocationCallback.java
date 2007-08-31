/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: InvocationCallback.java,v 1.3.2.2 2005/12/10 07:54:10 mclark Exp $
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

import java.lang.reflect.Method;
import java.io.Serializable;

/**
 * Interface to be implemented by objects registered for invocation
 * callbacks with the JSONRPCBridge.
 */

public interface InvocationCallback extends Serializable
{

    /**
     * Callback before invocation of an RPC method.
     * @param context   The transport context (the HttpServletRequest
                        object in the case of the HTTP transport).
     * @param instance  The object instance or null if it is a static method.
     * @param method    Method that failed the invocation.
     * @param arguments The arguments passed to the method
     */
    public void preInvoke(Object context, Object instance,
			  Method method, Object arguments[])
	throws Exception;

    /**
     * Callback after invocation of an RPC method.
     * @param context   The transport context (the HttpServletRequest
                        object in the case of the HTTP transport).
     * @param instance  The object instance or null if it is a static method.
     * @param method    Method that failed the invocation.
     * @param result    The returned result from the method
     */
    public void postInvoke(Object context, Object instance,
			   Method method, Object result)
	throws Exception;

}
