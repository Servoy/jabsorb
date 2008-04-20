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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class to simplify running queries that run a count.
 * 
 * @author Arthur Blake
 */
public class CountQuery
{
  private int count = 0;

  /**
   * Execute a SQL statement that is a simple count of rows.
   * Handles all the details of the query, and getting the count.
   * The count may be obtained by calling the getCount() method after calling
   * this constructor.
   *
   * @param conn     connection to get data from
   * @param sql      sql query to execute as a prepared statement
   * @param bindVars array of bind variables for sql
   * @throws SQLException if any error occurs, including unexpected results from a simple count query.
   */
  public CountQuery(Connection conn, String sql, Object[] bindVars) throws SQLException
  {
    List dataList = new DataList(conn, 0, 0, 0, 0, sql, bindVars);
    if (dataList.size() != 1)
    {
      throw new SQLException("got unexpected (other than one) result row count from query.");
    }

    Map row = (Map) dataList.get(0);
    Set keys = row.keySet();

    if (keys.size() != 1)
    {
      throw new SQLException("got unexpected (other than one) result column from query.");
    }
    Iterator i = keys.iterator();

    Object firstKey = i.next();
    Object value = row.get(firstKey);
    if (value instanceof Number)
    {
      count = ((Number) value).intValue();
    }
    else
    {
      throw new SQLException("got unexpected type from count query (was expecting Number)");
    }
  }

  /**
   * Get the count results from the query.
   *
   * @return the count obtained from the count query
   */
  public int getCount()
  {
    return count;
  }
}
