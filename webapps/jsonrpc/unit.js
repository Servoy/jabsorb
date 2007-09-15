var jsonurl = "JSON-RPC";
var jsonrpc = null;

var cb;
var tests_start;

// the simplest circular reference case
var circRef1 = {};
circRef1.circRef1 = circRef1;

// another simple circular reference
var a = {};
var b = {};
a.b=b;
b.a=a;

// a circular reference in an array
var circ3 = {};
circ3.arr=[3,1,4,1,circ3,9,2,6,5,3,5,8,9,7,9];

var circ3ArrLen = circ3.arr.length;

// an example of an object that has a lot of duplicates
var dup1 = {};

dup1.usa = {
  name:'USA',
  description:'United States of America',
  states:50
};

dup1.diana = {};
dup1.donald = {};
dup1.arthur = {};

dup1.diana.son = dup1.arthur;
dup1.diana.country = dup1.usa;
dup1.donald.son = dup1.arthur;
dup1.donald.country = dup1.usa;
dup1.arthur.mother = dup1.diana;
dup1.arthur.father = dup1.donald;
dup1.arthur.country = dup1.usa;

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

dup2.diana.son = dup1.arthur;
dup2.diana.country = dup1.usa;
dup2.donald.son = dup1.arthur;
dup2.donald.country = dup1.usa;
dup2.arthur.mother = dup1.diana;
dup2.arthur.father = dup1.donald;
dup2.arthur.country = dup1.usa;
dup2.arthur.spouse = dup2.paula;
dup2.paula.spouse = dup2.arthur;
dup2.donald.spouse=dup2.diana;
dup2.diana.spouse=dup2.donald;
dup2.larry.friends=[dup2.arthur,dup2.paula,dup2.diana,dup2.donald,dup2.larry];
dup2.paula.friends=[dup2.arthur,dup2.larry,dup2.paula];


