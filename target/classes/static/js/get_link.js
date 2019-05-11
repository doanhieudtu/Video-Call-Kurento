$(document).ready(function () {
    var counter = 0;
    var email= document.getElementById('email').textContent;
    if(email!='')
    {
    	localStorage.setItem('email', email)
    }
    $("#addrow").on("click", function () {
    	if(counter==4){
    		$("#messaageAdd").text("Liên kết chỉ được gửi tối đa 5 thành viên");
    	}
    	else{
    		var newRow = $("<tr>");
            var cols = "";

            cols += '<td><input type="email" class="form-control email" name="email' + counter + '"/></td>';

            cols += '<td><input type="button" class="ibtnDel btn btn-md btn-danger "  value="Xóa"></td>';
            newRow.append(cols);
            $("table.order-list").append(newRow);
            counter++;
    		}
    });

    $("table.order-list").on("click", ".ibtnDel", function (event) {
    	$("#messaageAdd").text('');
        $(this).closest("tr").remove();       
        counter -= 1
    });
    
    $("#get_link").on("click", function () {
    	var link= $("#content-link").val();
    	if(link!='') $("#content-link").text("");
    	$.ajax({
    		dataType: 'text',
    		type: 'GET',
    	    url:"/get-link",
    	    contentType: 'text',
    	    success: function(data) {
    	    	$("#content-link").text(data);
    	    }
    	})
    });
    $('#shareLink').on("click",function()
    {
    	var lsEmail= '';
    	var link= $("#content-link").val();
    	$('#myTable tbody tr').each(function() {
    		var text= $(this).find("input").val();
    		if(text!=''){
    			lsEmail+=text+';';
    		}
    	 });
    	$.ajax({
    		dataType: 'text',
    		type: 'GET',
    	    url:"/share-link",
    	    contentType: 'text',
    	    data: {listEmail: lsEmail,link: link,sender: email},
    	    success: function(data) {
    	    	if(data="suscess")
    	    	{
    	    		$("#messaageAdd").text("Liên kết cuộc hội thoại đã được gửi.");
    	    	}
    	    	else
    	    		$("#messaageAdd").text("Liên kêt cuộc hội thoại không được gửi.");
    	    }
    	})
    });
});
function calculateRow(row) {
    var price = +row.find('input[name^="price"]').val();

}
function calculateGrandTotal() {
    var grandTotal = 0;
    $("table.order-list").find('input[name^="price"]').each(function () {
        grandTotal += +$(this).val();
    });
    $("#grandtotal").text(grandTotal.toFixed(2));
}
