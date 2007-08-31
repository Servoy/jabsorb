jsonurl = "/jsonrpc/JSON-RPC";
jsonrpc = null;


function clrscr()
{
    resultNode.value = "";
}

function print(s)
{
    resultNode.value += "" + s;
    resultNode.scrollTop = resultNode.scrollHeight;
}

function onLoad()
{
    resultNode = document.getElementById("result");

    try {
	jsonrpc = new JSONRpcClient(jsonurl);
    } catch(e) {
	if(e.message) alert(e.message);
	else alert(e);
    }
}

function doEval()
{
    clrscr();
    evalStr = document.getElementById("eval").value;
    print("Evaluating " + evalStr + "\n\n");

    try {
	print(eval(evalStr));
    } catch(e) {
	print("Exception: \n\n" + e);
    }
    return false;
}

function doListMethods()
{
    clrscr();
    print("Calling system.listMethods()\n\n");

    try {

	var rslt = jsonrpc.system.listMethods();
	for(var i=0; i < rslt.length; i++) {
	    print(rslt[i] + "\n");
	}

    } catch(e) {
	print("Exception: \n\n" + e);
    }
    return false;
}

function doBasicTests()
{
    clrscr();
    print("Running tests\n\n");

    // Some test data
    var bools = [true,false,true];
    var waggle = { bang: "foo", baz: 9 };
    var wiggle = { foo: "bang", bar: 11 };
    var list = {"list":[20,21,22,23,24,25,26,27,28,29],
		"javaClass":"java.util.Vector"};

    try {

	print("Calling test.voidFunction()");
	print(" returns " + jsonrpc.test.voidFunction() + "\n");

	print("Calling test.echo(\"hello\")");
	print(" returns " + jsonrpc.test.echo("hello") + "\n");

	print("Calling test.echo(1234)");
	print(" returns " + jsonrpc.test.echo(1234) + "\n");

	print("Calling test.echo([1,2,3])");
	print(" returns " + jsonrpc.test.echo([1,2,3]) + "\n");

	print("Calling test.echo([\"foo\", \"bar\", \"baz\"])");
	print(" returns " + jsonrpc.test.echo(["foo","bar","baz"]) + "\n");

	print("Calling test.echo(" + toJSON(waggle) + ")");
	print(" returns " + toJSON(jsonrpc.test.echo(waggle)) + "\n");

	print("Calling test.echo(" + toJSON(wiggle) + ")");
	print(" returns " + toJSON(jsonrpc.test.echo(wiggle)) + "\n");

	print("Calling test.echoChar(\"c\")");
	print(" returns " + jsonrpc.test.echoChar("c") + "\n");

	print("Calling test.echoIntegerObject(1234567890)");
	print(" returns " + jsonrpc.test.echoIntegerObject(1234567890) + "\n");

	print("Calling test.echoLongObject(1099511627776)");
	print(" returns " + jsonrpc.test.echoLongObject(1099511627776) + "\n");

	print("Calling test.echoFloatObject(3.3)");
	print(" returns " + jsonrpc.test.echoFloatObject(3.3) + "\n");

	print("Calling test.echoDoubleObject(9.9)");
	print(" returns " + jsonrpc.test.echoDoubleObject(9.9) + "\n");

	print("Calling test.echoBoolean(true)");
	print(" returns " + jsonrpc.test.echoBoolean(true) + "\n");

	print("Calling test.echoBoolean(false)");
	print(" returns " + jsonrpc.test.echoBoolean(false) + "\n");

	print("Calling test.echoByteArray(\"test test\")");
	print(" returns " + jsonrpc.test.echoByteArray("test test") + "\n");

	print("Calling test.echoCharArray(\"test again\")");
	print(" returns " + jsonrpc.test.echoCharArray("test again") + "\n");

	print("Calling test.echoBooleanArray(" + bools + ")");
	print(" returns " + jsonrpc.test.echoBooleanArray(bools) + "\n");

	print("Calling test.echoList(" + toJSON(list) + ")");
	print(" returns " + toJSON(jsonrpc.test.echoList(list)) + "\n");

	print("Calling test.concat(\"a\",\"b\")");
	print(" returns " + jsonrpc.test.concat("a","b") + "\n");

	print("Calling test.anArray()");
	print(" returns " + jsonrpc.test.anArray() + "\n");

	print("Calling test.anArrayList()");
	print(" returns " + toJSON(jsonrpc.test.anArrayList()) + "\n");

	print("Calling test.aVector()");
	print(" returns " + toJSON(jsonrpc.test.aVector()) + "\n");

	print("Calling test.aList()");
	print(" returns " + toJSON(jsonrpc.test.aList()) + "\n");
	
	print("Calling test.aSet()");
	print(" returns " + toJSON(jsonrpc.test.aSet()) + "\n");

	print("Calling test.aBean()");
	print(" returns " + toJSON(jsonrpc.test.aBean()) + "\n");

	print("Calling test.aHashtable()");
	print(" returns " + toJSON(jsonrpc.test.aHashtable()) + "\n");

    } catch(e) {
	print(" Exception: \n\n" + e);
    }
    return false;
}

function doReferenceTests()
{
    clrscr();
    print("Running Reference Tests\n\n");

    var rslt;
    var callableRef;
    var ref;

    try {

	print("var callableRef = test.getCallableRef()\n");
	callableRef = jsonrpc.test.getCallableRef();
	print("returns a CallableReference objectID=" +
	      callableRef.objectID + "\n\n");

	print("callableRef.ping()\n");
	rslt = callableRef.ping()
	    print("returns \"" + rslt + "\"\n\n");

	print("var ref = callableRef.getRef()\n");
	ref = callableRef.getRef();
	print("returns Reference objectID=" +
	      ref.objectID + "\n\n");

	print("callableRef.whatsInside(ref)\n");
	rslt = callableRef.whatsInside(ref);
	print("returns \"" + rslt + "\"\n\n");

    } catch(e) {
	print(" Exception: \n\n" + e);
    }
    return false;
}

function doContainerTests()
{
    clrscr();
    print("Running Container tests\n\n");

    try {

	print("wigArrayList = test.aWiggleArrayList(2)\n");
	var wigArrayList = jsonrpc.test.aWiggleArrayList(2);
	print("returns " + wigArrayList + "\n\n");

	print("test.aWiggleArrayList(wigArrayList)\n");
	var rslt = jsonrpc.test.wigOrWag(wigArrayList);
	print("returns \"" + rslt + "\"\n\n");

    } catch(e) {
	print("Exception: \n\n" + e);
    }
    return false;
}

function doExceptionTest()
{
    clrscr();
    print("Running Exception test\n\n");

    try {
	print("Calling test.throwException()\n\n");
	jsonrpc.test.throwException();
    } catch(e) {
	print("e.toString()=" + e.toString() + "\n");
	print("e.name=" + e.name + "\n");
	print("e.message=" + e.message + "\n");
	print("e.javaStack=" + e.javaStack + "\n");
    }
    return false;
}
