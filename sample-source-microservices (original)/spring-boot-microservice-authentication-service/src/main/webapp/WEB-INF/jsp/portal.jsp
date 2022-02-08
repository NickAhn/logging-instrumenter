<html>

<head>
<title>Demo Microservice-based Web Application</title>
</head>

<body>
	<H1>Demo Microservice-based Web Application</H1>
    Hello ${username}! <br/><br/><br/>
    
    <div>
    Access to Patient Microservice:
    
    <p id="demo"/>
    <script>
    	function renderer() {
    		if (${btg} || ${access}) {
    			return ` 
    <ul>
    	<li> Access to patient record by ID. Enter ID:
    		<input type="text" placeholder="Type something..." id="InputID">
    		<button type="button" onclick="getInputID();">Go!</button>
    	</li>
    	
    	<li> Access to patient record by name. Enter name:
    		<input type="text" placeholder="Type something..." id="InputName">
    		<button type="button" onclick="getInputName();">Go!</button>
    	</li>
    	
    	<li> Access to all patients with a certain disease. Enter disease:
    		<input type="text" placeholder="Type something..." id="InputDisease">
    		<button type="button" onclick="getInputDisease();">Go!</button>
    	</li>
    	
    	<li> <a href="./${username}/medical-history/patients" target="_blank">Click here</a> 
    		to access all patient records.
    	</li>
    </ul>`;
    		}
    		else {
    			return `<ul><li>You are not authorized to use Patient Microservice.</li>
    			<li>In order to have access to Patient Microservice break the glass 
    			and refresh this page. </li></ul>`;
    		}
    	}
    	document.getElementById("demo").innerHTML = renderer(); 
    </script>
    </div>
    
    <br/><br/>
    <div>
    Access to Authorization Microservice: 
    <ul>
    	<li> <a href="./authorization/access/${username}/to/patient-service" target="_blank">Click here</a> 
    		to see if you are authorized to use Patient Microservice.
    	</li>
    	
    	<li> <a href="./authorization/btg/for/${username}" target="_blank">Click here</a> 
    		to break the glass.
    	</li>
    	
    	<li> <a href="./authorization/mtg/for/${username}" target="_blank">Click here</a> 
    		to mend the glass.
    	</li>
    	
    	<li> <a href="./authorization/btg/users" target="_blank">Click here</a> 
    		to see which users have broken the glass.
    	</li> 
    	
    	<li> <a href="./authorization/btg/check/${username}" target="_blank">Click here</a> 
    		to see you have broken the glass.
    	</li>
    </ul>
    </div>
    
    <script> 
		function getInputID(){
			var inputVal = document.getElementById("InputID").value;
			window.open("./${username}/medical-history/id/" + inputVal);
		}
		
		function getInputName(){
			var inputVal = document.getElementById("InputName").value;
			window.open("./${username}/medical-history/name/" + inputVal);
		}
		
		function getInputDisease(){
			var inputVal = document.getElementById("InputDisease").value;
			window.open("./${username}/medical-history/disease/" + inputVal);
		}
    </script>
</body>

</html>