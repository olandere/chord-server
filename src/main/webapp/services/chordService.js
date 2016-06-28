(function() {
  'use strict';

  angular
    .module('chordApp')
    .factory('chordService', chordService);

  chordService.$inject = ['$http'];

  function chordService($http) {
    return {
      analyze: analyze,
      shellchord: shellchord,
      chords: chords
    };

    function analyze(fingering, tuning) {
      return doGet('/analyze/' + encodeURIComponent(fingering.trim()), fingering, tuning, false);
    }

    function shellchord(fretspan, chord, tuning, condense, jazz) {
      return doGet('/shellchord/' + fretspan, chord, tuning, condense, jazz);
    }

    function chords(fretspan, chord, tuning, condense, jazz) {
      return doGet('/chords/' + fretspan, chord, tuning, condense, jazz);
    }

    function doGet(url, chord, tuning, condense, jazz) {
      return $http.get(url, {
        params: {
          chord: encodeURIComponent(chord.trim()),
          tuning: tuning,
          condense: condense,
          jazz: jazz
        }
      })
    }
  }
})();
