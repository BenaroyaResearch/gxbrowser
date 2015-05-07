

var parseDatasetNameBIIR = function(name)
{
	var regexp = /^(BIIR.+|P\d+)_(\d{4}DS\d+).+/i;

	var results = name.match(regexp);

	if (results == null)
	{
		//console.log("failed:", name);
		return name;
	}

	//no datasets generated before year 2000, no dataset Ids before 1

	//assume we wonÕt be running this 11,881,376 years in the future ;)
	var numYearPlaces = 5;
	//assume no more than 11,881,376 datasets generated per year ;)
	var numDatasetIdPlaces = 5;

	//setting 1 -> n to 0 -> n-1
	var yearDiff = parseInt(results[2].replace(/DS\d+/, "")) - 2000;
	var datasetId = parseInt(results[2].replace(/\d{4}DS/, "")) - 1;

	var yearText = yearDiff.toString(); //decimalToString(yearDiff);
	var datasetIdText = datasetId.toString(); //decimalToString(datasetId);

	//console.log("name: ", name);
	//console.log("parsed year: ", yearDiff);
	//console.log("parsed ugghh: ",datasetId);
	var result = yearText.padLeft(numYearPlaces - yearText.length, "0") + datasetIdText.padLeft(numDatasetIdPlaces - yearText.length, "0");
	//console.log("parsed name: ", result);
	return result;
};

String.prototype.padLeft = function(width, character)
{
	if (!character)
	{
		character = " ";
	}

	return Array(width - String(this).length + 1).join(character) + this;
};

var decimalToString = function(positiveNum)
{
	var string = "";
	var quotient = positiveNum;
	while (quotient != 0)
	{
		var remainder = quotient % 26;
		quotient /= 26;

		string = String.fromCharCode(remainder + 96) + string;
	}

	return string;
};

$.tablesorter.addParser({
    id: "biirTitleSort",
    is: function(s) { return false; },
    format: function(s) { return parseDatasetNameBIIR(s); },
    type: "text"
});
