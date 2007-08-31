jsonurl = "/jsonrpc/JSON-RPC";
jsonrpc = null;

var tests = [
	{ 'code': 'jsonrpc.test.voidFunction()',
	  'test': 'result == undefined'
	},
	{ 'code': 'jsonrpc.test.throwException()',
	  'test': 'e == "java.lang.Exception: test exception"',
	  'exception': true
	},
	{ 'code': 'jsonrpc.test.echo("hello")',
	  'test': 'result == "hello"'
	},
	{ 'code': 'jsonrpc.test.echo("quote \\" backslash \\\\ ctrl \\t\\n")',
	  'test': 'result == "quote \\" backslash \\\\ ctrl \\t\\n"'
	},
	{ 'code': 'jsonrpc.test.echo(1234)',
	  'test': 'result == 1234'
	},
	{ 'code': 'jsonrpc.test.echo([1,2,3])',
	  'test': 'result.length == 3 && result[0] == 1 && result[1] == 2 && result[2] == 3'
	},
	{ 'code': 'jsonrpc.test.echo(["foo","bar","baz"])',
	  'test': 'result.length == 3 && result[0] == "foo" && result[1] == "bar" && result[2] == "baz"'
	},
	{ 'code': 'jsonrpc.test.echo({ bang: "foo", baz: 9 })',
	  'test': 'result.javaClass == "com.metaparadigm.jsonrpc.test.Test$Waggle" && result.bang =="foo" && result.baz == 9'
	},
	{ 'code': 'jsonrpc.test.echo({ foo: "bang", bar: 11 })',
	  'test': 'result.javaClass == "com.metaparadigm.jsonrpc.test.Test$Wiggle" && result.foo =="bang" && result.bar == 11'
	},
	{ 'code': 'jsonrpc.test.echoChar("c")',
	  'test': 'result == "c"'
	},
	{ 'code': 'jsonrpc.test.echoIntegerObject(1234567890)',
	  'test': 'result == 1234567890'
	},
	{ 'code': 'jsonrpc.test.echoLongObject(1099511627776)',
	  'test': 'result == 1099511627776'
	},
	{ 'code': 'jsonrpc.test.echoFloatObject(3.3)',
	  'test': 'result == 3.3'
	},
	{ 'code': 'jsonrpc.test.echoDoubleObject(9.9)',
	  'test': 'result == 9.9'
	},
	{ 'code': 'jsonrpc.test.echoBoolean(true)',
	  'test': 'result == true'
	},
	{ 'code': 'jsonrpc.test.echoBoolean(false)',
	  'test': 'result == false'
	},
	{ 'code': 'jsonrpc.test.echoByteArray("testing 123")',
	  'test': 'result == "testing 123"'
	},
	{ 'code': 'jsonrpc.test.echoCharArray("testing 456")',
	  'test': 'result == "testing 456"'
	},
	{ 'code': 'jsonrpc.test.echoBooleanArray([true,false,true])',
	  'test': 'result.length == 3 && result[0] == true && result[1] == false && result[2] == true'
	},
	{ 'code': 'jsonrpc.test.echoList({"list":[20, 21, 22, 23, 24, 25, 26, 27, 28, 29], "javaClass":"java.util.Vector"})',
	  'test': 'result.list.constructor == Array'
	},
	{ 'code': 'jsonrpc.test.concat("a","b")',
	  'test': 'result == "a and b"'
	},
	{ 'code': 'jsonrpc.test.anArray()',
	  'test': 'result.constructor == Array'
	},
	{ 'code': 'jsonrpc.test.anArrayList()',
	  'test': 'result.list.constructor == Array'
	},
	{ 'code': 'jsonrpc.test.aVector()',
	  'test': 'result.list.constructor == Array'
	},
	{ 'code': 'jsonrpc.test.aList()',
	  'test': 'result.list.constructor == Array'
	},
	{ 'code': 'jsonrpc.test.aSet()',
	  'test': 'result.set.constructor == Object'
	},
	{ 'code': 'jsonrpc.test.aBean()',
	  'test': 'result.javaClass == "com.metaparadigm.jsonrpc.test.BeanA"'
	},
	{ 'code': 'jsonrpc.test.aHashtable()',
	  'test': 'result.map.constructor == Object'
	}
];

