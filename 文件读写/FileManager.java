/********************************************************************
 * Copyright    :   srx Technologies Co., Ltd. 2003-2014
 * 
 * Filename     :   .java
 * Author       :   zhouxiaobo
 * Date         :   2014-7-31
 * Version      :   V1.00
 * Description  :   
 *
 * History      :   Modify Id  |  Date  |  Origin  |  Description
 *******************************************************************/

package com.koosoft.base.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

/**
 * 文件及网络资源基本操作类.包括问文件读、写、拷贝、复制等。 统一的错误和返回处理: 1.IO操作错误不抛出IOException
 * 2.读取失败返回null值,需检查返回值 3.写入失败返回false,需检查返回值 4.获取资源失败返回null值,需检查返回值
 */
@SuppressWarnings("unchecked")
public class FileManager {
	/**
	 * <pre>
	 * 将strSource文件内容拷贝到strTarget中.
	 * 例如：
	 * 将e:\\temp\\test1.java复制成e:\\temp\\test2.java，
	 * 如果目标文件已经存在则覆盖
	 * copyFile(&quot;e:\\temp\\test1.java&quot;,&quot;e:\\temp\\test2.java&quot;,true);
	 * </pre>
	 * 
	 * @param strSource
	 *            源文件
	 * @param strTarget
	 *            目标文件
	 * @param blnOverWrite
	 *            是否覆盖
	 * @return boolean 操作是否成功
	 */
	public static boolean copyFile(String strSource, String strTarget,
			boolean blnOverWrite) {
		File fSource = new File(strSource);
		File fTarget = new File(strTarget);
		return copyFile(fSource, fTarget, blnOverWrite);
	}

