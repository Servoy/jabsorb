/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: ObjectMatch.java,v 1.1.1.1 2004/03/31 14:21:00 mclark Exp $
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

class ObjectMatch
{
    public final static ObjectMatch OKAY = new ObjectMatch(-1);
    public final static ObjectMatch NULL = new ObjectMatch(0);

    protected int mismatch;

    public ObjectMatch(int mismatch)
    {
	this.mismatch = mismatch;
    }

    public ObjectMatch max(ObjectMatch m)
    {
	if(this.mismatch > m.mismatch) return this;
	return m;
    }
}
