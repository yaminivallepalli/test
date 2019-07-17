package com.fileupload;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;
import java.util.TimeZone;

import com.utilities.Utilities;

public class FileUploaderClient {

	public void uploadFile(String reportUploadURL, String reportPath, String userId, String executedUserId, String testCaseId, String testSetID, String moduleId,
			String submoduleId, boolean is_web, String result, String reportstatus, String mobilePlatform) {

		String charset = "UTF-8";
		File uploadFile = new File(reportPath);
		///String requestURL = "http://183.82.106.91:8030/BAF_Automation/UploadReportFile";

		try {
			String elapsedTime = Utilities.getElapsedTime(reportPath);
			MultipartUtility multipart = new MultipartUtility(reportUploadURL, charset);
			multipart.addFormField("user_id", userId);
			multipart.addFormField("executed_user_id", executedUserId);
			if (!testSetID.isEmpty()) {
				multipart.addFormField("testset_id", testSetID);
			} else {
				multipart.addFormField("testcase_id", testCaseId);
			}
			
			if(mobilePlatform != null && (mobilePlatform.equalsIgnoreCase("Android")||mobilePlatform.equalsIgnoreCase("iOS"))) {
				multipart.addFormField("report_type", mobilePlatform);
			} else {
				multipart.addFormField("report_type", is_web ? "web" : "api");
			}
			
			multipart.addFormField("module_id", (submoduleId == null || submoduleId.isEmpty() || submoduleId.equals("0")) ? moduleId : submoduleId);
			multipart.addFormField("report_result", result);
			multipart.addFormField("execution_time", elapsedTime);
			multipart.addFormField("report_status", reportstatus);
			multipart.addFormField("client_time_zone_id", TimeZone.getDefault().getID());
			multipart.addFilePart("file", uploadFile);

			List<String> response = multipart.finish();

			System.out.println("SERVER REPLIED:" + elapsedTime);

			for (String line : response) {
				System.out.println(line);
			}
			
			try {
				Files.deleteIfExists(Paths.get(reportPath));
			} catch (NoSuchFileException e) {
				System.out.println("No such file/directory exists");
			} catch (DirectoryNotEmptyException e) {
				System.out.println("Directory is not empty.");
			} catch (IOException e) {
				System.out.println("Invalid permissions.");
			}

			System.out.println("Cleaned Reports successful.");
		} catch (Exception ex) {
			System.err.println(ex);
		}

	}

}