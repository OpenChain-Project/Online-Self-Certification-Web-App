
		var defaultLang ='en';

/**************** For initializing the i18next framework ***********************/

    	i18next.use(i18nextXHRBackend);
    	jqueryI18next.init(i18next, $);
		i18next.init({
		fallbackLng: 'en',
		 backend: {
          loadPath: 'resources/locales/{{lng}}/translation.json'
      		},

		}, function(err, t) {
		i18next.changeLanguage(url('?locale') || defaultLang);
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

		function toggle(e)
		{
			e.preventDefault();
			window.location.href = e.target.parentElement.getAttribute('href')+'?locale='+(url('?locale') || defaultLang);
		}

/*****************Function to translate the HTML content***************************/
	
		i18next.on('languageChanged', () => {
		$('.translate').localize();
		});

/*************************************************************************************/


