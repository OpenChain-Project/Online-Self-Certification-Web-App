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
 * This is a common JavaScript file to be included on all pages for this app.
 * It depends on the div ID's error, status, login, and signup
 */
/**
 * List of all supported language keyed by 2 or 3 character ISO language
 */
var LANGUAGES = {"en":"English", 
                 "de":"German",
                 "fr":"French"};

var DEFAULT_LANGUAGE = "eng";

var currentLanguage = DEFAULT_LANGUAGE;

//TODO: Update the languages with the officially supported list
var NOPASSWORD = "***********";
// From http://www.whatwg.org/specs/web-apps/current-work/multipage/states-of-the-type-attribute.html#e-mail-state-%28type=email%29
var emailRegex = /^[a-zA-Z0-9.!#$%&'*+\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;
// the following was copied from the jqueryUI sample code
function checkRegexp( o, regexp, n ) {
    if ( !( regexp.test( o.val() ) ) ) {
      o.addClass( "ui-state-error" );
      updateTips( n );
      return false;
    } else {
      return true;
    }
  }
function checkLength( o, n, min, max ) {
    if ( o.val().length > max || o.val().length < min ) {
      o.addClass( "ui-state-error" );
      updateTips( "Length of " + n + " must be between " +
        min + " and " + max + "." );
      return false;
    } else {
      return true;
    }
  }

function checkChecked( o, lbl ) {
	var isChecked = o.is(':checked');
	if (!isChecked) {
		o.addClass( "ui-state-error" );
		lbl.addClass( "ui-state-error" );
		updateTips( '"' + lbl.text() + '" must be checked.' );
		return false;
	} else {
		return true;
	}
}

function checkEquals(a, b, n) {
	if (a.val() != b.val()) {
		a.addClass( "ui-state-error" );
		b.addClass( "ui-state-error" );
		updateTips(n + " do not match."); 
		return false;
	} else {
		return true;
	}
}
function updateTips( t ) {
    tips
      .text( t )
      .addClass( "ui-state-highlight" );
    setTimeout(function() {
      tips.removeClass( "ui-state-highlight", 1500 );
    }, 500 );
  }
var username;
var tips;

function openResendVerificationDialog(username, password) {
	$( "#resendveify" ).dialog({
		title: "Resend Verification",
		resizable: false,
	    height: 250,
	    width: 250,
	    modal: true,
	    dialogClass: 'success-dialog translate',
	    buttons: [{
	    		text: "No",
	    		click: function () {
	    			$( this ).dialog( "close" );
	        }
	    	}, {
	    		text: "Yes",
	    		click: function () {
	    			$.ajax({
	    			    url: "CertificationServlet",
	    			    data:JSON.stringify({
	    			        request:  "resendverify",
	    			        username: username.val(),
	    			        password: password.val()
	    			    }),
	    			    type: "POST",
	    			    dataType : "json",
	    			    contentType: "json",
	    			    async: false, 
	    			    success: function( json ) {
	    			    	password.val('');
	    			    	if ( json.status == "OK" ) {
	    			    		// redirect to the successful signup page
	    			    		window.location = "signupsuccess.html"+'?locale='+(url('?locale') || 'en');
	    			    	} else {
	    			    		displayError( json.error );
	    			    	} 	
	    			    },
	    			    error: function( xhr, status, errorThrown ) {
	    			    	password.val('');
	    			    	handleError(xhr, status, errorThrown);
	    			    }
	    			});
	    			$( this ).dialog( "close" );
	    		}
	    	}]
	    
	}).html( "<span class='translate' data-i18n='verify-username' >You have not verified the username.  Would you like to resend a verify email to the email address you registered?" );
}

function loginUser() {
	username = $("#login-username");
	var password = $("#login-password");
	tips = $(".validateTips");
	tips.text('');
	password.text('');
	var valid = true;
	username.removeClass("ui-state-error");
	password.removeClass("ui-state-error");
	valid = valid && checkLength( username, "username", 3, 40 );
	valid = valid && checkLength( password, "password", 8, 60 );
	valid = valid && checkRegexp( username, /^[a-z]([0-9a-z_\s])+$/i, "Username may consist of a-z, 0-9, underscores, spaces and must begin with a letter." );
	if (valid) {
		$.ajax({
		    url: "CertificationServlet",
		    data:JSON.stringify({
		        request:  "login",
		        username: username.val(),
		        password: password.val()
		    }),
		    type: "POST",
		    dataType : "json",
		    contentType: "json",
		    async: false, 
		    success: function( json ) {
		    	password.val('');
		    	if ( json.status == "OK" ) {
		    		if (json.admin) {
		    			
		    			window.location = "admin.html"+'?locale='+(url('?locale') || 'en');
		    		} else {
		    			window.location = "survey.html"+'?locale='+(url('?locale') || 'en');
		    		}
		    	} else if (json.status == "NOT_VERIFIED") {
		    		openResendVerificationDialog(username, password);
		    	} else {
		    		password.val('');
		    		displayError( json.error );
		    	} 	
		    },
		    error: function( xhr, status, errorThrown ) {
		    	handleError(xhr, status, errorThrown);
		    }
		});
	}
}
function displayError( error ) {	
	$( "#errors" ).dialog({
		title: "Error",
		resizable: false,
	    height: 250,
	    width: 300,
	    dialogClass: 'success-dialog translate',
	    modal: true,
	    buttons: {
	        "Ok" : function () {
	            $( this ).dialog( "close" );
	        }
	    }
	}).text( error ).parent().addClass( "ui-state-error" );
}

function handleError(xhr, status, errorThrown, msg) {
	if ( msg === undefined ) {
		var responseType = xhr.getResponseHeader("content-type") || "";
		if ( responseType.indexOf('text') > 1 && xhr.responseText != null && xhr.responseText!= "" ) {
			msg = "Sorry - there was a problem loading data: " + xhr.responseText;
		} else if ( responseType.indexOf('json') > 1 && xhr.responseText != null && xhr.responseText!= "" ) {
			response = JSON.parse(xhr.responseText);
			msg = response.error;		
		} else {
			msg = "Sorry - there was a problem loading data: " + errorThrown;
			
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

function signout() {
	$.ajax({
	    url: "CertificationServlet",
	    data:JSON.stringify({
	        request:  "logout"
	    }),
	    type: "POST",
	    dataType : "json",
	    contentType: "json",
	    async: false, 
	    success: function( json ) {
	    	if ( json.status == "OK" ) {
	    		createNavMenu();	// recreate the menu for logged out
	    		var certForm = $("#CertForm");
	    		if (certForm) {
	    			certForm.empty();
	    		}
	    		// redirect to home
	    		window.location = "index.html"+'?locale='+(url('?locale') || 'en');
	    	} else {
	    		displayError( json.error );
	    	} 	
	    },
	    error: function( xhr, status, errorThrown ) {
	    	handleError(xhr, status, errorThrown);
	    }
	});
}

function updateUserProfile() {
	//TODO add preferred language
	username = $("#update-username");
	var password = $("#update-password");
	var passwordVerify = $("#update-passwordverify");
	var name = $("#update-name");
	var organization = $("#update-organization");
	var email = $("#update-email");
	var address = $("#update-address");
	var okUseNameEmail = $("#update-use-name-email");
	tips = $(".validateTips");
	tips.text('');
	var valid = true;
	username.removeClass("ui-state-error");
	password.removeClass("ui-state-error");
	passwordVerify.removeClass("ui-state-error");
	name.removeClass("ui-state-error");
	organization.removeClass("ui-state-error");
	email.removeClass("ui-state-error");
	address.removeClass("ui-state-error");
	valid = valid && checkLength( username, "username", 3, 40 );
	var pw = null;
	if (password.val() && password.val() != NOPASSWORD && password.val() != "") {
		valid = valid && checkLength( password, "password", 8, 60 );
		valid = valid && checkLength( passwordVerify, "password", 8, 60 );
		valid = valid && checkEquals(password, passwordVerify, "passwords");
		pw = password.val();
	} 
	valid = valid && checkLength( name, "Name", 3, 100 );
	valid = valid && checkLength( organization, "Organization", 1, 100 );
	valid = valid && checkLength( email, "email", 3, 100 );
	valid = valid && checkLength( address, "address", 3, 500 );
	valid = valid && checkRegexp( username, /^[a-z]([0-9a-z_\s])+$/i, "Username may consist of a-z, 0-9, underscores, spaces and must begin with a letter." );
	valid = valid && checkRegexp( email, emailRegex, "e.g. user@linux-foundation.org");
	if (valid) {
		updateUser(username.val(), pw, name.val(), address.val(), 
				organization.val(), email.val(), okUseNameEmail.is(':checked'));
	}
}

function updateUser(username, password, name, address, organization, email, okUseNameEmail) {
	//TODO Add preferred language
	$.ajax({
	    url: "CertificationServlet",
	    data:JSON.stringify({
	        request:  "updateUser",
	        username: username,
	        password: password,
	        name: name,
	        address: address,
	        organization: organization,
	        email: email,
	        namePermission: okUseNameEmail,
	        emailPermission: okUseNameEmail
	    }),
	    type: "POST",
	    dataType : "json",
	    contentType: "json",
	    async: false, 
	    success: function( json ) {
	    	if ( json.status == "OK" ) {
	    			$( "#status" ).dialog({
	    			title: "Updated",
	    			resizable: false,
	    			height: 200,
	    			width: 200,
	    			modal: true,
	    			dialogClass: 'success-dialog translate',
	    			buttons: {
	    			    "Ok" : function () {
	    			        $( this ).dialog( "close" );
	    			        $('#updateprofileModal').modal('hide');
	    				      }
	    		   }
	    	  }).html( " <span class='translate' data-i18n='profile-notification'>Profile updated  successfully" );
	    	} else {
	    		displayError( json.error );
	    	} 	
	    },
	    error: function( xhr, status, errorThrown ) {
	    	handleError(xhr, status, errorThrown);
	    }
	});
}

function signupUser() {
	username = $("#signup-username");
	var password = $("#signup-password");
	var passwordVerify = $("#signup-passwordverify");
	var name = $("#signup-name");
	var organization = $("#signup-organization");
	var email = $("#signup-email");
	var address = $("#signup-address");
	var checkApproveEmail = $("#approval-use-name-email");
	var checkApproveEmailLbl = $("label[for='approval-use-name-email']");
	var checkApproveTerms = $("#read-terms");
	var checkApproveTermsLbl = $("label[for='read-terms']");
	//TODO Add preferred language
	tips = $(".validateTips");
	tips.text('');
	var valid = true;
	username.removeClass("ui-state-error");
	password.removeClass("ui-state-error");
	passwordVerify.removeClass("ui-state-error");
	name.removeClass("ui-state-error");
	organization.removeClass("ui-state-error");
	email.removeClass("ui-state-error");
	address.removeClass("ui-state-error");
	checkApproveEmail.removeClass("ui-state-error");
	checkApproveTerms.removeClass("ui-state-error");
	checkApproveEmailLbl.removeClass("ui-state-error");
	checkApproveTermsLbl.removeClass("ui-state-error");
	valid = valid && checkLength( username, "username", 3, 40 );
	valid = valid && checkLength( password, "password", 8, 60 );
	valid = valid && checkLength( passwordVerify, "password", 8, 60 );
	valid = valid && checkLength( name, "Name", 3, 100 );
	valid = valid && checkLength( organization, "Organization", 1, 100 );
	valid = valid && checkLength( email, "email", 3, 100 );
	valid = valid && checkLength( address, "address", 3, 500 );
	valid = valid && checkEquals(password, passwordVerify, "passwords");
	valid = valid && checkRegexp( username, /^[a-z]([0-9a-z_\s])+$/i, "Username may consist of a-z, 0-9, underscores, spaces and must begin with a letter." );
	valid = valid && checkRegexp( email, emailRegex, "e.g. user@linux-foundation.org");
	valid = valid && checkChecked( checkApproveTerms, checkApproveTermsLbl );
	if (valid) {
		signup(username.val(), password.val(), name.val(), address.val(), organization.val(), 
				email.val(), checkApproveEmail.is(':checked'));
	}
}

function signup(username, password, name, address, organization, email, approveUseNameEmail) {
	//TODO Add preferred language
	$.ajax({
	    url: "CertificationServlet",
	    data:JSON.stringify({
	        request:  "signup",
	        username: username,
	        password: password,
	        name: name,
	        address: address,
	        organization: organization,
	        email: email,
	        emailPermission: approveUseNameEmail,
	        namePermission: approveUseNameEmail
	    }),
	    type: "POST",
	    dataType : "json",
	    contentType: "json",
	    async: false, 
	    success: function( json ) {
	    	if ( json.status == "OK" ) {
	    		// redirect to the successful signup page
	    		window.location = "signupsuccess.html"+'?locale='+(url('?locale') || 'en');
	    	} else {
	    		displayError( json.error );
	    	} 	
	    },
	    error: function( xhr, status, errorThrown ) {
	    	handleError(xhr, status, errorThrown);
	    }
	});
}

function openSignupDialog() {
	$("#signup").dialog("open");
}

function openSignInDialog() {
	$("#login").dialog("open");
}

function openProfileDialog() {
	$.ajax({
	    url: "CertificationServlet",
	    data: {
	        request: "getuser"
	    },
	    type: "GET",
	    dataType : "json",
	    success: function( json ) {
	    	$("#update-username").val(json.username).prop("readonly", true);
	    	$("#update-password").attr("placeholder", NOPASSWORD);
	    	$("#update-passwordverify").attr("placeholder", NOPASSWORD);
	    	$("#update-name").val(json.name);
	    	$("#update-organization").val(json.organization);
	    	$("#update-email").val(json.email);
	    	$("#update-address").val(json.address);
	    	$("#update-use-name-email").prop('checked', json.emailPermission);
	    	
	    	$("#user-profile").dialog("open");
	    },
	    error: function( xhr, status, errorThrown ) {
	    	handleError( xhr, status, errorThrown);
	    }
	});
}

/**
 * Set the language for the JavaScript display
 * @param language
 * @param display
 */
function setLanguage(language, display) {
	$( '#language-dropdown_launcher' ).text( display );
	currentLanguage = language;
	//TODO Invoke whatever HTML/JavaScript framework is used
}

/**
 * @param language tag in IETF RFC 5646 format
 */
function changeLanguage(language, display) {
	//TODO Implement
	if (currentLanguage == language) {
		return;	// Already using this language
	}
	// update the back-end
	$.ajax({
		url: "CertificationServlet",
		data: JSON.stringify({
	        request:  "setlanguage",
	        language: language
	    }),
	    type: "POST",
	    dataType : "json",
	    contentType: "json",
	    async: false, 
	    success: function( json ) {
	    	if ( json.status == "OK" ) {
	    		// Change the text on the language dropdown
	    		setLanguage(language, display);
	    		//TODO  Refresh the page or reload the data
	    	} else {
	    		displayError( json.error );
	    	} 	
	    },
	    error: function( xhr, status, errorThrown ) {
	    	handleError( xhr, status, errorThrown );
	    }
	});
	// Refresh the page or reload the data
	// Invoke whatever HTML/JavaScript framework is used
}

/**
 * Create a navigation menu based on whether the user is logged in
 */
function createNavMenu() {
	$.ajax({
	    url: "CertificationServlet",
	    data: {
	        request: "getuser"
	    },
	    type: "GET",
	    dataType : "json",
	    success: function( json ) {
	    	userHtml = '';
	    	if (json.loggedIn) {
	    		userHtml += '<li class="nav-item active"><div class="dropdown"><span class="dropdown-toggle" data-toggle="dropdown"><i class="fa fa-user" aria-hidden="true">&nbsp;</i><span class="translate" data-i18n="Account"> Account </span></span><div class="dropdown-menu dropdown-menu-right"><ul class="language-setup-dropdown"><li class="nav-item active" data-toggle="modal" data-target="#updateprofileModal" id="user-dropdown_updateprofile"><a class=" dropdown-item" ><i class="fa fa-refresh"></i>&nbsp;<span class="translate" data-i18n="Update profile">Update profile</span></a></li>';
	    		userHtml += '<li class="nav-item active" id="user-dropdown_signout"><a class=" dropdown-item append" ><i class="fa fa-sign-out" aria-hidden="true"></i>&nbsp;<span class="translate" data-i18n="Sign out">Sign out</span></a></li></ul></div></div>\n';
	    	
	    	} else {
	    	
	    		userHtml += '<li class="nav-item active signin" ><a class="user-nav append" href="login.html" ><i class="fa fa-user-plus" aria-hidden="true"></i>&nbsp;<span class="translate" data-i18n="Sign in">Sign in</span></a></li>\n';
	    		userHtml += '<li class="nav-item active signin mb-0" ><a class="user-nav append" href="signup.html" ><i class="fa fa-sign-in" aria-hidden="true"></i>&nbsp;<span class="translate" data-i18n="Sign up">Sign up</span></a></li>\n';
	    	}
	    	$("#user-dropdown_menu").html(userHtml);	    	
	    	languageHtml = '';
	    	for (var language in LANGUAGES) {
	    		languageHtml += '<li id="language-dropdown_';
	    		languageHtml += language;
	    		languageHtml += '"><a href="javascript:void(0);"><span class="ui-icon-closethick">&nbsp;';
	    		languageHtml += LANGUAGES[language];
	    		languageHtml += '</span></a></li>\n';
	    	}
	    	$("#language-dropdown_menu").html(languageHtml);
	    	if (json.language != null) {
	    		display = LANGUAGES[json.language];
	    		if (language != null) {
	    			setLanguage(json.language, display);
	    		}
	    	} else {
	    		$( '#language-dropdown_launcher' ).text( LANGUAGES[DEFAULT_LANGUAGE] );
	    	}
	    	if (json.loggedIn) {
	    		$("#surveylink").html('<li><a class="append" href="survey.html" id="toggle"><i class="fa fa-pencil"></i>&nbsp; <span class="translate" data-i18n="Online Self-Certification">Online Self-Certification</span>&nbsp;&nbsp;&nbsp;</a></li>');
	    	} else {
	    		$("#surveylink").html('');
	    	}
	    	// Check for admin
	    	if (json.admin && json.loggedIn) {
	    		$("#adminlink").html('<li><a class="append" href="admin.html" id="toggle"><i class="fa fa-key" aria-hidden="true"></i>&nbsp;<span class="translate" data-i18n="Admin">Admin</span></a></li></div>');
	    	} else {
	    		$("#adminlink").html('');
	    	}
	    },
	    error: function( xhr, status, errorThrown ) {
	    	handleError( xhr, status, errorThrown);
	    }
	});
}

function requestPasswordReset() {
	username = $("#reset-username");
	var email = $("#reset-email");
	tips = $(".validateTips");
	tips.text('');
	var valid = true;
	username.removeClass("ui-state-error");
	email.removeClass("ui-state-error");
	valid = valid && checkLength( username, "username", 3, 40 );
	valid = valid && checkLength( email, "email", 3, 100 );
	valid = valid && checkRegexp( email, emailRegex, "e.g. user@linux-foundation.org");
	if (valid) {
		$.ajax({
		    url: "CertificationServlet",
		    data:JSON.stringify({
		        request:  "requestResetPassword",
		        username: username.val(),
		        email: email.val()
		    }),
		    type: "POST",
		    dataType : "json",
		    contentType: "json",
		    async: false, 
		    success: function( json ) {
		    	if ( json.status == "OK" ) {
		    		$("#reset-password").dialog("close");
		    		// redirect to the pw reset request page
		    		window.location = "pwresetrequest.html"+'?locale='+(url('?locale') || 'en');
		    	} else {
		    		displayError( json.error );
		    	} 	
		    },
		    error: function( xhr, status, errorThrown ) {
		    	handleError(xhr, status, errorThrown);
		    }
		});
	}
}

$(document).ready( function() {
	// Added by ViSolve 
	
	
	// Function Call for Signout
	$(document).on('click', '#user-dropdown_signout', function(){signout();});
	
	// Function Call for Update profile
	$(document).on('click', '#user-dropdown_updateprofile', function(){openProfileDialog();});
	
	
	
	// Function Call for Login
	$(document).on('click', '#login-button', function(){loginUser();});
	
	// Function Call for Signup
	$(document).on('click', '#signup-button', function(){signupUser();});
	
	
	// Function Call for Reset Password
	$(document).on('click', '#btnresetpassword', function(){requestPasswordReset();});
	
	//Function call for Update Profile 
	$(document).on('click', '#update-user-profile', function(){updateUserProfile();});

	
	// redirect to the index page
	$("#index-home").click(function () {
	window.location = "index.html"+'?locale='+(url('?locale') || 'en');
	});
	$("#topnav").load("topnav.html");
	$("#footer-outer").load('footer.html');
	createNavMenu();
});


         