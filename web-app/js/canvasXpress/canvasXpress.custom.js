/*
 * CanvasXpress 3.8 - JavaScript Canvas Library
 *
 * Copyright (c) 2009-2010 Isaac Neuhaus
 *
 * imnphd@gmail.com
 *
 * Redistributions of this source code must retain this copyright
 * notice and the following disclaimer.
 *  
 * CanvasXpress is licensed under the terms of the Open Source
 * LGPL 3.0 license. 
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Commercial use is permitted to the extent that this source code
 * do NOT become part of any other Open Source or Commercially licensed
 * development library or toolkit without explicit permission.
 * 
 * Network graphs were implemented based on the HeyGraph by Tom Martin
 * <http://www.heychinaski.com>.
 *
 * Thanks to Mingyi Liu for his contributions in extending the Ext panel
 * and adding major capabilities to the network graphs and animations.
 *
 */
if (typeof (CanvasXpress) == "undefined") {
    CanvasXpress = {}
}
var CanvasXpress = function (f, e, a, b, d) {
        if (!f) {
            document.body.appendChild(c);
            f = this.createNewTarget()
        } else {
            if (typeof (f) == "object") {
                e = f.data || false;
                a = f.config || false;
                b = f.events || false;
                d = f.hidden || false;
                f = f.renderTo || this.createNewTarget()
            }
        }
        if (!a) {
            a = {};
            this.userConfig = {}
        } else {
            this.userConfig = a
        }
        this.createNewTarget = function () {
            var k = document.createElement("canvas");
            var j = 0;
            var g = "canvasXpress" + j;
            var h = document.getElementById(g);
            while (h) {
                j++;
                g = "canvasXpress" + j;
                h = document.getElementById(g)
            }
            k.id = g;
            document.body.appendChild(k);
            return g
        };
        this.insertTarget = function (j, l, i, k, g) {
            if (j && l) {
                var m = document.getElementById(j);
                if (m) {
                    return
                } else {
                    m = document.createElement("canvas")
                }
                m.id = j;
                m.width = i;
                m.height = k;
                if (g) {
                    l.parentNode.insertBefore(m, l.nextSibling)
                } else {
                    l.parentNode.insertBefore(m, l)
                }
            }
        };
        this.removeTarget = function (g) {
            var h = document.getElementById(g);
            if (h) {
                h.parentNode.removeChild(h)
            }
        };
        this.setConfig = function (g) {
            if (a.graphType == "Network") {
                if (!a.backgroundType) {
                    this.backgroundType = "gradient";
                    this.nodeFontColor = this.background
                }
            }
            if (a) {
                for (var h = 0; h < g.length; h++) {
                    var j = g[h];
                    if (a.hasOwnProperty(j)) {
                        this[j] = a[j]
                    }
                }
            }
            if (this.isVML) {
                this.showAnimation = false
            }
        };
        this.setInitialConfig = function () {
            if (a) {
                for (var g in a) {
                    this[g] = a[g]
                }
            }
        };
        this.resetConfig = function (g) {
            var j = {};
            for (var h = 0; h < g.length; h++) {
                j[g[h]] = this[g[h]]
            }
            this.initConfig(true);
            for (var h in j) {
                this[h] = j[h]
            }
        };
        this.updateConfig = function (h) {
            if (h) {
                this.userConfig = h;
                for (var g in h) {
                    this[g] = h[g]
                }
            }
        };
        this.getConfig = function () {
            var k = {};
            for (var h = 0; h < this.config.length; h++) {
                var g = this.config[h];
                if (this[g] != undefined) {
                    k[g] = this[g]
                }
            }
            return k
        };
        this.getUserConfig = function () {
            return this.userConfig
        };
        this.updateData = function (g) {
            this.initData(g);
            this.initEvents();
            this.initGraph()
        };
        this.updateColors = function (c) {
            this.colors = c;
            this.initGraph();
        }
        this.save = function () {
            return {
                renderTo: this.target,
                data: this.data,
                config: this.getConfig(),
                events: this.events
            }
        };
        this.writeToConsole = function (g, j) {
            for (var h = 0; h < g.length; h++) {
                if (j) {
                    console.log(this.target + ": " + g[h])
                } else {
                    console.log(this.target + ": " + g[h] + " = " + this[g[h]])
                }
            }
        };
        this.dumpToConsole = function (g) {
            console.log(Dumper(g, DumperGetArgs(arguments, 1)))
        };
        this.isSubstandardBrowser = function () {
            if ((this.browser == "Firefox" && this.browserVersion > 2) || (this.browser == "Opera" && this.browserVersion > 9) || (this.browser == "Safari" && this.browserVersion > 4) || (this.browser == "Chrome" && this.browserVersion > 1)) {
                return false
            } else {
                return true
            }
        };
        this.isMobileApp = function () {
            if (this.browser.match(/iPhone|iPod|iPad|Android|BlackBerry/i)) {
                return true
            } else {
                return false
            }
        };
        this.setCanvas = function () {
            this.version = 3.8;
            this.target = f;
            this.events = b;
            var g = document.getElementById("wrapper-" + this.target);
            if (!g) {
                var i = document.createElement("div");
                i.id = "wrapper-" + this.target;
                i.style.position = "relative";
                i.style.cssFloat = "left";
                if (d) {
                    i.style.display = "none"
                }
                var j = document.getElementById(this.target);
                var h = j.parentNode;
                h.insertBefore(i, j);
                i.appendChild(j.parentNode.appendChild(j))
            }
            if (window.G_vmlCanvasManager && window.attachEvent && !window.opera) {
                this.isIE = true;
                this.subBrowser = true;
                this.mobileApp = false;
                if (this.browserVersion >= 9) {
                    this.isVML = false;
                    this.canvas = j
                } else {
                    this.isVML = true;
                    this.canvas = window.G_vmlCanvasManager.initElement(j)
                }
            } else {
                this.isIE = false;
                this.subBrowser = this.isSubstandardBrowser();
                this.mobileApp = this.isMobileApp();
                this.isVML = false;
                this.canvas = j
            }
            this.ctx = this.canvas.getContext("2d")
        };
        this.setPath = function () {
            var j = document.getElementsByTagName("script");
            if (j) {
                for (var h = 0; h < j.length; h++) {
                    if (!j[h].src) {
                        continue
                    }
                    var k = j[h].src;
                    var g = k.lastIndexOf("/");
                    var m = k.substring(0, g + 1);
                    var l = k.substring(g + 1);
                    if (l.match(/canvasXpress/)) {
                        this.path = m
                    }
                }
            }
        };
        this.initialize = function () {
            this.initBrowser();
            this.addCSS();
            this.setCanvas();
            this.setPath();
            this.initConfig();
            this.initData(e);
            this.initLayout();
            this.initEvents();
            this.initAnimation();
            this.initRemote();
            this.initGraph()
        };
        this.initialize();
        CanvasXpress.references.push(this)
    };
CanvasXpress.references = [];
CanvasXpress.prototype.addCSS = function () {
    var a = "table.shorts {border: 1px solid; border-collapse: collapse; font-size: x-small}";
    a += "td.k {text-align: center; padding: 2px 5px; margin: 2px 5px; border: 1px solid;}";
    a += "td.d {padding: 2px 5px; margin: 2px 5px; border: 1px solid;}";
    a += "th.d {padding: 2px 5px; margin: 2px 5px; border: 1px solid;}";
    var b = document.createElement("style");
    b.type = "text/css";
    if (b.styleSheet) {
        b.styleSheet.cssText = a
    } else {
        b.appendChild(document.createTextNode(a))
    }
    document.getElementsByTagName("head")[0].appendChild(b)
};
CanvasXpress.prototype.initBrowser = function () {
    this.dataBrowser = [{
        string: navigator.userAgent,
        subString: "Chrome",
        identity: "Chrome"
    }, {
        string: navigator.userAgent,
        subString: "OmniWeb",
        versionSearch: "OmniWeb/",
        identity: "OmniWeb"
    }, {
        string: navigator.vendor,
        subString: "Apple",
        identity: "Safari",
        versionSearch: "Version"
    }, {
        prop: window.opera,
        identity: "Opera"
    }, {
        string: navigator.vendor,
        subString: "iCab",
        identity: "iCab"
    }, {
        string: navigator.vendor,
        subString: "KDE",
        identity: "Konqueror"
    }, {
        string: navigator.userAgent,
        subString: "Firefox",
        identity: "Firefox"
    }, {
        string: navigator.vendor,
        subString: "Camino",
        identity: "Camino"
    }, {
        string: navigator.userAgent,
        subString: "Netscape",
        identity: "Netscape"
    }, {
        string: navigator.userAgent,
        subString: "MSIE",
        identity: "Explorer",
        versionSearch: "MSIE"
    }, {
        string: navigator.userAgent,
        subString: "Gecko",
        identity: "Mozilla",
        versionSearch: "rv"
    }, {
        string: navigator.userAgent,
        subString: "Mozilla",
        identity: "Netscape",
        versionSearch: "Mozilla"
    }, {
        string: navigator.userAgent,
        subString: "iPhone",
        identity: "iPhone"
    }, {
        string: navigator.userAgent,
        subString: "iPod",
        identity: "iPod"
    }, {
        string: navigator.userAgent,
        subString: "iPad",
        identity: "iPad"
    }, {
        string: navigator.userAgent,
        subString: "Android",
        identity: "Android"
    }, {
        string: navigator.userAgent,
        subString: "BlackBerry",
        identity: "BlackBerry"
    }];
    this.dataOS = [{
        string: navigator.platform,
        subString: "Win",
        identity: "Windows"
    }, {
        string: navigator.platform,
        subString: "Mac",
        identity: "Mac"
    }, {
        string: navigator.userAgent,
        subString: "iPhone",
        identity: "iPhone"
    }, {
        string: navigator.userAgent,
        subString: "iPod",
        identity: "iPod"
    }, {
        string: navigator.userAgent,
        subString: "iPad",
        identity: "iPad"
    }, {
        string: navigator.userAgent,
        subString: "Android",
        identity: "Android"
    }, {
        string: navigator.userAgent,
        subString: "BlackBerry",
        identity: "BlackBerry"
    }, {
        string: navigator.platform,
        subString: "Linux",
        identity: "Linux"
    }];
    this.searchString = function (e) {
        for (var a = 0; a < e.length; a++) {
            var b = e[a].string;
            var d = e[a].prop;
            this.versionSearchString = e[a].versionSearch || e[a].identity;
            if (b) {
                if (b.indexOf(e[a].subString) != -1) {
                    return e[a].identity
                }
            } else {
                if (d) {
                    return e[a].identity
                }
            }
        }
    };
    this.searchVersion = function (b) {
        var a = b.indexOf(this.versionSearchString);
        if (a == -1) {
            return
        }
        return parseFloat(b.substring(a + this.versionSearchString.length + 1))
    };
    this.initializeBrowser = function () {
        this.browser = this.searchString(this.dataBrowser) || "An unknown browser";
        this.browserVersion = this.searchVersion(navigator.userAgent) || this.searchVersion(navigator.appVersion) || "an unknown version";
        this.browserOS = this.searchString(this.dataOS) || "an unknown OS"
    };
    this.initializeBrowser()
};
CanvasXpress.prototype.initConfig = function (a) {
    this.config = [];
    this.graphType = "Bar";
    this.config.push("graphType");
    this.graphOrientation = "horizontal";
    this.config.push("graphOrientation");
    this.foreground = "rgb(0,0,0)";
    this.config.push("foreground");
    this.foregroundWindow = "rgb(0,0,0)";
    this.config.push("foregroundWindow");
    this.background = "rgb(255,255,255)";
    this.config.push("background");
    this.backgroundWindow = "rgb(255,255,255)";
    this.config.push("backgroundWindow");
    this.backgroundType = "solid";
    this.config.push("backgroundType");
    this.backgroundImage = false;
    this.config.push("backgroundImage");
    this.backgroundWindowGradientOrientation = "vertical";
    this.config.push("backgroundWindowGradientOrientation");
    this.backgroundGradient1Color = "rgb(0,0,200)";
    this.config.push("backgroundGradient1Color");
    this.backgroundGradient2Color = "rgb(0,0,36)";
    this.config.push("backgroundGradient2Color");
    this.backgroundWindowGradient1Color = "rgb(0,0,200)";
    this.config.push("backgroundWindowGradient1Color");
    this.backgroundWindowGradient2Color = "rgb(0,0,36)";
    this.config.push("backgroundWindowGradient2Color");
    this.gradient = false;
    this.config.push("gradient");
    this.gradientType = "radial";
    this.config.push("gradientType");
    this.gradientRatio = 1.3;
    this.config.push("gradientRatio");
    this.transparency = null;
    this.config.push("transparency");
    this.margin = 5;
    this.config.push("margin");
    this.showShadow = false;
    this.config.push("showShadow");
    this.shadowOffsetX = 3;
    this.config.push("shadowOffsetX");
    this.shadowOffsetY = 3;
    this.config.push("shadowOffsetY");
    this.shadowBlur = 2;
    this.config.push("shadowBlur");
    this.shadowColor = "rgba(0,0,0,0.5)";
    this.config.push("shadowColor");
    this.functions = [];
    this.fonts = this.isVML || this.subBrowser ? ["Gentilis", "Helvetiker", "Optimer"] : ["Gentilis", "Helvetiker", "Optimer", "Verdana, sans-serif"];
    this.fontName = this.isVML ? "Gentilis" : this.subBrowser ? "Optimer" : "Verdana, sans-serif";
    this.config.push("fontName");
    this.fontStyle = false;
    this.config.push("fontStyle");
    this.fontSize = 12;
    this.config.push("fontSize");
    this.font = this.fontStyle ? this.fontStyle + this.fontSize + "pt " + this.fontName : this.fontSize + "pt " + this.fontName;
    this.align = "center";
    this.config.push("align");
    this.baseline = "middle";
    this.config.push("baseline");
    this.maxTextSize = 14;
    this.config.push("maxTextSize");
    this.minTextSize = 4;
    this.config.push("minTextSize");
    this.autoScaleFont = true;
    this.config.push("autoScaleFont");
    this.scaleTextConstantMult = 40;
    this.scaleTextConstantAdd = 2.5;
    this.title = false;
    this.config.push("title");
    this.titleHeight = 30;
    this.config.push("titleHeight");
    this.subtitle = false;
    this.config.push("subtitle");
    this.subtitleHeight = 16;
    this.config.push("subtitleHeight");
    this.dashLength = 8;
    this.config.push("dashLength");
    this.dotLength = 5;
    this.config.push("dotLength");
    this.arrowPointSize = 10;
    this.config.push("arrowPointSize");
    this.capType = "butt";
    this.config.push("capType");
    this.outlineWidth = 1;
    this.config.push("outlineWidth");
    this.lines = [];
    this.colorScheme = "user";
    this.config.push("colorScheme");
    this.colors = ["rgb(255,0,0)", "rgb(0,0,255)", "rgb(0,255,0)", "rgb(255,255,0)", "rgb(0,255,255)", "rgb(255,0,255)", "rgb(153,51,0)", "rgb(0,0,128)", "rgb(128,128,0)", "rgb(255,102,0)", "rgb(0,128,128)", "rgb(102,0,102)", "rgb(255,153,204)", "rgb(102,102,153)", "rgb(51,51,0)", "rgb(255,204,153)", "rgb(204,255,255)", "rgb(153,153,255)", "rgb(255,128,128)", "rgb(51,102,255)", "rgb(51,153,102)", "rgb(255,204,0)", "rgb(0,204,255)", "rgb(208,32,144)", "rgb(153,51,102)", "rgb(0,102,204)", "rgb(153,204,0)", "rgb(255,153,0)", "rgb(51,204,204)", "rgb(204,153,255)", "rgb(128,0,0)", "rgb(51,51,153)", "rgb(0,128,0)", "rgb(255,255,204)", "rgb(153,204,255)", "rgb(128,0,128)"];
    this.colors0 = ["rgb(0,0,255)", "rgb(255,0,77)", "rgb(255,191,0)", "rgb(116,255,0)", "rgb(164,0,255)", "rgb(255,116,0)", "rgb(255,255,0)", "rgb(0,255,255)", "rgb(255,0,0)", "rgb(255,211,0)", "rgb(61,0,255)", "rgb(0,255,0)", "rgb(255,0,255)", "rgb(255,146,0)", "rgb(211,255,0)", "rgb(0,144,255)", "rgb(109,0,255)", "rgb(255,73,0)", "rgb(255,232,0)", "rgb(0,255,146)", "rgb(255,0,144)", "rgb(255,170,0)", "rgb(170,255,0)", "rgb(0,77,255)"];
    this.colors1 = ["rgb(60,60,157)", "rgb(157,60,90)", "rgb(157,133,60)", "rgb(104,157,60)", "rgb(122,60,157)", "rgb(157,104,60)", "rgb(157,157,60)", "rgb(60,157,157)", "rgb(157,60,60)", "rgb(157,140,60)", "rgb(83,60,157)", "rgb(60,157,60)", "rgb(157,60,157)", "rgb(157,115,60)", "rgb(140,157,60)", "rgb(60,115,157)", "rgb(102,60,157)", "rgb(157,88,60)", "rgb(157,148,60)", "rgb(60,157,115)", "rgb(157,60,115)", "rgb(157,125,60)", "rgb(125,157,60)", "rgb(60,90,157)"];
    this.colors2 = ["rgb(0,0,117)", "rgb(117,0,36)", "rgb(117,88,0)", "rgb(53,117,0)", "rgb(76,0,117)", "rgb(117,53,0)", "rgb(117,117,0)", "rgb(0,117,117)", "rgb(117,0,0)", "rgb(117,97,0)", "rgb(28,0,117)", "rgb(0,117,0)", "rgb(117,0,117)", "rgb(117,67,0)", "rgb(97,117,0)", "rgb(0,66,117)", "rgb(50,0,117)", "rgb(117,33,0)", "rgb(117,107,0)", "rgb(0,117,67)", "rgb(117,0,66)", "rgb(117,78,0)", "rgb(78,117,0)", "rgb(0,36,117)"];
    this.colors3 = ["rgb(101,101,255)", "rgb(255,101,147)", "rgb(255,216,101)", "rgb(171,255,101)", "rgb(200,101,255)", "rgb(255,171,101)", "rgb(255,255,101)", "rgb(101,255,255)", "rgb(255,101,101)", "rgb(255,229,101)", "rgb(137,101,255)", "rgb(101,255,101)", "rgb(255,101,255)", "rgb(255,189,101)", "rgb(229,255,101)", "rgb(101,188,255)", "rgb(167,101,255)", "rgb(255,145,101)", "rgb(255,241,101)", "rgb(101,255,189)", "rgb(255,101,188)", "rgb(255,204,101)", "rgb(204,255,101)", "rgb(101,147,255)"];
    this.colors4 = ["rgb(181,181,255)", "rgb(255,181,203)", "rgb(255,237,181)", "rgb(215,255,181)", "rgb(229,181,255)", "rgb(255,215,181)", "rgb(255,255,181)", "rgb(181,255,255)", "rgb(255,181,181)", "rgb(255,242,181)", "rgb(199,181,255)", "rgb(181,255,181)", "rgb(255,181,255)", "rgb(255,223,181)", "rgb(242,255,181)", "rgb(181,223,255)", "rgb(213,181,255)", "rgb(255,202,181)", "rgb(255,248,181)", "rgb(181,255,223)", "rgb(255,181,223)", "rgb(255,230,181)", "rgb(230,255,181)", "rgb(181,203,255)"];
    this.config.push("colors");
    this.shapes = ["sphere", "square", "triangle", "star", "rhombus", "octagon", "oval", "plus", "minus", "pacman", "mdavid", "rect2", "rect3", "rectangle"];
    this.sizes = [4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34];
    this.timeFormat = "isoDate";
    this.config.push("timeFormat");
    this.autoExtend = false;
    this.config.push("autoExtend");
    this.maxSmpStringLen = 50;
    this.config.push("maxSmpStringLen");
    this.maxVarStringLen = 30;
    this.config.push("maxVarStringLen");
    this.smpTitle = false;
    this.config.push("smpTitle");
    this.smpTitleFontSize = 10;
    this.config.push("smpTitleFontSize");
    this.smpTitleFont = (this.smpTitleFontSize) + "Pt " + this.fontName;
    this.smpTitleColor = this.foreground;
    this.config.push("smpTitleColor");
    this.scaleSmpTitleFontFactor = 1;
    this.config.push("scaleSmpTitleFontFactor");
    this.smpLabelFontSize = 10;
    this.config.push("smpLabelFontSize");
    this.smpLabelFont = (this.smpLabelFontSize) + "Pt " + this.fontName;
    this.smpLabelColor = this.foreground;
    this.config.push("smpLabelColor");
    this.scaleSmpLabelFontFactor = 1;
    this.config.push("scaleSmpLabelFontFactor");
    this.smpLabelRotate = 0;
    this.config.push("smpLabelRotate");
    this.varLabelFontSize = 10;
    this.config.push("varLabelFontSize");
    this.varLabelFont = (this.varLabelFontSize) + "Pt " + this.fontName;
    this.varLabelColor = this.foreground;
    this.config.push("varLabelColor");
    this.scaleVarLabelFontFactor = 1;
    this.config.push("scaleVarLabelFontFactor");
    this.varLabelRotate = 0;
    this.config.push("varLabelRotate");
    this.showSampleNames = true;
    this.config.push("showSampleNames");
    this.showVariableNames = true;
    this.config.push("showVariableNames");
    this.highlightVar = [];
    this.config.push("highlightVar");
    this.highlightSmp = [];
    this.config.push("highlightSmp");
    this.highlightNode = [];
    this.config.push("highlightNode");
    this.smpHighlightColor = "rgb(255,0,0)";
    this.config.push("smpHighlightColor");
    this.varHighlightColor = "rgb(255,0,0)";
    this.config.push("varHighlightColor");
    this.nodeHighlightColor = "rgb(255,0,0)";
    this.config.push("nodeHighlightColor");
    this.selectNode = {};
    this.isSelectNodes = 0;
    this.overlaysWidth = 30;
    this.config.push("overlaysWidth");
    this.overlays = [];
    this.smpOverlays = [];
    this.config.push("smpOverlays");
    this.varOverlays = [];
    this.config.push("varOverlays");
    this.showOverlays = true;
    this.config.push("showOverlays");
    this.overlayFontSize = 6;
    this.config.push("overlayFontSize");
    this.overlayFont = (this.overlayFontSize) + "Pt " + this.fontName;
    this.overlayFontColor = this.foreground;
    this.config.push("overlayFontColor");
    this.scaleOverlayFontFactor = 1;
    this.config.push("scaleOverlayFontFactor");
    this.showLegend = true;
    this.config.push("showLegend");
    this.legendPosition = "right";
    this.config.push("legendPosition");
    this.legendBox = true;
    this.config.push("legendBox");
    this.legendFontSize = 10;
    this.config.push("legendFontSize");
    this.legendFont = (this.legendFontSize) + "Pt " + this.fontName;
    this.legendColor = this.foreground;
    this.config.push("legendColor");
    this.legendBackgroundColor = this.background;
    this.config.push("legendBackgroundColor");
    this.scaleLegendFontFactor = 1;
    this.config.push("scaleLegendFontFactor");
    this.showIndicators = true;
    this.config.push("showIndicators");
    this.indicatorsPosition = "bottom";
    this.config.push("indicatorsPosition");
    this.showDecorations = false;
    this.config.push("showDecorations");
    this.decorationsPosition = "bottom";
    this.config.push("decorationsPosition");
    this.decorationsColor = this.foreground;
    this.config.push("decorationsColor");
    this.decorationFontSize = 10;
    this.config.push("decorationFontSize");
    this.decorationFont = (this.decorationFontSize) + "Pt " + this.fontName;
    this.scaleDecorationFontFactor = 1;
    this.config.push("scaleDecorationFontFactor");
    this.decorationsWidth = 10;
    this.config.push("decorationsWidth");
    this.decorationsHeight = 20;
    this.config.push("decorationsHeight");
    this.decorationsType = "bar";
    this.config.push("decorationsType");
    this.decorations = [];
    this.config.push("decorations");
    this.decorationsColors = [];
    this.config.push("decorationsColors");
    this.axisTickFontSize = 10;
    this.config.push("axisTickFontSize");
    this.axisTickFont = (this.axisTickFontSize) + "Pt " + this.fontName;
    this.axisTickColor = this.foreground;
    this.config.push("axisTickColor");
    this.scaleTickFontFactor = 1;
    this.config.push("scaleTickFontFactor");
    this.axisTitleFontSize = 10;
    this.config.push("axisTitleFontSize");
    this.axisTitleFont = (this.axisTitleFontSize) + "Pt " + this.fontName;
    this.axisTitleColor = this.foreground;
    this.config.push("axisTitleColor");
    this.scaleTitleFontFactor = 1;
    this.config.push("scaleTitleFontFactor");
    this.isGroupedData = false;
    this.isTransformedData = false;
    this.isBoxPlotCalc = false;
    this.isMarketDataFormated = false;
    this.isMarketSwitched = false;
    this.isGraphTime = false;
    this.config.push("isGraphTime");
    this.groupingFactors = [];
    this.config.push("groupingFactors");
    this.segregateSamplesBy = false;
    this.config.push("segregateSamplesBy");
    this.segregateVariablesBy = false;
    this.config.push("segregateVariablesBy");
    this.isLogData = false;
    this.config.push("isLogData");
    this.smpSort = -1;
    this.varSort = -1;
    this.tmpAsciiArray = [];
    this.sortDir = "ascending";
    this.config.push("sortDir");
    this.transformBase = 2;
    this.config.push("transformBase");
    this.transformType = false;
    this.config.push("transformType");
    this.ratioReference = 0;
    this.config.push("ratioReference");
    this.zscoreAxis = "samples";
    this.config.push("zscoreAxis");
    this.showErrorBars = true;
    this.config.push("showErrorBars");
    this.randomData = false;
    this.config.push("randomData");
    this.randomDataMean = 100;
    this.config.push("randomDataMean");
    this.randomDataSigma = 50;
    this.config.push("randomDataSigma");
    this.randomDataVariables = 6;
    this.config.push("randomDataVariables");
    this.randomDataSamples = 12;
    this.config.push("randomDataSamples");
    this.randomDataVariableAnnotations = 2;
    this.config.push("randomDataVariableAnnotations");
    this.randomDataSampleAnnotations = 3;
    this.config.push("randomDataSampleAnnotations");
    this.randomDataVariableAnnotationRatio = 2;
    this.config.push("randomDataVariableAnnotationRatio");
    this.randomDataSampleAnnotationRatio = 2;
    this.config.push("randomDataSampleAnnotationRatio");
    this.randomMissingDataPercentage = 0;
    this.config.push("randomMissingDataPercentage");
    this.zoom = 1;
    this.config.push("zoom");
    this.zoomStep = 0.1;
    this.config.push("zoomStep");
    this.panningX = 1;
    this.config.push("panningX");
    this.panningY = 1;
    this.config.push("panningY");
    this.panningStep = 0.1;
    this.config.push("panningStep");
    this.distance = "euclidian";
    this.config.push("distance");
    this.linkage = "single";
    this.config.push("linkage");
    this.clusterAxis = "samples";
    this.config.push("clusterAxis");
    this.kmeansClusters = 3;
    this.config.push("kmeansClusters");
    this.maxIterations = 10;
    this.config.push("maxIterations");
    this.imputeMethod = "mean";
    this.config.push("imputeMethod");
    this.centerData = false;
    this.config.push("centerData");
    this.xAxisAbsMax = null;
    this.xAxisAbsMin = null;
    this.xAxis2AbsMax = null;
    this.xAxis2AbsMin = null;
    this.yAxisAbsMax = null;
    this.yAxisAbsMin = null;
    this.zAxisAbsMax = null;
    this.zAxisAbsMin = null;
    this.setMax = null;
    this.config.push("setMax");
    this.setMin = null;
    this.config.push("setMin");
    this.setMax2 = null;
    this.config.push("setMax2");
    this.setMin2 = null;
    this.config.push("setMin2");
    this.setMaxX = null;
    this.config.push("setMaxX");
    this.setMinX = null;
    this.config.push("setMinX");
    this.setMaxY = null;
    this.config.push("setMaxY");
    this.setMinY = null;
    this.config.push("setMinY");
    this.setMaxZ = null;
    this.config.push("setMaxZ");
    this.setMinZ = null;
    this.config.push("setMinZ");
    this.xAxisCurrent = 0;
    this.config.push("xAxisCurrent");
    this.yAxisCurrent = 0;
    this.config.push("yAxisCurrent");
    this.zAxisCurrent = 0;
    this.config.push("zAxisCurrent");
    this.xAxisTitle = false;
    this.config.push("xAxisTitle");
    this.yAxisTitle = false;
    this.config.push("yAxisTitle");
    this.zAxisTitle = false;
    this.config.push("zAxisTitle");
    this.xAxis = [];
    this.config.push("xAxis");
    this.xAxis2 = [];
    this.config.push("xAxis2");
    this.yAxis = [];
    this.config.push("yAxis");
    this.zAxis = [];
    this.config.push("zAxis");
    this.xAxisTickStyle = "solid";
    this.config.push("xAxisTickStyle");
    this.yAxisTickStyle = "solid";
    this.config.push("yAxisTickStyle");
    this.zAxisTickStyle = "solid";
    this.config.push("zAxisTickStyle");
    this.xAxisTickColor = "rgb(220,220,220)";
    this.config.push("xAxisTickColor");
    this.yAxisTickColor = "rgb(220,220,220)";
    this.config.push("yAxisTickColor");
    this.zAxisTickColor = "rgb(220,220,220)";
    this.config.push("zAxisTickColor");
    this.axisExtension = 0.1;
    this.config.push("axisExtension");
    this.axisExact = false;
    this.config.push("axisExact");
    this.xAxisExact = false;
    this.config.push("xAxisExact");
    this.xAxis2Exact = false;
    this.config.push("xAxis2Exact");
    this.yAxisExact = false;
    this.config.push("yAxisExact");
    this.zAxisExact = false;
    this.config.push("zAxisExact");
    this.timeValues = [];
    this.config.push("timeValues");
    this.timeValueIndices = [];
    this.config.push("timeValueIndices");
    this.xAxisValues = [];
    this.config.push("xAxisValues");
    this.xAxis2Values = [];
    this.config.push("xAxis2Values");
    this.yAxisValues = [];
    this.config.push("yAxisValues");
    this.zAxisValues = [];
    this.config.push("zAxisValues");
    this.xAxisMinorValues = [];
    this.config.push("xAxisMinorValues");
    this.xAxis2MinorValues = [];
    this.config.push("xAxis2MinorValues");
    this.yAxisMinorValues = [];
    this.config.push("yAxisMinorValues");
    this.zAxisMinorValues = [];
    this.config.push("zAxisMinorValues");
    this.xAxisTransform = false;
    this.config.push("xAxisTransform");
    this.yAxisTransform = false;
    this.config.push("yAxisTransform");
    this.zAxisTransform = false;
    this.config.push("zAxisTransform");
    this.xAxisTransformTicks = true;
    this.config.push("xAxisTransformTicks");
    this.yAxisTransformTicks = true;
    this.config.push("yAxisTransformTicks");
    this.zAxisTransformTicks = true;
    this.config.push("zAxisTransformTicks");
    this.xAxisShow = true;
    this.config.push("xAxisShow");
    this.yAxisShow = true;
    this.config.push("yAxisShow");
    this.zAxisShow = true;
    this.config.push("zAxisShow");
    this.ticks = 50;
    this.config.push("ticks");
    this.timeTicks = 10;
    this.config.push("timeTicks");
    this.xAxisTicks = 5;
    this.config.push("xAxisTicks");
    this.xAxis2Ticks = 5;
    this.config.push("xAxis2Ticks");
    this.yAxisTicks = 5;
    this.config.push("yAxisTicks");
    this.zAxisTicks = 5;
    this.config.push("zAxisTicks");
    this.xAxisMinorTicks = true;
    this.config.push("xAxisMinorTicks");
    this.xAxis2MinorTicks = true;
    this.config.push("xAxis2MinorTicks");
    this.yAxisMinorTicks = true;
    this.config.push("yAxisMinorTicks");
    this.zAxisMinorTicks = false;
    this.config.push("zAxisMinorTicks");
    this.smpHairline = "dotted";
    this.config.push("smpHairline");
    this.smpHairlineColor = "rgb(220,220,220)";
    this.config.push("smpHairlineColor");
    this.blockContrast = false;
    this.config.push("blockContrast");
    this.blockContrastOddColor = this.background;
    this.config.push("blockContrastOddColor");
    this.blockContrastEvenColor = "rgb(240,240,240)";
    this.config.push("blockContrastEvenColor");
    this.blockSeparationFactor = 1;
    this.config.push("blockSeparationFactor");
    this.blockFactor = 1;
    this.config.push("blockFactor");
    this.showDataValues = false;
    this.config.push("showDataValues");
    this.lineDecoration = "symbol";
    this.config.push("lineDecoration");
    this.coordinateLineColor = true;
    this.config.push("coordinateLineColor");
    this.lineThickness = 1;
    this.config.push("lineTickness");
    this.colorBy = false;
    this.config.push("colorBy");
    this.shapeBy = false;
    this.config.push("shapeBy");
    this.sizeBy = false;
    this.config.push("sizeBy");
    this.isScatterLine = false;
    this.config.push("isScatterLine");
    this.selectDataPoint = [];
    this.hideDataPoint = [];
    this.isSelectDataPoints = 0;
    this.isHistogram = false;
    this.config.push("isHistogram");
    this.histogramBarWidth = 0.5;
    this.config.push("histogramBarWidth");
    this.histogramBins = 20;
    this.config.push("histogramBins");
    this.isCreateHistogram = false;
    this.allVsAll = false;
    this.config.push("allVsAll");
    this.allVsAllType = "both";
    this.config.push("allVsAllType");
    this.functionIntervals = 20;
    this.config.push("functionIntervals");
    this.xRotate = 45;
    this.config.push("xRotate");
    this.yRotate = 0;
    this.config.push("yRotate");
    this.zRotate = 0;
    this.config.push("zRotate");
    this.rotationDelay = 100;
    this.config.push("rotationDelay");
    this.rotationSensitivity = 45;
    this.config.push("rotationSensitivity");
    this.rotationStep = 2;
    this.config.push("rotationStep");
    this.indicatorCenter = "white";
    this.config.push("indicatorCenter");
    this.indicatorHeight = 8;
    this.config.push("indicatorHeight");
    this.heatmapType = "blue-red";
    this.config.push("heatmapType");
    this.varIndicesStart = -1;
    this.smpIndicesStart = -1;
    this.correlationAxis = "samples";
    this.config.push("correlationAxis");
    this.correlationAnchorLegend = false;
    this.config.push("correlationAnchorLegend");
    this.correlationAnchorLegendAlignWidth = 40;
    this.config.push("correlationAnchorLegendAlignWidth");
    this.showSmpDendrogram = false;
    this.config.push("showSmpDendrogram");
    this.showVarDendrogram = false;
    this.config.push("showVarDendrogram");
    this.varDendrogramPosition = "top";
    this.config.push("varDendrogramPosition");
    this.smpDendrogramPosition = "left";
    this.config.push("smpDendrogramPosition");
    this.dendrogramSpace = 6;
    this.config.push("dendrogramSpace");
    this.dendrogramHang = false;
    this.config.push("dendrogramHang");
    this.dendrogramHeight = false;
    this.config.push("dendrogramHeight");
    this.vennGroups = 4;
    this.config.push("vennGroups");
    this.vennCompartments = ["A", "B", "AB", "C", "AC", "BC", "ABC", "D", "AD", "BD", "CD", "ABD", "ACD", "BCD", "ABCD"];
    this.vennColors = false;
    this.config.push("vennColors");
    this.pieType = "separated";
    this.config.push("pieType");
    this.pieSegmentPrecision = 0;
    this.config.push("pieSegmentPrecision");
    this.pieSegmentSeparation = 3;
    this.config.push("pieSegmentSeparation");
    this.pieSegmentLabels = "inside";
    this.config.push("pieSegmentLabels");
    this.maxPieSectors = 20;
    this.config.push("maxPieSectors");
    this.showPieGrid = true;
    this.config.push("showPieGrid");
    this.showPieSampleLabel = true;
    this.config.push("showPieSampleLabel");
    this.showVolume = true;
    this.config.push("showVolume");
    this.calculateLayout = true;
    this.config.push("calculateLayout");
    this.networkFreeze = false;
    this.config.push("networkFreeze");
    this.approximateNodePositions = false;
    this.config.push("approximateNodePositions");
    this.networkLayoutType = "forceDirected";
    this.config.push("networkLayoutType");
    this.networkRoot = false;
    this.config.push("networkRoot");
    this.showNetworkRadialLayout = false;
    this.config.push("showNetworkRadialLayout");
    this.networkDepth = 0;
    this.networkDivisions = 0;
    this.preScaleNetwork = false;
    this.config.push("preScaleNetwork");
    this.showNodeNameThreshold = 50;
    this.config.push("showNodeNameThreshold");
    this.showHiddenChildEdges = true;
    this.config.push("showHiddenChildEdges");
    this.nodeSize = this.preScaleNetwork ? 10 : 20;
    this.config.push("nodeSize");
    this.edgeWidth = 1;
    this.config.push("edgeWidth");
    this.layoutTime = 15;
    this.config.push("layoutTime");
    this.nodeFontSize = this.nodeSize;
    this.config.push("nodeFontSize");
    this.nodeFont = this.nodeFontSize + "Pt " + this.fontName;
    this.nodeFontColor = this.foreground;
    this.config.push("nodeFontColor");
    this.scaleNodeFontFactor = 1;
    this.config.push("scaleNodeFontFactor");
    this.randomNetwork = false;
    this.config.push("randomNetwork");
    this.reduceRandomNetwork = false;
    this.config.push("reduceRandomNetwork");
    this.randomNetworkNodes = 50;
    this.config.push("randomNetworkNodes");
    this.randomNetworkNodeEdgesMax = 5;
    this.config.push("randomNetworkNodeEdgesMax");
    this.colorNodeBy = false;
    this.config.push("colorNodeBy");
    this.shapeNodeBy = false;
    this.config.push("shapeNodeBy");
    this.sizeNodeBy = false;
    this.config.push("sizeNodeBy");
    this.colorEdgeBy = false;
    this.config.push("colorEdgeBy");
    this.shapeEdgeBy = false;
    this.config.push("shapeEdgeBy");
    this.sizeEdgeBy = false;
    this.config.push("sizeEdgeBy");
    this.is3DNetwork = false;
    this.config.push("is3DNetwork");
    this.network2DRotate = 0;
    this.trackNameFontSize = 10;
    this.config.push("trackNameFontSize");
    this.trackNameFont = (this.trackNameFontSize) + "Pt " + this.fontName;
    this.trackNameFontColor = this.background;
    this.config.push("trackNameFontColor");
    this.showFeatureNameThereshold = 20;
    this.config.push("showFeatureNameThereshold ");
    this.featureNameFontSize = 8;
    this.config.push("featureNameFontSize");
    this.featureNameFont = (this.featureNameFontSize) + "Pt " + this.fontName;
    this.featureNameFontColor = this.background;
    this.config.push("featureNameFontColor");
    this.featureWidthDefault = 3;
    this.config.push("featureWidthDefault");
    this.featureHeightDefault = 8;
    this.config.push("featureHeightDefault");
    this.featureTypeDefault = "line";
    this.config.push("featureTypeDefault");
    this.sequenceFontSize = 10;
    this.config.push("sequenceFontSize");
    this.sequenceFont = (this.sequenceFontSize) + "Pt " + this.fontName;
    this.sequenceAColor = "rgb(255,255,0)";
    this.config.push("sequenceAColor");
    this.sequenceCColor = "rgb(0,255,255)";
    this.config.push("sequenceCColor");
    this.sequenceGColor = "rgb(0,255,0)";
    this.config.push("sequenceGColor");
    this.sequenceTColor = "rgb(255,0,0)";
    this.config.push("sequenceTColor");
    this.sequenceMColor = "rgb(255,0,255)";
    this.config.push("sequenceMColor");
    this.wireColor = "rgba(220,220,220,0.1)";
    this.config.push("wireColor");
    this.periodTicksLabels = 5;
    this.config.push("periodTicksLabels");
    this.debug = true;
    this.config.push("debug");
    this.disableConfigurator = false;
    this.config.push("disableConfigurator");
    this.disableEvents = false;
    this.config.push("disableEvents");
    this.disableDragEvents = false;
    this.config.push("disableDragEvents");
    this.disableMouseDownEvents = false;
    this.config.push("disableMouseDownEvents");
    this.eventKeys = true;
    this.config.push("eventKeys");
    this.dragAreaColor = "rgb(208,222,240)";
    this.config.push("dragAreaColor");
    this.selectedBackgroundColor = "rgb(255,0,0)";
    this.config.push("selectedBackgroundColor");
    this.infoAreaColor = "rgb(240,240,240)";
    this.config.push("infoAreaColor");
    this.infoTimeOut = 3000;
    this.config.push("infoTimeOut");
    this.modelEvent = "local";
    this.config.push("modelEvent");
    this.dataEvent = [];
    this.isBroadcasting = true;
    this.config.push("isBroadcasting");
    this.resizerAreaColor = "rgba(210,210,210,0.7)";
    this.config.push("resizerAreaColor");
    this.resizerAreaColorCurrent = "rgb(237,247,255)";
    this.config.push("resizerAreaColorCurrent");
    this.resizerAreaColorOutlineCurrent = "rgb(59,138,229)";
    this.config.push("resizerAreaColorOutlineCurrent");
    this.resizerTransparency = true;
    this.config.push("resizerTransparency");
    this.resizerVisibility = false;
    this.config.push("resizerVisibility");
    this.resizerDraw = false;
    this.config.push("resizerDraw");
    this.resizerPosition = "bottom";
    this.config.push("resizerPosition");
    this.resizerWidth = 50;
    this.config.push("resizerWidth");
    this.resizerBackgroundImage = false;
    this.config.push("resizerBackgroundImage");
    this.resizerDataIndex = 0;
    this.config.push("resizerDataIndex");
    this.resizerEventData = {};
    this.showAnimation = false;
    this.config.push("showAnimation");
    this.showAnimationFontSize = 10;
    this.config.push("showAnimationFontSize");
    this.showAnimationFont = (this.showAnimationFontSize) + "Pt " + this.fontName;
    this.showAnimationFontColor = this.background;
    this.config.push("showAnimationFontColor");
    this.animationTime = 30;
    this.config.push("animationTime");
    this.animationType = "grow";
    this.config.push("animationType");
    this.animationCycles = 20;
    this.config.push("animationCycles");
    this.snapshotCopyChangeOnly = true;
    this.config.push("snapshotCopyChangeOnly");
    this.snapshots = [];
    this.isAnimation = false;
    this.weight = 1;
    this.scaleX = 1;
    this.scaleY = 1;
    this.translateX = 0;
    this.translateY = 0;
    this.offsetX = 0;
    this.offsetY = 0;
    this.layoutWidth = 0;
    this.layoutHeight = 0;
    this.layout = "1X1";
    this.config.push("layout");
    this.layoutComb = false;
    this.config.push("layoutComb");
    this.layoutAdjust = false;
    this.config.push("layoutAdjust");
    this.layoutCurrent = 0;
    this.layoutMaxLegend = 0;
    this.layoutMulticolor = true;
    this.config.push("layoutMulticolor");
    this.layoutAxis = 3;
    this.imageDir = "http://canvasxpress.org/images/";
    this.config.push("imageDir");
    this.playButton = "play.png";
    this.config.push("playButton");
    this.stopButton = "stop.png";
    this.config.push("stopButton");
    this.previousButton = "rewind.png";
    this.config.push("previousButton");
    this.nextButton = "fast_forward.png";
    this.config.push("nextButton");
    this.remoteService = false;
    this.config.push("remoteService");
    this.remoteParams = {};
    this.config.push("remoteParams");
    this.remoteDirection = "next";
    this.config.push("remoteDirection");
    this.remoteDataIndex = 0;
    this.remoteWindow = 1;
    this.config.push("remoteWindow");
    this.remoteAutoPlay = false;
    this.config.push("remoteAutoPlay");
    this.remoteAutoPlayDelay = 1000;
    this.config.push("remoteAutoPlayDelay");
    this.remoteUpdating = false;
    if (!a) {
        this.setConfig(this.config);
        this.initDimensions();
        this.initUtils();
        this.initApi();
        this.initText();
        this.initTime();
        this.initAttributes()
    }
};
CanvasXpress.prototype.initDimensions = function () {
    if (this.canvas.width) {
        this.originalWidth = this.canvas.width;
        this.width = this.canvas.width
    } else {
        if (this.width) {
            this.originalWidth = this.width;
            this.canvas.width = this.width
        } else {
            this.width = 600;
            this.originalWidth = this.width;
            this.canvas.width = this.width
        }
    }
    if (this.canvas.height) {
        this.originalHeight = this.canvas.height;
        this.height = this.canvas.height
    } else {
        if (this.height) {
            this.originalHeight = this.height;
            this.canvas.height = this.height
        } else {
            this.height = 600;
            this.originalHeight = this.height;
            this.canvas.height = this.height
        }
    }
    this.top = this.margin;
    if (this.title) {
        this.top += this.titleHeight + this.margin;
        if (this.subtitle) {
            this.top += this.subtitleHeight + this.margin
        }
    }
    this.left = this.margin;
    this.right = this.margin;
    this.bottom = this.margin;
    this.x = this.width - (this.left + this.right);
    this.y = this.height - (this.top + this.bottom)
};
CanvasXpress.prototype.initUtils = function () {
    this.validateColor = function (b, a) {
        if (b.match(/^rgba?\((\d{1,3},\d{1,3},\d{1,3})(?:,([0-9\.]+))?/i)) {
            if (RegExp.$2) {
                b = "rgba(" + RegExp.$1 + "," + RegExp.$2 + ")"
            } else {
                b = "rgb(" + RegExp.$1 + ")"
            }
        } else {
            if (b.match(/^#?[0-9abcdef]/i)) {
                b = this.hexToRgb(b)
            } else {
                b = false
            }
        }
        return b && a != null ? this.addColorTransparency(b, a) : b
    };
    this.hexToRgb = function (d) {
        var b = function () {
                return parseInt(d.substring(0, 2), 16)
            };
        var a = function () {
                return parseInt(d.substring(2, 4), 16)
            };
        var e = function () {
                return parseInt(d.substring(4, 6), 16)
            };
        d = d.charAt(0) == "#" ? d.substring(1, 7) : d;
        return "rgb(" + b() + "," + a() + "," + e() + ")"
    };
    this.addColorTransparency = function (b, a) {
        if (b.match(/^rgba?\((\d{1,3},\d{1,3},\d{1,3})(?:,([0-9\.]+))?/i)) {
            if (RegExp.$2) {
                return "rgba(" + RegExp.$1 + "," + a + ")"
            } else {
                return "rgba(" + RegExp.$1 + "," + a + ")"
            }
        } else {
            return false
        }
    };
    this.getGradientColor = function (h) {
        if (h.match(/^rgba?\((\d{1,3}),(\d{1,3}),(\d{1,3})(?:,([0-9\.]+))?/i)) {
            var f = Math.floor(parseInt(RegExp.$1) / this.gradientRatio);
            var e = Math.floor(parseInt(RegExp.$2) / this.gradientRatio);
            var a = Math.floor(parseInt(RegExp.$3) / this.gradientRatio);
            var d = RegExp.$4 ? parseFloat(RegExp.$4) : false;
            if (d) {
                return "rgba(" + f + "," + e + "," + a + "," + d + ")"
            } else {
                return "rgb(" + f + "," + e + "," + a + ")"
            }
        } else {
            return false
        }
    };
    this.getVarColor = function (a) {
        if (this.isArray(this.highlightVar)) {
            for (var b = 0; b < this.highlightVar.length; b++) {
                if (this.highlightVar[b] == a) {
                    return this.varHighlightColor
                }
            }
        } else {
            if (this.highlightVar == a) {
                return this.varHighlightColor
            }
        }
        return this.varLabelColor
    };
    this.getSmpColor = function (b) {
        if (this.isArray(this.highlightSmp)) {
            for (var a = 0; a < this.highlightSmp.length; a++) {
                if (this.highlightSmp[a] == b) {
                    return this.smpHighlightColor
                }
            }
        } else {
            if (this.highlightSmp == b) {
                return this.smpHighlightColor
            }
        }
        return this.smpLabelColor
    };
    this.getNodeColor = function (b) {
        if (this.isArray(this.highlightNode)) {
            for (var a = 0; a < this.highlightNode.length; a++) {
                if (this.highlightNode[a] == b) {
                    return this.nodeHighlightColor
                }
            }
        } else {
            if (this.highlightNode == b) {
                return this.nodeHighlightColor
            }
        }
        return this.selectNode.hasOwnProperty(b) ? this.nodeHighlightColor : this.nodeFontColor
    };
    this.drawTitle = function () {
        var d;
        var b;
        var a;
        if (this.title) {
            b = this.titleHeight / 2;
            d = this.scaleTextToFont(this.title, this.titleHeight - b, this.x);
            this.drawText(this.title, this.width / 2, this.titleHeight / 2, d, false, "center", "middle");
            if (this.subtitle) {
                a = this.subtitleHeight / 4;
                d = this.scaleTextToFont(this.subtitle, this.subtitleHeight - a, this.x);
                this.drawText(this.subtitle, this.width / 2, (this.titleHeight + this.subtitleHeight) - (b / 2), d, false, "center", "middle")
            }
        }
    };
    this.disableGradientTransparency = function () {
        this.gradientTemp = this.gradient;
        this.transparencyTemp = this.transparency;
        this.gradient = false;
        this.transparency = null
    };
    this.enableGradientTransparency = function () {
        this.gradient = this.gradientTemp;
        this.transparency = this.transparencyTemp
    };
    this.resizeCanvas = function (f, a, d) {
        if (this.ctx) {
            this.disableGradientTransparency();
            if (f || a || d) {
                if (a || d) {
                    if (a) {
                        this.width = a;
                        this.originalWidth = a
                    }
                    if (d) {
                        this.height = d;
                        this.originalHeight = d
                    }
                } else {
                    if (f) {
                        this.width = this.originalWidth;
                        this.height = this.originalHeight
                    }
                }
                this.top = this.margin;
                if (this.title) {
                    this.top += this.titleHeight + this.margin;
                    if (this.subtitle) {
                        this.top += this.subtitleHeight + this.margin
                    }
                }
                this.left = this.margin;
                this.right = this.margin;
                this.bottom = this.margin;
                this.resetMap()
            }
            this.canvas.width = this.width;
            this.canvas.height = this.height;
            if (this.backgroundType == "image" || this.backgroundType == "windowImage") {
                var e = this;
                var b = new Image();
                b.onload = function () {
                    e.ctx.save();
                    e.ctx.translate(0, 0);
                    e.ctx.drawImage(b, 0, 0, this.width, this.height);
                    e.ctx.restore()
                };
                b.src = this.backgroundImage;
                b.style.zIndex = 6000
            } else {
                if (this.backgroundType == "solid" || this.backgroundType == "window" || this.backgroundType == "windowSolidGradient") {
                    this.rectangle(0, 0, this.width, this.height, this.background, this.background)
                } else {
                    this.setLinearGradient(0, 0, 0, this.height, this.backgroundGradient1Color, this.backgroundGradient2Color);
                    this.ctx.fillRect(0, 0, this.width, this.height)
                }
            }
            this.enableGradientTransparency()
        }
    };
    this.drawPlotWindow = function () {
        if (this.backgroundType.match("window")) {
            this.disableGradientTransparency();
            if (this.backgroundType == "windowGradient2" || this.backgroundType == "windowSolidGradient") {
                this.ctx.save();
                if (this.backgroundWindowGradientOrientation == "horizontal") {
                    this.setLinearGradient(this.offsetX, this.offsetY, this.offsetX + this.x, this.offsetY, this.backgroundWindowGradient1Color, this.backgroundWindowGradient2Color);
                    this.ctx.fillRect(this.offsetX + this.left, this.offsetY + this.top, this.x, this.y)
                } else {
                    this.setLinearGradient(this.offsetX, this.offsetY, this.offsetX, this.offsetY + this.y, this.backgroundWindowGradient1Color, this.backgroundWindowGradient2Color);
                    this.ctx.fillRect(this.offsetX + this.left, this.offsetY + this.top, this.x, this.y)
                }
            } else {
                this.rectangle(this.offsetX + this.left, this.offsetY + this.top, this.x, this.y, this.backgroundWindow, this.backgroundWindow)
            }
            this.enableGradientTransparency()
        }
    };
    this.maskPlotArea = function () {
        var d = this.layoutComb ? this.layoutWidth : this.width;
        var a = this.layoutComb ? this.layoutHeight : this.height;
        this.disableGradientTransparency();
        if (this.backgroundType == "image" || this.backgroundType == "windowImage") {
            var e = this;
            var b = new Image();
            b.onload = function () {
                e.ctx.save();
                e.ctx.translate(0, 0);
                e.ctx.drawImage(b, 0, 0, this.width, this.top, this.offsetX, this.offsetY, d, this.top);
                e.ctx.drawImage(b, 0, 0, this.left, this.height, this.offsetX, this.offsetY, this.left, a);
                e.ctx.drawImage(b, 0, this.height - this.bottom, this.width, this.bottom, this.offsetX, (this.offsetY + a) - this.bottom, d, this.bottom);
                e.ctx.drawImage(b, this.width - this.right, 0, this.right, this.height, (this.offsetX + d) - this.right, this.offsetY, this.right, a);
                e.ctx.restore()
            };
            b.src = this.backgroundImage;
            b.style.zIndex = 6000
        } else {
            if (this.backgroundType == "solid" || this.backgroundType == "window" || this.backgroundType == "windowSolidGradient") {
                this.rectangle(this.offsetX, this.offsetY, d, this.top, this.background, this.background);
                this.rectangle(this.offsetX, this.offsetY, this.left, a, this.background, this.background);
                this.rectangle(this.offsetX, (this.offsetY + a) - this.bottom, d, this.bottom, this.background, this.background);
                this.rectangle((this.offsetX + d) - this.right, this.offsetY, this.right, a, this.background, this.background)
            } else {
                this.setLinearGradient(0, 0, 0, this.height, this.backgroundGradient1Color, this.backgroundGradient2Color);
                this.ctx.fillRect(this.offsetX, this.offsetY, d, this.top);
                this.ctx.fillRect(this.offsetX, this.offsetY, this.left, a);
                this.ctx.fillRect(this.offsetX, (this.offsetY + a) - this.bottom, d, this.bottom);
                this.ctx.fillRect((this.offsetX + d) - this.right, this.offsetY, this.right, a)
            }
        }
        this.enableGradientTransparency()
    };
    this.maskTreeArea = function () {
        this.disableGradientTransparency();
        if (this.backgroundType == "image" || this.backgroundType == "windowImage") {
            var b = this;
            var a = new Image();
            a.onload = function () {
                b.ctx.save();
                b.ctx.translate(0, 0);
                b.ctx.drawImage(a, 0, 0, this.left, this.top, this.offsetX, this.offsetY, this.left, this.top);
                b.ctx.drawImage(a, this.left + this.x, 0, this.right, this.top, this.offsetX + this.left + this.x, this.offsetY, this.right, this.top);
                b.ctx.drawImage(a, 0, this.top + this.y, this.left, this.bottom, this.offsetX, this.offsetY + this.top + this.y, this.left, this.bottom);
                b.ctx.drawImage(a, this.left + this.x, this.top + this.y, this.right, this.bottom, this.offsetX + this.left + this.x, this.offsetY + this.top + this.y, this.right, this.bottom);
                b.ctx.restore()
            };
            a.src = this.backgroundImage;
            a.style.zIndex = 6000
        } else {
            if (this.backgroundType == "solid" || this.backgroundType == "window" || this.backgroundType == "windowSolidGradient") {
                this.rectangle(this.offsetX, this.offsetY, this.left, this.top, this.background, this.background);
                this.rectangle(this.offsetX + this.left + this.x, this.offsetY, this.right, this.top, this.background, this.background);
                this.rectangle(this.offsetX, this.offsetY + this.top + this.y, this.left, this.bottom, this.background, this.background);
                this.rectangle(this.offsetX + this.left + this.x, this.offsetY + this.top + this.y, this.right, this.bottom, this.background, this.background)
            } else {
                this.setLinearGradient(0, 0, 0, this.height, this.backgroundGradient1Color, this.backgroundGradient2Color);
                this.ctx.fillRect(this.offsetX, this.offsetY, this.left, this.top);
                this.ctx.fillRect(this.offsetX + this.left + this.x, this.offsetY, this.right, this.top);
                this.ctx.fillRect(this.offsetX, this.offsetY + this.top + this.y, this.left, this.bottom);
                this.ctx.fillRect(this.offsetX + this.left + this.x, this.offsetY + this.top + this.y, this.right, this.bottom)
            }
        }
        this.enableGradientTransparency()
    };
    this.setGradient = function (f, e, d, b, a) {
        if (this.gradientType == "radial") {
            this.setRadialGradient(f, e, d, b, false, false, true)
        } else {
            this.setLinearGradient(f, e, d, b, a, false, true)
        }
    };
    this.setLinearGradient = function (d, j, a, h, f, b, e) {
        if (!e) {
            this.ctx.save()
        }
        var i = this.ctx.createLinearGradient(d, j, a, h);
        if (!b) {
            b = this.getGradientColor(f);
            i.addColorStop(0, b);
            i.addColorStop(0.6, f);
            i.addColorStop(1, f)
        } else {
            i.addColorStop(0, f);
            i.addColorStop(1, b)
        }
        this.ctx.fillStyle = i
    };
    this.setRadialGradient = function (k, i, a, f, e, d, j) {
        if (!j) {
            this.ctx.save()
        }
        var b = a / 5.5;
        var h = this.ctx.createRadialGradient(k - b, i - b, 1, k, i, a);
        if (!e) {
            if (this.transparency != null) {
                e = "rgba(0,0,0," + this.transparency + ")"
            } else {
                e = "rgb(0,0,0)"
            }
        }
        if (!d) {
            d = "rgba(0,0,0,0)"
        }
        h.addColorStop(0, f);
        h.addColorStop(1, e);
        h.addColorStop(1, d);
        this.ctx.fillStyle = h
    };
    this.modifyObjectArray = function (g, b, h) {
        var a = this[h];
        if (b) {
            var e = {};
            var d = [];
            if (this.isArray(g)) {
                for (var f = 0; f < g.length; f++) {
                    e[g[f]] = 1
                }
            } else {
                e[g] = 1
            }
            for (var f = 0; f < a.length; f++) {
                if (!e.hasOwnProperty(a[f])) {
                    d.push(a[f])
                }
            }
            this[h] = d
        } else {
            if (this.isArray(g)) {
                for (var f = 0; f < g.length; f++) {
                    a.push(g[f])
                }
            } else {
                a.push(g)
            }
        }
    };
    this.getObjectArray = function (a) {
        var b = {};
        for (var d = 0; d < a.length; d++) {
            b[a[d]] = 1
        }
        return b
    };
    this.isInArray = function (e, b) {
        if (b && !(b.propertyIsEnumerable("length")) && typeof b === "object" && typeof b.length === "number") {
            for (var d = 0; d < b.length; d++) {
                if (b[d] == e) {
                    return true
                }
            }
        }
        return false
    };
    this.isArray = function (a) {
        if (a && !(a.propertyIsEnumerable("length")) && typeof a === "object" && typeof a.length === "number") {
            return true
        } else {
            return false
        }
    };
    this.toArray = function (a) {
        return this.isArray(a) ? a : [a]
    };
    this.cloneObject = function (d) {
        if (d == null || typeof (d) != "object") {
            return d
        }
        var a = new d.constructor();
        for (var b in d) {
            a[b] = this.cloneObject(d[b])
        }
        return a
    };
    this.isSameObject = function (e, d) {
        if (e === d) {
            return true
        }
        if (typeof (e) != "object" || typeof (d) != "object") {
            return false
        }
        for (var f in e) {
            if (e[f] === d[f]) {
                continue
            }
            if (d[f] === undefined || typeof (e[f]) != typeof (d[f])) {
                return false
            }
            if (!this.isSameObject(e[f], d[f])) {
                return false
            }
        }
        for (var f in d) {
            if (e[f] === undefined) {
                return false
            }
        }
        return true
    };
    this.drawDecorationData = function () {
        if (this.showDecorations && this.data.d) {
            if (this.data.d.nlfit) {
                var T, u, V;
                var h = this.smpIndices;
                var r, a;
                for (var S = 0; S < this.data.d.nlfit.length; S++) {
                    var C = [];
                    var B = [];
                    var l = S % this.colors.length;
                    var O = this.data.d.nlfit[S].param[0];
                    var L = this.data.d.nlfit[S].param[1];
                    var K = this.data.d.nlfit[S].param[2];
                    var I = this.data.d.nlfit[S].param[3];
                    var D = this.xAxisIndices[S];
                    if (this.xAxisTransform == "percentile") {
                        T = this.getAxisRangeBySample(D);
                        V = T[0];
                        u = T[1]
                    }
                    var E = this.data.y.data[D];
                    h.sort(function (j, i) {
                        return E[j] - E[i]
                    });
                    l = this.data.d.nlfit[S].color ? this.data.d.nlfit[S].color : this.colors[l];
                    for (var R = 0; R < h.length; R++) {
                        var n = h[R];
                        var H = this.getDataAtPos(D, n, false, this.xAxisTransform, V, u);
                        var G;
                        var o = [];
                        if (H > 0) {
                            if (R > 0) {
                                var b = h[R - 1];
                                var F = this.getDataAtPos(D, b, false, this.xAxisTransform, V, u);
                                if (F > 0) {
                                    var d = (H - F) / this.functionIntervals;
                                    for (var Q = 0; Q < this.functionIntervals; Q++) {
                                        o.push(F);
                                        F += d
                                    }
                                }
                            } else {
                                o = [H]
                            }
                            for (var Q = 0; Q < o.length; Q++) {
                                if (this.data.d.nlfit[S].type == "reg") {
                                    G = O + ((L - O) / (1 + K / (Math.pow(o[Q], I))))
                                } else {
                                    G = O + ((L - O) / (1 + (Math.pow((o[Q] / K), I))))
                                }
                                r = (this.offsetX + this.left) + ((o[Q] - this.xAxisMin) * this.xAxisUnit);
                                a = (this.offsetY + this.top + this.y) - ((G - this.yAxisMin) * this.yAxisUnit);
                                C.push(r);
                                B.push(a)
                            }
                        }
                    }
                    if (C.length > 0) {
                        this.drawShape("path", C, B, false, false, l, l, "open");
                        if (this.data.d.nlfit[S].type == "reg") {
                            G = O + ((L - O) / (1 + K / (Math.pow(K, I))))
                        } else {
                            G = O + ((L - O) / (1 + (Math.pow((K / K), I))))
                        }
                        r = (this.offsetX + this.left) + ((K - this.xAxisMin) * this.xAxisUnit);
                        a = (this.offsetY + this.top + this.y) - ((G - this.yAxisMin) * this.yAxisUnit);
                        this.drawLine("dashedLine", this.left, a, r, a, l);
                        this.drawLine("dashedLine", r, a, r, (this.top + this.y), l)
                    }
                }
            }
            if (this.data.d.line) {
                for (var S = 0; S < this.data.d.line.length; S++) {
                    if (this.data.d.line[S].x || this.data.d.line[S].y) {
                        var l = this.data.d.line[S].color ? this.data.d.line[S].color : this.colors[S % this.colors.length];
                        var X = this.data.d.line[S].type ? this.data.d.line[S].type : "line";
                        var H = this.data.d.line[S].x ? this.data.d.line[S].x : false;
                        var G = this.data.d.line[S].y ? this.data.d.line[S].y : false;
                        var r, a, p, Y;
                        if (H) {
                            r = (this.offsetX + this.left) + (H * this.xAxisUnit);
                            a = this.offsetY + this.top;
                            p = r;
                            Y = a + this.y;
                            this.drawLine(X, r, a, p, Y, l)
                        }
                        if (G) {
                            r = this.offsetX + this.left;
                            a = this.offsetY + this.top + (G * this.yAxisUnit);
                            p = r + this.x;
                            Y = a;
                            this.drawLine(X, r, a, p, Y, l)
                        }
                    }
                }
            }
            if (this.data.d.reg) {
                for (var S = 0; S < this.data.d.reg.length; S++) {
                    if (this.data.d.reg[S].slope && this.data.d.reg[S].intercept) {
                        var l = S % this.colors.length;
                        var X = this.data.d.reg[S].type ? this.data.d.reg[S].type : "line";
                        var t = this.data.d.reg[S].slope;
                        var M = this.data.d.reg[S].intercept;
                        var f = this.data.d.reg[S].cor;
                        var r = this.data.d.reg[S].start ? this.data.d.reg[S].start : this.xAxisMin;
                        var a = (t * r) + M;
                        var p = this.data.d.reg[S].end ? this.data.d.reg[S].end : this.xAxisMax;
                        var Y = (t * p) + M;
                        r = (this.offsetX + this.left) + (r * this.xAxisUnit);
                        a = (this.offsetY + this.top + this.y) - (a * this.yAxisUnit);
                        p = (this.offsetX + this.left) + (p * this.xAxisUnit);
                        Y = (this.offsetY + this.top + this.y) - (Y * this.yAxisUnit);
                        l = this.data.d.reg[S].color ? this.data.d.reg[S].color : this.colors[l];
                        this.drawLine(X, r, a, p, Y, l)
                    }
                }
            }
            if (this.data.d.nor) {
                for (var S = 0; S < this.data.d.nor.length; S++) {
                    if (this.data.d.nor[S].mu && this.data.d.nor[S].sigma) {
                        var P = [];
                        var N = [];
                        var A = [];
                        var r, a;
                        var l = S % this.colors.length;
                        var m = this.data.d.nor[S].mu;
                        var e = this.data.d.nor[S].sigma;
                        var w = this.data.d.nor[S].start ? this.data.d.nor[S].start : this.data.d.nor[S].yAxis ? this.yAxisMin : this.xAxisMin;
                        var s = this.data.d.nor[S].end ? this.data.d.nor[S].end : this.data.d.nor[S].yAxis ? this.yAxisMax : this.xAxisMax;
                        var g = 120;
                        if (w < m && m < s) {
                            var F, q, v, U;
                            var d = (m - w) / g;
                            var W = e * e;
                            var J = W * 2;
                            var z = 1 / Math.sqrt(2 * Math.PI * W);
                            if (this.data.d.nor[S].yAxis) {
                                v = this.data.d.nor[S].max ? this.data.d.nor[S].max * this.xAxisUnit : this.x - (this.x * this.axisExtension);
                                U = v / z;
                                F = w;
                                q;
                                for (var R = 0; R < g - 1; R++) {
                                    q = z * Math.pow(Math.E, -Math.pow(F - m, 2) / W);
                                    A.push([F, q]);
                                    r = (this.offsetX + this.left) + (q * U);
                                    a = (this.offsetY + this.top + this.y) - (F * this.yAxisUnit);
                                    P.push(r);
                                    N.push(a);
                                    F += d
                                }
                                d = (s - m) / g;
                                F = m;
                                for (var R = 0; R < g; R++) {
                                    q = z * Math.pow(Math.E, -Math.pow(F - m, 2) / W);
                                    A.push([F, q]);
                                    r = (this.offsetX + this.left) + (q * U);
                                    a = (this.offsetY + this.top + this.y) - (F * this.yAxisUnit);
                                    P.push(r);
                                    N.push(a);
                                    F += d
                                }
                                l = this.data.d.nor[S].color ? this.data.d.nor[S].color : this.colors[l];
                                this.drawShape("path", P, N, false, false, l, l, "open")
                            } else {
                                v = this.data.d.nor[S].max ? this.data.d.nor[S].max * this.yAxisUnit : this.y - (this.y * this.axisExtension);
                                U = v / z;
                                F = w;
                                q;
                                for (var R = 0; R < g - 1; R++) {
                                    q = z * Math.pow(Math.E, -Math.pow(F - m, 2) / W);
                                    A.push([F, q]);
                                    r = (this.offsetX + this.left) + (F * this.xAxisUnit);
                                    a = (this.offsetY + this.top + this.y) - (q * U);
                                    P.push(r);
                                    N.push(a);
                                    F += d
                                }
                                d = (s - m) / g;
                                F = m;
                                for (var R = 0; R < g; R++) {
                                    q = z * Math.pow(Math.E, -Math.pow(F - m, 2) / W);
                                    A.push([F, q]);
                                    r = (this.offsetX + this.left) + (F * this.xAxisUnit);
                                    a = (this.offsetY + this.top + this.y) - (q * U);
                                    P.push(r);
                                    N.push(a);
                                    F += d
                                }
                                l = this.data.d.nor[S].color ? this.data.d.nor[S].color : this.colors[l];
                                this.drawShape("path", P, N, false, false, l, l, "open")
                            }
                        }
                    }
                }
            }
            if (this.data.d.area) {
                for (var S = 0; S < this.data.d.area.length; S++) {
                    if (this.data.d.area[S].type && this.data.d.area[S].x && this.data.d.area[S].y && this.data.d.area[S].size) {
                        if (this.isValidShape(this.data.d.area[S].type)) {
                            var r = (this.offsetX + this.left) + (this.data.d.area[S].x * this.xAxisUnit);
                            var a = (this.offsetY + this.top + this.y) - (this.data.d.area[S].y * this.yAxisUnit);
                            var l = this.data.d.area[S].color ? this.data.d.area[S].color : this.colors[S % this.colors.length];
                            this[this.data.d.area[S].type](r, a, l, this.data.d.area[S].size)
                        }
                    }
                }
            }
        }
    };
    this.setDecorationLegendDimension = function () {
        this.legendDecorationWidth = 0;
        this.legendDecorationHeight = 0;
        if (this.showDecorations && this.data.d) {
            var h = this.getFontPt(this.decorationFont);
            var f = 0;
            var b = 0;
            var f = 0;
            var g = 0;
            if (this.data.d.nlfit) {
                var j = this.measureText(sprintf("%75s", "X"), this.decorationFont) + (this.margin * 4);
                var a = 0;
                for (var e = 0; e < this.data.d.nlfit.length; e++) {
                    var d = this.measureText(this.data.d.nlfit[e].label, this.decorationFont);
                    a = Math.max(d, a)
                }
                f = this.margin + a + this.margin + j + this.margin;
                this.legendDecorationHeight = (this.data.d.nlfit.length * h * 2) + h + this.margin
            }
            if (this.data.d.line) {
                var a = 0;
                for (var e = 0; e < this.data.d.line.length; e++) {
                    var d = this.measureText(this.data.d.line[e].label, this.decorationFont);
                    a = Math.max(d, a)
                }
                g = this.margin + a + this.margin;
                this.legendDecorationHeight += (this.data.d.line.length * h * 2) + this.margin
            }
            if (this.data.d.reg) {
                var j = this.measureText(sprintf("%45s", "X"), this.decorationFont) + (this.margin * 2);
                var a = 0;
                for (var e = 0; e < this.data.d.reg.length; e++) {
                    var d = this.measureText(this.data.d.reg[e].label, this.decorationFont);
                    a = Math.max(d, a)
                }
                b = this.margin + a + this.margin + j + this.margin;
                this.legendDecorationHeight += (this.data.d.reg.length * h * 2) + h + this.margin
            }
            if (this.data.d.nor) {
                var j = this.measureText(sprintf("%30s", "X"), this.decorationFont) + (this.margin * 1);
                var a = 0;
                for (var e = 0; e < this.data.d.nor.length; e++) {
                    var d = this.measureText(this.data.d.nor[e].label, this.decorationFont);
                    a = Math.max(d, a)
                }
                f = this.margin + a + this.margin + j + this.margin;
                this.legendDecorationHeight += (this.data.d.nor.length * h * 2) + h + this.margin
            }
            if (this.data.d.area) {
                var a = 0;
                for (var e = 0; e < this.data.d.area.length; e++) {
                    var d = this.measureText(this.data.d.area[e].label, this.decorationFont);
                    a = Math.max(d, a)
                }
                g = this.margin + a + this.margin;
                this.legendDecorationHeight += (this.data.d.area.length * h * 2) + this.margin
            }
            this.legendDecorationWidth = Math.max(Math.max(Math.max(f, b), f), g)
        }
    };
    this.drawDecorationLegend = function () {
        if (this.showDecorations && this.data.d) {
            var m = this.getFontPt(this.decorationFont);
            var d = this.measureText(sprintf("%15s", "X"), this.decorationFont);
            var a, k, h, l;
            if (this.decorationsPosition == "right") {
                k = this.offsetY + this.top + (m / 2) + ((this.y - this.legendDecorationHeight) / 2)
            } else {
                k = (this.offsetY + this.top + this.y + this.bottom) - this.legendDecorationHeight
            }
            if (this.data.d.nlfit) {
                l = (this.margin * 5) + (5 * d);
                if (this.decorationsPosition == "right") {
                    h = (this.offsetX + this.left + this.x + this.right + d) - l
                } else {
                    h = this.offsetX + this.left + ((this.x - this.legendDecorationWidth) / 2) + (this.legendDecorationWidth - l) + d
                }
                a = h;
                this.drawText("Min", a, k, this.decorationFont, this.decorationsColor, "right", "middle");
                a += d + this.margin;
                this.drawText("Max", a, k, this.decorationFont, this.decorationsColor, "right", "middle");
                a += d + this.margin;
                this.drawText("EC50", a, k, this.decorationFont, this.decorationsColor, "right", "middle");
                a += d + this.margin;
                this.drawText("Slope", a, k, this.decorationFont, this.decorationsColor, "right", "middle");
                a += d + this.margin;
                this.drawText("p-Val", a, k, this.decorationFont, this.decorationsColor, "right", "middle");
                k += m + m;
                for (var g = 0; g < this.data.d.nlfit.length; g++) {
                    var e = g % this.colors.length;
                    e = this.data.d.nlfit[g].color ? this.data.d.nlfit[g].color : this.colors[e];
                    a = (h + this.margin) - d;
                    if (this.graphType == "ScatterBubble2D" && this.decorationsPosition == "right" && this.zAxisShow) {
                        a += this.margin + this.getFontPt(this.axisTitleFont) + this.margin
                    }
                    this.drawText(this.data.d.nlfit[g].label, a, k, this.decorationFont, e, "right", "middle");
                    a = h;
                    for (var f = 0; f < 5; f++) {
                        var b = this.data.d.nlfit[g].param[f];
                        this.drawText(this.formatNumber(b), a, k, this.decorationFont, this.decorationsColor, "right", "middle");
                        a += d + this.margin
                    }
                    k += m + m
                }
            }
            if (this.data.d.line) {
                for (var g = 0; g < this.data.d.line.length; g++) {
                    if (this.data.d.line[g].label) {
                        if (this.decorationsPosition == "right") {
                            a = this.offsetX + this.left + this.x + this.margin
                        } else {
                            a = this.offsetX + this.left + ((this.x - this.legendDecorationWidth) / 2) + this.margin
                        }
                        for (var g = 0; g < this.data.d.line.length; g++) {
                            var e = this.data.d.line[g].color ? this.data.d.line[g].color : this.colors[g % this.colors.length];
                            if (this.graphType == "ScatterBubble2D" && this.decorationsPosition == "right" && this.zAxisShow) {
                                a += this.margin + this.getFontPt(this.axisTitleFont) + this.margin
                            }
                            this.drawText(this.data.d.line[g].label, a, k, this.decorationFont, e, "left", "middle");
                            k += m + m
                        }
                    }
                }
            }
            if (this.data.d.reg) {
                l = (this.margin * 3) + (3 * d);
                if (this.decorationsPosition == "right") {
                    h = (this.offsetX + this.left + this.x + this.right) - l
                } else {
                    h = this.offsetX + this.left + ((this.x - this.legendDecorationWidth) / 2) + (this.legendDecorationWidth - l) + d
                }
                a = h;
                this.drawText("Slope", a, k, this.decorationFont, this.decorationsColor, "right", "middle");
                a += d + this.margin;
                this.drawText("Int", a, k, this.decorationFont, this.decorationsColor, "right", "middle");
                a += d + this.margin;
                this.drawText("Cor", a, k, this.decorationFont, this.decorationsColor, "right", "middle");
                k += m + m;
                for (var g = 0; g < this.data.d.reg.length; g++) {
                    var e = g % this.colors.length;
                    e = this.data.d.reg[g].color ? this.data.d.reg[g].color : this.colors[e];
                    a = (h + this.margin) - d;
                    if (this.graphType == "ScatterBubble2D" && this.decorationsPosition == "right" && this.zAxisShow) {
                        a += this.margin + this.getFontPt(this.axisTitleFont) + this.margin
                    }
                    this.drawText(this.data.d.reg[g].label, a, k, this.decorationFont, e, "right", "middle");
                    a = h;
                    this.drawText(this.formatNumber(this.data.d.reg[g].slope), a, k, this.decorationFont, this.decorationsColor, "right", "middle");
                    a += d + this.margin;
                    this.drawText(this.formatNumber(this.data.d.reg[g].intercept), a, k, this.decorationFont, this.decorationsColor, "right", "middle");
                    a += d + this.margin;
                    this.drawText(this.formatNumber(this.data.d.reg[g].cor), a, k, this.decorationFont, this.decorationsColor, "right", "middle");
                    a += d + this.margin;
                    k += m + m
                }
            }
            if (this.data.d.nor) {
                l = (this.margin * 2) + (2 * d);
                if (this.decorationsPosition == "right") {
                    h = (this.offsetX + this.left + this.x + this.right) - l
                } else {
                    h = this.offsetX + this.left + ((this.x - this.legendDecorationWidth) / 2) + (this.legendDecorationWidth - l) + d
                }
                a = h;
                this.drawText("Mu", a, k, this.decorationFont, this.decorationsColor, "right", "middle");
                a += d + this.margin;
                this.drawText("Sigma", a, k, this.decorationFont, this.decorationsColor, "right", "middle");
                k += m + m;
                for (var g = 0; g < this.data.d.nor.length; g++) {
                    var e = g % this.colors.length;
                    e = this.data.d.nor[g].color ? this.data.d.nor[g].color : this.colors[e];
                    a = (h + this.margin) - d;
                    if (this.graphType == "ScatterBubble2D" && this.decorationsPosition == "right" && this.zAxisShow) {
                        a += this.margin + this.getFontPt(this.axisTitleFont) + this.margin
                    }
                    this.drawText(this.data.d.nor[g].label, a, k, this.decorationFont, e, "right", "middle");
                    a = h;
                    this.drawText(this.formatNumber(this.data.d.nor[g].mu), a, k, this.decorationFont, this.decorationsColor, "right", "middle");
                    a += d + this.margin;
                    this.drawText(this.formatNumber(this.data.d.nor[g].sigma), a, k, this.decorationFont, this.decorationsColor, "right", "middle");
                    a += d + this.margin;
                    k += m + m
                }
            }
            if (this.data.d.area) {
                for (var g = 0; g < this.data.d.area.length; g++) {
                    if (this.data.d.area[g].label) {
                        if (this.decorationsPosition == "right") {
                            a = this.offsetX + this.left + this.x + this.margin
                        } else {
                            a = this.offsetX + this.left + ((this.x - this.legendDecorationWidth) / 2) + this.margin
                        }
                        for (var g = 0; g < this.data.d.area.length; g++) {
                            var e = this.data.d.area[g].color ? this.data.d.area[g].color : this.colors[g % this.colors.length];
                            if (this.graphType == "ScatterBubble2D" && this.decorationsPosition == "right" && this.zAxisShow) {
                                a += this.margin + this.getFontPt(this.axisTitleFont) + this.margin
                            }
                            this.drawText(this.data.d.area[g].label, a, k, this.decorationFont, e, "left", "middle");
                            k += m + m
                        }
                    }
                }
            }
        }
    };
    this.setLegendFont = function () {
        if (this.autoScaleFont) {
            if (this.layoutComb) {
                this.legendFont = (parseInt(Math.max(this.minTextSize, this.legendFontSize - Math.min(this.layoutRows, this.layoutCols)) * this.scaleLegendFontFactor))
            } else {
                this.legendFont = (parseInt(this.legendFontSize * this.scaleLegendFontFactor))
            }
            this.legendFont = this.legendFont + "Pt " + this.fontName
        } else {
            this.legendFont = parseInt(this.legendFontSize) + "Pt " + this.fontName
        }
    };
    this.setSampleLegendDimension = function () {
        this.legendSampleWidth = 0;
        this.legendSampleHeight = 0;
        if (this.xAxisIndices.length != this.yAxisIndices.length || this.xAxisIndices.length > 1) {
            var e = this.getFontPt(this.legendFont);
            var b = 0;
            if (this.xAxisIndices.length == this.yAxisIndices.length && this.xAxisIndices.length > 1) {
                for (var d = 0; d < this.xAxisIndices.length; d++) {
                    var a = this.measureText(this.shortenText(this.xAxis[d], this.maxSmpStringLen) + " vs " + this.shortenText(this.yAxis[d], this.maxSmpStringLen), this.legendFont);
                    b = Math.max(a, b)
                }
            } else {
                if (this.xAxisIndices.length > this.yAxisIndices.length) {
                    b = this.measureText(this.shortenText(this.getMaxText(this.xAxis), this.maxSmpStringLen) + " vs " + this.shortenText(this.yAxis[0], this.maxSmpStringLen), this.legendFont)
                } else {
                    if (this.yAxisIndices.length > this.xAxisIndices.length) {
                        b = this.measureText(this.shortenText(this.getMaxText(this.yAxis), this.maxSmpStringLen) + " vs " + this.shortenText(this.xAxis[0], this.maxSmpStringLen), this.legendFont)
                    }
                }
            }
            this.legendSampleWidth = (e - 1) + b + (this.margin * 3);
            this.legendSampleHeight = (Math.max(this.xAxisIndices.length, this.yAxisIndices.length) * (e + this.margin)) + this.margin
        }
    };
    this.drawSampleLegend = function (b, k) {
        var j = b;
        var e = k;
        var p = this.getFontPt(this.legendFont);
        var o = (p - 1) / 2;
        k += this.margin + o;
        if (this.legendBox) {
            this.disableGradientTransparency();
            this.rectangle(j, e, this.legendSampleWidth, this.legendSampleHeight, this.legendBackgroundColor);
            this.enableGradientTransparency()
        }
        var m = Math.max(this.xAxisIndices.length, this.yAxisIndices.length);
        for (var g = 0; g < m; g++) {
            var d = g % this.colors.length;
            var n = 0;
            var h = this.xAxisIndices.length == this.yAxisIndices.length ? this.yAxis[g] : this.xAxisIndices.length > this.yAxisIndices.length ? this.yAxis[0] : this.xAxis[0];
            var l = this.shortenText(this.xAxisIndices.length < this.yAxisIndices.length ? this.yAxis[g] : this.xAxis[g], this.maxSmpStringLen) + " vs " + this.shortenText(h, this.maxSmpStringLen);
            var f = b + o + this.margin;
            var a = k + o + this.margin;
            if (this.colorBy) {
                this.drawShape(this.shapes[n], f, k, p - 1, p - 1, this.background, this.colors[d], "open")
            } else {
                this.drawShape(this.shapes[n], f, k, p - 1, p - 1, this.colors[d], this.foreground, "closed")
            }
            this.drawText(l, f + o + this.margin, k, this.legendFont, this.legendColor, "left", "middle");
            k += p + this.margin
        }
        if (this.legendBox) {
            this.rectangle(j, e, this.legendSampleWidth, this.legendSampleHeight, false, this.foreground, "open")
        }
        this.addArea(["rect", j, e, j + this.legendSampleWidth, e + this.legendSampleHeight], [-1], "-legend-sample")
    };
    this.setVariableLegendDimension = function () {
        this.legendVariableWidth = 0;
        this.legendVariableHeight = 0;
        if ((this.colorBy && this.colorBy == "variable") || (this.shapeBy && this.shapeBy == "variable") || (this.sizeBy && this.sizeBy == "variable")) {
            var a = this.sizeBy && this.sizeBy == "variable" ? Math.max(this.sizes[this.varIndices.length - 1], this.getFontPt(this.legendFont)) : this.getFontPt(this.legendFont);
            var b = this.shortenText(this.maxVarNameStr, this.maxVarStringLen);
            this.legendVariableWidth = (a - 1) + this.measureText(b, this.legendFont) + (this.margin * 3);
            this.legendVariableHeight = (this.varIndices.length * (a + this.margin)) + this.margin
        }
    };
    this.drawVariableLegend = function (d, m) {
        if ((this.colorBy && this.colorBy == "variable") || (this.shapeBy && this.shapeBy == "variable") || (this.sizeBy && this.sizeBy == "variable")) {
            var k = d;
            var f = m;
            var q = this.getFontPt(this.legendFont);
            var l = this.sizeBy && this.sizeBy == "variable" ? Math.max(this.sizes[this.varIndices.length - 1], q) : q;
            m += this.margin + (l / 2);
            if (this.legendBox && this.legendBackgroundColor) {
                this.disableGradientTransparency();
                this.rectangle(k, f, this.legendVariableWidth, this.legendVariableHeight, this.legendBackgroundColor);
                this.enableGradientTransparency()
            }
            for (var h = 0; h < this.varIndices.length; h++) {
                var n = this.varIndices[h];
                var e = this.colorBy && this.colorBy == "variable" ? h % this.colors.length : 0;
                var p = this.shapeBy && this.shapeBy == "variable" ? h % this.shapes.length : 0;
                var b = this.sizeBy && this.sizeBy == "variable" ? this.sizes[h % this.sizes.length] : l;
                var o = l / 2;
                var g = d + o + this.margin;
                var a = m + o + this.margin;
                var j = this.shortenText(this.data.y.vars[n], this.maxVarStringLen);
                if (this.colorBy && this.colorBy != "variable") {
                    this.drawShape(this.shapes[p], g, m, b, b, this.background, this.foreground, "open")
                } else {
                    this.drawShape(this.shapes[p], g, m, b, b, this.colors[e], this.foreground, "closed")
                }
                this.drawText(j, g + o + this.margin, m, this.legendFont, this.legendColor, "left", "middle");
                m += l + this.margin
            }
            if (this.legendBox) {
                this.rectangle(k, f, this.legendVariableWidth, this.legendVariableHeight, false, this.foreground, "open")
            }
            this.addArea(["rect", k, f, k + this.legendVariableWidth, f + this.legendVariableHeight], [-1], "-legend-variable")
        }
    };
    this.setColorLegendIndicatorDimensions = function () {
        var a = this.heatmapType.split("-");
        if (this.heatmapType.split("-").length > 1) {
            this.legendColorIndicatorWidth = parseInt(2 * 256 / this.indicatorBins) + (this.margin * 4) + 1
        } else {
            this.legendColorIndicatorWidth = parseInt(256 / this.indicatorBins) + (this.margin * 4) + 1
        }
        this.legendColorIndicatorHeight = this.indicatorHeight + (this.margin * 3) + this.getFontPt(this.legendFont);
        if (this.colorBy) {
            this.legendColorIndicatorHeight += this.getFontPt(this.legendFont) + this.margin
        }
    };
    this.getHeatmapColor = function (e, d, f) {
        var b = Math.abs(e);
        e += b;
        d += b;
        f += b;
        var a = (f * this.heatmapColors.length / d).toFixed() - 1;
        return this.heatmapColors[Math.max(0, a)]
    };
    this.drawColorIndicator = function (b, m, e, k, g) {
        var j = b;
        var d = m;
        var a = this.indicatorHeight;
        var l = this.indicatorHeight;
        if (this.indicatorsPosition == "bottom") {
            for (var f = 0; f < this.heatmapColors.length; f++) {
                var h = this.transparency != null ? this.addColorTransparency(this.heatmapColors[f], this.transparency) : this.heatmapColors[f];
                this.drawLine("line", b, m, b, m + l, h);
                b++
            }
            b = j;
            m += l + this.margin;
            this.drawText(sprintf("%." + g + "f", e), b, m, this.legendFont, this.legendColor, "center", "top");
            b += (this.heatmapColors.length + 0.5) / 2;
            if (this.heatmapColors.length >= 10) {
                this.drawText(sprintf("%." + g + "f", (e + k) / 2), b, m, this.legendFont, this.legendColor, "center", "top")
            }
            if (this.colorBy) {
                this.drawText(this.colorBy, b, m + this.margin + this.getFontPt(this.legendFont), this.legendFont, this.legendColor, "center", "top")
            }
            b += (this.heatmapColors.length + 0.5) / 2;
            this.drawText(sprintf("%." + g + "f", k), b, m, this.legendFont, this.legendColor, "center", "top");
            this.addArea(["rect", j - (this.margin * 2), d - this.margin, b + (this.margin * 2), m + this.getFontPt(this.legendFont) + this.margin], [-1], "-legend-indicator-color")
        } else {
            m -= l;
            for (var f = 0; f < this.heatmapColors.length; f++) {
                var h = this.transparency != null ? this.addColorTransparency(this.heatmapColors[f], this.transparency) : this.heatmapColors[f];
                this.drawLine("line", b, m, b + a, m, h);
                m--
            }
            b += a + this.margin;
            m = d;
            this.drawText(sprintf("%." + g + "f", e), b, m, this.legendFont, this.legendColor, "center", "top", -Math.PI / 2);
            m -= (this.heatmapColors.length + 0.5) / 2;
            if (this.heatmapColors.length >= 10) {
                this.drawText(sprintf("%." + g + "f", (e + k) / 2), b, m, this.legendFont, this.legendColor, "center", "top", -Math.PI / 2)
            }
            if (this.colorBy) {
                this.drawText(this.colorBy, b + this.margin + this.getFontPt(this.legendFont), m, this.legendFont, this.legendColor, "center", "top", -Math.PI / 2)
            }
            m -= (this.heatmapColors.length + 0.5) / 2;
            this.drawText(sprintf("%." + g + "f", k), b, m, this.legendFont, this.legendColor, "center", "top", -Math.PI / 2);
            this.addArea(["rect", j - this.margin, m - (this.margin * 2), b + this.margin + this.getFontPt(this.legendFont), d + (this.margin * 2)], [-1], "-legend-indicator-color")
        }
    };
    this.setShapeLegendIndicatorDimensions = function () {
        this.legendShapeIndicatorWidth = (10 * (this.indicatorHeight + this.margin)) + (this.margin * 2);
        this.legendShapeIndicatorHeight = this.indicatorHeight + (this.margin * 3) + this.getFontPt(this.legendFont);
        if (this.shapeBy) {
            this.legendShapeIndicatorHeight += this.getFontPt(this.legendFont) + this.margin
        }
    };
    this.drawShapeIndicator = function (b, n, f, m, j) {
        var l = b;
        var d = n;
        var k = this.indicatorHeight / 2;
        var e = b;
        var a = n;
        if (this.indicatorsPosition == "bottom") {
            for (var g = 0; g < 10; g++) {
                this.drawShape("pie" + g, e, a + this.margin, this.indicatorHeight, this.indicatorHeight, this.background, this.foreground, "open");
                e += this.indicatorHeight + k
            }
            b = l;
            n += this.indicatorHeight + this.margin;
            this.drawText(sprintf("%." + j + "f", f), b, n, this.legendFont, this.legendColor, "center", "top");
            b += this.indicatorHeight * 7;
            this.drawText(sprintf("%." + j + "f", (f + m) / 2), b, n, this.legendFont, this.legendColor, "center", "top");
            if (this.shapeBy) {
                this.drawText(this.shapeBy, b, n + this.margin + this.getFontPt(this.legendFont), this.legendFont, this.legendColor, "center", "top")
            }
            b += this.indicatorHeight * 7;
            this.drawText(sprintf("%." + j + "f", m), b, n, this.legendFont, this.legendColor, "center", "top");
            this.addArea(["rect", l - (this.margin * 2), d - this.margin, b + (this.margin * 2), n + this.getFontPt(this.legendFont) + this.margin], [-1], "-legend-indicator-shape")
        } else {
            for (var g = 0; g < 10; g++) {
                this["pie" + g](e + this.margin, a, this.background, this.indicatorHeight, "open", this.foreground);
                a -= this.indicatorHeight + k
            }
            b += this.indicatorHeight + this.margin;
            n = d;
            this.drawText(sprintf("%." + j + "f", f), b, n, this.legendFont, this.legendColor, "center", "top", -Math.PI / 2);
            n -= this.indicatorHeight * 7;
            if (this.heatmapColors.length >= 10) {
                this.drawText(sprintf("%." + j + "f", (f + m) / 2), b, n, this.legendFont, this.legendColor, "center", "top", -Math.PI / 2)
            }
            if (this.shapeBy) {
                this.drawText(this.shapeBy, b + this.margin + this.getFontPt(this.legendFont), n, this.legendFont, this.legendColor, "center", "top", -Math.PI / 2)
            }
            n -= this.indicatorHeight * 7;
            this.drawText(sprintf("%." + j + "f", m), b, n, this.legendFont, this.legendColor, "center", "top", -Math.PI / 2);
            this.addArea(["rect", l - this.margin, n - (this.margin * 2), b + this.margin + this.getFontPt(this.legendFont), d + (this.margin * 2)], [-1], "-legend-indicator-shape")
        }
    };
    this.setSizeLegendIndicatorDimensions = function () {
        var a = 0;
        var d = this.indicatorHeight / 2;
        for (var b = 0; b < 10; b++) {
            a += this.sizes[b] + d
        }
        this.legendSizeIndicatorWidth = (a - (this.sizes[0] + this.sizes[9])) + (this.margin * 4);
        this.legendSizeIndicatorHeight = this.sizes[9] + (this.margin * 3) + this.getFontPt(this.legendFont);
        if (this.sizeBy) {
            this.legendSizeIndicatorHeight += this.getFontPt(this.legendFont) + this.margin
        }
    };
    this.drawSizeIndicator = function (e, q, j, p, l) {
        var o = e;
        var f = q;
        var m = this.indicatorHeight / 2;
        var r = this.sizes[9];
        var g = e;
        var a = q;
        var n = e;
        var d = q;
        if (this.indicatorsPosition == "bottom") {
            for (var k = 0; k < 10; k++) {
                var b = this.sizes[k];
                this.drawShape("sphere", g, a + (r / 2), b, b, this.background, this.foreground, "open");
                g += b + m;
                if (k < 4) {
                    n += b + m
                } else {
                    if (k == 5) {
                        n += (b + m) / 2
                    }
                }
            }
            q += r + this.margin;
            this.drawText(sprintf("%." + l + "f", j), o, q, this.legendFont, this.legendColor, "center", "top");
            this.drawText(sprintf("%." + l + "f", (j + p) / 2), n, q, this.legendFont, this.legendColor, "center", "top");
            if (this.sizeBy) {
                this.drawText(this.sizeBy, o + (this.legendColorIndicatorWidth / 2), q + this.margin + this.getFontPt(this.legendFont), this.legendFont, this.legendColor, "center", "top")
            }
            this.drawText(sprintf("%." + l + "f", p), g - (r + m), q, this.legendFont, this.legendColor, "center", "top");
            this.addArea(["rect", o - (this.margin * 2), f - this.margin, (g - r) + (this.margin * 2), q + this.getFontPt(this.legendFont) + this.margin], [-1], "-legend-indicator-size")
        } else {
            for (var k = 0; k < 10; k++) {
                var b = this.sizes[k];
                this.drawShape("sphere", g + (r / 2), a, b, b, this.background, this.foreground, "open");
                a -= (b + m);
                if (k < 4) {
                    d -= (b + m)
                } else {
                    if (k == 5) {
                        d -= ((b + m) / 2)
                    }
                }
            }
            e += r + this.margin;
            this.drawText(sprintf("%." + l + "f", j), e, q, this.legendFont, this.legendColor, "center", "top", -Math.PI / 2);
            this.drawText(sprintf("%." + l + "f", (j + p) / 2), e, d, this.legendFont, this.legendColor, "center", "top", -Math.PI / 2);
            if (this.sizeBy) {
                this.drawText(this.sizeBy, e + this.margin + this.getFontPt(this.legendFont), f - (this.legendColorIndicatorWidth / 2), this.legendFont, this.legendColor, "center", "top", -Math.PI / 2)
            }
            this.drawText(sprintf("%." + l + "f", p), e, a + (r + m), this.legendFont, this.legendColor, "center", "top", -Math.PI / 2);
            this.addArea(["rect", o - this.margin, (a + (r + m)) - (this.margin * 2), e + this.margin + this.getFontPt(this.legendFont), f + (this.margin * 2)], [-1], "-legend-indicator-size")
        }
    };
    this.setDataColorShapeSizeIndicatorDimensions = function () {
        this.setColorLegendIndicatorDimensions();
        this.setShapeLegendIndicatorDimensions();
        this.setSizeLegendIndicatorDimensions()
    };
    this.drawSelectedBackground = function (g, j, k, b, e, f, d, a, i) {
        if (this.isSelectDataPoints) {
            if (this.selectDataPoint[g]) {
                if (this.selectDataPoint[g].hasOwnProperty(j)) {
                    this.drawShape(k, b, e, f + 12, d + 12, this.selectedBackgroundColor, this.foreground, "closed", a, 1, i)
                }
            }
        }
    };
    this.isVisibleSelectedDataPoint = function (a, b) {
        if (this.isSelectDataPoints) {
            if (this.hideDataPoint.length > 0 && this.hideDataPoint[a]) {
                if (this.hideDataPoint[a].hasOwnProperty(b)) {
                    return false
                }
            }
        }
        return true
    };
    this.getDiameterLegend = function () {
        var a;
        if (this.autoExtend) {
            a = 10
        } else {
            a = this.x >= 600 ? 10 : this.x >= 400 ? 8 : this.x >= 200 ? 6 : 4
        }
        return a
    };
    this.setColorLegendDimensions = function (a) {
        this.legendColorWidth = 0;
        this.legendColorHeight = 0;
        if (this.colorBy) {
            var g, f;
            var j = this.getDiameterLegend();
            if (a && this.data.x.hasOwnProperty(this.colorBy)) {
                g = this.setMaxSmpStringAnnt(this.colorBy);
                f = this.setMaxSmpStringLabel(this.colorBy)
            } else {
                if (this.data.z.hasOwnProperty(this.colorBy)) {
                    g = this.setMaxVarStringAnnt(this.colorBy);
                    f = this.setMaxVarStringLabel(this.colorBy)
                }
            }
            var b = this.measureText(g, this.legendFont);
            var e = this.measureText(f, this.legendFont) + j + this.margin;
            this.legendColorWidth = Math.max(b, e) + (this.margin * 2);
            var i = this.getFontPt(this.legendFont);
            var h = Math.max(j, i);
            this.legendColorHeight = (this.legendColorsN * (h + this.margin)) + i + (this.margin * 3)
        }
    };
    this.drawColorLegend = function (e, f) {
        if (this.colorBy && ((this.data.x && this.data.x.hasOwnProperty(this.colorBy)) || (this.data.z && this.data.z.hasOwnProperty(this.colorBy)))) {
            var i = this.getDiameterLegend();
            var g = this.getFontPt(this.legendFont);
            var j = g / 2;
            var h = f + this.margin + j;
            var a = Math.max(i, g);
            this.disableGradientTransparency();
            if (this.legendBackgroundColor) {
                this.rectangle(e, f, this.legendColorWidth, this.legendColorHeight, this.legendBackgroundColor)
            } else {
                this.rectangle(e, f, this.legendColorWidth, this.legendColorHeight, false, this.foreground, "open")
            }
            this.enableGradientTransparency();
            this.drawText(this.colorBy, e + (this.legendColorWidth / 2), h, this.legendFont, this.colorLegend, "center", "middle");
            h += j + this.margin;
            this.drawLine("line", e, h, e + this.legendColorWidth, h, this.foreground);
            h += this.margin + (a / 2);
            for (var b in this.legendColors) {
                this.drawShape("circle", e + this.margin + (i / 2), h, i, i, this.legendColors[b], this.foreground, "closed");
                this.drawText(b, e + (this.margin * 2) + i, h, this.legendFont, this.colorLegend, "left", "middle");
                h += this.margin + a
            }
            this.addArea(["rect", e, f, e + this.legendColorWidth, f + this.legendColorHeight], [-1], "-legend-color")
        }
    };
    this.setShapeLegendDimensions = function () {
        this.legendShapeWidth = 0;
        this.legendShapeHeight = 0;
        if (this.shapeBy && this.data.z.hasOwnProperty(this.shapeBy)) {
            var i = this.getDiameterLegend();
            var f = this.setMaxVarStringAnnt(this.shapeBy);
            var e = this.setMaxVarStringLabel(this.shapeBy);
            var a = this.measureText(f, this.legendFont);
            var b = this.measureText(e, this.legendFont) + i + this.margin;
            this.legendShapeWidth = Math.max(a, b) + (this.margin * 2);
            var h = this.getFontPt(this.legendFont);
            var g = Math.max(i, h);
            this.legendShapeHeight = (this.legendShapesN * (g + this.margin)) + h + (this.margin * 3)
        }
    };
    this.drawShapeLegend = function (b, e) {
        if (this.shapeBy && this.data.z.hasOwnProperty(this.shapeBy)) {
            var i = this.getDiameterLegend();
            var f = this.getFontPt(this.legendFont);
            var j = f / 2;
            var h = e + this.margin + j;
            var a = Math.max(i, f);
            this.disableGradientTransparency();
            if (this.legendBackgroundColor) {
                this.rectangle(b, e, this.legendShapeWidth, this.legendShapeHeight, this.legendBackgroundColor)
            } else {
                this.rectangle(b, e, this.legendShapeWidth, this.legendShapeHeight, false, this.foreground, "open")
            }
            this.enableGradientTransparency();
            this.drawText(this.shapeBy, b + (this.legendShapeWidth / 2), h, this.legendFont, this.colorLegend, "center", "middle");
            h += j + this.margin;
            this.drawLine("line", b, h, b + this.legendShapeWidth, h, this.foreground);
            h += this.margin + (a / 2);
            for (var g in this.legendShapes) {
                this.drawShape(this.legendShapes[g], b + this.margin + (i / 2), h, i, i, this.background, this.foreground, "closed");
                this.drawText(g, b + (this.margin * 2) + i, h, this.legendFont, this.colorLegend, "left", "middle");
                h += this.margin + a
            }
            this.addArea(["rect", b, e, b + this.legendShapeWidth, e + this.legendShapeHeight], [-1], "-legend-shape")
        }
    };
    this.setSizeLegendDimensions = function () {
        this.legendSizeWidth = 0;
        this.legendSizeHeight = 0;
        if (this.sizeBy && this.data.z.hasOwnProperty(this.sizeBy)) {
            var i = this.sizes[this.legendSizesN - 1];
            var f = this.setMaxVarStringAnnt(this.sizeBy);
            var e = this.setMaxVarStringLabel(this.sizeBy);
            var a = this.measureText(f, this.legendFont);
            var b = this.measureText(e, this.legendFont) + i + this.margin;
            this.legendSizeWidth = Math.max(a, b) + (this.margin * 2);
            var h = this.getFontPt(this.legendFont);
            var g = Math.max(i, h);
            this.legendSizeHeight = (this.legendSizesN * (g + this.margin)) + h + (this.margin * 3)
        }
    };
    this.drawSizeLegend = function (e, f) {
        if (this.sizeBy && this.data.z.hasOwnProperty(this.sizeBy)) {
            var i = this.sizes[this.legendSizesN - 1];
            var g = this.getFontPt(this.legendFont);
            var j = g / 2;
            var h = f + this.margin + j;
            var a = Math.max(i, g);
            this.disableGradientTransparency();
            if (this.legendBackgroundColor) {
                this.rectangle(e, f, this.legendSizeWidth, this.legendSizeHeight, this.legendBackgroundColor)
            } else {
                this.rectangle(e, f, this.legendSizeWidth, this.legendSizeHeight, false, this.foreground, "open")
            }
            this.enableGradientTransparency();
            this.drawText(this.sizeBy, e + (this.legendSizeWidth / 2), h, this.legendFont, this.colorLegend, "center", "middle");
            h += j + this.margin;
            this.drawLine("line", e, h, e + this.legendSizeWidth, h, this.foreground);
            h += this.margin + (a / 2);
            for (var b in this.legendSizes) {
                this.drawShape("circle", e + this.margin + (i / 2), h, this.legendSizes[b], this.legendSizes[b], this.background, this.foreground, "closed");
                this.drawText(b, e + (this.margin * 2) + i, h, this.legendFont, this.colorLegend, "left", "middle");
                h += this.margin + a
            }
            this.addArea(["rect", e, f, e + this.legendSizeWidth, f + this.legendSizeHeight], [-1], "-legend-size")
        }
    };
    this.setDataColorShapeSizeDimensions = function () {
        this.setColorLegendDimensions();
        this.setShapeLegendDimensions();
        this.setSizeLegendDimensions()
    };
    this.setDataColor = function (g) {

        if (this.colorBy) {
            var j = 0;
            var a = {};
            var h = "rgb(255,255,0)";
            this.dataColors = [];
            this.legendColors = {};
            this.legendColorsN = 0;
            if (g) {
              alert('g');
                if (!this.isGroupedData && this.data.x && this.data.x.hasOwnProperty(this.colorBy)) {
                    if (this.isNumeric(this.data.x[this.colorBy], true)) {
                        var f = this.range(this.data.x[this.colorBy], true);
                        for (var e = 0; e < this.smpIndices.length; e++) {
                            var k = this.smpIndices[e];
                            var d = this.data.x[this.colorBy][k];
                            var b = !isNaN(d) ? this.getHeatmapColor(f[0], f[1], d) : h;
                            this.dataColors.push(b)
                        }
                        this.legendColorsN = -1;
                        this.legendColorRange = f;
                        this.legendColorDecs = this.getAxisDecimals(this.getAxisIncrements(f[0], f[1], this.heatmapColors.length));
                        this.setColorLegendIndicatorDimensions()
                    } else {
                        for (var e = 0; e < this.smpIndices.length; e++) {
                            var k = this.smpIndices[e];
                            var b = this.data.x[this.colorBy][k];
                            if (!a.hasOwnProperty(b)) {
                                a[b] = j % this.colors.length;
                                this.legendColors[b] = this.colors[a[b]];
                                j++
                            }
                            this.dataColors.push(this.colors[a[b]])
                        }
                        this.legendColorsN = j;
                        this.setColorLegendDimensions(g)
                    }
                }
            } else {
              alert('not g');
                if (this.data.z && this.data.z.hasOwnProperty(this.colorBy)) {
                    if (this.isNumeric(this.data.z[this.colorBy], false, true)) {
                        var f = this.range(this.data.z[this.colorBy], false, true);
                        for (var e = 0; e < this.varIndices.length; e++) {
                            var k = this.varIndices[e];
                            var d = this.data.z[this.colorBy][k];
                            if (!isNaN(d)) {
                                this.dataColors.push(this.getHeatmapColor(f[0], f[1], d))
                            } else {
                                this.dataColors.push(h)
                            }
                        }
                        this.legendColorRange = f;
                        this.legendColorDecs = this.getAxisDecimals(this.getAxisIncrements(f[0], f[1], this.heatmapColors.length));
                        this.setColorLegendIndicatorDimensions()
                    } else {
                        for (var e = 0; e < this.varIndices.length; e++) {
                            var k = this.varIndices[e];
                            var b = this.data.z[this.colorBy][k];
                            if (!a.hasOwnProperty(b)) {
                                a[b] = j % this.colors.length;
                                this.legendColors[b] = this.colors[a[b]];
                                j++
                            }
                            this.dataColors.push(this.colors[a[b]])
                        }
                        this.legendColorsN = j;
                        this.setColorLegendDimensions()
                    }
                } else {
                    if (this.getSampleIndices(this.colorBy) > -1) {
                        j = this.getSampleIndices(this.colorBy);
                        var f = this.getAxisRangeBySample(j);
                        for (var e = 0; e < this.varIndices.length; e++) {
                            var k = this.varIndices[e];
                            var d = this.getDataAtPos(k, j);
                            if (!isNaN(d)) {
                                this.dataColors.push(this.getHeatmapColor(f[0], f[1], d))
                            } else {
                                this.dataColors.push(h)
                            }
                        }
                        this.legendColorRange = f;
                        this.legendColorDecs = this.getAxisDecimals(this.getAxisIncrements(f[0], f[1], this.heatmapColors.length));
                        this.setColorLegendIndicatorDimensions()
                    } else {
                        if (this.colorBy == "variable") {
                          alert('coloring by variable');
                            for (var e = 0; e < this.varIndices.length; e++) {
                                var b = e % this.colors.length;
                                this.dataColors.push(this.colors[b])
                            }
                            this.setVariableLegendDimension()
                        } else {
                           alert('coloring by other');
                            for (var e = 0; e < this.varIndices.length; e++) {
                                this.dataColors.push(this.colors[0])
                            }
                        }
                    }
                }
            }
        }
    };
    this.setDataShape = function () {
        if (this.shapeBy) {
            var g = 0;
            var a = {};
            var k = "square";
            this.dataShapes = [];
            this.legendShapes = {};
            this.legendShapesN = 0;
            if (this.data.z && this.data.z.hasOwnProperty(this.shapeBy)) {
                if (this.isNumeric(this.data.z[this.shapeBy], false, true)) {
                    var f = this.range(this.data.z[this.shapeBy], false, true);
                    for (var e = 0; e < this.varIndices.length; e++) {
                        var h = this.varIndices[e];
                        var b = this.data.z[this.shapeBy][h];
                        if (!isNaN(b)) {
                            var d = this.percentile(f[0], f[1], b);
                            var j = parseInt(d / 10);
                            if (j > 0) {
                                j--
                            }
                            this.dataShapes.push("pie" + j)
                        } else {
                            this.dataShapes.push(k)
                        }
                    }
                    this.legendShapeRange = f;
                    this.legendShapeDecs = this.getAxisDecimals(this.getAxisIncrements(f[0], f[1], 10));
                    this.setShapeLegendIndicatorDimensions()
                } else {
                    for (var e = 0; e < this.varIndices.length; e++) {
                        var h = this.varIndices[e];
                        var j = this.data.z[this.shapeBy][h];
                        if (!a.hasOwnProperty(j)) {
                            a[j] = g % this.shapes.length;
                            this.legendShapes[j] = this.shapes[a[j]];
                            g++
                        }
                        this.dataShapes.push(this.shapes[a[j]])
                    }
                    this.legendShapesN = g;
                    this.setShapeLegendDimensions()
                }
            } else {
                if (this.getSampleIndices(this.shapeBy) > -1) {
                    g = this.getSampleIndices(this.shapeBy);
                    var f = this.getAxisRangeBySample(g);
                    for (var e = 0; e < this.varIndices.length; e++) {
                        var h = this.varIndices[e];
                        var b = this.getDataAtPos(h, g);
                        if (!isNaN(b)) {
                            var d = this.percentile(f[0], f[1], b);
                            var j = parseInt(d / 10);
                            if (j > 0) {
                                j--
                            }
                            this.dataShapes.push("pie" + j)
                        } else {
                            this.dataShapes.push(k)
                        }
                    }
                    this.legendShapeRange = f;
                    this.legendShapeDecs = this.getAxisDecimals(this.getAxisIncrements(f[0], f[1], 10));
                    this.setShapeLegendIndicatorDimensions()
                } else {
                    if (this.shapeBy == "variable") {
                        for (var e = 0; e < this.varIndices.length; e++) {
                            var j = e % this.shapes.length;
                            this.dataShapes.push(this.shapes[j])
                        }
                        this.setVariableLegendDimension()
                    } else {
                        for (var e = 0; e < this.varIndices.length; e++) {
                            this.dataShapes.push(this.shapes[0])
                        }
                    }
                }
            }
        }
    };
    this.setDataSize = function () {
        if (this.sizeBy) {
            var a = 0;
            var d = {};
            this.dataSizes = [];
            this.legendSizes = {};
            this.legendSizesN = 0;
            if (this.data.z && this.data.z.hasOwnProperty(this.sizeBy)) {
                if (this.isNumeric(this.data.z[this.sizeBy], false, true)) {
                    var e = this.range(this.data.z[this.sizeBy], false, true);
                    for (var f = 0; f < this.varIndices.length; f++) {
                        var g = this.varIndices[f];
                        var h = this.data.z[this.sizeBy][g];
                        if (!isNaN(h)) {
                            var j = this.percentile(e[0], e[1], h);
                            var b = parseInt(j / 10);
                            if (b > 0) {
                                b--
                            }
                            this.dataSizes.push(this.sizes[b])
                        } else {
                            this.dataSizes.push(0)
                        }
                    }
                    this.legendSizeRange = e;
                    this.legendSizeDecs = this.getAxisDecimals(this.getAxisIncrements(e[0], e[1], 10));
                    this.setSizeLegendIndicatorDimensions()
                } else {
                    for (var f = 0; f < this.varIndices.length; f++) {
                        var g = this.varIndices[f];
                        var b = this.data.z[this.sizeBy][g];
                        if (!d.hasOwnProperty(b)) {
                            d[b] = a % this.sizes.length;
                            this.legendSizes[b] = this.sizes[d[b]];
                            a++
                        }
                        this.dataSizes.push(this.sizes[d[b]])
                    }
                    this.legendSizesN = a;
                    this.setSizeLegendDimensions()
                }
            } else {
                if (this.getSampleIndices(this.sizeBy) > -1) {
                    a = this.getSampleIndices(this.sizeBy);
                    var e = this.getAxisRangeBySample(a);
                    for (var f = 0; f < this.varIndices.length; f++) {
                        var g = this.varIndices[f];
                        var h = this.getDataAtPos(g, a);
                        if (!isNaN(h)) {
                            var j = this.percentile(e[0], e[1], h);
                            var b = parseInt(j / 10);
                            if (b > 0) {
                                b--
                            }
                            this.dataSizes.push(this.sizes[b])
                        } else {
                            this.dataSizes.push(0)
                        }
                    }
                    this.legendSizeRange = e;
                    this.legendSizeDecs = this.getAxisDecimals(this.getAxisIncrements(e[0], e[1], 10));
                    this.setSizeLegendIndicatorDimensions()
                } else {
                    if (this.sizeBy == "variable") {
                        for (var f = 0; f < this.varIndices.length; f++) {
                            var b = f % this.sizes.length;
                            this.dataSizes.push(this.sizes[b])
                        }
                        this.setVariableLegendDimension()
                    } else {
                        for (var f = 0; f < this.varIndices.length; f++) {
                            this.dataSizes.push(this.scatterPointSize)
                        }
                    }
                }
            }
        }
    };
    this.setDataColorShapeSize = function () {
        this.setLegendFont();
        this.setDataColor();
        this.setDataShape();
        this.setDataSize();
        if (this.graphType == "Scatter2D" || this.graphType == "ScatterBubble2d") {
            this.setSampleLegendDimension()
        }
    };
    this.setLegendDimensions = function () {
        this.legendLayout = [];
        var a = this.margin;
        var m = this.margin;
        if (this.showIndicators) {
            var g = ["Color", "Shape", "Size"];
            for (var d = 0; d < g.length; d++) {
                var k = this["legend" + g[d] + "IndicatorWidth"];
                var e = this["legend" + g[d] + "IndicatorHeight"];
                if (k) {
                    if (this.indicatorsPosition == "bottom") {
                        m += this.margin + e
                    } else {
                        a += this.margin + e
                    }
                }
            }
        }
        var b = 0;
        var l = 0;
        var j = 0;
        if (this.showLegend) {
            var f = ["Color", "Shape", "Size", "Variable", "Sample"];
            for (var d = 0; d < f.length; d++) {
                var k = this["legend" + f[d] + "Width"];
                var e = this["legend" + f[d] + "Height"];
                if (k > 0) {
                    if (this.legendPosition == "right") {
                        if (l + this.margin + e > this.height) {
                            a += this.margin + k;
                            b = k;
                            l = e;
                            j++;
                            this.legendLayout[j] = [f[d]]
                        } else {
                            if (k > b) {
                                a += (k - b);
                                b = k
                            }
                            l += e;
                            if (!this.legendLayout[j]) {
                                this.legendLayout[j] = []
                            }
                            this.legendLayout[j].push(f[d])
                        }
                    } else {
                        if (b + this.margin + k > this.width) {
                            m += this.margin + e;
                            b = k;
                            l = e;
                            j++;
                            this.legendLayout[d] = [f[d]]
                        } else {
                            if (e > l) {
                                m += (e - l);
                                l = e
                            }
                            b += k;
                            if (!this.legendLayout[j]) {
                                this.legendLayout[j] = []
                            }
                            this.legendLayout[j].push(f[d])
                        }
                    }
                }
            }
        }
        this.legendWidth = a + this.margin;
        this.legendHeight = m + this.margin
    };
    this.getXYLegendCoords = function (p, a, r) {
        var o, f, n, k;
        var m = 0;
        var d = 0;
        var q = -1;
        var g = -1;
        if (this.legendLayout) {
            for (var e = 0; e < this.legendLayout.length; e++) {
                o = 0;
                f = 0;
                for (var b = 0; b < this.legendLayout[e].length; b++) {
                    if (this.legendLayout[e][b] == p) {
                        g = b
                    }
                    o += this["legend" + this.legendLayout[e][b] + "Width"] + this.margin;
                    f += this["legend" + this.legendLayout[e][b] + "Height"] + this.margin;
                    m = Math.max(m, o);
                    d = Math.max(d, f)
                }
                if (g > -1) {
                    q = e;
                    break
                }
            }
            if (this.legendPosition == "right") {
                r = g == 0 ? this.offsetY + this.top : r;
                n = a;
                k = g == 0 ? this.offsetY + this.top + ((this.y - d) / 2) : r;
                a = g == this.legendLayout[q].length - 1 ? a + m : a;
                r = k + this["legend" + this.legendLayout[q][g] + "Height"] + this.margin
            } else {
                a = g == 0 ? this.offsetX + this.left : a;
                n = g == 0 ? this.offsetX + this.left + ((this.x - m) / 2) + this.margin : a + this.margin;
                k = r;
                a = n + this["legend" + this.legendLayout[q][g] + "Width"] + this.margin;
                r = g == this.legendLayout[q].length - 1 ? r + d : r
            }
        }
        return [n, k, a, r]
    };
    this.drawScatterLegend = function () {
        var d = this.offsetX + this.left + this.x;
        var l = this.offsetY + this.top + this.y;
        if (this.showIndicators) {
            var p, j;
            var a, o;
            if (this.graphType == "Scatter2D" || this.graphType == "ScatterBubble2D") {
                d += (this.margin * 2);
                l += this.get2DXAxisHeight() + this.margin;
                if (this.graphType == "ScatterBubble2D" && this.zAxisShow) {
                    d += this.getFontPt(this.axisTitleFont) + (this.margin * 2)
                }
            }
            var m = ["Color", "Shape", "Size"];
            for (var f = 0; f < m.length; f++) {
                p = this["legend" + m[f] + "IndicatorWidth"];
                j = this["legend" + m[f] + "IndicatorHeight"];
                if (p) {
                    var b = "draw" + m[f] + "Indicator";
                    var g = this["legend" + m[f] + "Range"];
                    var e = this["legend" + m[f] + "Decs"];
                    if (this.indicatorsPosition == "bottom") {
                        a = this.offsetX + this.left + ((this.x - p) / 2);
                        o = l;
                        this[b](a, o, g[0], g[1], e);
                        l += j + this.margin
                    } else {
                        a = d;
                        o = this.offsetY + this.top + (this.y - ((this.y - p) / 2));
                        this[b](a, o, g[0], g[1], e);
                        d += j + this.margin
                    }
                }
            }
        }
        if (this.showLegend) {
            var k = ["Color", "Shape", "Size", "Variable", "Sample"];
            for (var f = 0; f < k.length; f++) {
                p = this["legend" + k[f] + "Width"];
                j = this["legend" + k[f] + "Height"];
                if (p > 0) {
                    var b = "draw" + k[f] + "Legend";
                    var n = this.getXYLegendCoords(k[f], d, l);
                    this[b](n[0], n[1]);
                    if (this.legendPosition == "right") {
                        l = n[3] + this.margin
                    } else {
                        d = n[2] + this.margin
                    }
                }
            }
        }
    };
    this.set3DRotation = function () {
        var a;
        var d = 0;
        var b = 0;
        if (this.graphType == "Network") {
            d = 360;
            b = 360
        } else {
            if (this.graphType == "Scatter3D") {
                d = 90;
                b = 0
            }
        }
        if (this.xRotate > d) {
            this.xRotate = d
        }
        if (this.xRotate < 0) {
            this.xRotate = b
        }
        if (this.yRotate > d) {
            this.yRotate = d
        }
        if (this.yRotate < 0) {
            this.yRotate = b
        }
        if (this.zRotate > d) {
            this.zRotate = d
        }
        if (this.zRotate < 0) {
            this.zRotate = b
        }
        if (this.xRotate > 0) {
            a = 180 / this.xRotate;
            this.ry = Math.PI / a
        } else {
            this.ry = 0
        }
        if (this.yRotate > 0) {
            a = 180 / this.yRotate;
            this.rx = Math.PI / a
        } else {
            this.rx = 0
        }
        if (this.zRotate > 0) {
            a = 180 / this.zRotate;
            this.rz = Math.PI / a
        } else {
            this.rz = 0
        }
    };
    this.set3DParams = function () {
        this.perspective = this.x * 1.5;
        this.len = this.x / 4;
        this.pad = this.x / 2
    };
    this.get3DTransfrom = function (f, g, h) {
        var e, d, b;
        var a = [];
        d = g;
        b = h;
        g = d * Math.cos(this.rx) - b * Math.sin(this.rx);
        h = d * Math.sin(this.rx) + b * Math.cos(this.rx);
        e = f;
        b = h;
        f = b * Math.sin(this.ry) + e * Math.cos(this.ry);
        h = b * Math.cos(this.ry) - e * Math.sin(this.ry);
        e = f;
        d = g;
        f = e * Math.cos(this.rz) - d * Math.sin(this.rz);
        g = e * Math.sin(this.rz) + d * Math.cos(this.rz);
        f = f * (this.perspective / (h + this.perspective));
        g = g * (this.perspective / (h + this.perspective));
        a.push(f + this.pad);
        a.push(g + this.pad);
        a.push(h + this.pad);
        return a
    }
};
CanvasXpress.prototype.initApi = function () {
    this.getValidGraphTypes = function () {
        return this.validGraphTypes
    };
    this.setHeatmapScheme = function () {
        this.initializeAttributes()
    };
    this.hasIndicator = function () {
        if (this.graphType.match(/Scatter/) || this.graphType == "Bar") {
            return true
        } else {
            return false
        }
    };
    this.hasLegend = function () {
        if (this.graphType != "Network" && this.graphType != "Genome" && this.graphType != "Correlation" && this.graphType != "Heatmap") {
            return true
        } else {
            return false
        }
    };
    this.hasLegendProperties = function () {
        if (this.graphType != "Network" && this.graphType != "Genome" && this.graphType != "Correlation" && this.graphType != "Heatmap" && this.graphType != "Pie") {
            return true
        } else {
            return false
        }
    };
    this.hasData = function () {
        if (this.graphType != "Network" && this.graphType != "Genome" && this.graphType != "Venn") {
            return true
        } else {
            return false
        }
    };
    this.hasDataSamples = function () {
        if (this.graphType != "Network" && this.graphType != "Genome" && this.graphType != "Venn" && this.graphType != "Pie" && !this.graphType.match(/Scatter/)) {
            if (this.graphType == "Correlation" && this.correlationAxis != "samples") {
                return false
            }
            return true
        } else {
            return false
        }
    };
    this.hasDataGroups = function () {
        if (this.graphType != "Network" && this.graphType != "Genome" && this.graphType != "Venn" && this.graphType != "Pie" && !this.graphType.match(/Scatter/)) {
            if (this.graphType == "Correlation" && this.correlationAxis != "samples") {
                return false
            }
            if (this.data.x) {
                return true
            } else {
                return false
            }
        } else {
            return false
        }
    };
    this.hasDataVariables = function () {
        if (this.graphType != "Network" && this.graphType != "Genome" && this.graphType != "Venn") {
            if (this.graphType == "Correlation" && this.correlationAxis != "variables") {
                return false
            }
            return true
        } else {
            return false
        }
    };
    this.hasDataProperties = function () {
        if (this.graphType != "Network" && this.graphType != "Genome" && this.graphType != "Heatmap" && this.graphType != "Venn" && this.graphType != "Pie" && this.graphType != "Correlation") {
            return true
        } else {
            return false
        }
    };
    this.hasOrientation = function () {
        if (this.graphType != "Network" && this.graphType != "Genome" && this.graphType != "Venn" && this.graphType != "Pie" && this.graphType != "Correlation" && !this.graphType.match(/Scatter/)) {
            return true
        } else {
            return false
        }
    };
    this.hasOverlays = function () {
        if (this.graphType != "Network" && this.graphType != "Genome" && this.graphType != "Venn" && this.graphType != "Pie" && this.graphType != "Correlation" && !this.graphType.match(/Scatter/)) {
            return true
        } else {
            return false
        }
    };
    this.hasDendrograms = function () {
        if (this.graphType != "Heatmap" && (this.data.t.smps || this.data.t.vars)) {
            return true
        } else {
            return false
        }
    };
    this.hasDecorations = function () {
        return this.data.d && (this.data.d.nlfit || this.data.d.line || this.data.d.reg || this.data.d.nor || this.data.d.area) ? true : false
    };
    this.isSegregable = function () {
        if (this.graphType != "Network" && this.graphType != "Genome" && this.graphType != "Venn" && this.graphType != "Pie" && this.graphType != "Correlation" && !this.graphType.match(/Scatter/)) {
            return true
        } else {
            return false
        }
    };
    this.getLineTypes = function () {
        var f = ["line"];
        var d = ["", "dashed", "dotted", "bezierY", "bezierX", "curved"];
        var a = ["arrow", "arrowHead", "arrowTail", "arrowHeadSquareTail", "arrowTailSquareHead", "square", "squareHead", "squareTail", "squareHeadArrowTail", "squareTailArrowHead"];
        for (var e = 0; e < a.length; e++) {
            f.push(a[e] + "Line")
        }
        for (var e = 1; e < d.length; e++) {
            for (var b = 0; b < a.length; b++) {
                f.push(d[e] + this.capitalize(a[b]) + "Line")
            }
        }
        return f
    };
    this.isValidShape = function (b) {
        for (var a = 0; a < this.shapes.length; a++) {
            if (this.shapes[a] == b) {
                return true
            }
        }
        return false
    };
    this.modifyHighlights = function (d, b, e) {
        var a = e ? "highlightVar" : "highlightSmp";
        this.modifyObjectArray(d, b, a)
    };
    this.getHighlights = function (b) {
        var a = b ? this.highlightVar : this.highlightSmp;
        return this.getObjectArray(a)
    };
    this.modifySmpOverlays = function (b, a) {
        this.modifyObjectArray(b, a, "smpOverlays")
    };
    this.modifyVarOverlays = function (b, a) {
        this.modifyObjectArray(b, a, "varOverlays")
    };
    this.getSmpOverlays = function () {
        return this.getObjectArray(this.smpOverlays)
    };
    this.getVarOverlays = function () {
        return this.getObjectArray(this.varOverlays)
    };
    this.clusterSamples = function () {
        this.clusterAxis = "samples";
        this.showSmpDendrogram = true;
        if (!this.data.t) {
            this.data.t = {}
        }
        this.data.t.smps = this.cluster();
        this.draw()
    };
    this.clusterVariables = function () {
        this.clusterAxis = "variables";
        this.showVarDendrogram = true;
        if (!this.data.t) {
            this.data.t = {}
        }
        this.data.t.vars = this.cluster();
        this.draw()
    };
    this.kmeansSamples = function () {
        this.clusterAxis = "samples";
        this.showOverlays = true;
        if (!this.data.x) {
            this.data.x = {}
        }
        this.data.x["k-Means"] = this.kmeans();
        if (!this.isInArray("k-Means", this.smpOverlays)) {
            this.smpOverlays.push("k-Means")
        }
        this.draw()
    };
    this.kmeansVariables = function () {
        this.clusterAxis = "variables";
        this.showOverlays = true;
        if (!this.data.z) {
            this.data.z = {}
        }
        this.data.z["k-Means"] = this.kmeans();
        if (!this.isInArray("k-Means", this.varOverlays)) {
            this.varOverlays.push("k-Means")
        }
        this.draw()
    };
    this.functions.push("clusterSamples");
    this.functions.push("clusterVariables");
    this.functions.push("kmeansSamples");
    this.functions.push("kmeansVariables");
    this.functions.push("addDataTable");
    this.functions.push("resetDataTable");
    this.functions.push("print")
};
CanvasXpress.prototype.initText = function () {
    this.drawText = function (o, l, i, g, h, k, j, d, e, p) {
        if (!o) {
            return
        }
        if (!this.ctx) {
            if (this.debug) {
                alert("Dude, there is no canvas")
            }
            return
        }
        if (isNaN(l) || isNaN(i)) {
            if (this.debug) {
                alert("Not a valid coordinate (" + l + ", " + i + ") to draw " + o)
            }
            return
        }
        if (d < (Math.PI / -2) || d > (Math.PI / 2)) {
            if (this.debug) {
                alert("Dude, you can only rotate between -PI/2 and PI/2")
            }
            return
        }
        if (!isNaN(o)) {
            o = this.formatNumber(o)
        }
        if (!l) {
            l = 0
        }
        if (!i) {
            i = 0
        }
        if (!g) {
            g = this.font
        }
        if (!h) {
            h = this.foreground
        }
        if (!k) {
            k = this.align
        }
        if (!j) {
            j = this.baseline
        }
        if (e) {
            var n = this.measureText(o, g);
            while (n > e) {
                o = o.substring(0, o.length - 1);
                n = this.measureText(o, g)
            }
        }
        this.ctx.save();
        this.ctx.strokeStyle = h;
        this.ctx.fillStyle = h;
        this.ctx.font = g;
        this.ctx.save();
        this.ctx.translate(l, i);
        this.ctx.textAlign = k;
        this.ctx.textBaseline = j;
        if (this.showShadow) {
            this.ctx.shadowOffsetX = this.shadowOffsetX;
            this.ctx.shadowOffsetY = this.shadowOffsetY;
            this.ctx.shadowBlur = this.shadowBlur;
            this.ctx.shadowColor = this.shadowColor
        }
        if (d) {
            this.ctx.rotate(d)
        }
        if (p) {
            this.ctx.strokeText(o, 0, 0)
        } else {
            this.ctx.fillText(o, 0, 0)
        }
        this.ctx.restore();
        if (this.showShadow) {
            this.ctx.shadowOffsetX = 0;
            this.ctx.shadowOffsetY = 0;
            this.ctx.shadowBlur = 0;
            this.ctx.shadowColor = this.background
        }
        return o
    };
    this.capitalize = function (a) {
        if (a) {
            return a.charAt(0).toUpperCase() + a.slice(1)
        }
    };
    this.measureText = function (a, b) {
        if (!this.ctx) {
            return 0
        }
        if (!b) {
            b = this.font
        }
        this.ctx.font = b;
        return this.ctx.measureText(a).width
    };
    this.scaleTextToFont = function (b, e, a) {
        var d = e + "pt " + this.fontName;
        if (b) {
            while (this.measureText(b, d) > a && e >= this.minTextSize) {
                e -= 2;
                d = e + "pt " + this.fontName
            }
        }
        return d
    };
    this.getFontPt = function (a) {
        return parseInt(a.match(/^\d+/)[0])
    };
    this.scaleTextToSize = function (a) {
        var d = this.scaleTextConstantMult;
        if (!a) {
            a = 1
        }
        return Math.ceil(1 / Math.sqrt(a) * d)
    };
    this.formatNumber = function (d) {
        if (d.toString().length >= 7) {
            var b = sprintf("%.2e", Number(d));
            var a = sprintf("%.2f", Number(d));
            if (a.toString().length > b.toString().length) {
                return b.toString()
            } else {
                return a.toString()
            }
        } else {
            return d.toString()
        }
    };
    this.getMaxText = function (d) {
        var f = "";
        var b = 0;
        for (var e = 0; e < d.length; e++) {
            if (d[e].length > b) {
                f = d[e];
                b = d[e].length
            }
        }
        return f
    };
    this.shortenText = function (b, a) {
        if (b.length <= a) {
            return b
        } else {
            return b.substring(0, a - 3) + " ..."
        }
    };
    this.convertToNumber = function (a) {
        if (!isNaN(a)) {
            return parseFloat(a)
        } else {
            return a
        }
    }
};
CanvasXpress.prototype.initTime = function () {
    this.times = {
        second: 1000,
        minute: 60000,
        hour: 3600000,
        day: 86400000,
        week: 604800000,
        month: 2592000000,
        year: 31556952000
    };
    this.parseDate = function (e) {
        var f = parseInt(e.toString().substring(0, 4));
        var a = e.toString().substring(4, 6);
        var b = parseInt(e.toString().substring(6, 8));
        a = parseInt(a.replace(/^0/, ""));
        return new Date(f, a - 1, b, 0, 0)
    };
    this.createRandomTimes = function (b) {
        var f;
        var h = new Date();
        var a = h.getTime();
        var g = [a];
        var j = this.isGroupedData ? this.data.w : this.data.y;
        if (!b || !this.times[b]) {
            b = "day"
        }
        for (var e = 0; e < j.smps.length; e++) {
            f = Date.parse(j.smps[e]);
            if (!isNaN(f)) {
                a = f
            } else {
                a -= this.times[b]
            }
            g.unshift(a)
        }
        return g
    };
    this.validateTimeAxis = function () {
        var g = this.isGroupedData ? this.data.w : this.data.y;
        var a = false;
        var f = [];
        for (var b = 0; b < this.smpIndices.length; b++) {
            var e = this.smpIndices.length != g.smps.length ? Date.parse(g.smps[this.smpIndices[b]]) : Date.parse(g.smps[b]);
            if (isNaN(e)) {
                if (this.isExample) {
                    f = this.createRandomTimes();
                    return f
                } else {
                    a = true;
                    break
                }
            } else {
                f.push(e)
            }
        }
        if (this.smpIndices.length == g.smps.length) {
            this.smpIndices.sort(function (h, d) {
                return f[h] - f[d]
            });
            f.sort()
        } else {
            f.sort();
            this.smpIndices.sort(function (h, d) {
                return f[d] - f[h] || 0
            })
        }
        return a ? false : f
    };
    this.setTimeAxis = function () {
        var e = this.validateTimeAxis();
        if (e) {
            this.timeValues = [];
            this.timeValueIndices = [];
            var h = e.length;
            var a = e[h - 1] - e[0];
            var b = 0;
            if (h < this.timeTicks) {
                b = 1
            } else {
                b = Math.ceil(h / this.timeTicks)
            }
            var g = b - 1;
            while (g < h) {
                var f = e[g];
                this.timeValues.push(dateFormat(new Date(f), this.timeFormat));
                this.timeValueIndices.push(g);
                g += b
            }
        } else {
            alert("Not valid dates");
            this.isGraphTime = false
        }
    }
};
CanvasXpress.prototype.initAttributes = function () {
    this.validateNumbers = function (b) {
        for (var d = 0; d < b.length; d++) {
            if (isNaN(b[d])) {
                return false
            }
        }
        return true
    };
    this.clip = function (u, g, j) {
        var e, d, o, m, i, n, u;
        var p, f, a, k;
        var q = [];
        if (j && j.length == 4) {
            f = j[0];
            p = j[1];
            a = f + j[2];
            k = p + j[3]
        } else {
            f = this.layoutComb ? this.offsetX + this.left : this.left;
            p = this.layoutComb ? this.offsetY + this.top : this.top;
            a = f + this.x;
            k = p + this.y
        }
        if (u == "rect") {
            e = parseInt(g[0]);
            o = parseInt(g[1]);
            d = parseInt(e + g[2]);
            m = parseInt(o + g[3]);
            if (e > a || m > k || d < f || o < p) {
                return false
            }
            if (e < f) {
                e = f
            }
            if (d > a) {
                d = a
            }
            if (m < p) {
                m = p
            }
            if (o > k) {
                o = k
            }
            q = [e, o, d - e, m - o]
        } else {
            if (u == "poly") {} else {
                if (u == "circle") {
                    e = parseInt(g[0]);
                    o = parseInt(g[1]);
                    if (e > a || e < f || o < p || o > k) {
                        return false
                    }
                    q = [e, o]
                } else {
                    if (u == "line") {
                        e = parseFloat(g[0]);
                        o = parseFloat(g[1]);
                        d = parseFloat(g[2]);
                        m = parseFloat(g[3]);
                        if (e == d) {
                            if (e < f || e > a) {
                                return false
                            } else {
                                if (o > m) {
                                    if (o < p) {
                                        return false
                                    } else {
                                        if (o > k) {
                                            o = k
                                        }
                                    }
                                    if (m > k) {
                                        return false
                                    } else {
                                        if (m < p) {
                                            m = p
                                        }
                                    }
                                } else {
                                    if (o > k) {
                                        return false
                                    } else {
                                        if (o < p) {
                                            o = p
                                        }
                                    }
                                    if (m < p) {
                                        return false
                                    } else {
                                        if (m > k) {
                                            m = k
                                        }
                                    }
                                }
                            }
                        } else {
                            if (o == m) {
                                if (o < p || o > k) {
                                    return false
                                } else {
                                    if (e > d) {
                                        if (e < f) {
                                            return false
                                        } else {
                                            if (e > a) {
                                                e = a
                                            }
                                        }
                                        if (d > a) {
                                            return false
                                        } else {
                                            if (d < f) {
                                                d = f
                                            }
                                        }
                                    } else {
                                        if (e > a) {
                                            return false
                                        } else {
                                            if (e < f) {
                                                e = f
                                            }
                                        }
                                        if (d < f) {
                                            return false
                                        } else {
                                            if (d > a) {
                                                d = a
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (e > d) {
                                    if (e < f || d > a) {
                                        return false
                                    } else {
                                        if (o > m) {
                                            if (o < p || m > k) {
                                                return false
                                            } else {
                                                i = o - m;
                                                n = e - d;
                                                if (o > k) {
                                                    u = o - k;
                                                    u = u * n / i;
                                                    e = d + u;
                                                    o = k
                                                } else {
                                                    if (e > a) {
                                                        u = e - a;
                                                        u = u * i / n;
                                                        e = a;
                                                        o -= u
                                                    }
                                                }
                                                if (m < p) {
                                                    u = p - m;
                                                    u = u * n / i;
                                                    d += u;
                                                    m = p
                                                } else {
                                                    if (d < f) {
                                                        u = f - d;
                                                        u = u * i / n;
                                                        d = f;
                                                        m = o - u
                                                    }
                                                }
                                            }
                                        } else {
                                            if (m < p || o > k) {
                                                return false
                                            } else {
                                                i = m - o;
                                                n = e - d;
                                                if (o < p) {
                                                    u = p - o;
                                                    u = u * n / i;
                                                    e -= u;
                                                    o = p
                                                } else {
                                                    if (e > a) {
                                                        u = e - a;
                                                        u = u * i / n;
                                                        e = a;
                                                        o = m - u
                                                    }
                                                }
                                                if (m > k) {
                                                    u = m - k;
                                                    u = u * n / i;
                                                    d = e - u;
                                                    m = k
                                                } else {
                                                    if (d < f) {
                                                        u = f - d;
                                                        u = u * i / n;
                                                        d = f;
                                                        m -= u
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    if (d < f || e > a) {
                                        return false
                                    } else {
                                        if (o > m) {
                                            if (o < p || m > k) {
                                                return false
                                            } else {
                                                i = o - m;
                                                n = d - e;
                                                if (o > k) {
                                                    u = o - k;
                                                    u = u * n / i;
                                                    e = d - u;
                                                    o = k
                                                } else {
                                                    if (e < f) {
                                                        u = f - e;
                                                        u = u * i / n;
                                                        e = f;
                                                        o -= u
                                                    }
                                                }
                                                if (m < p) {
                                                    u = p - m;
                                                    u = u * n / i;
                                                    d -= u;
                                                    m = p
                                                } else {
                                                    if (d > a) {
                                                        u = d - a;
                                                        u = u * i / n;
                                                        d = a;
                                                        m = o - u
                                                    }
                                                }
                                            }
                                        } else {
                                            if (m < p || o > k) {
                                                return false
                                            } else {
                                                i = m - o;
                                                n = d - e;
                                                if (o < p) {
                                                    u = p - o;
                                                    u = u * n / i;
                                                    e += u;
                                                    o = p
                                                } else {
                                                    if (e < f) {
                                                        u = f - e;
                                                        u = u * i / n;
                                                        e = f;
                                                        o = m - u
                                                    }
                                                }
                                                if (m > k) {
                                                    u = m - k;
                                                    u = u * n / i;
                                                    d = e + u;
                                                    m = k
                                                } else {
                                                    if (d > a) {
                                                        u = d - a;
                                                        u = u * i / n;
                                                        d = a;
                                                        m -= u
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        q = [e, o, d, m]
                    } else {
                        return false
                    }
                }
            }
        }
        return q
    };
    this.lineLength = function (b, e, a, d) {
        return Math.sqrt(Math.pow(a - b, 2) + Math.pow(d - e, 2))
    };
    this.shortenLine = function (b, k, a, j, l, g, m) {
        if (this.validateNumbers([b, k, a, j, l, g])) {
            if (m.match(/bezier/)) {
                if (m.match(/beziery/i)) {
                    return j > k ? [b, k + l, a, j - g] : [b, k - l, a, j + g]
                } else {
                    return a > b ? [b + l, k, a - g, j] : [b - l, k, a + g, j]
                }
            } else {
                if (m.match(/curved/)) {
                    return [b, k, a, j]
                } else {
                    var d = Math.atan2(j - k, a - b);
                    var f = Math.cos(d);
                    var i = Math.sin(d);
                    var h = this.lineLength(b, k, a, j);
                    if (h) {
                        b += f * l;
                        k += i * l;
                        a -= f * g;
                        j -= i * g
                    }
                }
            }
            return [b, k, a, j]
        }
    };
    this.errorBar = function (e, g, d, f, a, h) {
        if (!this.isGroupedData && this.isRawData) {
            return
        }
        if (this.showErrorBars && this.validateNumbers([a])) {
            var b = this.drawLine("line", e, g, d, f, h, false, false, false, false, true);
            if (a > 0) {
                this.drawLine("line", d, f - a / 2, d, f + a / 2, h, false, false, false, false, true)
            } else {
                this.drawLine("line", d - a / 2, f, d + a / 2, f, h, false, false, false, false, true)
            }
            return b
        }
    };
    this.drawLine = function (W, C, a, z, ak, ae, V, Y, X, ag, ai, aj) {
        var P = this;
        var aa;
        var J = [];
        var R = false;
        var q = false;
        var o = false;
        var Q = function () {
                P.ctx.save();
                if (P.transparency != null) {
                    if (ae) {
                        ae = P.validateColor(ae, P.transparency) || P.addColorTransparency(P.foreground, P.transparency)
                    } else {
                        ae = P.addColorTransparency(P.foreground, P.transparency)
                    }
                } else {
                    if (ae) {
                        ae = P.validateColor(ae) || P.foreground
                    } else {
                        ae = P.foreground
                    }
                }
                P.ctx.fillStyle = ae;
                P.ctx.strokeStyle = ae;
                P.ctx.lineWidth = V ? V : P.outlineWidth;
                P.ctx.lineCap = Y ? Y : P.capType;
                if (P.showShadow) {
                    P.ctx.shadowOffsetX = P.shadowOffsetX;
                    P.ctx.shadowOffsetY = P.shadowOffsetY;
                    P.ctx.shadowBlur = P.shadowBlur;
                    P.ctx.shadowColor = P.shadowColor
                }
            };
        var E = function () {
                if (P.showShadow) {
                    P.ctx.shadowOffsetX = 0;
                    P.ctx.shadowOffsetY = 0;
                    P.ctx.shadowBlur = 0;
                    P.ctx.shadowColor = P.background
                }
                P.ctx.restore()
            };
        var D = function (am) {
                var y = function (aw, az, au, aA) {
                        var av = [];
                        for (var ax = 0; ax < aw.length; ax++) {
                            av.push([(aw[ax][0] * Math.cos(az)) - (aw[ax][1] * Math.sin(az)), (aw[ax][0] * Math.sin(az)) + (aw[ax][1] * Math.cos(az))])
                        }
                        var ay = [];
                        for (var ax = 0; ax < av.length; ax++) {
                            ay.push([av[ax][0] + au, av[ax][1] + aA])
                        }
                        return ay
                    };
                var x = P.arrowPointSize;
                var w = Math.max((x / 2) - 1, 1);
                var s = am ? [
                    [0, -x],
                    [0, x]
                ] : [
                    [Math.floor((x / 2.5)), 0],
                    [-x, -w],
                    [-x, w]
                ];
                var at = z - C;
                var aq = ak - a;
                if (q) {
                    if (q == "Y") {
                        if (Math.abs(aq) > Math.abs(at)) {
                            at = 0
                        } else {
                            at = z > C ? P.arrowPointSize : -P.arrowPointSize
                        }
                    } else {
                        if (Math.abs(at) > Math.abs(aq)) {
                            aq = 0
                        } else {
                            aq = ak > a ? P.arrowPointSize : -P.arrowPointSize
                        }
                    }
                } else {
                    if (o) {
                        var ao = P.lineLength(C, a, z, ak);
                        var p = P.shortenLine(C, a, z, ak, 0, ao / 2, "line");
                        var t = p[2];
                        var b = p[3];
                        var ar = ao / 2;
                        var an = o / ar;
                        var ap = z > C ? Math.asin((b - a) / ar) : Math.asin(-(b - a) / ar);
                        var e = ap - (Math.PI / 20);
                        if (z > C) {
                            C = t + ar * Math.cos(e - an);
                            a = b + ar * Math.sin(e - an);
                            z = t + ar * Math.cos(ap - an);
                            ak = b + ar * Math.sin(ap - an)
                        } else {
                            C = t + ar * Math.cos((e - an) + Math.PI);
                            a = b + ar * Math.sin((e - an) + Math.PI);
                            z = t + ar * Math.cos((ap - an) + Math.PI);
                            ak = b + ar * Math.sin((ap - an) + Math.PI)
                        }
                        at = z - C;
                        aq = ak - a
                    }
                }
                var f = y(s, Math.atan2(aq, at), z, ak);
                Q();
                P.ctx.beginPath();
                P.ctx.moveTo(f[0][0], f[0][1]);
                for (var al = 1; al < f.length; al++) {
                    if (!isNaN(f[al][0]) && !isNaN(f[al][1])) {
                        P.ctx.lineTo(f[al][0], f[al][1])
                    }
                }
                if (!am) {
                    if (!isNaN(f[0][0]) && !isNaN(f[0][1])) {
                        P.ctx.lineTo(f[0][0], f[0][1])
                    }
                    P.ctx.closePath();
                    P.ctx.fill()
                } else {
                    P.ctx.closePath();
                    P.ctx.stroke()
                }
                E()
            };
        if (this.validateNumbers([C, a, z, ak])) {
            if (!X) {
                X = 0
            }
            if (!ag) {
                ag = 0
            }
            if (W.match(/bezier/i)) {
                if (W.match(/beziery/i)) {
                    aa = "bezierLine";
                    q = "Y";
                    R = "Y";
                    if (X || ag) {
                        if (ak > a) {
                            if (a + X > ak - ag) {
                                W = W.replace(/beziery/i, "");
                                aa = "line";
                                q = false;
                                R = false
                            }
                        } else {
                            if (a - X < ak + ag) {
                                W = W.replace(/beziery/i, "");
                                aa = "line";
                                q = false;
                                R = false
                            }
                        }
                    }
                } else {
                    aa = "bezierLine";
                    q = "X";
                    R = "X";
                    if (X || ag) {
                        if (z > C) {
                            if (C + X > z - ag) {
                                W = W.replace(/bezier[x]?/i, "");
                                aa = "line";
                                q = false;
                                R = false
                            }
                        } else {
                            if (C - X < z + ag) {
                                W = W.replace(/bezier[x]?/i, "");
                                aa = "line";
                                q = false;
                                R = false
                            }
                        }
                    }
                }
            } else {
                if (W.match(/curved/i)) {
                    if (W.match(/curvedc/i)) {
                        aa = "curvedLine";
                        o = true;
                        R = true
                    } else {
                        aa = "curvedLine";
                        o = true;
                        R = false
                    }
                } else {
                    if (W.match(/dashed/i)) {
                        aa = "dashedLine"
                    } else {
                        if (W.match(/dotted/i)) {
                            aa = "dottedLine"
                        } else {
                            aa = "line"
                        }
                    }
                }
            }
            if (X || ag) {
                var Z = this.shortenLine(C, a, z, ak, X, ag, W);
                C = Z[0];
                a = Z[1];
                z = Z[2];
                ak = Z[3]
            }
            if (ai) {
                var H = this.clip("line", [C, a, z, ak], aj);
                if (H) {
                    C = H[0];
                    a = H[1];
                    z = H[2];
                    ak = H[3]
                } else {
                    return false
                }
            }
            switch (aa) {
            case "dashedLine":
                var k = Math.atan2(ak - a, z - C);
                var N = Math.cos(k);
                var F = Math.sin(k);
                var I = this.lineLength(C, a, z, ak);
                var ac = Math.floor(I / (this.dashLength * 2));
                if (ac) {
                    var T = N * this.dashLength;
                    var S = F * this.dashLength;
                    var g = C;
                    var ah = a;
                    var d = z;
                    var af = ak;
                    d = g + T;
                    af = ah + S;
                    for (var ad = 0; ad < ac; ad++) {
                        this.drawLine("line", g, ah, d, af, ae, V, Y);
                        g += (T * 2);
                        ah += (S * 2);
                        d = g + T;
                        af = ah + S
                    }
                    this.drawLine("line", g, ah, d, af, ae, V, Y);
                    J = ["poly", C + 1, a, z + 1, ak, z - 1, ak, C - 1, a]
                } else {
                    return false
                }
                break;
            case "dottedLine":
                var k = Math.atan2(ak - a, z - C);
                var N = Math.cos(k);
                var F = Math.sin(k);
                var I = this.lineLength(C, a, z, ak);
                var ac = Math.ceil(I / (this.dotLength * 2));
                var O = I / ac;
                if (ac) {
                    var T = N * O;
                    var S = F * O;
                    var g = C;
                    var ah = a;
                    var d = z;
                    var af = ak;
                    g += T;
                    ah += S;
                    for (var ad = 0; ad < ac - 1; ad++) {
                        this.drawShape("circle", g, ah, 2, 2, ae, ae, "closed");
                        g += T;
                        ah += S
                    }
                    J = ["poly", C + 1, a, z + 1, ak, z - 1, ak, C - 1, a]
                } else {
                    return false
                }
                break;
            case "bezierLine":
                if (C == z || a == ak) {
                    return this.drawLine("line", C, a, z, ak, ae, V, Y, X, ag, ai, aj)
                }
                C = parseInt(C) + 0.5;
                a = parseInt(a) + 0.5;
                z = parseInt(z) + 0.5;
                ak = parseInt(ak) + 0.5;
                R = R == "Y" ? "Y" : "X";
                var L = z > C ? true : false;
                var m = (C + z) / 2;
                var l = (a + ak) / 2;
                if (L) {
                    if (R == "X") {
                        J = ["poly", C, a - 1, m + 1, a - 1, m + 1, ak - 1, z, ak - 1, z, ak + 1, m - 1, ak + 1, m - 1, a + 1, C, a + 1]
                    } else {
                        J = ["poly", C - 1, a, C - 1, l + 1, z - 1, l + 1, z - 1, ak, z + 1, ak, z + 1, l - 1, C + 1, l - 1, C + 1, a]
                    }
                } else {
                    if (R == "X") {
                        J = ["poly", C, a - 1, m - 1, a - 1, m - 1, ak - 1, z, ak - 1, z, ak + 1, m + 1, ak + 1, m + 1, a + 1, C, a + 1]
                    } else {
                        J = ["poly", C - 1, a, C - 1, l - 1, z - 1, l - 1, z - 1, ak, z + 1, ak, z + 1, l + 1, C + 1, l + 1, C + 1, a]
                    }
                }
                Q();
                this.ctx.moveTo(C, a);
                if (R == "Y") {
                    this.ctx.bezierCurveTo(C, ak, z, a, z, ak)
                } else {
                    this.ctx.bezierCurveTo(z, a, C, ak, z, ak)
                }
                this.ctx.stroke();
                E();
                break;
            case "curvedLine":
                var I = this.lineLength(C, a, z, ak);
                var B = this.shortenLine(C, a, z, ak, 0, I / 2, "line");
                var M = B[2];
                var A = B[3];
                var U = I / 2;
                var G = X / U;
                var K = ag / U;
                var j = (M - C);
                var h = (A - a);
                var v = R && C > z ? false : !R && z >= C ? true : R;
                var n = z >= C ? Math.asin(h / U) : Math.asin(-h / U);
                var ab = n + Math.PI;
                var u = Math.PI / 8;
                var r = z >= C ? 0 : Math.PI;
                J = ["poly"];
                for (var ad = 0; ad < 9; ad++) {
                    J.push(M - (U + 2) * Math.cos(n + ((u * ad)) - r));
                    J.push(A - (U + 2) * Math.sin(n + ((u * ad)) - r))
                }
                for (var ad = 8; ad >= 0; ad--) {
                    J.push(M - (U - 2) * Math.cos(n + ((u * ad)) - r));
                    J.push(A - (U - 2) * Math.sin(n + ((u * ad)) - r))
                }
                Q();
                this.ctx.beginPath();
                if (z >= C) {
                    this.ctx.arc(M, A, U, n - G, ab + K, v)
                } else {
                    this.ctx.arc(M, A, U, n + K, ab - G, v)
                }
                this.ctx.stroke();
                E();
                break;
            case "line":
                J = ["poly", C + 1, a, z + 1, ak, z - 1, ak, C - 1, a];
                Q();
                this.ctx.beginPath();
                this.ctx.moveTo(parseInt(C) + 0.5, parseInt(a) + 0.5);
                this.ctx.lineTo(parseInt(z) + 0.5, parseInt(ak) + 0.5);
                this.ctx.stroke();
                E();
                break
            }
            if (W.match(/arrowheadsquaretail|squaretailarrowhead/i)) {
                o = o ? X : false;
                D(false, q, o);
                o = o ? ag : false;
                D(true, q, o)
            } else {
                if (W.match(/arrowtailsquarehead|squareheadarrowtail/i)) {
                    o = o ? X : false;
                    D(true, q, o);
                    o = o ? ag : false;
                    D(false, q, o)
                } else {
                    if (W.match(/arrowhead/i)) {
                        o = o ? X : false;
                        D(false, q, o)
                    } else {
                        if (W.match(/squarehead/i)) {
                            o = o ? X : false;
                            D(true, q, o)
                        } else {
                            if (W.match(/arrowtail/i)) {
                                o = o ? ag : false;
                                D(false, q, o)
                            } else {
                                if (W.match(/squaretail/i)) {
                                    o = o ? ag : false;
                                    D(true, q, o)
                                } else {
                                    if (W.match(/arrow/i)) {
                                        o = o ? X : false;
                                        D(false, q, o);
                                        o = o ? ag : false;
                                        D(false, q, o)
                                    } else {
                                        if (W.match(/square/i)) {
                                            o = o ? X : false;
                                            D(true, q, o);
                                            o = o ? ag : false;
                                            D(true, q, o)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return J
        } else {
            return false
        }
        return J
    };
    this.rectangle = function (n, m, q, g, i, e, d, a, k, j, l) {
        return this.drawShape("rectangle", n + (q / 2), m + (g / 2), q, g, i, e, d, a, k, j, l)
    };
    this.drawShape = function (M, I, H, K, Y, Z, S, R, O, G, af, ag, N, ac, L, ae) {
        var F = this;
        var Q = function () {
                F.ctx.save();
                F.ctx.translate(I, H);
                if (O) {
                    F.ctx.rotate(O)
                }
                if (F.transparency != null) {
                    if (Z) {
                        Z = F.validateColor(Z, F.transparency) || F.addColorTransparency(F.foreground, F.transparency)
                    } else {
                        Z = F.addColorTransparency(F.foreground, F.transparency)
                    }
                } else {
                    if (Z) {
                        Z = F.validateColor(Z) || F.foreground
                    } else {
                        Z = F.foreground
                    }
                }
                if (F.gradient) {
                    if (F.gradientType == "radial") {
                        F.setGradient(0, 0, Math.max(Math.abs(K), Math.abs(Y)), Z)
                    } else {
                        F.setGradient(0, 0, K, Y, Z)
                    }
                } else {
                    F.ctx.fillStyle = Z
                }
                if (S) {
                    S = F.validateColor(S) || F.foreground
                }
                F.ctx.strokeStyle = S ? S : F.foreground;
                F.ctx.lineWidth = G ? G : F.outlineWidth;
                if (F.showShadow) {
                    F.ctx.shadowOffsetX = F.shadowOffsetX;
                    F.ctx.shadowOffsetY = F.shadowOffsetY;
                    F.ctx.shadowBlur = F.shadowBlur;
                    F.ctx.shadowColor = F.shadowColor
                }
            };
        var aj = function (e, b) {
                if (M == "image") {
                    return
                }
                if (e) {
                    F.ctx.beginPath();
                    F.ctx.moveTo(e[0][0], e[0][1]);
                    for (var d = 1; d < e.length; d++) {
                        F.ctx.lineTo(e[d][0], e[d][1])
                    }
                    if (!b) {
                        F.ctx.closePath()
                    }
                }
                if (R && R == "open") {
                    F.ctx.stroke()
                } else {
                    F.ctx.stroke();
                    F.ctx.fill()
                }
                if (F.showShadow) {
                    F.ctx.shadowOffsetX = 0;
                    F.ctx.shadowOffsetY = 0;
                    F.ctx.shadowBlur = 0;
                    F.ctx.shadowColor = F.background
                }
                F.ctx.restore()
            };
        var aa = function (r) {
                if (r) {
                    if (r[0] == "circle") {
                        return r
                    }
                    var f = r.shift();
                    var o = [];
                    if (f == "rect") {
                        var p = r.shift() - I;
                        var s = r.shift() - H;
                        var e = r.shift() - I;
                        var h = r.shift() - H;
                        o.push((s * Math.sin(O)) - (p * Math.cos(O)));
                        o.push((s * Math.cos(O)) + (p * Math.sin(O)));
                        o.push((h * Math.sin(O)) - (p * Math.cos(O)));
                        o.push((h * Math.cos(O)) + (p * Math.sin(O)));
                        o.push((h * Math.sin(O)) - (e * Math.cos(O)));
                        o.push((h * Math.cos(O)) + (e * Math.sin(O)));
                        o.push((s * Math.sin(O)) - (e * Math.cos(O)));
                        o.push((s * Math.cos(O)) + (e * Math.sin(O)))
                    } else {
                        while (r.length > 1) {
                            var i = r.shift() - I;
                            var b = r.shift() - H;
                            o.push((b * Math.sin(O)) - (i * Math.cos(O)));
                            o.push((b * Math.cos(O)) + (i * Math.sin(O)))
                        }
                    }
                    var d = ["poly"];
                    while (o.length > 1) {
                        d.push(o.shift() + I);
                        d.push(o.shift() + H)
                    }
                    return d
                }
            };
        var C, E;
        if (M == "polygon" || M == "path") {
            var V = Number.MAX_VALUE;
            var A = Number.MIN_VALUE;
            var U = Number.MAX_VALUE;
            var v = Number.MIN_VALUE;
            var B = M == "polygon" ? false : true;
            C = [];
            E = ["poly"];
            if (I.length == H.length) {
                for (var X = 0; X < I.length; X++) {
                    if (this.validateNumbers([I[X], H[X]])) {
                        V = Math.min(V, I[X]);
                        A = Math.max(A, I[X]);
                        U = Math.min(U, H[X]);
                        v = Math.max(v, H[X]);
                        C.push([I[X], H[X]]);
                        E.push(I[X], H[X])
                    } else {
                        return false
                    }
                }
                I = (V + A) / 2;
                H = (U + v) / 2;
                for (var X = 0; X < C.length; X++) {
                    C[X][0] -= I;
                    C[X][1] -= H
                }
                if (O) {
                    E = aa(E)
                }
                if (C) {
                    Q(B);
                    aj(C)
                }
                return E
            } else {
                return false
            }
        } else {
            if (this.validateNumbers([I, H, K, Y])) {
                if (af) {
                    C = M == "rectangle" ? this.clip("rect", [I - (K / 2), H - (Y / 2), K, Y], ag) : this.clip("circle", [I, H], ag);
                    if (C) {
                        if (M == "rectangle") {
                            K = C[2];
                            Y = C[3];
                            I = C[0] + (K / 2);
                            H = C[1] + (Y / 2)
                        } else {
                            I = C[0];
                            H = C[1]
                        }
                    } else {
                        return false
                    }
                }
                var j = K / 2;
                var ah = Y / 2;
                var P = Y / K;
                var T = 1.5;
                var E = ["rect", I - j, H - ah, I + j, H + ah];
                switch (M) {
                case "image":
                    C = false;
                    var F = this;
                    var D = new Image();
                    D.onload = function () {
                        if (F.graphType == "Network" && !F.preScaleNetwork) {
                            I = (F.offsetX + I) * F.scaleFactor;
                            H = (F.offsetY + H) * F.scaleFactor
                        }
                        F.ctx.save();
                        F.ctx.translate(I, H);
                        if (O) {
                            F.ctx.rotate(O)
                        }
                        F.ctx.drawImage(D, -j * F.scaleFactor, -ah * F.scaleFactor, K * F.scaleFactor, Y * F.scaleFactor);
                        F.ctx.restore()
                    };
                    D.src = L;
                    D.style.zIndex = ae != null ? ae : 6001;
                    break;
                case "circle":
                case "sphere":
                    E = ["circle", I, H, K / 2];
                    C = false;
                    Q();
                    this.ctx.beginPath();
                    this.ctx.arc(0, 0, K / 2, 0, Math.PI * 2, true);
                    this.ctx.closePath();
                    aj();
                    break;
                case "box":
                case "rect":
                case "rectangle":
                    C = [
                        [-j, -ah],
                        [j, -ah],
                        [j, ah],
                        [-j, ah]
                    ];
                    break;
                case "rect2":
                    return this.drawShape("rectangle", I, H, K, K / 2, Z, S, R, O, G, af, ag, N, ac);
                    break;
                case "rect3":
                    return this.drawShape("rectangle", I, H, K, K / 3, Z, S, R, O, G, af, ag, N, ac);
                    break;
                case "roundrect":
                    var J = K > Y ? Y / 4 : K / 4;
                    C = false;
                    Q();
                    this.ctx.beginPath();
                    ctx.moveTo(-j, -ah + J);
                    ctx.lineTo(-j, ah - J);
                    ctx.quadraticCurveTo(-j, ah, -j + J, ah);
                    ctx.lineTo(j - J, ah);
                    ctx.quadraticCurveTo(j, ah, j, ah - J);
                    ctx.lineTo(j, -ah + J);
                    ctx.quadraticCurveTo(j, -ah, j - J, -ah);
                    ctx.lineTo(-j + J, -ah);
                    ctx.quadraticCurveTo(-j, -ah, -j, -ah + J);
                    this.ctx.closePath();
                    aj();
                    break;
                case "square":
                    C = [
                        [-j, -j],
                        [j, -j],
                        [j, j],
                        [-j, j]
                    ];
                    break;
                case "triangle":
                    if (K == Y) {
                        return this.drawShape("equilateral", I, H, K, Y, Z, S, R, O, G, af, ag)
                    }
                    E = ["poly", I, H - ah, I + j, H + ah, I - j, H + ah];
                    C = [
                        [0, -ah],
                        [j, ah],
                        [-j, ah]
                    ];
                    break;
                case "equilateral":
                    var J = K * Math.sqrt(3) / 6;
                    var g = J / 2;
                    var n = Math.sqrt((J * J) - (g * g));
                    E = ["poly", I, H - J, I + n, H + g, I - n, H + g];
                    C = [
                        [0, -ah],
                        [j, ah],
                        [-j, ah]
                    ];
                    break;
                case "diamond":
                case "rhombus":
                    E = ["poly", I, H - ah, I + j, H, I, H + ah, I - j, H];
                    C = [
                        [0, -ah],
                        [j, 0],
                        [0, ah],
                        [-j, 0]
                    ];
                    break;
                case "hexagon":
                    var ad = K / 3;
                    var l = ad - j;
                    C = [
                        [l, -ah],
                        [-l, -ah],
                        [j, 0],
                        [-l, ah],
                        [l, ah],
                        [-j, 0]
                    ];
                    break;
                case "octagon":
                    var ai = K / 4;
                    var W = Y / 4;
                    C = [
                        [-ai, -ah],
                        [ai, -ah],
                        [j, -W],
                        [j, W],
                        [ai, ah],
                        [-ai, ah],
                        [-j, W],
                        [-j, -W]
                    ];
                    break;
                case "oval":
                    C = false;
                    Q();
                    this.ctx.scale(1, P);
                    this.ctx.beginPath();
                    this.ctx.arc(0, 0, K / 2, 0, Math.PI * 2, true);
                    this.ctx.closePath();
                    aj();
                    break;
                case "oval2":
                    return this.drawShape("oval", I, H, K, K / 2, Z, S, R, O, G, af, ag, N, ac);
                    break;
                case "oval3":
                    return this.drawShape("oval", I, H, K, K / 3, Z, S, R, O, G, af, ag, N, ac);
                    break;
                case "ellipse":
                    var a = j * 0.5522848;
                    var ab = ah * 0.5522848;
                    C = false;
                    Q();
                    this.ctx.beginPath();
                    this.ctx.moveTo(0, ah);
                    this.ctx.bezierCurveTo(0, ah - ab, j - a, 0, j, 0);
                    this.ctx.bezierCurveTo(j + a, 0, K, ah - ab, K, ah);
                    this.ctx.bezierCurveTo(K, ah + ab, j + a, Y, j, Y);
                    this.ctx.bezierCurveTo(j - a, Y, 0, ah + ab, 0, ah);
                    this.ctx.closePath();
                    aj();
                    break;
                case "ellipse2":
                    return this.drawShape("ellipse", I, H, K, K / 2, Z, S, R, O, G, af, ag, N, ac);
                    break;
                case "ellipse3":
                    return this.drawShape("ellipse", I, H, K, K / 3, Z, S, R, O, G, af, ag, N, ac);
                    break;
                case "plus":
                    C = [
                        [-T, -ah],
                        [T, -ah],
                        [T, -T],
                        [j, -T],
                        [j, T],
                        [T, T],
                        [T, ah],
                        [-T, ah],
                        [-T, T],
                        [-j, T],
                        [-j, -T],
                        [-T, -T],
                        [-T, -ah]
                    ];
                    break;
                case "minus":
                    C = [
                        [-j, -T],
                        [j, -T],
                        [j, T],
                        [-j, T],
                        [-j, -T]
                    ];
                    break;
                case "mdavid":
                    var ad = K / 3;
                    var m = Math.PI / 6;
                    E = ["circle", I, H, K / 2];
                    C = false;
                    Q();
                    this.ctx.beginPath();
                    this.ctx.moveTo(ad, 0);
                    for (var X = 0; X < 11; X++) {
                        this.ctx.rotate(m);
                        if (X % 2 == 0) {
                            this.ctx.lineTo((ad / 0.55), 0)
                        } else {
                            this.ctx.lineTo(ad, 0)
                        }
                    }
                    this.ctx.closePath();
                    aj();
                    break;
                case "star":
                    var k = K / 4;
                    var q = Math.PI / 5;
                    E = ["circle", I, H, K / 2];
                    C = false;
                    Q();
                    this.ctx.rotate(q * 0.45);
                    this.ctx.beginPath();
                    this.ctx.moveTo(k, 0);
                    for (var X = 0; X < 9; X++) {
                        this.ctx.rotate(q);
                        if (X % 2 == 0) {
                            this.ctx.lineTo((k / 0.35), 0)
                        } else {
                            this.ctx.lineTo(k, 0)
                        }
                    }
                    this.ctx.closePath();
                    aj();
                    break;
                case "pie":
                    if (typeof (N) == "undefined") {
                        N = 0
                    }
                    if (typeof (ac) == "undefined") {
                        ac = Math.PI * 2
                    }
                    C = false;
                    Q();
                    this.ctx.scale(1, P);
                    this.ctx.beginPath();
                    this.ctx.arc(0, 0, K / 2, N, ac, false);
                    this.ctx.closePath();
                    aj();
                    break;
                case "pie0":
                    return this.drawShape("pie", I, H, K, K / 2, Z, S, R, O, G, af, ag, 0, Math.PI / 5);
                    break;
                case "pie1":
                    return this.drawShape("pie", I, H, K, K / 2, Z, S, R, O, G, af, ag, 0, Math.PI / 3.5);
                    break;
                case "pie2":
                    return this.drawShape("pie", I, H, K, K / 2, Z, S, R, O, G, af, ag, 0, Math.PI / 2);
                    break;
                case "pie3":
                    return this.drawShape("pie", I, H, K, K / 2, Z, S, R, O, G, af, ag, 0, Math.PI / 1.5);
                    break;
                case "pie4":
                    return this.drawShape("pie", I, H, K, K / 2, Z, S, R, O, G, af, ag, 0, Math.PI);
                    break;
                case "pie5":
                    return this.drawShape("pie", I, H, K, K / 2, Z, S, R, O, G, af, ag, 0, Math.PI * 1.2);
                    break;
                case "pie6":
                    return this.drawShape("pie", I, H, K, K / 2, Z, S, R, O, G, af, ag, 0, Math.PI * 1.4);
                    break;
                case "pie7":
                    return this.drawShape("pie", I, H, K, K / 2, Z, S, R, O, G, af, ag, 0, Math.PI * 1.6);
                    break;
                case "pie8":
                    return this.drawShape("pie", I, H, K, K / 2, Z, S, R, O, G, af, ag, 0, Math.PI * 1.8);
                    break;
                case "pie9":
                    return this.drawShape("pie", I, H, K, K / 2, Z, S, R, O, G, af, ag, 0, Math.PI * 2);
                    break;
                case "pacman":
                    return this.drawShape("pie", I, H, K, K / 2, Z, S, R, O, G, af, ag, Math.PI * 5 / 4, Math.PI * 3 / 4);
                    break
                }
                if (O) {
                    E = aa(E)
                }
                if (C) {
                    Q();
                    aj(C)
                }
                return E
            } else {
                return false
            }
        }
    };
    this.setRGB = function () {
        this.reds = [];
        this.greens = [];
        this.blues = [];
        this.yellows = [];
        this.cyans = [];
        this.purples = [];
        this.indicatorBins = this.allVsAll ? Math.min(this.x, this.y) / (this.layoutRows * 2) : Math.min(this.x, this.y) / 4;
        this.indicatorBins = this.indicatorBins > 512 ? 1 : this.indicatorBins > 256 ? 2 : this.indicatorBins > 128 ? 4 : 8;
        var f = this.indicatorBins;
        var d = parseInt(256 / f);
        var e;
        var b;
        if (this.indicatorCenter == "rainbow-green") {
            e = f - 1;
            b = 255;
            for (var a = 0; a < d; a++) {
                this.greens.push("rgb(0,255," + e + ")");
                this.cyans.push("rgb(0," + b + ",255)");
                this.blues.push("rgb(" + e + ",0,255)");
                this.purples.push("rgb(255,0," + b + ")");
                this.reds.push("rgb(255," + e + ",0)");
                this.yellows.push("rgb(" + b + ",255,0)");
                e += f;
                b -= f
            }
        } else {
            if (this.indicatorCenter.match("rainbow")) {
                e = f - 1;
                b = 255;
                for (var a = 0; a < d; a++) {
                    this.reds.push("rgb(255,0," + e + ")");
                    this.purples.push("rgb(" + b + ",0,255)");
                    this.blues.push("rgb(0," + e + ",255)");
                    this.cyans.push("rgb(0,255," + b + ")");
                    this.greens.push("rgb(" + e + ",255,0)");
                    this.yellows.push("rgb(255," + b + ",0)");
                    e += f;
                    b -= f
                }
            } else {
                if (this.indicatorCenter == "black") {
                    e = 255;
                    for (var a = 0; a < d; a++) {
                        this.reds.push("rgb(" + e + ",0,0)");
                        this.purples.push("rgb(" + e + ",0," + e + ")");
                        this.blues.push("rgb(0,0," + e + ")");
                        this.cyans.push("rgb(0," + e + "," + e + ")");
                        this.greens.push("rgb(0," + e + ",0)");
                        this.yellows.push("rgb(" + e + "," + e + ",0)");
                        e -= f
                    }
                } else {
                    this.reds.push("rgb(255,0,0)");
                    this.purples.push("rgb(255,0,255)");
                    this.blues.push("rgb(0,0,255)");
                    this.cyans.push("rgb(0,255,255)");
                    this.greens.push("rgb(0,255,0)");
                    this.yellows.push("rgb(255,255,0)");
                    e = f - 1;
                    for (var a = 0; a < d - 1; a++) {
                        this.reds.push("rgb(255," + e + "," + e + ")");
                        this.purples.push("rgb(255," + e + ",255)");
                        this.blues.push("rgb(" + e + "," + e + ",255)");
                        this.cyans.push("rgb(" + e + ",255,255)");
                        this.greens.push("rgb(" + e + ",255," + e + ")");
                        this.yellows.push("rgb(255,255," + e + ")");
                        e += f
                    }
                }
            }
        }
    };
    this.setLineTypes = function () {
        this.lines = this.getLineTypes()
    };
    this.initializeAttributes = function () {
        this.setLineTypes();
        this.setRGB()
    };
    this.initializeAttributes()
};
CanvasXpress.prototype.initLayout = function () {
    this.isValidLayout = function () {
        var a = this.layout.split(/X/i);
        if (a.length == 2) {
            this.layoutRows = a[0];
            this.layoutCols = a[1]
        } else {
            this.layoutRows = 1;
            this.layoutCols = 1;
            this.layoutValid = false
        }
        if (!this.graphType.match(/Scatter/) && this.layoutComb && !this.autoExtend && this.data.l && (this.data.l.vars || this.data.l.smps) && this.data.l.weight && this.data.l.type) {
            this.data.l.comp = [];
            if (this.data.l.vars && this.data.l.smps) {
                var f = 0;
                for (var d = 0; d < this.data.l.smps.length; d++) {
                    for (var b = 0; b < this.data.l.vars.length; b++) {
                        this.data.l.comp[f] = [this.getVariableIndices(this.data.l.vars[b]), this.getSampleIndices(this.data.l.smps[d])];
                        f++
                    }
                }
                this.layoutRows = this.data.l.smps.length;
                this.layoutCols = this.data.l.vars.length
            } else {
                if (this.data.l.vars) {
                    for (var d = 0; d < this.data.l.vars.length; d++) {
                        this.data.l.comp[d] = this.getVariableIndices(this.data.l.vars[d])
                    }
                } else {
                    for (var d = 0; d < this.data.l.smps.length; d++) {
                        this.data.l.comp[d] = this.getSampleIndices(this.data.l.smps[d])
                    }
                }
                if (this.graphType != "Pie") {
                    if (this.graphOrientation == "vertical") {
                        this.layoutRows = this.data.l.comp.length
                    } else {
                        this.layoutCols = this.data.l.comp.length
                    }
                }
            }
            this.layoutValid = true;
            this.layoutAdjust = true
        } else {
            if (this.layoutComb && !this.autoExtend && this.data.l && this.data.l.smps && this.data.l.weight && this.data.l.type) {
                this.data.l.comp = [];
                var e = this.getVariableIndices(this.data.y.vars);
                for (var d = 0; d < this.data.l.type.length; d++) {
                    this.data.l.comp[d] = e
                }
                this.layoutValid = true
            } else {
                if (!this.autoExtend && this.data.l && this.data.l.comp) {
                    if (this.data.l.comp.length <= (this.layoutRows * this.layoutCols)) {
                        this.layoutValid = true
                    } else {
                        this.layoutValid = false
                    }
                } else {
                    this.layoutValid = false
                }
            }
        }
    };
    this.setLayout = function (b) {
        if (b == 0) {
            this.layoutCols = 1;
            this.layoutRows = 1;
            this.scaleX = 1;
            this.scaleY = 1;
            this.translateX = 0;
            this.translateY = 0;
            this.layout = "1X1";
            this.legendBox = true;
            this.layoutValid = false
        } else {
            if (!this.graphType.match(/Scatter|Pie|Venn|Network|Genome/)) {
                this.layoutCols = b;
                this.layoutRows = Math.ceil(this.varIndices.length / b);
                this.layout = this.layoutRows + "X" + this.layoutCols;
                this.autoExtend = false;
                this.data.l = {};
                this.data.l["comp"] = [];
                if (this.graphType == "Candlestick") {
                    for (var a = 0; a < this.varIndices.length; a++) {
                        this.data.l["comp"].push(this.varIndices)
                    }
                } else {
                    for (var a = 0; a < this.varIndices.length; a++) {
                        this.data.l["comp"].push([this.varIndices[a]])
                    }
                    this.legendBox = false
                }
                this.layoutValid = true
            }
        }
        this.draw()
    };
    this.addLayoutResizer = function (b) {
        var a = 0;
        if (this.resizerVisibility && this.resizerPosition == b) {
            a += this.resizerWidth + (this.margin * 2);
            if (this.resizerVisibility == "scroller") {
                a += 14 + this.margin
            }
        }
        return a
    };
    this.getLayoutResizerDimensions = function () {
        if (this.resizerVisibility) {
            var z, k, f, e;
            var g = this.data.l.comp.length - 1;
            var m = this.resizerWidth;
            var A = this.resizerWidth;
            var C = this.layoutTop;
            var d = this.layoutRight;
            var s = this.layoutBottom;
            var j = this.layoutLeft;
            var B = this.getAxesVals(0);
            var u = 0;
            var q = 0;
            switch (this.resizerPosition) {
            case "right":
                z = "xAxis";
                k = "y";
                f = "v";
                e = "r";
                C += this["subGraphTop" + 0];
                j = this.resizerVisibility == "scroller" ? this.width - ((this.margin * 2) + this.resizerWidth + 14) : this.width - (this.margin + this.resizerWidth);
                m = this.height - (C + s + this["subGraphBottom" + g]);
                u += this.resizerWidth;
                break;
            case "left":
                z = "xAxis";
                k = "y";
                f = "v";
                e = "l";
                C += this["subGraphTop" + 0];
                j = this.resizerVisibility == "scroller" ? (this.margin * 2) + 14 : this.margin;
                m = this.height - (C + s + this["subGraphBottom" + g]);
                u += this.resizerWidth;
                break;
            case "top":
                z = "xAxis";
                k = "x";
                f = "h";
                e = "t";
                C = this.resizerVisibility == "scroller" ? (this.margin * 2) + 14 : this.margin;
                j += this["subGraphLeft" + 0];
                A = this.width - (j + d + this["subGraphRight" + g]);
                q += this.resizerWidth;
                break;
            case "bottom":
                z = "xAxis";
                k = "x";
                f = "h";
                e = "b";
                C = this.resizerVisibility == "scroller" ? this.height - ((this.margin * 2) + this.resizerWidth + 14) : this.width - (this.margin + this.resizerWidth);
                j += this["subGraphLeft" + 0];
                A = this.width - (j + d + this["subGraphRight" + g]);
                q += this.resizerWidth;
                break
            }
            return ([z, j + u, j + u + A, C + q, C + q + m, k, f, e, B, j, C, A, m])
        }
    };
    this.drawLayoutResizer = function () {
        if (this.resizerVisibility) {
            var a = this.getLayoutResizerDimensions();
            this.showAxesResizer(a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7], a[8], "", 0, 0, 0);
            this.drawLayoutResizerData(a);
            this.drawLayoutResizerMasks(a)
        }
    };
    this.updateAxesResizerLayout = function (a) {
        if (this.resizerVisibility && this.layoutValid) {
            var b = this.getLayoutResizerDimensions();
            this.showAxesResizer(b[0], b[1], b[2], b[3], b[4], b[5], b[6], b[7], b[8], "", 0, 0, 0)
        }
    };
    this.drawLayoutResizerData = function (m) {
        if (this.resizerBackgroundImage) {}
        if (this.resizerDataIndex > -1) {
            this.setAllSamplesVisible();
            var a, k, b, e, o;
            var n = this.graphType == "Candlestick" ? "close" : false;
            var h = [];
            var f = [];
            var p = [];
            for (var g = 0; g < this.smpIndices.length; g++) {
                h.push(this.getDataAtPos(this.resizerDataIndex, g, n))
            }
            var j = this.range(h);
            if (this.resizerPosition.match(/top|bottom/)) {
                a = m[11] / (this.smpIndices.length - 1);
                k = m[12] / ((j[1] - j[0]) * 1.2);
                b = (j[1] - j[0]) * 0.1 * k;
                e = m[9];
                var l = m[10] + m[12];
                f.push(e);
                p.push(l);
                for (var g = 0; g < h.length; g++) {
                    o = l - (((h[g] - j[0]) * k) + b);
                    f.push(e);
                    p.push(o);
                    e += a
                }
                e -= a;
                f.push(e);
                p.push(l)
            } else {
                a = m[12] / (this.smpIndices.length - 1);
                k = m[11] / ((j[1] - j[0]) * 1.2);
                b = (j[1] - j[0]) * 0.1 * k;
                o = m[10];
                f.push(m[9]);
                p.push(m[10]);
                for (var g = 0; g < h.length; g++) {
                    e = m[9] + ((h[g] - j[0]) * k) + b;
                    f.push(e);
                    p.push(o);
                    o += a
                }
                o -= a;
                f.push(m[9]);
                p.push(o)
            }
            this.drawShape("polygon", f, p, false, false, this.resizerAreaColorCurrent, this.resizerAreaColorOutlineCurrent, "closed")
        }
    };
    this.drawLayoutResizerMasks = function (i) {
        var h = 14;
        var g = 9 - 1;
        var f = parseInt(g / 2);
        var b = this.resizerEventData.areas;
        if (this.resizerEventData.pos == "h") {
            var e = b.lim[0] + b.min[0];
            var a = b.lim[0] + b.max[0] + 1;
            this.rectangle(i[9], i[10], e - i[9], this.resizerWidth, this.resizerAreaColor, this.resizerAreaColor);
            this.rectangle(a, i[10], (i[9] + i[11]) - (a - 1), this.resizerWidth, this.resizerAreaColor, this.resizerAreaColor)
        } else {}
    };
    this.setLayoutLeft = function () {
        var a = 0;
        if (this.graphOrientation != "vertical" && this.segregateSamplesBy && this.segregateVariablesBy) {
            this.set1DVarSmpLabelFont();
            a = this.fontSize + (this.margin * 4) + this.measureText(this.maxSmpNameStr, this.smpLabelFont) + this.getSmpOverlaysLength()
        } else {
            if (this.graphOrientation != "vertical" && this.segregateVariablesBy) {
                this.set1DVarSmpLabelFont();
                a = (this.margin * 2) + this.measureText(this.maxSmpNameStr, this.smpLabelFont) + this.getSmpOverlaysLength()
            } else {
                if ((this.segregateSamplesBy || this.segregateVariablesBy) && this.graphOrientation == "vertical") {
                    a = this.fontSize + (this.margin * 2)
                } else {
                    if (this.graphType == "Candlestick" && this.graphOrientation != "vertical") {
                        this.setTimeAxis();
                        this.set1DVarSmpLabelFont();
                        a = this.getSampleLabelLength() + this.getSmpOverlaysLength()
                    } else {
                        if (this.graphType == "Pie") {
                            a = (this.margin * 2) + this.measureText("XX", this.smpLabelFont)
                        }
                    }
                }
            }
        }
        return a + this.addLayoutResizer("left")
    };
    this.setLayoutRight = function () {
        var a = 0;
        if (this.graphType == "Pie" && this.showLegend) {
            var b = this.getPieLegendDimensions();
            a = b[0]
        }
        return a + this.addLayoutResizer("right")
    };
    this.setLayoutTop = function () {
        var a = 0;
        if (((this.segregateSamplesBy || this.segregateVariablesBy) && this.graphOrientation != "vertical") || (this.segregateSamplesBy && this.segregateVariablesBy) || (this.graphType == "Pie")) {
            a = this.fontSize + (this.margin * 2)
        }
        return a + this.addLayoutResizer("top")
    };
    this.setLayoutBottom = function () {
        var a = 0;
        if (this.segregateSamplesBy && this.segregateVariablesBy) {
            a = this.getLegendHeight()
        } else {
            if (this.segregateVariablesBy && this.graphOrientation == "vertical") {
                this.set1DVarSmpLabelFont();
                a = (this.margin * 2) + this.measureText(this.maxSmpNameStr, this.smpLabelFont) + this.getSmpOverlaysLength()
            } else {
                if (this.graphType == "Candlestick" && this.graphOrientation == "vertical") {
                    this.setTimeAxis();
                    this.set1DVarSmpLabelFont();
                    a = this.getSampleLabelLength() + this.getSmpOverlaysLength()
                }
            }
        }
        return a + this.addLayoutResizer("bottom")
    };
    this.drawLayoutSampleOverlays = function (b) {
        var a = this.smpIndices;
        this.smpIndices = b;
        this.set1DVarSmpLabelFont();
        this.smpIndices = a;
        this.showSampleNames = true;
        this.showOverlays = true;
        this.draw1DYLayout()
    };
    this.drawLayoutVariableLegend = function () {
        this.showLegend = true;
        if (this.graphType == "Pie") {
            this.drawPieLegend()
        } else {
            this.draw1DLegend()
        }
        this.showLegend = false
    };
    this.setLayoutDimensions = function () {
        this.offsetX = 0;
        this.offsetY = 0;
        this.layoutLeft = this.setLayoutLeft();
        this.layoutRight = this.setLayoutRight();
        this.layoutTop = this.setLayoutTop();
        this.layoutBottom = this.setLayoutBottom()
    };
    this.drawLabelsLayout = function () {
        var o = 0;
        var n = 0;
        var m = 0;
        var e = 0;
        if (this.resizerVisibility) {
            if (this.resizerPosition == "top") {
                n = this.resizerVisibility == "scroller" ? (this.margin * 2) + this.resizerWidth + 14 : this.margin + this.resizerWidth;
                e = n
            } else {
                if (this.resizerPosition == "left") {
                    o = this.resizerVisibility == "scroller" ? (this.margin * 2) + this.resizerWidth + 14 : this.margin + this.resizerWidth;
                    m = o
                }
            }
        }
        if (this.segregateSamplesBy && this.segregateVariablesBy) {
            n += (this.fontSize / 2) + (this.margin * 2);
            o = this.layoutLeft;
            for (var q = 0; q < this.data.l.vars.length; q++) {
                var g = q * this.data.l.smps.length;
                var p = (this.width - (this.layoutLeft + this.layoutRight)) * this["subGraphWeight" + g][0];
                o += (p / 2);
                this.drawText(this.data.l.name[g][0], o, n, this.font, this.foreground, "center", "middle");
                o += (p / 2)
            }
            o = m + (this.fontSize / 2) + this.margin;
            n = this.layoutTop;
            for (var g = 0; g < this.data.l.smps.length; g++) {
                var k = (this.height - (this.layoutTop + this.layoutBottom)) * this["subGraphWeight" + g][1];
                n += (k / 2);
                this.drawText(this.data.l.name[g][1], o, n, this.font, this.foreground, "center", "middle", -Math.PI / 2);
                n += (k / 2)
            }
        } else {
            if ((this.segregateSamplesBy || this.segregateVariablesBy) && this.graphOrientation == "vertical" && this.data.l.name) {
                o += (this.fontSize / 2) + this.margin;
                n += this.layoutTop;
                for (var g = 0; g < this.data.l.name.length; g++) {
                    var k = (this.height - (this.layoutTop + this.layoutBottom)) * this["subGraphWeight" + g];
                    n += (k / 2);
                    this.drawText(this.data.l.name[g], o, n, this.font, this.foreground, "center", "middle", -Math.PI / 2);
                    n += (k / 2)
                }
            } else {
                if ((this.segregateSamplesBy || this.segregateVariablesBy) && this.graphOrientation == "horizontal" && this.data.l.name) {
                    n += (this.fontSize / 2) + this.margin;
                    o += this.layoutLeft;
                    for (var g = 0; g < this.data.l.name.length; g++) {
                        var p = (this.width - (this.layoutLeft + this.layoutRight)) * this["subGraphWeight" + g];
                        o += (p / 2);
                        this.drawText(this.data.l.name[g], o, n, this.font, this.foreground, "center", "middle");
                        o += (p / 2)
                    }
                } else {
                    if (this.graphType == "Pie" && (this.showPieGrid || this.showPieSampleLabel)) {
                        var k, p;
                        var l = 0;
                        var b = this.layoutTop;
                        var d = this.layoutLeft;
                        var a = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"];
                        for (var g = 0; g < this.layoutRows; g++) {
                            k = (this.height - (this.layoutTop + this.layoutBottom)) * this["subGraphWeight" + l][1];
                            o = this.layoutLeft - this.margin;
                            b += k;
                            if (this.showPieGrid) {
                                this.drawText(g + 1, o, b - (k / 2), this.font, this.foreground, "right", "middle")
                            }
                            for (var f = 0; f < this.layoutCols; f++) {
                                if (this.xAxis.length <= l) {
                                    break
                                }
                                p = (this.width - (this.layoutLeft + this.layoutRight)) * this["subGraphWeight" + l][0];
                                d += p;
                                n = this.layoutTop - this.margin;
                                if (g == 0) {
                                    if (this.showPieGrid) {
                                        this.drawText(a[f], d - (p / 2), n, this.font, this.foreground, "right", "middle")
                                    }
                                }
                                if (this.showPieSampleLabel) {
                                    this.drawText(this.xAxis[l], d - (p / 1.1), b - (k / 5), this.legendFont, this.foreground, "center", "middle")
                                }
                                l++
                            }
                            d = this.layoutLeft
                        }
                    }
                }
            }
        }
    };
    this.setLayoutGraphAttributes = function () {
        var g = 0;
        for (var d = 0; d < this.layoutRows; d++) {
            for (var b = 0; b < this.layoutCols; b++) {
                var e = "subGraphType" + g;
                if (!this[e]) {
                    if (this.data.l.type && this.data.l.type[g]) {
                        this[e] = this.data.l.type[g]
                    } else {
                        this[e] = this.graphType
                    }
                }
                var f = "subSummaryType" + g;
                if (this[e] == "Boxplot" || this.summaryType == "iqr") {
                    this[f] = "iqr"
                } else {
                    if (this.summaryType == "median") {
                        this[f] = "median"
                    } else {
                        if (this.summaryType == "sum") {
                            this[f] = "sum"
                        } else {
                            if (this[e] == "Correlation") {
                                this[f] = "cor"
                            } else {
                                if (this[e] == "Candlestick") {
                                    if ((this.graphOrientation == "vertical" && d > 0) || (this.graphOrientation == "horizontal" && b == 0)) {
                                        this[f] = "volume"
                                    } else {
                                        this[f] = "candle"
                                    }
                                } else {
                                    this[f] = "mean"
                                }
                            }
                        }
                    }
                }
                var a = "subTransformType" + g;
                if (!this[a]) {
                    if (this.data.l.transform && this.data.l.transform[g]) {
                        this[a] = this.data.l.transform[g]
                    } else {
                        this[a] = this.subTransformType
                    }
                }
                g++
            }
        }
    };
    this.setLayoutWeights = function () {
        if (this.data.l.weight) {
            var e = 0;
            var f = 0;
            var a;
            var b;
            if (this.allVsAll) {
                for (var d = 0; d < this.layoutRows; d++) {
                    a = "subGraphWeight" + d;
                    b = this[a] ? this[a] : this.data.l.weight[d];
                    e += b
                }
                for (var d = 0; d < this.data.l.weight.length; d++) {
                    a = "subGraphWeight" + d;
                    b = this[a] ? this[a] : this.data.l.weight[d];
                    this.data.l.weight[d] = b / e;
                    this[a] = this.data.l.weight[d]
                }
            } else {
                if (this.data.l.vars && this.data.l.smps) {
                    for (var d = 0; d < this.data.l.weight.length; d++) {
                        a = "subGraphWeight" + d;
                        b = this[a] ? this[a] : this.data.l.weight[d];
                        e += b[0];
                        f += b[1]
                    }
                    for (var d = 0; d < this.data.l.weight.length; d++) {
                        a = "subGraphWeight" + d;
                        b = this[a] ? this[a] : this.data.l.weight[d];
                        this.data.l.weight[d][0] = b[0] / e * this.data.l.smps.length;
                        this.data.l.weight[d][1] = b[1] / f * this.data.l.vars.length;
                        this[a] = this.data.l.weight[d]
                    }
                } else {
                    if (this.graphType == "Pie") {
                        for (var d = 0; d < this.data.l.comp.length; d++) {
                            a = "subGraphWeight" + d;
                            this[a] = this.data.l.weight[d]
                        }
                    } else {
                        for (var d = 0; d < this.data.l.weight.length; d++) {
                            a = "subGraphWeight" + d;
                            b = this[a] ? this[a] : this.data.l.weight[d];
                            e += b
                        }
                        for (var d = 0; d < this.data.l.weight.length; d++) {
                            a = "subGraphWeight" + d;
                            b = this[a] ? this[a] : this.data.l.weight[d];
                            this.data.l.weight[d] = b / e;
                            this[a] = this.data.l.weight[d]
                        }
                    }
                }
            }
        } else {
            if (this.data.l.comp) {
                this.data.l.weight = [];
                for (var d = 0; d < this.data.l.comp.length; d++) {
                    a = "subGraphWeight" + d;
                    this.data.l.weight[d] = this.data.l.comp.length;
                    this[a] = this.data.l.weight[d]
                }
            } else {
                alert("Wuz going on dude? This aint't gonna work!")
            }
        }
    };
    this.setMinimumLayoutAxisFonts = function () {
        var h = 0;
        var e = Number.MAX_VALUE;
        for (var d = 0; d < this.layoutRows; d++) {
            for (var b = 0; b < this.layoutCols; b++) {
                var g, f, a;
                if (this.data.l.desc && this.data.l.desc[h]) {
                    g = this.data.l.desc[h]
                } else {
                    g = "NA"
                }
                a = this.graphOrientation == "vertical" ? this.height * this["subGraphWeight" + h] : this.width * this["subGraphWeight" + h];
                this.setAxisFont(g, a);
                f = this.getFontPt(this.axisTitleFont);
                if (f < e) {
                    e = f;
                    this.minLayoutAxis = e;
                    this.minLayoutAxisTitle = g
                }
                h++
            }
        }
    };
    this.getLayoutSummaryTypes = function () {
        var g = 0;
        var a = {};
        var e = [];
        for (var d = 0; d < this.layoutRows; d++) {
            for (var b = 0; b < this.layoutCols; b++) {
                var f = "subSummaryType" + g;
                if (!a.hasOwnProperty(this[f])) {
                    e.push(this[f]);
                    a[this[f]] = true
                }
                g++
            }
        }
        return e
    };
    this.setLayoutRange = function () {
        if (this.graphType.match(/Scatter/)) {
            this.xAxis = this.data.y.smps;
            this.yAxis = this.data.y.smps;
            this.initAxes(true)
        }
    };
    this.validateLayoutSummaryGrouping = function () {
        if (this.isGroupedData) {
            var b = this.getLayoutSummaryTypes();
            for (var a = 0; a < b.length; a++) {
                if (!this.isValidPlotData(b[a])) {
                    this.groupSamples(this.groupingFactors, b);
                    break
                }
            }
        }
    };
    this.resetLayout = function () {
        delete(this.data.l);
        delete(this.layoutParams);
        this.offsetX = 0;
        this.offsetY = 0;
        this.layoutValidN = false;
        this.layoutValidR = false;
        this.layoutValidC = false;
        this.layoutComb = false;
        this.layout = "1X1"
    };
    this.drawLayoutCompartments = function (h) {
        var f, e, D, C, l, o, y, q, a;
        var A = 0;
        var s = 0;
        var w = this.varIndices;
        var u = this.smpIndices;
        var d = this.getObjectArray(w);
        var g = this.getObjectArray(u);
        var f = this.width;
        var B = false;
        var z = ["left", "right", "top", "bottom", "x", "y", "width", "height", "weight", "offsetX", "offsetY", "varIndices", "smpIndices", "grpIndices", "graphType", "summaryType", "transformType", "varIndicesStart", "smpIndicesStart", "dataEvent", "minData", "maxData", "xAxisMin", "xAxisMax", "xAxis2Min", "xAxis2Max", "yAxisMin", "yAxisMax", "zAxisMin", "zAxisMax", "xAxisAbsMin", "xAxisAbsMax", "xAxis2AbsMin", "xAxis2AbsMax", "yAxisAbsMin", "yAxisAbsMax", "zAxisAbsMin", "zAxisAbsMax", "xAxisUnit", "xAxis2Unit", "yAxisUnit", "zAxisUnit", "setMin", "setMax", "setMin2", "setMax2", "setMinX", "setMaxX", "setMinY", "setMaxY", "setMinZ", "setMaxZ"];
        var n = ["graphType", "transformType", "varIndicesStart", "smpIndicesStart", "dataEvent", "setMin", "setMax", "setMin2", "setMax2", "setMinX", "setMaxX", "setMinY", "setMaxY", "setMinZ", "setMaxZ"];
        var r;
        if (this.layoutComb) {
            if (!this.graphType.match(/Scatter/)) {
                if (this.data.l.vars) {
                    y = 0;
                    if (this.data.l.comp) {
                        for (var x = 0; x < this.data.l.comp.length; x++) {
                            y = Math.max(y, this.data.l.comp[x].length)
                        }
                    }
                    this.layoutMaxLegend = y
                } else {
                    this.layoutMaxLegend = this.varIndices.length
                }
            }
            this.setLayoutDimensions();
            this.initAxes(false, true);
            if (this.layoutParams && this.layoutValidN > -1) {
                r = this.layoutParams;
                if (r[this.layoutValidN]) {
                    for (var x = 0; x < z.length; x++) {
                        r[this.layoutValidN][z[x]] = this[z[x]]
                    }
                }
            }
            this.offsetX = this.layoutLeft;
            this.offsetY = this.layoutTop;
            this.layoutMaxLegendLabel = this.data.l.vars ? this.maxVarNameStr : this.maxSmpNameStr;
            this.layoutParams = [];
            if (!this.graphType.match(/Scatter/)) {
                if (this.data.l.vars && this.data.l.smps) {
                    this.layoutRows = this.data.l.smps.length;
                    this.layoutCols = this.data.l.vars.length;
                    this.layoutMaxSamples = this.smpIndices.length * this.layoutCols / this.layoutRows;
                    this.legendPosition = "bottom"
                } else {
                    y = Math.max(this.layoutRows, this.layoutCols);
                    if (this.graphType != "Pie") {
                        if (this.graphOrientation == "vertical") {
                            this.layoutRows = y;
                            this.layoutCols = 1;
                            this.legendPosition = "right"
                        } else {
                            this.layoutRows = 1;
                            this.layoutCols = y;
                            this.legendPosition = "bottom"
                        }
                    }
                }
            }
            if (this.graphType.match(/Scatter/) && !this.layoutAdjust) {
                this.setLayoutRange()
            }
            this.setLayoutWeights();
            this.setMinimumLayoutAxisFonts();
            this.setLayoutGraphAttributes();
            this.validateLayoutSummaryGrouping();
            this.drawLabelsLayout();
            for (var x = 0; x < this.layoutRows; x++) {
                for (var v = 0; v < this.layoutCols; v++) {
                    this.layoutCurrent = A;
                    if (this.graphType == "Pie") {
                        if (this.data.l.smps.length <= A) {
                            break
                        }
                    }
                    if (r) {
                        for (var t = 0; t < n.length; t++) {
                            this[n[t]] = r[A][n[t]]
                        }
                    }
                    this.weight = this["subGraphWeight" + A];
                    this.graphType = this["subGraphType" + A];
                    this.summaryType = this["subSummaryType" + A];
                    this.transformType = this["subTransformType" + A];
                    if (this["subTransformType" + A]) {
                        this.isTransformedData = true
                    }
                    if (this.data.l.log && this.data.l.log[A]) {
                        this.isLogData = this.data.l.log[A]
                    }
                    if (this.graphType.match(/Scatter/)) {
                        if (this.data.l.smps[A].xAxis && this.data.l.smps[A].yAxis) {
                            this.xAxis = this.data.l.smps[A].xAxis;
                            this.yAxis = this.data.l.smps[A].yAxis;
                            this.xAxisIndices = this.getSampleIndices(this.xAxis);
                            this.yAxisIndices = this.getSampleIndices(this.yAxis)
                        } else {
                            this.xAxis = false;
                            this.yAxis = false;
                            this.xAxisIndices = false;
                            this.yAxisIndices = false
                        }
                        this.layoutWidth = (this.width - (this.layoutLeft + this.layoutRight)) * this["subGraphWeight" + A];
                        this.layoutHeight = (this.height - (this.layoutTop + this.layoutBottom)) * this["subGraphWeight" + A];
                        this.varIndices = this.data.l.comp[A];
                        if (this.graphType.match(/Scatter/) && this.layoutAdjust && this.xAxis && this.yAxis) {
                            this.initAxes(true);
                            this.layoutAxis = 3
                        } else {
                            if (this.graphType.match(/Scatter/)) {
                                this.layoutAxis = v == 0 ? 2 : 0;
                                this.layoutAxis = x == this.layoutRows && this.layoutAxis ? 3 : x == this.layoutRows ? 1 : 0
                            }
                        }
                    } else {
                        if (this.data.l.vars && this.data.l.smps) {
                            B = this.graphOrientation == "vertical" ? true : false;
                            this.showLegend = false
                        } else {
                            if (this.graphType == "Pie") {
                                B = false;
                                this.showLegend = false
                            } else {
                                B = this.data.l.smps ? true : false
                            }
                        }
//                        this.showSampleNames = B;
                        this.showOverlays = B;
                        this.xAxisTitle = this.data.l.desc && this.data.l.desc[A] ? this.data.l.desc[A] : false;
                        if (this.data.l.vars && this.data.l.smps) {
                            this.layoutHeight = (this.height - (this.layoutTop + this.layoutBottom)) * this["subGraphWeight" + A][1];
                            this.layoutWidth = (this.width - (this.layoutLeft + this.layoutRight)) * this["subGraphWeight" + A][0];
                            a = [];
                            for (var t = 0; t < this.data.l.comp[A][0].length; t++) {
                                if (d.hasOwnProperty(this.data.l.comp[A][0][t])) {
                                    a.push(this.data.l.comp[A][0][t])
                                }
                            }
                            this.varIndices = a;
                            a = [];
                            for (var t = 0; t < this.data.l.comp[A][1].length; t++) {
                                if (g.hasOwnProperty(this.data.l.comp[A][1][t])) {
                                    a.push(this.data.l.comp[A][1][t])
                                }
                            }
                            this.smpIndices = a
                        } else {
                            if (this.graphType == "Pie") {
                                this.layoutWidth = (this.width - (this.layoutLeft + this.layoutRight)) * this["subGraphWeight" + A][0];
                                this.layoutHeight = (this.height - (this.layoutTop + this.layoutBottom)) * this["subGraphWeight" + A][1]
                            } else {
                                if (this.graphOrientation == "vertical") {
                                    this.layoutHeight = (this.height - (this.layoutTop + this.layoutBottom)) * this["subGraphWeight" + A];
                                    this.layoutWidth = this.width - (this.layoutLeft + this.layoutRight)
                                } else {
                                    this.layoutWidth = (this.width - (this.layoutLeft + this.layoutRight)) * this["subGraphWeight" + A];
                                    this.layoutHeight = this.height - (this.layoutTop + this.layoutBottom)
                                }
                            }
                            a = [];
                            if (this.data.l.vars) {
                                for (var t = 0; t < this.data.l.comp[A].length; t++) {
                                    if (d.hasOwnProperty(this.data.l.comp[A][t])) {
                                        a.push(this.data.l.comp[A][t])
                                    }
                                }
                                this.varIndices = a
                            } else {
                                for (var t = 0; t < this.data.l.comp[A].length; t++) {
                                    if (g.hasOwnProperty(this.data.l.comp[A][t])) {
                                        a.push(this.data.l.comp[A][t])
                                    }
                                }
                                this.smpIndices = a
                            }
                        }
                        if (this.layoutAdjust) {
                            this.layoutAxis = 3;
                            this.initAxes(true)
                        }
                    }
                    h.call(this);
                    this["subGraphTop" + A] = this.top;
                    this["subGraphBottom" + A] = this.bottom;
                    this["subGraphRight" + A] = this.right;
                    this["subGraphLeft" + A] = this.left;
                    this["subGraphX" + A] = this.x;
                    this["subGraphY" + A] = this.y;
                    if (this.graphType.match(/Scatter/)) {
                        if (x == v) {
                            var p = this.scaleTextToFont(this.maxSmpNameStr, this.maxTextSize, (this.layoutWidth - (this.layoutLeft + this.layoutRight)));
                            this.drawText(this.data.y.smps[x], this.offsetX + this.left + (this.x / 2), this.offsetY + this.top + (this.y / 2), p, this.foreground, "center", "middle")
                        }
                    } else {
                        if (!B) {
                            if (this.data.l.vars && this.data.l.smps) {
                                if (this.graphOrientation != "vertical" && A % this.data.l.vars.length == 0) {
                                    this.drawLayoutSampleOverlays(u)
                                }
                                if (A >= this.data.l.vars.length * (this.data.l.smps.length - 1)) {
                                    this.drawLayoutVariableLegend()
                                }
                            } else {
                                if (this.graphType != "Pie") {
                                    if ((this.graphOrientation == "vertical" && (A + 1) == y) || (this.graphOrientation != "vertical" && A == 0)) {
                                        this.drawLayoutSampleOverlays(u)
                                    }
                                } else {
                                    if (this.graphType == "Pie" && A == 0) {
                                        this.drawLayoutVariableLegend()
                                    }
                                }
                            }
                        } else {
                            if (this.data.l.vars && this.data.l.smps) {
                                if (A >= this.data.l.vars.length * (this.data.l.smps.length - 1)) {
                                    this.drawLayoutVariableLegend()
                                }
                            }
                        }
                    }
                    var b = {};
                    for (var t = 0; t < z.length; t++) {
                        b[z[t]] = this[z[t]]
                    }
                    b.width = this.layoutWidth;
                    b.height = this.layoutHeight;
                    b.subGraphType = this.graphType;
                    b.subSummaryType = this.SummaryType;
                    b.subTransformType = this.transformType;
                    this.layoutParams.push(b);
                    if (this.graphType.match(/Scatter/) || (this.data.l.vars && this.data.l.smps)) {
                        this.offsetX += this.layoutWidth
                    } else {
                        if (this.graphOrientation == "vertical") {
                            this.offsetY += this.layoutHeight
                        } else {
                            this.offsetX += this.layoutWidth
                        }
                    }
                    A++
                }
                if (this.graphType.match(/Scatter|Pie/) || (this.data.l.vars && this.data.l.smps)) {
                    this.offsetX = this.layoutLeft;
                    this.offsetY += this.layoutHeight
                }
            }
            this.drawLayoutResizer();
            this.varIndices = w;
            this.smpIndices = u;
            this.showSampleNames = true;
            this.showOverlays = true;
            this.showLegend = true
        } else {
            this.scaleX = 1 / this.layoutCols;
            this.scaleY = 1 / this.layoutRows;
            this.ctx.save();
            this.ctx.scale(this.scaleX, this.scaleY);
            C = 0;
            e = this.height;
            for (var x = 0; x < this.layoutRows; x++) {
                this.translateY = this.height * x;
                f = this.width;
                D = 0;
                for (var v = 0; v < this.layoutCols; v++) {
                    this.translateX = this.width * v;
                    if (this.data.l.comp.length > A) {
                        if (v == 0 && this.layoutCols == 1) {
                            this.layoutAxis = 3
                        } else {
                            if (x == 0 && this.layoutCols > 1) {
                                this.layoutAxis = 1
                            } else {
                                if (x == this.layoutRows - 1) {
                                    this.layoutAxis = 2
                                } else {
                                    this.layoutAxis = 0
                                }
                            }
                        }
                        this.varIndices = this.data.l.comp[A];
                        if (this.layoutAdjust) {
                            this.layoutAxis = 3;
                            this.initAxes(true)
                        }
                        if (this.data.l.type && this.data.l.type[A]) {
                            this.graphType = this.data.l.type[A]
                        }
                        h.call(this);
                        D += parseFloat(f);
                        this.ctx.translate(D, 0);
                        if (this.layoutCols > 2) {
                            f /= Math.pow(this.layoutCols + 1, this.layoutCols + 1)
                        } else {
                            f /= Math.pow(this.layoutCols + 4, this.layoutCols + 4)
                        }
                        if (this.layoutMulticolor) {
                            this.colors.push(this.colors.shift())
                        }
                        A++
                    }
                }
                C += parseFloat(e);
                this.ctx.translate(-D * this.layoutCols, C);
                e /= Math.pow(this.layoutRows + 1, this.layoutRows + 1)
            }
            this.ctx.restore();
            this.ctx.translate(-D * (this.layoutCols - 1), -C * (this.layoutRows - 1));
            this.ctx.scale(this.layoutCols, this.layoutRows);
            this.varIndices = w;
            if (this.layoutMulticolor) {
                for (var x = 0; x < A; x++) {
                    this.colors.unshift(this.colors.pop())
                }
            }
        }
    };
    this.initializeLayout = function () {
        this.isValidLayout()
    };
    this.initializeLayout()
};
CanvasXpress.prototype.initConfigurator = function () {
    this.addConfigurator = function (a) {
        return function (g, d) {
            if (a.isVML || a.disableConfigurator) {
                return
            }
            a.configuringOn = true;
            if (d) {
                a.configuringNetwork = d
            }
            var n = a.adjustedCoordinates(g);
            if (n) {
                var l = n.x;
                var j = n.y;
                var h = document.getElementById(a.target + "canvasXpressConfigurator");
                var f = document.getElementById(a.target + "canvasXpressConfiguratorKey");
                if (h) {
                    h.style.display = "block";
                    h.style.left = l + "px";
                    h.style.top = j + "px";
                    setTimeout(function () {
                        f.focus()
                    }, 300);
                    return
                } else {
                    h = document.createElement("div")
                }
                f = document.createElement("input");
                var o = document.createElement("select");
                var m = document.createElement("input");
                var i = document.createElement("input");
                h.id = a.target + "canvasXpressConfigurator";
                h.style.left = l + "px";
                h.style.top = j + "px";
                h.style.position = "absolute";
                f.id = a.target + "canvasXpressConfiguratorKey";
                f.type = "text";
                f.style.display = "block";
                o.id = a.target + "canvasXpressConfiguratorSugestions";
                o.style.display = "none";
                a.addEvtListener(o, "change", a.clickSelect, false);
                a.addEvtListener(o, "click", a.clickSelect, false);
                m.id = a.target + "canvasXpressConfiguratorValue";
                m.type = "text";
                m.style.display = "none";
                i.id = a.target + "canvasXpressConfiguratorButton";
                i.value = "Draw";
                i.type = "button";
                i.style.display = "none";
                a.addEvtListener(i, "click", a.handleSelect, false);
                h.appendChild(f);
                h.appendChild(o);
                h.appendChild(m);
                h.appendChild(i);
                a.canvas.parentNode.appendChild(h);
                setTimeout(function () {
                    f.focus()
                }, 300)
            }
        }
    }(this);
    this.selectConfig = function (j) {
        var l;
        var g = document.getElementById(this.target + "canvasXpressConfiguratorSugestions");
        var b = document.getElementById(this.target + "canvasXpressConfiguratorKey");
        var a = document.getElementById(this.target + "canvasXpressConfiguratorValue");
        var h = [];
        if (this.configuringNetwork) {
            l = this.configuringNetwork < this.data.nodes.length ? this.data.nodes[this.configuringNetwork] : this.data.edges[this.configuringNetwork - this.data.nodes.length]
        } else {
            l = this
        }
        if (b && a && a.value && l.hasOwnProperty(b.value)) {
            if (this.getKeyCode(j) == 13) {
                if (typeof (this[b.value]) == "function") {
                    this.resetConfigurator();
                    this[b.value]()
                } else {
                    l[b.value] = JSON.parse(a.value);
                    this.resetConfigurator();
                    this.draw()
                }
            } else {
                return
            }
        }
        this.resetSugestions();
        if (this.configuringNetwork) {
            for (var f in l) {
                h.push(f)
            }
        } else {
            for (var f = 0; f < this.config.length; f++) {
                if (this.config[f].toLowerCase().indexOf(b.value.toLowerCase()) >= 0) {
                    h.push(this.config[f])
                }
            }
            for (var f = 0; f < this.functions.length; f++) {
                if (this.functions[f].toLowerCase().indexOf(b.value.toLowerCase()) >= 0) {
                    h.push(this.functions[f])
                }
            }
        }
        if (h.length > 0) {
            if (h.length == 1) {
                b.value = h[0];
                this.resetSugestions();
                this.configureSelect()
            } else {
                h.sort();
                var f = document.getElementById(this.target + "canvasXpressImage");
                f.style.zIndex = 0;
                for (var f = 0; f < h.length; f++) {
                    var l = document.createElement("option");
                    l.text = h[f];
                    l.value = h[f];
                    try {
                        g.add(l, null)
                    } catch (d) {
                        g.add(l)
                    }
                }
                g.style.display = "block";
                g.size = Math.min(h.length, 5)
            }
        }
        return false
    };
    this.resetSugestions = function () {
        var e = document.getElementById(this.target + "canvasXpressConfiguratorSugestions");
        var d = document.getElementById(this.target + "canvasXpressConfiguratorValue");
        var a = document.getElementById(this.target + "canvasXpressConfiguratorButton");
        if (e) {
            if (e.hasChildNodes()) {
                while (e.childNodes.length >= 1) {
                    e.removeChild(e.firstChild)
                }
            }
            e.style.display = "none"
        }
        if (d) {
            d.value = "";
            d.style.display = "none"
        }
        if (a) {
            a.style.display = "none"
        }
    };
    this.clickSelect = function (a) {
        return function (f) {
            var b = document.getElementById(a.target + "canvasXpressConfiguratorKey");
            var d = document.getElementById(a.target + "canvasXpressConfiguratorSugestions");
            b.value = d.value;
            a.resetSugestions();
            a.configureSelect();
            return false
        }
    }(this);
    this.configureSelect = function () {
        var e = document.getElementById(this.target + "canvasXpressConfiguratorKey");
        var d = document.getElementById(this.target + "canvasXpressConfiguratorValue");
        var a = document.getElementById(this.target + "canvasXpressConfiguratorButton");
        if (e && d && a) {
            d.style.display = "block";
            a.style.display = "block";
            if (this.configuringNetwork) {
                if (typeof (this[e.value]) == "function") {
                    d.style.display = "none"
                } else {
                    if (this.configuringNetwork < this.data.nodes.length) {
                        d.value = JSON.stringify(this.data.nodes[this.configuringNetwork][e.value])
                    } else {
                        d.value = JSON.stringify(this.data.edges[this.configuringNetwork - this.data.nodes.length][e.value])
                    }
                }
            } else {
                if (typeof (this[e.value]) == "function") {
                    d.style.display = "none"
                } else {
                    d.value = JSON.stringify(this[e.value])
                }
            }
            setTimeout(function () {
                d.focus();
                d.select()
            }, 300)
        }
    };
    this.handleSelect = function (a) {
        return function (f) {
            var d = document.getElementById(a.target + "canvasXpressConfiguratorKey");
            var b = document.getElementById(a.target + "canvasXpressConfiguratorValue");
            if (d && b) {
                if (a.configuringNetwork) {
                    if (a.configuringNetwork < a.data.nodes.length) {
                        if (typeof (a[d.value]) == "function") {
                            a[d.value](a.data.nodes[a.configuringNetwork])
                        } else {
                            a.data.nodes[a.configuringNetwork][d.value] = JSON.parse(b.value)
                        }
                    } else {
                        if (typeof (a[d.value]) == "function") {
                            a[d.value](a.data.edges[a.configuringNetwork - a.data.nodes.length])
                        } else {
                            a.data.edges[a.configuringNetwork - a.data.nodes.length][d.value] = JSON.parse(b.value)
                        }
                    }
                } else {
                    if (typeof (a[d.value]) == "function") {
                        a[d.value]()
                    } else {
                        a[d.value] = JSON.parse(b.value)
                    }
                }
                d.value = "";
                b.value = "";
                a.resetConfigurator();
                a.draw()
            }
            return false
        }
    }(this);
    this.resetConfigurator = function () {
        var b = document.getElementById(this.target + "canvasXpressImage");
        var d = document.getElementById(this.target + "canvasXpressConfigurator");
        var a = document.getElementById(this.target + "canvasXpressConfiguratorKey");
        if (b && d && a) {
            this.resetSugestions();
            b.style.zIndex = 9000;
            a.value = "";
            d.style.display = "none";
            this.configuringOn = false;
            this.configuringNetwork = false
        }
    }
};
CanvasXpress.prototype.initDataTable = function () {
    this.addDataTable = function () {
        if (document.getElementById(this.target + "canvasXpressTable")) {
            return
        }
        var e = function (j, h, b, i) {
                var m = i ? document.createElement("th") : document.createElement("td");
                m.innerHTML = h;
                m.style.padding = "2px 5px";
                m.style.margin = "2px 5px";
                m.style.border = "1px solid";
                if (b) {
                    m["class"] = b
                }
                j.appendChild(m)
            };
        var g = document.createElement("table");
        var k = document.createElement("thead");
        var a = document.createElement("tbody");
        var l = document.createElement("tr");
        g.id = this.target + "canvasXpressTable";
        g.style.border = "1px solid";
        g.style.borderCollapse = "collapse";
        g.style.fontSize = "x-small";
        e(l, "", "b");
        if (this.data.x) {
            for (var f in this.data.x) {
                e(l, f, "x", true)
            }
        }
        for (var f = 0; f < this.data.y.vars.length; f++) {
            e(l, this.data.y.vars[f], "v", true)
        }
        k.appendChild(l);
        if (this.data.z) {
            l = document.createElement("tr");
            for (var f in this.data.z) {
                e(l, f, "z", true);
                if (this.data.z) {
                    for (var d in this.data.z) {
                        e(l, "", "b")
                    }
                }
                for (var d = 0; d < this.data.y.vars.length; d++) {
                    e(l, this.data.z[f][d], "z")
                }
            }
            k.appendChild(l)
        }
        for (var f = 0; f < this.data.y.smps.length; f++) {
            l = document.createElement("tr");
            e(l, this.data.y.smps[f], "s", true);
            if (this.data.x) {
                for (var d in this.data.x) {
                    e(l, this.data.x[d][f], "x")
                }
            }
            for (var d = 0; d < this.data.y.vars.length; d++) {
                e(l, this.data.y.data[d][f])
            }
            a.appendChild(l)
        }
        g.appendChild(k);
        g.appendChild(a);
        this.canvas.parentNode.appendChild(g)
    };
    this.resetDataTable = function () {
        this.removeTarget(this.target + "canvasXpressTable")
    };
    this.hideUnhideDataTable = function () {
        var a = document.getElementById(this.target + "canvasXpressTable");
        if (a) {
            if (a.style.display == "block") {
                a.style.display = "none"
            } else {
                a.style.display = "block"
            }
        }
    }
};
CanvasXpress.prototype.initDendrogramEvents = function () {
    this.modifyDendrogram = function (h) {
        var f = h.t.t == "varDendrogram" ? this.varIndicesStart : this.smpIndicesStart;
        var d = h.t.t.replace("Dendrogram", "s");
        var g = this[h.t.t].object;
        var i = h.t.d + "-" + h.t.o;
        var e = this.findDendrogramBranch(g, i);
        var b = this.reverseDendrogramBranch(e);
        var a = this.writeNewick(g, true);
        this.data.t[d] = a;
        this.draw()
    };
    this.findDendrogramBranch = function (b, d, a) {
        if (b.id == d) {
            a = b
        }
        if (!a) {
            if (b.left) {
                a = this.findDendrogramBranch(b.left, d, a)
            }
            if (b.right) {
                a = this.findDendrogramBranch(b.right, d, a)
            }
        } else {
            return a
        }
        return a
    };
    this.reverseDendrogramBranch = function (b) {
        if (b && b.right && b.left) {
            b.right = this.reverseDendrogramBranch(b.right);
            b.left = this.reverseDendrogramBranch(b.left);
            var a = b.left;
            b.left = b.right;
            b.right = a;
            if (b.mid) {
                b.mid = b.mid.reverse()
            }
        }
        return b
    }
};
CanvasXpress.prototype.initSelectEvents = function () {
    this.showHideSelectedDataPoint = function (f, i) {
        this.stopEvent(f);
        if (i == 45 || i == 46) {
            var g = this.layoutComb ? this.layoutValidN : 0;
            var d = this.layoutComb ? this.layoutParams[g].graphType : this.graphType;
            if (d == "Network") {
                var b = [];
                var a = i == 45 ? false : true;
                for (var h in this.selectNode) {
                    b.push(h)
                }
                if (b.length > 0) {
                    this.hideUnhideNodes(b, a)
                }
            } else {
                if (d.match(/Scatter/)) {
                    if (i == 45) {
                        this.selectDataPoint = this.hideDataPoint;
                        this.hideDataPoint = []
                    } else {
                        this.hideDataPoint = this.selectDataPoint;
                        this.selectDataPoint = []
                    }
                }
            }
            this.draw()
        }
    };
    this.addRemoveToSelectedDataPoints = function (h, f) {
        var b = this.isBroadcasting ? CanvasXpress.references : [this];
        for (var e = 0; e < b.length; e++) {
            var g = b[e];
            if (!h) {
                var j = g.layoutComb ? g.layoutValidN : 0;
                h = g.layoutComb ? g.layoutParams[j].graphType : g.graphType
            }
            if (h == "Network") {
                var a = parseInt(f.join(", "));
                if (a < g.data.nodes.length) {
                    var d = g.data.nodes[a].id;
                    if (g.selectNode.hasOwnProperty(d)) {
                        g.isSelectNodes--;
                        delete g.selectNode[d]
                    } else {
                        g.isSelectNodes++;
                        g.selectNode[d] = true
                    }
                }
            } else {
                if (h.match(/Scatter/)) {
                    var d;
                    if (!g.selectDataPoint[f[0]]) {
                        g.selectDataPoint[f[0]] = {}
                    }
                    if (h == "Scatter3D") {
                        d = f[1] + ":" + f[2] + ":" + f[3]
                    } else {
                        d = f[1] + ":" + f[2]
                    }
                    if (g.selectDataPoint[f[0]].hasOwnProperty(d)) {
                        g.isSelectDataPoints--;
                        delete g.selectDataPoint[f[0]][d]
                    } else {
                        g.isSelectDataPoints++;
                        g.selectDataPoint[f[0]][d] = true
                    }
                }
            }
        }
    };
    this.resetSelectedDataPoints = function () {
        if (this.isSelectDataPoints) {
            this.selectDataPoint = [];
            this.hideDataPoint = [];
            this.isSelectDataPoints = 0
        }
    };
    this.resetSelectedNodes = function () {
        if (this.isSelectNodes) {
            this.selectNode = {};
            this.isSelectNodes = 0
        }
    }
};
CanvasXpress.prototype.initKeyEvents = function () {
    this.getKeyCode = function (a) {
        if (a) {
            if ((a.charCode) && (a.keyCode == 0)) {
                return a.charCode
            } else {
                return a.keyCode
            }
        }
    };
    this.registerKey = function (a) {
        if (!a) {
            a = window.event
        }
        var b = this.getKeyCode(a);
        if (this.eventKeys || (this.ctrlOn && this.altOn && b == 107)) {
            if (b == 27) {
                if (this.ctrlOn) {
                    if (this.shiftOn) {
                        if (this.ctrlShiftDisabled) {
                            this.ctrlShiftDisabled = false
                        } else {
                            this.ctrlShiftDisabled = true
                        }
                    } else {
                        if (this.altOn) {
                            if (this.ctrlAltDisabled) {
                                this.ctrlAltDisabled = false
                            } else {
                                this.ctrlAlttDisabled = true
                            }
                        } else {
                            if (this.ctrlDisabled) {
                                this.ctrlDisabled = false
                            } else {
                                this.ctrlDisabled = true
                            }
                        }
                    }
                } else {
                    if (this.shiftOn) {
                        if (this.ctrlOn) {
                            if (this.ctrlShiftDisabled) {
                                this.ctrlShiftDisabled = false
                            } else {
                                this.ctrlShiftDisabled = true
                            }
                        } else {
                            if (this.altOn) {
                                if (this.shiftAltDisabled) {
                                    this.shiftAltDisabled = false
                                } else {
                                    this.shiftAlttDisabled = true
                                }
                            } else {
                                if (this.shiftDisabled) {
                                    this.shiftDisabled = false
                                } else {
                                    this.shiftDisabled = true
                                }
                            }
                        }
                    } else {
                        if (this.altOn) {
                            if (this.configuringOn) {
                                this.masterReset(a)
                            } else {
                                if (this.ctrlOn) {
                                    if (this.ctrlAltDisabled) {
                                        this.ctrlAltDisabled = false
                                    } else {
                                        this.ctrlAlttDisabled = true
                                    }
                                } else {
                                    if (this.shiftOn) {
                                        if (this.shiftAltDisabled) {
                                            this.shiftAltDisabled = false
                                        } else {
                                            this.shiftAlttDisabled = true
                                        }
                                    } else {
                                        if (this.altDisabled) {
                                            this.altDisabled = false
                                        } else {
                                            this.altDisabled = true
                                        }
                                    }
                                }
                            }
                        } else {
                            this.masterReset(a)
                        }
                    }
                }
            } else {
                if (this.configuringOn) {
                    return
                } else {
                    if (b == 16) {
                        this.shiftOn = true;
                        if (this.ctrlOn) {
                            if (!this.ctrlShiftDisabled) {
                                this.showCtrlShiftShorts()
                            }
                        } else {
                            if (this.altOn) {
                                if (!this.shiftAltDisabled) {
                                    this.showShiftAltShorts()
                                }
                            } else {
                                if (!this.shiftDisabled) {
                                    this.showShiftShorts()
                                }
                            }
                        }
                    } else {
                        if (b == 17) {
                            this.ctrlOn = true;
                            if (this.shiftOn) {
                                if (!this.ctrlShiftDisabled) {
                                    this.showCtrlShiftShorts()
                                }
                            } else {
                                if (this.altOn) {
                                    if (!this.ctrlAltDisabled) {
                                        this.showCtrlAltShorts()
                                    }
                                } else {
                                    if (!this.ctrlDisabled) {
                                        this.showCtrlShorts()
                                    }
                                }
                            }
                        } else {
                            if (b == 18 || b == 224) {
                                this.altOn = true;
                                if (this.ctrlOn) {
                                    if (!this.ctrlAltDisabled) {
                                        this.showCtrlAltShorts()
                                    }
                                } else {
                                    if (this.shiftOn) {
                                        if (!this.shiftAltDisabled) {
                                            this.showShiftAltShorts()
                                        }
                                    } else {
                                        if (!this.altDisabled) {
                                            this.showAltShorts()
                                        }
                                    }
                                }
                            } else {
                                if ((b > 90 && b < 95) || b == 0) {
                                    this.winOn = true
                                } else {
                                    if (b == 107 || b == 109 || b == 61 || b == 187 || b == 189) {
                                        if (b == 109 || b == 189) {
                                            this.handleWheelEvent(a, -1)
                                        } else {
                                            this.handleWheelEvent(a, 1)
                                        }
                                    } else {
                                        if (b >= 33 && b <= 40) {
                                            this.handlePanning(a, b)
                                        } else {
                                            if (this.ctrlOn && this.shiftOn) {
                                                if (!this.ctrlShiftDisabled) {
                                                    if (b == 80) {
                                                        this.print()
                                                    }
                                                    if (b >= 33 && b <= 40) {
                                                        this.arrowMove(b)
                                                    }
                                                    document.defaultAction = true
                                                }
                                            } else {
                                                if (this.ctrlOn && this.altOn) {
                                                    if (!this.ctrlAltDisabled) {
                                                        if (b == 107) {
                                                            if (this.eventKeys) {
                                                                this.eventKeys = false
                                                            } else {
                                                                this.eventKeys = true
                                                            }
                                                        }
                                                        document.defaultAction = true
                                                    }
                                                } else {
                                                    if (this.shiftOn && this.altOn) {
                                                        if (!this.shiftAltDisabled) {
                                                            if (b >= 50 && b < 90) {
                                                                this.setGraphType(b)
                                                            }
                                                            document.defaultAction = true
                                                        }
                                                    } else {
                                                        if (this.ctrlOn) {
                                                            if (!this.ctrlDisabled) {
                                                                if (b >= 33 && b <= 40) {
                                                                    this.arrowMove(b)
                                                                } else {
                                                                    if (b == 45 || b == 46) {
                                                                        this.showHideSelectedDataPoint(a, b)
                                                                    } else {
                                                                        if (b >= 48 && b < 58) {
                                                                            this.setLayout(b - 48)
                                                                        } else {
                                                                            if (b >= 65 && b < 90) {
                                                                                if (b == 80) {
                                                                                    this.print()
                                                                                }
                                                                                if (this.graphType == "Network") {
                                                                                    this.alignDistributeSelectedNodes(b)
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                document.defaultAction = true
                                                            }
                                                        } else {
                                                            if (this.shiftOn) {
                                                                if (!this.shiftDisabled) {
                                                                    document.defaultAction = true
                                                                }
                                                            } else {
                                                                if (this.altOn) {
                                                                    if (!this.altDisabled) {
                                                                        document.defaultAction = true
                                                                    }
                                                                } else {
                                                                    this.resetKey()
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    };
    this.handlePanning = function (g, k) {
        if (this.graphType == "Network") {
            this.cancelEvent(g);
            var n = Math.abs(this.offsetX * this.panningStep);
            var m = Math.abs(this.offsetX * this.panningStep);
            if (k == 33) {
                this.panningY -= m * 5
            } else {
                if (k == 34) {
                    this.panningY += m * 5
                } else {
                    if (k == 35) {
                        this.panningX = n * 20;
                        this.panningY = m * 20
                    } else {
                        if (k == 36) {
                            this.panningX = 0;
                            this.panningY = 0
                        } else {
                            if (k == 37) {
                                this.panningX += n
                            } else {
                                if (k == 38) {
                                    this.panningY -= m
                                } else {
                                    if (k == 39) {
                                        this.panningX -= n
                                    } else {
                                        if (k == 40) {
                                            this.panningY += m
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            var b = this.calculateLayout;
            var a = this.randomNetwork;
            this.calculateLayout = false;
            this.randomNetwork = false;
            this.draw();
            this.calculateLayout = b;
            this.randomNetwork = a
        } else {
            if (this.graphType == "Heatmap") {
                this.cancelEvent(g);
                var h = this.layoutComb ? this.layoutValidN : 0;
                var q;
                var o = this.layoutComb ? this.layoutParams[h].varIndices : this.varIndices;
                if (this.isGroupedData) {
                    q = this.layoutComb ? this.layoutParams[h].grpIndices : this.grpIndices
                } else {
                    q = this.layoutComb ? this.layoutParams[h].smpIndices : this.smpIndices
                }
                this.setOriginalIndices(k, o, q);
                if (k == 35) {
                    return false
                } else {
                    if (k == 36) {
                        this.varIndicesStart = -1;
                        this.smpIndicesStart = -1;
                        this.varIndices = this.startingVarIndices[h];
                        if (this.isGroupedData) {
                            this.grpIndices = this.startingSmpIndices[h]
                        } else {
                            this.smpIndices = this.startingSmpIndices[h]
                        }
                        this.draw()
                    } else {
                        if (k == 37) {
                            if (this.varIndicesStart > 0) {
                                this.varIndices.unshift(this.startingVarIndices[h][this.varIndicesStart - 1]);
                                this.varIndices.pop();
                                this.varIndicesStart--;
                                this.draw()
                            }
                        } else {
                            if (k == 38 || k == 33) {
                                var p = k == 33 ? 5 : 1;
                                var j = false;
                                for (var f = 0; f < p; f++) {
                                    if (this.smpIndicesStart > 0) {
                                        this.smpIndices.unshift(this.startingSmpIndices[h][this.smpIndicesStart - 1]);
                                        this.smpIndices.pop();
                                        this.smpIndicesStart--;
                                        j = true
                                    }
                                }
                                if (j) {
                                    this.draw()
                                }
                            } else {
                                if (k == 39) {
                                    if (this.varIndicesStart > -1 && this.varIndicesStart + o.length < this.startingVarIndices[h].length) {
                                        this.varIndices.push(this.startingVarIndices[h][this.varIndices.length]);
                                        this.varIndices.shift();
                                        this.varIndicesStart++;
                                        this.draw()
                                    }
                                } else {
                                    if (k == 40 || k == 34) {
                                        var p = k == 34 ? 5 : 1;
                                        var j = false;
                                        for (var f = 0; f < p; f++) {
                                            if (this.smpIndicesStart > -1 && this.smpIndicesStart + q.length < this.startingSmpIndices[h].length) {
                                                this.smpIndices.push(this.startingSmpIndices[h][this.smpIndices.length]);
                                                this.smpIndices.shift();
                                                this.smpIndicesStart++;
                                                j = true
                                            }
                                        }
                                        if (j) {
                                            this.draw()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false
    };
    this.showCtrlShiftShorts = function () {
        var a = "<table class=shorts>";
        a += "<tr><th colspan=2>Shift + Ctrl</th></tr>";
        if (this.graphType == "Network") {
            if (this.is3DNetwork) {
                a += "<tr><td class=k>&rarr;</td><td class=d>Rotate clockwise " + (this.rotationStep * 1) + "&deg; continuously</td></tr>";
                a += "<tr><td class=k>&larr;</td><td class=d>Rotate anti-clockwise " + (this.rotationStep * 1) + "&deg; continuously</td></tr>";
                a += "<tr><td class=k>&darr;</td><td class=d>Rotate forward " + (this.rotationStep * 1) + "&deg; continuously</td></tr>";
                a += "<tr><td class=k>&uarr;</td><td class=d>Rotate backward " + (this.rotationStep * 1) + "&deg; continuously</td></tr>";
                a += "<tr><td class=k>PgUp</td><td class=d>Rotate anti-clockwise and backward " + (this.rotationStep * 3) + "&deg; continuously</td></tr>";
                a += "<tr><td class=k>PgDn</td><td class=d>Rotate clockwise and forward " + (this.rotationStep * 3) + "&deg; continuously</td></tr>"
            } else {
                a += "<tr><td class=k>&rarr; or &darr;</td><td class=d>Rotate clockwise " + (this.rotationStep * 1) + "&deg; continuously</td></tr>";
                a += "<tr><td class=k>&larr; or &uarr;</td><td class=d>Rotate anti-clockwise " + (this.rotationStep * 1) + "&deg; continuously</td></tr>";
                a += "<tr><td class=k>PgUp</td><td class=d>Rotate anti-clockwise " + (this.rotationStep * 3) + "&deg; continuously</td></tr>";
                a += "<tr><td class=k>PgDn</td><td class=d>Rotate clockwise " + (this.rotationStep * 3) + "&deg; continuously</td></tr>"
            }
        } else {
            if (this.graphType == "Scatter2D" || this.graphType == "ScatterBubble2D") {
                if (this.type2D == "XYZ") {
                    if (this.xAxis.length > 1) {
                        a += "<tr><td class=k>&rarr; or &darr; or &larr; or &uarr;</td><td class=d>Update samples in X, Y and Z axes continuously (1x)</td></tr>";
                        a += "<tr><td class=k>PgUp or PgDn</td><td class=d>Update samples in X, Y and Z axes continuously (2x)</td></tr>"
                    }
                } else {
                    if (this.type2D == "XY") {
                        if (this.zAxis.length > 1) {
                            a += "<tr><td class=k>&rarr; or &darr; or &larr; or &uarr;</td><td class=d>Update sample in Z axis continuously (1x)</td></tr>";
                            a += "<tr><td class=k>PgUp or PgDn</td><td class=d>Update sample in Z axis continuously (2x)</td></tr>"
                        }
                    } else {
                        if (this.type2D == "X") {
                            if (this.yAxis.length > 1) {
                                a += "<tr><td class=k>&rarr; or &darr; or &larr; or &uarr;</td><td class=d>Update samples in Y and Z axes continuously (1x)</td></tr>";
                                a += "<tr><td class=k>PgUp or PgDn</td><td class=d>Update samples in Y and Z axes continuously (2x)</td></tr>"
                            }
                        } else {
                            if (this.xAxis.length > 1) {
                                a += "<tr><td class=k>&rarr; or &darr; or &larr; or &uarr;</td><td class=d>Update samples in X and Z axes continuously (1x)</td></tr>";
                                a += "<tr><td class=k>PgUp or PgDn</td><td class=d>Update samples in X and Z axes continuously (2x)</td></tr>"
                            }
                        }
                    }
                }
            } else {
                if (this.graphType == "Scatter3D") {
                    a += "<tr><td class=k>&rarr; or &darr; or &larr; or &uarr;</td><td class=d>Rotate continuously</td></tr>";
                    a += "<tr><td class=k>PgUp or PgDn</td><td class=d>Rotate continuously</td></tr>"
                } else {
                    if (this.isSegregable()) {
                        a += "<tr><td class=k>Click and Drag</td><td class=d>Select Samples</td></tr>"
                    }
                }
            }
        }
        a += "<tr><td class=k>P</td><td class=d>Print</td></tr>";
        a += "</table>";
        this.showInfoSpan(false, a)
    };
    this.showCtrlAltShorts = function () {
        var a = "<table class=shorts>";
        a += "<tr><th colspan=2>Ctrl + Alt</th></tr>";
        a += "<tr><td class=k>k</td><td class=d>Enable/Disable key events</td></tr>";
        a += "</table>";
        this.showInfoSpan(false, a)
    };
    this.showShiftAltShorts = function () {
        var a = "<table class=shorts>";
        a += "<tr><th colspan=2>Shift + Alt</th></tr>";
        a += "<tr><td class=k>2</td><td class=d>Scatter2D</td></tr>";
        a += "<tr><td class=k>3</td><td class=d>Scatter3D</td></tr>";
        a += "<tr><td class=k>4</td><td class=d>ScatterBubble2D</td></tr>";
        a += "<tr><td class=k>A</td><td class=d>Area</td></tr>";
        a += "<tr><td class=k>B</td><td class=d>Bar</td></tr>";
        a += "<tr><td class=k>C</td><td class=d>BarLine</td></tr>";
        a += "<tr><td class=k>D</td><td class=d>Dotplot</td></tr>";
        a += "<tr><td class=k>G</td><td class=d>Genome</td></tr>";
        a += "<tr><td class=k>H</td><td class=d>Heatmap</td></tr>";
        a += "<tr><td class=k>I</td><td class=d>Boxplot</td></tr>";
        a += "<tr><td class=k>L</td><td class=d>Line</td></tr>";
        a += "<tr><td class=k>M</td><td class=d>Candlestick</td></tr>";
        a += "<tr><td class=k>N</td><td class=d>Network</td></tr>";
        a += "<tr><td class=k>P</td><td class=d>Pie</td></tr>";
        a += "<tr><td class=k>S</td><td class=d>Stacked</td></tr>";
        a += "<tr><td class=k>T</td><td class=d>StackedPercent</td></tr>";
        a += "<tr><td class=k>V</td><td class=d>Venn</td></tr>";
        a += "<tr><td class=k>X</td><td class=d>Correlation</td></tr>";
        a += "</table>";
        this.showInfoSpan(false, a)
    };
    this.showCtrlShorts = function () {
        var a = "<table class=shorts>";
        a += "<tr><th colspan=2>Ctrl</th></tr>";
        if (this.graphType == "Network") {
            if (this.is3DNetwork) {
                a += "<tr><td class=k>&rarr;</td><td class=d>Rotate clockwise " + (this.rotationStep * 1) + "&deg;</td></tr>";
                a += "<tr><td class=k>&larr;</td><td class=d>Rotate anti-clockwise " + (this.rotationStep * 1) + "&deg;</td></tr>";
                a += "<tr><td class=k>&darr;</td><td class=d>Rotate forward " + (this.rotationStep * 1) + "&deg;</td></tr>";
                a += "<tr><td class=k>&uarr;</td><td class=d>Rotate backward " + (this.rotationStep * 1) + "&deg;</td></tr>";
                a += "<tr><td class=k>PgUp</td><td class=d>Rotate anti-clockwise and backward " + (this.rotationStep * 3) + "&deg;</td></tr>";
                a += "<tr><td class=k>PgDn</td><td class=d>Rotate clockwise and forward " + (this.rotationStep * 3) + "&deg;</td></tr>"
            } else {
                a += "<tr><td class=k>&rarr; or &darr;</td><td class=d>Rotate clockwise " + (this.rotationStep * 1) + "&deg;</td></tr>";
                a += "<tr><td class=k>&larr; or &uarr;</td><td class=d>Rotate anti-clockwise " + (this.rotationStep * 1) + "&deg;</td></tr>";
                a += "<tr><td class=k>PgUp</td><td class=d>Rotate anti-clockwise " + (this.rotationStep * 3) + "&deg;</td></tr>";
                a += "<tr><td class=k>PgDn</td><td class=d>Rotate clockwise " + (this.rotationStep * 3) + "&deg;</td></tr>"
            }
            a += "<tr><td class=k>click</td><td class=d>Select/Unselect node</td></tr>";
            if (this.isSelectNodes) {
                a += "<tr><td class=k>Delete</td><td class=d>Hide selected nodes</td></tr>";
                a += "<tr><td class=k>Insert</td><td class=d>Show selected-hidden nodes</td></tr>";
                a += "<tr><td class=k>t</td><td class=d>Align top</td></tr>";
                a += "<tr><td class=k>r</td><td class=d>Align right</td></tr>";
                a += "<tr><td class=k>b</td><td class=d>Align bottom</td></tr>";
                a += "<tr><td class=k>l</td><td class=d>Align left</td></tr>";
                a += "<tr><td class=k>v</td><td class=d>Distribute verticaly</td></tr>";
                a += "<tr><td class=k>h</td><td class=d>Distribute Horizontaly</td></tr>";
                a += "<tr><td class=k>u</td><td class=d>Undo move</td></tr>"
            }
        } else {
            if (this.graphType == "Scatter2D" || this.graphType == "ScatterBubble2D") {
                if (this.type2D == "XYZ") {
                    if (this.xAxis.length > 1) {
                        a += "<tr><td class=k>&rarr; or &darr; or &larr; or &uarr;</td><td class=d>Update samples in X, Y and Z axes (1x)</td></tr>";
                        a += "<tr><td class=k>PgUp or PgDn</td><td class=d>Update samples in X, Y and Z axes (2x)</td></tr>"
                    }
                } else {
                    if (this.type2D == "XY") {
                        if (this.zAxis.length > 1) {
                            a += "<tr><td class=k>&rarr; or &darr; or &larr; or &uarr;</td><td class=d>Update sample in Z axis (1x)</td></tr>";
                            a += "<tr><td class=k>PgUp or PgDn</td><td class=d>Update sample in Z axis (2x)</td></tr>"
                        }
                    } else {
                        if (this.type2D == "X") {
                            if (this.yAxis.length > 1) {
                                a += "<tr><td class=k>&rarr; or &darr; or &larr; or &uarr;</td><td class=d>Update samples in Y and Z axes (1x)</td></tr>";
                                a += "<tr><td class=k>PgUp or PgDn</td><td class=d>Update samples in Y and Z axes (2x)</td></tr>"
                            }
                        } else {
                            if (this.xAxis.length > 1) {
                                a += "<tr><td class=k>&rarr; or &darr; or &larr; or &uarr;</td><td class=d>Update samples in X and Z axes (1x)</td></tr>";
                                a += "<tr><td class=k>PgUp or PgDn</td><td class=d>Update samples in X and Z axes (2x)</td></tr>"
                            }
                        }
                    }
                }
                if (this.isSelectDataPoints) {
                    a += "<tr><td class=k>Delete</td><td class=d>Hide selected nodes</td></tr>";
                    a += "<tr><td class=k>Insert</td><td class=d>Show selected-hidden nodes</td></tr>"
                }
            } else {
                if (this.graphType == "Scatter3D") {
                    a += "<tr><td class=k>&rarr;</td><td class=d>Rotate clockwise " + (this.rotationStep * 1) + "&deg;</td></tr>";
                    a += "<tr><td class=k>&larr;</td><td class=d>Rotate anti-clockwise " + (this.rotationStep * 1) + "&deg;</td></tr>";
                    a += "<tr><td class=k>&darr;</td><td class=d>Rotate forward " + (this.rotationStep * 1) + "&deg;</td></tr>";
                    a += "<tr><td class=k>&uarr;</td><td class=d>Rotate backward " + (this.rotationStep * 1) + "&deg;</td></tr>";
                    a += "<tr><td class=k>PgUp</td><td class=d>Rotate anti-clockwise and backward " + (this.rotationStep * 3) + "&deg;</td></tr>";
                    a += "<tr><td class=k>PgDn</td><td class=d>Rotate clockwise and forward " + (this.rotationStep * 3) + "&deg;</td></tr>";
                    a += "<tr><td class=k>click</td><td class=d>Select/Unselect data points</td></tr>";
                    if (this.isSelectDataPoints) {
                        a += "<tr><td class=k>Delete</td><td class=d>Hide selected nodes</td></tr>";
                        a += "<tr><td class=k>Insert</td><td class=d>Show selected-hidden nodes</td></tr>"
                    }
                } else {
                    if (this.isSegregable()) {
                        a += "<tr><td class=k>1 - 9</td><td class=d>Number of columns to plot individual variables</td></tr>";
                        a += "<tr><td class=k>0</td><td class=d>Reset layout</td></tr>"
                    }
                }
            }
        }
        a += "<tr><td class=k>P</td><td class=d>Print</td></tr>";
        a += "</table>";
        this.showInfoSpan(false, a)
    };
    this.showShiftShorts = function () {
        var a = "<table class=shorts>";
        a += "<tr><th colspan=2>Shift</th></tr>";
        a += "<tr><td class=k>&nbsp;</td><td class=d>&nbsp;</td></tr>";
        a += "</table>";
        this.showInfoSpan(false, a)
    };
    this.showAltShorts = function () {
        var a = "<table class=shorts>";
        a += "<tr><th colspan=2>Alt</th></tr>";
        if (this.graphType == "Network") {
            a += "<tr><td class=k>left-click node</td><td class=d>Configure Node</td></tr>"
        }
        a += "</table>";
        this.showInfoSpan(false, a)
    };
    this.arrowMove = function (e) {
        switch (this.graphType) {
        case "Network":
            if (this.is3DNetwork) {
                if (e == 33) {
                    this.xRotate += (this.rotationStep * 3);
                    this.yRotate -= (this.rotationStep * 3)
                } else {
                    if (e == 34) {
                        this.xRotate -= (this.rotationStep * 3);
                        this.yRotate += (this.rotationStep * 3)
                    } else {
                        if (e == 37) {
                            this.xRotate += this.rotationStep
                        } else {
                            if (e == 38) {
                                this.yRotate -= this.rotationStep
                            } else {
                                if (e == 39) {
                                    this.xRotate -= this.rotationStep
                                } else {
                                    if (e == 40) {
                                        this.yRotate += this.rotationStep
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (e == 33) {
                    this.network2DRotate -= (this.rotationStep * 3)
                } else {
                    if (e == 34) {
                        this.network2DRotate += (this.rotationStep * 3)
                    } else {
                        if (e == 37 || e == 38) {
                            this.network2DRotate -= this.rotationStep
                        } else {
                            if (e == 39 || e == 40) {
                                this.network2DRotate += this.rotationStep
                            }
                        }
                    }
                }
            }
            break;
        case "Scatter2D":
        case "ScatterBubble2D":
            var b;
            var d;
            if (this.type2D == "XYZ") {
                b = ["xAxisCurrent", "yAxisCurrent", "zAxisCurrent"];
                d = ["xAxis", "yAxis", "zAxis"]
            } else {
                if (this.type2D == "XY") {
                    b = ["zAxisCurrent"];
                    d = ["zAxis"]
                } else {
                    if (this.type2D == "X") {
                        b = ["yAxisCurrent", "zAxisCurrent"];
                        d = ["yAxis", "zAxis"]
                    } else {
                        b = ["xAxisCurrent", "zAxisCurrent"];
                        d = ["xAxis", "zAxis"]
                    }
                }
            }
            if (e == 33) {
                for (var a = 0; a < b.length; a++) {
                    this[b[a]] += 2
                }
            } else {
                if (e == 34) {
                    for (var a = 0; a < b.length; a++) {
                        this[b[a]] -= 2
                    }
                } else {
                    if (e == 35) {
                        for (var a = 0; a < b.length; a++) {
                            this[b[a]] = this[d[a]].length - 1
                        }
                    } else {
                        if (e == 36) {
                            for (var a = 0; a < b.length; a++) {
                                this[b[a]] = 0
                            }
                        } else {
                            if (e == 37 || e == 40) {
                                for (var a = 0; a < b.length; a++) {
                                    this[b[a]]--
                                }
                            } else {
                                if (e == 38 || e == 39) {
                                    for (var a = 0; a < b.length; a++) {
                                        this[b[a]]++
                                    }
                                }
                            }
                        }
                    }
                }
            }
            this.xAxisCurrent = this.xAxisCurrent < 0 ? this.xAxis.length - 1 : this.xAxisCurrent > this.xAxis.length - 1 ? 0 : this.xAxisCurrent;
            this.yAxisCurrent = this.yAxisCurrent < 0 ? this.xAxis.length - 1 : this.yAxisCurrent > this.yAxis.length - 1 ? 0 : this.yAxisCurrent;
            this.zAxisCurrent = this.zAxisCurrent < 0 ? this.xAxis.length - 1 : this.zAxisCurrent > this.zAxis.length - 1 ? 0 : this.zAxisCurrent;
            break;
        case "Scatter3D":
            if (e == 33) {
                this.xRotate += (this.rotationStep * 3);
                this.yRotate -= (this.rotationStep * 3)
            } else {
                if (e == 34) {
                    this.xRotate -= (this.rotationStep * 3);
                    this.yRotate += (this.rotationStep * 3)
                } else {
                    if (e == 35) {
                        this.xRotate = 0;
                        this.yRotate = 0;
                        this.zRotate = 45
                    } else {
                        if (e == 36) {
                            this.xRotate = 45;
                            this.yRotate = 0;
                            this.zRotate = 0
                        } else {
                            if (e == 37) {
                                this.xRotate += this.rotationStep
                            } else {
                                if (e == 38) {
                                    this.yRotate -= this.rotationStep
                                } else {
                                    if (e == 39) {
                                        this.xRotate -= this.rotationStep
                                    } else {
                                        if (e == 40) {
                                            this.yRotate += this.rotationStep
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            break
        }
        if (this.ctrlOn && this.shiftOn && !this.animationOn) {
            this.animationOn = true;
            this.animate(e)
        } else {
            this.draw()
        }
    };
    this.animate = function (d) {
        var a = this;
        var b = function () {
                this.update = function () {
                    if (!a.animationOn) {
                        clearInterval(e)
                    }
                    a.arrowMove(d);
                    switch (a.graphType) {
                    case "Network":
                        if (this.is3DNetwork) {
                            if (d == 33) {
                                this.xRotate += (this.rotationStep * 3);
                                this.yRotate -= (this.rotationStep * 3)
                            } else {
                                if (d == 34) {
                                    this.xRotate -= (this.rotationStep * 3);
                                    this.yRotate += (this.rotationStep * 3)
                                } else {
                                    if (d == 37) {
                                        this.xRotate -= this.rotationStep
                                    } else {
                                        if (d == 38) {
                                            this.yRotate -= this.rotationStep
                                        } else {
                                            if (d == 39) {
                                                this.xRotate += this.rotationStep
                                            } else {
                                                if (d == 40) {
                                                    this.yRotate += this.rotationStep
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if (d == 33) {
                                this.network2DRotate += (this.rotationStep * 3)
                            } else {
                                if (d == 34) {
                                    this.network2DRotate -= (this.rotationStep * 3)
                                } else {
                                    if (d == 37 || d == 38) {
                                        this.network2DRotate -= this.rotationStep
                                    } else {
                                        if (d == 39 || d == 40) {
                                            this.network2DRotate += this.rotationStep
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case "Scatter2D":
                    case "ScatterBubble2D":
                        if (d != 37) {
                            if (a.graphType == "ScatterBubble2D" && a.xAxisIndices.length > 1 && a.xAxisIndices.length == a.yAxisIndices.length && a.xAxisIndices.length == a.zAxisIndices.length) {
                                if (a.xAxisCurrent >= a.xAxis.length - 1) {
                                    a.xAxisCurrent = a.xAxis.length - 1;
                                    a.yAxisCurrent = a.yAxis.length - 1;
                                    a.zAxisCurrent = a.zAxis.length - 1;
                                    d = 37
                                }
                            } else {
                                if (a.xAxisIndices.length > 1 && a.xAxisIndices.length == a.yAxisIndices.length) {
                                    if (a.zAxisCurrent >= a.zAxis.length - 1) {
                                        a.zAxisCurrent = a.zAxis.length - 1;
                                        d = 37
                                    }
                                } else {
                                    if (a.xAxisIndices.length > a.yAxisIndices.length) {
                                        if (a.yAxisCurrent >= a.yAxis.length - 1) {
                                            a.yAxisCurrent = a.yAxis.length - 1
                                        }
                                        if (a.zAxisCurrent >= a.zAxis.length - 1) {
                                            a.zAxisCurrent = a.zAxis.length - 1
                                        }
                                        if (a.yAxisCurrent >= a.yAxis.length - 1 && a.zAxisCurrent >= a.zAxis.length - 1) {
                                            d = 37
                                        }
                                    } else {
                                        if (a.xAxisCurrent >= a.xAxis.length - 1) {
                                            a.xAxisCurrent = a.xAxis.length - 1
                                        }
                                        if (a.zAxisCurrent >= a.zAxis.length - 1) {
                                            a.zAxisCurrent = a.zAxis.length - 1
                                        }
                                        if (a.xAxisCurrent >= a.xAxis.length - 1 && a.zAxisCurrent >= a.zAxis.length - 1) {
                                            d = 37
                                        }
                                    }
                                }
                            }
                        } else {
                            if (d != 39) {
                                if (a.graphType == "ScatterBubble2D" && a.xAxisIndices.length > 1 && a.xAxisIndices.length == a.yAxisIndices.length && a.xAxisIndices.length == a.zAxisIndices.length) {
                                    if (a.xAxisCurrent <= 0) {
                                        a.xAxisCurrent = 0;
                                        a.yAxisCurrent = 0;
                                        a.zAxisCurrent = 0;
                                        d = 39
                                    }
                                } else {
                                    if (a.xAxisIndices.length > 1 && a.xAxisIndices.length == a.yAxisIndices.length) {
                                        if (a.zAxisCurrent <= 0) {
                                            a.zAxisCurrent = 0;
                                            d = 39
                                        }
                                    } else {
                                        if (a.xAxisIndices.length > a.yAxisIndices.length) {
                                            if (a.yAxisCurrent <= 0) {
                                                a.yAxisCurrent = 0
                                            }
                                            if (a.zAxisCurrent <= 0) {
                                                a.zAxisCurrent = 0
                                            }
                                            if (a.yAxisCurrent <= 0 && a.zAxisCurrent <= 0) {
                                                d = 39
                                            }
                                        } else {
                                            if (a.xAxisCurrent <= 0) {
                                                a.xAxisCurrent = 0
                                            }
                                            if (a.zAxisCurrent <= 0) {
                                                a.zAxisCurrent = 0
                                            }
                                            if (a.xAxisCurrent <= 0 && a.zAxisCurrent <= 0) {
                                                d = 39
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case "Scatter3D":
                        if (a.xRotate <= 0 && (d == 39 || d < 37)) {
                            a.xRotate = 0;
                            a.yRotate = 0;
                            d = 40
                        } else {
                            if (a.xRotate >= 90 && (d == 37 || d < 37)) {
                                a.xRotate = 90;
                                a.yRotate = 90;
                                d = 38
                            } else {
                                if (a.yRotate <= 0 && (d == 38 || d < 37)) {
                                    a.yRotate = 0;
                                    a.xRotate = 90;
                                    d = 39
                                } else {
                                    if (a.yRotate >= 90 && (d == 40 || d < 37)) {
                                        a.yRotate = 90;
                                        a.xRotate = 0;
                                        d = 37
                                    }
                                }
                            }
                        }
                        break
                    }
                };
                var e = setInterval(this.update, a.animationTime)
            };
        b.call()
    };
    this.setGraphType = function (b) {
        var a;
        if (b == 66) {
            a = "Bar"
        } else {
            if (b == 76) {
                a = "Line"
            } else {
                if (b == 65) {
                    a = "Area"
                } else {
                    if (b == 67) {
                        a = "BarLine"
                    } else {
                        if (b == 73) {
                            a = "Boxplot"
                        } else {
                            if (b == 68) {
                                a = "Dotplot"
                            } else {
                                if (b == 72) {
                                    a = "Heatmap"
                                } else {
                                    if (b == 83) {
                                        a = "Stacked"
                                    } else {
                                        if (b == 84) {
                                            a = "StackedPercent"
                                        } else {
                                            if (b == 50) {
                                                a = "Scatter2D"
                                            } else {
                                                if (b == 52) {
                                                    a = "ScatterBubble2D"
                                                } else {
                                                    if (b == 51) {
                                                        a = "Scatter3D"
                                                    } else {
                                                        if (b == 88) {
                                                            a = "Correlation"
                                                        } else {
                                                            if (b == 86) {
                                                                a = "Venn"
                                                            } else {
                                                                if (b == 80) {
                                                                    a = "Pie"
                                                                } else {
                                                                    if (b == 78) {
                                                                        a = "Network"
                                                                    } else {
                                                                        if (b == 71) {
                                                                            a = "Genome"
                                                                        } else {
                                                                            if (b == 77) {
                                                                                a = "Candlestick"
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (a && this.isValidGraphType(a)) {
            this.graphType = a;
            this.draw()
        } else {
            alert("Dude! You cannot plot this data in a " + a + " graph")
        }
    };
    this.redraw = function () {
        this.setInitialConfig();
        switch (this.graphType) {
        case "Area":
        case "Stacked":
        case "StackedPercent":
        case "Correlation":
        case "Venn":
        case "Pie":
            this.setAllVariablesVisible();
            this.setAllSamplesVisible();
            return true;
        case "Bar":
        case "Line":
        case "BarLine":
        case "Boxplot":
        case "Dotplot":
        case "Genome":
        case "Candlestick":
            this.setAllVariablesVisible();
            this.setAllSamplesVisible();
            this.setMin = null;
            this.setMax = null;
            this.setMin2 = null;
            this.setMax2 = null;
            break;
        case "Scatter2D":
        case "ScatterBubble2D":
            this.resetSelectedDataPoints();
            this.setAllVariablesVisible();
            this.setAllSamplesVisible();
            this.setMinX = null;
            this.setMaxX = null;
            this.setMinY = null;
            this.setMaxY = null;
            break;
        case "Scatter3D":
            this.resetSelectedDataPoints();
            this.setAllVariablesVisible();
            this.setAllSamplesVisible();
            this.xRotate = 45;
            this.yRotate = 0;
            break;
        case "Heatmap":
            this.setAllVariablesVisible();
            this.setAllSamplesVisible();
            this.varIndicesStart = -1;
            this.smpIndicesStart = -1;
            break;
        case "Network":
            this.resetSelectedNodes();
            this.setAllNodesVisible();
            this.network2DRotate = 0;
            if (this.randomNetwork) {
                this.layoutDone = false
            }
            this.ctx.translate(-this.offsetX, -this.offsetY);
            this.ctx.scale(1 / this.scaleFactor, 1 / this.scaleFactor);
            break
        }
        this.draw();
        return true
    };
    this.resetKey = function (a) {
        if (this.eventKeys) {
            this.ctrlOn = false;
            this.altOn = false;
            this.shiftOn = false;
            this.winOn = false
        }
    };
    this.resetZoomPan = function () {
        this.zoom = 1;
        this.panningX = 0;
        this.panningY = 0
    };
    this.masterReset = function (a) {
        this.resetDragDiv(a);
        this.resetSelectedDataPoints();
        this.resetSelectedNodes();
        this.resetDataTable();
        this.resetConfigurator();
        this.resetAxesResizer(a);
        this.resetKey(a);
        this.resetFlags(a);
        this.resetZoomPan();
        this.redraw()
    }
};
CanvasXpress.prototype.initDraggingEvents = function () {
    this.addDragDiv = function () {
        if (document.getElementById(this.target + "canvasXpressDrag")) {
            return
        }
        var a = document.createElement("div");
        a.id = this.target + "canvasXpressDrag";
        a.style.width = "0.5px";
        a.style.height = "0.5px";
        a.style.opacity = 0.5;
        a.style.filter = "alpha(opacity = 50)";
        a.style.backgroundColor = this.dragAreaColor;
        a.style.position = "absolute";
        a.style.zIndex = 9000;
        this.canvas.parentNode.appendChild(a)
    };
    this.registerDragDivNetwork = function (h) {
        var g, d, i;
        var a = h.target || h.srcElement;
        var b = a.id;
        if (b.match(/-legend-/)) {
            if (b.match(/-legend-Nodes/)) {
                this.moveLegend = "Nodes"
            } else {
                if (b.match(/-legend-Edges/)) {
                    this.moveLegend = "Edges"
                } else {
                    this.moveLegend = b.split(/-legend-/)[1]
                }
            }
        } else {
            if (b.match(/-lab$/)) {
                if (this.shiftOn) {
                    g = parseInt(b.split(/-lab/)[0].replace(this.target, ""));
                    d = true
                } else {
                    g = this.isEvent(h);
                    g = parseInt(g.split(/-/)[0].replace(this.target, ""))
                }
            } else {
                if (b.match(/-dec$/)) {
                    if (this.shiftOn) {
                        g = parseInt(b.split(/-dec/)[0].replace(this.target, ""));
                        i = true
                    } else {
                        this.resetDragDiv(h);
                        this.resetFlags(h)
                    }
                } else {
                    g = this.isEvent(h);
                    if (g) {
                        g = parseInt(g.split(/-/)[0].replace(this.target, ""))
                    } else {
                        g = parseInt(b.replace(this.target, ""))
                    }
                }
            }
            if (!isNaN(g) && g < this.data.nodes.length) {
                a.style.cursor = "move";
                this.moveNode = true;
                this.moveNodeIndex = g;
                if (d) {
                    this.moveNodeLab = true
                } else {
                    if (i) {
                        this.moveNodeDec = true
                    }
                }
                var f = this.data.nodes[this.moveNodeIndex];
                this.moveNodeW = f.width ? f.width : f.size ? (this.nodeSize * f.size) : this.nodeSize;
                this.moveNodeH = f.height ? f.height : f.size ? (this.nodeSize * f.size) : this.nodeSize;
                if (this.moveNodeLab) {
                    this.moveNodeX = f.labelX != null ? f.labelX - ((this.xMouseDown / this.scaleFactor) - this.offsetX) : 0;
                    this.moveNodeY = f.labelY != null ? f.labelY - ((this.yMouseDown / this.scaleFactor) - this.offsetY) : 0
                } else {
                    if (this.moveNodeDec) {
                        this.moveNodeX = f.decorationsX ? f.decorationsX - ((this.xMouseDown / this.scaleFactor) - this.offsetX) : 0;
                        this.moveNodeY = f.decorationsY ? f.decorationsY - ((this.yMouseDown / this.scaleFactor) - this.offsetY) : 0
                    } else {
                        if (this.is3DNetwork) {
                            this.moveNodeX = f.x3d - ((this.xMouseDown / this.scaleFactor) - this.offsetX);
                            this.moveNodeY = f.y3d - ((this.yMouseDown / this.scaleFactor) - this.offsetY)
                        } else {
                            this.moveNodeX = f.x - ((this.xMouseDown / this.scaleFactor) - this.offsetX);
                            this.moveNodeY = f.y - ((this.yMouseDown / this.scaleFactor) - this.offsetY)
                        }
                    }
                }
            }
        }
    };
    this.registerDragDiv = function (k) {
        if (!k) {
            k = window.event
        }
        if (this.resizeCanvasCursorShow) {
            this.resizingCanvasOn = true;
            return
        } else {
            if (!k) {
                return
            }
            var p = this.adjustedCoordinates(k);
            if (p) {
                var a = p.x;
                var q = p.y;
                var o = 0;
                for (var f = 0; f < this.layoutRows; f++) {
                    for (var b = 0; b < this.layoutCols; b++) {
                        var h = this.layoutComb ? this.layoutParams[o].graphType : this.graphType;
                        if (h.match(/Correlation|Pie|Venn|Stacked|Area/)) {
                            return false
                        }
                        var n = this.getBoundsXY(o, f, b);
                        if (a >= n[0] && a <= n[1] && q >= n[2] && q <= n[3]) {
                            this.draggingOn = true;
                            this.draggingShiftOn = this.shiftOn;
                            this.draggingAltOn = this.altOn;
                            this.draggingCtrlOn = this.ctrlOn;
                            this.xMouseDown = a;
                            this.yMouseDown = q;
                            var m = document.getElementById(this.target + "canvasXpressDrag");
                            if (h == "Network") {
                                this.registerDragDivNetwork(k)
                            }
                            if ((!h.match(/Scatter|Heatmap|Network|Genome/) && this.graphOrientation == "vertical" && !this.draggingShiftOn) || (!h.match(/Scatter|Heatmap|Network|Genome/) && this.graphOrientation != "vertical" && this.draggingShiftOn)) {
                                m.style.left = n[0] + "px"
                            } else {
                                m.style.left = this.xMouseDown + "px"
                            }
                            if ((!h.match(/Scatter|Heatmap|Network|Genome/) && this.graphOrientation != "vertical" && !this.draggingShiftOn) || (!h.match(/Scatter|Heatmap|Network|Genome/) && this.graphOrientation == "vertical" && this.draggingShiftOn) || h == "Genome") {
                                m.style.top = n[2] + "px"
                            } else {
                                m.style.top = this.yMouseDown + "px"
                            }
                            if (this.layoutValid) {
                                this.layoutValidR = f;
                                this.layoutValidC = b;
                                this.layoutValidN = o
                            }
                            return false
                        }
                        o++
                    }
                }
            }
        }
    };
    this.updateDragDivScatter3D = function (d, u, g, e, m) {
        if (!this.rotatingOn) {
            this.rotatingOn = new Date().getTime();
            var k = this;
            var b = this.xMouseDown;
            var o = this.yMouseDown;
            var a = d;
            var n = u;
            var f = this.lineLength(b, o, a, n);
            var r = this.layoutComb ? this.layoutParams[m].x : this.layoutValid ? this.x * this.scaleX : this.x;
            var h = this.layoutComb ? this.layoutParams[m].y : this.layoutValid ? this.y * this.scaleY : this.y;
            var v = ((a - b) * this.rotationSensitivity) / r;
            var s = ((n - o) * this.rotationSensitivity) / h;
            var q = (f * this.rotationSensitivity) / r;
            this.xRotate -= v;
            this.yRotate += s;
            this.zRotate -= q;
            this.draw();
            this.rotatingOn = true;
            var p = function () {
                    this.update = function () {
                        var j = new Date().getTime();
                        if (j - k.rotatingOn > k.rotationDelay) {
                            k.rotatingOn = false;
                            clearInterval(i)
                        }
                    };
                    var i = setInterval(this.update, 20)
                };
            p.call()
        }
    };
    this.updateDragDivNetwork = function (o, g, A, m, k, p) {
        this.movingOn = true;
        var s = g;
        var q = A;
        var f, B, z, v;
        var b = o.target || o.srcElement;
        if (this.moveNode || this.moveLegend) {
            if (this.moveLegend) {
                if (this.moveLegend == "Nodes") {
                    B = this.data.legend.pos.nodes.x - ((s / this.scaleFactor) - this.offsetX);
                    z = this.data.legend.pos.nodes.y - ((q / this.scaleFactor) - this.offsetY);
                    this.data.legend.pos.nodes.x -= B;
                    this.data.legend.pos.nodes.y -= z
                } else {
                    if (this.moveLegend == "Edges") {
                        B = this.data.legend.pos.edges.x - ((s / this.scaleFactor) - this.offsetX);
                        z = this.data.legend.pos.edges.y - ((q / this.scaleFactor) - this.offsetY);
                        this.data.legend.pos.edges.x -= B;
                        this.data.legend.pos.edges.y -= z
                    } else {
                        v = parseInt(this.moveLegend.replace("Text", ""));
                        B = this.data.legend.text[v].x - ((s / this.scaleFactor) - this.offsetX);
                        z = this.data.legend.text[v].y - ((q / this.scaleFactor) - this.offsetY);
                        this.data.legend.text[v].x -= B;
                        this.data.legend.text[v].y -= z
                    }
                }
            } else {
                if (this.data.nodes[this.moveNodeIndex]) {
                    f = this.data.nodes[this.moveNodeIndex];
                    if (this.is3DNetwork) {
                        B = (f.x3d - ((s / this.scaleFactor) - this.offsetX)) - this.moveNodeX;
                        z = (f.y3d - ((q / this.scaleFactor) - this.offsetY)) - this.moveNodeY
                    } else {
                        B = (f.x - ((s / this.scaleFactor) - this.offsetX)) - this.moveNodeX;
                        z = (f.y - ((q / this.scaleFactor) - this.offsetY)) - this.moveNodeY
                    }
                    var a = this.randomNetwork;
                    if (this.moveNodeLab) {
                        B = f.labelX != null ? (f.labelX - ((s / this.scaleFactor) - this.offsetX)) - this.moveNodeX : B;
                        z = f.labelY != null ? (f.labelY - ((q / this.scaleFactor) - this.offsetY)) - this.moveNodeY : z;
                        this.modifyXYNodeLab(f.id, B, z)
                    } else {
                        if (this.moveNodeDec) {
                            B = f.decorationsX ? (f.decorationsX - ((s / this.scaleFactor) - this.offsetX)) - this.moveNodeX : B;
                            z = f.decorationsY ? (f.decorationsY - ((q / this.scaleFactor) - this.offsetY)) - this.moveNodeY : z;
                            this.modifyXYNodeDec(f.id, B, z)
                        } else {
                            if (this.draggingShiftOn) {
                                var u = (this.moveNodeW - B) + ((g - this.xMouseDown) / this.scaleFactor);
                                var n = (this.moveNodeH - z) + ((A - this.yMouseDown) / this.scaleFactor);
                                this.modifyNodeSize(f.id, Math.abs(u), Math.abs(n));
                                for (var v in this.selectNode) {
                                    if (v != f.id) {
                                        this.modifyNodeSize(v, u, n)
                                    }
                                }
                            } else {
                                this.modifyXYNode(f.id, B, z);
                                for (var v in this.selectNode) {
                                    if (v != f.id) {
                                        this.modifyXYNode(v, B, z)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            this.panningX = ((g - this.xMouseDown) / this.scaleFactor);
            this.panningY = ((A - this.yMouseDown) / this.scaleFactor)
        }
        this.calculateLayout = false;
        this.randomNetwork = false;
        this.draw();
        if (this.is3DNetwork && f) {
            this.moveNodeIndex = this.data.nodeIndices[f.id]
        }
        this.randomNetwork = a;
        var d = document.getElementById(b.id);
        if (d) {
            d.style.cursor = this.draggingShiftOn ? "se-resize" : "move"
        }
    };
    this.updateDragDiv = function (k) {
        if (this.draggingOn) {
            if (!k) {
                k = window.event
            }
            if (this.resetOn) {
                this.resetOn = false;
                this.stopEvent(k);
                return
            }
            var p = this.adjustedCoordinates(k);
            if (p) {
                var a = p.x;
                var q = p.y;
                var f = this.layoutValidR;
                var b = this.layoutValidC;
                var o = this.layoutValidN;
                var h = this.layoutComb ? this.layoutParams[o].graphType : this.graphType;
                if (h.match(/Correlation|Pie|Venn|Stacked|Area/)) {
                    return false
                }
                if (h == "Scatter3D" && !this.shiftOn) {
                    this.updateDragDivScatter3D(a, q, f, b, o)
                } else {
                    if (h == "Network" && ((this.moveNode || this.moveLegend) || (!this.ctrlOn && !this.shiftOn && !this.altOn))) {
                        this.updateDragDivNetwork(k, a, q, f, b, o)
                    } else {
                        var n = this.getBoundsXY(o, f, b);
                        var m = document.getElementById(this.target + "canvasXpressDrag");
                        if ((!h.match(/Scatter|Heatmap|Network|Genome/) && this.graphOrientation == "vertical" && !this.draggingShiftOn) || (!h.match(/Scatter|Heatmap|Network|Genome/) && this.graphOrientation != "vertical" && this.draggingShiftOn)) {
                            m.style.width = (n[1] - n[0]) + "px"
                        } else {
                            if (a > this.xMouseDown) {
                                if (a > n[1]) {
                                    m.style.width = (n[1] - this.xMouseDown) + "px"
                                } else {
                                    m.style.width = (a - this.xMouseDown) + "px"
                                }
                            } else {
                                if (a < n[0]) {
                                    m.style.left = n[0] + "px";
                                    m.style.width = (this.xMouseDown - n[0]) + "px"
                                } else {
                                    m.style.left = a + "px";
                                    m.style.width = (this.xMouseDown - a) + "px"
                                }
                            }
                        }
                        if ((!h.match(/Scatter|Heatmap|Network|Genome/) && this.graphOrientation != "vertical" && !this.draggingShiftOn) || (!h.match(/Scatter|Heatmap|Network|Genome/) && this.graphOrientation == "vertical" && this.draggingShiftOn) || h == "Genome") {
                            m.style.height = (n[3] - n[2]) + "px"
                        } else {
                            if (q > this.yMouseDown) {
                                if (q > n[3]) {
                                    m.style.height = (n[3] - this.yMouseDown) + "px"
                                } else {
                                    m.style.height = (q - this.yMouseDown) + "px"
                                }
                            } else {
                                if (q < n[2]) {
                                    m.style.top = n[2] + "px";
                                    m.style.height = (this.yMouseDown - n[2]) + "px"
                                } else {
                                    m.style.top = q + "px";
                                    m.style.height = (this.yMouseDown - q) + "px"
                                }
                            }
                        }
                    }
                }
            }
            this.movingOn = true;
            this.resetOn = false
        } else {
            if (this.resizeCanvasCursorShow && this.resizingCanvasOn) {
                this.updateCanvasResizer(k)
            } else {
                this.registerCanvasResizer(k)
            }
        }
    };
    this.endDragDiv = function (j) {
        if (this.draggingOn) {
            var q = this.layoutComb ? this.layoutParams[this.layoutValidN].graphType : this.graphType;
            if (q.match(/Correlation|Pie|Venn|Stacked|Area/)) {
                return false
            }
            if (!j) {
                j = window.event
            }
            if (q == "Scatter3D" && !this.shiftOn) {
                this.resetDragDiv(j)
            } else {
                if (q == "Network" && (this.moveNode || this.moveLegend)) {
                    var m = document.getElementById(this.target + "canvasXpressImage");
                    if (m) {
                        m.style.cursor = "default";
                        var b = j.target || j.srcElement;
                        b.style.cursor = "default";
                        if (this.isSelectNodes && this.movingOn) {
                            this.resetSelectedNodes();
                            this.draw()
                        }
                        if (this.userEvents.enddragnode && this.movingOn) {
                            var i;
                            if (this.moveLegend) {
                                i = {
                                    legend: this.moveLegend
                                }
                            } else {
                                var p = this.moveNodeIndex;
                                var a = this.findChildrenNodeIndices(this.data.nodes[p].id);
                                if (a) {
                                    a.push(p)
                                } else {
                                    a = [p]
                                }
                                i = this.extractDataObject(a)
                            }
                            this.resetDragDiv(j);
                            this.userEvents.enddragnode.call(this, i)
                        } else {
                            this.resetDragDiv(j)
                        }
                    }
                } else {
                    if (this.movingOn) {
                        var s = this.adjustedCoordinates(j);
                        if (s) {
                            var h = s.x;
                            var r = s.y;
                            if (h != this.xMouseDown || r != this.yMouseDown) {
                                var k = document.getElementById(this.target + "canvasXpressDrag");
                                var g = parseInt(k.style.left);
                                var o = parseInt(k.style.top);
                                var f = g + parseInt(k.style.width);
                                var n = o + parseInt(k.style.height);
                                this.handleDragEvent(g, o, f, n);
                                this.resetDragDiv(j)
                            }
                        }
                    } else {
                        this.resetDragDiv(j)
                    }
                }
            }
        } else {
            this.resetDragDiv(j)
        }
    };
    this.validateMinMax = function (b, a) {
        if (!isNaN(b) && !isNaN(a)) {
            if (b > a) {
                return false
            }
            return true
        } else {
            return false
        }
    };
    this.isObjectInsideCoordiantes = function (e, b, f, a, d) {
        if (b < e[0] || b < e[2]) {
            if (a > e[0]) {
                if (f < e[1] || f < e[3]) {
                    if (d > e[1]) {
                        return true
                    }
                }
            }
        }
        return false
    };
    this.handleDragEventNetwork = function (f, u, d, t) {
        var g = {};
        var l = [];
        var v = [];
        var b = this.randomNetwork;
        if (this.modelEvent == "local") {
            var j = document.getElementById(this.target + "canvasXpressMap");
            if (j) {
                var q = j.childNodes;
                for (var k = 0; k < q.length; k++) {
                    var p = q[k].coords.split(",");
                    if (this.isObjectInsideCoordiantes(p, f, u, d, t)) {
                        var e = q[k].id.replace(this.target, "");
                        if (e < this.data.nodes.length) {
                            g[e] = 1
                        }
                    }
                }
            }
        } else {
            for (var k = 1; k < this.dataEvent.length; k++) {
                var e = this.dataEvent[k][0];
                var p = this.dataEvent[k][2];
                if (this.isObjectInsideCoordiantes(p, f, u, d, t)) {
                    if (e < this.data.nodes.length) {
                        g[e] = 1
                    }
                }
            }
        }
        for (var k = 0; k < this.data.nodes.length; k++) {
            if (!g[k]) {
                l.push(this.data.nodes[k].id)
            } else {
                v.push(this.data.nodes[k].id)
            }
        }
        if (l.length < this.data.nodes.length) {
            if (this.draggingShiftOn) {
                this.setSelectNodes(v);
                this.isSelectNodes = v.length;
                this.draggingShiftOn = false;
                this.draw()
            } else {
                this.resetZoomPan();
                this.hideUnhideNodes(l, true);
                this.ctx.translate(-this.offsetX, -this.offsetY);
                this.ctx.scale(1 / this.scaleFactor, 1 / this.scaleFactor);
                this.calculateLayout = false;
                this.randomNetwork = false;
                this.draw();
                this.randomNetwork = b
            }
        }
    };
    this.setOriginalIndices = function (e, d, a) {
        if (!this.startingVarIndices) {
            this.startingVarIndices = []
        }
        if (!this.startingSmpIndices) {
            this.startingSmpIndices = []
        }
        if (!this.startingVarIndices[e]) {
            this.startingVarIndices[e] = [];
            for (var b = 0; b < d.length; b++) {
                this.startingVarIndices[e][b] = d[b]
            }
        }
        if (!this.startingSmpIndices[e]) {
            this.startingSmpIndices[e] = [];
            for (var b = 0; b < a.length; b++) {
                this.startingSmpIndices[e][b] = a[b]
            }
        }
    };
    this.handleDragEventHeatmap = function (j, d, n, b, l) {
        var o = [];
        var r = [];
        var q;
        var p = this.layoutComb ? this.layoutParams[j].varIndices : this.varIndices;
        if (this.isGroupedData) {
            q = this.layoutComb ? this.layoutParams[j].grpIndices : this.grpIndices
        } else {
            q = this.layoutComb ? this.layoutParams[j].smpIndices : this.smpIndices
        }
        this.setOriginalIndices(j, p, q);
        if (this.layoutComb) {
            this.varIndicesStart = this.layoutParams[j].varIndicesStart < 0 ? 0 : this.layoutParams[j].varIndicesStart;
            if (!this.isGroupedData) {
                this.smpIndicesStart = this.layoutParams[j].smpIndicesStart < 0 ? 0 : this.layoutParams[j].smpIndicesStart
            }
        } else {
            this.varIndicesStart = this.varIndicesStart < 0 ? 0 : this.varIndicesStart;
            if (!this.isGroupedData) {
                this.smpIndicesStart = this.smpIndicesStart < 0 ? 0 : this.smpIndicesStart
            }
        }
        this.varIndices = [];
        if (this.isGroupedData) {
            this.grpIndices = []
        } else {
            this.smpIndices = []
        }
        if (this.modelEvent == "local") {
            var e = document.getElementById(this.target + "canvasXpressMap");
            if (e) {
                var k = e.childNodes;
                for (var g = 0; g < k.length; g++) {
                    var h = k[g].coords.split(",");
                    if ((d < h[0] || d < h[2]) && b > h[0] && (n < h[1] || n < h[3]) && l > h[1]) {
                        var f = k[g].id.replace(this.target, "").split(",");
                        if (!o[f[0]]) {
                            this.varIndices.push(f[0]);
                            o[f[0]] = 1
                        }
                        if (!r[f[1]]) {
                            if (this.isGroupedData) {
                                this.grpIndices.push(f[1])
                            } else {
                                this.smpIndices.push(f[1])
                            }
                            r[f[1]] = 1
                        }
                    }
                }
            }
        } else {
            for (var g = 1; g < this.dataEvent.length; g++) {
                var f = this.layoutComb ? this.layoutParams[j].dataEvent[g][0] : this.dataEvent[g][0];
                var h = this.layoutComb ? this.layoutParams[j].dataEvent[g][2] : this.dataEvent[g][2];
                if ((d < h[0] || d < h[2]) && b > h[0] && (n < h[1] || n < h[3]) && l > h[1]) {
                    if (!o[f[0]]) {
                        this.varIndices.push(f[0]);
                        o[f[0]] = 1
                    }
                    if (!r[f[1]]) {
                        if (this.isGroupedData) {
                            this.grpIndices.push(f[1])
                        } else {
                            this.smpIndices.push(f[1])
                        }
                        r[f[1]] = 1
                    }
                }
            }
        }
        for (var g = 0; g < p.length; g++) {
            if (p[g] == this.varIndices[0]) {
                this.varIndicesStart += g
            }
        }
        for (var g = 0; g < q.length; g++) {
            if (q[g] == this.smpIndices[0]) {
                if (!this.isGroupedData) {
                    this.smpIndicesStart += g
                }
            }
        }
        this.draw()
    };
    this.handleDragEventScatter = function (u, l, e, r, b, q) {
        var t, k, j, p;
        this.selectDataPoint = [];
        var v = 0;
        if (this.shiftOn) {
            if (this.modelEvent == "local") {
                var f = document.getElementById(this.target + "canvasXpressMap");
                if (f) {
                    var n = f.childNodes;
                    for (var g = 0; g < n.length; g++) {
                        var h = n[g].coords.split(",");
                        if (this.isObjectInsideCoordiantes(h, e, r, b, q)) {
                            var d = n[g].id.replace(this.target, "");
                            this.addRemoveToSelectedDataPoints(u, d.split(","));
                            v++
                        }
                    }
                }
            } else {
                for (var g = 1; g < this.dataEvent.length; g++) {
                    var d = this.dataEvent[g][0];
                    var h = this.dataEvent[g][2];
                    if (this.isObjectInsideCoordiantes(h, e, r, b, q)) {
                        this.addRemoveToSelectedDataPoints(u, d.split(","));
                        v++
                    }
                }
            }
            if (v > 0) {
                this.isSelectDataPoints = v;
                this.draw()
            }
        } else {
            if (this.layoutValid) {
                if (this.layoutComb) {
                    t = this.layoutParams[l].xAxisMin + ((e - (this.layoutParams[l].offsetX + this.layoutParams[l].left)) / this.layoutParams[l].xAxisUnit);
                    k = this.layoutParams[l].xAxisMin + ((b - (this.layoutParams[l].offsetX + this.layoutParams[l].left)) / this.layoutParams[l].xAxisUnit);
                    j = this.layoutParams[l].yAxisMax - ((r - (this.layoutParams[l].offsetY + this.layoutParams[l].top)) / this.layoutParams[l].yAxisUnit);
                    p = this.layoutParams[l].yAxisMax - ((q - (this.layoutParams[l].offsetY + this.layoutParams[l].top)) / this.layoutParams[l].yAxisUnit)
                } else {
                    t = this.xAxisMin + ((e - (this.left * this.scaleX)) / (this.xAxisUnit * this.scaleX));
                    k = this.xAxisMin + ((b - (this.left * this.scaleX)) / (this.xAxisUnit * this.scaleX));
                    j = this.yAxisMax - ((r - (this.top * this.scaleY)) / (this.yAxisUnit * this.scaleY));
                    p = this.yAxisMax - ((q - (this.top * this.scaleY)) / (this.yAxisUnit * this.scaleY))
                }
            } else {
                t = this.xAxisMin + ((e - this.left) / this.xAxisUnit);
                k = this.xAxisMin + ((b - this.left) / this.xAxisUnit);
                j = this.yAxisMax - ((r - this.top) / this.yAxisUnit);
                p = this.yAxisMax - ((q - this.top) / this.yAxisUnit)
            }
            if (this.validateMinMax(t, k) && this.validateMinMax(p, j)) {
                this.setMinX = t;
                this.setMaxX = k;
                this.setMaxY = j;
                this.setMinY = p;
                this.draw()
            }
        }
    };
    this.handleDragEventGenome = function (h, d, f, b, e) {
        var a, g;
        if (this.layoutValid) {
            if (this.layoutComb) {
                a = this.layoutParams[h].xAxisMin + ((d - this.layoutParams[h].left) / this.layoutParams[h].xAxisUnit);
                g = this.layoutParams[h].xAxisMin + ((b - this.layoutParams[h].left) / this.layoutParams[h].xAxisUnit)
            } else {
                a = this.xAxisMin + ((d - (this.left * this.scaleX)) / (this.xAxisUnit * this.scaleX));
                g = this.xAxisMin + ((b - (this.left * this.scaleX)) / (this.xAxisUnit * this.scaleX))
            }
        } else {
            a = this.xAxisMin + ((d - this.left) / this.xAxisUnit);
            g = this.xAxisMin + ((b - this.left) / this.xAxisUnit)
        }
        if (this.validateMinMax(a, g)) {
            this.setMin = a;
            this.setMax = g;
            this.draw()
        }
    };
    this.handleDragEventOneDimension = function (d, y, r, f, q, e) {
        if (this.draggingShiftOn) {
            var k = [];
            var l = document.getElementById(this.target + "canvasXpressMap");
            var A = [];
            if (l) {
                var B = l.childNodes;
                if (this.graphOrientation == "vertical") {
                    var g, h;
                    for (var o = 0; o < B.length; o++) {
                        g = B[o].coords.split(",");
                        if (this.isObjectInsideCoordiantes(g, r, f, q, e)) {
                            h = B[o].id.replace(this.target, "").split(",");
                            if (!k[h[1]]) {
                                A.push(h[1]);
                                k[h[1]] = 1
                            }
                        }
                    }
                } else {
                    for (var o = 0; o < B.length; o++) {
                        g = B[o].coords.split(",");
                        if (this.isObjectInsideCoordiantes(g, r, f, q, e)) {
                            h = B[o].id.replace(this.target, "").split(",");
                            if (!k[h[1]]) {
                                A.push(h[1]);
                                k[h[1]] = 1
                            }
                        }
                    }
                }
            }
            this.setSamplesVisible(A, true);
            this.draggingShiftOn = false;
            this.draw()
        } else {
            var n, p, D, C;
            var x, v, u, t;
            var j, b, z, w;
            if (d == "Candlestick" && y > 0) {
                D = this.xAxis2Min;
                C = this.xAxis2Max
            } else {
                n = this.xAxisMin;
                p = this.xAxisMax
            }
            if (this.layoutValid) {
                if (this.layoutComb) {
                    if (this.layoutCurrent != y) {
                        if (d == "Candlestick" && y > 0) {
                            D = this.layoutParams[y].xAxis2Min;
                            C = this.layoutParams[y].xAxis2Max
                        } else {
                            n = this.layoutParams[y].xAxisMin;
                            p = this.layoutParams[y].xAxisMax
                        }
                    }
                    x = this.layoutParams[y].offsetX + this.layoutParams[y].left;
                    v = x + this.layoutParams[y].x;
                    u = this.layoutParams[y].offsetY + this.layoutParams[y].top;
                    t = u + this.layoutParams[y].y;
                    j = this.layoutParams[y].xAxisUnit;
                    b = this.layoutParams[y].xAxis2Unit;
                    z = 1;
                    w = 1
                } else {
                    x = ((this.width / this.layoutCols) * this.layoutValidC) + (this.left * this.scaleX);
                    v = ((this.width / this.layoutCols) * this.layoutValidC) + ((this.left + this.x) * this.scaleX);
                    u = ((this.height / this.layoutRows) * this.layoutValidR) + (this.top * this.scaleY);
                    t = ((this.height / this.layoutRows) * this.layoutValidR) + ((this.top + this.y) * this.scaleY);
                    j = this.xAxisUnit;
                    b = this.xAxis2Unit;
                    z = this.scaleX;
                    w = this.scaleY
                }
            } else {
                x = this.left;
                v = this.left + this.x;
                u = this.top;
                t = this.top + this.y;
                j = this.xAxisUnit;
                b = this.xAxis2Unit;
                z = 1;
                w = 1
            }
            if (this.graphOrientation == "vertical") {
                if (d == "Candlestick" && y > 0) {
                    D = D + ((t - e) / (b * w));
                    C = C - ((f - u) / (b * w))
                } else {
                    n = n + ((t - e) / (j * w));
                    p = p - ((f - u) / (j * w))
                }
            } else {
                if (d == "Candlestick" && y > 0) {
                    D = D + ((r - x) / (b * z));
                    C = C - ((v - q) / (b * z))
                } else {
                    n = n + ((r - x) / (j * z));
                    p = p - ((v - q) / (j * z))
                }
            }
            if (d == "Candlestick" && y > 0 && this.validateMinMax(D, C)) {
                this.setMin2 = D;
                this.setMax2 = C;
                this.draw()
            } else {
                if (this.validateMinMax(n, p)) {
                    this.setMin = n;
                    this.setMax = p;
                    this.draw()
                }
            }
        }
    };
    this.handleDragEvent = function (b, e, a, d) {
        var g = this.layoutComb ? this.layoutValidN : 0;
        var f = this.layoutComb ? this.layoutParams[g].graphType : this.graphType;
        if (f == "Network") {
            this.handleDragEventNetwork(b, e, a, d)
        } else {
            if (f == "Heatmap") {
                this.handleDragEventHeatmap(g, b, e, a, d)
            } else {
                if (f.match(/Scatter/)) {
                    this.handleDragEventScatter(f, g, b, e, a, d)
                } else {
                    if (f == "Genome") {
                        this.handleDragEventGenome(g, b, e, a, d)
                    } else {
                        this.handleDragEventOneDimension(f, g, b, e, a, d)
                    }
                }
            }
        }
    };
    this.resetDragDiv = function (a) {
        var b = document.getElementById(this.target + "canvasXpressDrag");
        if (b) {
            b.style.left = "0px";
            b.style.top = "0px";
            b.style.width = "0px";
            b.style.height = "0px"
        }
    };
    this.initializeDraggingEvents = function () {
        if (!this.disableEvents) {
            this.addDragDiv()
        }
    };
    this.initializeDraggingEvents()
};
CanvasXpress.prototype.initInfoEvents = function () {
    this.addInfoSpan = function () {
        if (document.getElementById(this.target + "canvasXpressInfo")) {
            this.resetInfoSpan();
            return
        }
        var a = document.createElement("span");
        a.id = this.target + "canvasXpressInfo";
        a.style.backgroundColor = this.infoAreaColor;
        a.style.margin = "1px 3px";
        a.style.padding = "1px 3px";
        a.style.fontSize = "x-small";
        a.style.border = "1px solid";
        a.style.borderColor = this.background;
        a.style.position = "absolute";
        a.style.display = "none";
        a.style.zIndex = 19010;
        this.canvas.parentNode.appendChild(a);
        this.addInfoSpanShadow()
    };
    this.addInfoSpanShadow = function () {
        if (document.getElementById(this.target + "canvasXpressInfoShadow")) {
            return
        }
        var a = document.createElement("div");
        a.id = this.target + "canvasXpressInfoShadow";
        a.style.backgroundColor = "#000000";
        a.style.margin = "1px";
        a.style.padding = "1px";
        a.style.fontSize = "x-small";
        a.style.border = "1px solid";
        a.style.borderColor = "#000000";
        a.style.position = "absolute";
        a.style.display = "none";
        a.style.zIndex = 19009;
        a.style.msFilter = "progid:DXImageTransform.Microsoft.Alpha(Opacity=25)";
        a.style.filter = "progid:DXImageTransform.Microsoft.Alpha(opacity=25)";
        a.style.mozOpacity = 0.25;
        a.style.opacity = 0.25;
        this.canvas.parentNode.appendChild(a)
    };
    this.resetInfoSpan = function (h, g) {
        var b = document.getElementById(this.target + "canvasXpressInfo");
        var i = document.getElementById(this.target + "canvasXpressInfoShadow");
        if (b && i && this.infoStartTime) {
            var a = new Date().getTime() - this.infoStartTime;
            if (a > 100 || g) {
                b.style.left = "0px";
                b.style.top = "0px";
                b.style.borderColor = this.background;
                b.style.display = "none";
                b.innerHTML = "";
                i.style.left = "0px";
                i.style.top = "0px";
                i.style.borderColor = "black";
                i.style.display = "none";
                i.innerHTML = ""
            }
        }
    };
    this.showInfoSpan = function (b, j, a) {
        if (this.draggingOn || this.remoteUpdating) {
            return
        }
        var m = document.getElementById(this.target + "canvasXpressInfo");
        var f = document.getElementById(this.target + "canvasXpressInfoShadow");
        if (m && f) {
            var l = this.adjustedCoordinates(b, m);
            if (l) {
                var h = l.x;
                var g = b ? l.y : l.y + 25;
                if (m) {
                    m.style.borderColor = j.match("</table>") ? this.background : this.foreground;
                    m.style.left = (h + 5) + "px";
                    m.style.top = (g - 20) + "px";
                    m.style.display = "inline";
                    m.innerHTML = j;
                    f.style.left = (h + 15) + "px";
                    f.style.top = (g - 17) + "px";
                    f.style.display = "inline";
                    f.innerHTML = j;
                    this.infoStartTime = new Date().getTime();
                    if (a) {}
                    var k = this;
                    var i = function () {
                            this.update = function () {
                                var e = new Date().getTime();
                                if (e - k.infoStartTime > k.infoTimeOut) {
                                    k.resetInfoSpan();
                                    clearInterval(d)
                                }
                            };
                            var d = setInterval(this.update, 500)
                        };
                    i.call()
                }
            }
        }
    };
    this.initializeInfoEvents = function () {
        if (!this.disableEvents) {
            this.addInfoSpan()
        }
    };
    this.initializeInfoEvents()
};
CanvasXpress.prototype.initImageMapEvents = function () {
    this.addImage = function () {
        if (document.getElementById(this.target + "canvasXpressImage")) {
            return
        }
        var d = this;
        var a = function () {
                if (navigator.onLine) {
                    return "http://canvasxpress.org/js/canvasXpress.gif"
                } else {
                    if (d.path) {
                        return d.path + "canvasXpress.gif"
                    } else {
                        alert("Dude! I couldn't find canvasXpress.js")
                    }
                }
            };
        var e = document.getElementById(this.target);
        var b = document.createElement("img");
        b.id = this.target + "canvasXpressImage";
        b.src = a();
        b.width = this.width;
        b.height = this.height;
        b.useMap = "#" + this.target + "canvasXpressMap";
        b.style.left = "0px";
        b.style.top = "0px";
        b.style.border = "0px";
        b.style.position = "absolute";
        b.style.zIndex = 9000;
        this.canvas.parentNode.appendChild(b)
    };
    this.resizeImage = function (b) {
        var a = document.getElementById("wrapper-" + this.target);
        var d = document.getElementById(this.target + "canvasXpressImage");
        if (a && d) {
            a.style.width = this.width + "px";
            a.style.height = this.height + "px";
            d.style.left = "0px";
            d.style.top = "0px";
            d.style.width = this.width + "px";
            d.style.height = this.height + "px";
            if (!b) {
                if (this.modelEvent != "local") {
                    this.addGlobalArea(["rect", this.left, this.top, this.left + this.x, this.top + this.y])
                }
            }
        }
    };
    this.mousedownImage = function (a) {
        return function (b) {
            if (!a.configuringOn) {
                a.cancelEvent(b);
                a.registerDragDiv(b)
            }
            return false
        }
    }(this);
    this.mousemoveImage = function (a) {
        return function (b) {
            if (!a.configuringOn) {
                a.cancelEvent(b);
                a.updateDragDiv(b)
            }
            return false
        }
    }(this);
    this.mouseupImage = function (a) {
        return function (b) {
            if (!a.configuringOn) {
                a.stopEvent(b);
                a.endDragDiv(b);
                a.endCanvasResizer(b);
                a.endAxesResizer(b);
                a.resetFlags(b)
            }
            return false
        }
    }(this);
    this.clickImage = function (a) {
        return function (b) {
            if (!a.configuringOn) {
                if (a.isAxis(b)) {
                    a.stopEvent(b)
                } else {
                    if (a.axesResizerShow) {
                        a.stopEvent(b);
                        a.resetAxesResizer(b)
                    } else {
                        if (a.mobileApp) {
                            a.stopEvent(b);
                            a.addConfigurator(b)
                        }
                    }
                }
            }
        }
    }(this);
    this.dblclickImage = function (a) {
        return function (b) {
            a.cancelEvent(b);
            a.addConfigurator(b);
            return false
        }
    }(this);
    this.mousemoveDoc = function (a) {
        return function (b) {
            a.cancelEvent(b);
            a.updateDragDiv(b);
            return false
        }
    }(this);
    this.keydownDoc = function (a) {
        return function (b) {
            a.registerKey(b)
        }
    }(this);
    this.keyupDoc = function (a) {
        return function (b) {
            if (a.configuringOn) {
                a.selectConfig(b)
            } else {
                a.resetKey(b)
            }
        }
    }(this);
    this.resizeWindow = function (a) {
        return function (b) {
            a.resizeImage(true);
            return false
        }
    }(this);
    this.wheelImage = function (a) {
        return function (b) {
            var f = 0;
            if (!b) {
                b = window.event
            }
            if (b.wheelDelta) {
                f = b.wheelDelta / 120;
                if (window.opera) {
                    f = -f
                }
            } else {
                if (b.detail) {
                    f = -b.detail / 3
                }
            }
            if (f) {
                a.handleWheelEvent(b, f)
            }
        }
    }(this);
    this.addCanvasListeners = function () {
        var b = this;
        var a = document.getElementById(this.target + "canvasXpressImage");
        var e = document.getElementById(this.target + "canvasXpressDrag");
        if (a && e) {
            this.addEvtListener(a, "mousedown", this.mousedownImage, false);
            this.addEvtListener(a, "mousemove", this.mousemoveImage, false);
            this.addEvtListener(a, "mouseup", this.mouseupImage, false);
            this.addEvtListener(a, "click", this.clickImage, false);
            this.addEvtListener(a, "dblclick", this.dblclickImage, false);
            this.addEvtListener(a, "mousewheel", this.wheelImage, false);
            this.addEvtListener(e, "mousemove", this.mousemoveImage, false);
            this.addEvtListener(this.canvas, "click", this.clickImage, false);
            this.addEvtListener(document, "mousemove", this.mousemoveDoc, false);
            this.addEvtListener(document, "mouseup", this.mouseupImage, false);
            this.addEvtListener(document, "keydown", this.keydownDoc, false);
            this.addEvtListener(document, "keyup", this.keyupDoc, false);
            this.addEvtListener(window, "resize", this.resizeWindow, false)
        }
    };
    this.handleWheelEvent = function (g, i) {
        if (this.graphType == "Network") {
            this.cancelEvent(g);
            if (i > 0) {
                this.zoom += (this.zoom * this.zoomStep)
            } else {
                this.zoom -= (this.zoom * this.zoomStep)
            }
            if (this.zoom < 0.1) {
                this.zoom = 0.1
            }
            var a = this.calculateLayout;
            var f = this.randomNetwork;
            this.calculateLayout = false;
            this.randomNetwork = false;
            this.draw();
            this.calculateLayout = a;
            this.randomNetwork = f
        } else {
            if (this.graphType == "Heatmap") {
                this.cancelEvent(g);
                var j = this.layoutComb ? this.layoutValidN : 0;
                var b;
                var h = this.layoutComb ? this.layoutParams[j].varIndices : this.varIndices;
                if (this.isGroupedData) {
                    b = this.layoutComb ? this.layoutParams[j].grpIndices : this.grpIndices
                } else {
                    b = this.layoutComb ? this.layoutParams[j].smpIndices : this.smpIndices
                }
                this.setOriginalIndices(j, h, b);
                if (i > 0) {
                    if (h.length > 3) {
                        this.varIndices.shift();
                        this.varIndices.pop();
                        if (this.varIndicesStart < 0) {
                            this.varIndicesStart++
                        }
                        this.varIndicesStart++
                    }
                    if (b.length > 3) {
                        this.smpIndices.shift();
                        this.smpIndices.pop();
                        if (this.smpIndicesStart < 0) {
                            this.smpIndicesStart++
                        }
                        this.smpIndicesStart++
                    }
                } else {
                    if (this.varIndicesStart > 0) {
                        this.varIndices.unshift(this.startingVarIndices[j][this.varIndicesStart - 1]);
                        this.varIndicesStart--
                    }
                    if (this.varIndicesStart + h.length < this.startingVarIndices[j].length) {
                        this.varIndices.push(this.startingVarIndices[j][this.varIndices.length])
                    }
                    if (this.smpIndicesStart > 0) {
                        this.smpIndices.unshift(this.startingSmpIndices[j][this.smpIndicesStart - 1]);
                        this.smpIndicesStart--
                    }
                    if (this.smpIndicesStart + b.length < this.startingSmpIndices[j].length) {
                        this.smpIndices.push(this.startingSmpIndices[j][this.smpIndices.length])
                    }
                }
                this.draw()
            }
        }
        return false
    };
    this.addMap = function () {
        if (document.getElementById(this.target + "canvasXpressMap")) {
            return this.resetMap()
        }
        var a = document.createElement("map");
        a.name = this.target + "canvasXpressMap";
        a.id = this.target + "canvasXpressMap";
        this.canvas.parentNode.appendChild(a);
        this.addArea(["rect", 0, 0, 3, 3], [-1])
    };
    this.setLayoutValidIndices = function (k) {
        if (this.layoutComb) {
            var h = this.adjustedCoordinates(k);
            if (h) {
                var a = h.x;
                var g = h.y;
                var m = 0;
                for (var f = 0; f < this.layoutRows; f++) {
                    for (var d = 0; d < this.layoutCols; d++) {
                        var b = this.getBoundsXY(m, f, d);
                        if (a >= b[0] && a <= b[1] && g >= b[2] && g <= b[3]) {
                            if (this.layoutValid) {
                                this.layoutValidR = f;
                                this.layoutValidC = d;
                                this.layoutValidN = m
                            }
                        }
                        m++
                    }
                }
            }
        }
    };
    this.getNetworkEventAreaId = function (f) {
        if (this.modelEvent == "local") {
            var a = f.target || f.srcElement;
            if (a) {
                var b = a.id;
                var d;
                if (b.match(/-legend-/)) {
                    if (b.match(/-legend-Nodes/)) {
                        return false
                    } else {
                        if (b.match(/-legend-Edges/)) {
                            return false
                        } else {
                            return false
                        }
                    }
                } else {
                    if (b.match(/-lab$|-dec$/)) {
                        d = this.isEvent(f);
                        if (d) {
                            return [parseInt(d.split(/-/)[0].replace(this.target, ""))]
                        } else {
                            return false
                        }
                    } else {
                        d = this.isEvent(f);
                        if (d) {
                            return [parseInt(d.split(/-/)[0].replace(this.target, ""))]
                        } else {
                            return [parseInt(b.replace(this.target, ""))]
                        }
                    }
                }
            }
        }
    };
    this.getEventAreaId = function (b) {
        if (this.graphType == "Network") {
            return this.getNetworkEventAreaId(b)
        }
        if (this.modelEvent == "local") {
            var a = b.target || b.srcElement;
            if (a) {
                this.setLayoutValidIndices(b);
                return a.id.replace(this.target, "").split(",")
            } else {
                this.setLayoutValidIndices(b);
                return this.isEvent(b)
            }
        }
    };
    this.getEventAreaData = function (a) {
        return this.extractDataObject(this.getEventAreaId(a))
    };
    this.handleMouseAreaEvents = function (b, d, a) {
        if (a[0] == -1) {
            this.showInfoSpan(d, "CanvasXpress (" + this.version + ")")
        } else {
            var f = this.extractDataObject(a);
            if (this.userEvents[b]) {
                if (typeof (this.userEvents[b]) == "object" && this.userEvents[b].handler && this.userEvents[b].scope) {
                    if (this.ctrlOn && b == "click") {
                        this.addRemoveToSelectedDataPoints(false, this.getEventAreaId(d));
                        this.draw()
                    } else {
                        if (this.shiftOn && b == "click" && this.graphType == "Network" && this.networkLayoutType == "radial") {
                            this.reRootRadialNetwork(this.getEventAreaId(d))
                        } else {
                            if (this.altOn && b == "click" && this.graphType == "Network") {
                                this.addConfigurator.apply(this, [d, this.getEventAreaId(d)])
                            } else {
                                this.userEvents[b].handler.apply(this.userEvents[b].scope, [f, d])
                            }
                        }
                    }
                } else {
                    if (typeof (this.userEvents[b]) == "object" && this.userEvents[b].handler) {
                        if (this.ctrlOn && b == "click") {
                            this.addRemoveToSelectedDataPoints(false, this.getEventAreaId(d));
                            this.draw()
                        } else {
                            if (this.shiftOn && b == "click" && this.graphType == "Network" && this.networkLayoutType == "radial") {
                                this.reRootRadialNetwork(this.getEventAreaId(d))
                            } else {
                                if (this.altOn && b == "click" && this.graphType == "Network") {
                                    this.addConfigurator.apply(this, [d, this.getEventAreaId(d)])
                                } else {
                                    this.userEvents[b].handler(f, d)
                                }
                            }
                        }
                    } else {
                        if (this.ctrlOn && b == "click") {
                            this.addRemoveToSelectedDataPoints(false, this.getEventAreaId(d));
                            this.draw()
                        } else {
                            if (this.shiftOn && b == "click" && this.graphType == "Network" && this.networkLayoutType == "radial") {
                                this.reRootRadialNetwork(this.getEventAreaId(d))
                            } else {
                                if (this.altOn && b == "click" && this.graphType == "Network") {
                                    this.addConfigurator.apply(this, [d, this.getEventAreaId(d)])
                                } else {
                                    this.userEvents[b](f, d)
                                }
                            }
                        }
                    }
                }
            }
        }
    };
    this.mouseoverArea = function (a) {
        return function (d) {
            var b = a.getEventAreaId(d);
            if (b) {
                a.handleMouseAreaEvents("mouseover", d, b)
            }
        }
    }(this);
    this.mouseoutArea = function (a) {
        return function (d) {
            var b = a.getEventAreaId(d);
            if (b) {
                a.handleMouseAreaEvents("mouseout", d, b)
            }
        }
    }(this);
    this.clickArea = function (a) {
        return function (d) {
            var b = a.getEventAreaId(d);
            if (b) {
                a.handleMouseAreaEvents("click", d, b)
            }
        }
    }(this);
    this.mousemoveArea = function (a) {
        return function (d) {
            a.updateDragDiv(d);
            if (a.modelEvent != "local") {
                var b = a.isEvent(d);
                if (b) {
                    a.handleMouseAreaEvents("mousemove", d, b)
                }
            }
        }
    }(this);
    this.mousedownArea = function (a) {
        return function (b) {
            a.cancelEvent(b);
            a.registerDragDiv(b)
        }
    }(this);
    this.mouseupArea = function (a) {
        return function (b) {
            a.mouseupImage(b)
        }
    }(this);
    this.addRemoveImageMapListeners = function (d, b) {
        this[d](b, "mouseover", this.mouseoverArea, false);
        this[d](b, "mousemove", this.mousemoveArea, false);
        this[d](b, "mouseout", this.mouseoutArea, false);
        this[d](b, "mousedown", this.mousedownArea, false);
        this[d](b, "mouseup", this.mouseupArea, false);
        this[d](b, "click", this.clickArea, false);
        this[d](b, "mousewheel", this.wheelImage, false)
    };
    this.formatCoords = function (j, k) {
        var f = [];
        if (this.graphType != "Network") {
            for (var e = 0; e < k.length; e++) {
                if (e % 2) {
                    f.push(sprintf("%.0f", (k[e] + this.translateY) * this.scaleY))
                } else {
                    if (this.layoutValid && j == "circle" && e == 2) {
                        f.push(sprintf("%.0f", k[e] * ((this.scaleX + this.scaleY) / 2)))
                    } else {
                        f.push(sprintf("%.0f", (k[e] + this.translateX) * this.scaleX))
                    }
                }
            }
        } else {
            for (var e = 0; e < k.length; e++) {
                f.push(sprintf("%.0f", k[e]))
            }
        }
        if (j == "rect") {
            var b = Math.min(f[0], f[2]);
            var d = Math.min(f[1], f[3]);
            var a = Math.max(f[0], f[2]);
            var g = Math.max(f[1], f[3]);
            f = [b, d, a, g]
        }
        return f.join(",")
    };
    this.addArea = function (j, e, h) {
        if (!j || !e) {
            return
        }
        var g = j.shift();
        if (g && j) {
            if (this.modelEvent == "local") {
                var b = document.getElementById(this.target + "canvasXpressMap");
                if (b) {
                    var d = document.createElement("area");
                    d.shape = g;
                    d.coords = this.formatCoords(g, j);
                    d.id = this.target + e.join(",");
                    d.style.cursor = "default";
                    if (h) {
                        d.id += h
                    }
                    this.addRemoveImageMapListeners("addEvtListener", d);
                    b.appendChild(d)
                }
            } else {
                this.dataEvent.push([e, g, j])
            }
        }
    };
    this.addGlobalArea = function (f) {
        if (!f) {
            return
        }
        var e = f.shift();
        if (e && f) {
            var b = document.getElementById(this.target + "canvasXpressMap");
            var d = document.createElement("area");
            d.shape = e;
            d.coords = this.formatCoords(e, f);
            d.id = this.target + "globalArea";
            this.addRemoveImageMapListeners("addEvtListener", d);
            b.appendChild(d)
        }
    };
    this.resetMap = function () {
        var b = document.getElementById(this.target + "canvasXpressMap");
        if (b) {
            var e = b.childNodes;
            var g = [];
            for (var f = 1; f < e.length; f++) {
                if (e[f].id) {
                    g.push(e[f])
                }
            }
            for (var f = 0; f < g.length; f++) {
                var d = g[f];
                if (d) {
                    this.addRemoveImageMapListeners("removeEvtListener", d);
                    d.parentNode.removeChild(d)
                }
            }
            if (this.modelEvent != "local") {
                this.dataEvent = [this.dataEvent[0]]
            }
        }
    };
    this.initializeImageMapEvents = function () {
        if (!this.disableEvents) {
            this.addImage();
            this.addMap();
            this.addCanvasListeners()
        }
    };
    this.initializeImageMapEvents()
};
CanvasXpress.prototype.initCanvasResizerEvents = function () {
    this.addCanvasResizerDiv = function () {
        if (document.getElementById(this.target + "canvasXpressCanvasResize")) {
            return
        }
        var a = document.createElement("div");
        a.id = this.target + "canvasXpressCanvasResize";
        a.style.left = "0px";
        a.style.top = "0px";
        a.style.width = "0.5px";
        a.style.height = "0.5px";
        a.style.opacity = 0.5;
        a.style.filter = "alpha(opacity = 50)";
        a.style.position = "absolute";
        a.style.display = "none";
        a.style.zIndex = 7001;
        this.canvas.parentNode.appendChild(a)
    };
    this.registerCanvasResizer = function (f) {
        if (!f) {
            f = window.event
        }
        var b = document.getElementById(this.target + "canvasXpressImage");
        if (b) {
            var d = this.adjustedCoordinates(f);
            if (d) {
                var a = d.x;
                var g = d.y;
                if (a > this.width - 18 && g > this.height - 18) {
                    b.style.cursor = "se-resize";
                    this.resizeCanvasCursorShow = "se-resize"
                } else {
                    if (a > this.width - 18) {
                        b.style.cursor = "e-resize";
                        this.resizeCanvasCursorShow = "e-resize"
                    } else {
                        if (g > this.height - 18) {
                            b.style.cursor = "s-resize";
                            this.resizeCanvasCursorShow = "s-resize"
                        } else {
                            if (!this.movingOn) {
                                b.style.cursor = "default";
                                this.resizeCanvasCursorShow = false
                            }
                        }
                    }
                }
            }
        }
    };
    this.updateCanvasResizer = function (d) {
        if (this.resizeCanvasCursorShow && this.resizingCanvasOn) {
            document.body.style.cursor = this.resizeCanvasCursorShow;
            var b = document.getElementById(this.target + "canvasXpressCanvasResize");
            var a = this.adjustedCoordinates(d);
            if (b && a) {
                if (this.resizeCanvasCursorShow == "se-resize") {
                    b.style.width = a.x + "px";
                    b.style.height = a.y + "px"
                } else {
                    if (this.resizeCanvasCursorShow == "e-resize") {
                        b.style.width = a.x + "px";
                        b.style.height = this.height + "px"
                    } else {
                        if (this.resizeCanvasCursorShow == "s-resize") {
                            b.style.width = this.width + "px";
                            b.style.height = a.y + "px"
                        }
                    }
                }
                b.style.display = "block"
            }
        }
    };
    this.endCanvasResizer = function (f) {
        if (this.resizeCanvasCursorShow) {
            var b = document.getElementById(this.target + "canvasXpressImage");
            var d = document.getElementById(this.target + "canvasXpressCanvasResize");
            if (b && d) {
                var a = parseInt(d.style.width.replace("px", ""));
                var g = parseInt(d.style.height.replace("px", ""));
                this.draw(a, g);
                d.style.width = "0.5px";
                d.style.height = "0.5px";
                d.style.display = "none";
                b.style.cursor = "default";
                this.resizeCanvasCursorShow = false;
                this.resizingCanvasOn = false
            }
        }
    };
    this.initializeCanvasResizerEvents = function () {
        if (!this.disableEvents) {
            this.addCanvasResizerDiv()
        }
    };
    this.initializeCanvasResizerEvents()
};
CanvasXpress.prototype.initAxisResizerEvents = function () {
    this.addAxesResizeDiv = function () {
        if (document.getElementById(this.target + "canvasXpressAxesResize")) {
            return
        }
        var e = ["Previous", "Next", "Current", "Middle", "Min", "Max", "Close"];
        var b = 9001;
        var f = document.createElement("div");
        f.id = this.target + "canvasXpressAxesResize";
        if (this.resizerTransparency) {
            f.style.opacity = 0.85;
            f.style.filter = "alpha(opacity = 85)"
        }
        f.style.position = "absolute";
        f.style.display = "none";
        f.style.zIndex = b;
        for (var a = 0; a < e.length; a++) {
            b++;
            var g = document.createElement("div");
            g.id = this.target + "canvasXpressAxesResize" + e[a];
            if (this.resizerTransparency) {
                g.style.opacity = 0.85;
                g.style.filter = "alpha(opacity = 85)"
            }
            g.style.position = "absolute";
            g.style.zIndex = b;
            f.appendChild(g)
        }
        this.canvas.parentNode.appendChild(f)
    };
    this.clickAxesResizeClose = function (a) {
        return function (b) {
            a.cancelEvent(b);
            a.resetAxesResizer(b);
            a.resetFlags(b)
        }
    }(this);
    this.mousemoveActiveAxesResize = function (a) {
        return function (b) {
            a.activateDeactivateAxesResizer(b)
        }
    }(this);
    this.mousedownAxesResize = function (a) {
        return function (b) {
            a.cancelEvent(b);
            a.registerAxesResizer(b);
            return false
        }
    }(this);
    this.mousemoveAxesResize = function (a) {
        return function (b) {
            a.cancelEvent(b);
            a.updateAxesResizer(b);
            return false
        }
    }(this);
    this.mouseupAxesResize = function (a) {
        return function (b) {
            a.stopEvent(b);
            a.endAxesResizer(b);
            return false
        }
    }(this);
    this.addRemoveAxesResizerListeners = function (u, a, k, d, s, e, p, q, l) {
        var g = document.getElementById(this.target + "canvasXpressImage");
        var b = [a, d, s, e, p];
        this[u](g, "mousemove", this.mousemoveAxesResize, false);
        this[u](a, "mousemove", this.mousemoveActiveAxesResize, false);
        this[u](a, "mouseout", this.mousemoveActiveAxesResize, false);
        this[u](k, "click", this.clickAxesResizeClose, false);
        for (var f = 0; f < b.length; f++) {
            this[u](b[f], "mousedown", this.mousedownAxesResize, false);
            this[u](b[f], "mousemove", this.mousemoveAxesResize, false);
            this[u](b[f], "mouseup", this.mouseupAxesResize, false)
        }
    };
    this.showAxesResizer = function (b, F, E, f, e, G, w, C, q, t, a, J, I) {
        if (this.axesResizerShow) {
            return
        }
        var u = document.getElementById(this.target + "canvasXpressAxesResize");
        var A = document.getElementById(this.target + "canvasXpressAxesResizeMin");
        var j = document.getElementById(this.target + "canvasXpressAxesResizeMax");
        var B = document.getElementById(this.target + "canvasXpressAxesResizeMiddle");
        var H = document.getElementById(this.target + "canvasXpressAxesResizeCurrent");
        var D = document.getElementById(this.target + "canvasXpressAxesResizeClose");
        var i = document.getElementById(this.target + "canvasXpressAxesResizePrevious");
        var g = document.getElementById(this.target + "canvasXpressAxesResizeNext");
        if (u && A && j && H && D && i && g) {
            this.updateResizerEventData(false, false, b, F, E, f, e, G, w, C, q, t, a, J, I);
            var o = this.resizerEventData.areas.org[1] - this.resizerEventData.areas.org[0];
            var k = this.resizerEventData.areas.org[3] - this.resizerEventData.areas.org[2];
            var M = 14;
            var L = 9;
            var K = parseInt(L / 2);
            if (this.resizerEventData.pos == "h") {
                C = this.resizerEventData.l.match(/^t/) ? "top" : "bottom";
                u.style.left = (this.resizerEventData.areas.org[0] - K) + "px";
                u.style.top = this.resizerEventData.areas.org[2] + "px";
                u.style.width = ((o + L) - 1) + "px";
                u.style.height = M + "px";
                u.style.backgroundImage = "url('" + this.imageDir + "scroller_empty_hor_" + C + ".png')";
                u.style.backgroundRepeat = "repeat-x";
                i.style.left = "0px";
                i.style.top = "0px";
                i.style.width = L + "px";
                i.style.height = M + "px";
                i.style.backgroundImage = "url('" + this.imageDir + "prev_" + C + ".png')";
                A.style.left = this.resizerEventData.areas.min[0] + "px";
                A.style.top = "0px";
                A.style.width = L + "px";
                A.style.height = M + "px";
                A.style.cursor = "e-resize";
                A.style.backgroundImage = "url('" + this.imageDir + "handle_left_" + C + ".png')";
                H.style.left = this.resizerEventData.areas.cur[0] + "px";
                H.style.top = "0px";
                H.style.width = this.resizerEventData.areas.cur[1] + "px";
                H.style.height = M + "px";
                H.style.cursor = "move";
                H.style.backgroundImage = "url('" + this.imageDir + "scroller_body_hor_" + C + ".png')";
                H.style.backgroundRepeat = "repeat-x";
                j.style.left = this.resizerEventData.areas.max[0] + "px";
                j.style.top = "0px";
                j.style.width = L + "px";
                j.style.height = M + "px";
                j.style.cursor = "e-resize";
                j.style.backgroundImage = "url('" + this.imageDir + "handle_right_" + C + ".png')";
                j.style.backgroundRepeat = "no-repeat";
                B.style.left = (this.resizerEventData.areas.min[0] + ((this.resizerEventData.areas.cur[1] - 1) / 2)) + "px";
                B.style.top = "0px";
                B.style.width = "8px";
                B.style.height = M + "px";
                B.style.cursor = "move";
                B.style.backgroundImage = "url('" + this.imageDir + "scroller_handle_hor_" + C + ".png')";
                g.style.left = (o - 1) + "px";
                g.style.top = "0px";
                g.style.width = L + "px";
                g.style.height = M + "px";
                g.style.backgroundImage = "url('" + this.imageDir + "next_" + C + ".png')";
                D.style.left = (o + L) + "px";
                D.style.top = "0px";
                D.style.width = L + "px";
                D.style.height = M + "px";
                D.style.backgroundImage = "url('" + this.imageDir + "close.png')"
            } else {
                C = this.resizerEventData.l.match(/^l/) ? "left" : "right";
                u.style.left = this.resizerEventData.areas.org[0] + "px";
                u.style.top = (this.resizerEventData.areas.org[2] - K) + "px";
                u.style.width = M + "px";
                u.style.height = (k + L) + "px";
                u.style.backgroundImage = "url('" + this.imageDir + "scroller_empty_ver_" + C + ".png')";
                u.style.backgroundRepeat = "repeat-y";
                i.style.left = "0px";
                i.style.top = k + "px";
                i.style.width = M + "px";
                i.style.height = L + "px";
                i.style.backgroundImage = "url('" + this.imageDir + "prev_" + C + ".png')";
                A.style.left = "0px";
                A.style.top = this.resizerEventData.areas.min[2] + "px";
                A.style.width = M + "px";
                A.style.height = L + "px";
                A.style.cursor = "n-resize";
                A.style.backgroundImage = "url('" + this.imageDir + "handle_bottom_" + C + ".png')";
                A.style.backgroundRepeat = "no-repeat";
                H.style.left = "0px";
                H.style.top = this.resizerEventData.areas.cur[2] + "px";
                H.style.width = M + "px";
                H.style.height = this.resizerEventData.areas.cur[3] + "px";
                H.style.cursor = "move";
                H.style.backgroundImage = "url('" + this.imageDir + "scroller_body_ver_" + C + ".png')";
                H.style.backgroundRepeat = "repeat-y";
                j.style.left = "0px";
                j.style.top = this.resizerEventData.areas.max[2] + "px";
                j.style.width = M + "px";
                j.style.height = L + "px";
                j.style.cursor = "n-resize";
                j.style.backgroundImage = "url('" + this.imageDir + "handle_top_" + C + ".png')";
                j.style.backgroundRepeat = "no-repeat";
                B.style.left = "0px";
                B.style.top = (this.resizerEventData.areas.max[2] + (this.resizerEventData.areas.cur[3] / 2)) + "px";
                B.style.width = M + "px";
                B.style.height = M + "px";
                B.style.cursor = "move";
                B.style.backgroundImage = "url('" + this.imageDir + "scroller_handle_ver_" + C + ".png')";
                B.style.backgroundRepeat = "no-repeat";
                g.style.left = "0px";
                g.style.top = "0px";
                g.style.width = M + "px";
                g.style.height = L + "px";
                g.style.backgroundImage = "url('" + this.imageDir + "next_" + C + ".png')";
                D.style.left = (M + 1) + "px";
                D.style.top = -L + "px";
                D.style.width = L + "px";
                D.style.height = M + "px";
                D.style.backgroundImage = "url('" + this.imageDir + "close.png')"
            }
            u.style.display = "block";
            if (this.resizerVisibility && this.layoutValid) {
                D.style.display = "none"
            }
            this.addRemoveAxesResizerListeners("addEvtListener", u, D, A, j, B, H, i, g);
            if (this.layoutValid) {
                this.layoutValidR = J;
                this.layoutValidC = I;
                this.layoutValidN = a
            }
            this.axesResizerShow = true
        }
    };
    this.updateResizerEventData = function (i, F, z, o, n, a, V, S, I, M, G, H, x, r, q) {
        var E, D, k, j, y, w, h, e, O, N, C, B, m, U;
        z = z ? z : this.resizerEventData.axis;
        o = o ? o : this.resizerEventData.areas.org[0];
        n = n ? n : this.resizerEventData.areas.org[1];
        a = a ? a : this.resizerEventData.areas.org[2];
        V = V ? V : this.resizerEventData.areas.org[3];
        S = S ? S : this.resizerEventData.dim;
        I = I ? I : this.resizerEventData.pos;
        M = M ? M : this.resizerEventData.l;
        G = G ? G : this.resizerEventData.vals;
        H = H ? H : this.resizerEventData.set ? this.resizerEventData.set : "";
        x = x ? x : this.resizerEventData.c, r = r ? r : this.resizerEventData.i;
        q = q ? q : this.resizerEventData.j;
        var Q = G.graphType;
        var f = n - o;
        var b = V - a;
        var T = G[z + "AbsMin"];
        var A = G[z + "AbsMax"];
        var t = I == "h" ? f : b;
        var P = t / (A - T);
        if (!i || isNaN(i)) {
            i = Q.match(/Scatter/) ? G["setMin" + S.toUpperCase()] : Q == "BarLine" && M.match(/b|r/) ? G.setMin2 : G.setMin;
            if (!i || isNaN(i)) {
                i = T
            }
        }
        i = Math.max(i, T);
        if (!F || isNaN(F)) {
            F = Q.match(/Scatter/) ? G["setMax" + S.toUpperCase()] : Q == "BarLine" && M.match(/b|r/) ? G.setMax2 : G.setMax;
            if (!F || isNaN(F)) {
                F = A
            }
        }
        F = Math.min(F, A);
        var u = (i - T) * P;
        var R = (F - T) * P;
        var L = 14;
        var K = 9;
        var J = parseInt(K / 2);
        if (I == "h") {
            E = parseInt(u);
            D = K;
            k = 0;
            j = L;
            O = parseInt(u + J + 1);
            N = parseInt(R - (u + 1));
            C = 0;
            B = L;
            y = parseInt(R - 1);
            w = K;
            h = 0;
            e = L;
            m = n + (K - 1);
            U = V
        } else {
            E = 0;
            D = L;
            k = parseInt(b - (u + 1));
            j = K;
            O = 0;
            N = L;
            C = parseInt((b - R) + J);
            B = parseInt(R - u);
            y = 0;
            w = L;
            h = parseInt(b - R);
            e = K;
            m = n;
            U = V + (K - 1)
        }
        this.resizerEventData = {
            g: Q,
            axis: z,
            dim: S,
            pos: I,
            l: M,
            vals: G,
            min: T,
            max: A,
            smin: i,
            smax: F,
            len: t,
            unit: P,
            width: L,
            set: H,
            c: x,
            i: r,
            j: q,
            areas: {
                org: [o, n, a, V],
                lim: [o, m, a, U],
                min: [E, D, k, j],
                max: [y, w, h, e],
                cur: [O, N, C, B]
            }
        }
    };
    this.updateAxesResizer = function (r) {
        if (this.axesResizingOn) {
            if (!r) {
                r = window.event
            }
            var l = this.resizerEventData.areas;
            var u = this.adjustedCoordinates(r);
            if (u) {
                var b = this.xMouseDown - u.x;
                var a = this.yMouseDown - u.y;
                var g, f;
                var n = null;
                var p = null;
                var s = "";
                var A = 14;
                var z = 9 - 1;
                var v = parseInt(z / 2);
                if (this.resizerEventData.active == "min") {
                    g = b > l.min[0] ? this.xMouseDown - l.min[0] : b < (l.min[0] + l.min[1]) - l.max[0] ? this.xMouseDown - ((l.min[0] + l.min[1]) - l.max[0]) : u.x;
                    f = a > l.min[2] - (l.max[2] + l.max[3]) ? this.yMouseDown - (l.min[2] - (l.max[2] + l.max[3])) : a < (l.lim[2] + l.min[2] + l.min[3]) - l.lim[3] ? (l.lim[3] - (l.lim[2] + l.min[2] + l.min[3])) + this.yMouseDown : u.y
                } else {
                    if (this.resizerEventData.active == "max") {
                        g = b > l.max[0] - (l.min[0] + l.min[1]) ? this.xMouseDown - (l.max[0] - (l.min[0] + l.min[1])) : b < (l.lim[0] + l.max[0] + l.max[1]) - l.lim[1] ? (l.lim[1] - (l.lim[0] + l.max[0] + l.max[1])) + this.xMouseDown : u.x;
                        f = a > l.max[2] ? this.yMouseDown - l.max[2] : a < (l.lim[2] + l.max[2] + l.max[3]) - (l.lim[2] + l.min[2]) ? ((l.lim[2] + l.min[2]) - (l.lim[2] + l.max[2] + l.max[3])) + this.yMouseDown : u.y
                    } else {
                        if (this.resizerEventData.active == "cur") {
                            g = b > l.min[0] ? this.xMouseDown - l.min[0] : b < (l.lim[0] + l.max[0] + l.max[1]) - l.lim[1] ? (l.lim[1] - (l.lim[0] + l.max[0] + l.max[1])) + this.xMouseDown : u.x;
                            f = a > l.max[2] ? this.yMouseDown - l.max[2] : a < (l.lim[2] + l.min[2] + l.min[3]) - l.lim[3] ? (l.lim[3] - (l.lim[2] + l.min[2] + l.min[3])) + this.yMouseDown : u.y
                        }
                    }
                }
                var i = g - this.xMouseDown;
                var h = f - this.yMouseDown;
                var m = i / this.resizerEventData.unit;
                var k = h / this.resizerEventData.unit;
                var o = document.getElementById(this.target + "canvasXpressAxesResizeMin");
                var j = document.getElementById(this.target + "canvasXpressAxesResizeMax");
                var w = document.getElementById(this.target + "canvasXpressAxesResizeMiddle");
                var d = document.getElementById(this.target + "canvasXpressAxesResizeCurrent");
                if (o && j && w && d) {
                    if (this.resizerEventData.active == "min") {
                        if (this.resizerEventData.pos == "h") {
                            o.style.left = (l.min[0] + i) + "px";
                            w.style.left = ((l.min[0] + i) + ((l.cur[1] - i) / 2)) + "px";
                            d.style.left = (l.min[0] + i + v + 1) + "px";
                            d.style.width = (l.cur[1] - i) + "px";
                            n = this.formatNumber(this.resizerEventData.smin + m)
                        } else {
                            o.style.top = (l.min[2] + h) + "px";
                            w.style.top = ((l.min[2] + h) - ((l.cur[3] + h) / 2)) + "px";
                            d.style.height = ((l.cur[3] + h)) + "px";
                            n = this.formatNumber(this.resizerEventData.smin - k)
                        }
                        this.showInfoSpan(r, n)
                    } else {
                        if (this.resizerEventData.active == "max") {
                            if (this.resizerEventData.pos == "h") {
                                j.style.left = (l.max[0] + i) + "px";
                                w.style.left = ((l.min[0] + i) + ((l.cur[1] - i) / 2)) + "px";
                                d.style.width = (l.cur[1] + i) + "px";
                                p = this.formatNumber(this.resizerEventData.smax + m)
                            } else {
                                j.style.top = (l.max[2] + h) + "px";
                                w.style.top = ((l.max[2] + h) + ((l.cur[3] - h) / 2)) + "px";
                                d.style.top = (l.max[2] + h + v) + "px";
                                d.style.height = (l.cur[3] - h) + "px";
                                p = this.formatNumber(this.resizerEventData.smax - k)
                            }
                            this.showInfoSpan(r, p)
                        } else {
                            if (this.resizerEventData.active == "cur") {
                                if (this.resizerEventData.pos == "h") {
                                    o.style.left = (l.min[0] + i) + "px";
                                    w.style.left = ((l.min[0] + i) + (l.cur[1] / 2)) + "px";
                                    j.style.left = (l.max[0] + i) + "px";
                                    d.style.left = (l.min[0] + i + v + 1) + "px";
                                    n = this.formatNumber(this.resizerEventData.smin + m);
                                    p = this.formatNumber(this.resizerEventData.smax + m)
                                } else {
                                    o.style.top = (l.min[2] + h) + "px";
                                    w.style.top = ((l.max[2] + h) + (l.cur[3] / 2)) + "px";
                                    j.style.top = (l.max[2] + h) + "px";
                                    d.style.top = (l.max[2] + h + v) + "px";
                                    n = this.formatNumber(this.resizerEventData.smin - k);
                                    p = this.formatNumber(this.resizerEventData.smax - k)
                                }
                                this.showInfoSpan(r, n + "--" + p)
                            }
                        }
                    }
                    this.resizerEventData.lastMin = n;
                    this.resizerEventData.lastMax = p;
                    if (this.resizerDraw) {
                        var q = this.resizerEventData.lastMin != null ? parseFloat(this.resizerEventData.lastMin) : this.resizerEventData.smin ? parseFloat(this.resizerEventData.smin) : false;
                        var t = this.resizerEventData.lastMax != null ? parseFloat(this.resizerEventData.lastMax) : this.resizerEventData.smax ? parseFloat(this.resizerEventData.smax) : false;
                        if (this.resizerEventData.lastMin) {
                            this["setMin" + this.resizerEventData.set] = q
                        }
                        if (this.resizerEventData.lastMax) {
                            this["setMax" + this.resizerEventData.set] = t
                        }
                        this.draw()
                    }
                }
            }
        }
    };
    this.registerAxesResizer = function (f) {
        if (!f) {
            f = window.event
        }
        var i = this.adjustedCoordinates(f);
        if (i) {
            var h = i.x;
            var g = i.y;
            var d = this.resizerEventData.areas;
            var k = d.lim[1] - d.lim[0];
            var j = d.lim[3] - d.lim[2];
            var a = 5;
            var b = k > j ? "h" : "v";
            if (h >= d.min[0] + d.lim[0] && h <= d.min[0] + d.min[1] + d.lim[0] && g >= d.min[2] + d.lim[2] && g <= d.min[2] + d.min[3] + d.lim[2]) {
                this.resizerEventData.active = "min"
            } else {
                if (h >= d.max[0] + d.lim[0] && h <= d.max[0] + d.max[1] + d.lim[0] && g >= d.max[2] + d.lim[2] && g <= d.max[2] + d.max[3] + d.lim[2]) {
                    this.resizerEventData.active = "max"
                } else {
                    if (h >= d.cur[0] + d.lim[0] && h <= d.cur[0] + d.cur[1] + d.lim[0] && g >= d.cur[2] + d.lim[2] && g <= d.cur[2] + d.cur[3] + d.lim[2]) {
                        this.resizerEventData.active = "cur"
                    } else {
                        if (b == "h") {
                            if (h >= d.lim[0] && h < d.min[0] + d.lim[0]) {
                                this.resizerEventData.active = "prev"
                            } else {
                                if (h > d.lim[0] + d.max[0] + d.max[1] && h <= d.lim[1] - a) {
                                    this.resizerEventData.active = "next"
                                }
                            }
                        } else {
                            if (g >= d.lim[2] + d.min[2] + d.min[3] && g <= d.lim[2] + d.lim[3]) {
                                this.resizerEventData.active = "prev"
                            } else {
                                if (g >= d.lim[2] && g <= d.lim[2] + d.max[2]) {
                                    this.resizerEventData.active = "next"
                                }
                            }
                        }
                    }
                }
            }
            this.axesResizingOn = true;
            this.xMouseDown = h;
            this.yMouseDown = g
        }
    };
    this.activateDeactivateAxesResizer = function (j) {
        if (!j) {
            j = window.event
        }
        var r = this.adjustedCoordinates(j);
        if (r) {
            var m;
            var q = r.x;
            var l = r.y;
            var i = this.resizerEventData.areas;
            var t = i.lim[1] - i.lim[0];
            var s = i.lim[3] - i.lim[2];
            var d = 5;
            var g = t > s ? "h" : "v";
            if (g == "h") {
                if (q >= i.lim[0] && q <= i.lim[0] + i.min[0]) {
                    m = "prev"
                } else {
                    if (q >= i.lim[0] + i.max[0] + i.max[1] && q <= i.lim[1] - d) {
                        m = "next"
                    }
                }
            } else {
                if (l >= i.lim[2] + i.min[2] + i.min[3] && l <= i.lim[2] + i.lim[3]) {
                    m = "prev"
                } else {
                    if (l >= i.lim[2] && l <= i.lim[2] + i.max[2]) {
                        m = "next"
                    }
                }
            }
            var f = document.getElementById(this.target + "canvasXpressAxesResizePrevious");
            var h = document.getElementById(this.target + "canvasXpressAxesResizeNext");
            if (f && h) {
                if (m == "prev") {
                    var k = f.style.backgroundImage;
                    if (j.type.match(/mouseout/i)) {
                        if (k.match(/_active.png/)) {
                            f.style.backgroundImage = k.replace("_active.png", ".png")
                        }
                    } else {
                        if (!k.match(/_active.png/)) {
                            f.style.backgroundImage = k.replace(".png", "_active.png")
                        }
                    }
                } else {
                    if (m == "next") {
                        var k = h.style.backgroundImage;
                        if (j.type.match(/mouseout/i)) {
                            if (k.match(/_active.png/)) {
                                h.style.backgroundImage = k.replace("_active.png", ".png")
                            }
                        } else {
                            if (!k.match(/_active.png/)) {
                                h.style.backgroundImage = k.replace(".png", "_active.png")
                            }
                        }
                    } else {
                        var k = f.style.backgroundImage;
                        if (k.match(/_active.png/)) {
                            f.style.backgroundImage = k.replace("_active.png", ".png")
                        }
                        k = h.style.backgroundImage;
                        if (k.match(/_active.png/)) {
                            h.style.backgroundImage = k.replace("_active.png", ".png")
                        }
                    }
                }
            }
            return false
        }
    };
    this.moveAxesResizer = function () {
        var g = this.resizerEventData.smin;
        var j = this.resizerEventData.smax;
        var d = (j - g) / 10;
        if (this.resizerEventData.active == "prev") {
            if (g - d < this.resizerEventData.min) {
                d = g - this.resizerEventData.min;
                this.resizerEventData.lastMin = this.resizerEventData.min;
                this.resizerEventData.lastMax = j - d
            } else {
                this.resizerEventData.lastMin = g - d;
                this.resizerEventData.lastMax = j - d
            }
        } else {
            if (j + d > this.resizerEventData.max) {
                d = this.resizerEventData.max - j;
                this.resizerEventData.lastMin = g + d;
                this.resizerEventData.lastMax = this.resizerEventData.max
            } else {
                this.resizerEventData.lastMin = g + d;
                this.resizerEventData.lastMax = j + d
            }
        }
        var f = this.resizerEventData.areas;
        var e = document.getElementById(this.target + "canvasXpressAxesResizeMin");
        var l = document.getElementById(this.target + "canvasXpressAxesResizeMax");
        var k = document.getElementById(this.target + "canvasXpressAxesResizeMiddle");
        var i = document.getElementById(this.target + "canvasXpressAxesResizeCurrent");
        var b = 14;
        var a = 9 - 1;
        var m = parseInt(a / 2);
        var h = d * this.resizerEventData.unit;
        if (this.resizerEventData.pos == "h" && this.resizerEventData.active == "prev" || this.resizerEventData.pos == "v" && this.resizerEventData.active == "next") {
            h *= -1
        }
        if (this.resizerEventData.pos == "h") {
            e.style.left = (f.min[0] + h + 1) + "px";
            k.style.left = ((f.min[0] + h) + (f.cur[1] / 2)) + "px";
            l.style.left = (f.max[0] + h) + "px";
            i.style.left = (f.min[0] + h + m + 1) + "px"
        } else {
            e.style.top = (f.min[2] + h + 1) + "px";
            k.style.top = ((f.max[2] + h) + (f.cur[3] / 2)) + "px";
            l.style.top = (f.max[2] + h) + "px";
            i.style.top = (f.max[2] + h + m + 1) + "px"
        }
    };
    this.getAxesVals = function (e) {
        var d = ["minData", "maxData", "xAxisMin", "xAxisMax", "xAxis2Min", "xAxis2Max", "yAxisMin", "yAxisMax", "zAxisMin", "zAxisMax", "xAxisUnit", "xAxis2Unit", "yAxisUnit", "zAxisUnit", "setMin", "setMax", "setMin2", "setMax2", "setMinX", "setMaxX", "setMinY", "setMaxY", "setMinZ", "setMaxZ", "xAxisAbsMin", "xAxisAbsMax", "xAxis2AbsMin", "xAxis2AbsMax", "yAxisAbsMin", "yAxisAbsMax", "zAxisAbsMin", "zAxisAbsMax", "graphType", "x", "y"];
        var b = {};
        if (this.layoutComb) {
            for (var a = 0; a < d.length; a++) {
                b[d[a]] = this.layoutParams[e][d[a]]
            }
        } else {
            for (var a = 0; a < d.length; a++) {
                b[d[a]] = this[d[a]]
            }
        }
        return b
    };
    this.isAxis = function (h) {
        if (!h) {
            h = window.event
        }
        if (this.resizerVisibility && this.layoutValid) {
            return false
        }
        var r = this.adjustedCoordinates(h);
        if (r) {
            var p = r.x;
            var n = r.y;
            var l = 0;
            var q = (this.margin * 1);
            var o = (this.margin * 2);
            var a = 18;
            for (var d = 0; d < this.layoutRows; d++) {
                for (var b = 0; b < this.layoutCols; b++) {
                    var f = this.layoutComb ? this.layoutParams[l].graphType : this.graphType;
                    if (f.match(/Network|Venn|Correlation|Heatmap/)) {
                        return false
                    }
                    var k = this.getBoundsXY(l, d, b);
                    if (f.match(/Scatter/)) {
                        if (p >= k[0] && p <= k[1] && n >= k[3] && n <= k[3] + a) {
                            var m = this.getAxesVals(l);
                            this.showAxesResizer("xAxis", k[0], k[1], k[3] + q, k[3] + this.getAxisFont(m.x) + o, "x", "h", "b", m, "X", l, d, b);
                            return true
                        } else {
                            if (p >= k[0] - a && p <= k[0] && n >= k[2] && n <= k[3]) {
                                var m = this.getAxesVals(l);
                                this.showAxesResizer("yAxis", k[0] - (this.getAxisFont(m.y) + o), k[0] - q, k[2], k[3], "y", "v", "l", m, "Y", l, d, b);
                                return true
                            }
                        }
                    } else {
                        if (this.graphOrientation == "vertical") {
                            if (p >= k[0] - a && p <= k[0] && n >= k[2] && n <= k[3]) {
                                var m = this.getAxesVals(l);
                                this.showAxesResizer("xAxis", k[0] - (this.getAxisFont(m.y) + o), k[0] - q, k[2], k[3], "y", "v", "l", m, "", l, d, b);
                                return true
                            } else {
                                if (p >= k[1] && p <= k[1] + a && n >= k[2] && n <= k[3]) {
                                    var m = this.getAxesVals(l);
                                    if (f == "BarLine") {
                                        this.showAxesResizer("xAxis2", k[1] + q, k[1] + this.getAxisFont(m.y) + o, k[2], k[3], "y", "v", "r", m, "2", l, d, b)
                                    } else {
                                        this.showAxesResizer("xAxis", k[1] + q, k[1] + this.getAxisFont(m.y) + o, k[2], k[3], "y", "v", "r", m, "", l, d, b)
                                    }
                                    return true
                                }
                            }
                        } else {
                            if (p >= k[0] && p <= k[1] && n >= k[2] - a && n <= k[2]) {
                                var m = this.getAxesVals(l);
                                this.showAxesResizer("xAxis", k[0], k[1], k[2] - (this.getAxisFont(m.x) + o), k[2] - q, "x", "h", "t", m, "", l, d, b);
                                return true
                            } else {
                                if (p >= k[0] && p <= k[1] && n >= k[3] && n <= k[3] + a) {
                                    var m = this.getAxesVals(l);
                                    if (f == "BarLine") {
                                        this.showAxesResizer("xAxis2", k[0], k[1], k[3] + q, k[3] + this.getAxisFont(m.x) + o, "x", "h", "b", m, "2", l, d, b)
                                    } else {
                                        this.showAxesResizer("xAxis", k[0], k[1], k[3] + q, k[3] + this.getAxisFont(m.x) + o, "x", "h", "b", m, "", l, d, b)
                                    }
                                    return true
                                }
                            }
                        }
                    }
                    l++
                }
            }
        }
        return false
    };
    this.endAxesResizer = function (d) {
        if (this.axesResizingOn) {
            this.resetInfoSpan(d);
            if (this.resizerEventData.active == "prev" || this.resizerEventData.active == "next") {
                this.moveAxesResizer()
            }
            var a = this.resizerEventData.lastMin != null ? parseFloat(this.resizerEventData.lastMin) : parseFloat(this.resizerEventData.smin);
            var b = this.resizerEventData.lastMax != null ? parseFloat(this.resizerEventData.lastMax) : parseFloat(this.resizerEventData.smax);
            if (this.resizerEventData.lastMin) {
                this["setMin" + this.resizerEventData.set] = a
            }
            if (this.resizerEventData.lastMax) {
                this["setMax" + this.resizerEventData.set] = b
            }
            this.updateResizerEventData(a, b);
            this.draw();
            this.axesResizingOn = false
        } else {
            if (this.resizerVisibility && this.layoutValid) {
                this.resetAxesResizer(d)
            }
        }
    };
    this.resetAxesResizer = function (g) {
        if (this.axesResizerShow) {
            var a = document.getElementById(this.target + "canvasXpressAxesResize");
            var f = document.getElementById(this.target + "canvasXpressAxesResizeClose");
            var b = document.getElementById(this.target + "canvasXpressAxesResizeMin");
            var l = document.getElementById(this.target + "canvasXpressAxesResizeMax");
            var d = document.getElementById(this.target + "canvasXpressAxesResizeMiddle");
            var j = document.getElementById(this.target + "canvasXpressAxesResizeCurrent");
            var k = document.getElementById(this.target + "canvasXpressAxesResizePrevious");
            var i = document.getElementById(this.target + "canvasXpressAxesResizeNext");
            if (a && f && b && l && j && k && i) {
                this.addRemoveAxesResizerListeners("removeEvtListener", a, f, b, l, d, j, k, i);
                a.style.display = "none";
                this.resizerEventData = {};
                this.resetInfoSpan(g);
                this.axesResizerShow = false;
                if (this.resizerVisibility && this.layoutValid) {
                    this.updateAxesResizerLayout()
                }
            }
        }
    };
    this.initializeAxisResizerEvents = function () {
        if (!this.disableEvents) {
            this.addAxesResizeDiv()
        }
    };
    this.initializeAxisResizerEvents()
};
CanvasXpress.prototype.initEvents = function () {
    this.print = function (a) {
        return function (b) {
            alert("A new window will open so you can right click on the graph and save it");
            if (a.isIE) {
                var g = a.canvas.parentNode.childNodes[0];
                return window.open().document.write("<html><body>" + g.innerHTML + "</body></html>")
            } else {
                var f = a.canvas.toDataURL("image/png");
                return window.open().document.write('<html><body><img src="' + f + '" /></body></html>')
            }
        }
    }(this);
    this.cancelEvent = function (a) {
        if (!a) {
            a = window.event
        }
        if (a.preventDefault) {
            a.preventDefault()
        } else {
            a.returnValue = true
        }
    };
    this.stopEvent = function (a) {
        if (!a) {
            a = window.event
        }
        if (a.stopPropagation) {
            a.stopPropagation()
        } else {
            a.cancelBubble = true
        }
    };
    this.normalizeEvtName = function (a) {
        return this.isIE ? "on" + a : a
    };
    this.addEvtListener = function (d, b, g, a) {
        if (this.isIE) {
            d.attachEvent(this.normalizeEvtName(b), g)
        } else {
            d.addEventListener(b, g, a);
            if (b == "mousewheel") {
                d.addEventListener("DOMMouseScroll", g, a)
            }
        }
    };
    this.removeEvtListener = function (d, b, g, a) {
        if (this.isIE) {
            d.detachEvent(this.normalizeEvtName(b), g)
        } else {
            d.removeEventListener(b, g, a);
            if (b == "mousewheel") {
                d.removeEventListener("DOMMouseScroll", g, a)
            }
        }
    };
    this.adjustedCoordinates = function (g, d) {
        if (!d) {
            d = document.getElementById(this.target + "canvasXpressImage")
        }
        if (d) {
            var i = {};
            var f = d.parentNode.getClientRects();
            if (f && f[0]) {
                var b = f[0].left;
                var d = f[0].top;
                var a = g && g.clientX ? g.clientX : g && g[0] ? g[0] : b;
                var h = g && g.clientY ? g.clientY : g && g[1] ? g[1] : d;
                i.x = document.body.scrollLeft > Math.abs(b) + a && a > document.body.scrollLeft - b ? Math.abs(b) + a : a - b;
                i.y = document.body.scrollTop > Math.abs(d) + h && h > document.body.scrollTop - d ? Math.abs(d) + h : (h - d)
            } else {
                i.x = 0;
                i.y = 0
            }
            return i
        }
    };
    this.isEvent = function (g) {
        var n = function (z, w, v, u) {
                switch (z) {
                case "rect":
                    if (w >= u[0] && w <= u[2] && v >= u[1] && v <= u[3]) {
                        return true
                    } else {
                        return false
                    }
                case "circle":
                    var r = u[2] / 2;
                    u[0] = parseInt(u[0]);
                    u[1] = parseInt(u[1]);
                    if (w >= u[0] - r && w <= u[0] + r && v >= u[1] - r && v <= u[1] + r) {
                        return true
                    } else {
                        return false
                    }
                case "poly":
                    var m = [];
                    var a = [];
                    var t = false;
                    for (var q = 0; q < u.length; q++) {
                        if (q % 2) {
                            a.push(u[q])
                        } else {
                            m.push(u[q])
                        }
                    }
                    if (a[0] < a[1]) {
                        a = a.reverse();
                        m = m.reverse()
                    }
                    for (var q = 0, e = m.length - 1; q < m.length; e = q++) {
                        if (((a[q] <= v && v < a[e]) || (a[e] <= v && v < a[q])) && (w < (m[e] - m[q]) * (v - a[q]) / (a[e] - a[q]) + m[q])) {
                            t = !t
                        }
                    }
                    return t
                }
            };
        var o = this.adjustedCoordinates(g);
        if (o) {
            var l = o.x;
            var j = o.y;
            if (this.modelEvent == "local") {
                var d = document.getElementById(this.target + "canvasXpressMap");
                if (d) {
                    var k = d.childNodes;
                    for (var f = k.length - 1; f >= 0; f--) {
                        var b = k[f].id.replace(this.target, "");
                        var p = k[f].shape;
                        var h = k[f].coords.split(",");
                        if (n(p, l, j, h)) {
                            return b
                        }
                    }
                }
            } else {
                var k = this.dataEvent;
                for (var f = k.length - 1; f >= 0; f--) {
                    var b = k[f][0];
                    var p = k[f][1];
                    var h = k[f][2];
                    if (n(p, l, j, h)) {
                        return b
                    }
                }
            }
        }
        return false
    };
    this.getBoundsXY = function (d, b, a) {
        if (this.layoutValid) {
            if (this.layoutComb) {
                return [this.layoutParams[d].offsetX + this.layoutParams[d].left, this.layoutParams[d].offsetX + this.layoutParams[d].left + this.layoutParams[d].x, this.layoutParams[d].offsetY + this.layoutParams[d].top, this.layoutParams[d].offsetY + this.layoutParams[d].top + this.layoutParams[d].y]
            } else {
                return [(this.width / this.layoutCols * a) + (this.left * this.scaleX), (this.width / this.layoutCols * a) + ((this.left + this.x) * this.scaleX), (this.height / this.layoutRows * b) + (this.top * this.scaleY), (this.height / this.layoutRows * b) + ((this.top + this.y) * this.scaleY)]
            }
        } else {
            return [this.left, this.left + this.x, this.top, this.top + this.y]
        }
    };
    this.extractDataObject = function (l) {
        var j = this.layoutComb && this.layoutValidN > -1 ? this.layoutParams[this.layoutValidN].graphType : this.graphType;
        switch (j) {
        case "Area":
            var b = {
                x: {},
                y: {},
                z: {}
            };
            b.x = this.data.x;
            b.y.vars = [];
            b.y.vars.push(this.data.y.vars[l[0]]);
            b.y.smps = this.data.y.smps;
            b.y.data = this.data.y.data[l[0]];
            for (var a in this.data.z) {
                b.z[a] = [this.data.z[a][l[0]]]
            }
            return b;
        case "Bar":
        case "Line":
        case "BarLine":
        case "Boxplot":
        case "Dotplot":
        case "Heatmap":
        case "Stacked":
        case "StackedPercent":
        case "Scatter2D":
        case "ScatterBubble2D":
        case "Scatter3D":
        case "Candlestick":
            if (!l || isNaN(l[0])) {
                if (l[0].match(/Dendrogram/)) {
                    var b = {
                        t: {}
                    };
                    var a = l[0].split("-");
                    b.t.d = parseInt(a[1]);
                    b.t.o = parseInt(a[2]);
                    b.t.t = a[3];
                    return b
                } else {
                    return
                }
            }
            if (this.isGroupedData) {
                var b = {
                    x: {},
                    w: {},
                    z: {}
                };
                for (var a in this.data.z) {
                    b.z[a] = [this.data.z[a][l[0]]]
                }
                for (var a in this.data.w) {
                    if (a == "grps" || a == "smps") {
                        b.w[a] = [];
                        b.w[a].push(this.data.w[a][l[1]])
                    } else {
                        if (a == "vars") {
                            b.w[a] = [];
                            b.w[a].push(this.data.y.vars[l[0]])
                        } else {
                            if (this.data.w[a].length > l[0] && this.data.w[a][l[0]].length > l[1]) {
                                b.w[a] = [];
                                b.w[a].push(this.data.w[a][l[0]][l[1]])
                            }
                        }
                    }
                }
                for (var a in this.data.x) {
                    var f = [];
                    for (var h = 0; h < b.w.grps[0].length; h++) {
                        f.push(this.data.x[a][b.w.grps[0][h]])
                    }
                    b.x[a] = f
                }
            } else {
                var b = {
                    x: {},
                    y: {},
                    z: {}
                };
                if (j.match(/Scatter/)) {
                    var u = [l[0]];
                    u.push(l[1]);
                    u.push(l[2]);
                    if (j == "Scatter3D" || this.type2D == "XYZ" || j == "ScatterBubble2D") {
                        u.push(l[3])
                    }
                    if (this.colorBy && this.getSampleIndices(this.colorBy) > -1) {
                        u.push(this.getSampleIndices(this.colorBy))
                    }
                    if (this.shapeBy && this.getSampleIndices(this.shapeBy) > -1) {
                        u.push(this.getSampleIndices(this.shapeBy))
                    }
                    if (this.sizeBy && this.getSampleIndices(this.sizeBy) > -1) {
                        u.push(this.getSampleIndices(this.sizeBy))
                    }
                    l = u
                }
                if (this.data.x) {
                    for (var a in this.data.x) {
                        var f = [];
                        for (var h = 1; h < l.length; h++) {
                            f.push(this.data.x[a][l[h]])
                        }
                        b.x[a] = f
                    }
                }
                if (this.data.z) {
                    for (var a in this.data.z) {
                        var k = [];
                        b.z[a] = [this.data.z[a][l[0]]]
                    }
                }
                var m = [];
                var w = [];
                if (this.isRawData && j != "Candlestick") {
                    for (var h = 1; h < l.length; h++) {
                        m.push(this.data.y.data[l[0]][l[h]]);
                        w.push(this.data.y.smps[l[h]])
                    }
                    b.y.vars = [this.data.y.vars[l[0]]];
                    b.y.smps = w;
                    b.y.data = m
                } else {
                    for (var h = 1; h < l.length; h++) {
                        w.push(this.data.y.smps[l[h]])
                    }
                    b.y.vars = [this.data.y.vars[l[0]]];
                    b.y.smps = w;
                    for (var a in this.data.y) {
                        m = [];
                        for (var h = 1; h < l.length; h++) {
                            if (a && a != "vars" && a != "smps" && a != "desc") {
                                m.push(this.data.y[a][l[0]][l[h]])
                            }
                        }
                        if (a && a != "vars" && a != "smps" && a != "desc") {
                            b.y[a] = m
                        }
                    }
                }
            }
            return b;
        case "Pie":
            var b = {
                x: {},
                y: {},
                z: {}
            };
            if (this.data.x) {
                for (var a in this.data.x) {
                    b.x[a] = [this.data.x[a][l[l.length - 1]]]
                }
            }
            if (this.data.z) {
                for (var a in this.data.z) {
                    var k = [];
                    for (var h = 0; h < l.length - 1; h++) {
                        b.z[a] = [this.data.z[a][l[h]]]
                    }
                }
            }
            var m = [];
            var r = [];
            for (var h = 0; h < l.length - 1; h++) {
                r.push(this.data.y.vars[l[h]])
            }
            b.y.vars = r;
            b.y.smps = [this.data.y.smps[l[l.length - 1]]];
            for (var a in this.data.y) {
                m = [];
                for (var h = 0; h < l.length - 1; h++) {
                    if (a && a != "vars" && a != "smps" && a != "desc") {
                        m.push(this.data.y[a][l[h]][l[l.length - 1]])
                    }
                }
                if (a && a != "vars" && a != "smps" && a != "desc") {
                    b.y[a] = m
                }
            }
            return b;
        case "Correlation":
            if (!l || !l[0].match(/^\d/)) {
                return
            }
            var m = this.isGroupedData ? this.data.w.cor : this.data.y.cor;
            if (this.correlationAxis == "samples") {
                var b = {
                    x: {},
                    y: {}
                };
                for (var a in this.data.x) {
                    var f = [];
                    for (var h = 0; h < l.length; h++) {
                        f.push(this.data.x[a][l[h]])
                    }
                    b.x[a] = f
                }
                b.y.smps = [this.data.y.smps[l[0]], this.data.y.smps[l[1]]]
            } else {
                var b = {
                    y: {},
                    z: {}
                };
                for (var a in this.data.y) {
                    var k = [];
                    for (var h = 0; h < l.length; h++) {
                        k.push(this.data.y[a][l[h]])
                    }
                    b.z[a] = k
                }
                b.y.vars = [this.data.y.vars[l[0]], this.data.y.vars[l[1]]]
            }
            b.y.data = m[l[0]][l[1]];
            return b;
        case "Venn":
            var b = {
                venn: {
                    data: {}
                }
            };
            b.venn.data[this.vennCompartments[l[0]]] = this.data.venn.data[this.vennCompartments[l[0]]];
            return b;
        case "Network":
            var b = {};
            if (!l || l.length == 0) {
                return
            }
            if (isNaN(l[0])) {
                l[0] = l[0].toString();
                if (l[0].match(/legend/)) {
                    if (l[0].match(/Nodes/)) {
                        b = {
                            nodeLegend: this.data.legend.nodes
                        }
                    } else {
                        if (l[0].match(/Edges/)) {
                            b = {
                                edgeLegend: this.data.legend.edges
                            }
                        } else {
                            if (l[0].match(/Decorations/)) {
                                b = {
                                    decorationLegend: this.data.legend.decorations
                                }
                            } else {
                                var u = parseInt(l[0].replace("-1-legend-Text", ""));
                                b = {
                                    textLegend: this.data.legend.text[u]
                                }
                            }
                        }
                    }
                }
            } else {
                for (var h = 0; h < l.length; h++) {
                    if (l[h] >= this.data.nodes.length) {
                        if (!b.edges) {
                            b.edges = []
                        }
                        b.edges.push(this.data.edges[l[h] - this.data.nodes.length])
                    } else {
                        if (!b.nodes) {
                            b.nodes = []
                        }
                        b.nodes.push(this.data.nodes[l[h]])
                    }
                }
            }
            return b;
        case "Genome":
            var b = [];
            b[0] = {};
            var u = 0;
            for (var h = 0; h < this.data.tracks.length; h++) {
                var e = this.data.tracks[h].data.length;
                u += e;
                if (l[0] < u) {
                    var q = l[0] - (u - e);
                    for (var a in this.data.tracks[h]) {
                        if (a == "data") {
                            b[0][a] = [];
                            b[0][a].push(this.data.tracks[h][a][q])
                        } else {
                            b[0][a] = this.data.tracks[h][a]
                        }
                    }
                    return b
                }
            }
        }
    };
    this.setUserEvents = function () {
        this.userEvents = {};
        if (this.disableEvents) {
            return
        }
        var b = this;
        if (this.events && this.events.enddragnode) {
            this.userEvents.enddragnode = this.events.enddragnode
        }
        if (this.events && this.events.mouseout) {
            this.userEvents.mouseout = this.events.mouseout
        } else {
            this.userEvents.mouseout = function (g, f) {
                b.resetInfoSpan(f)
            }
        }
        if (this.events && this.events.click) {
            this.userEvents.click = this.events.click
        } else {
            this.userEvents.click = function (g, f) {
                if (g && g.t) {
                    b.modifyDendrogram(g)
                } else {
                    DumperAlert(g)
                }
            }
        }
        var d = ["mouseover"];
        if (b.modelEvent != "local") {
            d.push("mousemove")
        }
        for (var a = 0; a < d.length; a++) {
            if (this.events && this.events[d[a]]) {
                this.userEvents[d[a]] = this.events[d[a]]
            } else {
                switch (this.graphType) {
                case "Bar":
                case "Line":
                case "Area":
                case "BarLine":
                case "Boxplot":
                case "Dotplot":
                case "Heatmap":
                case "Stacked":
                case "StackedPercent":
                case "Scatter2D":
                case "ScatterBubble2D":
                case "Scatter3D":
                    this.userEvents[d[a]] = function (i, g) {
                        if (i) {
                            var h;
                            if (b.isGroupedData && i.w) {
                                h = "<b>" + i.w.vars[0] + "</b><br>";
                                if (b.summaryType == "mean") {
                                    for (var f = 0; f < i.w.smps.length; f++) {
                                        h += "<b>" + i.w.smps[f] + "</b>: " + i.w.mean[f] + "<br>"
                                    }
                                } else {
                                    if (b.summaryType == "median" || b.summaryType == "iqr") {
                                        for (var f = 0; f < i.w.smps.length; f++) {
                                            h += "<b>" + i.w.smps[f] + "</b>: " + i.w.median[f] + "<br>"
                                        }
                                    } else {
                                        if (b.summaryType == "sum") {
                                            for (var f = 0; f < i.w.smps.length; f++) {
                                                h += "<b>" + i.w.smps[f] + "</b>: " + i.w.sum[f] + "<br>"
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (i.y) {
                                    h = "<b>" + i.y.vars[0] + "</b><br>";
                                    for (var f = 0; f < i.y.smps.length; f++) {
                                        h += "<b>" + i.y.smps[f] + "</b>: " + i.y.data[f] + "<br>"
                                    }
                                } else {
                                    if (i.t) {
                                        h = "<b>" + i.t.t + "</b><br>Depth: " + i.t.d + "<br>"
                                    } else {
                                        return
                                    }
                                }
                            }
                            b.showInfoSpan(g, h)
                        }
                    };
                    break;
                case "Pie":
                    this.userEvents[d[a]] = function (k, h) {
                        if (k) {
                            var i = "<b>" + k.y.smps[0] + "</b><br>";
                            for (var f = 0; f < k.y.vars.length; f++) {
                                var g = sprintf("%." + b.pieSegmentPrecision + "f", (k.y.data[f] / b.varPie.tot) * 100);
                                i += "<b>" + k.y.vars[f] + "</b>: " + k.y.data[f] + " (" + g + "%)<br>"
                            }
                            b.showInfoSpan(h, i)
                        }
                    };
                    break;
                case "Candlestick":
                    this.userEvents[d[a]] = function (h, f) {
                        if (h) {
                            var g = dateFormat(h.y.smps[0], b.timeFormat) + ", " + h.y.close[0];
                            b.showInfoSpan(f, g)
                        }
                    };
                    break;
                case "Correlation":
                    this.userEvents[d[a]] = function (h, f) {
                        if (h) {
                            var g;
                            if (b.correlationAxis == "samples") {
                                g = h.y.smps[0] + " vs " + h.y.smps[1]
                            } else {
                                g = h.y.vars[0] + " vs " + h.y.vars[1]
                            }
                            b.showInfoSpan(f, g)
                        }
                    };
                    break;
                case "Venn":
                    this.userEvents[d[a]] = function (i, g) {
                        if (i) {
                            var h;
                            for (var f in i.venn.data) {
                                h = f + " = " + i.venn.data[f];
                                break
                            }
                            b.showInfoSpan(g, h)
                        }
                    };
                    break;
                case "Network":
                    this.userEvents[d[a]] = function (j, h) {
                        if (j) {
                            var i;
                            if (j && j.nodes) {
                                if (j.nodes[0]) {
                                    i = j.nodes[0].name || j.nodes[0].label || j.nodes[0].id
                                }
                            } else {
                                if (j && j.edges) {
                                    var g = b.nodes[j.edges[0].id1];
                                    var f = b.nodes[j.edges[0].id2];
                                    i = (g.label || g.id) + " - " + (f.label || f.id)
                                }
                            }
                            if (i) {
                                b.showInfoSpan(h, i)
                            }
                        }
                    };
                    break;
                case "Genome":
                    this.userEvents[d[a]] = function (g, f) {
                        if (g) {
                            b.showInfoSpan(f, g[0].data[0].id)
                        }
                    };
                    break
                }
            }
        }
    };
    this.resetFlags = function (a) {
        this.resetOn = true;
        this.animationOn = false;
        this.draggingOn = false;
        this.draggingShiftOn = false;
        this.draggingAltOn = false;
        this.draggingCtrlOn = false;
        this.movingOn = false;
        this.xMouseDown = false;
        this.yMouseDown = false;
        this.moveNode = false;
        this.moveNodeIndex = false;
        this.moveNodeLab = false;
        this.moveNodeDec = false;
        this.moveLegend = false;
        this.moveNodeX = false;
        this.moveNodeY = false;
        this.moveNodeW = false;
        this.moveNodeH = false;
        if (a) {
            this.stopEvent(a)
        }
        document.defaultAction = true;
        document.body.style.cursor = "default"
    };
    this.initializeEvents = function () {
        this.setUserEvents();
        this.initConfigurator();
        this.initDataTable();
        if (!this.disableDragEvents) this.initDraggingEvents();
        if (!this.disableResizerEvents) this.initCanvasResizerEvents();
        if (!this.disableResizerEvents) this.initAxisResizerEvents();
        this.initInfoEvents();
        this.initKeyEvents();
        this.initSelectEvents();
        this.initDendrogramEvents();
        this.initImageMapEvents()
    };
    this.initializeEvents()
};
CanvasXpress.prototype.initAnimation = function () {
    this.saveSnapshot = function () {
        var a = this.cloneVisualData(this.data);
        if (a) {
            this.snapshots.push(a)
        }
    };
    this.playSnapshot = function (b, e, d) {
        if (this.snapshots.length < 1) {
            return
        }
        this.stopSnapshotPlay();
        var a = this;
        this.snapshotPlay = {
            idx: 0,
            time: b,
            task: setTimeout(function () {
                a.nextSnapshot(d)
            }, 0),
            callback: e,
            oldData: this.data
        };
        this.snapshotPaused = false
    };
    this.nextSnapshot = function (d) {
        if (this.snapshotPlay.idx >= this.snapshots.length) {
            if (this.snapshotPlay.callback) {
                this.snapshotPlay.callback.call(this, d)
            } else {
                this.snapshotPlay.idx = 0
            }
        }
        if (this.snapshotPlay) {
            var b = this.snapshotPlay.time || this.snapshots[this.snapshotPlay.idx].time || 50;
            this.loadData(this.snapshots[this.snapshotPlay.idx++], true);
            var a = this;
            this.snapshotPlay.task = setTimeout(function () {
                a.nextSnapshot(d)
            }, b);
            this.snapshotPaused = false
        }
    };
    this.stopSnapshotPlay = function (a) {
        if (!this.snapshotPlay) {
            return
        }
        clearTimeout(this.snapshotPlay.task);
        this.loadData(this.snapshotPlay.oldData, a);
        delete this.snapshotPlay;
        this.snapshotPaused = false
    };
    this.clearSnapshot = function () {
        this.stopSnapshotPlay(true);
        this.snapshots = [];
        this.snapshotPaused = false
    };
    this.duplicateSnapshot = function () {
        if (this.snapshots.length < 1) {
            return
        }
        var a = (this.snapshotPlay ? this.snapshotPlay.idx : this.snapshots.length) - 1;
        var b = this.cloneVisualData(this.snapshots[a]);
        if (this.snapshotPlay) {
            this.snapshots.splice(a + 1, 0, b);
            this.snapshotPlay.idx++
        } else {
            this.snapshots.push(b)
        }
    };
    this.makeSnapshotPlay = function () {
        if (!this.snapshotPlay) {
            this.snapshotPlay = {
                idx: this.snapshots.length,
                oldData: this.data
            }
        } else {
            this.pauseSnapshot()
        }
    };
    this.moveSnapshot = function (d) {
        if (this.snapshots.length < 1) {
            return
        }
        this.makeSnapshotPlay();
        var b = this.snapshotPlay.idx - 1,
            a = b + d;
        if (a >= 0 && a <= this.snapshots.length) {
            this.snapshots.splice(a, 0, (this.snapshots.splice(b, 1))[0])
        }
        this.snapshotPlay.idx = a + 1
    };
    this.prevSnapshotOnce = function () {
        if (this.snapshots.length < 2) {
            return
        }
        this.makeSnapshotPlay();
        this.snapshotPlay.idx -= 2;
        if (this.snapshotPlay.idx < 0) {
            this.snapshotPlay.idx = 0
        }
        this.loadData(this.snapshots[this.snapshotPlay.idx++], true);
        this.snapshotPaused = true
    };
    this.nextSnapshotOnce = function () {
        if (!this.snapshotPlay) {
            return
        }
        if (this.snapshotPlay.idx < this.snapshots.length) {
            this.loadData(this.snapshots[this.snapshotPlay.idx++], true)
        }
        this.snapshotPaused = true
    };
    this.hasNextSnapshot = function () {
        return this.snapshots.length > 1 && this.snapshotPlay && this.snapshotPaused && this.snapshotPlay.idx < this.snapshots.length
    };
    this.hasPrevSnapshot = function () {
        return this.snapshots.length > 1 && (!this.snapshotPlay || (this.snapshotPlay.idx > 1 && this.snapshotPaused))
    };
    this.updateSnapshot = function () {
        if (this.snapshotPlay) {
            this.snapshots[this.snapshotPlay.idx - 1] = this.cloneVisualData(this.data)
        }
    };
    this.pauseSnapshot = function () {
        if (!this.snapshotPlay) {
            return
        }
        if (this.snapshotPlay.task) {
            clearTimeout(this.snapshotPlay.task)
        }
        this.snapshotPaused = true
    };
    this.setSnapshotTime = function (a) {
        if (a > 0) {
            this.snapshots[this.snapshotPlay.idx - 1].time = a
        }
    };
    this.getSnapshotTime = function () {
        return this.snapshots[this.snapshotPlay.idx - 1].time || 50
    };
    this.assembleObj = function (b, k, m) {
        var e = [];
        if (!k[m]) {
            return b[m]
        }
        if (!k[m].indices) {
            k[m].indices = {}
        }
        for (var h = 0; h < b[m].length; h++) {
            var f = b[m][h],
                a = f.id || f.id1 + "-" + f.id2;
            if (!k[m].deleted[a]) {
                var l = k[m].changed[a];
                if (l) {
                    for (var g in l) {
                        f[g] = l[g]
                    }
                }
                var p = k[m].indices[a] != null ? k[m].indices[a] : m == "nodes" ? b.nodeIndices[a] : b.edgeIndices[a];
                e[p] = f;
                k[m].indices[a] = p
            }
        }
        for (var a in k[m].added) {
            e[k[m].indices[a]] = k[m].added[a]
        }
        return e
    };
    this.assembleData = function (b, e) {
        var a = {};
        a.nodes = this.assembleObj(b, e, "nodes");
        a.nodeIndices = e.nodes && e.nodes.indices ? e.nodes.indices : b.nodeIndices;
        a.edges = this.assembleObj(b, e, "edges");
        a.legend = e.legend || b.legend;
        return a
    };
    this.loadData = function (e, f) {
        this.data = e;
        switch (this.graphType) {
        case "Bar":
        case "Line":
        case "BarLine":
        case "Dotplot":
        case "Scatter2D":
        case "ScatterBubble2D":
            break;
        case "Scatter3D":
        case "Area":
        case "Boxplot":
        case "Heatmap":
        case "Stacked":
        case "StackedPercent":
        case "Candlestick":
        case "Correlation":
        case "Venn":
        case "Pie":
        case "Genome":
            return false;
            break;
        case "Network":
            if (this.data.type == "changedData") {
                this.data = this.assembleData(this.cloneObject(this.snapshotsBase), this.cloneObject(this.data))
            }
            this.edges = this.data.edges;
            var b = {};
            for (var d = 0; d < this.data.nodes.length; d++) {
                var a = this.data.nodes[d];
                b[a.id] = a
            }
            this.nodes = b;
            break
        }
        if (f) {
            this.draw(false, false, true)
        }
    };
    this.changedNodeData = function (e, h) {
        var j = {
            deleted: {},
            changed: {},
            added: {}
        };
        for (var b in e.nodeIndices) {
            var k = h.nodeIndices[b];
            if (k == null) {
                j.deleted[b] = 1
            } else {
                var i = e.nodes[e.nodeIndices[b]],
                    f = h.nodes[k],
                    l = {
                        id: b
                    },
                    g = false;
                for (var a in f) {
                    if (!this.isSameObject(f[a], i[a])) {
                        l[a] = f[a];
                        g = true
                    }
                }
                if (g) {
                    j.changed[b] = l
                }
            }
        }
        for (var b in h.nodeIndices) {
            if (e.nodeIndices[b] == null) {
                j.added[b] = h.nodes[h.nodeIndices[b]]
            }
        }
        j.indices = this.changedIndices(e.nodeIndices, h.nodeIndices);
        return j
    };
    this.getEdgeIndices = function (g) {
        var b = {};
        if (g.edges.length) {
            for (var a = 0; a < g.edges.length; a++) {
                var f = g.edges[a];
                b[f.id1 + "-" + f.id2] = a
            }
        }
        return b
    };
    this.changedEdgeData = function (g, h) {
        var e = {
            deleted: {},
            changed: {},
            added: {}
        };
        if (!g.edgeIndices) {
            g.edgeIndices = this.getEdgeIndices(g)
        }
        var f = this.getEdgeIndices(h);
        for (var i in g.edgeIndices) {
            var a = f[i];
            if (a == null) {
                e.deleted[i] = 1
            } else {
                var b = h.edges[a];
                if (!this.isSameObject(g.edges[g.edgeIndices[i]], b)) {
                    e.changed[i] = b
                }
            }
        }
        for (var i in f) {
            if (g.edgeIndices[i] == null) {
                e.added[i] = h.edges[f[i]]
            }
        }
        e.indices = this.changedIndices(g.edgeIndices, f);
        return e
    };
    this.changedIndices = function (b, e) {
        var a = {};
        for (var f in e) {
            if (b[f] == null || b[f] != e[f]) {
                a[f] = e[f]
            }
        }
        return a
    };
    this.getSnapshotChanged = function (b) {
        var a = this.snapshotsBase;
        return {
            type: "changedData",
            nodes: this.changedNodeData(a, b),
            edges: this.changedEdgeData(a, b),
            legend: this.isSameObject(a.legend, b.legend) ? null : b.legend
        }
    };
    this.cloneVisualData = function (g) {
        switch (this.graphType) {
        case "Bar":
        case "Line":
        case "BarLine":
        case "Dotplot":
        case "Scatter2D":
        case "ScatterBubble2D":
            return this.cloneObject(g);
            break;
        case "Scatter3D":
        case "Area":
        case "Boxplot":
        case "Heatmap":
        case "Stacked":
        case "StackedPercent":
        case "Candlestick":
        case "Correlation":
        case "Venn":
        case "Pie":
        case "Genome":
            return false;
            break;
        case "Network":
            if (this.snapshotCopyChangeOnly) {
                if (this.snapshots.length) {
                    return g.type == "changedData" ? this.cloneObject(g) : this.getSnapshotChanged(g)
                } else {
                    this.snapshotsBase = this.cloneObject(g);
                    return {
                        type: "changedData"
                    }
                }
            }
            if (!this.snapshotNoCopyData) {
                return this.cloneObject(g)
            }
            var e = {
                nodes: [],
                edges: []
            };
            if (g.nodes && g.nodes.length) {
                for (var b = 0; b < g.nodes.length; b++) {
                    var h = {};
                    var f = g.nodes[b];
                    var k = ["outline", "width", "height", "pattern", "rotate", "color", "shape", "size", "x", "y", "id", "hideName", "hideChildren", "hideParent", "label", "labelX", "labelY", "labelSize", "name", "hide", "anchor", "parentNode"];
                    for (var a = 0; a < k.length; a++) {
                        h[k[a]] = f[k[a]]
                    }
                    e.nodes.push(h)
                }
            }
            if (g.edges && g.edges.length) {
                for (var b = 0; b < g.edges.length; b++) {
                    var h = {};
                    var f = g.edges[b];
                    var k = ["color", "width", "type", "id1", "id2", "anchor"];
                    for (var a = 0; a < k.length; a++) {
                        h[k[a]] = f[k[a]]
                    }
                    e.edges.push(h)
                }
            }
            return e;
            break
        }
    };
    this.isValidAnimation = function () {
        switch (this.graphType) {
        case "Bar":
        case "Line":
        case "BarLine":
        case "Dotplot":
        case "Scatter2D":
        case "ScatterBubble2D":
            return true;
        case "Scatter3D":
        case "Area":
        case "Boxplot":
        case "Heatmap":
        case "Stacked":
        case "StackedPercent":
        case "Candlestick":
        case "Correlation":
        case "Venn":
        case "Pie":
        case "Genome":
        case "Network":
            return false
        }
    };
    this.createAnimation = function (f) {
        switch (this.graphType) {
        case "Bar":
        case "Line":
        case "BarLine":
        case "Dotplot":
        case "Scatter2D":
        case "ScatterBubble2D":
            this.isAnimation = true;
            var g = this.cloneVisualData(this.data);
            if (f.match(/grow|spring|random/) && this.data.y.data) {
                var e = this.animationCycles;
                for (var d = 0; d < e; d++) {
                    for (var b = 0; b < this.data.y.data.length; b++) {
                        for (var a = 0; a < this.data.y.data[b].length; a++) {
                            if (!isNaN(this.data.y.data[b][a])) {
                                var h = this.data.y.data[b][a];
                                if (f == "grow") {
                                    this.data.y.data[b][a] = h / e * d
                                } else {
                                    if (f == "spring") {
                                        this.data.y.data[b][a] = h / (e - 1.5) * (d + 1)
                                    } else {
                                        this.data.y.data[b][a] = h / e * Math.floor(Math.random() * d)
                                    }
                                }
                            }
                        }
                    }
                    this.saveSnapshot();
                    this.data = this.cloneVisualData(g)
                }
            }
            this.data = g;
            this.saveSnapshot();
            this.playSnapshot(this.animationTime, this.stopAnimation);
            break;
        case "Scatter3D":
        case "Area":
        case "Boxplot":
        case "Heatmap":
        case "Stacked":
        case "StackedPercent":
        case "Candlestick":
        case "Correlation":
        case "Venn":
        case "Pie":
        case "Genome":
        case "Network":
            return;
            break
        }
    };
    this.stopAnimation = function () {
        this.stopSnapshotPlay();
        this.snapshots = [];
        this.isAnimation = false
    };
    this.setSnapshotsData = function (a) {
        this.snapshots = a.ss || a;
        this.snapshotsBase = a.base
    };
    this.getSnapshotsData = function () {
        return {
            base: this.snapshotsBase,
            ss: this.snapshots
        }
    };
    this.createDemoNetworkAnimation = function (h) {
        var p = [],
            e = {},
            q = [];
        for (var u = 0; u < h; u++) {
            var n = u + 1;
            p.push({
                size: 0.5,
                x: Math.floor(Math.random() * h * 5),
                y: Math.floor(Math.random() * h * 5),
                outline: "rgb(255,255,255)",
                color: "rgb(255,0,0)",
                id: n,
                label: n,
                hideName: true,
                labelSize: 0.7,
                name: "Node " + n,
                pattern: "closed",
                shape: "star"
            });
            e[n] = u
        }
        var d = {
            nodes: p,
            nodeIndices: e,
            edges: [],
            edgeIndices: {}
        };
        q.push({
            type: "changedData"
        });
        var s = 10;
        var b = 5;
        var o = 255 / b;
        var a = 0.5 / b;
        var k = 5;
        for (var t = 0; t < s; t++) {
            var m = Math.floor(Math.random() * p.length);
            for (var u = 0; u < b; u++) {
                var g = Math.floor(255 - (u + 1) * o);
                var l = Math.floor((u + 1) * o),
                    v = [g, l, l];
                var f = {};
                f[p[m].id] = {
                    size: 0.5 + a * (u + 1),
                    color: "rgb(" + v.join(",") + ")"
                };
                q.push({
                    type: "changedData",
                    nodes: {
                        deleted: {},
                        added: {},
                        changed: f
                    },
                    time: 5
                })
            }
        }
        this.setSnapshotsData({
            base: d,
            ss: q
        })
    };
    this.createDemoNetworkAnimation1 = function () {
        var b = [];
        var m = {};
        var p = [];
        var o = 10;
        var n = 200;
        var a = "ball";
        var g = {
            width: o,
            x: 100,
            y: 0,
            color: "rgb(0,255,0)",
            id: a,
            label: a,
            hideName: true,
            labelSize: 0.7,
            name: a,
            pattern: "closed",
            shape: "sphere"
        };
        a = "plank";
        var h = {
            width: 200,
            height: 5,
            x: 100,
            y: n,
            color: "rgb(255,255,0)",
            id: a,
            label: a,
            hideName: true,
            labelSize: 0.7,
            name: a,
            pattern: "closed",
            shape: "rectangle"
        };
        a = "ref";
        var e = {
            width: 10,
            x: 80,
            y: 0,
            color: "rgb(0,0,255)",
            id: a,
            label: a,
            hideName: true,
            labelSize: 0.7,
            name: a,
            pattern: "closed",
            shape: "sphere"
        };
        var d = {
            nodes: [g, h, e],
            nodeIndices: {
                ball: 0,
                plank: 1,
                ref: 2
            },
            edges: [],
            edgeIndices: {}
        };
        p.push({
            type: "changedData"
        });
        for (var l = 1, k = 0; k < n - o / 2; k += o / 2, l += 0.1) {
            var f = l * l;
            p.push({
                type: "changedData",
                nodes: {
                    deleted: {},
                    added: {},
                    changed: {
                        ball: {
                            y: k,
                            time: 25 * o > k ? 50 - k * 2 / o : 5
                        }
                    }
                },
                time: 100 / f
            })
        }
        this.setSnapshotsData({
            base: d,
            ss: p
        })
    }
};
CanvasXpress.prototype.initData = function (a) {
    this.exampleRawData = {
        x: {
            Tissue: ["Kidney", "Liver", "Kidney", "Liver", "Kidney", "Liver", "Kidney", "Liver"],
            Donor: ["D1", "D1", "D1", "D1", "D2", "D2", "D2", "D2"],
            Time: ["1h", "1h", "2h", "2h", "1h", "1h", "2h", "2h"]
        },
        z: {
            Symbol: ["abc", "def", "ghi", "jkl", "mno", "pqr"],
            Cat: ["A", "A", "B", "C", "D", "C"],
            Class: ["A", "A", "A", "A", "A", "B"]
        },
        y: {
            vars: ["Gene1", "Gene2", "Gene3", "Gene4", "Gene5", "Gene6"],
            smps: ["Smp1", "Smp2", "Smp3", "Smp4", "Smp5", "Smp6", "Smp7", "Smp8"],
            desc: ["Intensity", "Normalized Intensity"],
            data: [
                [10, 12, 3, 4, 100, 73, 42, 64],
                [12, 4, 60, 5, 24, 14, 32, 13],
                [7, 12, 20, 13, 49, 52, 42, 92],
                [21, 10, 30, 8, 65, 166, 47, 58],
                [15, 14, 100, 5, 34, 30, 82, 51],
                [100, 82, 73, 4, 3, 4, 5, 2]
            ]
        },
        a: {
            xAxis: ["Gene1", "Gene2", "Gene3"],
            xAxis2: ["Gene4", "Gene5", "Gene6"]
        },
        t: {
            vars: "((Gene6:0.2,Gene3:0.3):0.2,((Gene1:0.25,(Gene4:0.15,Gene2:0.3):0.2):0.3,Gene5:0.7):0.1)",
            smps: "((Smp2:0.8,(Smp4:0.7,((Smp1:0.6,(Smp6:0.2,Smp5:0.1):0.2):0.15,(Smp8:0.15,Smp3:0.05):0.1):0.15):0.2):0.05,Smp7:0.9)"
        },
        d: {
            nlfit: [{
                param: [1, 100, 50, 1, 1.23e-7],
                label: "Fit Example",
                type: "reg"
            }]
        },
        l: {
            vars: [
                ["Gene1", "Gene2", "Gene3", "Gene4", "Gene5"],
                ["Gene6"]
            ],
            weight: [0.7, 0.3],
            type: ["Bar", "Line"],
            desc: ["Intensity", "Score"]
        },
        venn: {
            data: {
                A: 340,
                B: 562,
                C: 620,
                D: 592,
                AB: 639,
                AC: 456,
                AD: 257,
                BC: 915,
                BD: 354,
                CD: 143,
                ABC: 552,
                ABD: 578,
                ACD: 298,
                BCD: 613,
                ABCD: 148
            },
            legend: {
                A: "List 1",
                B: "List 2",
                C: "List 3",
                D: "List 4"
            }
        },
        nodes: [{
            id: "Gene1",
            color: "rgb(255,0,0)",
            shape: "square",
            size: 1
        }, {
            id: "Gene2",
            color: "rgb(255,0,0)",
            shape: "square",
            size: 1.8
        }, {
            id: "Gene3",
            color: "rgb(255,0,0)",
            shape: "square",
            size: 1.4
        }, {
            id: "Gene4",
            color: "rgb(255,0,0)",
            shape: "square",
            size: 1.2
        }, {
            id: "Gene5",
            color: "rgb(255,0,0)",
            shape: "square",
            size: 1.3
        }, {
            id: "Gene6",
            color: "rgb(255,0,0)",
            shape: "square",
            size: 1
        }, {
            id: "Gene7",
            color: "rgb(255,0,0)",
            shape: "square",
            size: 1
        }, {
            id: "Gene8",
            color: "rgb(255,0,0)",
            shape: "square",
            size: 1
        }, {
            id: "SNP1",
            color: "rgb(0,255,0)",
            shape: "sphere",
            size: 1
        }, {
            id: "SNP2",
            color: "rgb(0,255,0)",
            shape: "sphere",
            size: 1
        }, {
            id: "SNP3",
            color: "rgb(0,255,0)",
            shape: "sphere",
            size: 1
        }, {
            id: "SNP4",
            color: "rgb(0,255,0)",
            shape: "sphere",
            size: 1.5
        }, {
            id: "SNP5",
            color: "rgb(0,255,0)",
            shape: "sphere",
            size: 1
        }, {
            id: "SNP6",
            color: "rgb(0,255,0)",
            shape: "sphere",
            size: 1
        }, {
            id: "SNP7",
            color: "rgb(0,255,0)",
            shape: "sphere",
            size: 1
        }, {
            id: "SNP8",
            color: "rgba(0,255,0)",
            shape: "sphere",
            size: 1.2
        }, {
            id: "PH1",
            color: "rgb(0,255,255,0.5)",
            shape: "triangle",
            size: 1,
            hideChildren: false
        }, {
            id: "PH2",
            color: "rgb(0,255,255,0.6)",
            shape: "triangle",
            size: 1,
            parentNode: "PH1"
        }, {
            id: "PH3",
            color: "rgb(0,255,255,0.7)",
            shape: "triangle",
            size: 1,
            parentNode: "PH1"
        }, {
            id: "PH4",
            color: "rgb(0,255,255,0.8)",
            shape: "triangle",
            size: 1,
            parentNode: "PH3"
        }],
        edges: [{
            id1: "Gene1",
            id2: "Gene2",
            color: "rgb(51,102,255)",
            width: "1",
            type: "line"
        }, {
            id1: "Gene2",
            id2: "Gene3",
            color: "rgb(51,102,255)",
            width: "1",
            type: "arrowHeadLine"
        }, {
            id1: "Gene1",
            id2: "Gene4",
            color: "rgb(51,102,255)",
            width: "1",
            type: "arrowHeadLine"
        }, {
            id1: "Gene3",
            id2: "Gene5",
            color: "rgb(51,102,255)",
            width: "1",
            type: "dashedArrowHeadLine"
        }, {
            id1: "Gene6",
            id2: "Gene5",
            color: "rgb(51,102,255)",
            width: "1",
            type: "arrowHeadSquareTailLine"
        }, {
            id1: "Gene7",
            id2: "Gene5",
            color: "rgb(153,153,255)",
            width: "1",
            type: "dottedArrowHeadLine"
        }, {
            id1: "Gene8",
            id2: "Gene7",
            color: "rgb(51,102,255)",
            width: "1",
            type: "line"
        }, {
            id1: "Gene4",
            id2: "Gene7",
            color: "rgb(51,102,255)",
            width: "1",
            type: "squareHeadArrowTailLine"
        }, {
            id1: "Gene1",
            id2: "SNP2",
            color: "rgb(153,102,255)",
            width: "1",
            type: "bezierLine"
        }, {
            id1: "SNP1",
            id2: "SNP4",
            color: "rgb(51,102,152)",
            width: "1",
            type: "line"
        }, {
            id1: "SNP5",
            id2: "Gene5",
            color: "rgb(153,102,255)",
            width: "1",
            type: "arrowHeadLine"
        }, {
            id1: "SNP1",
            id2: "PH2",
            color: "rgb(51,102,152)",
            width: "1",
            type: "line"
        }, {
            id1: "SNP1",
            id2: "Gene2",
            color: "rgb(51,102,152)",
            width: "1",
            type: "line"
        }, {
            id1: "PH4",
            id2: "SNP7",
            color: "rgb(153,102,255)",
            width: "1",
            type: "arrowHeadLine"
        }, {
            id1: "PH2",
            id2: "Gene5",
            color: "rgb(51,102,152)",
            width: "1",
            type: "line"
        }, {
            id1: "PH1",
            id2: "Gene7",
            color: "rgb(153,102,255)",
            width: "1",
            type: "arrowHeadLine"
        }, {
            id1: "PH3",
            id2: "Gene2",
            color: "rgb(51,102,152)",
            width: "1",
            type: "line"
        }, {
            id1: "SNP6",
            id2: "Gene2",
            color: "rgb(51,102,152)",
            width: "1",
            type: "line"
        }, {
            id1: "SNP3",
            id2: "Gene4",
            color: "rgb(153,102,255)",
            width: "1",
            type: "arrowHeadLine"
        }, {
            id1: "SNP4",
            id2: "Gene5",
            color: "rgb(51,102,152)",
            width: "1",
            type: "line"
        }, {
            id1: "SNP5",
            id2: "Gene7",
            color: "rgb(153,102,255)",
            width: "1",
            type: "arrowHeadLine"
        }, {
            id1: "SNP7",
            id2: "Gene5",
            color: "rgb(153,102,255)",
            width: "1",
            type: "arrowHeadLine"
        }, {
            id1: "SNP8",
            id2: "Gene6",
            color: "rgb(153,102,255)",
            width: "1",
            type: "arrowHeadLine"
        }, {
            id1: "SNP8",
            id2: "Gene2",
            color: "rgb(51,102,152)",
            width: "1",
            type: "line"
        }],
        legend: {
            nodes: [],
            edges: []
        },
        tracks: [{
            name: "Affy Probes",
            type: "box",
            connect: true,
            fill: "rgb(255,255,51)",
            line: "rgb(0,0,0)",
            data: [{
                id: "123456_at",
                dir: "right",
                data: [
                    [100, 120],
                    [123, 132],
                    [141, 160]
                ]
            }, {
                id: "234567_at",
                dir: "left",
                data: [
                    [181, 200],
                    [211, 230],
                    [251, 270]
                ]
            }, {
                id: "345678_at",
                dir: "right",
                data: [
                    [281, 300],
                    [311, 330],
                    [351, 370]
                ]
            }]
        }, {
            hide: true,
            type: "bar",
            height: 20,
            fill: ["rgb(255,0,0)", "rgb(0,0,255)", "rgb(255,255,0)"],
            line: ["rgb(255,0,0)", "rgb(0,0,255)", "rgb(255,255,0)"],
            data: [{
                id: "123456_at",
                data: [100, 25, 35, 46]
            }, {
                id: "234567_at",
                data: [181, 80, 45, 10]
            }, {
                id: "345678_at",
                data: [281, 65, 46, 29]
            }]
        }, {
            name: "Tissue Distribution (Heart, Liver, Kidney)",
            hide: false,
            type: "heatmap",
            autowidth: true,
            height: 20,
            line: "rgb(0,0,0)",
            smps: ["Heart", "Kidney", "Liver"],
            data: [{
                id: "123456_at",
                data: [100, 25, 35, 46]
            }, {
                id: "234567_at",
                data: [181, 80, 45, 10]
            }, {
                id: "345678_at",
                data: [281, 65, 46, 29]
            }]
        }, {
            name: "Ref Seq",
            type: "box",
            connect: true,
            fill: "rgb(0,100,0)",
            stroke: "rgb(0,0,0)",
            data: [{
                id: "NM_012345",
                dir: "left",
                data: [
                    [171, 190],
                    [201, 220],
                    [241, 290]
                ]
            }, {
                id: "NM_567890",
                dir: "right",
                data: [
                    [121, 140],
                    [145, 156],
                    [168, 196]
                ]
            }, {
                id: "NM_987654",
                dir: "right",
                data: [
                    [271, 290],
                    [301, 320],
                    [341, 390]
                ]
            }]
        }, {
            name: "SNP",
            type: "triangle",
            fill: "rgb(100,0,0)",
            line: "rgb(0,0,0)",
            data: [{
                id: "SNP123",
                data: 123
            }, {
                id: "SNP234",
                data: 145
            }, {
                id: "SNP789",
                data: 220
            }]
        }, {
            type: "sequence",
            subtype: "DNA",
            hide: true,
            line: "rgb(255,255,255)",
            data: [{
                id: "SNP123",
                data: [119, "AGCT[TA]CGAG"]
            }, {
                id: "SNP234",
                data: [141, "ATCG[TG]AATA"]
            }, {
                id: "SNP789",
                data: [216, "GCCC[CT]AGGG"]
            }]
        }],
        market: [{
            symbol: "BMY",
            data: [
                [20100824, 26.26, 26.37, 25.95, 26.02, 11625900, 26.02],
                [20100823, 26.48, 26.76, 26.38, 26.48, 12146600, 26.48],
                [20100820, 26.31, 26.54, 26.08, 26.44, 18140100, 26.44],
                [20100819, 26.2, 26.29, 25.81, 26.06, 8218000, 26.06],
                [20100818, 26.53, 26.57, 26.23, 26.28, 12235800, 26.28],
                [20100817, 26.4, 26.79, 26.26, 26.59, 12325700, 26.59],
                [20100816, 26.24, 26.34, 26.04, 26.28, 10377700, 26.28],
                [20100813, 26.24, 26.46, 26.1, 26.32, 5760100, 26.32],
                [20100812, 26.01, 26.39, 26, 26.33, 7350500, 26.33],
                [20100811, 26.32, 26.5, 26.15, 26.25, 8808100, 26.25],
                [20100810, 26.32, 26.78, 26.3, 26.66, 7009500, 26.66],
                [20100809, 26.37, 26.54, 26.3, 26.51, 6825300, 26.51],
                [20100806, 26.29, 26.45, 26.05, 26.37, 8774900, 26.37],
                [20100805, 25.83, 26.38, 25.8, 26.38, 12264600, 26.38],
                [20100804, 25.7, 26.13, 25.61, 26.03, 10233700, 26.03],
                [20100803, 25.65, 25.85, 25.58, 25.68, 6842900, 25.68],
                [20100802, 25.33, 25.61, 25.29, 25.53, 9770900, 25.53],
                [20100730, 24.98, 25.13, 24.78, 24.92, 11435700, 24.92],
                [20100729, 25.37, 25.5, 24.85, 25.08, 9463800, 25.08],
                [20100728, 25.25, 25.36, 25.02, 25.12, 8072400, 25.12],
                [20100727, 25.09, 25.35, 24.84, 25.32, 14152600, 25.32],
                [20100726, 24.57, 25.03, 24.57, 24.97, 8817400, 24.97],
                [20100723, 24.94, 24.95, 24.26, 24.65, 13043700, 24.65],
                [20100722, 24.96, 25.22, 24.75, 24.93, 10385300, 24.93],
                [20100721, 24.92, 25.11, 24.59, 24.75, 9830000, 24.75],
                [20100720, 24.65, 25.09, 24.46, 25.02, 10655500, 25.02],
                [20100719, 25.27, 25.27, 24.78, 24.84, 11804800, 24.84],
                [20100716, 25.44, 25.47, 25.1, 25.17, 13136300, 25.17]
            ]
        }]
    };
    this.exampleBasicData = {
        x: {
            Tissue: ["Kidney", "Liver", "Breast", "Brain"],
            Donor: ["D1", "D2", "D2", "D1"]
        },
        z: {
            Symbol: ["abc", "def", "ghi", "jkl", "mno", "pqr"],
            Desc: ["blah", "yeah", "maybe", "good", "bad", "awsome"]
        },
        y: {
            vars: ["Gene1", "Gene2", "Gene3", "Gene4", "Gene5", "Gene6"],
            smps: ["Smp1", "Smp2", "Smp3", "Smp4"],
            desc: ["Intensity"],
            mean: [
                [1, 2, 3, 4],
                [2, 1, 3, 6],
                [3, 2, 1, 5],
                [4, 2, 1, 3],
                [4, 3, 2, 1],
                [9, 2, 7, 4]
            ],
            stdev: [
                [1, 3, 2, 1],
                [2, 3, 2, 1],
                [2, 2, 2, 1],
                [2, 3, 1, 1],
                [1, 1, 1, 1],
                [3, 1, 3, 1]
            ],
            n: [
                [4, 5, 5, 5],
                [4, 5, 5, 5],
                [6, 4, 4, 5],
                [6, 4, 5, 6],
                [5, 5, 5, 5],
                [6, 5, 5, 5]
            ]
        },
        a: {
            xAxis: ["Gene1", "Gene2", "Gene3"],
            xAxis2: ["Gene4", "Gene5", "Gene6"]
        }
    };
    this.isValidPlotData = function (b) {
        var d = this.isGroupedData ? this.data.w : this.data.y;
        if (b == "cor" && d.cor) {
            return true
        } else {
            if (b == "venn" && this.data.venn) {
                return true
            } else {
                if (b == "network" && this.data.nodes) {
                    return true
                } else {
                    if (b == "genome" && this.data.tracks) {
                        return true
                    } else {
                        if (b == "sum" && d.sum && d.sum[0].length > 0) {
                            return true
                        } else {
                            if (b == "mean" && d.mean && d.mean[0].length > 0) {
                                return true
                            } else {
                                if (b == "median" && d.median && d.median[0].length > 0) {
                                    return true
                                } else {
                                    if (b == "iqr" && d.iqr1 && d.qtl1 && d.median && d.qtl3 && d.iqr3 && d.median[0].length > 0) {
                                        return true
                                    } else {
                                        if (b == "candle" && d.close && d.open && d.high && d.low && d.close[0].length > 0) {
                                            return true
                                        } else {
                                            if (b == "volume" && d.volume && d.volume[0].length > 0) {
                                                return true
                                            } else {
                                                if (b == "raw" && this.isRawData) {
                                                    return true
                                                } else {
                                                    return false
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    };
    this.setAllNodesVisible = function () {
        this.nodes = {};
        this.data.nodeIndices = {};
        for (var b = 0; b < this.data.nodes.length; b++) {
            var d = this.data.nodes[b];
            d.hide = false;
            this.nodes[d.id] = d;
            this.data.nodeIndices[d.id] = b
        }
    };
    this.setNodeIndices = function () {
        this.nodes = {};
        this.data.nodeIndices = {};
        for (var b = 0; b < this.data.nodes.length; b++) {
            var d = this.data.nodes[b];
            this.nodes[d.id] = d;
            this.data.nodeIndices[d.id] = b
        }
    };
    this.setNodes = function () {
        if (this.data.nodeIndices) {
            this.nodes = {};
            for (var b = 0; b < this.data.nodes.length; b++) {
                var d = this.data.nodes[b];
                this.nodes[d.id] = d
            }
            this.data.nodes = [];
            for (var e in this.data.nodeIndices) {
                this.data.nodes[this.data.nodeIndices[e]] = this.nodes[e]
            }
        } else {
            this.setNodeIndices()
        }
    };
    this.hideUnhideNodes = function (e, d) {
        d = d ? true : false;
        if (e) {
            if (this.isArray(e)) {
                for (var b = 0; b < e.length; b++) {
                    var g = e[b];
                    var f = this.data.nodes[this.data.nodeIndices[g]];
                    f.hide = d;
                    if (this.nodes && this.nodes[g]) {
                        this.nodes[g].hide = d
                    }
                }
            }
        } else {
            for (var b = 0; b < this.data.nodes.length; b++) {
                var f = this.data.nodes[b];
                f.hide = d;
                f.hideChildren = d;
                if (this.nodes && this.nodes[f.id]) {
                    this.nodes[f.id].hide = d;
                    this.nodes[f.id].hideChildren = d
                }
            }
        }
    };
    this.hideUnhideChildrenNodes = function (f, d) {
        d = d ? true : false;
        if (this.nodeParentHood[f] && this.nodeParentHood[f].children) {
            for (var b = 0; b < this.nodeParentHood[f].children.length; b++) {
                var e = this.nodeParentHood[f].children[b];
                this.data.nodes[this.data.nodeIndices[e]].hiddenParent = d;
                this.hideUnhideChildrenNodes(e, d)
            }
        }
    };
    this.setSelectNodes = function (d) {
        this.selectNode = {};
        this.isSelectNodes = 0;
        for (var b = 0; b < d.length; b++) {
            this.selectNode[d[b]] = true;
            this.isSelectNodes++
        }
    };
    this.getAnnotations = function (d) {
        var b = [];
        if (d) {
            if (this.data.z) {
                for (var e in this.data.z) {
                    b.push(e)
                }
            }
        } else {
            if (this.data.x) {
                for (var e in this.data.x) {
                    b.push(e)
                }
            }
        }
        return b
    };
    this.setAllVariablesVisible = function () {
        if (this.data.y && this.data.y.vars) {
            this.varIndices = [];
            this.hiddenVars = [];
            this.xAxisVarIndices = [];
            this.xAxis2VarIndices = [];
            for (var b = 0; b < this.data.y.vars.length; b++) {
                this.varIndices.push(b);
                this.hiddenVars.push(false)
            }
        }
        if (this.data.a) {
            if (this.data.a.xAxis) {
                this.xAxisVarIndices = this.getVariableIndices(this.data.a.xAxis)
            }
            if (this.data.a.xAxis2) {
                this.xAxis2VarIndices = this.getVariableIndices(this.data.a.xAxis2)
            }
        }
    };
    this.resetVariablesVisibleByAxis = function (b) {
        if (this.data.a) {
            this.setVariablesVisibleByAxis(b, this.getVariableIndices(this.data.a[b]))
        }
    };
    this.setVariablesVisibleByAxis = function (f, b) {
        var d = f + "VarIndices";
        this[d] = [];
        for (var e = 0; e < b.length; e++) {
            this[d].push(b[e])
        }
    };
    this.getVariablesVisibleByAxis = function (b) {
        return this[b + "VarIndices"]
    };
    this.getVariablesByAxis = function (b) {
        if (this.data.a && this.data.a[b]) {
            return this.data.a[b]
        }
        return []
    };
    this.setVariablesVisible = function (b) {
        this.varIndices = [];
        for (var d = 0; d < b.length; d++) {
            this.varIndices.push(b[d])
        }
    };
    this.getVariableIndices = function (b) {
        if (b && this.data.y.vars) {
            var f = [];
            if (this.isArray(b)) {
                if (b.length > 0) {
                    for (var e = 0; e < b.length; e++) {
                        for (var d = 0; d < this.data.y.vars.length; d++) {
                            if (this.data.y.vars[d] == b[e]) {
                                f.push(d);
                                break
                            }
                        }
                    }
                    return f
                }
            } else {
                for (var e = 0; e < this.data.y.vars.length; e++) {
                    if (this.data.y.vars[e] == b) {
                        return e
                    }
                }
                return -1
            }
        }
    };
    this.hideUnhideVars = function (h) {
        if (h) {
            if (this.isArray(h)) {
                for (var e = 0; e < h.length; e++) {
                    for (var d = 0; d < this.data.y.vars.length; d++) {
                        if (this.data.y.vars[d] == h[e]) {
                            if (this.hiddenVars[d]) {
                                this.hiddenVars[d] = false
                            } else {
                                this.hiddenVars[d] = true
                            }
                            break
                        }
                    }
                }
            } else {
                for (var e = 0; e < this.data.y.vars.length; e++) {
                    if (this.data.y.vars[e] == h) {
                        if (this.hiddenVars[e]) {
                            this.hiddenVars[e] = false
                        } else {
                            this.hiddenVars[e] = true
                        }
                        break
                    }
                }
            }
            var b = [];
            for (var e = 0; e < this.data.y.vars.length; e++) {
                if (!this.hiddenVars[e]) {
                    b.push(e)
                }
            }
            this.setVariablesVisible(b);
            var g = ["xAxis", "xAxis2"];
            if (this.data.a) {
                for (var e = 0; e < g.length; e++) {
                    var k = this.getVariableIndices(this.data.a[g[e]]);
                    if (k.length > 0) {
                        var b = [];
                        for (var d = 0; d < k.length; d++) {
                            var f = k[d];
                            if (!this.hiddenVars[f]) {
                                b.push(f)
                            }
                        }
                        this.setVariablesVisibleByAxis(g[e], b)
                    }
                }
            }
        }
    };
    this.getVariables = function () {
        var f = [];
        for (var e = 0; e < this.data.y.vars.length; e++) {
            var b = -1;
            for (var d = 0; d < this.varIndices.length; d++) {
                if (this.varIndices[d] == e) {
                    b = d;
                    break
                }
            }
            f.push({
                name: this.data.y.vars[e],
                hidden: this.hiddenVars[e],
                index: b
            })
        }
        return f
    };
    this.setAllSamplesVisible = function () {
        if (this.data.y && this.data.y.smps) {
            this.smpIndices = [];
            this.hiddenSmps = [];
            for (var b = 0; b < this.data.y.smps.length; b++) {
                this.smpIndices.push(b);
                this.hiddenSmps.push(false)
            }
        }
        if (this.data.w && this.data.w.smps) {
            this.grpIndices = [];
            this.hiddenGrps = [];
            for (var b = 0; b < this.data.w.smps.length; b++) {
                this.grpIndices.push(b);
                this.hiddenGrps.push(false)
            }
        }
    };
    this.getSamplesByAxis = function (b) {
        if (this[b]) {
            return this[b]
        }
        return []
    };
    this.setSamplesVisible = function (b, d) {
        if (this.isGroupedData) {
            this.grpIndices = [];
            for (var f = 0; f < b.length; f++) {
                this.grpIndices.push(b[f])
            }
        } else {
            this.smpIndices = [];
            for (var f = 0; f < b.length; f++) {
                this.smpIndices.push(b[f])
            }
        }
        if (d) {
            var e = this.getObjectArray(b);
            var h = this.isGroupedData ? this.data.w : this.data.y;
            var g = this.isGroupedData ? this.hiddenGrps : this.hiddenSmps;
            for (var f = 0; f < h.smps.length; f++) {
                if (e.hasOwnProperty(f)) {
                    g[f] = false
                } else {
                    g[f] = true
                }
            }
        }
    };
    this.getSampleIndices = function (b) {
        var g = this.isGroupedData ? this.data.w : this.data.y;
        if (b && g.smps) {
            var f = [];
            if (this.isArray(b)) {
                if (b.length > 0) {
                    for (var e = 0; e < b.length; e++) {
                        for (var d = 0; d < g.smps.length; d++) {
                            if (g.smps[d] == b[e]) {
                                f.push(d);
                                break
                            }
                        }
                    }
                    return f
                }
            } else {
                for (var e = 0; e < g.smps.length; e++) {
                    if (g.smps[e] == b) {
                        return e
                    }
                }
                return -1
            }
        }
    };
    this.hideUnhideSmps = function (h) {
        if (h) {
            var k = this.isGroupedData ? this.data.w : this.data.y;
            var f = this.isGroupedData ? this.hiddenGrps : this.hiddenSmps;
            var g = [];
            if (this.isArray(h)) {
                for (var e = 0; e < h.length; e++) {
                    for (var d = 0; d < k.smps.length; d++) {
                        if (k.smps[d] == h[e]) {
                            if (f[d]) {
                                f[d] = false
                            } else {
                                f[d] = true
                            }
                            break
                        }
                    }
                }
            } else {
                for (var e = 0; e < k.smps.length; e++) {
                    if (k.smps[e] == h) {
                        if (f[e]) {
                            f[e] = false
                        } else {
                            f[e] = true
                        }
                        break
                    }
                }
            }
            var b = [];
            for (var e = 0; e < k.smps.length; e++) {
                if (!f[e]) {
                    b.push(e)
                }
            }
            this.setSamplesVisible(b)
        }
    };
    this.getSamples = function () {
        var e = [];
        var k = this.isGroupedData ? this.data.w.smps : this.data.y.smps;
        var l = this.isGroupedData ? this.data.w : this.data.y;
        var g = this.isGroupedData ? this.hiddenGrps : this.hiddenSmps;
        var h = this.isGroupedData ? this.grpIndices : this.smpIndices;
        for (var f = 0; f < k.length; f++) {
            var b = -1;
            for (var d = 0; d < h.length; d++) {
                if (h[d] == f) {
                    b = d;
                    break
                }
            }
            e.push({
                name: k[f],
                hidden: g[f],
                index: b
            })
        }
        return e
    };
    this.setIndices = function () {
        if (this.graphType == "Network") {
            this.setNodes()
        } else {
            if (this.graphType != "Genome" && this.graphType != "Venn") {
                this.setAllVariablesVisible();
                this.setAllSamplesVisible()
            }
        }
    };
    this.resetIndices = function () {
        if (this.isGroupedData) {
            this.setIndices()
        }
    };
    this.setMaxSmpStringAttr = function (d, b) {
        this.setMaxSmpStringAnnt(d, b);
        this.setMaxSmpStringLabel(d, b);
        this.setMaxSmpStringName(b)
    };
    this.setMaxSmpStringAnnt = function (f, e) {
        if (!e) {
            e = this.smpLabelFont
        }
        var d = 0;
        this.maxSmpAnnt = 0;
        this.maxSmpAnntStr = "";
        if (this.data.x) {
            if (f && this.data.x[f]) {
                return f
            } else {
                for (var g in this.data.x) {
                    var b = this.measureText(g, e);
                    if (b > d) {
                        d = b;
                        this.maxSmpAnnt = g.length;
                        this.maxSmpAnntStr = g
                    }
                }
            }
        }
    };
    this.setMaxSmpStringLabel = function (g, e) {
        if (!e) {
            e = this.smpLabelFont
        }
        var d = 0;
        this.maxSmpLabel = 0;
        this.maxSmpLabelStr = "";
        if (this.data.x) {
            if (g && this.data.x[g]) {
                var j = "";
                for (var f = 0; f < this.smpIndices.length; f++) {
                    var b = this.measureText(this.data.x[g][this.smpIndices[f]], e);
                    if (b > d) {
                        d = b;
                        j = this.data.x[g][this.smpIndices[f]]
                    }
                }
                return j
            } else {
                for (var h in this.data.x) {
                    for (var f = 0; f < this.smpIndices.length; f++) {
                        if (this.data.x[h][this.smpIndices[f]]) {
                            var b = this.measureText(this.data.x[h][this.smpIndices[f]], e);
                            if (b > d) {
                                d = b;
                                this.maxSmpLabel = this.data.x[h][this.smpIndices[f]].length;
                                this.maxSmpLabelStr = this.data.x[h][this.smpIndices[f]]
                            }
                        }
                    }
                }
            }
        }
    };
    this.setMaxSmpStringName = function (e) {
        if (!e) {
            e = this.smpLabelFont
        }
        if (this.data.y || this.data.w) {
            var g = this.isGroupedData ? this.data.w : this.data.y;
            var d = 0;
            this.maxSmpName = 0;
            this.maxSmpNameStr = "";
            if (this.isGroupedData) {
                for (var f = 0; f < g.smps.length; f++) {
                    var b = this.measureText(g.smps[f], e);
                    if (b > d) {
                        d = b;
                        this.maxSmpName = g.smps[f].length;
                        this.maxSmpNameStr = g.smps[f]
                    }
                }
            } else {
                for (var f = 0; f < this.smpIndices.length; f++) {
                    var b = this.measureText(g.smps[this.smpIndices[f]], e);
                    if (b > d) {
                        d = b;
                        this.maxSmpName = g.smps[this.smpIndices[f]].length;
                        this.maxSmpNameStr = g.smps[this.smpIndices[f]]
                    }
                }
            }
        }
    };
    this.setMaxVarStringAttr = function (d, b) {
        this.setMaxVarStringAnnt(d, b);
        this.setMaxVarStringLabel(d, b);
        this.setMaxVarStringName(b)
    };
    this.setMaxVarStringAnnt = function (f, e) {
        if (!e) {
            e = this.varLabelFont
        }
        var d = 0;
        this.maxVarAnnt = 0;
        this.maxVarAnntStr = "";
        if (this.data.z) {
            if (f && this.data.z[f]) {
                return f
            } else {
                for (var g in this.data.z) {
                    var b = this.measureText(g, e);
                    if (b > d) {
                        d = b;
                        this.maxVarAnnt = g.length;
                        this.maxVarAnntStr = g
                    }
                }
            }
        }
    };
    this.setMaxVarStringLabel = function (g, e) {
        if (!e) {
            e = this.varLabelFont
        }
        var d = 0;
        this.maxVarLabel = 0;
        this.maxVarLabelStr = "";
        if (this.data.z) {
            if (g && this.data.z[g]) {
                var j = "";
                for (var f = 0; f < this.varIndices.length; f++) {
                    var b = this.measureText(this.data.z[g][this.varIndices[f]], e);
                    if (b > d) {
                        d = b;
                        j = this.data.z[g][this.varIndices[f]]
                    }
                }
                return j
            } else {
                for (var h in this.data.z) {
                    for (var f = 0; f < this.varIndices.length; f++) {
                        if (this.data.z[h][this.varIndices[f]]) {
                            var b = this.measureText(this.data.z[h][this.varIndices[f]], e);
                            if (b > d) {
                                d = b;
                                this.maxVarLabel = this.data.z[h][this.varIndices[f]].length;
                                this.maxVarLabelStr = this.data.z[h][this.varIndices[f]]
                            }
                        }
                    }
                }
            }
        }
    };
    this.setMaxVarStringName = function (e) {
        if (!e) {
            e = this.varLabelFont
        }
        if (this.data.y || this.data.w) {
            var g = this.graphType == "BarLine" ? this.data.y : this.isGroupedData ? this.data.w : this.data.y;
            var d = 0;
            this.maxVarName = 0;
            this.maxVarNameStr = "";
            if (g.vars) {
                for (var f = 0; f < this.varIndices.length; f++) {
                    var b = this.measureText(g.vars[this.varIndices[f]], e);
                    if (b > d) {
                        d = b;
                        this.maxVarName = g.vars[this.varIndices[f]].length;
                        this.maxVarNameStr = g.vars[this.varIndices[f]]
                    }
                }
            }
        }
    };
    this.setRangeData = function (e, f, b, v) {
        var d = this.isGroupedData && !f ? this.data.w : this.data.y;
        var o = Number.MAX_VALUE;
        var s = Number.MIN_VALUE;
        if (this.setMin != null && this.setMax != null) {
            this.minData = this.setMin;
            this.maxData = this.setMax;
            return
        }
        if (this.isGroupedData && !e) {
            e = this.summaryType
        }
        if (e == "genome") {
            for (var t = 0; t < this.data.tracks.length; t++) {
                for (var q = 0; q < this.data.tracks[t].data.length; q++) {
                    for (var p = 0; p < this.data.tracks[t].data[q].data.length; p++) {
                        if (this.data.tracks[t].type == "box") {
                            n = this.data.tracks[t].data[q].data[p];
                            s = Math.max(s, Math.max(n[0], n[1]));
                            o = Math.min(o, Math.min(n[0], n[1]))
                        } else {
                            if (this.data.tracks[t].type.match(/bar|heatmap|sequence/)) {
                                n = this.data.tracks[t].data[q].data;
                                s = Math.max(s, n[0]);
                                o = Math.min(o, n[0])
                            } else {
                                n = this.data.tracks[t].data[q].data;
                                s = Math.max(s, n);
                                o = Math.min(o, n)
                            }
                        }
                    }
                }
            }
        } else {
            if (e == "percentile" && !f) {
                o = 0;
                s = 100
            } else {
                if ((e == "mean" || e == "median" || e == "sum") && !f) {
                    if (v) {
                        var g = this.isGroupedData ? this.grpIndices : this.smpIndices;
                        var z = this.isTransformedData ? d.trans : d[this.summaryType];
                        for (var t = 0; t < g.length; t++) {
                            var m = g[t];
                            var l = 0;
                            for (var q = 0; q < this.varIndices.length; q++) {
                                var u = this.varIndices[q];
                                var B = z[u][m];
                                if (!isNaN(B) && B > 0) {
                                    l += B
                                }
                            }
                            s = Math.max(l, s)
                        }
                        o = 0
                    } else {
                        for (var t = 0; t < this.varIndices.length; t++) {
                            var m = this.varIndices[t];
                            var n;
                            if (this.isTransformedData) {
                                n = d.trans[m]
                            } else {
                                if (this.summaryType == "mean") {
                                    n = d.mean[m]
                                } else {
                                    if (this.summaryType == "median") {
                                        n = d.median[m]
                                    } else {
                                        if (this.summaryType == "sum") {
                                            n = d.sum[m]
                                        }
                                    }
                                }
                            }
                            for (var q = 0; q < n.length; q++) {
                                var y;
                                var B = n[q];
                                if (d.stdev && !this.isTransformedData) {
                                    y = d.stdev[m][q]
                                } else {
                                    y = Number.NaN
                                }
                                if (!isNaN(B)) {
                                    if (!isNaN(y)) {
                                        if (b) {
                                            if (B > 0) {
                                                o = Math.min(B, o);
                                                s = Math.max(B + y, s)
                                            }
                                        } else {
                                            o = Math.min(B, o);
                                            s = Math.max(B + y, s)
                                        }
                                    } else {
                                        if (b) {
                                            if (B > 0) {
                                                o = Math.min(B, o);
                                                s = Math.max(B, s)
                                            }
                                        } else {
                                            o = Math.min(B, o);
                                            s = Math.max(B, s)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (e == "iqr" && !f) {
                        if (this.isTransformedData) {
                            alert("Transformation not allowed");
                            this.isTransformedData = false
                        }
                        for (var t = 0; t < this.varIndices.length; t++) {
                            var m = this.varIndices[t];
                            for (var q = 0; q < d.iqr1.length; q++) {
                                var x = d.iqr1[m][q];
                                var w = d.iqr3[m][q];
                                var r = d.out[m][q];
                                if (!isNaN(x) && !isNaN(w)) {
                                    if (b) {
                                        if (x > 0 && w > 0) {
                                            o = Math.min(x, o);
                                            s = Math.max(w, s)
                                        }
                                    } else {
                                        o = Math.min(x, o);
                                        s = Math.max(w, s)
                                    }
                                }
                                if (r) {
                                    for (var p = 0; p < r.length; p++) {
                                        if (!isNaN(r[p])) {
                                            if (b) {
                                                if (r[p] > 0) {
                                                    o = Math.min(r[p], o);
                                                    s = Math.max(r[p], s)
                                                }
                                            } else {
                                                o = Math.min(r[p], o);
                                                s = Math.max(r[p], s)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (e == "candle") {
                            if (this.isTransformedData) {
                                alert("Transformation not allowed");
                                this.isTransformedData = false
                            }
                            var g = this.isGroupedData ? this.grpIndices : this.smpIndices;
                            for (var t = 0; t < this.varIndices.length; t++) {
                                var m = this.varIndices[t];
                                for (var q = 0; q < g.length; q++) {
                                    var u = g[q];
                                    var h = d.high[m][u];
                                    var A = d.low[m][u];
                                    if (!isNaN(h) && !isNaN(A)) {
                                        o = Math.min(A, o);
                                        s = Math.max(h, s)
                                    }
                                }
                            }
                        } else {
                            if (e == "volume") {
                                var g = this.isGroupedData ? this.grpIndices : this.smpIndices;
                                for (var t = 0; t < this.varIndices.length; t++) {
                                    var m = this.varIndices[t];
                                    for (var q = 0; q < g.length; q++) {
                                        var u = g[q];
                                        if (!isNaN(d.volume[m][u])) {
                                            o = Math.min(d.volume[m][u], o);
                                            s = Math.max(d.volume[m][u], s)
                                        }
                                    }
                                }
                            } else {
                                if (e == "cor") {
                                    var g = this.correlationAxis == "variables" ? this.varIndices : this.isGroupedData ? this.grpIndices : this.smpIndices;
                                    for (var t = 0; t < g.length; t++) {
                                        for (var q = 0; q < g.length; q++) {
                                            var B = this.isTransformedData ? d.trans[t][q] : d.cor[t][q];
                                            if (!isNaN(B)) {
                                                if (b) {
                                                    if (B > 0) {
                                                        o = Math.min(B, o);
                                                        s = Math.max(B, s)
                                                    }
                                                } else {
                                                    o = Math.min(B, o);
                                                    s = Math.max(B, s)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    var z = this.isTransformedData ? d.trans : d.data;
                                    if (v) {
                                        for (var t = 0; t < this.smpIndices.length; t++) {
                                            var l = 0;
                                            for (var q = 0; q < this.varIndices.length; q++) {
                                                var B = z[this.varIndices[q]][this.smpIndices[t]];
                                                if (!isNaN(B) && B > 0) {
                                                    l += B
                                                }
                                            }
                                            s = Math.max(l, s)
                                        }
                                        o = 0
                                    } else {
                                        for (var t = 0; t < this.varIndices.length; t++) {
                                            for (var q = 0; q < this.smpIndices.length; q++) {
                                                var B = z[this.varIndices[t]][this.smpIndices[q]];
                                                if (!isNaN(B)) {
                                                    if (b) {
                                                        if (B > 0) {
                                                            o = Math.min(B, o);
                                                            s = Math.max(B, s)
                                                        }
                                                    } else {
                                                        o = Math.min(B, o);
                                                        s = Math.max(B, s)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        this.minData = this.setMin != null ? this.setMin : o;
        this.maxData = this.setMax != null ? this.setMax : s
    };
    this.sortIndices = function (g, e, b, i, f) {
        var d = this;
        var h = this.isGroupedData ? this.grpIndices : g == "vars" ? this.varIndices : this.smpIndices;
        if (!e) {
            e = this.sortDir
        }
        if (b && g == "vars" && this.data.z[b]) {
            this.tmpAsciiArray = this.data.z[b];
            this.smpSort = -1;
            this.varSort = b
        } else {
            if (b && g == "smps" && this.data.x[b]) {
                this.tmpAsciiArray = this.data.x[b];
                this.varSort = -1;
                this.smpSort = b
            } else {
                if (i) {
                    this.tmpAsciiArray = this.getDataForSmpGrpAtIndex(i - 1);
                    this.smpSort = i - 1
                } else {
                    if (f) {
                        this.tmpAsciiArray = this.data.y.data[f - 1];
                        this.varSort = f - 1
                    } else {
                        this.tmpAsciiArray = this.isGroupedData ? this.data.w.smps : g == "vars" ? this.data.y.vars : this.data.y.smps;
                        if (g == "vars") {
                            this.varSort = false
                        } else {
                            this.smpSort = false
                        }
                    }
                }
            }
        }
        if (this.isAsciiArray(this.tmpAsciiArray)) {
            h.sort(function (k, j) {
                return d.sortAsciibetically(k, j)
            })
        } else {
            h.sort(function (k, j) {
                return d.sortNumerically(k, j)
            })
        }
        if (e != "ascending") {
            h.reverse()
        }
        this.tmpAsciiArray = []
    };
    this.isAsciiArray = function (b) {
        for (var d = 0; d < b.length; d++) {
            if (isNaN(b[d])) {
                return true
            }
        }
        return false
    };
    this.sortAsciibetically = function (e, d) {
        return ((this.tmpAsciiArray[e] > this.tmpAsciiArray[d]) ? 1 : (this.tmpAsciiArray[e] < this.tmpAsciiArray[d]) ? -1 : 0)
    };
    this.sortNumerically = function (e, d) {
        return (this.tmpAsciiArray[e] - this.tmpAsciiArray[d])
    };
    this.isNumeric = function (b, g, f) {
        if (g) {
            for (var e = 0; e < this.smpIndices.length; e++) {
                var d = this.smpIndices[e];
                if (b[d] && isNaN(b[d])) {
                    return false
                }
            }
        } else {
            if (f) {
                for (var e = 0; e < this.varIndices.length; e++) {
                    var d = this.varIndices[e];
                    if (b[d] && isNaN(b[d])) {
                        return false
                    }
                }
            } else {
                for (var d = 0; d < b.length; d++) {
                    if (b[d] && isNaN(b[d])) {
                        return false
                    }
                }
            }
        }
        return true
    };
    this.cleanArray = function (b, h, g) {
        var e = [];
        if (h) {
            for (var f = 0; f < this.smpIndices.length; f++) {
                var d = this.smpIndices[f];
                if (!isNaN(b[d])) {
                    e.push(b[d])
                }
            }
        } else {
            if (g) {
                for (var f = 0; f < this.varIndices.length; f++) {
                    var d = this.varIndices[f];
                    if (!isNaN(b[d])) {
                        e.push(b[d])
                    }
                }
            } else {
                for (var d = 0; d < b.length; d++) {
                    if (!isNaN(b[d])) {
                        e.push(b[d])
                    }
                }
            }
        }
        return e
    };
    this.cleanArrays = function (d, b, k, j) {
        var h = [];
        var g = [];
        if (k) {
            for (var f = 0; f < this.smpIndices.length; f++) {
                var e = this.smpIndices[f];
                if (!isNaN(d[e]) && !isNaN(b[e])) {
                    h.push(d[e]);
                    g.push(b[e])
                }
            }
        } else {
            if (j) {
                for (var f = 0; f < this.varIndices.length; f++) {
                    var e = this.varIndices[f];
                    if (!isNaN(d[e]) && !isNaN(b[e])) {
                        h.push(d[e]);
                        g.push(b[e])
                    }
                }
            } else {
                for (var e = 0; e < d.length; e++) {
                    if (!isNaN(d[e]) && !isNaN(b[e])) {
                        h.push(d[e]);
                        g.push(b[e])
                    }
                }
            }
        }
        return [h, g]
    };
    this.sortArray = function (b, d) {
        var f = [];
        if (b.length != d.length) {
            return b
        }
        for (var e = 0; e < b.length; e++) {
            if (b[d[e]]) {
                f[e] = b[d[e]]
            } else {
                return b
            }
        }
        return f
    };
    this.reverseArray = function (b) {
        var f = [];
        var e = 0;
        for (var d = b.length - 1; d >= 0; d--) {
            f[e] = b[d];
            e++
        }
        return f
    };
    this.percentile = function (f, b, g) {
        var d = b - f;
        if (d == 0) {
            return 50
        }
        var e = g - f;
        return e * 100 / d
    };
    this.rank = function (b) {
        var e = [];
        for (var d = 0; d < b.length; d++) {
            e.push(d)
        }
        e.sort(function (g, f) {
            return b[g] - b[f]
        });
        return e
    };
    this.min = function (b, g, f) {
        b = this.cleanArray(b, g, f);
        var e = Number.MAX_VALUE;
        for (var d = 0; d < b.length; d++) {
            e = Math.min(e, b[d])
        }
        return e
    };
    this.max = function (d, g, f) {
        d = this.cleanArray(d, g, f);
        var b = Number.MIN_VALUE;
        for (var e = 0; e < d.length; e++) {
            b = Math.max(b, d[e])
        }
        return b
    };
    this.sum = function (b, h, g, f) {
        b = this.cleanArray(b, h, g);
        var e = 0;
        for (var d = 0; d < b.length; d++) {
            if (f) {
                if (b[d] > 0) {
                    e += b[d]
                }
            } else {
                e += b[d]
            }
        }
        return e
    };
    this.mean = function (b, h, f) {
        b = this.cleanArray(b, h, f);
        var d = 0;
        var g = 0;
        for (var e = 0; e < b.length; e++) {
            if (!isNaN(b[e])) {
                d += b[e];
                g++
            }
        }
        if (g > 0) {
            d /= g
        }
        return d
    };
    this.range = function (d, h, g) {
        d = this.cleanArray(d, h, g);
        var f = Number.MAX_VALUE;
        var b = Number.MIN_VALUE;
        for (var e = 0; e < d.length; e++) {
            f = Math.min(d[e], f);
            b = Math.max(d[e], b)
        }
        return [f, b]
    };
    this.median = function (b, j, g) {
        b = this.cleanArray(b, j, g);
        var f = [];
        var e = [];
        var h = 0;
        f[0] = Number.NaN;
        f[1] = Number.NaN;
        for (var d = 0; d < b.length; d++) {
            e.push(b[d]);
            h++
        }
        if (h > 0) {
            f[1] = h;
            e.sort(function (k, i) {
                return k - i
            });
            if (h % 2) {
                f[0] = e[parseInt(h / 2)]
            } else {
                f[0] = (e[parseInt((h - 1) / 2)] + e[h / 2]) / 2
            }
        }
        return f
    };
    this.meanStandardDeviationN = function (b, k, h) {
        b = this.cleanArray(b, k, h);
        var g = [];
        var e = 0;
        var d = 0;
        var j = 0;
        for (var f = 0; f < b.length; f++) {
            d += b[f];
            e += b[f] * b[f];
            j++
        }
        if (j > 0) {
            d /= j;
            e = Math.sqrt(e / j - d * d);
            g.push(d);
            g.push(e);
            g.push(j)
        } else {
            g.push("");
            g.push("");
            g.push("")
        }
        return g
    };
    this.quantiles = function (m, e, t) {
        m = this.cleanArray(m, e, t);
        var p = [];
        var j = [];
        var f = m.length;
        if (f > 2) {
            var q = Math.floor(f * 0.25);
            var l = Math.floor(f * 0.5);
            var d = Math.floor(f * 0.75);
            m.sort(function (n, i) {
                return n - i
            });
            var b;
            var s;
            var r;
            if (f % 2) {
                s = m[l]
            } else {
                s = this.mean([m[l - 1], m[l]])
            }
            if ((f / 2) % 2) {
                b = m[q];
                r = m[d]
            } else {
                b = this.mean([m[q - 1], m[q]]);
                r = this.mean([m[d - 1], m[d]])
            }
            var o = (r - b) * 1.5;
            var h;
            var g;
            for (var k = 0; k <= q; k++) {
                if (m[k] < b - o) {
                    j.push(m[k])
                } else {
                    h = m[k];
                    break
                }
            }
            for (var k = f - 1; k >= d; k--) {
                if (m[k] > r + o) {
                    j.push(m[k])
                } else {
                    g = m[k];
                    break
                }
            }
            p.push(b);
            p.push(s);
            p.push(r);
            p.push(h);
            p.push(g);
            p.push(j);
            p.push(f)
        } else {
            if (f > 1) {
                p.push(m[0]);
                p.push(m[1]);
                p.push(m[1]);
                p.push(m[0]);
                p.push(m[1]);
                p.push(j);
                p.push(f)
            } else {
                if (f > 0) {
                    p.push(m[0]);
                    p.push(m[0]);
                    p.push(m[0]);
                    p.push(m[0]);
                    p.push(m[0]);
                    p.push(j);
                    p.push(f)
                } else {
                    p.push(Number.NaN);
                    p.push(Number.NaN);
                    p.push(Number.NaN);
                    p.push(Number.NaN);
                    p.push(Number.NaN);
                    p.push(Number.NaN)
                }
            }
        }
        return p
    };
    this.covariance = function (h, g, b, m) {
        var f = this.cleanArrays(h, g, b, m);
        h = f[0];
        g = f[1];
        var l = 0;
        var d = 0;
        var k = this.mean(h);
        var j = this.mean(g);
        for (var e = 0; e < h.length; e++) {
            l += h[e] * g[e];
            d++
        }
        if (d > 0) {
            l /= d;
            return l - (k * j)
        }
        return l
    };
    this.correlation = function (j, h, d, q) {
        var g = this.cleanArrays(j, h, d, q);
        j = g[0];
        h = g[1];
        var e = 0;
        var o = 0;
        var l = 0;
        var b = 0;
        var k = 0;
        var m = this.covariance(j, h);
        var p = 0;
        for (var f = 0; f < j.length; f++) {
            o += j[f];
            l += h[f];
            b += j[f] * j[f];
            k += h[f] * h[f];
            e++
        }
        if (e > 0) {
            p = (e * e) * m / Math.sqrt(((e * b) - (o * o)) * ((e * k) - (l * l)))
        }
        return p
    };
    this.regression = function (l, k, d, t) {
        var j = this.cleanArrays(l, k, d, t);
        l = j[0];
        k = j[1];
        var e = 0;
        var q = 0;
        var o = 0;
        var s = 0;
        var b = 0;
        var m = 0;
        var p = this.covariance(l, k);
        var r = 0;
        var h = 0;
        var f = 0;
        for (var g = 0; g < l.length; g++) {
            q += l[g];
            o += k[g];
            s += l[g] * k[g];
            b += l[g] * l[g];
            m += k[g] * k[g];
            e++
        }
        if (e > 2) {
            h = (e * s - q * o) / (e * b - q * q);
            f = (o - h * q) / e;
            r = (e * e) * p / Math.sqrt(((e * b) - (q * q)) * ((e * m) - (o * o)))
        }
        return [h, f, r]
    };
    this.normalize = function (g) {
        for (var f = 0; f < g.length; f++) {
            var b = this.meanStandardDeviationN(g[f]);
            for (var e = 0; e < g[f].length; e++) {
                g[f][e] = (g[f][e] - b[0]) / b[1]
            }
        }
        return g
    };
    this.euclidianDistance = function (e, b, k, j) {
        var g = this.cleanArrays(e, b, k, j);
        e = g[0];
        b = g[1];
        var h = 0;
        if (e.length == b.length) {
            for (var f = 0; f < e.length; f++) {
                h += Math.pow(e[f] - b[f], 2)
            }
            h = Math.sqrt(h)
        }
        return h
    };
    this.manhattanDistance = function (e, b, k, j) {
        var g = this.cleanArrays(e, b, k, j);
        e = g[0];
        b = g[1];
        var h = 0;
        if (e.length == b.length) {
            for (var f = 0; f < e.length; f++) {
                h += Math.abs(e[f] - b[f])
            }
        }
        return h
    };
    this.maxDistance = function (e, b, k, j) {
        var g = this.cleanArrays(e, b, k, j);
        e = g[0];
        b = g[1];
        var h = 0;
        if (e.length == b.length) {
            for (var f = 0; f < e.length; f++) {
                h = Math.max(h, Math.abs(e[f] - b[f]))
            }
        }
        return h
    };
    this.addVectors = function (e, d, j, h) {
        var g = this.cleanArrays(e, d, j, h);
        e = g[0];
        d = g[1];
        var b = [];
        if (e.length == d.length) {
            for (var f = 0; f < e.length; f++) {
                b.push(e[f] + d[f])
            }
        }
        return b
    };
    this.compareVectors = function (d, b) {
        if (this.isArray(d) && this.isArray(b)) {
            if (d.length != b.length) {
                return false
            }
            for (var e = 0; e < d.length; e++) {
                if (this.isArray(d[e]) && this.isArray(b[e])) {
                    if (!this.compareArrays(d[e], b[e])) {
                        return false
                    }
                }
                if (d[e] !== b[e]) {
                    return false
                }
            }
            return true
        }
    };
    this.vectorDotProduct = function (d, b, j, h) {
        var f = this.cleanArrays(d, b, j, h);
        d = f[0];
        b = f[1];
        var g = 0;
        if (d.length == b.length) {
            for (var e = 0; e < d.length; e++) {
                g += d[e] * b[e]
            }
        }
        return g
    };
    this.multiplyVectorByValue = function (b, f, g, e) {
        b = this.cleanArray(b, g, e);
        for (var d = 0; d < b.length; d++) {
            b[d] *= f
        }
        return b
    };
    this.getCentroid = function (e, h, g, b) {
        var j = [];
        if (h.length == b.length) {
            var f = e + g;
            for (var d = 0; d < h.length; d++) {
                j.push((e * h[d] + g * b[d]) / f)
            }
        }
        return j
    };
    this.createNode = function (j, e, f, g, i, b) {
        var h = {
            id: j,
            left: e,
            right: f,
            dist: g,
            centroid: i
        };
        if (b) {
            h.mid = b
        }
        if (e == null && f == null) {
            h.size = 1;
            h.depth = 0
        } else {
            h.size = e.size + f.size;
            h.depth = 1 + Math.max(e.depth, f.depth)
        }
        return h
    };
    this.createRandomVectors = function (f, j) {
        var e = j.length;
        if (f > e) {
            return null
        }
        var o = [];
        var h = [];
        var b = {};
        var p = 0;
        var q = 0;
        var m, d;
        while (q < f) {
            if (p == e) {
                return null
            }
            var l = Math.floor(Math.random() * e);
            if (l in b) {
                continue
            }
            b[l] = 1;
            p++;
            m = j[l];
            d = true;
            for (var g = 0; g < q; g++) {
                if (this.compareVectors(m, o[g])) {
                    d = false;
                    break
                }
            }
            if (d) {
                o[q] = m;
                h[q] = l;
                q++
            }
        }
        return {
            vectors: o,
            indices: h
        }
    };
    this.imputeDatum = function (b) {
        var f = [];
        if (this.clusterAxis == "samples") {
            for (var d = 0; d < this.varIndices.length; d++) {
                var e = this.varIndices[d];
                f.push(this.data.y.data[e][b])
            }
        } else {
            for (var d = 0; d < this.smpIndices.length; d++) {
                var e = this.smpIndices[d];
                f.push(this.data.y.data[b][e])
            }
        }
        return this[this.imputeMethod](f)
    };
    this.getIdsForClustering = function () {
        var f = [];
        if (this.clusterAxis == "samples") {
            for (var b = 0; b < this.smpIndices.length; b++) {
                var e = this.smpIndices[b];
                f[b] = this.data.y.smps[e]
            }
        } else {
            for (var b = 0; b < this.varIndices.length; b++) {
                var e = this.varIndices[b];
                f[b] = this.data.y.vars[e]
            }
        }
        return f
    };
    this.getDataForClustering = function () {
        var l = [];
        var b = [];
        if (this.clusterAxis == "samples") {
            for (var f = 0; f < this.smpIndices.length; f++) {
                l[f] = []
            }
            for (var f = 0; f < this.varIndices.length; f++) {
                var g = this.varIndices[f];
                for (var e = 0; e < this.smpIndices.length; e++) {
                    var h = this.smpIndices[e];
                    var k = this.data.y.data[g][h];
                    if (isNaN(k)) {
                        if (!b[e]) {
                            b[e] = this.imputeDatum(h)
                        }
                        k = b[e]
                    }
                    l[e][f] = k
                }
            }
        } else {
            for (var f = 0; f < this.varIndices.length; f++) {
                l[f] = []
            }
            for (var f = 0; f < this.varIndices.length; f++) {
                var g = this.varIndices[f];
                for (var e = 0; e < this.smpIndices.length; e++) {
                    var h = this.smpIndices[e];
                    var k = this.data.y.data[g][h];
                    if (isNaN(k)) {
                        if (!b[f]) {
                            b[f] = this.imputeDatum(g)
                        }
                        k = b[f]
                    }
                    l[f][e] = k
                }
            }
        }
        if (this.centerData) {
            l = this.normalize(l)
        }
        return l
    };
    this.cluster = function () {
        var w;
        var q = this.getDataForClustering();
        var h = this.getIdsForClustering();
        var g = [];
        var f = [];
        var r = [];
        var v = [];
        for (var o = 0; o < q.length; o++) {
            g[o] = []
        }
        for (var o = 0; o < q.length; o++) {
            g[o][o] = Number.POSITIVE_INFINITY;
            for (var m = o + 1; m < q.length; m++) {
                g[o][m] = this[this.distance + "Distance"](q[o], q[m]);
                g[m][o] = g[o][m]
            }
        }
        for (var o = 0; o < q.length; o++) {
            f[o] = 0;
            for (var m = 0; m < q.length; m++) {
                if (g[o][f[o]] > g[o][m]) {
                    f[o] = m
                }
            }
        }
        for (var o = 0; o < q.length; o++) {
            v[o] = [];
            v[o][0] = this.createNode(h[o], null, null, 0, q[o]);
            r[o] = 1
        }
        for (var b = 0; b < q.length - 1; b++) {
            var n = 0;
            for (var o = 0; o < q.length; o++) {
                if (g[o][f[o]] < g[n][f[n]]) {
                    n = o
                }
            }
            var k = f[n];
            var e = v[n][0];
            var s = v[k][0];
            var u = this.getCentroid(e.size, e.centroid, s.size, s.centroid);
            var t = this.createNode(-1, e, s, g[n][k], u);
            v[n].splice(0, 0, t);
            r[n] += r[k];
            for (var m = 0; m < q.length; m++) {
                if (this.linkage == "single") {
                    if (g[n][m] > g[k][m]) {
                        g[m][n] = g[n][m] = g[k][m]
                    }
                } else {
                    if (this.linkage == "complete") {
                        if (g[n][m] < g[k][m]) {
                            g[m][n] = g[n][m] = g[k][m]
                        }
                    } else {
                        if (this.linkage == "average") {
                            g[m][n] = g[n][m] = (r[n] * g[n][m] + r[k] * g[k][m]) / (r[n] + r[m])
                        }
                    }
                }
            }
            g[n][n] = Number.POSITIVE_INFINITY;
            for (var o = 0; o < q.length; o++) {
                g[o][k] = g[k][o] = Number.POSITIVE_INFINITY
            }
            for (var m = 0; m < q.length; m++) {
                if (f[m] == k) {
                    f[m] = n
                }
                if (g[n][m] < g[n][f[n]]) {
                    f[n] = m
                }
            }
            w = t
        }
        return this.writeNewick(w)
    };
    this.isLeafNode = function (b) {
        if (b.left == null && b.right == null) {
            return true
        } else {
            return false
        }
    };
    this.writeNewick = function (d, k) {
        var j = "";
        var h = "";
        if (this.isLeafNode(d)) {
            j = d.id
        } else {
            var b;
            var e = this.writeNewick(d.left, k);
            var g = this.writeNewick(d.right, k);
            if (d.mid) {
                for (var f = 0; f < d.mid.length; f++) {
                    if (k && d.mid[f].dist) {
                        h += "," + d.mid[f].id + ":" + d.mid[f].dist
                    } else {
                        h += "," + d.mid[f].id
                    }
                    h += d.mid[f].id
                }
            }
            j = "(" + e + h + "," + g + ")"
        }
        if (k && d.dist) {
            j += ":" + d.dist
        }
        return j
    };
    this.kmeans = function () {
        var n;
        var p = this.getDataForClustering();
        var e = this.getIdsForClustering();
        var o = [];
        var q = [];
        var s = true;
        var r = 0;
        var k = null;
        var x = this.createRandomVectors(this.kmeansClusters, p);
        if (x == null) {
            return null
        } else {
            k = x.vectors
        }
        while (s) {
            for (var m = 0; m < this.kmeansClusters; m++) {
                q[m] = 0
            }
            for (var m = 0; m < p.length; m++) {
                var u = p[m];
                var h = Number.MAX_VALUE;
                var f;
                for (var g = 0; g < this.kmeansClusters; g++) {
                    var b = this.euclidianDistance(k[g], u);
                    if (b < h) {
                        h = b;
                        f = g
                    }
                }
                q[f]++;
                o[m] = f
            }
            var w = [];
            for (var m = 0; m < this.kmeansClusters; m++) {
                w[m] = null
            }
            for (var m = 0; m < p.length; m++) {
                if (w[o[m]] == null) {
                    w[o[m]] = p[m]
                } else {
                    w[o[m]] = this.addVectors(w[o[m]], p[m])
                }
            }
            for (var m = 0; m < this.kmeansClusters; m++) {
                w[m] = this.multiplyVectorByValue(w[m], 1 / q[m])
            }
            s = false;
            for (var m = 0; m < this.kmeansClusters; m++) {
                if (!this.compareVectors(w[m], k[m])) {
                    s = true;
                    break
                }
            }
            k = w;
            r++;
            if (r > this.maxIterations) {
                s = false
            }
        }
        for (var m = 0; m < o.length; m++) {
            o[m] = "K-" + (o[m] + 1)
        }
        return o
    };
    this.transpose = function (k) {
        if (k) {
            var m = [];
            for (var g = 0; g < k.length; g++) {
                for (var f = 0; f < k[g].length; f++) {
                    if (!m[f]) {
                        m[f] = []
                    }
                    m[f][g] = k[g][f]
                }
            }
            return m
        } else {
            this.ungroupSamples();
            this.transform("reset");
            if (this.data.l) {
                delete(this.data.l)
            }
            if (this.data.d) {
                delete(this.data.d)
            }
            if (this.data.a) {
                delete(this.data.a)
            }
            if (this.data.t) {
                var e = this.data.t.vars;
                var h = this.data.t.smps;
                if (e) {
                    this.data.t.smps = e
                }
                if (h) {
                    this.data.t.vars = h
                }
            }
            var b = this.data.x;
            var l = this.data.z;
            if (b) {
                this.data.z = b
            }
            if (l) {
                this.data.x = l
            }
            var h = this.data.y.smps;
            this.data.y.smps = this.data.y.vars;
            this.data.y.vars = h;
            var m = this.data.y.data;
            this.data.y.data = [];
            for (var g = 0; g < this.data.y.vars.length; g++) {
                this.data.y.data[g] = [];
                for (var f = 0; f < this.data.y.smps.length; f++) {
                    this.data.y.data[g][f] = m[f][g]
                }
            }
            this.initializeData(this.data);
            this.resetObject()
        }
    };
    this.checkObject = function () {
        var e = this.data.y;
        if (this.isGroupedData) {
            this.data.w = {};
            e = this.data.w
        }
        var d = ["vars", "smps", "desc", "data", "mean", "median", "stdev", "n", "qtl1", "qtl3", "iqr1", "iqr3", "out", "trans", "cor", "sum", "grps", "close", "open", "high", "low", "volume"];
        for (var b = 0; b < d.length; b++) {
            if (!e[d[b]]) {
                e[d[b]] = []
            }
        }
    };
    this.resetObject = function (b) {
        var e = ["mean", "median", "stdev", "n", "qtl1", "qtl3", "iqr1", "iqr3", "out", "cor", "sum", "grps", "close", "open", "high", "low", "volume"];
        if (this.isGroupedData) {
            if (!this.isTransformedData) {
                e.push("trans")
            }
            for (var d = 0; d < e.length; d++) {
                delete(this.data.y[e[d]])
            }
            delete(this.data.w)
        } else {
            if (!this.isTransformedData) {
                e.push("trans")
            }
            if (this.isRawData) {
                for (var d = 0; d < e.length; d++) {
                    delete(this.data.y[e[d]])
                }
            } else {
                delete(this.data.y.data)
            }
        }
        if (b) {
            this.checkObject()
        }
    };
    this.getDataAtPos = function (h, f, d, e, g, b) {
        var k = Number.NaN;
        var l = this.isGroupedData ? this.data.w : this.data.y;
        if (this.isGroupedData || !this.isRawData) {
            if (this.isTransformedData) {
                k = l.trans[h][f]
            } else {
                d = d ? d : this.summaryType;
                if (l.hasOwnProperty(d)) {
                    k = l[d][h][f]
                } else {
                    k = 0
                }
            }
        } else {
            if (d && l[d][h]) {
                k = l[d][h][f]
            } else {
                if (this.isTransformedData) {
                    k = l.trans[h][f]
                } else {
                    k = l.data[h][f]
                }
            }
        }
        if (e) {
            if (!isNaN(k)) {
                if (e == "log2") {
                    k = Math.log(k) / Math.LN2
                } else {
                    if (e == "log10") {
                        k = Math.log(k) / Math.LN10
                    } else {
                        if (e == "exp2") {
                            k = Math.pow(2, k)
                        } else {
                            if (e == "exp10") {
                                k = Math.pow(2, k)
                            } else {
                                if (e == "percentile") {
                                    k = this.percentile(g, b, k)
                                }
                            }
                        }
                    }
                }
            }
        }
        return k
    };
    this.getDataForSmpGrpAtIndex = function (d) {
        var b = [];
        if (this.isGroupedData || !this.isRawData) {
            var h = this.isGroupedData ? this.data.w : this.data.y;
            if (this.isTransformedData) {
                b.push(h.trans[g][d])
            } else {
                var e = this.summaryType;
                if (h.hasOwnProperty(e)) {
                    for (var f = 0; f < this.varIndices.length; f++) {
                        var g = this.varIndices[f];
                        b.push(h[e][g][d])
                    }
                }
            }
        } else {
            if (this.isTransformedData) {
                for (var f = 0; f < this.varIndices.length; f++) {
                    var g = this.varIndices[f];
                    b.push(this.data.y.trans[g][d])
                }
            } else {
                for (var f = 0; f < this.varIndices.length; f++) {
                    var g = this.varIndices[f];
                    b.push(this.data.y.data[g][d])
                }
            }
        }
        return b
    };
    this.getMetaDataForVariableAtIndex = function (b) {
        var e = {};
        if (this.data.z) {
            for (var d in this.data.z) {
                e[d] = this.data.z[d][b]
            }
        }
        e[name] = this.data.y.vars[b];
        return e
    };
    this.getMetaDataForSampleAtIndex = function (b) {
        if (this.isGroupedData) {
            return this.getMetaDataForGrpAtIndex(b)
        } else {
            return this.getMetaDataForSmpAtIndex(b)
        }
    };
    this.getMetaDataForSmpAtIndex = function (b) {
        var e = {};
        if (this.data.x) {
            for (var d in this.data.x) {
                e[d] = this.data.x[d][b]
            }
        }
        e[name] = this.data.y.vars[b];
        return e
    };
    this.getMetaDataForGrpAtIndex = function (b) {
        var f = [];
        for (var d = 0; d < this.data.w.grps[b].length; d++) {
            var e = this.data.w.grps[b][d];
            f.push(this.getMetaDataForSmpAtIndex[e])
        }
        return f
    };
    this.summarize = function (m, p) {
        var b = this.isGroupedData ? this.data.w : this.data.y;
        var d = this.isGroupedData ? false : true;
        if (!this.isGroupedData) {
            this.checkObject()
        }
        if (m == "cor") {
            this.isBoxPlotCalc = false;
            if (this.correlationAxis == "samples") {
                var q = this.isGroupedData ? this.grpIndices : this.smpIndices;
                for (var f = 0; f < q.length; f++) {
                    var r = q[f];
                    var n = this.getDataForSmpGrpAtIndex(r);
                    if (!b.cor[r]) {
                        b.cor[r] = []
                    }
                    for (var e = f; e < q.length; e++) {
                        var h = q[e];
                        if (!b.cor[h]) {
                            b.cor[h] = []
                        }
                        var l = this.getDataForSmpGrpAtIndex(h);
                        if (f == e) {
                            if (this.isTransformedData) {
                                b.trans[r][h] = 1
                            }
                            b.cor[r][h] = 1
                        } else {
                            b.cor[r][h] = this.correlation(n, l);
                            b.cor[h][r] = b.cor[r][h];
                            if (this.isTransformedData) {
                                b.trans[r][h] = b.cor[r][h];
                                b.trans[h][r] = b.cor[h][r]
                            }
                        }
                    }
                }
            } else {
                for (var f = 0; f < this.varIndices.length; f++) {
                    var r = this.varIndices[f];
                    if (!b.cor[r]) {
                        b.cor[r] = []
                    }
                    for (var e = f; e < this.varIndices.length; e++) {
                        var h = this.varIndices[e];
                        if (!b.cor[h]) {
                            b.cor[h] = []
                        }
                        if (f == e) {
                            b.cor[r][h] = 1;
                            if (this.isTransformedData) {
                                b.trans[r][h] = 1
                            }
                        } else {
                            b.cor[r][h] = this.correlation(b.data[r], b.data[h], d);
                            b.cor[h][r] = b.cor[r][h];
                            if (this.isTransformedData) {
                                b.trans[r][h] = b.cor[r][h];
                                b.trans[h][r] = b.cor[h][r]
                            }
                        }
                    }
                }
            }
        } else {
            this.isBoxPlotCalc = m == "iqr" ? true : false;
            for (var f = 0; f < this.varIndices.length; f++) {
                var g;
                var r = this.varIndices[f];
                if (!b.mean[r]) {
                    b.trans[r] = [];
                    b.mean[r] = [];
                    b.median[r] = [];
                    b.stdev[r] = [];
                    b.qtl1[r] = [];
                    b.qtl3[r] = [];
                    b.iqr1[r] = [];
                    b.iqr3[r] = [];
                    b.out[r] = [];
                    b.n[r] = [];
                    b.sum[r] = []
                }
                if (this.isGroupedData) {
                    g = p[r]
                } else {
                    b.grps = [];
                    g = [];
                    for (var e = 0; e < this.smpIndices.length; e++) {
                        var h = this.smpIndices[e];
                        b.grps[e] = [];
                        b.grps[e].push(h);
                        g[e] = [b.data[r][h]]
                    }
                }
                if (m == "iqr") {
                    this.summaryType = "iqr";
                    for (var e = 0; e < g.length; e++) {
                        var o = this.quantiles(g[e], d);
                        if (this.isTransformedData) {
                            b.trans[r][e] = o[1]
                        }
                        b.median[r][e] = o[1];
                        b.qtl1[r][e] = o[0];
                        b.qtl3[r][e] = o[2];
                        b.iqr1[r][e] = o[3];
                        b.iqr3[r][e] = o[4];
                        b.out[r][e] = o[5];
                        b.n[r][e] = o[6]
                    }
                } else {
                    if (m == "median") {
                        this.summaryType = "median";
                        for (var e = 0; e < g.length; e++) {
                            var o = this.median(g[e], d);
                            if (this.isTransformedData) {
                                b.trans[r][e] = o[1]
                            }
                            b.median[r][e] = o[0];
                            b.n[r][e] = o[1]
                        }
                    } else {
                        if (m == "mean") {
                            this.summaryType = "mean";
                            for (var e = 0; e < g.length; e++) {
                                var o = this.meanStandardDeviationN(g[e], d);
                                if (this.isTransformedData) {
                                    b.trans[r][e] = o[1]
                                }
                                b.mean[r][e] = o[0];
                                b.stdev[r][e] = o[1];
                                b.n[r][e] = o[2]
                            }
                        } else {
                            if (m == "sum") {
                                this.summaryType = "sum";
                                for (var e = 0; e < g.length; e++) {
                                    var k = this.sum(g[e], d);
                                    if (this.isTransformedData) {
                                        b.trans[r][e] = k
                                    }
                                    b.sum[r][e] = k;
                                    b.n[r][e] = g[e].length
                                }
                            }
                        }
                    }
                }
            }
        }
    };
    this.copySummarizedData = function () {
        var g = this.isGroupedData ? this.data.w : this.data.y;
        var f;
        for (var d = 0; d < this.varIndices.length; d++) {
            var e = this.varIndices[d];
            g.data[e] = [];
            g.trans[e] = [];
            if (this.summaryType == "mean") {
                for (var b = 0; b < g.mean[e].length; b++) {
                    g.data[e][b] = g.mean[e][b]
                }
            } else {
                if (this.summaryType == "median" || this.summaryType == "iqr") {
                    for (var b = 0; b < g.median[e].length; b++) {
                        g.data[e][b] = g.median[e][b]
                    }
                } else {
                    if (this.summaryType == "candle") {
                        for (var b = 0; b < g.close[e].length; b++) {
                            g.data[e][b] = g.close[e][b]
                        }
                    } else {
                        if (this.summaryType == "volume") {
                            for (var b = 0; b < g.volume[e].length; b++) {
                                g.data[e][b] = g.volume[e][b]
                            }
                        } else {
                            if (this.summaryType == "sum") {
                                for (var b = 0; b < g.volume[e].length; b++) {
                                    g.data[e][b] = g.sum[e][b]
                                }
                            }
                        }
                    }
                }
            }
            if (!f) {
                f = e
            }
        }
        return f
    };
    this.transformValue = function (d, e, b) {
        switch (d) {
        case "log2":
            return b ? Math.pow(2, e) : Math.log(e) / Math.LN2;
        case "log10":
            return b ? Math.pow(10, e) : Math.log(e) / Math.LN10;
        case "exp2":
            return b ? Math.log(e) / Math.LN2 : Math.pow(2, e);
        case "exp10":
            return b ? Math.log(e) / Math.LN10 : Math.pow(10, e);
        default:
            return e
        }
    };
    this.transform = function (e, d, b) {
        var f = 0;
        if (!this.isGroupedData) {
            this.checkObject()
        }
        if (!this.isRawData || this.isGroupedData) {
            f = this.copySummarizedData()
        }
        this.transformType = e;
        if (e == "reset" || e == "undo") {
            this.transformReset();
            this.transformType = false;
            this.isTransformedData = false
        } else {
            if (e == "save") {
                this.transformSave();
                this.transformType = false;
                this.isTransformedData = false
            } else {
                if (e == "log2" || e == "log10" || e == "exp2" || e == "exp10") {
                    this.transformLogExp(e);
                    this.isTransformedData = true
                } else {
                    if (e == "ratio" || e == "ratio2" || e == "ratio10") {
                        this.transformRatio(e, b);
                        this.isTransformedData = true
                    } else {
                        this.transformRelative(e, d, f);
                        this.isTransformedData = true
                    }
                }
            }
        }
    };
    this.transformReset = function () {
        var f = this.isGroupedData ? this.data.w : this.data.y;
        for (var d = 0; d < this.varIndices.length; d++) {
            var e = this.varIndices[d];
            for (var b = 0; b < f.data[e].length; b++) {
                if (f.trans[e] && !isNaN(f.data[e][b])) {
                    f.trans[e][b] = f.data[e][b]
                }
            }
        }
    };
    this.transformSave = function () {
        var f = this.isGroupedData ? this.data.w : this.data.y;
        for (var d = 0; d < this.varIndices.length; d++) {
            var e = this.varIndices[d];
            for (var b = 0; b < f.data[e].length; b++) {
                if (!isNaN(f.data[e][b])) {
                    f.data[e][b] = f.trans[e][b]
                }
            }
        }
    };
    this.transformLogExp = function (f) {
        var g = this.isGroupedData ? this.data.w : this.data.y;
        if (f == "log") {
            f = this.transformBase == 2 ? "type2" : "type10"
        } else {
            if (f == "exp") {
                f = this.transformBase == 2 ? "exp2" : "exp10"
            }
        }
        for (var d = 0; d < this.varIndices.length; d++) {
            var e = this.varIndices[d];
            if (!g.trans[e]) {
                g.trans[e] = []
            }
            for (var b = 0; b < g.data[e].length; b++) {
                if (!isNaN(g.data[e][b])) {
                    if (f == "log2") {
                        this.isLogData = true;
                        this.transformBase = 2;
                        g.trans[e][b] = Math.log(g.data[e][b]) / Math.LN2
                    } else {
                        if (f == "log10") {
                            this.isLogData = true;
                            this.transformBase = 10;
                            g.trans[e][b] = Math.log(g.data[e][b]) / Math.LN10
                        } else {
                            if (f == "exp2") {
                                this.isLogData = false;
                                this.transformBase = 2;
                                g.trans[e][b] = Math.pow(2, g.data[e][b])
                            } else {
                                if (f == "exp10") {
                                    this.isLogData = false;
                                    this.transformBase = 10;
                                    g.trans[e][b] = Math.pow(10, g.data[e][b])
                                }
                            }
                        }
                    }
                }
            }
        }
    };
    this.transformRatio = function (g, b) {
        var k = this.isGroupedData ? this.data.w : this.data.y;
        b = b ? b : this.ratioReference;
        for (var e = 0; e < this.varIndices.length; e++) {
            var f = this.varIndices[e];
            if (!k.trans[f]) {
                k.trans[f] = []
            }
            for (var d = 0; d < k.data[f].length; d++) {
                if (!isNaN(k.data[f][d]) && !isNaN(k.data[f][b])) {
                    if (this.isLogData) {
                        if (g == "ratio") {
                            g = this.transformBase == 2 ? "ratio2" : "ratio10"
                        }
                        var h;
                        if (k.data[f][d] >= k.data[f][b]) {
                            h = k.data[f][d] - k.data[f][b];
                            if (g == "ratio2") {
                                k.trans[f][d] = Math.pow(2, h)
                            } else {
                                if (g == "ratio10") {
                                    k.trans[f][d] = Math.pow(10, h)
                                }
                            }
                        } else {
                            h = k.data[f][b] / k.data[f][e];
                            if (g == "ratio2") {
                                k.trans[f][d] = Math.pow(2, h) * -1
                            } else {
                                k.trans[f][d] = Math.pow(10, h) * -1
                            }
                        }
                    } else {
                        if (k.data[f][d] >= k.data[f][b]) {
                            k.trans[f][d] = k.data[f][d] / k.data[f][b]
                        } else {
                            k.trans[f][d] = k.data[f][b] / k.data[f][d] * -1
                        }
                    }
                }
            }
        }
    };
    this.transformRelative = function (m, e, f) {
        var b = this.isGroupedData ? this.data.w : this.data.y;
        var d = this.isGroupedData ? false : true;
        e = e ? e : this.zscoreAxis;
        if (e == "samples") {
            for (var h = 0; h < b.data[f].length; h++) {
                var k = [];
                for (var g = 0; g < this.varIndices.length; g++) {
                    var l = this.varIndices[g];
                    k.push(b.data[l][h])
                }
                if (m == "percentile") {
                    var n = this.range(k, d)
                } else {
                    if (m == "zscore") {
                        var n = this.meanStandardDeviationN(k, d)
                    }
                }
                for (var g = 0; g < this.varIndices.length; g++) {
                    var l = this.varIndices[g];
                    if (!b.trans[l]) {
                        b.trans[l] = []
                    }
                    if (!isNaN(b.data[l][h])) {
                        if (m == "percentile") {
                            b.trans[l][h] = this.percentile(n[0], n[1], b.data[l][h])
                        } else {
                            if (m == "zscore") {
                                b.trans[l][h] = (b.data[l][h] - n[0]) / n[1]
                            }
                        }
                    }
                }
            }
        } else {
            for (var h = 0; h < this.varIndices.length; h++) {
                var o = this.varIndices[h];
                b.trans[o] = [];
                var k = b.data[o];
                if (m == "percentile") {
                    var n = this.range(k, d)
                } else {
                    if (m == "zscore") {
                        var n = this.meanStandardDeviationN(k, d)
                    }
                }
                for (var g = 0; g < k.length; g++) {
                    if (!isNaN(b.data[o][g])) {
                        if (m == "percentile") {
                            b.trans[o][g] = this.percentile(n[0], n[1], b.data[o][g])
                        } else {
                            if (m == "zscore") {
                                b.trans[o][g] = (b.data[o][g] - n[0]) / n[1]
                            }
                        }
                    }
                }
            }
        }
    };
    this.getGroupingFactors = function (b) {
        if (b) {
            return this.groupingFactors
        } else {
            return this.getObjectArray(this.groupingFactors)
        }
    };
    this.modifyGroupingFactors = function (d, b) {
        this.modifyObjectArray(d, b, "groupingFactors")
    };
    this.segregateSamplesVariables = function (l, k, g, m) {
        delete(this.data.l);
        this.deleteSubGraphAttributes();
        this.data.l = {};
        this.data.l.smps = l;
        this.data.l.vars = k;
        this.data.l.weight = [];
        this.data.l.type = [];
        this.data.l.name = [];
        this.data.l.desc = [];
        var h = 1 / l.length;
        var e = 1 / k.length;
        var f = this.data.y.desc[0] ? this.data.y.desc[0] : "";
        for (var d = 0; d < k.length; d++) {
            for (var b = 0; b < l.length; b++) {
                this.data.l.weight.push([e, h]);
                this.data.l.type.push(this.graphType);
                this.data.l.name.push([m[d], g[b]]);
                this.data.l.desc.push(f)
            }
        }
        this.layoutComb = true;
        this.layout = l.length + "X" + k.length;
        this.isValidLayout()
    };
    this.desegregateSamples = function () {
        this.segregateSamplesBy = false;
        this.showAnimation = this.showAnimationTemp;
        this.resetLayout();
        if (this.segregateVariablesBy) {
            this.segregateVariables(this.segregateVariablesBy)
        } else {
            this.isValidLayout()
        }
    };
    this.segregateSamples = function (k) {
        if (this.data.x && this.data.x[k]) {
            delete(this.layoutParams);
            var d = {};
            var j = 0;
            var g = [];
            var h = [];
            for (var e = 0; e < this.data.x[k].length; e++) {
                var f = this.data.x[k][e];
                if (!d.hasOwnProperty(f)) {
                    d[f] = [];
                    j++
                }
                d[f].push(this.data.y.smps[e])
            }
            var b = 1 / j;
            if (this.segregateVariablesBy) {
                vars = this.data.l.vars;
                for (var f in d) {
                    g.push(f);
                    h.push(d[f])
                }
                this.segregateSamplesBy = k;
                this.segregateSamplesVariables(h, this.data.l.vars, g, this.data.l.name)
            } else {
                delete(this.data.l);
                this.deleteSubGraphAttributes();
                this.data.l = {};
                this.data.l.smps = [];
                this.data.l.weight = [];
                this.data.l.type = [];
                this.data.l.name = [];
                this.data.l.desc = [];
                for (var f in d) {
                    this.data.l.smps.push(d[f]);
                    this.data.l.weight.push(b);
                    this.data.l.type.push(this.graphType);
                    this.data.l.name.push(f);
                    this.data.l.desc.push(this.data.y.desc && this.data.y.desc[0] ? this.data.y.desc[0] : "")
                }
                this.segregateSamplesBy = k;
                this.layoutComb = true;
                this.showAnimationTemp = this.showAnimation;
                this.showAnimation = false;
                this.layout = "1X" + this.data.l.smps.length;
                this.isValidLayout()
            }
        }
    };
    this.desegregateVariables = function () {
        this.segregateVariablesBy = false;
        this.showAnimation = this.showAnimationTemp;
        this.resetLayout();
        if (this.segregateSamplesBy) {
            this.segregateSamples(this.segregateSamplesBy)
        } else {
            this.isValidLayout()
        }
    };
    this.segregateVariables = function (b) {
        if (this.data.z && this.data.z[b]) {
            delete(this.layoutParams);
            var e = {};
            var m = 0;
            var k = [];
            var j = [];
            for (var f = 0; f < this.data.z[b].length; f++) {
                var h = this.data.z[b][f];
                if (!e.hasOwnProperty(h)) {
                    e[h] = [];
                    m++
                }
                e[h].push(this.data.y.vars[f])
            }
            var d = 1 / m;
            if (this.segregateSamplesBy) {
                for (var h in e) {
                    j.push(h);
                    k.push(e[h])
                }
                this.segregateVariablesBy = b;
                this.segregateSamplesVariables(this.data.l.smps, k, this.data.l.name, j)
            } else {
                delete(this.data.l);
                this.deleteSubGraphAttributes();
                this.data.l = {};
                this.data.l.vars = [];
                this.data.l.weight = [];
                this.data.l.type = [];
                this.data.l.name = [];
                this.data.l.desc = [];
                for (var h in e) {
                    this.data.l.vars.push(e[h]);
                    this.data.l.weight.push(d);
                    this.data.l.type.push(this.graphType);
                    this.data.l.name.push(h);
                    this.data.l.desc.push(this.data.y.desc && this.data.y.desc[0] ? this.data.y.desc[0] : "")
                }
                this.segregateVariablesBy = b;
                this.layoutComb = true;
                this.showAnimationTemp = this.showAnimation;
                this.showAnimation = false;
                this.layout = "1X" + this.data.l.vars.length;
                this.isValidLayout()
            }
        }
    };
    this.deleteSubGraphAttributes = function () {
        if (this.layoutRows && this.layoutCols) {
            var e = 0;
            for (var d = 0; d < this.layoutRows; d++) {
                for (var b = 0; b < this.layoutCols; b++) {
                    delete(this["subGraphWeight" + e]);
                    delete(this["subGraphType" + e]);
                    delete(this["subGraphSummaryType" + e]);
                    delete(this["subGraphTransformType" + e]);
                    e++
                }
            }
        }
    };
    this.unsetAllVsAll = function () {
        if (this.graphType.match(/Scatter/)) {
            this.xAxis = this.xAxisTemp;
            this.xAxisTmp = [];
            this.yAxis = this.yAxisTemp;
            this.yAxisTmp = [];
            this.zAxis = this.zAxisTemp;
            this.zAxisTmp = [];
            this.allVsAll = false;
            this.showAnimation = this.showAnimationTemp;
            this.resetLayout();
            this.isValidLayout()
        }
    };
    this.setAllVsAll = function () {
        if (this.graphType.match(/Scatter/)) {
            var f = this.data.y.data[0].length;
            var b = 1 / f;
            this.xAxisTemp = this.xAxisTemp || this.xAxis;
            this.xAxis = [];
            this.yAxisTemp = this.yAxisTemp || this.yAxis;
            this.yAxis = [];
            this.zAxisTemp = this.zAxisTemp || this.zAxis;
            this.zAxis = [];
            this.data.l = {};
            this.data.l.smps = [];
            this.data.l.weight = [];
            this.data.l.type = [];
            this.data.l.desc = [];
            for (var e = 0; e < f; e++) {
                for (var d = 0; d < f; d++) {
                    this.data.l.weight.push(b);
                    this.data.l.desc.push(this.data.y.desc && this.data.y.desc[0] ? this.data.y.desc[0] : "");
                    this.data.l.type.push(this.graphType);
                    if (e == d) {
                        this.data.l.smps.push({})
                    } else {
                        if (this.allVsAllType == "upper") {
                            if (d > e) {
                                this.data.l.smps.push({
                                    xAxis: [this.data.y.smps[d]],
                                    yAxis: [this.data.y.smps[e]]
                                })
                            } else {
                                this.data.l.smps.push({})
                            }
                        } else {
                            if (this.allVsAllType == "lower") {
                                if (e > d) {
                                    this.data.l.smps.push({
                                        xAxis: [this.data.y.smps[d]],
                                        yAxis: [this.data.y.smps[e]]
                                    })
                                } else {
                                    this.data.l.smps.push({})
                                }
                            } else {
                                this.data.l.smps.push({
                                    xAxis: [this.data.y.smps[d]],
                                    yAxis: [this.data.y.smps[e]]
                                })
                            }
                        }
                    }
                }
            }
            this.layoutComb = true;
            this.showAnimationTemp = this.showAnimation;
            this.showAnimation = false;
            this.layout = f + "X" + f;
            this.isValidLayout()
        }
    };
    this.setMultiplePies = function () {
        if (this.graphType.match(/Pie/)) {
            var g, j;
            var h = this.xAxis.length;
            if (this.layout) {
                var d = this.layout.split(/X/i);
                g = parseInt(d[0]);
                j = parseInt(d[1]);
                if (g * j < h) {
                    g = Math.ceil(Math.sqrt(h));
                    j = Math.floor(Math.sqrt(h));
                    this.layout = g + "X" + j
                }
            } else {
                g = Math.ceil(Math.sqrt(h));
                j = Math.floor(Math.sqrt(h));
                this.layout = g + "X" + j
            }
            var b = 1 / g;
            var f = 1 / j;
            delete(this.layoutParams);
            delete(this.data.l);
            this.deleteSubGraphAttributes();
            this.data.l = {};
            this.data.l.smps = [];
            this.data.l.weight = [];
            this.data.l.type = [];
            this.data.l.desc = [];
            for (var e = 0; e < h; e++) {
                this.data.l.smps.push([this.xAxis[e]]);
                this.data.l.weight.push([f, b]);
                this.data.l.desc.push(this.data.y.desc && this.data.y.desc[0] ? this.data.y.desc[0] : "");
                this.data.l.type.push(this.graphType)
            }
            this.layoutComb = true;
            this.showAnimationTemp = this.showAnimation;
            this.showAnimation = false;
            this.isValidLayout()
        }
    };
    this.ungroupSamples = function () {
        this.isGroupedData = false;
        this.groupingFactors = [];
        this.grpIndices = [];
        this.hiddenGrps = [];
        this.setMaxSmpStringName();
        return
    };
    this.createHistogram = function (e) {
        var o, l, k, q, r;
        var d = 0;
        var p = [];
        var b = [];
        var n = [];
        var h = {};
        h.y = {};
        h.y.data = [];
        h.y.vars = [];
        h.y.smps = [];
        if (this.graphType.match(/Scatter/)) {
            if (this.type2D == "XYZ") {
                o = e == "y" ? this.yAxisIndices[this.yAxisCurrent] : this.xAxisIndices[this.xAxisCurrent];
                p.push(o)
            } else {
                if (this.type2D == "XY") {
                    for (var g = 0; g < this.xAxisIndices.length; g++) {
                        o = e == "y" ? this.yAxisIndices[g] : this.xAxisIndices[g];
                        p.push(o)
                    }
                } else {
                    if (this.type2D == "X") {
                        if (e == "y") {
                            p.push(this.yAxisIndices[this.yAxisCurrent])
                        } else {
                            for (var g = 0; g < this.xAxisIndices.length; g++) {
                                p.push(this.xAxisIndices[g])
                            }
                        }
                    } else {
                        if (e == "y") {
                            for (var g = 0; g < this.yAxisIndices.length; g++) {
                                p.push(this.yAxisIndices[g])
                            }
                        } else {
                            p.push(this.xAxisIndices[this.xAxisCurrent])
                        }
                    }
                }
            }
            for (var g = 0; g < this.histogramBins; g++) {
                h.y.vars.push("Bin" + (g + 1));
                h.y.data[g] = []
            }
            for (var g = 0; g < p.length; g++) {
                h.y.smps.push(this.data.y.smps[p[g]]);
                h.y.smps.push(this.data.y.smps[p[g]] + " counts");
                b.push(this.data.y.smps[p[g]]);
                n.push(this.data.y.smps[p[g]] + " counts");
                var m = [];
                for (var f = 0; f < this.histogramBins; f++) {
                    m[f] = 0
                }
                l = this.getDataForSmpGrpAtIndex(p[g]);
                k = this.range(l);
                q = ((k[1] - k[0]) / this.histogramBins) + (k[0] / (this.histogramBins * 2));
                for (var f = 0; f < l.length; f++) {
                    r = Math.floor((l[f] - k[0]) / q);
                    m[r]++
                }
                for (var f = 0; f < this.histogramBins; f++) {
                    h.y.data[f][d] = q * (f + 1)
                }
                d++;
                for (var f = 0; f < this.histogramBins; f++) {
                    h.y.data[f][d] = m[f]
                }
                d++
            }
            this.isCreateHistogram = true;
            this.isHistogram = true;
            this.histogramBarWidthStndBy = this.histogramBarWidth;
            this.dataStndBy = this.data;
            this.data = {};
            this.resetConfig(["graphType", "isHistogram"]);
            this.xAxis = b;
            this.yAxis = n;
            this.showLegend = false;
            this.histogramBarWidth = 1.5;
            this.initializeData(h)
        }
    };
    this.removeHistogram = function () {
        this.isCreateHistogram = false;
        this.histogramBarWidth = this.histogramBarWidthStndBy;
        this.switchData()
    };
    this.addRegressionLine = function () {
        var d, j, f, b, k, h, e;
        if (this.graphType.match(/Scatter/)) {
            if (this.type2D == "XYZ") {
                d = this.xAxisIndices[this.xAxisCurrent];
                j = this.yAxisIndices[this.yAxisCurrent];
                h = this.data.y.smps[d] + " vs " + this.data.y.smps[j];
                f = this.getDataForSmpGrpAtIndex(d);
                b = this.getDataForSmpGrpAtIndex(j);
                k = this.regression(f, b);
                this.addRegressionDecoration(k, h, this.foreground)
            } else {
                if (this.type2D == "XY") {
                    for (var g = 0; g < this.xAxisIndices.length; g++) {
                        e = g % this.colors.length;
                        d = this.xAxisIndices[g];
                        j = this.yAxisIndices[g];
                        h = this.data.y.smps[d] + " vs " + this.data.y.smps[j];
                        f = this.getDataForSmpGrpAtIndex(d);
                        b = this.getDataForSmpGrpAtIndex(j);
                        k = this.regression(f, b);
                        this.addRegressionDecoration(k, h, this.colors[e])
                    }
                } else {
                    if (this.type2D == "X") {
                        j = this.yAxisIndices[this.yAxisCurrent];
                        b = this.getDataForSmpGrpAtIndex(j);
                        for (var g = 0; g < this.xAxisIndices.length; g++) {
                            e = g % this.colors.length;
                            d = this.xAxisIndices[g];
                            h = this.data.y.smps[d] + " vs " + this.data.y.smps[j];
                            f = this.getDataForSmpGrpAtIndex(d);
                            k = this.regression(f, b);
                            this.addRegressionDecoration(k, h, this.colors[e])
                        }
                    } else {
                        d = this.xAxisIndices[this.xAxisCurrent];
                        f = this.getDataForSmpGrpAtIndex(d);
                        for (var g = 0; g < this.yAxisIndices.length; g++) {
                            e = g % this.colors.length;
                            j = this.yAxisIndices[g];
                            b = this.getDataForSmpGrpAtIndex(j);
                            h = this.data.y.smps[d] + " vs " + this.data.y.smps[j];
                            k = this.regression(f, b);
                            this.addRegressionDecoration(k, h, this.colors[e])
                        }
                    }
                }
            }
        }
    };
    this.addRegressionDecoration = function (e, d, b) {
        this.showDecorations = true;
        if (!this.data.d) {
            this.data.d = {}
        }
        if (!this.data.d.reg) {
            this.data.d.reg = []
        }
        this.data.d.reg.push({
            slope: e[0],
            intercept: e[1],
            cor: e[2],
            label: d,
            color: b
        })
    };
    this.addNormalDistributionLine = function (d) {
        var p, h, n, j, k, l, m, g, e, g, b, o;
        if (this.graphType.match(/Scatter/)) {
            if (this.type2D == "XYZ") {
                n = d == "y" ? this.yAxisIndices[this.yAxisCurrent] : this.xAxisIndices[this.xAxisCurrent];
                j = d == "y" ? this.xAxisIndices[this.xAxisCurrent] : this.yAxisIndices[this.yAxisCurrent];
                k = this.getDataForSmpGrpAtIndex(n);
                l = this.getDataForSmpGrpAtIndex(j);
                m = this.max(l);
                g = this.data.y.smps[n];
                e = this.meanStandardDeviationN(k);
                this.addNormalDistributionDecoration(e, m, g, this.foreground, d)
            } else {
                p = d == "y" ? this.yAxisIndices : this.xAxisIndices;
                h = d == "y" ? this.xAxisIndices : this.yAxisIndices;
                o = d == "y" ? this.xAxisCurrent : this.yAxisCurrent;
                for (var f = 0; f < p.length; f++) {
                    b = f % this.colors.length;
                    n = p[f];
                    j = h[f] ? h[f] : h[o];
                    k = this.getDataForSmpGrpAtIndex(n);
                    l = this.getDataForSmpGrpAtIndex(j);
                    m = this.max(l);
                    g = this.data.y.smps[n];
                    e = this.meanStandardDeviationN(k);
                    this.addNormalDistributionDecoration(e, m, g, this.colors[b], d)
                }
            }
        }
    };
    this.addNormalDistributionDecoration = function (g, b, e, d, f) {
        this.showDecorations = true;
        if (!this.data.d) {
            this.data.d = {}
        }
        if (!this.data.d.nor) {
            this.data.d.nor = []
        }
        if (f && f == "y") {
            this.data.d.nor.push({
                mu: g[0],
                sigma: g[1],
                yAxis: true,
                max: b,
                label: e,
                color: d
            })
        } else {
            this.data.d.nor.push({
                mu: g[0],
                sigma: g[1],
                max: b,
                label: e,
                color: d
            })
        }
    };
    this.createRandomData = function () {
        var f = {};
        f.y = {};
        f.y.data = [];
        f.y.vars = [];
        f.y.smps = [];
        for (var d = 0; d < this.randomDataSamples; d++) {
            f.y.smps[d] = "Sample" + (d + 1)
        }
        for (var d = 0; d < this.randomDataVariables; d++) {
            f.y.data[d] = [];
            f.y.vars[d] = "Variable" + (d + 1);
            for (var b = 0; b < this.randomDataSamples; b++) {
                f.y.data[d].push(Math.floor(((Math.random() * 2 - 1) + (Math.random() * 2 - 1) + (Math.random() * 2 - 1)) * this.randomDataSigma + this.randomDataMean))
            }
        }
        f.x = {};
        var e = Math.floor(this.randomDataSamples / this.randomDataSampleAnnotationRatio);
        for (var d = 0; d < this.randomDataSampleAnnotations; d++) {
            f.x["Factor" + (d + 1)] = [];
            for (var b = 0; b < this.randomDataSamples; b++) {
                f.x["Factor" + (d + 1)][b] = "Level" + (Math.floor(Math.random() * e) + 1)
            }
        }
        f.z = {};
        var e = Math.floor(this.randomDataVariables / this.randomDataVariableAnnotationRatio);
        for (var d = 0; d < this.randomDataVariableAnnotations; d++) {
            f.z["Annt" + (d + 1)] = [];
            for (var b = 0; b < this.randomDataVariables; b++) {
                f.z["Annt" + (d + 1)][b] = "Desc" + (Math.floor(Math.random() * e) + 1)
            }
        }
        if (this.data && !this.dataStndBy) {
            this.dataStndBy = this.data;
            this.data = {}
        }
        this.resetConfig(["graphType"]);
        this.initializeData(f)
    };
    this.restoreRandomData = function () {
        this.switchData()
    };
    this.groupSamples = function (f, q) {
        if (f.length < 1) {
            return this.ungroupSamples()
        }
        for (var l = 0; l < f.length; l++) {
            if (!f || !this.data.x[f[l]]) {
                return this.ungroupSamples()
            }
        }
        if (this.layoutComb && this.data.l.comp) {
            this.varIndices = [];
            for (var l = 0; l < this.data.l.comp.length; l++) {
                for (var h = 0; h < this.data.l.comp[l].length; h++) {
                    this.varIndices.push(this.data.l.comp[l][h])
                }
            }
        }
        this.resetIndices();
        this.isGroupedData = true;
        this.groupingFactors = f;
        this.resetObject(true);
        this.hiddenGrps = [];
        var w = {};
        var b = [];
        var e = 0;
        for (var l = 0; l < this.smpIndices.length; l++) {
            var s = this.smpIndices[l];
            var v = [];
            var r;
            for (var h = 0; h < f.length; h++) {
                var m = f[h];
                r = this.data.x[m][s];
                if (!r) {
                    r = "NA"
                }
                v.push(r)
            }
            r = v.join(" - ");
            if (!w.hasOwnProperty(r)) {
                this.data.w.smps.push(r);
                b[e] = r;
                w[r] = [];
                w[r].push(s);
                e++
            } else {
                w[r].push(s)
            }
        }
        for (var l = 0; l < b.length; l++) {
            var r = b[l];
            this.data.w.grps.push(w[r])
        }
        this.grpIndices = [];
        for (var l = 0; l < this.data.w.grps.length; l++) {
            this.grpIndices.push(l);
            this.hiddenGrps.push(false)
        }
        for (var l = 0; l < this.varIndices.length; l++) {
            this.data.w.vars.push(this.data.y.vars[this.varIndices[l]])
        }
        var n = [];
        if (q) {
            if (this.isArray(q)) {
                n = q
            } else {
                n.push(q)
            }
        } else {
            q = this.graphType == "Boxplot" ? "iqr" : "mean";
            n.push(q)
        }
        for (var u = 0; u < n.length; u++) {
            var p = [];
            var o = n[u];
            if (this.isTransformedData) {
                a = this.data.y.trans
            } else {
                if (this.isRawData) {
                    a = this.data.y.data
                } else {
                    if (this.summaryType == "median" || this.summaryType == "iqr") {
                        a = this.data.y.median
                    } else {
                        if (this.summaryType == "mean") {
                            a = this.data.y.mean
                        } else {
                            if (this.summaryType == "cor") {
                                a = this.data.y.cor
                            } else {
                                if (this.summaryType == "sum") {
                                    a = this.data.y.sum
                                } else {
                                    a = this.data.y[this.summaryType]
                                }
                            }
                        }
                    }
                }
            }
            for (var l = 0; l < this.varIndices.length; l++) {
                var s = this.varIndices[l];
                p[s] = [];
                for (var h = 0; h < this.data.w.grps.length; h++) {
                    p[s][h] = [];
                    for (var g = 0; g < this.data.w.grps[h].length; g++) {
                        var d = this.data.w.grps[h][g];
                        p[s][h].push(a[s][d])
                    }
                }
            }
            this.summarize(o, p)
        }
        this.setMaxVarStringName();
        this.setMaxSmpStringName();
        this.xAxisValues = [];
        this.xAxis2Values = [];
        this.yAxisValues = [];
        this.zAxisValues = [];
        if (!this.layoutComb || !this.layoutAdjust) {
            if (typeof (this.setAxes) == "function") {
                this.setAxes()
            }
        }
    };
    this.initializeDataAttributes = function () {
        this.setIndices();
        if (this.graphType != "Network" && this.graphType != "Genome" && this.graphType != "Venn") {
            this.setMaxVarStringAttr();
            this.setMaxSmpStringAttr()
        }
    };
    this.initializeData = function (b) {
        if (a && !b) {
            this.isExample = false;
            this.data = a;
            delete a
        } else {
            if (b) {
                this.isExample = false;
                this.data = b;
                delete b
            } else {
                if (!this.remoteService) {
                    this.isExample = true
                }
                this.data = this.exampleRawData
            }
        }
        this.validGraphTypes = [];
        if (this.data.y || this.data.venn || this.data.nodes || this.data.tracks || this.data.market) {
            if (this.data.market) {
                this.setMarketData();
                this.summaryType = "candle";
                this.validGraphTypes.push("Candlestick")
            }
            if (this.data.venn) {
                this.summaryType = "venn";
                this.validGraphTypes.push("Venn")
            }
            if (this.data.nodes) {
                this.summaryType = "network";
                this.validGraphTypes.push("Network")
            }
            if (this.data.tracks) {
                this.summaryType = "genome";
                this.validGraphTypes.push("Genome")
            }
            if (this.data.y) {
                if (!this.data.y.data && !this.data.y.mean && !this.data.y.median && !this.data.y.cor && !this.data.y.sum && !this.data.y.close) {
                    alert("Dude! there is not a valid data structure");
                    return
                } else {
                    if (this.data.y.data) {
                        this.isRawData = true;
                        this.summaryType = "raw"
                    } else {
                        this.isRawData = false;
                        if (this.data.y.iqr1 && this.data.y.qtl1 && this.data.y.median && this.data.y.qtl3 && this.data.y.iqr3) {
                            this.summaryType = "iqr"
                        } else {
                            if (this.data.y.median) {
                                this.summaryType = "median"
                            } else {
                                if (this.data.y.mean) {
                                    this.summaryType = "mean"
                                } else {
                                    if (this.data.y.cor) {
                                        this.summaryType = "cor"
                                    } else {
                                        if (this.data.y.sum) {
                                            this.summaryType = "sum"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                this.validGraphTypes.push("Bar");
                this.validGraphTypes.push("Line");
                this.validGraphTypes.push("Area");
                this.validGraphTypes.push("BarLine");
                this.validGraphTypes.push("Boxplot");
                this.validGraphTypes.push("Dotplot");
                this.validGraphTypes.push("Heatmap");
                this.validGraphTypes.push("Stacked");
                this.validGraphTypes.push("StackedPercent");
                this.validGraphTypes.push("Pie");
                this.validGraphTypes.push("Correlation");
                if (this.data.y.smps) {
                    if (this.data.y.smps.length > 2) {
                        this.validGraphTypes.push("Scatter2D");
                        this.validGraphTypes.push("ScatterBubble2D");
                        this.validGraphTypes.push("Scatter3D")
                    } else {
                        if (this.data.y.smps.length > 1) {
                            this.validGraphTypes.push("Scatter2D")
                        }
                    }
                }
            }
            this.validGraphTypes.sort()
        }
        this.initializeDataAttributes()
    };
    this.setMarketData = function () {
        if (this.data.market && !this.isMarketDataFormated) {
            var e = this.data.market;
            this.data.market = {};
            this.data.market.smps = [];
            this.data.market.vars = [];
            this.data.market.close = [];
            this.data.market.open = [];
            this.data.market.high = [];
            this.data.market.low = [];
            this.data.market.volume = [];
            this.data.market.desc = ["Price", "Vol"];
            var l = {};
            var h = 0;
            for (var g = 0; g < e.length; g++) {
                this.data.market.vars.push(e[g].symbol);
                this.data.market.close[g] = [];
                this.data.market.open[g] = [];
                this.data.market.high[g] = [];
                this.data.market.low[g] = [];
                this.data.market.volume[g] = [];
                var k = e[g].data;
                for (var f = 0; f < k.length; f++) {
                    var m = this.parseDate(k[f][0]);
                    var b = 0;
                    if (!l.hasOwnProperty(m)) {
                        b = h;
                        l[m] = h;
                        this.data.market.smps.push(m);
                        h++
                    } else {
                        b = l[m]
                    }
                    this.data.market.open[g][b] = k[f][1];
                    this.data.market.high[g][b] = k[f][2];
                    this.data.market.low[g][b] = k[f][3];
                    this.data.market.close[g][b] = k[f][4];
                    this.data.market.volume[g][b] = k[f][5]
                }
            }
        }
        this.isMarketDataFormated = true
    };
    this.switchToMarketData = function () {
        var b = this.data.market;
        this.dataStndBy = this.data;
        this.data = {};
        this.data.y = b;
        this.initializeDataAttributes();
        if (this.showVolume) {
            this.deleteSubGraphAttributes();
            this.data.l = {};
            this.data.l.vars = [this.data.y.vars, this.data.y.vars];
            this.data.l.weight = this.graphOrientation == "vertical" ? [0.75, 0.25] : [0.25, 0.75];
            this.data.l.type = ["Candlestick", "Candlestick"];
            this.data.l.name = this.graphOrientation == "vertical" ? ["Price", "Volume"] : ["Volume", "Price"];
            this.data.l.desc = this.graphOrientation == "vertical" ? ["Price", "Volume"] : ["Volume", "Price"];
            this.segregateSamplesBy = false;
            this.segregateVariablesBy = false;
            this.layoutComb = true;
            this.layout = "1X2";
            this.isValidLayout()
        }
    };
    this.switchFromMarketData = function () {
        this.switchData();
        if (this.showVolume) {
            this.resetLayout();
            this.isValidLayout()
        }
    };
    this.switchData = function () {
        if (this.dataStndBy) {
            this.initConfig();
            this.data = this.dataStndBy;
            this.initializeData(this.data)
        }
    };
    this.initializeData()
};
CanvasXpress.prototype.initRemote = function () {
    this.ajaxRequest = function (url, callback, format, method) {
        format = format == "json" ? "json" : "text";
        method = method == "POST" ? "POST" : "GET";
        var http = false;
        if (window.XMLHttpRequest) {
            try {
                http = new XMLHttpRequest()
            } catch (e) {
                http = false
            }
        } else {
            if (typeof ActiveXObject != "undefined") {
                try {
                    http = new ActiveXObject("Msxml2.XMLHTTP")
                } catch (e) {
                    try {
                        http = new ActiveXObject("Microsoft.XMLHTTP")
                    } catch (E) {
                        http = false
                    }
                }
            }
        }
        if (http && url && callback) {
            var now = "uid=" + new Date().getTime();
            url += (url.indexOf("?") + 1) ? "&" : "?";
            url += now;
            http.open(method, url, true);
            if (method == "GET") {
                if (http.overrideMimeType) {
                    http.overrideMimeType("text/xml")
                }
            } else {
                var parameters = false;
                var parts = url.split("?");
                url = parts[0];
                parameters = parts[1];
                http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
                http.setRequestHeader("Content-length", parameters.length);
                http.setRequestHeader("Connection", "close")
            }
            http.onreadystatechange = function () {
                if (http.readyState == 4) {
                    if (http.status == 200) {
                        var result = "";
                        if (http.responseText) {
                            result = http.responseText
                        }
                        if (format == "json") {
                            result = result.replace(/[\n\r]/g, "");
                            result = eval("(" + result + ")")
                        }
                        if (callback) {
                            callback(result)
                        }
                    } else {
                        if (error) {
                            error(http.status)
                        }
                    }
                }
            };
            http.send(parameters)
        }
    };
    this.requestRemoteData = function (ini) {
        var params = "index=" + this.remoteDataIndex + "&dir=" + this.remoteDirection;
        for (var p in this.remoteParams) {
            params += "&" + p + "=" + this.remoteParams[p]
        }
        if (ini) {
            params += "&records=true"
        }
        var url = this.remoteService + "?" + params;
        this.ajaxRequest(url, this.updateRemoteData, "json", "GET")
    };
    this.updateRemoteData = function (t) {
        return function (res) {
            if (res && res.data) {
                var d = res.data;
                var a = t.remoteDirection == "next" ? true : false;
                var w = document.getElementById("wrapper-" + t.target);
                var p = w.parentNode;
                var r = w.getClientRects();
                var o = document.getElementById(t.target);
                t.insertTarget(d.renderTo, w, o.width, o.height, a);
                d.hidden = true;
                new CanvasXpress(d);
                t.addSelectOptionsRemoteData(p, res.ids);
                t.updateSelectOptionsRemoteData(p);
                t.resetInfoSpan(false, true);
                t.animateTransition(p, r[0]);
                t.target = d.renderTo
            } else {
                t.remoteDataIndex = t.remoteDirection == "next" ? t.remoteDataIndex - 1 : t.remoteDataIndex + 1;
                t.remoteUpdating = false
            }
        }
    }(this);
    this.addSelectOptionsRemoteData = function (p, ids) {
        if (ids) {
            var s = p.parentNode.firstChild.lastChild;
            for (var i = 0; i < ids.length; i++) {
                var opt = document.createElement("option");
                opt.text = ids[i];
                opt.value = i;
                if (this.remoteDataIndex == i) {
                    opt.selected = "selected"
                }
                try {
                    s.add(opt, null)
                } catch (ex) {
                    s.add(opt)
                }
            }
        }
    };
    this.updateSelectOptionsRemoteData = function (p) {
        var s = p.parentNode.firstChild.lastChild;
        if (s) {
            s.selectedIndex = this.remoteDataIndex
        }
    };
    this.animateTransition = function (b, r) {
        if (b && r) {
            var that = this;
            var step = 15;
            var s = 0;
            var c1 = b.firstChild;
            var c2 = b.lastChild;
            var w = r.width;
            var h = r.height;
            b.parentNode.style.overflow = "hidden";
            if (this.isIE) {
                if (this.remoteDirection == "next") {
                    b.removeChild(c1)
                } else {
                    b.removeChild(c2)
                }
                b.style.overflow = "auto";
                b.parentNode.style.overflow = "visible";
                this.remoteUpdating = false
            } else {
                var updater = function () {
                        this.update = function () {
                            var n = Math.atan((s / step) * 10 - 5) * 0.5 / Math.atan(5) + 0.5;
                            var l = w * n;
                            if (s >= step) {
                                clearInterval(clearUpdateInt);
                                if (that.remoteDirection == "next") {
                                    b.removeChild(c1);
                                    c2.style.left = 0 + "px"
                                } else {
                                    b.removeChild(c2);
                                    c1.style.left = 0 + "px"
                                }
                                b.style.overflow = "auto";
                                b.parentNode.style.overflow = "visible";
                                that.remoteUpdating = false
                            } else {
                                if (that.remoteDirection == "next") {
                                    c1.style.left = (l * -1) + "px";
                                    c2.style.left = (l * -1) + "px"
                                } else {
                                    c1.style.left = (-w + l) + "px";
                                    c2.style.left = (-w + l) + "px"
                                }
                                c1.style.display = "block";
                                c2.style.display = "block"
                            }
                            s++
                        };
                        var clearUpdateInt = setInterval(this.update, 100)
                    };
                updater.call()
            }
        }
    };
    this.handleRemoteSelect = function (t) {
        return function (e) {
            var i = t.isIE ? e.srcElement.selectedIndex : e.target.selectedIndex;
            t.remoteDirection = i > t.remoteDataIndex ? "next" : "previous";
            t.remoteDataIndex = i;
            t.requestRemoteData()
        }
    }(this);
    this.handleRemoteClickStop = function (t) {
        return function (e) {
            t.remoteStop = true
        }
    }(this);
    this.handleRemoteClickPlay = function (t) {
        return function (e) {
            var s = t.isIE ? e.srcElement.parentNode.lastChild : e.target.parentNode.lastChild;
            t.remoteDirection = "next";
            t.remoteStop = false;
            t.playRemoteData(s)
        }
    }(this);
    this.playRemoteData = function (s) {
        var that = this;
        var updater = function () {
                this.update = function () {
                    if (!that.remoteUpdating) {
                        that.remoteUpdating = true;
                        that.showInfoSpan(false, "Updating ...", true);
                        if (s && s.tagName.match(/select/i) && that.remoteDataIndex >= s.length - 1) {
                            that.remoteDataIndex = 0
                        } else {
                            that.remoteDataIndex++
                        }
                        that.requestRemoteData()
                    }
                    if (that.remoteStop) {
                        clearInterval(cl)
                    }
                };
                var cl = setInterval(this.update, that.remoteAutoPlayDelay)
            };
        updater.call()
    };
    this.handleRemoteClickPrev = function (t) {
        return function (e) {
            if (!t.remoteUpdating) {
                var s = t.isIE ? e.srcElement.parentNode.lastChild : e.target.parentNode.lastChild;
                t.remoteUpdating = true;
                t.showInfoSpan(false, "Updating ...", true);
                t.remoteDirection = "previous";
                if (s && s.tagName.match(/select/i) && t.remoteDataIndex <= 0) {
                    t.remoteDataIndex = s.length - 1
                } else {
                    t.remoteDataIndex--
                }
                t.requestRemoteData()
            }
        }
    }(this);
    this.handleRemoteClickNext = function (t) {
        return function (e) {
            if (!t.remoteUpdating) {
                var s = t.isIE ? e.srcElement.parentNode.lastChild : e.target.parentNode.lastChild;
                t.remoteUpdating = true;
                t.showInfoSpan(false, "Updating ...", true);
                t.remoteDirection = "next";
                if (s && s.tagName.match(/select/i) && t.remoteDataIndex >= s.length - 1) {
                    t.remoteDataIndex = 0
                } else {
                    t.remoteDataIndex++
                }
                t.requestRemoteData()
            }
        }
    }(this);
    this.addRemoteNavigationTop = function (id, w) {
        var m = "3px";
        var n = document.createElement("div");
        var r = document.createElement("img");
        var p = document.createElement("img");
        var e = document.createElement("img");
        var f = document.createElement("img");
        var s = document.createElement("select");
        n.id = "canvasXpressRemoteWindowTopBar" + id;
        n.style.position = "absolute";
        n.style.marginLeft = w + "px";
        r.src = this.imageDir + this.previousButton;
        r.style.margin = m;
        this.addEvtListener(r, "click", this.handleRemoteClickPrev, false);
        n.appendChild(r);
        p.src = this.imageDir + this.playButton;
        p.style.margin = m;
        this.addEvtListener(p, "click", this.handleRemoteClickPlay, false);
        n.appendChild(p);
        e.src = this.imageDir + this.stopButton;
        e.style.margin = m;
        this.addEvtListener(e, "click", this.handleRemoteClickStop, false);
        n.appendChild(e);
        f.src = this.imageDir + this.nextButton;
        f.style.margin = m;
        this.addEvtListener(f, "click", this.handleRemoteClickNext, false);
        n.appendChild(f);
        s.style.position = "absolute";
        s.style.fontSize = "x-small";
        this.addEvtListener(s, "change", this.handleRemoteSelect, false);
        n.appendChild(s);
        return n
    };
    this.addRemoteNavigationLeftRight = function (d, id, w, h, m, q) {
        var n = document.createElement("div");
        var i = document.createElement("img");
        n.style.height = (h + q) + "px";
        n.style.width = m + "px";
        n.style.top = "0px";
        i.style.position = "absolute";
        i.style.top = (h / 2) + "px";
        i.style.left = "1px";
        if (d == "l") {
            n.style.position = "relative";
            n.id = "canvasXpressRemoteWindowPrevBar" + id;
            n.style.left = "0px";
            this.addEvtListener(n, "click", this.handleRemoteClickPrev, false);
            i.src = this.imageDir + this.previousButton
        } else {
            n.style.position = "absolute";
            n.id = "canvasXpressRemoteWindowNextBar" + id;
            n.style.left = (m + w) + "px";
            this.addEvtListener(n, "click", this.handleRemoteClickNext, false);
            i.src = this.imageDir + this.nextButton
        }
        i.style.margin = "auto";
        n.appendChild(i);
        return n
    };
    this.addRemoteWindow = function () {
        var w = document.getElementById("wrapper-" + this.target);
        var c = document.getElementById(this.target);
        var p = w.parentNode;
        var n = 0;
        var x = 18;
        var y = 20;
        var i = "canvasXpressRemoteWindow" + n;
        var e = document.getElementById(i);
        if (p.id && !p.id.match(/canvasXpressRemoteWindow/)) {
            while (e) {
                n++;
                i = "canvasXpressRemoteWindow" + n;
                e = document.getElementById(i)
            }
            var d = document.createElement("div");
            var t = this.addRemoteNavigationTop(n, x);
            var l = this.addRemoteNavigationLeftRight("l", n, c.width, c.height, x, y);
            var v = document.createElement("div");
            var r = this.addRemoteNavigationLeftRight("r", n, c.width, c.height, x, y);
            d.id = "canvasXpressRemoteWindow" + n;
            d.style.height = (y + c.height) + "px";
            d.style.width = ((x * 2) + c.width) + "px";
            d.style.position = "relative";
            v.id = "canvasXpressRemoteWindowBuffer" + n;
            v.style.height = c.height + "px";
            v.style.width = (c.width * 2) + "px";
            v.style.position = "absolute";
            v.style.left = x + "px";
            v.style.top = y + "px";
            v.style.overflow = "hidden";
            d.appendChild(t);
            d.appendChild(l);
            d.appendChild(v);
            d.appendChild(r);
            p.insertBefore(d, w);
            v.appendChild(w.parentNode.appendChild(w));
            this.requestRemoteData(true);
            this.initialRemoteDataRequest = true;
            if (this.remoteAutoPlay) {
                this.remoteUpdating = true;
                this.remoteDirection = "next";
                this.remoteStop = false;
                this.playRemoteData(t.lastChild)
            }
        } else {
            this.initialRemoteDataRequest = false
        }
    };
    this.initializeRemote = function () {
        if (this.remoteService) {
            this.addRemoteWindow()
        }
    };
    this.initializeRemote()
};
CanvasXpress.prototype.initGraph = function () {
    this.draw = function (b, d, a) {
        this.setUserEvents();
        this.initializeGraph(true, b, d, a)
    };
    this.isValidGraphType = function (a) {
        switch (a) {
        case "Bar":
        case "Line":
        case "Area":
        case "Dotplot":
        case "Heatmap":
        case "Stacked":
        case "StackedPercent":
        case "Boxplot":
        case "Correlation":
        case "Pie":
            if (this.data.y) {
                return true
            } else {
                return false
            }
        case "BarLine":
            if (this.data.y && this.data.a && this.data.a.xAxis && this.data.a.xAxis2) {
                return true
            } else {
                return false
            }
        case "Scatter2D":
            if (this.data.y && this.data.y.smps && this.data.y.smps.length > 1) {
                return true
            } else {
                return false
            }
        case "ScatterBubble2D":
        case "Scatter3D":
            if (this.data.y && this.data.y.smps && this.data.y.smps.length > 2) {
                return true
            } else {
                return false
            }
        case "Candlestick":
            if (this.data.market) {
                return true
            } else {
                return false
            }
        case "Venn":
            if (this.data.venn) {
                return true
            } else {
                return false
            }
        case "Network":
            if (this.data.nodes) {
                return true
            } else {
                return false
            }
        case "Genome":
            if (this.data.tracks) {
                return true
            } else {
                return false
            }
        }
        return false
    };
    this.setColorScheme = function () {
        switch (this.colorScheme) {
        case "basic":
            this.colors = this.colors0;
            break;
        case "dark":
            this.colors = this.colors1;
            break;
        case "strong":
            this.colors = this.colors2;
            break;
        case "light":
            this.colors = this.colors3;
            break;
        case "pastel":
            this.colors = this.colors4;
            break;
        case "user":
            this.colors = this.colors;
            break
        }
    };
    this.reinitializeGraph = function () {
        if (this.graphType == "Candlestick") {
            this.isGraphTime = true;
            if (!this.isMarketSwitched) {
                this.switchToMarketData();
                this.initAxes();
                this.summaryType = "candle";
                this.initializeDataAttributes();
                this.isMarketSwitched = true
            }
        } else {
            if (this.isExample && this.isMarketSwitched) {
                this.switchFromMarketData();
                this.isMarketSwitched = false;
                this.summaryType = this.data.y.data ? "raw" : "mean";
                this.initializeDataAttributes();
                this.isGraphTime = false;
                if (this.showVolume) {
                    this.layoutComb = false
                }
            } else {
                if (this.graphType.match(/Scatter/) && this.allVsAll) {
                    this.setAllVsAll()
                } else {
                    if (this.graphType.match(/Pie/) && this.xAxis.length > 1) {
                        this.setMultiplePies()
                    }
                }
            }
        }
    };
    this.initializeGraph = function (e, d, a, b) {
        if (this.initialRemoteDataRequest) {
            return
        }
        this.setColorScheme();
        this.reinitializeGraph();
        if ((!this.layoutComb || !this.layoutAdjust) && !b) {
            if (this.graphType != "Candlestick") {
                this.initAxes(e)
            }
        }
        if (e) {
            this.resizeCanvas(true, d, a)
        } else {
            this.resizeCanvas(false, d, a)
        }
        if (this.isVML) {
            this.showShadow = false
        }
        if (this.isValidAnimation() && this.showAnimation && !this.isAnimation) {
            this.createAnimation(this.animationType)
        } else {
            switch (this.graphType) {
            case "Bar":
            case "Line":
            case "Area":
            case "BarLine":
            case "Dotplot":
            case "Heatmap":
            case "Stacked":
            case "StackedPercent":
            case "Candlestick":
                if (this.isBoxPlotCalc && this.isGroupedData) {
                    this.groupSamples(this.getGroupingFactors(true))
                }
                this.oneDPlot();
                break;
            case "Boxplot":
                if (!this.isBoxPlotCalc && this.isGroupedData) {
                    this.groupSamples(this.getGroupingFactors(true))
                }
                this.oneDPlot();
                break;
            case "Scatter2D":
            case "ScatterBubble2D":
                this.Scatter2D();
                break;
            case "Scatter3D":
                this.Scatter3D();
                break;
            case "Correlation":
                this.Correlation();
                break;
            case "Venn":
                this.Venn();
                break;
            case "Pie":
                this.Pie();
                break;
            case "Network":
                this.Network();
                break;
            case "Genome":
                this.Genome();
                break
            }
            this.resizeImage()
        }
    };
    this.initializeGraph()
};
CanvasXpress.prototype.initAxes = function (b, a) {
    this.getValidAxes = function (d) {
        var e = [];
        if (this.graphType == "Scatter2D") {
            e.push("xAxis");
            e.push("yAxis")
        } else {
            if (this.graphType == "ScatterBubble2D" || this.graphType == "Scatter3D") {
                e.push("xAxis");
                e.push("yAxis");
                e.push("zAxis")
            } else {
                if (this.graphType.match(/Network|Genome|Correlation|Venn|Heatmap|Pie/)) {
                    if (d) {
                        e.push("xAxis")
                    } else {
                        return false
                    }
                } else {
                    if (this.graphType == "BarLine" || this.graphType == "Candlestick") {
                        e.push("xAxis");
                        e.push("xAxis2")
                    } else {
                        e.push("xAxis")
                    }
                }
            }
        }
        return e
    };
    this.addRemoveSamplesInAxis = function (f, e, d) {
        if (this.graphType == "Scatter2D" || this.graphType == "ScatterBubble2D") {
            if (d) {
                return this.removeSamplesFromAxis(f, e)
            } else {
                return this.addSamplesToAxis(f, e)
            }
        } else {
            if (this.graphType == "Scatter3D" || this.graphType == "Pie") {
                return this.switchSampleInAxis(f, e)
            }
        }
    };
    this.addSamplesToAxis = function (f, e) {
        if (this[e]) {
            if (this.isArray(f)) {
                for (var d = 0; d < f.length; d++) {
                    this[e].push(f[d])
                }
            } else {
                this[e].push(f)
            }
            return false
        } else {
            return "Not a valid axis"
        }
    };
    this.removeSamplesFromAxis = function (h, g) {
        if (this[g]) {
            if (this[g].length > 1) {
                var e = {};
                var d = [];
                if (this.isArray(h)) {
                    for (var f = 0; f < h.length; f++) {
                        e[h[f]] = 1
                    }
                } else {
                    e[h] = 1
                }
                for (var f = 0; f < this[g].length; f++) {
                    if (!e.hasOwnProperty(this[g][f])) {
                        d.push(this[g][f])
                    }
                }
                this[g] = d;
                return false
            } else {
                return g + " cannot be null"
            }
        } else {
            return "Not a valid axis"
        }
    };
    this.switchSampleInAxis = function (e, d) {
        if (this[d]) {
            this[d][0] = e;
            return false
        } else {
            return "Not a valid axis"
        }
    };
    this.addRemoveVariablesInAxis = function (f, e, d) {
        if (d) {
            return this.removeVariablesFromAxis(f, e)
        } else {
            return this.addVariablesToAxis(f, e)
        }
    };
    this.addVariablesToAxis = function (f, e) {
        if (!this.data.a) {
            this.data.a = {}
        }
        if (!this.data.a[e]) {
            this.data.a[e] = []
        }
        if (this.isArray(f)) {
            for (var d = 0; d < f.length; d++) {
                this.data.a[e].push(f[d])
            }
        } else {
            this.data.a[e].push(f)
        }
        this.resetVariablesVisibleByAxis(e);
        return false
    };
    this.removeVariablesFromAxis = function (h, g) {
        if (this.data.a && this.data.a[g]) {
            var e = {};
            var d = [];
            if (this.isArray(h)) {
                for (var f = 0; f < h.length; f++) {
                    e[h[f]] = 1
                }
            } else {
                e[h] = 1
            }
            for (var f = 0; f < this.data.a[g].length; f++) {
                if (!e.hasOwnProperty(this.data.a[g][f])) {
                    d.push(this.data.a[g][f])
                }
            }
            this.data.a[g] = d
        }
        this.resetVariablesVisibleByAxis(g);
        return false
    };
    this.getAxisRangeBySample = function (d) {
        var h = [];
        if (this.isArray(d)) {
            for (var f = 0; f < this.varIndices.length; f++) {
                var g = this.varIndices[f];
                for (var e = 0; e < d.length; e++) {
                    h.push(this.getDataAtPos(g, e))
                }
            }
        } else {
            for (var f = 0; f < this.varIndices.length; f++) {
                var g = this.varIndices[f];
                h.push(this.getDataAtPos(g, d))
            }
        }
        return this.range(h)
    };
    this.getAxisIncrements = function (g, m, n, j) {
        if (j) {
            return m == g ? m * this.axisExtension / n : (m - g) / n
        } else {
            var l = (m - g) * this.axisExtension;
            var e = m == g ? g - (g * this.axisExtension) : g - l;
            var h = m == g ? m + (m * this.axisExtension) : m + l;
            var i = h - e;
            var k = -Math.floor(Math.log(i / n) / Math.LN10);
            var f = Math.pow(10, -k);
            var d = ((i / n) / f);
            if (d < 1.5) {
                return f
            } else {
                if (d < 2) {
                    return f * 2
                } else {
                    if (d < 2.5) {
                        return f * 2.5
                    } else {
                        if (d < 5) {
                            return f * 5
                        } else {
                            if (d < 7.5) {
                                return f * 7.5
                            } else {
                                return f * 10
                            }
                        }
                    }
                }
            }
        }
    };
    this.getAxisMin = function (d, e) {
        var f = e * Math.floor(d / e);
        return f < 0 && this.minData > 0 ? 0 : f
    };
    this.getAxisDecimals = function (d) {
        var e = (d - parseInt(d)).toString().replace(/\./, "").length - 1;
        return e
    };
    this.setAxis = function (d) {
        var e = this.isGroupedData ? this.data.w : this.data.y;
        if (this[d].length == 0) {
            if (d == "xAxis") {
                this[d].push(e.smps[this.smpIndices[0]])
            } else {
                if (d == "yAxis") {
                    if (e.smps.length > 1) {
                        this[d].push(e.smps[this.smpIndices[1]])
                    } else {
                        this[d].push(e.smps[this.smpIndices[0]])
                    }
                } else {
                    if (d == "zAxis") {
                        if (e.smps.length > 2) {
                            this[d].push(e.smps[this.smpIndices[2]])
                        } else {
                            if (e.smps.length > 1) {
                                this[d].push(e.smps[this.smpIndices[1]])
                            } else {
                                this[d].push(e.smps[this.smpIndices[0]])
                            }
                        }
                    }
                }
            }
        } else {
            if (this.graphType == "Scatter3D" && this[d].length > 1) {
                this[d] = [this[d][0]]
            }
        }
    };
    this.setAxisValues = function (f) {
        var x, p, m, n, o, v, k;
        var e = {};
        var h = f + "MinorValues";
        var l = f + "Values";
        var u = f + "Ticks";
        var t = f + "Incr";
        var d = f + "Decs";
        var q = f + "Min";
        var g = f + "Transform";
        if (this[g] == "percentile") {
            this[d] = 0;
            this[l] = [];
            this[h] = [];
            var w = [0, 25, 50, 75, 100];
            for (var s = 0; s < w.length; s++) {
                x = this.formatNumber(w[s]);
                if (e.hasOwnProperty(x)) {
                    k = true
                } else {
                    e[x] = 1
                }
                this[l].push(x)
            }
            for (var s = 5; s < 100; s += 5) {
                this[h].push(s)
            }
        } else {
            if (this[l].length == 0 || b) {
                if (b) {
                    this[l] = [];
                    this[h] = []
                }
                x = this[q];
                o = this[t] / this[u];
                for (var s = 1; s <= this[u]; s++) {
                    for (var r = 1; r < this[u]; r++) {
                        this[h].push(x + (o * r))
                    }
                    x += this[t];
                    if (this[d] == 0 && x.toString().match(/\.5$/)) {
                        this[d] = 1
                    }
                    p = this.formatNumber(sprintf("%." + this[d] + "f", x));
                    if (e.hasOwnProperty(p)) {
                        k = true
                    } else {
                        e[p] = 1
                    }
                    this[l].push(p)
                }
            } else {
                m = this[l];
                this[l] = [];
                for (var s = 0; s < m.length; s++) {
                    x = m[s];
                    n = parseFloat(x);
                    p = this.formatNumber(sprintf("%." + this[d] + "f", n));
                    if (e.hasOwnProperty(p)) {
                        k = true
                    } else {
                        e[p] = 1
                    }
                    this[l].push(p)
                }
            }
        }
        if (k) {
            this[d]++;
            this[l] = [];
            this[h] = [];
            this.setAxisValues(f)
        }
    };
    this.setAxisAttributes = function (i, p, s, f, o) {
        var r = f && f.match(/log/) ? true : false;
        var m = i + "Ticks";
        var e = i + "Incr";
        var h = i + "Exact";
        var d = i + "AbsMin";
        var l = i + "AbsMax";
        var j = i + "Min";
        var q = i + "Max";
        var n = i + "Range";
        var k = i + "Decs";
        var g = i + "Transform";
        this.setRangeData(p, s, r, o);
        if (f) {
            if (f == "log2") {
                this.minData = Math.log(this.minData) / Math.LN2;
                this.maxData = Math.log(this.maxData) / Math.LN2
            } else {
                if (f == "log10") {
                    this.minData = Math.log(this.minData) / Math.LN10;
                    this.maxData = Math.log(this.maxData) / Math.LN10
                } else {
                    if (f == "exp2") {
                        this.minData = Math.pow(2, this.minData);
                        this.maxData = Math.pow(2, this.maxData)
                    } else {
                        if (f == "exp10") {
                            this.minData = Math.pow(10, this.minData);
                            this.maxData = Math.pow(10, this.maxData)
                        } else {
                            if (f == "percentile") {
                                this.minData = -12.5;
                                this.maxData = 112.5
                            }
                        }
                    }
                }
            }
        }
        this[d] = this.setMin == null ? this.minData : this[d];
        this[l] = this.setMax == null ? this.maxData : this[l];
        this[e] = this.getAxisIncrements(this.minData, this.maxData, this[m], this[h]);
        this[k] = this.getAxisDecimals(this[e]);
        if (o) {
            this[j] = 0;
            if (this.graphType.match(/Percent/)) {
                this[q] = 100
            } else {
                this[q] = this[j] + (this[e] * this[m])
            }
        } else {
            if (this[h] || (f && f == "percentile")) {
                this[j] = this.minData;
                this[q] = this.maxData
            } else {
                this[j] = this.getAxisMin(this.minData, this[e]);
                this[q] = this[j] + (this[e] * this[m])
            }
        }
        this[n] = this[q] - this[j];
        this.setAxisValues(i)
    };
    this.setAxes = function () {
        if (this.graphType.match(/Correlation/)) {
            var f = this.isGroupedData ? this.data.w : this.data.y;
            this.setMin = null;
            this.setMax = null;
            if (this.correlationAnchorLegend) {
                this.setAxisAttributes("xAxis")
            }
            if (!f.cor || f.cor.length == 0) {
                this.summarize("cor");
                this.setRangeData("cor")
            }
            if (!this.yAxisTitle) {
                this.yAxisTitle = "Correaltion"
            }
        } else {
            if (this.graphType.match(/Scatter|Pie/)) {
                var t = this.smpIndices;
                var p = ["xAxis"];
                var d = ["X"];
                this.setMin = null;
                this.setMax = null;
                if (this.graphType.match(/Scatter/)) {
                    p.push("yAxis");
                    d.push("Y")
                }
                if (this.graphType.match(/ScatterBubble2D|Scatter3D/)) {
                    p.push("zAxis");
                    d.push("Z")
                }
                if (this.graphType == "Scatter3D") {
                    this.xAxisTicks = 10;
                    this.yAxisTicks = 10;
                    this.zAxisTicks = 10
                }
                for (var k = 0; k < p.length; k++) {
                    var l = p[k] + "Indices";
                    var j = "setMin" + d[k];
                    var r = "setMax" + d[k];
                    this.setAxis(p[k]);
                    this[l] = this.getSampleIndices(this[p[k]]);
                    this.setSamplesVisible(this[l]);
                    if (this[j]) {
                        this.setMin = this[j]
                    }
                    if (this[r]) {
                        this.setMax = this[r]
                    }
                    this.setAxisAttributes(p[k], false, false, this[p[k] + "Transform"]);
                    this.setSamplesVisible(t)
                }
                if (!this.xAxisTitle) {
                    this.xAxisTitle = this.xAxis.length == 1 ? this.xAxis[0] : " "
                }
                if (!this.yAxisTitle) {
                    this.yAxisTitle = this.yAxis.length == 1 ? this.yAxis[0] : " "
                }
                if (!this.zAxisTitle) {
                    this.zAxisTitle = this.zAxis.length == 1 ? this.zAxis[0] : " "
                }
            } else {
                if (this.graphType.match(/Genome/)) {
                    if (this.setMin == null || this.setMax == null) {
                        this.setRangeData("genome")
                    }
                    this.xAxisExact = true;
                    this.xAxisTransform = false;
                    this.xAxisMin = this.setMin != null ? this.setMin : this.minData;
                    this.xAxisMax = this.setMax != null ? this.setMax : this.maxData;
                    this.xAxisDecs = 0;
                    this.xAxisRange = this.xAxisMax - this.xAxisMin;
                    this.xAxisTicks = this.ticks;
                    this.xAxisIncr = this.xAxisRange / this.xAxisTicks;
                    this.setAxisValues("xAxis")
                } else {
                    var o = this.smpIndices;
                    var e = this.varIndices;
                    var h = this.grpIndices;
                    var m = this.varIndices;
                    var s = this.graphType == "Dotplot" && this.isGroupedData ? true : false;
                    var g = this.graphType.match(/Percent/) ? "percentile" : false;
                    var n = this.graphType.match(/Stacked|Area/) ? true : false;
                    var q = this.graphType == "Candlestick" ? "candle" : g ? g : false;
                    if (this.graphType == "Heatmap") {
                        this.graphOrientation = "horizontal";
                        if (this.varIndicesStart > -1 || this.smpIndicesStart > -1) {
                            this.setAllVariablesVisible();
                            this.setAllSamplesVisible()
                        }
                    } else {
                        if (this.graphType == "StackedPercent") {
                            this.setMin = null;
                            this.setMax = null;
                            this.axisExact = true;
                            this.xAxisExact = true
                        } else {
                            if (this.graphType == "Boxplot" && !this.isGroupedData) {
                                if (!this.data.y.median) {
                                    this.summarize("iqr")
                                }
                            }
                        }
                    }
                    if (this.graphType == "BarLine" && this.data.a) {
                        this.setVariablesVisible(this.getVariablesVisibleByAxis("xAxis"))
                    }
                    this.setAxisAttributes("xAxis", q, s, g, n);
                    this["xAxisTitle"] = !this["xAxisTitle"] && this.data.y.desc ? this.data.y.desc[0] : "";
                    if ((this.graphType == "BarLine" && this.data.a) || (this.graphType == "Candlestick" && this.showVolume)) {
                        q = this.graphType == "Candlestick" ? "volume" : false;
                        if (this.graphType == "BarLine") {
                            this.setVariablesVisible(m);
                            this.setVariablesVisible(this.getVariablesVisibleByAxis("xAxis2"))
                        }
                        var r = this.setMax != null ? this.setMax : null;
                        var j = this.setMin != null ? this.setMin : null;
                        this.setMax = this.setMax2 != null ? this.setMax2 : null;
                        this.setMin = this.setMin2 != null ? this.setMin2 : null;
                        this.setAxisAttributes("xAxis2", q, s, g, n);
                        this["xAxis2Title"] = !this["xAxis2Title"] && this.data.y.desc ? this.data.y.desc[1] : "";
                        this.setMax = r != null ? r : null;
                        this.setMin = j != null ? j : null
                    }
                    this.setVariablesVisible(m);
                    this.smpIndices = o;
                    this.varIndices = e;
                    this.grpIndices = h
                }
            }
        }
    };
    this.setScatterPointSize = function () {
        this.scatterPointSize = this.varIndices.length > 50 ? this.sizes[1] : this.varIndices.length > 20 ? this.sizes[2] : this.sizes[3];
        if (this.layoutComb) {
            this.scatterPointSize = Math.ceil(this.scatterPointSize / this.layoutRows)
        }
    };
    this.setAxisUnits = function (i, k) {
        var j = i + "Unit";
        var g = i + "Range";
        var h = i + "Min";
        var d = i + "Max";
        var f = i + "Exact";
        var e = k ? k : i.substring(0, 1);
        this[j] = this[e] / this[g]
    };
    this.setAxisFont = function (i, h) {
        var g = this.graphType == "Venn" ? 16 : 12;
        if (this.autoScaleFont || !this.axisTickFont || !this.decorationFont || !this.axisTitleFont) {
            var d, f, e;
            if (h >= 600) {
                d = 12 * this.scaleTickFontFactor;
                e = 10 * this.scaleDecorationFontFactor;
                f = this.getFontPt(this.scaleTextToFont(i, g, h - 2)) * this.scaleTitleFontFactor
            } else {
                if (h >= 400) {
                    d = 10 * this.scaleTickFontFactor;
                    e = 8 * this.scaleDecorationFontFactor;
                    f = this.getFontPt(this.scaleTextToFont(i, g, h - 2)) * this.scaleTitleFontFactor
                } else {
                    if (h >= 200) {
                        d = 8 * this.scaleTickFontFactor;
                        e = 6 * this.scaleDecorationFontFactor;
                        f = 8 * this.scaleTitleFontFactor
                    } else {
                        d = 6 * this.scaleTickFontFactor;
                        e = 4 * this.scaleDecorationFontFactor;
                        f = 6 * this.scaleTitleFontFactor
                    }
                }
            }
            this.axisTickFont = (parseInt(d)) + "Pt " + this.fontName;
            this.decorationFont = (parseInt(e)) + "Pt " + this.fontName;
            this.axisTitleFont = (parseInt(f)) + "Pt " + this.fontName
        } else {
            this.axisTickFont = parseInt(this.axisTickFontSize) + "Pt " + this.fontName;
            this.decorationFont = parseInt(this.decorationFontSize) + "Pt " + this.fontName;
            this.axisTitleFont = parseInt(this.axisTitleFontSize) + "Pt " + this.fontName
        }
    };
    this.getAxisFont = function (d) {
        if (d >= 600) {
            return 12
        } else {
            if (d >= 400) {
                return 10
            } else {
                if (d >= 200) {
                    return 8
                } else {
                    return 6
                }
            }
        }
    };
    this.getHeatmapShades = function (d) {
        switch (d) {
        case "green":
            return this.greens;
        case "red":
            return this.reds;
        case "blue":
            return this.blues;
        case "yellow":
            return this.yellows;
        case "cyan":
            return this.cyans;
        case "purple":
            return this.purples
        }
    };
    this.setHeatmapColors = function (d) {
        this.setRGB();
        var l = d ? d : this.maxData - this.minData;
        var e = this.heatmapType.split("-");
        var n = 256 / this.indicatorBins;
        var m = this.getHeatmapShades(e[0]);
        this.heatmapColors = [];
        this.heatmapBin = l / this.indicatorBins;
        if (this.indicatorCenter.match("rainbow") && e.length > 1) {
            var f = ["red", "purple", "blue", "cyan", "green", "yellow"];
            if (this.indicatorCenter == "rainbow-green") {
                f.reverse()
            }
            var o = null;
            for (var k = 0; k < f.length; k++) {
                if (f[k] == e[0]) {
                    o = k
                }
            }
            if (o == null) {
                alert("Dude! " + e[0] + " ain't a valid color ... This is not looking good ...");
                this.heatmapColors = this.blues;
                return
            }
            var g = [f[o]];
            o++;
            while (g.length < 6) {
                if (o > 5) {
                    o = 0
                }
                g.push(f[o]);
                o++
            }
            o = null;
            for (var k = 0; k < g.length; k++) {
                if (g[k] == e[1]) {
                    o = k
                }
            }
            if (o == null) {
                alert("Dude! " + e[1] + " ain't a valid color ... This is not looking good ...");
                this.heatmapColors = this.blues;
                return
            }
            for (var k = 0; k < o; k++) {
                m = this.getHeatmapShades(g[k]);
                for (var h = 0; h < n; h++) {
                    this.heatmapColors.push(m[h])
                }
            }
        } else {
            if (e.length > 1) {
                for (var k = 0; k < n; k++) {
                    this.heatmapColors.push(m[k])
                }
                if (this.indicatorCenter == "black") {
                    this.heatmapColors.push("rgb(0,0,0)")
                } else {
                    this.heatmapColors.push("rgb(255,255,255)")
                }
                m = this.getHeatmapShades(e[1]);
                for (var k = n - 1; k >= 0; k--) {
                    this.heatmapColors.push(m[k])
                }
            } else {
                if (this.indicatorCenter == "black") {
                    this.heatmapColors.push("rgb(0,0,0)")
                } else {
                    this.heatmapColors.push("rgb(255,255,255)")
                }
                for (var k = n - 1; k >= 0; k--) {
                    this.heatmapColors.push(m[k])
                }
            }
        }
    };
    this.initializeAxis = function () {
        if (!this.graphType.match(/Venn|Network/)) {
            if (!a) {
                this.setAxes();
                this.setHeatmapColors()
            }
        }
    };
    this.initializeAxis()
};
CanvasXpress.prototype.Pie = function () {
    this.setVarPie = function () {
        this.varPie = {
            no: 0,
            idso: [],
            ids: [],
            data: {},
            oprc: 0,
            tot: 0
        };
        var b = this.smpIndices[0];
        if (this.varIndices.length >= this.maxPieSectors - 1) {
            var a = this;
            this.varIndices.sort(function (i, h) {
                return a.data.y.data[h][b] - a.data.y.data[i][b]
            })
        }
        for (var e = 0; e < this.varIndices.length; e++) {
            var f = this.varIndices[e];
            this.varPie.tot += this.getDataAtPos(f, b)
        }
        for (var e = 0; e < this.varIndices.length; e++) {
            var f = this.varIndices[e];
            var g = this.getDataAtPos(f, b);
            var d = g / this.varPie.tot;
            this.varPie.data[f] = d;
            if (e >= this.maxPieSectors - 1) {
                this.varPie.oprc += d;
                this.varPie.idso.push(f);
                this.varPie.no++
            } else {
                this.varPie.ids.push(f)
            }
        }
        if (this.varPie.no) {
            this.varPie.ids.push(-1)
        }
    };
    this.getPieLegendDimensions = function () {
        this.setLegendFont();
        this.setVarPie();
        var f = this.getFontPt(this.legendFont);
        var e = this.shortenText(this.maxVarNameStr, this.maxVarStringLen);
        var b = this.measureText(e, this.legendFont);
        if (!this.autoExtend) {
            if (b > this.width / 2) {
                this.legendFont = this.scaleTextToFont(b, f, this.width / 2)
            }
        }
        var d = f + this.measureText(e, this.legendFont) + (this.margin * 5);
        var g = this.varIndices.length > this.maxPieSectors ? this.maxPieSectors : this.varIndices.length;
        var a = (g * (f + this.margin)) + (this.margin * 3);
        return ([d, a])
    };
    this.setPieXYDimensions = function () {
        var a = this.getPieLegendDimensions();
        if (this.autoExtend) {
            if (this.showLegend) {
                this.right = a[0]
            }
            if ((a[0] + this.left + this.right) > this.width && this.showLegend) {
                this.x = a[0];
                this.width = this.left + this.x + this.right
            } else {
                this.x = this.width - (this.left + this.right)
            }
            if ((a[1] + this.top + this.bottom) > this.height && this.showLegend) {
                this.y = a[1];
                this.height = this.top + this.y + this.bottom
            } else {
                this.y = this.height - (this.top + this.bottom)
            }
            this.resizeCanvas()
        } else {
            if (this.showLegend) {
                this.right = this.layoutComb ? this.layoutRight : a[0]
            }
            this.x = this.layoutComb ? this.layoutWidth : this.width - (this.left + this.right);
            this.y = this.layoutComb ? this.layoutHeight : this.height - (this.top + this.bottom)
        }
        this.setAxisFont("Pie", this.x)
    };
    this.drawPieDataPoints = function () {
        var o = Math.min(this.x, this.y);
        var y = (o - (o / 5)) / 2;
        var n = this.pieSegmentLabels == "inside" ? y - (y / 3) : y + (y / 10);
        var r = this.layoutComb ? this.offsetX + (this.x / 2) : this.left + (this.x / 2);
        var a = this.layoutComb ? this.offsetY + (this.y / 2) : this.top + (this.y / 2);
        var e = 0;
        var t = this.layoutComb ? this.smpIndices[0] : this.xAxisIndices[0];
        for (var u = 0; u < this.varPie.ids.length; u++) {
            var h = this.varPie.ids[u];
            var z = h > -1 ? this.varPie.data[h] : this.varPie.oprc;
            var g = u % this.colors.length;
            var m = Math.PI * (2 * e);
            var f = Math.PI * (2 * (e + z));
            var x = (m + f) / 2;
            var q = (f - m) / 32;
            var w = this.transparency != null ? this.validateColor(this.colors[g], this.transparency) : this.colors[g];
            if (this.gradient) {
                this.setRadialGradient(r, a, y * 1.3, w)
            } else {
                this.ctx.fillStyle = w
            }
            this.ctx.beginPath();
            this.ctx.lineWidth = this.pieSegmentSeparation;
            this.ctx.strokeStyle = this.pieType == "separated" ? this.background : this.pieType == "solid" ? this.foreground : this.foreground;
            if (this.showShadow) {
                this.ctx.shadowOffsetX = this.shadowOffsetX;
                this.ctx.shadowOffsetY = this.shadowOffsetY;
                this.ctx.shadowBlur = this.shadowBlur;
                this.ctx.shadowColor = this.shadowColor
            }
            this.ctx.moveTo(r, a);
            this.ctx.arc(r, a, y, m, f, false);
            this.ctx.lineTo(r, a);
            this.ctx.closePath();
            this.ctx.fill();
            this.ctx.stroke();
            if (this.showShadow) {
                this.ctx.restore();
                this.ctx.shadowOffsetX = 0;
                this.ctx.shadowOffsetY = 0;
                this.ctx.shadowBlur = 0;
                this.ctx.shadowColor = this.background
            }
            if (this.pieSegmentLabels) {
                var v = r - Math.sin(x - (Math.PI / 2)) * n;
                var d = a + Math.cos(x - (Math.PI / 2)) * n;
                this.drawText(sprintf("%." + this.pieSegmentPrecision + "f", z * 100) + "%", v, d, this.axisTickFont, this.axisTickColor, "center", "middle");
                v = r + y * Math.cos(m);
                d = a + y * Math.sin(m);
                var b = ["poly", r, a, v, d];
                var l = m;
                for (var p = 0; p < 32; p++) {
                    l += q;
                    v = r + y * Math.cos(l);
                    d = a + y * Math.sin(l);
                    b.push(v);
                    b.push(d)
                }
                b.push(r);
                b.push(a);
                if (h > -1) {
                    this.addArea(b, [h, t])
                } else {
                    this.varPie.idso.push(t);
                    this.addArea(b, this.varPie.idso)
                }
            }
            e += z
        }
        this.ctx.lineWidth = 1
    };
    this.drawPieLegend = function () {
        if (this.showLegend) {
            var l = this.getPieLegendDimensions();
            var m = this.getFontPt(this.legendFont);
            var k = m / 2;
            var b = this.layoutComb ? (this.width - l[0]) + this.margin + k : this.left + this.x + (this.margin * 2) + k;
            var a = b + k + this.margin;
            var h = this.layoutComb ? (this.layoutTop + (this.margin * 3) + ((this.height - (this.layoutTop + this.layoutBottom)) / 2)) - (l[1] / 2) : this.top + ((this.y / 2) - (l[1] / 2)) + this.margin;
            var g = h;
            for (var e = 0; e < this.varPie.ids.length; e++) {
                var j = this.varPie.ids[e];
                var d = e % this.colors.length;
                var f = j > -1 ? this.shortenText(this.data.y.vars[j], this.maxVarStringLen) : "Other";
                this.drawShape("square", b, g, 10, 10, this.colors[d], this.foreground, "closed");
                this.drawText(f, a, g, this.legendFont, this.legendColor, "left", "middle");
                g += m + this.margin
            }
            if (this.legendBox) {
                this.rectangle(b - (this.margin * 2), h - (this.margin * 2), l[0] - m, l[1] - m, false, this.foreground, "open")
            }
        }
    };
    this.drawPiePlot = function () {
        this.setPieXYDimensions();
        this.drawTitle();
        this.drawPieDataPoints();
        this.drawPieLegend()
    };
    this.initializePiePlot = function () {
        if (this.xAxisMin < 0) {
            alert("Dude you can't draw a pie chart with negative numbers!");
            return
        }
        if (this.layoutValid) {
            this.autoExtend = false;
            this.drawLayoutCompartments(this.drawPiePlot)
        } else {
            this.drawPiePlot()
        }
    };
    this.initializePiePlot()
};
CanvasXpress.prototype.Venn = function () {
    this.setVennXYDimensions = function () {
        this.setLegendFont();
        var a = this.autoExtend ? Math.max(this.width, this.height) : this.height;
        this.height = a;
        this.setAxisFont("Venn", a);
        if (this.showLegend) {
            this.bottom += (this.getFontPt(this.legendFont) + this.margin) * this.vennGroups
        }
        this.y = this.height - (this.top + this.bottom);
        this.x = this.y;
        if (this.x < 400) {
            var b = this.getFontPt(this.axisTickFont) - 2;
            if (this.x < 200) {
                b -= 1
            }
            this.axisTickFont = b + "Pt " + this.fontName
        }
        this.width = this.left + this.x + this.right;
        this.resizeCanvas()
    };
    this.drawVennBubble = function (b, d, a) {
        if (this.vennGroups == 4) {
            if (this.isVML) {
                a = a.replace("rgb", "rgba");
                a = a.replace(")", ",0.5)");
                this.drawShape("ellipse2", 0, 0, this.x / 1.4, this.x / 1.4, a, this.foreground, "closed")
            } else {
                this.drawShape("ellipse2", 0, 0, this.x / 1.4, this.x / 1.4, a, this.foreground, "closed");
                this.ctx.globalCompositeOperation = "lighter"
            }
        } else {
            if (this.isVML) {
                a = a.replace("rgb", "rgba");
                a = a.replace(")", ",0.5)");
                this.drawShape("circle", b, d, this.x / 2, this.x / 2, a, this.foreground, "closed")
            } else {
                this.drawShape("circle", b, d, this.x / 2, this.x / 2, a, this.foreground, "closed");
                this.ctx.globalCompositeOperation = "lighter"
            }
        }
    };
    this.drawVennLayout = function () {
        var h, j, b, f;
        var a = ["A", "B", "C", "D"];
        this.ctx.clearRect(0, 0, this.width, this.height);
        if (this.vennGroups == 4) {
            this.ctx.save();
            h = this.margin + (this.x / 4.94);
            j = this.top + (this.x / 13);
            this.ctx.translate(h, j);
            this.ctx.rotate(Math.PI / 4);
            this.drawVennBubble(0, 0, this.vennColors && this.validateColor(this.vennColors[0]) ? this.validateColor(this.vennColors[0]) : "rgb(255,0,0)");
            h = this.x / 22.4;
            j = -this.x / 5.32;
            this.ctx.translate(h, j);
            this.drawVennBubble(0, 0, this.vennColors && this.validateColor(this.vennColors[0]) ? this.validateColor(this.vennColors[1]) : "rgb(0,153,51)");
            h = this.x / 1.84;
            j = this.x / -5.32;
            this.ctx.translate(h, j);
            this.ctx.rotate(Math.PI / 2);
            h = -this.x / 22.4;
            j = -this.x / 5.32;
            this.ctx.translate(h, j);
            this.drawVennBubble(0, 0, this.vennColors && this.validateColor(this.vennColors[0]) ? this.validateColor(this.vennColors[2]) : "rgb(0,0,255)");
            h = this.x / 22.4;
            j = this.x / 5.32;
            this.ctx.translate(h, j);
            this.drawVennBubble(0, 0, this.vennColors && this.validateColor(this.vennColors[0]) ? this.validateColor(this.vennColors[3]) : "rgb(153,204,0)");
            this.ctx.restore();
            b = [11.2, 3.05, 1.46, 1.08];
            f = [7, 42, 42, 7]
        } else {
            if (this.vennGroups == 3) {
                this.ctx.save();
                h = this.left + (this.x / 2);
                j = this.top + (this.x / 2.86);
                this.drawVennBubble(h, j, this.vennColors && this.validateColor(this.vennColors[0]) ? this.validateColor(this.vennColors[0]) : "rgb(255,0,0)");
                j += (this.x / 3.33);
                h = this.left + (this.x / 3);
                this.drawVennBubble(h, j, this.vennColors && this.validateColor(this.vennColors[0]) ? this.validateColor(this.vennColors[1]) : "rgb(0,255,0)");
                h = this.left + (this.x / 1.5);
                this.drawVennBubble(h, j, this.vennColors && this.validateColor(this.vennColors[0]) ? this.validateColor(this.vennColors[2]) : "rgb(0,0,255)");
                this.ctx.restore();
                b = [2, 8.66, 1.13];
                f = [20, 2.3, 2.3]
            } else {
                if (this.vennGroups == 2) {
                    this.ctx.save();
                    h = this.left + (this.x / 3);
                    j = this.top + (this.x / 2.67);
                    this.drawVennBubble(h, j, this.vennColors && this.validateColor(this.vennColors[0]) ? this.validateColor(this.vennColors[0]) : "rgb(0,0,255)");
                    h = this.left + (this.x / 1.5);
                    j = this.top + (this.x / 2.67);
                    this.drawVennBubble(h, j, this.vennColors && this.validateColor(this.vennColors[0]) ? this.validateColor(this.vennColors[1]) : "rgb(255,0,0)");
                    this.ctx.restore();
                    b = [3, 1.5];
                    f = [16, 16]
                } else {
                    if (this.vennGroups == 1) {
                        h = this.left + (this.x / 2);
                        j = this.top + (this.x / 2);
                        this.drawShape("circle", h, j, this.x, this.x, this.vennColors && this.validateColor(this.vennColors[0]) ? this.validateColor(this.vennColors[0]) : "rgb(255,0,0)", this.foreground, "closed");
                        b = [100];
                        f = [100]
                    }
                }
            }
        }
        for (var e = 0; e < this.vennGroups; e++) {
            var d = this.left + (this.x / b[e]);
            var g = this.top + (this.x / f[e]);
            this.drawText(a[e], d, g, this.axisTickFont, this.axisTickColor, "center", "middle")
        }
    };
    this.drawVennDataPoints = function () {
        var a, f;
        if (this.vennGroups == 4) {
            a = [5.6, 2.94, 3.5, 1.54, 3.29, 2, 2.59, 1.212, 2, 1.442, 1.4, 1.757, 2.31, 1.624, 2];
            f = [2.94, 7, 3.92, 7, 1.89, 4.06, 2.66, 2.94, 1.54, 1.89, 3.92, 1.729, 1.729, 2.66, 2.03];
            this.y = (this.x * 1.1) / 1.4
        } else {
            if (this.vennGroups == 3) {
                a = [2, 3.5, 2.55, 1.4, 1.6, 2, 2];
                f = [3.6, 1.54, 2.07, 1.54, 2.07, 1.54, 1.83];
                this.y = this.x * 0.95
            } else {
                if (this.vennGroups == 2) {
                    a = [3.5, 1.4, 2];
                    f = [2.66, 2.66, 2.66];
                    this.y = this.x * 0.65
                } else {
                    if (this.vennGroups == 1) {
                        a = [2];
                        f = [2]
                    }
                }
            }
        }
        for (var e = 0; e < a.length; e++) {
            var d = this.left + (this.x / a[e]);
            var g = this.top + (this.x / f[e]);
            var b = this.getFontPt(this.axisTickFont);
            var h = this.data.venn.data[this.vennCompartments[e]] ? this.data.venn.data[this.vennCompartments[e]] : 0;
            this.drawText(h, d, g, this.axisTickFont, this.axisTickColor, "center", "middle");
            this.addArea(["circle", d, g, b], [e])
        }
    };
    this.drawVennLegend = function () {
        if (this.showLegend) {
            var g = ["A", "B", "C", "D"];
            var j = this.getFontPt(this.legendFont);
            var f = this.left;
            var d = f + (j * 2);
            var h = this.top + this.y + this.margin + (j / 2);
            for (var e = 0; e < this.vennGroups; e++) {
                this.drawText(g[e] + ": ", f, h, this.legendFont, this.legendColor, "left", "middle");
                var b = this.data.venn.legend && this.data.venn.legend[g[e]] ? this.data.venn.legend[g[e]] : "N/A";
                var a = (this.measureText(b, this.legendFont) > this.x) ? this.scaleTextToFont(b, j, this.x - this.margin) : this.legendFont;
                this.drawText(b, d, h, a, this.legendColor, "left", "middle");
                h += this.margin + j
            }
        }
    };
    this.drawVennPlot = function () {
        this.setVennXYDimensions();
        this.drawTitle();
        this.showShadow = false;
        this.drawVennLayout();
        this.showShadow = this.tmpshowShadow;
        this.drawVennDataPoints();
        this.drawVennLegend()
    };
    this.initializeVennPlot = function () {
        this.transparency = null;
        this.tmpshowShadow = this.showShadow;
        this.background = "rgb(255,255,255)";
        this.foreground = "rgb(0,0,0)";
        this.drawVennPlot()
    };
    this.initializeVennPlot()
};
CanvasXpress.prototype.Correlation = function () {
    this.setCorrelationVarSmpLabelFont = function (b) {
        var a = this.scaleTextToSize(b);
        a = this.correlationAxis == "samples" ? a * this.scaleSmpLabelFontFactor : a * this.scaleVarLabelFontFactor;
        a = Math.min(a, this.maxTextSize);
        this.smpLabelFont = (a) + "Pt " + this.fontName;
        this.varLabelFont = (a) + "Pt " + this.fontName
    };
    this.getCorrelationLabelsHeight = function () {
        var a = 0;
        if (this.correlationAxis == "samples") {
            if (this.maxSmpName > this.maxSmpStringLen) {
                a += this.measureText(this.maxSmpNameStr.substring(0, this.maxSmpStringLen), this.smpLabelFont)
            } else {
                a += this.measureText(this.maxSmpNameStr, this.smpLabelFont)
            }
        } else {
            if (this.maxVarName > this.maxVarStringLen) {
                a += this.measureText(this.maxVarNameStr.substring(0, this.maxVarStringLen), this.varLabelFont)
            } else {
                a += this.measureText(this.maxVarNameStr, this.varLabelFont)
            }
        }
        if (this.correlationAnchorLegend) {
            a += this.correlationAnchorLegendAlignWidth + this.margin
        }
        return a
    };
    this.setCorrelationXYDimensions = function () {
        var a = this.autoExtend ? Math.max(this.width, this.height) : Math.min(this.width, this.height);
        this.width = a;
        this.height = a / 2;
        if (this.yAxisShow) {
            this.setAxisFont(this.yAxisTitle, this.height);
            this.left = this.margin + this.getFontPt(this.axisTitleFont) + this.margin
        }
        this.x = this.width - (this.left + this.right);
        var b = this.correlationAxis == "variables" ? this.varIndices.length : this.isGroupedData ? this.grpIndices.length : this.smpIndices.length;
        this.diamondSize = this.x / b;
        this.setCorrelationVarSmpLabelFont(b);
        this.y = this.x / 2 + (this.diamondSize / 2);
        if (this.xAxisShow) {
            this.top += this.margin + this.getCorrelationLabelsHeight() + this.margin
        }
        this.height = this.top + this.y + this.bottom;
        this.resizeCanvas()
    };
    this.drawCorrelationLayout = function () {
        if (this.yAxisShow) {
            this.drawText(this.yAxisTitle, this.left / 2, this.top + (this.y / 2), this.axisTitleFont, this.axisTitleColor, "center", "middle", -Math.PI / 2)
        }
        if (this.xAxisShow) {
            var d = this.left + this.diamondSize / 2;
            var p = this.top - this.margin;
            var o = this.top - (this.getCorrelationLabelsHeight() + this.margin);
            var n = o + this.correlationAnchorLegendAlignWidth;
            var l, g, j;
            var a = this.isGroupedData ? this.data.w : this.data.y;
            var r = this.correlationAxis == "variables" ? this.varIndices : this.isGroupedData ? this.grpIndices : this.smpIndices;
            var m = this.correlationAxis == "samples" ? this.maxSmpStringLen : this.maxVarStringLen;
            if (this.correlationAnchorLegend) {
                j = this.correlationAxis == "samples" ? a.data[0] : this.getDataForSmpGrpAtIndex(0);
                r.sort(function (s, i) {
                    return j[s] - j[i]
                });
                this.setAxisUnits("xAxis")
            }
            if (this.correlationAxis == "samples") {
                l = a.smps;
                g = this.smpLabelFont
            } else {
                l = a.vars;
                g = this.varLabelFont
            }
            for (var h = 0; h < r.length; h++) {
                var q = r[h];
                var k = this.shortenText(l[q], m);
                var f = this.correlationAxis == "samples" ? this.getSmpColor(l[q]) : this.getVarColor(l[q]);
                if (r.length < 100) {
                    this.drawText(k, d, p, g, f, "left", "middle", -Math.PI / 2)
                } else {
                    if (h % 2) {
                        this.drawText(k, d, p, g, f, "left", "middle", -Math.PI / 2)
                    }
                }
                if (this.correlationAnchorLegend) {
                    var e = j[q];
                    var b = this.left + ((e - this.xAxisMin) * this.xAxisUnit);
                    this.drawLine("line", b, o - this.margin, b, o, f);
                    this.drawLine("line", b, o, d, n, f)
                }
                d += this.diamondSize
            }
        }
        d = this.left + (this.x / 16);
        p = this.top + (this.y * 7 / 8);
        if (this.correlationAnchorLegend) {
            this.setRangeData("cor")
        }
        this.drawColorIndicator(d, p, this.minData, this.maxData, 2)
    };
    this.drawCorrelationDataPoints = function () {
        var d, q, b, o;
        var t = this.diamondSize / 2;
        var p = "rgb(255,255, 0)";
        var m = this.isGroupedData ? this.data.w.cor : this.data.y.cor;
        var s = this.correlationAxis == "variables" ? this.varIndices : this.isGroupedData ? this.grpIndices : this.smpIndices;
        d = this.left + t;
        q = this.top + t;
        for (var l = 0; l < Math.ceil(s.length / 2); l++) {
            var r = s[l];
            b = d + (this.diamondSize * l);
            o = q;
            for (var h = l; h < s.length - l; h++) {
                var n = s[h];
                var f = m[r][n];
                var e = this.getHeatmapColor(this.minData, this.maxData, f);
                if (isNaN(f)) {
                    this.addArea(this.drawShape("diamond", b, o, this.diamondSize, this.diamondSize, p, p, "closed"), [r, n])
                } else {
                    this.addArea(this.drawShape("diamond", b, o, this.diamondSize, this.diamondSize, e, e, "closed"), [r, n])
                }
                b += t;
                o += t
            }
            o -= this.diamondSize;
            for (var g = h - 2; g >= l; g--) {
                var a = s[(s.length - g) - 1];
                var f = m[a][n];
                var e = this.getHeatmapColor(this.minData, this.maxData, f);
                if (isNaN(f)) {
                    this.addArea(this.drawShape("diamond", b, o, this.diamondSize, this.diamondSize, p, p, "closed"), [a, n])
                } else {
                    this.addArea(this.drawShape("diamond", b, o, this.diamondSize, this.diamondSize, e, e, "closed"), [a, n])
                }
                b += t;
                o -= t
            }
        }
        this.drawLine("line", this.left, this.top + t, this.left + (this.x / 2), this.top + this.y, this.foreground, false, "butt");
        this.drawLine("line", this.left + this.x, this.top + t, this.left + (this.x / 2), this.top + this.y, this.foreground, false, "butt");
        d = this.left + t;
        q = this.top;
        b = d + (this.x / 2);
        o = (this.top + this.y) - t;
        for (var l = 0; l < s.length; l++) {
            this.drawLine("line", d, q, b, o, this.foreground, false, "butt");
            d += this.diamondSize;
            b += t;
            o -= t
        }
        d = (this.left + this.x) - t;
        q = this.top;
        b = (this.left + (this.x / 2)) - t;
        o = (this.top + this.y) - t;
        for (var l = 0; l < s.length; l++) {
            this.drawLine("line", d, q, b, o, this.foreground, false, "butt");
            d -= this.diamondSize;
            b -= t;
            o -= t
        }
    };
    this.drawCorrelationPlot = function () {
        this.setCorrelationXYDimensions();
        this.drawCorrelationLayout();
        this.drawTitle();
        this.drawCorrelationDataPoints()
    };
    this.initializeCorrelationPlot = function () {
        this.drawCorrelationPlot()
    };
    this.initializeCorrelationPlot()
};
CanvasXpress.prototype.Scatter2D = function () {
    this.get2DYAxisWidth = function () {
        return this.yAxisShow ? this.getFontPt(this.axisTitleFont) + this.measureText(this.getMaxText(this.yAxisValues), this.axisTickFont) + (this.margin * 5) : this.margin
    };
    this.get2DXAxisHeight = function () {
        return this.xAxisShow ? this.getFontPt(this.axisTickFont) + this.getFontPt(this.axisTitleFont) + (this.margin * 5) : this.margin
    };
    this.set2DType = function () {
        if (this.graphType == "ScatterBubble2D" && this.xAxisIndices.length > 1 && this.xAxisIndices.length == this.yAxisIndices.length && this.xAxisIndices.length == this.zAxisIndices.length) {
            this.type2D = "XYZ"
        } else {
            if (this.xAxisIndices.length > 1 && this.xAxisIndices.length == this.yAxisIndices.length) {
                this.type2D = "XY"
            } else {
                if (this.xAxisIndices.length > this.yAxisIndices.length) {
                    this.type2D = "X"
                } else {
                    this.type2D = "Y"
                }
            }
        }
    };
    this.set2DText = function () {
        var b = this.xAxisTitle.length > this.yAxisTitle.length ? this.xAxisTitle : this.yAxisTitle;
        var a = this.layoutComb ? Math.min(this.layoutWidth, this.layoutHeight) : Math.min(this.width, this.height);
        this.setAxisFont(b, a)
    };
    this.setDecorationsLegendPositions = function () {
        if (this.showDecorations) {
            this.setDecorationLegendDimension();
            if (this.showLegend) {
                if (this.legendPosition == "bottom") {
                    this.decorationsPosition = "right"
                } else {
                    this.decorationsPosition = "bottom"
                }
            }
        }
    };
    this.set2DXYDimensions = function () {
        this.setDecorationsLegendPositions();
        this.setLegendDimensions();
        if (this.showLegend || this.showIndicators) {
            this.right = this.legendWidth
        }
        if (this.showDecorations && this.decorationsPosition == "right") {
            this.right += this.legendDecorationWidth + (this.margin * 2)
        }
        if (this.graphType == "ScatterBubble2D" && this.zAxisShow) {
            this.right += this.getFontPt(this.axisTitleFont) + this.margin
        } else {
            this.right += this.margin
        }
        this.left = this.get2DYAxisWidth();
        this.bottom = this.get2DXAxisHeight();
        if (this.showLegend || this.showIndicators) {
            this.bottom += this.legendHeight
        }
        if (this.showDecorations && this.decorationsPosition == "bottom") {
            this.bottom += this.legendDecorationHeight + (this.margin * 2)
        }
        this.x = this.autoExtend ? this.width - this.left : this.layoutComb ? this.layoutWidth - (this.left + this.right) : this.width - (this.left + this.right);
        this.y = this.layoutComb ? this.layoutHeight - (this.top + this.bottom) : this.height - (this.top + this.bottom);
        if (!this.layoutComb) {
            this.width = this.autoExtend ? this.left + this.x + this.right : this.layoutComb ? this.layoutWidth : this.width;
            this.resizeCanvas()
        }
        this.setScatterPointSize();
        this.setAxisUnits("xAxis");
        this.setAxisUnits("yAxis")
    };
    this.set2DWireFrame = function () {
        var e, g, b, f, a, h;
        e = this.offsetX + this.left;
        b = e + this.x;
        a = this.yAxisTickStyle == "dotted" ? "dottedLine" : "line";
        if (this.yAxisMinorTicks) {
            for (var d = 0; d < this.yAxisMinorValues.length; d++) {
                h = parseFloat(this.yAxisMinorValues[d]);
                g = (this.offsetY + this.top + this.y) - ((h - this.yAxisMin) * this.yAxisUnit);
                this.drawLine(a, e, g, b, g, this.yAxisTickColor, this.outlineWidth / 3, "butt")
            }
        }
        for (var d = 0; d < this.yAxisValues.length; d++) {
            h = parseFloat(this.yAxisValues[d]);
            g = (this.offsetY + this.top + this.y) - ((h - this.yAxisMin) * this.yAxisUnit);
            this.drawLine(a, e, g, b, g, this.yAxisTickColor, false, "butt")
        }
        g = this.offsetY + this.top;
        f = g + this.y;
        a = this.xAxisTickStyle == "dotted" ? "dottedLine" : "line";
        if (this.xAxisMinorTicks) {
            for (var d = 0; d < this.xAxisMinorValues.length; d++) {
                h = parseFloat(this.xAxisMinorValues[d]);
                e = (this.offsetX + this.left) + ((h - this.xAxisMin) * this.xAxisUnit);
                this.drawLine(a, e, g, e, f, this.yAxisTickColor, this.outlineWidth / 3, "butt")
            }
        }
        for (var d = 0; d < this.xAxisValues.length; d++) {
            h = parseFloat(this.xAxisValues[d]);
            e = (this.offsetX + this.left) + ((h - this.xAxisMin) * this.xAxisUnit);
            this.drawLine(a, e, g, e, f, this.yAxisTickColor, false, "butt")
        }
    };
    this.set2DLayout = function () {
        var d = this.offsetX + this.margin + (this.getFontPt(this.axisTitleFont) / 2);
        var e = this.offsetY + this.top + (this.y / 2);
        if (this.yAxisShow) {
            this.drawText(this.yAxisTitle, d, e, this.axisTitleFont, this.axisTitleColor, "center", "middle", -Math.PI / 2)
        }
        var b = this.measureText(this.getMaxText(this.yAxisValues), this.axisTickFont) / 2;
        d = this.offsetX + this.margin + this.getFontPt(this.axisTitleFont) + this.margin + this.margin + b;
        for (var a = 0; a < this.yAxisValues.length - 1; a++) {
            var g = parseFloat(this.yAxisValues[a]);
            var f;
            if (this.yAxisTransform && !this.yAxisTransformTicks) {
                f = sprintf("%." + this.yAxisDecs + "f", this.transformValue(this.yAxisTransform, g, true))
            } else {
                f = sprintf("%." + this.yAxisDecs + "f", g)
            }
            e = (this.offsetY + this.top + this.y) - ((g - this.yAxisMin) * this.yAxisUnit);
            if (this.yAxisShow) {
                this.drawText(f, d, e, this.axisTickFont, this.axisTickColor, "center", "middle")
            }
        }
        d = this.offsetX + this.left + (this.x / 2);
        e = this.offsetY + this.top + this.y + this.margin + this.margin + this.getFontPt(this.axisTickFont) + this.margin + this.margin + (this.getFontPt(this.axisTitleFont) / 2);
        if (this.xAxisShow) {
            this.drawText(this.xAxisTitle, d, e, this.axisTitleFont, this.axisTitleColor, "center", "middle")
        }
        e = this.offsetY + this.top + this.y + this.margin + this.margin + (this.getFontPt(this.axisTickFont) / 2);
        for (var a = 0; a < this.xAxisValues.length - 1; a++) {
            var g = parseFloat(this.xAxisValues[a]);
            var f;
            if (this.xAxisTransform && !this.xAxisTransformTicks) {
                f = sprintf("%." + this.xAxisDecs + "f", this.transformValue(this.xAxisTransform, g, true))
            } else {
                f = sprintf("%." + this.xAxisDecs + "f", g)
            }
            d = (this.offsetX + this.left) + ((g - this.xAxisMin) * this.xAxisUnit);
            if (this.xAxisShow) {
                this.drawText(f, d, e, this.axisTickFont, this.axisTickColor, "center", "middle")
            }
        }
        if (this.graphType == "ScatterBubble2D") {
            if (this.zAxisShow) {
                d = this.offsetX + this.left + this.x + this.margin;
                e = this.offsetY + this.top + (this.y / 2);
                this.drawText(this.zAxisTitle, d, e, this.axisTitleFont, this.axisTitleColor, "center", "bottom", Math.PI / 2)
            }
        }
        this.rectangle(this.offsetX + this.left, this.offsetY + this.top, this.x, this.y, false, this.foreground, "open")
    };
    this.draw2DDataPoints = function () {
        var q, G, F;
        var v, b, a;
        var D, h, f;
        if (this.type2D == "XYZ") {
            var t = this.xAxisIndices[this.xAxisCurrent];
            var H = this.yAxisIndices[this.yAxisCurrent];
            var l = this.zAxisIndices[this.zAxisCurrent];
            var I = t + ":" + H;
            if (this.xAxisTransform == "percentile") {
                q = this.getAxisRangeBySample(this.xAxisIndices);
                F = q[0];
                G = q[1]
            }
            if (this.yAxisTransform == "percentile") {
                v = this.getAxisRangeBySample(this.yAxisIndices);
                a = v[0];
                b = v[1]
            }
            D = this.getAxisRangeBySample(this.zAxisIndices);
            f = D[0];
            h = D[1];
            for (var w = 0; w < this.varIndices.length; w++) {
                var E = this.varIndices[w];
                var o = this.getDataAtPos(E, t, false, this.xAxisTransform, F, G);
                var n = this.getDataAtPos(E, H, false, this.yAxisTransform, a, b);
                var m = this.getDataAtPos(E, l, false, "percentile", f, h);
                var s = this.sizeBy ? this.dataSizes[w] : this.sizes[Math.floor(m / 10) + 1];
                var k = this.colorBy ? this.dataColors[w] : this.colors[0];
                var u = this.shapeBy ? this.dataShapes[w] : this.shapes[0];
                var C = (this.offsetX + this.left) + ((o - this.xAxisMin) * this.xAxisUnit);
                var e = (this.offsetY + this.top + this.y) - ((n - this.yAxisMin) * this.yAxisUnit);
                if (this.isVisibleSelectedDataPoint(E, I)) {
                    this.drawSelectedBackground(E, I, u, C, e, s, s, false, true);
                    this.addArea(this.drawShape(u, C, e, s, s, k, this.foreground, "closed", false, false, true), [E, t, H, l]);
                    if (this.isScatterLine && w < this.varIndices.length - 1 && this.isVisibleSelectedDataPoint(this.varIndices[w + 1], I)) {
                        E = this.varIndices[w + 1];
                        o = this.getDataAtPos(E, t, false, this.xAxisTransform, F, G);
                        n = this.getDataAtPos(E, H, false, this.yAxisTransform, a, b);
                        var B = (this.offsetX + this.left) + ((o - this.xAxisMin) * this.xAxisUnit);
                        var d = (this.offsetY + this.top + this.y) - ((n - this.yAxisMin) * this.yAxisUnit);
                        this.drawLine("line", C, e, B, d, this.colors[0])
                    }
                }
            }
        } else {
            if (this.type2D == "XY") {
                for (var A = 0; A < this.xAxisIndices.length; A++) {
                    var k = A % this.colors.length;
                    var t = this.xAxisIndices[A];
                    var H = this.yAxisIndices[A];
                    var l;
                    var I = t + ":" + H;
                    if (this.xAxisTransform == "percentile") {
                        q = this.getAxisRangeBySample(t);
                        F = q[0];
                        G = q[1]
                    }
                    if (this.yAxisTransform == "percentile") {
                        v = this.getAxisRangeBySample(H);
                        a = v[0];
                        b = v[1]
                    }
                    if (this.graphType == "ScatterBubble2D" && !this.sizeBy) {
                        if (this.zAxisIndices[this.zAxisCurrent]) {
                            l = this.zAxisIndices[this.zAxisCurrent];
                            D = this.getAxisRangeBySample(l);
                            f = D[0];
                            h = D[1]
                        }
                    }
                    for (var w = 0; w < this.varIndices.length; w++) {
                        var E = this.varIndices[w];
                        var o = this.getDataAtPos(E, t, false, this.xAxisTransform, F, G);
                        var n = this.getDataAtPos(E, H, false, this.yAxisTransform, a, b);
                        var m;
                        var u = this.shapeBy ? this.dataShapes[w] : this.shapes[0];
                        var s = this.scatterPointSize;
                        if (this.sizeBy) {
                            s = this.dataSizes[w]
                        } else {
                            if (this.graphType == "ScatterBubble2D") {
                                if (this.zAxisIndices[A]) {
                                    m = this.getDataAtPos(E, l, false, "percentile", f, h);
                                    s = this.sizes[Math.floor(m / 10) + 1]
                                }
                            }
                        }
                        var C = (this.offsetX + this.left) + ((o - this.xAxisMin) * this.xAxisUnit);
                        var e = (this.offsetY + this.top + this.y) - ((n - this.yAxisMin) * this.yAxisUnit);
                        if (this.isHistogram) {
                            var d = (this.offsetY + this.top + this.y) - e;
                            if (this.isVisibleSelectedDataPoint(E, I)) {
                                this.drawSelectedBackground(E, I, "rectangle", C - this.histogramBarWidth, e - this.histogramBarWidth, this.histogramBarWidth * 2, d, false, true);
                                this.addArea(this.rectangle(C - this.histogramBarWidth, e, this.histogramBarWidth * 2, d, this.colors[k], false, "closed", false, false, true), [E, t, H])
                            }
                        } else {
                            if (this.isVisibleSelectedDataPoint(E, I)) {
                                this.drawSelectedBackground(E, I, u, C, e, s, s, false, true);
                                if (this.graphType == "ScatterBubble2D") {
                                    if (this.colorBy) {
                                        this.addArea(this.drawShape(u, C, e, s, s, this.dataColors[w], this.colors[k], "closed", false, false, true), [E, t, H, l])
                                    } else {
                                        this.addArea(this.drawShape(u, C, e, s, s, this.colors[k], this.foreground, "closed", false, false, true), [E, t, H, l])
                                    }
                                } else {
                                    if (this.colorBy) {
                                        this.addArea(this.drawShape(u, C, e, s, s, this.dataColors[w], this.colors[k], "closed", false, false, true), [E, t, H])
                                    } else {
                                        this.addArea(this.drawShape(u, C, e, s, s, this.colors[k], this.foreground, "closed", false, false, true), [E, t, H])
                                    }
                                }
                                if (this.isScatterLine && w < this.varIndices.length - 1 && this.isVisibleSelectedDataPoint(this.varIndices[w + 1], I)) {
                                    E = this.varIndices[w + 1];
                                    o = this.getDataAtPos(E, t, false, this.xAxisTransform, F, G);
                                    n = this.getDataAtPos(E, H, false, this.yAxisTransform, a, b);
                                    var B = (this.offsetX + this.left) + ((o - this.xAxisMin) * this.xAxisUnit);
                                    var d = (this.offsetY + this.top + this.y) - ((n - this.yAxisMin) * this.yAxisUnit);
                                    this.drawLine("line", C, e, B, d, this.colors[A])
                                }
                            }
                        }
                    }
                }
            } else {
                if (this.type2D == "X") {
                    var r = this.yAxisIndices[this.yAxisCurrent];
                    if (this.yAxisTransform == "percentile") {
                        v = this.getAxisRangeBySample(r);
                        a = v[0];
                        b = v[1]
                    }
                    var g;
                    if (this.graphType == "ScatterBubble2D" && !this.sizeBy) {
                        if (this.zAxisIndices[this.zAxisCurrent]) {
                            g = this.zAxisIndices[this.zAxisCurrent];
                            D = this.getAxisRangeBySample(g);
                            f = D[0];
                            h = D[1]
                        }
                    }
                    for (var A = 0; A < this.xAxisIndices.length; A++) {
                        var k = A % this.colors.length;
                        var p = this.xAxisIndices[A];
                        var I = p + ":" + r;
                        if (this.xAxisTransform == "percentile") {
                            q = this.getAxisRangeBySample(p);
                            F = q[0];
                            G = q[1]
                        }
                        for (var w = 0; w < this.varIndices.length; w++) {
                            var E = this.varIndices[w];
                            var o = this.getDataAtPos(E, p, false, this.xAxisTransform, F, G);
                            var n = this.getDataAtPos(E, r, false, this.yAxisTransform, a, b);
                            var m;
                            var s = this.scatterPointSize;
                            var u = this.shapeBy ? this.dataShapes[w] : this.shapes[0];
                            if (this.sizeBy) {
                                s = this.dataSizes[w]
                            } else {
                                if (this.graphType == "ScatterBubble2D") {
                                    if (this.zAxisIndices[this.zAxisCurrent]) {
                                        m = this.getDataAtPos(E, g, false, "percentile", f, h);
                                        s = this.sizes[Math.floor(m / 10) + 1]
                                    }
                                }
                            }
                            var C = (this.offsetX + this.left) + ((o - this.xAxisMin) * this.xAxisUnit);
                            var e = (this.offsetY + this.top + this.y) - ((n - this.yAxisMin) * this.yAxisUnit);
                            if (this.isHistogram) {
                                var d = (this.offsetY + this.top + this.y) - e;
                                if (this.isVisibleSelectedDataPoint(E, I)) {
                                    this.drawSelectedBackground(E, I, "rectangle", C - this.histogramBarWidth, e - this.histogramBarWidth, this.histogramBarWidth * 2, d, false, true);
                                    this.addArea(this.rectangle(C - this.histogramBarWidth, e, this.histogramBarWidth * 2, d, this.colors[k], false, "closed", false, false, true), [E, p, r])
                                }
                            } else {
                                if (this.isVisibleSelectedDataPoint(E, I)) {
                                    this.drawSelectedBackground(E, I, u, C, e, s, s, false, true);
                                    if (this.graphType == "ScatterBubble2D") {
                                        if (this.colorBy) {
                                            this.addArea(this.drawShape(u, C, e, s, s, this.dataColors[w], this.colors[k], "closed", false, false, true), [E, p, r, g])
                                        } else {
                                            this.addArea(this.drawShape(u, C, e, s, s, this.colors[k], this.foreground, "closed", false, false, true), [E, p, r, g])
                                        }
                                    } else {
                                        if (this.colorBy) {
                                            this.addArea(this.drawShape(u, C, e, s, s, this.dataColors[w], this.colors[k], "closed", false, false, true), [E, p, r])
                                        } else {
                                            this.addArea(this.drawShape(u, C, e, s, s, this.colors[k], this.foreground, "closed", false, false, true), [E, p, r])
                                        }
                                    }
                                    if (this.isScatterLine && w < this.varIndices.length - 1 && this.isVisibleSelectedDataPoint(this.varIndices[w + 1], I)) {
                                        E = this.varIndices[w + 1];
                                        o = this.getDataAtPos(E, p, false, this.xAxisTransform, F, G);
                                        n = this.getDataAtPos(E, r, false, this.yAxisTransform, a, b);
                                        var B = (this.offsetX + this.left) + ((o - this.xAxisMin) * this.xAxisUnit);
                                        var d = (this.offsetY + this.top + this.y) - ((n - this.yAxisMin) * this.yAxisUnit);
                                        this.drawLine("line", C, e, B, d, this.colors[A])
                                    }
                                }
                            }
                        }
                    }
                } else {
                    var r = this.xAxisIndices[this.xAxisCurrent];
                    if (this.xAxisTransform == "percentile") {
                        q = this.getAxisRangeBySample(r);
                        F = q[0];
                        G = q[1]
                    }
                    var g;
                    if (this.graphType == "ScatterBubble2D" && !this.sizeBy) {
                        if (this.zAxisIndices[this.zAxisCurrent]) {
                            g = this.zAxisIndices[this.zAxisCurrent];
                            D = this.getAxisRangeBySample(g);
                            f = D[0];
                            h = D[1]
                        }
                    }
                    for (var A = 0; A < this.yAxisIndices.length; A++) {
                        var k = this.colors[A % this.colors.length];
                        var p = this.yAxisIndices[A];
                        var I = r + ":" + p;
                        if (this.yAxisTransform == "percentile") {
                            v = this.getAxisRangeBySample(p);
                            a = v[0];
                            b = v[1]
                        }
                        for (var w = 0; w < this.varIndices.length; w++) {
                            var E = this.varIndices[w];
                            var o = this.getDataAtPos(E, r, false, this.xAxisTransform, F, G);
                            var n = this.getDataAtPos(E, p, false, this.yAxisTransform, a, b);
                            var m;
                            var u = this.shapeBy ? this.dataShapes[w] : this.shapes[0];
                            var s = this.scatterPointSize;
                            if (this.colorBy) {
                                k = this.dataColors[w]
                            }
                            if (this.sizeBy) {
                                s = this.dataSizes[w]
                            } else {
                                if (this.graphType == "ScatterBubble2D") {
                                    if (this.zAxisIndices[this.yAxisCurrent]) {
                                        m = this.getDataAtPos(E, g, false, "percentile", f, h);
                                        s = this.sizes[Math.floor(m / 10) + 1]
                                    }
                                }
                            }
                            var C = (this.offsetX + this.left) + ((o - this.xAxisMin) * this.xAxisUnit);
                            var e = (this.offsetY + this.top + this.y) - ((n - this.yAxisMin) * this.yAxisUnit);
                            if (this.isHistogram) {
                                var d = (this.offsetY + this.top + this.y) - e;
                                if (this.isVisibleSelectedDataPoint(E, I)) {
                                    this.drawSelectedBackground(E, I, "rectangle", C - this.histogramBarWidth, e - this.histogramBarWidth, this.histogramBarWidth * 2, d, false, true);
                                    this.addArea(this.rectangle(C - this.histogramBarWidth, e, this.histogramBarWidth * 2, d, k, false, "closed", false, false, true), [E, r, p])
                                }
                            } else {
                                if (this.isVisibleSelectedDataPoint(E, I)) {
                                    this.drawSelectedBackground(E, I, u, C, e, s, s, false, true);
                                    if (this.graphType == "ScatterBubble2D") {
                                        this.addArea(this.drawShape(u, C, e, s, s, k, this.foreground, "closed", false, false, true), [E, r, p, g])
                                    } else {
                                        this.addArea(this.drawShape(u, C, e, s, s, k, this.foreground, "closed", false, false, true), [E, r, p])
                                    }
                                    if (this.isScatterLine && w < this.varIndices.length - 1 && this.isVisibleSelectedDataPoint(this.varIndices[w + 1], I)) {
                                        E = this.varIndices[w + 1];
                                        o = this.getDataAtPos(E, r, false, this.xAxisTransform, F, G);
                                        n = this.getDataAtPos(E, p, false, this.yAxisTransform, a, b);
                                        var B = (this.offsetX + this.left) + ((o - this.xAxisMin) * this.xAxisUnit);
                                        var d = (this.offsetY + this.top + this.y) - ((n - this.yAxisMin) * this.yAxisUnit);
                                        this.drawLine("line", C, e, B, d, this.colors[A])
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    };
    this.draw2DLegend = function () {
        if (this.showLegend) {
            this.drawScatterLegend()
        }
        if (this.showDecorations) {
            this.drawDecorationLegend()
        }
    };
    this.set2DprivateParams = function () {
        this.set2DType();
        this.set2DText()
    };
    this.draw2DPlot = function () {
        if (this.xAxis && this.yAxis) {
            this.setDataColorShapeSize()
        }
        this.set2DprivateParams();
        this.set2DXYDimensions();
        if (this.xAxis && this.yAxis) {
            this.drawPlotWindow();
            this.set2DWireFrame();
            this.draw2DDataPoints();
            this.maskPlotArea();
            this.set2DLayout();
            this.drawTitle();
            this.drawDecorationData();
            this.draw2DLegend()
        }
    };
    this.initializeScatter2D = function () {
        if (this.layoutValid) {
            this.drawLayoutCompartments(this.draw2DPlot)
        } else {
            this.draw2DPlot()
        }
    };
    this.initializeScatter2D()
};
CanvasXpress.prototype.Scatter3D = function () {
    this.set3DText = function () {
        this.setAxisFont(false, this.x)
    };
    this.set3DXYDimensions = function () {
        if (this.height < this.width) {
            this.height = this.width
        }
        this.x = this.width - this.top;
        this.y = this.height - this.top;
        this.left = this.top;
        if (this.showLegend || this.showIndicators) {
            this.setLegendDimensions();
            if (this.autoExtend) {
                this.width = this.x + this.legendWidth;
                this.height = this.y + this.legendHeight
            } else {
                var a = Math.max(this.legendWidth, this.legendHeight);
                this.x -= a;
                this.y -= a;
                this.width = this.x + this.legendWidth;
                this.height = this.y + this.legendHeight
            }
        }
        this.resizeCanvas();
        this.setScatterPointSize()
    };
    this.draw3DLayout = function (h, a, m, j, b, n, g, d) {
        var f, p, e, o, l;
        for (var k = 0; k < h.length; k++) {
            l = this.get3DTransfrom(h[k], a[k], m[k]);
            f = l[0] + this.left;
            p = l[1] + this.top;
            l = this.get3DTransfrom(j[k], b[k], n[k]);
            e = l[0] + this.left;
            o = l[1] + this.top;
            if (d == "dotted") {
                this.drawLine("dottedLine", f, p, e, o, g[k], false, "butt")
            } else {
                this.drawLine("line", f, p, e, o, g[k], false, "butt")
            }
        }
    };
    this.set3DXLayout = function () {
        var t = [],
            d = [],
            m = [],
            r = [],
            b = [],
            k = [],
            e = [];
        var q, l, n, a, u, p, j, h;
        var f = this.len / 15;
        var o = (15 * -f) + f;
        var g = this.xAxisTitle ? this.xAxisTitle : this.data.y.smps[this.xAxisIndex];
        n = o;
        q = 0;
        l = 0;
        while (n < this.len) {
            if ((q + 1) % 3) {
                t[l] = n;
                d[l] = -this.len;
                m[l] = this.len;
                r[l] = n;
                b[l] = this.len;
                k[l] = this.len;
                e.push(this.xAxisTickColor);
                l++;
                t[l] = -this.len;
                d[l] = n;
                m[l] = this.len;
                r[l] = this.len;
                b[l] = n;
                k[l] = this.len;
                e.push(this.xAxisTickColor);
                l++
            }
            n += f;
            q++
        }
        this.draw3DLayout(t, d, m, r, b, k, e, this.xAxisTickStyle);
        t = [], d = [], m = [], r = [], b = [], k = [], e = [];
        n = o;
        q = 0;
        l = 0;
        u = this.xAxisIncr;
        while (n < this.len) {
            if (!((q + 1) % 3)) {
                t[l] = n;
                d[l] = -this.len;
                m[l] = this.len;
                r[l] = n;
                b[l] = this.len;
                k[l] = this.len;
                e.push(this.foreground);
                l++;
                t[l] = -this.len;
                d[l] = n;
                m[l] = this.len;
                r[l] = this.len;
                b[l] = n;
                k[l] = this.len;
                e.push(this.foreground);
                a = this.get3DTransfrom(n, this.len, -this.len);
                j = a[0] + this.left;
                h = a[1] + this.top;
                p = sprintf("%." + this.xAxisDecs + "f", this.xAxisMin + u);
                u += this.xAxisIncr;
                if (this.xAxisShow) {
                    this.drawText(p, j, h + 5, this.axisTickFont, this.axisTickColor, "right", false, -Math.PI / 2)
                }
                l++
            }
            n += f;
            q++
        }
        this.draw3DLayout(t, d, m, r, b, k, e, this.xAxisTickStyle);
        if (this.xAxisShow) {
            this.draw3DAxisTitle(o - (f * 2), this.len + f, this.len + (f * 4), this.len + (f * 4), -this.len - (f * 4), -this.len - (f * 4), g, "x")
        }
        t = [-this.len, -this.len, this.len, -this.len];
        d = [-this.len, -this.len, -this.len, this.len];
        m = [this.len, this.len, this.len, this.len];
        r = [-this.len, this.len, this.len, this.len];
        b = [this.len, -this.len, this.len, this.len];
        k = [this.len, this.len, this.len, this.len];
        e = [this.foreground, this.foreground, this.foreground, this.foreground];
        this.draw3DLayout(t, d, m, r, b, k, e, this.xAxisTickStyle)
    };
    this.set3DYLayout = function () {
        var v = [],
            d = [],
            m = [],
            u = [],
            b = [],
            k = [],
            e = [];
        var r, l, o, a, w, q, j, h;
        var f = this.len / 15;
        var t = this.yAxisLen / 30;
        var p = (15 * -f) + f;
        var g = this.yAxisTitle ? this.yAxisTitle : this.data.y.smps[this.yAxisIndex];
        var n = [];
        o = p;
        r = 0;
        l = 0;
        w = this.yAxisIncr;
        while (o < this.len) {
            if ((r + 1) % 3) {
                v[l] = -this.len;
                d[l] = -this.len;
                m[l] = o;
                u[l] = -this.len;
                b[l] = this.len;
                k[l] = o;
                e.push(this.yAxisTickColor);
                l++;
                v[l] = -this.len;
                d[l] = o;
                m[l] = -this.len;
                u[l] = -this.len;
                b[l] = o;
                k[l] = this.len;
                e.push(this.yAxisTickColor);
                l++
            } else {
                n.push(w);
                w += this.yAxisIncr
            }
            o += f;
            r++
        }
        this.draw3DLayout(v, d, m, u, b, k, e, this.yAxisTickStyle);
        v = [], d = [], m = [], u = [], b = [], k = [], e = [];
        o = p;
        r = 0;
        l = 0;
        while (o < this.len) {
            if (!((r + 1) % 3)) {
                v[l] = -this.len;
                d[l] = -this.len;
                m[l] = o;
                u[l] = -this.len;
                b[l] = this.len;
                k[l] = o;
                e.push(this.foreground);
                l++;
                v[l] = -this.len;
                d[l] = o;
                m[l] = -this.len;
                u[l] = -this.len;
                b[l] = o;
                k[l] = this.len;
                e.push(this.foreground);
                a = this.get3DTransfrom(-this.len, o, -this.len);
                j = a[0] + this.left;
                h = a[1] + this.top;
                w = n.pop();
                q = sprintf("%." + this.yAxisDecs + "f", this.yAxisMin + w);
                if (this.yAxisShow) {
                    if (this.yRotate >= 45 && this.xRotate >= 45) {
                        this.drawText(q, j, h + 5, this.axisTickFont, this.axisTickColor, "right", false, -Math.PI / 2)
                    } else {
                        this.drawText(q, j - 5, h, this.axisTickFont, this.axisTickColor, "right")
                    }
                }
                l++
            }
            o += f;
            r++
        }
        this.draw3DLayout(v, d, m, u, b, k, e, this.yAxisTickStyle);
        if (this.yAxisShow) {
            this.draw3DAxisTitle(-this.len - (f * 4), -this.len - (f * 4), p - (f * 2), this.len + f, -this.len - (f * 4), -this.len - (f * 4), g, "y")
        }
        v = [-this.len, -this.len, -this.len, -this.len];
        d = [-this.len, -this.len, -this.len, this.len];
        m = [-this.len, this.len, this.len, this.len];
        u = [-this.len, -this.len, -this.len, -this.len];
        b = [this.len, -this.len, this.len, this.len];
        k = [-this.len, -this.len, this.len, -this.len];
        e = [this.foreground, this.foreground, this.foreground, this.foreground];
        this.draw3DLayout(v, d, m, u, b, k, e, this.yAxisTickStyle)
    };
    this.set3DZLayout = function () {
        var t = [],
            d = [],
            m = [],
            r = [],
            b = [],
            k = [],
            e = [];
        var q, l, n, a, u, p, j, h;
        var f = this.len / 15;
        var o = (15 * -f) + f;
        var g = this.zAxisTitle ? this.zAxisTitle : this.data.y.smps[this.zAxisIndex];
        n = o;
        q = 0;
        l = 0;
        while (n < this.len) {
            if ((q + 1) % 3) {
                t[l] = -this.len;
                d[l] = this.len;
                m[l] = n;
                r[l] = this.len;
                b[l] = this.len;
                k[l] = n;
                e.push(this.zAxisTickColor);
                l++;
                t[l] = n;
                d[l] = this.len;
                m[l] = -this.len;
                r[l] = n;
                b[l] = this.len;
                k[l] = this.len;
                e.push(this.zAxisTickColor);
                l++
            }
            n += f;
            q++
        }
        this.draw3DLayout(t, d, m, r, b, k, e, this.zAxisTickStyle);
        t = [], d = [], m = [], r = [], b = [], k = [], e = [];
        n = o;
        q = 0;
        l = 0;
        u = this.zAxisIncr;
        while (n < this.len) {
            if (!((q + 1) % 3)) {
                t[l] = -this.len;
                d[l] = this.len;
                m[l] = n;
                r[l] = this.len;
                b[l] = this.len;
                k[l] = n;
                e.push(this.foreground);
                l++;
                t[l] = n;
                d[l] = this.len;
                m[l] = -this.len;
                r[l] = n;
                b[l] = this.len;
                k[l] = this.len;
                e.push(this.foreground);
                a = this.get3DTransfrom(this.len, this.len, n);
                j = a[0] + this.left;
                h = a[1] + this.top;
                p = sprintf("%." + this.zAxisDecs + "f", this.zAxisMin + u);
                u += this.zAxisIncr;
                if (this.zAxisShow) {
                    if (this.xRotate >= 45) {
                        if (this.yRotate >= 45) {
                            this.drawText(p, j + 5, h, this.axisTickFont, this.axisTickColor, "left")
                        } else {
                            this.drawText(p, j, h + 5, this.axisTickFont, this.axisTickColor, "right", false, -Math.PI / 2)
                        }
                    } else {
                        this.drawText(p, j + 5, h, this.axisTickFont, this.axisTickColor, "left")
                    }
                }
                l++
            }
            n += f;
            q++
        }
        this.draw3DLayout(t, d, m, r, b, k, e, this.zAxisTickStyle);
        if (this.zAxisShow) {
            this.draw3DAxisTitle(this.len + (f * 6), this.len + (f * 6), this.len + (f * 6), this.len + (f * 6), o - (f * 2), this.len + f, g, "z")
        }
        t = [-this.len, -this.len, this.len, -this.len];
        d = [this.len, this.len, this.len, this.len];
        m = [-this.len, this.len, -this.len, -this.len];
        r = [-this.len, this.len, this.len, this.len];
        b = [this.len, this.len, this.len, this.len];
        k = [this.len, this.len, this.len, -this.len];
        e = [this.foreground, this.foreground, this.foreground, this.foreground];
        this.draw3DLayout(t, d, m, r, b, k, e, this.zAxisTickStyle)
    };
    this.draw3DAxisTitle = function (d, b, p, o, j, g, q, e) {
        var a = this.measureText(q);
        var s = a / 2;
        var k = q.split("");
        var n = (d + b) / 2;
        var m = (p + o) / 2;
        var l = (j + g) / 2;
        var r = this.xRotate >= 45 || this.yRotate >= 45 ? 4 : 0;
        if (this.xRotate >= 45 || this.yRotate >= 45) {
            a += k.length * 4;
            s = a / 2
        }
        if (e == "x") {
            n -= s;
            for (var f = 0; f < k.length; f++) {
                var h = this.get3DTransfrom(n, m, l);
                this.drawText(k[f], this.offsetX + h[0], this.offsetY + h[1], this.axisTitleFont, this.axisTitleColor, "left", "top");
                n += this.measureText(k[f]) + r
            }
        } else {
            if (e == "y") {
                m += s;
                for (var f = 0; f < k.length; f++) {
                    var h = this.get3DTransfrom(n, m, l);
                    this.drawText(k[f], this.offsetX + h[0], this.offsetY + h[1], this.axisTitleFont, this.axisTitleColor, "left", "bottom", -Math.PI / 2);
                    m -= this.measureText(k[f]) + r
                }
            } else {
                if (e == "z") {
                    l -= s;
                    for (var f = 0; f < k.length; f++) {
                        var h = this.get3DTransfrom(n, m, l);
                        this.drawText(k[f], this.offsetX + h[0], this.offsetY + h[1], this.axisTitleFont, this.axisTitleColor, "left", "bottom");
                        l += this.measureText(k[f]) + r
                    }
                }
            }
        }
    };
    this.set3DLayout = function () {
        this.set3DXLayout();
        this.set3DYLayout();
        this.set3DZLayout()
    };
    this.set3DAxes = function () {
        var b = ["x", "y", "z"];
        for (var a = 0; a < b.length; a++) {
            var d = this.smpIndices;
            this[b[a] + "AxisIndex"] = this.getSampleIndices([this[b[a] + "Axis"]])[0];
            this.setSamplesVisible([this[b[a] + "AxisIndex"]]);
            this.setRangeData();
            this[b[a] + "AxisMin"] = this.minData;
            this[b[a] + "AxisMax"] = this.maxData;
            this[b[a] + "AxisIncr"] = this.getAxisIncrements(this[b[a] + "AxisMin"], this[b[a] + "AxisMax"], 10);
            this[b[a] + "AxisDecs"] = this.getAxisDecimals(this[b[a] + "AxisIncr"]);
            this[b[a] + "AxisMin"] = this.getAxisMin(this.minData, this[b[a] + "AxisIncr"]);
            this[b[a] + "AxisMax"] = this[b[a] + "AxisMin"] + (this[b[a] + "AxisIncr"] * 10);
            this[b[a] + "AxisRange"] = this[b[a] + "AxisMax"] - this[b[a] + "AxisMin"];
            this[b[a] + "AxisUnit"] = this.len * 2 / this[b[a] + "AxisRange"];
            this[b[a] + "AxisOffset"] = (this[b[a] + "AxisMin"] + (this[b[a] + "AxisRange"] / 2)) * this[b[a] + "AxisUnit"];
            this.setSamplesVisible(d)
        }
    };
    this.set3DDataPoints = function () {
        this.dataPoints = [];
        for (var d = 0; d < this.varIndices.length; d++) {
            var e = this.varIndices[d];
            var a = this.getDataAtPos(e, this.xAxisIndex);
            var g = this.getDataAtPos(e, this.yAxisIndex);
            var f = this.getDataAtPos(e, this.zAxisIndex);
            var b = this.get3DTransfrom((a * this.xAxisUnit) - this.xAxisOffset, this.yAxisOffset - (g * this.yAxisUnit), (f * this.zAxisUnit) - this.zAxisOffset);
            this.dataPoints.push(b)
        }
    };
    this.draw3DDataPoints = function () {
        var l = this.xAxisIndex + ":" + this.yAxisIndex + ":" + this.zAxisIndex;
        if (this.isScatterLine) {
            for (var g = 0; g < this.varIndices.length - 1; g++) {
                if (this.isVisibleSelectedDataPoint(g, l) && this.isVisibleSelectedDataPoint(g + 1, l)) {
                    var d = this.offsetX + this.left + this.dataPoints[g][0];
                    var k = this.offsetY + this.top + this.dataPoints[g][1];
                    var a = this.offsetX + this.left + this.dataPoints[g + 1][0];
                    var j = this.offsetY + this.top + this.dataPoints[g + 1][1];
                    this.drawLine("line", d, k, a, j, this.colors[0])
                }
            }
        }
        var h = [];
        var f = [];
        for (var g = 0; g < this.dataPoints.length; g++) {
            f.push(g);
            h.push(Math.sqrt((this.dataPoints[g][0] * this.dataPoints[g][0]) + (this.dataPoints[g][1] * this.dataPoints[g][1]) + (this.dataPoints[g][2] * this.dataPoints[g][2])))
        }
        f.sort(function (o, i) {
            return h[i] - h[o]
        });
        for (var m = 0; m < this.dataPoints.length; m++) {
            var g = f[m];
            if (this.dataPoints[g][0] > this.left && this.dataPoints[g][0] < (this.left + this.x) && this.dataPoints[g][1] > this.top && this.dataPoints[g][1] < (this.top + this.y)) {
                var e = this.colorBy ? this.dataColors[g] : this.colors[0];
                var n = this.shapeBy ? this.dataShapes[g] : this.shapes[0];
                var b = this.sizeBy ? this.dataSizes[g] : this.scatterPointSize;
                if (this.isVisibleSelectedDataPoint(g, l)) {
                    this.drawSelectedBackground(g, l, n, this.offsetX + this.left + this.dataPoints[g][0], this.offsetY + this.top + this.dataPoints[g][1], b, b);
                    this.addArea(this.drawShape(n, this.offsetX + this.left + this.dataPoints[g][0], this.offsetY + this.top + this.dataPoints[g][1], b, b, e, this.foreground, "closed"), [g, this.xAxisIndex, this.yAxisIndex, this.zAxisIndex])
                }
            }
        }
    };
    this.set3DprivateParams = function () {
        this.set3DText();
        this.set3DParams()
    };
    this.draw3DPlot = function () {
        this.setDataColorShapeSize();
        this.set3DXYDimensions();
        this.set3DprivateParams();
        this.set3DRotation();
        this.set3DAxes();
        this.set3DLayout();
        this.set3DDataPoints();
        this.drawTitle();
        this.draw3DDataPoints();
        this.drawScatterLegend()
    };
    this.initializeScatter3D = function () {
        if (this.layoutValid) {
            this.drawLayoutCompartments(this.draw3DPlot)
        } else {
            this.draw3DPlot()
        }
    };
    this.initializeScatter3D()
};
CanvasXpress.prototype.oneDPlot = function () {
    this.getShapeSize = function () {
        if (this.x < 100 || this.y < 100) {
            return this.rowBlockSize / 6
        } else {
            if (this.x < 200 || this.y < 200) {
                return this.rowBlockSize / 3
            } else {
                return this.rowBlockSize / 1.5
            }
        }
    };
    this.setRowColBlocks = function () {
        var d = this.isGroupedData ? this.grpIndices.length : this.smpIndices.length;
        this.rowBlocks = 0;
        if (this.graphType == "Line" || this.graphType == "Area" || this.graphType == "Heatmap" || this.graphType == "Stacked" || this.graphType == "StackedPercent") {
            this.rowBlocks = d;
            if (this.graphType == "Heatmap") {
                this.colBlocks = this.varIndices.length
            } else {
                this.colBlocks = 1
            }
        } else {
            if (this.graphType == "BarLine" && this.data.a) {
                var b = this.getVariablesVisibleByAxis("xAxis");
                var a = this.getVariablesVisibleByAxis("xAxis2");
                if (b.length >= a.length) {
                    this.rowBlocks = b.length * d
                } else {
                    this.rowBlocks = a.length * d
                }
                this.colBlocks = 1
            } else {
                this.rowBlocks = this.varIndices.length * d;
                this.colBlocks = 1
            }
        }
    };
    this.set1DVarSmpLabelFont = function () {
        if (this.autoScaleFont) {
            var a;
            if (this.data.l && this.data.l.vars && this.data.l.smps && this.graphOrientation == "vertical") {
                a = this.scaleTextToSize(this.layoutMaxSamples * 2)
            } else {
                a = this.isGraphTime ? this.scaleTextToSize(this.timeValues.length * 2) : this.scaleTextToSize(this.smpIndices.length * 2)
            }
            a = Math.min(a * this.scaleSmpLabelFontFactor, this.maxTextSize);
            this.smpLabelFont = (a) + "Pt " + this.fontName;
            a = this.scaleTextToSize(this.varIndices.length * 2);
            a = Math.min(a * this.scaleVarLabelFontFactor, this.maxTextSize);
            this.varLabelFont = (a) + "Pt " + this.fontName
        } else {
            this.smpLabelFont = parseInt(this.smpLabelFontSize) + "Pt " + this.fontName;
            this.varLabelFont = parseInt(this.varLabelFontSize) + "Pt " + this.fontName
        }
    };
    this.set1DText = function () {
        var a;
        if (!this.layoutComb) {
            if (this.data.y.desc) {
                if (this.data.y.desc.length > 1) {
                    if (this.data.y.desc[0].length > this.data.y.desc[1].length) {
                        a = this.data.y.desc[0]
                    } else {
                        a = this.data.y.desc[1]
                    }
                } else {
                    a = this.data.y.desc[0]
                }
            } else {
                a = "NA"
            }
        }
        if (this.layoutComb) {
            this.setAxisFont(this.minLayoutAxisTitle, this.minLayoutAxis)
        } else {
            if (this.graphOrientation == "vertical") {
                this.setAxisFont(a, this.y)
            } else {
                this.setAxisFont(a, this.x)
            }
        }
    };
    this.getSampleTitleHeight = function () {
        if (this.smpTitle) {
            return parseInt(this.smpTitleFontSize * this.scaleSmpTitleFontFactor) + this.margin
        } else {
            return 0
        }
    };
    this.getSampleLabelLength = function () {
        if (this.showSampleNames) {
            if (this.isGraphTime) {
                return this.measureText(dateFormat(this.timeFormat), this.smpLabelFont) + (this.margin * 2)
            } else {
                if (this.maxSmpName > this.maxSmpStringLen) {
                    return this.measureText(this.maxSmpNameStr.substring(0, this.maxSmpStringLen), this.smpLabelFont) + (this.margin * 2)
                } else {
                    return this.measureText(this.maxSmpNameStr, this.smpLabelFont) + (this.margin * 2)
                }
            }
        } else {
            return 0
        }
    };
    this.getSmpOverlaysLength = function () {
        var a = 0;
        if (this.showOverlays) {
            if (!this.isGroupedData) {
                for (var b = 0; b < this.smpOverlays.length; b++) {
                    if (this.data.x.hasOwnProperty(this.smpOverlays[b])) {
                        a++
                    }
                }
                if (a > 0) {
                    a = (a * this.overlaysWidth) + this.margin
                }
            }
        }
        return a
    };
    this.getVarOverlaysHeight = function () {
        var a = 0;
        if (this.showOverlays) {
            if (this.graphType == "Heatmap") {
                for (var b = 0; b < this.varOverlays.length; b++) {
                    if (this.data.z.hasOwnProperty(this.varOverlays[b])) {
                        a++
                    }
                }
                if (a > 0) {
                    a = (a * this.overlaysWidth) + this.margin
                }
            }
        }
        return a
    };
    this.getVarOverlaysLength = function () {
        var a = 0;
        if (this.showOverlays) {
            if (this.graphType == "Heatmap") {
                for (var b = 0; b < this.varOverlays.length; b++) {
                    if (this.data.z.hasOwnProperty(this.varOverlays[b])) {
                        a = Math.max(a, this.measureText(this.varOverlays[b], this.overlayFont) + this.margin)
                    }
                }
            }
        }
        return a
    };
    this.getAxisTitleTickLength = function () {
        if (this.graphOrientation == "vertical") {
            var a;
            if (this.graphType == "Candlestick") {
                if (this.showVolume) {
                    a = Math.max(this.measureText(this.getMaxText(this.xAxisValues), this.axisTickFont), this.measureText(this.getMaxText(this.xAxis2Values), this.axisTickFont))
                } else {
                    if (this.summaryType == "candle") {
                        a = this.measureText(this.getMaxText(this.xAxisValues), this.axisTickFont)
                    } else {
                        a = this.measureText(this.getMaxText(this.xAxis2Values), this.axisTickFont)
                    }
                }
            } else {
                a = this.measureText(this.getMaxText(this.xAxisValues), this.axisTickFont)
            }
            return this.getFontPt(this.axisTitleFont) + a + (this.margin * 3)
        } else {
            return this.getFontPt(this.axisTitleFont) + this.getFontPt(this.axisTickFont) + (this.margin * 5)
        }
    };
    this.getHeatmapIndicatorHeight = function () {
        return this.indicatorHeight + (this.margin * 4) + this.getFontPt(this.legendFont)
    };
    this.getLegendHeight = function (d) {
        if (this.graphType == "Heatmap") {
            return 0
        } else {
            if (this.colorBy && this.graphType == "Bar") {
                if (this.legendColorsN > 0) {
                    return this.legendColorHeight + this.margin
                } else {
                    return this.legendPosition == "right" ? this.legendColorIndicatorWidth : this.legendColorIndicatorHeight
                }
            } else {
                var a = Math.min(this.getFontPt(this.legendFont), this.maxTextSize);
                var b = this.layoutComb && !d ? this.layoutMaxLegend : this.varIndices.length;
                return (b * a * 1.5) + (a / 2) + this.margin
            }
        }
    };
    this.getLegendWidth = function () {
        if (this.graphType == "Heatmap") {
            return 0
        } else {
            if (this.colorBy && this.graphType == "Bar") {
                if (this.legendColorsN > 0) {
                    return this.legendColorWidth + this.margin
                } else {
                    return this.legendPosition == "right" ? this.legendColorIndicatorHeight : this.legendColorIndicatorWidth
                }
            } else {
                var b = Math.min(this.getFontPt(this.legendFont), this.maxTextSize);
                var a = this.layoutComb ? this.layoutMaxLegendLabel : this.maxVarNameStr;
                var d = a.length > this.maxVarStringLen ? a.substring(0, this.maxVarStringLen) : a;
                return (b * 2) + this.measureText(d, this.legendFont) + (this.margin * 6)
            }
        }
    };
    this.getVarTreeHeight = function () {
        var a = 0;
        if (this.graphType == "Heatmap" && this.showVarDendrogram && this.varDendrogram) {
            a = ((this.varDendrogram.depth + 1) * this.dendrogramSpace) + this.margin
        }
        return a
    };
    this.getSmpTreeWidthHeight = function () {
        var a = 0;
        if (this.showSmpDendrogram && this.smpDendrogram && !this.isGroupedData) {
            a = ((this.smpDendrogram.depth + 1) * this.dendrogramSpace) + this.margin
        }
        return a
    };
    this.set1DXYDimensionsLeft = function () {
        if (this.graphOrientation == "vertical") {
            this.left = this.margin + this.getAxisTitleTickLength()
        } else {
            if (this.smpDendrogramPosition == "right") {
                this.left = this.margin + this.getSampleTitleHeight() + this.getSampleLabelLength() + this.getSmpOverlaysLength()
            } else {
                this.left = this.margin + this.getSampleTitleHeight() + this.getSmpTreeWidthHeight() + this.getSampleLabelLength() + this.getSmpOverlaysLength()
            }
        }
    };
    this.set1DXYDimensionsRight = function () {
        if (this.graphOrientation == "vertical") {
            this.right = this.margin + this.getAxisTitleTickLength()
        } else {
            if (this.smpDendrogramPosition == "right") {
                this.right = this.margin + this.getSmpTreeWidthHeight()
            } else {
                this.right = this.margin
            }
        }
        if (this.showLegend && this.legendPosition == "right") {
            this.right += this.getLegendWidth()
        }
        if (this.showOverlays && this.graphType == "Heatmap") {
            this.right += this.getVarOverlaysLength()
        }
    };
    this.set1DXYDimensionsTop = function () {
        this.top = this.margin;
        if (this.title) {
            this.top += this.titleHeight + this.margin;
            if (this.subtitle) {
                this.top += this.subtitleHeight + this.margin
            }
        }
        if (this.graphOrientation == "vertical") {
            if (this.smpDendrogramPosition != "bottom") {
                this.top += this.getSmpTreeWidthHeight()
            }
        } else {
            if (this.graphType == "Heatmap") {
                if (this.varDendrogramPosition == "bottom") {
                    this.top += this.getHeatmapIndicatorHeight()
                } else {
                    this.top += this.getHeatmapIndicatorHeight() + this.getVarTreeHeight()
                }
            } else {
                this.top += this.getAxisTitleTickLength()
            }
        }
    };
    this.set1DXYDimensionsBottom = function () {
        if (this.graphOrientation == "vertical") {
            this.bottom = this.margin + this.getSampleTitleHeight() + this.getSampleLabelLength() + this.getSmpOverlaysLength();
            if (this.smpDendrogramPosition == "bottom") {
                this.bottom += this.getSmpTreeWidthHeight()
            }
            if (this.showLegend && this.legendPosition == "bottom") {
                this.bottom += this.getLegendHeight()
            }
        } else {
            if (this.graphType == "Heatmap") {
                this.bottom = this.margin + this.measureText(this.maxVarNameStr, this.varLabelFont) + this.margin + this.getVarOverlaysHeight();
                if (this.varDendrogramPosition == "bottom") {
                    this.bottom += this.getVarTreeHeight()
                }
            } else {
                this.bottom = this.margin + this.getAxisTitleTickLength();
                if (this.showLegend && this.legendPosition == "bottom") {
                    this.bottom += this.getLegendHeight()
                }
            }
        }
    };
    this.set1DXYDimensions = function (a) {
        if (this.isGraphTime) {
            if (!this.showVolume || (this.layoutComb && this.layoutCurrent == 0)) {
                this.setTimeAxis()
            }
        }
        if (!a) {
            this.setRowColBlocks();
            this.set1DVarSmpLabelFont()
        }
        if (this.graphOrientation == "vertical") {
            this.set1DXYDimensionsTop();
            this.set1DXYDimensionsBottom();
            this.y = this.layoutComb ? this.layoutHeight - (this.top + this.bottom) : this.height - (this.top + this.bottom);
            if (this.y < 0) {
                this.showLegend = false;
                this.set1DXYDimensionsBottom();
                this.y = this.layoutComb ? this.layoutHeight - (this.top + this.bottom) : this.height - (this.top + this.bottom);
                if (this.y < 0) {
                    alert("Dude it doesn't fit make the graph bigger because it looks ugly!")
                } else {
                    alert("Dude the legend was taken out because it doesn't fit make the graph bigger!")
                }
            }
            this.set1DText();
            this.set1DXYDimensionsLeft();
            this.set1DXYDimensionsRight();
            this.x = this.layoutComb ? this.layoutWidth - (this.left + this.right) : this.width - (this.left + this.right);
            if (this.x < 0) {
                this.showLegend = false;
                this.set1DXYDimensionsRight();
                this.x = this.layoutComb ? this.layoutWidth - (this.left + this.right) : this.width - (this.left + this.right);
                if (this.x < 0) {
                    alert("Dude it doesn't fit make the graph bigger because it looks ugly!")
                } else {
                    alert("Dude the legend was taken out because it doesn't fit make the graph bigger!")
                }
            }
        } else {
            this.set1DXYDimensionsLeft();
            this.set1DXYDimensionsRight();
            this.x = this.layoutComb ? this.layoutWidth - (this.left + this.right) : this.width - (this.left + this.right);
            if (this.x < 0) {
                this.showLegend = false;
                this.set1DXYDimensionsRight();
                this.x = this.layoutComb ? this.layoutWidth - (this.left + this.right) : this.width - (this.left + this.right);
                if (this.x < 0) {
                    alert("Dude it doesn't fit make the graph bigger because it looks ugly!")
                } else {
                    alert("Dude the legend was taken out because it doesn't fit make the graph bigger!")
                }
            }
            this.set1DText();
            this.set1DXYDimensionsTop();
            this.set1DXYDimensionsBottom();
            this.y = this.layoutComb ? this.layoutHeight - (this.top + this.bottom) : this.height - (this.top + this.bottom);
            if (this.y < 0) {
                this.showLegend = false;
                this.set1DXYDimensionsBottom();
                this.y = this.layoutComb ? this.layoutHeight - (this.top + this.bottom) : this.height - (this.top + this.bottom);
                if (this.y < 0) {
                    alert("Dude it doesn't fit make the graph bigger because it looks ugly!")
                } else {
                    alert("Dude the legend was taken out because it doesn't fit make the graph bigger!")
                }
            }
            if (this.graphType == "Heatmap" && !this.layoutComb) {
                if (this.bottom * 1.5 > this.y) {
                    if (this.dendrogramSpace > 2) {
                        this.dendrogramSpace -= 2
                    }
                    var b = this.getFontPt(this.varLabelFont) - 1;
                    this.varLabelFont = b.toString() + "pt " + this.fontName;
                    this.resizeCanvas(true);
                    this.set1DXYDimensions(true)
                }
            }
            if (this.left * 1.5 > this.x && !this.layoutComb) {
                if (this.dendrogramSpace > 2) {
                    this.dendrogramSpace -= 2
                }
                var b = this.getFontPt(this.smpLabelFont) - 1;
                this.smpLabelFont = b.toString() + "pt " + this.fontName;
                this.resizeCanvas(true);
                this.set1DXYDimensions(true)
            }
        }
    };
    this.set1DPrivateParams = function () {
        var a = this.isGroupedData ? this.grpIndices.length : this.smpIndices.length;
        if (this.autoExtend) {
            this.rowBlockSize = this.scaleTextToSize(this.rowBlocks) + this.scaleTextConstantAdd;
            this.rowBlockSize *= this.blockFactor;
            if (this.graphType == "Heatmap") {
                if (this.colBlocks < 20) {
                    this.colBlockSize = this.scaleTextToSize(this.colBlocks) + this.scaleTextConstantAdd + (this.scaleTextConstantAdd * 3)
                } else {
                    this.colBlockSize = (parseInt(1 / Math.sqrt(this.colBlocks) * this.scaleTextConstantMult)) + 1
                }
                this.blockSeparation = this.rowBlockSize / 2;
                this.y = this.rowBlockSize * this.rowBlocks;
                this.x = this.colBlockSize * this.colBlocks
            } else {
                this.blockSeparation = this.rowBlockSize * this.blockSeparationFactor;
                if (this.graphOrientation == "vertical") {
                    this.x = (this.rowBlockSize * this.rowBlocks) + (this.blockSeparation * a) + this.blockSeparation
                } else {
                    this.y = (this.rowBlockSize * this.rowBlocks) + (this.blockSeparation * a) + this.blockSeparation
                }
                this.colBlockSize = 1
            }
            this.width = (this.left + this.x + this.right) * this.layoutCols;
            this.height = (this.top + this.y + this.bottom) * this.layoutRows;
            this.resizeCanvas()
        } else {
            if (this.graphType == "Heatmap") {
                this.rowBlockSize = this.y / this.rowBlocks;
                this.blockSeparation = this.rowBlockSize;
                this.colBlockSize = this.x / this.varIndices.length
            } else {
                this.rowBlockSize = this.graphOrientation == "vertical" ? this.x / (this.rowBlocks + a + 1) : this.y / (this.rowBlocks + a + 1);
                this.blockSeparation = this.rowBlockSize;
                this.colBlockSize = 1
            }
        }
    };
    this.setTrees = function () {
        if (this.showSmpDendrogram && this.data.t.smps) {
            this.smpDendrogram = {};
            this.smpDendrogram.data = [];
            this.smpDendrogram.nodes = [];
            this.smpDendrogram.idxs = [];
            this.smpDendrogram.heights = [];
            this.parseNewick(this.data.t.smps, "smpDendrogram")
        }
        if (this.showVarDendrogram && this.data.t.vars) {
            this.varDendrogram = {};
            this.varDendrogram.data = [];
            this.varDendrogram.nodes = [];
            this.varDendrogram.idxs = [];
            this.varDendrogram.heights = [];
            this.parseNewick(this.data.t.vars, "varDendrogram")
        }
    };
    this.parseNewick = function (b, e) {
        var p = b.split(/,/);
        var w = 0;
        var f = 0;
        var g = 0;
        var m = 1;
        var l = 0;
        var u = 0;
        var a = 0;
        var k = 0;
        for (var s = 0; s < p.length; s++) {
            var q = p[s];
            var h = q.split(/:(?=[0-9\.]+)/);
            var x = h.shift();
            x = x.replace(/[\(\)]/g, "");
            this[e].nodes.push(x);
            var v = e == "smpDendrogram" ? this.getSampleIndices(x) : this.getVariableIndices(x);
            this[e].idxs.push(v);
            for (var r = 0; r < h.length; r++) {
                var d = h[r].replace(/[\(\)]/g, "");
                u = Math.max(u, d);
                this[e].heights.push(d)
            }
            this[e].max = u;
            var t = p[s].split(/ */);
            f = 0;
            for (var r = 0; r < t.length; r++) {
                if (t[r] == "(") {
                    w++;
                    f++
                }
                if (t[r] == ")") {
                    w--;
                    f--
                }
            }
            k += f;
            if (g > w) {
                this[e].data.push([m, g, f, k])
            } else {
                this[e].data.push([m, w, f, k])
            }
            g = w;
            a = Math.max(a, g);
            l += f;
            m++
        }
        this[e].unit = u > 0 ? (a * this.dendrogramSpace) / u : 0;
        this[e].depth = a;
        if (l > 0) {
            alert("Dude! Malformed newick tree. There are " + l + " additional right parenthesis!")
        } else {
            if (l < 0) {
                alert("Dude! Malformed newick tree. There are " + Math.abs(l) + " additional left parenthesis!")
            }
        }
        if (e == "varDendrogram") {
            if (this.varIndicesStart < 0) {
                if (this[e].nodes.length != this.data.y.vars.length) {
                    alert("Dude! The length of the variable nodes in the dendrogram (" + this[e].nodes.length + ") is different to that one in the data (" + this.data.y.vars.length + "). This ain't gonna work!");
                    this.showVarDendrogram = false
                } else {
                    if (this[e].nodes.length != this.varIndices.length) {
                        alert("Ooops! Cannot show dendrogram with " + this[e].nodes.length + " nodes when only " + this.varIndices.length + " variables are visible.");
                        this.showVarDendrogram = false
                    } else {
                        this.varIndices = this[e].idxs
                    }
                }
            } else {
                var o = this.varIndicesStart + this.varIndices.length;
                this.varIndices = [];
                for (var s = this.varIndicesStart; s < o; s++) {
                    this.varIndices.push(this[e].idxs[s])
                }
            }
        } else {
            if (this.smpIndicesStart < 0) {
                if (this[e].nodes.length != this.data.y.smps.length) {
                    alert("Dude! The length of the sample nodes in the dendrogram (" + this[e].nodes.length + ") is different to that one in the data (" + this.data.y.smps.length + "). This ain't gonna work!");
                    this.showSmpDendrogram = false
                } else {
                    if (this[e].nodes.length != this.smpIndices.length) {
                        alert("Ooops! Cannot show dendrogram with " + this[e].nodes.length + " nodes when only " + this.smpIndices.length + " samples visible.");
                        this.showSmpDendrogram = false
                    } else {
                        this.smpIndices = this[e].idxs
                    }
                }
            } else {
                var o = this.smpIndicesStart + this.smpIndices.length;
                this.smpIndices = [];
                for (var s = this.smpIndicesStart; s < o; s++) {
                    this.smpIndices.push(this[e].idxs[s])
                }
            }
        }
    };
    this.setOverlayColors = function () {
        var n = 0;
        if (this.smpOverlays.length > 0 && !this.isGroupedData) {
            this.smpOverlayColors = [];
            for (var h = 0; h < this.smpOverlays.length; h++) {
                var a = {};
                this.smpOverlayColors[h] = [];
                var g = this.smpOverlays[h];
                if (this.data.x.hasOwnProperty(g)) {
                    if (this.isNumeric(this.data.x[g], true)) {
                        var k = this.range(this.data.x[g], true);
                        var m = "rgb(255,255,0)";
                        for (var f = 0; f < this.smpIndices.length; f++) {
                            var l = this.smpIndices[f];
                            var e = this.data.x[g][l];
                            var d = !isNaN(e) ? this.getHeatmapColor(k[0], k[1], e) : m;
                            this.smpOverlayColors[h].push(d)
                        }
                    } else {
                        for (var f = 0; f < this.smpIndices.length; f++) {
                            var l = this.smpIndices[f];
                            var b = this.data.x[g][l];
                            if (!a.hasOwnProperty(b)) {
                                a[b] = n % this.colors.length;
                                n++
                            }
                            this.smpOverlayColors[h].push(this.colors[a[b]])
                        }
                    }
                }
            }
        }
        n = 0;
        if (this.varOverlays.length > 0 && this.graphType == "Heatmap") {
            this.varOverlayColors = [];
            for (var h = 0; h < this.varOverlays.length; h++) {
                var a = {};
                this.varOverlayColors[h] = [];
                var g = this.varOverlays[h];
                if (this.data.z.hasOwnProperty(g)) {
                    if (this.isNumeric(this.data.z[g], true)) {
                        var k = this.range(this.data.z[g], true);
                        var m = "rgb(255,255,0)";
                        for (var f = 0; f < this.varIndices.length; f++) {
                            var l = this.varIndices[f];
                            var e = this.data.z[g][l];
                            var d = !isNaN(e) ? this.getHeatmapColor(k[0], k[1], e) : m;
                            this.varOverlayColors[h].push(d)
                        }
                    } else {
                        for (var f = 0; f < this.varIndices.length; f++) {
                            var l = this.varIndices[f];
                            var b = this.data.z[g][l];
                            if (!a.hasOwnProperty(b)) {
                                a[b] = n % this.colors.length;
                                n++
                            }
                            this.varOverlayColors[h].push(this.colors[a[b]])
                        }
                    }
                }
            }
        }
    };
    this.setOverlayFont = function () {
        var a = Math.min(this.scaleOverlayFontFactor * this.overlayFontSize, this.maxTextSize);
        a = parseInt(Math.max(a, this.minTextSize));
        this.overlayFont = (a) + "Pt " + this.fontName
    };
    this.set1DAttributes = function () {
        this.setLegendFont();
        this.setTrees();
        this.setOverlayColors();
        this.setOverlayFont();
        this.setDataColor(true)
    };
    this.set1DXAxis = function () {
        var a = this.graphOrientation == "vertical" ? "y" : "x";
        this.setAxisUnits("xAxis", a);
        if ((this.graphType == "BarLine" && this.data.a) || this.graphType == "Candlestick") {
            this.setAxisUnits("xAxis2", a)
        }
    };
    this.draw1DWireFrame = function () {
        var q, e, p, d, o, a, f, j, n;
        var g = this.isGroupedData ? this.grpIndices : this.smpIndices;
        var s = this.xAxisTickStyle == "dotted" || this.yAxisTickStyle == "dotted" ? "dottedLine" : "line";
        var t = this.smpHairline == "dotted" ? "dottedLine" : "line";
        if (this.graphType != "Heatmap") {
            if (this.graphType == "BarLine" && this.data.a && this.data.y.desc[1]) {
                var b = this.getVariablesVisibleByAxis("xAxis");
                var r = this.getVariablesVisibleByAxis("xAxis2");
                n = Math.max(b.length, r.length)
            } else {
                if (this.graphType.match(/Stacked|Line|Area/)) {
                    n = 1
                } else {
                    n = this.varIndices.length
                }
            }
            j = n * this.rowBlockSize;
            if (this.graphOrientation == "vertical") {
                if (this.isGraphTime) {
                    var h = (this.x - this.blockSeparation) / g.length;
                    var l = h / 2;
                    var k = this.offsetX + this.left + (this.blockSeparation * 0.5) + l;
                    e = this.offsetY + this.top;
                    d = this.offsetY + this.top + this.y;
                    for (var m = 0; m < this.timeValueIndices.length; m++) {
                        q = (this.timeValueIndices[m] * h) + k;
                        if (this.smpHairline) {
                            this.drawLine(t, q, e, q, d, this.smpHairlineColor, false, "butt")
                        }
                    }
                } else {
                    q = this.offsetX + this.left + this.blockSeparation + (j / 2);
                    p = this.offsetX + this.left;
                    o = this.offsetX + this.left + (this.blockSeparation * 1.5) + j;
                    e = this.offsetY + this.top;
                    d = this.offsetY + this.top + this.y;
                    for (var m = 0; m < g.length; m++) {
                        if (this.blockContrast) {
                            f = m % 2 ? this.blockContrastEvenColor : this.blockContrastOddColor;
                            this.ctx.save();
                            this.rectangle(p, e, o, d, f, f);
                            this.ctx.restore();
                            p = o;
                            o = p + this.blockSeparation + j
                        }
                        if (this.smpHairline) {
                            this.drawLine(t, q, e, q, d, this.smpHairlineColor, false, "butt")
                        }
                        q += this.blockSeparation + j
                    }
                }
                if (this.xAxisMinorTicks || this.xAxis2MinorTicks) {
                    for (var m = 0; m < this.xAxisMinorValues.length; m++) {
                        var u = parseFloat(this.xAxisMinorValues[m]);
                        e = this.offsetY + this.top + this.y - ((u - this.xAxisMin) * this.xAxisUnit);
                        this.drawLine(s, this.offsetX + this.left, e, this.offsetX + this.left + this.x, e, this.xAxisTickColor, this.outlineWidth / 3, "butt")
                    }
                }
                q = this.offsetX + this.left - (this.margin * 2);
                for (var m = 0; m < this.xAxisValues.length; m++) {
                    var u = parseFloat(this.xAxisValues[m]);
                    e = this.offsetY + this.top + this.y - ((u - this.xAxisMin) * this.xAxisUnit);
                    this.drawLine(s, this.offsetX + this.left, e, this.offsetX + this.left + this.x, e, this.xAxisTickColor, false, "butt")
                }
            } else {
                if (this.isGraphTime) {
                    var h = (this.y - this.blockSeparation) / g.length;
                    var l = h / 2;
                    var k = this.offsetY + this.top + (this.blockSeparation * 0.5) + l;
                    q = this.offsetX + this.left;
                    p = this.offsetX + this.left + this.x;
                    for (var m = 0; m < this.timeValueIndices.length; m++) {
                        e = (this.timeValueIndices[m] * h) + k;
                        if (this.smpHairline) {
                            this.drawLine(t, q, e, p, e, this.smpHairlineColor, false, "butt")
                        }
                    }
                } else {
                    q = this.offsetX + this.left;
                    p = this.offsetX + this.left + this.x;
                    e = this.offsetY + this.top + this.blockSeparation + (j / 2);
                    d = this.offsetY + this.top;
                    a = this.offsetY + this.top + (this.blockSeparation * 1.5) + j;
                    for (var m = 0; m < g.length; m++) {
                        if (this.blockContrast) {
                            f = m % 2 ? this.blockContrastEvenColor : this.blockContrastOddColor;
                            this.ctx.save();
                            this.rectangle(q, d, p, a, f, f);
                            this.ctx.restore();
                            d = a;
                            a = d + this.blockSeparation + j
                        }
                        if (this.smpHairline) {
                            this.drawLine(t, q, e, p, e, this.smpHairlineColor, false, "butt")
                        }
                        e += this.blockSeparation + j
                    }
                }
                if (this.xAxisMinorTicks || this.xAxis2MinorTicks) {
                    for (var m = 0; m < this.xAxisMinorValues.length; m++) {
                        var u = parseFloat(this.xAxisMinorValues[m]);
                        q = this.offsetX + this.left + ((u - this.xAxisMin) * this.xAxisUnit);
                        this.drawLine(s, q, this.offsetY + this.top, q, this.offsetY + this.top + this.y, this.xAxisTickColor, this.outlineWidth / 3, "butt")
                    }
                }
                e = this.offsetY + this.top - ((this.margin * 2) + (this.getFontPt(this.axisTickFont) / 2));
                for (var m = 0; m < this.xAxisValues.length; m++) {
                    var u = parseFloat(this.xAxisValues[m]);
                    q = this.offsetX + this.left + ((u - this.xAxisMin) * this.xAxisUnit);
                    this.drawLine(s, q, this.offsetY + this.top, q, this.offsetY + this.top + this.y, this.xAxisTickColor, false, "butt")
                }
            }
        }
    };
    this.drawTrees = function () {
        if (!this.isGroupedData) {
            this.drawDendrogram("smpDendrogram");
            if (this.graphType == "Heatmap") {
                this.drawDendrogram("varDendrogram")
            }
            this.maskTreeArea()
        }
    };
    this.getMaxDepthDendrogramXYs = function (d) {
        if (d.length > 0) {
            var a = [];
            for (var b = 0; b < d.length; b++) {
                a.push(d[b][1])
            }
            a.sort(function (f, e) {
                return e - f
            });
            for (var b = 0; b < d.length; b++) {
                if (d[b][1] == a[0]) {
                    return b
                }
            }
        } else {
            return -1
        }
    };
    this.drawDendrogram = function (E) {
        var J = E == "varDendrogram" ? "showVarDendrogram" : "showSmpDendrogram";
        var g = E == "varDendrogram" ? this.varIndices : this.smpIndices;
        var B, a, A, ad, ab, F;
        var T = this.dendrogramSpace / 2;
        if (this.graphType != "Heatmap") {
            if (this.graphType == "BarLine" && this.data.a && this.data.y.desc[1]) {
                var E = this.getVariablesVisibleByAxis("xAxis");
                var G = this.getVariablesVisibleByAxis("xAxis2");
                F = Math.max(E.length, G.length)
            } else {
                if (this.graphType.match(/Stacked|Line|Area/)) {
                    F = 1
                } else {
                    F = this.varIndices.length
                }
            }
            ab = F * this.rowBlockSize
        } else {
            ab = 0
        }
        if (this[J] && this[E]) {
            var D = this[E].data;
            var ac = this[E].idxs;
            var F = this[E].depth;
            var p = this.getMaxDepthDendrogramXYs(D);
            var h = this[E].heights;
            var f = this[E].max;
            var n = {};
            var v = {};
            var e = [];
            if (E == "varDendrogram") {
                for (var aa = 0; aa < D.length; aa++) {
                    e[aa] = this.createNode(this.data.y.vars[ac[aa]], null, null, 0, null)
                }
            } else {
                for (var aa = 0; aa < D.length; aa++) {
                    e[aa] = this.createNode(this.data.y.smps[ac[aa]], null, null, 0, null)
                }
            }
            while (D.length > 1) {
                aa = p;
                var m = [aa];
                var N = D[aa][0];
                var M = D[aa][1];
                var L = D[aa][2];
                var P = D[aa][3];
                var s = [N];
                var q = [M];
                var o = [L];
                var t = [P];
                var H = {};
                var S = "";
                var C = L;
                H[aa] = 1;
                for (var Y = aa + 1; Y < D.length; Y++) {
                    var X = D[Y][0];
                    var W = D[Y][1];
                    var V = D[Y][2];
                    var Z = D[Y][3];
                    if (W == M && ((V == 0 && Z <= P) || (Z < P))) {
                        s.push(X);
                        q.push(W);
                        o.push(V);
                        t.push(Z);
                        C += V;
                        H[Y] = 1;
                        m.push(Y)
                    } else {
                        break
                    }
                }
                if (s.length > 1) {
                    if (E == "varDendrogram") {
                        var k = this.colBlockSize / 2;
                        var O;
                        if (this.varDendrogramPosition == "bottom") {
                            O = (this.offsetY + this.top + this.y + this.bottom) - (this.margin + (this.dendrogramSpace * (F + 1)))
                        } else {
                            O = (this.offsetY + this.top) - (this.margin + this.getVarTreeHeight())
                        }
                        for (var aa = 0; aa < s.length; aa++) {
                            e[m[aa]].dist = h[m[aa]];
                            B = (this.offsetX + this.left + k) + (this.colBlockSize * (s[aa] - 1));
                            B = this.varIndicesStart > -1 ? B - (this.colBlockSize * this.varIndicesStart) : B;
                            if (this.varDendrogramPosition == "bottom") {
                                ad = this.height - (this.margin + (this.dendrogramSpace * q[aa]));
                                if (this.dendrogramHeight) {} else {
                                    if (this.isLeafNode(e[m[aa]]) && !this.dendrogramHang) {
                                        a = O
                                    } else {
                                        a = ad - this.dendrogramSpace
                                    }
                                }
                            } else {
                                a = O + (this.dendrogramSpace * q[aa]);
                                if (this.dendrogramHeight) {} else {
                                    if (this.isLeafNode(e[m[aa]]) && !this.dendrogramHang) {
                                        ad = O + (this.dendrogramSpace * (F + 1))
                                    } else {
                                        ad = a + this.dendrogramSpace
                                    }
                                }
                            }
                            this.drawLine("line", B, a, B, ad, this.foreground, false, "butt", false, false, true, [0, 0, this.width, this.height]);
                            if (!n.hasOwnProperty(P)) {
                                n[P] = 0
                            }
                            if (!this.isLeafNode(e[m[aa]])) {
                                this.addArea(["rect", B - T, ad - T, B + T, ad + T], [-P], "-" + n[P] + "-varDendrogram");
                                n[P]++
                            }
                            if (aa < s.length - 1) {
                                A = (this.offsetX + this.left + k) + (this.colBlockSize * (s[aa + 1] - 1));
                                A = this.varIndicesStart > -1 ? A - (this.colBlockSize * this.varIndicesStart) : A;
                                if (this.varDendrogramPosition == "bottom") {
                                    this.drawLine("line", B, ad, A, ad, this.foreground, false, "butt", false, false, true, [0, 0, this.width, this.height])
                                } else {
                                    this.drawLine("line", B, a, A, a, this.foreground, false, "butt", false, false, true, [0, 0, this.width, this.height])
                                }
                            }
                        }
                    } else {
                        if (this.graphOrientation == "vertical") {
                            var O;
                            if (this.smpDendrogramPosition == "bottom") {
                                O = (this.offsetY + this.top + this.y + this.bottom) - (this.margin + this.getSampleTitleHeight() + (this.dendrogramSpace * (F + 1)));
                                if (this.showLegend && this.legendPosition == "bottom") {
                                    O -= this.getLegendHeight()
                                }
                            } else {
                                O = this.offsetY + this.top - this.getSmpTreeWidthHeight()
                            }
                            for (var aa = 0; aa < s.length; aa++) {
                                e[m[aa]].dist = h[m[aa]];
                                B = (this.offsetX + this.left + this.blockSeparation + (ab / 2)) + ((this.blockSeparation + ab) * (s[aa] - 1));
                                B = this.smpIndicesStart > -1 ? B - ((this.blockSeparation + ab) * this.smpIndicesStart) : B;
                                if (this.smpDendrogramPosition == "bottom") {
                                    ad = this.height - (this.margin + (this.dendrogramSpace * q[aa]));
                                    if (this.showLegend && this.legendPosition == "bottom") {
                                        ad -= this.getLegendHeight()
                                    }
                                    if (this.dendrogramHeight) {} else {
                                        if (isEnd && !this.dendrogramHang) {
                                            a = O
                                        } else {
                                            a = ad - this.dendrogramSpace
                                        }
                                    }
                                } else {
                                    a = O + (this.dendrogramSpace * q[aa]);
                                    if (this.dendrogramHeight) {} else {
                                        if (isEnd && !this.dendrogramHang) {
                                            ad = O + (this.dendrogramSpace * (F + 1))
                                        } else {
                                            ad = a + this.dendrogramSpace
                                        }
                                    }
                                }
                                this.drawLine("line", B, a, B, ad, this.foreground, false, "butt", false, false, true, [0, 0, this.width, this.height]);
                                if (!n.hasOwnProperty(P)) {
                                    n[P] = 0
                                }
                                if (!this.isLeafNode(e[m[aa]])) {
                                    this.addArea(["rect", B - T, ad - T, B + T, ad + T], [-P], "-" + n[P] + "-smpDendrogram");
                                    n[P]++
                                }
                                if (aa < s.length - 1) {
                                    A = (this.offsetX + this.left + this.blockSeparation + (ab / 2)) + ((this.blockSeparation + ab) * (s[aa + 1] - 1));
                                    A = this.smpIndicesStart > -1 ? A - ((this.blockSeparation + ab) * this.smpIndicesStart) : A;
                                    if (this.smpDendrogramPosition == "bottom") {
                                        this.drawLine("line", B, ad, A, ad, this.foreground, false, "butt", false, false, true, [0, 0, this.width, this.height])
                                    } else {
                                        this.drawLine("line", B, a, A, a, this.foreground, false, "butt", false, false, true, [0, 0, this.width, this.height])
                                    }
                                }
                            }
                        } else {
                            for (var aa = 0; aa < s.length; aa++) {
                                e[m[aa]].dist = h[m[aa]];
                                if (this.smpDendrogramPosition == "right") {
                                    A = (this.offsetX + this.left + this.x + this.margin) + ((this.dendrogramSpace * (F + 1)) - (this.dendrogramSpace * q[aa]))
                                } else {
                                    B = this.offsetX + this.margin + this.getSampleTitleHeight() + (this.dendrogramSpace * q[aa])
                                }
                                if (this.graphType == "Heatmap") {
                                    a = (this.offsetY + this.top + this.blockSeparation) + (this.rowBlockSize * (s[aa] - 1));
                                    a = this.smpIndicesStart > -1 ? a - (this.rowBlockSize * this.smpIndicesStart) : a;
                                    if (!this.autoExtend) {
                                        a -= this.blockSeparation / 2
                                    }
                                } else {
                                    a = (this.offsetY + this.top + this.blockSeparation + (ab / 2)) + ((this.blockSeparation + ab) * (s[aa] - 1));
                                    a = this.smpIndicesStart > -1 ? a - ((this.blockSeparation + ab) * this.smpIndicesStart) : a
                                }
                                if (this.smpDendrogramPosition == "right") {
                                    if (this.dendrogramHeight) {} else {
                                        if (this.isLeafNode(e[m[aa]]) && !this.dendrogramHang) {
                                            B = this.offsetX + this.left + this.x + this.margin
                                        } else {
                                            B = A - this.dendrogramSpace
                                        }
                                    }
                                } else {
                                    if (this.dendrogramHeight) {} else {
                                        if (this.isLeafNode(e[m[aa]]) && !this.dendrogramHang) {
                                            A = this.offsetX + this.margin + this.getSampleTitleHeight() + (this.dendrogramSpace * (F + 1))
                                        } else {
                                            A = B + this.dendrogramSpace
                                        }
                                    }
                                }
                                this.drawLine("line", B, a, A, a, this.foreground, false, "butt", false, false, true, [0, 0, this.width, this.height]);
                                if (!n.hasOwnProperty(P)) {
                                    n[P] = 0
                                }
                                if (!this.isLeafNode(e[m[aa]])) {
                                    this.addArea(["rect", A - T, a - T, A + T, a + T], [-P], "-" + n[P] + "-smpDendrogram");
                                    n[P]++
                                }
                                if (aa < s.length - 1) {
                                    if (this.graphType == "Heatmap") {
                                        ad = (this.offsetY + this.top + this.blockSeparation) + (this.rowBlockSize * (s[aa + 1] - 1));
                                        ad = this.smpIndicesStart > -1 ? ad - (this.rowBlockSize * this.smpIndicesStart) : ad;
                                        if (!this.autoExtend) {
                                            ad -= this.blockSeparation / 2
                                        }
                                    } else {
                                        ad = (this.offsetY + this.top + this.blockSeparation + (ab / 2)) + ((this.blockSeparation + ab) * (s[aa + 1] - 1));
                                        ad = this.smpIndicesStart > -1 ? ad - ((this.blockSeparation + ab) * this.smpIndicesStart) : ad
                                    }
                                    if (this.smpDendrogramPosition == "right") {
                                        this.drawLine("line", A, a, A, ad, this.foreground, false, "butt", false, false, true, [0, 0, this.width, this.height])
                                    } else {
                                        this.drawLine("line", B, a, B, ad, this.foreground, false, "butt", false, false, true, [0, 0, this.width, this.height])
                                    }
                                }
                            }
                        }
                    }
                    var U, R;
                    var b = [];
                    var I = [];
                    var Q = [];
                    var K = [];
                    var u = this.mean(s);
                    var d = false;
                    if (!v.hasOwnProperty(P)) {
                        v[P] = 0
                    }
                    S = (P - 1) + "-" + v[P];
                    for (var R in H) {
                        K.push(e[R])
                    }
                    for (var Y = 0; Y < D.length; Y++) {
                        if (!H.hasOwnProperty(Y)) {
                            b.push(D[Y]);
                            I.push(e[Y])
                        } else {
                            if (!d) {
                                b.push([u, M - 1, C, t[t.length - 1]]);
                                U = K.shift();
                                R = K.pop();
                                if (K.length > 0) {
                                    I.push(this.createNode(S, U, R, 0, null, K))
                                } else {
                                    I.push(this.createNode(S, U, R, 0, null))
                                }
                                v[P]++;
                                d = true
                            }
                        }
                    }
                    for (var Y = 0; Y < h.length; Y++) {
                        if (!H.hasOwnProperty(Y)) {
                            Q.push(h[Y])
                        }
                    }
                    D = b;
                    e = I;
                    h = Q;
                    p = this.getMaxDepthDendrogramXYs(D)
                }
            }
            this[E].object = e[0]
        }
    };
    this.draw1DYLayout = function () {
        var y, e, x, d, w, b, m, t;
        if (this.smpTitle) {
            var p = (parseInt(this.smpTitleFontSize * this.scaleSmpTitleFontFactor)) + "Pt " + this.fontName;
            if (this.graphOrientation == "vertical") {
                y = this.offsetX + this.left + (this.x / 2);
                e = this.offsetY + this.height - (this.margin + (parseInt(this.smpTitleFontSize * this.scaleSmpTitleFontFactor) / 2));
                this.drawText(this.smpTitle, y, e, p, this.smpTitleFontColor, "center", "middle")
            } else {
                y = this.offsetX + this.margin + (parseInt(this.smpTitleFontSize * this.scaleSmpTitleFontFactor) / 2);
                e = this.offsetY + this.top + (this.y / 2);
                this.drawText(this.smpTitle, y, e, p, this.smpTitleColor, "center", "middle", -Math.PI / 2)
            }
        }
        if (this.showSampleNames) {
            var g = this.isGroupedData ? this.grpIndices : this.smpIndices;
            var o = this.isGroupedData ? this.data.w.smps : this.data.y.smps;
            if (this.graphType == "Heatmap") {
                if (this.isGraphTime) {
                    var k = this.y / g.length;
                    var s = k / 2;
                    var n = this.offsetY + this.top + s;
                    y = (this.offsetX + this.left) - (this.margin * 2);
                    for (var u = 0; u < this.timeValueIndices.length; u++) {
                        e = (this.timeValueIndices[u] * k) + n;
                        if (this.smpLabelRotate) {
                            this.drawText(this.timeValues[u], y - this.margin, e, this.smpLabelFont, f, "right", "middle", this.smpLabelRotate * Math.PI / 180)
                        } else {
                            this.drawText(this.timeValues[u], y, e, this.smpLabelFont, f, "right", "middle")
                        }
                    }
                } else {
                    y = this.offsetX + this.left;
                    e = this.offsetY + this.top + this.blockSeparation;
                    if (!this.autoExtend) {
                        e -= this.blockSeparation / 2
                    }
                    t = 1;
                    var h = this.getSmpOverlaysLength();
                    for (var u = 0; u < g.length; u++) {
                        var l = this.shortenText(o[g[u]], this.maxSmpStringLen);
                        var v = (this.margin * 2) + h;
                        var f = this.getSmpColor(o[g[u]]);
                        if (this.smpLabelRotate) {
                            this.drawText(l, y - (v + this.margin), e, this.smpLabelFont, f, "right", "middle", this.smpLabelRotate * Math.PI / 180)
                        } else {
                            this.drawText(l, y - v, e, this.smpLabelFont, f, "right", "middle")
                        }
                        e += this.rowBlockSize
                    }
                }
                var s = this.colBlockSize / 2;
                y = this.offsetX + this.left + s;
                e = this.offsetY + this.top + this.y + this.margin;
                if (this.varOverlays.length > 0) {
                    e += (this.varOverlays.length * this.overlaysWidth) + this.margin
                }
                for (var u = 0; u < this.varIndices.length; u++) {
                    var q = this.shortenText(this.data.y.vars[this.varIndices[u]], this.maxVarStringLen);
                    var f = this.getVarColor(this.data.y.vars[this.varIndices[u]]);
                    if (this.varLabelRotate) {
                        this.drawText(q, y, e + this.margin, this.varLabelFont, f, "right", "middle", -Math.PI / 2 + (this.varLabelRotate * Math.PI / 180))
                    } else {
                        this.drawText(q, y, e, this.varLabelFont, f, "right", "middle", -Math.PI / 2)
                    }
                    y += this.colBlockSize
                }
            } else {
                if (this.graphType == "BarLine" && this.data.a && this.data.y.desc[1]) {
                    var a = this.getVariablesVisibleByAxis("xAxis");
                    var z = this.getVariablesVisibleByAxis("xAxis2");
                    t = Math.max(a.length, z.length)
                } else {
                    if (this.graphType.match(/Stacked|Line|Area/)) {
                        t = 1
                    } else {
                        t = this.varIndices.length
                    }
                }
                m = t * this.rowBlockSize;
                if (this.graphOrientation == "vertical") {
                    if (this.isGraphTime) {
                        var k = (this.x - this.blockSeparation) / g.length;
                        var s = k / 2;
                        var n = this.offsetX + this.left + (this.blockSeparation * 0.5) + s;
                        e = this.offsetY + this.top + this.y + this.getSmpOverlaysLength() + this.margin + this.margin;
                        for (var u = 0; u < this.timeValueIndices.length; u++) {
                            y = (this.timeValueIndices[u] * k) + n;
                            if (this.smpHairline) {
                                if (this.smpLabelRotate) {
                                    this.drawText(this.timeValues[u], y, e + this.margin, this.smpLabelFont, f, "right", "middle", -Math.PI / 2 + (this.smpLabelRotate * Math.PI / 180))
                                } else {
                                    this.drawText(this.timeValues[u], y, e, this.smpLabelFont, f, "right", "middle", -Math.PI / 2)
                                }
                            }
                        }
                    } else {
                        y = this.offsetX + this.left + this.blockSeparation + (m / 2);
                        e = this.offsetY + this.top;
                        d = this.offsetY + this.top + this.y;
                        b = d + this.getSmpOverlaysLength() + this.margin + this.margin;
                        for (var u = 0; u < g.length; u++) {
                            var l = this.shortenText(o[g[u]], this.maxSmpStringLen);
                            var f = this.getSmpColor(o[g[u]]);
                            if (this.smpLabelRotate) {
                                this.drawText(l, y, b + this.margin, this.smpLabelFont, f, "right", "middle", -Math.PI / 2 + (this.smpLabelRotate * Math.PI / 180))
                            } else {
                                this.drawText(l, y, b, this.smpLabelFont, f, "right", "middle", -Math.PI / 2)
                            }
                            y += this.blockSeparation + m
                        }
                    }
                } else {
                    if (this.isGraphTime) {
                        var k = (this.y - this.blockSeparation) / g.length;
                        var s = k / 2;
                        var n = this.offsetY + this.top + (this.blockSeparation * 0.5) + s;
                        y = (this.offsetX + this.left) - (this.margin * 2);
                        for (var u = 0; u < this.timeValueIndices.length; u++) {
                            e = (this.timeValueIndices[u] * k) + n;
                            if (this.smpLabelRotate) {
                                this.drawText(this.timeValues[u], y - this.margin, e, this.smpLabelFont, f, "right", "middle", this.smpLabelRotate * Math.PI / 180)
                            } else {
                                this.drawText(this.timeValues[u], y, e, this.smpLabelFont, f, "right", "middle")
                            }
                        }
                    } else {
                        y = this.offsetX + this.left;
                        x = this.offsetX + this.left + this.x;
                        w = y - (this.getSmpOverlaysLength() + (this.margin * 2));
                        e = this.offsetY + this.top + this.blockSeparation + (m / 2);
                        for (var u = 0; u < g.length; u++) {
                            var l = this.shortenText(o[g[u]], this.maxSmpStringLen);
                            var f = this.getSmpColor(o[g[u]]);
                            if (this.smpLabelRotate) {
                                this.drawText(l, w - this.margin, e, this.smpLabelFont, f, "right", "middle", this.smpLabelRotate * Math.PI / 180)
                            } else {
                                this.drawText(l, w, e, this.smpLabelFont, f, "right", "middle")
                            }
                            e += this.blockSeparation + m
                        }
                    }
                }
            }
        }
        if (this.showOverlays) {
            if (this.smpOverlays.length > 0 && !this.isGroupedData) {
                m = t * this.rowBlockSize;
                if (this.graphOrientation == "vertical") {
                    y = this.offsetX + this.left;
                    x = m;
                    d = this.overlaysWidth;
                    if (this.graphType != "Heatmap") {
                        y += this.blockSeparation / 2;
                        x += this.blockSeparation
                    }
                    for (var u = 0; u < this.smpIndices.length; u++) {
                        e = this.offsetY + this.top + this.y + this.margin;
                        for (var r = 0; r < this.smpOverlays.length; r++) {
                            this.rectangle(y, e, x, d, this.smpOverlayColors[r][u]);
                            if (m > this.overlaysWidth) {
                                this.drawText(this.data.x[this.smpOverlays[r]][this.smpIndices[u]], y + (x / 2), e + (d / 2), this.overlayFont, this.overlayFontColor, "center", "middle", false, m - 2)
                            } else {
                                this.drawText(this.data.x[this.smpOverlays[r]][this.smpIndices[u]], y + (x / 2), e + (d / 2), this.overlayFont, this.overlayFontColor, "center", "middle", -Math.PI / 2, this.overlaysWidth - 2)
                            }
                            e += d
                        }
                        y += x
                    }
                    y = this.offsetX + this.left + this.x;
                    e = this.offsetY + this.top + this.y + this.margin + (d / 2);
                    for (var u = 0; u < this.smpOverlays.length; u++) {
                        this.drawText(this.smpOverlays[u], y, e, this.overlayFont, this.overlayFontColor, "left", "middle", false, m * 2);
                        e += d
                    }
                } else {
                    x = this.overlaysWidth;
                    e = this.offsetY + this.top;
                    d = m;
                    if (this.graphType != "Heatmap") {
                        e += this.blockSeparation / 2;
                        d += this.blockSeparation
                    }
                    for (var u = 0; u < this.smpIndices.length; u++) {
                        y = this.offsetX + this.left - (this.margin + (this.smpOverlays.length * this.overlaysWidth) + this.margin);
                        for (var r = 0; r < this.smpOverlays.length; r++) {
                            this.rectangle(y, e, x, d, this.smpOverlayColors[r][u]);
                            if (m > this.overlaysWidth) {
                                this.drawText(this.data.x[this.smpOverlays[r]][this.smpIndices[u]], y + (x / 2), e + (d / 2), this.overlayFont, this.overlayFontColor, "center", "middle", -Math.PI / 2, m - 2)
                            } else {
                                this.drawText(this.data.x[this.smpOverlays[r]][this.smpIndices[u]], y + (x / 2), e + (d / 2), this.overlayFont, this.overlayFontColor, "center", "middle", false, this.overlaysWidth - 2)
                            }
                            y += x
                        }
                        e += d
                    }
                    y = (this.offsetX + this.left + (x / 2)) - (this.margin + (this.smpOverlays.length * this.overlaysWidth) + this.margin);
                    e = this.offsetY + this.top + this.y;
                    if (this.graphType == "Heatmap") {
                        e += this.margin
                    }
                    for (var u = 0; u < this.smpOverlays.length; u++) {
                        this.drawText(this.smpOverlays[u], y, e, this.overlayFont, this.overlayFontColor, "right", "middle", -Math.PI / 2, m * 2);
                        y += x
                    }
                }
            }
            if (this.varOverlays.length > 0 && this.graphType == "Heatmap") {
                y = this.offsetX + this.left + (this.colBlockSize / 2);
                d = this.overlaysWidth;
                for (var u = 0; u < this.varIndices.length; u++) {
                    e = this.offsetY + this.top + this.y + this.margin;
                    for (var r = 0; r < this.varOverlays.length; r++) {
                        this.rectangle(y - (this.colBlockSize / 2), e, this.colBlockSize, d, this.varOverlayColors[r][u]);
                        if (this.colBlockSize > this.overlaysWidth) {
                            this.drawText(this.data.z[this.varOverlays[r]][this.varIndices[u]], y, e + (d / 2), this.overlayFont, this.overlayFontColor, "center", "middle", -Math.PI / 2, this.colBlockSize - 2)
                        } else {
                            this.drawText(this.data.z[this.varOverlays[r]][this.varIndices[u]], y, e + (d / 2), this.overlayFont, this.overlayFontColor, "center", "middle", false, this.overlaysWidth - 2)
                        }
                        e += this.overlaysWidth
                    }
                    y += this.colBlockSize
                }
                y = this.offsetX + this.left + this.x + this.margin;
                e = this.offsetY + this.top + this.y + this.margin + (d / 2);
                for (var u = 0; u < this.varOverlays.length; u++) {
                    this.drawText(this.varOverlays[u], y, e, this.overlayFont, this.overlayFontColor, "left", "middle", false, this.colBlockSize * 2);
                    e += d
                }
            }
        }
    };
    this.draw1DXLayout = function () {
        if (!this.layoutValid) {
            this.layoutAxis = 3
        }
        if (this.graphType == "Heatmap") {
            var b = (this.offsetX + this.left + (this.x / 2)) - (this.heatmapColors.length / 2);
            var e = this.offsetY + this.top - this.getHeatmapIndicatorHeight();
            if (this.varDendrogramPosition == "top") {
                e -= this.getVarTreeHeight()
            }
            this.drawColorIndicator(b, e, this.minData, this.maxData, this.xAxisDecs)
        } else {
            var j;
            var f;
            if (this.graphOrientation == "vertical") {
                if (this.layoutAxis == 1 || this.layoutAxis == 3) {
                    var b = this.offsetX + this.margin + (this.getFontPt(this.axisTitleFont) / 2);
                    var e = this.offsetY + this.top + (this.y / 2);
                    j = this.graphType == "Candlestick" && this.summaryType == "volume" ? this.xAxis2Title : this.xAxisTitle ? this.xAxisTitle : this.data.y.desc && this.data.y.desc[0] ? this.data.y.desc[0] : this.yAxisTitle ? this.yAxisTitle : "";
                    if (this.graphType == "BarLine" && this.data.a && this.data.y.desc && this.data.y.desc[1]) {
                        this.drawText(j + " (Bars)", b, e, this.axisTitleFont, this.axisTitleColor, "center", "middle", -Math.PI / 2)
                    } else {
                        this.drawText(j, b, e, this.axisTitleFont, this.axisTitleColor, "center", "middle", -Math.PI / 2)
                    }
                    f = this.graphType == "Candlestick" && this.summaryType == "volume" ? this.xAxis2Values : this.xAxisValues;
                    b = this.offsetX + this.left - (this.margin * 2);
                    for (var a = 0; a < f.length - 1; a++) {
                        var h = parseFloat(f[a]);
                        var g = this.graphType == "Candlestick" && this.summaryType == "volume" ? sprintf("%." + this.xAxis2Decs + "f", h) : sprintf("%." + this.xAxisDecs + "f", h);
                        e = this.graphType == "Candlestick" && this.summaryType == "volume" ? this.offsetY + this.top + this.y - ((h - this.xAxis2Min) * this.xAxis2Unit) : this.offsetY + this.top + this.y - ((h - this.xAxisMin) * this.xAxisUnit);
                        this.drawText(g, b, e, this.axisTickFont, this.axisTickColor, "right", "middle")
                    }
                }
                if (this.layoutAxis == 2 || this.layoutAxis == 3) {
                    b = this.offsetX + this.left + this.x + (this.margin * 2);
                    if (this.graphType == "BarLine" && this.data.a && this.data.y.desc && this.data.y.desc[1]) {
                        for (var a = 0; a < this.xAxis2Values.length - 1; a++) {
                            var h = parseFloat(this.xAxis2Values[a]);
                            var g = sprintf("%." + this.xAxis2Decs + "f", h);
                            e = this.offsetY + this.top + this.y - ((h - this.xAxis2Min) * this.xAxis2Unit);
                            this.drawText(g, b, e, this.axisTickFont, this.axisTickColor, "left", "middle")
                        }
                        j = this.data.y.desc && this.data.y.desc[1] ? this.data.y.desc[1] + " (Lines)" : ""
                    } else {
                        f = this.graphType == "Candlestick" && this.summaryType == "volume" ? this.xAxis2Values : this.xAxisValues;
                        for (var a = 0; a < f.length - 1; a++) {
                            var h = parseFloat(f[a]);
                            var g = this.graphType == "Candlestick" && this.summaryType == "volume" ? sprintf("%." + this.xAxis2Decs + "f", h) : sprintf("%." + this.xAxisDecs + "f", h);
                            e = this.graphType == "Candlestick" && this.summaryType == "volume" ? this.offsetY + this.top + this.y - ((h - this.xAxis2Min) * this.xAxis2Unit) : this.offsetY + this.top + this.y - ((h - this.xAxisMin) * this.xAxisUnit);
                            this.drawText(g, b, e, this.axisTickFont, this.axisTickColor, "left", "middle")
                        }
                        j = this.graphType == "Candlestick" && this.summaryType == "volume" ? this.xAxis2Title : this.xAxisTitle ? this.xAxisTitle : this.data.y.desc && this.data.y.desc[0] ? this.data.y.desc[0] : this.yAxisTitle ? this.yAxisTitle : ""
                    }
                    b = (this.offsetX + this.left + this.x + this.right) - (this.margin + (this.getFontPt(this.axisTitleFont) / 2));
                    if (this.showLegend && this.legendPosition == "right") {
                        b -= this.getLegendWidth()
                    }
                    e = this.offsetY + this.top + (this.y / 2);
                    this.drawText(j, b, e, this.axisTitleFont, this.axisTitleColor, "center", "middle", -Math.PI / 2)
                }
            } else {
                if (this.layoutAxis == 1 || this.layoutAxis == 3) {
                    var b = this.offsetX + this.left + (this.x / 2);
                    var e = this.offsetY + this.top - ((this.margin * 4) + this.getFontPt(this.axisTickFont) + (this.getFontPt(this.axisTitleFont) / 2));
                    j = this.graphType == "Candlestick" && this.summaryType == "volume" ? this.xAxis2Title : this.xAxisTitle ? this.xAxisTitle : this.data.y.desc && this.data.y.desc[0] ? this.data.y.desc[0] : this.yAxisTitle ? this.yAxisTitle : "";
                    if (this.graphType == "BarLine" && this.data.a && this.data.y.desc && this.data.y.desc[1]) {
                        this.drawText(j + " (Bars)", b, e, this.axisTitleFont, this.axisTitleColor, "center", "middle")
                    } else {
                        this.drawText(j, b, e, this.axisTitleFont, this.axisTitleColor, "center", "middle")
                    }
                    f = this.graphType == "Candlestick" && this.summaryType == "volume" ? this.xAxis2Values : this.xAxisValues;
                    e = this.offsetY + this.top - ((this.margin * 2) + (this.getFontPt(this.axisTickFont) / 2));
                    for (var a = 0; a < f.length - 1; a++) {
                        var h = parseFloat(f[a]);
                        var g = this.graphType == "Candlestick" && this.summaryType == "volume" ? sprintf("%." + this.xAxis2Decs + "f", h) : sprintf("%." + this.xAxisDecs + "f", h);
                        b = this.graphType == "Candlestick" && this.summaryType == "volume" ? this.offsetX + this.left + ((h - this.xAxis2Min) * this.xAxis2Unit) : this.offsetX + this.left + ((h - this.xAxisMin) * this.xAxisUnit);
                        this.drawText(g, b, e, this.axisTickFont, this.axisTickColor, "center", "middle")
                    }
                }
                if (this.layoutAxis == 2 || this.layoutAxis == 3) {
                    e = this.offsetY + this.top + this.y + (this.margin * 2) + (this.getFontPt(this.axisTickFont) / 2);
                    if (this.graphType == "BarLine" && this.data.a && this.data.y.desc && this.data.y.desc[1]) {
                        for (var a = 0; a < this.xAxis2Values.length - 1; a++) {
                            var h = parseFloat(this.xAxis2Values[a]);
                            var g = sprintf("%." + this.xAxis2Decs + "f", h);
                            b = this.offsetX + this.left + ((h - this.xAxis2Min) * this.xAxis2Unit);
                            this.drawText(g, b, e, this.axisTickFont, this.axisTickColor, "center", "middle")
                        }
                        j = this.data.y.desc && this.data.y.desc[1] ? this.data.y.desc[1] + " (Lines)" : ""
                    } else {
                        f = this.graphType == "Candlestick" && this.summaryType == "volume" ? this.xAxis2Values : this.xAxisValues;
                        for (var a = 0; a < f.length - 1; a++) {
                            var h = parseFloat(f[a]);
                            var g = this.graphType == "Candlestick" && this.summaryType == "volume" ? sprintf("%." + this.xAxis2Decs + "f", h) : sprintf("%." + this.xAxisDecs + "f", h);
                            b = this.graphType == "Candlestick" && this.summaryType == "volume" ? this.offsetX + this.left + ((h - this.xAxis2Min) * this.xAxis2Unit) : this.offsetX + this.left + ((h - this.xAxisMin) * this.xAxisUnit);
                            this.drawText(g, b, e, this.axisTickFont, this.axisTickColor, "center", "middle")
                        }
                        j = this.graphType == "Candlestick" && this.summaryType == "volume" ? this.xAxis2Title : this.xAxisTitle ? this.xAxisTitle : this.data.y.desc && this.data.y.desc[0] ? this.data.y.desc[0] : this.yAxisTitle ? this.yAxisTitle : ""
                    }
                    b = this.offsetX + this.left + (this.x / 2);
                    e = this.offsetY + this.top + this.y + (this.margin * 4) + this.getFontPt(this.axisTickFont) + (this.getFontPt(this.axisTitleFont) / 2);
                    this.drawText(j, b, e, this.axisTitleFont, this.axisTitleColor, "center", "middle")
                }
            }
            var d = this.backgroundType.match(/window/i) ? this.foregroundWindow : this.foreground;
            this.rectangle(this.offsetX + this.left, this.offsetY + this.top, this.x, this.y, false, d, "open", false, this.outlineWidth + 1)
        }
    };
    this.draw1DDataPoints = function () {
        var S = this.isGroupedData ? this.grpIndices.length : this.smpIndices.length;
        var g = this.backgroundType.match(/window/i) ? this.foregroundWindow : this.foreground;
        switch (this.graphType) {
        case "Bar":
            var d = this.xAxisMin < 0 ? Math.abs(this.xAxisMin) * this.xAxisUnit : 0;
            var f = this.isGroupedData ? this.grpIndices : this.smpIndices;
            if (this.graphOrientation == "vertical") {
                var a = (this.offsetY + this.top + this.y) - d;
                var C = this.offsetX + this.left + this.blockSeparation;
                var B = this.rowBlockSize;
                for (var V = 0; V < S; V++) {
                    var N = f[V];
                    for (var U = 0; U < this.varIndices.length; U++) {
//                      alert(this.colorBy);
//                      alert(this.legendColorsN);
                        var m = this.colorBy && this.legendColorsN ? this.dataColors[V] : this.colors[U % this.colors.length];
//                        var m = this.colors[V];
                        var v = this.varIndices[U];
                        var K = this.getDataAtPos(v, N) - this.xAxisMin;
                        var ae = K * this.xAxisUnit - d;
                        this.addArea(this.rectangle(C, a, B, -ae, m, g, "closed", false, false, true), [v, N]);
                        if (this.isGroupedData || this.data.y.hasOwnProperty("stdev")) {
                            var W = this.getDataAtPos(v, N, "stdev") - this.xAxisMin;
                            if (!isNaN(W)) {
                                var ad = (W / 2) * this.xAxisUnit - (d / 2);
                                this.errorBar(C + this.rowBlockSize / 2, a - ae, C + this.rowBlockSize / 2, (a - ae) + ad, -this.rowBlockSize / 2, g);
                                this.errorBar(C + this.rowBlockSize / 2, a - ae, C + this.rowBlockSize / 2, (a - ae) - ad, -this.rowBlockSize / 2, g)
                            }
                        }
                        if (this.showDataValues) {
                            this.drawText(K, C + this.rowBlockSize / 2, a - (ae + this.overlayFontSize), this.overlayFont, m, "center", "bottom")
                        }
                        C += B
                    }
                    C += this.blockSeparation
                }
            } else {
                var C = this.offsetX + this.left + d;
                var a = this.offsetY + this.top + this.blockSeparation;
                var ae = this.rowBlockSize;
                for (var V = 0; V < S; V++) {
                    var N = f[V];
                    for (var U = 0; U < this.varIndices.length; U++) {
                        var m = this.colorBy && this.legendColorsN ? this.dataColors[V] : this.colors[U % this.colors.length];
                        var v = this.varIndices[U];
                        var K = this.getDataAtPos(v, N) - this.xAxisMin;
                        var B = K * this.xAxisUnit - d;
                        this.addArea(this.rectangle(C, a, B, ae, m, g, "closed", false, false, true), [v, N]);
                        if (this.isGroupedData || this.data.y.hasOwnProperty("stdev")) {
                            var W = this.getDataAtPos(v, N, "stdev") - this.xAxisMin;
                            if (!isNaN(W)) {
                                var A = (W / 2) * this.xAxisUnit - (d / 2);
                                this.errorBar(C + B, a + this.rowBlockSize / 2, (C + B) + A, a + this.rowBlockSize / 2, this.rowBlockSize / 2, g);
                                this.errorBar(C + B, a + this.rowBlockSize / 2, (C + B) - A, a + this.rowBlockSize / 2, this.rowBlockSize / 2, g)
                            }
                        }
                        if (this.showDataValues) {
                            this.drawText(K, C + B + this.axisTickFontSize, a + this.rowBlockSize / 2, this.overlayFont, m, "left", "middle")
                        }
                        a += ae
                    }
                    a += this.blockSeparation
                }
            }
            return true;
        case "Stacked":
        case "StackedPercent":
            var f = this.isGroupedData ? this.grpIndices : this.smpIndices;
            if (this.graphOrientation == "vertical") {
                var C = this.offsetX + this.left + this.blockSeparation;
                var B = this.rowBlockSize;
                for (var V = 0; V < S; V++) {
                    var N = f[V];
                    var X = this.getDataForSmpGrpAtIndex(N);
                    var D = this.sum(X);
                    var a = this.offsetY + this.top + this.y;
                    for (var U = 0; U < this.varIndices.length; U++) {
                        var m = U % this.colors.length;
                        var v = this.varIndices[U];
                        var K = this.getDataAtPos(v, N);
                        var ae = this.graphType == "StackedPercent" ? ((K * 100) / D) * this.xAxisUnit : K * this.xAxisUnit;
                        this.addArea(this.rectangle(C, a, B, -ae, this.colors[m]), [v, N]);
                        a -= ae
                    }
                    C += B + this.blockSeparation
                }
            } else {
                var a = this.offsetY + this.top + this.blockSeparation;
                var ae = this.rowBlockSize;
                for (var V = 0; V < S; V++) {
                    var N = f[V];
                    var X = this.getDataForSmpGrpAtIndex(N);
                    var D = this.sum(X);
                    var C = this.offsetX + this.left;
                    for (var U = 0; U < this.varIndices.length; U++) {
                        var m = U % this.colors.length;
                        var v = this.varIndices[U];
                        var K = this.getDataAtPos(v, N);
                        var B = this.graphType == "StackedPercent" ? ((K * 100) / D) * this.xAxisUnit : K * this.xAxisUnit;
                        this.addArea(this.rectangle(C, a, B, ae, this.colors[m]), [v, N]);
                        C += B
                    }
                    a += ae + this.blockSeparation
                }
            }
            return true;
        case "Line":
            var f = this.isGroupedData ? this.grpIndices : this.smpIndices;
            if (this.graphOrientation == "vertical") {
                var C = this.offsetX + this.left + this.blockSeparation + (this.rowBlockSize / 2);
                var B = C + this.rowBlockSize + this.blockSeparation;
                for (var V = 0; V < S - 1; V++) {
                    var N = f[V];
                    var I = f[V + 1];
                    for (var U = 0; U < this.varIndices.length; U++) {
                        var m = this.colors[U % this.colors.length];
                        var e = this.lineDecoration == "symbol" ? g : m;
                        var L = this.lineDecoration == "symbol" ? this.shapes[U % this.shapes.length] : "sphere";
                        var v = this.varIndices[U];
                        var H = this.getDataAtPos(v, N) - this.xAxisMin;
                        var G = this.getDataAtPos(v, I) - this.xAxisMin;
                        var a = (this.offsetY + this.top + this.y) - (H * this.xAxisUnit);
                        var ae = (this.offsetY + this.top + this.y) - (G * this.xAxisUnit);
                        if (!isNaN(H) && !isNaN(G)) {
                            if (H < G) {
                                this.drawLine("line", B, ae, C, a, e, this.lineThickness, "butt", false, false, true)
                            } else {
                                this.drawLine("line", C, a, B, ae, e, this.lineThickness, "butt", false, false, true)
                            }
                        }
                        if (this.isGroupedData || this.data.y.hasOwnProperty("stdev")) {
                            H = this.getDataAtPos(v, N, "stdev") - this.xAxisMin;
                            G = this.getDataAtPos(v, I, "stdev") - this.xAxisMin;
                            if (!isNaN(H)) {
                                var ad = (H / 2) * this.xAxisUnit;
                                this.errorBar(C, a, C, a + ad, -this.rowBlockSize / 2, m);
                                this.errorBar(C, a, C, a - ad, -this.rowBlockSize / 2, m)
                            }
                            if (!isNaN(G)) {
                                var ad = (G / 2) * this.xAxisUnit;
                                this.errorBar(B, ae, B, ae + ad, -this.rowBlockSize / 2, m);
                                this.errorBar(B, ae, B, ae - ad, -this.rowBlockSize / 2, m)
                            }
                        }
                        if (!isNaN(H)) {
                            if (this.lineDecoration) {
                                this.addArea(this.drawShape(L, C, a, this.rowBlockSize / 3, this.rowBlockSize / 3, m, g, "closed", false, false, true), [v, N])
                            }
                        }
                        if (!isNaN(G)) {
                            if (this.lineDecoration) {
                                if (V == S - 2) {
                                    this.addArea(this.drawShape(L, B, ae, this.rowBlockSize / 3, this.rowBlockSize / 3, m, g, "closed", false, false, true), [v, I])
                                } else {
                                    this.drawShape(L, B, ae, this.rowBlockSize / 3, this.rowBlockSize / 3, m, g, "closed", false, false, true)
                                }
                            }
                        }
                    }
                    C = B;
                    B += this.rowBlockSize + this.blockSeparation
                }
            } else {
                var a = this.offsetY + this.top + this.blockSeparation + (this.rowBlockSize / 2);
                var ae = a + this.rowBlockSize + this.blockSeparation;
                for (var V = 0; V < S - 1; V++) {
                    var N = f[V];
                    var I = f[V + 1];
                    for (var U = 0; U < this.varIndices.length; U++) {
                        var m = this.colors[U % this.colors.length];
                        var e = this.lineDecoration == "symbol" ? g : m;
                        var L = this.lineDecoration == "symbol" ? this.shapes[U % this.shapes.length] : "sphere";
                        var v = this.varIndices[U];
                        var H = this.getDataAtPos(v, N) - this.xAxisMin;
                        var G = this.getDataAtPos(v, I) - this.xAxisMin;
                        var C = this.offsetX + this.left + H * this.xAxisUnit;
                        var B = this.offsetX + this.left + G * this.xAxisUnit;
                        if (!isNaN(H) && !isNaN(G)) {
                            this.drawLine("line", B, ae, C, a, e, this.lineThickness, "butt", false, false, true)
                        }
                        if (this.isGroupedData || this.data.y.hasOwnProperty("stdev")) {
                            H = this.getDataAtPos(v, N, "stdev") - this.xAxisMin;
                            G = this.getDataAtPos(v, I, "stdev") - this.xAxisMin;
                            if (!isNaN(H)) {
                                var A = (H / 2) * this.xAxisUnit;
                                this.errorBar(C, a, C + A, a, this.rowBlockSize / 2, m);
                                this.errorBar(C, a, C - A, a, this.rowBlockSize / 2, m)
                            }
                            if (!isNaN(G)) {
                                var A = (G / 2) * this.xAxisUnit;
                                this.errorBar(B, ae, B + A, ae, this.rowBlockSize / 2, m);
                                this.errorBar(B, ae, B - A, ae, this.rowBlockSize / 2, m)
                            }
                        }
                        if (!isNaN(H)) {
                            if (this.lineDecoration) {
                                this.addArea(this.drawShape(L, C, a, this.rowBlockSize / 3, this.rowBlockSize / 3, m, g, "closed", false, false, true), [v, N])
                            }
                        }
                        if (!isNaN(G)) {
                            if (this.lineDecoration) {
                                if (V == S - 2) {
                                    this.addArea(this.drawShape(L, B, ae, this.rowBlockSize / 3, this.rowBlockSize / 3, m, g, "closed", false, false, true), [v, I])
                                } else {
                                    this.drawShape(L, B, ae, this.rowBlockSize / 3, this.rowBlockSize / 3, m, g, "closed", false, false, true)
                                }
                            }
                        }
                    }
                    a = ae;
                    ae += this.rowBlockSize + this.blockSeparation
                }
            }
            return true;
        case "Area":
            var f = this.isGroupedData ? this.grpIndices : this.smpIndices;
            var F = [];
            var M = [];
            if (this.graphOrientation == "vertical") {
                for (var V = 0; V < S; V++) {
                    F[V] = this.offsetY + this.top + this.y;
                    M[V] = this.offsetY + this.top + this.y
                }
                for (var V = 0; V < this.varIndices.length; V++) {
                    var C = this.offsetX + this.left + this.blockSeparation + (this.rowBlockSize / 2);
                    var O = [this.offsetX + this.left];
                    var y = [this.offsetY + this.top + this.y];
                    var N = this.varIndices[V];
                    var m = this.colors[V % this.colors.length];
                    for (var U = 0; U < S; U++) {
                        var v = f[U];
                        var K = this.getDataAtPos(N, v) - this.xAxisMin;
                        var a = !isNaN(K) ? K * this.xAxisUnit : 0;
                        O.push(C);
                        y.push(F[U] - a);
                        F[U] -= a;
                        C += this.rowBlockSize + this.blockSeparation
                    }
                    C -= this.rowBlockSize + this.blockSeparation;
                    O.push(this.offsetX + this.left + this.x);
                    y.push(this.offsetY + this.top + this.y);
                    for (var U = S - 1; U >= 0; U--) {
                        O.push(C);
                        y.push(M[U]);
                        M[U] = F[U];
                        C -= this.rowBlockSize + this.blockSeparation
                    }
                    this.addArea(this.drawShape("polygon", O, y, false, false, m, m, "closed"), [N])
                }
            } else {
                for (var V = 0; V < S; V++) {
                    F[V] = this.offsetX + this.left;
                    M[V] = this.offsetX + this.left
                }
                for (var V = 0; V < this.varIndices.length; V++) {
                    var a = this.offsetY + this.top + this.blockSeparation + (this.rowBlockSize / 2);
                    var O = [this.offsetX + this.left];
                    var y = [this.offsetY + this.top];
                    var N = this.varIndices[V];
                    var m = this.colors[V % this.colors.length];
                    for (var U = 0; U < S; U++) {
                        var v = f[U];
                        var K = this.getDataAtPos(N, v) - this.xAxisMin;
                        var C = !isNaN(K) ? K * this.xAxisUnit : 0;
                        O.push(F[U] + C);
                        y.push(a);
                        F[U] += C;
                        a += this.rowBlockSize + this.blockSeparation
                    }
                    a -= this.rowBlockSize + this.blockSeparation;
                    O.push(this.offsetX + this.left);
                    y.push(this.offsetY + this.top + this.y);
                    for (var U = S - 1; U >= 0; U--) {
                        O.push(M[U]);
                        y.push(a);
                        M[U] = F[U];
                        a -= this.rowBlockSize + this.blockSeparation
                    }
                    this.addArea(this.drawShape("polygon", O, y, false, false, m, m, "closed"), [N])
                }
            }
            return true;
        case "BarLine":
            var d = this.xAxisMin < 0 ? Math.abs(this.xAxisMin) * this.xAxisUnit : 0;
            var f = this.isGroupedData ? this.grpIndices : this.smpIndices;
            var u = Math.abs(this.xAxisVarIndices.length - this.xAxis2VarIndices.length);
            var z = this.xAxisVarIndices.length > this.xAxis2VarIndices.length ? 0 : u * this.rowBlockSize;
            var p = this.xAxisVarIndices.length > this.xAxis2VarIndices.length ? u * this.rowBlockSize : 0;
            if (this.graphOrientation == "vertical") {
                var a = (this.offsetY + this.top + this.y) - d;
                var C = this.offsetX + this.left + this.blockSeparation;
                var B = this.rowBlockSize;
                for (var V = 0; V < S; V++) {
                    var N = f[V];
                    for (var U = 0; U < this.xAxisVarIndices.length; U++) {
                        var m = U % this.colors.length;
                        var v = this.xAxisVarIndices[U];
                        var K = this.getDataAtPos(v, N) - this.xAxisMin;
                        var ae = K * this.xAxisUnit - d;
                        this.addArea(this.rectangle(C, a, B, -ae, this.colors[m], g, "closed", false, false, true), [v, N]);
                        if (this.isGroupedData || this.data.y.hasOwnProperty("stdev")) {
                            K = this.getDataAtPos(v, N, "stdev") - this.xAxisMin;
                            if (!isNaN(K)) {
                                var ad = (K / 2) * this.xAxisUnit - (d / 2);
                                this.errorBar(C + this.rowBlockSize / 2, a - ae, C + this.rowBlockSize / 2, (a - ae) + ad, -this.rowBlockSize / 2, g);
                                this.errorBar(C + this.rowBlockSize / 2, a - ae, C + this.rowBlockSize / 2, (a - ae) - ad, -this.rowBlockSize / 2, g)
                            }
                        }
                        C += B
                    }
                    C += this.blockSeparation + z
                }
                var C = this.offsetX + this.left + this.blockSeparation + (this.rowBlockSize / 2);
                var B = C + this.blockSeparation + (Math.max(this.xAxisVarIndices.length, this.xAxis2VarIndices.length) * this.rowBlockSize);
                for (var V = 0; V < S - 1; V++) {
                    var N = f[V];
                    var I = f[V + 1];
                    for (var U = 0; U < this.xAxis2VarIndices.length; U++) {
                        var m = this.coordinateLineColor ? U % this.colors.length : (U + this.xAxisVarIndices.length) % this.colors.length;
                        var v = this.xAxis2VarIndices[U];
                        var H = this.getDataAtPos(v, N) - this.xAxis2Min;
                        var G = this.getDataAtPos(v, I) - this.xAxis2Min;
                        var a = (this.offsetY + this.top + this.y) - H * this.xAxis2Unit;
                        var ae = (this.offsetY + this.top + this.y) - G * this.xAxis2Unit;
                        if (!isNaN(H) && !isNaN(G)) {
                            if (H < G) {
                                this.drawLine("line", B, ae, C, a, this.colors[m], this.lineThickness, "butt", false, false, true)
                            } else {
                                this.drawLine("line", C, a, B, ae, this.colors[m], this.lineThickness, "butt", false, false, true)
                            }
                        }
                        if (this.isGroupedData || this.data.y.hasOwnProperty("stdev")) {
                            H = this.getDataAtPos(v, N, "stdev") - this.xAxis2Min;
                            G = this.getDataAtPos(v, I, "stdev") - this.xAxis2Min;
                            if (!isNaN(H)) {
                                var ad = (H / 2) * this.xAxis2Unit;
                                this.errorBar(C, a, C, a + ad, -this.rowBlockSize / 2, this.colors[m]);
                                this.errorBar(C, a, C, a - ad, -this.rowBlockSize / 2, this.colors[m])
                            }
                            if (!isNaN(G)) {
                                var ad = (G / 2) * this.xAxis2Unit;
                                this.errorBar(B, ae, B, ae + ad, -this.rowBlockSize / 2, this.colors[m]);
                                this.errorBar(B, ae, B, ae - ad, -this.rowBlockSize / 2, this.colors[m])
                            }
                        }
                        if (!isNaN(H)) {
                            this.addArea(this.drawShape("sphere", C, a, this.rowBlockSize / 3, this.rowBlockSize / 3, this.colors[m], g, "closed", false, false, true), [v, N])
                        }
                        if (!isNaN(G)) {
                            if (V == S - 2) {
                                this.addArea(this.drawShape("sphere", B, ae, this.rowBlockSize / 3, this.rowBlockSize / 3, this.colors[m], g, "closed", false, false, true), [v, I])
                            } else {
                                this.drawShape("sphere", B, ae, this.rowBlockSize / 3, this.rowBlockSize / 3, this.colors[m], g, "closed", false, false, true)
                            }
                        }
                        C += this.rowBlockSize;
                        B += this.rowBlockSize
                    }
                    C += this.blockSeparation + p;
                    B += this.blockSeparation + p
                }
            } else {
                var C = this.offsetX + this.left + d;
                var a = this.offsetY + this.top + this.blockSeparation;
                var ae = this.rowBlockSize;
                for (var V = 0; V < S; V++) {
                    var N = f[V];
                    for (var U = 0; U < this.xAxisVarIndices.length; U++) {
                        var m = U % this.colors.length;
                        var v = this.xAxisVarIndices[U];
                        var K = this.getDataAtPos(v, N) - this.xAxisMin;
                        var B = K * this.xAxisUnit - d;
                        this.addArea(this.rectangle(C, a, B, ae, this.colors[m], g, "closed", false, false, true), [v, N]);
                        if (this.isGroupedData || this.data.y.hasOwnProperty("stdev")) {
                            K = this.getDataAtPos(v, N, "stdev") - this.xAxisMin;
                            if (!isNaN(K)) {
                                var A = (K / 2) * this.xAxisUnit - (d / 2);
                                this.errorBar(C + B, a + this.rowBlockSize / 2, (C + B) + A, a + this.rowBlockSize / 2, this.rowBlockSize / 2, g);
                                this.errorBar(C + B, a + this.rowBlockSize / 2, (C + B) - A, a + this.rowBlockSize / 2, this.rowBlockSize / 2, g)
                            }
                        }
                        a += ae
                    }
                    a += this.blockSeparation + z
                }
                var a = this.offsetY + this.top + this.blockSeparation + (this.rowBlockSize / 2);
                var ae = a + this.blockSeparation + (Math.max(this.xAxisVarIndices.length, this.xAxis2VarIndices.length) * this.rowBlockSize);
                for (var V = 0; V < S - 1; V++) {
                    var N = f[V];
                    var I = f[V + 1];
                    for (var U = 0; U < this.xAxis2VarIndices.length; U++) {
                        var m = this.coordinateLineColor ? U % this.colors.length : (U + this.xAxisVarIndices.length) % this.colors.length;
                        var v = this.xAxis2VarIndices[U];
                        var H = this.getDataAtPos(v, N) - this.xAxis2Min;
                        var G = this.getDataAtPos(v, I) - this.xAxis2Min;
                        var C = this.offsetX + this.left + H * this.xAxis2Unit;
                        var B = this.offsetX + this.left + G * this.xAxis2Unit;
                        if (!isNaN(H) && !isNaN(G)) {
                            this.drawLine("line", B, ae, C, a, this.colors[m], this.lineThickness, "butt", false, false, true)
                        }
                        if (this.isGroupedData || this.data.y.hasOwnProperty("stdev")) {
                            H = this.getDataAtPos(v, N, "stdev") - this.xAxis2Min;
                            G = this.getDataAtPos(v, I, "stdev") - this.xAxis2Min;
                            if (!isNaN(H)) {
                                var A = (H / 2) * this.xAxis2Unit;
                                this.errorBar(C, a, C + A, a, this.rowBlockSize / 2, this.colors[m]);
                                this.errorBar(C, a, C - A, a, this.rowBlockSize / 2, this.colors[m])
                            }
                            if (!isNaN(G)) {
                                var A = (G / 2) * this.xAxis2Unit;
                                this.errorBar(B, ae, B + A, ae, this.rowBlockSize / 2, this.colors[m]);
                                this.errorBar(B, ae, B - A, ae, this.rowBlockSize / 2, this.colors[m])
                            }
                        }
                        if (!isNaN(H)) {
                            this.addArea(this.drawShape("sphere", C, a, this.rowBlockSize / 3, this.rowBlockSize / 3, this.colors[m], g, "closed", false, false, true), [v, N])
                        }
                        if (!isNaN(G)) {
                            if (V == S - 2) {
                                this.addArea(this.drawShape("sphere", B, ae, this.rowBlockSize / 3, this.rowBlockSize / 3, this.colors[m], g, "closed", false, false, true), [v, I])
                            } else {
                                this.drawShape("sphere", B, ae, this.rowBlockSize / 3, this.rowBlockSize / 3, this.colors[m], g, "closed", false, false, true)
                            }
                        }
                        a += this.rowBlockSize;
                        ae += this.rowBlockSize
                    }
                    a += this.blockSeparation + p;
                    ae += this.blockSeparation + p
                }
            }
            return true;
        case "Boxplot":
            var f = this.isGroupedData ? this.grpIndices : this.smpIndices;
            if (this.graphOrientation == "vertical") {
                var C = this.offsetX + this.left + this.blockSeparation;
                var B = this.rowBlockSize;
                for (var V = 0; V < S; V++) {
                    var N = f[V];
                    for (var U = 0; U < this.varIndices.length; U++) {
                        var m = U % this.colors.length;
                        var v = this.varIndices[U];
                        var x = this.getDataAtPos(v, N, "iqr1") - this.xAxisMin;
                        var s = this.getDataAtPos(v, N, "iqr3") - this.xAxisMin;
                        var n = this.getDataAtPos(v, N, "qtl1") - this.xAxisMin;
                        var l = this.getDataAtPos(v, N, "qtl3") - this.xAxisMin;
                        var E = this.getDataAtPos(v, N, "median") - this.xAxisMin;
                        var P = this.getDataAtPos(v, N, "out");
                        var a = (this.offsetY + this.top + this.y) - (n * this.xAxisUnit);
                        var ae = (this.offsetY + this.top + this.y) - (l * this.xAxisUnit);
                        var ad = (this.offsetY + this.top + this.y) - (E * this.xAxisUnit);
                        var ac = (this.offsetY + this.top + this.y) - (x * this.xAxisUnit);
                        var aa = (this.offsetY + this.top + this.y) - (s * this.xAxisUnit);
                        var Z = ae - a;
                        this.addArea(this.rectangle(C, a, B, Z, this.colors[m], g, "closed", false, false, true), [v, N]);
                        this.drawLine("line", C, ad, C + (B - 1), ad, g, false, "butt", false, false, true);
                        this.errorBar(C + this.rowBlockSize / 2, a, C + this.rowBlockSize / 2, ac, -this.rowBlockSize / 2, g);
                        this.errorBar(C + this.rowBlockSize / 2, ae, C + this.rowBlockSize / 2, aa, -this.rowBlockSize / 2, g);
                        if (P) {
                            for (var T = 0; T < P.length; T++) {
                                a = (this.offsetY + this.top + this.y) - ((P[T] - this.xAxisMin) * this.xAxisUnit);
                                this.drawShape("sphere", C + this.rowBlockSize / 2, a, this.rowBlockSize / 2, this.rowBlockSize / 2, this.colors[m], g, "closed", false, false, true)
                            }
                        }
                        C += B
                    }
                    C += this.blockSeparation
                }
            } else {
                var a = this.offsetY + this.top + this.blockSeparation;
                var ae = this.rowBlockSize;
                for (var V = 0; V < S; V++) {
                    var N = f[V];
                    for (var U = 0; U < this.varIndices.length; U++) {
                        var m = U % this.colors.length;
                        var v = this.varIndices[U];
                        var x = this.getDataAtPos(v, N, "iqr1") - this.xAxisMin;
                        var s = this.getDataAtPos(v, N, "iqr3") - this.xAxisMin;
                        var n = this.getDataAtPos(v, N, "qtl1") - this.xAxisMin;
                        var l = this.getDataAtPos(v, N, "qtl3") - this.xAxisMin;
                        var E = this.getDataAtPos(v, N, "median") - this.xAxisMin;
                        var P = this.getDataAtPos(v, N, "out");
                        var C = this.offsetX + this.left + (n * this.xAxisUnit);
                        var B = this.offsetX + this.left + (l * this.xAxisUnit);
                        var A = this.offsetX + this.left + (E * this.xAxisUnit);
                        var w = this.offsetX + this.left + (x * this.xAxisUnit);
                        var t = this.offsetX + this.left + (s * this.xAxisUnit);
                        var r = B - C;
                        this.addArea(this.rectangle(C, a, r, ae, this.colors[m], g, "closed", false, false, true), [v, N]);
                        this.drawLine("line", A, a, A, a + (ae - 1), g, false, "butt", false, false, true);
                        this.errorBar(C, a + this.rowBlockSize / 2, w, a + this.rowBlockSize / 2, this.rowBlockSize / 2, g);
                        this.errorBar(B, a + this.rowBlockSize / 2, t, a + this.rowBlockSize / 2, this.rowBlockSize / 2, g);
                        if (P) {
                            for (var T = 0; T < P.length; T++) {
                                C = this.offsetX + this.left + ((P[T] - this.xAxisMin) * this.xAxisUnit);
                                this.drawShape("sphere", C, a + this.rowBlockSize / 2, this.rowBlockSize / 2, this.rowBlockSize / 2, this.colors[m], g, "closed", false, false, true)
                            }
                        }
                        a += ae
                    }
                    a += this.blockSeparation
                }
            }
            return true;
        case "Candlestick":
            var f = this.isGroupedData ? this.grpIndices : this.smpIndices;
            if (this.graphOrientation == "vertical") {
                var C = this.offsetX + this.left + this.blockSeparation;
                var B = this.rowBlockSize;
                for (var V = 0; V < S; V++) {
                    var N = f[V];
                    for (var U = 0; U < this.varIndices.length; U++) {
                        var m = U % this.colors.length;
                        var v = this.varIndices[U];
                        if (this.summaryType == "candle") {
                            var J = this.getDataAtPos(v, N, "open") - this.xAxisMin;
                            var Q = this.getDataAtPos(v, N, "close") - this.xAxisMin;
                            var R = this.getDataAtPos(v, N, "high") - this.xAxisMin;
                            var b = this.getDataAtPos(v, N, "low") - this.xAxisMin;
                            var a = (this.offsetY + this.top + this.y) - (J * this.xAxisUnit);
                            var ae = (this.offsetY + this.top + this.y) - (Q * this.xAxisUnit);
                            var ad = (this.offsetY + this.top + this.y) - (R * this.xAxisUnit);
                            var ac = (this.offsetY + this.top + this.y) - (b * this.xAxisUnit);
                            if (Q > J) {
                                this.addArea(this.rectangle(C, a, B, ae - a, false, this.colors[m], "open", false, false, true), [v, N]);
                                this.drawLine("line", C + this.rowBlockSize / 2, ae, C + this.rowBlockSize / 2, ad, this.colors[m], false, "butt", false, false, true);
                                this.drawLine("line", C + this.rowBlockSize / 2, ac, C + this.rowBlockSize / 2, a, this.colors[m], false, "butt", false, false, true)
                            } else {
                                this.addArea(this.rectangle(C, a, B, ae - a, this.colors[m], this.colors[m], "closed", false, false, true), [v, N]);
                                this.drawLine("line", C + this.rowBlockSize / 2, ac, C + this.rowBlockSize / 2, ae, this.colors[m], false, "butt", false, false, true);
                                this.drawLine("line", C + this.rowBlockSize / 2, a, C + this.rowBlockSize / 2, ad, this.colors[m], false, "butt", false, false, true)
                            }
                        } else {
                            var ab = this.getDataAtPos(v, N, "volume") - this.xAxis2Min;
                            var a = (this.offsetY + this.top + this.y) - (ab * this.xAxis2Unit);
                            var ae = (ab * this.xAxis2Unit);
                            this.addArea(this.rectangle(C, a, B, ae, this.colors[m], this.colors[m], "closed", false, false, true), [v, N])
                        }
                        C += B
                    }
                    C += this.blockSeparation
                }
            } else {
                var a = this.offsetY + this.top + this.blockSeparation;
                var ae = this.rowBlockSize;
                for (var V = 0; V < S; V++) {
                    var N = f[V];
                    for (var U = 0; U < this.varIndices.length; U++) {
                        var m = U % this.colors.length;
                        var v = this.varIndices[U];
                        if (this.summaryType == "candle") {
                            var J = this.getDataAtPos(v, N, "open") - this.xAxisMin;
                            var Q = this.getDataAtPos(v, N, "close") - this.xAxisMin;
                            var R = this.getDataAtPos(v, N, "high") - this.xAxisMin;
                            var b = this.getDataAtPos(v, N, "low") - this.xAxisMin;
                            var C = this.offsetX + this.left + (J * this.xAxisUnit);
                            var B = this.offsetX + this.left + (Q * this.xAxisUnit);
                            var A = this.offsetX + this.left + (R * this.xAxisUnit);
                            var w = this.offsetX + this.left + (b * this.xAxisUnit);
                            if (Q > J) {
                                this.addArea(this.rectangle(C, a, B - C, ae, false, this.colors[m], "open", false, false, true), [v, N]);
                                this.drawLine("line", B, a + this.rowBlockSize / 2, A, a + this.rowBlockSize / 2, this.colors[m], false, "butt", false, false, true);
                                this.drawLine("line", w, a + this.rowBlockSize / 2, C, a + this.rowBlockSize / 2, this.colors[m], false, "butt", false, false, true)
                            } else {
                                this.addArea(this.rectangle(C, a, B - C, ae, this.colors[m], this.colors[m], "closed", false, false, true), [v, N]);
                                this.drawLine("line", w, a + this.rowBlockSize / 2, B, a + this.rowBlockSize / 2, this.colors[m], false, "butt", false, false, true);
                                this.drawLine("line", C, a + this.rowBlockSize / 2, A, a + this.rowBlockSize / 2, this.colors[m], false, "butt", false, false, true)
                            }
                        } else {
                            var ab = this.getDataAtPos(v, N, "volume") - this.xAxis2Min;
                            var C = this.offsetX + this.left;
                            var B = ab * this.xAxis2Unit;
                            this.addArea(this.rectangle(C, a, B, ae, this.colors[m], this.colors[m], "closed", false, false, true), [v, N])
                        }
                        a += ae
                    }
                    a += this.blockSeparation
                }
            }
            return true;
        case "Dotplot":
            if (this.graphOrientation == "vertical") {
                var C = this.offsetX + this.left + this.blockSeparation + this.rowBlockSize / 2;
                var B = this.rowBlockSize;
                var q = this.getShapeSize();
                if (this.isGroupedData) {
                    for (var V = 0; V < S; V++) {
                        var N = this.grpIndices[V];
                        for (var U = 0; U < this.varIndices.length; U++) {
                            var m = U % this.colors.length;
                            var L = parseInt(U / this.colors.length);
                            var v = this.varIndices[U];
                            for (var T = 0; T < this.data.w.grps[N].length; T++) {
                                var Y = this.data.w.grps[N][T];
                                var K = this.data.y.data[v][Y] - this.xAxisMin;
                                var a = (this.offsetY + this.top + this.y) - (K * this.xAxisUnit);
                                if (!isNaN(K)) {
                                    this.addArea(this.drawShape(this.shapes[L], C, a, q, q, this.colors[m], g, "closed", false, false, true), [v, N])
                                }
                            }
                            C += B
                        }
                        C += this.blockSeparation
                    }
                } else {
                    for (var V = 0; V < S; V++) {
                        var N = this.smpIndices[V];
                        for (var U = 0; U < this.varIndices.length; U++) {
                            var m = U % this.colors.length;
                            var L = parseInt(U / this.colors.length);
                            var v = this.varIndices[U];
                            var K = this.getDataAtPos(v, N) - this.xAxisMin;
                            var a = (this.offsetY + this.top + this.y) - (K * this.xAxisUnit);
                            if (this.data.y.hasOwnProperty("stdev")) {
                                var W = this.getDataAtPos(v, N, "stdev") - this.xAxisMin;
                                if (!isNaN(W)) {
                                    var ae = (W / 2) * this.xAxisUnit;
                                    this.errorBar(C, a, C, a + ae, -this.blockSeparation, this.colors[m]);
                                    this.errorBar(C, a, C, a - ae, -this.blockSeparation, this.colors[m])
                                }
                            }
                            if (!isNaN(K)) {
                                this.addArea(this.drawShape(this.shapes[L], C, a, q, q, this.colors[m], g, "closed", false, false, true), [v, N])
                            }
                            C += B
                        }
                        C += this.blockSeparation
                    }
                }
            } else {
                var a = this.offsetY + this.top + this.blockSeparation + this.rowBlockSize / 2;
                var ae = this.rowBlockSize;
                var q = this.getShapeSize();
                if (this.isGroupedData) {
                    for (var V = 0; V < S; V++) {
                        var N = this.grpIndices[V];
                        for (var U = 0; U < this.varIndices.length; U++) {
                            var m = U % this.colors.length;
                            var L = parseInt(U / this.colors.length);
                            var v = this.varIndices[U];
                            for (var T = 0; T < this.data.w.grps[N].length; T++) {
                                var Y = this.data.w.grps[N][T];
                                var K = this.data.y.data[v][Y] - this.xAxisMin;
                                var C = this.offsetX + this.left + K * this.xAxisUnit;
                                if (!isNaN(K)) {
                                    this.addArea(this.drawShape(this.shapes[L], C, a, q, q, this.colors[m], g, "closed", false, false, true), [v, N])
                                }
                            }
                            a += ae
                        }
                        a += this.blockSeparation
                    }
                } else {
                    for (var V = 0; V < S; V++) {
                        var N = this.smpIndices[V];
                        for (var U = 0; U < this.varIndices.length; U++) {
                            var m = U % this.colors.length;
                            var L = parseInt(U / this.colors.length);
                            var v = this.varIndices[U];
                            var K = this.getDataAtPos(v, N) - this.xAxisMin;
                            var C = this.offsetX + this.left + K * this.xAxisUnit;
                            if (this.data.y.hasOwnProperty("stdev")) {
                                var W = this.getDataAtPos(v, N, "stdev") - this.xAxisMin;
                                if (!isNaN(W)) {
                                    var B = (W / 2) * this.xAxisUnit;
                                    this.errorBar(C, a, C + B, a, this.blockSeparation, this.colors[m]);
                                    this.errorBar(C, a, C - B, a, this.blockSeparation, this.colors[m])
                                }
                            }
                            if (!isNaN(K)) {
                                this.addArea(this.drawShape(this.shapes[L], C, a, q, q, this.colors[m], g, "closed", false, false, true), [v, N])
                            }
                            a += ae
                        }
                        a += this.blockSeparation
                    }
                }
            }
            return true;
        case "Heatmap":
            var a = this.offsetY + this.top;
            var B = this.colBlockSize;
            var ae = this.rowBlockSize;
            var h = "rgb(255,255, 0)";
            var f = this.isGroupedData ? this.grpIndices : this.smpIndices;
            var o = this.varIndices.length * 5 > this.x ? true : false;
            for (var V = 0; V < S; V++) {
                var C = this.offsetX + this.left;
                var N = f[V];
                for (var U = 0; U < this.varIndices.length; U++) {
                    var v = this.varIndices[U];
                    var K = this.getDataAtPos(v, N);
                    if (!isNaN(K)) {
                        var m = this.getHeatmapColor(this.minData, this.maxData, K);
                        if (o) {
                            g = m
                        }
                        this.addArea(this.rectangle(C, a, B, ae, m, g, "closed", false, false, true), [v, N]);
                        if (this.showDataValues) {
                            this.drawText(K, (C + B) - 2, (a + ae) - 2, this.overlayFont, this.foreground, "right", "bottom")
                        }
                    } else {
                        this.addArea(this.rectangle(C, a, B, ae, h, g, "closed", false, false, true), [v, N])
                    }
                    C += B
                }
                a += ae
            }
            return true
        }
        throw 'Illegal graph type "' + this.graphType + '"'
    };
    this.draw1DLegend = function () {
        if (this.showLegend && this.graphType != "Heatmap") {
            if (this.colorBy && this.graphType == "Bar") {
                var d = this.offsetX + this.left;
                var n = this.offsetY + this.top;
                if (this.legendPosition == "right") {
                    if (this.legendColorsN > 0) {
                        d = (d + this.x + this.right) - (this.legendColorWidth + this.margin);
                        n += (this.y - this.legendColorHeight) / 2;
                        this.drawColorLegend(d, n)
                    } else {
                        d = (d + this.x + this.right) - this.legendColorIndicatorHeight;
                        n += ((this.y - this.legendColorIndicatorWidth) / 2) + this.legendColorIndicatorWidth;
                        this.drawColorIndicator(d, n, this.legendColorRange[0], this.legendColorRange[1], this.legendColorDecs)
                    }
                } else {
                    if (this.legendColorsN > 0) {
                        d += (this.x - this.legendColorWidth) / 2;
                        n = (n + this.y + this.bottom) - (this.legendColorHeight + this.margin);
                        this.drawColorLegend(d, n)
                    } else {
                        d += ((this.x - this.legendColorIndicatorWidth) / 2);
                        n = (n + this.y + this.bottom) - this.legendColorIndicatorHeight;
                        this.drawColorIndicator(d, n, this.legendColorRange[0], this.legendColorRange[1], this.legendColorDecs)
                    }
                }
            } else {
                if (this.graphType == "BarLine") {
                    var h = this.maxVarName > this.maxVarStringLen ? this.maxVarNameStr.substring(0, this.maxVarStringLen) : this.maxVarNameStr;
                    var f = this.getFontPt(this.legendFont);
                    var d = this.legendPosition == "right" ? this.offsetX + this.left + this.x + (this.margin * 4) : (this.offsetX + this.left + (this.x / 2)) - ((this.measureText(h, this.legendFont) + f) / 2);
                    var n = this.legendPosition == "right" ? (this.offsetY + this.top + (this.y / 2)) - (this.getLegendHeight(true) / 2) : this.offsetY + this.top + this.y;
                    if (this.smpDendrogramPosition == "right") {
                        d += this.getSmpTreeWidthHeight()
                    }
                    if (this.legendPosition == "bottom") {
                        if (this.graphOrientation == "vertical") {
                            n += this.getSampleTitleHeight() + this.getSampleLabelLength() + this.getSmpOverlaysLength() + this.margin
                        } else {
                            n += this.getAxisTitleTickLength() + this.margin
                        }
                        if (this.showSmpDendrogram && this.smpDendrogramPosition == "bottom") {
                            n += this.getSmpTreeWidthHeight()
                        }
                    } else {
                        if (this.graphOrientation == "vertical") {
                            d += this.getAxisTitleTickLength()
                        }
                    }
                    var m = n - this.margin;
                    if (this.legendBox && this.legendBackgroundColor) {
                        var h = this.maxVarName > this.maxVarStringLen ? this.maxVarNameStr.substring(0, this.maxVarStringLen) : this.maxVarNameStr;
                        var l = ((this.xAxisVarIndices.length + this.xAxis2VarIndices.length) * f * 1.5) + this.margin;
                        var a = (this.margin * 3) + (f * 2) + this.measureText(h, this.legendFont);
                        this.rectangle(d - this.margin * 2, m, a, l, this.legendBackgroundColor, this.legendBackgroundColor, "close")
                    }
                    for (var g = 0; g < this.xAxisVarIndices.length; g++) {
                        var o = this.xAxisVarIndices[g];
                        var e = g % this.colors.length;
                        var h = this.shortenText(this.data.y.vars[o], this.maxVarStringLen);
                        var j = this.getVarColor(this.data.y.vars[o]);
                        this.rectangle(d, n, f, f, this.colors[e]);
                        this.drawText(h, d + (f * 2), n + (f / 2), this.legendFont, j, "left", "middle");
                        n += f * 1.5
                    }
                    for (var g = 0; g < this.xAxis2VarIndices.length; g++) {
                        var o = this.xAxis2VarIndices[g];
                        var e = this.coordinateLineColor ? g % this.colors.length : (g + this.xAxisVarIndices.length) % this.colors.length;
                        var h = this.shortenText(this.data.y.vars[o], this.maxVarStringLen);
                        var j = this.getVarColor(this.data.y.vars[o]);
                        this.drawLine("line", d, n + (f / 2), d + f, n + (f / 2), this.colors[e]);
                        this.drawText(h, d + (f * 2), n + (f / 2), this.legendFont, j, "left", "middle");
                        n += f * 1.5
                    }
                    if (this.legendBox) {
                        var h = this.maxVarName > this.maxVarStringLen ? this.maxVarNameStr.substring(0, this.maxVarStringLen) : this.maxVarNameStr;
                        var l = ((n - m) - (f / 2)) + this.margin;
                        var a = (this.margin * 3) + (f * 2) + this.measureText(h, this.legendFont);
                        this.rectangle(d - this.margin * 2, m, a, l, false, this.foreground, "open")
                    }
                } else {
                    var h = this.maxVarName > this.maxVarStringLen ? this.maxVarNameStr.substring(0, this.maxVarStringLen) : this.maxVarNameStr;
                    var f = this.getFontPt(this.legendFont);
                    var d = this.legendPosition == "right" ? this.offsetX + this.left + this.x + (this.margin * 4) : (this.offsetX + this.left + (this.x / 2)) - ((this.measureText(h, this.legendFont) + f) / 2);
                    var n = this.legendPosition == "right" ? (this.offsetY + this.top + (this.y / 2)) - (this.getLegendHeight(true) / 2) : this.offsetY + this.top + this.y;
                    if (this.legendPosition == "bottom") {
                        if (this.graphOrientation == "vertical") {
                            n += this.getSampleTitleHeight() + this.getSampleLabelLength() + this.getSmpOverlaysLength() + this.margin
                        } else {
                            n += this.getAxisTitleTickLength() + this.margin
                        }
                        if (this.showSmpDendrogram && this.smpDendrogramPosition == "bottom") {
                            n += this.getSmpTreeWidthHeight()
                        }
                    } else {
                        if (this.graphOrientation == "vertical") {
                            d += this.getAxisTitleTickLength()
                        }
                    }
                    if (this.smpDendrogramPosition == "right" && this.showLegend && this.legendPosition == "right") {
                        d += this.getSmpTreeWidthHeight()
                    }
                    var m = n - this.margin;
                    if (this.legendBox && this.legendBackgroundColor) {
                        var h = this.maxVarName > this.maxVarStringLen ? this.maxVarNameStr.substring(0, this.maxVarStringLen) : this.maxVarNameStr;
                        var l = ((this.varIndices.length * f * 1.5)) + this.margin;
                        var a = (this.margin * 3) + (f * 2) + this.measureText(h, this.legendFont);
                        this.disableGradientTransparency();
                        this.rectangle(d - this.margin * 2, m, a, l, this.legendBackgroundColor, this.legendBackgroundColor, "close");
                        this.enableGradientTransparency()
                    }
                    for (var g = 0; g < this.varIndices.length; g++) {
                        var o = this.varIndices[g];
                        var e = g % this.colors.length;
                        var p = this.shapes[g % this.shapes.length];
                        var h = this.shortenText(this.data.y.vars[o], this.maxVarStringLen);
                        var j = this.getVarColor(this.data.y.vars[o]);
                        var b = this.getFontPt(this.legendFont);
                        if (this.graphType == "Line") {
                            var k = this.rowBlockSize / 2;
                            if (this.lineDecoration) {
                                if (this.lineDecoration == "symbol") {
                                    this.drawLine("line", d, n + (f / 2), d + f, n + (f / 2), this.foreground);
                                    this.drawShape(p, d + (f / 2), n + (f / 2), b, b, this.colors[e], this.foreground, "closed")
                                } else {
                                    if (this.lineDecoration == "dot") {
                                        this.drawLine("line", d, n + (f / 2), d + f, n + (f / 2), this.colors[e]);
                                        this.drawShape("circle", d + (f / 2), n + (f / 2), b, b, this.colors[e], this.foreground, "closed")
                                    }
                                }
                            } else {
                                this.drawLine("line", d, n + (f / 2), d + f, n + (f / 2), this.colors[e])
                            }
                        } else {
                            if (this.graphType == "Dotplot") {
                                p = this.shapes[parseInt(g / this.colors.length)];
                                this.drawShape(p, d + (f / 2), n + (f / 2), f, f, this.colors[e], this.foreground, "closed")
                            } else {
                                this.rectangle(d, n, f, f, this.colors[e])
                            }
                        }
                        this.drawText(h, d + (f * 2), n + (f / 2), this.legendFont, j, "left", "middle");
                        n += f * 1.5
                    }
                    if (this.legendBox) {
                        var h = this.maxVarName > this.maxVarStringLen ? this.maxVarNameStr.substring(0, this.maxVarStringLen) : this.maxVarNameStr;
                        var l = ((n - m) - (f / 2)) + this.margin;
                        var a = (this.margin * 3) + (f * 2) + this.measureText(h, this.legendFont);
                        this.rectangle(d - this.margin * 2, m, a, l, false, this.foreground, "open")
                    }
                }
            }
        }
    };
    this.setIndicatorsLegendPositions = function () {
        if (this.colorBy && this.graphType == "Bar") {
            this.indicatorsPosition = this.legendPosition
        }
    };
    this.draw1DLayout = function () {
        this.drawTrees();
        this.draw1DXLayout();
        this.draw1DYLayout()
    };
    this.draw1DData = function () {
        this.drawPlotWindow();
        this.draw1DWireFrame();
        this.draw1DDataPoints();
        this.maskPlotArea();
        this.draw1DLayout();
        this.draw1DLegend();
        this.drawTitle()
    };
    this.draw1DPlot = function () {
        this.setIndicatorsLegendPositions();
        this.set1DAttributes();
        this.set1DXYDimensions();
        this.set1DPrivateParams();
        this.set1DXAxis();
        this.draw1DData()
    };
    this.initialize1DPlot = function () {
        if (this.layoutValid) {
            this.drawLayoutCompartments(this.draw1DPlot)
        } else {
            this.draw1DPlot()
        }
    };
    this.initialize1DPlot()
};
CanvasXpress.prototype.Network = function () {
    this.findXYCoordinates = function (b) {
        var a = this.adjustedCoordinates(b);
        var d = {};
        d.x = (a.x / this.scaleFactor || 1) - this.offsetX;
        d.y = (a.y / this.scaleFactor || 1) - this.offsetY;
        return d
    };
    this.addNode = function (e, d) {
        if (e) {
            if (!e.id) {
                var a = 0;
                var f = "id" + a;
                while (this.nodes.hasOwnProperty(f)) {
                    a++;
                    f = "id" + a
                }
                e.id = f
            }
            if (!e.color) {
                e.color = this.colors[0]
            }
            if (!e.shape) {
                e.shape = this.shapes[0]
            }
            if (!e.size) {
                e.size = 1
            }
            if (d) {
                var b = this.findXYCoordinates(d);
                e.x = b.x;
                e.y = b.y
            }
            this.data.nodeIndices[e.id] = this.data.nodes.length;
            this.data.nodes.push(e);
            this.nodes[e.id] = e;
            return e
        }
    };
    this.removeNode = function (d, k) {
        if (d && d.id) {
            var a = k ? this.getObjectArray(this.findChildrenNodeIndices(d.id)) : false;
            var g = [];
            var b = 0;
            if (this.nodeParentHood[d.id]) {
                for (var h = 0; h < this.nodeParentHood[d.id].children.length; h++) {
                    var l = this.nodeParentHood[d.id].children[h];
                    delete(this.data.nodes[this.data.nodeIndices[l]].parentNode)
                }
            }
            this.data.nodeIndices = {};
            for (var h = 0; h < this.data.nodes.length; h++) {
                if (this.data.nodes[h].id != d.id) {
                    if (a && a.hasOwnProperty(h)) {
                        var f = this.edgesForNode(this.data.edges, this.data.nodes[h].id);
                        for (var e = 0; e < f.length; e++) {
                            this.removeEdge(f[e])
                        }
                    } else {
                        g.push(this.data.nodes[h]);
                        this.data.nodeIndices[this.data.nodes[h].id] = b;
                        b++
                    }
                } else {
                    var f = this.edgesForNode(this.data.edges, d.id);
                    for (var e = 0; e < f.length; e++) {
                        this.removeEdge(f[e])
                    }
                }
            }
            this.data.nodes = g
        }
    };
    this.addEdge = function (a) {
        if (a && a.id1 && a.id2) {
            if (!a.color) {
                a.color = this.colors[0]
            }
            if (!a.width) {
                a.width = 1
            }
            if (!a.type) {
                a.type = "line"
            }
            this.data.edges.push(a)
        }
    };
    this.removeEdge = function (d) {
        if (d && d.id1 && d.id2) {
            var b = [];
            for (var a = 0; a < this.data.edges.length; a++) {
                if (this.data.edges[a].id1 != d.id1 || this.data.edges[a].id2 != d.id2) {
                    b.push(this.data.edges[a])
                }
            }
            this.data.edges = b
        }
    };
    this.getAllObjectAttributes = function (d) {
        var e = [];
        if (this.data[d]) {
            for (var b = 0; b < this.data[d].length; b++) {
                for (var a in this.data[d][b]) {
                    if (e.hasOwnProperty(a)) {
                        e[a]++
                    } else {
                        e[a] = 1
                    }
                }
            }
        }
        return e
    };
    this.setAllObjectAttributes = function (l, m, q) {
        if (this.data[l]) {
            var n = this.getAllObjectAttributeValues(l, q);
            if (n) {
                if (this.isNumeric(n)) {
                    var j = this.range(n);
                    if (m == "color") {
                        var o = "rgb(255,255,0)";
                        this.setHeatmapColors(j[1] - j[0]);
                        for (var g = 0; g < this.data[l].length; g++) {
                            if (this.data[l][g].hasOwnProperty(q)) {
                                var e = this.data[l][g][q];
                                var d = this.getHeatmapColor(j[0], j[1], e);
                                this.data[l][g][m] = d
                            } else {
                                this.data[l][g][m] = o
                            }
                        }
                    } else {
                        if (m == "size") {
                            for (var g = 0; g < this.data[l].length; g++) {
                                if (this.data[l][g].hasOwnProperty(q)) {
                                    var e = this.data[l][g][q];
                                    var f = this.percentile(j[0], j[1], e);
                                    var b = parseInt(f * 25 / 10);
                                    this.data[l][g][m] = 1 + (b / 100)
                                } else {
                                    this.data[l][g][m] = 0.5
                                }
                            }
                        } else {
                            if (m == "shape" && l == "nodes") {
                                var s = "square";
                                for (var g = 0; g < this.data[l].length; g++) {
                                    if (this.data[l][g].hasOwnProperty(q)) {
                                        var e = this.data[l][g][q];
                                        var f = this.percentile(j[0], j[1], e);
                                        var r = parseInt(f / 10);
                                        if (r > 0) {
                                            r--
                                        }
                                        this.data[l][g][m] = "pie" + r
                                    } else {
                                        this.data[l][g][m] = s
                                    }
                                }
                            }
                        }
                    }
                } else {
                    var p = 0;
                    var h = 0;
                    var k = 0.1;
                    var a = {
                        color: {},
                        shape: {},
                        size: {}
                    };
                    for (var g = 0; g < n.length; g++) {
                        if (!a.color.hasOwnProperty(n[g])) {
                            a.color[n[g]] = this.colors[p % this.colors.length];
                            p++
                        }
                        if (l == "nodes") {
                            if (!a.shape.hasOwnProperty(n[g])) {
                                a.shape[n[g]] = this.shapes[h % this.shapes.length];
                                h++
                            }
                        } else {
                            if (!a.shape.hasOwnProperty(n[g])) {
                                a.lines[n[g]] = this.lines[h % this.lines.length];
                                h++
                            }
                        }
                        if (!a.size.hasOwnProperty(n[g])) {
                            a.size[n[g]] = k;
                            k += 0.1
                        }
                    }
                    for (var g = 0; g < this.data[l].length; g++) {
                        if (this.data[l][g].hasOwnProperty(q)) {
                            var e = this.data[l][g][q];
                            this.data[l][g][m] = a[m][e]
                        }
                    }
                }
            }
        }
    };
    this.getAllObjectAttributeValues = function (d, a) {
        var e = [];
        if (this.data[d]) {
            for (var b = 0; b < this.data[d].length; b++) {
                if (this.data[d][b].hasOwnProperty(a)) {
                    e.push(this.data[d][b][a])
                }
            }
        }
        return e
    };
    this.modifyNodeSize = function (e, a, b) {
        var d = this.data.nodes[this.data.nodeIndices[e]];
        delete(d.size);
        d.width = a;
        d.height = b
    };
    this.modifyXYNode = function (h, d, a) {
        var g = this.data.nodes[this.data.nodeIndices[h]];
        g.x -= d;
        g.y -= a;
        if (g.labelX != null && g.labelY != null) {
            this.modifyXYNodeLab(h, d, a)
        }
        if (this.networkLayoutType != "radial") {
            if (this.nodeParentHood[g.id] && this.nodeParentHood[g.id]["children"]) {
                for (var f = 0; f < this.nodeParentHood[g.id]["children"].length; f++) {
                    var b = this.nodeParentHood[g.id]["children"][f];
                    var e = this.data.nodes[this.data.nodeIndices[b]];
                    this.modifyXYNode(b, d, a)
                }
            }
        }
    };
    this.modifyXYNodeLab = function (h, d, b) {
        var g = this.data.nodes[this.data.nodeIndices[h]];
        var e = g.label != null ? g.label : g.name ? g.name : g.id;
        var f = this.measureText(e, this.nodeFont);
        var a = this.nodeFontSize;
        if (g.labelX != null && g.labelY != null) {
            g.labelX -= d;
            g.labelY -= b
        } else {
            if (this.rangeX > this.rangeY) {
                g.labelX = (g.x - (f / 2)) - d;
                g.labelY = (g.y + ((this.nodeSize * g.size) / 1.3)) - b
            } else {
                g.labelX = (g.x + ((this.nodeSize * g.size) / 1.3)) - d;
                g.labelY = (g.y - (a / 2)) - b
            }
        }
    };
    this.modifyXYNodeDec = function (e, b, a) {
        var d = this.data.nodes[this.data.nodeIndices[e]];
        d.decorationsX -= b;
        d.decorationsY -= a
    };
    this.getHiddenNodes = function () {
        var b = [];
        if (this.data.nodes) {
            for (var a = 0; a < this.data.nodes.length; a++) {
                if (this.data.nodes[a].hide) {
                    b.push(this.data.nodes[a])
                }
            }
        }
        return b
    };
    this.sendNodeToBack = function (d) {
        var b = [d];
        for (var a = 0; a < this.data.nodes.length; a++) {
            if (this.data.nodes[a].id != d.id) {
                b.push(this.data.nodes[a])
            }
        }
        this.data.nodes = b;
        this.setNodeIndices()
    };
    this.sendNodeBackward = function (e) {
        var d = [];
        var a = this.data.nodeIndices[e.id];
        if (a > 0) {
            for (var b = 0; b < a - 1; b++) {
                d.push(this.data.nodes[b])
            }
            d.push(this.data.nodes[a]);
            d.push(this.data.nodes[a - 1]);
            for (var b = a + 1; b < this.data.nodes.length; b++) {
                d.push(this.data.nodes[b])
            }
            this.data.nodes = d;
            this.setNodeIndices()
        }
    };
    this.bringNodeToFront = function (d) {
        var b = [];
        for (var a = 0; a < this.data.nodes.length; a++) {
            if (this.data.nodes[a].id != d.id) {
                b.push(this.data.nodes[a])
            }
        }
        b.push(d);
        this.data.nodes = b;
        this.setNodeIndices()
    };
    this.bringNodeForward = function (e) {
        var d = [];
        var a = this.data.nodeIndices[e.id];
        if (this.data.nodes.length - 1 > a) {
            for (var b = 0; b < a; b++) {
                d.push(this.data.nodes[b])
            }
            d.push(this.data.nodes[a + 1]);
            d.push(this.data.nodes[a]);
            for (var b = a + 2; b < this.data.nodes.length; b++) {
                d.push(this.data.nodes[b])
            }
            this.data.nodes = d;
            this.setNodeIndices()
        }
    };
    this.addLegend = function (a, e, d) {
        this.inititalizeNetworkLegend();
        if (a && e) {
            if (d && a == "text") {
                var b = this.findXYCoordinates(d);
                e.x = b.x;
                e.y = b.y
            }
            this.data.legend[a].push(e);
            return e
        }
    };
    this.deleteLegend = function (d, e) {
        if (d && e && e.id) {
            var b = [];
            for (var a = 0; a < this.data.legend[d].length; a++) {
                if (this.data.legend[d][a].id != e.id) {
                    b.push(this.data.legend[d][a])
                }
            }
            this.data.legend[d] = b;
            delete this.data.legend.ids[d][e.id]
        }
    };
    this.alignDistributeSelectedNodes = function (b) {
        if (this.isSelectNodes > 1) {
            var e, a, g, k, f;
            var h = this.getSelectedNodeCoordinates();
            if (b == 84) {
                e = this.min(h.y);
                for (var f in this.selectNode) {
                    var d = this.data.nodes[this.data.nodeIndices[f]];
                    this.addCoordinatesToStack(d, d.x, d.y);
                    d.y = e
                }
            } else {
                if (b == 82) {
                    e = this.max(h.x);
                    for (var f in this.selectNode) {
                        var d = this.data.nodes[this.data.nodeIndices[f]];
                        this.addCoordinatesToStack(d, d.x, d.y);
                        d.x = e
                    }
                } else {
                    if (b == 66) {
                        e = this.max(h.y);
                        for (var f in this.selectNode) {
                            var d = this.data.nodes[this.data.nodeIndices[f]];
                            this.addCoordinatesToStack(d, d.x, d.y);
                            d.y = e
                        }
                    } else {
                        if (b == 76) {
                            e = this.min(h.x);
                            for (var f in this.selectNode) {
                                var d = this.data.nodes[this.data.nodeIndices[f]];
                                this.addCoordinatesToStack(d, d.x, d.y);
                                d.x = e
                            }
                        } else {
                            if (b == 86) {
                                a = this.range(h.y);
                                g = this.rank(h.y);
                                k = (a[1] - a[0]) / (this.isSelectNodes - 1);
                                e = a[0];
                                for (var j = 0; j < g.length; j++) {
                                    f = h.i[g[j]];
                                    var d = this.data.nodes[this.data.nodeIndices[f]];
                                    this.addCoordinatesToStack(d, d.x, d.y);
                                    d.y = e + (j * k)
                                }
                            } else {
                                if (b == 72) {
                                    a = this.range(h.x);
                                    g = this.rank(h.x);
                                    k = (a[1] - a[0]) / (this.isSelectNodes - 1);
                                    e = a[0];
                                    for (var j = 0; j < g.length; j++) {
                                        f = h.i[g[j]];
                                        var d = this.data.nodes[this.data.nodeIndices[f]];
                                        this.addCoordinatesToStack(d, d.x, d.y);
                                        d.x = e + (j * k)
                                    }
                                } else {
                                    if (b == 85) {
                                        for (var f in this.selectNode) {
                                            var d = this.data.nodes[this.data.nodeIndices[f]];
                                            if (d.stack && d.stack.x.length > 0) {
                                                d.x = d.stack.x.pop();
                                                d.y = d.stack.y.pop()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            this.draw()
        }
    };
    this.getSelectedNodeCoordinates = function () {
        var d = {
            x: [],
            y: [],
            i: []
        };
        for (var a in this.selectNode) {
            var b = this.data.nodes[this.data.nodeIndices[a]];
            d.x.push(b.x);
            d.y.push(b.y);
            d.i.push(a)
        }
        return d
    };
    this.addCoordinatesToStack = function (b, a, d) {
        if (!b.stack) {
            b.stack = {
                x: [],
                y: []
            }
        }
        b.stack.x.push(a);
        b.stack.y.push(d)
    };
    this.findNodesBy = function (g) {
        var a = [];
        var k = 0;
        for (var b in g) {
            g[b] = this.toArray(g[b]);
            k++
        }
        for (var h = 0; h < this.data.nodes.length; h++) {
            var d = this.data.nodes[h];
            var e = 0;
            for (var b in g) {
                var l = g[b];
                for (var f = 0; f < l.length; f++) {
                    if (d[b] && d[b] == l[f]) {
                        e++
                    }
                }
            }
            if (e == k) {
                a.push(d)
            }
        }
        return a
    };
    this.setNetworkDimensions = function () {
        this.x = this.width;
        this.y = this.height
    };
    this.setNetworkPrivateParams = function () {
        if (this.data.nodes.length > 0) {
            this.layoutDone = false;
            this.x = this.width;
            this.y = this.height;
            if (this.networkLayoutType == "radial") {
                if (this.width > this.height) {
                    this.width = this.height
                } else {
                    this.height = this.width
                }
                this.x = this.width;
                this.y = this.height;
                this.networkXCenter = this.x / 2;
                this.networkYCenter = this.y / 2
            } else {
                this.x = this.width;
                this.y = this.height;
                this.set3DParams();
                this.area = this.x * this.y;
                this.k = Math.sqrt(this.area / this.data.nodes.length);
                this.temperature = this.data.nodes.length + Math.floor(Math.sqrt(this.data.edges.length));
                this.minimumTemperature = 1;
                this.initialTemperature = this.temperature;
                this.iteration = 0
            }
            if (this.x <= 400 || this.y <= 400) {
                this.nodeSize -= 4;
                this.nodeFont = (this.nodeSize - 6) + "Pt " + this.fontName;
                this.showAnimationFont = this.nodeFont + "Pt " + this.fontName
            } else {
                this.edgeWidth = 2
            }
        } else {
            this.layoutDone = true
        }
    };
    this.networkContains = function (a, b) {
        return this.findNode(a, b) != null
    };
    this.findNode = function (b, e) {
        for (var d in b) {
            var a = b[d];
            if (e.call(a)) {
                return a
            }
        }
        return null
    };
    this.filterEdges = function (b, f) {
        var e = [];
        for (var d in b) {
            var a = b[d];
            if (f.call(a)) {
                e.push(a)
            }
        }
        return e
    };
    this.reduceNetwork = function (a, f, d) {
        var e = null;
        for (var b in a) {
            if (e == null) {
                if (d) {
                    e = d.apply(d, [a[b]])
                } else {
                    e = a[b]
                }
            } else {
                e = f.apply(a[b], [e, a[b]])
            }
        }
        return e
    };
    this.vectorDifference = function (b, a) {
        return {
            x: b.x - a.x,
            y: b.y - a.y,
            z: b.z - a.z
        }
    };
    this.vectorMagnitude = function (a) {
        if (this.is3DNetwork) {
            var b = Math.sqrt((a.x * a.x) + (a.y * a.y) + (a.z * a.z));
            return isNaN(b) ? 0 : b
        } else {
            return Math.sqrt((a.x * a.x) + (a.y * a.y))
        }
    };
    this.normalizeGraph = function (j) {
        var h = [];
        var a = {};
        var k = this;
        for (var f in j.nodes) {
            if (!a.hasOwnProperty(j.nodes[f].id)) {
                var e = j.nodes[f];
                var d = [];
                a[e.id] = 1;
                if (!this.networkContains(d, function () {
                    return k.networkContains(this.nodes, function () {
                        return this.id = e.id
                    })
                })) {
                    var b = {};
                    b.nodes = [];
                    b.edges = [];
                    this.addNodeToGraph(e, b, j);
                    if (b.nodes.length > 0) {
                        h.push(b)
                    }
                    for (var g = 0; g < b.nodes.length; g++) {
                        a[b.nodes[g].id] = 1
                    }
                }
            }
        }
        return h
    };
    this.addNodeToGraph = function (d, f, e) {
        if (this.findNodeForId(d.id, f) == null) {
            f.nodes.push(d);
            var a = this.edgesForNode(e.edges, d.id);
            for (edgeIndex in a) {
                var b = a[edgeIndex];
                f.edges.push(b);
                if (b.id1 == d.id) {
                    this.addNodeToGraph(this.findNodeForId(b.id2, e), f, e)
                } else {
                    this.addNodeToGraph(this.findNodeForId(b.id1, e), f, e)
                }
            }
        }
    };
    this.findNodeForId = function (b, a) {
        return this.findNode(a.nodes, function () {
            return b == this.id
        })
    };
    this.edgesForNode = function (a, b) {
        return this.filterEdges(a, function (d) {
            return (this.id1 == b || this.id2 == b)
        })
    };
    this.attractiveForce = function (b, a) {
        return (b * b) / a
    };
    this.repulsiveForce = function (b, a) {
        return (a * a) / b
    };
    this.storePositions = function () {
        var e = {};
        for (var b = 0; b < this.data.nodes.length; b++) {
            var d = this.data.nodes[b];
            if (!d.hide && !d.hiddenParent) {
                var a = {
                    x: d.x,
                    y: d.y,
                    z: d.z
                };
                e[d.id] = a
            }
        }
        return e
    };
    this.calculateRepulsiveDisplacement = function (f) {
        for (var e = 0; e < this.data.nodes.length; e++) {
            var k = this.data.nodes[e];
            if (!k.hide && !k.hiddenParent) {
                var a = {
                    x: 0,
                    y: 0,
                    z: 0
                };
                for (var b = 0; b < this.data.nodes.length; b++) {
                    var g = this.data.nodes[b];
                    if (!g.hide && !g.hiddenParent) {
                        var h = this.vectorDifference(k, g);
                        var d = this.vectorMagnitude(h);
                        if (d != 0) {
                            a.x += (h.x / d) * this.repulsiveForce(d, this.k);
                            a.y += (h.y / d) * this.repulsiveForce(d, this.k);
                            a.z += (h.z / d) * this.repulsiveForce(d, this.k)
                        }
                    }
                    f[k.id] = a
                }
            }
        }
    };
    this.calculateAttractiveDisplacement = function (f) {
        for (var d = 0; d < this.data.edges.length; d++) {
            var e = this.data.edges[d];
            if (!this.nodes || !this.nodes[e.id1] || !this.nodes[e.id2]) {
                alert('Nodes "' + e.id1 + '" and/or "' + e.id2 + '" are missing!');
                return
            }
            if (this.nodes && !this.nodes[e.id1].hide && !this.nodes[e.id2].hide && !this.nodes[e.id1].hiddenParent && !this.nodes[e.id2].hiddenParent) {
                var h = this.vectorDifference(this.nodes[e.id1], this.nodes[e.id2]);
                var b = this.vectorMagnitude(h);
                if (b != 0) {
                    var g = f[e.id1];
                    g.x -= (h.x / b) * this.attractiveForce(b, this.k);
                    g.y -= (h.y / b) * this.attractiveForce(b, this.k);
                    g.z -= (h.z / b) * this.attractiveForce(b, this.k);
                    var a = f[e.id2];
                    a.x += (h.x / b) * this.attractiveForce(b, this.k);
                    a.y += (h.y / b) * this.attractiveForce(b, this.k);
                    a.z += (h.z / b) * this.attractiveForce(b, this.k)
                }
            }
        }
    };
    this.isLayoutDone = function () {
        var j = this.storePositions();
        var k = 0;
        var b = 0;
        var e;
        var h;
        for (var g = 0; g < this.data.nodes.length; g++) {
            if (!this.data.nodes[g].hide && !this.data.nodes[g].hiddenParent) {
                var d = this.data.nodes[g].id;
                var a = j[d];
                if (e) {
                    e.x = Math.min(e.x, a.x);
                    e.y = Math.min(e.y, a.y);
                    e.z = Math.min(e.z, a.z);
                    h.x = Math.max(h.x, a.x);
                    h.y = Math.max(h.y, a.y);
                    h.z = Math.max(h.z, a.z)
                } else {
                    e = {};
                    h = {};
                    e.x = a.x;
                    e.y = a.y;
                    e.z = a.z;
                    h.x = a.x;
                    h.y = a.y;
                    h.z = a.z
                }
                var l = Math.abs(this.vectorMagnitude(this.vectorDifference(this.previousNodePositions[d], a)));
                k += l;
                b++
            }
        }
        var o = k / b;
        var f = Math.abs(this.vectorMagnitude(this.vectorDifference(e, h)));
        var m = Math.abs(this.vectorMagnitude({
            x: this.x,
            y: this.y,
            z: parseInt((this.x + this.y) / 2)
        }));
        this.minimumTemperature = (f / m);
        this.layoutDone = o < (this.minimumTemperature / 2);
        this.previousNodePositions = j;
        if (this.initialProgress == null && this.temperature <= this.minimumTemperature) {
            this.initialProgress = o - (this.minimumTemperature / 2)
        }
        if (this.initialProgress != null) {
            this.layoutProgress = Math.max(this.layoutProgress, 1 - ((o - (this.minimumTemperature / 2)) / this.initialProgress))
        }
    };
    this.applyDisplacement = function (e) {
        if (this.nodes) {
            for (var f in e) {
                var d = this.nodes[f];
                var b = e[f];
                var a = this.vectorMagnitude(b);
                if (a != 0) {
                    d.x += (b.x / a) * Math.min(this.temperature, Math.abs(b.x));
                    d.y += (b.y / a) * Math.min(this.temperature, Math.abs(b.y));
                    d.z += (b.z / a) * Math.min(this.temperature, Math.abs(b.z))
                }
            }
        }
    };
    this.setInitialForceDirectedLayout = function () {
        this.layoutProgress = 0;
        for (var a = 0; a < this.data.nodes.length; a++) {
            if (!this.data.nodes[a].hide && !this.data.nodes[a].hiddenParent) {
                this.data.nodes[a].x = Math.floor(Math.random() * this.x);
                this.data.nodes[a].y = Math.floor(Math.random() * this.y);
                this.data.nodes[a].z = this.is3DNetwork ? Math.floor(Math.random() * parseInt((this.x + this.y) / 2)) : 0
            }
        }
        this.previousNodePositions = this.storePositions()
    };
    this.updateForceDirectedLayout = function (d) {
        var e = new Date().getTime();
        var b = e;
        if (this.data.edges.length < 1) {
            this.layoutDone = true
        }
        if (!this.layoutDone) {
            while (b - e < d) {
                var a = {};
                this.calculateRepulsiveDisplacement(a);
                this.calculateAttractiveDisplacement(a);
                this.applyDisplacement(a);
                this.iteration++;
                this.temperature = Math.max(this.temperature - (this.initialTemperature / 100), this.minimumTemperature);
                if (this.iteration % 10 == 0) {
                    this.isLayoutDone()
                }
                b = new Date().getTime()
            }
        }
        return this.layoutDone
    };
    this.renderNetwork = function () {
        this.ctx.shadowOffsetX = this.shadowOffsetX;
        this.ctx.shadowOffsetY = this.shadowOffsetY;
        this.ctx.shadowBlur = this.shadowBlur;
        this.ctx.shadowColor = this.shadowColor;
        this.setNetworkRotation();
        this.resizeCanvas();
        if (!this.layoutDone && !this.showAnimation) {
            this.drawNetworkProgressBar(true);
            return
        }
        if (this.data.nodes.length > 0) {
            if (this.layoutDone) {
                this.setXYNodePrecision()
            }
            this.setNetworkMinMaxRangeXY();
            this.setNetworkScaleFactor();
            if (this.scaleFactor) {
                if (this.showAnimation) {
                    if (this.preScaleNetwork) {
                        this.centerNetwork();
                        this.ctx.save();
                        this.ctx.scale(1 / this.scaleFactor, 1 / this.scaleFactor);
                        this.ctx.translate(this.offsetX, this.offsetY);
                        this.ctx.restore();
                        this.drawNetworkNodes();
                        this.drawNetworkEdges();
                        if (this.layoutDone) {
                            this.drawNetworkLegend()
                        }
                        this.ctx.save();
                        this.ctx.translate(-this.offsetX, -this.offsetY);
                        this.ctx.scale(this.scaleFactor, this.scaleFactor);
                        this.ctx.restore()
                    } else {
                        this.ctx.scale(this.scaleFactor, this.scaleFactor);
                        this.ctx.translate(this.offsetX, this.offsetY);
                        if (this.networkLayoutType == "radial" && this.showNetworkRadialLayout) {
                            this.renderRadialLayout()
                        }
                        this.drawNetworkNodes();
                        this.drawNetworkEdges();
                        if (this.layoutDone) {
                            this.drawNetworkDecorations();
                            this.drawNetworkLegend()
                        }
                        this.ctx.translate(-this.offsetX, -this.offsetY);
                        this.ctx.scale(1 / this.scaleFactor, 1 / this.scaleFactor)
                    }
                } else {
                    if (this.preScaleNetwork) {
                        this.centerNetwork();
                        this.ctx.save();
                        this.ctx.scale(this.scaleFactor, this.scaleFactor);
                        this.ctx.translate(this.offsetX, this.offsetY);
                        this.ctx.restore()
                    } else {
                        this.ctx.scale(this.scaleFactor, this.scaleFactor);
                        this.ctx.translate(this.offsetX, this.offsetY)
                    }
                    if (this.networkLayoutType == "radial" && this.showNetworkRadialLayout) {
                        for (var a = 1; a < this.networkDepth; a++) {
                            this.drawShape("circle", this.networkXCenter, this.networkYCenter, a * this.networkLevelSize * 2, a * this.networkLevelSize * 2, this.background, this.infoAreaColor, "open")
                        }
                    }
                    this.drawNetworkNodes();
                    this.drawNetworkEdges();
                    if (this.layoutDone) {
                        this.drawNetworkDecorations();
                        this.drawNetworkLegend()
                    }
                }
                this.drawNetworkProgressBar()
            }
        }
    };
    this.reRootRadialNetwork = function (f) {
        var e = this.data.nodes[f].id;
        this.networkCurrentAngle = this.getAngle(this.finalRadialNodePositions[e].x, this.finalRadialNodePositions[e].y, this.finalRadialNodePositions[this.networkRoot].x, this.finalRadialNodePositions[this.networkRoot].y, 0) + 180;
        this.orderRadialNetwork = {};
        var b = this.data.nodeIndices[e];
        var d = 0;
        for (var a = b; a < this.data.nodes.length; a++) {
            this.orderRadialNetwork[this.data.nodes[a].id] = d;
            d++
        }
        for (var a = 0; a < b; a++) {
            this.orderRadialNetwork[this.data.nodes[a].id] = d;
            d++
        }
        this.networkXCenter = this.finalRadialNodePositions[e].x;
        this.networkYCenter = this.finalRadialNodePositions[e].y;
        this.networkRoot = e;
        this.layoutDone = false;
        this.draw()
    };
    this.renderRadialLayout = function () {
        var a = this.data.nodes[this.data.nodeIndices[this.networkRoot]];
        for (var b = 1; b < this.networkDepth; b++) {
            this.drawShape("circle", a.x, a.y, b * this.networkLevelSize * 2, b * this.networkLevelSize * 2, this.background, this.infoAreaColor, "open")
        }
    };
    this.centerNetwork = function () {
        for (var a = 0; a < this.data.nodes.length; a++) {
            var b = this.data.nodes[a];
            if (!b.hide && !b.hiddenParent) {
                b.x = (this.offsetX + b.x) * this.scaleFactor;
                b.y = (this.offsetY + b.y) * this.scaleFactor
            }
        }
        this.scaleFactor = 0.9;
        this.offsetX = this.x * 0.05;
        this.offsetY = this.y * 0.05
    };
    this.adjustNetworkObjects = function (b) {
        if (b) {
            if (b[0] == "circle") {
                b[1] = (this.offsetX + b[1]) * this.scaleFactor;
                b[2] = (this.offsetY + b[2]) * this.scaleFactor;
                b[3] = b[3] * this.scaleFactor
            } else {
                for (var a = 1; a < b.length; a++) {
                    if (a % 2) {
                        b[a] = (this.offsetX + b[a]) * this.scaleFactor
                    } else {
                        b[a] = (this.offsetY + b[a]) * this.scaleFactor
                    }
                }
            }
        } else {
            return false
        }
        return b
    };
    this.setXYNodePrecision = function () {
        if (this.approximateNodePositions) {
            for (var a = 0; a < this.data.nodes.length; a++) {
                var b = this.data.nodes[a];
                if (b.x) {
                    b.x = parseInt(b.x);
                    this.nodes[b.id].x = b.x
                }
                if (b.y) {
                    b.y = parseInt(b.y);
                    this.nodes[b.id].y = b.y
                }
                if (b.z) {
                    b.z = parseInt(b.z);
                    this.nodes[b.id].z = b.z
                }
            }
        }
    };
    this.getLengthToNodeBoundary = function (d, l, b, k, f) {
        if (f) {
            var i = f.size ? f.size : 1;
            var a = f.width ? f.width : this.nodeSize * i;
            var h = f.height ? f.height : this.nodeSize * i;
            var e = Math.atan2(k - l, b - d);
            var g = Math.cos(e) * (a / 1.8);
            var j = Math.sin(e) * (h / 1.8);
            return this.lineLength(d, l, d + g, l + j) + (this.nodeSize / 2)
        } else {
            return this.nodeSize
        }
    };
    this.drawNetworkEdges = function () {
        if (this.nodes) {
            var a = {};
            var e = this.foreground;
            for (var o = 0; o < this.data.edges.length; o++) {
                var f = this.data.edges[o];
                var n = this.nodes[f.id1];
                var m = this.nodes[f.id2];
                var s = false;
                if (!n.hide && !n.hiddenParent && !m.hide && !m.hiddenParent && !f.anchor) {
                    s = true
                } else {
                    if (this.showHiddenChildEdges) {
                        if (n.hide || n.hiddenParent || (n.parentNode && this.nodes[n.parentNode].hideChildren)) {
                            n = this.findVisibleParentNode(f.id1);
                            if (n) {
                                s = true
                            }
                        }
                        if (m.hide || m.hiddenParent || (m.parentNode && this.nodes[m.parentNode].hideChildren)) {
                            m = this.findVisibleParentNode(f.id2);
                            if (m) {
                                s = true
                            }
                        }
                    }
                }
                var u = n.id + ":" + m.id;
                if (s) {
                    if (!a.hasOwnProperty(u)) {
                        var b = this.selectNode.hasOwnProperty(n.id) && this.selectNode.hasOwnProperty(m.id) ? this.nodeHighlightColor : f.color ? f.color : e;
                        var h = f.width ? f.width : 1;
                        var g = f.cap ? f.cap : false;
                        var l, j, q, p;
                        if (this.is3DNetwork) {
                            l = n.x3d;
                            j = n.y3d;
                            q = m.x3d;
                            p = m.y3d
                        } else {
                            l = n.x;
                            j = n.y;
                            q = m.x;
                            p = m.y
                        }
                        var k = f.type ? f.type : "line";
                        var t = this.getLengthToNodeBoundary(l, j, q, p, n);
                        var d = this.getLengthToNodeBoundary(q, p, l, j, m);
                        var r = this.drawLine(k, l, j, q, p, b, h, g, t, d);
                        if (this.layoutDone && !this.isAnimation) {
                            if (this.preScaleNetwork) {
                                this.addArea(r, [o + this.data.nodes.length])
                            } else {
                                this.addArea(this.adjustNetworkObjects(r), [o + this.data.nodes.length])
                            }
                        }
                        this.ctx.lineWidth = 1;
                        a[u] = 1
                    }
                }
            }
        }
    };
    this.drawNetworkNodes = function () {
        var l = this.data.nodes.length < this.showNodeNameThreshold ? true : false;
        var h = this.foreground;
        var z = this.shapes[0];
        var j = this.rangeX > this.rangeY ? "bottom" : "right";
        for (var A = 0; A < this.data.nodes.length; A++) {
            var w = this.data.nodes[A];
            if (!w.hide && !w.hiddenParent && !w.anchor) {
                var p = this.is3DNetwork ? w.x3d : w.x;
                var D = this.is3DNetwork ? w.y3d : w.y;
                var t = w.shape ? w.shape : z;
                var g = this.selectNode.hasOwnProperty(w.id) ? this.nodeHighlightColor : w.color ? w.color : h;
                var B = w.outline ? w.outline : this.foreground;
                var C = w.size ? w.size : 1;
                var o = w.width ? w.width : this.nodeSize * C;
                var v = w.height ? w.height : this.nodeSize * C;
                var q = w.pattern ? w.pattern : "closed";
                var m = w.rotate ? w.rotate * Math.PI / 180 : false;
                var x = w.oulineWidth ? w.oulineWidth : false;
                var f = w.imagePath ? w.imagePath : false;
                var a = w.zIndex ? w.zIndex : false;
                var E = this.drawShape(t, p, D, o, v, g, B, q, m, x, false, false, false, false, f, a);
                if (this.layoutDone && !this.isAnimation) {
                    if (this.preScaleNetwork) {
                        this.addArea(E, [A])
                    } else {
                        this.addArea(this.adjustNetworkObjects(E), [A])
                    }
                    if (l && !w.hideName) {
                        var d, b;
                        var k = w.label ? w.label : w.name ? w.name : w.id;
                        var y = this.getNodeColor(w.id);
                        var e = w.labelSize ? this.nodeFontSize * w.labelSize * this.scaleNodeFontFactor : this.nodeFontSize * this.scaleNodeFontFactor;
                        var r = (parseInt(e)) + "Pt " + this.fontName;
                        var u = this.measureText(k, r);
                        var s = e;
                        if (w.labelX != null && w.labelY != null) {
                            d = w.labelX - (u / 2);
                            b = w.labelY - (s / 2);
                            this.drawText(k, w.labelX, w.labelY, r, y, "center", "middle")
                        } else {
                            if (w.labelPosition) {
                                switch (w.labelPosition) {
                                case "top":
                                    d = p - (u / 2);
                                    b = D - ((v / 1.3) + s);
                                    this.drawText(k, p, b, r, y, "center", "top");
                                    break;
                                case "bottom":
                                    d = p - (u / 2);
                                    b = D + (v / 1.3);
                                    this.drawText(k, p, b, r, y, "center", "top");
                                    break;
                                case "right":
                                    d = p + (o / 1.3);
                                    b = D - (s / 2);
                                    this.drawText(k, d, D, r, y, "left", "middle");
                                    break;
                                case "left":
                                    d = p - ((o / 1.3) + u);
                                    b = D - (s / 2);
                                    this.drawText(k, d, D, r, y, "left", "middle");
                                    break;
                                case "center":
                                    this.drawText(k, p, D, r, y, "center", "middle");
                                    break
                                }
                            } else {
                                if (j == "right") {
                                    d = p + (o / 1.3);
                                    b = D - (s / 2);
                                    this.drawText(k, d, D, r, y, "left", "middle")
                                } else {
                                    d = p - (u / 2);
                                    b = D + (v / 1.3);
                                    this.drawText(k, p, b, r, y, "center", "top")
                                }
                            }
                        }
                        var n = ["rect", d, b, d + u, b + s];
                        if (this.preScaleNetwork && !this.isAnimation) {
                            this.addArea(n, [A], "-lab")
                        } else {
                            this.addArea(this.adjustNetworkObjects(n), [A], "-lab")
                        }
                    }
                }
            }
        }
    };
    this.drawNetworkDecorations = function () {
        if (this.showDecorations && this.decorations.length > 0) {
            var m = this.decorations.length * this.decorationsWidth;
            var t = m / 2;
            var e = this.decorationsHeight / 2;
            var u = this.decorationsHeight * 2 / 3;
            var g = this.decorationsHeight / 3;
            this.setHeatmapColors(this.decorationsRange[1] - this.decorationsRange[0]);
            for (var p = 0; p < this.data.nodes.length; p++) {
                var n = this.data.nodes[p];
                if (!n.hide && !n.hiddenParent && !n.anchor) {
                    var h = this.is3DNetwork ? n.x3d : n.x;
                    var r = this.is3DNetwork ? n.y3d : n.y;
                    var v = n.size ? this.nodeSize * n.size : this.nodeSize;
                    var a = v / 2;
                    var s, d, q, b, l, f, k;
                    switch (this.decorationsPosition) {
                    case "top":
                        s = h - t;
                        d = r - (a + this.decorationsHeight + this.decorationsWidth);
                        break;
                    case "bottom":
                        s = h - t;
                        d = r + (a + this.decorationsHeight + this.decorationsWidth);
                        break;
                    case "right":
                        s = h + a + this.decorationsWidth;
                        d = r - e;
                        break;
                    case "left":
                        s = h - (a + (this.decorationsWidth * (this.decorations.length + 1)));
                        d = r - e;
                        break
                    }
                    if (n.decorationsX != null && n.decorationsY != null) {
                        s = n.decorationsX;
                        d = n.decorationsY
                    }
                    if (this.preScaleNetwork && !this.isAnimation) {
                        this.addArea(["rect", s, d, s + m, d - this.decorationsHeight], [p], "-dec")
                    } else {
                        this.addArea(this.adjustNetworkObjects(["rect", s, d, s + m, d - this.decorationsHeight]), [p], "-dec")
                    }
                    if (this.decorationsType == "bar") {
                        k = this.decorationsRange[1] < 0 ? d : this.decorationsRange[0] >= 0 ? d + this.decorationsHeight : (d + this.decorationsHeight) - Math.abs(this.decorationsRange[0] * this.decorationsUnits);
                        for (var o = 0; o < this.decorations.length; o++) {
                            l = this.decorations[o];
                            if (n[l] && !isNaN(n[l])) {
                                this.rectangle(s, k, this.decorationsWidth, -n[l] * this.decorationsUnits, this.decorationsColors[o])
                            }
                            s += this.decorationsWidth
                        }
                    } else {
                        for (var o = 0; o < this.decorations.length; o++) {
                            l = this.decorations[o];
                            if (n[l] != null && !isNaN(n[l])) {
                                f = this.getHeatmapColor(this.decorationsRange[0], this.decorationsRange[1], n[l])
                            } else {
                                f = "rgb(255,255, 0)"
                            }
                            this.rectangle(s, d, this.decorationsWidth, u, f);
                            this.drawShape("circle", s + this.decorationsWidth / 2, (d + this.decorationsHeight) - (g / 3), g / 3, g / 3, this.decorationsColors[o], this.foreground, "closed");
                            s += this.decorationsWidth
                        }
                    }
                }
            }
        }
    };
    this.getLegendId = function (b, d) {
        if (b) {
            if (!d) {
                var a = 0;
                d = b + "LegendId" + a;
                while (this.data.legend.ids[b].hasOwnProperty(d)) {
                    a++;
                    d = b + "LegendId" + a
                }
            }
            this.data.legend.ids[b][d] = true;
            return d
        }
    };
    this.findNetworkXYRange = function () {
        if (this.data.nodes.length > 0) {
            var a = Number.MAX_VALUE;
            var f = Number.MIN_VALUE;
            var g = Number.MAX_VALUE;
            var e = Number.MIN_VALUE;
            if (this.is3DNetwork && this.layoutDone && this.scaleFactor) {
                for (var b = 0; b < this.data.nodes.length; b++) {
                    var d = this.data.nodes[b];
                    if (!d.hide && !d.hiddenParent && !d.anchor) {
                        a = Math.min(a, d.x3d);
                        f = Math.max(f, d.x3d);
                        g = Math.min(g, d.y3d);
                        e = Math.max(e, d.y3d)
                    }
                }
            } else {
                for (var b = 0; b < this.data.nodes.length; b++) {
                    var d = this.data.nodes[b];
                    if (!d.hide && !d.hiddenParent && !d.anchor) {
                        a = Math.min(a, d.x);
                        f = Math.max(f, d.x);
                        g = Math.min(g, d.y);
                        e = Math.max(e, d.y)
                    }
                }
            }
        }
        return ({
            minX: a,
            maxX: f,
            minY: g,
            maxY: e
        })
    };
    this.inititalizeNetworkLegend = function () {
        if (!this.data.legend) {
            this.data.legend = {}
        }
        if (!this.data.legend.pos) {
            this.data.legend.pos = {
                nodes: {
                    x: false,
                    y: false
                },
                edges: {
                    x: false,
                    y: false
                },
                decorations: {
                    x: false,
                    y: false
                }
            }
        }
        if (!this.data.legend.ids) {
            this.data.legend.ids = {
                nodes: {},
                edges: {},
                text: {},
                decorations: {}
            }
        }
    };
    this.drawNetworkLegend = function () {
        this.inititalizeNetworkLegend();
        var a = this.findNetworkXYRange();
        this.curLegendX = a.minX;
        this.curLegendY = a.minY;
        this.drawNetworkLegendNodes();
        this.drawNetworkLegendEdges();
        this.drawNetworkLegendText();
        this.drawNetworkLegendDecorations()
    };
    this.drawNetworkLegendNodes = function () {
        if (this.data.legend.nodes) {
            var g = this.data.legend.pos.nodes.x || this.curLegendX;
            var e = this.data.legend.pos.nodes.y || this.curLegendY;
            this.data.legend.pos.nodes.x = parseInt(g);
            this.data.legend.pos.nodes.y = parseInt(e);
            var v = 0;
            var u = 0;
            for (var w = 0; w < this.data.legend.nodes.length; w++) {
                this.data.legend.nodes[w].id = this.getLegendId("nodes", this.data.legend.nodes[w].id);
                var t = this.data.legend.nodes[w].color || this.nodeFontColor;
                var x = this.data.legend.nodes[w].outline ? this.data.legend.nodes[w].outline : this.foreground;
                var a = this.data.legend.nodes[w].shape || this.shapes[0];
                var p = this.data.legend.nodes[w].size ? this.data.legend.nodes[w].size * this.nodeFontSize : this.nodeFontSize;
                var l = this.data.legend.nodes[w].width ? this.data.legend.nodes[w].width : p;
                var r = this.data.legend.nodes[w].height ? this.data.legend.nodes[w].height : p;
                var m = this.data.legend.nodes[w].pattern ? this.data.legend.nodes[w].pattern : "closed";
                var k = this.data.legend.nodes[w].rotate ? this.data.legend.nodes[w].rotate * Math.PI / 180 : false;
                var s = this.data.legend.nodes[w].outlineWidth ? this.data.legend.nodes[w].outlineWidth : false;
                var h = this.data.legend.nodes[w].imagePath ? this.data.legend.nodes[w].imagePath : false;
                var b = this.data.legend.nodes[w].zIndex ? this.data.legend.nodes[w].zIndex : false;
                var j = this.data.legend.nodes[w].font ? this.data.legend.nodes[w].font * this.nodeFontSize * this.scaleNodeFontFactor : this.nodeFontSize * this.scaleNodeFontFactor;
                var o = (parseInt(j)) + "Pt " + this.fontName;
                var n = this.data.legend.nodes[w].text ? this.data.legend.nodes[w].text : "";
                var q = this.measureText(n, o);
                p = parseFloat(sprintf("%.1f", p));
                v = Math.max(v, (this.margin * 3) + p + q);
                u = p / 2;
                e += u;
                this.drawShape(a, this.margin + g + u, this.margin + e + u, l, r, t, x, m, k, s, false, false, false, false, h, b);
                this.drawText(n, (this.margin * 2) + g + p, this.margin + e + u, o, this.nodeFontColor, "left", "middle");
                e += p
            }
            e += this.margin + u;
            var z = this.data.legend.pos.nodes.x;
            var f = this.data.legend.pos.nodes.y;
            var y = v;
            var d = e - this.data.legend.pos.nodes.y;
            if (this.legendBox) {
                this.rectangle(z, f, y, d, false, this.nodeFontColor, "open")
            }
            var A = ["rect", z, f, z + y, f + d];
            if (this.preScaleNetwork) {
                this.addArea(A, [-1], "-legend-Nodes")
            } else {
                this.addArea(this.adjustNetworkObjects(A), [-1], "-legend-Nodes")
            }
            this.curLegendX = g;
            this.curLegendY = e
        }
    };
    this.drawNetworkLegendEdges = function () {
        if (this.data.legend.edges) {
            var e = this.data.legend.pos.edges.x || this.curLegendX;
            var b = this.data.legend.pos.edges.y || this.curLegendY;
            this.data.legend.pos.edges.x = parseInt(e);
            this.data.legend.pos.edges.y = parseInt(b);
            var o = 0;
            var n = 0;
            for (var p = 0; p < this.data.legend.edges.length; p++) {
                this.data.legend.edges[p].id = this.getLegendId("edges", this.data.legend.edges[p].id);
                var m = this.data.legend.edges[p].color || this.nodeFontColor;
                var g = this.data.legend.edges[p].type || "line";
                var l = this.data.legend.edges[p].width || 1;
                var k = this.data.legend.edges[p].size ? this.data.legend.edges[p].size * this.nodeFontSize : this.nodeFontSize;
                var f = this.data.legend.edges[p].font ? this.data.legend.edges[p].font * this.nodeFontSize * this.scaleNodeFontFactor : this.nodeFontSize * this.scaleNodeFontFactor;
                var j = (parseInt(f)) + "Pt " + this.fontName;
                var h = this.data.legend.edges[p].text || "";
                var q = this.measureText(h, j);
                k = parseFloat(sprintf("%.1f", k));
                o = Math.max(o, (this.margin * 3) + k + q);
                n = f / 2;
                b += n;
                this.ctx.lineWidth = l * this.edgeWidth;
                this.drawLine(g, this.margin + e, this.margin + b + n, e + k, this.margin + b + n, m, false, false, 0, 0);
                this.drawText(h, (this.margin * 2) + e + k, this.margin + b + n, j, this.nodeFontColor, "left", "middle");
                b += f
            }
            b += this.margin + n;
            var s = this.data.legend.pos.edges.x;
            var d = this.data.legend.pos.edges.y;
            var r = o;
            var a = b - this.data.legend.pos.edges.y;
            if (this.legendBox) {
                this.rectangle(s, d, r, a, false, this.nodeFontColor, "open")
            }
            var t = ["rect", s, d, s + r, d + a];
            if (this.preScaleNetwork) {
                this.addArea(t, [-1], "-legend-Edges")
            } else {
                this.addArea(this.adjustNetworkObjects(t), [-1], "-legend-Edges")
            }
            this.curLegendX = e;
            this.curLegendY = b
        }
    };
    this.drawNetworkLegendText = function () {
        if (this.data.legend.text) {
            var s = 0;
            for (var m = 0; m < this.data.legend.text.length; m++) {
                this.data.legend.text[m].id = this.getLegendId("text", this.data.legend.text[m].id);
                var k = this.data.legend.text[m].x || this.curLegendX;
                var g = this.data.legend.text[m].y || this.curLegendY;
                this.data.legend.text[m].x = parseInt(k);
                this.data.legend.text[m].y = parseInt(g);
                var e = this.data.legend.text[m].boxed || this.legendBox;
                var h = this.data.legend.text[m].margin || this.margin;
                var n = this.setNetworkLegendMargins(h);
                var j = this.data.legend.text[m].color || this.nodeFontColor;
                var r = this.data.legend.text[m].text || "";
                var p = this.data.legend.text[m].font ? this.data.legend.text[m].font * this.nodeFontSize * this.scaleNodeFontFactor : this.nodeFontSize * this.scaleNodeFontFactor;
                p = parseInt(p);
                var f = (p) + "Pt " + this.fontName;
                var d = this.measureText(r, f);
                s = p / 2;
                this.drawText(r, n[3] + k, n[0] + g + s, f, j, "left", "middle");
                var b = this.data.legend.text[m].x;
                var q = this.data.legend.text[m].y;
                var a = n[1] + n[3] + d;
                var o = n[0] + n[2] + p;
                if (e) {
                    this.rectangle(b, q, a, o, false, this.nodeFontColor, "open")
                }
                g += n[0] + n[2] + p;
                var l = ["rect", b, q, b + a, q + o];
                if (this.preScaleNetwork) {
                    this.addArea(l, [-1], "-legend-Text" + m)
                } else {
                    this.addArea(this.adjustNetworkObjects(l), [-1], "-legend-Text" + m)
                }
                this.curLegendX = k;
                this.curLegendY = g
            }
        }
    };
    this.drawNetworkLegendDecorations = function () {
        if (this.data.legend.decorations) {
            var f = this.data.legend.pos.decorations.x || this.curLegendX;
            var d = this.data.legend.pos.decorations.y || this.curLegendY;
            this.data.legend.pos.decorations.x = parseInt(f);
            this.data.legend.pos.decorations.y = parseInt(d);
            var t = 0;
            var s = 0;
            for (var u = 0; u < this.data.legend.decorations.length; u++) {
                this.data.legend.decorations[u].id = this.getLegendId("decorations", this.data.legend.decorations[u].id);
                var r = this.data.legend.decorations[u].color || this.nodeFontColor;
                var v = this.data.legend.decorations[u].outline ? this.data.legend.decorations[u].outline : this.foreground;
                var a = this.data.legend.decorations[u].shape || this.shapes[0];
                var n = this.data.legend.decorations[u].size ? this.data.legend.decorations[u].size * this.nodeFontSize : this.nodeFontSize;
                var j = this.data.legend.decorations[u].width ? this.data.legend.decorations[u].width : n;
                var p = this.data.legend.decorations[u].height ? this.data.legend.decorations[u].height : n;
                var k = this.data.legend.decorations[u].pattern ? this.data.legend.decorations[u].pattern : "closed";
                var h = this.data.legend.decorations[u].rotate ? this.data.legend.decorations[u].rotate * Math.PI / 180 : false;
                var q = this.data.legend.decorations[u].outlineWidth ? this.data.legend.decorations[u].otlineWidth : false;
                var g = this.data.legend.decorations[u].font ? this.data.legend.decorations[u].font * this.nodeFontSize * this.scaleNodeFontFactor : this.nodeFontSize * this.scaleNodeFontFactor;
                var m = (parseInt(g)) + "Pt " + this.fontName;
                var l = this.data.legend.decorations[u].text ? this.data.legend.decorations[u].text : "";
                var o = this.measureText(l, m);
                n = parseFloat(sprintf("%.1f", n));
                t = Math.max(t, (this.margin * 3) + n + o);
                s = n / 2;
                d += s;
                this.drawShape(a, this.margin + f + s, this.margin + d + s, j, p, r, v, k, h, q);
                this.drawText(l, (this.margin * 2) + f + n, this.margin + d + s, m, this.nodeFontColor, "left", "middle");
                d += n
            }
            d += this.margin + s;
            var x = this.data.legend.pos.decorations.x;
            var e = this.data.legend.pos.decorations.y;
            var w = t;
            var b = d - this.data.legend.pos.decorations.y;
            if (this.legendBox) {
                this.rectangle(x, e, w, b, false, this.nodeFontColor, "open")
            }
            var y = ["rect", x, e, x + w, e + b];
            if (this.preScaleNetwork) {
                this.addArea(y, [-1], "-legend-Decorations")
            } else {
                this.addArea(this.adjustNetworkObjects(y), [-1], "-legend-Decorations")
            }
            this.curLegendX = f;
            this.curLegendY = d
        }
    };
    this.setNetworkLegendMargins = function (a) {
        if (isNaN(a)) {
            var b = a.split(/[,\s]+/);
            if (b.length > 3) {
                return [parseInt(b[0]), parseInt(b[1]), parseInt(b[2]), parseInt(b[3])]
            } else {
                if (b.length == 3) {
                    return [parseInt(b[0]), parseInt(b[1]), parseInt(b[2]), parseInt(b[1])]
                } else {
                    if (b.length == 2) {
                        return [parseInt(b[0]), parseInt(b[1]), parseInt(b[0]), parseInt(b[1])]
                    } else {
                        return [parseInt(b[0]), parseInt(b[0]), parseInt(b[0]), parseInt(b[0])]
                    }
                }
            }
        } else {
            return [parseInt(a), parseInt(a), parseInt(a), parseInt(a)]
        }
    };
    this.drawNetworkProgressBar = function (b) {
        if (!this.layoutDone) {
            this.ctx.save();
            this.ctx.fillStyle = "#fff";
            this.ctx.strokeStyle = "#fff";
            var a = (new Date().getTime() - this.preUpdateTime) / (this.layoutTime * 1000);
            a = Math.max(a, this.layoutProgress);
            if (b) {
                this.ctx.strokeRect((this.width / 2) - 50, (this.height / 2) - 5, 100, 10);
                this.ctx.fillRect((this.width / 2) - 50, (this.height / 2) - 5, a * 100, 10);
                this.drawText("Calculating ...", (this.width / 2) - 50, (this.height / 2) + 10, this.showAnimationFont, this.showAnimationFontColor, "left", "top")
            } else {
                this.ctx.strokeRect(this.width - 110, this.height - 20, 100, 10);
                this.ctx.fillRect(this.width - 110, this.height - 20, a * 100, 10)
            }
            this.ctx.restore()
        }
    };
    this.setNetworkMinMaxRangeXY = function () {
        this.minX = Number.MAX_VALUE;
        this.minY = Number.MAX_VALUE;
        this.maxX = 0;
        this.maxY = 0;
        if (this.is3DNetwork && this.layoutDone) {
            for (var b = 0; b < this.data.nodes.length; b++) {
                var e = this.data.nodes[b];
                var a = e.width ? e.width : e.size ? (this.nodeSize * e.size) : this.nodeSize;
                var d = e.height ? e.height : e.size ? (this.nodeSize * e.size) : this.nodeSize;
                if (!e.hide) {
                    this.minX = Math.min(this.minX, e.x3d - a / 2);
                    this.minY = Math.min(this.minY, e.y3d - d / 2);
                    this.maxX = Math.max(this.maxX, e.x3d + a / 2);
                    this.maxY = Math.max(this.maxY, e.y3d + d / 2)
                }
            }
        } else {
            for (var b = 0; b < this.data.nodes.length; b++) {
                var e = this.data.nodes[b];
                var a = e.width ? e.width : e.size ? (this.nodeSize * e.size) : this.nodeSize;
                var d = e.height ? e.height : e.size ? (this.nodeSize * e.size) : this.nodeSize;
                if (!e.hide) {
                    this.minX = Math.min(this.minX, e.x - a / 2);
                    this.minY = Math.min(this.minY, e.y - d / 2);
                    this.maxX = Math.max(this.maxX, e.x + a / 2);
                    this.maxY = Math.max(this.maxY, e.y + d / 2)
                }
            }
        }
        this.minX -= (this.nodeSize * 3);
        this.minY -= (this.nodeSize * 3);
        this.maxX += (this.nodeSize * 3);
        this.maxY += (this.nodeSize * 3);
        this.rangeX = this.maxX - this.minX;
        this.rangeY = this.maxY - this.minY
    };
    this.setNetworkRotation = function () {
        if (this.is3DNetwork && this.layoutDone) {
            var f = [];
            var b = [];
            var l = [];
            this.set3DRotation();
            for (var d = 0; d < this.data.nodes.length; d++) {
                b.push(d);
                var a = this.data.nodes[d];
                var e = this.get3DTransfrom(a.x, a.y, a.z);
                a.x3d = e[0];
                a.y3d = e[1];
                a.z3d = e[2];
                f.push(Math.sqrt((a.x3d * a.x3d) + (a.y3d * a.y3d) + (a.z3d * a.z3d)))
            }
            b.sort(function (m, i) {
                return f[i] - f[m]
            });
            for (var k = 0; k < this.data.nodes.length; k++) {
                var d = b[k];
                var a = this.data.nodes[d];
                l.push(a)
            }
            this.data.nodes = l;
            this.setNodeIndices()
        } else {
            if (this.network2DRotate != 0) {
                if (this.network2DRotate >= Math.abs(360)) {
                    this.network2DRotate = 0
                }
                var g = Math.PI * this.network2DRotate / 360;
                for (var d = 0; d < this.data.nodes.length; d++) {
                    var a = this.data.nodes[d];
                    if (a.x != null && a.y != null) {
                        var j = a.x;
                        var h = a.y;
                        a.x = (j * Math.cos(g)) - (h * Math.sin(g));
                        a.y = (h * Math.cos(g)) + (j * Math.sin(g))
                    }
                }
                if (this.data.legend.pos.nodes.x && this.data.legend.pos.nodes.y) {
                    var j = this.data.legend.pos.nodes.x;
                    var h = this.data.legend.pos.nodes.y;
                    this.data.legend.pos.nodes.x = (j * Math.cos(g)) - (h * Math.sin(g));
                    this.data.legend.pos.nodes.y = (h * Math.cos(g)) + (j * Math.sin(g))
                }
                if (this.data.legend.pos.edges.x && this.data.legend.pos.edges.y) {
                    var j = this.data.legend.pos.edges.x;
                    var h = this.data.legend.pos.edges.y;
                    this.data.legend.pos.edges.x = (j * Math.cos(g)) - (h * Math.sin(g));
                    this.data.legend.pos.edges.y = (h * Math.cos(g)) + (j * Math.sin(g))
                }
                if (this.data.legend.text) {
                    for (var d = 0; d < this.data.legend.text.length; d++) {
                        var j = this.data.legend.text[d].x;
                        var h = this.data.legend.text[d].y;
                        this.data.legend.text[d].x = (j * Math.cos(g)) - (h * Math.sin(g));
                        this.data.legend.text[d].y = (h * Math.cos(g)) + (j * Math.sin(g))
                    }
                }
                this.network2DRotate = 0
            }
        }
    };
    this.setNetworkScaleFactor = function () {
        if (!this.networkFreeze) {
            this.scaleFactor = this.networkLayoutType == "radial" ? 0.3 : Math.min(this.x / (this.maxX - this.minX), this.y / (this.maxY - this.minY));
            this.scaleFactor *= this.zoom;
            this.widthBounds = this.x / this.scaleFactor;
            this.heightBounds = this.y / this.scaleFactor;
            this.offsetX = ((this.widthBounds / 2) - ((this.minX + this.maxX) / 2)) + this.panningX;
            this.offsetY = ((this.heightBounds / 2) - ((this.minY + this.maxY) / 2)) + this.panningY
        }
    };
    this.optimizeNetworkLayout = function () {
        if (this.data.nodes.length > 0) {
            this.isAnimation = true;
            this.setInitialForceDirectedLayout();
            this.preUpdateTime = new Date().getTime();
            var a = this.layoutTime * 1000;
            var b = this;
            var d = function () {
                    this.update = function () {
                        if (b.updateForceDirectedLayout(1000 / 30) || (a && new Date().getTime() - b.preUpdateTime > a)) {
                            b.layoutDone = true;
                            clearInterval(e);
                            b.isAnimation = false
                        }
                        b.renderNetwork()
                    };
                    var e = setInterval(this.update, 1000 / 30)
                };
            d.call()
        }
    };
    this.getAngle = function (f, h, d, g, e) {
        var b = (Math.atan2(g - h, d - f) * 180 / Math.PI) % 360;
        if (!e) {
            e = 0
        }
        while (b < e) {
            b += 360
        }
        while (b >= (e + 360)) {
            b -= 360
        }
        return b
    };
    this.getPolar = function (d, b) {
        return {
            x: this.networkXCenter + d * Math.cos(b * Math.PI / 180),
            y: this.networkYCenter + d * Math.sin(b * Math.PI / 180)
        }
    };
    this.setNodePositions = function (a) {
        for (var d in a) {
            var b = this.data.nodes[this.data.nodeIndices[d]];
            b.x = a[d].x;
            b.y = a[d].y
        }
    };
    this.animateRadialNodePositions = function () {
        this.isAnimation = true;
        var f = this;
        var e = 50;
        var d = 0;
        var b = 0;
        for (var h in f.finalRadialNodePositions) {
            if (this.initialRadialNodePositions[h].r == 0) {
                this.initialRadialNodePositions[h].a = this.finalRadialNodePositions[h].a
            }
            if (this.finalRadialNodePositions[h].r == 0) {
                this.finalRadialNodePositions[h].a = this.initialRadialNodePositions[h].a
            }
            var a = (this.finalRadialNodePositions[h].a - this.initialRadialNodePositions[h].a);
            if (a < 0) {
                a += 360
            }
            if (a > 360) {
                a -= 360
            }
            if (a > 180) {
                a -= 360
            }
            this.finalRadialNodePositions[h].a = (this.initialRadialNodePositions[h].a + a)
        }
        var g = function () {
                this.update = function () {
                    var k = {};
                    var s = Math.atan((d / e) * 10 - 5) * 0.5 / Math.atan(5) + 0.5;
                    var p = 1 - s;
                    for (var m in f.finalRadialNodePositions) {
                        var j = ((p * f.initialRadialNodePositions[m].a) + (s * f.finalRadialNodePositions[m].a));
                        var l = ((p * f.initialRadialNodePositions[m].r) + (s * f.finalRadialNodePositions[m].r));
                        var q = f.getPolar(l, j);
                        k[m] = {
                            x: q.x,
                            y: q.y
                        }
                    }
                    if (d >= e) {
                        clearInterval(i);
                        f.isAnimation = false;
                        k = f.finalRadialNodePositions
                    }
                    d++;
                    f.setNodePositions(k);
                    f.renderNetwork()
                };
                var i = setInterval(this.update, 50)
            };
        g.call()
    };
    this.setFinalRadialNodePositions = function (e, d) {
        if (!this.finalRadialNodePositions) {
            this.finalRadialNodePositions = {}
        }
        if (!this.nodeParentHood.drawn.hasOwnProperty(e)) {
            var m, o, g, n, b;
            var f = this.data.nodes[this.data.nodeIndices[e]];
            if (!f.hide && !f.hiddenParent) {
                if (this.nodeParentHood[e].children.length > 1 && !d) {
                    var k = [];
                    var j = this;
                    if (this.orderRadialNetwork) {
                        this.nodeParentHood[e].children.sort(function (l, i) {
                            return j.orderRadialNetwork[l] - j.orderRadialNetwork[i]
                        })
                    }
                    for (var h = 0; h < this.nodeParentHood[e].children.length; h++) {
                        if (this.nodeParentHood[this.nodeParentHood[e].children[h]].level > this.nodeParentHood[e].level) {
                            this.setFinalRadialNodePositions(this.nodeParentHood[e].children[h]);
                            k.push(this.nodeParentHood[this.nodeParentHood[e].children[h]].angle)
                        }
                    }
                    if (k.length > 0) {
                        if (k.length == 1) {
                            n = k[0]
                        } else {
                            if (k[0] > k[k.length - 1]) {
                                n = ((k[0] + k[k.length - 1]) % 360) / 2
                            } else {
                                n = (k[0] + k[k.length - 1]) / 2
                            }
                        }
                    } else {
                        this.setFinalRadialNodePositions(e, true)
                    }
                } else {
                    n = this.networkCurrentAngle;
                    this.networkCurrentAngle = (this.networkCurrentAngle + this.networkAngleStep)
                }
                g = (this.networkDepth - 1) - this.nodeParentHood[e].level;
                m = this.getPolar(this.networkRadialRadius, n);
                o = this.shortenLine(this.networkXCenter, this.networkYCenter, m.x, m.y, 0, this.networkLevelSize * g, "line");
                b = this.lineLength(this.networkXCenter, this.networkYCenter, o[2], o[3]);
                if (this.nodeParentHood[e].level == 0) {
                    n = 0;
                    b = 0
                }
                this.finalRadialNodePositions[f.id] = {
                    x: o[2],
                    y: o[3],
                    r: b,
                    a: n % 360
                };
                this.nodeParentHood.drawn[e] = true;
                this.nodeParentHood[e].angle = n
            }
        }
    };
    this.setInitialRadialNodePositions = function () {
        if (!this.initialRadialNodePositions) {
            this.initialRadialNodePositions = {};
            for (var a = 0; a < this.data.nodes.length; a++) {
                if (!this.data.nodes[a].hide && !this.data.nodes[a].hiddenParent) {
                    var b = this.data.nodes[a];
                    this.initialRadialNodePositions[b.id] = {
                        x: this.networkXCenter,
                        y: this.networkYCenter,
                        r: 0,
                        a: 0
                    }
                }
            }
        } else {
            this.initialRadialNodePositions = this.finalRadialNodePositions;
            this.finalRadialNodePositions = false
        }
    };
    this.setRadialNetwrorkLayout = function () {
        if (this.data.nodes.length > 0) {
            this.networkLevelSize = this.x * 4 / (this.networkDepth * 2);
            this.networkAngleStep = 360 / this.networkDivisions;
            this.networkCurrentAngle = this.networkCurrentAngle ? this.networkCurrentAngle : 0;
            this.networkRadialRadius = this.networkLevelSize * (this.networkDepth - 1);
            this.setInitialRadialNodePositions();
            this.setFinalRadialNodePositions(this.networkRoot);
            this.layoutDone = true;
            if (this.showAnimation) {
                this.animateRadialNodePositions()
            } else {
                this.setNodePositions(this.finalRadialNodePositions);
                this.renderNetwork()
            }
        }
    };
    this.createRandomNetwork = function (u) {
        var m = {};
        var a = {};
        if (u) {
            m = this.createMesh()
        } else {
            m.nodes = [];
            var e = Math.floor(Math.random() * this.randomNetworkNodes) + 1;
            for (var o = 0; o < e; o++) {
                var h = {};
                var s = Math.floor(Math.random() * this.shapes.length);
                var f = Math.floor(Math.random() * this.colors.length);
                h.id = "id" + o;
                h.shape = this.shapes[s];
                h.color = this.colors[f];
                h.size = 1;
                m.nodes.push(h)
            }
            m.edges = [];
            for (var o = 0; o < m.nodes.length; o++) {
                var g = m.nodes[o];
                var l = [];
                for (var k = 0; k < Math.floor(Math.random() * this.randomNetworkNodeEdgesMax) - 1; k++) {
                    l.push(Math.floor(Math.random() * e))
                }
                for (var k = 0; k < l.length; k++) {
                    var d = m.nodes[l[k]];
                    var r = o + ":" + l[k];
                    var q = l[k] + ":" + o;
                    if (g.id != d.id && !a.hasOwnProperty(r) && !a.hasOwnProperty(r)) {
                        var b = {};
                        var t = Math.floor(Math.random() * this.lines.length);
                        var f = Math.floor(Math.random() * this.colors.length);
                        b.id1 = g.id;
                        b.id2 = d.id;
                        b.width = 1;
                        b.type = this.lines[t];
                        b.color = this.colors[f];
                        m.edges.push(b);
                        a[r] = 1;
                        a[q] = 1
                    }
                }
            }
        }
        if (this.reduceRandomNetwork) {
            var p = this.normalizeGraph(m);
            m = this.reduceNetwork(p, function (j, i) {
                if (j.nodes && i.nodes) {
                    if (j.nodes.length >= i.nodes.length) {
                        return j
                    } else {
                        return i
                    }
                } else {
                    if (j.nodes) {
                        return j
                    } else {
                        if (i.nodes) {
                            return i
                        } else {
                            return null
                        }
                    }
                }
            })
        }
        if (m.nodes.length < 5) {
            return this.createRandomNetwork(true)
        } else {
            var l = [];
            for (var o = 0; o < m.edges.length; o++) {
                if (m.edges[o].id1 && m.edges[o].id2) {
                    l.push(m.edges[o])
                }
            }
            m.edges = l;
            return m
        }
    };
    this.createMesh = function () {
        var g = {};
        g.nodes = [];
        g.edges = [];
        var f = 7;
        var a = this;
        for (var i = 0; i < f * f; i++) {
            var d = {};
            var h = Math.floor(Math.random() * this.shapes.length);
            var b = Math.floor(Math.random() * this.colors.length);
            d.id = "id" + i;
            d.shape = this.shapes[h];
            d.color = this.colors[b];
            d.size = 1;
            g.nodes.push(d)
        }
        var e = function (n, m) {
                var l = {};
                var j = Math.floor(Math.random() * a.lines.length);
                var k = Math.floor(Math.random() * a.colors.length);
                l.id1 = g.nodes[n].id;
                l.id2 = g.nodes[m].id;
                l.width = 1;
                l.type = a.lines[j];
                l.color = a.colors[k];
                return l
            };
        for (var i = 0; i < g.nodes.length; i++) {
            if (Math.floor(Math.random() * f) != 0) {
                if (i + 1 < g.nodes.length && (i + 1) % f != 0) {
                    g.edges.push(e(i, i + 1))
                }
                if (i + f < g.nodes.length) {
                    g.edges.push(e(i, i + f))
                }
            }
        }
        return g
    };
    this.setNodeChildren = function (g, b, h) {
        this.networkDepth = b > this.networkDepth ? b : this.networkDepth;
        var e = this.data.nodes[this.data.nodeIndices[g]];
        if (!e.hide && !e.hiddenParent) {
            var a = this.edgesForNode(this.data.edges, g);
            if (!this.nodeParentHood.hasOwnProperty(g)) {
                if (a.length == 1) {
                    this.networkDivisions++
                }
                if (a.length > 0) {
                    for (var d = 0; d < a.length; d++) {
                        if (!this.nodeParentHood[g]) {
                            this.nodeParentHood[g] = {};
                            this.nodeParentHood[g].children = [];
                            this.nodeParentHood[g].level = b;
                            this.nodeParentHood[g].angle = 0;
                            this.nodeParentHood[g].parent = h
                        }
                        this.nodeParentHood[g].level = b < this.nodeParentHood[g].level ? b : this.nodeParentHood[g].level;
                        if (a[d].id1 == g) {
                            this.nodeParentHood[g].children.push(a[d].id2);
                            this.setNodeChildren(a[d].id2, b + 1, g)
                        } else {
                            if (a[d].id2 == g) {
                                this.nodeParentHood[g].children.push(a[d].id1);
                                this.setNodeChildren(a[d].id1, b + 1, g)
                            }
                        }
                    }
                }
            } else {
                this.nodeParentHood[g].level = b < this.nodeParentHood[g].level ? b : this.nodeParentHood[g].level;
                for (var d = 0; d < this.nodeParentHood[g].children.length; d++) {
                    var f = this.nodeParentHood[g].children[d];
                    if (this.nodeParentHood[f]) {
                        if ((b + 1) < this.nodeParentHood[f].level) {
                            this.nodeParentHood[f].level = b + 1;
                            this.nodeParentHood[f].parent = g
                        }
                    }
                }
            }
        }
    };
    this.setNodeParentHood = function () {
        this.nodeParentHood = {};
        if (this.networkLayoutType == "radial") {
            if (!this.networkRoot) {
                this.networkRoot = this.data.nodes[0].id
            }
            this.nodeParentHood.drawn = {};
            this.networkDepth = 0;
            this.networkDivisions = 0;
            this.setNodeChildren(this.networkRoot, 0, false)
        } else {
            for (var a = 0; a < this.data.nodes.length; a++) {
                if (!this.data.nodes[a].hide) {
                    if (this.data.nodes[a].parentNode || this.data.nodes[a].hideChildren) {
                        if (this.data.nodes[a].parentNode && !this.nodeParentHood.hasOwnProperty(this.data.nodes[a].parentNode)) {
                            this.nodeParentHood[this.data.nodes[a].parentNode] = {}
                        }
                        if (this.data.nodes[a].parentNode) {
                            if (!this.nodeParentHood[this.data.nodes[a].parentNode].children) {
                                this.nodeParentHood[this.data.nodes[a].parentNode].children = []
                            }
                            this.nodeParentHood[this.data.nodes[a].parentNode].children.push(this.data.nodes[a].id);
                            if (this.data.nodes[this.data.nodeIndices[this.data.nodes[a].parentNode]].hideChildren) {
                                this.data.nodes[a].hiddenParent = true
                            }
                        }
                        if (this.data.nodes[a].hideChildren) {
                            if (!this.nodeParentHood.hasOwnProperty(this.data.nodes[a].id)) {
                                this.nodeParentHood[this.data.nodes[a].id] = {}
                            }
                            this.nodeParentHood[this.data.nodes[a].id].hideChildren = true
                        }
                    }
                }
            }
            for (var b in this.nodeParentHood) {
                if (this.data.nodes[this.data.nodeIndices[b]].hideChildren || this.isParentHidden(b)) {
                    this.hideUnhideChildrenNodes(b, true)
                } else {
                    if (this.nodeParentHood[b].children) {
                        this.hideUnhideChildrenNodes(b, false)
                    }
                }
            }
        }
    };
    this.isParentHidden = function (d) {
        var b = this.findParentNodes(d);
        if (b) {
            for (var a = 0; a < b.length; a++) {
                if (this.data.nodes[this.data.nodeIndices[b[a]]].hideChildren) {
                    return true
                }
            }
        }
        return false
    };
    this.findParentNodes = function (d, a) {
        if (this.data.nodes[this.data.nodeIndices[d]].parentNode) {
            var b = this.data.nodes[this.data.nodeIndices[d]].parentNode;
            if (!a) {
                a = []
            }
            a.push(b);
            a = this.findParentNodes(b, a);
            return a
        } else {
            return a ? a.reverse() : false
        }
    };
    this.findChildrenNodeIndices = function (d, b) {
        if (!b) {
            b = []
        }
        if (this.nodeParentHood[d] && this.nodeParentHood[d]["children"]) {
            for (var a = 0; a < this.nodeParentHood[d]["children"].length; a++) {
                b.push(this.data.nodeIndices[this.nodeParentHood[d]["children"][a]]);
                b = this.findChildrenNodeIndices(this.nodeParentHood[d]["children"][a], b)
            }
        }
        return b.length > 0 ? b : false
    };
    this.hasChildren = function (a) {
        return this.nodeParentHood[a] && this.nodeParentHood[a].children
    };
    this.findVisibleParentNode = function (d) {
        var b = this.findParentNodes(d);
        if (b) {
            b.reverse();
            for (var a = 0; a < b.length; a++) {
                if (!this.nodes[b[a]].hide && !this.nodes[b[a]].hiddenParent) {
                    return this.nodes[b[a]]
                }
            }
        }
        return false
    };
    this.collateNetworks = function () {
        if (this.data.nodes.length > 0) {
            var b = this.normalizeGraph({
                nodes: this.data.nodes,
                edges: this.data.edges
            });
            if (b.length > 1) {
                var f = {
                    id: "canvasXpressAnchorNode",
                    anchor: true
                };
                this.addNode(f);
                for (var d = 0; d < b.length; d++) {
                    var a = b[d].nodes[0];
                    var e = {
                        id1: "canvasXpressAnchorNode",
                        id2: a.id,
                        anchor: true
                    };
                    this.addEdge(e)
                }
            }
        }
    };
    this.setObjectAttributes = function () {
        if (this.colorNodeBy) {
            this.setAllObjectAttributes("nodes", "color", this.colorNodeBy)
        }
        if (this.shapeNodeBy) {
            this.setAllObjectAttributes("nodes", "shape", this.shapeNodeBy)
        }
        if (this.sizeNodeBy) {
            this.setAllObjectAttributes("nodes", "size", this.sizeNodeBy)
        }
        if (this.colorEgdeBy) {
            this.setAllObjectAttributes("egdes", "color", this.colorEgdeBy)
        }
        if (this.shapeEgdeBy) {
            this.setAllObjectAttributes("egdes", "shape", this.shapeEgdeBy)
        }
        if (this.sizeEgdeBy) {
            this.setAllObjectAttributes("egdes", "size", this.sizeEgdeBy)
        }
    };
    this.setDecorationsRangeUnits = function () {
        this.decorationsRange = [];
        var d = {};
        if (this.showDecorations && this.decorations.length > 0) {
            var h = [];
            for (var f = 0; f < this.data.nodes.length; f++) {
                var g = this.data.nodes[f];
                for (var e = 0; e < this.decorations.length; e++) {
                    if (g[this.decorations[e]] && !isNaN(g[this.decorations[e]])) {
                        h.push(g[this.decorations[e]])
                    }
                }
            }
            this.decorationsRange = this.range(h);
            var a = this.decorationsRange[1] - this.decorationsRange[0];
            var b = this.decorationsRange[0] - (a * 0.05);
            if (b > 0) {
                this.decorationsRange[0] = b;
                a = this.decorationsRange[1] - this.decorationsRange[0]
            }
            this.decorationsUnits = this.decorationsHeight / a;
            if (!this.data.legend) {
                this.data.legend = {}
            }
            this.data.legend.decorations = [];
            for (var f = 0; f < this.decorations.length; f++) {
                if (this.decorationsColors[f]) {
                    d[this.decorationsColors[f]] = true;
                    this.data.legend.decorations.push({
                        shape: "square",
                        color: this.decorationsColors[f],
                        text: this.decorations[f],
                        size: 0.5,
                        font: 0.5
                    })
                } else {
                    for (var e = 0; e < this.colors.length; e++) {
                        if (!d.hasOwnProperty(this.colors[e])) {
                            this.decorationsColors[f] = this.colors[e];
                            d[this.colors[e]] = true;
                            this.data.legend.decorations.push({
                                shape: "square",
                                color: this.decorationsColors[f],
                                text: this.decorations[f],
                                size: 0.5,
                                font: 0.5
                            });
                            break
                        }
                    }
                }
            }
        }
    };
    this.drawNetworkPlot = function () {
        this.showAnimationFont = (this.showAnimationFontSize) + "Pt " + this.fontName;
        this.setNodeParentHood();
        this.setObjectAttributes();
        this.setDecorationsRangeUnits();
        if (this.calculateLayout) {
            this.collateNetworks();
            this.setNetworkPrivateParams();
            if (this.networkLayoutType == "radial") {
                this.setRadialNetwrorkLayout()
            } else {
                this.optimizeNetworkLayout()
            }
        } else {
            this.layoutDone = true;
            this.renderNetwork()
        }
    };
    this.initializeNetwork = function () {
        this.setNetworkDimensions();
        if (!this.nodes && this.data.nodes) {
            this.setNodes()
        }
        if (this.randomNetwork) {
            var a = this.createRandomNetwork();
            this.data.nodes = a.nodes;
            this.data.edges = a.edges;
            this.setAllNodesVisible();
            this.setNodes();
            this.layoutDone = false
        }
        this.drawNetworkPlot()
    };
    this.initializeNetwork()
};
CanvasXpress.prototype.Genome = function () {
    this.setGenomeSubtracks = function () {
        this.subtracks = [];
        this.y = this.margin * 2;
        var f = 0;
        for (var l = 0; l < this.data.tracks.length; l++) {
            if (this.data.tracks[l].name) {
                this.y += this.getFontPt(this.trackNameFont) + (this.margin * 2)
            }
            var d = this.data.tracks[l];
            var p = d.data.length > this.showFeatureNameThereshold || d.hide ? false : true;
            var n = d.type ? d.type : "line";
            var b = d.width ? d.width : this.featureWidthDefault;
            var q = d.height ? d.height : this.featureHeightDefault;
            this.subtracks[l] = {
                features: [],
                cur: []
            };
            this.subtracks[l].cur[0] = -3;
            this.subtracks[l].features[0] = [];
            if (p) {
                this.y += this.getFontPt(this.featureNameFont) + this.margin
            }
            this.y += q + this.margin;
            for (var h = 0; h < d.data.length; h++) {
                var e = 0;
                var a, g;
                var r = d.data[h];
                var m = this.measureText(r.id, this.featureNameFont);
                var k = r.data;
                r.counter = f++;
                if (n == "box") {
                    a = k[0][0] * this.xAxisUnit;
                    g = k[k.length - 1][1] * this.xAxisUnit
                } else {
                    if (n.match(/bar|heatmap/)) {
                        a = k[0] * this.xAxisUnit;
                        g = a + b
                    } else {
                        if (n == "sequence") {
                            a = k[0] * this.xAxisUnit;
                            g = a + (k[1].length * this.xAxisUnit)
                        } else {
                            a = k * this.xAxisUnit;
                            g = a + 1
                        }
                    }
                }
                var o = p ? Math.max(a + m, g) : g;
                while (a < (this.subtracks[l].cur[e] + 2)) {
                    e++;
                    if (!this.subtracks[l].cur[e]) {
                        if (p) {
                            this.y += this.getFontPt(this.featureNameFont) + this.margin
                        }
                        this.y += q + this.margin;
                        this.subtracks[l].cur[e] = -3;
                        this.subtracks[l].features[e] = []
                    }
                }
                this.subtracks[l].features[e].push(h);
                this.subtracks[l].cur[e] = o
            }
            this.y += this.margin * 2
        }
    };
    this.getGenomeRange = function (a) {
        var f = [];
        for (var d = 0; d < a.length; d++) {
            var e = a[d].data;
            for (var b = 1; b < e.length; b++) {
                f.push(e[b])
            }
        }
        return this.range(f)
    };
    this.plotGenomeData = function () {
        var y, Y, x, w;
        var E = this.getFontPt(this.trackNameFont);
        var K = this.getFontPt(this.featureNameFont);
        var b = this.top + (this.margin * 2);
        for (var T = 0; T < this.data.tracks.length; T++) {
            var z = this.left;
            if (this.data.tracks[T].name) {
                this.drawText(this.data.tracks[T].name, z, b + (E / 2), this.trackNameFont, this.trackNameFontColor, "left", "middle");
                b += E + (this.margin * 2)
            }
            var D = this.cloneObject(this.data.tracks[T]);
            var o = D.data.length > this.showFeatureNameThereshold || D.hide ? false : true;
            var X = D.type ? D.type : "line";
            var g = D.height ? D.height : this.featureHeightDefault;
            var A = D.fills ? D.fills : D.fill ? D.fill : this.foreground;
            var F = D.lines ? D.lines : D.line ? D.line : this.foreground;
            var r = X.match(/bar|heatmap/) ? this.getGenomeRange(D.data) : false;
            for (var S = 0; S < this.subtracks[T].features.length; S++) {
                var u = this.subtracks[T].features[S];
                for (var R = 0; R < u.length; R++) {
                    var W = u[R];
                    var J = D.data[W];
                    var V = J.data;
                    var v = J.id;
                    if (X == "box") {
                        if (V.length > 0) {
                            z = this.left + ((V[0][0] - this.xAxisMin) * this.xAxisUnit);
                            if (o) {
                                this.drawText(v, z, b + (K / 2), this.featureNameFont, this.featureNameFontColor, "left", "middle");
                                b += K + this.margin
                            }
                            this.addArea(["rect", z, b, ((V[V.length - 1][1] - this.xAxisMin) * this.xAxisUnit), b + g], [J.counter]);
                            var C = Math.floor(g * this.xAxisUnit);
                            if (J.dir == "right") {
                                y = (V[V.length - 1][1] - this.xAxisMin) * this.xAxisUnit;
                                if (y - C < (V[V.length - 1][0] - this.xAxisMin) * this.xAxisUnit) {
                                    V.pop()
                                } else {
                                    V[V.length - 1][1] -= g
                                }
                                z = this.left + ((V[V.length - 1][0] - this.xAxisMin) * this.xAxisUnit);
                                Y = b + (g / 2);
                                this.drawLine("line", z, Y, y, Y, A, false, "butt");
                                z = y - ((g / 2) * this.xAxisUnit);
                                this.drawLine("line", z, b, y, Y, A, false, "butt");
                                this.drawLine("line", z, b + g, y, Y, A, false, "butt")
                            } else {
                                if (J.dir == "left") {
                                    if (z + C > (V[0][1] - this.xAxisMin) * this.xAxisUnit) {
                                        V.shift()
                                    } else {
                                        V[0][0] += g
                                    }
                                    y = this.left + ((V[0][0] - this.xAxisMin) * this.xAxisUnit);
                                    Y = b + (g / 2);
                                    this.drawLine("line", z, Y, y, Y, A, false, "butt");
                                    y = z + ((g / 2) * this.xAxisUnit);
                                    this.drawLine("line", z, Y, y, b, A, false, "butt");
                                    this.drawLine("line", z, Y, y, b + g, A, false, "butt")
                                }
                            }
                            for (var Q = 0; Q < V.length; Q++) {
                                z = this.left + ((V[Q][0] - this.xAxisMin) * this.xAxisUnit);
                                y = ((V[Q][1] - this.xAxisMin) * this.xAxisUnit) - z;
                                if (D.connect) {
                                    if (Q < V.length - 1) {
                                        Y = b + (g / 2);
                                        x = this.left + ((V[Q + 1][0] - this.xAxisMin) * this.xAxisUnit);
                                        w = (((V[Q][1] + ((V[Q + 1][0] - V[Q][1]) / 2)) - this.xAxisMin) * this.xAxisUnit);
                                        this.drawLine("line", z + y, Y, w, b, A, false, "butt");
                                        this.drawLine("line", w, b, x, Y, A, false, "butt")
                                    }
                                }
                                this.rectangle(z, b, y, g, A, F)
                            }
                        }
                    } else {
                        if (X == "bar") {
                            var a = D.autowidth ? this.xAxisUnit / (V.length - 1) : D.width ? D.width : this.featureWidthDefault;
                            z = this.left + ((V[0] - this.xAxisMin) * this.xAxisUnit);
                            if (o) {
                                this.drawText(v, z, b + (K / 2), this.featureNameFont, this.featureNameFontColor, "left", "middle");
                                b += K + this.margin
                            }
                            this.addArea(["rect", z, b, z + (V.length * a), b + g], [J.counter]);
                            y = z;
                            var N = r[1] - r[0];
                            var e = r[0] - (N * 0.05);
                            if (e > 0) {
                                r[0] = e;
                                N = r[1] - r[0]
                            }
                            var B = g / N;
                            var d = r[1] < 0 ? b : r[0] >= 0 ? b + g : (b + g) - Math.abs(r[0] * B);
                            for (var M = 1; M < V.length; M++) {
                                var H = typeof A === "object" && A[M - 1] ? A[M - 1] : A[0];
                                var t = typeof F === "object" && F[M - 1] ? F[M - 1] : F[0];
                                var U = V[M] * B;
                                this.rectangle(y, d, a, -U, H, t);
                                if (D.autowidth) {
                                    y += a
                                } else {
                                    y += a + 2
                                }
                            }
                        } else {
                            if (X == "heatmap") {
                                var a = D.autowidth ? this.xAxisUnit : D.width ? D.width : this.featureWidthDefault;
                                z = this.left + ((V[0] - this.xAxisMin) * this.xAxisUnit);
                                if (o) {
                                    this.drawText(v, z, b + (K / 2), this.featureNameFont, this.featureNameFontColor, "left", "middle");
                                    b += K + this.margin
                                }
                                this.addArea(["rect", z, b, z + a, b + g], [J.counter]);
                                var U = g / (V.length - 1);
                                var p = "rgb(255,255,0)";
                                Y = b;
                                for (var M = 1; M < V.length; M++) {
                                    var G = V[M];
                                    var q = this.getHeatmapColor(r[0], r[1], G);
                                    if (isNaN(G)) {
                                        this.rectangle(z, Y, a, U, p, p)
                                    } else {
                                        this.rectangle(z, Y, a, U, q, q)
                                    }
                                    Y += U
                                }
                            } else {
                                if (X == "sequence") {
                                    var P = D.subtype;
                                    z = this.left + ((V[0] - this.xAxisMin) * this.xAxisUnit);
                                    if (o) {
                                        this.drawText(v, z, b + (K / 2), this.featureNameFont, this.featureNameFontColor, "left", "middle");
                                        b += K + this.margin
                                    }
                                    var m = V[1].split(/ */);
                                    this.addArea(["rect", z, b, z + (m.length * this.xAxisUnit), b + K], [J.counter]);
                                    var O = 0;
                                    var M = P == "DNA" ? 1 : 3;
                                    var Q = this.measureText("A", this.sequenceFont) > M * this.xAxisUnit ? true : false;
                                    while (m.length > 0) {
                                        var f = m.shift();
                                        if (f == "[") {
                                            f = m.shift();
                                            var I = [];
                                            while (f != "]") {
                                                I.push(f);
                                                f = m.shift()
                                            }
                                            if (Q) {
                                                this.drawLine("line", z + (O * this.xAxisUnit), b, z + (O * this.xAxisUnit), b + K, this.sequenceMColor, false, "butt")
                                            } else {
                                                this.drawText(I.join("/"), z + (O * this.xAxisUnit), b + (K / 2), this.sequenceFont, this.sequenceMColor, "center", "middle")
                                            }
                                            O += M
                                        } else {
                                            var q = "sequence" + f.toUpperCase() + "Color";
                                            if (q == "sequenceTColor") {
                                                var L = true
                                            }
                                            if (Q) {
                                                this.drawLine("line", z + (O * this.xAxisUnit), b, z + (O * this.xAxisUnit), b + K, this[q], false, "butt")
                                            } else {
                                                this.drawText(f, z + (O * this.xAxisUnit), b + (K / 2), this.sequenceFont, this[q], "center", "middle")
                                            }
                                            O += M
                                        }
                                    }
                                } else {
                                    if (X == "triangle") {
                                        z = this.left + ((V - this.xAxisMin) * this.xAxisUnit);
                                        if (o) {
                                            this.drawText(v, z, b + (K / 2), this.featureNameFont, this.featureNameFontColor, "left", "middle");
                                            b += K + this.margin
                                        }
                                        this.addArea(this.drawShape("triangle", z, b + (g / 2), g, g, A, F, "closed"), [J.counter])
                                    } else {
                                        z = this.left + ((V - this.xAxisMin) * this.xAxisUnit);
                                        if (o) {
                                            this.drawText(v, z, b + (K / 2), this.featureNameFont, this.featureNameFontColor, "left", "middle");
                                            b += K + this.margin
                                        }
                                        this.addArea(this.drawLine("line", z, b, z, b + g, F, false, "butt"), [J.counter])
                                    }
                                }
                            }
                        }
                    }
                    if (o) {
                        b -= (K + this.margin)
                    }
                }
                if (o) {
                    b += K + this.margin
                }
                b += g + this.margin
            }
            b += (this.margin * 2)
        }
    };
    this.setGenomeWireFrame = function () {
        var d = this.getFontPt(this.featureNameFont) / 2;
        if (this.xAxisTickStyle == "dotted") {
            this.drawLine("dottedLine", this.left, this.top, this.left, this.top + this.y, this.wireColor, false, "butt")
        } else {
            this.drawLine("line", this.left, this.top, this.left, this.top + this.y, this.wireColor, false, "butt")
        }
        for (var b = 0; b < this.xAxisValues.length; b++) {
            var e = parseFloat(this.xAxisValues[b]);
            var a = this.left + ((e - this.xAxisMin) * this.xAxisUnit);
            if (this.xAxisTickStyle == "dotted") {
                this.drawLine("dottedLine", a, this.top, a, this.top + this.y, this.wireColor, false, "butt")
            } else {
                this.drawLine("line", a, this.top, a, this.top + this.y, this.wireColor, false, "butt")
            }
            if (!((b + (this.periodTicksLabels + 1)) % this.periodTicksLabels)) {
                this.drawText(parseInt(e), a, this.top + this.y + this.margin + d, this.featureNameFont, this.xAxisTickColor, "center", "middle")
            }
        }
        if (this.xAxisTickStyle == "dotted") {
            this.drawLine("dottedLine", this.left + this.x, this.top, this.left + this.x, this.top + this.y, this.wireColor, false, "butt")
        } else {
            this.drawLine("line", this.left + this.x, this.top, this.left + this.x, this.top + this.y, this.wireColor, false, "butt")
        }
    };
    this.setGenomeAxisUnits = function () {
        this.setAxisUnits("xAxis")
    };
    this.setGenomeFontsColors = function () {
        this.trackNameFont = (this.trackNameFontSize) + "Pt " + this.fontName;
        this.featureNameFont = (this.featureNameFontSize) + "Pt " + this.fontName;
        this.sequenceFont = (this.sequenceFontSize) + "Pt " + this.fontName;
        if (!this.wireColor.match(/,0.1\)$/)) {
            this.wireColor = this.wireColor.replace(/\)$/, ",0.1)")
        }
    };
    this.setGenomeDimentions = function () {
        this.bottom += this.margin + this.getFontPt(this.featureNameFont);
        if (this.width) {
            this.x = this.width - (this.left + this.right)
        } else {
            this.width = this.left + this.x + this.right
        }
        this.height = this.top + this.y + this.bottom;
        this.resizeCanvas()
    };
    this.drawGenome = function () {
        this.setGenomeFontsColors();
        this.setGenomeAxisUnits();
        this.setGenomeSubtracks();
        this.setGenomeDimentions();
        this.setGenomeWireFrame();
        this.plotGenomeData()
    };
    this.initializeGenome = function () {
        this.drawGenome()
    };
    this.initializeGenome()
};