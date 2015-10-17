'use strict';

var chordApp = angular.module('chordApp', ['chordApp.directives']);

chordApp.filter('musicNotation', function(){
    return function(a){
        return a.replace(/b/g, '\u266D').replace(/#/g, '\u266F');
    };
}).controller("ChordListCtrl", function ($scope, $http) {
  //    $scope.drawChordChart = drawChordChart;
  $scope.myData = {};
  $scope.myData.doClick = function (item, event) {
    $scope.myData.chords = [];
    var url, firstChar = $scope.myData.chord.trim().charAt(0);
    if (firstChar.match(/[abcdefgrs]/i)) {
      url = ($scope.myData.shell ? "/shellchord/" : "/chords/") +
            ($scope.myData.fretSpan || 4);
    } else {
      url = "/analyze/" + encodeURIComponent($scope.myData.chord.trim());
    }
    var responsePromise = $http.get(url, {
        params: {
            "chord": encodeURIComponent($scope.myData.chord.trim()),
            "tuning": $scope.myData.tuning || "E A D G B E",
            "condense": $scope.myData.condense}});

    responsePromise.success(function (data, status, headers, config) {
      $scope.myData.chords = data;
    });
    responsePromise.error(function (data, status, headers, config) {
      alert("AJAX failed!");
    });
  };
  $scope.onChange = function () {
    $scope.myData.chord = $scope.myData.chord.replace(/b/g, '\u266D');
    $scope.myData.chord = $scope.myData.chord.replace(/#/g, '\u266F');
  }
});
