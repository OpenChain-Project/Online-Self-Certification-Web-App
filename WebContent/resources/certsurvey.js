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

var submitted;

function getQuestionFormHtml(questions) {
	var html = '<table class="questiontable ui-corner-all">\n<col class="number_col" /><col class="question_col" /><col class="answer_col" /><col class="answer_col" />\n<col class="number_col" />\n';
	var inSubQuestions = false;
	html += '<th class="number_col">#</th><th class="question_col">Question</th><th class="answer_col">Answer</th><th class="answer_col"></th><th class="number_col">Spec Ref</th>\n';
	for (var i = 0; i < questions.length; i++) {
		var type = questions[i].type;
		var isSubQuestion = (questions[i].hasOwnProperty('subQuestionNumber'));
		html += '<tr><td>'+questions[i].number + ':</td>';
		if (isSubQuestion && inSubQuestions) {
			html += '<td class=tablebullet>&bull;&nbsp;';
		} else {
			html += '<td>';
		}
		html+= questions[i].question + '</td>';
		
		if (type == 'SUBQUESTIONS') {
			html += '<td class="answer_cell"></td>';
			html += '<td class="answer_cell"></td>';
			inSubQuestions = true;
		} else if (type=="YES_NO_NA") {
			html += '<td class="answer_cell"><span>';
			html += '<input type="radio" name="answer-' + questions[i].number + '" id="answer-' + questions[i].number + '_yes"  class="choicena-yes';
			if (isSubQuestion && inSubQuestions) {
				html += ' subquestion-of-';
				html += questions[i].subQuestionNumber;
			}
			html += '"  value="yes" />';		
			html += '<input type="radio" name="answer-' + questions[i].number + '" id="answer-' + questions[i].number + '_no" class="choicena-no';
			if (isSubQuestion && inSubQuestions) {
				html += ' subquestion-of-';
				html += questions[i].subQuestionNumber;
			}
			html += '" value="no" />';
			html += '<input type="radio" name="answer-' + questions[i].number + '" id="answer-' + questions[i].number + '_na" class="choicena-na';
			if (isSubQuestion && inSubQuestions) {
				html += ' subquestion-of-';
				html += questions[i].subQuestionNumber;
			}
			html += '" value="na" />';
			html += '<span class="yesnona-switch">';
			html += '<label for="answer-' + questions[i].number + '_yes">Yes</label>';		
			html += '<span></span>';
			html += '<label for="answer-' + questions[i].number + '_no">No</label>';
			html += '<label for="answer-' + questions[i].number + '_na">N/A</label>';
			html += '</span></span></td>';
			html += '<td class="answer_cell"></td>';
			if (!isSubQuestion) {
				inSubQuestions = false;
			}
		} else {
			html += '<td class="answer_cell"><span>';
			html += '<input type="radio" name="answer-' + questions[i].number + '" id="answer-' + questions[i].number + '_yes"  class="choice-yes';
			if (isSubQuestion && inSubQuestions) {
				html += ' subquestion-of-';
				html += questions[i].subQuestionNumber;
			}
			html += '"  value="yes" />';		
			html += '<input type="radio" name="answer-' + questions[i].number + '" id="answer-' + questions[i].number + '_no" class="choice-no';
			if (isSubQuestion && inSubQuestions) {
				html += ' subquestion-of-';
				html += questions[i].subQuestionNumber;
			}
			html += '" value="no" />';
			html += '<span class="yesno-switch">';
			html += '<label for="answer-' + questions[i].number + '_yes">Yes</label>';		
			html += '<span></span>';
			html += '<label for="answer-' + questions[i].number + '_no">No</label>';
			html += '</span></span></td>';
			html += '<td class="answer_cell"></td>';
			if (!isSubQuestion) {
				inSubQuestions = false;
			}
		}
		html += '<td class="number_cell">';
		html += questions[i].specReference;
		html += '</td>';
		html += '</tr>\n';
	}
	html += '</table>\n';
	return html;
}
function getSurvey() {
	var certForm = $("#CertForm");
	if (!certForm.length ) {
		return;
	}
	if (certForm.is(':ui-accordion')) {
		certForm.accordion("destroy");
	}
	certForm.html('Loading <img src="resources/loading.gif" alt="Loading" class="loading" id="survey-loading">');
	$.ajax({
	    url: "CertificationServlet",
	    data: {
	        request: "getsurveyResponse"
	    },
	    type: "GET",
	    dataType : "json",
	    success: function( surveyResponse ) {
	    	var specVersion = surveyResponse.specVersion;
	    	var surveyVersion = surveyResponse.specVersion;
	    	var versionParts = specVersion.split( "." );
	    	if ( versionParts.length > 2 ) {
	    		specVersion = versionParts[0] + "." + versionParts[1];
	    	}
	    	$( "#specVersion" ).text( specVersion );
	    	$( "#surveyVersion" ).text( surveyVersion );
	    	certForm.empty();
	    	var survey = surveyResponse.survey;
	    	var responses = surveyResponse.responses;
	    	var sections = survey.sections;
	    	submitted = surveyResponse.submitted;
	    	var html = '';
	    	for ( var i = 0; i < sections.length; i++ ) {
	    		var divReference = 'section_' + sections[i].name;
	    		html += '<h3>' + sections[i].name + ': ' + sections[i].title + 
	    		'<div style="float:right" id="h_'+divReference+'">[NUM] answered out of [NUM]</div>'+'</h3>\n';
	    		html += '<div id="' + divReference + '">\n';
	    		html += getQuestionFormHtml(sections[i].questions);
	    		html += '</div>\n';
	    	}
	    	certForm.html(html);
	    	certForm.accordion({heightStyle:"content"});
	    	// For each section, set any answers, tally the num answered, and 
	    	// set set the button click to keep track of the numbers
	    	for ( var i = 0; i < sections.length; i++ ) {
	    		var divReference = 'section_' + sections[i].name;
	    		$("#"+divReference).find(":input").each(function(index) {
		    		var id = $(this).attr('id');
		    		if (id.substring(0,7) == 'answer-') {
		    			var questionNumber = id.substring(7,id.lastIndexOf('_'));
		    			var myresponse = responses[questionNumber];
		    			if (myresponse) {
		    				// Fill in the existing value
		    				if (myresponse.answer == 'Yes' && $(this).attr('value') == 'yes') {
		    					$(this).prop('checked',true);
		    				} else if (myresponse.answer == 'No' && $(this).attr('value') == 'no') {
		    					$(this).prop('checked',true);
		    				} else if (myresponse.answer == 'NotApplicable' && $(this).attr('value') == 'n/a') {
		    					$(this).prop('checked',true);
		    				} else {
		    					$(this).prop('checked',false);
		    				}
		    			}
		    			$(this).change(function(e) {
		    				updateSectionQuestionCounts($(this).parents('.ui-accordion-content'));
		    			});
		    		}
	    		});
	    		updateSectionQuestionCounts($("#"+divReference));
	    		updateButtons();
	    	}
	    },
	    error: function( xhr, status, errorThrown ) {
	    	handleError( xhr, status, errorThrown);
	    }
	});
}

