if(!window.console)
{
  window.console={};
  window.console.log=function(s)
  {
    document.getElementById("console").appendChild($t(s));
    document.getElementById("console").appendChild($n(br))
  }
}

//bridge reference
var jsonrpcs = [];

//when the tests started
var tests_start;

//The difference of the width of detail tables vs the main table
var innerTableWidthDifference=30;

//from prototype.js
var $A = function(iterable)
{
  var results,i,length;
  if (!iterable)
  {
    return [];
  }
  if (iterable.toArray)
  {
    return iterable.toArray();
  }
  else
  {
    results = [];
    i=0;
    length = iterable.length;
    for (; i < length; i++)
    {
      results.push(iterable[i]);
    }
    return results;
  }
};

//from prototype.js
Function.prototype.bind = function() {
  var __method = this, args = $A(arguments), object = args.shift();
  return function()
  {
  
    __method.apply(object, args.concat($A(arguments)));
    return false;
  };
};

//from prototype.js
Function.prototype.bindAsEventListener = function() {
  var __method = this, args = $A(arguments), object = args.shift();
  return function(event) {
    __method.apply(object, [event || window.event].concat(args));
    return false;
  };
};

//shorthand to save typing
function $n(nodeType)
{
  if(nodeType.constructor === Array)
  {
    var x = document.createElement(nodeType[0]);
    x.type=nodeType[1];
    return x;
  }
  return document.createElement(nodeType);
}
function $t(text)
{
  return document.createTextNode(text);
}
var valueKey="__value";
function addElementInCell(obj,type,data,parent,key)
{
  addElement(obj,"td",data.cellAttributes,parent,"cell");
  addElement(obj,type,data,obj.cell,key);
}
function addElement(obj,type,data,parent,key)
{
  var k;
  var createdObj = $n(type);
  var usedKey;
  if(key)
  {
    usedKey=key;
  }
  else
  {
    usedKey=valueKey;
  }
  obj[usedKey] = createdObj;
  if(data)
  {
    for(k in data)
    {
      if(k=="cellAttributes")
      {
        continue;
      }
      obj[usedKey][k]=data[k];
    }
  }
  if(parent)
  {
    if(parent[valueKey])
    {
      parent[valueKey].appendChild(createdObj);
    }
    else
    {
      parent.appendChild(createdObj);
    }
  }
}
function addText(obj,text,parent,key)
{
  var createdObj = $t(text);
  var usedKey;
  if(key)
  {
    usedKey=key;
  }
  else
  {
    usedKey=valueKey;
  }
  obj[usedKey] = createdObj;
  if(parent)
  {
    if(parent[valueKey])
    {
      parent[valueKey].appendChild(createdObj);
    }
    else
    {
      parent.appendChild(createdObj);
    }
  }
}

function changeCss(theClass,element,value) {
  //Last Updated on May 21, 2008
  //documentation for this script at
  //http://www.shawnolson.net/a/503/altering-css-class-attributes-with-javascript.html
  var cssRules;
  if (document.all) 
  {
   cssRules = 'rules';
  }
  else if (document.getElementById) 
  {
   cssRules = 'cssRules';
  }
  var added = false;
  for (var S = 0; S < document.styleSheets.length; S++)
  {
    for (var R = 0; R < document.styleSheets[S][cssRules].length; R++) 
    {
      if (document.styleSheets[S][cssRules][R].selectorText == theClass) 
      {
        if(document.styleSheets[S][cssRules][R].style[element])
        {
          document.styleSheets[S][cssRules][R].style[element] = value;
          added=true;
          break;
        }
      }
    }
    if(!added)
    {
      if(document.styleSheets[S].insertRule)
      {
        document.styleSheets[S].insertRule(theClass+' { '+element+': '+value+'; }',document.styleSheets[S][cssRules].length);
      } 
      else if (document.styleSheets[S].addRule) 
      {
        document.styleSheets[S].addRule(theClass,element+': '+value+';');
      }
    }
  }
}

