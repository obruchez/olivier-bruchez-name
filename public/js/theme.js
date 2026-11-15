// Applies the theme chosen in the sidebar (light or dark) before the page is painted.
// Without a stored choice, the page follows the system setting.
(function () {
  try {
    var theme = localStorage.getItem("theme");
    if (theme === "light" || theme === "dark") document.documentElement.dataset.theme = theme;
  } catch (e) {
    // No storage (private mode, blocked): keep following the system setting
  }
})();
