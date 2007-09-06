package org.json;

/*
 Copyright (c) 2002 JSON.org

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 The Software shall be used for Good, not Evil.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

/**
 * A JSONTokener takes a source string and extracts characters and tokens from
 * it. It is used by the JSONObject and JSONArray constructors to parse JSON
 * source strings.
 * 
 * @author JSON.org
 * @version 2
 */
public class JSONTokener
{

  /**
   * Get the hex value of a character (base16).
   * 
   * @param c A character between '0' and '9' or between 'A' and 'F' or between
   *          'a' and 'f'.
   * @return An int between 0 and 15, or -1 if c was not a hex digit.
   */
  public static int dehexchar(char c)
  {
    if (c >= '0' && c <= '9')
    {
      return c - '0';
    }
    if (c >= 'A' && c <= 'F')
    {
      return c - ('A' - 10);
    }
    if (c >= 'a' && c <= 'f')
    {
      return c - ('a' - 10);
    }
    return -1;
  }

  /**
   * Created: 17 April 1997 Author: Bert Bos <bert@w3.org>
   * 
   * unescape: http://www.w3.org/International/unescape.java
   * 
   * Copyright 1997 World Wide Web Consortium, (Massachusetts Institute of
   * Technology, European Research Consortium for Informatics and Mathematics,
   * Keio University). All Rights Reserved. This work is distributed under the
   * W3C Software License [1] in the hope that it will be useful, but WITHOUT
   * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
   * FITNESS FOR A PARTICULAR PURPOSE.
   * 
   * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
   * 
   * @param s The string to unescape
   * @return The string with escape codes replaced with their values.
   */
  private static String unescape(String s)
  {
    StringBuffer sbuf = new StringBuffer();
    int l = s.length();
    int ch = -1;
    int b, sumb = 0;
    for (int i = 0, more = -1; i < l; i++)
    {
      /* Get next byte b from URL segment s */
      switch (ch = s.charAt(i))
      {
        case '%':
          ch = s.charAt(++i);
          int hb = (Character.isDigit((char) ch) ? ch - '0' : 10 + Character
              .toLowerCase((char) ch) - 'a') & 0xF;
          ch = s.charAt(++i);
          int lb = (Character.isDigit((char) ch) ? ch - '0' : 10 + Character
              .toLowerCase((char) ch) - 'a') & 0xF;
          b = (hb << 4) | lb;
          break;
        // case '+':
        // b = ' ' ;
        // break ;
        default:
          b = ch;
      }
      /* Decode byte b as UTF-8, sumb collects incomplete chars */
      if ((b & 0xc0) == 0x80)
      { // 10xxxxxx (continuation byte)
        sumb = (sumb << 6) | (b & 0x3f); // Add 6 bits to sumb
        if (--more == 0)
          sbuf.append((char) sumb); // Add char to sbuf
      }
      else if ((b & 0x80) == 0x00)
      { // 0xxxxxxx (yields 7 bits)
        sbuf.append((char) b); // Store in sbuf
      }
      else if ((b & 0xe0) == 0xc0)
      { // 110xxxxx (yields 5 bits)
        sumb = b & 0x1f;
        more = 1; // Expect 1 more byte
      }
      else if ((b & 0xf0) == 0xe0)
      { // 1110xxxx (yields 4 bits)
        sumb = b & 0x0f;
        more = 2; // Expect 2 more bytes
      }
      else if ((b & 0xf8) == 0xf0)
      { // 11110xxx (yields 3 bits)
        sumb = b & 0x07;
        more = 3; // Expect 3 more bytes
      }
      else if ((b & 0xfc) == 0xf8)
      { // 111110xx (yields 2 bits)
        sumb = b & 0x03;
        more = 4; // Expect 4 more bytes
      }
      else
      /* if ((b & 0xfe) == 0xfc) */{ // 1111110x (yields 1 bit)
        sumb = b & 0x01;
        more = 5; // Expect 5 more bytes
      }
      /* We don't test if the UTF-8 encoding is well-formed */
    }
    return sbuf.toString();
  }

  /**
   * The index of the next character.
   */
  private int myIndex;

  /**
   * The source string being tokenized.
   */
  private String mySource;

  /**
   * Construct a JSONTokener from a string.
   * 
   * @param s A source string.
   */
  public JSONTokener(String s)
  {
    this.myIndex = 0;
    this.mySource = s;
  }

