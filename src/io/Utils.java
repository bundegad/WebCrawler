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

import javax.imageio.ImageIO;

import exceptions.ServerException;
import http.FileType;
import http.HTTPResponseCode;



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

	//chunked extension for handeling sending response in chunks
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
	
	public static String readHeadersFromInputStream(InputStream in) throws ServerException {
		
		BufferedReader inputStream = new BufferedReader(new InputStreamReader(in));
		StringBuilder builder = new StringBuilder();

		try {

			String line  = inputStream.readLine();
			while(line != null && !line.equals("") ) {
				builder.append(line + CRLF);

				line = inputStream.readLine();
				
			}
				
			builder.append(CRLF);

			
		} catch (IOException e) {
			throw new ServerException(HTTPResponseCode.INTERNAL_ERROR);
		} 
		
		return builder.toString();
	}
	
	public static String readChunkedDataFromInputStream(InputStream in, int timeout) throws ServerException {
		
		BufferedReader inputStream = new BufferedReader(new InputStreamReader(in));
		StringBuilder builder = new StringBuilder();
		
		try {

			String lengthline  = inputStream.readLine();
			System.out.println("Length line is " + lengthline);
			int length = Integer.parseInt(lengthline, 16);
			
			while (length != 0) {
				
				byte[] data = new byte[length];
				in.read(data);
				builder.append(data);
				lengthline  = inputStream.readLine();
				length = Integer.parseInt(lengthline, 16);
			}
			
			
			builder.append(CRLF);
			
		} catch (IOException e) {
			throw new ServerException(HTTPResponseCode.INTERNAL_ERROR);
		} catch (NumberFormatException e) {
			throw new ServerException(HTTPResponseCode.BAD_REQUEST);
		} 
		
		return builder.toString();
	}
	
	public static String readDataFromInputStream(InputStream in , int contentLength) throws ServerException {
		try {
			StringBuilder builder = new StringBuilder();
			BufferedReader inputStream = new BufferedReader(new InputStreamReader(in));
			
			while (inputStream.ready()) {
				builder.append((char) inputStream.read());
			}
			
			return builder.toString();
					
		} catch (IOException e) {
			throw new ServerException(HTTPResponseCode.BAD_REQUEST);
		}
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
