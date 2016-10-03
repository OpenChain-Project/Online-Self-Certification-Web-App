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
	$("#uploadsurvey").dialog("open");
}

function openUpdateSurveyDialog() {
	$("#updatesurvey").dialog("open");
}

function downloadSurvey() {
	var specVersion = $("#downloadsurvey-version").val();
	window.location="CertificationServlet?request=downloadSurvey&specVersion="+specVersion;
}

var csvFileForUpload;
var csvFileForUpdate;
var storeCsvFileUpload = function(event) {
	csvFileForUpload = event.target.files[0];
};
var storeCsvFileUpdate = function(event) {
	csvFileForUpdate = event.target.files[0];
};

function updateSurvey() {
	$("#btUpdateSurvey").button("disable");
	var specVersion = $("#updatesurvey-version").val();
	if (csvFileForUpdate == null) {
		dislplayError("No file choosen for update");
		return;
	}
	var csvLines;
	var reader = new FileReader();
	reader.onload = function(){
		var text = reader.result;
		csvLines = text.split('\n');
		$.ajax({
		    url: "CertificationServlet",
		    data:JSON.stringify({
		        request: "updatesurvey",
		        specVersion: specVersion,
		        csvLines: csvLines
		    }),
		    type: "POST",
		    dataType : "json",
		    contentType: "json",
		    success: function( json ) {
		    	$("#btUpdateSurvey").button("enable");
		    	if (json.status == "OK") {
		    		$( "#status" ).dialog({
		    			title: "Updated",
		    			resizable: false,
		    		    height: 200,
		    		    width: 200,
		    		    modal: true,
		    		    buttons: {
		    		        "Ok" : function () {
		    		            $( this ).dialog( "close" );
		    		        }
		    		    }
		    		}).text( "Questions successfully updated" );
		    	} else {
		    		displayError(json.error);
		    	}
		    },
		    error: function( xhr, status, errorThrown ) {
		    	$("#btUpdateSurvey").button("enable");
		    	handleError( xhr, status, errorThrown);
		    }
		});
	};
	reader.onloadend = function(){
		if (reader.error != null) {
			displayError(reader.error.message);
		}
	};
	reader.readAsText(csvFileForUpdate);
}


function uploadSurvey() {
	$("#btUploadSurvey").button("disable");
	var specVersion = $("#uploadsurvey-version").val();
	var sectionTexts = [];
	$("#sectiontext-input-area").find(".section-text-group").each(function(index){
		var id = $(this).attr('id');
		var sectionName = $("#"+id+"-sname").val();
		var sectionTitle = $("#"+id+"-stitle").val();
		if (sectionName != null && sectionName.trim().length > 0) {
			sectionTexts.push({name:sectionName, title:sectionTitle});
		}
	});
	if (csvFileForUpload == null) {
		dislplayError("No file choosen for upload");
		return;
	}
	var csvLines;
	var reader = new FileReader();
	reader.onload = function(){
		var text = reader.result;
		csvLines = text.split('\n');
		$.ajax({
		    url: "CertificationServlet",
		    data:JSON.stringify({
		        request: "uploadsurvey",
		        specVersion: specVersion,
		        csvLines: csvLines,
		        sectionTexts: sectionTexts
		    }),
		    type: "POST",
		    dataType : "json",
		    contentType: "json",
		    success: function( json ) {
		    	$("#btUploadSurvey").button("enable");
		    	if (json.status == "OK") {
		    		$( "#status" ).dialog({
		    			title: "Uploaded",
		    			resizable: false,
		    		    height: 200,
		    		    width: 200,
		    		    modal: true,
		    		    buttons: {
		    		        "Ok" : function () {
		    		            $( this ).dialog( "close" );
		    		        }
		    		    }
		    		}).text( "Questions successfully uploaded" );
		    	} else {
		    		displayError(json.error);
		    	}
		    },
		    error: function( xhr, status, errorThrown ) {
		    	$("#btUploadSurvey").button("enable");
		    	handleError( xhr, status, errorThrown);
		    }
		});
	};
	reader.onloadend = function(){
		if (reader.error != null) {
			displayError(reader.error.message);
		}
	};
	reader.readAsText(csvFileForUpload);
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
	
	$("#uploadsurvey").dialog({
		title: "Upload Survey",
		autoOpen: false,
		height: 600,
		width: 550,
		modal: true,
		buttons: [{
			text: "Upload",
			click: function() {
				$(this).dialog("close");
				uploadSurvey();
			}
		}, {
			text: "Cancel",
			click: function() {
				$(this).dialog("close");
			}
		}]
	}).find("form").on("submit", function(event) {
		event.preventDefault();
		uploadSurvey();
	});
	
	$("#updatesurvey").dialog({
		title: "Update Survey Questions",
		autoOpen: false,
		height: 300,
		width: 350,
		modal: true,
		buttons: [{
			text: "Update",
			click: function() {
				$(this).dialog("close");
				updateSurvey();
			}
		}, {
			text: "Cancel",
			click: function() {
				$(this).dialog("close");
			}
		}]
	}).find("form").on("submit", function(event) {
		event.preventDefault();
		updateSurvey();
	});
	
	$("#btDownloadSurvey").button().click(function(event) {
	      event.preventDefault();
	      openDownloadSurveyDialog();
	});
	$("#btUploadSurvey").button().button().click(function(event) {
	      event.preventDefault();
	      openUploadSurveyDialog();
	});
	
	$("#btUpdateSurvey").button().button().click(function(event) {
	      event.preventDefault();
	      openUpdateSurveyDialog();
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
	
	$("#submission-status-loading").show();
	$.ajax({
	    url: "CertificationServlet",
	    data: {
	        request: "getsubmissions"
	    },
	    type: "GET",
	    dataType : "json",
	    success: function( json ) {
	    	$("#submission-status-loading").hide();
	    	fillSubmissionStatusTable(json);
	    },
	    error: function( xhr, status, errorThrown ) {
	    	$("#submission-status-loading").hide();
	    	handleError( xhr, status, errorThrown);
	    }
	});
});