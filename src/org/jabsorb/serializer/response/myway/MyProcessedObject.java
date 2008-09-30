package org.jabsorb.serializer.response.myway;

import org.jabsorb.serializer.ProcessedObject;
import org.json.JSONObject;

public class MyProcessedObject extends ProcessedObject
{
  private Index index;

  public MyProcessedObject(Object object, String index)
  {
    super(object);
    this.index = new Index(index);
  }
  public MyProcessedObject(Object object)
  {
    super(object);
    this.index=new Index();
  }
  
  public void setIndexValue(String index)
  {
    this.index.setIndex(index);
  }

  public Index getIndex()
  {
    return index;
  }
  @Override
  public Object getSerialized()
  {
    final Object o =getRealSerialized(); 
    if((o ==null)||(o instanceof JSONObject))
    {
      return getIndex();
    }
    return o;
  }

  public Object getRealSerialized()
  {
    return super.getSerialized();
  }
}
