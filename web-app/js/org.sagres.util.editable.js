$(document).ready(function() {
//  $('.min-max').summarize();

  // editable textareas
  $('.textarea>.editable-value').editable(function(value, settings, submitdata) {
      var setUrl = this.getAttribute("id");
      var finalResult;
      $.ajax({
        type : "POST",
        url  : setUrl,
        data : submitdata,
        cache: false,
        async : false,
        success: function(result) {
          finalResult = result;
        }
      });
      return finalResult;
    },
    {
      loadurl : function() {
        var getUrl = this.getAttribute("name");
        return { url: getUrl };
      },
      type        : 'textarea',
      cancel      : 'Cancel',
      submit      : 'Save',
      height      : 100,
      submitdata  : function(value, settings) {
        var data = { paragraph: true };
        var maxLength = $(this).parent().find("input#maxLength").val();
        if (maxLength != "0") {
          data["maxLength"] = maxLength;
        }
        return data;
      },
      callback    : function(value, settings, original) {
        $(this).html(original);
        $(this).find('span').html(value);
        $(this).find('.min-max').summarize({ width: $(this).width() });
      }
    }
  );

  // editable textfield
  $('.text>.editable-value').editable(function(value, settings, submitdata) {
      var setUrl = this.getAttribute("id");
      var finalResult;
      $.ajax({
        type : "POST",
        url  : setUrl,
        data : submitdata,
        cache: false,
        async : false,
        success: function(result) {
          finalResult = result;
        }
      });
      return finalResult;
    },
    {
      loadurl : function() {
        var getUrl = this.getAttribute("name");
        return { url: getUrl };
      },
      type        : 'text',
      submitdata  : function(value, settings) {
        var data = {};
        var maxLength = $(this).parent().find("input#maxLength").val();
        if (maxLength != "0") {
          data["maxLength"] = maxLength;
        }
        return data;
      },
      callback    : function(value, settings, original) {
        $(this).html(original);
        $(this).find('span').html(value);
      }
    }
  );

  // editable checkbox
  $('.checkbox>.editable-value input[type=checkbox]').live('change', function() {
    var parent = $(this).parent().parent();
    var url = parent.attr("id");
    var submitdata = { value: $(this).is(":checked") };
    $.ajax({
        type : "POST",
        url  : url,
        data : submitdata,
        cache: false,
        async : false
    });
  });

  // edit button mouse interactions
//  $('.editButton').bind({
//      mouseenter: function() {
//        $('body').css('cursor','pointer');
//      },
//      mouseleave: function() {
//        $('body').css('cursor','default');
//      },
//      click: function() {
//        $(this).parent().parent().find('.editable-value div').click();
//      }
//  });

  $('.editable-text-field').bind({
    focus: function() {
      $(this).after("<div class='help-message'>click here or press enter to update</div>");
    },
    blur: function() {
      $(this).parent().find('div.help-message').detach();
    },
    change: function() {
      var url = $(this).attr("id");
      var submitdata = { property: $(this).attr("name"), value: $(this).val() };
      $.ajax({
          type : "POST",
          url  : url,
          data : submitdata,
          cache: false,
          async : false
      });
      $(this).blur();
    }
  });

});