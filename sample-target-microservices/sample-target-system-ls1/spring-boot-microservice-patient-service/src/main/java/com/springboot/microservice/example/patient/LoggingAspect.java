package com.springboot.microservice.example.patient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;


@Aspect
@Configuration
public class LoggingAspect {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	static String localDBPath = 
			"/home/sep/Desktop/src_microservice/spring-boot-microservice-patient-service/src/main/resources/local-db.txt";
	static String remoteDBPath =
			"/home/sep/Desktop/src_microservice/spring-boot-microservice-patient-service/src/main/resources/remote-db.txt";
	static String logDBPath = 
			"/home/sep/Desktop/src_microservice/spring-boot-microservice-patient-service/src/main/resources/log-db.txt";

	
	@Autowired
	private Environment env;
	
	private RestClient restClinet = 
			new RestClient(new RestTemplateBuilder());
	
	private EngineCommunication ec;
	
	static String G = "loggedfunccall(X0,X1,X2,X3)"; // goal
	static String T = "(X0, X1, X2, X3)";	//response format
	
	//trigger aspects, if any

	//logging event aspects
	@Before("execution (* com.springboot.microservice.example.patient.PatientController.getPatientMedHistByName(..))")
	public void before0(JoinPoint joinPoint) throws Throwable	{
		// callEvent prefix realization		
		logger.info("Before aspect for {}", joinPoint);
		String precond = buildPreCond(joinPoint);
		appendFile(precond, localDBPath);
		logger.info(precond + " appended to local DB.");
		
		// send GET to triggers and collect the results
		// addPrecond prefix realization
		writeFile("", remoteDBPath);
		String content;
		content = restClinet.getPostsPlainJSON("http://localhost:8060/localdb");
		if (content != null) {
			appendFile(content, remoteDBPath);
		}
		logger.info(content + " appended to remote DB.");
				
		// initialize Prolog with LS, local and remote data
		// check if loggedfunccall is derivable and update log accordingly
		this.ec = new EngineCommunication();
		addLoggingSpec();
		addAssertionsFromDB(localDBPath);
		addAssertionsFromDB(remoteDBPath);
		ec.query(G, T);
		writeFile(ec.getQueryResult(), logDBPath);
		ec.turnOff();
	}

	private void addLoggingSpec( ) {
		ec.addFact("dynamic loggedfunccall/4 as incremental"); 
		ec.addFact("dynamic funccall/4 as incremental"); 
		ec.addFact("dynamic funccall/4 as incremental"); 
		
		ec.addFact("assert((loggedfunccall(T0, patient-service, \"com.springboot.microservice.example.patient.PatientController.getPatientMedHistByName\", [U, P]) :- funccall(T0, patient-service, \"com.springboot.microservice.example.patient.PatientController.getPatientMedHistByName\", [U, P]), funccall(T1, authorization-service, \"com.springboot.microservice.example.authorization.AuthorizationController.breakTheGlass\", [U]), <(T1, T0), ==(U, user)))");
		
	}
	
	private void addAssertionsFromDB(String filePath) {
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line = reader.readLine();
			while (line != null && line.length()>0) {
				logger.info("assert({})", line);
				ec.addFact("assert(" + line + ")");
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private String buildPreCond(JoinPoint joinPoint)  {
		String timestamp = ((Long) System.currentTimeMillis()).toString();
		String serviceName = 
				env.getRequiredProperty("spring.application.name");
		String methodName = "\"" + getMethodName( 
				joinPoint.getSignature().toString()) + "\"";
		String methodArg = arrayToString(joinPoint.getArgs());
		
		return "funccall(" + timestamp + 
				", " + serviceName +
				", "  + methodName + 
				", " + methodArg + ")";
	}
	
	private String arrayToString(Object[] array) {
		if (array.length == 0) return "[]";
		String str = "[" + array[0];
		for (int i = 1; i < array.length; i++) {
			str += ", " + array[i].toString().toLowerCase();
		}
		str += "]";
		return str;
	}
	
	private String getMethodName(String signature) {
		return (signature.split("\\s+")[1]).split("\\(")[0];
	}
	
	private void appendFile(String st, String filePath) 
			throws IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
		out.println(st);
		out.close();
	}
	
	private void writeFile(String st, String filePath) 
			throws IOException {
		PrintWriter out = new PrintWriter(filePath, "UTF-8");
		out.print(st);
		out.close();
	}


}
