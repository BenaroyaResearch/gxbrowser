/*
 * MAT plot drawing using Raphael
 * Data expected to be in the following format:
 *
 * data[1] = { 1:
 *
 */
$(function() {

  var modulePlotColorKey = {
    over: {
      0: "#FFFFFF", 10: "#", 20: "#", 30: "#", 40: "#",
      50: "#", 60: "#", 70: "#", 80: "#", 100: "#"
    },
    under: {
      0: "#FFFFFF", 10: "#", 20: "#", 30: "#", 40: "#",
      50: "#", 60: "#", 70: "#", 80: "#", 100: "#"
    }
  };

  var gsaPlotColorKey = {
    over: {
      p001: "#FFFFFF", p01: "#", p03: "#", p05: "#"
    },
    under: {
      p001: "#FFFFFF", p01: "#", p03: "#", p05: "#"
    }
  };

  /*
   * Defaults
   */
  var defaultOptions = {
    // data
    data: [],
    container: "chart",
    // sizing
    width: 800,
    height: 600,
    marginLeft: 50,
    marginRight: 50,
    scale: 1,
    // font
    font: {
      family: "Fontin-Sans, Arial",
      size: "10px",
      color: "#333333"
    },
    mouseover: null,
    click: null
  };

  var _renderFrame = function(options) {

  };

  var _renderAxes = function(options) {

  };

  var _renderData = function(options) {

  };

  var draw = function(options) {
    var plotOptions = $.extend({}, defaultOptions, options);
    _renderFrame(options);
    _renderAxes(options);
    _renderData(options);
  };

  var fitToScreen = function(container) {

  };

  var scale = function(container,scaleValue) {

  };

  var download = function(container) {

  };

});