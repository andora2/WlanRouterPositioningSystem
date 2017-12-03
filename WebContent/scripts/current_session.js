var g_latestUploadedFile;
var g_sensorList = [];
var g_refreshHeatMapInterval;
var g_REFRESH_RATE_MS = 1000;
var g_selectedGroundPlanId = function(){ getUrlParameter("ground_plan_id") };
var g_selectedSensor = {
			sensor_id:0,
			sensor_chart_dbm_id: "" //"sensor_chart_dbm_"
		};
var g_currentSession;
var g_currentGroundPlan;
var g_selectedGroundPlan;
var g_currentGeoLocation;

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
		$("#current_ground_plan_img").attr("src", "rest/groundplan/image/" + g_currentSession.groundplanimage.filename);
    	$('html, body').animate({
            scrollTop: $("#sensor_chart_list").offset().top
        }, 2000);
    	
    	loadSessionSensorChartList(g_currentSession.id);
    	
		g_heatmap = initHeatMap();
		initSensors(g_currentSession);
		g_refreshHeatMapInterval = startHeatMapAutoRefresh(g_REFRESH_RATE_MS);
		registerSensorPosOnGroundPlan();
		registerBeaconConfigBtn();
		registerWifiSSIDListRefreshBtn();
	}	
}

function loadSessionFromDB(sessionId){
	var loadedSession; 
	if( isSet(sessionId) ){
		$.ajax({
		    url : "rest/planing_session/get/" + sessionId,
		    type : "get",
		    async: false,
		    success : function(session) {
		    	loadedSession = session;
		    },
		    error: function() {
		    	console.log("Failed to request the selected ground plan!(URL = 'rest/planing_session/get/" + sessionId +"'");
		    }
		 });	
	}	
	return loadedSession;
}

function getLatestSessionFromDB(){
	var latestSession; 
	$.ajax({
	    url : "rest/planing_session/latest",
	    type : "get",
	    async: false,
	    success : function(session) {
	    	latestSession = session;
	    },
	    error: function() {
	    	console.log("Failed to request the latest session!(URL = 'rest/planing_session/latest'");
	    }
	 });	
	return latestSession;
}

function initSelectedGroundPlan(groundPlanId){
	var selectedGroundPlanId = isSet(groundPlanId)? groundPlanId: getUrlParameter("ground_plan_id");
	var selectedGroundPlan;
	if( isSet(selectedGroundPlanId) ){
		$.ajax({
		    url : "rest/groundplan/get/" + selectedGroundPlanId,
		    type : "get",
		    async: false,
		    success : function(groundPlan) {
		    	selectedGroundPlan = groundPlan;
				$("#select_ground_plan_img").attr("src", "rest/groundplan/image/" + selectedGroundPlan.filename);
				$("#select_ground_plan_btn").text("Groundplan");
				$("#select_ground_plan_btn").attr("class","btn btn-md btn-primary display-3");

				$("#session_name_inputfield").attr("style","");
				$("#session_description_inputfield").attr("style","");
				$("#start_session_btn").attr("style","");
				
		    },
		    error: function() {
		    	console.log("Failed to request the selected ground plan!(URL = 'rest/groundplan/get/" + g_selectedGroundPlanId +"'");
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
		     url: 'rest/planing_session/add/' + $("#session_name_inputfield").val() + "/" + g_selectedGroundPlan.id + "/" + $("#session_description_inputfield").val(),
			 success : function(sessionData) {
				 g_currentSession = sessionData;
				 initCurrentSession();
				 $("#start_session_btn").attr("enabled", false);
			    },
			 error: function() {
			    	console.log("Failed to create new Session!(URL = 'rest/planing_session/add/" + $("#session_name_inputfield").val() + "/" + g_selectedGroundPlan.id + "/" + $("#session_description_inputfield").val() + "'");
			    }
		});
	});	
}

function getCurrentGeoLonLat(){
	return isSet(g_currentGeoLocation) && isSet(g_currentGeoLocation.coords)?
			{lon: g_currentGeoLocation.coords.latitude,
			 lat: g_currentGeoLocation.coords.longitude }:
			{lon: 0.0,
			 lat: 0.0 };
}

function getCurrentGeoLon(){
	location.coords.latitude;
	location.coords.longitude;
}

function refreshGeoLocation(){
	if(navigator.geolocation)
	    navigator.geolocation.getCurrentPosition(handleGetCurrentPosition, onError);
}

