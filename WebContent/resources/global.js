

var set_locale_to = function(locale) 
{
  if (locale)
    $.i18n().locale = locale;
  else
    locale = 'en';
  
   $.i18n().load( 
  './resources/i18n-json/'+locale+'.json',locale
  ).done(function() {
  
$('.translate').each(function() {
    var args = [], $this = $(this);
    if ($this.data('args'))
      args = $this.data('args').split(',');
    $this.html( $.i18n.apply(null, args) );
  }); 
  });
};



jQuery(function() {

 
 set_locale_to(url('?locale'));

/****************When Language is changed from navbar dropdown ***********************/

  $(".language-setup-dropdown li a").click(function(){
      var value = $(this).attr('language');
      History.pushState(null, null, "?locale=" + value); 
   });


/****************TO get the language of a previous page *****************************/

  
    $("#toggle").click(function() 
    {
    	window.location.href = $("#toggle").attr("href")+'?locale='+(url('?locale'));
	    
    });

    


/****************To change a language inside same html******************************/

    History.Adapter.bind(window, 'statechange', function()
    {
      set_locale_to(url('?locale'));
    });






});