//removes all elements of the array "name", from an object
function clearChildren(node,name)
{
  if(!node)
  {
    return;
  }
  if(!name)
  {
    return;  
  }
  while(node[name].length>0)
  {
    node.removeChild(node[name][0]);
  }
}

// some variables to hold stuff
var tbody,asyncNode,profileNode,maxRequestNode,showSuccessesNode,hideUnrunNode;

//holds the loaded jabsorbs
var j_absorbs;

//loads page
function onLoad()
{
  j_absorbs=getLoadedJabsorbs();
  var menu = createMenu();
  document.getElementById("actionMenu").appendChild(menu[valueKey]);
  
  asyncNode = menu.firstRow.options.async[valueKey];
  profileNode = menu.firstRow.options.profile[valueKey];
  showSuccessesNode = menu.firstRow.options.showSuccesses[valueKey];
  hideUnrunNode = menu.firstRow.options.hideUnrun[valueKey];
  maxRequestNode = menu.secondRow.options.maxRequests[valueKey];

  showSuccessesNode.onclick=updateAllTestsVisibility;
  hideUnrunNode.onclick=updateAllTestsVisibility;
  profileNode.onclick=updateAllTestsVisibility;
  var libraries=menu.secondRow.options.library[valueKey];
  
  selectJabsorbConstructor(
      libraries.item(libraries.selectedIndex).value);
}
var loadingJabsorbs=[];
function selectJabsorbConstructor_cb(name,callback)
{
  var cb=function(jabsorb){
    jabsorb.name=name;
    loadingJabsorbs.shift();
    //add the tests table
    jsonrpcs.push(jabsorb);
    var displayTable = createShowTestsTable(jabsorb,name);
    var results = document.getElementById("results");
    results.appendChild(displayTable);
    updateAllTestsVisibility(jabsorb);
    if(callback){
      callback(jabsorb);
    }
    if(loadingJabsorbs.length>0)
    {
      window[loadingJabsorbs[0].name](
          loadingJabsorbs[0],
          j_absorbLibraries[loadingJabsorbs[0].name.replace("_","-")])
    }
  }
  cb.name=name;
  return cb;
};

function selectJabsorbConstructor(name,keepOthers,callback)
{
  if(name==="All libraries")
  {
    var i;
    clearAllResultGroups();
    for(i=0;i<j_absorbs.length;i++)
    {
      selectJabsorbConstructor(j_absorbs[i],true);
    }
  }
  else
  {
    if(!keepOthers)
    {
      clearAllResultGroups();
    }
    var cb=selectJabsorbConstructor_cb(name,callback)
    loadingJabsorbs.push(cb);
    
    if(loadingJabsorbs.length==1)
    {
      window[name](
        cb,
        j_absorbLibraries[name.replace("_","-")]);
    }
  }
}

function _selectJabsorbConstructor(e)
{
  selectJabsorbConstructor(e.target.value);
}

