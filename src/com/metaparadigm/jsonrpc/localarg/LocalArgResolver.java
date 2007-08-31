/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: LocalArgResolver.java,v 1.2 2006/03/06 12:41:33 mclark Exp $
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

package com.metaparadigm.jsonrpc.localarg;

/**
 * Interface to be implemented by objects registered to locally resolve
 * method arguments using transport context information.
 */

public interface LocalArgResolver {
    /**
     * Resolve an argument locally using the given context information.
     *
     * @param context   The transport context (the HttpServletRequest
     object in the case of the HTTP transport).
     */
    public Object resolveArg(Object context) throws LocalArgResolveException;
}
