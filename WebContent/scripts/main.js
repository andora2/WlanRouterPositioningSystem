var g_heatmap;
var g_sensorList = [];
var g_refreshHeatMapInterval;
var g_REFRESH_RATE_MS = 1000;

window.onload = function() {
	g_heatmap = initHeatMap();
	initSensors();
	g_refreshHeatMapInterval = startHeatMapAutoRefresh(g_REFRESH_RATE_MS);
	console.log(navigator.connection);
};

function initSensors(){
	addSensor(  1, //i_id 
				"192.168.0.122", //i_ip 
				"First Sesnor", //i_name
				"No description yet", //i_description
				50, //x
				50 //y
			);
	
	/*addSensor(  2, //i_id 
			"192.168.0.121", //i_ip 
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
				min: -90,
				max: -50,
				value: -90
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

