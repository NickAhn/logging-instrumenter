package com.microservices.instrumentation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;




public class LoggingInstrumentation {

	// input json file content
	private static String jsonFile;

	// input system paths file content
	private static String pathsFile;

	// input path to SWI prolog
	private static String swiPath;

	// list of system paths
	private static List<String> systemPaths = new ArrayList<String>();

	// map from trigger system paths to lists of trigger pointcuts
	private static Map<String, List<String>> triggerSystemPathMaps = new HashMap<String, List<String>>();

	// map from logging event system paths to lists of logging event pointcuts
	private static Map<String, List<String>> loggingEventSystemPathMaps = new HashMap<String, List<String>>();

	// list of pom.xml files that need to be edited
	private static List<File> modifiedPomXMLs = new ArrayList<File>();

	// AOP dependency that is added to pom.xml files
	private static final String aopDependency = "\t<dependency>\n" + 
			"			<groupId>org.springframework.boot</groupId>\n" + 
			"			<artifactId>spring-boot-starter-aop</artifactId>\n" + 
			"		</dependency>\n\t";

	// local path to resource files (db's, application-properties, etc.)
	private static final String resourcesPath = "/src/main/resources";

	// local path to source code files 
	private static final String codePath = "/src/main/java";

	// path to LocalDBController.java template for triggers
	private static final String localDBControllerTemplateFilePath = 
			"./code-templates/LocalDBController-template.txt";

	// path to LoggingAspect.java template for triggers
	private static final String TriggerLoggingAspectTemplateFilePath = 
			"./code-templates/LoggingAspect-trigger-template.txt";

	// template for before aspects in triggers
	private static final String TriggerBeforeAspect = 
			"@Before(\"execution (* <POINTCUT>(..))\")\n" + 
					"	public void <METHOD_NAME>(JoinPoint joinPoint) throws Throwable	{\n" + 
					"		// callEvent prefix realization		\n" + 
					"		logger.info(\"Before aspect for {}\", joinPoint);\n" + 
					"		String precond = buildPreCond(joinPoint);\n" + 
					"		appendFile(precond, localDBPath);\n" + 
					"		logger.info(precond + \" appended to local DB.\");\n" + 
					"	}\n\t";

	// path to RestClient.java template for triggers
	private static final String restClientTemplateFilePath = 
			"./code-templates/RestClient-template.txt";

	// path to EngineCommunication.java template for logging events
	private static final String engineCommTemplateFilePath = 
			"./code-templates/EngineCommunication-template.txt";


	// path to LoggingAspect.java template for logging events
	private static final String LoggingEventLoggingAspectTemplateFilePath = 
			"./code-templates/LoggingAspect-loggingevent-template.txt";

	// template for before aspects in logging events
	private static final String LoggingEventBeforeAspect = 
			"@Before(\"execution (* <POINTCUT>(..))\")\n" + 
					"	public void <METHOD_NAME>(JoinPoint joinPoint) throws Throwable	{\n" + 
					"		// callEvent prefix realization		\n" + 
					"		logger.info(\"Before aspect for {}\", joinPoint);\n" + 
					"		String precond = buildPreCond(joinPoint);\n" + 
					"		appendFile(precond, localDBPath);\n" + 
					"		logger.info(precond + \" appended to local DB.\");\n" + 
					"		\n" + 
					"		// send GET to triggers and collect the results\n" + 
					"		// addPrecond prefix realization\n" + 
					"		writeFile(\"\", remoteDBPath);\n" + 
					"		String content;\n" +
					"		<GET_REQS>" + 
					"		\n" + 
					"		// initialize Prolog with LS, local and remote data\n" + 
					"		// check if loggedfunccall is derivable and update log accordingly\n" + 
					"		this.ec = new EngineCommunication();\n" + 
					"		addLoggingSpec();\n" + 
					"		addAssertionsFromDB(localDBPath);\n" + 
					"		addAssertionsFromDB(remoteDBPath);\n" + 
					"		ec.query(G, T);\n" + 
					"		writeFile(ec.getQueryResult(), logDBPath);\n" + 
					"		ec.turnOff();\n" + 
					"	}";
	
	
	// template for sending GET request to each trigger by logging event
	private static final String getRequestToTriggerTemplate = 
			"content = restClinet.getPostsPlainJSON(\"http://localhost:<PORT>/localdb\");\n" + 
					"		if (content != null) {\n" + 
					"			appendFile(content, remoteDBPath);\n" + 
					"		}\n" + 
					"		logger.info(content + \" appended to remote DB.\");\n\t\t";