function createMenu()
{
  var table={};
  
  addElement(table,"table",
      {
        cellpadding:0,
        cellspacing:0,
        border:0,
        width:"100%"
      });
  
  addElement(table,"tbody",{},table,"body");
  
  table.firstRow={};
  addElement(table.firstRow,"tr",{},table.body);
  
  table.secondRow={};
  addElement(table.secondRow,"tr",{},table.body);

  table.firstRow.runAllTests={};
  addElementInCell(
      table.firstRow.runAllTests,["input","button"],
      {
        cellAttributes:{align:"left"},
        value:"Run All Tests",
        onclick:runAllTests
      },
      table.firstRow);
  
  table.firstRow.clearAllResults={};
  addElementInCell(
      table.firstRow.clearAllResults,["input","button"],
      {
        cellAttributes:{align:"left"},
        value:"Clear All Results",
        onclick:clearAllResults
      },
      table.firstRow);
  
  table.secondRow.expandAll={};
  addElementInCell(
      table.secondRow.expandAll,["input","button"],
      {
        cellAttributes:{align:"left"},
        value:"Expand All",
        onclick:expandAllResults
      },
      table.secondRow);
  
  table.secondRow.collapseAll={};
  addElementInCell(
      table.secondRow.collapseAll,["input","button"],
      {
        cellAttributes:{align:"left"},
        value:"Collapse All",
        onclick:collapseAllResults
      },
      table.secondRow);

  table.firstRow.options={}
  addElementInCell(table.firstRow.options,"div",{cellAttributes:{align:"right"},},table.firstRow);
  
  table.firstRow.options.showSuccesses={}
  addElement(table.firstRow.options.showSuccesses,
      ["input","checkbox"],{id:"showSuccesses",checked:"1",},
      table.firstRow.options);
  addText(table.firstRow.options.showSuccesses,
     "Show Successes |",table.firstRow.options,"text");
  
  table.firstRow.options.hideUnrun={}
  addElement(table.firstRow.options.hideUnrun,
      ["input","checkbox"],{id:"hideUnrun"},
      table.firstRow.options);
  addText(table.firstRow.options.hideUnrun,
      "Hide Unrun |",table.firstRow.options,"text");

  table.firstRow.options.profile={}
  addElement(table.firstRow.options.profile,
      ["input","checkbox"],{id:"profile"},
      table.firstRow.options);
  addText(table.firstRow.options.hideUnrun,
      "Profile  |",table.firstRow.options,"text");

  table.firstRow.options.async={}
  addElement(table.firstRow.options.async,
      ["input","checkbox"],{id:"async",checked:"0"},
      table.firstRow.options);
  addText(table.firstRow.options.hideUnrun,
      "Asynchronous )",table.firstRow.options,"text");

  table.secondRow.options={};
  addElementInCell(table.secondRow.options,"div",{cellAttributes:{align:"right"},},table.secondRow);

  table.secondRow.options.maxRequests={};
  addText(table.secondRow.options.maxRequests,
      "Max parallel async requests",table.secondRow.options,"text");
  addElement(table.secondRow.options.maxRequests,
      ["input","text"],{id:"max_requests",value:8,size:2},
      table.secondRow.options);

  table.secondRow.options.library={};
  addText(table.secondRow.options.library,
      "Library",table.secondRow.options,"text");
  addElement(table.secondRow.options.library,
      "select",{id:"librarySelector",onchange:_selectJabsorbConstructor},
      table.secondRow.options);
  table.secondRow.options.library.options={};
  
  table.secondRow.options.library.options[0]={};
  addElement(table.secondRow.options.library.options[0],
    "option",{id:"all-libraries"},table.secondRow.options.library);    
  addText(table.secondRow.options.library.options[0],
      "All libraries",table.secondRow.options.library.options[0],"text");
  for(i =0;i< j_absorbs.length;i++)
  {
    table.secondRow.options.library.options[i+1]={};
    addElement(table.secondRow.options.library.options[i+1],
      "option",{id:("library-"+i)},table.secondRow.options.library);    
    addText(table.secondRow.options.library.options[i+1],
        j_absorbs[i],table.secondRow.options.library.options[i+1],"text");
  }
  return table;
}

function updateAllTestsVisibility()
{
  var name;
  if(profileNode.checked)
  {
    changeCss(".profile_heading","display","block");
    changeCss(".profile_td","display","table-cell");
    changeCss(".profile_th","display","table-cell");
  }
  else
  {
    changeCss(".profile_heading","display","none");
    changeCss(".profile_td","display","none");
    changeCss(".profile_th","display","none");  
  }
  for(var i=0;i<jsonrpcs.length;i++)
  {
    for(name in unitTests)
    { 
      updateTestSetVisibility(jsonrpcs[0],name);
    }
  }
}
function updateTestSetVisibility(jsonrpc,name)
{
  var i,
    row,
    viewableCounter=0,
    tests=unitTests[name].tests;

  for (i = 0; i < tests.length; i++)
  {
    row = document.getElementById(jsonrpc.name+name+"row." + i);

    if(tests[i].completed)
    {
      //if showing successes show the row
      if(showSuccessesNode.checked)
      {
        showTableRow(row);
        viewableCounter++;
      }
      //if it passed and we're hiding successes don't show the row
      else if(tests[i].pass)
      {
        hideTableRow(row);
      }
      //if it failed show it.
      else
      {
        showTableRow(row);
        viewableCounter++;
      }
    }
    else if(hideUnrunNode.checked)
    {
      hideTableRow(row);
    }
    else
    {
      showTableRow(row);
      viewableCounter++;
    }
  }
  if((viewableCounter>0)&&(unitTests[name][jsonrpc.name].tableExpanded))
  {
    displayTests(jsonrpc,name);
  }
  else
  {
    hideTests(jsonrpc,name);
  }
}

