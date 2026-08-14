// CSP-safe replacement for the former inline <script> block (JENKINS-74429).
// Opens/closes the release-history modals via event delegation.
(function () {
    "use strict";

    function closeModal(modal) {
        modal.style.display = "none";
    }

    document.addEventListener("click", function (event) {
        var toggle = event.target.closest(".edb-popup-toggle");
        if (toggle) {
            event.preventDefault();
            var modal = document.getElementById(toggle.getAttribute("data-popup-id"));
            if (modal) {
                modal.style.display = "block";
            }
            return;
        }

        var closeButton = event.target.closest(".modal .close");
        if (closeButton) {
            closeModal(closeButton.closest(".modal"));
            return;
        }

        if (event.target.classList && event.target.classList.contains("modal")) {
            closeModal(event.target);
        }
    });
})();
