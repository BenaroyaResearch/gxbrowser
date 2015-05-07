function GanttChart(elementId){
  this.elementId = elementId;
  this.height = 500;
  this.width = 500;
  this.gridSize = 25;
  this.labelAreaSize = 300;
  this.currentRow = 1; // Starts at 1 so there is an empty row for headers
  this.phaseColor = "#AAA";
  //this.taskColors = ["blue", "green", "orange", "red", "yellow", "#C24704","#D9CC3C","#FFEB79","#A0E0A9","#00ADA7"];
  this.taskColors = ["Aqua", "Blue", "Fuchsia",	"Green", "Lime", "Maroon", "Navy", "Olive", "Purple", "Red", "Teal", "Yellow"];
  this.cellpadding = 4;

  var colorCounter = 0; //Math.floor(Math.random() * this.taskColors.length); //0;

  this.loadData = function(payload){
    this.project = payload;
  }

  this.draw = function(){
    // Get the list of dates for the project duration
    var dates = this.getDates();
    // Calculate total graph width
    width = this.labelAreaSize + dates.length * this.gridSize;
    var rows = this.getRows();
    height = rows*this.gridSize;

    // Generate the chart area
    this.paper = Raphael(this.elementId, width, height);

    this.grid();
    this.title(this.project[0].name);
    this.headers(dates);

    // Iterate through all the phases, print labels and child tasks
    var phases = this.project[0].phases;
    for(p in phases){
      var caption = this.paper.text(5,(this.currentRow*this.gridSize)+(this.gridSize/2),phases[p].name);
      caption.attr({"font-size":14 , "stroke":"none" , "fill":"black", "text-anchor":"start"});

      //TODO print phase bounds
      this.phaseLine(phases[p]);

      this.currentRow++;

      var tasks = phases[p].tasks;
      for(t in tasks){
        this.bar(tasks[t]);
        this.currentRow++;
      }

      // Cycle the selected color, tasks under a phase will have the same color
      if(colorCounter < this.taskColors.length - 1){
        colorCounter++;
      }else{
        colorCounter = 0;
      }
    }

  }

  // Gets the total number of rows
  this.getRows = function(){
    var phases = this.project[0].phases || null;
    var rows = 1;
    var i = 0;
    var startDate = moment(this.project[0].startDate);
    var endDate = moment(this.project[0].endDate);
    // check to see that the project is within the time bounds here.
    for (var p = 0; p < phases.length; p++){
    	var pStartDate = moment(phases[p].startDate);
    	var pEndDate = moment(phases[p].endDate);
    	//console.log("pName: " + phases[p].name + " startDate: " + startDate + " pEndDate: " + pEndDate);
    	if (pEndDate.isAfter(startDate, 'date')) {
    		//console.log("...passed");
    		rows++;
    		//var tasks = phases[p].tasks;
    		for (var t = 0; t < phases[p].tasks.length; t++){
    			var tStartDate = moment(phases[p].tasks[t].startDate);
    			var tEndDate = moment(phases[p].tasks[t].endDate);
    			if (tEndDate.isAfter(startDate, 'date') && tStartDate.isBefore(endDate, 'date')) {
    				rows++;
    			} else {
    	    		phases[p].tasks.splice(t, 1);	// remove
    	    		t--;							// fixup
    			}
    		}
    	} else {
    		phases.splice(p, 1);	// remove
    		p--;					// fixup
    		//console.log("...failed");
    	}
    }
    return rows;
  }

  // Draw the grid
  this.grid = function(){
    // Draw horizontal grid lines
    for(var y=this.gridSize; y < height; y+=this.gridSize){
      var pathExp = "M0," + y + "L" + width + "," + y;
      var gridLine = this.paper.path(pathExp);
      gridLine.attr({"stroke":"#000"});
    }
    
    // Today:
    var dates = this.getDates();
    var dayDiff = moment.duration(new Date() - dates[0]).asDays()
    if (dayDiff > 0){
    	var x =  this.labelAreaSize + (Math.floor(dayDiff) * this.gridSize) + this.gridSize * 0.5
    	var pathExp = "M" + x + ",0L" + x + "," + height;
    	var gridLine = this.paper.path(pathExp);
    	gridLine.attr({"stroke":"#f00", "stroke-width":"2"});
    }

    //Draw vertical grid lines (starts at the edge of the space allotted for phase/task labels)
    var calDate = dates[0].clone();
    for(var x=this.labelAreaSize; x < width; x+=this.gridSize){
      if (calDate.day() == 0 || calDate.day() == 6) {
          var pathExp = "M" + x + ",0L" + x + "," + height;
          var gridRect = this.paper.rect(x, 0, this.gridSize, height);
          gridRect.attr({"stroke":"#000", "fill": "#070", "opacity":0.1});
      }
      var pathExp = "M" + x + ",0L" + x + "," + height;
      var gridLine = this.paper.path(pathExp);
      gridLine.attr({"stroke":"#000"});

      calDate.add("days", 1)
    }
  }

  //Draws label title
  this.title = function(title) {
	  var description = this.paper.text(0.5*this.labelAreaSize, 0.5*this.gridSize, title)
      description.attr({"font-size":16, "text-anchor":"middle"});
  }
  
  // Draws column headers, currently using M-D formatting
  this.headers = function(dates){
    var currentCol = 0;
    for(d in dates){
      var heading = this.paper.text(this.labelAreaSize + currentCol*this.gridSize + 0.5*this.gridSize, 0.5*this.gridSize,dates[d].format("M-D"));
      heading.attr({"text-anchor":"middle"});
      currentCol++;
    }
  }

  // Draw phase bounding shape
  this.phaseLine = function(phase){
    var dates = this.getDates();
    var startDate = moment(phase.startDate);
    var endDate = moment(phase.endDate);
    var hasBegun = false;
    endDate.add("days",1); // Add an extra day so that the bar actually encompasses the end date

    var offset = moment.duration(startDate - dates[0]);
    // detect project already started
    if (offset.asDays() < 0) {
    	hasBegun = true;
    	startDate = dates[0]
    	offset = moment.duration(startDate - dates[0]);
    }
    var x = offset.asDays()*this.gridSize+this.labelAreaSize+this.cellpadding;
    var y = this.currentRow*this.gridSize+.25*this.gridSize;
    var duration = moment.duration(endDate - startDate);
    
    if (duration.asDays() < 0) {
    	return; // nothing to draw?
    }
    var barWidth = (duration.asDays())*this.gridSize-(2*this.cellpadding);
    var barHeight = this.gridSize/4;

    var bar = this.paper.rect(x, y, barWidth, barHeight);
    bar.attr({"stroke":this.phaseColor});
    bar.attr({"fill":this.phaseColor});
    if (phase.href) {
    	bar.attr({"href": phase.href})
    }

    // Draw the starting bound triangle
    if (!hasBegun) {
    	var p1 = x+","+y;
    	var p2 = x+","+((y+.75*this.gridSize)-this.cellpadding);
    	var p3 = (x+.5*this.gridSize)+","+y;
    	var startTriangle = this.paper.path("M"+p1+"L"+p2+"L"+p3+"Z");
    	startTriangle.attr({"stroke":this.phaseColor,"fill":this.phaseColor});
    }

    // Draw the ending bound triangle
    var p1 = x+barWidth+","+y;
    var p2 = x+barWidth+","+((y+.75*this.gridSize)-this.cellpadding);
    var p3 = ((x+barWidth)-.5*this.gridSize)+","+y;
    var endTriangle = this.paper.path("M"+p1+"L"+p2+"L"+p3+"Z");
    endTriangle.attr({"stroke":this.phaseColor,"fill":this.phaseColor});

  }

  // Draws a task bar
  this.bar = function(task){
    var dates = this.getDates();
    var startDate = moment(task.startDate);
    var endDate = moment(task.endDate);
    endDate.add("days",1); // Add an extra day so that the bar actually encompasses the end date

    var offset = moment.duration(startDate - dates[0]);
    // detect project already started
    if (offset.asDays() < 0) {
    	startDate = dates[0]
    	offset = moment.duration(startDate - dates[0]);
    }

    var x = offset.asDays()*this.gridSize+this.labelAreaSize;
    var y = this.currentRow*this.gridSize;
    var duration = moment.duration(endDate-startDate);
    
    if (duration.asDays() < 0) {
    	return; // nothing to draw?
    }

    var barWidth = (duration.asDays())*this.gridSize;
    var barHeight = this.gridSize;

    var bar = this.paper.rect(x+this.cellpadding, y+this.cellpadding, barWidth-(2*this.cellpadding), barHeight-(2*this.cellpadding));
    var caption = this.paper.text(10,y+(barHeight/2),task.name);
    caption.attr({"font-size":14 , "stroke":"none" , "fill":"black", "text-anchor":"start"});
    var colorString = this.taskColors[colorCounter];
    if (task.color) {
    	colorString = task.color
    }
    var fill = Raphael.color(colorString);
    var count = moment.workingDays(startDate, endDate.subtract("days", 1))
    bar.attr({"fill":fill, "title": count + (count > 1 ? " working days" : " working day") } );
    if (task.href) {
    	bar.attr({"href": task.href})
    }

    // Add glow effect on mouseover
    var glow;
    var glowRadius = this.cellpadding;
    bar.hover(function(){
      glow = bar.glow({"width":glowRadius,"opacity":0.75,"color":fill});
    }, function(){ // Remove glow on mouseout
      glow.remove();
    });

  }

  // Returns an array of dates for the duration of the project
  this.getDates = function(){
    //TODO Parse start/end dates from data
    var startDate = moment(this.project[0].startDate);
    var endDate = moment(this.project[0].endDate);

    var dates = [];

    while(startDate <= endDate){
      dates.push(startDate.clone());
      startDate.add("days",1);
    }

    return dates;
  }

}

