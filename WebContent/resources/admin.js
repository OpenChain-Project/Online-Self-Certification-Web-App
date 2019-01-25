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
/**
 * Functions added by visolve
 * 001: Pagination for Table. Note: Include DataTable library files
 */
var lastUpdateCommit; // Holds the last survey update commit hash


/**
 * On initial click of the update survey button, opens a dialog confirming you want to "updateSurveyForReal"
 */
function updateSurvey() {
	
	var tag = $( "#gitTag" ).val();
	if ( tag == "None" ) {
		tag = null;
	}
	var commit = $( "#gitCommit" ).val();
	if ( !commit || commit.trim() === "" ) {
		commit = null;
	}
	$( "#btUpdateSurvey" ).button( "disable" );
	$.ajax({
	    url: "CertificationServlet",
	    data: {
	        request: "getUpdateSurveyResults",
	        tag: tag,
	        commit: commit
	    },
	    type: "GET",
	    dataType : "json",
	    success: function( json ) {
	    	lastUpdateCommit = json.commit;
	    	var summaryHtml = "";
	    	if ( json.warnings.length > 0 ) {
	    		summaryHtml += "<h3 class='translate' data-i18n='Warning'> Warning <span>: </span> </h3>\n";
	    		for (i in json.warnings) {
	    			summaryHtml += "<p>";
	    			summaryHtml += json.warnings[i];
	    			summaryHtml += "</p>\n";
	    		}
	    	}
    		summaryHtml += "<p> ";
    		summaryHtml += json.versionsUpdated.length.toString();
    		summaryHtml += "<span class='translate' data-i18n='Summary-history-update'> questionnaire version/languages will be updated</p>\n";
    		summaryHtml += json.versionsAdded.length.toString();
    		summaryHtml += "<span class='translate' data-i18n='Summary-history-add'> questionnaire version/languages will be added</p>\n";
	    	$("#confirm-update-tab-summary").html(summaryHtml);
	    	var updatedHtml = "";
	    	if ( json.versionsUpdated.length == 0 ) {
	    		updatedHtml = "<span class='translate' data-i18n='no-update'>No questionnaire/versions were updated</span>";
	    	} else {
	    		for ( i in json.versionsUpdated ) {
	    			updatedHtml += "<p>";
	    			updatedHtml += json.versionsUpdated[i];
	    			updatedHtml += "</p>\n";
	    		}
	    	}
	    	$( "#confirm-update-tab-updates" ).html( updatedHtml );
	    	var addedHtml = "";
	    	if ( json.versionsAdded.length == 0 ) {
	    		addedHtml = "<span class='translate' data-i18n='no-new-add'> No questionnaire/versions were added </span>";
	    	} else {
	    		for ( i in json.versionsAdded ) {
	    			addedHtml += "<p>";
	    			addedHtml += json.versionsAdded[i];
	    			addedHtml += "</p>\n";
	    		}
	    	}
	    	$( "#confirm-update-tab-added" ).html( addedHtml );
	    	$( "#confirm-update-tabs" ).tabs();
	    	$( "#dialog-confirm-update" ).dialog( "open" );
	    },
	    error: function( xhr, status, errorThrown ) {
	    	$( "#btUpdateSurvey" ).button( "enable" );
	    	handleError( xhr, status, errorThrown );
	    }
	});
}

/**
 * Update the survey including an update to the database
 */
function updateSurveyForReal( commit ) {
	$( "#btUpdateSurvey" ).button( "disable" );
	$.ajax({
	    url: "CertificationServlet",
	    data:JSON.stringify({
	        request: "updatesurvey",
	        tag: null,
	        commit: lastUpdateCommit
	    }),
	    type: "POST",
	    dataType : "json",
	    contentType: "json",
	    success: function( json ) {
	    	$( "#btUpdateSurvey" ).button( "enable" );
	    	if ( json.status == "OK" ) {
	    		var dialogText = json.surveyUpdateResult.versionsUpdated.length.toString();
	    		dialogText += "<p class='translate' data-18n='questionnaire-updated'>questionnaire version/languages were updated</p>; ";
	    		dialogText += json.surveyUpdateResult.versionsAdded.length.toString();
	    		dialogText += "<p class='translate' data-18n='questionnaire-added'>questionnaire version/languages were added </p>";
	    		if ( json.surveyUpdateResult.warnings.length > 0 ) {
	    			dialogText += "<p class='translate' data-18n='questionnaire-warning'> with the following warnings <span>:</span></p>";
	    			for ( i in json.surveyUpdateResult.warnings ) {
	    				dialogText += " '";
	    				dialogText += json.surveyUpdateResult.warnings[i];
	    				dialogText += "';";
	    			}
	    		}
	    		$( "#status" ).dialog({
	    			title: "Updated",
	    			resizable: false,
	    		    height: 250,
	    		    width: 250,
	    		    modal: true,
	    		    dialogClass: 'success-dialog translate',
	    		    buttons: {
	    		        "Ok" : function () {
	    		            $( this ).dialog( "close" );
	    		        }
	    		    }
	    		}).text( dialogText );
	    	} else {
	    		displayError( json.error );
	    	}
	    },
	    error: function( xhr, status, errorThrown ) {
	    	$( "#btUpdateSurvey" ).button( "enable" );
	    	handleError( xhr, status, errorThrown );
	    }
	});
}

