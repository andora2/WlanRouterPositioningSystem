var g_latestUploadedFile;
var g_sensorList = [];
var g_refreshHeatMapInterval;
var g_REFRESH_RATE_MS = 400;
var g_selectedSensor = {
			sensor_id:0,
			sensor_chart_dbm_id: "" //"sensor_chart_dbm_"
		};

var g_selectedGroundPlanId;

window.onload = function() {
	loadCurrentSessionTpl();
	g_selectedGroundPlanId = getUrlParameter("ground_plan_id");
	if( !( g_selectedGroundPlanId === undefined ) ){
		$.ajax({
		    url : "../rest/groundplan/get/" + g_selectedGroundPlanId,
		    type : "get",
		    async: false,
		    success : function(groundPlan) {
				$("#select_ground_plan_img").attr("src", "../rest/main/image/" + groundPlan.filename);
				$("#current_ground_plan_img").attr("src", "../rest/main/image/" + groundPlan.filename);
            	$('html, body').animate({
                    scrollTop: $("#progress-bars3-12").offset().top
                }, 2000);

		    },
		    error: function() {
		    	console.log("Failed to request the selected ground plan!(URL = '../rest/groundplan/get/" + g_selectedGroundPlanId +"'");
		    }
		 });	
	}
	g_heatmap = initHeatMap();
	initSensors();
	g_refreshHeatMapInterval = startHeatMapAutoRefresh(g_REFRESH_RATE_MS);
	registerSensorPosOnGroundPlan();
	registerSensorSelection();
	
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
            return sParameterName[1] === undefined ? undefined : sParameterName[1];
        }
    }
};

function registerSensorPosOnGroundPlan(){
	$("#heatmapContainer").dblclick(function(event){ 
		if( g_selectedSensor.sensor_chart_dbm_id.length > 0 ){
			var parentOffset = $(this).parent().offset(); 
			var relX = event.pageX - parentOffset.left;
			var relY = event.pageY - parentOffset.top;
			var sensor = getSensorFromGlobalListById(g_selectedSensor.sensor_id);
			if( sensor != null ){
				sensor.heatmapData.x = Math.trunc(relX);
				sensor.heatmapData.y = Math.trunc(relY);
			}
		}
	});	
}

function registerSensorSelection(){
	$("div").click(function(event){
		var sensorChartIdStart = "sensor_chart_dbm_";
		var targetId = event.target.id;
		if( !(event.target.id === undefined || event.target.id === null) && event.target.id.startsWith(sensorChartIdStart)){
			if( g_selectedSensor.sensor_chart_dbm_id.length > 0 ){
				$("#"+g_selectedSensor.sensor_chart_dbm_id).attr("style", "");
			}
			$("#"+event.target.id).attr("style", "border: 2px solid black");
			
			var sensorId = event.target.id.slice(sensorChartIdStart.length); //getSensorIDX by id
			g_selectedSensor.sensor_chart_dbm_id = event.target.id;
			g_selectedSensor.sensor_id = parseInt(sensorId);
		} 
	});	
}

function getSensorFromGlobalListById(i_sensor_id){
	var result = g_sensorList.filter(function( obj ) {
		  return obj.id == i_sensor_id;
		});
	
	return result.length > 0? result[0]: null;
}

