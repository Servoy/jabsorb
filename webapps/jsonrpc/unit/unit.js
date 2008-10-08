//loads page
function onLoad()
{
  var summary = Summary();
  var testVisibility=TestVisibility();
  var testExpanding=TestExpanding(testVisibility);
  var clearResults=ClearResults(testVisibility,summary);
  var constructorSelector=ConstructorSelector(clearResults);
  var menu = Menu(constructorSelector);
  var postResults = PostResults(testVisibility,summary);
  var runTests = RunTests(menu,postResults,clearResults);
  
  testVisibility.setMenu(menu);
  
  testVisibility.setIterator(constructorSelector.getIterator());
  runTests.setIterator(constructorSelector.getIterator());
  clearResults.setIterator(constructorSelector.getIterator());
  testExpanding.setIterator(constructorSelector.getIterator());
  
  menu.assignCallbacks(runTests,clearResults,testVisibility,testExpanding);
  
  document.getElementById("actionMenu").appendChild(menu.getGui());
  document.getElementById("summary").appendChild(summary.getGui());
  
  constructorSelector.getSelector.callback=function(jabsorb)
  {
    var displayTable = TestTable(runTests,testExpanding,clearResults,
        summary,jabsorb);
    var results = document.getElementById("results");
    results.appendChild(displayTable.getGui());
    testVisibility.updateAllTestsVisibility(jabsorb);
  }
  menu.selector.update();
}