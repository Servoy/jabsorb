package org.jabsorb.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public interface ITest
{
  static public class Waggle implements Serializable
  {

    private final static long serialVersionUID = 2;

    private int               baz;

    private String            bang;

    Integer                   bork;

    public Waggle()
    {
    }

    public Waggle(int i)
    {
      baz = i;
      bang = "!";
    }

    public int getBaz()
    {
      return baz;
    }

    public void setBaz(int baz)
    {
      this.baz = baz;
    }

    public String getBang()
    {
      return bang;
    }

    public void setBang(String bang)
    {
      this.bang = bang;
    }

    public Integer getBork()
    {
      return bork;
    }

    public void setBork(Integer bork)
    {
      this.bork = bork;
    }

    public String toString()
    {
      return "waggle " + baz + " and " + bang;
    }
  }

  static public class Wiggle implements Serializable
  {

    private final static long serialVersionUID = 2;

    private String            foo;

    private int               bar;

    public Wiggle()
    {
    }

    public Wiggle(int i)
    {
      bar = i;
      foo = "foo";
    }

    public String getFoo()
    {
      return foo;
    }

    public void setFoo(String foo)
    {
      this.foo = foo;
    }

    public int getBar()
    {
      return bar;
    }

    public void setBar(int bar)
    {
      this.bar = bar;
    }

    public String toString()
    {
      return "wiggle " + foo + " and " + bar;
    }
  }

  void voidFunction();

  String[] echo(String strings[]);

  int echo(int i);

  int[] echo(int i[]);

  String echo(String message);

  List echoList(List l);

  byte[] echoByteArray(byte ba[]);

  char[] echoCharArray(char ca[]);

  char echoChar(char c);

  boolean echoBoolean(boolean b);

  boolean[] echoBooleanArray(boolean ba[]);

  Integer[] echoIntegerArray(Integer i[]);

  Integer echoIntegerObject(Integer i);

  Long echoLongObject(Long l);

  Float echoFloatObject(Float f);

  Double echoDoubleObject(Double d);

  Date echoDateObject(Date d);

  Object echoObject(Object o);

  Object echoObjectArray(Object[] o);

  int[] anArray();

  ArrayList anArrayList();

  Vector aVector();

  List aList();

  Set aSet();

  BeanA aBean();

  Hashtable aHashtable();

  String[] twice(String string);

  String concat(String msg1, String msg2);

  ITest.Wiggle echo(ITest.Wiggle wiggle);

  ITest.Waggle echo(ITest.Waggle waggle);

  ArrayList aWiggleArrayList(int numWiggles);

  ArrayList aWaggleArrayList(int numWaggles);

  String wigOrWag(ArrayList al);
}
