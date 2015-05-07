/**
 * jQuery Text Summarizer
 *
 */
(function($) {
  $(".min-max").live("click", function(e) {
    var minMax = $(this);
    var text = minMax.parent().find('#text');
    minMax.toggleClass('ui-icon-min');
    minMax.toggleClass('ui-icon-max');
    text.toggle();
    text.parent().find("#summary").toggle();
    e.stopPropagation();
    e.preventDefault();
  });

  $.fn.summarize = function(options) {

    var settings = $.extend({}, $.fn.summarize.defaults, options);
    var numLines = settings["lines"];
    var more = settings["more"];
    var width = settings["width"]

    return this.each(function() {
      // initialize with summary
      var div = $(this).parent().find('#text');

      // check if there is already a summary
      div.parent().find("#summary").remove();

      var text = div.html().split('</p>')[0];//div.text();
      var divWidth = width ? width : div.width();
      div.after('<span id="fonttest" style="width: auto;">abcdef ghijkl mnopqrstuvw xyz1234567890! ,;.?</span>');
      var testNode = div.parent().find("#fonttest");
      var averageWidth = testNode.width() / 45;
      testNode.detach();
      var averageNumCharsPerLine = divWidth / averageWidth;
      var totalNumChars = averageNumCharsPerLine * numLines;

      var summaryText = jQuery.trim(text);
      if (summaryText.length > 0)
      {
        if (text.length > totalNumChars)
        {
          while (totalNumChars > 0 && text.charAt(totalNumChars) != ' ') {
            totalNumChars--;
          }
          summaryText = text.substr(0, totalNumChars) + ' ' + more;
        }
      }
      div.after('<div id="summary" class="sampleset-content summary">' + summaryText + '</div>');

      var isMinimized = $(this).hasClass('ui-icon-min');
      div.toggle(!isMinimized);
      div.parent().find("#summary").toggle(isMinimized);

//      console.log('Div Width = ' + divWidth);
//      console.log('Average Width = ' + averageWidth);
//      console.log('Num of Chars Per Line = ' + averageNumCharsPerLine);
//      console.log('Total Num of Chars = ' + totalNumChars);
    });
  };

  // publicly accessible defaults
  $.fn.summarize.defaults = {
    lines   : 3,
    more    : '...'
  };

})(jQuery);