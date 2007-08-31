var jsonurl = "/jsonrpc/JSON-RPC";
var jsonserver = null;

var evalStr;
var txRslt;

onLoad = function()
{
    evalStr = document.getElementById("txEval").value;     
    txRslt = document.getElementById("txResult");

    try {
	jsonserver = new JSONRpcClient(jsonurl);
    } catch(e) {
	alert(e);
    }
}

doEval = function()
{
    txRslt.value = "Evaluating " + evalStr + "\n\n";

    try {

	var rslt = eval(evalStr);
	txRslt.value += "" + rslt;

    } catch(e) {
	txRslt.value = "Error trace: \n\n" + e;
    }
    return false;
}

doListMethods = function()
{
    txRslt.value = "Calling system.listMethods()\n\n";

    try {

	var rslt = jsonserver.system.listMethods();
	for(var i=0; i < rslt.length; i++) {
	    txRslt.value += rslt[i] + "\n";
	}

    } catch(e) {
	txRslt.value = "Error trace: \n\n" + e;
    }
    return false;
}

doBasicTests = function()
{
    txRslt.value = "Running tests\n\n";

    var rslt;

    try {

	txRslt.value += "Calling test.voidFunction()";
	rslt = jsonserver.test.voidFunction();
	txRslt.value += " returns " + rslt + "\n";

	txRslt.value += "Calling test.anArray()";
	rslt = jsonserver.test.anArray();
	txRslt.value += " returns " + rslt + "\n";

	txRslt.value += "Calling test.anArrayList()";
	rslt = jsonserver.test.anArrayList();
	txRslt.value += " returns " + rslt + "\n";

	txRslt.value += "Calling test.aVector()";
	rslt = jsonserver.test.aVector();
	txRslt.value += " returns " + rslt + "\n";

	txRslt.value += "Calling test.aList()";
	rslt = jsonserver.test.aList();
	txRslt.value += " returns " + rslt + "\n";
	
	txRslt.value += "Calling test.aSet()";
	rslt = jsonserver.test.aSet();
	txRslt.value += " returns " + rslt + "\n";

	txRslt.value += "Calling test.aBean()";
	rslt = jsonserver.test.aBean();
	txRslt.value += " returns " + rslt + "\n";

	txRslt.value += "Calling test.aHashtable()";
	rslt = jsonserver.test.aHashtable();
	txRslt.value += " returns " + rslt + "\n";

	txRslt.value += "Calling test.twice(\"foo\")";
	rslt = jsonserver.test.twice("foo");
	txRslt.value += " returns " + rslt + "\n";

	txRslt.value += "Calling test.echoByteArray(\"test test\")";
	rslt = jsonserver.test.echoByteArray("test test");
	txRslt.value += " returns " + rslt + "\n";

	txRslt.value += "Calling test.echoCharArray(\"test again\")";
	rslt = jsonserver.test.echoCharArray("test again");
	txRslt.value += " returns " + rslt + "\n";

	txRslt.value += "Calling test.echoChar(\"c\")";
	rslt = jsonserver.test.echoChar("c");
	txRslt.value += " returns " + rslt + "\n";

	txRslt.value += "Calling test.echoBoolean(true)";
	rslt = jsonserver.test.echoBoolean(true);
	txRslt.value += " returns " + rslt + "\n";

	txRslt.value += "Calling test.echoBoolean(false)";
	rslt = jsonserver.test.echoBoolean(false);
	txRslt.value += " returns " + rslt + "\n";

	txRslt.value += "Calling test.echoBooleanArray([true,false,true])";
	rslt = jsonserver.test.echoBooleanArray([true,false,true]);
	txRslt.value += " returns " + rslt + "\n";

	txRslt.value += "Calling test.echo(\"a string\")";
	rslt = jsonserver.test.echo("a string");
	txRslt.value += " returns " + rslt + "\n";

	txRslt.value += "Calling test.echo(2)";
	rslt = jsonserver.test.echo(2);
	txRslt.value += " returns " + rslt + "\n";

	txRslt.value += "Calling test.echo({bang: 'foo', baz: 9})";
	rslt = jsonserver.test.echo({bang: 'foo', baz: 9});
	txRslt.value += " returns " + rslt + "\n";

	txRslt.value += "Calling test.echo([\"abc\", \"def\"])";
	rslt = jsonserver.test.echo(["abc","def"]);
	txRslt.value += " returns " + rslt + "\n";

	txRslt.value += "Calling test.echo([3,4])";
	rslt = jsonserver.test.echo([3,4]);
	txRslt.value += " returns " + rslt + "\n";

	txRslt.value += "Calling test.waggleToWiggle({bang: 'foo', baz: 9})";
	rslt = jsonserver.test.waggleToWiggle({bang: 'foo', baz: 9});
	txRslt.value += " returns " + rslt + "\n";
	
	txRslt.value += "Calling test.echoList([{\"list\":[20,21,22,23,24,25,26,27,28,29],\"javaClass\":\"java.util.Vector\"}])";
	rslt = jsonserver.test.echoList({"list":[20,21,22,23,24,25,26,27,28,29],"javaClass":"java.util.Vector"});
	txRslt.value += " returns " + rslt + "\n";

    } catch(e) {
	txRslt.value += " caught exception: " + e;
    }
    return false;
}

doReferenceTests = function()
{
    txRslt.value = "Running Reference Tests\n\n";

    var rslt;
    var callableRef;
    var ref;

    try {

	txRslt.value += "var callableRef = test.getCallableRef()\n";
	callableRef = jsonserver.test.getCallableRef();
	txRslt.value += "returns a CallableReference objectID=" +
	    callableRef.objectID + "\n\n";

	txRslt.value += "callableRef.ping()\n";
	rslt = callableRef.ping()
	    txRslt.value += "returns \"" + rslt + "\"\n\n";

	txRslt.value += "var ref = callableRef.getRef()\n";
	ref = callableRef.getRef();
	txRslt.value += "returns Reference objectID=" +
	    ref.objectID + "\n\n";

	txRslt.value += "callableRef.whatsInside(ref)\n";
	rslt = callableRef.whatsInside(ref);
	txRslt.value += "returns \"" + rslt + "\"\n\n";

    } catch(e) {
	txRslt.value += "\n" + e + "\n";
    }
    return false;
}

doContainerTests = function()
{
    txRslt.value = "Running Container tests\n\n";

    try {

	txRslt.value += "wigArrayList = test.aWiggleArrayList(2)\n";
	var wigArrayList = jsonserver.test.aWiggleArrayList(2);
	txRslt.value += "returns " + wigArrayList + "\n\n";

	txRslt.value += "test.aWiggleArrayList(wigArrayList)\n";
	var rslt = jsonserver.test.wigOrWag(wigArrayList);
	txRslt.value += "returns \"" + rslt + "\"\n\n";

    } catch(e) {
	txRslt.value += "\n" + e + "\n";
    }
    return false;
}

doExceptionTest = function()
{
    txRslt.value = "Running Exception test\n\n";

    try {
	txRslt.value = "Calling test.fark()\n\n";
	jsonserver.test.fark();
    } catch(e) {
	txRslt.value += e;
    }
    return false;
}
