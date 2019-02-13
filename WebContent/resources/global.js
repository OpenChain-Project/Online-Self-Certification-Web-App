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

/*
 * This is a common JavaScript file containing the code to support internationalization
 */
/**
 * List of all supported language keyed by 2 or 3 character ISO language.  The value is the display for the language
 */
var LANGUAGES = {"en":"English", 
                 "ru":"Russian"};
//TODO: See if we can use I18N to populate this variable - i18next.languages should contain the language

/**************** For initializing the i18next framework ***********************/
		 i18next.use(i18nextXHRBackend);
			jqueryI18next.init(i18next, $);
			i18next.init({
//				debug: true,
		   fallbackLng: getCurrentLanguage(),
			 backend: {
		      loadPath: 'resources/locales/{{lng}}/translation.json'
		  		},
		
			}, function(err, t) {
				i18next.changeLanguage(getCurrentLanguage());
				//document.getElementById('add').innerHTML = i18next.t('Getting Started_content_1_sub');
				
				
			});


/****************Get the current language from the URL***********/
			function getCurrentLanguage() {
				//TODO: See if we can get this from i18next - i18next should be the language, but it is not being set
				return (url('?locale') || 'en');
			}
			
/****************Error handling functions ***********************/
			
			function displayError( error ) {
				
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
			  	
			  	
				$( "#errors" ).dialog({
					title:'<span class="translate" data-i18n="Error">Error</span>',
					resizable: false,
				    height: 250,
				    width: 300,
				    dialogClass: 'success-dialog translate',
				    modal: true,
				    buttons: [{
				    	text: "Ok",
			    		"data-i18n": "Ok",
			    		 click: function () { 
				            $( this ).dialog( "close" );
				        }
				    }]
				}).html ( error ).parent().addClass( "ui-state-error" );
				$('.translate').localize();
			}

			function handleError(xhr, status, errorThrown, msg) {
				if ( msg === undefined ) {
					var responseType = xhr.getResponseHeader("content-type") || "";
					if ( responseType.indexOf('text') >= 0 && xhr.responseText != null && xhr.responseText!= "" ) {
						msg = '<span class="translate" data-i18n="error-msg">Sorry - there was a problem loading data: &nbsp; </span>' + xhr.responseText;
					} else if ( responseType.indexOf('json') > 1 && xhr.responseText != null && xhr.responseText!= "" ) {
						response = JSON.parse(xhr.responseText);
						msg = response.error;		
					} else {
						msg ='<span class="translate" data-i18n="error-msg">Sorry - there was a problem loading data: &nbsp;</span>' + errorThrown;
						
					}
				}
				if ( xhr.status == 401 ) {
					// Redirect to login page
					window.location="login.html"+'?locale='+(url('?locale') || 'en');
				} else {
					displayError( msg );
			        console.log( "Error: " + xhr.responseText );
			        console.log( "Status: " + status );
			        console.dir( xhr );       
				}	
			}
			
/****************When Language is changed ***********************/

		function changeLng(lng) 
		{
			if (!lng || !LANGUAGES[lng]) {
				displayError( "Language not supported" );
				return;
			}
			// update the back-end
			$.ajax({
				url: "CertificationServlet",
				data: JSON.stringify({
			        request:  "setlanguage",
			        language: lng,
			        locale: lng
			    }),
			    type: "POST",
			    dataType : "json",
			    contentType: "json",
			    async: false, 
			    success: function( json ) {
			    	if ( json.status == "OK" ) {
			    		updateLanguageUI(lng);
			    	} else {
			    		displayError( json.error );
			    	} 	
			    },
			    error: function( xhr, status, errorThrown ) {
			    	handleError( xhr, status, errorThrown );
			    }
			});	
		}
		
/**********Update the language user interface and I18N framework*********************/
		function updateLanguageUI(language) {
			if (language == getCurrentLanguage()) {
				return;	// already set.  This is an important check otherwise we will keep refreshing and calling the method
			}
			var u = new URL(window.location.href);
    		if(u.searchParams.has("locale"))
    		{
    			u.searchParams.set("locale",language ? language : (url('?locale') || 'en'));
    		}
    		else
    		{
    			u.searchParams.append("locale",language ? language : (url('?locale') || 'en'));
    		}
    		window.location.href=u;
    		i18next.changeLanguage(language ? language : (url('?locale') || 'en'));
		}
	
		
/****************Creates HTML choices for user dropdown list of language selections*********/
		function getLanguageSelectionHtml() {  // Used in the profile update and user signup pages
			var html = '';
			for (var lang in LANGUAGES) {
				html += '<option class="translate" data-i18n="';
				html += LANGUAGES[lang];
				html += '" value="';
				html += lang;
				html += '">';
				html += LANGUAGES[lang];
				html += ' (';
				html += lang;
				html += ')</option>\n';
			}
			return html;
		}
/****************Creates the HTML for the dropdown list items from the list of languages****/
		function getLanguageHtml() {	// Used for the main navigation language chooser dropdown
			var html = '';
			for (var lang in LANGUAGES) {
				html += '<li><a onclick="changeLng(';
				html += "'";
				html += lang;
				html += "'";
				html += ')" class ="dropdown-item translate" data-i18n="';
				html += LANGUAGES[lang];
				html += '">';
				html += LANGUAGES[lang];
				html += ' (';
				html += lang;
				html += ')</a></li>\n';
			}
			return html;
		}
		
/*******************TO get the language of a previous page ***********************/

		
		$(document).delegate('.append','click', function($this){ 
			 event.preventDefault();
			 window.location.href = $(this).attr('href')+'?locale='+(url('?locale') || 'en');
		 });
		
		
		
/*****************Function to translate the HTML content***************************/
	
		i18next.on('languageChanged', () => {
			$('.translate').localize();
		});

/*************************************************************************************/
	