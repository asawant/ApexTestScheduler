package com.persistent.sfdc.plugin;

import java.rmi.RemoteException;
import java.util.List;

import com.sforce.soap.apex.CodeCoverageResult;
import com.sforce.soap.apex.Connector;
import com.sforce.soap.apex.RunTestFailure;
import com.sforce.soap.apex.RunTestsRequest;
import com.sforce.soap.apex.RunTestsResult;
import com.sforce.soap.apex.SoapConnection;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.SaveResult;
import com.sforce.soap.enterprise.sobject.Code_Coverage__c;
import com.sforce.soap.enterprise.sobject.Coverage_Line_Item__c;
import com.sforce.soap.enterprise.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * anand_sawant 03/08/2013 3:42:12 PM
 */
public class RunAllTests {

	static SoapConnection connection;
	static EnterpriseConnection econnection;
	
	/**
	 * @param args
	 * @throws ConnectionException
	 * @throws RemoteException
	 */
	public static void main(String[] args) throws RemoteException,
			ConnectionException {
		runAllTests();

	}

	public static String runAllTests() throws ConnectionException,
			RemoteException {
		StringBuffer sbuff = new StringBuffer("");
		ConnectorConfig config = new ConnectorConfig();
		config.setUsername("asawant02@dev.com");
		config.setPassword("test@234");
		config.setAuthEndpoint("https://login.salesforce.com/services/Soap/c/27.0");
		econnection = new EnterpriseConnection(config);
		config.setServiceEndpoint(config.getServiceEndpoint());
		
		
		// display some current settings
		sbuff.append("Auth EndPoint: " + config.getAuthEndpoint());
		sbuff.append("Service EndPoint: " + config.getServiceEndpoint());
		sbuff.append("Username: " + config.getUsername());
		
		ConnectorConfig config1 = new ConnectorConfig();
		config1.setUsername("asawant02@dev.com");
		config1.setPassword("test@234");
		config1.setAuthEndpoint("https://login.salesforce.com/services/Soap/c/27.0");
		
		config1.setSessionId(econnection.getSessionHeader().getSessionId());
		config1.setServiceEndpoint("https://ap1-api.salesforce.com/services/Soap/s/27.0");
		connection = Connector.newConnection(config1);

		
		long start = System.currentTimeMillis();
		RunTestsResult res = null;
		RunTestsRequest rtr = new RunTestsRequest();
		rtr.setAllTests(true);
		res = connection.runTests(rtr);

		int totalLines = 0;
		int linesCoveredByTests = 0;
		sbuff.append("Number of tests: " + res.getNumTestsRun());
		sbuff.append("Number of failures: " + res.getNumFailures());
		
		Code_Coverage__c ccResult = new Code_Coverage__c();
		ccResult.setNumber_of_Failures__c((double) res.getNumFailures());
		ccResult.setNumber_of_Tests__c((double) res.getNumTestsRun());
		
		SaveResult[] createResult = econnection.create(new SObject[]{ccResult});
		String parentId = createResult[0].getId();
		sbuff.append(createResult[0].getSuccess());
		Coverage_Line_Item__c []ccLineItems = new Coverage_Line_Item__c[res.getCodeCoverage().length+res.getFailures().length];
		int p = 0;
		
		String testFailureDetails = "";
		if (res.getNumFailures() > 0) {
			for (RunTestFailure rtf : res.getFailures()) {
				testFailureDetails += "Failure: "
						+ (rtf.getNamespace() == null ? "" : rtf.getNamespace()
								+ ".") + rtf.getName() + "."
						+ rtf.getMethodName() + ": " + rtf.getMessage() + "\n"
						+ rtf.getStackTrace();
				sbuff.append(testFailureDetails);
				
				Coverage_Line_Item__c ccLineItem = new Coverage_Line_Item__c();
				ccLineItem.setCoverage_Line_Item__c(parentId);
				ccLineItem.setTotal_Lines__c((double) 0);
				ccLineItem.setLines_Not_Covered__c((double) 0);
				ccLineItem.setSource_File_Name__c(rtf.getName());
				ccLineItem.setType__c(rtf.getType());
				ccLineItem.setTest_Failure_Details__c(testFailureDetails);
				ccLineItems[p++]=ccLineItem;
			}
		}
		
		
	
		if (res.getCodeCoverage() != null) {
			for (CodeCoverageResult ccr : res.getCodeCoverage()) {
				 String coverageDetails = "Code coverage for " + ccr.getType() +
				 (ccr.getNamespace() == null ? "" : ccr.getNamespace() + ".")
				 + ccr.getName() + ": "
				 + ccr.getNumLocationsNotCovered()
				 + " locations not covered out of "
				 + ccr.getNumLocations();

				double coverage = ccr.getNumLocations() != 0 ? (ccr
						.getNumLocations() - ccr.getNumLocationsNotCovered())
						* 100 / ccr.getNumLocations() : 0;
				System.out.println("Coverage % for "
						+ ccr.getName()
						+ ": "
						+ coverage
						+ " Lines Covered: "
						+ (ccr.getNumLocations() - ccr
								.getNumLocationsNotCovered()) + " of "
						+ ccr.getNumLocations());
				totalLines += ccr.getNumLocations();
				linesCoveredByTests += (ccr.getNumLocations() - ccr
						.getNumLocationsNotCovered());
				Coverage_Line_Item__c ccLineItem = new Coverage_Line_Item__c();
				ccLineItem.setCoverage_Line_Item__c(parentId);
				ccLineItem.setTotal_Lines__c((double) ccr.getNumLocations());
				ccLineItem.setLines_Not_Covered__c((double) ccr.getNumLocationsNotCovered());
				ccLineItem.setSource_File_Name__c(ccr.getName());
				ccLineItem.setType__c(ccr.getType());
				ccLineItem.setCoverage_Details__c(coverageDetails);
				ccLineItems[p++]=ccLineItem;

				// if (ccr.getNumLocationsNotCovered() > 0) {
				// for (CodeLocation cl : ccr.getLocationsNotCovered())
				// System.out.println("\tLine " + cl.getLine());
				// }
			}
		}
		
		SaveResult[] cliResult = econnection.create(ccLineItems);
		sbuff.append(cliResult.length);
		sbuff.append("Overall Org Coverage: " + linesCoveredByTests * 100
				/ totalLines);
		sbuff.append("Finished in "
				+ (System.currentTimeMillis() - start) + "ms");
		
		return sbuff.toString();
	}

	
	public String createCodeCoverageRecord(CodeCoverage cc){
		String ccId = null;
		
		
		
		return ccId;
	}
	
