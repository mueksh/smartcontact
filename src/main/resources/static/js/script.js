function openNav() {
  document.getElementById("mySidebar").style.width = "250px";
  document.getElementById("main").style.marginLeft = "250px";
}

function closeNav() {
  document.getElementById("mySidebar").style.width = "0";
  document.getElementById("main").style.marginLeft= "0";
}



//search contacts 

const search=()=> {
	//console.log("serach..")
	
	let query = $("#search-input").val();
	
	if(query==""){
		
	$(".search-result").hide();
	}else{
		
		console.log(query);
		
		//sending request to server
		
		let url=`http://localhost:8080/search/${query}`;
		
		fetch(url)
		.then((response)=>{
			return response.json();
			})
			.then((data)=>{
				//data...
				console.log(data);
				
				let text=`<div class='list-group'>`;
				
				data.forEach((contact)=>{
					text+=`<a href='/user/${contact.cId}/contact' class='list-group-item list-group'>${contact.name}</a>`
			});
			text +=`</div>`
			$(".search-result").html(text);
			$(".search-result").show();
	
		});
		
 }

};



