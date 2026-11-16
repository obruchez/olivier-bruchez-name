// olivier.bruchez.name: list filtering and statistics charts. No dependencies besides Chart.js on the
// statistics page.

(function () {
  "use strict";

  // ---------- List filter ----------

  function setUpListFilter() {
    var input = document.getElementById("list-filter");
    if (!input) return;

    var groups = Array.prototype.map.call(document.querySelectorAll(".year-group"), function (group) {
      return {
        element: group,
        count: group.querySelector("[data-count]"),
        entries: Array.prototype.map.call(group.querySelectorAll(".entry"), function (entry) {
          return { element: entry, text: normalize(entry.textContent) };
        })
      };
    });

    var total = document.querySelector("[data-total]");
    var initialTotal = total ? total.textContent : "";
    var empty = document.querySelector(".list-empty");
    var yearLinks = document.querySelectorAll(".year-nav a");

    function apply() {
      var words = normalize(input.value).split(" ").filter(Boolean);
      var shownTotal = 0;
      var shownGroups = {};

      groups.forEach(function (group) {
        var shown = 0;

        group.entries.forEach(function (entry) {
          var matches = words.every(function (word) {
            return entry.text.indexOf(word) >= 0;
          });
          entry.element.hidden = !matches;
          if (matches) shown++;
        });

        group.element.hidden = shown === 0;
        shownGroups[group.element.id] = shown > 0;

        // "Next" items (currently reading, upcoming...) are not part of the totals
        if (group.count) {
          group.count.textContent = shown;
          shownTotal += shown;
        }
      });

      if (total) total.textContent = words.length ? shownTotal : initialTotal;
      if (empty) empty.hidden = groups.some(function (group) { return !group.element.hidden; });

      Array.prototype.forEach.call(yearLinks, function (link) {
        link.hidden = !shownGroups[decodeURIComponent(link.hash.slice(1))];
      });
    }

    var pending = null;
    input.addEventListener("input", function () {
      if (pending) cancelAnimationFrame(pending);
      pending = requestAnimationFrame(apply);
    });
  }

  // Lower case, without accents, single spaces: "Béjart  Ballet" matches "bejart ballet"
  function normalize(text) {
    return text
      .toLowerCase()
      .normalize("NFD")
      .replace(/[̀-ͯ]/g, "")
      .replace(/\s+/g, " ")
      .trim();
  }

  // ---------- Charts ----------

  var charts = [];

  function cssVariable(name) {
    return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
  }

  function withAlpha(hex, alpha) {
    var value = parseInt(hex.slice(1), 16);
    return "rgba(" + (value >> 16) + ", " + ((value >> 8) & 255) + ", " + (value & 255) + ", " + alpha + ")";
  }

  function drawCharts() {
    if (typeof Chart === "undefined") return;

    charts.forEach(function (chart) { chart.destroy(); });
    charts = [];

    var accent = cssVariable("--accent");
    var ink = cssVariable("--ink");
    var muted = cssVariable("--muted");
    var rule = cssVariable("--rule");
    var paper = cssVariable("--paper");

    Chart.defaults.font.family = cssVariable("--sans");
    Chart.defaults.font.size = 12;
    Chart.defaults.color = muted;
    Chart.defaults.animation = false;

    Array.prototype.forEach.call(document.querySelectorAll("canvas.chart"), function (canvas) {
      var labels = JSON.parse(canvas.dataset.labels);
      var series = JSON.parse(canvas.dataset.series);
      var isRange = canvas.dataset.kind === "range";

      var scales = {
        x: {
          grid: { display: false },
          border: { color: rule },
          ticks: { maxRotation: 0, autoSkip: true, autoSkipPadding: 12 }
        },
        y: {
          beginAtZero: true,
          grid: { color: rule },
          border: { display: false },
          ticks: { precision: 0, maxTicksLimit: 6 }
        }
      };

      var tooltip = {
        backgroundColor: ink,
        titleColor: paper,
        bodyColor: paper,
        padding: 10,
        cornerRadius: 4,
        displayColors: false
      };

      var config;

      if (isRange) {
        // Yearly average as a line, lowest-to-highest as a band behind it
        scales.y.max = 5;
        scales.y.ticks.precision = undefined;

        config = {
          type: "line",
          data: {
            labels: labels,
            datasets: [
              {
                label: series[0].name,
                data: series[0].values,
                borderColor: accent,
                backgroundColor: accent,
                borderWidth: 2,
                pointRadius: labels.length > 1 ? 0 : 4,
                pointHoverRadius: 5,
                pointHitRadius: 16,
                tension: 0.25
              },
              {
                label: series[1].name,
                data: series[1].values,
                borderWidth: 0,
                pointRadius: 0,
                pointHitRadius: 0,
                fill: "+1",
                backgroundColor: withAlpha(accent, 0.16),
                tension: 0.25
              },
              {
                label: series[2].name,
                data: series[2].values,
                borderWidth: 0,
                pointRadius: 0,
                pointHitRadius: 0,
                tension: 0.25
              }
            ]
          },
          options: {
            maintainAspectRatio: false,
            interaction: { mode: "index", intersect: false },
            scales: scales,
            plugins: {
              legend: {
                position: "bottom",
                align: "start",
                labels: {
                  boxWidth: 14,
                  boxHeight: 8,
                  filter: function (item) { return item.datasetIndex < 2; },
                  generateLabels: function (chart) {
                    var items = Chart.defaults.plugins.legend.labels.generateLabels(chart);
                    items[0].fillStyle = accent;
                    items[0].strokeStyle = accent;
                    items[1].text = "Lowest to highest";
                    items[1].lineWidth = 0;
                    return items;
                  }
                },
                onClick: function () {}
              },
              tooltip: Object.assign({}, tooltip, {
                callbacks: {
                  label: function (context) {
                    return context.dataset.label + ": " + Math.round(context.parsed.y * 100) / 100;
                  }
                }
              })
            }
          }
        };
      } else {
        config = {
          type: "bar",
          data: {
            labels: labels,
            datasets: [
              {
                label: series[0].name,
                data: series[0].values,
                backgroundColor: accent,
                hoverBackgroundColor: ink,
                borderRadius: { topLeft: 3, topRight: 3 },
                categoryPercentage: 0.8,
                barPercentage: 0.85,
                maxBarThickness: 28
              }
            ]
          },
          options: {
            maintainAspectRatio: false,
            interaction: { mode: "index", intersect: false },
            scales: scales,
            plugins: { legend: { display: false }, tooltip: tooltip }
          }
        };
      }

      charts.push(new Chart(canvas, config));
    });
  }

  // Theme switch of the sidebar: follow the system ("auto", the default), or force light or dark.
  // The stored choice is applied early by theme.js; here we only handle the buttons.
  function setUpThemeSwitch() {
    var box = document.querySelector(".theme-switch");
    if (!box) return;

    var buttons = box.querySelectorAll("button");

    function show(choice) {
      buttons.forEach(function (button) {
        button.setAttribute("aria-pressed", button.dataset.themeChoice === choice);
      });
    }

    buttons.forEach(function (button) {
      button.addEventListener("click", function () {
        var choice = button.dataset.themeChoice;
        if (choice === "auto") delete document.documentElement.dataset.theme;
        else document.documentElement.dataset.theme = choice;
        try {
          if (choice === "auto") localStorage.removeItem("theme");
          else localStorage.setItem("theme", choice);
        } catch (e) {
          // No storage: the choice only lasts for this page
        }
        show(choice);
        drawCharts();
      });
    });

    show(document.documentElement.dataset.theme || "auto");
    box.hidden = false;
  }

  // A cover whose file is missing is replaced by the same placeholder as entries without cover
  function replaceBrokenCovers() {
    function replace(image) {
      var placeholder = document.createElement("span");
      placeholder.className = "entry-cover entry-cover-none";
      placeholder.setAttribute("aria-hidden", "true");
      placeholder.innerHTML =
        '<svg class="list-icon" viewBox="0 0 24 24"><use href="#list-icon"/></svg>';
      image.replaceWith(placeholder);
    }

    document.addEventListener(
      "error",
      function (event) {
        if (event.target.matches && event.target.matches("img.entry-cover")) replace(event.target);
      },
      true
    );

    document.querySelectorAll("img.entry-cover").forEach(function (image) {
      if (image.complete && image.naturalWidth === 0) replace(image);
    });
  }

  // A playlist: an audio element followed by a list of links to audio files. A click on a track plays
  // it in the player, and the next one follows when it ends. Without this script the links still work.
  function setUpPlaylists() {
    document.querySelectorAll(".playlist").forEach(function (playlist) {
      var audio = playlist.querySelector("audio");
      var links = Array.prototype.slice.call(playlist.querySelectorAll("ol a"));
      if (!audio || links.length === 0) return;

      function select(index, play) {
        links.forEach(function (link, i) {
          link.parentNode.classList.toggle("playing", i === index);
        });
        audio.src = links[index].href;
        if (play) audio.play();
      }

      links.forEach(function (link, index) {
        link.addEventListener("click", function (event) {
          event.preventDefault();
          select(index, true);
        });
      });

      audio.addEventListener("ended", function () {
        var next =
          links.findIndex(function (link) {
            return link.parentNode.classList.contains("playing");
          }) + 1;
        if (next < links.length) select(next, true);
      });

      select(0, false);
    });
  }

  // Comments (Disqus) are loaded only when asked for: nothing is sent to Disqus before that.
  function setUpComments() {
    var comments = document.getElementById("comments");
    var button = comments && comments.querySelector(".comments-show");
    if (!button) return;

    var loaded = false;

    function load() {
      if (loaded) return;
      loaded = true;
      button.remove();
      window.disqus_config = function () {
        this.page.url = comments.dataset.url;
        this.page.title = comments.dataset.title;
      };
      var script = document.createElement("script");
      script.src = "https://" + encodeURIComponent(comments.dataset.shortname) + ".disqus.com/embed.js";
      script.setAttribute("data-timestamp", String(Date.now()));
      document.head.appendChild(script);
    }

    button.addEventListener("click", load);
    // A link to a comment or to the comments opens them directly
    if (/^#(comment|disqus_thread)/.test(window.location.hash)) load();

    // Otherwise they load by themselves when the reader gets near the end of the post
    if ("IntersectionObserver" in window) {
      var observer = new IntersectionObserver(
        function (entries) {
          if (entries.some(function (entry) { return entry.isIntersecting; })) {
            observer.disconnect();
            load();
          }
        },
        { rootMargin: "800px 0px" }
      );
      observer.observe(comments);
    }
  }

  document.addEventListener("DOMContentLoaded", function () {
    replaceBrokenCovers();
    setUpComments();
    setUpPlaylists();
    setUpListFilter();
    setUpThemeSwitch();
    drawCharts();

    // Charts take their colours from the page, so follow light/dark changes
    if (window.matchMedia) {
      window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", drawCharts);
    }
  });
})();
