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

Module("testing", "0.0.1", function(thisMod){
    
    /**
        Returns the average time used for executing a function.
        @param repeat   How often the function should be executed.
        @param fn         The function to execute.
        @param ...         The rest of the parameters are sent to the function as arguments.
    */
    thisMod.timeExec=function(repeat, fn){
        var args = [];
        for(var i=2;i<arguments.length;i++){
            args.push(arguments[i]);
        }
        var t=(new Date()).getTime();
        for(var i=0;i<=repeat;i++){
            fn.apply(null, args);    
        }
        return ((new Date()).getTime()-t) / (repeat+1);
    }
})
