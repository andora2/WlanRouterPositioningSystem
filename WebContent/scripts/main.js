/**
 * 
 */

var g_schwenkSlider;

(function(){  
	initPortList();
	g_schwenkSlider = $("#schwenk_slider").slider();
	
	g_schwenkSlider.on("slide", function(slideEvt) {
		 	$("#ex6SliderVal").text(slideEvt.value);
		 	
			$.ajax({
			     async: false,
			     type: 'GET',
			     url: 'rest/arduino/schwenk01/' + slideEvt.value,
			     success: function(data) {
			          //callback
			     }
			});
		 	
		 });	    
	
})();


function initPortList(){
	$.ajax({
	     async: true,
	     type: 'GET',
	     url: 'rest/arduino/serial_ports',
	     success: function(portList) {
	    	 //var portList = jQuery.parseJSON(data);
	    	 for (i = 0; i < portList.length; i++) {
	    		 var item = '<a id="serial_port_list_element_'+ portList[i].systemPortName +'" class="dropdown-item" href="#" onclick="changeSerialComPort(\''+ portList[i].systemPortName +'\');return false;">'+ portList[i].systemPortName +' - ' + portList[i].descriptivePortName + '(Open: ' + portList[i].open + ') </a>';
	    		 $("#port_list").append(item);
	    	 }
	     }
	});
	
}

function changeSerialComPort(i_strSystemPortName){
	$.ajax({
	     async: false,
	     type: 'GET',
	     url: 'rest/arduino/connect_to_serial_port/'+ i_strSystemPortName,
	     success: function(serialPort) {
	    	 $('#serial_port_list_element_'+ i_strSystemPortName).html( serialPort.systemPortName +' - ' + serialPort.descriptivePortName + '(Open: TRUE )' );
	     }
	});	
}

// Closure
(function() {
  /**
   * Decimal adjustment of a number.
   *
   * @param {String}  type  The type of adjustment.
   * @param {Number}  value The number.
   * @param {Integer} exp   The exponent (the 10 logarithm of the adjustment base).
   * @returns {Number} The adjusted value.
   */
  function decimalAdjust(type, value, exp) {
    // If the exp is undefined or zero...
    if (typeof exp === 'undefined' || +exp === 0) {
      return Math[type](value);
    }
    value = +value;
    exp = +exp;
    // If the value is not a number or the exp is not an integer...
    if (isNaN(value) || !(typeof exp === 'number' && exp % 1 === 0)) {
      return NaN;
    }
    // Shift
    value = value.toString().split('e');
    value = Math[type](+(value[0] + 'e' + (value[1] ? (+value[1] - exp) : -exp)));
    // Shift back
    value = value.toString().split('e');
    return +(value[0] + 'e' + (value[1] ? (+value[1] + exp) : exp));
  }

  // Decimal round
  if (!Math.round10) {
    Math.round10 = function(value, exp) {
      return decimalAdjust('round', value, exp);
    };
  }
  // Decimal floor
  if (!Math.floor10) {
    Math.floor10 = function(value, exp) {
      return decimalAdjust('floor', value, exp);
    };
  }
  // Decimal ceil
  if (!Math.ceil10) {
    Math.ceil10 = function(value, exp) {
      return decimalAdjust('ceil', value, exp);
    };
  }
})();

  function writeMessage(canvas, message) {
    var context = canvas.getContext('2d');
    context.clearRect(0, 0, canvas.width, canvas.height);
    context.font = '18pt Calibri';
    context.fillStyle = 'black';
    context.fillText(message, 10, 25);
  }
  
  function getMousePos(canvas, evt) {
    var rect = canvas.getBoundingClientRect();
    return {
      x: evt.clientX - rect.left,
      y: evt.clientY - rect.top
    };
  }
  var canvas = document.getElementById('myCanvas');
  var context = canvas.getContext('2d');
  var isMouseDown = false;
  var camPos0;
  var camPos1; 
  var newCamPosDelta;

  var eventMap = {};

  function touchHandler(event)
  {
      var touches = event.changedTouches,
          first = touches[0],
          type = "";
      switch(event.type)
      {
          case "touchstart": type = "mousedown"; break;
          case "touchmove":  type = "mousemove"; break;        
          case "touchend":   type = "mouseup";   break;
          default:           return;
      }

      // initMouseEvent(type, canBubble, cancelable, view, clickCount, 
      //                screenX, screenY, clientX, clientY, ctrlKey, 
      //                altKey, shiftKey, metaKey, button, relatedTarget);

      var simulatedEvent = document.createEvent("MouseEvent");
      simulatedEvent.initMouseEvent(type, true, true, window, 1, 
                                    first.screenX, first.screenY, 
                                    first.clientX, first.clientY, false, 
                                    false, false, false, 0/*left*/, null);

      first.target.dispatchEvent(simulatedEvent);
      event.preventDefault();
  }

  function init() 
  {
      document.addEventListener("touchstart", touchHandler, true);
      document.addEventListener("touchmove", touchHandler, true);
      document.addEventListener("touchend", touchHandler, true);
      document.addEventListener("touchcancel", touchHandler, true);    
  }
  
  (function(){  
	  init();
  })();
  
  
  //canvas.addEventListener('mousemove', function(evt) {
  canvas.onmousemove = function(evt) {
    writeMessage(canvas, "Mouse moveing" );
	  
	if(isMouseDown == true){
	    var mousePos = getMousePos(canvas, evt);
	    //var message = 'Mouse position: ' + mousePos.x + ',' + mousePos.y;
	    //writeMessage(canvas, message);
	    
	    var camPosY = 180 - (180 * (mousePos.x/canvas.width));
	    var camPosX = 180 * (mousePos.y/canvas.height);
	    if(camPos0 == null){
	    	camPos0 = {};
	    
	    	camPos0["x"] = camPosX;
	    	camPos0["y"] = camPosY;
	    } else {
	    	if( camPos1 == null ){
	    		camPos1 = {};
	    	}
	    		
	    	camPos1["x"] = camPosX;
	    	camPos1["y"] = camPosY;

			var newCamPosDx = camPos1.x - camPos0.x; 
			var newCamPosDy = camPos1.y - camPos0.y; 

			var message = 'Mouse delta: ' + newCamPosDx + ',' + newCamPosDy;
		    writeMessage(canvas, message);
			
			if( newCamPosDx != 0 || newCamPosDy != 0 ){
				$.ajax({
				     async: false,
				     type: 'GET',
				     url: 'rest/arduino/move02/' + Math.round10(newCamPosDx,-3) + '/' + Math.round10(newCamPosDy,-3),
				     success: function(data) {
				          //callback
				     }
				});
			}
			camPos0.x = camPos1.x; 
			camPos0.y = camPos1.y; 
	    	
	    }
	    
	}    
  };

  canvas.onmousedown = function(evt) {
	  writeMessage(canvas, "Mouse downing" );
	  isMouseDown = true;
	  camPos0 = null;
	  camPos1 = null;
  };
  
  canvas.onmouseup = function(evt) {
	  writeMessage(canvas, "Mouse upping" );
	  isMouseDown = false;
	  //camPos0 = null;
	  //camPos1 = null; 
  };  
  
