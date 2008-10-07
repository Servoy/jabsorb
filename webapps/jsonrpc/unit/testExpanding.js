var testExpanding=function(testVisibility)
{
  pub={};
  prv={};
  
  pub.expandAllResults=function(jsonrpc)
  {
    prv.iterator(pub.expandTestSet,jsonrpc);
  }
  pub.collapseAllResults=function(jsonrpc)
  {
    prv.iterator(pub.collapseTestSet,jsonrpc);
  }
  pub.setIterator=function(i)
  {
    prv.iterator=i;
  }
  pub.expandTestSet=function(jsonrpc,name)
  {
    unitTests[name][jsonrpc.name].expandIcon.childNodes[0].nodeValue="\u25b2";
    //this.childNodes[0].src="images/red-collapse.gif";
    unitTests[name][jsonrpc.name].expandIcon.title="collapse";
    unitTests[name][jsonrpc.name].tableExpanded=true;
    testVisibility.updateTestSetVisibility(jsonrpc,name);
  
  }
  pub.collapseTestSet=function(jsonrpc,name)
  {
    unitTests[name][jsonrpc.name].expandIcon.childNodes[0].nodeValue="\u25bc";
    unitTests[name][jsonrpc.name].expandIcon.title="expand";
    unitTests[name][jsonrpc.name].tableExpanded=false;
    testVisibility.hideTests(jsonrpc,name);
  }
  return pub;
}