	// collection of json objects populated by logging spec
	private JSONObject jsonWholeObj;
	private JSONArray logprogObj;
	private JSONObject[] fullHornClauseObj;
	private String[] hornClauseType;
	private JSONObject[] mainHornClauseObj;
	private JSONObject[] fullHeadObj;
	private String[] headType;
	private JSONObject[] headLiteralObj;
	private String[] headLiteralSymbolType;
	private String[] headLiteralType;
	private String[] headLiteralName;
	private JSONArray headLiteralArgsObj;
	private JSONObject[][] headLiteralArgObj;
	private String[][] headLiteralArgType;
	private String[][] headLiteralArgName;
	private JSONArray bodyObj;
	private JSONObject[][] bodyLiteralObj;
	private String[][] bodyLiteralSymbolType;
	private String[][] bodyLiteralType;
	private String[][] bodyLiteralName;
	private JSONArray bodyLiteralArgsObj;
	private JSONObject[][][] bodyLiteralArgObj;
	private String[][][] bodyLiteralArgType;
	private String[][][] bodyLiteralArgName;
	

	// lists of all pointcuts, trigger pointcuts, and logging event pointcuts
	private List<String> pointCuts = new ArrayList<String>();
	private List<String> triggerPointCuts = new ArrayList<String>();
	private List<String> loggingEventPointCuts = new ArrayList<String>();

	// map from logging event pointcut to list of its trigger pointcuts
	private Map<String, List<String>> loggingEventToTriggersMap = new HashMap<String, List<String>>();


	public static void main(String[] args) {

		if (args.length != 3) {
			System.err.println("ERROR: Need to pass "
					+ "1) logging specification file, "
					+ "2) subsystem paths file, and "
					+ "3) path to SWI Prolog engine.");
			return;
		}

		jsonFile = args[0];
		pathsFile = args[1];
		swiPath = args[2];

		new LoggingInstrumentation();
	}

	public LoggingInstrumentation() {
		initData();
		extractPointCuts(); 
		setSystemPaths(); 
		editPomXMLsAndSystemPaths();
		addDbFiles();

		addLoggingEventRestClient();
		addLoggingEventEngineComm();
		addLoggingEventLoggingAspect();
		
		addTriggerLocalDBController();
		addTriggerLoggingAspect();
		

	}

