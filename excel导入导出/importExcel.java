public String excelStudentInfo() {
		try {
				Workbook workbook = null;
				FileInputStream is = new FileInputStream(getExcelFile());
				String importName = FileManager.importLogName(IMPORTLOG);
				try {
				workbook = null;
				workbook = new HSSFWorkbook(is);
			} catch (Exception e) {

				is = new FileInputStream(getExcelFile());
				workbook = null;
				workbook = new XSSFWorkbook(is);
			}
			Sheet sheet = workbook.getSheetAt(0);

			// 解析公式结果
			FormulaEvaluator evaluator = workbook.getCreationHelper()
					.createFormulaEvaluator();
			List<String> list = new ArrayList<String>();

			int minRowIx = sheet.getFirstRowNum();
			int maxRowIx = sheet.getLastRowNum();
			Row row1 = sheet.getRow(0);
			short minColIx = row1.getFirstCellNum();
			short maxColIx = row1.getLastCellNum();
			for (int rowIx = 2; rowIx <= maxRowIx; rowIx++) {
				Row row = sheet.getRow(rowIx);
				int a = row.getFirstCellNum();
				if(null == row || row.getFirstCellNum() != 0) {
					continue;
				}
				CellValue firstcellValue = evaluator.evaluate(row.getCell(new Integer(0)));
				if (firstcellValue == null || firstcellValue.equals("")) {
					continue;
				}
				StringBuilder sb = new StringBuilder();
							for (short colIx = 0; colIx <= maxColIx; colIx++) {
								Cell cell = row.getCell(new Integer(colIx));
								CellValue cellValue = evaluator.evaluate(cell);
								if (cellValue == null || cellValue.equals("")) {
									sb.append(SEPARATOR + "");
									continue;
								}
								switch (cellValue.getCellType()) {
									case Cell.CELL_TYPE_BOOLEAN:
										sb.append(SEPARATOR + cellValue.getBooleanValue());
										break;
									case Cell.CELL_TYPE_NUMERIC:
										if (DateUtil.isCellDateFormatted(cell)) {
											sb.append(SEPARATOR + cell.getDateCellValue());
										} else {
											DecimalFormat df = new DecimalFormat("0");
											String whatYourWant = df.format(cell.getNumericCellValue());
											sb.append(SEPARATOR + whatYourWant);
//							double d = cellValue.getNumberValue();
//							String strNumber = String.valueOf(d);
//							if ( colIx == 1 || colIx == 2
//									|| colIx == 5 || colIx == 6 || colIx == 7) {
//								sb.append(SEPARATOR + (int) d);
//							} else {
//								sb.append(SEPARATOR + strNumber);
//							}
						}
						break;
					case Cell.CELL_TYPE_STRING:
						sb.append(SEPARATOR + cellValue.getStringValue());
						break;
					case Cell.CELL_TYPE_FORMULA:
						break;
					case Cell.CELL_TYPE_BLANK:
						break;
					case Cell.CELL_TYPE_ERROR:
						break;
					default:
						break;
					}
				}
				list.add(sb.toString());
			}
			if (list == null || list.size() < 0) {
				result = json.toJson(ResultMsgUtil.RESULT_CODE_0);
			} else {
				String status = "";
				for (int k = 0; k < list.size(); k++) {
					System.out.println(k);
					String str = (String) (list.get(k));
					String[] str1 = str.split(SEPARATOR);
					if (!"".equals(str)) {
						UserBean user = (UserBean) session.get("user");
						HashMap map = new HashMap();
						map.put("universityId", user.getCollegeid());
						map.put("professionCode", "");
						map.put("createtime", System.currentTimeMillis());
						map.put("luquNuber", str1[1].trim());
						map.put("zhunkaozhengNuber", str1[2].trim());
						map.put("studentName", str1[3].trim());
						if(str1[3].trim().equals("")){
							status += ResultMsgUtil.RESULT_CODE_ERROR+",";
							FileManager.writeStringToFile(importName,"第"+(k+3)+"行第3列不能为空",true);
							continue;
						}
						map.put("sex", str1[4].trim().equals("女")?"1":"0");

						map.put("identificationNunber", str1[5].trim());
						if(str1[5].trim().equals("")){
							status += ResultMsgUtil.RESULT_CODE_ERROR+",";
							FileManager.writeStringToFile(importName,"第"+(k+3)+"行第5列身份证号不能为空",true);
							continue;
						}
						String birthday = "0";
						if (str1[6].trim().equals("")) {
							if (str1[5].trim().length() < 18) {
								birthday = "0";
							} else {
								birthday = str1[5].trim().substring(6, 14);
							}
						} else {
							birthday = str1[6].trim();
						}
						map.put("birthday", birthday);
						map.put("cellphone", str1[7].trim());
						map.put("politicalStatus", str1[8].trim());
						map.put("political",str1[8].trim());
						Integer politicalId = teacherSlaveServices.getPoliticalIdByName(map);
						map.put("politicalStatusId",politicalId);
						map.put("nation", str1[9].trim());
						Integer nationId = teacherSlaveServices.getNationIdByName(map);
						map.put("nationId",nationId);
						map.put("schoolYear",str1[10].trim());
						map.put("examineeCategory", str1[11].equals("统招")?"1":"0");
						// 查找学院id,如果为空重新创建学院
						map.put("academy", str1[12].trim());
						if(str1[12].equals("")){
							status += ResultMsgUtil.RESULT_CODE_ERROR+",";
							FileManager.writeStringToFile(importName,"第"+(k+3)+"行第12列不能为空",true);
							continue;
						}
						Integer academyId = this.studentSlaveServices.getAcademyIdForExcel(map);
						if(academyId != null){
						}else{
							academyId = this.studentMasterServices.addAcademyByExcel(map);
						}
						map.put("academyId", academyId);
						// 查找专业id,如果为空重新创建专业
						map.put("profession", str1[13].trim());
						if(str1[13].equals("")){
							status += ResultMsgUtil.RESULT_CODE_ERROR+",";
							FileManager.writeStringToFile(importName,"第"+(k+3)+"行第13列不能为空",true);
							continue;
						}
						Integer professionId = this.studentSlaveServices.getProfessionIdForExcel(map);
						if(professionId != null){
							map.put("professionId",professionId);
						}else{
							professionId = this.studentMasterServices.addProfessionByExcel(map);
							map.put("professionId",professionId);
						}
						map.put("province", str1[14].trim());
						Integer provinceId = 0;
						if( !"".equals(str1[14].trim())){
							provinceId = this.studentSlaveServices.getProvinceIdForExcel(str1[14]);
						}
						if(provinceId != null){
							map.put("provinceId",provinceId);
							map.put("studentOriginProvinceId", provinceId);
						}else{
							map.put("provinceId",0);
							map.put("studentOriginProvinceId", 0);
						}
						map.put("city", str1[15].trim());
						Integer cityId = 0;
						cityId = this.studentSlaveServices.getCityIdForExcel(map);
						if(cityId != null){
							map.put("cityId",cityId);
							map.put("studentOriginProvinceCityId", cityId);
						}else{
							map.put("cityId",0);
							map.put("studentOriginProvinceCityId",0);
						}
						map.put("area",str1[16].trim());
						Integer areaId =0;
						areaId = this.studentSlaveServices.getAreaIdForExcel(map);
						if(areaId !=null){
							map.put("studentOriginProvinceAreaId",areaId);
						}else{
							map.put("studentOriginProvinceAreaId",0);
						}
						map.put("studentAddress", str1[17].trim());
						map.put("xuezhi",str1[18].trim());
						Integer loginType = this.studentSlaveServices.getLoginTypeByUniversityId(map);
//						if(loginType==3){ // 身份证号作为登录号
						Integer idcount = this.studentSlaveServices.getIdentificationNunberCount(map);
						map.put("userName", str1[5].trim());
						if(idcount > 0) { // 账号已存在
							status += ResultMsgUtil.RESULT_CODE_0+",";
							FileManager.writeStringToFile(importName,"第"+(k+3)+"行第5列身份证号已存在",true);
							continue;
						}
//						} else if(loginType==2){ // 准考证号作为登录号
//							map.put("userName", str1[2].trim());
//							Integer count3 = this.studentSlaveServices.getZhunkaozhengNuberCount(map);
//							if(count3 > 0) { // 账号已存在
//								status += ResultMsgUtil.RESULT_CODE_0+",";
//								continue;
//							}
//						} else if(loginType==1){ // 学号作为登录号
//							map.put("userName", str1[1].trim());
//							Integer count1 = this.studentSlaveServices.getStudentNumberCount(map);
//							if(count1 > 0) { // 账号已存在
//								status += ResultMsgUtil.RESULT_CODE_0+",";
//								continue;
//							}
//						} else { // 默认  录取号作为登录号
//							map.put("userName", str1[3].trim());
//							Integer count2 = this.studentSlaveServices.getLuquNuberCount(map);
//							if(count2 > 0) { // 账号已存在
//								status += ResultMsgUtil.RESULT_CODE_0+",";
//								continue;
//							}
//						}
						//map.put("userName", str1[3]);
						if(str1[5] != null){
							String pwd = str1[5].substring(str1[5].length()-6, str1[5].length()).toUpperCase();
							map.put("password", Md5Util.md5(pwd));
						}else{
							map.put("password", Md5Util.md5("123456"));
						}
						Integer count1 = this.studentSlaveServices
								.getIdentificationNunberCount(map);
						//Integer count2 = this.studentSlaveServices.isExistPhone(str1[20]);
						if (count1 > 0) {
							status += ResultMsgUtil.RESULT_CODE_0+",";
						} else {
							Integer count = this.studentMasterServices
									.excelImportStudent(map);
							if (count > 0) {
								status += ResultMsgUtil.RESULT_CODE_SUCCESS+",";
							} else {
								status += ResultMsgUtil.RESULT_CODE_ERROR+",";
							}
						}
					}
				}
				String[] rt = status.split(",");
				int success = 0;
				int error = 0;
				int exsit = 0;
				for (String s : rt) {
					if(s.equals("0")){
						exsit++;
					}else if(s.equals("1")){
						success++;
					}else if(s.equals("-1")){
						error++;
					}
				}
				result = json.toJson("上传成功"+success+"条数据，重复"+exsit+"条数据，失败"+error+"条数据。");
			}
		} catch (Exception e1) {
			log.error("error:" + e1.getMessage());
			e1.printStackTrace();
			result = json
					.toJson("上传失败,请检查格式!");
		}
		writeText(result);
		return SUCCESS;
	}
	
