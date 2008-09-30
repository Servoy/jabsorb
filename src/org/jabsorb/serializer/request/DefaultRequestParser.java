package org.jabsorb.serializer.request;

import org.jabsorb.JSONSerializer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DefaultRequestParser implements RequestParser
{
  public JSONArray unmarshallArguments(JSONObject jsonReq) throws JSONException
  {
    return jsonReq.getJSONArray(JSONSerializer.PARAMETER_FIELD);
  }
}
