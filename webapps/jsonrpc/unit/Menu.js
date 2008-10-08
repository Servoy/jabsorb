function Menu(constructorSelector)
{
  var prv={};
  var pub={};
  pub.assignCallbacks=function(runTests,clearResults,testVisibility,testExpanding)
  {
    prv.table.firstRow.runAllTests[valueKey].onclick=runTests.runAllTests;
    prv.table.secondRow.expandAll[valueKey].onclick=testExpanding.expandAllResults;
    prv.table.secondRow.collapseAll[valueKey].onclick=testExpanding.collapseAllResults
    prv.table.firstRow.clearAllResults[valueKey].onclick=clearResults.clearAllResults;

    pub.showSuccessesNode.onclick=testVisibility.updateAllTestsVisibility;
    pub.hideUnrunNode.onclick=testVisibility.updateAllTestsVisibility;
    pub.profileNode.onclick=testVisibility.updateAllTestsVisibility;
  }
  pub.addTo=function(domObj)
  {
    domObj.appendChild(prv.table[valueKey]);
  }
  pub.table={};
  prv.table=pub.table;
  prv.init=function()
  {
    addElement(prv.table,"table",
        {
          cellpadding:0,
          cellspacing:0,
          border:0,
          width:"100%"
        });
    
    addElement(prv.table,"tbody",{},prv.table,"body");
    
    prv.table.firstRow={};
    addElement(prv.table.firstRow,"tr",{},prv.table.body);
    
    prv.table.secondRow={};
    addElement(prv.table.secondRow,"tr",{},prv.table.body);
  
    prv.table.firstRow.runAllTests={};
    addElementInCell(
        prv.table.firstRow.runAllTests,
        ["input","button"],
        {
          cellAttributes:{align:"left"},
          value:"Run All Tests"
        },
        prv.table.firstRow);
    
    prv.table.firstRow.clearAllResults={};
    addElementInCell(
        prv.table.firstRow.clearAllResults,["input","button"],
        {
          cellAttributes:{align:"left"},
          value:"Clear All Results",
        },
        prv.table.firstRow);
    
    prv.table.secondRow.expandAll={};
    addElementInCell(
        prv.table.secondRow.expandAll,["input","button"],
        {
          cellAttributes:{align:"left"},
          value:"Expand All"
        },
        prv.table.secondRow);
    
    prv.table.secondRow.collapseAll={};
    addElementInCell(
        prv.table.secondRow.collapseAll,["input","button"],
        {
          cellAttributes:{align:"left"},
          value:"Collapse All"
        },
        prv.table.secondRow);
  
    prv.table.firstRow.options={}
    addElementInCell(prv.table.firstRow.options,"div",{cellAttributes:{align:"right"},},prv.table.firstRow);
    
    prv.table.firstRow.options.showSuccesses={}
    addElement(prv.table.firstRow.options.showSuccesses,
        ["input","checkbox"],{id:"showSuccesses",checked:"1",},
        prv.table.firstRow.options);
    addText(prv.table.firstRow.options.showSuccesses,
       "Show Successes |",prv.table.firstRow.options,"text");
    
    prv.table.firstRow.options.hideUnrun={}
    addElement(prv.table.firstRow.options.hideUnrun,
        ["input","checkbox"],{id:"hideUnrun"},
        prv.table.firstRow.options);
    addText(prv.table.firstRow.options.hideUnrun,
        "Hide Unrun |",prv.table.firstRow.options,"text");
  
    prv.table.firstRow.options.profile={}
    addElement(prv.table.firstRow.options.profile,
        ["input","checkbox"],{id:"profile"},
        prv.table.firstRow.options);
    addText(prv.table.firstRow.options.hideUnrun,
        "Profile  |",prv.table.firstRow.options,"text");
  
    prv.table.firstRow.options.async={}
    addElement(prv.table.firstRow.options.async,
        ["input","checkbox"],{id:"async",checked:"0"},
        prv.table.firstRow.options);
    addText(prv.table.firstRow.options.hideUnrun,
        "Asynchronous )",prv.table.firstRow.options,"text");
  
    prv.table.secondRow.options={};
    addElementInCell(prv.table.secondRow.options,"div",{cellAttributes:{align:"right"},},prv.table.secondRow);
  
    var selector=constructorSelector.getSelector();
    selector.gui.style.cssFloat="right";
    prv.table.secondRow.options[valueKey].appendChild(selector.gui);
    prv.table.secondRow.options.library=selector;
    

    prv.table.secondRow.options.maxRequests={};
    addElement(prv.table.secondRow.options.maxRequests,"div",
        {cssFloat:"right"},prv.table.secondRow.options,"container");
    addText(prv.table.secondRow.options.maxRequests,
        "Max parallel async requests",prv.table.secondRow.options.maxRequests.container,"text");
    addElement(prv.table.secondRow.options.maxRequests,
        ["input","text"],{id:"max_requests",value:8,size:2},
        prv.table.secondRow.options.maxRequests.container);
    
    pub.asyncNode = prv.table.firstRow.options.async[valueKey];
    pub.profileNode = prv.table.firstRow.options.profile[valueKey];
    pub.showSuccessesNode = prv.table.firstRow.options.showSuccesses[valueKey];
    pub.hideUnrunNode = prv.table.firstRow.options.hideUnrun[valueKey];
    pub.maxRequestNode = prv.table.secondRow.options.maxRequests[valueKey];
    pub.selector=prv.table.secondRow.options.library;
  }
  prv.init.apply(this,arguments);
  return pub;
}