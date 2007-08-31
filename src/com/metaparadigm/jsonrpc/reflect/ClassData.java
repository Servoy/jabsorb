/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: ClassData.java,v 1.2 2006/03/06 12:41:32 mclark Exp $
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

package com.metaparadigm.jsonrpc.reflect;

import java.util.HashMap;

public class ClassData {
    protected Class clazz;

    // key methodKey, val (Method || Method[])
    protected HashMap methodMap;

    // key methodKey, val (Method || Method[])
    protected HashMap staticMethodMap;

    public Class getClazz() { return clazz; }
    
    public HashMap getMethodMap() { return methodMap; }
    
    public HashMap getStaticMethodMap() { return staticMethodMap; }
}
