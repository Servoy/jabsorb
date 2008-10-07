if(!window.console)
{
  window.console={};
  window.console.log=function(s)
  {
    document.getElementById("console").appendChild($t(s));
    document.getElementById("console").appendChild($n(br))
  }
}

//from prototype.js
var $A = function(iterable)
{
  var results,i,length;
  if (!iterable)
  {
    return [];
  }
  if (iterable.toArray)
  {
    return iterable.toArray();
  }
  else
  {
    results = [];
    i=0;
    length = iterable.length;
    for (; i < length; i++)
    {
      results.push(iterable[i]);
    }
    return results;
  }
};

//from prototype.js
Function.prototype.bind = function() {
  var __method = this, args = $A(arguments), object = args.shift();
  return function()
  {
  
    __method.apply(object, args.concat($A(arguments)));
    return false;
  };
};

//from prototype.js
Function.prototype.bindAsEventListener = function() {
  var __method = this, args = $A(arguments), object = args.shift();
  return function(event) {
    __method.apply(object, [event || window.event].concat(args));
    return false;
  };
};

/**
 * Shorthand to create a new node. 
 * If it is an input type, give it an
 * @param nodeType Either a string containing the node type to create or
 *                 For input types, an array containing "input" and the type
 *                 which will be set right away.
 * @returns a new node to be inserted into the dom.
 */ 
function $n(nodeType)
{
  if(nodeType.constructor === Array)
  {
    var x = document.createElement(nodeType[0]);
    x.type=nodeType[1];
    return x;
  }
  return document.createElement(nodeType);
}
/**
 * Creates a new text node
 * @param text The node to contain in the text
 * @return A new text node
 */
function $t(text)
{
  return document.createTextNode(text);
}

/**
 * Changes the css for a class
 * @param theClass The css class to change, eg ".className"
 * @param element The element of the class, eg "color"
 * @param value The new value the element should contain, eg "blue"
 * @return Nothing is returned.
 */
function changeCss(theClass,element,value) {
  //Last Updated on May 21, 2008
  //documentation for this script at
  //http://www.shawnolson.net/a/503/altering-css-class-attributes-with-javascript.html
  var cssRules;
  if (document.all) 
  {
   cssRules = 'rules';
  }
  else if (document.getElementById) 
  {
   cssRules = 'cssRules';
  }
  var added = false;
  for (var S = 0; S < document.styleSheets.length; S++)
  {
    for (var R = 0; R < document.styleSheets[S][cssRules].length; R++) 
    {
      if (document.styleSheets[S][cssRules][R].selectorText == theClass) 
      {
        if(document.styleSheets[S][cssRules][R].style[element])
        {
          document.styleSheets[S][cssRules][R].style[element] = value;
          added=true;
          break;
        }
      }
    }
    if(!added)
    {
      if(document.styleSheets[S].insertRule)
      {
        document.styleSheets[S].insertRule(theClass+' { '+element+': '+value+'; }',document.styleSheets[S][cssRules].length);
      } 
      else if (document.styleSheets[S].addRule) 
      {
        document.styleSheets[S].addRule(theClass,element+': '+value+';');
      }
    }
  }
}

/**
 * Removes all elements of the array "name", from an object
 * @param node The node to remove elements from
 * @param name The name of the value to remove all elements from
 * @return Nothing is returned.
 */
function clearChildren(node,name)
{
  if(!node)
  {
    return;
  }
  if(!name)
  {
    return;  
  }
  while(node[name].length>0)
  {
    node.removeChild(node[name][0]);
  }
}

/**
 * The default place to keep elements
 */
var valueKey="__value";
/**
 * Adds an element.
 * @param obj Where to store the element.
 * @param type The type of html element to create (will be created using $n())
 * @param data (Optional) Data to apply to the created element. This is useful to set css values
 * @param parent (Optional) An object in which the value may be stored, using 
 * the DOM appendChild() method. 
 * It will be stored under the valueKey key in parent if it exists, otherwise 
 * it will added directly to the parent.
 *  This mean that parent[valueKey] or parent must have an appendChild() method.
 * @param key (Optional) The key underwhich the element will be stored in obj. 
 * If this does not exist, it will be stored in the valueKey variable
 * @return Not used.
 */
function addElement(obj,type,data,parent,key)
{
  var k;
  var createdObj = $n(type);
  var usedKey;
  if(key)
  {
    usedKey=key;
  }
  else
  {
    usedKey=valueKey;
  }
  obj[usedKey] = createdObj;
  if(data)
  {
    for(k in data)
    {
      if(k=="cellAttributes")
      {
        continue;
      }
      obj[usedKey][k]=data[k];
    }
  }
  if(parent)
  {
    if(parent[valueKey])
    {
      parent[valueKey].appendChild(createdObj);
    }
    else
    {
      parent.appendChild(createdObj);
    }
  }
}
/**
 * Adds a text value to the DOM.
 * @param obj The value to directly store the text node in 
 * @param text The text of the text node to create. 
 * @param parent The parent of the text node which will have the text added in either 
 * parent[valueKey] if it exists or parent directly, using the appendChild() method.
 * @param key The key under which the created value is stored in obj. 
 * @return Not used.
 */
function addText(obj,text,parent,key)
{
  var createdObj = $t(text);
  var usedKey;
  if(key)
  {
    usedKey=key;
  }
  else
  {
    usedKey=valueKey;
  }
  obj[usedKey] = createdObj;
  if(parent)
  {
    if(parent[valueKey])
    {
      parent[valueKey].appendChild(createdObj);
    }
    else
    {
      parent.appendChild(createdObj);
    }
  }
}
/**
 * Adds an element in a table cell. It will store the cell in "cell" in parent.
 * @param obj The element to add the value to
 * @param type The type of element to create
 * @param data (Optional) Data to apply to the created element. This is useful to set css values
 * @param parent The row to add the cell to which this element will be stored in.
 * @param key The key in which the created element is assigned to in object 
 * @return Nothing returned 
 */
function addElementInCell(obj,type,data,parent,key)
{
  addElement(obj,"td",data.cellAttributes,parent,"cell");
  addElement(obj,type,data,obj.cell,key);
}