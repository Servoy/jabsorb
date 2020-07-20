package org.json;

import junit.framework.TestCase;

public class XMLTest extends TestCase {

    public void testNumberStaysStringWhenNumberstringsAreNotConverted() {
        JSONObject json = XML.toJSONObject("<value>123</value>");

        Object value = json.get("value");
        assertEquals(String.class, value.getClass());
        assertEquals("123", value);
    }

    public void testSmallNumberBecomesLong() {
        JSONObject json = XML.toJSONObject("<value>123</value>", true);

        Object value = json.get("value");
        assertEquals(Long.class, value.getClass());
        assertEquals(new Long("123"), value);
    }

    public void testSmallNegativeNumberBecomesLong() {
        JSONObject json = XML.toJSONObject("<value>-123</value>", true);

        Object value = json.get("value");
        assertEquals(Long.class, value.getClass());
        assertEquals(new Long("-123"), value);
    }

    public void testSmallFractionNumberBecomesDouble() {
        JSONObject json = XML.toJSONObject("<value>123.4</value>", true);

        Object value = json.get("value");
        assertEquals(Double.class, value.getClass());
        assertEquals(new Double("123.4"), value);
    }

    public void testSmallFractionNegativeNumberBecomesDouble() {
        JSONObject json = XML.toJSONObject("<value>-123.4</value>", true);

        Object value = json.get("value");
        assertEquals(Double.class, value.getClass());
        assertEquals(new Double("-123.4"), value);
    }

    public void testMediumFractionNumberBecomesDouble() {
        JSONObject json = XML.toJSONObject("<value>12162019091.2</value>", true);

        Object value = json.get("value");
        assertEquals(Double.class, value.getClass());
        assertEquals(new Double("12162019091.2"), value);
    }
    
    public void testMediumFractionNumberWithCommanAsDecimalStaysString() {
        JSONObject json = XML.toJSONObject("<value>12162019091,2</value>", true);

        Object value = json.get("value");
        assertEquals(String.class, value.getClass());
        assertEquals("12162019091,2", value);
    }

    public void testMediumFractionNegativeNumberBecomesDouble() {
        JSONObject json = XML.toJSONObject("<value>-12162019091.2</value>", true);

        Object value = json.get("value");
        assertEquals(Double.class, value.getClass());
        assertEquals(new Double("-12162019091.2"), value);
    }

    public void testLargeNumberStaysString() {
        JSONObject json = XML.toJSONObject("<value>121620190912397792</value>", true);

        Object value = json.get("value");
        assertEquals(String.class, value.getClass());
        assertEquals("121620190912397792", value);
    }

    public void testLargeNegativeNumberStaysString() {
        JSONObject json = XML.toJSONObject("<value>-121620190912397792</value>", true);

        Object value = json.get("value");
        assertEquals(String.class, value.getClass());
        assertEquals("-121620190912397792", value);
    }

    public void testDoublevalueWithLostPrecisionStaysString() {
        JSONObject json = XML.toJSONObject("<value>12162019091.0000002</value>", true);

        Object value = json.get("value");
        assertEquals(String.class, value.getClass());
        assertEquals("12162019091.0000002", value);
    }

    public void testNonParsebleNumberStaysString() {
        JSONObject json = XML.toJSONObject("<value>1st</value>", true);

        Object value = json.get("value");
        assertEquals(String.class, value.getClass());
        assertEquals("1st", value);
    }
}