//loads page
function onLoad()
{
  var testVisibility=TestVisibility();
  var testExpanding=TestExpanding(testVisibility);
  var clearResults=ClearResults(testVisibility);
  var constructorSelector=ConstructorSelector(clearResults);
  var menu = Menu(constructorSelector);
  var postResults=PostResults(testVisibility);
  var runTests = RunTests(menu,postResults,clearResults);
  
  testVisibility.setMenu(menu);
  
  testVisibility.setIterator(constructorSelector.getIterator());
  runTests.setIterator(constructorSelector.getIterator());
  clearResults.setIterator(constructorSelector.getIterator());
  testExpanding.setIterator(constructorSelector.getIterator());
  
  menu.assignCallbacks(runTests,clearResults,testVisibility,testExpanding);
  menu.addTo(document.getElementById("actionMenu"))
  
  var testTable=TestTable(runTests,testExpanding,clearResults);
  var libraries=menu.table.secondRow.options.library[valueKey];
  constructorSelector.getSelector.callback=function(jabsorb)
  {
    var displayTable = testTable.createShowTestsTable(jabsorb);
    var results = document.getElementById("results");
    results.appendChild(displayTable);
    testVisibility.updateAllTestsVisibility(jabsorb);
  }
  menu.selector.update();
}