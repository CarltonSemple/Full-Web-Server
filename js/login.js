	
function login()
{
	var poster = new XMLHttpRequest(); // http://stackoverflow.com/questions/692196/post-request-javascript
	//I need to get the name of the server
	var parameters = "start-variables&username=" + document.getElementById('name').value + "&password=" +document.getElementById('password').value + "&end-variables";
	poster.open("POST", "index.html", true);
	poster.send(parameters);	
}