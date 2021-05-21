function resetPassword() {
	var username = $("#reset-acc-username");
	var password = $("#reset-pass");
	var passwordVerify = $("#reset-pass-verify");
	tips = $(".validateTips");
	tips.text('');
	password.text('');
	var valid = true;
	username.removeClass("ui-state-error");
	password.removeClass("ui-state-error");
	valid = valid && checkLength( username, "username", 3, 40 );
	valid = valid && checkLength( password, "password", 8, 60 );
	valid = valid && checkLength( passwordVerify, "password", 8, 60 );
	valid = valid && checkEquals(password, passwordVerify, "passwords");
	valid = valid && checkRegexp( username, /^[a-z]([0-9a-z_\s])+$/i, "Username may consist of a-z, 0-9, underscores, spaces and must begin with a letter." );
	if (valid) {
		$.ajax({
		    url: "CertificationServlet",
		    data:JSON.stringify({
		        request:  "changePassword",
		        username: username.val(),
		        password: password.val(),
		        locale: getCurrentLanguage(),
		        reCaptchaResponse: grecaptcha.getResponse(resetWidgetId)
		    }),
		    type: "POST",
		    dataType : "json",
		    contentType: "json",
		    async: false, 
		    success: function( json ) {
		    	if ( json.status == "OK" ) {
		    		window.location = "pwresetsuccess.html" +'?locale='+(url('?locale') || 'en');
		    	} else {
		    		displayError( json.error );
		    		grecaptcha.reset(resetWidgetId);
		    	} 	
		    },
		    error: function( xhr, status, errorThrown ) {
		    	handleError(xhr, status, errorThrown);
		    	grecaptcha.reset(resetWidgetId);
		    }
		});
	}
}
$(document).ready( function() {
	$(document).on('click', '#reset-user-password', function(){resetPassword();});
});