function fillSubmissionStatusTable(submissions) {
	$("#submitted-awaiting-approval").empty();
	$("#submitted-approved").empty();
	$("#submitted-rejected").empty();
	$("#not-submitted").empty();
	$(".status-button").attr("disbled",true);
	var submittedAwaitingApprovalHtml = '<thead><tr><th></th><th class="translate" data-i18n="User Name">User Name</th><th class="translate" data-i18n="Organization">Organization</th><th class="translate" data-i18n="Email">Email</th><th class="translate" data-i18n="% Complete">% Complete</th><th class="translate" data-i18n="Score">Score</th></tr></thead>\n<tbody>';
	var submittedRejectedHtml = '<thead><tr><th></th><th class="translate" data-i18n="User Name">User Name</th><th class="translate" data-i18n="Organization">Organization</th><th class="translate" data-i18n="Email">Email</th><th class="translate" data-i18n="% Complete">% Complete</th><th class="translate" data-i18n="Score">Score</th></tr></thead>\n<tbody>';
	var notSubmittedHtml = '<thead><tr><th class="translate" data-i18n="User Name">User Name</th><th class="translate" data-i18n="Organization">Organization</th><th class="translate" data-i18n="Email">Email</th><th class="translate" data-i18n="% Complete">% Complete</th><th class="translate" data-i18n="Score">Score</th></tr></thead>\n<tbody>';
	var submittedApprovedHtml = '<thead><tr><th></th><th class="translate" data-i18n="User Name">User Name</th><th class="translate" data-i18n="Organization">Organization</th><th class="translate" data-i18n="Email">Email</th><th class="translate" data-i18n="% Complete">% Complete</th><th class="translate" data-i18n="Score">Score</th></tr></thead>\n<tbody>';
	for (var i = 0; i < submissions.length; i++) {
		var html = '<tr id="submission-';
		html += submissions[i].id;
		html += '">';
		if (submissions[i].submitted) {
			html += '<td class="cb_col"><input class="status-cb" id = "submission-checked-';
			html += submissions[i].id;
			html += '" type="checkbox" name="submission-checked-';
			html += submissions[i].id;
			html += '" /></td>';
		}
		html += '<td class="username_col">';
		html += submissions[i].user.username;
		html += '</td><td class="organization_col">';
		html += submissions[i].user.organization;
		html += '</td><td class="email_col">';
		html += submissions[i].user.email;		
		html += '</td><td class="completed_col">';
		html += submissions[i].percentComplete;
		html += '</td><td class="score_col">';
		html += submissions[i].score;
		html += '</td></tr>\n';
		if (submissions[i].submitted) {
			if (submissions[i].approved) {
				submittedApprovedHtml += html;
			} else if (submissions[i].rejected) {
				submittedRejectedHtml += html;
			} else {
				submittedAwaitingApprovalHtml += html;
				
			}
		} else {
			notSubmittedHtml += html;
		}
	}
	html +='</tbody>';
	var submittedAwaitingApproval = $("#submitted-awaiting-approval");
	submittedAwaitingApproval.html(submittedAwaitingApprovalHtml);
	var submittedApproved = $("#submitted-approved");
	submittedApproved.html(submittedApprovedHtml);
	var submittedRejected = $("#submitted-rejected");
	submittedRejected.html(submittedRejectedHtml);
	var notSubmitted = $("#not-submitted");
	notSubmitted.html(notSubmittedHtml);
	addCheckboxButtonEnablers(submittedAwaitingApproval, $(".submitted-awaiting-approval-button"));
	addCheckboxButtonEnablers(submittedRejected, $(".submitted-rejected-button"));
	addCheckboxButtonEnablers(submittedApproved, $(".submitted-approved-button"));
	
	/*************001 starts  here	************/
	
	if(!$.fn.dataTable.isDataTable("#submitted-awaiting-approval")){
		$('#submitted-awaiting-approval').DataTable( {
		       "searching" : false,
		       "bLengthChange": false,
		       "processing": true,
		       "pageLength": 5,
		       "language": {					
		            "url": "resources/locales/"+(url('?locale') ||'en')+"/translation.json"
		        }
		} );
	}
	if(!$.fn.dataTable.isDataTable("#submitted-approved")){
		$('#submitted-approved').DataTable( {
			   "searching" : false,
		       "bLengthChange": false,
		       "processing": true,
		       "pageLength": 5,
		       "language": {					
		            "url": "resources/locales/"+(url('?locale') ||'en')+"/translation.json"
		        }
		} );
	}
	if(!$.fn.dataTable.isDataTable("#submitted-rejected")){
		$('#submitted-rejected').DataTable( {
			   "searching" : false,
		       "bLengthChange": false,
		       "processing": true,
		       "pageLength": 5,
		       "language": {					
		            "url": "resources/locales/"+(url('?locale') ||'en')+"/translation.json"
		        }
		} );
	}
	if(!$.fn.dataTable.isDataTable("#not-submitted")){
		$('#not-submitted').DataTable( {
			   "searching" : false,
		       "bLengthChange": false,
		       "processing": true,
		       "pageLength": 5,
		       "language": {					
		            "url": "resources/locales/"+(url('?locale') ||'en')+"/translation.json"
		        }
		});
	}
	/*************001 ends  here************/
}

