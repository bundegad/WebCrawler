package io;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import exceptions.ServerException;
import http.FileType;
import http.HTTPConstants;
import http.HTTPResponseCode;
import http.HTTPUtils;
import http.HTTPUtils.HttpParsedMessageObject;



public class Utils {

	//Constants Symbols/Tokens
	public static final String CRLF = "\r\n";
	public static final String FILE_SEPERATOR = File.separator;
	
	//HTTP protocol constants
	public static final String HTTP_TYPE_1_0 = "1.0";
	public static final String HTTP_TYPE_1_1 = "1.1";
	
	public static final String HTTP_CONNECTION_KEY = "connection";
	public static final String HTTP_CONNECTION_CLOSE = "closed";
	public static final String HTTP_CONNECTION_KEEP_ALIVE = "keep-alive";
	
	public static final String HTTP_CONTENT_LENGTH_KEY = "content-length";
	public static final String HTTP_CONTENT_TYPE_KEY = "content-type";
	
	public static final String HTTP_CONTENT_MESSAGE_TYPE = "message/http";
	public static final String HTTP_TRANSFER_ENCODING = "transfer-encoding";
	public static final String HTTP_CHUNKED_KEY = "chunked";
	public static final String HTTP_CHUNKED_KEY_YES = "yes";

	public static final int CHUNKED_SIZE = 1024;
	
	



	public static String readFile(String file) throws IOException {

		//Change this to dynamic config.ini	
		BufferedReader reader;
		try {

			Path configPath = Paths.get(file);
			reader = Files.newBufferedReader(configPath);
			StringBuilder builder = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				builder.append(line);
				line = reader.readLine();
			}	
			reader.close();
			
			return builder.toString();

		} catch (IOException | InvalidPathException | SecurityException ex) {
			throw new IOException();
		}
	}

	public static byte[] readImageFile(String file) {
		
		if (FileType.getTypeForFile(file) == FileType.ico) {
			try {
				return Files.readAllBytes(new File(file).toPath());
			} catch (Exception e) {
				//TODO - Add exception
				return null;
			}
			
		}
		
		BufferedImage originalImage;
		try {
			originalImage = ImageIO.read(new File(file));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write( originalImage, FileType.getExtension(file),  baos );
			baos.flush();
			byte[] imageInByte = baos.toByteArray();
			baos.close();

			return imageInByte;

		} catch (Exception e) {
			e.printStackTrace();
			//TODO - Add exception
			return null;
		}
	} 
	
	public static HTTPUtils.HttpParsedMessageObject readHttpMessageFromInputStream(InputStream in) throws ServerException {
		
		BufferedReader inputStream = new BufferedReader(new InputStreamReader(in));
		String firstLine = null;
		HashMap<String, String> headers = new HashMap<>();
		byte[] body = null;
		boolean isChunked = false;

		try {
			
			//First line
			firstLine = inputStream.readLine();
			
			//Headers
			Pattern pattern = Pattern.compile(HTTPConstants.REQUEST_HEADERLINE_PATTERN_STRING);
			Matcher matcher = null;
			String line = inputStream.readLine();
			
			while(line != null && !line.equals("") ) {
				matcher = pattern.matcher(line);
				if (matcher.matches()) {
					headers.put(matcher.group(1).toLowerCase().trim(), matcher.group(2).toLowerCase().trim());
					isChunked |= matcher.group(1).trim().equals(HTTPConstants.HTTP_TRANSFER_ENCODING) 
							&& matcher.group(2).equals(HTTP_CHUNKED_KEY);
				} else {
					throw new ServerException(HTTPResponseCode.BAD_REQUEST);
				}
				
				line = inputStream.readLine();
			}
			
			//Read chunked body
			StringBuilder builder = new StringBuilder();
			if (isChunked) {
				
			}
			
			int contentLength = 0;
			if (headers.containsKey(HTTP_CONTENT_LENGTH_KEY)) {
				contentLength = Integer.parseInt(headers.get(HTTPConstants.HTTP_CONTENT_LENGTH_KEY));
			}
			
			int numBytes = 0;
			while (numBytes < contentLength) {
				builder.append((char) inputStream.read());
				numBytes++;
			}
			
			body = new byte[numBytes];
			System.arraycopy(builder.toString().getBytes(), 0, body, 0, numBytes);

			
		} catch (IOException e) {
			throw new ServerException(HTTPResponseCode.INTERNAL_ERROR);
		} 
		return new HttpParsedMessageObject(firstLine, headers, body);
	}
	
	public static void writeOutputStream(OutputStream out, String content) {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
		try {
			writer.write(content);
			writer.flush();
			//writer.close();//remove
		} catch (IOException e) {
			//TODO - Add exception
		}

	}

	public static void writeOutputStream(OutputStream out, byte[] content) {
		try {
			out.write(content);
			out.flush();
		} catch (IOException e) {
			//TODO - Add exception
		}
	}
	
	public static  void writeOutputStreamChunked(OutputStream out, byte[] FileContent)  {
		
		try {
			
			int startChunk = 0;
			while (startChunk <=  FileContent.length) {
				int length = startChunk + CHUNKED_SIZE <= FileContent.length ? 
						CHUNKED_SIZE : FileContent.length - startChunk;
				
				String firstLine = String.format("%s%s", Integer.toHexString(length), CRLF);
				out.write(firstLine.getBytes());
				out.write(FileContent, startChunk, length);
				out.write(CRLF.getBytes());
				out.flush();
				
				if (length == 0) {
					break;
				}
				
				startChunk += length;
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
			//TODO - Add exception
		} 
	}
	
	public static boolean isValidFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists() || file.isDirectory() || !file.canRead()) {
			return false;
		}
		return true;
	}
	
	public static boolean isValidDirectory(String directory) {
		File file = new File(directory);
		return file.exists() && file.isDirectory() && file.canRead();
	}
	
}
