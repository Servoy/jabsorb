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
 * Result of a query for FileAnalysis information.
 * 
 * @author Arthur Blake
 */
public class FileAnalysisQueryResults
{
  /**
   * The query results page view that the user is viewing.
   */
  private FileAnalysis[] results;
  
  /** 
   * Total number of records (of which results may represent a subset)
   */
  private int totalCount;

  /**
   * Get the results.
   *
   * @return the results.
   */
  public FileAnalysis[] getResults()
  {
    return results;
  }

  /**
   * Get the totalCount.
   *
   * @return the totalCount.
   */
  public int getTotalCount()
  {
    return totalCount;
  }

  /**
   * Set the results.
   *
   * @param results the results.
   */
  public void setResults(FileAnalysis[] results)
  {
    this.results = results;
  }

  /**
   * Set the totalCount.
   *
   * @param totalCount the totalCount.
   */
  public void setTotalCount(int totalCount)
  {
    this.totalCount = totalCount;
  }
  
}
