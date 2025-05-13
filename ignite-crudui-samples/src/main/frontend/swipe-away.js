import "@polymer/polymer/lib/mixins/gesture-event-listeners.js";
import { addListener as addGestureListener } from "@polymer/polymer/lib/utils/gestures.js";

const trackElement = function(trackElement, menuElement, callback) {
  addGestureListener(trackElement, "track", e => {
    if (window.innerWidth > 1024) {
      // CSS defines a hideable menu only used for smaller screens
      return;
    }
    if (e.detail.state == "start") {
      menuElement.origTransition = menuElement.style.transition;
      menuElement.style.transition = "none";
      menuElement.origMarginLeft = menuElement.style.marginLeft;
      menuElement.style.marginLeft = "0px";
    }
    if (e.detail.state == "track") {
      const marginLeft = Math.min(0, e.detail.dx);
      menuElement.style.marginLeft = marginLeft + "px";
    }

    if (e.detail.state == "end") {
      menuElement.style.transition = menuElement.origTransition;
      delete menuElement.origTransition;
      menuElement.style.marginLeft = menuElement.origMarginLeft;
      delete menuElement.origMarginLeft;

      if (e.detail.dx < -50) {
        callback(e.detail);
      }
    }
  });
};

window.addSwipeAway = function(
  menuElement,
  callbackContainer,
  callbackMethod,
  additionalElement
) {
  trackElement(menuElement, menuElement, function(details) {
    callbackContainer.$server[callbackMethod](details);
  });
  trackElement(additionalElement, menuElement, function(details) {
    callbackContainer.$server[callbackMethod](details);
  });
};
