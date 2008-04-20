// this is an extension of the ExtJS grid that allows the Grid
// to have simple filters on each columns.

// Originally written by cocorossello
// see http://extjs.com/forum/showthread.php?p=106262#post106262
// and (en espanol) http://extjs.com/forum/showthread.php?t=23967&page=2

// Modified/rewritten by Arthur Blake:
//    Simplified some things, translated to English, fixed column hide/show bug,
//    changed filter submission logic to be part of the class 
//    (you can override the buildFilter method to modify this)
  
Ext.grid.FilteredColumnModel= Ext.extend(Ext.grid.ColumnModel, {
    getFilter: function(index){
        return this.config[index].filter;
    }
});

Ext.grid.FilteredGridPanel = Ext.extend(Ext.grid.GridPanel, {
  initComponent : function()
  {
    Ext.grid.GridPanel.superclass.initComponent.call(this);

    if(this.columns && (this.columns instanceof Array))
    {
      this.colModel = new Ext.grid.FilteredColumnModel(this.columns);
    }
    this.searchFields=[];

    this.prevFilter = {};      // previous filter than ran
    this.prevFilterCount = 0; // initialize to -1 to force first filter to run

    var templateText = 
    '<table border="0" cellspacing="0" cellpadding="0" class="x-grid3-header" style="{tstyle}">'+
    '<thead><tr class="x-grid3-hd-row x-grid3-header">{cells}</tr><tr>';

    for(var i=0;i<this.columns.length;i++)
    {
      templateText += '<td><div id="' +
        this.id + 'Search' + i + '"></div></td>';
    }
    templateText+='</tr></thead></table>';

    delete this.columns;

    var headerTpl = new Ext.Template(templateText);
    this.viewConfig=this.viewConfig || {};
    
    Ext.applyIf(this.viewConfig ,{forceFit:true,templates:{}} );
    this.viewConfig.templates.header=this.viewConfig.templates.header || headerTpl;

    if(this.sm)
    {
      this.selModel = this.sm;
      delete this.sm;
    }
    this.store = Ext.StoreMgr.lookup(this.store);

    this.addEvents('filterChanged', 'refresh');
    this.on('filterChanged',this.triggerSearch,this);
  },
  // trigger a search 150 msec after a filter changes, and insure that 
  // multiple changes issued in succession (within a 150 msec window) result 
  // in only one search submitted
  triggerSearch:function()
  {
    if(this.countDown)
    {
      clearTimeout(this.countDown);
    }
    this.countDown=this.refresh.defer(150,this);
  },
  
  // manipulate the base params of the store to provide the filters needed
  // to go.  Return true only if filter actually changed
  // return false if filters did not change.
  buildFilter: function() {
    var sf=this.searchFields,val,key,i=0,j=sf.length,filter={},filterCount=0;
    for(;i<j;i++)
    {
      if(sf[i])
      {
        key = sf[i].getName();
        val = sf[i].getValue();
        if (val===null || val===undefined || val==="")
        {
          delete this.store.baseParams[key];
        }
        else
        {
          filter[key]=val;
          filterCount++;
          this.store.baseParams[key]=val;
        }
      }
    }

    // compare the filter just constructed to the previously submitted
    // filter.  Only submit the new filter if it actually changed    
    if (filterCount !== this.prevFilterCount)
    {
      this.prevFilter = filter;
      this.prevFilterCount = filterCount;
      return true; // submit filter!
    }
    for (i in filter)
    {
      if (!i in this.prevFilter || filter[i]!==this.prevFilter[i])
      {
        this.prevFilter = filter;
        this.prevFilterCount = filterCount;
        return true;  // submit filter!
      }
    }
    return false;   // don't submit filter!
  },
  
  refresh:function(){
    if (this.buildFilter())
    {
	    this.store.load({
	      params:{start:0,limit:this.getBottomToolbar()? 
	        this.getBottomToolbar().pageSize : 100}
	    });
	    this.store.on('load',function(){
	      this.lastRefresh=new Date();
	      this.fireEvent('refresh',this);
	    },this);
    }
    this.countDown=undefined;
  },
  createSearchFields:function(){
    var defaults,i=0,did,cm=this.getColumnModel(),j=cm.getColumnCount(), parent;

    // build the search fields from from the column model
    this.searchFields=[];
    this.store.filters=[];

    for (;i<j;i++)
    {
    	if (cm.getFilter(i))
    	{
	      did = Ext.get(this.id+'Search'+i);
	      defaults =
	      {
	        defaultValue: '',               // value used when field is cleared
	        searchHideElem: did.parent(),   // parent td elem for hide/show
	        renderTo: did,
	        xtype: 'textfield',
	        style:'background: #FFF0B9;',
	        listeners: {
	          valid: {fn:function(){this.fireEvent('filterChanged');},scope:this}
	        }
	      };
	      Ext.apply(defaults, cm.getFilter(i));
	      
	      this.store.filters[i]=this.searchFields[i]= 
	        Ext.ComponentMgr.create(defaults);
    	}
    }
    if(!window.FilteringInitialized)
    {
      this.syncFields.defer(1000,this);
      window.FilteringInitialized=true;
    }
    else
    {
      this.syncFields();
    }
    this.on('columnresize',this.syncFields,this);
    this.on('columnmove',this.createSearchFields,this);
    this.on('resize',this.syncFields,this);
    cm.on('hiddenchange',this.syncFields,this);
  },
  syncFields:function(){
    var cm = this.getColumnModel(),
      i=0,sf = this.searchFields,j=sf.length;

    for(;i<j;i++)
    {
      if(sf[i])
      {
        sf[i].setSize(cm.getColumnWidth(i)-10);
        sf[i].searchHideElem.setDisplayed((cm.isHidden(i)?"none":true));
      }
    }
  },
  clearFilters:function()
  {
    var sf = this.searchFields,i=0,j=sf.length;
    for(;i<j;i++)
    {
      if(sf[i])
      {
        sf[i].setValue(sf[i].defaultValue);
      }
    }
  },
  afterRender : function(){
    Ext.grid.GridPanel.superclass.afterRender.call(this);
    this.view.layout();
    this.viewReady = true;
    this.createSearchFields.defer(100,this);
  }
});
