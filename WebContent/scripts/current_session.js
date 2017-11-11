var g_latestUploadedFile;
var g_sensorList = [];
var g_refreshHeatMapInterval;
var g_REFRESH_RATE_MS = 400;
var g_selectedGroundPlanId = function(){ getUrlParameter("ground_plan_id") };
var g_selectedSensor = {
			sensor_id:0,
			sensor_chart_dbm_id: "" //"sensor_chart_dbm_"
		};
var g_currentSession;
var g_currentGroundPlan;
var g_selectedGroundPlan;


window.onload = function() {
	loadCurrentSessionTpl();
	initSelectedGroundPlan();
	initCurrentSession();
};

function initCurrentSession(sessionId){
	g_selectedGroundPlan = initSelectedGroundPlan();

	//Enable Session-START Button only if 
	// - at least GroundPlan has been selected
	// - OR Session already started.
	if( isSet(g_selectedGroundPlan) || isSet(g_currentSession) ){
		registerSessionStartConfigBtn();	
	}
	registerShowLatestSessionBtn()
	
	if( !isSet(g_currentSession)){
		var currentSessionId = isSet(sessionId)? sessionId: getUrlParameter("current_session_id");
		if( isSet(currentSessionId) ){
			g_currentSession = loadSessionFromDB(currentSessionId);
		}
	}	
	
	startSession();	
}

function startSession(){
	if( isSet(g_currentSession) ){
		$("#current_ground_plan_img").attr("src", "../rest/groundplan/image/" + g_currentSession.groundplanimage.filename);
    	$('html, body').animate({
            scrollTop: $("#sensor_chart_list").offset().top
        }, 2000);
    	
		g_heatmap = initHeatMap();
		initSensors(g_currentSession);
		g_refreshHeatMapInterval = startHeatMapAutoRefresh(g_REFRESH_RATE_MS);
		registerSensorPosOnGroundPlan();
		registerSensorSelection();
		registerBeaconConfigBtn();	
	}	
}

function loadSessionFromDB(sessionId){
	var loadedSession; 
	if( isSet(sessionId) ){
		$.ajax({
		    url : "../rest/planing_session/get/" + sessionId,
		    type : "get",
		    async: false,
		    success : function(session) {
		    	loadedSession = session;
		    },
		    error: function() {
		    	console.log("Failed to request the selected ground plan!(URL = '../rest/planing_session/get/" + sessionId +"'");
		    }
		 });	
	}	
	return loadedSession;
}

function getLatestSessionFromDB(){
	var latestSession; 
	$.ajax({
	    url : "../rest/planing_session/latest",
	    type : "get",
	    async: false,
	    success : function(session) {
	    	latestSession = session;
	    },
	    error: function() {
	    	console.log("Failed to request the latest session!(URL = '../rest/planing_session/latest'");
	    }
	 });	
	return latestSession;
}

function initSelectedGroundPlan(groundPlanId){
	var selectedGroundPlanId = isSet(groundPlanId)? groundPlanId: getUrlParameter("ground_plan_id");
	var selectedGroundPlan;
	if( isSet(selectedGroundPlanId) ){
		$.ajax({
		    url : "../rest/groundplan/get/" + selectedGroundPlanId,
		    type : "get",
		    async: false,
		    success : function(groundPlan) {
		    	selectedGroundPlan = groundPlan;
				$("#select_ground_plan_img").attr("src", "../rest/groundplan/image/" + selectedGroundPlan.filename);
				$("#select_ground_plan_btn").text("Groundplan");
				$("#select_ground_plan_btn").attr("class","btn btn-md btn-primary display-3");

				$("#session_name_inputfield").attr("style","");
				$("#session_description_inputfield").attr("style","");
				$("#start_session_btn").attr("style","");
				
		    },
		    error: function() {
		    	console.log("Failed to request the selected ground plan!(URL = '../rest/groundplan/get/" + g_selectedGroundPlanId +"'");
		    }
		 });	
	}
	return selectedGroundPlan;
};