function onLoad()
{
    testTableBody = document.getElementById("tests");
    asyncNode = document.getElementById("async");
    profileNode = document.getElementById("profile");
    maxRequestNode = document.getElementById("max_requests");

    try {
	jsonrpc = new JSONRpcClient(jsonurl);
    } catch(e) {
	if(e.message) alert(e.message);
	else alert(e);
    }
    displayTests();
    clearResults();
}

function displayTests()
{
    for(var i = 0; i < tests.length; i++) {
	var row = testTableBody.insertRow(testTableBody.rows.length);
	var ccell = row.insertCell(row.cells.length);
	var rcell = row.insertCell(row.cells.length);
	var pcell = row.insertCell(row.cells.length);
	ccell.innerHTML = "<div class=\"code_cell\">" +
	    tests[i].code + "</div>";
	ccell.className = "test_td";
	rcell.className = "test_td";
	pcell.className = "test_td";
	rcell.id = "result." + i;
	pcell.id = "pass." + i;
    }
}

function clearResults()
{
    for(var i = 0; i < tests.length; i++) {
	var resultsNode = document.getElementById("result." + i);
	var passNode = document.getElementById("pass." + i);
	resultsNode.innerHTML = "<div class=\"result_cell\"></div>";
	passNode.innerHTML = "<div class=\"pass_cell\"></div>";
    }
}

function postResults(i, result, e, profile)
{
    var resultsNode = document.getElementById("result." + i);
    var passNode = document.getElementById("pass." + i);
    var resultText;
    var pass = false;
    if(e) {
	if(e.mesage) resultText = e.message;
	else resultText = e.toString();
	if(tests[i].exception) {
	    try {
		eval("pass = " + tests[i].test);
	    } catch(e) {}
	}
    } else {
	if(typeof result == "object") resultText = toJSON(result);
	else resultText = result;
	try {
	    eval("pass = " + tests[i].test);
	} catch(e) {}
    }
    if(profile) {
	resultsNode.innerHTML = "<div class=\"result_cell\">" +
	    "submit=" + (profile.submit - tests_start) +
	    ", start=" + (profile.start - tests_start) +
	    ", end=" + (profile.end - tests_start) +
	    ", dispatch=" + (profile.dispatch - tests_start) +
	    " (rtt=" + (profile.end - profile.start) + ")</div>";
    } else {
	resultsNode.innerHTML = "<div class=\"result_cell\">" +
	    resultText + "</div>";
    }
    if(pass) passNode.innerHTML = "<div class=\"pass_cell\">pass</div>";
    else passNode.innerHTML = "<div class=\"fail_cell\">fail</pass>";
}

function runTestAsync(i)
{
    var result;
    var exception;
    try {
	// create a callback
	var cb = "var cb" + i +" = function(result, e, profile) " +
	    "{ postResults(" + i + ", result, e, profile); }";
	// insert callback into first argument
	var code = tests[i].code;
	code = code.replace(/\(([^\)])/, "(cb" + i + ", $1");
	code = code.replace(/\(\)/, "(cb" + i + ")");
	eval(cb);
	eval(code);
    } catch (e) {
	postResults(i, null, e);
    }
}

function runTestSync(i)
{
    var result;
    var exception;
    var profile;
    if(profileNode.checked) {
	profile = {};
	profile.submit = profile.start = new Date();
    }
    try {
	eval("result = " + tests[i].code);
    } catch (e) { exception = e; }
    if(profileNode.checked) {
	profile.end = profile.dispatch = new Date();
    }
    postResults(i, result, exception, profile);
}

function runTests()
{
    var n = maxRequestNode.value
    if(maxRequestNode.value < 1 || maxRequestNode.value > 99) {
	alert("Max requests should be between 1 and 99");
	return;
    }
    JSONRpcClient.max_req_active = maxRequestNode.value;

    clearResults();
    if(profileNode.checked) tests_start = new Date();
    if(asyncNode.checked) {
	JSONRpcClient.profile_async = profileNode.checked;
	for(var i = 0; i < tests.length; i++) runTestAsync(i);
    } else {
	for(var i = 0; i < tests.length; i++) runTestSync(i);
    }
}
