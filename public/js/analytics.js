// Google Analytics 4. Loaded only when a measurement ID is configured (analytics.measurementid);
// the ID comes from the data attribute of this script's tag, because the CSP forbids inline scripts.
(function () {
  "use strict";

  var id = document.currentScript && document.currentScript.dataset.measurementId;
  if (!id) return;

  window.dataLayer = window.dataLayer || [];
  window.gtag = function () {
    window.dataLayer.push(arguments);
  };
  window.gtag("js", new Date());
  window.gtag("config", id);

  var script = document.createElement("script");
  script.async = true;
  script.src = "https://www.googletagmanager.com/gtag/js?id=" + encodeURIComponent(id);
  document.head.appendChild(script);
})();