	/**
	 * <pre>
	 * 将fSource文件内容拷贝到fTarget中.
	 * 例如：
	 * 将e:\\temp\\test1.java复制成e:\\temp\\test2.java，
	 * 如果目标文件已经存在则覆盖
	 * File fSource = new File(&quot;e:\\temp\\test1.java&quot;);
	 * File fTarget = new File(&quot;e:\\temp\\test2.java&quot;);
	 * copyFile(fSource ,fTarget ,true);
	 * </pre>
	 * 
	 * @param fSource
	 *            源文件
	 * @param fTarget
	 *            目标文件
	 * @param blnOverWrite
	 *            是否覆盖
	 * @return boolean 操作是否成功
	 */
	public static boolean copyFile(File fSource, File fTarget,
			boolean blnOverWrite) {
		// 当目标文件已经存在并且不覆盖时返回true
		if (fTarget.exists() && blnOverWrite != true) {
			return true;
		}
		File fParent = new File(fTarget.getParent());
		if (!fParent.exists()) {
			fParent.mkdirs();
		}
		InputStream in = null;
		OutputStream out = null;
		try {
			// 获取源文件输入流
			in = new BufferedInputStream(new FileInputStream(fSource));
			if (!fTarget.exists()) {
				fTarget.createNewFile();
			}
			// 获取目标文件输出流
			out = new BufferedOutputStream(new FileOutputStream(fTarget));
			int bytes;
			byte[] buffer = new byte[4096];
			while ((bytes = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytes);
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace(System.out);
			return false;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
		}
	}

	/**
	 * <pre>
	 * 复制目录，将strSource复制到strTarget下.
	 * 例如：
	 * 将e:\\temp\\test1目录复制到e:\\share目录下,如果
	 * 目标文件已经存在，则覆盖
	 * copyDir(&quot;e:\\temp\\test1&quot;,&quot;e:\\share&quot;,true);
	 * 复制后的目标目录结构：e:\\share\test1
	 * </pre>
	 * 
	 * @param strSource
	 *            源目录
	 * @param strTarget
	 *            目标目录
	 * @param blnOverWrite
	 *            是否覆盖
	 * @return boolean 操作是否成功
	 */
	public static boolean copyDir(String strSource, String strTarget,
			boolean blnOverWrite) {
		/* 支持通配符“*” */
		// 是否使用通配符
		boolean isWildcard = false;
		if (strSource.endsWith("*")) {
			isWildcard = true;
			strSource = strSource.substring(0, strSource.length() - 2);
		}
		if (File.separator.equals("\\")) {
			strSource = strSource.replaceAll("/", "\\\\");
			strTarget = strTarget.replaceAll("/", "\\\\");
		}
		File fSource = new File(strSource);
		// 取得源目录下文件列表
		String[] arr = getFileList(strSource);
		// String strBaseDir = fSource.getParent() + File.separator;
		String strBaseDir = fSource.getParent();
		int iLength = strBaseDir.length();
		if (isWildcard) {
			iLength += fSource.getName().length();
		}
		int iCount = arr.length;
		int iErr = 0;
		File file = null;
		// 取得源目录下的目录列表并在目标目录下创建新目录
		for (int i = 0; i < iCount; i++) {
			file = new File(arr[i]);
			if (file.isDirectory()) {
				// 构造目标目录描述串，并创建目录
				String strT = strTarget + File.separator
						+ arr[i].substring(iLength, arr[i].length());
				file = new File(strT);
				if (!file.exists()) {
					iErr += (file.mkdirs() ? 0 : 1);
				}
			}
		}
		// 取得源目录下的文件列表并在目标目录下创建新文件
		for (int i = 0; i < iCount; i++) {
			file = new File(arr[i]);
			if (file.isFile()) {
				// 构造目标文件描述串，并复制文件
				String strT = strTarget + File.separator
						+ arr[i].substring(iLength, arr[i].length());
				iErr += (copyFile(arr[i], strT, blnOverWrite) ? 0 : 1);
			}
		}
		return (iErr == 0) ? true : false;
	}

	/**
	 * <pre>
	 * 移动文件strSource到strTarget.
	 * 例如：
	 * moveFile(e:\\test1\\test1.txt,e:\\test2\\test2,true);
	 * </pre>
	 * 
	 * @param strSource
	 *            源文件
	 * @param strTarget
	 *            目标文件
	 * @param blnOverWrite
	 *            是否覆盖
	 * @return boolean 操作是否成功
	 */
	public static boolean moveFile(String strSource, String strTarget,
			boolean blnOverWrite) {
		File fSource = new File(strSource);
		File fTarget = new File(strTarget);
		return moveFile(fSource, fTarget, blnOverWrite);
	}

	/**
	 * <pre>
	 * 移动文件fSource到fTarget.
	 * 例如：
	 * File fSource = new File(&quot;e:\\test1\\test1.txt&quot;);
	 * File fTarget = new File(&quot;e:\\test2\\test2&quot;);
	 * moveFile(fSource ,fTarget ,true);
	 * </pre>
	 * 
	 * @param fSource
	 *            源文件
	 * @param fTarget
	 *            目标文件
	 * @param blnOverWrite
	 *            是否覆盖
	 * @return boolean 操作是否成功
	 */
	public static boolean moveFile(File fSource, File fTarget,
			boolean blnOverWrite) {
		boolean bln = copyFile(fSource, fTarget, blnOverWrite);
		if (bln != true) {
			return false;
		}
		fSource.deleteOnExit();
		return true;
	}

	/**
	 * <pre>
	 * 移动目录，将strSource移动到strTarget下.
	 * 例如：
	 * 将e:\\temp\\test1目录移动到e:\\share目录下
	 * moveDir(&quot;e:\\temp\\test1&quot;,&quot;e:\\share&quot;,true);
	 * </pre>
	 * 
	 * @param strSource
	 *            源目录
	 * @param strTarget
	 *            目标目录
	 * @param blnOverWrite
	 *            是否覆盖
	 * @return boolean 操作是否成功
	 */
	public static boolean moveDir(String strSource, String strTarget,
			boolean blnOverWrite) {
		File fSource = new File(strSource);
		boolean bln = copyDir(strSource, strTarget, blnOverWrite);
		if (bln != true) {
			return false;
		}
		fSource.deleteOnExit();
		return true;
	}

	/**
	 * <pre>
	 * 将指定文件内容读到字符串中.
	 * 例如：
	 * String strR = readFileToString(&quot;e:\\test\\test1.txt&quot;);
	 * </pre>
	 * 
	 * @param strFile
	 *            文件路径
	 * @return String - 文件内容
	 */
	public static String readFileToString(String strFile) {
		File file = new File(strFile);
		return readFileToString(file);
	}

	/**
	 * <pre>
	 * 将指定文件内容读到字符串中.
	 * 例如：
	 * File file = new File(&quot;e:\\test\\test1.txt&quot;);
	 * String strR = readFileToString(file);
	 * </pre>
	 * 
	 * @param file
	 *            文件对象
	 * @return String - 文件内容
	 */
	public static String readFileToString(File file) {
		InputStream in = null;
		try {
			String content = null;
			in = new BufferedInputStream(new FileInputStream(file));
			int iLength = in.available();
			byte[] b = new byte[iLength];
			in.read(b);
			content = new String(b);
			return content;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
		}
	}

	/**
	 * 把文件读成字节
	 * 
	 * @param file
	 *            File
	 * @return byte[]
	 */
	public static byte[] readFileToBytes(File file) {
		InputStream in = null;
		byte[] b;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			int iLength = in.available();
			b = new byte[iLength];
			in.read(b);
			return b;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
		}
	}

