var ConstructorSelector=function(clearResults)
{
  var pub={};
  var prv={};
  
  prv.loadingJabsorbs=[];
  prv.jsonrpcs=[];
  pub.getIterator=function()
  {
    return function(x,jsonrpc)
    {
      var list;
      if((jsonrpc)&&(jsonrpc.constructor.toString().indexOf("vent")==-1))
      {
        list=[jsonrpc];
      }
      else
      {
        list=prv.jsonrpcs;
      }
      for(var i=0;i<list.length;i++)
      {
        for(name in unitTests)
        {
          x(list[i],name)
        }
      }
    }
  }
  prv.selectJabsorbConstructor_cb=function(name,callback)
  {
    var cb=function(jabsorb){
      jabsorb.name=name;
      prv.loadingJabsorbs.shift();
      //add the tests table
      prv.jsonrpcs.push(jabsorb);
      if(callback){
        callback(jabsorb);
      }
      if(prv.loadingJabsorbs.length>0)
      {
        window[prv.loadingJabsorbs[0].name](
            prv.loadingJabsorbs[0],
            j_absorbLibraries[prv.loadingJabsorbs[0].name.replace("_","-")])
      }
    }
    cb.name=name;
    return cb;
  };
  
  pub.selectJabsorbConstructor=function(name,keepOthers,callback)
  {
    if(name==="All libraries")
    {
      prv.jsonrpcs=[];
      var i;
      clearResults.clearAllResultGroups();
      for(i=0;i<pub.getLoadedJabsorbs().length;i++)
      {
        pub.selectJabsorbConstructor(pub.getLoadedJabsorbs()[i],true,callback);
      }
    }
    else
    {
      if(!keepOthers)
      {
        clearResults.clearAllResultGroups();
        prv.jsonrpcs=[];
      }
      var cb=prv.selectJabsorbConstructor_cb(name,callback)
      prv.loadingJabsorbs.push(cb);
      
      if(prv.loadingJabsorbs.length==1)
      {
        window[name](
          cb,
          j_absorbLibraries[name.replace("_","-")]);
      }
    }
  }
  
  pub._selectJabsorbConstructor=function(e,keepOthers,callback)
  {
    pub.selectJabsorbConstructor(e.target.value,keepOthers,callback);
  }
  prv.loadedJabsorbs=null;
  pub.getLoadedJabsorbs=function()
  {
    if(prv.loadedJabsorbs==null)
    {
      var x,i=0;
      prv.loadedJabsorbs=[];
      for(x in window)
      {
        if(x.substring(0,"jabsorb".length)==="jabsorb")
        {
          prv.loadedJabsorbs.push(x);
        }
      }
      prv.loadedJabsorbs.reverse();
    }
    return prv.loadedJabsorbs;
  }
  
  pub.getSelector=function()
  {
    
    var library={};
    library.gui=$n("div");
    addText(library,
        "Library",library.gui,"text");
    addElement(
        library,
        "select",
        {
          id:"librarySelector",
          onchange:function(e)
                   {
                     pub._selectJabsorbConstructor(
                         e,false,pub.getSelector.callback);
                   }
        },
        library.gui);
    library.options={};
    
    library.options[0]={};
    addElement(library.options[0],
      "option",{id:"all-libraries"},library);    
    addText(library.options[0],
        "All libraries",library.options[0],"text");
    var j_absorbs=pub.getLoadedJabsorbs();
    for(i =0;i< j_absorbs.length;i++)
    {
      library.options[i+1]={};
      addElement(library.options[i+1],
        "option",{id:("library-"+i)},library);    
      addText(library.options[i+1],
          j_absorbs[i],library.options[i+1],"text");
    }
    library.getSelected=function()
    {
      return library[valueKey].item(library[valueKey].selectedIndex).value;
    }
    library.update=function()
    {
      pub.selectJabsorbConstructor(
          library.getSelected(),false,
          pub.getSelector.callback
          );
    }
    return library;
  }

  return pub;
}