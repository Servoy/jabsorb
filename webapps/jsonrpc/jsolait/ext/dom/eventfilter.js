Module("dom.eventfilter", "0.0.1", function(thisMod){
    var lang = importModule("lang");
    var dom = importModule("dom");
    
    thisMod.EventFilter=Class("EventFilter", dom.EventTarget, function(thisClass, Super){
        thisClass.prototype.init=function(){
            Super.prototype.init.call(this);
            this._obj={};
            this._match=function(){ return true};
        }
        /**
            Sets the event filter. This is very similar to an SQL statement :).
            All events where the statement is true are dispatched all others are discarded.
            the event object is accessed using the 'evt' identifier all other names in the statement 
            reffer to objects registered with addObject or are keywords: 'AND' or 'OR'.
            Operators and Strings can be used, as well. 
            example: 
               "evt.clientX > 20 OR evt.target=myrect"
               
               will match all events where evt.clientX is larger than 20 and
               all where the target is the object that was added with filter.addObject("myrect", someSVGElement);
        */
        thisClass.prototype.setFilter=function(filter){
            var t= new lang.Tokenizer(filter); //analyze the statement
            var fn="";
            var innme=false;
            while(!t.finished()){
                var n = t.next();
                switch(n[0]){
                    case lang.tokens.OP: //operater token
                        innme=false;
                        if(n[1] == "="){ //need to be  == for comparison
                            fn+="==";
                        }else if(n[1] == "."){//child object access needs to be remembered so 
                            fn+=".";           //names are not translated
                            innme=true;
                        }else{
                            fn+=n[1];
                        }
                        break;
                    case lang.tokens.NAME:
                        switch(n[1]){
                            case "AND": //AND OR translation
                                fn+="&&";
                                break;
                            case "OR":
                                fn+="||";
                                break;
                            default:
                                if(innme || n[1] == "evt"){ //evt name stays 
                                    fn+=n[1];
                                }else{ //all other names are translated to use the objects registered with the filter
                                    fn+='objs["%s"]'.format(n[1]);
                                }
                        }
                        break;
                    default:
                        fn+=n[1];
                }
            }
            //create  a script which is used for matching
            fn = "fn = function(evt, objs){\n  return (%s);\n}".format(fn);
            //create a function out of the script generated
            eval(fn);
            this._match = fn;
        }
        
        thisClass.prototype.addObject=function(name, obj){
            this._obj[name] = obj;
        }
        
        thisClass.prototype.handleEvent=function(evt){
            //only matching events are dispatched
            if(this._match(evt, this._obj)){
                this.dispatchEvent(evt);
            }
        }
    })
})
