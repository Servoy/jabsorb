/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007-2008 The jabsorb team
 *
 * based on original code from
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
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

package org.jabsorb.serializer.response.results;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Use this for all successful results to be returned from the server to the
 * client.
 * 
 * @author William Becker
 */
public class SuccessfulResult extends JSONRPCResult
{
  /**
   * The result of the call
   */
  private Object result;

  /**
   * Creates a new SuccessfulResult
   * 
   * @param id The id of the response
   * @param result The result of the call
   */
  public SuccessfulResult(Object id, Object result)
  {
    super(id);
    this.result = result;
  }

  @Override
  public JSONObject createOutput() throws JSONException
  {
    JSONObject o = super._createOutput();
    o.put("result", result);
    return o;
  }

}
