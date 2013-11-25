// Copyright 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


// Known Issues:
//
// * Patterns are not implemented.
// * Radial gradient are not implemented. The VML version of these look very
//   different from the canvas one.
// * Clipping paths are not implemented.
// * Coordsize. The width and height attribute have higher priority than the
//   width and height style values which isn't correct.
// * Painting mode isn't implemented.
// * Canvas width/height should is using content-box by default. IE in
//   Quirks mode will draw the canvas using border-box. Either change your
//   doctype to HTML5
//   (http://www.whatwg.org/specs/web-apps/current-work/#the-doctype)
//   or use Box Sizing Behavior from WebFX
//   (http://webfx.eae.net/dhtml/boxsizing/boxsizing.html)
// * Non uniform scaling does not correctly scale strokes.
// * Optimize. There is always room for speed improvements.

// Only add this code if we do not already have a canvas implementation
if (!document.createElement('canvas').getContext) {

(function() {

  // alias some functions to make (compiled) code shorter
  var m = Math;
  var mr = m.round;
  var ms = m.sin;
  var mc = m.cos;
  var max = m.max;
  var abs = m.abs;
  var sqrt = m.sqrt;

  // this is used for sub pixel precision
  var Z = 10;
  var Z2 = Z / 2;

  /**
   * This funtion is assigned to the <canvas> elements as element.getContext().
   * @this {HTMLElement}
   * @return {CanvasRenderingContext2D_}
   */
  function getContext() {
    return this.context_ ||
        (this.context_ = new CanvasRenderingContext2D_(this));
  }

  var slice = Array.prototype.slice;

  /**
   * Binds a function to an object. The returned function will always use the
   * passed in {@code obj} as {@code this}.
   *
   * Example:
   *
   *   g = bind(f, obj, a, b)
   *   g(c, d) // will do f.call(obj, a, b, c, d)
   *
   * @param {Function} f The function to bind the object to
   * @param {Object} obj The object that should act as this when the function
   *     is called
   * @param {*} var_args Rest arguments that will be used as the initial
   *     arguments when the function is called
   * @return {Function} A new function that has bound this
   */
  function bind(f, obj, var_args) {
    var a = slice.call(arguments, 2);
    return function() {
      return f.apply(obj, a.concat(slice.call(arguments)));
    };
  }

  var G_vmlCanvasManager_ = {
    init: function(opt_doc) {
      if (/MSIE/.test(navigator.userAgent) && !window.opera) {
        var doc = opt_doc || document;
        // Create a dummy element so that IE will allow canvas elements to be
        // recognized.
        doc.createElement('canvas');
        doc.attachEvent('onreadystatechange', bind(this.init_, this, doc));
      }
    },

    init_: function(doc) {
      // create xmlns
      if (!doc.namespaces['g_vml_']) {
        doc.namespaces.add('g_vml_', 'urn:schemas-microsoft-com:vml');
      }

      // Setup default CSS.  Only add one style sheet per document
      if (!doc.styleSheets['ex_canvas_']) {
        var ss = doc.createStyleSheet();
        ss.owningElement.id = 'ex_canvas_';
        ss.cssText = 'canvas{display:inline-block;overflow:hidden;' +
            // default size is 300x150 in Gecko and Opera
            'text-align:left;width:300px;height:150px}' +
            'g_vml_\\:*{behavior:url(#default#VML)}';
      }

      // find all canvas elements
      var els = doc.getElementsByTagName('canvas');
      for (var i = 0; i < els.length; i++) {
        this.initElement(els[i]);
      }
    },

    /**
     * Public initializes a canvas element so that it can be used as canvas
     * element from now on. This is called automatically before the page is
     * loaded but if you are creating elements using createElement you need to
     * make sure this is called on the element.
     * @param {HTMLElement} el The canvas element to initialize.
     * @return {HTMLElement} the element that was created.
     */
    initElement: function(el) {
      if (!el.getContext) {

        el.getContext = getContext;

        // do not use inline function because that will leak memory
        el.attachEvent('onpropertychange', onPropertyChange);
        el.attachEvent('onresize', onResize);

        var attrs = el.attributes;
        if (attrs.width && attrs.width.specified) {
          // TODO: use runtimeStyle and coordsize
          // el.getContext().setWidth_(attrs.width.nodeValue);
          el.style.width = attrs.width.nodeValue + 'px';
        } else {
          el.width = el.clientWidth;
        }
        if (attrs.height && attrs.height.specified) {
          // TODO: use runtimeStyle and coordsize
          // el.getContext().setHeight_(attrs.height.nodeValue);
          el.style.height = attrs.height.nodeValue + 'px';
        } else {
          el.height = el.clientHeight;
        }
        //el.getContext().setCoordsize_()
      }
      return el;
    }
  };

  function onPropertyChange(e) {
    var el = e.srcElement;

    switch (e.propertyName) {
      case 'width':
        el.style.width = el.attributes.width.nodeValue + 'px';
        el.getContext().clearRect();
        break;
      case 'height':
        el.style.height = el.attributes.height.nodeValue + 'px';
        el.getContext().clearRect();
        break;
    }
  }

  function onResize(e) {
    var el = e.srcElement;
    if (el.firstChild) {
      el.firstChild.style.width =  el.clientWidth + 'px';
      el.firstChild.style.height = el.clientHeight + 'px';
    }
  }

  G_vmlCanvasManager_.init();

  // precompute "00" to "FF"
  var dec2hex = [];
  for (var i = 0; i < 16; i++) {
    for (var j = 0; j < 16; j++) {
      dec2hex[i * 16 + j] = i.toString(16) + j.toString(16);
    }
  }

  function createMatrixIdentity() {
    return [
      [1, 0, 0],
      [0, 1, 0],
      [0, 0, 1]
    ];
  }

  function matrixMultiply(m1, m2) {
    var result = createMatrixIdentity();

    for (var x = 0; x < 3; x++) {
      for (var y = 0; y < 3; y++) {
        var sum = 0;

        for (var z = 0; z < 3; z++) {
          sum += m1[x][z] * m2[z][y];
        }

        result[x][y] = sum;
      }
    }
    return result;
  }

  function copyState(o1, o2) {
    o2.fillStyle     = o1.fillStyle;
    o2.lineCap       = o1.lineCap;
    o2.lineJoin      = o1.lineJoin;
    o2.lineWidth     = o1.lineWidth;
    o2.miterLimit    = o1.miterLimit;
    o2.shadowBlur    = o1.shadowBlur;
    o2.shadowColor   = o1.shadowColor;
    o2.shadowOffsetX = o1.shadowOffsetX;
    o2.shadowOffsetY = o1.shadowOffsetY;
    o2.strokeStyle   = o1.strokeStyle;
    o2.globalAlpha   = o1.globalAlpha;
    o2.arcScaleX_    = o1.arcScaleX_;
    o2.arcScaleY_    = o1.arcScaleY_;
    o2.lineScale_    = o1.lineScale_;
  }

  function processStyle(styleString) {
    var str, alpha = 1;

    styleString = String(styleString);
    if (styleString.substring(0, 3) == 'rgb') {
      var start = styleString.indexOf('(', 3);
      var end = styleString.indexOf(')', start + 1);
      var guts = styleString.substring(start + 1, end).split(',');

      str = '#';
      for (var i = 0; i < 3; i++) {
        str += dec2hex[Number(guts[i])];
      }

      if (guts.length == 4 && styleString.substr(3, 1) == 'a') {
        alpha = guts[3];
      }
    } else {
      str = styleString;
    }

    return [str, alpha];
  }

  function processLineCap(lineCap) {
    switch (lineCap) {
      case 'butt':
        return 'flat';
      case 'round':
        return 'round';
      case 'square':
      default:
        return 'square';
    }
  }

  /**
   * This class implements CanvasRenderingContext2D interface as described by
   * the WHATWG.
   * @param {HTMLElement} surfaceElement The element that the 2D context should
   * be associated with
   */
  function CanvasRenderingContext2D_(surfaceElement) {
    this.m_ = createMatrixIdentity();

    this.mStack_ = [];
    this.aStack_ = [];
    this.currentPath_ = [];

    // Canvas context properties
    this.strokeStyle = '#000';
    this.fillStyle = '#000';

    this.lineWidth = 1;
    this.lineJoin = 'miter';
    this.lineCap = 'butt';
    this.miterLimit = Z * 1;
    this.globalAlpha = 1;
    this.canvas = surfaceElement;

    var el = surfaceElement.ownerDocument.createElement('div');
    el.style.width =  surfaceElement.clientWidth + 'px';
    el.style.height = surfaceElement.clientHeight + 'px';
    el.style.overflow = 'hidden';
    el.style.position = 'absolute';
    surfaceElement.appendChild(el);

    this.element_ = el;
    this.arcScaleX_ = 1;
    this.arcScaleY_ = 1;
    this.lineScale_ = 1;
  }

  var contextPrototype = CanvasRenderingContext2D_.prototype;
  contextPrototype.clearRect = function() {
    this.element_.innerHTML = '';
    this.currentPath_ = [];
  };

  contextPrototype.beginPath = function() {
    // TODO: Branch current matrix so that save/restore has no effect
    //       as per safari docs.
    this.currentPath_ = [];
  };

  contextPrototype.moveTo = function(aX, aY) {
    var p = this.getCoords_(aX, aY);
    this.currentPath_.push({type: 'moveTo', x: p.x, y: p.y});
    this.currentX_ = p.x;
    this.currentY_ = p.y;
  };

  contextPrototype.lineTo = function(aX, aY) {
    var p = this.getCoords_(aX, aY);
    this.currentPath_.push({type: 'lineTo', x: p.x, y: p.y});

    this.currentX_ = p.x;
    this.currentY_ = p.y;
  };

  contextPrototype.bezierCurveTo = function(aCP1x, aCP1y,
                                            aCP2x, aCP2y,
                                            aX, aY) {
    var p = this.getCoords_(aX, aY);
    var cp1 = this.getCoords_(aCP1x, aCP1y);
    var cp2 = this.getCoords_(aCP2x, aCP2y);
    bezierCurveTo(this, cp1, cp2, p);
  };

  // Helper function that takes the already fixed cordinates.
  function bezierCurveTo(self, cp1, cp2, p) {
    self.currentPath_.push({
      type: 'bezierCurveTo',
      cp1x: cp1.x,
      cp1y: cp1.y,
      cp2x: cp2.x,
      cp2y: cp2.y,
      x: p.x,
      y: p.y
    });
    self.currentX_ = p.x;
    self.currentY_ = p.y;
  }

  contextPrototype.quadraticCurveTo = function(aCPx, aCPy, aX, aY) {
    // the following is lifted almost directly from
    // http://developer.mozilla.org/en/docs/Canvas_tutorial:Drawing_shapes

    var cp = this.getCoords_(aCPx, aCPy);
    var p = this.getCoords_(aX, aY);

    var cp1 = {
      x: this.currentX_ + 2.0 / 3.0 * (cp.x - this.currentX_),
      y: this.currentY_ + 2.0 / 3.0 * (cp.y - this.currentY_)
    };
    var cp2 = {
      x: cp1.x + (p.x - this.currentX_) / 3.0,
      y: cp1.y + (p.y - this.currentY_) / 3.0
    };

    bezierCurveTo(this, cp1, cp2, p);
  };

  contextPrototype.arc = function(aX, aY, aRadius,
                                  aStartAngle, aEndAngle, aClockwise) {
    aRadius *= Z;
    var arcType = aClockwise ? 'at' : 'wa';

    var xStart = aX + mc(aStartAngle) * aRadius - Z2;
    var yStart = aY + ms(aStartAngle) * aRadius - Z2;

    var xEnd = aX + mc(aEndAngle) * aRadius - Z2;
    var yEnd = aY + ms(aEndAngle) * aRadius - Z2;

    // IE won't render arches drawn counter clockwise if xStart == xEnd.
    if (xStart == xEnd && !aClockwise) {
      xStart += 0.125; // Offset xStart by 1/80 of a pixel. Use something
                       // that can be represented in binary
    }

    var p = this.getCoords_(aX, aY);
    var pStart = this.getCoords_(xStart, yStart);
    var pEnd = this.getCoords_(xEnd, yEnd);

    this.currentPath_.push({type: arcType,
                           x: p.x,
                           y: p.y,
                           radius: aRadius,
                           xStart: pStart.x,
                           yStart: pStart.y,
                           xEnd: pEnd.x,
                           yEnd: pEnd.y});

  };

  contextPrototype.rect = function(aX, aY, aWidth, aHeight) {
    this.moveTo(aX, aY);
    this.lineTo(aX + aWidth, aY);
    this.lineTo(aX + aWidth, aY + aHeight);
    this.lineTo(aX, aY + aHeight);
    this.closePath();
  };

  contextPrototype.strokeRect = function(aX, aY, aWidth, aHeight) {
    // Will destroy any existing path (same as FF behaviour)
    this.beginPath();
    this.moveTo(aX, aY);
    this.lineTo(aX + aWidth, aY);
    this.lineTo(aX + aWidth, aY + aHeight);
    this.lineTo(aX, aY + aHeight);
    this.closePath();
    this.stroke();
    this.currentPath_ = [];
  };

  contextPrototype.fillRect = function(aX, aY, aWidth, aHeight) {
    // Will destroy any existing path (same as FF behaviour)
    this.beginPath();
    this.moveTo(aX, aY);
    this.lineTo(aX + aWidth, aY);
    this.lineTo(aX + aWidth, aY + aHeight);
    this.lineTo(aX, aY + aHeight);
    this.closePath();
    this.fill();
    this.currentPath_ = [];
  };

  contextPrototype.createLinearGradient = function(aX0, aY0, aX1, aY1) {
    return new CanvasGradient_('gradient');
  };

  contextPrototype.createRadialGradient = function(aX0, aY0,
                                                   aR0, aX1,
                                                   aY1, aR1) {
    var gradient = new CanvasGradient_('gradientradial');
    gradient.radius1_ = aR0;
    gradient.radius2_ = aR1;
    gradient.focus_.x = aX0;
    gradient.focus_.y = aY0;
    return gradient;
  };

  contextPrototype.drawImage = function(image, var_args) {
    var dx, dy, dw, dh, sx, sy, sw, sh;

    // to find the original width we overide the width and height
    var oldRuntimeWidth = image.runtimeStyle.width;
    var oldRuntimeHeight = image.runtimeStyle.height;
    image.runtimeStyle.width = 'auto';
    image.runtimeStyle.height = 'auto';

    // get the original size
    var w = image.width;
    var h = image.height;

    // and remove overides
    image.runtimeStyle.width = oldRuntimeWidth;
    image.runtimeStyle.height = oldRuntimeHeight;

    if (arguments.length == 3) {
      dx = arguments[1];
      dy = arguments[2];
      sx = sy = 0;
      sw = dw = w;
      sh = dh = h;
    } else if (arguments.length == 5) {
      dx = arguments[1];
      dy = arguments[2];
      dw = arguments[3];
      dh = arguments[4];
      sx = sy = 0;
      sw = w;
      sh = h;
    } else if (arguments.length == 9) {
      sx = arguments[1];
      sy = arguments[2];
      sw = arguments[3];
      sh = arguments[4];
      dx = arguments[5];
      dy = arguments[6];
      dw = arguments[7];
      dh = arguments[8];
    } else {
      throw Error('Invalid number of arguments');
    }

    var d = this.getCoords_(dx, dy);

    var w2 = sw / 2;
    var h2 = sh / 2;

    var vmlStr = [];

    var W = 10;
    var H = 10;

    // For some reason that I've now forgotten, using divs didn't work
    vmlStr.push(' <g_vml_:group',
                ' coordsize="', Z * W, ',', Z * H, '"',
                ' coordorigin="0,0"' ,
                ' style="width:', W, ';height:', H, ';position:absolute;');

    // If filters are necessary (rotation exists), create them
    // filters are bog-slow, so only create them if abbsolutely necessary
    // The following check doesn't account for skews (which don't exist
    // in the canvas spec (yet) anyway.

    if (this.m_[0][0] != 1 || this.m_[0][1]) {
      var filter = [];

      // Note the 12/21 reversal
      filter.push('M11=', this.m_[0][0], ',',
                  'M12=', this.m_[1][0], ',',
                  'M21=', this.m_[0][1], ',',
                  'M22=', this.m_[1][1], ',',
                  'Dx=', mr(d.x / Z), ',',
                  'Dy=', mr(d.y / Z), '');

      // Bounding box calculation (need to minimize displayed area so that
      // filters don't waste time on unused pixels.
      var max = d;
      var c2 = this.getCoords_(dx + dw, dy);
      var c3 = this.getCoords_(dx, dy + dh);
      var c4 = this.getCoords_(dx + dw, dy + dh);

      max.x = max(max.x, c2.x, c3.x, c4.x);
      max.y = max(max.y, c2.y, c3.y, c4.y);

      vmlStr.push('padding:0 ', mr(max.x / Z), 'px ', mr(max.y / Z),
                  'px 0;filter:progid:DXImageTransform.Microsoft.Matrix(',
                  filter.join(''), ", sizingmethod='clip');")
    } else {
      vmlStr.push('top:', mr(d.y / Z), 'px;left:', mr(d.x / Z), 'px;');
    }

    vmlStr.push(' ">' ,
                '<g_vml_:image src="', image.src, '"',
                ' style="width:', Z * dw, ';',
                ' height:', Z * dh, ';"',
                ' cropleft="', sx / w, '"',
                ' croptop="', sy / h, '"',
                ' cropright="', (w - sx - sw) / w, '"',
                ' cropbottom="', (h - sy - sh) / h, '"',
                ' />',
                '</g_vml_:group>');

    this.element_.insertAdjacentHTML('BeforeEnd',
                                    vmlStr.join(''));
  };

  contextPrototype.stroke = function(aFill) {
    var lineStr = [];
    var lineOpen = false;
    var a = processStyle(aFill ? this.fillStyle : this.strokeStyle);
    var color = a[0];
    var opacity = a[1] * this.globalAlpha;

    var W = 10;
    var H = 10;

    lineStr.push('<g_vml_:shape',
                 ' filled="', !!aFill, '"',
                 ' style="position:absolute;width:', W, ';height:', H, ';"',
                 ' coordorigin="0 0" coordsize="', Z * W, ' ', Z * H, '"',
                 ' stroked="', !aFill, '"',
                 ' path="');

    var newSeq = false;
    var min = {x: null, y: null};
    var max = {x: null, y: null};

    for (var i = 0; i < this.currentPath_.length; i++) {
      var p = this.currentPath_[i];
      var c;

      switch (p.type) {
        case 'moveTo':
          c = p;
          lineStr.push(' m ', mr(p.x), ',', mr(p.y));
          break;
        case 'lineTo':
          lineStr.push(' l ', mr(p.x), ',', mr(p.y));
          break;
        case 'close':
          lineStr.push(' x ');
          p = null;
          break;
        case 'bezierCurveTo':
          lineStr.push(' c ',
                       mr(p.cp1x), ',', mr(p.cp1y), ',',
                       mr(p.cp2x), ',', mr(p.cp2y), ',',
                       mr(p.x), ',', mr(p.y));
          break;
        case 'at':
        case 'wa':
          lineStr.push(' ', p.type, ' ',
                       mr(p.x - this.arcScaleX_ * p.radius), ',',
                       mr(p.y - this.arcScaleY_ * p.radius), ' ',
                       mr(p.x + this.arcScaleX_ * p.radius), ',',
                       mr(p.y + this.arcScaleY_ * p.radius), ' ',
                       mr(p.xStart), ',', mr(p.yStart), ' ',
                       mr(p.xEnd), ',', mr(p.yEnd));
          break;
      }


      // TODO: Following is broken for curves due to
      //       move to proper paths.

      // Figure out dimensions so we can do gradient fills
      // properly
      if (p) {
        if (min.x == null || p.x < min.x) {
          min.x = p.x;
        }
        if (max.x == null || p.x > max.x) {
          max.x = p.x;
        }
        if (min.y == null || p.y < min.y) {
          min.y = p.y;
        }
        if (max.y == null || p.y > max.y) {
          max.y = p.y;
        }
      }
    }
    lineStr.push(' ">');

    if (!aFill) {
      var lineWidth = this.lineScale_ * this.lineWidth;

      // VML cannot correctly render a line if the width is less than 1px.
      // In that case, we dilute the color to make the line look thinner.
      if (lineWidth < 1) {
        opacity *= lineWidth;
      }

      lineStr.push(
        '<g_vml_:stroke',
        ' opacity="', opacity, '"',
        ' joinstyle="', this.lineJoin, '"',
        ' miterlimit="', this.miterLimit, '"',
        ' endcap="', processLineCap(this.lineCap), '"',
        ' weight="', lineWidth, 'px"',
        ' color="', color, '" />'
      );
    } else if (typeof this.fillStyle == 'object') {
      var focus = {x: '50%', y: '50%'};
      var width = max.x - min.x;
      var height = max.y - min.y;
      var dimension = width > height ? width : height;

      focus.x = mr(this.fillStyle.focus_.x / width * 100 + 50) + '%';
      focus.y = mr(this.fillStyle.focus_.y / height * 100 + 50) + '%';

      var colors = [];

      // inside radius (%)
      if (this.fillStyle.type_ == 'gradientradial') {
        var inside = this.fillStyle.radius1_ / dimension * 100;

        // percentage that outside radius exceeds inside radius
        var expansion = this.fillStyle.radius2_ / dimension * 100 - inside;
      } else {
        var inside = 0;
        var expansion = 100;
      }

      var insidecolor = {offset: null, color: null};
      var outsidecolor = {offset: null, color: null};

      // We need to sort 'colors' by percentage, from 0 > 100 otherwise ie
      // won't interpret it correctly
      this.fillStyle.colors_.sort(function(cs1, cs2) {
        return cs1.offset - cs2.offset;
      });

      for (var i = 0; i < this.fillStyle.colors_.length; i++) {
        var fs = this.fillStyle.colors_[i];

        colors.push(fs.offset * expansion + inside, '% ', fs.color, ',');

        if (fs.offset > insidecolor.offset || insidecolor.offset == null) {
          insidecolor.offset = fs.offset;
          insidecolor.color = fs.color;
        }

        if (fs.offset < outsidecolor.offset || outsidecolor.offset == null) {
          outsidecolor.offset = fs.offset;
          outsidecolor.color = fs.color;
        }
      }
      colors.pop();

      lineStr.push('<g_vml_:fill',
                   ' color="', outsidecolor.color, '"',
                   ' color2="', insidecolor.color, '"',
                   ' type="', this.fillStyle.type_, '"',
                   ' focusposition="', focus.x, ', ', focus.y, '"',
                   ' colors="', colors.join(''), '"',
                   ' opacity="', opacity, '" />');
    } else {
      lineStr.push('<g_vml_:fill color="', color, '" opacity="', opacity,
                   '" />');
    }

    lineStr.push('</g_vml_:shape>');

    this.element_.insertAdjacentHTML('beforeEnd', lineStr.join(''));
  };

  contextPrototype.fill = function() {
    this.stroke(true);
  }

  contextPrototype.closePath = function() {
    this.currentPath_.push({type: 'close'});
  };

  /**
   * @private
   */
  contextPrototype.getCoords_ = function(aX, aY) {
    var m = this.m_;
    return {
      x: Z * (aX * m[0][0] + aY * m[1][0] + m[2][0]) - Z2,
      y: Z * (aX * m[0][1] + aY * m[1][1] + m[2][1]) - Z2
    }
  };

  contextPrototype.save = function() {
    var o = {};
    copyState(this, o);
    this.aStack_.push(o);
    this.mStack_.push(this.m_);
    this.m_ = matrixMultiply(createMatrixIdentity(), this.m_);
  };

  contextPrototype.restore = function() {
    copyState(this.aStack_.pop(), this);
    this.m_ = this.mStack_.pop();
  };

  contextPrototype.translate = function(aX, aY) {
    var m1 = [
      [1,  0,  0],
      [0,  1,  0],
      [aX, aY, 1]
    ];

    this.m_ = matrixMultiply(m1, this.m_);
  };

  contextPrototype.rotate = function(aRot) {
    var c = mc(aRot);
    var s = ms(aRot);

    var m1 = [
      [c,  s, 0],
      [-s, c, 0],
      [0,  0, 1]
    ];

    this.m_ = matrixMultiply(m1, this.m_);
  };

  contextPrototype.scale = function(aX, aY) {
    this.arcScaleX_ *= aX;
    this.arcScaleY_ *= aY;
    var m1 = [
      [aX, 0,  0],
      [0,  aY, 0],
      [0,  0,  1]
    ];

    var m = this.m_ = matrixMultiply(m1, this.m_);

    // Get the line scale.
    // Determinant of this.m_ means how much the area is enlarged by the
    // transformation. So its square root can be used as a scale factor
    // for width.
    var det = m[0][0] * m[1][1] - m[0][1] * m[1][0];
    this.lineScale_ = sqrt(abs(det));
  };

  /******** STUBS ********/
  contextPrototype.clip = function() {
    // TODO: Implement
  };

  contextPrototype.arcTo = function() {
    // TODO: Implement
  };

  contextPrototype.createPattern = function() {
    return new CanvasPattern_;
  };

  // Gradient / Pattern Stubs
  function CanvasGradient_(aType) {
    this.type_ = aType;
    this.radius1_ = 0;
    this.radius2_ = 0;
    this.colors_ = [];
    this.focus_ = {x: 0, y: 0};
  }

  CanvasGradient_.prototype.addColorStop = function(aOffset, aColor) {
    aColor = processStyle(aColor);
    this.colors_.push({offset: 1 - aOffset, color: aColor});
  };

  function CanvasPattern_() {}

  // set up externs
  G_vmlCanvasManager = G_vmlCanvasManager_;
  CanvasRenderingContext2D = CanvasRenderingContext2D_;
  CanvasGradient = CanvasGradient_;
  CanvasPattern = CanvasPattern_;

})();

} // if
/**
 * @overview
 * Griaffe HTML5 canvas graphics library
 * A set of Javascript objects that can be used to build animated
 * interactive graphics using the HTML5 canvas.
 * @author Nik Cross
 * @version 0.001 alpha
 * @license MIT
 */

