/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: ErrorInvocationCallback.java,v 1.1.2.1 2005/12/10 07:54:10 mclark Exp $
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

/**
 * Interface to be implemented by objects registered for invocation
 * callbacks that include error information.
 */

public interface ErrorInvocationCallback extends InvocationCallback
{
    /**
     * Listener for exceptions thrown from an RPC service.
     * @param context   The transport context (the HttpServletRequest
                        object in the case of the HTTP transport).
     * @param instance  The object instance or null if it is a static method.
     * @param method    Method that failed the invocation.
     * @param error     Error resulting from the invocation.
     */
    public void invocationError(Object context, Object instance,
				Method method, Throwable error);
}
