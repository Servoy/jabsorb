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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * JSON-RPC call API for the ExtJS/project metrics jabsorb demo.
 * The public methods in this class can be called directly from the browser.
 * Objects passed and returned are passed to the JavaScript side via JSON-RPC.
 * 
 * @author Arthur Blake
 */
public class ProjectMetricsHandler
{
  // interface to Apache Derby embedded database
  private static final ProjectMetricsDatabase db = new ProjectMetricsDatabase(); 

  /**
   * Get the list of all projects as a JSON array.
   *
   * This demonstrates an alternative way to interact with the client and DB
   * without having to define intermediate Data Transformation Objects.
   *
   * @param query query object that ExtJS will send us (we ignore it.)
   * @return A JSONArray containing one object for each project.
   */
  public JSONArray getProjects(JSONObject query) 
    throws SQLException,JSONException
  {
    // Quick n dirty way to get the results of some SQL directly as JSON.
    DataList list = new DataList(db.connection(),0,0,0,0,
        "SELECT DISTINCT PROJECT as LABEL, " +
        "PROJECT as VALUE FROM FILEANALYSIS ORDER BY LABEL",null);
    addAllChoice(list);
    return list.toJSON();
  }

  /**
   * Get the list of all file extension types as a JSON array.
   * 
   * This demonstrates an alternative way to interact with the client and DB
   * without having to define intermediate Data Transformation Objects.
   *
   * @param query query object that ExtJS will send us (we ignore it.)
   * @return A JSONArray containing one object for each file extension type.
   */
  public JSONArray getTypes(JSONObject query) 
    throws SQLException,JSONException
  {
    // Quick n dirty way to get the results of some SQL directly as JSON.
    DataList list = new DataList(db.connection(),0,0,0,0,
        "SELECT DISTINCT TYPE as LABEL, " +
        "TYPE as VALUE FROM FILEANALYSIS ORDER BY LABEL",null);
    addAllChoice(list);
    return list.toJSON();
  }
  
  /**
   * Translate a FileAnalysisQuery object coming from the ExtJS proxy 
   * into a SQL query, run the query, and return the results.
   * 
   * Note:  This method is called directly from the browser over jabsorb's
   * JSON-RPC proxy.
   * 
   * This uses DTOs (Data Transformation Objects), FileAnalysisQuery
   * and FileAnalysisQueryResults to move data back and forth from the 
   * browser.
   * 
   * @param query requested search
   * @return QueryResults object containing the requested results.
   */
  public FileAnalysisQueryResults queryRecords(FileAnalysisQuery query)
    throws SQLException
  {
    FileAnalysisQueryResults results = new FileAnalysisQueryResults();

    if (query != null)
    {
      Connection c = null;
      try
      {
        c = db.connection();

        // dynamically build where clause based on query filter parameters
        StringBuffer whereClause = new StringBuffer();
        List args = new ArrayList();

        // build whereClause....
        addWhereParm(whereClause, args,
            "PROJECT", "=", query.getProject());
        
        addWhereParm(whereClause, args,
            "PATH", "LIKE", query.getPath());

        addWhereParm(whereClause, args,
            "NAME", "LIKE", query.getName());

        addWhereParm(whereClause, args,
            "TYPE", "=", query.getType());

        // get bind variables
        Object[] bindVars = args.toArray();

        // make a query to count how many results
        CountQuery count = 
          new CountQuery(c, "SELECT COUNT(*) FROM FILEANALYSIS"
              + whereClause, bindVars);

        // order results based on user's sort request
        whereClause.append(" ORDER BY ");
        String sort = query.getSort();
        if (blankOrNull(sort))
        {
          sort = "1"; // sort by first column if no sort specified.
        }
        whereClause.append(sort);
        if ("DESC".equals(query.getDir()))
        {
          whereClause.append(" DESC");
        }

        DataList data = new DataList(c, query.getStart(), query.getLimit(), 
            0, 0, 
          "SELECT ID,PROJECT,PATH,NAME,TYPE,SRC,SIZE,LINES FROM FILEANALYSIS" 
            + whereClause, bindVars);
        
        results.setTotalCount(count.getCount());
        
        FileAnalysis[] arr = new FileAnalysis[data.size()];
        int idx=0;
        for (Iterator i= data.iterator(); i.hasNext();)
        {
          Map m = (Map)i.next();

          FileAnalysis f = new FileAnalysis();
          
          f.setId(((Integer)m.get("ID")).intValue());
          f.setProject((String)m.get("PROJECT"));
          f.setPath((String)m.get("PATH"));
          f.setName((String)m.get("NAME"));
          f.setType((String)m.get("TYPE"));
          f.setSrc(integerToBool((Integer)m.get("SRC")));
          f.setSize(((Long)m.get("SIZE")).longValue());
          f.setLines(((Integer)m.get("LINES")).intValue());
          
          arr[idx++] = f;
        }
        results.setResults(arr);
      }
      finally
      {
        if (c != null)
        {
          c.close();
        }
      }
    }
    return results;
  }

  /**
   * Insert an "All" choice at the drop of a List used to construct 
   * a dropdown combobox for the client.
   * 
   * @param list list to add All choice to.
   */
  private void addAllChoice(List list)
  {
    Map allChoice = new HashMap();
    allChoice.put("LABEL", "All");
    allChoice.put("VALUE","");
    list.add(0, allChoice);
  }

  /**
   * Add a where parameter to a where clause that is being built up.
   * If the object being bound is null or blank, then skip it.
   *  
   * @param clause StringBuffer to hold where clause being built up.
   * @param args   List to hold SQL bind variables.
   * @param field  database field being bound.
   * @param operator query operator.
   * @param bindVar  optional bind variable. (if null or blank, it is not bound)
   */
  private void addWhereParm(StringBuffer clause, List args, 
      String field, String operator, Object bindVar)
  {
    // if the object being bound is blank or null, skip it
    if (blankOrNull(bindVar))
    {
      return;
    }
    
    int size = args.size();
    if (size==0)
    {
      clause.append(" WHERE ");
    }
    
    if (size>0)
    {
      clause.append(" AND ");
    }
    
    clause.append(field);
    clause.append(" ");
    clause.append(operator);
    clause.append(" ?");

    if ("LIKE".equals(operator))
    {
      args.add("%" + bindVar + "%");
    }
    else
    {
      args.add(bindVar);
    }
  }
  
  /**
   * Convenience method to determine if an object is null, or it's String
   * representation is blank or null.
   * 
   * @param obj Object to test.
   * @return true if object is null or blank.
   */
  private boolean blankOrNull(Object obj)
  {
    return obj==null || obj.toString()==null || obj.toString().length()==0;
  }

  /**
   * Helper method to convert an Integer to a boolean (Derby doesn't have
   * boolean types)
   * 
   * @param i Integer.
   * @return true if the Integer is 1.
   */
  private boolean integerToBool(Integer i)
  {
    return i!=null && i.intValue()==1;
  }
}