function addCheckboxButtonEnablers(context, buttons) {
	var selector = $('td input:checkbox',context);
	selector.change(function() {
		if($(this).prop('checked')) {
			buttons.removeAttr("disabled");
		} else {
			var somethingChecked = false;
			selector.each(function() {
				if($(this).prop('checked')) {
					somethingChecked = true;
				}
			});
			if (!somethingChecked) {
				
				buttons.attr("disabled",true);
			}
		}
	});
}

/**
 * Request a status change for a submission
 * @param request One of setApproved, resetApproved, setRejected, resetRejeted
 * @param ids
 */
function requestStatusChange(request, ids) {
	disableSubmitCheckboxes();
	$.ajax({
	    url: "CertificationServlet",
	    data:JSON.stringify({
	        request: request,
	        ids: ids
	    }),
	    type: "POST",
	    dataType : "json",
	    contentType: "json",
	    success: function( json ) {
	    	enableSubmitCheckboxes();
	    	if (json.status == "OK") {
	    		reloadSubmissionStatus();
	    	} else {
	    		displayError(json.error);
	    	}
	    },
	    error: function( xhr, status, errorThrown ) {
	    	enableSubmitCheckboxes();
	    	handleError( xhr, status, errorThrown);
	    }
	});
}

/**
 * @param context Selector for the table containing the checkmarks
 * @return Array of ID's for each row in the table that has a checkmark
 */
function getCheckedIds(context) {
	var retval = [];
	$('td input:checkbox',context).each(function() {
		if($(this).prop('checked')) {
			var idString = $(this).attr("id");
			var id = idString.substring("submission-checked-".length,idString.length);
			retval.push(id);
		}
	});
	return retval;
}

function disableSubmitCheckboxes() {
	$(".status-cb").attr("disabled", true);
}

function enableSubmitCheckboxes() {
	$(".status-cb").removeAttr("disabled");
}

function reloadSubmissionStatus() {
	$("#submitted-awaiting-approval").empty();
	$("#submitted-approved").empty();
	$("#submitted-rejected").empty();
	$("#not-submitted").empty();
	$(".status-button").attr("disabled",true);
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
}

/**
 * Fill in the tags dropdown list
 */
function getTags() {
	$( "#gitTag" ).html( '<option value="None">[Most Recent]</option>' ); // Temporary placeholder until we retrieve the information from the backend
	$.ajax({
	    url: "CertificationServlet",
	    data: {
	        request: "getGitTags"
	    },
	    type: "GET",
	    dataType : "json",
	    success: function( json ) {
	    	var newHtml = '<option value="None">[Most Recent]</option>';
	    	for (s in json) {
	    		newHtml += '\n<option value="';
	    		newHtml += json[s];
	    		newHtml += '">';
	    		newHtml += json[s];
	    		newHtml += "</option>";
	    	}
	    	$( "#gitTag" ).html( newHtml );
	    },
	    error: function( xhr, status, errorThrown ) {
	    	handleError( xhr, status, errorThrown );
	    }
	});
}

$(document).ready( function() {
	
	getTags();
	$( "#dialog-confirm-update" ).dialog({
	  title: "Confirm Survey Update",  
	  autoOpen: false,
	  resizable: true,
	  height: 450,
	  width: 400,
	  modal: true,
	  dialogClass: 'success-dialog translate',
	  buttons: {
	    "Update": function() {
	      updateSurveyForReal( lastUpdateCommit );
	      $( this ).dialog( "close" );
	    },
	    Cancel: function() 
	    {
	      $( "#btUpdateSurvey" ).button( "enable" );
	      $( this ).dialog( "close" );
	    }
	  }
	});
	
	$( "#confirm-update-tabs" ).tabs();
	
	
	
	$("#btUpdateSurvey").button().button().click(function(event) {
	      event.preventDefault();
	      updateSurvey();
	});
	
	$("#btApprove").button().button().click(function(event) {
	      event.preventDefault();
	      requestStatusChange("setApproved", getCheckedIds($("#submitted-awaiting-approval")));
	      location.reload();
	});
	
	$("#btReject").button().button().click(function(event) {
	      event.preventDefault();
	      requestStatusChange("setRejected", getCheckedIds($("#submitted-awaiting-approval")));
	      location.reload();
	      
	});
	
	$("#btUnReject").button().button().click(function(event) {
	      event.preventDefault();
	      requestStatusChange("resetRejected", getCheckedIds($("#submitted-rejected")));
	      location.reload();
	});
	
	$("#btUnApprove").button().button().click(function(event) {
	      event.preventDefault();
	      requestStatusChange("resetApproved", getCheckedIds($("#submitted-approved")));
	      location.reload();
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
	
	reloadSubmissionStatus();
	
	
	
});