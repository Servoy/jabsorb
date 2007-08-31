/*
  Copyright (c) 2003 Jan-Klaas Kollhof
  
  This file is part of the JavaScript o lait library(jsolait).
  
  jsolait is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; either version 2.1 of the License, or
  (at your option) any later version.
 
  This software is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this software; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/ 

/**
    SVGTimer implementation as proposed by the W3C SVG1.2 working draft.
    It uses setTimeout and handles the timouts calling the timer registered for that timeout.
    The timer in turn calls all handlers.
*/
Module("svgtimer", "0.0.1", function(thisMod){
    var timers=[];  //stores all active timers.
    /**
        Removes a timer from the timer array.
        This is for module internal use.
    */
    var removeTimer=function(timer){
        delete timers[timer.__id];
    }
    /**
        Adds a timer from the timer array.
        This is for module internal use.
    */
    var addTimer=function(timer){
        var id=0;
        while(timers["t" + id] != null){
            id++;
        }
        id = "t" + id;
        timer.__id = id;
        timers[id] = timer;
        return timer.__id;
    }
    /**
        Handles setTimeout events and notifies the required timers.
        This is for module internal use.
    */
    __handleTimeout=function(id){
        timers[id].__ontime();
    }
    
    /**
        SVGTimer implementation.
    */
    var SVGTimer = Class("SVGTimer", function(thisClass, Super){
        thisClass.prototype.delay=-1;
        thisClass.prototype.interval=-1; 
        
        thisClass.prototype.init=function(delay, interval){
            this.delay = delay;
            this.interval = interval;
            this.__handlers = [];
            this.__timeout = null;
        }
        /**
            Starts a timer.
        */
        thisClass.prototype.start=function(){
            var id = addTimer(this);
            this.__timeout = setTimeout("__handleTimeout('" + id + "')", this.delay);
        }
        /**
            Stops a timer.
        */
        thisClass.prototype.stop=function(){
            removeTimer(this);
            clearTimeout(this.__timeout);
            this.__timeout = null;
        }
        /**
            Adds a handler which will be invoked upon a timer event.
            @param handler   An SVGRunnable object(object with a run method) or a function object.
            Note: A function as a handler is not part of the specs!
        */
        thisClass.prototype.addHandler=function(handler){
            this.__handlers.push(handler);
            return handler;
        }
        /**
            Removes a handler.
            @param handler   The handler to remove.
        */
        thisClass.prototype.removeHandler=function(handler){
            for(var i=0;i<this.__handlers.length;i++){
                if(this.__handlers[i] == handler){
                    this.__handlers.splice(i,1);
                    return handler;
                }
            }
        }
        /**
            Invoked by the module internal timout handler.
            It will call the handlers in order of registration.
        */
        thisClass.prototype.__ontime=function(){
            if(this.interval > -1){
                this.__timeout = setTimeout("__handleTimeout('" + this.__id + "')", this.interval);
            }else{
                removeTimer(this);
            }
            
            for(var i=0;i<this.__handlers.length;i++){
                if(this.__handlers[i].run){
                    this.__handlers[i].run();
                }else{
                    if(typeof this.__handlers[i] == "function"){
                        this.__handlers[i]();
                    }
                }
            }
        }
    })

    /**
        Creates an SVGTimer and starts it.
        @param timeout  The delay after which the timer should be invoked.
    */
    createAndStartTimer=function(timeout){
        var t = new SVGTimer(timeout, -1);
        t.start();
        return t;
    }
    
    /**
        Creates an SVGTimer and starts it if start is true.
        @param timeout  The delay after which the timer should be invoked.
        @param start      True if the timer should be started.
    */
    createTimer=function(timeout, start){
        var t = new SVGTimer(timeout, -1);
        if(start){
            t.start();
        }
        return t;
    }

    /**
        Creates an SVGTimer with an interval and starts it.
        @param timeout  The interval after which the timer should be invoked and reinvoked.
    */
    createAndStartInterval=function(timeout){
        var t = new SVGTimer(timeout, timeout);
        t.start();
        return t;
    }

    /**
        Creates an SVGTimer with an interval and starts it if start is true.
        @param timeout  The interval after which the timer should be invoked and reinvoked again.
        @param start      True if the timer should be started.
    */
    createInterval=function(timeout, start){
        var t = new SVGTimer(timeout, timeout);
        if(start){
            t.start();
        }
        return t;
    }
})

