$(function() {
    $("#main").tabs({
        ajaxOptions: {
            error: function( xhr, status, index, anchor ) { 
                $( anchor.hash ).html(
                    "Couldn't load this resource. There's probably something wrong with the server. ");
            }
        }
    });
});