$(document).ready(function () {

    var id = $(this).find('option:selected').attr("value");
    var name = $(this).find('option:selected').attr("name");
    console.log(id + ', ' + name);
    $("#id").val(id);
    $("#name").val(name);

    $("#selectbasic").change(
        function () {
            var id = $(this).find('option:selected').attr("value");
            var name = $(this).find('option:selected').attr("name");
            console.log(id + ', ' + name);
            $("#id").val(id);
            $("#name").val(name);
    });

    $('select.dropdown')
      .dropdown()
    ;
});