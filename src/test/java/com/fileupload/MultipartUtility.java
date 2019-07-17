package com.fileupload;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MultipartUtility {
	private final String boundary;
	private static final String LINE_FEED = "\r\n";
	private URLConnection connection;
	private String charset;
	private OutputStream outputStream;
	private PrintWriter writer;

	/**
	 * This constructor initializes a new HTTP POST request with content type is set
	 * to multipart/form-data
	 * 
	 * @param requestURL
	 * @param charset
	 * @throws IOException
	 * @throws Exception 
	 */
	public MultipartUtility(String requestURL, String charset) throws Exception {
		this.charset = charset;

		// creates a unique boundary based on time stamp
	    boundary = Long.toHexString(System.currentTimeMillis());
	    TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}


			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
				// TODO Auto-generated method stub
				
			}
		} };

		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		connection = new URL(requestURL).openConnection();
		connection.setUseCaches(false);
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		outputStream = connection.getOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
	}

	/**
	 * Adds a form field to the request
	 * 
	 * @param name
	 *            field name
	 * @param value
	 *            field value
	 */
	public void addFormField(String name, String value) {
		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE_FEED);
		writer.append("Content-Type: text/plain; charset=" + "UTF-8").append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.append(value).append(LINE_FEED).flush();
	}
	


	/**
	 * Adds a upload file section to the request
	 * 
	 * @param fieldName
	 *            name attribute in <input type="file" name="..." />
	 * @param uploadFile
	 *            a File to be uploaded
	 * @throws IOException
	 */
	public void addFilePart(String fieldName, File uploadFile) throws IOException {

		String fileName = uploadFile.getName();
		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"").append(LINE_FEED);
		writer.append("Content-Type: text/plain; charset=" + charset).append(LINE_FEED); // Text file itself must be
		// saved in this charset!
		writer.append(LINE_FEED).flush();
		Files.copy(uploadFile.toPath(), outputStream);
		outputStream.flush(); // Important before continuing with writer!
		writer.append(LINE_FEED).flush(); // CRLF is important! It indicates end of boundary.

		outputStream.flush(); // Important before continuing with writer!
		writer.append(LINE_FEED).flush(); // CRLF is important! It indicates end of boundary.

		// End of multipart/form-data.
		writer.append("--" + boundary + "--").append(LINE_FEED).flush();
	}

	/**
	 * Adds a header field to the request.
	 * 
	 * @param name
	 *            - name of the header field
	 * @param value
	 *            - value of the header field
	 */
	public void addHeaderField(String name, String value) {
		writer.append(name + ": " + value).append(LINE_FEED);
		writer.flush();
	}

	/**
	 * Completes the request and receives response from the server.
	 * 
	 * @return a list of Strings as response in case the server returned status OK,
	 *         otherwise an exception is thrown.
	 * @throws IOException
	 */
	public List<String> finish() throws IOException {
		List<String> response = new ArrayList<String>();

		int responseCode = ((HttpURLConnection) connection).getResponseCode();

		InputStream is;
		if (responseCode >= 400) {
			is = ((HttpURLConnection) connection).getErrorStream();
		} else {
			is = connection.getInputStream();
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			response.add(inputLine);
		}
		in.close();

		return response;
	}
}
