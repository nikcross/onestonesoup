    var OpenForum = new function(){
		var self = this;
		var objects= [];
		var tables = [];
		var tabs = [];
		var cards = [];
		var nextId = 0;
		var hash;

		self.getRoot = function() {
			var root = document.location.toString();
			root = root.substring( root.indexOf("://")+3 );
			root = root.substring( root.indexOf("/")+1 );
			root = "/"+root.substring( 0,root.indexOf("/") );
			return root;
		}

		self.addCard = function(hash,target,htmlUri,scriptUri) {
			cards[hash] = {target: target,htmlUri: htmlUri,scriptUri: scriptUri};
		}

		self.addTable = function(table) {
			tables[table.id]=table;
		};

		self.getTable = function(ofId) {
			return tables[ofId];
		};

		self.addTab = function(tab) {
			tabs[tab.id]=tab;
		};

		self.getTab = function(ofId) {
			return tabs[ofId];
		};

		self.getNextId = function() {
			nextId++;
			return nextId;
		};

		self.getObject= function(id) {
			if(objects[id]==undefined) {
				objects[id] = new OpenForumObject(id);
			}
			return objects[id];
		};

		self.scan = function() {
			if(self.hash != window.location.hash) {
				self.hash = window.location.hash;
				self._onHash(self.hash);
			}
			for(var tabIndex in tabs) {
				tabs[tabIndex].refresh();
			}
			for(var tableIndex in tables) {
				tables[tableIndex].refresh();
			}
			for(var objectIndex in objects) {
				object = objects[objectIndex];
				if(typeof(object)=="undefined") {
				} else {
					object.scan();
				}
			}
		}

		self.crawl = function (node) {
			self.crawlTabs(node);
			self.crawlTables(node);
			self.crawlParts(node);
		}

		self.crawlParts = function (node,prefix) {
			if(typeof(prefix)=="undefined") {
				prefix="";
			}

			if(node.attributes && node.attributes['of-id']) {
				var nodeName = node.attributes['of-id'].nodeValue;
				if(prefix.length>0) {
					nodeName = prefix+"."+nodeName;
				}

				var object = OpenForum.getObject(nodeName).add( node );
				objects[objects.length]=object;
			}
			if( typeof(node.innerHTML)!="undefined" && node.innerHTML.indexOf("{{")!=-1) {
				self.parseParts(node,objects,prefix);
			}

			for(var nodeIndex in node.childNodes) {
				var childNode = node.childNodes[nodeIndex];

				self.crawlParts(childNode,prefix);

				if(childNode.id && childNode.id.indexOf("OFTable")==0) {
					self.getTable(childNode.id).setTableNode(childNode);
				}

				if(childNode.id && childNode.id.indexOf("OFTabs")==0) {
					self.getTab(childNode.id).setTabNode(childNode);
				}
			}
			return objects;
		}

		self.crawlTables = function (node) {
			for(var nodeIndex in node.childNodes) {
				var childNode = node.childNodes[nodeIndex];
				self.crawlTables(childNode);
				if(childNode.attributes && childNode.attributes['of-repeatFor']) {
					self.addTable( new OpenForumTable(childNode) );
				}
			}
		}

		self.crawlTabs = function (node) {
			for(var nodeIndex in node.childNodes) {
				var childNode = node.childNodes[nodeIndex];
				self.crawlTabs(childNode);
				if(childNode.attributes && childNode.attributes['of-tabFor']) {
					self.addTab( new OpenForumTab(childNode) );
				}
			}
		}

		self.parseParts= function(node,objects,prefix) {
			var data = node.innerHTML;

			var spans = [];
			while(data.indexOf("{{")!=-1) {
				name = data.substring(data.indexOf("{{")+2,data.indexOf("}}"));

				data = data.substring(0,data.indexOf("{{"))+
				"<span id='OpenForumId"+nextId+"'>OpenForumId"+nextId+"</span>"+
				data.substring(data.indexOf("}}")+2);
				spans[spans.length] = {id: 'OpenForumId'+nextId,name: name};

				nextId++;
			}
			node.innerHTML = data;

			for(var spanIndex in spans) {
				var span = spans[spanIndex];
				var object = self.getObject( span.name );
				object.add( document.getElementById(span.id) );
				objects[objects.length]=object;
			}
		}

		self._onHash= function(hash) {
			hash = hash.substring(1);
			if(hash=="") {
				hash="home";
			}
			if(cards[hash]) {
				var source = Ajax.doGet(cards[hash].htmlUri);
				self.loadScript(cards[hash].scriptUri);
				document.getElementById(cards[hash].target).innerHTML = source;
				self.crawl(document.body);
			}
			self.onHash(hash);
		};
		self.onHash= function(hash) {};
		self.init= function() {};
        self.close= function() {};

		this.onload= function(next) {
            objects= [];
			tables = [];
			tabs = [];
			nextId = 0;

			this.crawl(document.body);
			this.createData();
			this.hash = "";
			this.scan();
    		this.init();
			this.interval = setInterval(this.scan,500,500);
		}

        this.onunload= function() {
            this.close();
        }

		this.getObjects = function() {
			return objects;
		} 

		this.listData = function() {
			var list = [];
			for(var objectIndex in objects) {
				var object = objects[objectIndex];
				if(typeof(object)=="undefined") {
					continue;
				}
				if(object.getId()) {
					list[list.length]=object.getId();
				}
			}	
			return list;
		}

		this.createData = function() {
			for(var objectIndex in objects) {
				var object = objects[objectIndex];
				if(typeof(object)=="undefined") {
					continue;
				}
				this.createParents(object.getId());
				if( eval("typeof("+object.getId()+")")=="undefined" ) {
					eval(object.getId()+"=\"\";");
					object.setValue("");
				}
			}
		}

		this.createParents = function(id)  {
			if(id.indexOf(".")==-1) {
				return;
			}
			var parts = id.split(".");
			var id = "";
			for(var index=0;index<parts.length-1;index++) {
				if(id.length>0) {
					id+=".";
				}
				id+=parts[index];

				if( eval("typeof("+id+")")=="undefined" ) {
					eval(id+"={};");
				}
			}
		}

		this.createObjectSignature = function(object,signature,depth) {
			var signature = "";

			if(typeof(object)!="object") {
		    	signature+="("+typeof(object)+")"+object;
			}

			if( typeof(depth)=="undefined" ) {
		    	var depth=0;
		    }
			depth++;
		    if(depth>10) {
		    	return;
		     }

			for(var index in object) {
		    	var part = object[index];
		        if(typeof(part)!="object") {
		          signature+=index+"="+"("+typeof(part)+")"+part+",";
		     	  continue;
		        } else {
		        	signature+=index+"{";
		        	signature+=this.createObjectSignature(part,signature,depth);
		            signature+="},";
		        }
		    }
		    depth--;
		    return signature;
		}

		this.childCount = function(object) {
			var count=0;

			for(var index in object) {
		    	count++;
		    }
		    return count;
		}

		this.loadScripts = function(scriptURLs,callback) {
			var scripts="&callback="+callback;
			var index=1;
			for(scriptIndex in scriptURLs) {
				scripts+="&script"+index+"="+scriptURLs[scriptIndex];
				index++;
			}
		   var fileref = document.createElement("script");
		   fileref.setAttribute("src",self.getRoot()+"/OpenForum/Javascripts?action=getScripts"+scripts);
		   fileref.setAttribute("type","text/javascript");
		   document.getElementsByTagName("head")[0].appendChild(fileref);
		}

		this.loadScript = function(scriptURL) {
		   var fileref = document.createElement("script");
		   fileref.setAttribute("src",scriptURL);
		   fileref.setAttribute("type","text/javascript");
		   document.getElementsByTagName("head")[0].appendChild(fileref);
		}

		this.loadCSS = function(cssURL) {
			var fileref = document.createElement("link");
  			fileref.setAttribute("rel", "stylesheet");
 			fileref.setAttribute("type", "text/css");
			fileref.setAttribute("href", cssURL);
			document.getElementsByTagName("head")[0].appendChild(fileref);
		}
	}

        if( typeof(JSON)=="undefined" ) {
        	JSON = new function() {};
        }

		JSON.get = function( page,action,parameters ) {
          var request = {method: 'GET',page: page,action: action,parameters: parameters, onSuccess: JSON.onSuccess, onError: JSON.onError, go: JSON.go};
			return request;
		}

       JSON.post = function( post,action,parameters ) {
          var request = {method: 'POST',page: page,action: action,parameters: parameters, onSuccess: JSON.onSuccess, onError: JSON.onError, go: JSON.go};
			return request;
		}
		JSON.onSuccess = function(onSuccess) {
			this.onSuccess = function(data) {
              var object = eval("({"+data+"})").response;
				onSuccess(object);
			}
			return this;
		}
		JSON.onError = function(onError) {
			this.onError = function(error) {
				onError(error);
			}
			return this;
		}
		JSON.go = function() {
			var request = "action="+this.action;
          if(this.method=="GET") {
			if(this.parameters && this.parameters.length>0) {
				request+="&"+this.parameters;
                Ajax.sendRequest( new AjaxRequest(this.method,this.page,request,"",this.onSuccess,this.onError,true) );
			}
          } else {
		    Ajax.sendRequest( new AjaxRequest(this.method,this.page,"",this.parameters,this.onSuccess,this.onError,true) );
          }
		}

	function OpenForumTab(node) {
		this.id="OFTabs"+OpenForum.getNextId();
		this.rowNode = node;
		this.tabNode = node.parentNode;
		this.rowHTML = node.parentNode.innerHTML;
		this.initFunction = node.attributes['of-init'].nodeValue;
		this.changedFunction = node.attributes['of-onChange'].nodeValue;
		this.tabFor = node.attributes['of-tabFor'].nodeValue;
		this.target = this.tabFor.substring(this.tabFor.indexOf(" in ")+4);
		this.element = this.tabFor.substring(0,this.tabFor.indexOf(" in "));
		this.targetObject = OpenForum.getObject(this.target);
		this.targetChildCount = OpenForum.childCount( this.targetObject.getValue() );
		this.targetObjectSignature = OpenForum.createObjectSignature( this.targetObject.getValue() );

		this.tabNode.innerHTML = "";
		this.tabNode.id=this.id;

		this.setTabNode = function(newTabNode) {
			this.tabNode = newTabNode;
		}

		this.refresh = function() {
			var collection = this.targetObject.getValue();

			//check if changed
			var objectSignature = OpenForum.createObjectSignature( this.targetObject.getValue() );
			if(objectSignature!=this.targetObjectSignature) {
				this.targetObjectSignature=objectSignature;
				for( var elementIndex in collection ) {
					eval( this.changedFunction+"("+elementIndex+")" );
				}
			}

			//check if count changed
			var childCount = OpenForum.childCount( this.targetObject.getValue() );
			if(childCount==this.targetChildCount) {
				return;
			}
			this.targetChildCount=childCount;

			var tabData = "";
			for( var elementIndex in collection ) {
				var item = collection[elementIndex];
				eval("var "+this.element+"=item;");

				var data = this.rowHTML;

				while(data.indexOf("{{")!=-1) {
					name = data.substring(data.indexOf("{{")+2,data.indexOf("}}"));
					data = data.substring(0,data.indexOf("{{"))+
					eval(name)+
					data.substring(data.indexOf("}}")+2);
				}
				tabData += data;
			}
			this.tabNode.innerHTML=tabData;

			//initialise tabs
			for( var elementIndex in collection ) {
				eval( this.initFunction+"("+elementIndex+")" );
			}
		}
	}

	function OpenForumTable(node) {
		this.id="OFTable"+OpenForum.getNextId();
		this.rowNode = node;
		this.tableNode = node.parentNode;

        node.parentNode.removeChild(node);

        var temp = document.createElement("table");
        temp.appendChild(node);

		this.rowHTML = temp.innerHTML;
		this.repeatFor = node.attributes['of-repeatFor'].nodeValue;
		this.target = this.repeatFor.substring(this.repeatFor.indexOf(" in ")+4);
		this.element = this.repeatFor.substring(0,this.repeatFor.indexOf(" in "));
		this.targetObject = OpenForum.getObject(this.target);
		this.targetObjectSignature = OpenForum.createObjectSignature( this.targetObject.getValue() );

		this.tableTop = this.tableNode.innerHTML;
		this.tableNode.id=this.id;

		this.setTableNode = function(newTableNode) {
			this.tableNode = newTableNode;
		}

		this.refresh = function() {
			//check if changed
			var objectSignature = OpenForum.createObjectSignature( this.targetObject.getValue() );
			if(objectSignature==this.targetObjectSignature) {
				return;
			}
			this.targetObjectSignature=objectSignature;

			var tableData = this.tableTop;
			var collection = this.targetObject.getValue();
			for( var elementIndex in collection ) {
				var item = collection[elementIndex];
				eval("var "+this.element+"=item;");

				var data = this.rowHTML;
				while(data.indexOf("{{")!=-1) {
					name = data.substring(data.indexOf("{{")+2,data.indexOf("}}"));
					data = data.substring(0,data.indexOf("{{"))+
					eval(name)+
					data.substring(data.indexOf("}}")+2);
				}
				tableData += data;
			}
			this.tableNode.innerHTML=tableData;
		}
	}

	function OpenForumObject(objectId) {
		var id = objectId;
		var value = "";
		this.targets=[];
		this.listeners=[];

        this.getId = function() {
            return id;
        }

		this.add = function(target) {
			this.targets[this.targets.length]=target;
		}

		this.setValue = function(newValue,exclude) {
			if(newValue===value) {
				return;
			}
			value = newValue;
			for(var targetIndex in this.targets) {
				var target = this.targets[targetIndex];
				if(target==null) {
					continue;
				}
				if(exclude && exclude===target) {
					continue;
				}
                if(typeof(target.type)!="undefined" && target.type=="checkbox") {
            		target.checked = value;
				} else if(typeof(target.value)!="undefined") {
    				target.value = value;
				} else if(target.innerHTML) {
					if(value==="") {
						target.innerHTML = " "; 
					} else {
						target.innerHTML = ""+value;
					}
				}
			}
		}

		this.getValue = function() {
			return value;
		}

		this.scan = function() {
			for(var targetIndex in this.targets) {
				var target = this.targets[targetIndex];
				if(target==null) {
					continue;
				}
				
               if(typeof(target.type)!="undefined" && target.type=="checkbox") {
        			if(target.checked!==value) {

                      this.setValue(target.checked,target);
                      eval(this.getId()+"=value;");
                      this.notifyListeners();
                      return;
                  }
				} else if(typeof(target.value)!="undefined") {
                  if(target.value!=value) {
                      this.setValue(target.value,target);
                      eval(this.getId()+"=value;");
                      this.notifyListeners();
                      return;
                  }
              }
			}
			var testId = this.getId();
			if( eval("typeof("+testId+")")!="undefined") {
				if( value!=eval(testId)) {
					this.setValue(eval(testId));
					this.notifyListeners();
				}
			} else {
				eval(testId+"=value;")
			}

		}

		this.addListener = function(listener) {
			this.listeners.push(listener);
		}

		this.notifyListeners = function() {
			for(var listenerIndex in this.listeners) {
				listener = this.listeners[listenerIndex];
				listener( this );
			}
		}

		this.getId = function() {
			return id;
		}

		this.getTargets = function() {
			return this.targets;
		}
	}	

	onload = function() {
			OpenForum.onload();
		}

    onunload = function() {
			OpenForum.onunload();
		}        


