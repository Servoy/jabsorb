var ClearResults=function(testVisibility,summary)
{
  var pub={};
  var prv={};
  
  pub.clearAllResultGroups=function()
  {
    var results = document.getElementById("results");
    var i;
    while(results.childNodes.length>0)
    {
      results.removeChild(results.childNodes.item(0));
    }
  }
  
  //Clears all displayed test results
  pub.clearAllResults=function(jsonrpc)
  {
    prv.iterator(pub.clearResultSet,jsonrpc);
  }
  pub.setIterator=function(i)
  {
    prv.iterator=i;
  }
  //clears all the posted results for a set of tests
  pub.clearResultSet=function(jsonrpc,name)
  {  
    var successDiv=document.getElementById(jsonrpc.name+name+"successCount");
    clearChildren(successDiv,"childNodes");
    successDiv.appendChild($t("-"));
    var failDiv=document.getElementById(jsonrpc.name+name+"failCount");
    clearChildren(failDiv,"childNodes");
    failDiv.appendChild($t("-"));
    for(var i =0;i< unitTests[name].tests.length;i++)
    {
      pub.clearResult(jsonrpc,name,i);
    }
    prv.clearSummary(jsonrpc,name);
  }
  
  //Clear the result for the ith test in the test set given by name
  pub.clearResult=function(jsonrpc,name,i)
  {
    var j,nodes = [
      document.getElementById(jsonrpc.name+name+"result." + i),
      document.getElementById(jsonrpc.name+name+"expected." + i),
      document.getElementById(jsonrpc.name+name+"pass." + i),
      document.getElementById(jsonrpc.name+name+"profile." + i)
    ];
    if(!unitTests[name].tests[i][jsonrpc.name])
    {
      unitTests[name].tests[i][jsonrpc.name]={};
    }
    unitTests[name].tests[i][jsonrpc.name].completed=false;
    unitTests[name].tests[i][jsonrpc.name].running=false;
  
    for(j=0;j<nodes.length;j++)
    {
      clearChildren(nodes[j],"childNodes");
    }
  }
  prv.clearSummary=function(jsonrpc)
  {
    var successes=0;
    var failures=0;
    if(unitTests[name].tests[jsonrpc.name]){
    //Only count it if the test is not running and has been completed
      successes+=unitTests[name].tests[jsonrpc.name].successCount;
      failures+=unitTests[name].tests[jsonrpc.name].failCount;
      unitTests[name].tests[jsonrpc.name].successCount=0;
      unitTests[name].tests[jsonrpc.name].failCount=0;
    }
    summary.subtractSuccesses(successes);
    summary.subtractFailures(failures);
  }
  return pub;
}