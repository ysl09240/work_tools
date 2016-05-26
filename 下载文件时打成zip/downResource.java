/**
	 * ��Դ����
	 *˼·��1.���ļ���ַ
			2.��ȡ��Դ�ļ��������zip	
			3.�������ɵ�zip���ļ���ַ����ǰ�Σ�ֱ�ӵ������
	 *
	 * @return
     */
	public String downResource(){
		//�����url��ǰ�δ�������Ҫ���ص��ļ���ַ
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
					System.out.println("FileInputStream�ر�ʧ��");
					e.printStackTrace();
				}
			}

			if (zip != null) {
				try {
					zip.close();
				} catch (IOException e) {
					System.out.println("FileOutputStream�ر�ʧ��");
					e.printStackTrace();
				}
			}
		}
		result = obj.toJSONString();
		writeJson(result);
		return SUCCESS;
	}
