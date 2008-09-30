package org.jabsorb.serializer.response.myway;

public class Index
{
  private String index;

  public Index(String index)
  {
    this.index = index;
  }
  public Index()
  {
    this(null);
  }
  public String getIndex()
  {
    return index;
  }

  public void setIndex(String index)
  {
    this.index = index;
  }

  public String toString()
  {
    return index;
  }
}