function showTableRow(row)
{
  row.style.display="";
}
function hideTableRow(row)
{
  row.style.display="none";
}
function expandAllResults(jsonrpc)
{
  var list;
  if((jsonrpc)&&(jsonrpc.constructor.toString().indexOf("vent")==-1))
  {
    list=[jsonrpc];
  }
  else
  {
    list=jsonrpcs;
  }
  for(var i=0;i<list.length;i++)
  {
    for(var name in unitTests)
    {
      expandTestSet(list[i],name);
    }
  }
}
function collapseAllResults(jsonrpc)
{
  var list;
  if((jsonrpc)&&(jsonrpc.constructor.toString().indexOf("vent")==-1))
  {
    list=[jsonrpc];
  }
  else
  {
    list=jsonrpcs;
  }
  for(var i=0;i<list.length;i++)
  {
    for(var name in unitTests)
    {
      collapseTestSet(list[i],name);
    }
  }
}

function expandTestSet(jsonrpc,name)
{
  unitTests[name][jsonrpc.name].expandIcon.childNodes[0].nodeValue="\u25b2";
  //this.childNodes[0].src="images/red-collapse.gif";
  unitTests[name][jsonrpc.name].expandIcon.title="collapse";
  unitTests[name][jsonrpc.name].tableExpanded=true;
  updateTestSetVisibility(jsonrpc,name);

}
function collapseTestSet(jsonrpc,name)
{
  unitTests[name][jsonrpc.name].expandIcon.childNodes[0].nodeValue="\u25bc";
  unitTests[name][jsonrpc.name].expandIcon.title="expand";
  unitTests[name][jsonrpc.name].tableExpanded=false;
  hideTests(jsonrpc,name);
}

//This shows the summary of all tests
function createShowTestsTable(jsonrpc)
{
  var table=$n("table"),
      thead,
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

  headingTexts=["Test Set - "+jsonrpc.name,"Tests","Successes","Failures"];
  tableCellIds=["testName","testCount","successCount","failCount"];

  table.className="test_table";

  //create the header
  thead = $n("thead");
  table.appendChild(thead);  
  
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
      var actions=[function(){runAllTests(jsonrpc);return false;},
                   function(){clearAllResults(jsonrpc);return false;},
                   function(){expandAllResults(jsonrpc);return false;},
                   function(){collapseAllResults(jsonrpc);return false;},
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
    table.appendChild(tbody);
  
    //create the summary
    summaryRow = $n("tr");
    tbody.appendChild(summaryRow);

    //TODO: do this on the tbody instead
    altClass = "tr" + table.tBodies.length%2;
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
            expandTestSet(jsonrpc,name);
          }
          else
          {
            collapseTestSet(jsonrpc,name);
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
        href.onclick=runTestSet.bind(this,jsonrpc,unitTestGroupName);
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
      createDisplayTestSetTable(jsonrpc,unitTestGroupName);
    div.appendChild(unitTests[unitTestGroupName][jsonrpc.name].viewer);
    unitTests[unitTestGroupName][jsonrpc.name].viewer.style.display="none";
    cell.appendChild(div);
  }
  return table;
}

//Creates the detailed analysis of each test case
function createDisplayTestSetTable(jsonrpc,name)
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
        href.onclick=runTest.bind(this,jsonrpc,name,i);
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