//前段请求	
	  $(".fileUpload").upload({
                action: "<%=basePath%>studentinfo/excelStudentInfo.action",  //上传地址
                fileName: "excelFile",          //文件名称。用于后台接收
                accept: ".xls,.xlsx", //文件类型
                submit:function(input){
                    $("#excelName").val(input[0].value);
                    $("#progressbar1").progressBar(80,{speed:100,barImage: '<%=basePath%>images/progressbg_yellow.gif',boxImage:'<%=basePath%>images/progressbar.gif'});
                    return true;
                },
                success: function(data) {
                    var result2 = "";
                    if(data!=''){
                        result2 = data;
                     $("#progressbar1").progressBar(100,{speed:0,barImage: '<%=basePath%>images/progressbg_green.gif',boxImage:'<%=basePath%>images/progressbar.gif'});
                        $(".result2").html("<span style=\"color:green;\">"+result2+"</span>");
                    }else{
                        result2 = "上传失败";
                        $("#progressbar1").progressBar(100,{speed:0,barImage: '<%=basePath%>images/progressbg_red.gif',boxImage:'<%=basePath%>images/progressbar.gif'});
                        $(".result2").html("<span style=\"color:red;\">"+result2+"</span>");
                    }
//                    location.href="quantityImport.jsp?result2="+encodeURI(encodeURI(result2));

                },
                error:function(){
//                    $(".fileUploadFail").removeClass("hideImg");
                }
            });

	