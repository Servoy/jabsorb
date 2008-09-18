/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2008 The jabsorb team
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
package org.jabsorb.ext;

/**
 * A size (& line count for sourec code modules) analysis of a single file.
 * This is a Data Transformation Object for serializing and/or passing 
 * over JSON-RPC.
 * 
 * This is used by the ExtJS/project metrics demonstration in jabsorb.
 * 
 * @author Arthur Blake
 */
public class FileAnalysis 
{
  /**
   * Unique row id field.
   */
  private int id;

  /**
   * Number of lines in the file.
   */
  private int lines;

  /**
   * file name (not including extension)
   */
  private String name;

  /**
   * Path to the file (not including name and extension)
   */
  private String path;

  /**
   * Name of the project this file belongs to.
   */
  private String project;

  /**
   * File size in bytes
   */
  private long size;

  /**
   * true if the file is source/text.  If this is false, then the value
   * of lines is meaningless.  
   */
  private boolean src;
  
  /**
   * File extension (e.g.  .js  , .java, etc.)
   */
  private String type;

  /**
   * Get the unique id of this FileAnalyis object.
   * 
   * @return the unique id of this FileAnalyis object.
   */
  public int getId()
  {
    return id;
  }

  /**
   * Get the number of lines of source code.
   * 
   * @return the number of lines of source code.
   */
  public int getLines() 
  {
    return lines;
  }

  /**
   * The file name.
   * 
   * @return the file name
   */
  public String getName() 
  {
    return name;
  }

  /**
   * The file path.
   * @return the file path.
   */
  public String getPath() 
  {
    return path;
  }
  
  /**
   * Get the project name.
   * 
   * @return the project name.
   */
  public String getProject()
  {
    return project;
  }
  
  /**
   * Get the size in bytes.
   * @return the size in bytes.
   */
  public long getSize() 
  {
    return size;
  }

  /**
   * @return the type
   */
  public String getType()
  {
    return type;
  }

  /**
   * Get the src flag.
   * 
   * @return true if this is a source code file, otherwise false.
   */
  public boolean isSrc() 
  {
    return src;
  }

  /**
   * Set the unique id of this FileAnalyis object.
   * 
   * @param id the unique id of this FileAnalyis object.
   */
  public void setId(int id)
  {
    this.id = id;
  }

  /**
   * Set the number of lines of source code.
   * 
   * @param lines the number of lines of source code.
   */
  public void setLines(int lines) 
  {
    this.lines = lines;
  }

  /**
   * Set the file name.
   * 
   * @param name the file name.
   */
  public void setName(String name) 
  {
    this.name = name;
  }
  
  /**
   * Set the file path.
   * 
   * @param path the file path.
   */
  public void setPath(String path) 
  {
    this.path = path;
  }

  /**
   * Set the project name.
   * 
   * @param project the project name.
   */
  public void setProject(String project)
  {
    this.project = project;
  }

  /**
   * Set the size of the file in bytes.
   * 
   * @param size the size.
   */
  public void setSize(long size) 
  {
    this.size = size;
  }

  /**
   * Set the flag indicating if this is a source code file.
   * 
   * @param src true if this is a source code file.
   */
  public void setSrc(boolean src) 
  {
    this.src = src;
  }

  /**
   * @param type the type to set
   */
  public void setType(String type) 
  {
    this.type = type;
  }

}
