function isSet(i){
	return !( i === undefined ) && i != null;
};

var getUrlParameter = function getUrlParameter(sParam) {
	var location = window.location;
	var urlElem = location.search.substring(1);
    var sPageURL = decodeURIComponent( urlElem ); 
    var sURLVariables = sPageURL.split('&');
    var sParameterName;
    var i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return isSet( sParameterName[1] ) ? sParameterName[1]: undefined;
        }
    }
};


