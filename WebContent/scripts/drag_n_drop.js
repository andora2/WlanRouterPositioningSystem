$(window).load(function() {

var canvas = document.getElementById("myCanvas");
var context = canvas.getContext("2d");


context.font = "38pt Arial";
context.fillStyle = "cornflowerblue";
context.strokeStyle = "blue";



function drawCircle(e){
    var loc = windowtoCanvas(canvas, e.clientX, e.clientY);

    console.log(loc);

    context.beginPath();

	context.fillStyle = "#ffffff";
	context.strokeStyle = "#125512";
	context.lineWidth = 1; 

	context.arc(loc.x, loc.y, randRange(10, 24), 0, Math.PI*2);
	context.fill();
	context.stroke();

}

function windowtoCanvas(canvas, x, y){
	var bbox = canvas.getBoundingClientRect();
	return{x: x- bbox.left*(canvas.width/bbox.width), y: y - bbox.top*(canvas.height/bbox.height) 

	}

}

function randRange(min, max){
	return Math.floor(Math.random()*(max - min +1)) + min;

}

canvas.addEventListener("mousemove", drawCircle);

    
});