function loadCurrentSessionTpl(){
	var data = {
		sensor_chart_list: [
			{ id: 1,
			  name: "Sofa",
			  description: "Irgend eine tolle beschreibung. Vieleicht auch mit addresse",
			  timestamp: "2017-03-11 10:10:11",
		      signal_dbm: -70,
		      signal_quality_pct: 50,
			},
			{ id: 2,
				  name: "Schlafzimmer",
				  description: "Irgend eine tolle beschreibung. Vieleicht auch mit addresse",
				  timestamp: "2017-03-11 10:10:11",
			      signal_dbm: -60,
			      signal_quality_pct: 70,
			},
			{ id: 3,
				  name: "Küche",
				  description: "Irgend eine tolle beschreibung. Vieleicht auch mit addresse",
				  timestamp: "2017-03-11 10:10:11",
			      signal_dbm: -20,
			      signal_quality_pct: 90,
			},
			{ id: 3,
				  name: "Küche",
				  description: "Irgend eine tolle beschreibung. Vieleicht auch mit addresse",
				  timestamp: "2017-03-11 10:10:11",
			      signal_dbm: -20,
			      signal_quality_pct: 90,
			},
			{ id: 3,
				  name: "Küche",
				  description: "Irgend eine tolle beschreibung. Vieleicht auch mit addresse",
				  timestamp: "2017-03-11 10:10:11",
			      signal_dbm: -20,
			      signal_quality_pct: 90,
			},
			{ id: 3,
				  name: "Küche",
				  description: "Irgend eine tolle beschreibung. Vieleicht auch mit addresse",
				  timestamp: "2017-03-11 10:10:11",
			      signal_dbm: -20,
			      signal_quality_pct: 90,
			},
			{ id: 3,
				  name: "Küche",
				  description: "Irgend eine tolle beschreibung. Vieleicht auch mit addresse",
				  timestamp: "2017-03-11 10:10:11",
			      signal_dbm: -20,
			      signal_quality_pct: 90,
			},
			{ id: 3,
				  name: "Küche",
				  description: "Irgend eine tolle beschreibung. Vieleicht auch mit addresse",
				  timestamp: "2017-03-11 10:10:11",
			      signal_dbm: -20,
			      signal_quality_pct: 90,
			},
			{ id: 3,
				  name: "Küche",
				  description: "Irgend eine tolle beschreibung. Vieleicht auch mit addresse",
				  timestamp: "2017-03-11 10:10:11",
			      signal_dbm: -20,
			      signal_quality_pct: 90,
			},
			{ id: 3,
				  name: "Küche",
				  description: "Irgend eine tolle beschreibung. Vieleicht auch mit addresse",
				  timestamp: "2017-03-11 10:10:11",
			      signal_dbm: -20,
			      signal_quality_pct: 90,
			},
			{ id: 3,
				  name: "Küche",
				  description: "Irgend eine tolle beschreibung. Vieleicht auch mit addresse",
				  timestamp: "2017-03-11 10:10:11",
			      signal_dbm: -20,
			      signal_quality_pct: 90,
			},
			{ id: 3,
				  name: "Küche",
				  description: "Irgend eine tolle beschreibung. Vieleicht auch mit addresse",
				  timestamp: "2017-03-11 10:10:11",
			      signal_dbm: -20,
			      signal_quality_pct: 90,
			},
			{ id: 3,
				  name: "Küche",
				  description: "Irgend eine tolle beschreibung. Vieleicht auch mit addresse",
				  timestamp: "2017-03-11 10:10:11",
			      signal_dbm: -20,
			      signal_quality_pct: 90,
			},
			{ id: 3,
				  name: "Küche",
				  description: "Irgend eine tolle beschreibung. Vieleicht auch mit addresse",
				  timestamp: "2017-03-11 10:10:11",
			      signal_dbm: -20,
			      signal_quality_pct: 90,
			},
			
		]	
	};
	
	$.ajax({
	    url : "current_session.tpl.html",
	    type : "get",
	    async: false,
	    success : function(template) {
		    var rendered = Mustache.render(template, data);
		    $('#target').html(rendered);
	    },
	    error: function() {
	       connectionError();
	    }
	 });	
	/*$.get('current_session.tpl.html', function(template) {
	    var rendered = Mustache.render(template, data);
	    $('#target').html(rendered);
	  });*/	
}

function initSensors(){
	addSensor(  1, //i_id 
				"192.168.0.122", //i_ip 
				"First Sesnor", //i_name
				"No description yet", //i_description
				50, //x
				50 //y
			);
	
	addSensor(  2, //i_id 
			"192.168.0.121", //i_ip 
			"Second Sesnor", //i_name
			"No description yet eather", //i_description
			100, //x
			200 //y
		);
}

function addSensor(i_id, i_ip, i_name, i_description, i_x, i_y){
	g_sensorList[g_sensorList.length] = {
		id: i_id,
		ip: i_ip,
		name: i_name,
		description: i_description,
		heatmapData: {
			x: i_x,
			y: i_y,
			wifiSignal_dbm: {
				min: -85,
				max: -55,
				value: -85
			},
			pingSpeed_ms: {
				min: 0,
				max: 2000,
				value: 0
			},
			downSpeed_kbps: {
				min: 0,
				max: 50000,
				value: 0
			},
			upSpeed_kbps: {
				min: 0,
				max: 50000,
				value: 0
			}
		}
	};
}

