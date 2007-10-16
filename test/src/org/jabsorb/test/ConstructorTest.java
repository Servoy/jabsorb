package org.jabsorb.test;

public class ConstructorTest
{
  public static void main(String args[])
  {
    ConstructorTest c = new ConstructorTest(123);
    System.out.println(c.message);
  }
  public ConstructorTest(int i,String s)
  {
    message="int,String";
  }
  public ConstructorTest(int i,int j)
  {
    message="int,int";
  }
  
  public ConstructorTest(int i)
  {
    message="int";
  }
  final public String message;
  //Constructor Tests
  public ConstructorTest()
  {
    message="default";
  }
 //adding this makes it fail many tests!
  /*public ConstructorTest(Integer i)
  {
    message="int";
  }*/
  public ConstructorTest(long l)
  {
    message="long";
  }
  public ConstructorTest(float l)
  {
    message="float";
  }
  public ConstructorTest(double l)
  {
    message="double";
  }
  public ConstructorTest(boolean b)
  {
    message="boolean";
  }
  public ConstructorTest(String s)
  {
    message="String";
  }
  public ConstructorTest(Object o)
  {
    message="Object";
  }
  
  public String getMessage()
  {
    return message;
  }
  /**/
}
