


var set_locale_to = function(locale) {
  if (locale)
    $.i18n().locale = locale;

  $('.translate').each(function() {
    var args = [], $this = $(this);
    if ($this.data('args'))
      args = $this.data('args').split(',');
    $this.html( $.i18n.apply(null, args) );
  });
};



jQuery(function() {

  $.i18n().load( {
    'ru': './resources/i18n-json/ru.json',
    'en': './resources/i18n-json/en.json'

  } ).done(function() {


  var w = new URLSearchParams(window.location.search);
  var value = w.get('locale');
  set_locale_to(value);

  //set_locale_to(url('?locale'));

   /* var userLang = navigator.language || navigator.userLanguage; 
    alert ("The language is: " + userLang);*/
    

  $(".language-setup-dropdown li a").click(function(){
      var value = $(this).attr('language');
      
       var u = new URL(window.location.href);

       if(u.searchParams.has("locale"))
       {
          u.searchParams.set("locale",value);
       }
       else
       {
        u.searchParams.append("locale",value);
       }

       window.location.href=u;
 
    });


  });
});