function registerShowLatestSessionBtn(){
	$('#show_latest_session_btn').click( function() {
		stopAndClearCurrentSession();
		loadNewOrLatestSessionForm();
	});	
}

function registerSessionStartConfigBtn(){
	$('#start_session_btn').click( function() {
		$.ajax({
		     async: false,
		     type: 'GET',
		     url: '../rest/planing_session/add/' + $("#session_name_inputfield").val() + "/" + g_selectedGroundPlan.id + "/" + $("#session_description_inputfield").val(),
			 success : function(sessionData) {
				 g_currentSession = sessionData;
				 initCurrentSession();
				 $("#start_session_btn").attr("enabled", false);
			    },
			 error: function() {
			    	console.log("Failed to create new Session!(URL = '../rest/planing_session/add/" + $("#session_name_inputfield").val() + "/" + g_selectedGroundPlan.id + "/" + $("#session_description_inputfield").val() + "'");
			    }
		});
	});	
}


function registerBeaconConfigBtn(){
	$('#config_beacon_btn').click( function() {
		$.ajax({
		     async: false,
		     type: 'GET',
		     url: '../rest/sensor/config/' + $("#wifi_ssid").val() + "/" + $("#wifi_pwd").val() + "/" + $("#beacon_name").val(),
			 success : function(sensorData) {
				g_currentSession.sensors.push(sensorData);
				addSensor(  sensorData.id, //i_id 
							sensorData.hostname, //i_ip 
							sensorData.name, //i_name
							sensorData.description, //i_description
							50, //x
							50 //y 
						 );
							
			    },
			 error: function(result) {
			    	console.log("Error: "+ result + "<b>Failed to register new Sensor for Session!(URL = '../rest/sensor/config/" + $("#wifi_ssid").val() + "/" + $("#wifi_pwd").val() + "/" + $("#beacon_name").val() + "'");
			    }
		});
	});	
}
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
		if( isSet(event.target.id) && event.target.id.startsWith(sensorChartIdStart)){
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
			/*{ id: 1,
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
			{ id: 4,
				  name: "Garten",
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
			},*/
			
		]	
	};
	

	
	$.ajax({
	    url : "current_session.tpl.html",
	    type : "get",
	    async: false,
	    success : function(template) {
		    var rendered = Mustache.render(template, data);
		    $('#target').html(rendered);
		    loadNewOrLatestSessionForm();
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

function loadNewOrLatestSessionForm(){
	var tplName = "create_session_form.tpl.html";
	var latestSession; 

	var selectedGroundPlanId = getUrlParameter("ground_plan_id");
	if( !isSet(selectedGroundPlanId) ){
		latestSession= getLatestSessionFromDB();
		if( isSet(latestSession) ){
			tplName = "new_or_load_latest_session.tpl.html";
		}
	}
	
	$.ajax({
	    url : tplName,
	    type : "get",
	    async: false,
	    success : function(template) {
	    	var data =  {};
	    	if( isSet(latestSession) ){
	    		data = latestSession;
	    	}
		    var rendered = Mustache.render(template, data);
		    $('#new_or_load_session_section').html(rendered);
	    	if( isSet(latestSession) ){
	    		setSessionFormTitle("Latest Session");
	    		registerRestartLatestSessionBtn();
	    		registerContinueLatestSessionBtn();
	    		registerNewSessionBtn();
	    	}
	    },
	    error: function() {
	       connectionError();
	    }
	 });	
}

function setSessionFormTitle(i_text){
	$('#current_session_form_title').text(i_text);
}

function registerRestartLatestSessionBtn(){
	$('#restart_latest_session_btn').click( function() {
		g_currentSession = getLatestSessionFromDB();
		//remove sensordata? OR ask before?
		startSession();
	});	
}

function registerContinueLatestSessionBtn(){
	$('#continue_latest_session_btn').click( function() {
		g_currentSession = getLatestSessionFromDB();
		//remove sensordata? OR ask before?
		startSession();
	});	
}

function stopAndClearCurrentSession(){
	stopHeatMapAutoRefresh();
	
	//remove all SensorCharts
	$.get('session_add_new_sensor_form_card_column.tpl.html', function(template) {
	    var rendered = Mustache.render(template, {});
	    $('#sensor_chart_list').html(rendered);
	});
	
	//reset session groundplan image
	$("#current_ground_plan_img").attr("src", "assets/images/SelectGroundPlanImage.jpg");

	//reset all global-var's
	g_selectedSensor = {
			sensor_id:0,
			sensor_chart_dbm_id: "" //"sensor_chart_dbm_"
		};
	g_currentSession = undefined;
	g_currentGroundPlan = undefined;
	g_selectedGroundPlan = undefined;	
}

function registerNewSessionBtn(){
	$('#new_session_btn').click( function() {
		stopAndClearCurrentSession()
		
		$.ajax({
		    url : "create_session_form.tpl.html",
		    type : "get",
		    async: false,
		    success : function(template) {
		    	var data =  {};
			    var rendered = Mustache.render(template, data);
			    $('#new_or_load_session_section').html(rendered);
			    
				registerSessionStartConfigBtn();	
				registerShowLatestSessionBtn();
			    
		    	$('html, body').animate({
		            scrollTop: $("#new_or_load_session_section").offset().top
		        }, 2000);
			    
		    },
		    error: function() {
		       connectionError();
		    }
		 });	

	});	
}

function loadSessionSensorChartList(){
	$.ajax({
	    url : "../rest/planing_session/sensors",
	    type : "get",
	    async: false,
	    success : function(resultSensorList) {
				$.ajax({
				    url : "sensor_chart_galery_elements.tpl.html",
				    type : "get",
				    async: false,
				    success : function(template) {
				    	var data = { sensor_chart_list: [] };
				    	resultSensorList.forEach( function(sensor, idx, origList) { 
				    		data.sensor_chart_list[data.sensor_chart_list.length] = { id: sensor.id,
				    																  name: sensor.name,
				    																  description: sensor.description,
				    																  timestamp: session.starttime,
				    															      signal_dbm: -70,
				    															      signal_quality_pct: 50,
				    																};
				    	} );
					    var rendered = Mustache.render(template, data);
					    $('#progress-bars3-12').html(rendered);
				    },
				    error: function(data) {
				       ajaxError();
				    }
				 });	
		    },
		    error: function(data) {
		       ajaxError();
		    }
		 });		
}

function initSensors(session){
	if( isSet(session) ){
		session.sensors.forEach( function(sensor, idx, origList) { 
			addSensor(  sensor.id, //i_id 
					sensor.hostname, //i_ip 
					sensor.name, //i_name
					sensor.description, //i_description
					50, //x
					50 //y
				);
		} );
	}

	/*addSensor(  1, //i_id 
			"192.168.2.103", //i_ip 
			"First Sesnor", //i_name
			"No description yet", //i_description
			50, //x
			50 //y
		);
	addSensor(  2, //i_id 
			"192.168.2.104", //i_ip 
			"Second Sesnor", //i_name
			"No description yet eather", //i_description
			100, //x
			200 //y
		);
	addSensor(  3, //i_id 
			"192.168.2.105", //i_ip 
			"Second Sesnor", //i_name
			"No description yet eather", //i_description
			100, //x
			200 //y
		);
	addSensor(  4, //i_id 
			"192.168.2.107", //i_ip 
			"Second Sesnor", //i_name
			"No description yet eather", //i_description
			100, //x
			200 //y
		);*/

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

function loadMustachTemplateInHtmlElement(i_tplFileName, i_targetHtmlElementId, i_data, i_preOnSuccess, i_postOnSucces, i_onError){
	$.ajax({
	    url : i_tplFileName,
	    type : "get",
	    async: false,
	    success : function(template) {
	    	var data =  i_data;
	    	if( isSet(i_preOnSuccess) ){
		    	i_preOnSuccess(data, template);
	    	}
	    	
	    	var rendered = Mustache.render(template, data);
		    $('#new_or_load_session_section').html(rendered);
	    	
		    if( isSet(i_postOnSuccess) ){
	    		i_postOnSuccess(data, template);
	    	}
	    },
	    error: i_onError
	 });	
	
}
