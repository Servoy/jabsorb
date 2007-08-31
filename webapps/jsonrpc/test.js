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

onSubmit = function()
{
    var evalStr = document.getElementById("txEval").value;     
    var txRslt = document.getElementById("txResult");
    txRslt.value = "";

    try {
        var rslt = eval("jsonserver." + evalStr);
        txRslt.value = "" + rslt;
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

runTests = function()
{
    var txRslt = document.getElementById("txResult");
    txRslt.value = "Running tests\n";

    txRslt.value += "Calling test.anArray()";
    try {
        var rslt = jsonserver.test.anArray();
        txRslt.value += " returns " + rslt + "\n";
    } catch(e) {
        var em;
        if(e.toTraceString) {
            em = e.toTraceString();
        }else{
            em = e.message;
        }
        txRslt.value += "\n" + em + "\n";
    }

    txRslt.value += "Calling test.anArrayList()";
    try {
        var rslt = jsonserver.test.anArrayList();
        txRslt.value += " returns " + rslt + "\n";
    } catch(e) {
        var em;
        if(e.toTraceString) {
            em = e.toTraceString();
        }else{
            em = e.message;
        }
        txRslt.value += "\n" + em + "\n";
    }

    txRslt.value += "Calling test.aVector()";
    try {
        var rslt = jsonserver.test.aVector();
        txRslt.value += " returns " + rslt + "\n";
    } catch(e) {
        var em;
        if(e.toTraceString) {
            em = e.toTraceString();
        }else{
            em = e.message;
        }
        txRslt.value += "\n" + em + "\n";
    }

    txRslt.value += "Calling test.aHashtable()";
    try {
        var rslt = jsonserver.test.aHashtable();
        txRslt.value += " returns " + rslt + "\n";
    } catch(e) {
        var em;
        if(e.toTraceString) {
            em = e.toTraceString();
        }else{
            em = e.message;
        }
        txRslt.value += "\n" + em + "\n";
    }

    txRslt.value += "Calling test.twice(\"foo\")";
    try {
        var rslt = jsonserver.test.twice("foo");
        txRslt.value += " returns " + rslt + "\n";
    } catch(e) {
        var em;
        if(e.toTraceString) {
            em = e.toTraceString();
        }else{
            em = e.message;
        }
        txRslt.value += "\n" + em + "\n";
    }


    txRslt.value += "Calling test.echoByteArray(\"test test\")";
    try {
        var rslt = jsonserver.test.echoByteArray("test test");
        txRslt.value += " returns " + rslt + "\n";
    } catch(e) {
        var em;
        if(e.toTraceString) {
            em = e.toTraceString();
        }else{
            em = e.message;
        }
        txRslt.value += "\n" + em + "\n";
    }

    txRslt.value += "Calling test.echoCharArray(\"test again\")";
    try {
        var rslt = jsonserver.test.echoCharArray("test again");
        txRslt.value += " returns " + rslt + "\n";
    } catch(e) {
        var em;
        if(e.toTraceString) {
            em = e.toTraceString();
        }else{
            em = e.message;
        }
        txRslt.value += "\n" + em + "\n";
    }

    txRslt.value += "Calling test.echoChar(\"c\")";
    try {
        var rslt = jsonserver.test.echoChar("c");
        txRslt.value += " returns " + rslt + "\n";
    } catch(e) {
        var em;
        if(e.toTraceString) {
            em = e.toTraceString();
        }else{
            em = e.message;
        }
        txRslt.value += "\n" + em + "\n";
    }

    txRslt.value += "Calling test.echo(\"a string\")";
    try {
        var rslt = jsonserver.test.echo("a string");
        txRslt.value += " returns " + rslt + "\n";
    } catch(e) {
        var em;
        if(e.toTraceString) {
            em = e.toTraceString();
        }else{
            em = e.message;
        }
        txRslt.value += "\n" + em + "\n";
    }

    txRslt.value += "Calling test.echo(2)";
    try {
        var rslt = jsonserver.test.echo(2);
        txRslt.value += " returns " + rslt + "\n";
    } catch(e) {
        var em;
        if(e.toTraceString) {
            em = e.toTraceString();
        }else{
            em = e.message;
        }
        txRslt.value += "\n" + em + "\n";
    }

    txRslt.value += "Calling test.echo({bang: 'foo', baz: 9})";
    try {
        var rslt = jsonserver.test.echo({bang: 'foo', baz: 9});
        txRslt.value += " returns " + rslt + "\n";
    } catch(e) {
        var em;
        if(e.toTraceString) {
            em = e.toTraceString();
        }else{
            em = e.message;
        }
        txRslt.value += "\n" + em + "\n";
    }

    txRslt.value += "Calling test.echo([\"abc\", \"def\"])";
    try {
        var rslt = jsonserver.test.echo(["abc","def"]);
        txRslt.value += " returns " + rslt + "\n";
    } catch(e) {
        var em;
        if(e.toTraceString) {
            em = e.toTraceString();
        }else{
            em = e.message;
        }
        txRslt.value += "\n" + em + "\n";
    }

    txRslt.value += "Calling test.echo([3,4])";
    try {
        var rslt = jsonserver.test.echo([3,4]);
        txRslt.value += " returns " + rslt + "\n";
    } catch(e) {
        var em;
        if(e.toTraceString) {
            em = e.toTraceString();
        }else{
            em = e.message;
        }
        txRslt.value += "\n" + em + "\n";
    }

    txRslt.value += "Calling test.waggleToWiggle({bang: 'foo', baz: 9})";
    try {
        var rslt = jsonserver.test.waggleToWiggle({bang: 'foo', baz: 9});
        txRslt.value += " returns " + rslt + "\n";
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

runReferenceTests = function()
{
    var txRslt = document.getElementById("txResult");

    try {
        txRslt.value = "Calling var callableRef = test.getCallableRef()\n";
        var rslt = jsonserver.test.getCallableRef();
	if(rslt.json_type != "CallableReference") {
	  txRslt.value += " doesn't return a CallableReference. stopping.\n";
	  return false;
	}
	txRslt.value += "returns a CallableReference object_id=" +
			rslt.object_id + "\n\n";
        var callableRef = new jsonrpc.ServerProxy(jsonurl + "?object_id=" +
					  rslt.object_id);

        txRslt.value += "Calling callableRef.ping()\n";
	rslt = callableRef.ping()
        txRslt.value += "returns " + rslt + "\n\n";

        txRslt.value += "Calling var ref = callableRef.getRef()\n";
	var ref = callableRef.getRef();
	if(ref.json_type != "Reference") {
	  txRslt.value += " doesn't return a Reference. stopping.\n";
	  return false;
	}
	txRslt.value += "returns Reference object_id=" +
			ref.object_id + "\n\n";

        txRslt.value += "Calling callableRef.whatsInside(ref)\n";
	rslt = callableRef.whatsInside(ref);
        txRslt.value += "returns " + rslt + "\n\n";

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