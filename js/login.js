	
function login()
{
	var poster = new XMLHttpRequest(); // http://stackoverflow.com/questions/692196/post-request-javascript	
	var parameters = "username=" + document.getElementById('name').value + "&password=" +document.getElementById('password').value;
	// Prepare for response from server
	poster.onreadystatechange = function()
	{
		if(poster.readyState == 4 && poster.status == 200)
			document.getElementById("StatusArea").innerHTML = poster.responseText;
	}
	poster.open("POST", "", true); // true = asynchronous (AJAX)
	poster.send(parameters);	
}