	/**
	 * 把文件读成字节
	 * 
	 * @param strFile
	 *            String
	 * @return byte[]
	 */
	public static byte[] readFileToBytes(String strFile) {
		File file = new File(strFile);
		return readFileToBytes(file);
	}

	/**
	 * <pre>
	 * 将指定文件内容读到字符串中.
	 * 例如：
	 * File file = new File(&quot;e:\\test\\test1.txt&quot;);
	 * String strR = readFileToString(file);
	 * </pre>
	 * 
	 * @param file
	 *            文件对象
	 * @param charsetName
	 *            String 编码
	 * @return String - 文件内容
	 */
	public static String readFileToString(File file, String charsetName) {
		try {
			byte[] bytes = readFileToBytes(file);
			if (bytes == null) {
				return null;
			}
			String content = new String(bytes, charsetName);
			return content;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(System.out);
			return null;
		}
	}

	/**
	 * <pre>
	 * 将指定文件内容读到字符串中.
	 * 例如：
	 * File file = new File(&quot;e:\\test\\test1.txt&quot;);
	 * String strR = readFileToString(file);
	 * </pre>
	 * 
	 * @param file
	 *            String 文件路径
	 * @param charsetName
	 *            String 编码
	 * @return String - 文件内容
	 */
	public static String readFileToString(String strFile, String charsetName) {
		File file = new File(strFile);
		return readFileToString(file, charsetName);
	}

	/**
	 * <pre>
	 * 向指定文件中写入内容.
	 * 例如：
	 * boolean bln = writeStringToFile(&quot;e:\\test\\test1.txt&quot;,&quot;测试&quot;,true);
	 * </pre>
	 * 
	 * @param strFile
	 *            文件路径
	 * @param strContent
	 *            要写入的字符串
	 * @param blnAppend
	 *            是否追加在源文件后
	 * @return boolean 操作是否成功
	 */
	public static boolean writeStringToFile(String strFile, String strContent,
			boolean blnAppend) {
		File file = new File(strFile);
		return writeStringToFile(file, strContent, blnAppend);
	}

	/**
	 * 用指定编码向指定文件中写入内容.
	 * 
	 * @param strFile
	 *            String
	 * @param strContent
	 *            String
	 * @param blnAppend
	 *            boolean
	 * @param charsetName
	 *            String
	 * @return boolean
	 */
	public static boolean writeStringToFile(String strFile, String strContent,
			boolean blnAppend, String charsetName) {
		File file = new File(strFile);
		return writeStringToFile(file, strContent, blnAppend, charsetName);
	}

