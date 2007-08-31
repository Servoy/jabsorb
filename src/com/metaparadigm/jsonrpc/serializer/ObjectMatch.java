/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: ObjectMatch.java,v 1.2 2006/03/06 12:41:32 mclark Exp $
 *
 * Copyright Metaparadigm Pte. Ltd. 2004.
 * Michael Clark <michael@metaparadigm.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.metaparadigm.jsonrpc.serializer;

/**
 * This class is returned from the Serializer tryUnmarshall method to indicate
 * number of mismatched fields. This is used to handle ambiguities with
 * JavaScript's typeless objects combined with and Java's operator overloading.
 */

public class ObjectMatch {

    public final static ObjectMatch OKAY = new ObjectMatch(-1);
    public final static ObjectMatch NULL = new ObjectMatch(0);

    private int mismatch;

    public int getMismatch() {
        return mismatch;
    }

    public ObjectMatch(int mismatch) {
        this.mismatch = mismatch;
    }

    public ObjectMatch max(ObjectMatch m) {
        if (this.mismatch > m.mismatch)
            return this;
        return m;
    }
}
