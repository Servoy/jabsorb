/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: JSONRPCBridgeServletArgResolver.java,v 1.1 2005/04/24 11:13:06 mclark Exp $
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class JSONRPCBridgeServletArgResolver implements LocalArgResolver
{
    public Object resolveArg(Object context) throws LocalArgResolveException
    {
	if(!(context instanceof HttpServletRequest))
	    throw new LocalArgResolveException("invalid context");

	HttpServletRequest request = (HttpServletRequest)context;
	HttpSession session = request.getSession();
	return session.getAttribute("JSONRPCBridge");
    }
}
