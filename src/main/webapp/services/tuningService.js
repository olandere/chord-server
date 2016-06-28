(function() {
  'use strict';

  angular
    .module('chordApp')
    .factory('tuningService', tuningService);

  tuningService.$inject = ['$cookies'];

  function tuningService($cookies) {

    return {
      getTunings: getTunings,
      addTuning: addTuning
    };

    function getTunings() {
      return $cookies.getObject('tunings');
    }

    function addTuning(tuning) {
      var tunings = getTunings();
      if (_.isArray(tunings)) {
        tunings.push(tuning);
        $cookies.putObject('tunings', _.uniq(tunings));
      } else {
        $cookies.putObject('tunings', [tuning]);
      }
    }
  }
})();
