/**
	 * 资源下载
	 *思路：1.传文件地址
			2.读取资源文件并打包成zip	
			3.把新生成的zip的文件地址传到前段，直接点击下载
	 *
	 * @return
     */
	public String downResource(){
		//这里的url是前段传过来的要下载的文件地址
		String path=request.getParameter("url");
		URL u = null;
		try {
			u = new URL(path);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		String fileName=path.substring((path.lastIndexOf("/")+1));
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(date);
		JSONObject obj = new JSONObject();
		// String tempPath = APKURL.replace("/", File.separator) +
		// "Public"+File.separator+"Uploads"+File.separator + dateStr;
		String tempPath = SAVEEXCELPATH + dateStr + "/";
		File file = new File(tempPath);
		if (!file.isDirectory()) {
			file.mkdirs();
		}
		long time=System.currentTimeMillis();
		BufferedInputStream fis = null;
		ZipOutputStream zip = null;
		String pathStr = tempPath +  time
				+ ".zip";
		String zipStr = EXCELPATH + dateStr +"/"+ time
				+ ".zip";
		try {
			zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(pathStr)));
			fis = new BufferedInputStream(u.openStream());
			zip.putNextEntry(new ZipEntry(fileName));
			byte[] buffer = new byte[2048];
			int len = 0;
			while ((len = fis.read(buffer)) != -1) {
				zip.write(buffer, 0, len);
			}
			zip.finish();
			obj.put("success", true);
			obj.put("url", zipStr);
		} catch (Exception e) {
			obj.put("success",false);
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					System.out.println("FileInputStream关闭失败");
					e.printStackTrace();
				}
			}

			if (zip != null) {
				try {
					zip.close();
				} catch (IOException e) {
					System.out.println("FileOutputStream关闭失败");
					e.printStackTrace();
				}
			}
		}
		result = obj.toJSONString();
		writeJson(result);
		return SUCCESS;
	}