/**
 * Canvas is the Giraffe representation of a canvas element on
 * a page.
 * @description
 * It can have any number of GraphicsObject added to it,
 * each drawn at its required place.
 * An instance of Canvas can also be used to control animation and interaction
 * between the user and GraphicsObjects using the Giraffe Animation library and
 * the Giraffe Interactive library
 * @class
 * @param {String} canvasElementId dom canvas id
 */
function Canvas(canvasElementId)
{
  var self=this;
  this.id = "canvas"+Giraffe.canvases.length;
  
  Giraffe.canvases[ this.id ] = this;

  this.graphicsObjects = [];
  this.canvasElement = document.getElementById(canvasElementId);

  if(BrowserDetect.browser=="MSIE" || BrowserDetect.browser=="Explorer") 
  {
     this.canvasElement = window.G_vmlCanvasManager.initElement(this.canvasElement); 
  }

  this.width = this.canvasElement.width;
  this.height = this.canvasElement.height;
  this.canvasContext = this.canvasElement.getContext('2d');

  this.canvasContext.clearRect(0,0,this.width,this.height); // clear canvas
  
  this.scaleX=1;
  this.scaleY=1;
  
  /**#@+
   * @memberOf Canvas
   */
  
  /**
   * Sets the scale of all graphics on the canvas. May be used to zoom in and out.
   * @param {float} scaleX the multiplier scale for the x axis. Must be greater than 0.
   * @param {float} scaleY the multiplier scale for the y axis. Must be greater than 0.
   */
  this.scale = function(scaleX,scaleY) {
      this.scaleX=scaleX;
      this.scaleY=scaleY;
  }
  
  this.scaleSet=false;
  
  /**
   * Clears the canvas and repaints all graphics on the canvas.
   * If not using the animation library, this method is called to
   * render the GraphicsObjects onto the canvas.
   */
  this.repaint = function()
  {
    this.canvasContext.clearRect(0,0,this.width,this.height); // clear canvas

    if(this.scaleSet==false) {
    	this.canvasContext.scale(this.scaleX,this.scaleY);
    	this.scaleSet=true;
    }
    for(this.loop=0;this.loop<this.graphicsObjects.length;this.loop++)
    {
    	if(this.graphicsObjects[this.loop].visible==true) {
    		this.graphicsObjects[this.loop]._repaint();
    	}
    }
  }

  /**
   * @private
   */
  this._store = function() {
    this.canvasContext.save();
  }

  /**
   * @private
   */
  this._restore = function() {
    this.canvasContext.restore();
  }

  /**
   * Adds a GraphicsObject instance to the canvas to be drawn
   * @param {GraphicsObject} graphicsObject an instance of a GraphicsObject to add to the canvas.
   *
   */
  this.add = function( graphicsObject )
  {
    this.graphicsObjects[this.graphicsObjects.length] = graphicsObject;
    graphicsObject.canvasParent = this;
    graphicsObject.canvas = this.canvasContext;
    graphicsObject.draw(); // initialises composites
  }
  
  /**
   * Removes a GraphicsObject instance from the canvas.
   * @param {GraphicsObject} graphicsObject to be removed
   */
  this.remove = function( graphicsObject ) {
	for(this.loop=0;this.loop<this.graphicsObjects.length;this.loop++)
    {
		if(this.graphicsObjects[this.loop]==graphicsObject) {
			graphicsObject.canvasParent=null;
			this.graphicsObjects.splice(this.loop,1);
		}
	}
  }
  
  /**
   * Stretches a canvas to fit a window while maintaining the design aspect ratio
   * @param screenDesignSize {Giraffe.Size} the width and height intended for the canvas
   */
  this.stretchToFitWindow = function() {
	 	var designWidth = screen.width;
	 	var designHeight = screen.height;
	 	
	 	var scaleChange = 1;
	   	var docWidth = window.outerWidth;
	   	var docHeight = window.outerHeight;

	   	if (docWidth != designWidth) {
	   		var scaleX = docWidth / designWidth;
	   		var scaleY = docHeight / designHeight;
	   		
	   		if (scaleX < scaleY) {
	   			scaleChange = scaleX;
	   		} else {
	   			scaleChange = scaleY;
	   		}
	   		
	   		this.scale(scaleChange,scaleChange);
	   	  	screen.width = designWidth*scaleChange;
	   	  	screen.height = designHeight*scaleChange;
	   	}
	   }
}
/**#@-*/

