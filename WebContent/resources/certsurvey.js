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
 *
/* Functions added by ViSolve
 * 001:  Yes No Images are added based on Radio button checked
 * 
 */

var submitted, resetDialog, selectVersionDialog;

function getQuestionFormHtml(questions) {
	var html = '<div ><table class="questiontable ui-corner-all table table-hover ">\n';
	var inSubQuestions = false;
		for (var i = 0; i < questions.length; i++) {
		var type = questions[i].type;
		var isSubQuestion = (questions[i].hasOwnProperty('subQuestionOfNumber'));
		html += '<tr class="bord_"><td class="answer_cell" style="width:100px;">';
		
		html += '<span> <i class="fa yes_no_img"></i></span></td>';
		if (type == 'SUBQUESTIONS') {
			html += '<td class="answer_cell"></td>';
			html += '<td class="answer_cell"></td>';
			inSubQuestions = true;
		} 
		else if (type=="YES_NO_NA") {
			html += '<td class="answer_cell"><span>';
			html += '<input type="radio" name="answer-' + questions[i].number + '" id="answer-' + questions[i].number + '_yes"  class="choicena-yes';
			if (isSubQuestion && inSubQuestions) {
				html += ' subquestion-of-';
				html += questions[i].subQuestionOfNumber;
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
		} else 
			{					
			html += '<td class="answer_cell pad_cell"><div class="form-check custom-checkbox"><label class="form-check-label">';
			html += '<input type="radio" class="form-check-input custom-radiobtn" name="answer-' + questions[i].number + '" id="answer-' + questions[i].number + '_yes"';
			if (isSubQuestion && inSubQuestions) {
				html += ' subquestion-of-';
				html += questions[i].subQuestionNumber;
			}
			html += '"  value="yes" /><span class="translate" data-i18n="Yes">Yes</span>';
			html += '</label></div>';			
			html += '<div class="form-check custom-checkbox"><label class="form-check-label">';		
			html += '<input type="radio" class="form-check-input custom-radiobtn" name="answer-' + questions[i].number + '" id="answer-' + questions[i].number + '_no"';
			if (isSubQuestion && inSubQuestions) {
				html += ' subquestion-of-';
				html += questions[i].subQuestionNumber;
			}
			html += '" value="no" /><span class="translate" data-i18n="No">No</span>';
			html += '</label></div></td>';			
			html += '<td class="answer_cell pad_cell"></td>';
			if (!isSubQuestion) {
				inSubQuestions = false;
			}
		}
		if (isSubQuestion) {
			html += '<td class="tablebullet answer_cell"> <p id="question-pad" class="table-bullet" >';			
		} 
		else {
			html += '<td class="answer_cell pad_cell"><p id="question-pad">';
		}
		html+=questions[i].question +  '</p></td>\n';
		html += '</tr>\n';
	}
	html += '</table>\n</div>';
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
	        request: "getsurveyResponse",
	        locale: getCurrentLanguage()
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
	    		html += '<h3>' + sections[i].title + '<div  style="float:right" id="h_'+divReference+'">[NUM] answered out of [NUM]</div>'+'</h3>\n';
	    		html += '<div id="' + divReference + '" class="no-ofquestion">\n';
	    		html += getQuestionFormHtml(sections[i].questions);
	    		html += '</div>\n';
	    	}
	    	$('.translate').localize();
	    	certForm.html(html);
	    	certForm.accordion({heightStyle:"content",collapsible: true,active: false});
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
		    				var a = $(this).parent().parent().parent().parent().children(":first").children(":first").children(":first");
		    				// Fill in the existing value
		    				if (myresponse.answer == 'Yes' && $(this).attr('value') == 'yes') {
		    					$(this).prop('checked',true);   				
		    					a.addClass('fa-check-circle');
		    				} else if (myresponse.answer == 'No' && $(this).attr('value') == 'no') {
		    					$(this).prop('checked',true);
		    					a.addClass('fa-times-circle-o');
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
	html += ' <span class="translate" data-i18n="answered out of" > answered out of </span> ';
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
	if (numQuestions == numAnswered){
		$("#"+$(section).attr('aria-labelledby')).addClass("all-answered");			
	}
}

function saveAll( showDialog ) {
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
	var data = JSON.stringify({request: "updateAnswers", 'answers': answers,
        locale: getCurrentLanguage()});
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
	    		if ( showDialog ) {	    			
	    			event.preventDefault();
	    		      $( "#status" ).dialog().data( "uiDialog" )._title = function(title) {
	    		    	    title.html( this.options.title );
	    		    	};
	    		    	$( "#status" ).dialog({
	    		    		title: '<span class="translate" data-i18n="Saved">Saved</span>',
	    		    		resizable: false,
	    		    		height: 200,
	    		    		width: 200,
	    		    		dialogClass: 'success-dialog translate',
	    		    		modal: true,
	    		    		buttons: [{
	    		    			text: "Ok",
	    		    			"data-i18n": "Ok",
	    		    			click: function () { 
	    		    				$( this ).dialog( "close" );
	    		    			}
	    		    		}]
	    		    	}).html( "<span class='translate' data-i18n='Save-ans-dialog'> Saved Successful </span>" );
	    		    	$('.translate').localize();
	    			}
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
	var data = JSON.stringify({request: "unsubmit",
        locale: getCurrentLanguage()});
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
		    	$.widget("ui.dialog", $.extend({}, $.ui.dialog.prototype, {
				    _title: function(title) {
				        if (!this.options.title ) {
				            title.html("&#160;");
				        } else {
				            title.html(this.options.title);
				        }
				    }
				}));
	    		$( "#status" ).dialog({
	    			title: '<span class="translate" data-i18n="Unsubmit">UnSubmitted</span>',
	    			resizable: false,
	    		    height: 200,
	    		    width: 200,
	    		    modal: true,
	    		    dialogClass: 'success-dialog translate dg-unsubmit',
	    		    buttons: [{
	    		    	text: "Ok",
			    		"data-i18n": "Ok",
			    		 click: function () { 
	    		            $( this ).dialog( "close" );
	    		        }
	    		    }]
	    		}).html( "<span class='translate' data-i18n='Responses Unsubmitted'>Responses Unsubmitted</span>" );
	    		$('.translate').localize();
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
	var data = JSON.stringify({request: "finalSubmission", 'answers': answers,
        locale: getCurrentLanguage()});
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
	    		$.widget("ui.dialog", $.extend({}, $.ui.dialog.prototype, {
				    _title: function(title) {
				        if (!this.options.title ) {
				            title.html("&#160;");
				        } else {
				            title.html(this.options.title);
				        }
				    }
				}));
	    		$( "#status" ).dialog({
	    			title: '<span class="translate" data-i18n="Saved">Saved</span>',
	    			resizable: false,
	    		    height: 200,
	    		    width: 200,
	    		    modal: true,
	    		    dialogClass: 'success-dialog translate',
	    		    buttons: [{
	    		    	text: "ok",
			    		"data-i18n": "Ok",
			    		click: function () {	    		        
	    		            $( this ).dialog( "close" );
	    		        }
	    		    }]
	    		}).html( "<span class='translate' data-i18n='submission-notice' >Thank you - your information has been submitted</span>" );
	    		$('.translate').localize();
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
	$("#btSaveAnswers").click(function(event)
	{
      event.preventDefault();
      saveAll( true );
    });
	$("#btSaveAndSubmit").button();
	$("#btSaveAndSubmit").click(function(event) {
	      event.preventDefault();
	      $.widget("ui.dialog", $.extend({}, $.ui.dialog.prototype, {
			    _title: function(title) {
			        if (!this.options.title ) {
			            title.html("&#160;");
			        } else {
			            title.html(this.options.title);
			        }
			    }
			}));
	      $( "#submitconfirm" ).dialog({
				title: '<span class="translate" data-i18n="Confirm Submit">Confirm Submit</span>',
				resizable: false,
			    height: 270,
			    width: 350,
			    modal: true,
			    dialogClass: 'success-dialog translate',
			    buttons: [{
			    		text: "Agree",
			    		"data-i18n": "Agree",
			    		click: function () {
			    			finalSubmission();
			    			$( this ).dialog( "close" );
			    		},			    		
			    	},
			    	{
			    		text: "Cancel",
			    		"data-i18n": "Cancel",
			    		click: function () {
			    			$( this ).dialog( "close" );
			        }
			    	}]
			}).html( '<span class="confirm-submit-content" data-i18n="confirm-submit-dialog">By clicking "submit" you confirm:</span><ol type="1"><li class="translate" data-i18n="confirm-submit-subdialog1">Your answers to the Conformance Self-Certification Questionnaire are accurate and verifiable.</li><li class="translate" data-i18n="confirm-submit-subdialog2">Your answers reflect your adherence to the OpenChain Specification.</li></ol>');
	      $('.translate').localize();
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
	    $.ajax({
		    url: "CertificationServlet",
		    data: {
		        request: "getSurveyVersions",
		        locale: getCurrentLanguage()
		    },
		    type: "GET",
		    dataType : "json",
		    success: function( majorVersions ) {
		    	var items = "";
		    	var maxVersion = "";
		    	for ( var i = 0; i < majorVersions.length; i++) {
		    		items += '<option value="' + majorVersions[i] + '">' + majorVersions[i] + '</option>\n';
		    		if ( majorVersions[i] > maxVersion ) {
		    			maxVersion = majorVersions[i];
		    		}
		    	}
		    	$( "#resetVersionSelect" ).html(items);
		    	$( "#resetVersionSelect" ).val(maxVersion);		    	
		    	resetDialog.dialog( "open" );		    	
		    },
		    error: function( xhr, status, errorThrown ) {
		    	handleError( xhr, status, errorThrown);
		    }
		});	  	
	});		
	resetDialog = $( "#resetsurvey" ).dialog().data( "uiDialog" )._title = function(title) 
	{
	  	    title.html( this.options.title );
	  	  $('#resetsurvey').dialog( "close" );	  	   
	 };
	resetDialog = $( "#resetsurvey" ).dialog({				
		title: '<span class="translate" data-i18n="Reset Answers">Reset Answers</span>',
		autoOpen: false,
		resizable: false,
	    height: 250,
	    width: 330,
	    modal: true,
	    dialogClass: 'success-dialog translate',
	    buttons: [{
	    		text: "Yes",
	    		"data-i18n":"Yes",
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
	    			        request:  "resetanswers",
	    			        specVersion: $( "#resetVersionSelect" ).val(),
	    			        locale: getCurrentLanguage()
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
	    		"data-i18n":"Cancel",
	    		click: function () {
	    			$( this ).dialog( "close" );
	        }
	    	}]	    
	});
	$("#btSelectVersion").button();
	$("#btSelectVersion").click(function(event) {	
	    $.ajax({
		    url: "CertificationServlet",
		    data: {
		        request: "getSupportedSpecVersions",
		        locale: getCurrentLanguage()
		    },
		    type: "GET",
		    dataType : "json",
		    success: function( majorVersions ) {
		    	var items = "";
		    	var maxVersion = "";
		    	for ( var i = 0; i < majorVersions.length; i++) {
		    		items += '<option value="' + majorVersions[i] + '">' + majorVersions[i] + '</option>\n';
		    		if ( majorVersions[i] > maxVersion ) {
		    			maxVersion = majorVersions[i];
		    		}
		    	}
		    	$( "#versionSelect" ).html(items);
		    	$( "#versionSelect" ).val(maxVersion);
		    	selectVersionDialog.dialog( "open" );		    	
		    },
		    error: function( xhr, status, errorThrown ) {
		    	handleError( xhr, status, errorThrown);
		    }
		});	  	
	});	
	selectVersionDialog = $( "#selectversion" ).dialog().data( "uiDialog" )._title = function(title) 
	{
	  	    title.html( this.options.title );
	  	  $('#selectversion').dialog( "close" );
	  	    
	 };
	selectVersionDialog = $( "#selectversion" ).dialog({
		title: '<span class="translate" data-i18n="Select Version">Select Version</span>',
		autoOpen: false,
		resizable: false,
	    height: 250,
	    width: 330,
	    modal: true,
	    dialogClass: 'success-dialog translate',
	    buttons: [{
	    		text: "OK",
	    		"data-i18n" : "Ok",
	    		click: function () {
	    			saveAll( false );
	    			var certForm = $("#CertForm");
	    			if (certForm.is(':ui-accordion')) {
	    				certForm.accordion("destroy");
	    			}
	    			certForm.html('Loading <img src="resources/loading.gif" alt="Loading" class="loading" id="survey-loading">');
	    			// This will be cleared when the form is loaded
	    			$.ajax({
	    			    url: "CertificationServlet",
	    			    data:JSON.stringify({
	    			        request:  "setCurrentSurveyResponse",
	    			        specVersion: $( "#versionSelect" ).val(),
	    			        create: true,
	    			        locale: getCurrentLanguage()
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
	    		"data-i18n" : "Cancel",
	    		click: function () {
	    			$( this ).dialog( "close" );
	        }
	    	}]	    
	});	
	getSurvey();
	/* 001 Starts here */
	$(document).delegate('.form-check-input','click', function($this){   	
  	   var appendimg = $(this).parent().parent().parent().parent().children(":first").children(":first").children(":first");
         if ($this.target.value == 'yes') {      	
           appendimg.removeClass('fa-times-circle-o');
      	   appendimg.addClass('fa-check-circle');
         }
         else if ($this.target.value == 'no') {
            appendimg.removeClass('fa-check-circle');
            appendimg.addClass('fa-times-circle-o');
         }      
     });	
	/* 001 ends here */	 
});