	/**
	 * <pre>
	 * 向指定文件中写入内容.
	 * 例如：
	 * File file = new File(&quot;e:\\test\\test1.txt&quot;);
	 * boolean bln = writeStringToFile(file,&quot;测试&quot;,true);
	 * </pre>
	 * 
	 * @param file
	 *            文件对象
	 * @param strContent
	 *            要写入的字符串
	 * @param blnAppend
	 *            是否追加在源文件后
	 * @return boolean 操作是否成功
	 */
	public static boolean writeStringToFile(File file, String strContent,
			boolean blnAppend) {
		FileWriter writer = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			writer = new FileWriter(file, blnAppend);
			writer.write(strContent);
			writer.write(System.getProperty( "line.separator"));
			return true;
		} catch (IOException e) {
			e.printStackTrace(System.out);
			return false;
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
		}
	}

	/**
	 * 用指定编码向指定文件中写入内容.
	 * 
	 * @param file
	 *            File
	 * @param strContent
	 *            String
	 * @param blnAppend
	 *            boolean
	 * @param charsetName
	 *            String
	 * @return boolean
	 */
	public static boolean writeStringToFile(File file, String strContent,
			boolean blnAppend, String charsetName) {
		OutputStreamWriter writer = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			writer = new OutputStreamWriter(new FileOutputStream(file,
					blnAppend), charsetName);
			writer.write(strContent);
			return true;
		} catch (IOException e) {
			e.printStackTrace(System.out);
			return false;
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
		}
	}

	/**
	 * 把以后直接数组写入到指定文件
	 * 
	 * @param file
	 *            File
	 * @param bytes
	 *            byte[]
	 * @param blnAppend
	 *            boolean
	 * @return boolean
	 */
	public static boolean writeBytesToFile(File file, byte[] bytes,
			boolean blnAppend) {
		FileOutputStream out = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			out = new FileOutputStream(file, blnAppend);
			out.write(bytes);
			return true;
		} catch (IOException e) {
			e.printStackTrace(System.out);
			return false;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
		}
	}

	/**
	 * 把以后直接数组写入到指定文件
	 * 
	 * @param filePath
	 *            String
	 * @param bytes
	 *            byte[]
	 * @param blnAppend
	 *            boolean
	 * @return boolean
	 */
	public static boolean writeBytesToFile(String filePath, byte[] bytes,
			boolean blnAppend) {
		File file = new File(filePath);
		return writeBytesToFile(file, bytes, blnAppend);
	}

	/**
	 * 删除目录(包括目录下所有文件).
	 * 
	 * @param strDir
	 *            要删除的目录(绝对路径)
	 * @return true-成功 false-失败
	 */
	public static boolean deleteDir(String strDir) {
		String[] arrFile = getFileList(strDir);
		File file = null;
		for (int i = 0; i < arrFile.length; i++) {
			file = new File(arrFile[i]);
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				deleteDir(arrFile[i]);
			}
		}
		file = new File(strDir);
		file.delete();
		return true;
	}

	/**
	 * <pre>
	 * 获得给定目录下所有文件列表包括目录.
	 * 例如：
	 * String []arr = getFileList(&quot;e:\\test&quot;);
	 * </pre>
	 * 
	 * @param strDir
	 *            给定目录
	 * @return String[]-文件集合
	 */
	public static String[] getFileList(String strDir) {
		ArrayList arrlist = new ArrayList();
		File f = new File(strDir);
		if (f.exists()) {
			File[] arrF = f.listFiles();
			for (int i = 0; i < arrF.length; i++) {
				if (arrF[i].isFile()) {
					arrlist.add(arrF[i].getAbsolutePath());
				} else {
					arrlist.add(arrF[i].getAbsolutePath());
					String[] arr = getFileList(arrF[i].getAbsolutePath());
					for (int j = 0; j < arr.length; j++) {
						arrlist.add(arr[j]);
					}
				} // end if
			} // end for
		} // end if
		return (String[]) arrlist.toArray(new String[arrlist.size()]);
	}

	/**
	 * 获取文件或目录所占空间大小(单位Byte).
	 * 
	 * @param strfile
	 *            文件或目录路径
	 * @return 文件或目录占用空间大小
	 */
	public static long getFileSize(String strfile) {
		return getFileSize(new File(strfile));
	}

	/**
	 * 获取文件或目录所占空间大小(单位Byte).
	 * 
	 * @param file
	 *            文件或目录对象
	 * @return 文件或目录占用空间大小
	 */
	public static long getFileSize(File file) {
		long size = 0;
		if (file.isFile()) {
			try {
				InputStream is = new FileInputStream(file);
				size = is.available();
			} catch (FileNotFoundException e) {
				e.printStackTrace(System.out);
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
		} else {
			String[] stafiles = getFileList(file.getAbsolutePath());
			for (int i = 0; i < stafiles.length; i++) {
				File f = new File(stafiles[i]);
				if (f.isDirectory()) {
					size += 4096;
				} else {
					try {
						InputStream is = new FileInputStream(f);
						size += is.available();
					} catch (FileNotFoundException e) {
						e.printStackTrace(System.out);
					} catch (IOException e) {
						e.printStackTrace(System.out);
					}
				}
			}
		}
		return size;
	}

	/**
	 * 反序列化，从文件读取对象
	 * 
	 * @param strFile
	 *            文件全路径
	 * @return
	 */
	public static Object readFileToObject(String strFile) {
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(strFile));
			Object obj = in.readObject();
			return obj;
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return null;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace(System.out);
				return null;
			}
		}
	}

	/**
	 * 序列化，把对象写入到文件
	 * 
	 * @param strFile
	 * @param obj
	 * @return
	 */
	public static boolean writeObjectToFile(String strFile, Object obj) {
		if (obj == null) {
			return false;
		}
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(strFile));
			out.writeObject(obj);
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return false;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
		return true;
	}
	
	/**
	 * 获取一个文件的class path
	 * @param zlass
	 * @param fileName
	 * @return
	 */
	public static String getClassPath(Class zlass, String fileName) {
		String configFilePath = "";
		try {
			String classPath = new File(zlass.getResource("/").getFile()).getCanonicalPath();
			configFilePath = classPath + File.separator + fileName;
			System.out.println("配置文件:" + configFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return configFilePath;
	}

	/**
	 * 从类路径得到资源URL对象
	 * 
	 * @param sourcePath
	 * @return
	 */
	public static URL getResource(String sourcePath) {
		if (sourcePath == null || (sourcePath = sourcePath.trim()).length() < 1) {
			return null;
		}
		return FileManager.class.getClassLoader().getResource(sourcePath);
	}

	/**
	 * 从classpath路径得到资源，并以Properties对象返回
	 * 
	 * @param classpath
	 * @return
	 */
	public static Properties getResourceAsProperties(String classpath) {
		if (classpath == null || (classpath = classpath.trim()).length() < 1) {
			return null;
		}
		InputStream in = FileManager.class.getClassLoader()
				.getResourceAsStream(classpath);
		if (in == null) {
			return null;
		}
		try {
			Properties pro = new Properties();
			pro.load(in);
			return pro;
		} catch (IOException e) {
			e.printStackTrace(System.out);
			return null;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 从一个给定的classpath路径得到资源的File对象
	 * 
	 * @param classpath
	 * @return
	 */
	public static File getResourceAsFile(String classpath) {
		try {
			URL url = FileManager.class.getClassLoader().getResource(classpath);
			String filepath = url.getFile();
			File file = new File(filepath);
			return file;
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return null;
		}
	}

	/**
	 * 从给定的文件路径中加载Properties对象
	 * 
	 * @param propPath
	 * @return
	 */
	public static Properties getProperties(String propPath) {
		if (propPath == null || (propPath = propPath.trim()).length() < 1) {
			return null;
		}
		InputStream in = null;
		try {
			in = new FileInputStream(propPath);
			Properties pro = new Properties();
			pro.load(in);
			return pro;
		} catch (IOException e) {
			e.printStackTrace(System.out);
			return null;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 把给定的Properties对象存储到指定文件中
	 * 
	 * @param props
	 * @param propPath
	 * @return
	 */
	public static boolean savePropertiesToFile(Properties props, String propPath) {
		if (props == null) {
			return false;
		}
		if (propPath == null || (propPath = propPath.trim()).length() < 1) {
			return false;
		}
		OutputStream out = null;
		try {
			out = new FileOutputStream(propPath);
			props.store(out, "");
			return true;
		} catch (IOException e) {
			e.printStackTrace(System.out);
			return false;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 读取指定URL的内容,当且过程中发生异常时,返回null
	 * 
	 * @param url
	 * @return
	 */
	public static String getUrlContent(String url) {
		InputStream in = null;
		BufferedReader reader = null;
		StringBuffer htmlContent = new StringBuffer();
		try {
			// 通过网络API访问首页的地址得到页面的html输出
			in = new URL(url).openStream();
			reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while ((line = reader.readLine()) != null) {
				htmlContent.append(line).append("\n");
			}
			return htmlContent.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace(System.out);
			return null;
		} catch (IOException e) {
			e.printStackTrace(System.out);
			return null;
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
		}
	}

	/**
	 * 指定字符编码读取指定URL的内容,当且过程中发生异常时,返回null
	 * 
	 * @param url
	 * @param encode
	 * @return
	 */
	public static String getUrlContent(String url, String encode) {
		InputStream in = null;
		BufferedReader reader = null;
		StringBuffer htmlContent = new StringBuffer();
		try {
			// 通过网络API访问首页的地址得到页面的html输出
			in = new URL(url).openStream();
			reader = new BufferedReader(new InputStreamReader(in, encode));
			String line = "";
			while ((line = reader.readLine()) != null) {
				htmlContent.append(line).append("\n");
			}
			return htmlContent.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace(System.out);
			return null;
		} catch (IOException e) {
			e.printStackTrace(System.out);
			return null;
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
		}
	}

	/**
	 * 读取一个URL资源的内容,并写入指定文件中
	 * 
	 * @param url
	 * @param filePath
	 * @return
	 */
	public static boolean getUrlToFile(String url, String filePath) {
		InputStream in = null;
		OutputStream out = null;
		try {
			byte[] buff = new byte[4096];
			// 通过网络API访问首页的地址得到页面的html输出
			in = new URL(url).openStream();
			out = new FileOutputStream(filePath);
			int count = 0;
			while ((count = in.read(buff)) > 0) {
				out.write(buff, 0, count);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace(System.out);
			return false;
		} catch (IOException e) {
			e.printStackTrace(System.out);
			return false;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
		}
		return true;
	}

	/**
	 * 从给定的InputStream中读取数据,并写入指定文件中
	 * 
	 * @param in
	 * @param filePath
	 * @return
	 */
	public static boolean inputStreamToFile(InputStream in, String filePath) {
		OutputStream out = null;
		try {
			byte[] buff = new byte[4096];
			out = new FileOutputStream(filePath);
			int count = 0;
			while ((count = in.read(buff)) > 0) {
				out.write(buff, 0, count);
			}
		} catch (IOException e) {
			e.printStackTrace(System.out);
			return false;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
		}
		return true;
	}

	/**
	 * 将文件转化为文件
	 */
	public synchronized static boolean string2File(String res, String filePath) {
		boolean flag = true;
		BufferedReader bufferedReader = null;
		BufferedWriter bufferedWriter = null;
		try {
			File distFile = new File(filePath);
			FileOutputStream outputStream = new FileOutputStream(distFile);
			if (!distFile.getParentFile().exists()) {
				distFile.getParentFile().mkdirs();
			}
			bufferedReader = new BufferedReader(new StringReader(res));
			// bufferedWriter = new BufferedWriter(new FileWriter(distFile));
			bufferedWriter = new BufferedWriter(new java.io.OutputStreamWriter(
					outputStream, "UTF-8"));
			char buf[] = new char[1024]; // 字符缓冲区
			int len;
			while ((len = bufferedReader.read(buf)) != -1) {
				bufferedWriter.write(buf, 0, len);
			}
			bufferedWriter.flush();
			bufferedReader.close();
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			flag = false;
			return flag;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return flag;
	}
	
	/**
	 * 导入日志记录
	 * @return
	 */
	public static HashMap importLogName(String importLog,String showImportLog){
		HashMap map = new HashMap();
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(date);
		String tempPath = importLog + File.separator + dateStr + File.separator;
		File file = new File(tempPath);
		if (!file.isDirectory()) {
			file.mkdirs();
		}
		long time=System.currentTimeMillis();
		String pathStr = tempPath + File.separator + time
				+ "." +"txt";
		String pathShow = showImportLog + dateStr + "/" + time + "." +"txt";
		map.put("pathStr", pathStr);
		map.put("pathShow", pathShow);
		return map;
	}

	/**
	 * 导入日志记录
	 * @return
	 */
	public static String importLogName(String importLog){
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(date);
		String tempPath = importLog + File.separator + dateStr + File.separator;
		File file = new File(tempPath);
		if (!file.isDirectory()) {
			file.mkdirs();
		}
		long time=System.currentTimeMillis();
		String pathStr = tempPath + File.separator + time
				+ "." +"txt";
		return pathStr;
	}
	/**
	  * @Description: 支付日志
	  * @return_type: String
	  * @author: Cai ShaoSong
	  * @date: 2016-1-12下午3:25:14
	  * @params
	 */
	public static String orderPayLogName(String orderPayLog,Integer uId ,Integer stuId){
		Date date = new Date();
		String tempPath = orderPayLog +File.separator+ uId + File.separator;
		File file = new File(tempPath);
		if (!file.isDirectory()) {
			file.mkdirs();
		}
		String pathStr = tempPath + File.separator + stuId
				+ "." +"txt";
		return pathStr;
	}
}