/**
 * This is the prototype object for all graphics objects that can be placed on a canvas.
 * Extend this prototype to create your own GraphicsObject.
 * @class
 * 
 * @param {number} x the position the graphics object is to be drawn at on the x axis 
 * @param {number} y the position the graphics object is to be drawn at on the y axis 
 */
function GraphicsObject(x,y)
{
  this.canvasParent = null;
  this.canvas = null;
  this.x = x;
  this.y = y;
  this.rotation = 0;
  this.color = "black";
  this.fillColor = null;
  this.shadow = null;
  this.scaleX = 1;
  this.scaleY = 1;
  this.visible=true;
  this.mouseOver=false;

  /**#@+
   * @memberOf GraphicsObject
   */
  /**
   * @private
   */
  this._repaint = function()
  {
    this.canvasParent._store();
    if(this.canvas==undefined) {
    	this.canvas = this.canvasParent.canvasContext;
    }
    this.canvas.translate(this.x,this.y);
    if(this.scaleX!=1 || this.scaleY!=1)
    {
      this.canvas.scale(this.scaleX,this.scaleY);
    }
    if(this.rotation!=0)
    {
      this.canvas.rotate(this.rotation);
    }
    if(this.shadow!=null) {
    	this.canvas.shadowColor = this.shadow.color;
    	this.canvas.shadowBlur = this.shadow.blur;
    	this.canvas.shadowOffsetX = this.shadow.offsetX;
    	this.canvas.shadowOffsetY = this.shadow.offsetY;
  	}
    this.draw();
    this.canvasParent._restore();
  }

  /**
   * Uses the className to look up the values for color and background-color from
   * any css style sheet loaded in the current page.
   */
  this.setCSSClass = function(className) {
	  this.color = Giraffe.getCssValue(className,"color");
	  this.fillColor = Giraffe.getCssValue(className,"background-color");
	  return this;
  }
  
  /**
   * Sets the border outline color of the object.
   * @param {string} color a definition of the color. Can be in the form html color name eg. "blue", html hex color eg. '#00FF00' or red green blue alpha format eg. rgba(0,255,0,0.5)
   */
  this.setColor = function(color) {
    this.color=color;
    return this;
  }
  
  this.setShadow = function(shadow) {
	  this.shadow = shadow;
	  return this;
  }
  
  /**
   * Sets the fill color of the object.
   * @param {string} fillColor a definition of the color. Can be in the form html color name eg. "blue", html hex color eg. '#00FF00' or red green blue alpha format eg. rgba(0,255,0,0.5)
   */
  this.setFillColor = function(fillColor) {
    this.fillColor=fillColor;
    return this;
  }

  this.setRotation = function(rotation) {
	    this.rotation=rotation;
	    return this;
  }
  
  this.isInside = function(x,y){return false;}
  this.onClick = function(x,y){}
  this.onMouseOver = function(x,y){}
  this.onMouseOut = function(x,y){}
  this.onMousePressed = function(x,y){}
  this.onMouseReleased = function(x,y) {}
  this.animate = function(frame){}
  this.draw = function(){}
}
/**#@-*/

