var unitTests={
  "Circular References":
  {
    tests: 
    [
      // circular reference from server
      { code: function(jsonrpc,cb){return jsonrpc.test.aBean(cb)},
        test: function(result){ return result.beanB.beanA == result;}
      },

      //server circ ref test of a Map
      { code: function(jsonrpc,cb){return jsonrpc.test.aCircRefMap(cb)},
        test: function(result){ return result.map.me===result;}
      },
      //server circ ref test of a List generated on JS side
      { code: function(jsonrpc,cb)
              {
                var circRefList =
                {
                  "list": [
                    0,
                    1,
                    2,
                    {
                      "javaClass": "org.jabsorb.test.BeanB",
                      "beanA": {
                        "javaClass": "org.jabsorb.test.BeanA",
                        "id": 0
                      },
                      "id": 0
                    },
                    {
                      "javaClass": "java.util.HashMap",
                      "map": {
                        "2": "two",
                        "0": "zero",
                        "1": "one"
                      }
                    }
                  ],
                  "javaClass": "java.util.ArrayList"
                };
        
                circRefList.list[3].beanA.beanB = circRefList.list[3];
                circRefList.list[4].map.buckle_my_shoe = circRefList;
                circRefList.list[4].map.aBean = circRefList.list[3].beanA;
                return jsonrpc.test.echoObject(circRefList,cb)
              },
        test: function(result){ return result.list[4].map.buckle_my_shoe===result;}
      },
      //server circ ref test of an identical List generated on Java side
      { code: function(jsonrpc,cb){return jsonrpc.test.aCircRefList(cb)},
        test: function(result){ return result.list[4].map.buckle_my_shoe===result;}
      },
      // circular references 1
      { code: function(jsonrpc,cb)
              {
                // the simplest circular reference case
                var circRef1 = {};
                circRef1.circRef1 = circRef1;
                return jsonrpc.test.echoRawJSON({"field1": circRef1},cb)
              },
        test: function(result){ return result.field1.circRef1 === result.field1.circRef1.circRef1;}
      },
      // circular references 2
      { code: function(jsonrpc,cb)
              {
                // another simple circular reference
                var aaa = {};
                var bbb = {};
                aaa.bbb=bbb;
                bbb.aaa=aaa;
                return jsonrpc.test.echoRawJSON(aaa,cb)
              },
        test: function(result){ return (result.bbb.aaa===result);}
      },
      // circular references 3
      { code: function(jsonrpc,cb)
              {
                // a circular reference in an array
                var circ3 = {};
                circ3.arr=[3,1,4,1,circ3,9,2];
                return jsonrpc.test.echoRawJSON({"field1": circ3},cb);
              },
        test: function(result){ return result.field1.arr[4]===result.field1 
                   && result.field1.arr.length === 7;}
      },
      //duplicates test
      { code: function(jsonrpc,cb)
              {
                // an example of an object that has a lot of duplicates
                var dup1 = {};
        
                dup1.usa = {
                  name:'USA',
                  description:'United States of America',
                  states:50
                };
        
                dup1.diana = {};
                //dup1.donald = {};
                dup1.arthur = {};
        
                dup1.diana.son = dup1.arthur;
                //dup1.diana.country = dup1.usa;
                //dup1.donald.son = dup1.arthur;
                //dup1.donald.country = dup1.usa;
                dup1.arthur.mother = dup1.diana;
                //dup1.arthur.father = dup1.donald;
                //dup1.arthur.country = dup1.usa;
                //dup1.arthur.country.wow={};
                //dup1.arthur.country.wow.slick=dup1.arthur.country.wow;
                return jsonrpc.test.echoRawJSON({"field1": dup1},cb)
              },
        test: function(result){ return result.field1.arthur.mother == result.field1.diana 
                   && result.field1.diana.son===result.field1.arthur;}
      },
      //duplicates and circular references
      { code: function(jsonrpc,cb)
              {
                // an example of an object that has a lot of duplicates and circular references both
                var dup2 = {};
        
                dup2.usa = {
                  name:'USA',
                  description:'United States of America',
                  states:50
                };
        
                dup2.diana = {};
                dup2.donald = {};
                dup2.arthur = {};
                dup2.paula = {};
                dup2.larry = {};
        
                dup2.diana.son = dup2.arthur;
                dup2.diana.country = dup2.usa;
                dup2.donald.son = dup2.arthur;
                dup2.donald.country = dup2.usa;
                dup2.arthur.mother = dup2.diana;
                dup2.arthur.father = dup2.donald;
                dup2.arthur.country = dup2.usa;
                dup2.arthur.spouse = dup2.paula;
                dup2.paula.spouse = dup2.arthur;
                dup2.donald.spouse=dup2.diana;
                dup2.diana.spouse=dup2.donald;
                dup2.larry.friends=[dup2.arthur,dup2.paula,dup2.diana,dup2.donald,dup2.larry];
                dup2.paula.friends=[dup2.arthur,dup2.larry,dup2.paula];
        
                return jsonrpc.test.echoRawJSON(dup2,cb)
              },
        test: function(result){ return result.arthur.mother===result.diana 
                   && result.diana.son===result.arthur;}
      },
      // Array of duplicate Beans
      { code: function(jsonrpc,cb){return jsonrpc.test.aBeanArrayDup(cb)},
        test: function(result){ return result[0]===result[1] && result[1]===result[2];}
      }
    ]
  },
  
  "Duplicates":
  {
     tests:
     [
      // List of duplicate Strings
      { code: function(jsonrpc,cb){return jsonrpc.test.aStringListDup(cb)},
        test: function(result){ return result.list[0]===result.list[1] 
                   && result.list[1]===result.list[2];}
      },
      // Array of duplicate Strings
      { code: function(jsonrpc,cb){return jsonrpc.test.aStringArrayDup(cb)},
        test: function(result){ return result[0]===result[1] && result[1]===result[2]},
      },
      // the following 3 tests don't really have a good way to test if they pass/fail (need server side support)
      // but they are in here in the hopes that this server side support will be added soon

      //duplicates from the server
      { code: function(jsonrpc,cb){return jsonrpc.test.aDupDup(cb)},
        test: function(result){ return true;},
      },
      //more duplicates from the server
      { code: function(jsonrpc,cb){return jsonrpc.test.aDupDupDup(cb)},
        test: function(result){ return true;},
      },
      //a list with some nulls in it
      { code: function(jsonrpc,cb){return jsonrpc.test.listNull(cb)},
        test: function(result){ return true;}
      }
     ]
  },

  "Callable References":
  {
    tests:
    [
      { code: function(jsonrpc,cb){return jsonrpc.test.getCallableRefVector(cb)},
        functionTest: [[["list",0,"ping"],"ping pong"],
                       [["list",1,"ping"],"ping pong"]]
      },
//      { code:             'function(){ var callableRef = jsonrpc.test.getCallableRef(); return ({ oid:callableRef.objectID, ping:callableRef.ping(), refoid:callableRef.getRef().objectID, inside:callableRef.whatsInside(callableRef.getRef()) })}()',
//        asyncCode: 'var __=function(){ var callableRef = jsonrpc.test.getCallableRef(); cb     ({ oid:callableRef.objectID, ping:callableRef.ping(), refoid:callableRef.getRef().objectID, inside:callableRef.whatsInside(callableRef.getRef()) })}()',
//        test: 'result.ping == "ping pong" && result.inside =="a secret"'
//      },
//      { code:             'function(){ var callableRef = jsonrpc.test.getCallableRef(); var p = callableRef.ping();return({ping:p})}();',
//        asyncCode: 'var __=function(){ var callableRef = jsonrpc.test.getCallableRef(); var p = callableRef.ping();cb    ({ping:p});}();',
//        test: 'result.ping == "ping pong"'
//      },
      { code: function(jsonrpc,cb){return jsonrpc.test.getCallableRefInnerVector(cb)},
        functionTest: [[["list",0,"list",0,"ping"],"ping pong"], 
                       [["list",0,"list",1,"ping"],"ping pong"]]
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.getCallableRefMap(cb)},
        functionTest: [[["map","a","ping"],"ping pong"], 
                       [["map","b","ping"],"ping pong"]]
      }
//      ,
//      This is too hard to test since we don't know what the keys will be  
//      { code: function(jsonrpc,cb){return jsonrpc.test.getCallableRefSet(cb)},
//        functionTest: [[["set",0,"ping"],"ping pong"]]
//      }
    ]
  },
  
  "Primitives":
  {  
    tests:
    [
      { code: function(jsonrpc,cb){return jsonrpc.test.voidFunction(cb)},
        test: function(result){ return result == undefined;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoChar("c",cb)},
        test: function(result){ return result == "c";}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echo("",cb)},
        test: function(result){ return result == "";}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echo(123,cb)},
        test: function(result){ return result == 123;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echo("a string",cb)},
        test: function(result){ return result == "a string";}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoIntegerObject(1234567890,cb)},
        test: function(result){ return result == 1234567890;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoOverloadedObject(1234567890,cb)},
        test: function(result){ return result == "number method";}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoOverloadedObject(true,cb)},
        test: function(result){ return result == "boolean method";}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoLongObject(1099511627776,cb)},
        test: function(result){ return result == 1099511627776;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoFloatObject(3.3,cb)},
        test: function(result){ return result == 3.3;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoDoubleObject(9.9,cb)},
        test: function(result){ return result == 9.9;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoBoolean(true,cb)},
        test: function(result){ return result == true;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoBoolean(false,cb)},
        test: function(result){ return result == false;}
      }
    ]
  },
  
  "Objects":
  {  
    tests:
    [
      { code: function(jsonrpc,cb){return jsonrpc.test.concat("a","b",cb)},
        test: function(result){ return result == "a and b";}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoObject(
                   { "javaClass": "org.jabsorb.test.ITest$Waggle", 
                     "bang": "foo", 
                     "baz": 9, 
                     "bork": 5 
                   },cb)},
        test: function(result){ return result.javaClass == "org.jabsorb.test.ITest$Waggle" 
                   && result.bang =="foo" && result.baz == 9 && result.bork == 5;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoObject([1,"string"],cb)},
        test: function(result){ return result[0]== 1 && result[1]==="string";}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoRawJSON({ a: false},cb)},
        test: function(result){ return result.a === false;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoRawJSON({ a: ""},cb)},
        test: function(result){ return result.a === "";}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoRawJSON({ a: 0},cb)},
        test: function(result){ return result.a === 0;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoObjectArray([
                   { "javaClass": "org.jabsorb.test.ITest$Waggle", 
                     "bang": "foo", 
                     "baz": 9, 
                     "bork": 5 
                   }],cb)},
        test: function(result){ return result[0].javaClass == "org.jabsorb.test.ITest$Waggle" 
                   && result[0].bang =="foo" && result[0].baz == 9 
                   && result[0].bork == 5;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoObject([
                   { "javaClass": "org.jabsorb.test.ITest$Waggle", 
                     "bang": "foo", 
                     "baz": 9, 
                     "bork": 5 }],cb)},
        test: function(result){ return result[0].javaClass == "org.jabsorb.test.ITest$Waggle" 
                   && result[0].bang =="foo" && result[0].baz == 9 
                   && result[0].bork == 5;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoRawJSON({ "field1": "test" },cb)},
        test: function(result){ return result.field1 == "test";}
      }
    ]
  },
  
  "Constructors":
  {  
    tests:
    [
      { code: function(jsonrpc,cb){return jsonrpc.createObject("ConstructorTest",[],cb)},
        test: function(result){ return result.javaClass == "org.jabsorb.test.ConstructorTest";},
        functionTest: [[["getMessage"],"default"]]
      },
      { code: function(jsonrpc,cb){return jsonrpc.createObject("ConstructorTest",[1],cb)},
        test: function(result){ return result.javaClass == "org.jabsorb.test.ConstructorTest";},
        functionTest: [[["getMessage"],"int"]]
      },
      { code: function(jsonrpc,cb){return jsonrpc.createObject("ConstructorTest",[-1],cb)},
        test: function(result){ return result.javaClass == "org.jabsorb.test.ConstructorTest";},
        functionTest: [[["getMessage"],"int"]]
      },
      { code: function(jsonrpc,cb){return jsonrpc.createObject("ConstructorTest",[5000000000],cb)},
        test: function(result){ return result.javaClass == "org.jabsorb.test.ConstructorTest";},
        functionTest: [[["getMessage"],"long"]]
      },
      { code: function(jsonrpc,cb){return jsonrpc.createObject("ConstructorTest",[-5000000000],cb)},
        test: function(result){ return result.javaClass == "org.jabsorb.test.ConstructorTest";},
        functionTest: [[["getMessage"],"long"]]
      },
      { code: function(jsonrpc,cb){return jsonrpc.createObject("ConstructorTest",[3.4E37],cb)},
        test: function(result){ return result.javaClass == "org.jabsorb.test.ConstructorTest";},
        functionTest: [[["getMessage"],"float"]]
      },      
      { code: function(jsonrpc,cb){return jsonrpc.createObject("ConstructorTest",[-3.4E37],cb)},
        test: function(result){ return result.javaClass == "org.jabsorb.test.ConstructorTest";},
        functionTest: [[["getMessage"],"float"]]
      },      
      { code: function(jsonrpc,cb){return jsonrpc.createObject("ConstructorTest",[3.4E39],cb)},
        test: function(result){ return result.javaClass == "org.jabsorb.test.ConstructorTest";},
        functionTest: [[["getMessage"],"double"]]
      },      
      { code: function(jsonrpc,cb){return jsonrpc.createObject("ConstructorTest",[-3.4E39],cb)},
        test: function(result){ return result.javaClass == "org.jabsorb.test.ConstructorTest";},
        functionTest: [[["getMessage"],"double"]]
      },      
      { code: function(jsonrpc,cb){return jsonrpc.createObject("ConstructorTest",[true],cb)},
        test: function(result){ return result.javaClass == "org.jabsorb.test.ConstructorTest";},
        functionTest: [[["getMessage"],"boolean"]]
      },      
      { code: function(jsonrpc,cb){return jsonrpc.createObject("ConstructorTest",[false],cb)},
        test: function(result){ return result.javaClass == "org.jabsorb.test.ConstructorTest";},
        functionTest: [[["getMessage"],"boolean"]]
      },      
      { code: function(jsonrpc,cb){return jsonrpc.createObject("ConstructorTest",["hello world"],cb)},
        test: function(result){ return result.javaClass == "org.jabsorb.test.ConstructorTest";},
        functionTest: [[["getMessage"],"String"]]
      },
      { code: function(jsonrpc,cb){return jsonrpc.createObject("ConstructorTest",[321,"hello world"],cb)},
        test: function(result){ return result.javaClass == "org.jabsorb.test.ConstructorTest";},
        functionTest: [[["getMessage"],"int,String"]]
      },
      { code: function(jsonrpc,cb){return jsonrpc.createObject("ConstructorTest",[321,321],cb)},
        test: function(result){ return result.javaClass == "org.jabsorb.test.ConstructorTest";},
        functionTest: [[["getMessage"],"int,int"]]
      }
    ]
  },
  
  "Dates":
  {  
    tests:
    [
      { code: function(jsonrpc,cb){return jsonrpc.test.echoDateObject(new Date(1121689294000),cb)},
        test: function(result)
              {
                if(jsonrpc.transformDates)
                {
                  return !(
                      (result<new Date(1121689294000))
                    ||(result>new Date(1121689294000)))
                }
                else
                {
                     return result.javaClass == "java.util.Date" 
                  && result.time == 1121689294000;
                }
              }
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoSQLDateObject({javaClass:"java.sql.Date",time:1121689294001},cb)},
        test: function(result){ return result.javaClass == "java.sql.Date" 
                   && result.time == 1121689294001;}
      }
    ]
  },
  
  "Lists":
  {  
    tests:
    [
      { code: function(jsonrpc,cb){return jsonrpc.test.echo([1,2,3],cb)},
        test: function(result){ return result.length == 3 && result[0] == 1 
                   && result[1] == 2 && result[2] == 3;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echo(["foo", "bar", "baz"],cb)},
        test: function(result){ return result.length == 3 && result[0] == "foo" 
                   && result[1] == "bar" && result[2] == "baz";}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echo(["foo", null, "baz"],cb)},
        test: function(result){ return result.length == 3 && result[0] == "foo" 
                   && result[1] == null && result[2] == "baz";}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoList(
                   { "list":[20, 21, 22, 23, 24, 25, 26, 27, 28, 29], 
                     "javaClass":"java.util.Vector"},cb)},
        test: function(result){ return result.list.constructor == Array;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoList(
                   { "list":[null, null, null], 
                     "javaClass":"java.util.Vector"},cb)},
        test: function(result){ return result.list.constructor == Array;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoIntegerArray([1234, 5678],cb)},
        test: function(result){ return result[0] == 1234 && result[1] == 5678;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoByteArray("testing 123",cb)},
        test: function(result){ return result == "testing 123";}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoCharArray("testing 456",cb)},
        test: function(result){ return result == "testing 456";}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoBooleanArray([true, false, true],cb)},
        test: function(result){ return result.length == 3 && result[0] == true 
                   && result[1] == false && result[2] == true;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.anArray(cb)},
        test: function(result){ return result.constructor == Array;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.anArrayList(cb)},
        test: function(result){ return result.list.constructor == Array;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.aVector(cb)},
        test: function(result){ return result.list.constructor == Array;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.aList(cb)},
        test: function(result){ return result.list.constructor == Array;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoObject([1,2],cb)},
        test: function(result){ return result.constructor == Array && result[0]==1 
                    && result[1]==2;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoObject(["a","b"],cb)},
        test: function(result){ return result.constructor == Array && result[0]=="a" 
                   && result[1]=="b";}
      }
        
      
    ]
  },
  
  "Sets":
  {  
    tests:
    [
      { code: function(jsonrpc,cb){return jsonrpc.test.aSet(cb)},
        test: function(result){ return result.set.constructor == Object;}
      }
    ]
  },
  
  "Enums":
  {  
    tests:
    [
      { code: function(jsonrpc,cb){return jsonrpc.test.anEnum(cb)},
        test: function(result){ return result == "CLUBS";}
      }
    ]
  },
  
  "Maps":
  {  
    tests:
    [
      { code: function(jsonrpc,cb){return jsonrpc.test.aHashtable(cb)},
        test: function(result){ return result.map.constructor == Object;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echo({ bang: "foo", baz: 9 },cb)},
        test: function(result){ return result.javaClass == "org.jabsorb.test.ITest$Waggle" 
          && result.bang =="foo" && result.baz == 9;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echo({ bang: "foo", baz: 9, bork: 5 },cb)},
        test: function(result){ return result.javaClass == "org.jabsorb.test.ITest$Waggle" 
          && result.bang =="foo" && result.baz == 9;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echo({ bang: "foo", baz: 9, bork: null },cb)},
        test: function(result){ return result.javaClass == "org.jabsorb.test.ITest$Waggle" 
          && result.bang =="foo" && result.baz == 9;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echo({ foo: "bang", bar: 11 },cb)},
        test: function(result){ return result.javaClass == "org.jabsorb.test.ITest$Wiggle" 
          && result.foo =="bang" && result.bar == 11;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.trueBooleansInMap(
                   { javaClass:"java.util.HashMap", 
                     map: {"yo": false, 
                           "ho": true, 
                           "bottle": false, 
                           "rum":true}},cb)},
        test: function(result){ return result === 2;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.nullKeyedMap(cb)},
        test: function(result){ return result.map.normalKey=="normal value"
          &&result.map.null=="Null value";}
      }
    ]
  },
  "Exceptions":
  {  
    tests:
    [
      { code: function(jsonrpc,cb){return jsonrpc.test.throwException(cb)},
        test: function(result,e){ return e.code == 490 && e.msg == "test exception";},
        exception: true
      }
    ]
  },
  "Special Characters":
  {  
    tests:
    [
      { code: function(jsonrpc,cb){return jsonrpc.test.echo("hello",cb);},
        test: function(result){ return result == "hello";}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echo('\\""',cb)},
        test: function(result){ return result == '\\""';}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echo("\\\\",cb)},
        test: function(result){ return result == "\\\\";}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echo('\\b',cb)},
        test: function(result){ return result == "\\b";}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echo("\\t",cb)},
        test: function(result){ return result == "\\t";}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echo("\\n",cb)},
        test: function(result){ return result == "\\n";}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echo("\\f",cb)},
        test: function(result){ return result == "\\f";}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echo("\\r",cb)},
        test: function(result){ return result == "\\r";}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echo(1234,cb)},
        test: function(result){ return result == 1234;}
      },
      { code: function(jsonrpc,cb){return jsonrpc.test.echoRawJSON(
                   { "field1": "azAZ019!@#$^&*()_+-=\\|;,.<>/`~{}[]%" },cb)},
        test: function(result){ return result.field1 == "azAZ019!@#$^&*()_+-=\\|;,.<>/`~{}[]%";}
      }
    ]
  }
}