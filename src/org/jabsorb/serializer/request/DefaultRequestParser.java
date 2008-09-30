package org.jabsorb.serializer.request;

import org.jabsorb.JSONSerializer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple request parser that just returns the argument array without doing
 * anything
 */
public class DefaultRequestParser implements RequestParser
{
  public JSONArray unmarshallArguments(JSONObject jsonReq) throws JSONException
  {
    return jsonReq.getJSONArray(JSONSerializer.PARAMETER_FIELD);
  }
}
