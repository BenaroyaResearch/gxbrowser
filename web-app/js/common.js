function getBase() {
  var curPath = location.pathname;
  return curPath.substring(0, curPath.indexOf("/",1));
}

$(document).ready(function() {

  setTimeout(function() {
    $('.message').fadeOut('slow', function() {
      $(this).remove();
    });
    $('.error-message').fadeOut('slow', function() {
      $(this).remove();
    });
  }, 2000);

  $(".editable-text").live("focus", function() {
    localStorage.setItem(this.id, $(this).val());
  }).live("blur", function() {
    updateEditableText($(this));
  }).bind("keyup", function(e) {
    var code = (e.keyCode ? e.keyCode : e.which);
    if (code === 27)
    {
      var oldValue = localStorage.getItem(this.id);
      $(this).val(oldValue).trigger("blur");
    }
    else if (!$(this).is("textarea") && code == 13)
    {
      $(this).trigger("blur");
    }
  });

  $("input[type=number].editable-text").live("click", function() {
    updateEditableText($(this));
  });

  function updateEditableText(field) {
    var id = field.attr("id");
    var ids = id.split("::");
    var args = { property: ids[1], value: field.val() };
    var infoType = field.closest("form").attr("id");
    $.post(getBase()+"/"+infoType+"/setter/"+ids[0], args, function(result) {
      if (result === "error")
      {
        var oldValue = localStorage.getItem(id);
        field.val(oldValue);
      }
      else
      {
        field.siblings(".field-url").attr("href", result);
      }
    });
  }

});

(function ($) {
  $.fn.liveDraggable = function (opts) {
    this.live("mousemove", function() {
      $('.sortable-list li').draggable({
        appendTo    : 'body',
//              containment : 'window',
        cursorAt    : { left: 20, top: 10 },
        helper      : function() {
          var html = '<ol id="helper" class="drag-list unstyled">';
          if ($(this).hasClass('ui-selected'))
          {
            $(this).parent().find('li.ui-selected, li.ui-selecting').each(function() {
              html += $("<div/>").append($(this).clone()).html();
            });
          }
          else
          {
            $(this).siblings().removeClass('ui-selected ui-selecting');
            html += $("<div/>").append($(this).clone()).html();
          }
          html += '</ol>';
          return $(html);
        },
        revert      : 'invalid'
      });
    });
  };

  $.fn.textWidth = function() {
    var text = $(this).html();
    var html_calc = '<span>' + text + '</span>';
    $(this).html(html_calc);
    var width = $(this).find('span:first').width();
    return width;
  };

  $.fn.serializeObject = function()
  {
     var o = {};
     var a = this.serializeArray();
     $.each(a, function() {
         if (o[this.name]) {
             if (!o[this.name].push) {
                 o[this.name] = [o[this.name]];
             }
             o[this.name].push(this.value || '');
         } else {
             o[this.name] = this.value || '';
         }
     });
     return o;
  };

}(jQuery));