/**
 * Defines a graphics Circle primative
 * @class
 * @augments GraphicsObject
 * @param x {number} the position that the ... is to be drawn on the x axis
 * @param y {number} the position that the ... is to be drawn on the y axis
 * @param radius (number) the radius of the circle to be drawn. Must be greater than 0
 */
function Circle(x,y,radius)
{
	  /**#@+
	   * @memberOf Circle
	   */
  this.x=x;
  this.y=y;
  this.radius=radius;
  /**
   * @private
   */
  this.draw = function()
  {
    this.canvas.beginPath();
    if(this.fillColor!=null)
    {
      this.canvas.fillStyle = this.fillColor;
    }
    this.canvas.strokeStyle = this.color;
    this.canvas.arc(0,0,this.radius,0,6.2,false);
    this.canvas.closePath()
    if(this.fillColor!=null)
    {
      this.canvas.fill();
    }
    this.canvas.stroke(); 
  }
  
  /**
   * Checks to see if a given point lies inside the circle.
   * @param posX {number} the x axis of the point to check
   * @param posY {number} the y axis of the point to check
   * @returns true if the point lies within the Circle
   */
  this.isInside = function(posX,posY) {
		//alert("testing "+posX+","+posY);
	var xl = this.x-posX;
	var yl = this.y-posY;
	if( Math.round( 
		Math.pow( (xl*xl)+(yl*yl),0.5)
	)<this.radius) {
		return true;
	} else {
		return false;
	}
  }
}
Circle.prototype = new GraphicsObject();
/**#@-*/

