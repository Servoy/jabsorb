Module("svgpage", "0.0.1", function(thisMod){
    thisMod.ScrollView=Class("ScrollView", function(thisClass, Super){
        thisClass.prototype.init=function(container, content, sliderOffset){
            this.bb = document.createElement("rect");
            this.bb.setAttribute("x",0);
            this.bb.setAttribute("y",0);
            this.bb.setAttribute("width","100%");
            this.bb.setAttribute("height","100%");
            this.bb.setAttribute("fill","none");
            container.appendChild(this.bb);
            this.container = container;
            this.content = content;
            this.containerHeight = 0;
            this.contentHeight = 0;
            this.sliderMax = 0;
            this.sliderOffset = sliderOffset;
            this.slider = document.createElement("rect");
            this.slider.setAttribute("x", "100%");
            this.slider.setAttribute("y", "0");
            this.slider.setAttribute("ry", "5");
            this.slider.setAttribute("rx", "5");
            this.slider.setAttribute("width", "15");
            this.slider.setAttribute("height", "10");
            this.slider.setAttribute("transform", "translate(-15.5," + sliderOffset + ")");
            this.slider.setAttribute("class", "slider");
            this.slider.addEventListener("mousedown", this, false);
            this.container.appendChild(this.slider);
            document.documentElement.addEventListener("SVGResize", this, false);
        }
        
        thisClass.prototype.setSliderPos=function(y){
            y = parseInt(y);
            if(y < 0){
                y=0;
            }
            if(y > this.sliderMax){
                y = this.sliderMax;
            }
            this.slider.setAttribute("y",y );
            this.content.setAttribute("transform", "translate(0," + (- y * (this.contentHeight - this.containerHeight)/this.sliderMax ) + ")");
        }
        
        thisClass.prototype.handleEvent=function(evt){
            if(this[evt.type]){
                this[evt.type](evt);
            }
        }
                    
        thisClass.prototype.SVGResize = function(){
            this.containerHeight = this.bb.getBBox().height - this.sliderOffset;
            this.contentHeight = this.content.getBBox().height + this.content.getBBox().y;
            var sh =  this.containerHeight * this.containerHeight/ this.contentHeight;
            if (sh < 10){
              sh=10;
            }else if(sh > this.containerHeight){
                sh = this.containerHeight;
            }
            this.slider.setAttribute("height", sh);
            this.sliderMax  = this.containerHeight - sh;
            if(this.sliderMax < 0){
                this.sliderMax = 0;
            }
            this.setSliderPos(this.slider.getAttribute("y"));
        }
        
        thisClass.prototype.mousedown=function(evt){
            this.dragOffset = this.slider.getAttribute("y")-evt.clientY;
            document.documentElement.addEventListener("mousemove", this, false);
            document.documentElement.addEventListener("mouseup", this, false);
        }
        
        thisClass.prototype.mouseup=function(evt){
            document.documentElement.removeEventListener("mousemove", this, false);
            document.documentElement.removeEventListener("mouseup", this, false);
        }
        
        thisClass.prototype.mousemove=function(evt){
            y=evt.clientY+this.dragOffset;
            this.setSliderPos(y);
        }
    })
})
