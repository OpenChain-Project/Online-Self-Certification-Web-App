
	

/**************** For initializing the i18next framework ***********************/
				     var language = (url('?locale') || 'en');		
					 i18next.use(i18nextXHRBackend);
				    	jqueryI18next.init(i18next, $);
						i18next.init({
					   fallbackLng: language,
						 backend: {
				          loadPath: 'resources/locales/{{lng}}/translation.json'
				      		},

						}, function(err, t) {
						i18next.changeLanguage(language);
						});
						
/****************When Language is changed from navbar dropdown ***********************/

		function changeLng(lng) 
		{
			var u = new URL(window.location.href);
			if(u.searchParams.has("locale"))
			{
			u.searchParams.set("locale",lng);
			}
			else
			{
			u.searchParams.append("locale",lng);
			}
			window.location.href=u;
		i18next.changeLanguage(lng);	
		}
	
/*******************TO get the language of a previous page ***********************/

		
		$(document).delegate('.append','click', function($this){ 
			 event.preventDefault();
			 window.location.href = $(this).attr('href')+'?locale='+(url('?locale') || language);
		 });
		
		
		
/*****************Function to translate the HTML content***************************/
	
		i18next.on('languageChanged', () => {
			$('.translate').localize();
		});

/*************************************************************************************/
		