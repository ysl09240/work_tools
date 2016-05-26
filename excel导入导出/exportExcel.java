/**
	 * 导出excel数据表
	 *
	 * @return
	 */
	public String exportReconciliationExcel() {
		// 声明一个工作薄
		HSSFWorkbook workbook = new HSSFWorkbook();
		String title = request.getParameter("title");
		// 生成一个表格
		HSSFSheet sheet = workbook.createSheet(title);
		// 设置表格默认列宽度为15个字节
		sheet.setDefaultColumnWidth(20);
		// 生成一个样式
		HSSFCellStyle style = workbook.createCellStyle();
		// 设置这些样式
		style.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		// 生成一个字体
		HSSFFont font = workbook.createFont();
		font.setColor(HSSFColor.VIOLET.index);
		font.setFontHeightInPoints((short) 12);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		// 把字体应用到当前的样式
		style.setFont(font);
		// 生成并设置另一个样式
		HSSFCellStyle style2 = workbook.createCellStyle();
		style2.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
		style2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		style2.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style2.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style2.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style2.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		// 生成另一个字体
		HSSFFont font2 = workbook.createFont();
		font2.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
		// 把字体应用到当前的样式
		style2.setFont(font2);
		// 声明一个画图的顶级管理器
		// HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
		// 定义注释的大小和位置,详见文档
		// HSSFComment comment = patriarch.createComment(new HSSFClientAnchor(0,
		// 0, 0, 0, (short) 4, 2, (short) 6, 5));
		// 设置注释内容
		// comment.setString(new HSSFRichTextString("可以在POI中添加注释！"));
		// 设置注释作者，当鼠标移动到单元格上是可以在状态栏中看到该内容.
		// comment.setAuthor("leno");
		String head = request.getParameter("head");
		String[] headers = head.split(",");
		// 产生表格标题行
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell = null;
		for (int i = 0; i < headers.length; i++) {
			cell = row.createCell(i);
			cell.setCellStyle(style);
			HSSFRichTextString text = new HSSFRichTextString(headers[i]);
			cell.setCellValue(text);
		}
		List data = new ArrayList();
		UserBean user = (UserBean) session.get("user");
		Integer start = Integer.parseInt(request.getParameter("start"));
		Integer nums = Integer.parseInt(request.getParameter("nums"));
		String year = request.getParameter("year")==null?"":request.getParameter("year");
		String academyId = request.getParameter("academyId")==null?"":request.getParameter("academyId");
		String professionId = request.getParameter("professionId")==null?"":request.getParameter("professionId");
		String facestatus = request.getParameter("facestatus")==null?"":request.getParameter("facestatus");
		String studentName = request.getParameter("studentName")==null?"":request.getParameter("studentName");
		String identificationNunber = request.getParameter("identificationNunber")==null?"":request.getParameter("identificationNunber");
		HashMap map = new HashMap();
		map.put("startNum","0");
		map.put("countNum","9999");
		map.put("universityId",user.getCollegeid());
		map.put("year", year);
		map.put("academyId",academyId);
		map.put("professionId",professionId);
		map.put("facestatus",facestatus);
		map.put("studentName",studentName);
		map.put("identificationNunber",identificationNunber);
		List<Map<String,Object>> list = orderSlaveServices.getReconciliationList(map);

		if (list.size() <= 0) {
			JSONObject job = new JSONObject();
			job.put("success", "0");
			result = job.toJSONString();
			writeJson(result);
			return SUCCESS;
		}
//考生号,姓名,学院名称,专业名称,身份证号,合计金额,支付方式,缴费状态
		SimpleDateFormat f= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (int i = 0; i < list.size(); i++) {
			row = sheet.createRow((int) i + 1);
			row.createCell(0).setCellValue((String) list.get(i).get("luquNuber"));
			row.createCell(1).setCellValue((String) list.get(i).get("studentName"));
			row.createCell(2).setCellValue((String) list.get(i).get("academy"));
			row.createCell(3).setCellValue((String) list.get(i).get("profession"));
			row.createCell(4).setCellValue((String) list.get(i).get("identificationNunber"));
			row.createCell(5).setCellValue((Double)list.get(i).get("totalPrice"));
			Integer payMode = (Integer) list.get(i).get("paymode");
			Integer face = (Integer)list.get(i).get("facestatus");
			if(payMode.equals(0)){
				row.createCell(6).setCellValue("翼支付");
				row.createCell(7).setCellValue("1".equals(list.get(i).get("paystatus")) ? "己支付":"未支付");
			}else if(payMode.equals(1)){
				row.createCell(6).setCellValue("线下支付");
				row.createCell(7).setCellValue(face.equals(1) ? "己支付":"未支付");
			}else{
				row.createCell(6).setCellValue("批扣支付");
				row.createCell(7).setCellValue(face.equals(1) ? "己支付":"未支付");
			}
		}
		// 第五步 上传至服务器
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(date);
		JSONObject obj = new JSONObject();
		String tempPath = SAVEEXCELPATH + File.separator + dateStr
				+ File.separator;
		File file = new File(tempPath);
		if (!file.isDirectory()) {
			file.mkdirs();
		}
		Long time = System.currentTimeMillis();
		FileOutputStream fos = null;
		String fullPath = tempPath + File.separator + time + ".xls";
		String fullPath1 = EXCELPATH + dateStr + "/" + time + ".xls";
		try {
			fos = new FileOutputStream(fullPath);
			workbook.write(fos);
			fos.close();
			obj.put("success", true);
			obj.put("url", fullPath1);
		} catch (IOException e) {
			obj.put("success", false);
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
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

	public String excelOrder(){
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
								DecimalFormat df = new DecimalFormat("#.00");
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
					String str = (String) (list.get(k));
					String[] str1 = str.split(SEPARATOR);
					if (!"".equals(str)) {
						UserBean user = (UserBean) session.get("user");
						HashMap map = new HashMap();
						map.put("universityId", user.getCollegeid());
						map.put("createtime", System.currentTimeMillis());
						if(str1[1].trim().equals("")){
							status += ResultMsgUtil.RESULT_CODE_ERROR+",";
							FileManager.writeStringToFile(importName,"第"+(k+3)+"行第1列不能为空",true);
							continue;
						}
						map.put("identificationNunber", str1[1].trim());
						map.put("studentName", str1[2].trim());
						BigDecimal p2 = new BigDecimal(str1[3].trim());
						map.put("totalPrice", p2);
						if(str1[3].trim().equals("")){
							status += ResultMsgUtil.RESULT_CODE_ERROR+",";
							FileManager.writeStringToFile(importName,"第"+(k+3)+"行第3列不能为空",true);
							continue;
						}
						map.put("paymode", str1[4].trim().equals("银行扣款")?"2":(str1[4].trim().equals("线下支付")?"1":"0"));
						Integer studentId = this.studentSlaveServices
								.getStudentIdByidentificationNunber(map);
						//Integer count2 = this.studentSlaveServices.isExistPhone(str1[20]);
						if (studentId <= 0) {
							status += ResultMsgUtil.RESULT_CODE_ERROR+",";
						} else {
							map.put("studentId",studentId);
							Map<String,Object> orderinfoMap = orderSlaveServices.getOrderByStudentInfo(map);
							Map<String,Object> tempMap = (Map<String, Object>) orderinfoMap.get("orderinfo");
							Integer id = (Integer) tempMap.get("id");
							map.put("id",id);
							int count=0;
							int flag = 0;
							Double total = 0.0;
							//查出固定费用和非固定费用缴费状态
							List<Map<String,Object>> orderGoodsList= orderSlaveServices.getOrderGoodsList(map);
							for(Map<String,Object> orderMap : orderGoodsList){
								HashMap temp = new HashMap();
								temp.put("id",orderMap.get("id"));
								temp.put("type",orderMap.get("type"));
								count += orderMasterServices.updateOrderDetail(temp);
								total += Double.parseDouble((String) orderMap.get("price"));
							}
							String totalStr = new DecimalFormat("#.00").format(total);
							BigDecimal p1 = new BigDecimal(totalStr);
 							if(count ==orderGoodsList.size()&&p1.compareTo(p2)==0){
								map.put("facestatus",1);
								flag = orderMasterServices.updateOrderFacestatus(map);
							}
							if(flag<=0){
								status += ResultMsgUtil.RESULT_CODE_ERROR+",";
								continue;
							}else{
								status += ResultMsgUtil.RESULT_CODE_SUCCESS+",";
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
				result = json.toJson("上传成功"+success+"条数据，失败"+error+"条数据。");
			}
		} catch (Exception e1) {
			log.error("error:" + e1.getMessage());
			e1.printStackTrace();
			result = json.toJson("上传失败,请检查格式!");
		}
		writeText(result);
		return SUCCESS;
	}
	
	
	//前台请求
	function exportExcel(){
			$.ajax({
				url : "<%=basePath%>order/exportReconciliationExcel.action",
				type : "POST",
				dataType : "json",
				data : {
					"start":(start-1),
					"title":"学生缴费信息",
					"head" : "考生号,姓名,学院名称,专业名称,身份证号,合计金额,支付方式,缴费状态",
					"studentName":$.trim($("#studentName").val()),
					"academyId":$("#academyId").val(),
					"identificationNunber":$.trim($("#identificationNunber").val()),
					"profession":$("#profession").val(),
					"year":$("#year").val(),
					"nums":nums
				},
				beforeSend:function(XMLHttpRequest){
					// alert('远程调用开始...');
					$("#BgDiv").css({
						display : "block",
						height:$(document).height(),
						width:$(document).width()
					});
					$("#loading").show();
				},
				complete:function(XHR,TS){
					var resText=XHR.responseText;
					if(resText!=null && resText.indexOf("sessionState:0")>0){
						window.parent.location.href="../../login.jsp";
					}
					$("#BgDiv").hide();
					$("#loading").hide();
				},
				success : function(data){
					if(data.success ==true){
						location.href=data.url;
					}else if(data.success == "0"){
						$("#prompt").html("没有缴费信息!");
						$("#delSuccessDiv").show();
						setTimeout("$('#delSuccessDiv').hide()", 1500);
					}
				}
			});
		}
