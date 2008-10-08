var TestTable=function(runTests,testExpanding,clearResults,summary,jsonrpc)
{
  var pub={};
  var prv={};
  
  //This shows the summary of all tests
  prv.init=function(runTests,testExpanding,clearResults,summary,jsonrpc)
  {
    prv.table=$n("table");
    
    var thead,
        headerRow,
        i,
        unitTestGroupName,
        cell,
        headingTexts,
        tableCellIds,
        tests,
        tbody,
        summaryRow,
        detailedRow,
        textVal,
        href,
        downArrowDiv,
        div,
        text,
        altClass;
  
    prv.table.addEventListener(
        "DOMNodeRemoved",
        function(e)
        {
          if(e.target==prv.table){
            summary.subtractTestAmount(prv.getTestAmount());
            clearResults.clearAllResults(jsonrpc);
          }
        },false
    );
    
    headingTexts=["Test Set - "+jsonrpc.name,"Tests","Successes","Failures"];
    tableCellIds=["testName","testCount","successCount","failCount"];
  
    prv.table.className="test_table";
  
    //create the header
    thead = $n("thead");
    prv.table.appendChild(thead);  
    
    headerRow = $n("tr");
    thead.appendChild(headerRow);
    for(i=0;i<4;i++)
    {
      cell = $n("th");
      cell.className="test_th";
      cell.appendChild($t(headingTexts[i]));
      
      if(i==0)
      {
        var commands=["Run All Tests","Clear All Results","Expand All","Collapse All"];
        var shortCommands=["R","C","X","L"];
        var actions=[function(){runTests.runAllTests(jsonrpc);return false;},
                     function(){clearResults.clearAllResults(jsonrpc);return false;},
                     function(){testExpanding.expandAllResults(jsonrpc);return false;},
                     function(){testExpanding.collapseAllResults(jsonrpc);return false;},
                     ];
        for(var j=0;j<commands.length;j++)
        {
          href = $n("a");
          href.href="#";
          href.className="testSetRunner";
          href.onclick=actions[j];
          href.appendChild($t(shortCommands[j]));
          href.title=commands[j];
          cell.appendChild($t(" "));
          cell.appendChild(href);
        }
      }
      headerRow.appendChild(cell);
    }
    
    //creates the body
    for(unitTestGroupName in unitTests)
    {
      tests = unitTests[unitTestGroupName].tests;
      tbody = $n("tbody");
      prv.table.appendChild(tbody);
    
      //create the summary
      summaryRow = $n("tr");
      tbody.appendChild(summaryRow);
  
      //TODO: do this on the tbody instead
      altClass = "tr" + prv.table.tBodies.length%2;
      summaryRow.className = altClass;
      
      //create each cell in a row
      for(i=0;i<4;i++)
      {
        cell = $n("td");
        summaryRow.appendChild(cell);
        cell.className = "test_td";
        //The first one's text value is the group name
        if(i===0)
        {
          textVal=unitTestGroupName;
        }
        //The second is the number of tests
        else if(i===1)
        {
          textVal=tests.length;
        }
        //All others are -s
        else
        {
          textVal="-";
        }
        text = $t(textVal);
        //The first one needs the expand/collapse thing and the title,
        //The others are all simple
        if(i===0)
        {
          //var downArrow = $n("img");
          //downArrow.src="images/red-expand.gif"
          
          downArrowDiv = $n("span");
          downArrowDiv.title="expand";
          downArrowDiv.className="expandcollapse";
  
          downArrowDiv.appendChild($t("\u25bc"));
          downArrowDiv.down=true;
          downArrowDiv.onclick=
          function(e,name)
          {
            if(this.down)
            {
              testExpanding.expandTestSet(jsonrpc,name);
            }
            else
            {
              testExpanding.collapseTestSet(jsonrpc,name);
            }          
            this.down=!this.down;
          }.bindAsEventListener(downArrowDiv,unitTestGroupName);
          unitTests[unitTestGroupName][jsonrpc.name]={};
          unitTests[unitTestGroupName][jsonrpc.name].tableExpanded=false;
          unitTests[unitTestGroupName][jsonrpc.name].expandIcon=downArrowDiv;
          cell.appendChild(downArrowDiv);
          
          href = $n("a");
          href.href="#";
          href.className="testSetRunner";
          href.onclick=runTests.runTestSet.bind(runTests,jsonrpc,unitTestGroupName);
          href.appendChild(text);
          cell.appendChild(href);
        }
        else
        {
          div = $n("span");
          div.id=jsonrpc.name+unitTestGroupName+tableCellIds[i];
          div.appendChild(text);
          cell.appendChild(div);
        }
      }
      
      //create the detailed display
      detailedRow = $n("tr");
      tbody.appendChild(detailedRow);
  
      cell = $n("td");
      cell.className="innerTests_td " + altClass;
      detailedRow.appendChild(cell);
      cell.colSpan=4;
      div = $n("div");
      if(!unitTests[unitTestGroupName][jsonrpc.name])
      {
        unitTests[unitTestGroupName][jsonrpc.name]={};
      }
      unitTests[unitTestGroupName][jsonrpc.name].viewer = 
        prv.createDisplayTestSetTable(jsonrpc,unitTestGroupName);
      div.appendChild(unitTests[unitTestGroupName][jsonrpc.name].viewer);
      unitTests[unitTestGroupName][jsonrpc.name].viewer.style.display="none";
      cell.appendChild(div);
    }
    summary.addTestAmount(prv.getTestAmount());
  }
  prv.getTestAmount=function()
  {
    var count=0;
    for(var name in unitTests)
    {
      count+=unitTests[name].tests.length;
    }
    return count;
  }
  //Creates the detailed analysis of each test case
  prv.createDisplayTestSetTable=function(jsonrpc,name)
  {
    var table,
        thead,
        classes,
        innerTexts,
        i,j,
        row,
        cell,
        div,text,href;
  
    table=$n("table");  
    table.className="innerTests_table";
  
    classes=["code_heading","result_heading","expected_heading","pass_heading","profile_heading"];
    thClasses=["test_th","test_th","test_th","test_th","profile_th"];
    innerTexts=["Code","Result","Expected","Pass","Profile"];
    
    for(i=0;i<innerTexts.length;i++)
    {
      cell = $n("col");
      table.appendChild(cell);
    }
  
    thead = $n("thead");
    table.appendChild(thead);  
    
    row = $n("tr");
    thead.appendChild(row);
    for(i=0;i<innerTexts.length;i++)
    {
      cell = $n("th");
      cell.className=thClasses[i];
      div = $n("div");
      div.className=classes[i];
      text = $t(innerTexts[i]);
      div.appendChild(text);
      cell.appendChild(div);
      row.appendChild(cell);
    }
  
    var tBody = $n("tbody");
    table.appendChild(tBody);
    
    var ids=["code.","expected.","result.","pass.","profile."];
    var cellClasses=["test_td","test_td","test_td","test_td","profile_td"];
    for (i = 0; i < unitTests[name].tests.length; i++)
    {
      //if this row has not yet been created
      if(tBody.rows.length<=i)
      {
        row = $n("tr");
        tBody.appendChild(row);
      }
      else
      {
        row = tBody.rows[i+1];
      }
      clearChildren(row,"childNodes");
      row.className = "tr" + i%2;
      row.id = jsonrpc.name+name + "row."+ i;
  
      for(j=0;j<ids.length;j++)
      {
        cell = $n("td");
        row.appendChild(cell);
        cell.className = cellClasses[j];
  
        //Don't change this line without changing the id in postResults()
        cell.id = jsonrpc.name+name+ids[j]+i;
        if(j===0)
        {
          div = $n("div");
          div.className ="code_cell";
          href = $n("a");
          href.href="#";
          href.onclick=runTests.runTest.bind(runTests,jsonrpc,name,i);
          text = $t(unitTests[name].tests[i].code);
          href.appendChild(text);
          div.appendChild(href);
          cell.appendChild(div);
        }
        row.appendChild(cell);
      }
    }
    return table;
  }
  pub.getGui=function()
  {
    return prv.table;
  }
  prv.init.apply(this,arguments);
  return pub;
}