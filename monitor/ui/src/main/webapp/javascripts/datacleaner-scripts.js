/**
 * Bootstrap-friendly file upload widget. See
 * http://www.abeautifulsite.net/whipping-file-inputs-into-shape-with-bootstrap-3/
 */

function applyFileSelectEventHandler(selector) {
	selector.find('.btn-file :file').each(function(index) {
		$(this).unbind('fileselect');
		$(this).on('fileselect', function(event, numFiles, label) {
			var input = $(this).parents('.input-group').find(':text');
			var log = numFiles > 1 ? numFiles + ' files selected' : label;

			if (input.length) {
				input.val(log);
			} else {
				if (log) {
					alert(log);
				}
			}
		});
	});
}

$(document).on('change', '.btn-file :file', function() {
	var input = $(this);
	var numFiles = input.get(0).files ? input.get(0).files.length : 1;
	var label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
	input.trigger('fileselect', [ numFiles, label ]);
});

$(document).ready(function() {
	applyFileSelectEventHandler($(document));
});