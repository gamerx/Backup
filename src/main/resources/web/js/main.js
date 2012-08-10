$(function() {
    
    // Load main tab.
    $("#maintab").load('ajax/main', function() {
        $("#loading").hide();
    });
    
    // Bind tabs to the tab area.
    $("#main").tabs({
        ajaxOptions: {
            success: function( data ) {},
            error: function( xhr, status, index, anchor ) {
                $( anchor.hash ).html("Couldn't load this resource. There's probably something wrong with the server.");
            }
        }
    });
    
    // Bind the debug button.
    $("#enabledebug").live("click", function() {
        if(showOkDialog("This will generate quite a bit of output, are you sure?")) {
            $.ajax({
                type: "GET",
                url: "actions/enabledebug",
                success : onDebugEnable(data),
                error : function(httpReq,status,exception){
                    alert(status+" "+exception);
                }
            });
        }
    });
    
    // When we show the dialog.
    function showOkDialog(prompt) {
        var sure = false;
        $("#dialog").html("<p>"+prompt+"</p>").dialog({
            buttons: {
                "I'm Sure!": function() {
                    $(this).dialog("close");
                    sure = true;
                }, 
                "Noooooo!": function() {
                    $(this).dialog("close");
                    sure = false;
                }
            },
            modal: true,
            closeOnEscape: false
        });
        return sure;
    }
    
    // After enable.
    function onDebugEnable(data) {
        alert(data);
            
    }
        
});