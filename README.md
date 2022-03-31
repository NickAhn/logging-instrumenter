# logging-microservices
LogInst.v2: An instrumentation tool for microservices-based web applications that are written using Java Spring Boot, to enable audit logging.

This repo consists of the following details:
1. Logging instrumentation tool in Java (LogInst.v2)
  This tool receives as input the following: 1) The logical specification of logging requirements is passed as an argument in JSON format. LogInst parses this JSON file and extracts logging specfication in the form of Horn clauses, along with identifying triggers and logging events. 2) The microservices-based application is passed as another argument. For this purpose, LogInst.v2 receives a text file with paths to different microservices. These microservices are supposed to be built by Java Spring Framework. LogInst.v2 applies modifications to each microservice component according to the logging specification.	3) The last argument is the path to the Prolog engine in the local system. It uses SWI Prolog to logically infer the derivation of logging events according to the logging specification, the set of facts regarding trigger events, as well as any other collection of facts that are potentially needed.
  
2. A sample web application: a medical records system

3. Four versions of instrumented sample web application according to four logging specifications. The login specifications are defined using JSON.
