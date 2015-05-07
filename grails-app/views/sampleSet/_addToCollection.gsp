<g:javascript>
 var clearInputs = function() {
  $("div#addtocollection-modal").find("input#collectionName").val("");
 };
  var addToCollection = function() {
    var name = $("div#addtocollection-modal").find("input#collectionName").val();
    var ssid = ${sampleSet.id};
    var args = { name: name, id: ssid };
    $.post(getBase()+"/datasetCollection/addToCollection", args);
    clearInputs();
    closeModal("addtocollection-modal");
  };
  var showAllCollections = function() {
    $("input#collectionName").autocomplete("search","").focus();
  };
  $(document).ready(function() {
    $("input#collectionName").autocomplete({
      source: function(request, response) {
        $.getJSON(getBase()+"/datasetCollection/collections", request, function(data) {
          var items = [];
          $.each(data, function(key, val) {
            items.push(val);
          });
          response(items);
        });
      },
      select: function(event, ui) {
        $("input#collectionName").val(ui.item.value);
			  return false;
			},
      minLength : 0,
      delay     : 150
    });
  });
</g:javascript>
<div id="addtocollection-modal" class="modal hide" style="width: 300px;">
  <div class="modal-header">
    <a href="#" class="close">×</a>
    <h3>Add To Collection</h3>
  </div>
  <div class="modal-body">
    <p>Enter the collection name:<br/>
      <g:textField name="collectionName" style="vertical-align:middle;" class="medium"/>
      <button class="btn primary small" style="vertical-align:middle;" onclick="showAllCollections();">Show All</button>
    </p>
  </div>
  <div class="modal-footer">
    <button class="btn primary" onclick="addToCollection();">Save</button>
    <button class="btn" onclick="clearInputs();closeModal('addtocollection-modal');">Cancel</button>
  </div>
</div>