/**
 * Updates buttons based on the submission status
 */
function updateButtons() {
	if (submitted) {
		$("#btSaveAnswers").hide();
		$("#btSaveAndSubmit").hide();
		$("#btResetAnswers").hide();
		$("#btUnSubmit").show();
	} else {
		$("#btSaveAnswers").show();
		$("#btSaveAndSubmit").show();
		$("#btResetAnswers").show();
		$("#btUnSubmit").hide();
	}
}

function updateSectionHtml(section, numQuestions, numAnswers) {
	var sectionId = section.attr('id');
	var headerSectionDivId = 'h_' + sectionId;
	var headerDiv = section.parent().find('#'+headerSectionDivId);
	var html = String(numAnswers);
	html += ' answered out of ';
	html += String(numQuestions);
	headerDiv.html(html);
}

function updateSectionQuestionCounts(section) {
	var questionMap = new Map();	// Map of questions to number of answers
	$(section).find(":input").each(function(index) {
		// fill in the map
		var id = $(this).attr('id');
		if (id.substring(0,7) == 'answer-') {
			var question;
			var classes = $(this).attr('class');
			var subQuestionMatch = classes.match(/subquestion-of-([A-Za-z0-9_\.]+)/);
			if (subQuestionMatch != null && subQuestionMatch.length > 0) {
				question = subQuestionMatch[1];
			} else {
				question = id.substring(7,id.lastIndexOf('_'));
			}
			if (!questionMap.has(question)) {
				questionMap.set(question, 0);
			}
			if ($(this).prop('checked')) {
				questionMap.set(question, questionMap.get(question)+1);
			}
		}
	});
	var numQuestions = 0;
	var numAnswered = 0;
	questionMap.forEach(function(count, question) {
		numQuestions++;
		if (count > 0) {
			numAnswered++;
		}
	});
	updateSectionHtml(section,numQuestions,numAnswered);
}