//Show a detail table
function displayTests(jsonrpc,name)
{
  unitTests[name][jsonrpc.name].viewer.style.display="";
}

//Hide a detail table
function hideTests(jsonrpc,name)
{
  unitTests[name][jsonrpc.name].viewer.style.display="none";
}

function getLoadedJabsorbs()
{
  var x,i=0,jabsorbs=[];
  for(x in window)
  {
    if(x.substring(0,"jabsorb".length)==="jabsorb")
    {
      jabsorbs.push(x);
    }
  }
  jabsorbs.reverse();
  return jabsorbs;
}

function testAllCombos()
{
  var i;
  clearAllResultGroups();
  function loaded(_jabsorb)
  {
    runAllTests(_jabsorb);//jsonrpcs[i])
  }
  for(i=0;i<j_absorbs.length;i++)
  {
    selectJabsorbConstructor(j_absorbs[i],true,loaded);
  }
}

//Runs all the tests
function runAllTests(jsonrpc)
{
  var list;
  if((jsonrpc)&&(jsonrpc.constructor.toString().indexOf("vent")==-1))
  {
    list=[jsonrpc];
  }
  else
  {
    list=jsonrpcs;
  }
  for(var i=0;i<list.length;i++)
  {
    for(var name in unitTests)
    {
      runTestSet(list[i],name);
    }
  }
}

//Runs all the tests for a specific set
function runTestSet(jsonrpc,name)
{
  var i;
  if (maxRequestNode.value < 1 || maxRequestNode.value > 99)
  {
    alert("Max requests should be between 1 and 99");
    return;
  }
  jsonrpc.max_req_active = maxRequestNode.value;

  clearResultSet(jsonrpc,name);
  if (profileNode.checked)
  {
    tests_start = new Date();
  }
  if (asyncNode.checked)
  {
    jsonrpc.profile_async = profileNode.checked;
    for (i = 0; i < unitTests[name].tests.length; i++)
    {
      runTestAsync(jsonrpc,name,i);
    }
  }
  else
  {
    for (i = 0; i < unitTests[name].tests.length; i++)
    {
      runTestSync(jsonrpc,name,i);
    }
  }
}

//Runs a test and determines which mode (sync/async) to run it in.
//Also looks after profiling
function runTest(jsonrpc,name,i)
{
  clearResult(jsonrpc,name,i);
  if (profileNode.checked)
  {
    //Global
    tests_start = new Date();
  }
  unitTests[name].tests[i].pass=false;
  unitTests[name].tests[i].completed=false;
  unitTests[name].tests[i].running=true;
  if (asyncNode.checked)
  {
    jsonrpc.profile_async = profileNode.checked;
    runTestAsync(jsonrpc,name,i);
  }
  else
  {
    runTestSync(jsonrpc,name,i);
  }
}

//This is the callback function used with async tests
function testAsyncCB(jsonrpc,name,i)
{
  return function (result, e, profile)
  {
    postResults(jsonrpc,name,i, result, e, profile,true);
  };
}

//Run a test in async mode
function runTestAsync(jsonrpc,name,i)
{
  try
  {
    // insert post results callback into first argument and submit test
    unitTests[name].tests[i].code(jsonrpc,testAsyncCB(jsonrpc,name,i));
  }
  catch (e)
  {
    testAsyncCB(jsonrpc,name,i)(null,e,false);
  }
}

//Runs a test in sync mode
function runTestSync(jsonrpc,name,i)
{
  var result;
  var exception;
  var profile;
  if (profileNode.checked)
  {
    profile = {};
    profile.submit = profile.start = new Date();
  }
  try
  {
    result = unitTests[name].tests[i].code(jsonrpc,jsonrpc.doSync);
  }
  catch (e)
  {
    exception = e;
  }
  if (profileNode.checked)
  {
    profile.end = profile.dispatch = new Date();
  }
  postResults(jsonrpc,name,i, result, exception, profile,false);
}

