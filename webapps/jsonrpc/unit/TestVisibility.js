var TestVisibility=function(menu)
{
  var pub={};
  var prv={};
  pub.setMenu=function(_menu)
  {
    menu=_menu;
  }
  pub.updateAllTestsVisibility=function()
  {
    var name;
    if(menu.profileNode.checked)
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
    prv.iterator(pub.updateTestSetVisibility)
  }
  pub.setIterator=function(i)
  {
    prv.iterator=i;
  }
  pub.updateTestSetVisibility=function(jsonrpc,name)
  {
    var i,
      row,
      viewableCounter=0,
      tests=unitTests[name].tests;
  
    for (i = 0; i < tests.length; i++)
    {
      row = document.getElementById(jsonrpc.name+name+"row." + i);
  
      if(tests[i][jsonrpc.name]&&tests[i][jsonrpc.name].completed)
      {
        //if showing successes show the row
        if(menu.showSuccessesNode.checked)
        {
          prv.showTableRow(row);
          viewableCounter++;
        }
        //if it passed and we're hiding successes don't show the row
        else if(tests[i].pass)
        {
          prv.hideTableRow(row);
        }
        //if it failed show it.
        else
        {
          prv.showTableRow(row);
          viewableCounter++;
        }
      }
      else if(menu.hideUnrunNode.checked)
      {
        prv.hideTableRow(row);
      }
      else
      {
        prv.showTableRow(row);
        viewableCounter++;
      }
    }
    if((viewableCounter>0)&&(unitTests[name][jsonrpc.name].tableExpanded))
    {
      prv.displayTests(jsonrpc,name);
    }
    else
    {
      pub.hideTests(jsonrpc,name);
    }
  }
  
  prv.showTableRow=function(row)
  {
    if(row==null)
    {
      console.log("null row!")
    }
    row.style.display="";
  }
  
  prv.hideTableRow=function(row)
  {
    row.style.display="none";
  }
  
  //Show a detail table
  prv.displayTests=function(jsonrpc,name)
  {
    unitTests[name][jsonrpc.name].viewer.style.display="";
  }
  
  //Hide a detail table
  pub.hideTests=function(jsonrpc,name)
  {
    unitTests[name][jsonrpc.name].viewer.style.display="none";
  }
  
  return pub;
}