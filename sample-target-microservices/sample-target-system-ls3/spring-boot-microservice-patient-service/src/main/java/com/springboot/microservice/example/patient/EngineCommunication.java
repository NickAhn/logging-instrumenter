package com.springboot.microservice.example.patient;

import java.util.ArrayList;

import com.declarativa.interprolog.SWISubprocessEngine;

public class EngineCommunication implements Runnable{
	private String G;
	private String T;
	private String GG;
	private Object[] solutions = new Object [100];
	private static SWISubprocessEngine engine;
	private String queryResult;
		
	private int assertionCount = 0;

	public EngineCommunication() {
		
		engine = new SWISubprocessEngine("/usr/bin");
		
		new Thread(this, "EngineT").start();
	}
	
	public void turnOff() {
		engine.shutdown();
	}


	public void run() {
		
	}
	

	public int query(String queryMsg, String queryResponseFormat) 
			throws Throwable{
		setGoal(queryMsg, queryResponseFormat);
        return resolve();
	}

	public void addFact(String assertion) {
		engine.command(assertion);
		updateAssertionCount(assertion);
	}	
	
	public String getQueryResult() {
		return queryResult;
	}
	
	private int resolve() throws Throwable{
		solutions = (Object[]) engine.deterministicGoal(GG,"[LM]")[0];
		ArrayList<Object> solutionList = new ArrayList<Object>();
		String queryResult = "Solution length: " + solutions.length 
				+ ".\n\n";
		for(int i=0; i < solutions.length; i++) {
			if (!solutionList.contains(solutions[i])) {
				solutionList.add(solutions[i]);
				queryResult += "(" + solutions[i] + ")\n\n";
			}
		}
		this.queryResult = queryResult;
		return solutions.length;
	}

	private void updateAssertionCount(String assertion) {
		if (assertion.startsWith("assert")) {
			assertionCount++;
		}
		if (assertion.startsWith("retract")) {
			assertionCount--;
		}
		return;		
	}

	public int getAssertionCount(){
		return assertionCount;
	}
	

	private void setGoal(String goal, String queryResponseFormat) {
		this.G = goal;
		this.T = queryResponseFormat;
		this.GG = "findall(TM, ("+G+",buildTermModel("+T+",TM)), L), "
				+ "ipObjectSpec('ArrayOfObject',L,LM)";
		return;
	}
}

