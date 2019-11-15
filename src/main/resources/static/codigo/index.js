document.querySelector(".btn").addEventListener('click',registre)
document.querySelector(".eye").addEventListener('click',toglePassword)
let contador = 2
let interruptor = false
let img1 = document.querySelector("#mobile")
let img2 = document.querySelector("#mobile2")
setInterval(carrusel, 5000);
function carrusel() {
	interruptor = !interruptor
	if (interruptor) 
	{
		img1.addEventListener("animationend", reset);
		img1.classList.add("carruselOut")
		img2.classList.add("carruselentry")
	}else{
		img2.addEventListener("animationend", reset);
		img2.classList.add("carruselOut")
		img1.classList.add("carruselentry")
	}
	contador++
	contador == 5 ? contador = 1 : null
}

function reset() {
	if (interruptor) 
	{
		img1.classList.remove("carruselOut")
		img1.classList.add("left")
		img1.setAttribute("src", "img/mobile"+contador+".png");
		img2.classList.remove("carruselentry")
		img2.classList.remove("left")
	}else{
		img2.classList.remove("carruselOut")
		img2.classList.add("left")
		img2.setAttribute("src", "img/mobile"+contador+".png");
		img1.classList.remove("carruselentry")
		img1.classList.remove("left")
	}
	contador == 5 ? contador = 1 : null
}

function registre() 
{
 	let fullname = document.querySelector("input[name*=fullname]").value
    let username = document.querySelector("input[name*=Username]").value
    let password = document.querySelector("input[name*=password]").value
    if (fullname != "" && username != "" && password != "") {
	 	let formregistre = new FormData();
        formregistre.append("firstName",fullname)
        formregistre.append("email",username)
        formregistre.append("password",password)
	    fetch('/api/players',{
	        method:'POST',
	        body:formregistre
	    })
	    .then(function(response){
	        if(response.ok){
	        	console.log("nice registre")
	        	login()
	        }else{
	        	throw new Error(response.json());
	        }
	    })
	    .catch(error => error)
    	.then(json => console.log(json))
    }else if(username != "" && password != ""){
    	login()
    }else{
    	alert("Faltan Completar Algunos Campos")
    }
}

function login() {
	let username = document.querySelector("input[name*=Username]").value
	let password = document.querySelector("input[name*=password]").value
	console.log(username +" "+password )
	let formlogin = new FormData();
	formlogin.append("user-name",username)
	formlogin.append("user-password",password)
    fetch('/api/login',{
        method:'POST',
        body:formlogin
    })
    .then(function(response){
        if(response.ok){
            console.log("nicelog")
            location.assign("/web/games.html");
        }else{
        	throw new Error(response.json());
        }
    })
    .catch(error => error)
	.then(json => console.log(json))
}

function toglePassword(event) {
	let text = event.target.innerText
	if (text=="Ver")
	{
		document.querySelector("input[name*=password]").type = "text"
		event.target.innerText = "Ocultar"
	}else{
		document.querySelector("input[name*=password]").type = "password"
		event.target.innerText = "Ver"
	}
}