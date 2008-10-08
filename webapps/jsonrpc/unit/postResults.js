var postResults=function(testVisibility)
{
  var pub={};
  var prv={};

  prv.tests_start;
  
  /**
   * Displays the result of a test. Pass or Fail is calculated here.
   *
   * @param name the name of the test set
   * @param i the index of the test within the test set
   * @param result the string containing the result of the test
   * @param e any exception thrown when making the result
   * @param profile profile data (if it exists)(submit, start, end, dispatch)
   */ 
  pub.postResults=function(jsonrpc,name,i, result, e, profile,resultsAsync)
  {
    function resultPosted(pass,resultText)
    {
      var    profileText="";
      if (profile)
      {
        profileText =  "submit="   + (profile.submit -   prv.tests_start)   +
                     ", start="    + (profile.start -    prv.tests_start)   +
                     ", end="      + (profile.end -      prv.tests_start)   +
                     ", dispatch=" + (profile.dispatch - prv.tests_start)   +
                     " (rtt="      + (profile.end -      profile.start) + ")";
      }
      
      unitTests[name].tests[i].pass=pass;
      unitTests[name].tests[i].completed=true;
      unitTests[name].tests[i].running=false;
    
      testVisibility.updateTestSetVisibility(jsonrpc,name);
      prv.updateTestStyle(jsonrpc,name,i,pass,resultText,profileText);
      prv.updateSuccessFailCount(jsonrpc,name);
    }
  
    if (e)
    {
      prv.postException(unitTests[name].tests[i],result,e,resultPosted,resultsAsync);
    }
    else
    {
      prv.postNonException(jsonrpc,unitTests[name].tests[i],result,resultPosted,resultsAsync)
    }
  }
  prv.updateTestStyle=function(jsonrpc,name,i,pass,resultText,profileText)
  {
    var nodes=[
               document.getElementById(jsonrpc.name+name+"result." + i),
               document.getElementById(jsonrpc.name+name+"expected." + i),
               document.getElementById(jsonrpc.name+name+"pass." + i),
               document.getElementById(jsonrpc.name+name+"profile." + i)
              ];
    var testValues = [prv.getTestValue(unitTests[name].tests[i]),resultText,(pass?"pass":"FAIL"),profileText];
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
  prv.getTestValue=function(test)
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
  prv.updateSuccessFailCount=function(jsonrpc,name)
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
  prv.postNonException=function(jsonrpc,test,result,resultPosted,async)
  {
    var pass=true,resultText;
    var totalTests=0
    var testsPerformed=0;
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
      resultText= prv.resultToString(tmp);
    }
    else
    {
      resultText = result;
    }
    try
    {
      if(test.test)
      {
        pass=test.test(result,jsonrpc);
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
  
  prv.resultToString=function(o)
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
  
  
  prv.postException=function(test,result,e,resultPosted)
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
  pub.startTests=function()
  {
    prv.tests_start = new Date();
  }
  return pub;
}