function resultToString(o)
{
  var done={};
  var _toString=function(o)
  {
    var str,x;
    if (typeof o == "object")
    {
      if(done[o])
      {
        return "circRef";
      }
      done[o]="";
      if(o.constructor === Array)
      {
        str="[";
        for(x in o)
        {
          str+=toString(o[x])+", ";
        }  
        str+="]";
      }
      else
      {
        str="{";
        for(x in o)
        {
          str+=_toString(x)+": "+_toString(o[x])+", ";
        }  
        str+="}";
      }
      return str;
      
    }
    else
    {
      return o.toString();
    }
  };
  return _toString(o);
}

/**
 * Displays the result of a test. Pass or Fail is calculated here.
 *
 * @param name the name of the test set
 * @param i the index of the test within the test set
 * @param result the string containing the result of the test
 * @param e any exception thrown when making the result
 * @param profile profile data (if it exists)(submit, start, end, dispatch)
 */ 
function postResults(jsonrpc,name,i, result, e, profile,resultsAsync)
{
  function resultPosted(pass,resultText)
  {
    var    profileText="";
    if (profile)
    {
      profileText =  "submit="   + (profile.submit -   tests_start)   +
                   ", start="    + (profile.start -    tests_start)   +
                   ", end="      + (profile.end -      tests_start)   +
                   ", dispatch=" + (profile.dispatch - tests_start)   +
                   " (rtt="      + (profile.end -      profile.start) + ")";
    }
    
    unitTests[name].tests[i].pass=pass;
    unitTests[name].tests[i].completed=true;
    unitTests[name].tests[i].running=false;
  
    updateTestSetVisibility(jsonrpc,name);
    updateTestStyle(jsonrpc,name,i,pass,resultText,profileText);
    updateSuccessFailCount(jsonrpc,name);
  }

  if (e)
  {
    postException(unitTests[name].tests[i],result,e,resultPosted,resultsAsync);
  }
  else
  {
    postNonException(jsonrpc,unitTests[name].tests[i],result,resultPosted,resultsAsync)
  }
}