requestCount = 0;
function AjaxRequest(method,url,request,data,onSuccess,onError,asynchronous)
{
  var self=this;
  self.id = "request_"+requestCount;
  requestCount++;
  eval( self.id+"=this;" );

  self.method = method;
  self.url = url;
  self.request = request;
  self.data = data;
  self.onSuccess = onSuccess;
  self.onError = onError;
  self.asynchronous = asynchronous;
  self.transaction = null;

  this.processTransactionStateChange = function processTransactionStateChange(ev) {
    if (self.transaction.readyState == 4) {
      if (self.transaction.status == 200) {
          onSuccess(self.transaction.responseText);
      } else if (self.transaction.status == 0) {
      } else {
        onError( self.transaction.status,self.transaction.statusText );
      }
      eval( self.id+"=null;" );
    }
  }
}

Ajax = new function()
{
  this.postRequest = function postRequest( request )
  {
    request.asynchronous = true;
    this.sendRequest(request);
  }

  this.sendRequest = function sendRequest(request) {

    request.transaction = false;

    if(window.XMLHttpRequest)
    {
      try {
        request.transaction = new XMLHttpRequest();
      }
      catch(e)
      {
        alert(e);
        request.transaction = false;
      }
    }
    else if(window.ActiveXObject)
    {
      try {
        request.transaction = new ActiveXObject("Msxml2.XMLHTTP");
      }
      catch(e)
      {
        alert(e);
        try {
          request.transaction = new ActiveXObject("Microsoft.XMLHTTP");
        }
        catch(e)
        {
          alert(e);
          request.transaction = false;
        }
      }
    }
    if(request.transaction)
    {
      if(request.asynchronous == true)
      {
        var fn = eval(request.id+".processTransactionStateChange");
        request.transaction.onreadystatechange= function(ev){ fn(ev); };
        if(request.request!=null && request.request.length>0) {
          request.transaction.open(request.method, request.url+"?"+request.request,true);
    	  request.transaction.setRequestHeader("Content-Type","application/x-www-form-urlencoded");
          request.transaction.setRequestHeader("If-Modified-Since", new Date(0));
          request.transaction.send(request.data);
    	} else {
          request.transaction.open(request.method, request.url,true);
    	  request.transaction.setRequestHeader("Content-Type","application/x-www-form-urlencoded");
    	  request.transaction.setRequestHeader("If-Modified-Since", new Date(0));
       	  request.transaction.send(request.data);
        }
      }
      else
      {
        if(request.request!=null && request.request.length>0) {
          request.transaction.open(request.method, request.url+"?"+request.request,false);
    	  request.transaction.setRequestHeader("Content-Type","application/x-www-form-urlencoded");
        } else {
          request.transaction.open(request.method, request.url,false);
    	  request.transaction.setRequestHeader("Content-Type","application/x-www-form-urlencoded");
        }
        request.transaction.setRequestHeader("If-Modified-Since", new Date(0));
        request.transaction.send(request.data);
        this.currentRequest=null
        return encodeURIComponent(request.transaction.responseText);
      }
    }
    else
    {
      alert("failed");
    }
  }

  this.doGet = function(pageName,request,async,onSuccess,onError)
  {
    if(typeof(async)=="undefined") async=false;
    return decodeURIComponent(this.sendRequest( new AjaxRequest("GET",pageName,request,"",onSuccess,onError,async) ));
  }

  this.doDelete = function(pageName,request,async,onSuccess,onError)
  {
    if(typeof(async)=="undefined") async=false;

    return decodeURIComponent(this.sendRequest( new AjaxRequest("DELETE",pageName,request,"",onSuccess,onError,async) ));
  }

  this.processGet = function (pageName,request,onSuccess,onError)
  {
    this.postRequest( new AjaxRequest("GET",pageName,request,"",onSuccess,onError,true) );
  }

  this.doPost = function (pageName,post,async,onSuccess,onError)
  {
    if(typeof(async)=="undefined") async=false;

	dataArray = post.data
    this.data="";
    for(this.loop=0;this.loop<dataArray.length;this.loop++)
    {
      if(this.loop!=0)
      {
        this.data += "&";
      }
      this.data += dataArray[this.loop][0]+"="+encodeURIComponent(dataArray[this.loop][1]);
    }

    return decodeURIComponent(this.sendRequest( new AjaxRequest("POST",pageName,"",this.data,onSuccess,onError,async) ));
  }

  this.doPut = function (pageName,dataArray,async,onSuccess,onError)
  {
    if(typeof(async)=="undefined") async=false;

    this.data="";
    for(this.loop=0;this.loop<dataArray.length;this.loop++)
    {
      if(this.loop!=0)
      {
        this.data += "&";
      }
      this.data += dataArray[this.loop][0]+"="+encodeURIComponent(dataArray[this.loop][1]);
    }

    return decodeURIComponent(this.sendRequest( new AjaxRequest("PUT",pageName,"",this.data,onSuccess,onError,async) ));
  }
}

function Post()
{
  this.data = new Array();

  this.addItem = function(name,value)
  {
    this.item = new Array();
    this.data[this.data.length] = this.item;
    this.item[0] = name;
    this.item[1] = value;
  }

  this.addForm = function(formId) {
    form = document.getElementById(formId);
    for(var loop=0;loop<form.elements.length;loop++) {
    	name=form.elements[loop].name;
    	if(name.length>0) {
	    	this.addItem(name,form.elements[loop].value);
	    }
	  }
  }
}
