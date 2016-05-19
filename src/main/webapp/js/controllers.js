(function () {
  'use strict';

  angular
    .module('chordApp', ['chordApp.directives', 'angularSpinner', 'ngCookies', 'chordApp.filters'])
    .controller("ChordListCtrl", chordListController);

  chordListController.$inject = ['tuningService', '$http', 'usSpinnerService', '$filter'];

  function chordListController(tuningService, $http, usSpinnerService, $filter) {

    var vm = this;

    vm.myData = {};
    vm.isDisabled = true;

    vm.changeTuning = function (tuning) {
      vm.myData.tuning = $filter('transformSymbols')(tuning);
    };

    vm.myData.doClick = function (item, event) {
      usSpinnerService.spin('spinner-1');
      vm.isDisabled = true;
      vm.myData.chords = [];
      var url, firstChar = vm.myData.chord.trim().charAt(0);
      if (firstChar.match(/[abcdefgrs]/i)) {
        url = (vm.myData.shell ? "/shellchord/" : "/chords/") +
          (vm.myData.fretSpan || 4);
      } else {
        url = "/analyze/" + encodeURIComponent(vm.myData.chord.trim());
      }

      // store tuning
      var tuning = vm.myData.tuning || "EADGBE";
      tuningService.addTuning(tuning);

      var responsePromise = $http.get(url, {
        params: {
          "chord": encodeURIComponent(vm.myData.chord.trim()),
          "tuning": tuning,
          "condense": vm.myData.condense
        }
      }).then(function successCallback(response) {
        vm.myData.chords = response.data;
        usSpinnerService.stop('spinner-1');
        vm.isDisabled = false;
      }, function errorCallback(response) {
        alert("Unable to parse " + response.config.params.chord);
        usSpinnerService.stop('spinner-1');
        vm.isDisabled = false;
      });
    };

    vm.onChange = function () {
      if (!_.isUndefined(vm.myData.chord)) {
        vm.myData.chord = $filter('transformSymbols')(vm.myData.chord);
        vm.isDisabled = false;
      } else {
        vm.isDisabled = true;
      }
    }

  };
})();
