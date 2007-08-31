/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: JSONRPCResult.java,v 1.1.1.1 2004/03/31 14:21:00 mclark Exp $
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

import org.json.JSONObject;

class JSONRPCResult {

    private Object result = null;
    private int errorCode;

    public final static int CODE_SUCCESS = 0;
    public final static int CODE_EXCEPTION = 490;
    public final static int CODE_ERR_PARSE = 590;
    public final static int CODE_ERR_NOMETHOD = 591;
    public final static int CODE_ERR_UNMARSHALL = 592;
    public final static int CODE_ERR_MARSHALL = 593;

    public final static JSONRPCResult ERR_PARSE = new JSONRPCResult
	(CODE_ERR_PARSE, "couldn't parse request arguments");
    public final static JSONRPCResult ERR_NOMETHOD = new JSONRPCResult
	(CODE_ERR_NOMETHOD, "method not found");

    public JSONRPCResult(int errorCode, Object o)
    {
	this.errorCode = errorCode;
	this.result = o;
    }

    public String toString()
    {
	JSONObject o = new JSONObject();
	if(errorCode == CODE_SUCCESS) {
	    o.put("result", result);
	} else {
	    JSONObject err = new JSONObject();
	    err.put("code", new Integer(errorCode));
	    err.put("msg", result);
	    o.put("error", err);
	}
	return o.toString();
    }
}
