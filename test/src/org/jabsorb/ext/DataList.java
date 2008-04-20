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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A List that can be easily populated with data from a Database.
 * The idea is that you can execute a query and grab all the results
 * of that query all in one line of code.
 *
 * The ResultSet metadata from the query is used to construct this List
 * of ordered Maps from the results of each row.
 * 
 * Each Map in the list has a keys which are the names of the columns and 
 * values which are Objects representing the result for the column in each row.
 *
 * There are also a few other useful methods for getting more information
 * about the outcome of the query and manipulating the output into JSON.
 * 
 * @author Arthur Blake
 */
public class DataList extends ArrayList
{
  /**
   * Build an array of ColumnMetaData object from a ResultSetMetaData object.
   *
   * @param rmd ResultSetMetaData to build ColumnMetaData from.
   * @return ColumnMetaData array or null if ResultSetMetaData is null.
   *
   * @throws SQLException if there is a problem processing the 
   * ResultSetMetaData object.
   */
  public static ColumnMetaData[] buildColumnMetaDataFromResultSetMetaData(
      ResultSetMetaData rmd) throws SQLException
  {
    if (rmd==null)
    {
      return null;
    }

    int j = rmd.getColumnCount();

    ColumnMetaData[] cmd = new ColumnMetaData[j];

    for (int i=1; i <= j; i++)
    {
      ColumnMetaData c = new ColumnMetaData();

      c.setColumnName(rmd.getColumnName(i));
      c.setCatalogName(rmd.getCatalogName(i));
      c.setColumnClassName(rmd.getColumnClassName(i));
      c.setColumnDisplaySize(rmd.getColumnDisplaySize(i));
      c.setColumnLabel(rmd.getColumnLabel(i));
      c.setColumnType(rmd.getColumnType(i));
      c.setColumnTypeName(rmd.getColumnTypeName(i));
      c.setPrecision(rmd.getPrecision(i));
      c.setScale(rmd.getScale(i));
      c.setSchemaName(rmd.getSchemaName(i));
      c.setTableName(rmd.getTableName(i));
      c.setAutoIncrement(rmd.isAutoIncrement(i));
      c.setCaseSensitive(rmd.isCaseSensitive(i));
      c.setCurrency(rmd.isCurrency(i));
      c.setNullable(rmd.isNullable(i));
      c.setReadOnly(rmd.isReadOnly(i));
      c.setSearchable(rmd.isSearchable(i));
      c.setSigned(rmd.isSigned(i));
      c.setWritable(rmd.isWritable(i));
      c.setDefinitelyWritable(rmd.isDefinitelyWritable(i));

      cmd[i-1] = c;
    }
    return cmd;
  }

  /**
   * Actual number of columns in the result set (irrespective of the actual 
   * column scanning count.)
   */
  private int columnCount = 0;
  
  /**
   * The column meta data that was dynamically created when the SQL
   * was run.
   */
  private ColumnMetaData[] columnMetaData;

  private int colSkip;   // a paging variable as passed in from constructors
  
  private int count = 0; // record scanning count.
  
  private boolean hitBottom = true;  // Was ResultSet fully read?
  
  private int pageSize;  // a paging variable as passed in from constructors
  
  private int pageWidth; // a paging variable as passed in from constructors
  
  private int skip;      // a paging variable as passed in from constructors

  
  /**
   * Create a DataList that is populated with data from a database.  
   * The resulting List contains a Map for each row returned in the query.  
   * The map is keyed by select column, and the values are the data values
   * Objects returned from the query.
   *
   * @param conn      connection to get data from
   * @param skip      number of rows to skip before beginning to return results
   * @param pageSize  number of results to return (even if more are available).  
   *                  All results are returned if this is not greater than 0.
   * @param colSkip   number of columns to skip in returned columnset
   * @param pageWidth number of columns to return (even if more are available).  
   *                  All columns up to end (if colSkip>0) are returned if this 
   *                  argument is not greater than 0.
   * @param sql       SQL query to execute as a prepared statement
   * @param bindVars  Array of bind variables for sql
   * @throws SQLException if something goes wrong while accessing the DB.
   */
  public DataList(Connection conn, int skip, int pageSize, int colSkip, 
      int pageWidth, String sql, Object[] bindVars) throws SQLException
  {
    super();
    PreparedStatement p = null;
    try
    {
      p = conn.prepareStatement(sql);

      if (bindVars != null)
      {
        for (int i = 1; i <= bindVars.length; i++)
        {
          p.setObject(i, bindVars[i - 1]);
        }
      }
      read(skip, pageSize, colSkip, pageWidth, p.executeQuery());
    }
    finally
    {
      if (p != null)
      {
        p.close();
      }
    }
  }

  /**
   * Generate a DataList from an already opened ResultSet.
   * NOTE: The ResultSet is closed after being used.
   *
   * @param skip      number of rows to skip before beginning to return results
   * @param pageSize  number of results to return (even if more are available).  
   *                  All results are returned if this is not greater than 0.
   * @param colSkip   number of columns to skip in returned columnset
   * @param pageWidth number of columns to return (even if more are available).  
   *                  All columns up to end (if colSkip>0) are returned if this 
   *                  argument is not greater than 0.
   * @param r         ResultSet to read for constructing this DataList.
   * 
   * @throws SQLException if something goes wrong while accessing the DB.
   */
  public DataList(int skip, int pageSize, int colSkip, int pageWidth, 
      ResultSet r) throws SQLException
  {
    super();
    read(skip, pageSize, colSkip, pageWidth, r);
  }