/**
 * Defines a graphics Rectangle primative
 * @class
 * @augments GraphicsObject
 * @param x {number} the position that the ... is to be drawn on the x axis
 * @param y {number} the position that the ... is to be drawn on the y axis
 * @param width (number) the width of the rectangle
 * @param height (number) the height of the rectangle
 */
function Rectangle(x,y,width,height)
{
	  /**#@+
	   * @memberOf Rectangle
	   */
  this.x=x;
  this.y=y;
  this.width=width;
  this.height=height;

  /**
   * Checks to see if a given point lies inside the rectangle.
   * @param posX {number} the x axis of the point to check
   * @param posY {number} the y axis of the point to check
   * @returns true if the point lies within the Rectangle
   */
  this.isInside = function(posX,posY) {
	if(
		posX-this.x>0 &&
		posX-this.x<this.width &&
		posY-this.y>0 &&
		posY-this.y<this.height
		){ return true; } else { return false; }
  }
  
  /**
   * @private
   */
  this.draw = function()
  {
    if(this.fillColor!=null)
    {
      this.canvas.fillStyle = this.fillColor;
    }
    this.canvas.strokeStyle = this.color;
    if(this.fillColor!=null)
    {
      this.canvas.fillRect(0,0,this.width,this.height);
    }
    this.canvas.strokeRect(0,0,this.width,this.height);
  }
}
Rectangle.prototype = new GraphicsObject();
/**#@-*/
/**
 * Defines a graphics Line primative from one point to another
 * @class
 * @augments GraphicsObject
 * @param x {number} the position that the Line is to be drawn on the x axis
 * @param y {number} the position that the Line is to be drawn on the y axis
 * @param x2 {number} the position that the Line is to be drawn to on the x axis
 * @param y2 {number} the position that the Line is to be drawn to on the y axis
 */
function Line(x,y,x2,y2)
{
	  /**#@+
	   * @memberOf Line
	   */
  this.x=x;
  this.y=y;
  this.x2=x2;
  this.y2=y2;

  /**
   * @private
   */
  this.draw = function()
  {
    this.canvas.strokeStyle = this.color;

    this.canvas.beginPath();
    this.canvas.moveTo(0,0); 
    this.canvas.lineTo(this.x2,this.y2); 
    this.canvas.closePath();

    this.canvas.stroke(); 
  }
}
Line.prototype = new GraphicsObject();
/**#@-*/
/**
 * Defines a graphics primative
 * @class
 * @augments GraphicsObject
 * @param x {number} the position that the ... is to be drawn on the x axis
 * @param y {number} the position that the ... is to be drawn on the y axis
 */
function Text(x,y,text,textSize,font)
{
	  /**#@+
	   * @memberOf Text
	   */
  this.x=x;
  this.y=y;
  this.text=text;
  this.textSize=textSize;
  this.font = font;

  /**
   * @private
   */
  this.draw = function()
  {
    if(this.fillColor!=null)
    {
      this.canvas.fillStyle = this.fillColor;
    }
    this.canvas.strokeStyle = this.color;

    this.canvas.font = this.textSize+"px "+this.font;
    if(this.fillColor!=null)
    {
      this.canvas.fillText(this.text,0,0,400);
    } else {
       this.canvas.strokeText(this.text,0,0,400);
    }
  }
  
  /**
   * Checks to see if a given point lies inside the bounds of the text.
   * @param posX {number} the x axis of the point to check
   * @param posY {number} the y axis of the point to check
   * @returns true if the point lies within the Text
   */
  this.isInside = function(posX,posY) {
	if(
		posX-this.x>(this.textSize/2) &&
		posX-this.x<this.textSize+(this.text.length*(this.textSize/2)) &&
		posY-this.y>-(this.textSize/2) &&
		posY-this.y<(this.textSize/2)
		){ return true; } else { return false; }
  }
}
Text.prototype = new GraphicsObject();
/**#@-*/
/**
 * Defines a graphics Picture primative
 * @class
 * @augments GraphicsObject
 * @param x {number} the position that the picture is to be drawn on the x axis
 * @param y {number} the position that the picture is to be drawn on the y axis
 * @param src {string} the uri of the image to  be used. The image can be in gif, png or jpeg format.
 */
function Picture(x,y,src)
{
	  /**#@+
	   * @memberOf Picture
	   */
  this.x=x;
  this.y=y;
  this.img = new Image();
  this.img.src = src;
/**
 * @private
 */
  this.draw = function()
  {
    this.canvas.strokeStyle = this.color;
    this.canvas.drawImage(this.img,0,0);
  }
  
  /**
   * Checks to see if a given point lies inside the bounds of the picture.
   * @param posX {number} the x axis of the point to check
   * @param posY {number} the y axis of the point to check
   * @returns true if the point lies within the picture
   */
  this.isInside = function(posX,posY) {
	if(
		posX-this.x>0 &&
		posX-this.x<this.img.width &&
		posY-this.y>0 &&
		posY-this.y<this.img.height
		){ return true; } else { return false; }
  }
}
Picture.prototype = new GraphicsObject();
/**#@-*/
/**
 * Defines a graphics primative
 * @class
 * @augments GraphicsObject
 * @param x {number} the position that the ... is to be drawn on the x axis
 * @param y {number} the position that the ... is to be drawn on the y axis
 */
function Polygon(x,y)
{
	  /**#@+
	   * @memberOf Polygon
	   */
  this.x=x;
  this.y=y;

  this.points = new Array();
  this.addPoint = function(px,py) {
    this.points[this.points.length]=[px,py];
    return this;
  }
  /**
   * @private
   */
  this.draw = function()
  {
    this.canvas.beginPath();
    if(this.fillColor!=null)
    {
      this.canvas.fillStyle = this.fillColor;
    }
    this.canvas.strokeStyle = this.color;
    this.canvas.moveTo(this.points[0][0],this.points[0][1]);
    for(i=1;i<this.points.length;i++) {
      this.canvas.lineTo(this.points[i][0],this.points[i][1]);
    }
    this.canvas.lineTo(this.points[0][0],this.points[0][1]);
    if(this.fillColor!=null)
    {
      this.canvas.fill();
    }
    this.canvas.stroke(); 
  }
}
Polygon.prototype = new GraphicsObject();
/**#@-*/
/**
 * Defines a graphics primative
 * @class
 * @augments GraphicsObject
 * @param x {number} the position that the ... is to be drawn on the x axis
 * @param y {number} the position that the ... is to be drawn on the y axis
 */