function handleGetCurrentPosition(location){
	g_currentGeoLocation = location;
}

function onError(){
	g_currentGeoLocation = location;
}

function registerBeaconConfigBtn(){
	$('#config_beacon_btn').click( function() {
		refreshGeoLocation();
		geoLocation = getCurrentGeoLonLat();
		$.ajax({
		     async: false,
		     type: 'GET',
		     url: 'rest/sensor/add/' + g_currentSession.id + "/" + $("#wifi_ssid").val() + "/" + $("#wifi_pwd").val() + "/" + $("#beacon_name").val() + "/" + geoLocation.lon + "/" + geoLocation.lat,
		     //url: 'rest/sensor/add/' + g_currentSession.id + "/" + "MySSID" + "/" + "MyWifiPWD" + "/" + $("#beacon_name").val() + "/" + geoLocation.lon + "/" + geoLocation.lat,
			 success : function(sensorData) {
					g_currentSession.sensors.push(sensorData);
					addSensor(  sensorData //sensorData.id, //i_id 
								//sensorData.ipaddress, //i_ip 
								//sensorData.name, //i_name
								//50, //x
								//50 //y 
							 );
					addSensorToChartList(sensorData.id, sensorData.name);	
					registerSensorPosOnGroundPlan();
					registerSensorSelection(sensorData.id);
				    $("#sensor_chart_"+sensorData.id).asPieProgress({namespace: "pie_progress"})

			    },
			 error: function(result) {
			    	console.log("Error: "+ result + "<b>Failed to register new Sensor for Session!(URL = 'rest/sensor/config/" + $("#wifi_ssid").val() + "/" + $("#wifi_pwd").val() + "/" + $("#beacon_name").val() + "'");
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
				sensor.heatmapData.x = sensor.sensorDBObj.mapposx = Math.trunc(relX);
				sensor.heatmapData.y = sensor.sensorDBObj.mapposy = Math.trunc(relY);
				updateSensorOnDB(sensor.sensorDBObj);				
			}
		}
	});	
}

function updateSensorOnDB(i_sensor){
	$.ajax({
	    url: "rest/sensor/" + i_sensor.id + "/set_groundplan_pos/" + i_sensor.mapposx + "/" + i_sensor.mapposy,
	    type: 'GET',//'POST',
	    //data: i_sensor,
	    async: false,
	    success: function (sensor) {
	    	console.log("updated");
	    },
	    error: function (data) {
	    	ajaxError(data)
	    },
	    cache: false,
	    //contentType: false,
	    //processData: false
	});
}


function registerSensorSelection(i_sensorId){
	if( isSet(g_currentSession) && isSet(g_currentSession.id) ){
		
		var sensorHTMLID = "#sensor_chart_card_" + i_sensorId;
		$(sensorHTMLID).click(function(event){
			if( g_selectedSensor.sensor_id != i_sensorId ){
				if( g_selectedSensor.sensor_chart_dbm_id.length > 0 ){
					var oldSelectedSensorHTMLID = "#sensor_chart_card_" + i_sensorId;
					$("#sensor_chart_card_" + g_selectedSensor.sensor_id).attr("style", "");
					$("#sensor_action_btns_" + g_selectedSensor.sensor_id).attr("style","display:none");
				}
				$(sensorHTMLID).attr("style", "border: 2px solid black");
				$("#sensor_action_btns_" + i_sensorId).attr("style","display:inherit;");
				
				g_selectedSensor.sensor_chart_dbm_id = "#sensor_chart_dbm_" + i_sensorId;
				g_selectedSensor.sensor_id = i_sensorId;
				
			}
		});	
		
		var sensorBtnHTMLID = "#sensor_chart_remove_btn_" + i_sensorId;
		$(sensorBtnHTMLID).click(function(event){
			$.ajax({
			    url : "rest/sensor/delete/" + i_sensorId,
			    type : "get",
			    async: false,
			    success : function(res) {
					var sensor = getSensorFromGlobalListById(i_sensorId);
			    	updateSensorChartAndHeatmapRSSIDisplayValue( sensor, 
			    												 sensor.heatmapData.wifiSignal_dbm.min);
			    	$(sensorHTMLID).remove();
			   
			    },
			    error: function() {
			       connectionError();
			    }
			 });	
		});	

		sensorBtnHTMLID = "#sensor_chart_activation_btn_" + i_sensorId;
		$(sensorBtnHTMLID).click(function(event){
			var isActive = $(sensorBtnHTMLID).text() == "Deactivate";
			var activationTxt = isActive? "Activate": "Deactivate";
			$(sensorBtnHTMLID).text(activationTxt)
			var deactivatedOverlayStyle = isActive ? "display:inherit;" : "display:none;";
			$("#sensor_chart_deactivated_overlay_" + i_sensorId).attr("style", deactivatedOverlayStyle );
			
			if( isActive ){
				var sensor = getSensorFromGlobalListById(i_sensorId);
		    	updateSensorChartAndHeatmapRSSIDisplayValue( sensor, 
		    												 sensor.heatmapData.wifiSignal_dbm.min);

			}
		});	
	}
}

function isActiveSensor(i_nSensorId){
	sensorBtnHTMLID = "#sensor_chart_activation_btn_" + i_nSensorId;
	return isSet($(sensorBtnHTMLID)) && $(sensorBtnHTMLID).text() == "Deactivate";
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
	    url : "current_session/main.tpl.html",
	    type : "get",
	    async: false,
	    success : function(template) {
		    var rendered = Mustache.render(template, data);
		    $('#target').html(rendered);
		    registerWifiSSIDListRefreshBtn();
		    loadNewOrLatestSessionForm();
	    },
	    error: function() {
	       connectionError();
	    }
	 });	
	/*$.get('current_session/current_session.tpl.html', function(template) {
	    var rendered = Mustache.render(template, data);
	    $('#target').html(rendered);
	  });*/	
}

