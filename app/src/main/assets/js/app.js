/* ========================================
   LocalHub - Dashboard Logic
   ======================================== */

(function () {
    'use strict';

    let requestCount = 0;
    let startTime = Date.now();

    // Update uptime every second
    setInterval(updateUptime, 1000);

    // Try to load file listing from server
    loadFileList();

    // Increment request counter (page views)
    requestCount++;
    document.getElementById('requestCount').textContent = requestCount;

    // Set connection type
    updateConnectionType();

    function updateUptime() {
        const elapsed = Math.floor((Date.now() - startTime) / 1000);
        const h = Math.floor(elapsed / 3600);
        const m = Math.floor((elapsed % 3600) / 60);
        const s = elapsed % 60;
        const pad = n => String(n).padStart(2, '0');
        const text = h > 0 ? `${h}h ${pad(m)}m` : m > 0 ? `${m}m ${pad(s)}s` : `${s}s`;
        document.getElementById('uptimeTag').textContent = 'Uptime: ' + text;
    }

    function updateConnectionType() {
        const el = document.getElementById('connectionType');
        if (!navigator.onLine) {
            el.textContent = 'Offline';
            el.style.color = 'var(--red)';
        } else if (location.hostname === 'localhost' || location.hostname === '127.0.0.1') {
            el.textContent = 'Local';
            el.style.color = 'var(--green)';
        } else {
            el.textContent = 'Network';
            el.style.color = 'var(--orange)';
        }
    }

    function loadFileList() {
        const container = document.getElementById('fileList');
        const folderEl = document.getElementById('folderName');
        const fileCountEl = document.getElementById('fileCount');

        fetch('/')
            .then(function (res) {
                // Check if there's an index.html (we're serving content)
                return res.text();
            })
            .then(function () {
                // Try to get directory listing from a special endpoint or parse links
                return fetch('/?list=true').catch(function () {
                    return null;
                });
            })
            .catch(function () {
                return null;
            });

        // Generate a basic file list from the page itself
        // Since the server serves static files, we show a helpful placeholder
        setTimeout(function () {
            container.innerHTML = '<div class="file-empty">' +
                '<svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" opacity="0.3">' +
                '<path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/>' +
                '</svg>' +
                '<p>Serving files from local storage</p>' +
                '<p style="font-size:11px;margin-top:2px">Navigate to your project files via the URL bar</p>' +
                '</div>';
            fileCountEl.textContent = 'Active';
            folderEl.textContent = 'Local';
        }, 500);
    }

    // Global functions
    window.copyUrl = function () {
        var url = location.href;
        if (navigator.clipboard) {
            navigator.clipboard.writeText(url).then(function () {
                showToast('URL copied!');
            });
        } else {
            // Fallback
            var input = document.createElement('input');
            input.value = url;
            document.body.appendChild(input);
            input.select();
            document.execCommand('copy');
            document.body.removeChild(input);
            showToast('URL copied!');
        }
    };

    window.refreshFiles = function () {
        showToast('Refreshing...');
        loadFileList();
    };

    function showToast(message) {
        var toast = document.getElementById('toast');
        toast.textContent = message;
        toast.classList.add('show');
        setTimeout(function () {
            toast.classList.remove('show');
        }, 2000);
    }

    // Online/offline detection
    window.addEventListener('online', function () {
        document.getElementById('statusBadge').classList.remove('offline');
        document.getElementById('statusText').textContent = 'Active';
        updateConnectionType();
    });

    window.addEventListener('offline', function () {
        document.getElementById('statusBadge').classList.add('offline');
        document.getElementById('statusText').textContent = 'Offline';
        updateConnectionType();
    });

})();
