//when the tests started
var tests_start;

//loads page
function onLoad()
{
  var _testVisibility=testVisibility();
  var _testExpanding=testExpanding(_testVisibility);
  var _clearResults=clearResults(_testVisibility);
  var _constructorSelector=constructorSelector(_clearResults);
  var _menu = menu(_constructorSelector);
  var _postResults=postResults(_testVisibility);
  var _runTests = runTests(_menu,_postResults,_clearResults);
  
  _testVisibility.setMenu(_menu);
  
  _testVisibility.setIterator(_constructorSelector.getIterator());
  _runTests.setIterator(_constructorSelector.getIterator());
  _clearResults.setIterator(_constructorSelector.getIterator());
  _testExpanding.setIterator(_constructorSelector.getIterator());
  
  _menu.assignCallbacks(_runTests,_clearResults,_testVisibility,_testExpanding);
  _menu.addTo(document.getElementById("actionMenu"))
  
//  profileNode = _menu.table.firstRow.options.profile[valueKey];
  showSuccessesNode = _menu.table.firstRow.options.showSuccesses[valueKey];
  hideUnrunNode = _menu.table.firstRow.options.hideUnrun[valueKey];
  maxRequestNode = _menu.table.secondRow.options.maxRequests[valueKey];
  var _testTable=testTable(_runTests,_testExpanding,_clearResults);
  var libraries=_menu.table.secondRow.options.library[valueKey];
  _constructorSelector.getSelector.callback=function(jabsorb)
  {
    var displayTable = _testTable.createShowTestsTable(jabsorb);
    var results = document.getElementById("results");
    results.appendChild(displayTable);
    _testVisibility.updateAllTestsVisibility(jabsorb);
  }
  _menu.selector.update();
}