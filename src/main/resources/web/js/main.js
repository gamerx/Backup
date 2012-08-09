$(function() {
    
    $("#maintab").load('ajax/main', function() {
        $("#loading").hide();
    });
    
    // Bind tabs jQueryUI to the main div.
    $("#main").tabs({
        ajaxOptions: {
            error: function( xhr, status, index, anchor ) { 
                $( anchor.hash ).html("Couldn't load this resource. There's probably something wrong with the server. ");
            }
        }
    });
    
    // Bind tabs jQueryUI to the main div.
    $("#enabledebug").click(function() {
        alert("hey");
        
        $.ajax({
            type: "GET",
        
            url: "actions/enabledebug",
            success : function(data){
                alert(data);
            },
            error : function(httpReq,status,exception){
                alert(status+" "+exception);
            }
        });
    });
    
    function onDebugEnable() {
            
            $( "#dialog" ).dialog({
                
                buttons: {"Ok": function() {$(this).dialog("close");}, "No!": function() {$(this).dialog("close");}},
                modal: true
            });
            
        }
        
    });