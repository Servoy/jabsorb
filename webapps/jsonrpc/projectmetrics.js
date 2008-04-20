/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2008 The jabsorb team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/* 
 * A demonstration of jabsorb with the ext 2.0.2 grid and an Apache Derby
 * embedded database on the backend.
 */
Ext.onReady(function() {
  var jsonrpc,reader,proxy,store,grid,projectDropDownProxy;

  /**
   * A really generic exception handler for to handle runtime/boundary 
   * JSON-RPC exceptions.
   * 
   * Return true if the exception was handled, and false if there was no 
   * exception.
   * @param e a possible exception.
   */
  function except(e)
  {
    if (!e)
    {
      return false;
    }
    else if ((e.name && e.name == "JSONRpcClientException") || e.message)
    {
      Ext.Msg.alert('Exception',e.code + '<BR>' + e.message);
    }
    else
    {
      Ext.Msg.alert('Server Exception',e.code + '<BR>' + e.msg);
    }
    return true;
  }

  // create jabsorb JSON-RPC client object.
  jsonrpc = new JSONRpcClient("JSON-RPC");

  // create a reader to translate jabsorb's json-rpc
  // responses into a format that the store for the grid can handle
  reader = new Ext.data.JsonReader({
     root: 'results',
     totalProperty: 'totalCount',
     id: 'id'
    }, 
    [
      {name: 'project', mapping: 'project'},
      {name: 'path',    mapping: 'path'},
      {name: 'name',    mapping: 'name'},
      {name: 'type',    mapping: 'type'},
      {name: 'size',    mapping: 'size'},
      {name: 'lines',   mapping: 'lines'}
    ]);

  // create Ext proxy for getting records for the grid.  Pass in the json-rpc 
  // server side method that should be called to query records
  // see org.jabsorb.ext.ProjectMetricsHandler for Java side method
  // that the proxy will call
  proxy = new Ext.data.JsonRpcProxy(jsonrpc.ProjectMetrics.queryRecords);

  // if json-rpc exception occurs when the proxy is trying to load something
  // handle it generically 
  proxy.on("loadexception",function(p,o,e){except(e);});

  // create the store for the grid
  store = new Ext.data.Store({
    proxy: proxy,
    reader: reader,
    remoteSort: true  // the server will sort for us
  });

  /**
   * Helper function to build a column filter that is a simple combo box 
   * populated by server side data.
   * 
   * @param name / column mapping
   * @param rpc jabsorb method to call to populate combo box. 
   */
  function makeSimpleDropDown(name, rpc)
  {
    var proxy = new Ext.data.JsonRpcProxy(rpc);

    // use generic exception handler if something goes wrong on rpc call
    proxy.on("loadexception",function(p,o,e){except(e);});
  	
    var store = new Ext.data.Store({
      proxy: proxy,
      reader: new Ext.data.JsonReader({},[{name: 'VALUE'},{name:'LABEL'}]),
      remoteSort: true  // the server will sort for us
    });
  	
    var filter =
    {
      xtype:'combo',
      name: name,
      // make it work a little bit more like a regular html select
      triggerAction: 'all',  
      store: store, 
      displayField:'LABEL',valueField:'VALUE',defaultValue:"",
      typeAhead:true,
      selectOnFocus:true,
      forceSelection:true
    };
    return filter;     
  }

  // build the grid    
  grid = new Ext.grid.FilteredGridPanel({
    store: store,
    columns: [
      {header: 'Project', width: 100, sortable: true, dataIndex: 'project', 
        tooltip:'Open Source Project',
        filter: makeSimpleDropDown('project', jsonrpc.ProjectMetrics.getProjects)},
      {header: 'Path', width: 100, sortable: true, dataIndex: 'path', 
        tooltip:'Directory in Project', 
        filter:{xtype:'textfield',name:'path'}},
      {header: 'Filename', width: 100, sortable: true, dataIndex: 'name', 
        tooltip:'Filename (without path & extension)', 
        filter:{xtype:'textfield',name:'name'}},
      {header: 'File Type', width: 100, sortable: true, dataIndex: 'type', 
        tooltip:'The File Extension', 
        filter: makeSimpleDropDown('type', jsonrpc.ProjectMetrics.getTypes)},
      {header: 'Size in Bytes', width: 100, sortable: true, dataIndex: 'size', 
        tooltip:'Size in bytes'},
      {header: 'Lines of Code', width: 100, sortable: true, dataIndex: 'lines', 
        tooltip:'Lines of Source Code', 
        renderer:function(value){ 
          if (value===0) { // don't show 0 lines
            value="";
          }
          return Ext.grid.ColumnModel.defaultRenderer(value);
        }}
    ],
    viewConfig: {
      emptyText: 'No Data',
      forceFit: false
    },
    el: 'gridContainer',
    enableColumnMove:true,
    title: '',
    width: 700,
    height:450,
    frame: false,
    stripeRows: true,

    bbar: new Ext.PagingToolbar({  // create toolbar at bottom for paging
      pageSize: 50,
      store: store,
      displayInfo: true,
      displayMsg: 'Displaying {0} - {1} of {2}',
      emptyMsg: "Nothing to display"
    }),
    
    // tool bar ar top
    tbar : [{
      text:'Reset Filters',
      tooltip:'Clear All Filters',
      iconCls:'clearfilters',
      handler: function(){grid.clearFilters();}
    }],
    
    iconCls:'icon-grid'
  });

  grid.render();  // render the grid

  // trigger initial data store load
  store.load({params:{start:0, limit:50}}); 
});
