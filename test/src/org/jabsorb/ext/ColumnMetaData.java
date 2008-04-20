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

import java.sql.Types;

/**
 * Meta data about one database column.
 *
 * This class is modeled from (and populated by) the column information 
 * available from the ResultSetMetaData interface.
 *
 * @author Arthur Blake
 * 
 * @see java.sql.ResultSetMetaData
 */
public class ColumnMetaData
{
  /**
   * The designated column's name.
   */
  private String columnName;

  /**
   * The designated column's table's catalog name.
   */
  private String catalogName;

  /**
   * The fully-qualified name of the Java class whose instances 
   * are manufactured if the method ResultSet.getObject is called 
   * to retrieve a value from the column.
   */
  private String columnClassName;

  /**
   * The designated column's normal maximum width in characters.
   */
  private int columnDisplaySize;

  /**
   * The designated column's suggested title for use in printouts and displays.
   */
  private String columnLabel;

  /**
   * The designated column's SQL type.
   */
  private int columnType;

  /**
   * true if the columnType can be treated like a String (and therefore the 
   * LIKE & not LIKE operator and various string functions can be used on it.)
   *
   * This field is derived from the columnType
   */
  private boolean characterType;

  /**
   * true if the column is a Lob or locator type,
   * Any column type where the data is not directly stored in the column, but 
   * needs a separate database lookup to retrieve the column data.
   *
   * such as CLOB, BLOB, XMLDATA, etc.
   *
   * This field is derived from the columnType
   */
  private boolean lobType;

  /**
   * The designated column's database-specific type name.
   */
  private String columnTypeName;

  /**
   * The designated column's specified column size.
   */
  private int precision;

  /**
   * The designated column's number of digits to right of the decimal point.
   */
  private int scale;

  /**
   * The designated column's table's schema.
   */
  private String schemaName;

  /**
   * The designated column's table name.
   */
  private String tableName;

  /**
   * Whether the designated column is automatically numbered.
   */
  private boolean autoIncrement;

  /**
   * Whether a column's case matters.
   */
  private boolean caseSensitive;

  /**
   * Whether the designated column is a cash value.
   */
  private boolean currency;

  /**
   * The nullability of values in the designated column.
   * one of columnNoNulls, columnNullable or columnNullableUnknown
   */
  private int nullable;

  /**
   * Whether the designated column is definitely not writable.
   */
  private boolean readOnly;

  /**
   * Whether the designated column can be used in a where clause.
   */
  private boolean searchable;

  /**
   * Whether values in the designated column are signed numbers.
   */
  private boolean signed;

  /**
   * Whether it is possible for a write on the designated column to succeed.
   */
  private boolean writable;

  /**
   * Whether a write on the designated column will definitely succeed.
   */
  private boolean definitelyWritable;

  public String getColumnName()
  {
    return columnName;
  }

  public void setColumnName(String columnName)
  {
    this.columnName = columnName;
  }

  public String getCatalogName()
  {
    return catalogName;
  }

  public void setCatalogName(String catalogName)
  {
    this.catalogName = catalogName;
  }

  public String getColumnClassName()
  {
    return columnClassName;
  }

  public void setColumnClassName(String columnClassName)
  {
    this.columnClassName = columnClassName;
  }

  public int getColumnDisplaySize()
  {
    return columnDisplaySize;
  }

  public void setColumnDisplaySize(int columnDisplaySize)
  {
    this.columnDisplaySize = columnDisplaySize;
  }

  public String getColumnLabel()
  {
    return columnLabel;
  }

  public void setColumnLabel(String columnLabel)
  {
    this.columnLabel = columnLabel;
  }

  public int getColumnType()
  {
    return columnType;
  }

  public void setColumnType(int columnType)
  {
    this.columnType = columnType;

    this.characterType =
        columnType == Types.CHAR ||
        columnType == Types.VARCHAR ||
        columnType == Types.LONGVARCHAR ||
        columnType == Types.NCHAR ||
        columnType == Types.NVARCHAR ||
        columnType == Types.LONGNVARCHAR;

    this.lobType =
        columnType == Types.BLOB ||
        columnType == Types.CLOB ||
        columnType == Types.REF ||
        columnType == Types.DATALINK ||
        columnType == Types.NCLOB ||
        columnType == Types.SQLXML;
  }

  /**
   * Determine if this column is a character type (String, VARCHAR, CHAR, etc.)
   * This is derived from the columnType
   * @return true if this column is a character type, else false.
   * @see java.sql.Types
   */
  public boolean isCharacterType()
  {
    return this.characterType;
  }

  /**
   * Determine if the column is a Lob or locator type,
   * Any column type where the data is not directly stored in the column, but needs a separate database
   * lookup to retrieve the column data.
   *
   * such as CLOB, BLOB, XMLDATA, etc.
   *
   * This is derived from the columnType

   * @return true if the column is a Lob type column.
   */
  public boolean isLobType()
  {
    return lobType;
  }

  public String getColumnTypeName()
  {
    return columnTypeName;
  }

  public void setColumnTypeName(String columnTypeName)
  {
    this.columnTypeName = columnTypeName;
  }

  public int getPrecision()
  {
    return precision;
  }

  public void setPrecision(int precision)
  {
    this.precision = precision;
  }

  public int getScale()
  {
    return scale;
  }

  public void setScale(int scale)
  {
    this.scale = scale;
  }

  public String getSchemaName()
  {
    return schemaName;
  }

  public void setSchemaName(String schemaName)
  {
    this.schemaName = schemaName;
  }

  public String getTableName()
  {
    return tableName;
  }

  public void setTableName(String tableName)
  {
    this.tableName = tableName;
  }

  public boolean isAutoIncrement()
  {
    return autoIncrement;
  }

  public void setAutoIncrement(boolean autoIncrement)
  {
    this.autoIncrement = autoIncrement;
  }

  public boolean isCaseSensitive()
  {
    return caseSensitive;
  }

  public void setCaseSensitive(boolean caseSensitive)
  {
    this.caseSensitive = caseSensitive;
  }

  public boolean isCurrency()
  {
    return currency;
  }

  public void setCurrency(boolean currency)
  {
    this.currency = currency;
  }

  public int getNullable()
  {
    return nullable;
  }

  public void setNullable(int nullable)
  {
    this.nullable = nullable;
  }

  public boolean isReadOnly()
  {
    return readOnly;
  }

  public void setReadOnly(boolean readOnly)
  {
    this.readOnly = readOnly;
  }

  public boolean isSearchable()
  {
    return searchable;
  }

  public void setSearchable(boolean searchable)
  {
    this.searchable = searchable;
  }

  public boolean isSigned()
  {
    return signed;
  }

  public void setSigned(boolean signed)
  {
    this.signed = signed;
  }

  public boolean isWritable()
  {
    return writable;
  }

  public void setWritable(boolean writable)
  {
    this.writable = writable;
  }

  public boolean isDefinitelyWritable()
  {
    return definitelyWritable;
  }

  public void setDefinitelyWritable(boolean definitelyWritable)
  {
    this.definitelyWritable = definitelyWritable;
  }
}
