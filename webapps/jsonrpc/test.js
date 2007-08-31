var jsonurl = "/jsonrpc/JSON-RPC";
var jsonrpc = null;
var jsonserver = null;

try {
    var jsonrpc = importModule("jsonrpc");
    try {
        jsonserver = new jsonrpc.ServerProxy(jsonurl);
    } catch(e) {
        reportException(e);
        throw "connection to jsonrpc server failed.";
    }
} catch(e) {
    reportException(e);
    throw "importing of jsonrpc module failed.";
}


doEval = function()
{
    var evalStr = document.getElementById("txEval").value;     
    var txRslt = document.getElementById("txResult");
    txRslt.value = "Evaluating " + evalStr + "\n\n";

    var rslt;

    try {

        rslt = eval(evalStr);
        txRslt.value += "" + rslt;

    } catch(e) {
        var em;
        if(e.toTraceString) {
            em = e.toTraceString();
        }else{
            em = e.message;
        }
        txRslt.value = "Error trace: \n\n" + em;
    }
    return false;
}

doListMethods = function()
{
    var txRslt = document.getElementById("txResult");
    txRslt.value = "Calling system.listMethods()\n\n";

    var rslt;

    try {

        rslt = jsonserver.system.listMethods();
	for(var i=0; i < rslt.length; i++) {
            txRslt.value += rslt[i] + "\n";
        }

    } catch(e) {
        var em;
        if(e.toTraceString) {
            em = e.toTraceString();
        }else{
            em = e.message;
        }
        txRslt.value = "Error trace: \n\n" + em;
    }
    return false;
}

doBasicTests = function()
{
    var txRslt = document.getElementById("txResult");
    txRslt.value = "Running tests\n\n";

    var rslt;

    try {

        txRslt.value += "Calling test.anArray()";
        rslt = jsonserver.test.anArray();
        txRslt.value += " returns " + rslt + "\n";

        txRslt.value += "Calling test.anArrayList()";
        rslt = jsonserver.test.anArrayList();
        txRslt.value += " returns " + rslt + "\n";

        txRslt.value += "Calling test.aVector()";
        rslt = jsonserver.test.aVector();
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

    } catch(e) {
        var em;
        if(e.toTraceString) {
            em = e.toTraceString();
        }else{
            em = e.message;
        }
        txRslt.value = "Error trace: \n\n" + em;
    }
    return false;
}

doReferenceTests = function()
{
    var txRslt = document.getElementById("txResult");
    txRslt.value = "Running Reference Tests\n\n";

    var rslt;
    var callableRef;
    var ref;

    try {

        txRslt.value += "var callableRef = test.getCallableRef()\n";
        rslt = jsonserver.test.getCallableRef();
	if(rslt.json_type != "CallableReference") {
	  txRslt.value += " doesn't return a CallableReference. stopping.\n";
	  return false;
	}
	txRslt.value += "returns a CallableReference object_id=" +
			rslt.object_id + "\n\n";
        callableRef = new jsonrpc.ServerProxy(jsonurl + "?object_id=" +
					  rslt.object_id);

        txRslt.value += "callableRef.ping()\n";
	rslt = callableRef.ping()
        txRslt.value += "returns \"" + rslt + "\"\n\n";

        txRslt.value += "var ref = callableRef.getRef()\n";
	ref = callableRef.getRef();
	if(ref.json_type != "Reference") {
	  txRslt.value += " doesn't return a Reference. stopping.\n";
	  return false;
	}
	txRslt.value += "returns Reference object_id=" +
			ref.object_id + "\n\n";

        txRslt.value += "callableRef.whatsInside(ref)\n";
	rslt = callableRef.whatsInside(ref);
        txRslt.value += "returns \"" + rslt + "\"\n\n";

    } catch(e) {
        var em;
        if(e.toTraceString) {
            em = e.toTraceString();
        }else{
            em = e.message;
        }
        txRslt.value += "\n" + em + "\n";
    }
    return false;
}

doContainerTests = function()
{
    var txRslt = document.getElementById("txResult");
    txRslt.value = "Running Container tests\n\n";

    var rslt;
    var wigArrayList;

    try {

        txRslt.value += "wigArrayList = test.aWiggleArrayList(2)\n";
        wigArrayList = jsonserver.test.aWiggleArrayList(2);
        txRslt.value += "returns " + wigArrayList + "\n\n";

        txRslt.value += "test.aWiggleArrayList(wigArrayList)\n";
        rslt = jsonserver.test.wigOrWag(wigArrayList);
        txRslt.value += "returns \"" + rslt + "\"\n\n";

    } catch(e) {
        var em;
        if(e.toTraceString) {
            em = e.toTraceString();
        }else{
            em = e.message;
        }
        txRslt.value += "\n" + em + "\n";
    }
    return false;
}

doExceptionTest = function()
{
    var txRslt = document.getElementById("txResult");
    txRslt.value = "Running Exception test\n\n";

    var rslt;

    try {

        txRslt.value = "Calling test.fark()\n\n";
        rslt = jsonserver.test.fark();

    } catch(e) {
        var em;
        if(e.toTraceString) {
            em = e.toTraceString();
        }else{
            em = e.message;
        }
        txRslt.value += em;
    }
    return false;
}
