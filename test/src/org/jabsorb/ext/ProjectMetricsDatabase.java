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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.jabsorb.JSONSerializer;
import org.jabsorb.serializer.UnmarshallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface to an embedded Apache Derby SQL database.
 * 
 * If invoked as a program, it also can load FileAnalysis data from JSON 
 * files to create the embedded database.
 * 
 * This is used by the ExtJS/project metrics demonstration in jabsorb.
 * 
 * @author Arthur Blake
 */
public class ProjectMetricsDatabase
{
  /**
   * Test whether the database folder (and thus the database itself)
   * already exists or not.
   *
   * @return true if the database exists.
   */
  public static boolean exists()
  {
    return new File(getDerbyFolder()).exists();
  }

  /**
   * When this class is invoked as a program, it's used to create the database
   * from the .json input files.
   * 
   * @param args  one argument is expected which is the name of a json file to
   *              import into the newly created DB, if the arg is a path, then
   *              all .json files stored in that path (recursively) will be
   *              imported into the DB.
   * @throws SQLException if something goes wrong during DB creation.
   * @throws IOException  if something goes wrong while reading the filesystem.
   * @throws UnmarshallException if the json is in an unexpected format.
   */
  public static void main(String[] args)
    throws SQLException, IOException, UnmarshallException
  {
    PrintStream out = System.out;

    // if database already exists, just exit without recreating it
    if (ProjectMetricsDatabase.exists())
    {
      out.println("[OK] The Apache Derby database has already been created-- " + 
        "(remove the " + ProjectMetricsDatabase.getDerbyFolder() + 
        " folder if you need to rebuild it)");
      return;
    }

    if (args.length==0)
    {
      out.println("usage:  ProjectMetricsDatabase <path | file>");
      out.println("  Creates a local apache derby database and loads project ");
      out.println("  metrics information from one or more .json files.");
      out.println();
      out.println("  example:  ProjectMetricsDatabase test/rsrc/projectmetrics");
      System.exit(2);
    }

    File inputFile = new File(args[0]);

    if (!inputFile.exists())
    {
      out.println("path or file " + args[0] + " not found.");
      System.exit(3);
    }

    ProjectMetricsDatabase db = new ProjectMetricsDatabase();

    // get a connection and create the db if it doesn't yet exist
    Connection conn = null;

    // create the db tables
    try
    {
      conn = db.create();

      // drop the table
      log.info("dropping fileanalysis table");
      db.force(conn,"DROP TABLE FILEANALYSIS");

      // create table to hold all the json data
      // one row of this table maps one to one to a FileAnalysis object
      log.info("creating fileanalysis table");
      db.exec(conn, 
        "CREATE TABLE FILEANALYSIS (" +
        "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1," +
            "INCREMENT BY 1)," +
        "PROJECT VARCHAR(32) NOT NULL," +
        "PATH VARCHAR(1024) NOT NULL," +
        "NAME VARCHAR(128) NOT NULL," +
        "TYPE VARCHAR(64) NOT NULL," +
        "SRC SMALLINT NOT NULL," +
        "SIZE BIGINT NOT NULL," +
        "LINES INTEGER NOT NULL" +
        ")");

      db.loadFile(conn, inputFile);
    }
    finally
    {
      if (conn != null)
      {
        conn.close();
      }
    }
  }

  /**
   * Interface to logger.
   */
  private static final Logger log = 
    LoggerFactory.getLogger(ProjectMetricsDatabase.class);

  /**
   * Create a serializer for loading json objects from files.
   */
  private static JSONSerializer serializer = new JSONSerializer();

  /**
   * Use the default serializers and disable fixups.
   */
  static 
  {
    try 
    {
      serializer.registerDefaultSerializers();
      serializer.setFixupDuplicates(false);
      serializer.setFixupCircRefs(false);
    }
    catch (Exception e) 
    {
      log.error("couldn't register default serializers.", e);
    }
  }

  /**
   * Get the folder to store derby in.
   *
   * @param the folder in which the derby database will be stored.
   */
  private static String getDerbyFolder()
  {
    return System.getProperty("user.home") + "/.jabsorb/derby";
  }

  /**
   * Start up the project metrics embedded database.
   */
  public ProjectMetricsDatabase()
  {
    try
    {
      // just assume DB is located in a local path
      System.setProperty("derby.system.home", getDerbyFolder());

      // start apache derby embedded driver
      Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
    }
    catch (ClassNotFoundException cnf)
    {
      log.error("couldn't start db!", cnf);
      throw new RuntimeException("could not start embedded database");
    }
  }
  
  /**
   * Return a connection to the database.
   *
   * @return Connection to the database.
   * @throws SQLException if something goes wrong with the database.
   */
  public Connection connection() throws SQLException
  {
    return DriverManager.getConnection("jdbc:derby:projectanalysis");
  }

  /**
   * Create the database and return a connection to it.
   *
   * @return Connection to the newly created database.
   * @throws SQLException if something goes wrong.
   */
  private Connection create() throws SQLException
  {
    return DriverManager.getConnection("jdbc:derby:projectanalysis;create=true");
  }

  /**
   * Execute a single statement on the DB.
   * 
   * @param conn Connection to database.
   * @param sql SQL to execute.
   * @throws SQLException if a database problem occurs.
   */
  private void exec(Connection conn, String sql) throws SQLException
  {
    Statement s = null;
    try
    {
      s = conn.createStatement();
      s.execute(sql);
    }
    finally
    {
      if (s != null)
      {
        s.close();
      }
    }
  }

