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
    $("#enabledebug").on("click", function() {
        showOkDialog(
            "This will generate quite a bit of output, are you sure?",
            function() {
                $.ajax({
                    type: "GET",
                    dataType: 'json',
                    url: "action/enabledebug",
                    success : function(data) {
                        //$("#id").html(data);
                        alert(data);
                        var obj = $.parseJSON(data);
                        $.each(obj, function() {
                            lang += this['Language'] + "";
                        });

                        $('span').html(lang);

                        
                    },
                    error : function(httpReq,status,exception){
                        alert(status+" "+exception);
                    }
                });
            
            }
            );
    });
    
    // Dialog Generation.
    function showOkDialog(prompt, doAfter) {
        $("#dialog").html("<p>"+prompt+"</p>").dialog({
            buttons: {
                "Yes": function() {
                    $(this).dialog("close");
                    doAfter();
                }, 
                "Cancel": function() {
                    $(this).dialog("close");
                }
            }
        });
    }
});