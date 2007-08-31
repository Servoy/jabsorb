/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: MethodKey.java,v 1.2 2006/03/06 12:41:32 mclark Exp $
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

public class MethodKey {

    private String methodName;

    private int numArgs;

    public String getMethodName() { return methodName; }
    
    public int getNumArgs() { return numArgs; }
    
    public MethodKey(String methodName, int numArgs) {
        this.methodName = methodName;
        this.numArgs = numArgs;
    }

    public int hashCode() {
        return methodName.hashCode() * numArgs;
    }

    public boolean equals(Object o) {
        if (!(o instanceof MethodKey))
            return false;
        return (methodName.equals(((MethodKey) o).methodName) && numArgs == ((MethodKey) o).numArgs);
    }

}
