var cy = cytoscape({
    container: document.getElementById('cytoscape'), // container to render in
    style: [{
        selector: 'node',
        css: { 'content': 'data(name)' } //show label with "name" value
    }],
    //pixelRatio: 0.7,
});



function init() {
    addBridgeForAndroidWebView();
    console.log('initCore')
    const bridge = window.WebViewJavascriptBridge
    console.log(bridge)
    //    bridge.registerHandler('addTest', function(data, responseCallback) {
    //                        cy.add({
    //                             group: 'nodes',
    //                             data: { weight: 75 },
    //                             position: { x: 200, y: 200 }
    //                         });
    //                        //JS gets the data and returns it to the native
    //                        responseCallback(result)
    //                    });
    bridge.registerHandler('add',
        //https://js.cytoscape.org/#cy.add
        function (data) {
            cy.add(data)
        }
    );
    bridge.registerHandler('remove',
        function (data, callback) {
            var elem = cy.$('#' + data.id)
            if (elem == undefined || elem == null || elem.length == 0) {
                if (callback != undefined && callback !== null) {
                    callback(false)
                }
            } else {
                cy.remove(elem)
                if (callback != undefined && callback !== null) {
                    callback(true)
                }
            }
        }
    );
    bridge.registerHandler('filterNode',
        function (data, callback) {
            var elems = cy.nodes().filter(data.param)
            if (elems == undefined || elems == null || elems.length == 0) {
                callback([])
            } else {
                if (callback != undefined && callback !== null) {
                    var array = new Array(elems.length)
                    for (var i = 0; i < elems.length; i++) {
                        array[i] = elems[i].data
                    }
                    callback(array)
                }
            }
        }
    );

    bridge.registerHandler('filterEdge',
        function (data, callback) {
            var elems = cy.edges().filter(data.param)
            if (elems == undefined || elems == null || elems.length == 0) {
                callback([])
            } else {
                var array = new Array(elems.length)
                for (var i = 0; i < elems.length; i++) {
                    array[i] = elems[i].data
                }
                callback(array)
            }
        }
    );

    bridge.registerHandler('addNodeListener', function (data) {
        cy.on(data.event, 'node', function (evt) {
            var node = evt.target;
            bridge.callHandler('onNodeEvent', node)
        });
    })

    bridge.registerHandler('addEdgeListener', function (data) {
        cy.on(data.event, 'node', function (evt) {
            var edge = evt.target;
            bridge.callHandler('onEdgeEvent', edge)
        });
    })

    bridge.registerHandler('removeNodeListener', function (data) {
        cy.off(data.event, 'node');
    })

    bridge.registerHandler('removeEdgeListener', function (data) {
        cy.off(data.event, 'node');
    })

    bridge.registerHandler('png',
    function (data, callback) {
        callback(cy.png(data))
    }
);

    bridge.registerHandler('center', function (data) {
        if (data === undefined) {
            cy.center()
        } else if ((typeof data == 'string') && data.constructor == String) {
            cy.center(cy.$(data))
        } else {
            var set = new Set()
            data.forEach(element => {
                set.add(cy.$(element))
            });
            cy.center(
                set
            )
        }
    })

    bridge.registerHandler('reset', function () {
        cy.reset();
    })

    bridge.registerHandler('jsonString', function (data, callback) {
        callback(JSON.stringify(cy.json()))
    })

    bridge.registerHandler('restoreFromJsonString', function(data) {
        cy.json(JSON.parse(data))
    })

    bridge.callHandler('onInitialized')

    if (undefined == window.normalPipe) {
        console.log('window.normalPipe is not available')
    }
}

init();


//bridge.registerAsyn("cy", cy)
//bridge.registerAsyn("console",console)
//bridge.register("init",{
//  setNodeContent:function(name){
//    //cy.nodes().css({content:"data(" +name + ")"})
//    console.log("nodecontent:" + name)
//  },
//})
//
//
//bridge.registerAsyn("mycy", {
//    select:function(id, selectedId) {
//        if (selectedId != null) {
//            cy.getElementById(selectedId).unselect()
//        console.log("unselect:" + selectedId)
//        }
//        cy.getElementById(id).select()
//        console.log("select:" + id)
//    }
//})
//
//
//cy.on('tap', 'node', function(event){
//  bridge.call("onNodeClick", event.target.id(), function () {})
//});
//
//cy.on('tap', 'edge', function(event){
//  bridge.call("onEdgeClick", event.target.id(), function () {})
//});
//
//cy.on('taphold', 'node', function(event){
//  bridge.call("onNodeLongClick", event.target.id(), function () {})
//});
//
//cy.on('taphold', 'edge', function(event){
//  bridge.call("onEdgeLongClick", event.target.id(), function () {})
//});
//
//cy.on('tapselect', 'node', function(event){
//  bridge.call("onNodeSelected", event.target.id(), function () {})
//});
//
//cy.on('tapselect', 'edge', function(event){
//  bridge.call("onEdgeSelected", event.target.id(), function () {})
//});
//
//cy.on('tapunselect', 'node', function(event){
//  bridge.call("onNodeUnSelected", event.target.id(), function () {})
//});
//
//cy.on('tapunselect', 'edge', function(event){
//  bridge.call("onEdgeUnSelected", event.target.id(), function () {})
//});
//
//
//function getQueryVariable(variable)
//{
//       var query = window.location.search.substring(1);
//       var vars = query.split("&");
//       for (var i=0;i<vars.length;i++) {
//               var pair = vars[i].split("=");
//               if(pair[0] == variable){return pair[1];}
//       }
//       return(false);
//}
//
//let options = {
//  name: 'grid',
//
//  fit: true, // whether to fit the viewport to the graph
//  padding: 30, // padding used on fit
//  avoidOverlap: true, // prevents node overlap, may overflow boundingBox if not enough space
//  avoidOverlapPadding: 10, // extra spacing around nodes when avoidOverlap: true
//};
//
//cy.layout( options );
//
//
//bridge.call("onCytoscapeLoaded","")