	private void initData() {
		String jsonContent = readFile(jsonFile);
		jsonWholeObj = new JSONObject(jsonContent);
		logprogObj = jsonWholeObj.getJSONArray("logprog");
		fullHornClauseObj = new JSONObject [logprogObj.length()];
		hornClauseType = new String [logprogObj.length()];
		mainHornClauseObj = new JSONObject [logprogObj.length()];
		fullHeadObj = new JSONObject [logprogObj.length()];
		headType = new String [logprogObj.length()];
		headLiteralObj = new JSONObject [logprogObj.length()];
		headLiteralSymbolType = new String [logprogObj.length()];
		headLiteralType = new String [logprogObj.length()];
		headLiteralName = new String [logprogObj.length()];
		headLiteralArgObj = new JSONObject [logprogObj.length()] [];
		headLiteralArgType = new String [logprogObj.length()] [];
		headLiteralArgName = new String [logprogObj.length()] [];
		bodyLiteralObj = new JSONObject [logprogObj.length()] [];
		bodyLiteralSymbolType = new String [logprogObj.length()] [];
		bodyLiteralType = new String [logprogObj.length()] [];
		bodyLiteralName = new String [logprogObj.length()] [];
		bodyLiteralArgObj = new JSONObject [logprogObj.length()] [] [];
		bodyLiteralArgType = new String [logprogObj.length()] [] [];
		bodyLiteralArgName = new String [logprogObj.length()] [] [];

		for (int i = 0; i < logprogObj.length(); i++) {
			fullHornClauseObj[i] = logprogObj.getJSONObject(i);
			hornClauseType[i] = fullHornClauseObj[i].getString("hc_type");
			mainHornClauseObj[i] = fullHornClauseObj[i].getJSONObject("logic_clause");
			fullHeadObj[i] = mainHornClauseObj[i].getJSONObject("head");
			headType[i] = fullHeadObj[i].getString("head_type");
			headLiteralObj[i] = fullHeadObj[i].getJSONObject("literal");
			headLiteralSymbolType[i] = headLiteralObj[i].getString("symbol_type");
			headLiteralType[i] = headLiteralObj[i].getString("literal_type");
			headLiteralName[i] = headLiteralObj[i].getString("literal_name");
			headLiteralArgsObj = headLiteralObj[i].getJSONArray("args");
			headLiteralArgObj[i] = new JSONObject [headLiteralArgsObj.length()];
			headLiteralArgType[i] = new String [headLiteralArgsObj.length()];
			headLiteralArgName[i] = new String [headLiteralArgsObj.length()];
			for (int j = 0; j < headLiteralArgsObj.length(); j++){
				headLiteralArgObj[i][j] = headLiteralArgsObj.getJSONObject(j);
				headLiteralArgType[i][j] = headLiteralArgObj[i][j].getJSONObject("arg").getString("arg_type");
				headLiteralArgName[i][j] = headLiteralArgObj[i][j].getJSONObject("arg").getString("arg_name");
			}
			bodyObj = mainHornClauseObj[i].getJSONArray("body");
			bodyLiteralObj[i] = new JSONObject [bodyObj.length()];
			bodyLiteralSymbolType[i] = new String [bodyObj.length()];
			bodyLiteralType[i] = new String [bodyObj.length()];
			bodyLiteralName[i] = new String [bodyObj.length()];
			bodyLiteralArgObj[i] = new JSONObject [bodyObj.length()] [];
			bodyLiteralArgType[i] = new String [bodyObj.length()] [];
			bodyLiteralArgName[i] = new String [bodyObj.length()] [];
			for (int k = 0; k < bodyObj.length(); k++){
				bodyLiteralObj[i][k] = bodyObj.getJSONObject(k).getJSONObject("literal");
				bodyLiteralSymbolType[i][k] = bodyLiteralObj[i][k].getString("symbol_type");
				bodyLiteralType[i][k] = bodyLiteralObj[i][k].getString("literal_type");
				bodyLiteralName[i][k] = bodyLiteralObj[i][k].getString("literal_name");
				bodyLiteralArgsObj = bodyLiteralObj[i][k].getJSONArray("args");
				bodyLiteralArgObj[i][k] = new JSONObject [bodyLiteralArgsObj.length()];
				bodyLiteralArgType[i][k] = new String [bodyLiteralArgsObj.length()];
				bodyLiteralArgName[i][k] = new String [bodyLiteralArgsObj.length()];
				for (int j = 0; j < bodyLiteralArgsObj.length(); j++){
					bodyLiteralArgObj[i][k][j] = bodyLiteralArgsObj.getJSONObject(j);
					bodyLiteralArgType[i][k][j] = 
							bodyLiteralArgObj[i][k][j].getJSONObject("arg").getString("arg_type");
					bodyLiteralArgName[i][k][j] = 
							bodyLiteralArgObj[i][k][j].getJSONObject("arg").getString("arg_name");
				}
			}			
		}
	}