function saveAll() {
	$("#btSaveAnswers").button("disable");
	$("#btSaveAndSubmit").button("disable");
	var answers = [];
	$("#CertForm").find(":input").each(function(index) {
		var id = $(this).attr('id');
		if (id.substring(0,7) == 'answer-') {
			var value = $(this).attr('value');
			var checked = this.checked;
			var questionNumber = id.substring(7,id.lastIndexOf('_'));
			answers.push({'questionNumber':questionNumber, 'value':value, 'checked':checked});
		}
	});
	
	var data = JSON.stringify({request: "updateAnswers", 'answers': answers});
	$.ajax({
	    url: "CertificationServlet",
	    data: data,
	    contentType: "json",
	    type: "POST",
	    dataType : "json",
	    success: function( json ) {
	    	$("#btSaveAnswers").button("enable");
	    	$("#btSaveAndSubmit").button("enable");
	    	if (json.status == "OK") {
	    		$( "#status" ).dialog({
	    			title: "Saved",
	    			resizable: false,
	    		    height: 200,
	    		    width: 200,
	    		    modal: true,
	    		    buttons: {
	    		        "Ok" : function () {
	    		            $( this ).dialog( "close" );
	    		        }
	    		    }
	    		}).text( "Save Successful" );
	    	} else {
	    		displayError(json.error);
	    	}
	    	
	    },
    error: function( xhr, status, errorThrown ) {
    	$("#btSaveAnswers").button("enable");
    	$("#btSaveAndSubmit").button("enable");
    	handleError( xhr, status, errorThrown);
    }
  });
}

function unsubmit() {
	$("#btUnSubmit").button("disable");
	var data = JSON.stringify({request: "unsubmit"});
	$.ajax({
	    url: "CertificationServlet",
	    data: data,
	    contentType: "json",
	    type: "POST",
	    dataType : "json",
	    success: function( json ) {
	    	$("#btUnSubmit").button("enable");
	    	if (json.status == "OK") {
		    	submitted = false;
		    	updateButtons();
	    		$( "#status" ).dialog({
	    			title: "UnSubmitted",
	    			resizable: false,
	    		    height: 200,
	    		    width: 200,
	    		    modal: true,
	    		    buttons: {
	    		        "Ok" : function () {
	    		            $( this ).dialog( "close" );
	    		        }
	    		    }
	    		}).text( "Responses Unsubmitted" );
	    	} else {
	    		displayError(json.error);
	    	}
	    	
	    },
    error: function( xhr, status, errorThrown ) {
    	$("#btUnSubmit").button("enable");
    	handleError( xhr, status, errorThrown);
    }
  });
}

