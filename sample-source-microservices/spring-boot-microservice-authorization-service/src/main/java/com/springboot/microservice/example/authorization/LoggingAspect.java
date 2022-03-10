package com.springboot.microservice.example.authorization;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Aspect
@Configuration
public class LoggingAspect {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	static String localDBPath = 
			"/home/nickel/Documents/logging-microservices-master/sample-source-microservices/spring-boot-microservice-authorization-service/src/main/resources/local-db.txt";
	
	@Autowired
	private Environment env;
	
	@Before("execution (* com.springboot.microservice.example.authorization.AuthorizationController.getBTGUsers(..))")
	public void before1(JoinPoint joinPoint) throws Throwable	{
		// callEvent prefix realization		
		logger.info("Before aspect for {}", joinPoint);
		String precond = buildPreCond(joinPoint);
		appendFile(precond, localDBPath);
		logger.info(precond + " appended to local DB.");
	}
	
	@Before("execution (* com.springboot.microservice.example.authorization.AuthorizationController.breakTheGlass(..))")
	public void before0(JoinPoint joinPoint) throws Throwable	{
		// callEvent prefix realization		
		logger.info("Before aspect for {}", joinPoint);
		String precond = buildPreCond(joinPoint);
		appendFile(precond, localDBPath);
		logger.info(precond + " appended to local DB.");
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


}
