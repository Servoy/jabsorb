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

import java.util.Iterator;
import java.util.List;

import org.jabsorb.serializer.response.FixUp;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A successful result that contains fixups.
 * 
 * @author William Becker
 */
public class FixupsResult extends SuccessfulResult
{
  /**
   * Optional fixup entries to run against the result in order to reconstitute
   * duplicate and / or circular references that were detected. This is a List
   * of FixUp objects.
   * 
   * @see FixUp
   */
  private final List fixUps;

  /**
   * Creates a new FixupsResult
   * 
   * @param id The id of the response.
   * @param o The main data to return.
   * @param fixups The fixups to return.
   */
  public FixupsResult(Object id, Object o, List fixups)
  {
    super(id, o);
    this.fixUps = fixups;
  }

  public JSONObject createOutput() throws JSONException
  {
    JSONObject o = super.createOutput();
    if (fixUps != null && fixUps.size() > 0)
    {
      JSONArray fixups = new JSONArray();
      for (Iterator i = fixUps.iterator(); i.hasNext();)
      {
        FixUp fixup = (FixUp) i.next();
        fixups.put(fixup.toJSONArray());
      }
      o.put("fixups", fixups);
    }
    return o;
  }
}