function registerWifiSSIDListRefreshBtn(){
	initSSIDList();
	$('#refresh_ssid_list_btn').click(initSSIDList);	
}

function initSSIDList(){
	$('#wifi_ssid').empty();
	$.ajax({
	    url : 'rest/sensor/ssid_list',
	    type : "get",
	    async: true,
	    success : function(ssidList) {
			if( isSet(ssidList) ){
				$.each(ssidList, function (i, ssid) {
				    $('#wifi_ssid').append($('<option>', { 
				        value: ssid,
				        text : ssid 
				    }));
				});				
			}
	    },
	    error: function() {
	       connectionError();
	    }
	});
	
/*	$.get('rest/sensor/ssid_list', function(ssidList) {
		if( isSet(ssidList) ){
			ssidList.forEach( function(ssid) {
				$("#wifi_ssid").append(ssid);
			});
		}
	  });
	  */	
}
function loadNewOrLatestSessionForm(){
	var tplName = "current_session/create_session_form.tpl.html";
	var latestSession; 

	var selectedGroundPlanId = getUrlParameter("ground_plan_id");
	if( !isSet(selectedGroundPlanId) ){
		latestSession= getLatestSessionFromDB();
		if( isSet(latestSession) ){
			tplName = "current_session/new_or_load_latest_session.tpl.html";
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
	$.get('current_session/sensor_add_new_form_card_column.tpl.html', function(template) {
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
		    url : "current_session/create_session_form.tpl.html",
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

function loadSessionSensorChartList(i_sessionid){
	$.ajax({
	    url : "rest/planing_session/sensors/" + i_sessionid,
	    type : "get",
	    async: false,
	    success : function(resultSensorList) {
				$.ajax({
				    url : "current_session/sensor_chart_galery_elements.tpl.html",
				    type : "get",
				    async: false,
				    success : function(template) {
				    	var data = { sensor_chart_list: [] };
				    	resultSensorList.forEach( function(sensor, idx, origList) { 
				    		data.sensor_chart_list[data.sensor_chart_list.length] = { id: sensor.id,
				    																  name: sensor.name,
				    															      signal_dbm: -70,
				    															      signal_quality_pct: 50,
				    																};
				    	} );
					    var rendered = Mustache.render(template, data);
					    $('#sensor_chart_list').html(rendered);
					    
					    $(".pie_progress").asPieProgress({namespace: "pie_progress"})
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

function addSensorToChartList(i_id, i_name){
	$.ajax({
	    url : "current_session/sensor_chart_list_element.tpl.html",
	    type : "get",
	    async: false,
	    success : function(template) {
    		data = { sensor_chart_list : { id: i_id,
										   name: i_name,
									       signal_dbm: -70,
									       signal_quality_pct: 50,
										}
    		};
		    var rendered = Mustache.render(template, data);
		    $('#sensor_chart_list_card_collumns').append(rendered);
	    },
	    error: function(data) {
	       ajaxError();
	    }
	 });	
}

function initSensors(i_session){
	if( isSet(i_session) ){
		$.ajax({
		     async: false,
		     type: 'GET',
		     url: 'rest/sensor/sensors/' + i_session.id,
			 success : function(sensorList) {
				 sensorList.forEach( function(sensor, idx, origList) { 
						addSensor(  sensor //sensor.id, //i_id 
								//sensor.ipaddress, //i_ip 
								//sensor.name, //i_name
								//50, //x
								//50 //y
							);
						registerSensorSelection(sensor.id);

					} );
			    },
			 error: function(result) {
			    	console.log("Error: "+ result + "<b>Failed to get Senspors for Session!(URL = 'rest/sensor/sensors/" + i_session.id + "'");
			    }
		});
	}
}

function addSensor( i_sensor ){//i_id, i_ip, i_name, i_x, i_y){
	g_sensorList.push( {
		sensorDBObj: i_sensor,
		id: i_sensor.id,
		ip: i_sensor.ipaddress,
		name: i_sensor.name,
		heatmapData: {
			x: isSet(i_sensor.mapposx)? i_sensor.mapposx: 50,
			y: isSet(i_sensor.mapposy)? i_sensor.mapposy: 50,
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
	} );
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
	if( isSet( newHeatMapData ) ){
	    g_heatmap.setData(newHeatMapData);        
	}
};

function getWifiSignalHeatMapDataFromAllSensors(){
	if(isSet(g_sensorList) && g_sensorList.length > 0){
		var resWifiSignalDataPoints = getWifiSignalHeatMapDataPoints();
		var res = { 
			min: g_sensorList[0].heatmapData.wifiSignal_dbm.min,
			max: g_sensorList[0].heatmapData.wifiSignal_dbm.max,
			data: resWifiSignalDataPoints
			};
		
		return res;
	}
	return null;
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
	if(isSet(g_sensorList)){
		g_sensorList.forEach( function(sensor, idx, origList) { 
			refreshWifiSignalValueAsync(sensor); 
		} );
	}
};

function refreshWifiSignalValueAsync(i_sensor){
	if( isSet(i_sensor) && 
		isSet(i_sensor.ip) && 
		i_sensor.ip.length >= 7 &&
		isActiveSensor( i_sensor.id ) ){
		
		var urlGetRSSI = 'http://' + i_sensor.ip + '/rssi';
		return $.ajax({
		     async: true,
		     type: 'GET',
		     url: urlGetRSSI,
		     timeout: 1500,
		     success: function(i_dbmRSSIValue) {
		    	var newVal =  parseInt(i_dbmRSSIValue);
		    	updateSensorChartAndHeatmapRSSIDisplayValue(i_sensor, newVal);
		     },
			
			error: function(){
				// if sensor value is not retreivable we lost the connection => asumeing signal loss
		    	updateSensorChartAndHeatmapRSSIDisplayValue(i_sensor, 
		    												i_sensor.heatmapData.wifiSignal_dbm.min);
		    	//updateSensorChartAndHeatmapRSSIDisplayValue(i_sensor, 
					//	i_sensor.heatmapData.wifiSignal_dbm.min + 10);
			}
		});	
	}
}

function updateSensorChartAndHeatmapRSSIDisplayValue(i_sensor, i_dbmRSSIValue){
	var newVal =  i_dbmRSSIValue;
	i_sensor.heatmapData.wifiSignal_dbm.value = chopValueOnMinMaxBorder(  i_sensor.heatmapData.wifiSignal_dbm.min, 
																		  i_sensor.heatmapData.wifiSignal_dbm.max,
																		  i_dbmRSSIValue );
	
	var quality =  ((i_dbmRSSIValue - i_sensor.heatmapData.wifiSignal_dbm.min) / (i_sensor.heatmapData.wifiSignal_dbm.max - i_sensor.heatmapData.wifiSignal_dbm.min))*100;
	$('#sensor_chart_'+ i_sensor.id).asPieProgress("go", quality);
	$('#sensor_chart_dbm_'+ i_sensor.id).text( i_dbmRSSIValue + ' dbm' );
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
