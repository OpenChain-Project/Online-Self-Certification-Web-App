/**
 * Copyright (c) 2016 Source Auditor Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
*/

function openDownloadSurveyDialog() {
	$("#downloadsurvey").dialog("open");
}

function openUploadSurveyDialog() {
	displayError("This uploading survey has not yet been implemented.");
}

function downloadSurvey() {
	var specVersion = $("#downloadsurvey-version").val();
	window.location="CertificationServlet?request=downloadSurvey&specVersion="+specVersion;
}

function fillSubmissionStatusTable(submissions) {
	$("#submission-status").empty();
	var html = '<tr><th>User Name</th><th>Organization</th><th>Email</th><th>Submitted</th><th>% Complete</th><th>Score</th></tr>\n';
	for (var i = 0; i < submissions.length; i++) {
		html += '<tr id="submission-';
		html += submissions[i].user.username;
		html += '"  class="username_col"><td>';
		html += submissions[i].user.username;
		html += '</td><td class="organization_col">';
		html += submissions[i].user.organization;
		html += '</td><td class="email_col">';
		html += submissions[i].user.email;
		html += '</td><td class="submitted_col">';
		html += submissions[i].submitted;
		html += '</td><td class="completed_col">';
		html += submissions[i].percentComplete;
		html += '</td><td class="score_col">';
		html += submissions[i].score;
		html += '</td></tr>\n';
	}
	$("#submission-status").html(html);
}

$(document).ready( function() {
	$("#downloadsurvey").dialog({
		title: "Download Survey",
		autoOpen: false,
		height: 300,
		width: 350,
		modal: true,
		buttons: [{
			text: "Download",
			click: function() {
				$(this).dialog("close");
				downloadSurvey();
			}
		}, {
			text: "Cancel",
			click: function() {
				$(this).dialog("close");
			}
		}]
	}).find("form").on("submit", function(event) {
		event.preventDefault();
		downloadSurvey();
	});
	
	$("#btDownloadSurvey").button().click(function(event) {
	      event.preventDefault();
	      openDownloadSurveyDialog();
	});
	$("#btUploadSurvey").button().button().click(function(event) {
	      event.preventDefault();
	      openUploadSurveyDialog();
	});
	
	$.ajax({
	    url: "CertificationServlet",
	    data: {
	        request: "version"
	    },
	    type: "GET",
	    dataType : "json",
	    success: function( json ) {
	    	$("#software-version").text(json);
	    },
	    error: function( xhr, status, errorThrown ) {
	    	handleError( xhr, status, errorThrown);
	    }
	});
	
	$.ajax({
	    url: "CertificationServlet",
	    data: {
	        request: "getsubmissions"
	    },
	    type: "GET",
	    dataType : "json",
	    success: function( json ) {
	    	fillSubmissionStatusTable(json);
	    },
	    error: function( xhr, status, errorThrown ) {
	    	handleError( xhr, status, errorThrown);
	    }
	});
});