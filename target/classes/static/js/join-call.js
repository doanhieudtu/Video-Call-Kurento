$(document).ready(function () {
    var counter = 0;
    var email= document.getElementById('email').textContent;
    if(email!='')
    {
    	localStorage.setItem('email', email)
    }
})