	private void extractPointCuts(){
		for(int i = 0; i < fullHornClauseObj.length; i++){
			if (hornClauseType[i].equals("log_spec")){ // for all logging specs
				for (int k = 0; k < bodyLiteralName[i].length; k++){
					if (bodyLiteralName[i][k].equals("funccall")){ // if the body literal is an event or trigger
						//TODO: check if bodyLiteralType is negative_trigger AND add to json file
						if (bodyLiteralType[i][k].equals("negative_trigger")) {
							System.out.println("Found negative trigger!");
						}
						
						
						// extract the pointcut from the method path
						String pointCut = bodyLiteralArgName[i][k][2];
						// add the pointcut to the list of advice points
						if (!pointCuts.contains(pointCut)){
							pointCuts.add(pointCut);
						}
					}
				}
				// populate logging event pointcuts list, and
				// populate logging event to triggers map
				for (int k = 0; k < headLiteralName.length; k++) {
					if (headLiteralName[k].equals("loggedfunccall")) {
						if (!loggingEventPointCuts.contains(headLiteralArgName[k][2])) {
							loggingEventPointCuts.add(headLiteralArgName[k][2]);

							List<String> pcList = new ArrayList<String>();
							for(int j = 0; j < bodyLiteralName[k].length; j++) {
								if (bodyLiteralName[k][j].equals("funccall")) {
									String pointCut = bodyLiteralArgName[k][j][2];
									if (!pointCut.equals(headLiteralArgName[k][2])
											&& !pcList.contains(pointCut)) {
										pcList.add(pointCut);
									}
								}
							}
							loggingEventToTriggersMap.put(headLiteralArgName[k][2], pcList);
						}
						
					}
				}
			}
		}
		// populate trigger pointcut list
		for (String pointcut : pointCuts) {
			if (!loggingEventPointCuts.contains(pointcut)) {
				triggerPointCuts.add(pointcut);
			}
		}
	}

	private void setSystemPaths() {
		systemPaths = Arrays.asList(readFile(pathsFile).split("\\n"));
	}