  /**
   * Get the column scan count.  This is always the same as the number of 
   * columns in the result set, It is not the number of columns for the columns 
   * that the caller was interested in, set via the colSkip and pageWidth 
   * arguments in the constructor but rather, the actual column count for all 
   * columns in the result set.
   *
   * @return the column scan count.
   */
  public int getColumnCount()
  {
    return this.columnCount;
  }

  /**
   * Get an array ColumnMetaData objects for all columns in the scan column set
   * for the ResultSet that was scanned.
   *
   * @return an array ColumnMetaData objects for all columns in the scan column set
   */
  public ColumnMetaData[] getColumns()
  {
    return columnMetaData;
  }

  /**
   * Get the number of columns that were skipped when the ResultSet used to 
   * create this DataList was read.
   * 
   * @return the number of columns that were skipped when the ResultSet used 
   * to create this DataList was read.
   */
  public int getColSkip()
  {
    return colSkip;
  }

  /**
   * Get the count of records scanned.  Note, this is not the same as the size 
   * of the list.  It's the count of records that were read, skipped or 
   * otherwise.  If hitBottom returns true, this will indicate the total count 
   * of the query/ResultSet, if hitBottom is not true, it indicates the number 
   * of records that were scanned before we stopped scanning.
   *
   * @return the record scan count.
   */
  public int getCount()
  {
    return this.count;
  }

  /**
   * Get the maximum number of rows that the caller specified could be returned 
   * in this DataList (the actual number of rows read might be less than this.)
   * 
   * @return the maximum number of rows that the caller specified could be 
   * returned in this DataList.
   */
  public int getPageSize()
  {
    return pageSize;
  }

  /**
   * Get the maximum number of columns that the caller specified could be 
   * returned in this DataList
   * 
   * (the actual number of columns read might be less than this.)
   * 
   * @return the maximum number of columns that the caller specified could be 
   * returned in this DataList.
   */
  public int getPageWidth()
  {
    return pageWidth;
  }

  /**
   * Get the number of rows that were skipped when the ResultSet used to create 
   * this DataList was read.
   * 
   * @return the number of rows that were skipped when the ResultSet used to 
   * create this DataList was read.
   */
  public int getSkip()
  {
    return skip;
  }

  /**
   * Return true, if when the ResultSet was scanned, it was completely 
   * drained... or "hit bottom".
   * 
   * This is useful to know in some circumstances where we can't figure out 
   * the count.
   *
   * @return true if query/result set was scanned all the way to its end.
   */
  public boolean hitBottom()
  {
    return this.hitBottom;
  }

  /**
   * Convert this DataList to a JSONArray for working with 
   * JavaScript more easily.
   * 
   * Each element of the JDONArray is a JSONObject (converted directly from 
   * the map of the underlying row from the DataList.
   *
   * @return The DataList as a JSONArray.
   */
  public JSONArray toJSON()
  {
    JSONArray json = new JSONArray();
    for (Iterator i = iterator(); i.hasNext();)
    {
      Map row = (Map) i.next();

      JSONObject obj = new JSONObject(row);
      json.put(obj);
    }
    return json;
  }

  /**
   * Read a ResultSet into this DataList.
   *
   * @param skip      number of rows to skip before beginning to return results
   * @param pageSize  number of results to return (even if more are available).  
   *                  All results are returned if this is not greater than 0.
   * @param colSkip   number of columns to skip in returned columnset
   * @param pageWidth number of columns to return (even if more are available).  
   *                  All columns up to end (if colSkip>0) are returned if this 
   *                  argument is not greater than 0.
   * @param r         ResultSet to read for constructing this DataList.
   * 
   * @throws SQLException if something goes wrong while accessing the DB.
   */
  private void read(int skip, int pageSize, int colSkip, int pageWidth, 
    ResultSet r) throws SQLException
  {
    // set the paging variables
    this.skip=skip;
    this.pageSize = pageSize;
    this.colSkip = colSkip;
    this.pageWidth = pageWidth;

    if (colSkip <= 0)
    {
      colSkip = 0;
    }

    // convert pageWidth to be the "last" column we want.
    if (pageWidth > 0)
    {
      pageWidth += colSkip;
    }
    else
    {
      pageWidth = 0;
    }

    colSkip += 1; // convert to one indexed.

    boolean usePageSize = pageSize > 0;

    int recordNum = 0;
    String strRecordNum = "0";

    try
    {
      // get meta data to automatically create line map
      ResultSetMetaData rm = r.getMetaData();

      columnMetaData = buildColumnMetaDataFromResultSetMetaData(rm);

      int j = this.columnCount = columnMetaData.length;

      // limit by pageWidth
      if (pageWidth > 0 && pageWidth < j)
      {
        j = pageWidth;
      }

      while (r.next())
      {
        recordNum++;
        strRecordNum = String.valueOf(recordNum);

        if (skip > 0)
        {
          skip--;
          continue;
        }
        Map line = new LinkedHashMap();

        String colName;

        for (int i = colSkip; i <= j; i++)
        {
          colName = columnMetaData[i-1].getColumnName();
          line.put(colName, r.getObject(colName));
        }

        add(line);

        if (usePageSize && --pageSize == 0)
        {
          this.hitBottom = false;
          break;
        }
      }

      this.count = recordNum;

      // this logic is a little tricky, careful here...
      // we DON't want to invoke r.next it already returned false 
      // (it might cause a SQLException)
      // this is an edge case
      if (!hitBottom && !r.next())
      {
        this.hitBottom = true;
      }
    }
    finally
    {
      if (r != null)
      {
        r.close();
      }
    }
  }
}
