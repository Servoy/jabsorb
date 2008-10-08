var Summary = function()
{
  var pub={};
  var prv={};
  
  pub.getGui=function()
  {
    return prv.table[valueKey];
  }
  
  prv.init=function()
  {
    prv.table={};
    addElement(prv.table,"table",
        {
          className:"summary_table",
          cellpadding:0,
          cellspacing:0,
          border:0
        });
    
    addElement(prv.table,"thead",{},prv.table,"head");
    prv.table.headRow={};
    addElement(prv.table.headRow,"tr",{},prv.table.head);
    
    addElement(prv.table,"tbody",{},prv.table,"body");
    prv.table.bodyRow={};
    addElement(prv.table.bodyRow,"tr",{},prv.table.body);
    
    var headText=["Total Tests","Tests Completed","Successes","Failures"];
    var ids=["tests","completed","successes","failures"];
                  
    for(var i=0;i<ids.length;i++){
      addTextInCell(
          prv.table.headRow,
          headText[i],
          {cellAttributes:{className:"test_th"}},
          prv.table.headRow,
          ids[i]);
      
      addTextInCell(
          prv.table.bodyRow,
          "-",
          {cellAttributes:{className:"test_td",cellName:"cell_"+ids[i]}},
          prv.table.bodyRow,
          ids[i]);
    }
  }
  prv.testAmount=0;
  pub.addTestAmount=function(x)
  {
    prv.testAmount+=x;
    prv.update("cell_tests",prv.testAmount);
  }
  prv.update=function(cellName,value)
  {
    clearChildren(prv.table.bodyRow[cellName],"childNodes");
    prv.table.bodyRow[cellName].appendChild($t(value));
  }
  pub.subtractTestAmount=function(x)
  {
    prv.testAmount-=x;
    prv.update("cell_tests",prv.testAmount);
    if(prv.testAmount==0)
    {
      prv.successes=0;
      prv.failures=0;
      prv.update("cell_successes",prv.successes);
      prv.update("cell_failures",prv.failures);
      prv.updateTotal();
    }
  }
  prv.successes=0;
  pub.addSuccesses=function(x)
  {
    prv.successes+=x;
    prv.update("cell_successes",prv.successes);
    prv.updateTotal();
  }
  pub.subtractSuccesses=function(x)
  {
    prv.successes-=x;
    prv.update("cell_successes",prv.successes);
    prv.updateTotal();
  }
  prv.failures=0;
  pub.addFailures=function(x)
  {
    prv.failures+=x;
    prv.update("cell_failures",prv.failures);
    prv.updateTotal();
  }
  pub.subtractFailures=function(x)
  {
    prv.failures-=x;
    prv.update("cell_failures",prv.failures);
    prv.updateTotal();
  }  
  prv.updateTotal=function()
  {
    prv.update("cell_completed",prv.successes+prv.failures);
  }
  prv.init.apply(this,arguments);
  return pub;
}