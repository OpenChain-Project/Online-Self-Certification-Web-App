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
	    			    		window.location = "signupsuccess.html";
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
	    
	}).text( "You have not verified the username.  Would you like to resend a verify email to the email address you registered?" );
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
		    			window.location = "admin.html";
		    		} else {
		    			window.location = "survey.html";
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
		openSignInDialog();	// open the login dialog
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
	    		window.location = "index.html";
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
	username = $("#update-username");
	var password = $("#update-password");
	var passwordVerify = $("#update-passwordverify");
	var name = $("#update-name");
	var organization = $("#update-organization");
	var email = $("#update-email");
	var address = $("#update-address");
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
		updateUser(username.val(), pw, name.val(), address.val(), organization.val(), email.val());
	}
}

function updateUser(username, password, name, address, organization, email) {
	$.ajax({
	    url: "CertificationServlet",
	    data:JSON.stringify({
	        request:  "updateUser",
	        username: username,
	        password: password,
	        name: name,
	        address: address,
	        organization: organization,
	        email: email
	    }),
	    type: "POST",
	    dataType : "json",
	    contentType: "json",
	    async: false, 
	    success: function( json ) {
	    	if ( json.status == "OK" ) {
	    		$("#user-profile").dialog("close");
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
	valid = valid && checkChecked( checkApproveEmail, checkApproveEmailLbl );
	valid = valid && checkChecked( checkApproveTerms, checkApproveTermsLbl );
	if (valid) {
		signup(username.val(), password.val(), name.val(), address.val(), organization.val(), email.val());
	}
}

function signup(username, password, name, address, organization, email) {
	$.ajax({
	    url: "CertificationServlet",
	    data:JSON.stringify({
	        request:  "signup",
	        username: username,
	        password: password,
	        name: name,
	        address: address,
	        organization: organization,
	        email: email
	    }),
	    type: "POST",
	    dataType : "json",
	    contentType: "json",
	    async: false, 
	    success: function( json ) {
	    	if ( json.status == "OK" ) {
	    		$("#signup").dialog("close");
	    		// redirect to the successful signup page
	    		window.location = "signupsuccess.html";
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
	    	
	    	$("#user-profile").dialog("open");
	    },
	    error: function( xhr, status, errorThrown ) {
	    	handleError( xhr, status, errorThrown);
	    }
	});
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
	    	html = '';
	    	if (json.loggedIn) {
	    		html += '<li id="user-dropdown_updateprofile"><a href="javascript:void(0);"><span class="ui-icon-closethick">&nbsp;Update profile</span></a></li>\n';
	    		html += '<li id="user-dropdown_signout"><a href="javascript:void(0);"><span class="ui-icon-closethick">&nbsp;Sign out</span></a></li>\n';
	    	} else {
	    		html += '<li id="user-dropdown_signin"><a href="javascript:void(0);"><span class="ui-icon-plusthick">&nbsp;Sign in</span></a></li>\n';
	    		html += '<li id="user-dropdown_signup"><a href="javascript:void(0);"><span class="ui-icon-pencil">&nbsp;Sign up</span></a></li>\n';
	    	}
	    	$("#user-dropdown_menu").html(html);
	    	$("#usermenu").jui_dropdown( {
	    	    launcher_id: 'user-dropdown_launcher',
	    	    launcher_container_id: 'user-dropdown_container',
	    	    menu_id: 'user-dropdown_menu',
	    	    containerClass: 'user-dropdown_container',
	    	    menuClass: 'user-dropdown_menu',
	    	    launchOnMouseEnter: true,
	    	    onSelect: function( event, data ) {
	    	    	if ( data.id == "user-dropdown_signup" ) {
	    	    		openSignupDialog();
	    	    	} else if ( data.id == "user-dropdown_signout" ) {
	    	    		signout();
	    	    	} else if (data.id == "user-dropdown_signin") {
	    	    		openSignInDialog();
	    	    	} else if (data.id == "user-dropdown_updateprofile") {
	    	    		openProfileDialog();
	    	    	}
	    	    }
	    	  });
	    	// Check for admin
	    	if (json.admin && json.loggedIn) {
	    		$("#adminlink").html('<a href="admin.html"><span class="ui-icon ui-icon-key"></span>Admin</a>&nbsp;&nbsp;&nbsp;</div>');
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
		    		window.location = "pwresetrequest.html";
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
	$("#topnav").load("topnav.html");
	$("#login").dialog({
		title: "Login",
		autoOpen: false,
		height: 300,
		width: 350,
		modal: true,
		buttons: [{
			text: "Login",
			click: function() {
				loginUser();
			}
		}, {
			text: "Cancel",
			click: function() {
				window.location = "index.html";
				$(this).dialog("close");
			}
		}]
	}).find("form").on("submit", function(event) {
		event.preventDefault();
		loginUser();
	});
	$("#reset-password").dialog({
		title: "Password Reset",
		autoOpen: false,
		height: 350,
		width: 350,
		modal: true,
		buttons: [{
			text: "Reset Password",
			click: function() {
				requestPasswordReset();
			}
		}, {
			text: "Cancel",
			click: function() {
				window.location = "index.html";
				$(this).dialog("close");
			}
		}]
	}).find("form").on("submit", function(event) {
		event.preventDefault();
		requestPasswordReset();
	});
	$("#login").find("a.reset-password").click(function(event) {
		event.preventDefault();
		$("#login").dialog("close");
		$("#reset-password").dialog("open");
	});
	$("#user-profile").dialog({
		title: "Update Profile",
		autoOpen: false,
		height: 600,
		width: 450,
		modal: true,
		buttons: [{
			text: "Update",
			click: function() {
				updateUserProfile();
			}
		}, {
			text: "Cancel",
			click: function() {
				$(this).dialog("close");
			}
		}]
	}).find("form").on("submit", function(event) {
		event.preventDefault();
		updateUserProfile();
	});
	
	$("#signup").dialog({
		title: "Sign Up",
		autoOpen: false,
		height: 600,
		width: 450,
		modal: true,
		buttons: [{
			text: "Sign Up",
			click: function() {
				signupUser();
			}
		}, {
			text: "Cancel",
			click: function() {
				$(this).dialog("close");
			}
		}]
	}).find("form").on("submit", function(event) {
		event.preventDefault();
		signupUser();
	});
	$("#footer-outer").load('footer.html');
	createNavMenu();
});