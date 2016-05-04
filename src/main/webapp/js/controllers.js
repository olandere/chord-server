'use strict';

var chordApp = angular.module('chordApp', ['chordApp.directives', 'angularSpinner', 'ngCookies', 'chordApp.filters']);

chordApp.controller("ChordListCtrl", ['$scope', '$http', 'usSpinnerService', '$cookies', '$filter', function ($scope, $http, usSpinnerService, $cookies, $filter) {
  //    $scope.drawChordChart = drawChordChart;
  $scope.myData = {};
  $scope.isDisabled = true;

  $scope.changeTuning = function(tuning) {
    $scope.myData.tuning = $filter('transformSymbols')(tuning);
  };

  $scope.myData.doClick = function (item, event) {
    usSpinnerService.spin('spinner-1');
    $scope.isDisabled = true;
    $scope.myData.chords = [];
    var url, firstChar = $scope.myData.chord.trim().charAt(0);
    if (firstChar.match(/[abcdefgrs]/i)) {
      url = ($scope.myData.shell ? "/shellchord/" : "/chords/") +
        ($scope.myData.fretSpan || 4);
    } else {
      url = "/analyze/" + encodeURIComponent($scope.myData.chord.trim());
    }

    // store tuning
    var tuning = $scope.myData.tuning || "EADGBE";
    var tunings = $cookies.getObject("tunings");
    if (_.isArray(tunings)) {
      tunings.push(tuning);
      $cookies.putObject("tunings", _.uniq(tunings));
    } else {
      $cookies.putObject("tunings", [tuning]);
    }
    $scope.tunings = $cookies.getObject("tunings");

    var responsePromise = $http.get(url, {
      params: {
        "chord": encodeURIComponent($scope.myData.chord.trim()),
        "tuning": tuning,
        "condense": $scope.myData.condense
      }
    }).then(function successCallback(response) {
      $scope.myData.chords = response.data;
      usSpinnerService.stop('spinner-1');
      $scope.isDisabled = false;
    }, function errorCallback(response) {
      alert("Unable to parse " + response.config.params.chord);
      usSpinnerService.stop('spinner-1');
      $scope.isDisabled = false;
    });
  };

  $scope.onChange = function () {
    if (!_.isUndefined($scope.myData.chord)) {
      $scope.myData.chord = $filter('transformSymbols')($scope.myData.chord);
      $scope.isDisabled = false;
    } else {
      $scope.isDisabled = true;
    }
  }

}]);