	private void editPomXMLsAndSystemPaths() {
		for (String systemPath : systemPaths) {
			try {
				String pomFilePath = systemPath + "/pom.xml";
				File pomFile = new File(pomFilePath);
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(pomFile);
				doc.getDocumentElement().normalize();
				String groupId = doc.getElementsByTagName("groupId").item(1).getTextContent();
				String artifactId = doc.getElementsByTagName("artifactId").item(1).getTextContent();
				String nameFromPath = systemPath.split("/")[systemPath.split("/").length-1];
				for (String pointCut : pointCuts) {
					if (pointCut.startsWith(groupId) &&
							artifactId.equals(nameFromPath)) {
						// add aop dependency to pom.xml
						modifiedPomXMLs.add(pomFile);
						String pomFileContent = readFile(pomFilePath);
						if (!pomFileContent.contains("spring-boot-starter-aop")) {
							int index = pomFileContent.indexOf("</dependencies>");
							pomFileContent = insertStringIntoIndex(pomFileContent, aopDependency, --index);
							writeFile(pomFileContent, pomFilePath);
						}
						// populate trigger system paths map
						if (triggerPointCuts.contains(pointCut)) {
							if (!triggerSystemPathMaps.containsKey(systemPath)) {
								List<String> newPCList = new ArrayList<String>();
								newPCList.add(pointCut);
								triggerSystemPathMaps.put(systemPath, newPCList);
							}
							else {
								List<String> oldPCList = triggerSystemPathMaps.get(systemPath);
								oldPCList.add(pointCut);
								triggerSystemPathMaps.remove(systemPath);
								triggerSystemPathMaps.put(systemPath, oldPCList);
							} 

						}
						// populate logging event system paths map
						else {
							if (!loggingEventSystemPathMaps.containsKey(systemPath)) {
								List<String> newPCList = new ArrayList<String>();
								newPCList.add(pointCut);
								loggingEventSystemPathMaps.put(systemPath, newPCList);
							}
							else {
								List<String> oldPCList = loggingEventSystemPathMaps.get(systemPath);
								oldPCList.add(pointCut);
								loggingEventSystemPathMaps.remove(systemPath);
								loggingEventSystemPathMaps.put(systemPath, oldPCList);
							} 
						}
					}
				}

			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void addDbFiles() {
		for (String systemPath : systemPaths) {
			if (triggerSystemPathMaps.containsKey(systemPath)) {
				try {
					File localDBFile = new File(systemPath + resourcesPath + "/local-db.txt");
					localDBFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (loggingEventSystemPathMaps.containsKey(systemPath)) {
				try {
					File localDBFile = new File(systemPath + resourcesPath + "/local-db.txt");
					localDBFile.createNewFile();
					File remoteDBFile = new File(systemPath + resourcesPath + "/remote-db.txt");
					remoteDBFile.createNewFile();
					File logDBFile = new File(systemPath + resourcesPath + "/log-db.txt");
					logDBFile.createNewFile();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void addTriggerLocalDBController() {
		String content;
		for (String systemPath : systemPaths) {
			if (triggerSystemPathMaps.containsKey(systemPath)) {
				// select first pointcut to build path to LocalDBController.java
				// each trigger system needs one such file
				String pointcut = triggerSystemPathMaps.get(systemPath).get(0);
				try {
					//create the file
					String filePath = systemPath + sourceCodePath(pointcut) + "/LocalDBController.java";
					File file = new File(filePath);
					file.createNewFile();

					// rewrite content
					content = readFile(localDBControllerTemplateFilePath);
					content = content.replace("<PACKAGE>", packageName(pointcut));
					content = content.replace("<LOCAL_DB_PATH>", 
							"\"" + systemPath + resourcesPath + "/local-db.txt\"");
					writeFile(content, filePath);					
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
		}
	}

	private void addTriggerLoggingAspect() {
		String content;
		for (String systemPath : systemPaths) {
			String aspects = "";
			int counter = 0;
			if (triggerSystemPathMaps.containsKey(systemPath)) {
				// select first pointcut to build path to LoggingAspect.java
				// each trigger system needs one such file
				String pointcut = triggerSystemPathMaps.get(systemPath).get(0);
				try {
					//create the file
					String filePath = systemPath + sourceCodePath(pointcut) + "/LoggingAspect.java";
					File file = new File(filePath);
					if (!file.exists()) {
						file.createNewFile();
						
						// rewrite content
						content = readFile(TriggerLoggingAspectTemplateFilePath);
						content = content.replace("<PACKAGE>", packageName(pointcut));
						content = content.replace("<LOCAL_DB_PATH>", 
								"\"" + systemPath + resourcesPath + "/local-db.txt\"");
						for (String pc : triggerSystemPathMaps.get(systemPath)) {
							aspects += TriggerBeforeAspect;
							aspects = aspects.replace("<POINTCUT>", pc);
							aspects = aspects.replace("<METHOD_NAME>", "before" + counter);
							counter++;
						}
						content = content.replace("<ASPECTS>", aspects);
						writeFile(content, filePath);
					}
					else { // created by addLoggingEventLoggingAspect
						content = readFile(filePath);
						for (String pc : triggerSystemPathMaps.get(systemPath)) {
							aspects += TriggerBeforeAspect;
							aspects = aspects.replace("<POINTCUT>", pc);
							aspects = aspects.replace("<METHOD_NAME>", "beforeT" + counter);
							counter++;
						}
						content = content.replace(", if any", "\n\t" + aspects);
						writeFile(content, filePath);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
		}

	}

	private void addLoggingEventRestClient() {
		String content;
		for (String systemPath : systemPaths) {
			if (loggingEventSystemPathMaps.containsKey(systemPath)) {
				// select first pointcut to build path to RestClient.java
				// each logging event system needs one such file
				String pointcut = loggingEventSystemPathMaps.get(systemPath).get(0);
				try {
					//create the file
					String filePath = systemPath + sourceCodePath(pointcut) + "/RestClient.java";
					File file = new File(filePath);
					file.createNewFile();

					// rewrite content
					content = readFile(restClientTemplateFilePath);
					content = content.replace("<PACKAGE>", packageName(pointcut));
					writeFile(content, filePath);					
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
		}

	}
	//TODO
	private void addLoggingEventEngineComm() {
		String content;
		for (String systemPath : systemPaths) {
			if (loggingEventSystemPathMaps.containsKey(systemPath)) {
				// select first pointcut to build path to EngineCommunication.java
				// each logging event system needs one such file
				String pointcut = loggingEventSystemPathMaps.get(systemPath).get(0);
				try {
					//create the file
					String filePath = systemPath + sourceCodePath(pointcut) + "/EngineCommunication.java";
					File file = new File(filePath);
					file.createNewFile();

					// rewrite content
					content = readFile(engineCommTemplateFilePath);
					content = content.replace("<PACKAGE>", packageName(pointcut));
					content = content.replace("<SWI_PATH>", "\"" + swiPath + "\"");
					writeFile(content, filePath);					
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
		}
	}

	private void addLoggingEventLoggingAspect() {
		String content;
		String aspects = "";
		int counter = 0;
		String getReqs = "";
		for (String systemPath : systemPaths) {
			if (loggingEventSystemPathMaps.containsKey(systemPath)) {
				// select first pointcut to build path to LoggingAspect.java
				// each logging event system needs one such file
				String pointcut = loggingEventSystemPathMaps.get(systemPath).get(0);
				try {
					//create the file
					String filePath = systemPath + sourceCodePath(pointcut) + "/LoggingAspect.java";
					File file = new File(filePath);
					file.createNewFile();

					// rewrite content
					content = readFile(LoggingEventLoggingAspectTemplateFilePath);
					content = content.replace("<PACKAGE>", packageName(pointcut));
					content = content.replace("<LOCAL_DB_PATH>", 
							"\"" + systemPath + resourcesPath + "/local-db.txt\"");
					content = content.replace("<REMOTE_DB_PATH>", 
							"\"" + systemPath + resourcesPath + "/remote-db.txt\"");
					content = content.replace("<LOG_DB_PATH>", 
							"\"" + systemPath + resourcesPath + "/log-db.txt\"");
					for (String loggingEventPC : loggingEventSystemPathMaps.get(systemPath)) {
						aspects += LoggingEventBeforeAspect;
						aspects = aspects.replace("<POINTCUT>", loggingEventPC);
						aspects = aspects.replace("<METHOD_NAME>", "before" + counter);
						counter++;

						getReqs = "";
						//list of different trigger system paths
						List<String> tPaths = new ArrayList<String>(); 
						//for each trigger pointcut from a different system path
						//logging event needs to contact that trigger
						for (String triggerPC : loggingEventToTriggersMap.get(loggingEventPC)) {
							String triggerSystemPath = reverseSystemPathLookup(triggerPC);
							if (!reverseSystemPathLookup(loggingEventPC).equals(triggerSystemPath) 
									&& !tPaths.contains(triggerSystemPath)) {
								getReqs += getRequestToTriggerTemplate;
								getReqs = getReqs.replace("<PORT>", 
										getPort(reverseSystemPathLookup(triggerPC)));
								tPaths.add(triggerSystemPath);
							}
						}
						aspects = aspects.replace("<GET_REQS>", getReqs);
					}
					content = content.replace("<ASPECTS>", aspects);
					content = content.replace("<DEFINE_PREDS>", definePredicates());
					content = content.replace("<ASSERT_CLAUSES>", assertClauses());
					writeFile(content, filePath);					
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
		}

	}
	
	//TODO: assertClauses for lg
	private String assertClauses() {
		String assert_clauses = "";
		for(int i = 0; i < headLiteralName.length; i++){
			assert_clauses += "ec.addFact(\"assert(("; 
			if (headType[i].equals("pred")){
				assert_clauses += headLiteralName[i] + "(";
				for (int j = 0; j < headLiteralArgName[i].length; j++){
					if (headLiteralName[i].equals("loggedfunccall") && j == 2) {
						assert_clauses += "\\\"" + headLiteralArgName[i][j] + "\\\"";
					}
					else {
						assert_clauses += headLiteralArgName[i][j];
					}
					if (j != headLiteralArgName[i].length - 1) { // if not the last arg, put a comma
						assert_clauses += ", ";
					}
				}
				assert_clauses += ")";
				
			} 			
			else { // head type is empty!
				assert_clauses += "false";
			} 
			if (bodyLiteralName[i].length != 0){ // if body is not empty
				assert_clauses += " :- ";
				for (int k = 0; k < bodyLiteralName[i].length; k++){
					if (bodyLiteralType[i][k].equals("negative_trigger")) {
						System.out.println("break");
						continue;
					}
					
					assert_clauses += bodyLiteralName[i][k] + "(";
					for (int j = 0; j < bodyLiteralArgName[i][k].length; j++){
						//TODO: check if the type is negative trigger (if so, skip)
						if (bodyLiteralName[i][k].equals("funccall") && j == 2) {
							assert_clauses += "\\\"" + bodyLiteralArgName[i][k][j] + "\\\"";
						}
						else {
							assert_clauses += bodyLiteralArgName[i][k][j];
						}
						if (j != bodyLiteralArgName[i][k].length - 1) { // if not the last arg, put a comma
							assert_clauses += ", ";
						}
					}
					assert_clauses += ")";
					if (k != bodyLiteralName[i].length - 1){ // if not the last body literal, put a comma (conj)
						System.out.println(assert_clauses);
						assert_clauses += ", ";
						System.out.println(assert_clauses);
					}
					// set the goal and goal vars
					if (headLiteralName[i].equals("loggedfunccall")) {
						for (int j = 0; j < headLiteralArgName[i].length; j++){
							if (j != headLiteralArgName[i].length - 1) { // if not the last arg, put a comma
							}
						}
					}
				}
			}		
			assert_clauses += "))\");\n\t\t";
		}
		return assert_clauses;	
	}

	private String definePredicates() {
		String define_predicates = "";
		for(int i = 0; i < headLiteralName.length; i++){
			if (headLiteralSymbolType[i].equals("user_defined")){
				define_predicates += "ec.addFact(\"dynamic " + headLiteralName[i] + 
						"/" + headLiteralArgName[i].length +" as incremental\"); \n\t\t";
			}

			for (int k = 0; k < bodyLiteralName[i].length; k++){
				if (bodyLiteralSymbolType[i][k].equals("user_defined")){
					define_predicates += "ec.addFact(\"dynamic " + bodyLiteralName[i][k] + 
							"/" + bodyLiteralArgName[i][k].length +" as incremental\"); \n\t\t";
				}				
			}
		}

		return define_predicates;
	}

	private String getPort(String systemPath) {
		String applicationPropFilePath = systemPath + resourcesPath + "/application.properties";
		String applicationPropFileContent = readFile(applicationPropFilePath);
		String[] contentArr = applicationPropFileContent.split("\\s+");
		for (int i = 0; i < contentArr.length; i++) {
			if (contentArr[i].equals("server.port")) {
				return contentArr[i+2];
			} 
		}
		return "8080";
	}

	private String reverseSystemPathLookup(String pointCut) {
		for (String systemPath : systemPaths) {
			if (triggerSystemPathMaps.containsKey(systemPath)) {
				if (triggerSystemPathMaps.get(systemPath).contains(pointCut)) {
					return systemPath;
				}
			}
			if (loggingEventSystemPathMaps.containsKey(systemPath)) {
				if (loggingEventSystemPathMaps.get(systemPath).contains(pointCut)) {
					return systemPath;
				}
			}
		}
		return null;
	}

	private String sourceCodePath(String pointCut) {
		String[] pathArr = pointCut.split("\\.");
		String path = "";
		for (int i = 0; i < pathArr.length - 2; i++) {
			path += "/" + pathArr[i];
		}
		return codePath + path;
	}

	private String packageName (String pointCut) {
		String[] nameArr = pointCut.split("\\.");
		String name = nameArr[0];
		for (int i = 1; i < nameArr.length - 2; i++) {
			name += "." + nameArr[i];
		}
		return name;
	}

	private String insertStringIntoIndex(String main, String inserted, int index) { 
		String str = new String(); 
		for (int i = 0; i < main.length(); i++) { 
			str += main.charAt(i); 
			if (i == index) { 
				str += inserted; 
			} 
		}
		return str; 
	} 

	private static String readFile(String filePath) {

		String content = "";
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line = reader.readLine();
			while (line != null) {
				content += line + "\n";
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) { 
			e.printStackTrace();
		}
		return content;
	}

	private void writeFile(String st, String filePath) {
		try {
			PrintWriter out = new PrintWriter(filePath, "UTF-8");
			out.print(st);
			out.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}
