window.onload=function(){
	function generateRandomData(len){
		var points=[];
		var max=0;
		var width=840;
		var height=400;
		while(len--){
			var val=Math.floor(Math.random()*100);
			max=Math.max(max,val);
			var point={
					x:Math.floor(Math.random()*width),
					y:Math.floor(Math.random()*height),
					value:val};
			points.push(point);
		}
	}
	var data={
			max:max,
			data:points
			};
	return data;
}
var heatmapInstance=h337.create(
		{
			container:document.querySelector('.heatmap')
		}
	);