Module("dom", "0.0.2", function(thisMod){
    
    thisMod.EventTarget =Class("EventTarget", function(thisClass, Super){
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
        
        thisClass.prototype.addEventListener=function(evtType, listener, useCapture){
            if(this._listeners[evtType]){
                this._listeners[evtType].push(listener);
            }else{
                this._listeners[evtType] = [listener];
            }
        }
    }) 
    
    thisMod.EventListener=Class("EventListener", function(thisClass, Super){
        thisClass.prototype.handleEvent=function(evt){
            if(this[evt.type]){
                this[evt.type](evt);
            }
        }
    })
})