  /**
   * Back up one character. This provides a sort of lookahead capability, so
   * that you can test for a digit or letter before attempting to parse the next
   * number or identifier.
   */
  public void back()
  {
    if (this.myIndex > 0)
    {
      this.myIndex -= 1;
    }
  }

  /**
   * Determine if the source string still contains characters that next() can
   * consume.
   * 
   * @return true if not yet at the end of the source.
   */
  public boolean more()
  {
    return this.myIndex < this.mySource.length();
  }

  /**
   * Get the next character in the source string.
   * 
   * @return The next character, or 0 if past the end of the source string.
   */
  public char next()
  {
    if (more())
    {
      char c = this.mySource.charAt(this.myIndex);
      this.myIndex += 1;
      return c;
    }
    return 0;
  }

  /**
   * Consume the next character, and check that it matches a specified
   * character.
   * 
   * @param c The character to match.
   * @return The character.
   * @throws JSONException if the character does not match.
   */
  public char next(char c) throws JSONException
  {
    char n = next();
    if (n != c)
    {
      throw syntaxError("Expected '" + c + "' and instead saw '" + n + "'");
    }
    return n;
  }

  /**
   * Get the next n characters.
   * 
   * @param n The number of characters to take.
   * @return A string of n characters.
   * @throws JSONException Substring bounds error if there are not n characters
   *           remaining in the source string.
   */
  public String next(int n) throws JSONException
  {
    int i = this.myIndex;
    int j = i + n;
    if (j >= this.mySource.length())
    {
      throw syntaxError("Substring bounds error");
    }
    this.myIndex += n;
    return this.mySource.substring(i, j);
  }

  /**
   * Get the next char in the string, skipping whitespace and comments
   * (slashslash, slashstar, and hash).
   * 
   * @throws JSONException
   * @return A character, or 0 if there are no more characters.
   */
  public char nextClean() throws JSONException
  {
    for (;;)
    {
      char c = next();
      if (c == '/')
      {
        switch (next())
        {
          case '/':
            do
            {
              c = next();
            } while (c != '\n' && c != '\r' && c != 0);
            break;
          case '*':
            for (;;)
            {
              c = next();
              if (c == 0)
              {
                throw syntaxError("Unclosed comment");
              }
              if (c == '*')
              {
                if (next() == '/')
                {
                  break;
                }
                back();
              }
            }
            break;
          default:
            back();
            return '/';
        }
      }
      else if (c == '#')
      {
        do
        {
          c = next();
        } while (c != '\n' && c != '\r' && c != 0);
      }
      else if (c == 0 || c > ' ')
      {
        return c;
      }
    }
  }

  /**
   * Return the characters up to the next close quote character. Backslash
   * processing is done. The formal JSON format does not allow strings in single
   * quotes, but an implementation is allowed to accept them.
   * 
   * @param quote The quoting character, either <code>"</code>&nbsp;<small>(double
   *          quote)</small> or <code>'</code>&nbsp;<small>(single quote)</small>.
   * @return A String.
   * @throws JSONException Unterminated string.
   */
  public String nextString(char quote) throws JSONException
  {
    char c;
    StringBuffer sb = new StringBuffer();
    for (;;)
    {
      c = next();
      switch (c)
      {
        case 0:
        case '\n':
        case '\r':
          throw syntaxError("Unterminated string");
        case '\\':
          c = next();
          switch (c)
          {
            case 'b':
              sb.append('\b');
              break;
            case 't':
              sb.append('\t');
              break;
            case 'n':
              sb.append('\n');
              break;
            case 'f':
              sb.append('\f');
              break;
            case 'r':
              sb.append('\r');
              break;
            case 'u':
              sb.append((char) Integer.parseInt(next(4), 16));
              break;
            case 'x':
              sb.append((char) Integer.parseInt(next(2), 16));
              break;
            default:
              sb.append(c);
          }
          break;
        default:
          if (c == quote)
          {
            return unescape(sb.toString());
          }
          sb.append(c);
      }
    }
  }

  /**
   * Get the text up but not including the specified character or the end of
   * line, whichever comes first.
   * 
   * @param d A delimiter character.
   * @return A string.
   */
  public String nextTo(char d)
  {
    StringBuffer sb = new StringBuffer();
    for (;;)
    {
      char c = next();
      if (c == d || c == 0 || c == '\n' || c == '\r')
      {
        if (c != 0)
        {
          back();
        }
        return sb.toString().trim();
      }
      sb.append(c);
    }
  }