function Arc(x,y,startAngle,sweepAngle,radius)
{
	  /**#@+
	   * @memberOf Arc
	   */
  this.x=x;
  this.y=y;
  this.closed=true;
  this.startAngle=startAngle;
  this.sweepAngle=sweepAngle;
  this.radius=radius;
  /**
   * @private
   */
  this.draw = function()
  {
    this.canvas.beginPath();
    if(this.fillColor!=null)
    {
      this.canvas.fillStyle = this.fillColor;
    }
    var startAngleRad = this.startAngle*2*Math.PI/360;
    var sweepAngleRad = ((this.startAngle+this.sweepAngle)%360)*2*Math.PI/360;

    this.canvas.strokeStyle = this.color;
    if(this.closed==true) {
    	this.canvas.moveTo(0,0);
	    this.canvas.lineTo(
    		Math.cos(startAngleRad)*radius,
      		Math.sin(startAngleRad)*radius
    	);
    }
    this.canvas.arc(0,0,this.radius,startAngleRad,sweepAngleRad,false);
    if(this.closed==true) {
    	this.canvas.lineTo( 0,0 );
    }
    this.canvas.closePath()
    if(this.fillColor!=null)
    {
      this.canvas.fill();
    }
    this.canvas.stroke();
  }

  this.setColor = function(color) {
    this.color = color;
    return this;
  }
  this.setFillColor = function(fillColor) {
    this.fillColor= fillColor;
    return this;
  }
  
  this.isInside = function(posX,posY) {
	var xl = this.x-posX;
	var yl = this.y-posY;
	var d = Math.pow( (xl*xl)+(yl*yl),0.5);
	if( d<this.radius ) {
		if(xl==0 && yl==0) return true;
		var a = Math.atan(yl/xl);
		if(xl>0) a=Math.PI+a;
		if(a<0) a=(Math.PI*2)+a;
		a = (a*180)/(Math.PI)
		if(a>=this.startAngle &&
		a<=this.startAngle+this.sweepAngle) {
			return true;
		} else {
			return false;
		}
	} else {
		return false;
	}
  }
  this.setClosed = function() {
  	this.closed=true;
  	return this;
  }
  this.setOpen = function() {
  	this.closed=false;
  	return this;
  }
}
Arc.prototype = new GraphicsObject();
/**#@-*/
/**
 * Defines a graphics primative
 * @class
 * @augments GraphicsObject
 * @param x {number} the position that the ... is to be drawn on the x axis
 * @param y {number} the position that the ... is to be drawn on the y axis
 */
function Composite(x,y,rotation)
{
	  /**#@+
	   * @memberOf Composite
	   */
  this.x=x;
  this.y=y;
  this.rotation=rotation;
  this.parts = [];

  this.add = function( part )
  {
    if(part.canvasParent!=null)
    {
      return;
    }
    part.canvasParent = this.canvasParent;
    this.parts[this.parts.length] = part;
  }

  this.remove = function( part ) {
	for(this.loop=0;this.loop<this.parts.length;this.loop++)
    {
		if(this.parts[this.loop]==part) {
			part.canvasParent=null;
			this.parts.splice(this.loop,1);
		}
	}
  }
  
  this.deconstruct = function() {
	for(this.loop=0;this.loop<this.parts.length;this.loop++)
    {
		var part = this.parts[this.loop];
		part.x += this.x;
		part.y += this.y;
		part.onMouseOver = this.onMouseOver;
		part.onMouseOut = this.onMouseOut;
		part.onMousePressed = this.onMousePressed;
		part.onMouseReleased = this.onMouseReleased;
		part.onClick = this.onClick;
		part.rotation += this.rotation;
		part.canvasParent=null;
		this.canvasParent.add( part );
	}
	this.parts = new Array();
  }
  /**
   * @private
   */  
  this.draw = function()
  {
    for(this.loop=0;this.loop<this.parts.length;this.loop++)
    {
      if(this.parts[this.loop].canvasParent==null)
      {
        this.parts[this.loop].canvasParent=this.canvasParent;
        this.parts[this.loop].canvas=this.canvas;
      }
      if(this.parts[this.loop].visible==true) {
    	  this.parts[this.loop]._repaint();
      }
    }    
  }

  this.animate = function(frame) {
    for(this.loop=0;this.loop<this.parts.length;this.loop++)
    {    
      this.parts[this.loop].animate(frame);
    }
  }
  
  this.isInside = function(posX,posY) {
    for(this.loop=0;this.loop<this.parts.length;this.loop++)
    {    
      if(this.parts[this.loop].visible===true && this.parts[this.loop].isInside(posX-this.x,posY-this.y)) {
		return true;
	  }
    }
	return false;
  }
  
  this.onClick = function(posX,posY) {
    for(this.loop=0;this.loop<this.parts.length;this.loop++)
    {    
		if(this.parts[this.loop].visible===true && this.parts[this.loop].isInside(posX-this.x,posY-this.y)) {
			this.parts[this.loop].onClick(posX-this.x,posY-this.y);
		}
    }
	return false;
  }  
  this.onMouseOver = function(posX,posY) {
    for(this.loop=0;this.loop<this.parts.length;this.loop++)
    {
		if(this.parts[this.loop].visible===true && this.parts[this.loop].isInside(posX-this.x,posY-this.y)) {    
			this.parts[this.loop].onMouseOver(posX-this.x,posY-this.y);
			this.parts[this.loop].mouseOver=true;
		} else if(this.parts[this.loop].mouseOver==true) {
			this.parts[this.loop].mouseOver=false;
			this.parts[this.loop].onMouseOut(posX-this.x,posY-this.y);
		}
    }
  } 
  this.onMouseOut = function(posX,posY) {
    for(this.loop=0;this.loop<this.parts.length;this.loop++)
    {
		if(this.parts[this.loop].mouseOver==true) {
			this.parts[this.loop].mouseOver=false;
			this.parts[this.loop].onMouseOut(posX-this.x,posY-this.y);
		}
    }
  }   
  this.onMousePressed = function(posX,posY) {
    for(this.loop=0;this.loop<this.parts.length;this.loop++)
    {    
		if(this.parts[this.loop].visible===true && this.parts[this.loop].isInside(posX-this.x,posY-this.y)) {  
			this.parts[this.loop].onMousePressed(posX-this.x,posY-this.y);
		}
    }
  } 
  this.onMouseReleased = function(posX,posY) {
    for(this.loop=0;this.loop<this.parts.length;this.loop++)
    {    
      this.parts[this.loop].onMouseReleased(posX-this.x,posY-this.y);
    }
  } 
}
Composite.prototype = new GraphicsObject();
/**#@-*/
/**
 * Defines a graphics primative
 * @description
 * <iframe src="/web/giraffe/examples/giraffe-examples-home-page.html" width="500" height="500"></iframe>
 * @class
 * @augments GraphicsObject
 * @param x {number} the position that the ... is to be drawn on the x axis
 * @param y {number} the position that the ... is to be drawn on the y axis
 */
function RadialColor(canvas,color1,color2,x,y,radius) {
	  /**#@+
	   * @memberOf RadialColor
	   */
  this.canvas = canvas.canvasContext;
  this.colorStop = new Array();
  this.colorStop[0] = [0,color1,0,0,radius];
  this.colorStop[1] = [1,color2,x,y,0];

  this.getColor = function() {
    gradient = this.canvas.createRadialGradient(
      this.colorStop[1][2],this.colorStop[1][3],this.colorStop[1][4],
      this.colorStop[0][2],this.colorStop[0][3],this.colorStop[0][4]
    );
    var i=0;
    for(i=0;i<this.colorStop.length;i++)
    {
      gradient.addColorStop(this.colorStop[i][0],this.colorStop[i][1]);
    }
    return gradient;
  }
}

function Shadow() {
    this.color = '#999';
    this.blur = 20;
    this.offsetX = 15;
    this.offsetY = 15;
}
/**#@-*/
/**
 * Some helper methods used by Giraffe
 * @class
 */
Giraffe = {
		/**
		 * @private
		 */
		canvases : [],
		/**
		 * @private
		 */
		getCssValue : function(selector,attribute) {
			selector = selector.toLowerCase();
		   for(sheet=0;sheet<document.styleSheets.length;sheet++) {
			   var stylesheet = document.styleSheets[sheet];
			   var n = stylesheet.cssRules.length;
			   for(var i=0; i<n; i++)
			   {
			      var selectors = stylesheet.cssRules[i].selectorText.toLowerCase().split(",");
			      var m = selectors.length;
			      for(j=0; j<m; j++)
			      {
			         if(selectors[j].trim() == selector)
			         {
			            var value = stylesheet.cssRules[i].style.getPropertyValue(attribute);
			            if(value!="")
			            {
			               return value;
			            }
			         }
			      }
			   }
		   }
		   return null;
		}
}

/**
 * @private
 */
