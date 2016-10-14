function loadCertifiedTable() {
	$("#certified-table").empty();
	$("#status-loading").show();
	$.ajax({
	    url: "CertificationServlet",
	    data: {
	        request: "getcertified"
	    },
	    type: "GET",
	    dataType : "json",
	    success: function( json ) {
	    	$("#status-loading").hide();
	    	fillCertifiedTable(json);
	    },
	    error: function( xhr, status, errorThrown ) {
	    	$("#submission-status-loading").hide();
	    	handleError( xhr, status, errorThrown);
	    }
	});
}

function fillCertifiedTable(submissions) {
	$("#certified-table").empty();
	var html = '<tr><th>Organization</th><th>Contact Name</th><th>Contact Email</th></tr>\n';
	for (var i = 0; i < submissions.length; i++) {
		if (submissions[i].approved) {
			html += '<tr id="submission-';
			html += submissions[i].id;
			html += '"></td><td class="organization_col">';
			html += submissions[i].user.organization;
			html += '</td><td class="username_col">';
			html += submissions[i].user.name;
			html += '</td><td class="email_col">';
			html += submissions[i].user.email;		
			html += '</td></tr>\n';
		}
	}
	$("#certified-table").html(html);
}

$(document).ready( function() {
	loadCertifiedTable();
});