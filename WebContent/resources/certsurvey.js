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

function getQuestionFormHtml(questions) {
	var html = '<table class="questiontable ui-corner-all">\n<col class="number_col" /><col class="question_col" /><col class="answer_col" /><col class="answer_col" /><col class="answer_col" />\n';
	var inSubQuestions = false;
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
			html += '<td class="question_cell"></td>';
			html += '<td class="question_cell"></td>';
			inSubQuestions = true;
		} else {
			html += '<td class="question_cell"><input type="radio" name="answer-' + questions[i].number + '" id="answer-' + questions[i].number + '_yes" value="yes" />';		
			html += '<label for="answer-' + questions[i].number + '_yes">Yes</label></td>';		
			html += '<td class="question_cell"><input type="radio" name="answer-' + questions[i].number + '" id="answer-' + questions[i].number + '_no" value="no" />';
			html += '<label for="answer-' + questions[i].number + '_no">No</label></td>';
			if (questions[i].hasOwnProperty('notApplicablePrompt')) {
				html += '<td class="question_cell"><input type="radio" name="answer-' + questions[i].number + '" id="answer-' + questions[i].number + '_na" value="na" />';		
				html += '<label for="answer-' + questions[i].number + '_na">'+questions[i].notApplicablePrompt+'</label></td>';
			}
			if (!isSubQuestion) {
				inSubQuestions = false;
			}
		}
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
	$.ajax({
	    url: "CertificationServlet",
	    data: {
	        request: "getsurveyResponse"
	    },
	    type: "GET",
	    dataType : "json",
	    success: function( surveyResponse ) {
	    	$( "#version" ).text( surveyResponse.specVersion );
	    	certForm.empty();
	    	var htmlList = '<ul>\n';
	    	var htmlDivs = '';
	    	var survey = surveyResponse.survey;
	    	var responses = surveyResponse.responses;
	    	var sections = survey.sections;
	    	for ( var i = 0; i < sections.length; i++ ) {
	    		var tabReference = 'section_' + sections[i].name;
	    		htmlList += '<li><a href="#' + tabReference + '">Section '+sections[i].name + '</a></li>\n';
	    		htmlDivs += '<div id="' + tabReference + '">\n';
	    		htmlDivs += '<h3>' + sections[i].name + ': ' + sections[i].title + "</h3>\n";
	    		htmlDivs += getQuestionFormHtml(sections[i].questions);
	    		if (i < sections.length-1) {
	    			htmlDivs += '<div style="text-align:center" class="formbutton"><button type="button" id="save_answers_'+sections[i].name+'" class="ui-corners-all ui-dialog-buttons">Save & Continue</button></div>\n';
	    		}
	    		htmlDivs += '</div>\n';
	    	}
	    	htmlList += '</ul>\n';
	    	certForm.html(htmlList + htmlDivs);
	    	certForm.tabs();
	    	// Set any alread answered questions
	    	certForm.find(":input").each(function(index){
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
	    				} else {
	    					$(this).prop('checked',false);
	    				}
	    			}
	    		}
	    	});
	    	// Add actions to the buttons
	    	for ( var i = 0; i < sections.length-1; i++ ) {
	    		$("#save_answers_"+sections[i].name).click(function() {
	    			var answers = [];
	    			$(this).parent().parent().find(":input").each(function(index) {
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
	    			    	if (json.status == "OK") {
	    			    		var currentActive = certForm.tabs("option","active");
	    			    		var numTabs = $('#CertForm >ul >li').size();
	    			    		if (currentActive < numTabs-1) {
	    			    			certForm.tabs("option","active",currentActive+1);
	    			    		} 
	    			    	} else {
	    			    		displayError(json.error);
	    			    	}
	    			    	
	    			    },
	    		    error: function( xhr, status, errorThrown ) {
	    		    	handleError( xhr, status, errorThrown);
	    		    }
	    		  });
	    		});
	    	}
	    },
	    error: function( xhr, status, errorThrown ) {
	    	handleError( xhr, status, errorThrown);
	    }
	});
}

function saveAll() {
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
    	handleError( xhr, status, errorThrown);
    }
  });
}

function finalSubmission() {
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
	    		}).text( "Thank you - your information has been submitted" );
	    	} else {
	    		displayError(json.error);
	    	}
	    	
	    },
    error: function( xhr, status, errorThrown ) {
    	handleError( xhr, status, errorThrown);
    }
  });
}
$(document).ready( function() {
	getSurvey();
});