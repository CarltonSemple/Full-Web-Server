function saveEdits() 
{

//get the editable element
var editElems = {
        'edit1': document.getElementById('edit1').innerHTML,
        'edit2': document.getElementById('edit2').innerHTML,
		'edit3': document.getElementById('edit3').innerHTML,
		'edit4': document.getElementById('edit4').innerHTML,
		'edit5': document.getElementById('edit5').innerHTML,
		'edit6': document.getElementById('edit6').innerHTML
    };
	
//save the content to local storage. Stringify object as localstorage can only support string values
localStorage.setItem('userEdits', JSON.stringify(editElems));

//write a confirmation to the user
document.getElementById("update").innerHTML="Edits saved!";

}

function checkEdits()
{
    //find out if the user has previously saved edits
    var userEdits = localStorage.getItem('userEdits');
    
	if(userEdits)
	{
        userEdits = JSON.parse(userEdits);
		
        for(var elementId in userEdits)
		{
          document.getElementById(elementId).innerHTML = userEdits[elementId];
        }
    }
}