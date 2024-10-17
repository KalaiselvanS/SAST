package com.dhl.tools.fortify.reports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fortify.ui.model.report.ReportCommandlineInterface;

public class GenReport {
    private static final String WORK_DIR = Messages.getString("Main.workdir");
    private static final String RESULT_XML_FILE = WORK_DIR+Messages.getString("Main.outxml");
    private static final String TEMPLATE_XL_FILE = Messages.getString("Main.template");
    private static final List<String> HEADER = List.of("Project_Name", "InstanceID", "OWASP_Category", "Issue_Name", "Issue_Severity", "File_Name", "Path", "Line_No", "Comments", "Agreed_Action", "Development team comments");
	private static final boolean SKIP_HEADER = true;
    
	public static void main(String[] args) throws InterruptedException {
		try {
			GenReport genReport = new GenReport();
			File[] selectedFiles = getFprFilePath();
			if (selectedFiles != null && selectedFiles.length>0) {
				for (File file : selectedFiles) {
					genReport.processFPR(file);
				}
			} else {
				System.out.println("No files selected...");
			}
		} catch (IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
			System.out.println("File Not Found, please check if the .fpr file and xml result file is available in the same folder as the script");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	private void processFPR(File file)
			throws ParserConfigurationException, SAXException, IOException, FileNotFoundException {
		String fileFpr = file.getAbsolutePath();
		System.out.println("Processing file: " + fileFpr);
		String projectName = file.getName();
		projectName = projectName.substring(0, projectName.lastIndexOf("."));
		Animator.start();
		prepareXmlOutput(fileFpr);
		// Parse XML files
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document xmlOutputDocument = builder.parse(RESULT_XML_FILE);
		Element xmlOutputRootElement = xmlOutputDocument.getDocumentElement();

		// Initialize data structure
		List<List<String>> data = new ArrayList<>();
		data.add(HEADER);
		NodeList atrbList = xmlOutputRootElement.getElementsByTagName("Issue");
		List<String> emptylist = Arrays.asList("","","","","","","","","");
		int len = atrbList.getLength();
		if (len > 0) {
			for (int atrbIndex = 0; atrbIndex < len; atrbIndex++) {
				Element issue = (Element) atrbList.item(atrbIndex);
				List<String> row = new ArrayList<>(emptylist);
				if (issue != null) {
					row.set(0, projectName);
					row.set(1, issue.getAttribute("iid"));
					final String owasp_cat = getTextContent(issue, "ExternalCategory");
					row.set(2, owasp_cat.length()>0? owasp_cat : "None");
					row.set(3, getTextContent(issue, "Category"));
					String tag = getTextContent(issue, "Value");
					row.set(4, getDisplayTag(tag));
					row.set(5, getTextContent(issue, "FileName"));
					row.set(6, getTextContent(issue, "FilePath"));
					row.set(7, getTextContent(issue, "LineStart"));
					NodeList comments = issue.getElementsByTagName("Comment");
					StringBuilder commentsBuilder = new StringBuilder();
					for (int commentIndex = 0; commentIndex < comments.getLength(); commentIndex++) {
						Element comment = (Element) comments.item(commentIndex);
						commentsBuilder.append(getTextContent(comment, "Comment")).append("\n\n");
					}
					row.set(8, commentsBuilder.toString());
				}
				data.add(row);
			}
			// Write to Excel
			ExcelTemplateWriter.writeToExcel(fileFpr.substring(0, fileFpr.lastIndexOf('.')), data, TEMPLATE_XL_FILE, SKIP_HEADER);
		} else {
			System.out.println("No issues detected...");
		}
		Animator.stop();
		FileUtil.deleteFilesAndFolders(WORK_DIR);
	}

	private void prepareXmlOutput(String fileFpr) {
		ReportCommandlineInterface.main(new String[]{
				"-format", "xml",
				"-filterSet", Messages.getString("Main.filterSet"),
				"-template", Messages.getString("Main.xmlTemplate"),
				"-f", RESULT_XML_FILE,
				"-source", fileFpr});
	}

	private static File[] getFprFilePath() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle(Messages.getString("Main.FileChooserTitle"));
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		FileFilter fprFilter = new FileFilter() {
			@Override
			public String getDescription() {
				return "Select FPR files";
			}
			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".fpr");
			}
		};
		fileChooser.setFileFilter(fprFilter);
		int result = fileChooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			File[] selectedFiles = fileChooser.getSelectedFiles();
			return selectedFiles;
		}
		return null;
	}

	public static String getDisplayTag(String tag) {
		char c = tag.charAt(0);
		String displayTag = "-";
		switch (c) {
		case '4':
			displayTag = "Low";
			break;
		case '3':
			displayTag = "Medium";
			break;
		case '2':
			displayTag = "High";
			break;
		case '1':
			displayTag = "Critical";
			break;
		default:
			displayTag = "None";
			break;
		}
		return displayTag;
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

