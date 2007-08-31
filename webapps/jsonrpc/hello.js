function onLoad()
{
    jsonrpc = new JSONRpcClient("/jsonrpc/JSON-RPC");
}

function clickHello()
{
    var whoNode = document.getElementById("who");
    var result = jsonrpc.hello.sayHello(whoNode.value);
    alert("The server replied: " + result);
}
