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

package org.jabsorb.test;

import java.io.Serializable;

public class ConstructorTest implements Serializable
{
  /**
   * Generated Id
   */
  private static final long serialVersionUID = 1L;

  public static void main(String args[])
  {
    ConstructorTest c = new ConstructorTest(123);
    System.out.println(c.message);
  }

  public ConstructorTest(@SuppressWarnings("unused") int i,
      @SuppressWarnings("unused") String s)
  {
    message = "int,String";
  }

  public ConstructorTest(@SuppressWarnings("unused") int i,
      @SuppressWarnings("unused") int j)
  {
    message = "int,int";
  }

  public ConstructorTest(@SuppressWarnings("unused") int i)
  {
    message = "int";
  }

  final public String message;

  //Constructor Tests
  public ConstructorTest()
  {
    message = "default";
  }

  //adding this makes it fail many tests!
  /*
   * public ConstructorTest(Integer i) { message="int"; }
   */
  public ConstructorTest(@SuppressWarnings("unused") long l)
  {
    message = "long";
  }

  public ConstructorTest(@SuppressWarnings("unused") float l)
  {
    message = "float";
  }

  public ConstructorTest(@SuppressWarnings("unused") double l)
  {
    message = "double";
  }

  public ConstructorTest(@SuppressWarnings("unused") boolean b)
  {
    message = "boolean";
  }

  public ConstructorTest(@SuppressWarnings("unused") String s)
  {
    message = "String";
  }

  public ConstructorTest(@SuppressWarnings("unused") Object o)
  {
    message = "Object";
  }

  public String getMessage()
  {
    return message;
  }
  /**/
}