function finalSubmission() {
	
	$("#btSaveAndSubmit").button("disable");
	$("#btSaveAnswers").button("disable");
	var answers = [];
	$("#CertForm").find(":input").each(function(index) {
		var id = $(this).attr('id');
		if (id.substring(0,7) == 'answer-') {
			var value = $(this).attr('value');
			var checked = this.checked;
			var questionNumber = id.substring(7,id.lastIndexOf('_'));
			answers.push({'questionNumber':questionNumber, 'value':value, 'checked':checked});
		}
	});
	
	var data = JSON.stringify({request: "finalSubmission", 'answers': answers});
	$.ajax({
	    url: "CertificationServlet",
	    data: data,
	    contentType: "json",
	    type: "POST",
	    dataType : "json",
	    success: function( json ) {
    		$("#btSaveAndSubmit").button("enable");
    		$("#btSaveAnswers").button("enable");
	    	if (json.status == "OK") {
	    		submitted = true;
	    		updateButtons();
	    		$( "#status" ).dialog({
	    			title: "Saved",
	    			resizable: false,
	    		    height: 200,
	    		    width: 200,
	    		    modal: true,
	    		    buttons: {
	    		        "Ok" : function () {
	    		            $( this ).dialog( "close" );
	    		        }
	    		    }
	    		}).text( "Thank you - your information has been submitted" );
	    	} else {
	    		$("#btSaveAndSubmit").button("enable");
	    		$("#btSaveAnswers").button("enable");
	    		displayError(json.error);
	    	}
	    	
	    },
    error: function( xhr, status, errorThrown ) {
    	$("#btSaveAndSubmit").button("enable");
    	$("#btSaveAnswers").button("enable");
    	handleError( xhr, status, errorThrown);
    }
  });
}

function downloadAnswers() {
	window.location="CertificationServlet?request=downloadanswers";
}
$(document).ready( function() {
	$("#btSaveAnswers").button();
	$("#btSaveAnswers").click(function(event) {
      event.preventDefault();
      saveAll();
    });
	$("#btSaveAndSubmit").button();
	$("#btSaveAndSubmit").click(function(event) {
	      event.preventDefault();
	      $( "#submitconfirm" ).dialog({
				title: "Confirm Submit",
				resizable: false,
			    height: 200,
			    width: 300,
			    modal: true,
			    buttons: [{
			    		text: "Agree",
			    		click: function () {
			    			finalSubmission();
			    			$( this ).dialog( "close" );
			    		},
			    		
			    	},
			    	{
			    		text: "Cancel",
			    		click: function () {
			    			$( this ).dialog( "close" );
			        }
			    	}]
			    
			}).text( "Please confirm that you have verified all appropriate artifacts are maintained per the OpenChain specification." );
	});
	$("#btUnSubmit").button();
	$("#btUnSubmit").click(function(event) {
	      event.preventDefault();
	      unsubmit();
	});
	$("#btDownloadAnswers").button();
	$("#btDownloadAnswers").click(function(event) {
	      event.preventDefault();
	      downloadAnswers();
	});
	$("#btResetAnswers").button();
	$("#btResetAnswers").click(function(event) {
	      event.preventDefault();
	  	$( "#oktoreset" ).dialog({
			title: "Reset Answers",
			resizable: false,
		    height: 200,
		    width: 300,
		    modal: true,
		    buttons: [{
		    		text: "Yes",
		    		click: function () {
		    			var certForm = $("#CertForm");
		    			if (certForm.is(':ui-accordion')) {
		    				certForm.accordion("destroy");
		    			}
		    			certForm.html('Loading <img src="resources/loading.gif" alt="Loading" class="loading" id="survey-loading">');
		    			// This will be cleared when the form is loaded
		    			$.ajax({
		    			    url: "CertificationServlet",
		    			    data:JSON.stringify({
		    			        request:  "resetanswers"
		    			    }),
		    			    type: "POST",
		    			    dataType : "json",
		    			    contentType: "json",
		    			    async: true, 
		    			    success: function( json ) {
		    			    	if ( json.status == "OK" ) {
		    			    		getSurvey();
		    			    	} else {
		    			    		displayError( json.error );
		    			    	} 	
		    			    },
		    			    error: function( xhr, status, errorThrown ) {
		    			    	handleError(xhr, status, errorThrown);
		    			    }
		    			});
		    			$( this ).dialog( "close" );
		    		},
		    		
		    	},
		    	{
		    		text: "Cancel",
		    		click: function () {
		    			$( this ).dialog( "close" );
		        }
		    	}]
		    
		}).text( "This will delete all answers and start a new survey with the latest survey version.  Are you sure you want to continue?" );
	});
	getSurvey();
});