var unitTests={
  "Callable References":
  {
    tests:
    [
      { code:             'function(){ var callableRef = jsonrpc.test.getCallableRef(); return ({ oid:callableRef.objectID, ping:callableRef.ping(), refoid:callableRef.getRef().objectID, inside:callableRef.whatsInside(callableRef.getRef()) })}()',
        asyncCode: 'var __=function(){ var callableRef = jsonrpc.test.getCallableRef(); cb     ({ oid:callableRef.objectID, ping:callableRef.ping(), refoid:callableRef.getRef().objectID, inside:callableRef.whatsInside(callableRef.getRef()) })}()',
        test: 'result.ping == "ping pong" && result.inside =="a secret"'
      },
      { code:             'function(){ var callableRef = jsonrpc.test.getCallableRef(); var p = callableRef.ping();return({ping:p})}();',
        asyncCode: 'var __=function(){ var callableRef = jsonrpc.test.getCallableRef(); var p = callableRef.ping();cb    ({ping:p});}();',
        test: 'result.ping == "ping pong"'
      }
    ]
  },
  
  "Primitive Tests":
  {  
    tests:
    [
      { code: 'jsonrpc.test.echoChar("c")',
        test: 'result == "c"'
      },
      { code: 'jsonrpc.test.echoIntegerObject(1234567890)',
        test: 'result == 1234567890'
      },
      { code: 'jsonrpc.test.echoLongObject(1099511627776)',
        test: 'result == 1099511627776'
      },
      { code: 'jsonrpc.test.echoFloatObject(3.3)',
        test: 'result == 3.3'
      },
      { code: 'jsonrpc.test.echoDoubleObject(9.9)',
        test: 'result == 9.9'
      },
      { code: 'jsonrpc.test.echoBoolean(true)',
        test: 'result == true'
      },
      { code: 'jsonrpc.test.echoBoolean(false)',
        test: 'result == false'
      }
    ]
  },
  
  "Object Tests":
  {  
    tests:
    [
      { code: 'jsonrpc.test.concat("a","b")',
        test: 'result == "a and b"'
      },
      { code: 'jsonrpc.test.aBean()',
        test: 'result != null'
      },
      { code: 'jsonrpc.test.echoObject({ "javaClass": "org.jabsorb.test.Test$Waggle", "bang": "foo", "baz": 9, "bork": 5 })',
        test: 'result.javaClass == "org.jabsorb.test.Test$Waggle" && result.bang =="foo" && result.baz == 9 && result.bork == 5'
      },
      { code: 'jsonrpc.test.echoObjectArray([{ "javaClass": "org.jabsorb.test.Test$Waggle", "bang": "foo", "baz": 9, "bork": 5 }])',
        test: 'result[0].javaClass == "org.jabsorb.test.Test$Waggle" && result[0].bang =="foo" && result[0].baz == 9 && result[0].bork == 5'
      },
      { code: 'jsonrpc.test.echoRawJSON({ "field1": "test" })',
        test: 'result.field1 == "test"'
      },
    ]
  },

  "Date Tests":
  {  
    tests:
    [
      { code: 'jsonrpc.test.echoDateObject(new Date(1121689294000))',
        test: 'result.javaClass == "java.util.Date" && result.time == 1121689294000'
      }
    ]
  },
  
  "List Tests":
  {  
    tests:
    [
      { code: 'jsonrpc.test.echo([1,2,3])',
        test: 'result.length == 3 && result[0] == 1 && result[1] == 2 && result[2] == 3'
      },
      { code: 'jsonrpc.test.echo(["foo", "bar", "baz"])',
        test: 'result.length == 3 && result[0] == "foo" && result[1] == "bar" && result[2] == "baz"'
      },
      { code: 'jsonrpc.test.echo(["foo", null, "baz"])',
        test: 'result.length == 3 && result[0] == "foo" && result[1] == null && result[2] == "baz"'
      },
      { code: 'jsonrpc.test.echoList({"list":[20, 21, 22, 23, 24, 25, 26, 27, 28, 29], "javaClass":"java.util.Vector"})',
        test: 'result.list.constructor == Array'
      },
      { code: 'jsonrpc.test.echoList({"list":[null, null, null], "javaClass":"java.util.Vector"})',
        test: 'result.list.constructor == Array'
      },
      { code: 'jsonrpc.test.echoIntegerArray([1234, 5678])',
        test: 'result[0] == 1234 && result[1] == 5678'
      },
      { code: 'jsonrpc.test.echoByteArray("testing 123")',
        test: 'result == "testing 123"'
      },
      { code: 'jsonrpc.test.echoCharArray("testing 456")',
        test: 'result == "testing 456"'
      },
      { code: 'jsonrpc.test.echoBooleanArray([true, false, true])',
        test: 'result.length == 3 && result[0] == true && result[1] == false && result[2] == true'
      },
      { code: 'jsonrpc.test.anArray()',
        test: 'result.constructor == Array'
      },
      { code: 'jsonrpc.test.anArrayList()',
        test: 'result.list.constructor == Array'
      },
      { code: 'jsonrpc.test.aVector()',
        test: 'result.list.constructor == Array'
      },
      { code: 'jsonrpc.test.aList()',
        test: 'result.list.constructor == Array'
      }
    ]
  },
  
  "Set Tests":
  {  
    tests:
    [
      { code: 'jsonrpc.test.aSet()',
        test: 'result.set.constructor == Object'
      }
    ]
  },
  
  "Map Tests":
  {  
    tests:
    [
      { code: 'jsonrpc.test.aHashtable()',
        test: 'result.map.constructor == Object'
      },
      { code: 'jsonrpc.test.echo({ bang: "foo", baz: 9 })',
        test: 'result.javaClass == "org.jabsorb.test.Test$Waggle" && result.bang =="foo" && result.baz == 9'
      },
      { code: 'jsonrpc.test.echo({ bang: "foo", baz: 9, bork: 5 })',
        test: 'result.javaClass == "org.jabsorb.test.Test$Waggle" && result.bang =="foo" && result.baz == 9'
      },
      { code: 'jsonrpc.test.echo({ bang: "foo", baz: 9, bork: null })',
        test: 'result.javaClass == "org.jabsorb.test.Test$Waggle" && result.bang =="foo" && result.baz == 9'
      },
      { code: 'jsonrpc.test.echo({ foo: "bang", bar: 11 })',
        test: 'result.javaClass == "org.jabsorb.test.Test$Wiggle" && result.foo =="bang" && result.bar == 11'
      }
    ]
  },
  
  "Special Character Tests":
  {  
    tests:
    [
      { code: 'jsonrpc.test.voidFunction()',
        test: 'result == undefined'
      },
      { code: 'jsonrpc.test.throwException()',
        test: 'e == "java.lang.Exception: test exception"',
        exception: true
      },
      { code: 'jsonrpc.test.echo("hello")',
        test: 'result == "hello"'
      },
      { code: 'jsonrpc.test.echo("\\"")',
        test: 'result ==         "\\""'
      },
      { code: 'jsonrpc.test.echo("\\\\")',
        test: 'result ==         "\\\\"'
      },
      { code: 'jsonrpc.test.echo("\\b")',
        test: 'result ==         "\\b"'
      },
      { code: 'jsonrpc.test.echo("\\t")',
        test: 'result ==         "\\t"'
      },
      { code: 'jsonrpc.test.echo("\\n")',
        test: 'result ==         "\\n"'
      },
      { code: 'jsonrpc.test.echo("\\f")',
        test: 'result ==         "\\f"'
      },
      { code: 'jsonrpc.test.echo("\\r")',
        test: 'result ==         "\\r"'
      },
      { code: 'jsonrpc.test.echo(1234)',
        test: 'result == 1234'
      },
      { code:      'jsonrpc.test.echoRawJSON({ "field1": "azAZ019!@#$^&*()_+-=\\|;,.<>/`~{}[]%" })',
        asyncCode: 'jsonrpc.test.echoRawJSON(cb,{ "field1": "azAZ019!@#$^&*()_+-=\\|;,.<>/`~{}[]%" })',
        test: 'result.field1 == "azAZ019!@#$^&*()_+-=\\|;,.<>/`~{}[]%"'
      }
    ]
  }
}