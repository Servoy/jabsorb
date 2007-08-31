/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: SerializerState.java,v 1.2 2006/03/06 12:41:32 mclark Exp $
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

import java.util.HashMap;

/**
 * This class is used by Serializers to hold state during marshalling and
 * unmarshalling.
 */

public class SerializerState {

    private HashMap stateMap = null;

    public Object get(Class clazz) throws InstantiationException,
            IllegalAccessException {
        Object o;
        if (stateMap == null)
            stateMap = new HashMap();
        else if ((o = stateMap.get(clazz)) != null)
            return o;
        o = clazz.newInstance();
        stateMap.put(clazz, o);
        return o;
    }
}
