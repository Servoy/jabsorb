var RunTests=function(menu,postResults,clearResults)
{
  var pub={};
  var prv={};

  //Runs all the tests
  pub.runAllTests=function(jsonrpc)
  {
    prv.iterator(pub.runTestSet,jsonrpc);
  }
  pub.setIterator=function(i)
  {
    prv.iterator=i;
  }
  //Runs all the tests for a specific set
  pub.runTestSet=function(jsonrpc,name)
  {
    var i;
    if (menu.maxRequestNode.value < 1 || menu.maxRequestNode.value > 99)
    {
      alert("Max requests should be between 1 and 99");
      return;
    }
    jsonrpc.max_req_active = menu.maxRequestNode.value;
  
    clearResults.clearResultSet(jsonrpc,name);
    if (menu.profileNode.checked)
    {
      postResults.startTests();
    }
    if (menu.asyncNode.checked)
    {
      jsonrpc.profile_async = menu.profileNode.checked;
      for (i = 0; i < unitTests[name].tests.length; i++)
      {
        prv.runTestAsync(jsonrpc,name,i);
      }
    }
    else
    {
      for (i = 0; i < unitTests[name].tests.length; i++)
      {
        prv.runTestSync(jsonrpc,name,i);
      }
    }
  }
  
  //Runs a test and determines which mode (sync/async) to run it in.
  //Also looks after profiling
  pub.runTest=function(jsonrpc,name,i)
  {
    clearResults.clearResult(jsonrpc,name,i);
    if (menu.profileNode.checked)
    {
      //Global
      tests_start = new Date();
    }
    if(!unitTests[name].tests[i][jsonrpc.name])
    {
      unitTests[name].tests[i][jsonrpc.name]={};
    }
    unitTests[name].tests[i][jsonrpc.name].pass=false;
    unitTests[name].tests[i][jsonrpc.name].completed=false;
    unitTests[name].tests[i][jsonrpc.name].running=true;
    if (menu.asyncNode.checked)
    {
      jsonrpc.profile_async = menu.profileNode.checked;
      prv.runTestAsync(jsonrpc,name,i);
    }
    else
    {
      prv.runTestSync(jsonrpc,name,i);
    }
  }
  
  //This is the callback function used with async tests
  prv.testAsyncCB=function(jsonrpc,name,i)
  {
    return function (result, e, profile)
    {
      postResults.postResults(jsonrpc,name,i, result, e, profile,true);
    };
  }
  
  //Run a test in async mode
  prv.runTestAsync=function(jsonrpc,name,i)
  {
    try
    {
      // insert post results callback into first argument and submit test
      unitTests[name].tests[i].code(jsonrpc,prv.testAsyncCB(jsonrpc,name,i));
    }
    catch (e)
    {
      prv.testAsyncCB(jsonrpc,name,i)(null,e,false);
    }
  }
  
  //Runs a test in sync mode
  prv.runTestSync=function(jsonrpc,name,i)
  {
    var result;
    var exception;
    var profile;
    if (menu.profileNode.checked)
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
    if (menu.profileNode.checked)
    {
      profile.end = profile.dispatch = new Date();
    }
    postResults.postResults(jsonrpc,name,i, result, exception, profile,false);
  }
  return pub;
}