var BrowserDetect = {
		init: function () {
			this.browser = this.searchString(this.dataBrowser) || "An unknown browser";
			this.version = this.searchVersion(navigator.userAgent)
				|| this.searchVersion(navigator.appVersion)
				|| "an unknown version";
			this.OS = this.searchString(this.dataOS) || "an unknown OS";
		},
		searchString: function (data) {
			for (var i=0;i<data.length;i++)	{
				var dataString = data[i].string;
				var dataProp = data[i].prop;
				this.versionSearchString = data[i].versionSearch || data[i].identity;
				if (dataString) {
					if (dataString.indexOf(data[i].subString) != -1)
						return data[i].identity;
				}
				else if (dataProp)
					return data[i].identity;
			}
		},
		searchVersion: function (dataString) {
			var index = dataString.indexOf(this.versionSearchString);
			if (index == -1) return;
			return parseFloat(dataString.substring(index+this.versionSearchString.length+1));
		},
		dataBrowser: [
			{
				string: navigator.userAgent,
				subString: "Chrome",
				identity: "Chrome"
			},
			{ 	string: navigator.userAgent,
				subString: "OmniWeb",
				versionSearch: "OmniWeb/",
				identity: "OmniWeb"
			},
			{
				string: navigator.vendor,
				subString: "Apple",
				identity: "Safari"
			},
			{
				prop: window.opera,
				identity: "Opera"
			},
			{
				string: navigator.vendor,
				subString: "iCab",
				identity: "iCab"
			},
			{
				string: navigator.vendor,
				subString: "KDE",
				identity: "Konqueror"
			},
			{
				string: navigator.userAgent,
				subString: "Firefox",
				identity: "Firefox"
			},
			{
				string: navigator.vendor,
				subString: "Camino",
				identity: "Camino"
			},
			{		// for newer Netscapes (6+)
				string: navigator.userAgent,
				subString: "Netscape",
				identity: "Netscape"
			},
			{
				string: navigator.userAgent,
				subString: "MSIE",
				identity: "Explorer",
				versionSearch: "MSIE"
			},
			{
				string: navigator.userAgent,
				subString: "Gecko",
				identity: "Mozilla",
				versionSearch: "rv"
			},
			{ 		// for older Netscapes (4-)
				string: navigator.userAgent,
				subString: "Mozilla",
				identity: "Netscape",
				versionSearch: "Mozilla"
			}
		],
		dataOS : [
			{
				string: navigator.platform,
				subString: "Win",
				identity: "Windows"
			},
			{
				string: navigator.platform,
				subString: "Mac",
				identity: "Mac"
			},
			{
				string: navigator.platform,
				subString: "Linux",
				identity: "Linux"
			}
		]

	};
BrowserDetect.init();Giraffe.X=0;
Giraffe.Y=1;
Giraffe.DEG_TO_RAD = Math.PI/180;

Giraffe.setAnimated = function(canvas) {
  canvas.frame = 0;
  canvas.interval = null;
  canvas.frames = 0;
  canvas.looped = true;
  canvas.animationListeners = new Array();

  canvas.addAnimationListener = function(listener) {
  	this.animationListeners[this.animationListeners.length]=listener;
  }
  canvas.removeAnimationListener = function(listener) {
	for(this.loop=0;this.loop<this.animationListeners.length;this.loop++)
    {
		if(this.animationListeners[this.loop]==listener) {
			this.animationListeners.splice(this.loop,1);
		}
	}
  }

  canvas.startAnimation = function(fps,frames,looped)
  {
    this.frame = 0;
    this.frames = frames;
    this.looped = looped;
    this.interval = setInterval("Giraffe.canvases[\""+this.id+"\"].animate();",1000/fps);
  }

  canvas.stopAnimation = function()
  {
    clearInterval( this.interval );
  }

  canvas.animate = function()
  {
    for(this.loop=0;this.loop<this.animationListeners.length;this.loop++)
    {
      this.animationListeners[this.loop].processFrame(this.frame);
    }
    for(this.loop=0;this.loop<this.graphicsObjects.length;this.loop++)
    {
      this.graphicsObjects[this.loop].animate(this.frame);
    }
    this.repaint();
    this.frame++;
    if(this.frame>=this.frames)
    {
      if(this.looped==true)
      {
        this.frame=0;
      }
      else
      {
        this.stopAnimation();
      }
    }
  }
}

/**
 * @class
 */
Giraffe.Transition = function(target,frames) {
	this.frames = frames;
	this.target = target;
	this.canvas = null;
	this.currentFrame=0;
	this.play = false;
	
	this.start = function() {
		this.register();
		this.currentFrame=0;
		this.play=true;
	};
	this.process = function(frame){};
	
	this.processFrame = function() {
		if(this.play==false) { return; };
		this.currentFrame++;
		if(this.currentFrame==this.frames+1) {
			this.unregister();
			this.doNext();
		}
		if(this.currentFrame>this.frames) {
			return false;
		}
		
		this.process(this.currentFrame);
	}
	
	this.doNext = function(){};
	this.register = function(){
		this.canvas.addAnimationListener(this);
	};
	this.unregister = function(){
		this.canvas.removeAnimationListener(this);
	};
}

/**
 * Creates an animation sequence that squashes the x axis of a GraphicsObject
 * @class
 * @param target {GraphicsObject} the GraphicsObject to apply the animation to
 * @param frames integer the number of frames the animation should last for
 */
Giraffe.FlipOutX = function(target,frames) {
	this.frames = frames;
	this.target = target;
	this.canvas = target.canvasParent;
	this.register();
	this.currentFrame=0;

	/**
	 * @private
	 */
	this.process = function(frame) {
		this.target.scaleX = 1-((1*frame)/this.frames);
	}
}
Giraffe.FlipOutX.prototype = new Giraffe.Transition();

Giraffe.FlipInX = function(target,frames) {
	this.frames = frames;
	this.target = target;
	this.canvas = target.canvasParent;
	this.register();
	this.currentFrame=0;

	this.process = function(frame) {
		this.target.scaleX = (1*frame)/this.frames;
	}
}
Giraffe.FlipInX.prototype = new Giraffe.Transition();

Giraffe.FlipOutY = function(target,frames) {
	this.frames = frames;
	this.target = target;
	this.canvas = target.canvasParent;
	this.register();
	this.currentFrame=0;

	this.process = function(frame){
		this.target.scaleY = 1-((1*frame)/this.frames);
	}
}
Giraffe.FlipOutY.prototype = new Giraffe.Transition();

Giraffe.FlipInY = function(target,frames) {
	this.frames = frames;
	this.target = target;
	this.canvas = target.canvasParent;
	this.register();
	this.currentFrame=0;

	this.process = function(frame) {
		this.target.scaleY = (1*frame)/this.frames;
	}
}
Giraffe.FlipInY.prototype = new Giraffe.Transition();

Giraffe.MoveSequence = function(target,frames,matrix) {
	this.frames = frames;
	this.target = target;
	this.matrix = matrix;
	this.canvas = target.canvasParent;
	this.register();
	this.currentFrame=0;

	this.process = function(frame) {
		if(this.matrix) {
			this.target.x+=this.matrix[Giraffe.X];
			this.target.y+=this.matrix[Giraffe.Y];
		} else {
			this.target.x+=this.target.vx;
			this.target.y+=this.target.vy;
		}
	}
}
Giraffe.MoveSequence.prototype = new Giraffe.Transition();

Giraffe.RotationSequence = function(target,frames,steps) {
	this.frames = frames;
	this.target = target;
	this.steps = steps;
	this.canvas = target.canvasParent;
	this.register();
	this.currentFrame=0;

	this.process = function(frame) {
		this.target.setRotation( this.target.rotation+(Giraffe.DEG_TO_RAD*this.steps) );
	}
}
Giraffe.RotationSequence.prototype = new Giraffe.Transition();

