(function() {
  'use strict';

  angular
    .module('chordApp')
    .controller('ChordListCtrl', chordListController);

  chordListController.$inject = ['tuningService', 'usSpinnerService', '$filter', 'chordService'];

  function chordListController(tuningService, usSpinnerService, $filter, chordService) {

    var vm = this;

    vm.myData = {};
    vm.isDisabled = true;
    vm.myData.jazz = false;

    vm.changeTuning = function(tuning) {
      vm.myData.tuning = $filter('transformSymbols')(tuning);
    };

    vm.myData.doClick = function(item, event) {
      usSpinnerService.spin('spinner-1');
      var responsePromise;

      vm.isDisabled = true;
      vm.myData.chords = [];

      // store tuning
      var tuning = vm.myData.tuning || 'EADGBE';
      tuningService.addTuning($filter('capitalize')($filter('transformSymbols')(tuning)));

      var firstChar = vm.myData.chord.trim().charAt(0);
      if (firstChar.match(/[abcdefgrs]/i)) {
        if (vm.myData.shell) {
          responsePromise = chordService.shellchord(vm.myData.fretSpan || 4, vm.myData.chord, tuning,
            vm.myData.condense, vm.myData.jazz);
        } else {
          responsePromise = chordService.chords(vm.myData.fretSpan || 4, vm.myData.chord, tuning,
            vm.myData.condense, vm.myData.jazz);
        }
      } else {
        responsePromise = chordService.analyze(vm.myData.chord, tuning, false)
      }

      responsePromise.then(function successCallback(response) {
        vm.myData.chords = response.data;
        usSpinnerService.stop('spinner-1');
        vm.isDisabled = false;
      }, function errorCallback(response) {
        if (response.status === -1) {
          alert('Unable to reach server');
        } else {
          alert('Unable to parse ' + response.config.params.chord);
        }
        usSpinnerService.stop('spinner-1');
        vm.isDisabled = false;
      });
    };

    vm.onChange = function() {
      if (!_.isUndefined(vm.myData.chord)) {
        vm.myData.chord = $filter('transformSymbols')(vm.myData.chord);
        vm.isDisabled = false;
      } else {
        vm.isDisabled = true;
      }
    }

  }
})();
