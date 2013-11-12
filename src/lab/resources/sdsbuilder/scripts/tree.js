var NextTreeNodeIndex = 0;
var TreeNodes = [];

function Tree(elementId,name,attributes) {
    var root = new TreeNode(name,attributes);
    var elementId = elementId;

    this.render = function() {
        var element = document.getElementById(elementId);
        element.innerHTML = root.render(0);
    }
    this.addChild = function(name,attributes) {
        return root.addChild(name,attributes);
    }
    this.addJSON = function(node) {
        return root.addJSON(node);
    }
    this.render();

    this.expandAll = function() {

    }
    this.getNode = function(path) {

    }
}

var NextActionId=0;
var Actions = [];
function Action(newFn,newIcon) {
    var fn=newFn;
    var icon=newIcon;
    var id = "ActionId"+NextActionId;
    NextActionId++;
    Actions[id]=this;

    this.call = function() {
        fn();
    }
    this.render = function()
    {
        data="&nbsp;<a href='#' onClick='Actions[\""+id+"\"].call();return false;'><i class='icon-"+icon+"'></i></a>";
        alert(data);
        return data;
    };
}

function TreeNode(name,attributes) {
    var that = this;
    var id = "TreeNode"+NextTreeNodeIndex;
    NextTreeNodeIndex++;
    TreeNodes[id] = this;
    var children = [];
    var name=name;
    var attributes = attributes;
    var expanded = false;
    var SPACE = "&nbsp;&nbsp;&nbsp;&nbsp;";
    var localDepth = 0;

    this.addChild = function(id,title,attributes) {
        var newChild = new TreeNode(id,title,attributes);
        children[children.length] = newChild;
        return newChild;
    }

    this.addJSON = function(node) {
        var child = this.addChild( node.name,node.attributes );
        if(node.leaves) {
            for(var i in node.leaves) {
                child.addJSON( node.leaves[i] );
            }
        }
    }
    this.expand = function() {
        expanded=true;
        paint();
    }
    this.collapse = function() {
        expanded=false;
        paint();
    }
    this.toggle = function() {
        expanded=!expand;
        paint();
    }
    var paint = function() {
        document.getElementById(id).innerHTML = that.render(localDepth);
    }

    this.render = function(depth) {
        if(!depth) {
            depth=0;
        }
        localDepth = depth;
        var data = "";
        data+="<span id='"+id+"'>";
        for(var count=0;count<depth;count++) {
            data+=SPACE;
        }
        if(children.length>0) {
            if(expanded==false) {
                data+="<a href='#' onClick='TreeNodes[\""+id+"\"].expand();return false;'><i class='icon-plus'></i></a>";
            } else {
                data+="<a href='#' onClick='TreeNodes[\""+id+"\"].collapse();return false;'><i class='icon-minus'></i></a>";
            }
        } else {
            data+="&nbsp;";
        }
        data+=name;
        if(attributes && attributes.actions) {
            for(actionIndex in attributes.actions) {
                data+=attributes.actions[actionIndex].render();
            }
        }

        data+="<br/>";

        if(expanded==true) {
            for(childIndex in children) {
                data+=children[childIndex].render(depth+1);
            }
        }
        data+="</span>";

        return data;
    }

    this.addNode = function(node) {

    }
    this.removeNode = function(node) {

    }
    this.getChild = function(name) {

    }
}