Giraffe.ExplodeSequence = function(target,frames) {
	this.frames = frames;
	this.target = target;
	this.canvas = target.canvasParent;
	this.register();
	this.currentFrame=0;

	this.process = function(frame) {
		if(!this.parts) {
			this.parts = getExplodingParts(target);
		}
		
		var hasMinVelocity = false;
		for(i=0;i<this.parts.length;i++) {
			this.parts[i].x+=this.parts[i].vx;
			this.parts[i].y+=this.parts[i].vy;
			this.parts[i].vx*=0.9;
			this.parts[i].vy*=0.9;
			if(this.parts[i].vx>0.5 || this.parts[i].vx<-0.5 ||
				this.parts[i].vy>0.5 || this.parts[i].vy<-0.5) {
				hasMinVelocity=true;
			}
		}
		if(hasMinVelocity==false) {
			this.currentFrame = this.frames;
		}
	}
	
	var getExplodingParts = function(composite,parts) {
		if(!parts) {
			var parts = [];
		}
		for(var i=0;i<composite.parts.length;i++) {
			if(composite.parts[i].parts) { //is sub composite
				getExplodingParts(composite.parts[i],parts);
			}
			composite.parts[i].vx=composite.parts[i].x+((Math.random()*10)-5);
			composite.parts[i].vy=composite.parts[i].y+((Math.random()*10)-5);
			parts[parts.length]=composite.parts[i];
		}
		return parts;
	}
}
Giraffe.ExplodeSequence.prototype = new Giraffe.Transition();
/**
 * @class
 */
Giraffe.Interactive = {
		shiftKeyDown : false,
		controlKeyDown : false,
		init : function() {
			document.onkeydown = Giraffe.Interactive.keyDownHandler;
			document.onkeyup = Giraffe.Interactive.keyUpHandler;
			shiftKeyDown = false;
			controlKeyDown = false;
		},
		keyDownHandler : function(e) {
		    var pressedKey;
		    if (document.all) { e = window.event;
		        pressedKey = e.keyCode; }
		    if (e.which) {
		        pressedKey = e.which;
		    }
			if(pressedKey==16) {
				Giraffe.Interactive.shiftKeyDown=true;
			} else if(pressedKey=17) {
				Giraffe.Interactive.controlKeyDown=true;
			}
		},
		keyUpHandler : function(e) {
		    var pressedKey;
		    if (document.all) { e = window.event;
		        pressedKey = e.keyCode; }
		    if (e.which) {
		        pressedKey = e.which;
		    }
			if(pressedKey==16) {
				Giraffe.Interactive.shiftKeyDown=false;
			} else if(pressedKey=17) {
				Giraffe.Interactive.controlKeyDown=false;
			}
		},
		isDragging : function() {
			if( self.dragging && self.dragging.length>0 ) {
				return true;
			} else {
				return false;
			}
		},
		setInteractive : function(canvas) {
  			var canvas = canvas;
  
		  canvas.convertEvent = function(event,element) { 
			  position = getPosition(element);
			  x=event.x-position[Giraffe.X];
			  y=event.y-position[Giraffe.Y];
			  return {
				x : x, y : y  
			  }
		  }
  
		  canvas.onClick = function(event) {
		    event = canvas.convertEvent(event,canvas.canvasElement);
				
			for(this.loop=0;this.loop<canvas.graphicsObjects.length;this.loop++)
		    {
			  if(canvas.graphicsObjects[this.loop].visible===true && canvas.graphicsObjects[this.loop].isInside(event.x,event.y)) {
				canvas.graphicsObjects[this.loop].onClick(event.x,event.y);
			  }
			 }
		  }
		 canvas.onMouseDown = function(event) {
		event = canvas.convertEvent(event,canvas.canvasElement);
			
	    if(canvas.dragAndDrop==true) {
			canvas.dragStart=[event.x,event.y];
			canvas.dragging=new Array();
		}
		for(var dragTarget in canvas.draggable) {
			dragTarget=canvas.draggable[dragTarget];
	
			if(dragTarget.isInside(event.x,event.y)) {
				var included = false;
				for(var check in canvas.dragging) {
					check = canvas.dragging[check];
					if(check==dragTarget) {
						included=true;
						break;
					}
				}
				if(included==true) {
					continue;
				}
				dragTarget.dragging=true;
				canvas.dragging[canvas.dragging.length]=dragTarget;
				dragTarget.dragStart=[dragTarget.x,dragTarget.y];
				if(Giraffe.Interactive.shiftKeyDown==false) break;
			}
		}
		
		for(this.loop=0;this.loop<canvas.graphicsObjects.length;this.loop++)
	    {
		  if(canvas.graphicsObjects[this.loop].visible===true && canvas.graphicsObjects[this.loop].isInside(event.x,event.y)) {
			canvas.graphicsObjects[this.loop].onMousePressed(event.x,event.y);
		  }
		 }
	  }
	  canvas.setDragging = function(dragTarget) {
	  
 // alert("Here: "+dragTarget.x+","+dragTarget.y);
  
	  	dragTarget.dragging=true;
		canvas.dragging[canvas.dragging.length]=dragTarget;
		dragTarget.dragStart=[dragTarget.x,dragTarget.y];
	  }
	  canvas.onMouseUp = function(event) {
		event = canvas.convertEvent(event,canvas.canvasElement);
			
		if(canvas.dragAndDrop==true && canvas.dragging.length>0) {
			for(this.loop=0;this.loop<canvas.graphicsObjects.length;this.loop++)
			{
				if(canvas.graphicsObjects[this.loop].dragging) {
					canvas.graphicsObjects[this.loop].dragging=false;
				}
				if(canvas.graphicsObjects[this.loop].isInside(event.x,event.y)) {
					if(canvas.graphicsObjects[this.loop].onCatch) {
						for(var dropped in canvas.dragging) {
							dropped = canvas.dragging[dropped];
							if(canvas.graphicsObjects[this.loop]==dropped) {
								continue;
							}
							canvas.graphicsObjects[this.loop].onCatch(dropped,event.x,event.y);
						}
					}
				}
			}
	  		canvas.dragging=new Array();
		}
		for(this.loop=0;this.loop<canvas.graphicsObjects.length;this.loop++)
	    {
			canvas.graphicsObjects[this.loop].onMouseReleased(event.x,event.y);
		}
	  }
	  canvas.onMouseMoved = function(event) {
		event = canvas.convertEvent(event,canvas.canvasElement);
		
	    if(canvas.dragAndDrop==true) {
			var dragDX = event.x-canvas.dragStart[0];
			var dragDY = event.y-canvas.dragStart[1];
			for(var dragTarget in canvas.dragging) {
				dragTarget = canvas.dragging[dragTarget];
				dragTarget.x = dragTarget.dragStart[0]+dragDX;
				dragTarget.y = dragTarget.dragStart[1]+dragDY;
			}
		}
		for(this.loop=0;this.loop<canvas.graphicsObjects.length;this.loop++)
	    {
		  if(canvas.graphicsObjects[this.loop].isInside(event.x,event.y)) {
			canvas.graphicsObjects[this.loop].mouseOver=true;
			canvas.graphicsObjects[this.loop].onMouseOver(event.x,event.y);
		  } else if(canvas.graphicsObjects[this.loop].mouseOver==true) {
			canvas.graphicsObjects[this.loop].mouseOver=false;
			canvas.graphicsObjects[this.loop].onMouseOut(event.x,event.y);
		  }
		}
	  }
	  
	  canvas.makeDraggable = function(object) {
		this.dragAndDrop = true;
		this.draggable[this.draggable.length]=object;
	  }
	  canvas.removeDraggable = function(object) {
		this.dragAndDrop = false;
		var foundIndex = -1;
		for(var index in this.draggable) {
			if(this.draggable[index]==object) {
				foundIndex = index;
				break;
			}
		}
		if(foundIndex!=-1) {
			this.draggable.splice(foundIndex,1);
		}
	  }
	  
	  canvas.dragStart = [0,0];
	  canvas.dragging = new Array();
	  canvas.draggable = new Array();
	  canvas.dragAndDrop = false; 
	  
		canvas.canvasElement.onmousemove = canvas.onMouseMoved;
		canvas.canvasElement.onmouseup = canvas.onMouseUp;
		canvas.canvasElement.onmousedown = canvas.onMouseDown;
		canvas.canvasElement.onclick = canvas.onClick;
	}
}
Giraffe.Interactive.init();

/* behaviours */
function setReveal(target,reveal) {
	target.onMouseOver = function(x,y) {
		reveal.visible=true;
	}
	target.onMouseOut = function(x,y) {
		reveal.visible=false;
	}
	reveal.visible=false;
}

function getPosition(obj) {
	this.curleft = this.curtop = 0;
	if (obj.offsetParent) {
		this.curleft = obj.offsetLeft
		this.curtop = obj.offsetTop
		while (obj = obj.offsetParent) {
			this.curleft += obj.offsetLeft
			this.curtop += obj.offsetTop
		}
	}
	return [this.curleft,this.curtop];
}

if(typeof(initGiraffe)!="undefined") {
	initGiraffe();
}