Module("svgnet", "0.0.1", function(thisMod){
    ///a not so complete EventTarget class.
    var EventTarget =Class("EventTarget", function(thisClass, Super){
        thisClass.prototype.init=function(){
            this._listeners={};
        }
        
        thisClass.prototype.dispatchEvent = function(evt){
            if(this._listeners[evt.type]){
                var l = this._listeners[evt.type];
                for(var i=0;i<l.length;i++){
                    l[i].handleEvent(evt);
                }
            }
        }
        
        thisClass.prototype.addEventListener=function(evtType, listener){
            if(this._listeners[evtType]){
                this._listeners[evtType].push(listener);
            }else{
                this._listeners[evtType] = [listener];
            }
        }
    })
    
    //---------------------------------events------------------------------------------
    var ConnectionData = Class("ConnectionData", function(thisClass, Super){
        thisClass.prototype.init=function(data){
            this.type = "connectionData";
            this.data = data;
        }
        thisClass.prototype.data="";
    })
     
    var ConnectionConnected = Class("ConnectionConnected", function(thisClass, Super){
        thisClass.prototype.init=function(){
            this.type = "connectionConnected";
        }
    })
    var ConnectionDisonnected = Class("ConnectionDisconnected", function(thisClass, Super){
        thisClass.prototype.init=function(){
            this.type = "connectionDisconnected";
        }
    })
    
    var ConnectionClosed = Class("ConnectionClose", function(thisClass, Super){
        thisClass.prototype.init=function(){
            this.type = "connectionClosed";
        }
    })
    
    /**
        Connection Class.
        This is implemented using  Java Socket and a timer which checks constantly for new data.
        If data is available a ConnectionData event is dipatched.
    */
    thisMod.Connection=Class("Connection", EventTarget, function(thisClass, Super){
        thisClass.prototype.connect=function(url){
            var hp = url.split(":");
            var host = hp[0];
            var port = hp[1];
            this._s = new java.net.Socket(host,port);
            this._w = this._s.getOutputStream();
            this._r = this._s.getInputStream(); 
            
            var self = this;//timer hack
            this.updateTimer=setInterval(function(){
                self.__timer();
            },1);
        }
        
        thisClass.prototype.send=function(data){
            //socket streams want a byte array
            var buff = new Array(data.length);
            for(var i=0;i<data.length;i++){
                buff[i] = data.charCodeAt(i);
            }
            this._w.write(buff);
        }
        
        thisClass.prototype.close=function(data){
            this._s.close();
        }
        
        thisClass.prototype.__timer=function(){
            //do some state updating
            if(this._s.isClosed()){
                clearInterval(this.updateTimer);
                if(this.connected == true){
                    this.connected=false;
                    this.dispatchEvent(new ConnectionDisconnected(s));
                }
                this.dispatchEvent(new ConnectionClosed());
            }else if(this._s.isConnected()){
                if(this.connected == false){
                    this.connected = true;
                    this.dispatchEvent(new ConnectionConnected());
                }
                //read data from the socket
                var s="";
                var l = this._r.available();
                for(var i=0;i<l;i++){
                    s+= String.fromCharCode(this._r.read());
                }
                if(s.length > 0){
                    this.dispatchEvent(new ConnectionData(s));
                }
            }else{
                if(this.connected == true){
                    this.connected=false;
                    this.dispatchEvent(new ConnectionDisconnected(s));
                }
            }
        }
        //not used yet
        thisClass.prototype.setEncoding=function(enc){
        }
        
        thisClass.prototype.connected=false;
    })
    
    /**
        Global function to create a connection.
    */
    createConnection = function(){
        return new thisMod.Connection();
    }
})
