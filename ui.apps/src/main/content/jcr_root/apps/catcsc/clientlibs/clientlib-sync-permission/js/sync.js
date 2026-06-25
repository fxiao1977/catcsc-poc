(function ($, $document) {
    "use strict";

    function showDialog(title, message) {
        // Remove any existing dialog
        $("#sync-permission-dialog").remove();

        var dialogHtml = `
            <coral-dialog id="sync-permission-dialog" closable>
                <coral-dialog-header>${title}</coral-dialog-header>
                <coral-dialog-content>
                    <p>${message}</p>
                </coral-dialog-content>
                <coral-dialog-footer>
                    <button is="coral-button" variant="primary" coral-close>OK</button>
                </coral-dialog-footer>
            </coral-dialog>
        `;

        var $dialog = $(dialogHtml).appendTo("body");

        // IMPORTANT: upgrade Coral component before calling show()
        Coral.commons.ready(function () {
            var dialog = document.querySelector("#sync-permission-dialog");
            if (dialog && dialog.show) {
                dialog.show();
            } else {
                // fallback if still not upgraded
                console.warn("Dialog not upgraded, forcing upgrade");
                Coral.commons.ready(dialog);
                dialog.show();
            }
        });
    }

    $document.on("click", ".sync-permission-to-workfront", function () {

        var $selectedItems = $(".foundation-collection-item[selected]");

        if ($selectedItems.length === 0) {
            showDialog("Sync Permission", "Please select at least one folder.");
            return;
        }

        var folderPaths = [];

        $selectedItems.each(function () {
            var $item = $(this);

            var $meta = $item.find(".foundation-collection-assets-meta");
            var metaType = $meta.data("foundationCollectionMetaType");

            if (metaType === "directory") {
                var path = $item.data("foundationCollectionItemId");
                if (path) {
                    folderPaths.push(path);
                }
            }
        });

        if (folderPaths.length === 0) {
            showDialog("Sync Permission", "No folders selected.");
            return;
        }

        $.ajax({
            url: "/bin/catcsc/syncWorkfrontPermissions",
            type: "POST",
            traditional: true,
            dataType: "json",
            data: {
                folders: folderPaths
            },
            success: function (response) {
                var msg = response && response.message
                    ? response.message
                    : "Sync completed.";

                showDialog("Sync Permission to Workfront IDs", msg);
            },
            error: function (xhr) {
                var msg = "Failed to sync permissions.";

                if (xhr && xhr.responseText) {
                    try {
                        var json = JSON.parse(xhr.responseText);
                        if (json.message) {
                            msg = json.message;
                        }
                    } catch (e) {}
                }

                showDialog("Sync Permission to Workfront IDs", msg);
            }
        });
    });

})(Granite.$, Granite.$(document));
