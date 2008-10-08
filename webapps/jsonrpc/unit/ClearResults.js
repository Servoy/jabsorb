var ClearResults=function(testVisibility)
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
    
    unitTests[name].tests[i].completed=false;
    unitTests[name].tests[i].running=false;
  
    for(j=0;j<nodes.length;j++)
    {
      clearChildren(nodes[j],"childNodes");
    }
  }
  return pub;
}