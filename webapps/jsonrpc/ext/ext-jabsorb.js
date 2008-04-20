/**
 * @class Ext.data.JsonRpcProxy
 * @extends Ext.data.DataProxy
 * 
 * An implementation of Ext.data.DataProxy that invokes a json-rpc method call
 * via the json-rpc-java and/or jabsorb library to retrieve the data needed by 
 * the Reader when it's load method is called.
 * 
 * @constructor
 * @param {Function} rpc The jabsorb or json-rpc-java function to call to 
 *                     retrieve the data object which the Reader uses to 
 *                     construct a block of Ext.data.Records.  When invoked,
 *                     it will be passed one json object containing the call 
 *                     parameters used to retrieve and optionally sort the data
 *                     and a second callback function to load the resultant
 *                     data from the result of the rpc call.
 */
Ext.data.JsonRpcProxy = function(rpc) {
    Ext.data.JsonRpcProxy.superclass.constructor.call(this);
    this.rpc = rpc;
};

Ext.extend(Ext.data.JsonRpcProxy, Ext.data.DataProxy, {
    /**
     * Load data from the requested source read the data object into
     * a block of Ext.data.Records using the passed Ext.data.DataReader 
     * implementation and process that block using the passed callback.
     * 
     * @param {Object} params This parameter is not used by the JsonRpcProxy class.
     * @param {Ext.data.DataReader) reader The Reader object which converts the data
     * object into a block of Ext.data.Records.
     * @param {Function} callback The function into which to pass the block of Ext.data.records.
     * The function must be passed <ul>
     * <li>The Record block object</li>
     * <li>The "arg" argument from the load function</li>
     * <li>A boolean success indicator</li>
     * </ul>
     * @param {Object} scope The scope in which to call the callback
     * @param {Object} arg An optional argument which is passed to the callback as its second parameter.
     */
    load : function(params, reader, callback, scope, arg){
        if(this.fireEvent("beforeload", this, params) !== false)
        {
          var  o = {
            params : params || {},
            request: {
                callback : callback,
                scope : scope,
                arg : arg
            },
            reader: reader,
            callback : this.loadResponse,
            scope: this
          };

          this.rpc(function(r,e){o.callback.call(o.scope,o,r&&!e, r||e);}, o.params);
        }
        else
        {
          callback.call(scope||this, null, arg, false);
        }
    },

    // private
    loadResponse : function(o, success, response){
        if(!success){
            this.fireEvent("loadexception", this, o, response);
            o.request.callback.call(o.request.scope, null, o.request.arg, false);
            return;
        }
        var result;
        try {
            result = o.reader.readRecords(response);
        }catch(e){
            this.fireEvent("loadexception", this, o, response, e);
            o.request.callback.call(o.request.scope, null, o.request.arg, false);
            return;
        }
        this.fireEvent("load", this, o, o.request.arg);
        o.request.callback.call(o.request.scope, result, o.request.arg, true);
    },

    // private
    update : function(dataSet){
    },

    // private
    updateResponse : function(dataSet){
    }
});