function updateTestStyle(jsonrpc,name,i,pass,resultText,profileText)
{
  var nodes=[
             document.getElementById(jsonrpc.name+name+"result." + i),
             document.getElementById(jsonrpc.name+name+"expected." + i),
             document.getElementById(jsonrpc.name+name+"pass." + i),
             document.getElementById(jsonrpc.name+name+"profile." + i)
            ];
  var testValues = [getTestValue(unitTests[name].tests[i]),resultText,(pass?"pass":"FAIL"),profileText];
  var classes = ["result_cell","result_cell",(pass?"pass_cell":"fail_cell"),"result_cell"];
  for(var j=0;j<nodes.length;j++)
  {
    clearChildren(nodes[j],"childNodes");
    nodes[j].style.display="";
    var div = $n("div");
    div.className = classes[j];
    div.appendChild($t(testValues[j]));
    nodes[j].appendChild(div);
  }
}
function getTestValue(test)
{
  var text="";
  if(test.test)
  {
    text+=test.test.toString();
  }
  if(test.functionTest)
  {
    var i;
    var j;
    if(test.test)
    {
      text+=" &&";
    }
    for(i=0;i<test.functionTest.length;i++)
    {
      text+=" result";
      for(j=0;j<test.functionTest[i][0].length;j++)
      {
        text+="."+test.functionTest[i][0][j];
      }
      text+="=="+test.functionTest[i][1];
      if(i!=test.functionTest.length-1)
      {
        text+=" && ";
      }
    }
  }
  return text;
}
function updateSuccessFailCount(jsonrpc,name)
{
  //Update the global success/fail count for this test set
  var successCount=0;
  var failCount=0;
  for(j=0;j<unitTests[name].tests.length;j++)
  {
    //Only count it if the test is not running and has been completed
    if((!unitTests[name].tests[j].running) && (unitTests[name].tests[j].completed))
    {
      if(unitTests[name].tests[j].pass)
      {
        successCount++;
      }
      else
      {
        failCount++;
      }
    }
  }
  
  var successDiv=document.getElementById(jsonrpc.name+name+"successCount");
  clearChildren(successDiv,"childNodes");
  successDiv.appendChild($t(successCount));
  var failDiv=document.getElementById(jsonrpc.name+name+"failCount");
  clearChildren(failDiv,"childNodes");
  failDiv.appendChild($t(failCount));
}
function postNonException(jsonrpc,test,result,resultPosted,async)
{
  var pass=true,resultText;
  var totalTests=0
  var testsPerformed=0;
  function _rp(pass)
  {
    resultPosted(pass,resultText)
  }
  function oneResult(desiredResult)
  {
    return function(actualResult)
    {
      var pass=actualResult!=desiredResult;
      if(actualResult!=desiredResult)
      {
        resultPosted(false,resultText);
      }
      else
      {
        testsPerformed++;
      }
      if(testsPerformed===totalTests)
      {
        resultPosted(true,resultText);
      }
      return pass;
    };
  }
  if (typeof result == "object")
  {
    var tmp = jsonrpc.toJSON(result,"result");
    resultText= resultToString(tmp);
  }
  else
  {
    resultText = result;
  }
  try
  {
    if(test.test)
    {
      pass=test.test(result);
    }
    if(!pass)
    {
      resultPosted(false,resultText);
    }
    else if(test.functionTest)
    {
      totalTests=test.functionTest.length;
      var i;
      var j;
      var func,old;
      for(i=0;i<test.functionTest.length;i++)
      {
        func=result;
        for(j=0;j<test.functionTest[i][0].length;j++)
        {
          old=func;
          func=func[test.functionTest[i][0][j]];
          if(typeof func === "function")
          {
            if(async)
            {
              func.apply(old,[oneResult(test.functionTest[i][1])]);
            }
            else
            {
              func=oneResult(test.functionTest[i][1])(func());
              if(!func)
              {
                j=test.functionTest[i][0].length
                i=test.functionTest.length;
              }
            }
          }
        }
      }
    }
    else
    {
      resultPosted(true,resultText);
    }
  }
  catch(e)
  {
    var tmp="";
    for(var aa in e)
    {
      tmp+=aa+" "+e[aa]+"\t";
    }
    resultText=tmp;
  }
  
}
function postException(test,result,e,resultPosted)
{
  var pass=false,resultText;
  if (e.message)
  {
    resultText = e.message;
  }
  else
  {
    resultText = e.toString();
  }
  if (test.exception)
  {
    try
    {
      pass = test.test(result,e);
    }
    catch(e)
    {
    }
  }
  resultPosted(pass,resultText);
}

function clearAllResultGroups()
{
  var results = document.getElementById("results");
  var i;
  while(results.childNodes.length>0)
  {
    results.removeChild(results.childNodes.item(0));
  }
  jsonrpcs=[];
}

//Clears all displayed test results
function clearAllResults(jsonrpc)
{
  var list;
  if((jsonrpc)&&(jsonrpc.constructor.toString().indexOf("vent")==-1))
  {
    list=[jsonrpc];
  }
  else
  {
    list=jsonrpcs;
  }
  for(var i=0;i<list.length;i++)
  {
    for(var name in unitTests)
    {
      clearResultSet(list[i],name);
    }
    updateAllTestsVisibility(list[i]);
  }
}

//clears all the posted results for a set of tests
function clearResultSet(jsonrpc,name)
{  
  var successDiv=document.getElementById(jsonrpc.name+name+"successCount");
  clearChildren(successDiv,"childNodes");
  successDiv.appendChild($t("-"));
  var failDiv=document.getElementById(jsonrpc.name+name+"failCount");
  clearChildren(failDiv,"childNodes");
  failDiv.appendChild($t("-"));
  for(var i =0;i< unitTests[name].tests.length;i++)
  {
    clearResult(jsonrpc,name,i);
  }
}

//Clear the result for the ith test in the test set given by name
function clearResult(jsonrpc,name,i)
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
