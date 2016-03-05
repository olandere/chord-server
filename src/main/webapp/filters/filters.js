'use strict';

angular.module('chordApp.filters', []).
  filter("transformSymbols", function () {
  return function(text) {
    return text.replace(/b/g, '\u266D').replace(/#/g, '\u266F');
  }
});