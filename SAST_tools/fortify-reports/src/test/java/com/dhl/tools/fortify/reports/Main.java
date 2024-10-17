package com.dhl.tools.fortify.reports;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fortify.ui.model.report.ReportCommandlineInterface;

public class Main {
    private static final String WORK_DIR = Messages.getString("Main.workdir");
    private static final String RESULT_XML_FILE = WORK_DIR+Messages.getString("Main.outxml");
    private static final String TEMPLATE_XL_FILE = Messages.getString("Main.template");
    
    final static List<String> emptylist = Arrays.asList("","","","","","","","","");

	public static void main(String[] args) throws InterruptedException {
		System.out.println("Use GenReport for better quality....");
		try {
			String fileFpr = "C:\\myDrive\\work\\SAST\\eCS BD Enterprice Portal\\ReScan\\reports\\Fortify-nonSD_DECScans_12345__2024-08-31v2024BDEnterprisePortal_entportal-commercial-service__31302377__2024-09-03.fpr";
			String projectName = new File(fileFpr).getName();

			ReportCommandlineInterface.main(new String[]{
					"-format", "xml",
					"-filterSet", Messages.getString("Main.9"),
					"-template", 
					Messages.getString("Main.11"),
					"-f", RESULT_XML_FILE,
					"-source", fileFpr});
//			ProcessBuilder processBuilder = new ProcessBuilder("ReportGenerator",
//					"java", "com.fortify.ui.model.report.ReportCommandlineInterface",
//					"-format", "xml",
//					"-filterSet", "DHL Audit Results View",
//					"-template", "export_owasp_xml_2021.xml",
//					"-f", RESULT_XML_FILE,
//					"-source", fileFpr);
//			Process process = processBuilder.start();
//            process.waitFor();
			try {
				FileUtil.extractZipFile(fileFpr, WORK_DIR);
			} catch (IOException e) {
				System.err.println("Error during extraction: " + e.getMessage());
				throw e;
			}
			
			// Parse XML files
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document tree = builder.parse(WORK_DIR+"/audit.fvdl");
			Document treeC = builder.parse(WORK_DIR+"/audit.xml");
			Document treeA = builder.parse(RESULT_XML_FILE);
			Element root = tree.getDocumentElement();
			Element rootC = treeC.getDocumentElement();
			Element rootA = treeA.getDocumentElement();
			
			// Initialize data structure
			List<List<String>> data = new ArrayList<>();
			List<String> header = List.of("Project_Name", "InstanceID", "OWASP_Category", "Issue_Name", "Kingdom", "AnalyzerName", "ClassID", 
					"Tool_Severity", "Issue_Severity", "Confidence", "File_Name", "Path", "Line_No", "Abstract", "Explanation", "Recommendations",
					"Comments", "Commented_By", "Business_Impact", "Agreed_Action", "Development team comments"); //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$ //$NON-NLS-17$ //$NON-NLS-18$ //$NON-NLS-19$ //$NON-NLS-20$ //$NON-NLS-21$
			data.add(header);
			
			// Process vulnerabilities
			NodeList vulnerabilities = root.getElementsByTagName("Vulnerability");
			for (int a = 0; a < vulnerabilities.getLength(); a++) {
				Element vulnerability = (Element) vulnerabilities.item(a);
				List<String> row = new ArrayList(emptylist);
				row.set(0, projectName.substring(0, projectName.lastIndexOf(".")));
				
				// Process ClassInfo
				NodeList classInfoList = vulnerability.getElementsByTagName("ClassInfo");
				Element classInfo = (Element) classInfoList.item(0);
				row.set(3, getTextContent(classInfo, "Type") + ": " + getTextContent(classInfo, "Subtype"));
				String classId = classInfo.getElementsByTagName("ClassID").item(0).getTextContent();
				NodeList descriptions = root.getElementsByTagName("Description");
				for (int descIndex = 0; descIndex < descriptions.getLength(); descIndex++) {
					Element desc = (Element) descriptions.item(descIndex);
					if (desc.getAttribute("classID").equals(classId)) {
						row.set(8, getTextContent(desc, "Recommendations"));
						continue;
					}
				}
				
				// Process InstanceInfo
				NodeList instanceInfoList = vulnerability.getElementsByTagName("InstanceInfo");
				Element instanceInfo = (Element) instanceInfoList.item(0);
				String instanceId = instanceInfo.getElementsByTagName("InstanceID").item(0).getTextContent();
				row.set(1, instanceId);
//					row.add(instanceInfo.getElementsByTagName("InstanceSeverity").item(0).getTextContent());
				row.add(instanceInfo.getElementsByTagName("Confidence").item(0).getTextContent());
				
				NodeList issueList = rootC.getElementsByTagName("Issue");
				for (int issueIndex = 0; issueIndex < issueList.getLength(); issueIndex++) {
					Element issue = (Element) issueList.item(issueIndex);
					if (issue.getAttribute("instanceId").equals(instanceId)) {
						row.add(issue.getElementsByTagName("Tag").item(0).getTextContent());
						// Process comments
						NodeList comments = issue.getElementsByTagName("Comment");
						StringBuilder commentsBuilder = new StringBuilder();
						for (int commentIndex = 0; commentIndex < comments.getLength(); commentIndex++) {
							Element comment = (Element) comments.item(commentIndex);
							commentsBuilder.append(comment.getElementsByTagName("Content").item(0).getTextContent()).append("\n\n");
						}
						row.add(commentsBuilder.toString());
						row.add(issue.getElementsByTagName("Username").item(0).getTextContent());
					}
				}
				
				// Process additional attributes
				NodeList atrbList = rootA.getElementsByTagName("Issue");
				for (int atrbIndex = 0; atrbIndex < atrbList.getLength(); atrbIndex++) {
					Element atrb = (Element) atrbList.item(atrbIndex);
					NodeList elementsByTagName = atrb.getElementsByTagName("InstanceID");
					Node item = elementsByTagName.item(0);
					if (item != null) {
						String instanId = item.getTextContent();
						if (atrb.getAttribute("iid").equals(instanId)) {
							row.add(atrb.getElementsByTagName("Abstract").item(0).getTextContent());
							row.add(atrb.getElementsByTagName("Priority").item(0).getTextContent());
							row.add(atrb.getElementsByTagName("FileName").item(0).getTextContent());
							row.add(atrb.getElementsByTagName("ExternalCategory").item(0) != null ? atrb.getElementsByTagName("ExternalCategory").item(0).getTextContent() : "None"); //$NON-NLS-3$
						}
					}
				}
				// Process AnalysisInfo
				NodeList analysisInfoList = vulnerability.getElementsByTagName("Unified");
				for (int k = 0; k < analysisInfoList.getLength(); k++) {
					Element analysisInfo = (Element) analysisInfoList.item(k);
					row.set(6, analysisInfo.getElementsByTagName("SourceLocation").item(0).getAttributes().getNamedItem("path").getNodeValue());
					row.set(7, analysisInfo.getElementsByTagName("SourceLocation").item(0).getAttributes().getNamedItem("line").getNodeValue());
				}
				data.add(row);
			}
			// Write to Excel
			ExcelTemplateWriter.writeToExcel(fileFpr.substring(0, fileFpr.lastIndexOf('.')), data, TEMPLATE_XL_FILE);
			System.out.println("Completed");
		} catch (IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
			System.out.println("File Not Found, please check if the .fpr file and xml result file is available in the same folder as the script");
		} catch (Exception e) {//IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		FileUtil.deleteFilesAndFolders(WORK_DIR);
    }

	private static String getTextContent(Element element, String name) {
		String text = "";
		NodeList elementsByTagName = element.getElementsByTagName(name);
		if (elementsByTagName != null && elementsByTagName.item(0) != null) {
			text = elementsByTagName.item(0).getTextContent();
		}
		return text; 
	}
}

