(function() {
		var moment;
 
		moment = typeof require !== "undefined" && require !== null ? require("moment") : this.moment;

		function _workingDays (startDate, endDate) {
  	     
	        // Calculate days between dates
	        var millisecondsPerDay = 86400 * 1000; // Day in milliseconds
	        startDate.hours(0).minutes(0).seconds(0).milliseconds(1);  // Start just after midnight
	        endDate.hours(23).minutes(59).seconds(59).milliseconds(999);  // End just before midnight
	        var diff = moment.duration(endDate - startDate);  // Milliseconds between moment objects   
	        var days = Math.ceil(diff.asMilliseconds() / millisecondsPerDay);
	
	        // Subtract two weekend days for every week in between
	        var weeks = Math.floor(days / 7);
	        var days = days - (weeks * 2);
	
	        // Handle special cases
	        var startDay = startDate.day();
	        var endDay = endDate.day();
	
	        // Remove weekend not previously removed.   
	        if (startDay - endDay > 1)         
	            days = days - 2;      
	
	        // Remove start day if span starts on Sunday but ends before Saturday
	        if (startDay == 0 && endDay != 6)
	            days = days - 1  
	
	        // Remove end day if span ends on Saturday but starts after Sunday
	        if (endDay == 6 && startDay != 0)
	            days = days - 1  
	
	        return days;
		}

		 moment.workingDays = function(first, second) {
			 return (_workingDays(first, second));
		};
			  
		if ((typeof module !== "undefined" && module !== null ? module.exports : void 0) != null) {
			    module.exports = moment;
		}


}(this));