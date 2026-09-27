(function () {
  "use strict";

  const REPORT_URL_REGEX = /https?:\/\/[^\s"'<>]+\/t\/report\/([a-zA-Z0-9_-]+)/i;
  const GENERIC_URL_REGEX = /https?:\/\/[^\s"'<>]+/gi;
  const REPORT_MARKER_REGEXES = [
    /<!--\s*ASAT_REPORT_UR[L]?:\s*(https?:\/\/[^\s"'<>]+)\s*-->/i,
    /ASAT_REPORT_UR[L]?:\s*(https?:\/\/[^\s"'<>]+)/i,
    /ASAT_REPORT_UR[L]?:\s*["']?(https?:\/\/[^"'<>\s]+)["']?/i,
    /data-asat-report-url=["']true["'][^>]*href=["'](https?:\/\/[^"']+)["']/i,
    /href=["'](https?:\/\/[^"']+)["'][^>]*data-asat-report-url=["']true["']/i
  ];
  const ADDIN_SOURCE = "outlook-addin";
  const ADDIN_VERSION = "1.1.0";

  function setStatus(message, isError) {
    const status = document.getElementById("status");
    status.textContent = message;
    status.style.color = isError ? "#b42318" : "#067647";
  }

  function setLoading(isLoading) {
    const btn = document.getElementById("reportBtn");
    btn.disabled = isLoading;
    btn.textContent = isLoading ? "Reporting..." : "Report Email";
  }

  function getAsyncValue(getter) {
    return new Promise((resolve) => {
      try {
        getter((result) => {
          if (result && result.status === Office.AsyncResultStatus.Succeeded) {
            resolve(result.value);
            return;
          }
          resolve(null);
        });
      } catch (error) {
        resolve(null);
      }
    });
  }

  function readBodyAsync() {
    return new Promise((resolve, reject) => {
      const item = Office.context.mailbox.item;
      item.body.getAsync(
        Office.CoercionType.Html,
        { asyncContext: null },
        (result) => {
          if (result.status !== Office.AsyncResultStatus.Succeeded) {
            reject(new Error(result.error ? result.error.message : "Failed to read mail body."));
            return;
          }
          resolve(result.value || "");
        }
      );
    });
  }

  function decodeHtmlEntities(text) {
    if (!text) return text;
    const textarea = document.createElement("textarea");
    textarea.innerHTML = text;
    return textarea.value;
  }

  function tryDecodeURIComponent(value) {
    try {
      return decodeURIComponent(value);
    } catch (error) {
      return value;
    }
  }

  function normalizeCandidate(value) {
    if (!value) return null;
    let normalized = value.trim();
    if (!normalized) return null;

    normalized = decodeHtmlEntities(normalized);
    normalized = normalized.replace(/=3D/gi, "=");
    normalized = normalized.replace(/=\r?\n/g, "");
    normalized = normalized.replace(/&amp;/gi, "&");
    normalized = tryDecodeURIComponent(normalized);
    return normalized;
  }

  function extractReportUrlFromCandidate(candidate) {
    const normalized = normalizeCandidate(candidate);
    if (!normalized) return null;

    const directMatch = normalized.match(REPORT_URL_REGEX);
    if (directMatch && directMatch[0]) {
      return directMatch[0];
    }

    try {
      const parsed = new URL(normalized);
      const wrappedUrl = parsed.searchParams.get("url");
      if (wrappedUrl) {
        const decodedWrapped = normalizeCandidate(wrappedUrl);
        const wrappedMatch = decodedWrapped && decodedWrapped.match(REPORT_URL_REGEX);
        if (wrappedMatch && wrappedMatch[0]) {
          return wrappedMatch[0];
        }
      }
    } catch (error) {
      // Not a parseable absolute URL; ignore.
    }

    return null;
  }

  function extractReportUrlFromMarker(mailHtml) {
    if (!mailHtml) return null;

    if (mailHtml.indexOf("data-asat-report-url=\"true\"") >= 0 || mailHtml.indexOf("ASAT_REPORT_UR") >= 0) {
      const anchorHrefRegex = /href=["'](https?:\/\/[^"']+)["']/gi;
      let hrefMatch;
      while ((hrefMatch = anchorHrefRegex.exec(mailHtml)) !== null) {
        const markerUrl = extractReportUrlFromCandidate(hrefMatch[1]);
        if (markerUrl) {
          return markerUrl;
        }
      }
    }

    for (let i = 0; i < REPORT_MARKER_REGEXES.length; i++) {
      const match = mailHtml.match(REPORT_MARKER_REGEXES[i]);
      if (match && match[1]) {
        const markerUrl = extractReportUrlFromCandidate(match[1]);
        if (markerUrl) {
          return markerUrl;
        }
      }
    }

    const decodedHtml = decodeHtmlEntities(mailHtml);
    if (decodedHtml && decodedHtml !== mailHtml) {
      for (let i = 0; i < REPORT_MARKER_REGEXES.length; i++) {
        const match = decodedHtml.match(REPORT_MARKER_REGEXES[i]);
        if (match && match[1]) {
          const markerUrl = extractReportUrlFromCandidate(match[1]);
          if (markerUrl) {
            return markerUrl;
          }
        }
      }
    }

    return null;
  }

  function extractReportUrl(mailHtml) {
    const markerUrl = extractReportUrlFromMarker(mailHtml);
    if (markerUrl) {
      return markerUrl;
    }

    const directMatch = extractReportUrlFromCandidate(mailHtml);
    if (directMatch) {
      return directMatch;
    }

    const urls = mailHtml.match(GENERIC_URL_REGEX) || [];
    for (let i = 0; i < urls.length; i++) {
      const candidate = extractReportUrlFromCandidate(urls[i]);
      if (candidate) {
        return candidate;
      }
    }

    return null;
  }

  function buildAddinReportUrl(reportUrl) {
    return reportUrl.replace(/\/t\/report\/([a-zA-Z0-9_-]+)$/i, "/t/report/$1/addin");
  }

  async function collectAddinMetadata(reportUrl) {
    const item = Office.context.mailbox.item;
    const fromEmail =
      item.from && item.from.emailAddress ? item.from.emailAddress : null;

    const internetHeaders = await getAsyncValue((callback) => {
      if (typeof item.getAllInternetHeadersAsync !== "function") {
        callback({ status: Office.AsyncResultStatus.Failed });
        return;
      }
      item.getAllInternetHeadersAsync(callback);
    });

    return {
      source: ADDIN_SOURCE,
      addinVersion: ADDIN_VERSION,
      reportUrl: reportUrl,
      reportedAt: new Date().toISOString(),
      itemId: item.itemId || null,
      internetMessageId: item.internetMessageId || null,
      subject: item.subject || null,
      from: fromEmail,
      userEmail:
        Office.context.mailbox &&
        Office.context.mailbox.userProfile &&
        Office.context.mailbox.userProfile.emailAddress
          ? Office.context.mailbox.userProfile.emailAddress
          : null,
      diagnostics:
        Office.context.diagnostics &&
        Office.context.diagnostics.hostVersion
          ? {
              hostName: Office.context.diagnostics.hostName || null,
              hostVersion: Office.context.diagnostics.hostVersion || null,
              platform: Office.context.diagnostics.platform || null
            }
          : null,
      headers: internetHeaders
    };
  }

  async function reportMail() {
    try {
      setLoading(true);
      setStatus("Checking email metadata...");

      const body = await readBodyAsync();
      const reportUrl = extractReportUrl(body);
      if (!reportUrl) {
        throw new Error("Tracking URL not found. Ensure campaign email includes {{REPORT_URL}}.");
      }

      const reportAddinUrl = buildAddinReportUrl(reportUrl);
      const metadata = await collectAddinMetadata(reportUrl);
      let response = await fetch(reportAddinUrl, {
        method: "POST",
        mode: "cors",
        credentials: "omit",
        headers: {
          "Content-Type": "application/json",
          "X-ASAT-Report-Source": ADDIN_SOURCE
        },
        body: JSON.stringify(metadata)
      });

      // Backward compatibility with environments that only support GET /t/report/{trackingId}.
      if (!response.ok && response.status === 404) {
        response = await fetch(reportUrl, {
          method: "GET",
          mode: "cors",
          credentials: "omit",
          headers: {
            "X-ASAT-Report-Source": ADDIN_SOURCE
          }
        });
      }

      if (!response.ok) {
        throw new Error("Tracking endpoint rejected request with status " + response.status);
      }

      const idMatch = reportUrl.match(REPORT_URL_REGEX);
      const trackingId = idMatch && idMatch[1] ? idMatch[1] : "unknown";
      setStatus(
        "Reported successfully. " +
          "\nYou can now delete this email."
      );
    } catch (error) {
      setStatus("Failed to report this email.\n" + error.message, true);
    } finally {
      setLoading(false);
    }
  }

  function bindUi() {
    const reportBtn = document.getElementById("reportBtn");
    if (reportBtn) {
      reportBtn.addEventListener("click", reportMail);
    }
  }

  if (typeof Office === "undefined") {
    setStatus(
      // "Office.js did not load (blocked network or CSP). Allow script-src https://appsforoffice.microsoft.com on the gateway.",
      "Office.js did not load. Check browser DevTools: CSP must allow scripts from Microsoft's Office CDN (deploy gateway outlook-addin CSP fix).",
      true
    );
    return;
  }
  /*Office.onReady(() => {
    const reportBtn = document.getElementById("reportBtn");
    reportBtn.addEventListener("click", reportMail);
  });*/
  try {
    var started = Office.onReady();
    if (started && typeof started.then === "function") {
      started.then(bindUi).catch(function (err) {
        setStatus(
          "Office failed to start: " + (err && err.message ? err.message : String(err)),
          true
        );
      });
    } else {
      Office.onReady(bindUi);
    }
  } catch (err) {
    setStatus("Office.onReady error: " + (err && err.message ? err.message : String(err)), true);
  }
})();
