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
 * Represents a user request for a search on the FileAnalysis table.
 *  
 * @author Arthur Blake
 */
public class FileAnalysisQuery
{
  /** sort direction ASC|DESC */
  private String dir;

  /** how many results to return */
  private int limit;

  /**
   * Optional filter by name field.
   */
  private String name;

  /**
   * Optional filter by path field.
   */
  private String path;

  /**
   * Optional filter by project field.
   */
  private String project;

  /** sort column */
  private String sort;

  /** starting row */
  private int start;

  /**
   * Optional filter by type field.
   */
  private String type;

  /**
   * Get the sort direction.  This will be either ASC|DESC.
   * @return the sort direction.
   */
  public String getDir()
  {
    return dir;
  }

  /**
   * Get the number of results to return.
   * @return the result limit.
   */
  public int getLimit()
  {
    return limit;
  }

  /**
   * Get the file name filter.
   * @return the file name filter.
   */
  public String getName()
  {
    return name;
  }

  /**
   * Get the path filter.
   * @return the path filter.
   */
  public String getPath()
  {
    return path;
  }

  /**
   * Get the project filter.
   * @return the project filter.
   */
  public String getProject()
  {
    return project;
  }

  /**
   * Get the sort column.
   * @return the sort column.
   */
  public String getSort()
  {
    return sort;
  }

  /**
   * Get the starting row.
   * @return the starting row.
   */
  public int getStart()
  {
    return start;
  }

  /**
   * Get the type filter.
   * @return the type filter
   */
  public String getType()
  {
    return type;
  }

  /**
   * Set the sort direction.  This should be either ASC|DESC.
   * @param dir the sort direction.
   */
  public void setDir(String dir)
  {
    this.dir = dir;
  }

  /**
   * Set the number of results to return.
   * @param limit the result limit.
   */
  public void setLimit(int limit)
  {
    this.limit = limit;
  }
  
  /**
   * Set the name filter. 
   * @param name the name filter.
   */
  public void setName(String name)
  {
    this.name = name;
  }
  
  /**
   * Set the path filter.
   * @param path the path filter.
   */
  public void setPath(String path)
  {
    this.path = path;
  }

  /**
   * Set the project filter.
   * @param project the project filter.
   */
  public void setProject(String project)
  {
    this.project = project;
  }

  /**
   * Set the sort column.
   * @param sort the requested column to sort by.
   */
  public void setSort(String sort)
  {
    this.sort = sort;
  }

  /**
   * Set the starting row.
   * @param start the starting row.
   */
  public void setStart(int start)
  {
    this.start = start;
  }

  /**
   * Set the type filter.
   * @param type the type filter.
   */
  public void setType(String type)
  {
    this.type = type;
  }
  
}
