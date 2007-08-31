/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: ExceptionTransformer.java,v 1.1 2006/10/12 04:24:07 mclark Exp $
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

package com.metaparadigm.jsonrpc;

public interface ExceptionTransformer
{

    /**
     * Transform the exception to the format desired for transport to the
     * client.  This method should not itself throw an exception.
     * @return one of the JSON-compatible types (JSONObject, String,
     *   Boolean etc.), or a Throwable 
     */
    public Object transform(Throwable t);

}
