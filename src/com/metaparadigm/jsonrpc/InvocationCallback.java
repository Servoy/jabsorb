/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: InvocationCallback.java,v 1.2 2005/02/24 03:05:51 mclark Exp $
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

import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;

/**
 * Interface to be implemented by objects registered for invocation
 * callbacks with the JSONRPCBridge.
 */

public interface InvocationCallback {

    public void preInvoke(Object context, Object instance,
			  Method m, Object arguments[])
	throws Exception;

    public void postInvoke(Object context, Object instance,
			   Method m, Object result)
	throws Exception;

}