  /**
   * Exactly like the exec method , but if any SQLException is thrown, 
   * the Exception is supressed.
   * 
   * @param conn Connection to database.
   * @param sql SQL to execute.
   */
  private void force(Connection conn, String sql)
  {
    try
    {
      exec(conn,sql);
    }
    catch (SQLException ignore)
    {
      // ignore exception
    }
  }

  /**
   * Load a json file into the DB.  If the file is a directory, recursively 
   * load all json files stored in that folder.
   * 
   * @param conn Connection to the database.
   * @param file File or Folder to load.
   * 
   * @throws SQLException         if something goes wrong with the database. 
   * @throws UnmarshallException  if there is a problem loading a json file.
   * @throws IOException          if there is a filesystem problem.
   */
  private void loadFile(Connection conn, File file) 
    throws SQLException, IOException, UnmarshallException
  {
    String filename = file.getName();
    if (file.isDirectory())
    {
      File[] files = file.listFiles();
      log.info("loading contents of " + filename);

      for (int i=0,j=files.length ; i < j; i++)
      {
        loadFile(conn,files[i]);
      }
    }
    else
    {
      
      if (filename.toLowerCase().endsWith(".json")) // skip non-json files
      {
        log.info("  loading " + filename);
        store(conn, (List<FileAnalysis>) loadJSONFileIntoObject(file));
      }
    }
  }
  /**
   * Load a JSON object from a file.  blank lines are skipped, as well as lines 
   * that have the first two non-whitespace characters of "//" - these are 
   * considered comments.  Also lines ending with the single backslash (\) 
   * character are joined with the following line.
   *
   * @param f the file to load from
   *
   * @return the JSON object (de-serialized into a Java object)
   *
   * @throws IOException         if the file couldn't be found or read.
   * @throws UnmarshallException if the json couldn't be unmarshalled properly 
   *                             into a valid java object.
   */
  private Object loadJSONFileIntoObject(File f) 
    throws IOException, UnmarshallException
  {
    FileInputStream fin = new FileInputStream(f);
    try
    {
      return loadJSONStreamIntoObject(fin);
    }
    finally
    {
      fin.close();
    }
  }

  /**
   * Load a JSON object from a stream.  blank lines are skipped, as well as 
   * lines that have the first two non-whitespace characters of "//" - these are 
   * considered comments.  Also lines ending with the single backslash (\) 
   * character are joined with the following line.
   *
   * The input stream is closed after the contents are read.
   *
   * @param in InputStream to load JSON contents from.
   * @return  the JSON object (de-serialized into a Java object)
   *
   * @throws IOException         if there was an io error while reading the 
   *                             input stream.
   * @throws UnmarshallException if the json couldn't be unmarshalled properly 
   *                             into a valid java object.
   */
  private Object loadJSONStreamIntoObject(InputStream in) 
    throws IOException, UnmarshallException
  {
    if (in==null)
    {
      throw new UnmarshallException("no json data (stream is null)");
    }

    StringBuffer contents = new StringBuffer();
    try
    {
      LineNumberReader lr = new LineNumberReader(new InputStreamReader(in));

      String line,trimmed;
      while (true) 
      {
        line = lr.readLine();
        if (line == null) 
        {
          break;
        }

        trimmed = line.trim();

        // skip blank lines & commented lines
        if (trimmed.length()==0||trimmed.startsWith("//")) 
        {
          continue;
        }

        // look for line continuation character (\) as last char of line
        if (line.length()>0 && line.charAt(line.length()-1)=='\\') 
        {
          contents.append(line.substring(0,line.length()-1));
        } else 
        {
          contents.append(line);
          contents.append("\n");
        }
      }
    }
    catch (IOException e)
    {
      log.error("IOException occured", e);
      throw e;
    }
    finally
    {
      in.close();
    }

    try
    {
      return serializer.fromJSON(contents.toString());
    }
    catch (Exception e)
    {
      if (!(e instanceof UnmarshallException))
      {
        e = (UnmarshallException) 
          new UnmarshallException("unexpected exception").initCause(e); 
      }
      log.warn("Could not parse JSON from stream");
      throw (UnmarshallException)e;
    }
  }

  /**
   * Store a List of FileAnalysis objects into the database
   * as rows in the fileanalysis table.
   * 
   * @param conn Connection to the database.
   * @param list A List of FileAnalysis objects to store into the DB.
   * 
   * @throws SQLException         if something goes wrong with the database.
   */
  private void store(Connection conn, List<FileAnalysis> list) throws SQLException
  {
    if (list == null || list.size() == 0)
    {
      return;
    }

    String sql = 
      "insert into fileanalysis " +
      "(project, path, name, type, src, size, lines) " +
      "values (?,?,?,?,?,?,?)";

    PreparedStatement p = null;

    try
    {
      p = conn.prepareStatement(sql);
      
      for (FileAnalysis f:list)
      {
        p.setString(1, f.getProject());
        p.setString(2, f.getPath());
        p.setString(3, f.getName());
        p.setString(4, f.getType());
        p.setInt(5, (f.isSrc()?1:0));
        p.setLong(6, f.getSize());
        p.setInt(7,f.getLines());
        p.executeUpdate();
      }
    }
    finally
    {
      if (p != null)
      {
        p.close();
      }
    }
  }
}