	public boolean createCCLineItems(String ccId, List<CoverageLineItem> lstCLI){
		boolean created=false;
		
		
		return created;
	}
	
	
	public void createSObjects(List<SObject> lstSObj) throws ConnectionException{
		
		econnection.create((SObject[]) lstSObj.toArray());
		
	}
	
	
	public class CodeCoverage {
		public int getNumFailures() {
			return numFailures;
		}

		public void setNumFailures(int numFailures) {
			this.numFailures = numFailures;
		}

		public int getNumTests() {
			return numTests;
		}

		public void setNumTests(int numTests) {
			this.numTests = numTests;
		}

		int numFailures;
		int numTests;
	}

	public class CoverageLineItem {
		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public int getTotalLines() {
			return totalLines;
		}

		public void setTotalLines(int totalLines) {
			this.totalLines = totalLines;
		}

		public int getTotalLinesNotCovered() {
			return totalLinesNotCovered;
		}

		public void setTotalLinesNotCovered(int totalLinesNotCovered) {
			this.totalLinesNotCovered = totalLinesNotCovered;
		}

		public String getTestFailureDetails() {
			return testFailureDetails;
		}

		public void setTestFailureDetails(String testFailureDetails) {
			this.testFailureDetails = testFailureDetails;
		}

		public String getTestcoverageDetails() {
			return testcoverageDetails;
		}

		public void setTestcoverageDetails(String testcoverageDetails) {
			this.testcoverageDetails = testcoverageDetails;
		}

		public String getFkCodeCoverage() {
			return fkCodeCoverage;
		}

		public void setFkCodeCoverage(String fkCodeCoverage) {
			this.fkCodeCoverage = fkCodeCoverage;
		}

		String fileName;
		String type;
		int totalLines;
		int totalLinesNotCovered;
		String testFailureDetails;
		String testcoverageDetails;
		String fkCodeCoverage;
	}
}