function initHeatMap(){
	return h337.create({
		  container: document.getElementById('heatmapContainer'),
		  // a waterdrop gradient ;-)
		  //gradient: { .1: 'rgba(250,0,0,.2)', .2: "rgba(180,0,0, .2)", .4: "rgba(100,0,0, .2)", .7: "rgba(50,0,0, .2)", .95: 'rgba(0,250,0,.2)'},
		  maxOpacity: .6,
		  minOpacity: .0,
		  radius: 80,
		  blur: .9
		});
};

function getHeatMapCanvaSize(){
	return {
		width: (+window.getComputedStyle(document.body).width.replace(/px/,'')),
		height: (+window.getComputedStyle(document.body).height.replace(/px/,''))
	};
}

function stopHeatMapAutoRefresh(){
	if( !( g_refreshHeatMapInterval === undefined || g_refreshHeatMapInterval === null ) ){
		clearInterval( g_refreshHeatMapInterval );
	}
} 

function startHeatMapAutoRefresh(i_interval) {
	var interval = i_interval === undefined || i_interval === null || i_interval < 100 ? 400: i_interval;
	heatMapInterval = setInterval( onRefreshHeatMap, interval);
	return heatMapInterval;
}

function onRefreshHeatMap() {
	onRefreshHeatMapWifiSignalValues();
};

function onRefreshHeatMapWifiSignalValues() {
	updateWifiSignalValuesForAllSensors();
	var newHeatMapData = getWifiSignalHeatMapDataFromAllSensors(); 
    g_heatmap.setData(newHeatMapData);        
};

function getWifiSignalHeatMapDataFromAllSensors(){
	var resWifiSignalDataPoints = getWifiSignalHeatMapDataPoints();
	var res = { 
		min: g_sensorList[0].heatmapData.wifiSignal_dbm.min,
		max: g_sensorList[0].heatmapData.wifiSignal_dbm.max,
		data: resWifiSignalDataPoints
		};
	
	return res;
};

function getWifiSignalHeatMapDataPoints(){
	var resDataPoints = g_sensorList.map( function(sensor) {
		return { x: sensor.heatmapData.x,
			 y: sensor.heatmapData.y,
			 value: sensor.heatmapData.wifiSignal_dbm.value }
	});
	return resDataPoints;
};

function updateWifiSignalValuesForAllSensors(){
	/*var promises = [];
	g_sensorList.forEach( function(sensor) { 
								promises.push( refreshWifiSignalValueAsync(sensor) ) 
								} );
	$.when.apply($, promises).then(
			function(){
				return true;
				},
			function(){
				return false;
				});
	return false;*/
	g_sensorList.forEach( function(sensor, idx, origList) { 
			refreshWifiSignalValueAsync(sensor); 
		} );

};

function refreshWifiSignalValueAsync(i_sensor){
	var urlGetRSSI = 'http://' + i_sensor.ip + '/rssi';
	return $.ajax({
	     async: true,
	     type: 'GET',
	     url: urlGetRSSI,
	     timeout: 1500,
	     success: function(i_dbmRSSIValue) {
	    	var newVal =  parseInt(i_dbmRSSIValue);
	    	i_sensor.heatmapData.wifiSignal_dbm.value = chopValueOnMinMaxBorder(  i_sensor.heatmapData.wifiSignal_dbm.min, 
	    																		  i_sensor.heatmapData.wifiSignal_dbm.max,
	    																		  newVal );
	    	
	    	var quality =  ((newVal - i_sensor.heatmapData.wifiSignal_dbm.min) / (i_sensor.heatmapData.wifiSignal_dbm.max - i_sensor.heatmapData.wifiSignal_dbm.min))*100;
	    	$('#sensor_chart_'+ i_sensor.id).asPieProgress("go", quality);
	    	$('#sensor_chart_dbm_'+ i_sensor.id).text( newVal + ' dbm' );

	     },
		
		error: function(){
			// if sensor value is not retreivable we lost the connection => asumeing signal loss
			i_sensor.heatmapData.wifiSignal_dbm.value = i_sensor.heatmapData.wifiSignal_dbm.min;
		}
	});	
}

function chopValueOnMinMaxBorder(i_min, i_max, i_value){
	return i_value < i_min? i_min: i_value > i_max? i_max: i_value;
}


