'use strict';

angular.module('chordApp.directives', []).
  directive("chord", function () {
    var linker = function (scope, element, attrs) {
      var numFrets = 6;
      var chord = scope.chord.frets;
      var degrees = scope.chord.degrees;
      var name = scope.chord.name;

      var numStrings = chord.length;
      //Width and height
      var w = 11 * numStrings;
      var h = 10 + 10 * numFrets;
      var padding = 36;
      var topPad = 25;
      var bottomPad = 20;

      var svg = d3.select(element[0]).append("svg").attr("width", w + padding).attr("height", h + topPad + bottomPad);

      var frets = [];

      var computeAdjustment = function (numFrets, chord) {
        var minFret = _.min(_.filter(chord, function (v) {
          return _.isNumber(v) && v > 0;
        }));
        var maxFret = _.max(_.filter(chord, _.isNumber));
        var fretMarker = 0;
        var span = maxFret - minFret + 1;
        if (maxFret > numFrets) {
          if (span < numFrets) {
            fretMarker = (minFret % 2 == 1) ? minFret : minFret - 1;
          } else {
            fretMarker = minFret;
          }
        }
        return fretMarker;
      };

      var fretMarker = computeAdjustment(numFrets - 1, chord);
      var fretAdj = fretMarker > 0 ? 1 - fretMarker : 0;

      var stringScale = d3.scale.linear().domain([0, numStrings - 1]).range([padding / 2, w + padding / 2]);
      var fretScale = d3.scale.linear().domain([0, numFrets - 1]).range([topPad, h + topPad]);

      //TODO: can this be replaced/eliminated???
      for (var i = 0; i < numFrets; i++) {
        frets.push(i);
      }

      var chordElem = svg.append("g");
      chordElem.append("text").attr("text-anchor", "middle").attr("transform", "translate(0, 12)").attr("x",
        (w + padding) /
        2).attr("y",
        0).text(name);

      var stringLines = chordElem.append("g").selectAll("line")
        .data(chord)
        .enter()
        .append("line");

      stringLines.attr("x1", function (d, i) {
        return stringScale(i);
      })
        .attr("x2", function (d, i) {
          return stringScale(i);
        })
        .attr("y1", fretScale(0))
        .attr("y2", fretScale(numFrets - 1))
        .attr("stroke", "black");

      var fretLines = chordElem.append("g").selectAll("line")
        .data(frets)
        .enter()
        .append("line");

      fretLines.attr("y1", function (d, i) {
        return fretScale(i);
      })
        .attr("y2", function (d, i) {
          return fretScale(i);
        })
        .attr("x1", stringScale(0))
        .attr("x2", stringScale(numStrings - 1))
        .attr("stroke", "black")
        .attr("stroke-width", function (d, i) {
          if (i == 0 && fretAdj == 0) {
            return 3;
          } else {
            return 1;
          }
        });

      var dots = chordElem.append("g").attr("transform",
        "translate(0," + (-h / (2 * (numFrets - 1))) + ")").selectAll("circle")
        .data(chord)
        .enter()
        .append("circle");

      dots.attr("cx", function (d, i) {
        return stringScale(i);
      })
        .attr("cy", function (d, i) {
          if (d == 'x') {
            return 0;
          }
          return (d == 0) ? fretScale(0) : fretScale(d + fretAdj);
        }).attr("style", function (d, i) {
          if (d != 0) {
            return "fill: black";
          } else {
            return "fill: white";
          }
        })
        .attr("stroke", "black")
        .attr("r", function (d, i) {
          if (d == 'x') {
            return 0;
          } else {
            return 4;
          }
        });

      var crosses = chordElem.append("g").attr("transform", "translate(0, -2)").selectAll("text")
        .data(chord)
        .enter()
        .append("text");

      crosses.attr("x", function (d, i) {
        return stringScale(i);
      })
        .attr("y", function (d, i) {
          return fretScale(0);
        })
        .attr("text-anchor", "middle")
        .text(function (d) {
          if (d == 'x') {
            return '\u00D7';
          }
        });

      if (fretMarker > 0) {
        chordElem.append("g").append("text").text(fretMarker).attr("y", fretScale(1)).attr("x", function () {
          if (fretMarker < 10) {
            return 13;
          } else {
            return 14;
          }
        })
          .attr("text-anchor", "end");
      }

      // show chord degrees
      var chordDegrees = chordElem.append("g").selectAll("text")
        .data(degrees)
        .enter()
        .append("text");

      chordDegrees.attr("x", function (d, i) {
        return stringScale(i);
      })
        .attr("y", function (d, i) {
          return fretScale(numFrets);
        })
        .attr("text-anchor", "middle")
        .text(function (d) {
          if (d == 'x') {
            return '\u00D7'
          } else {
            return d;
          }
        });

    };
    var controller = function ($scope) {
      // Pending
    };
    return {
      restrict: 'A',
      controller: controller,
      link: linker
    };
  }).directive("chordInput", function () {

  });
