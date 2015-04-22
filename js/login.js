	
function login()
{
	var poster = new XMLHttpRequest(); // http://stackoverflow.com/questions/692196/post-request-javascript
	//I need to get the name of the server
	var parameters = "username=" + document.getElementById('name').value + "&password=" +document.getElementById('password').value;
	poster.open("POST", "index.html", true);
	poster.send(parameters);	
}