  /**
   * Get the text up but not including one of the specified delimeter characters
   * or the end of line, whichever comes first.
   * 
   * @param delimiters A set of delimiter characters.
   * @return A string, trimmed.
   */
  public String nextTo(String delimiters)
  {
    char c;
    StringBuffer sb = new StringBuffer();
    for (;;)
    {
      c = next();
      if (delimiters.indexOf(c) >= 0 || c == 0 || c == '\n' || c == '\r')
      {
        if (c != 0)
        {
          back();
        }
        return sb.toString().trim();
      }
      sb.append(c);
    }
  }

  /**
   * Get the next value. The value can be a Boolean, Double, Integer, JSONArray,
   * JSONObject, Long, or String, or the JSONObject.NULL object.
   * 
   * @throws JSONException If syntax error.
   * 
   * @return An object.
   */
  public Object nextValue() throws JSONException
  {
    char c = nextClean();
    String s;

    switch (c)
    {
      case '"':
      case '\'':
        return nextString(c);
      case '{':
        back();
        return new JSONObject(this);
      case '[':
        back();
        return new JSONArray(this);
    }

    /*
     * Handle unquoted text. This could be the values true, false, or null, or
     * it can be a number. An implementation (such as this one) is allowed to
     * also accept non-standard forms.
     * 
     * Accumulate characters until we reach the end of the text or a formatting
     * character.
     */

    StringBuffer sb = new StringBuffer();
    char b = c;
    while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0)
    {
      sb.append(c);
      c = next();
    }
    back();

    /*
     * If it is true, false, or null, return the proper value.
     */

    s = sb.toString().trim();
    if (s.equals(""))
    {
      throw syntaxError("Missing value");
    }
    if (s.equalsIgnoreCase("true"))
    {
      return Boolean.TRUE;
    }
    if (s.equalsIgnoreCase("false"))
    {
      return Boolean.FALSE;
    }
    if (s.equalsIgnoreCase("null"))
    {
      return JSONObject.NULL;
    }

    /*
     * If it might be a number, try converting it. We support the 0- and 0x-
     * conventions. If a number cannot be produced, then the value will just be
     * a string. Note that the 0-, 0x-, plus, and implied string conventions are
     * non-standard. A JSON parser is free to accept non-JSON forms as long as
     * it accepts all correct JSON forms.
     */

    if ((b >= '0' && b <= '9') || b == '.' || b == '-' || b == '+')
    {
      if (b == '0')
      {
        if (s.length() > 2 && (s.charAt(1) == 'x' || s.charAt(1) == 'X'))
        {
          try
          {
            return new Integer(Integer.parseInt(s.substring(2), 16));
          }
          catch (Exception e)
          {
            /* Ignore the error */
          }
        }
        else
        {
          try
          {
            return new Integer(Integer.parseInt(s, 8));
          }
          catch (Exception e)
          {
            /* Ignore the error */
          }
        }
      }
      try
      {
        return new Integer(s);
      }
      catch (Exception e)
      {
        try
        {
          return new Long(s);
        }
        catch (Exception f)
        {
          try
          {
            return new Double(s);
          }
          catch (Exception g)
          {
            return s;
          }
        }
      }
    }
    return s;
  }

  /**
   * Skip characters until past the requested string. If it is not found, we are
   * left at the end of the source.
   * 
   * @param to A string to skip past.
   */
  public boolean skipPast(String to)
  {
    this.myIndex = this.mySource.indexOf(to, this.myIndex);
    if (this.myIndex < 0)
    {
      this.myIndex = this.mySource.length();
      return false;
    }
    this.myIndex += to.length();
    return true;

  }

  /**
   * Skip characters until the next character is the requested character. If the
   * requested character is not found, no characters are skipped.
   * 
   * @param to A character to skip to.
   * @return The requested character, or zero if the requested character is not
   *         found.
   */
  public char skipTo(char to)
  {
    char c;
    int index = this.myIndex;
    do
    {
      c = next();
      if (c == 0)
      {
        this.myIndex = index;
        return c;
      }
    } while (c != to);
    back();
    return c;
  }

  /**
   * Make a JSONException to signal a syntax error.
   * 
   * @param message The error message.
   * @return A JSONException object, suitable for throwing
   */
  public JSONException syntaxError(String message)
  {
    return new JSONException(message + toString());
  }

  /**
   * Make a printable string of this JSONTokener.
   * 
   * @return " at character [this.myIndex] of [this.mySource]"
   */
  public String toString()
  {
    return " at character " + this.myIndex + " of " + this.mySource;
  }
}