var tests = [

// circular reference from server
{ code: 'jsonrpc.test.aBean()',
  test: 'result != null'
},

//server circ ref test of a Map  
{ code: 'jsonrpc.test.aCircRefMap()',
  test: 'result.map.me===result'},

// circular references 1
{ code: 'jsonrpc.test.echoRawJSON({"field1": circRef1})',
  test: 'result.field1.circRef1 === result.field1'},

// circular references 2
{ code: 'jsonrpc.test.echoRawJSON({"field1": a})',
  test: 'result.field1.a.b.a===result.field1.a'},

// circular references 3
{ code: 'jsonrpc.test.echoRawJSON({"field1": circ3})',
  test: 'result.field1.arr[4]===result.field1 && circ3ArrLen === result.field1.arr.length'},

//duplicates test
{ code: 'jsonrpc.test.echoRawJSON({"field1": dup1})',
test: 'result.field1.arthur.mother==result.field1.diana&&result.field1.diana.son===result.field1.arthur'},

//duplicates and circular references
{ code: 'jsonrpc.test.echoRawJSON({"field1": dup2})',
test: 'result.field1.arthur.mother==result.field1.diana&&result.field1.diana.son===result.field1.arthur'},

{ code: 'jsonrpc.test.echoRawJSON({ "field1": "+plus+and%percent%" })',
  test: 'result.field1 == "+plus+and%percent%"'},

{ code: 'jsonrpc.test.echoRawJSON({ "field1": "+plus+and%percent%" })',
  test: 'result.field1 == "+plus+and%percent%"'},

{ code: 'jsonrpc.test.echoRawJSON({ "field1": "+" })',
  test: 'result.field1 == "+"'},

{ code: 'jsonrpc.test.echoRawJSON({ "field1": "%" })',
  test: 'result.field1 == "%"'},

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
{ code: 'jsonrpc.test.echo([1,2,3])',
  test: 'result.length == 3 && result[0] == 1 && result[1] == 2 && result[2] == 3'
},
{ code: 'jsonrpc.test.echo(["foo", "bar", "baz"])',
  test: 'result.length == 3 && result[0] == "foo" && result[1] == "bar" && result[2] == "baz"'
},
{ code: 'jsonrpc.test.echo(["foo", null, "baz"])',
  test: 'result.length == 3 && result[0] == "foo" && result[1] == null && result[2] == "baz"'
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
},
{ code: 'jsonrpc.test.echoChar("c")',
  test: 'result == "c"'
},
{ code: 'jsonrpc.test.echoIntegerArray([1234, 5678])',
  test: 'result[0] == 1234 && result[1] == 5678'
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
{ code: 'jsonrpc.test.echoDateObject(new Date(1121689294000))',
  test: 'result.javaClass == "java.util.Date" && result.time == 1121689294000'
},
{ code: 'jsonrpc.test.echoBoolean(true)',
  test: 'result == true'
},
{ code: 'jsonrpc.test.echoBoolean(false)',
  test: 'result == false'
},
{ code: 'jsonrpc.test.echoByteArray("testing 123")',
  test: 'result == "testing 123"'
},
{ code: 'jsonrpc.test.echoCharArray("testing 456")',
  test: 'result == "testing 456"'
},
{ code: 'jsonrpc.test.echoBooleanArray([true,false,true])',
  test: 'result.length == 3 && result[0] == true && result[1] == false && result[2] == true'
},
{ code: 'jsonrpc.test.echoList({"list":[20, 21, 22, 23, 24, 25, 26, 27, 28, 29], "javaClass":"java.util.Vector"})',
  test: 'result.list.constructor == Array'
},
{ code: 'jsonrpc.test.echoList({"list":[null, null, null], "javaClass":"java.util.Vector"})',
  test: 'result.list.constructor == Array'
},
{ code: 'jsonrpc.test.concat("a","b")',
  test: 'result == "a and b"'
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
},
{ code: 'jsonrpc.test.aSet()',
  test: 'result.set.constructor == Object'
},
{ code: 'jsonrpc.test.aHashtable()',
  test: 'result.map.constructor == Object'
},
{ code: 'jsonrpc.test.echoObject({ "javaClass": "org.jabsorb.test.Test$Waggle", "bang": "foo", "baz": 9, "bork": 5 })',
  test: 'result.javaClass == "org.jabsorb.test.Test$Waggle" && result.bang =="foo" && result.baz == 9 && result.bork == 5'
},
{ code: 'jsonrpc.test.echoObjectArray([{ "javaClass": "org.jabsorb.test.Test$Waggle", "bang": "foo", "baz": 9, "bork": 5 }])',
  test: 'result[0].javaClass == "org.jabsorb.test.Test$Waggle" && result[0].bang =="foo" && result[0].baz == 9 && result[0].bork == 5'
},
{ code: 'jsonrpc.test.echoRawJSON({ "field1": "test" })',
  test: 'result.field1 == "test"'
}
  ];

// some variables to hold stuff
var tbody,asyncNode,profileNode,maxRequestNode;

function onLoad()
{
  tbody = document.getElementById("tests");
  asyncNode = document.getElementById("async");
  profileNode = document.getElementById("profile");
  maxRequestNode = document.getElementById("max_requests");

  jsonrpc = new JSONRpcClient(jsonurl);

  displayTests();
  clearAllResults();
}

function displayTests()
{
  var i,
      row,
      ccell,
      rcell,
      ecell,
      pcell;

  for (i = 0; i < tests.length; i++)
  {
    row = document.createElement("tr");

    row.className = "tr" + i%2;
    ccell = document.createElement("td");
    rcell = document.createElement("td");
    ecell = document.createElement("td");
    pcell = document.createElement("td");

    ccell.innerHTML = "<div class=\"code_cell\"><a href=\"#\" onclick=\"runTest(" +
                      i + ")\">" + tests[i].code + "</a></div>";

    ccell.className = "test_td";
    rcell.id = "result." + i;
    rcell.className = "test_td";
    ecell.id = "expected." + i;
    ecell.className = "test_td";
    pcell.id = "pass." + i;
    pcell.className = "test_td";

    row.appendChild(ccell);
    row.appendChild(rcell);
    row.appendChild(ecell);
    row.appendChild(pcell);
    tbody.appendChild(row);
  }
}

function clearResult(i)
{
  var resultsNode = document.getElementById("result." + i);
  var expectedNode = document.getElementById("expected." + i);
  var passNode = document.getElementById("pass." + i);
  resultsNode.innerHTML = "<div class=\"result_cell\"></div>";
  expectedNode.innerHTML = "<div class=\"result_cell\"></div>";
  passNode.innerHTML = "<div class=\"pass_cell\"></div>";
}

function clearAllResults()
{
  for (var i = 0; i < tests.length; i++)
  {
    clearResult(i);
  }
}

function postResults(i, result, e, profile)
{
  var resultsNode = document.getElementById("result." + i);
  var expectedNode = document.getElementById("expected." + i);
  var passNode = document.getElementById("pass." + i);
  var resultText;
  var pass = false;
  if (e)
  {
    if (e.message)
    {
      resultText = e.message;
    }
    else
    {
      resultText = e.toString();
    }
    if (tests[i].exception)
    {
      try
      {
        eval("pass = " + tests[i].test);
      }
      catch(e)
      {
      }
    }
  }
  else
  {
    if (typeof result == "object")
    {
      resultText = toJSON(result);
    }
    else
    {
      resultText = result;
    }
    try
    {
      eval("pass = " + tests[i].test);
    }
    catch(e)
    {
    }
  }
  if (profile)
  {
    resultsNode.innerHTML = "<div class=\"result_cell\">" +
                            "submit=" + (profile.submit - tests_start) +
                            ", start=" + (profile.start - tests_start) +
                            ", end=" + (profile.end - tests_start) +
                            ", dispatch=" + (profile.dispatch - tests_start) +
                            " (rtt=" + (profile.end - profile.start) + ")</div>";
  }
  else
  {
    resultsNode.innerHTML = "<div class=\"result_cell\">" +
                            resultText + "</div>";
  }
  expectedNode.innerHTML="<div class=\"result_cell\">" +
                            tests[i].test + "</div>";
  if (pass)
  {
    passNode.innerHTML = "<div class=\"pass_cell\">pass</div>";
  }
  else
  {
    passNode.innerHTML = "<div class=\"fail_cell\">FAIL!</pass>";
  }
}

function testAsyncCB(i)
{
  return function (result, e, profile)
  {
    postResults(i, result, e, profile);
  };
}

function runTestAsync(i)
{
  var cb,code,str;
  try
  {
    // insert post results callback into first argument and submit test
    cb = testAsyncCB(i);
    code = tests[i].code;
    code = code.replace(/\(([^\)])/, "(cb, $1");
    code = code.replace(/\(\)/, "(cb)");

    // run the test
    eval(code);
  }
  catch (e)
  {
    str="";
    for (var a in e)
    {
      str+=a+" "+e[a]+"\n";
    }
    alert(str);
  }
}

function runTestSync(i)
{
  var result;
  var exception;
  var profile;
  if (profileNode.checked)
  {
    profile = {};
    profile.submit = profile.start = new Date();
  }
  try
  {
    eval("result = " + tests[i].code);
  }
  catch (e)
  {
    exception = e;
  }
  if (profileNode.checked)
  {
    profile.end = profile.dispatch = new Date();
  }
  postResults(i, result, exception, profile);
}

function runTest(i)
{
  clearResult(i);
  if (profileNode.checked)
  {
    tests_start = new Date();
  }
  if (asyncNode.checked)
  {
    JSONRpcClient.profile_async = profileNode.checked;
    cb = [];
    runTestAsync(i);
  }
  else
  {
    runTestSync(i);
  }
}

function runAllTests()
{
  var i;
  if (maxRequestNode.value < 1 || maxRequestNode.value > 99)
  {
    alert("Max requests should be between 1 and 99");
    return;
  }
  JSONRpcClient.max_req_active = maxRequestNode.value;

  clearAllResults();
  if (profileNode.checked)
  {
    tests_start = new Date();
  }
  if (asyncNode.checked)
  {
    JSONRpcClient.profile_async = profileNode.checked;
    cb = [];
    for (i = 0; i < tests.length; i++)
    {
      runTestAsync(i);
    }
  }
  else
  {
    for (i = 0; i < tests.length; i++)
    {
      runTestSync(i);
    }
  }
}
