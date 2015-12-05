package lt.marius.converter.network;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class NetworkUtils {
	
	private final static int DOWNLOAD_BUFFER_SIZE = 512;
	private static String LOCAL_STORAGE;
	private static final String DEFAULT_ENCODING = "UTF-8";
	
	/** For faulty certificates bypassing */
	
	// always verify the host - dont check for certificate
	private final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
	        public boolean verify(String hostname, SSLSession session) {
	                return true;
	        }
	};
	
	// Trust every server - don't check for any certificate
	private static void trustAllHosts() {
	        // Create a trust manager that does not validate certificate chains
	        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                        return new java.security.cert.X509Certificate[] {};
	                }

//	                @Override
					public void checkClientTrusted(
							java.security.cert.X509Certificate[] chain,
							String authType)
							throws java.security.cert.CertificateException {
						
					}

//					@Override
					public void checkServerTrusted(
							java.security.cert.X509Certificate[] chain,
							String authType)
							throws java.security.cert.CertificateException {
						
					}
	        } };

	        // Install the all-trusting trust manager
	        try {
	                SSLContext sc = SSLContext.getInstance("TLS");
	                sc.init(null, trustAllCerts, new java.security.SecureRandom());
	                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	        } catch (Exception e) {
	                e.printStackTrace();
	        }
	}
	
	interface IDownloadAction {
		boolean doDownload(InputStream is);
		String getRemoteFileName();
		String getLocalFileName();
	}
	
	
	
	
	/**
	 * Gets POST connection to the specified url host
	 * @param url
	 * @return
	 */
	private static HttpURLConnection getConnection(URL url) {
		HttpURLConnection conn = null;
		try {
			if (url.getProtocol().toLowerCase().equals("https")) {
				trustAllHosts();
				HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
				https.setHostnameVerifier(DO_NOT_VERIFY);
				conn = https;
			} else {
				conn = (HttpURLConnection)url.openConnection();
			}
		} catch (IOException ex) {}
		
		try {
			if (conn != null) conn.setRequestMethod("POST");
		} catch (ProtocolException e) {
			e.printStackTrace();
		}
		if (conn != null) conn.setDoInput(true);
		return conn;
	}

	/**
	 * Downloads specified file and returns it as CHAR_ENCODING encoded string
	 * @param fileUrl - file to download
	 * @return downloaded string
	 */
	public static String downloadString(String fileUrl) {
		StringBuffer buff = new StringBuffer();
		HttpURLConnection conn = null;
		InputStream is = null;
		BufferedReader read = null;
		try {
			conn = getConnection(new URL(fileUrl));
			conn.setRequestMethod("GET");
//			conn.setDoInput(true);
//			conn.setDoOutput(true);
			conn.connect();
			try {
				is = conn.getInputStream();
			} catch (FileNotFoundException ex) {
				try {
					System.out.println(conn.getResponseCode());
				} catch (IOException e1) {
				}
				is = null;
			}
			if (is == null) {
				is = conn.getErrorStream();
			}
			
//			BufferedInputStream read = new BufferedInputStream(is);
			read = new BufferedReader(new InputStreamReader(is, DEFAULT_ENCODING));
			try {
				int dataByte;
				while ((dataByte = read.read()) != -1) {
					buff.append((char)dataByte);
				}
			} catch (IOException exx) {}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			
			try {
				read.close();
			} catch (Exception e) {}
			try {
				is.close();
			} catch (Exception e) {}
			if (conn != null) conn.disconnect();
		}
		return buff.toString();
	}
	
	/**
	 * Downloads specified file and returns it as CHAR_ENCODING encoded string
	 * @param fileUrl - file to download
	 * @return downloaded string
	 */
	public static void downloadFile(IDownloadAction downloadAction) {
		HttpURLConnection conn = null;
		InputStream is = null;
		try {
			conn = getConnection(new URL(downloadAction.getRemoteFileName()));
			conn.connect();
			is = conn.getInputStream();
			downloadAction.doDownload(is);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {}
			if (conn != null) conn.disconnect();
		}
	}
	
	/**
	 * Downloads specified file and returns it as CHAR_ENCODING encoded string
	 * @param fileUrl - file to download
	 * @return downloaded string
	 */
	public byte[] downloadBytes(String baseUrl, String fileName) {
		HttpURLConnection conn = null;
		InputStream is = null;
		ByteArrayOutputStream bos = null;
		byte []ret = null;
		try {
			conn = getConnection(new URL(baseUrl + '/' + fileName));
			conn.connect();
			is = conn.getInputStream();
//			BufferedInputStream read = new BufferedInputStream(is);
			byte buffer[] = new byte[DOWNLOAD_BUFFER_SIZE];
			bos = new ByteArrayOutputStream();
			int len;
			while ((len = is.read(buffer)) != -1) {
				bos.write(buffer, 0, len);
			}
			ret = bos.toByteArray();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.flush();
					bos.close();
				} catch (IOException e) {}
			}
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {}
			if (conn != null) conn.disconnect();
		}
		return ret;
	}
	
	public boolean fileExistsLocally(String fileName) {
		return new File(LOCAL_STORAGE + '/' + fileName).exists();
	}
	
	/**
	 * 
	 * @param url - remote url to retrieve stream from
	 * @return opened input stream. Client should take care of
	 * closing it.
	 */
	public static InputStream getRemoteStream(String url, Map<String, String> properties) {
		HttpURLConnection conn = null;
		InputStream is = null;
		try {
			conn = getConnection(new URL(url));
			if (properties != null) {
				for (Entry<String, String> entry : properties.entrySet()) {
					conn.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}
			conn.setRequestMethod("GET");
			conn.connect();
			is = conn.getInputStream();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException ex) {
			Scanner s = new Scanner(conn.getErrorStream());
			while(s.hasNext()) {
				System.out.println(s.nextLine());
			}
			
			ex.printStackTrace();
		}
		return is;
	}

	
}
