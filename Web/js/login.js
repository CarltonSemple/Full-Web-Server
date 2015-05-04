	
function login()
{
	var poster = new XMLHttpRequest(); // http://stackoverflow.com/questions/692196/post-request-javascript	
	var parameters = "username=" + document.getElementById('name').value + "&password=" +document.getElementById('password').value;
	// Prepare for response from server
	poster.onreadystatechange = function()
	{
		if(poster.readyState == 4 && poster.status == 200)
		{			
			// If the user is logged in, receive the variables
			if(poster.responseText.indexOf("=") > -1)
			{
				var variables = poster.responseText.substring(0);
				window.location.href="fitnessup.html" + "?" + variables;	
			}
			else
				document.getElementById("StatusArea").innerHTML = poster.responseText;
		}
	}
	poster.open("POST", "login", true); // true = asynchronous (AJAX)
	poster.send(parameters);	
}