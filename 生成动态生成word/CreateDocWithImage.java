package com.koosoft.base.utils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @description freemarker导出word带图片
 * @author: YangSonglin
 * @date
 */
public class CreateDocWithImage {
    private Configuration configuration = null;

    public CreateDocWithImage() {
        configuration = new Configuration();
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassicCompatible(true);
    }

    public String createDoc(Map<String, Object> dataMap, String downloadType, String savePath){
        configuration.setClassForTemplateLoading(this.getClass(), "/com/koosoft/config/model");
        Template t = null;
        Writer out = null;
        String docUrl = "";
        try {
            t = configuration.getTemplate(downloadType+".xml");
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = sdf.format(date);
            savePath = savePath+dateStr;
            File outFile = new File(savePath);
            if (!outFile.isDirectory()) {
                outFile.mkdirs();
            }
            long time=System.currentTimeMillis();
            docUrl = Config.getProperty("resourceurl") + dateStr + "/" + time+".doc";
            savePath = savePath + "/" + time
                    + ".doc";
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(savePath),"UTF-8"));
            t.process(dataMap, out);
            outFile.delete();

        } catch (TemplateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return docUrl;
    }
    public String getImageStr(String imgUrl) {
        String imgFile = imgUrl;
        InputStream in = null;
        byte[] data = null;
        try {
            in = new FileInputStream(imgFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);
    }

    /**
     * 将网络图片进行Base64位编码
     *
     * @param imgUrl
     *			图片的url路径，如http://.....xx.jpg
     * @return
     */
    public String encodeImgageToBase64(String imageUrl) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        URL url = null;
        ByteArrayOutputStream outputStream = null;
        try {
            url = new URL(imageUrl);
            BufferedImage bufferedImage = ImageIO.read(url);
            outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", outputStream);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(outputStream.toByteArray());// 返回Base64编码过的字节数组字符串
    }
    /**
     * 将本地图片进行Base64位编码
     *
     * @param imgUrl
     *			图片的url路径，如http://.....xx.jpg
     * @return
     */
    public String encodeImgageToBase64(File imageFile) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        ByteArrayOutputStream outputStream = null;
        try {
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", outputStream);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(outputStream.toByteArray());// 返回Base64编码过的字节数组字符串
    }

    /**
     * 将Base64位编码的图片进行解码，并保存到指定目录
     *
     * @param base64
     *			base64编码的图片信息
     * @return
     */
    public static void decodeBase64ToImage(String base64, String path,
                                           String imgName) {
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            FileOutputStream write = new FileOutputStream(new File(path
                    + imgName));
            byte[] decoderBytes = decoder.decodeBuffer(base64);
            write.write(decoderBytes);
            write.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){

        String tempStr = Config.getProperty("save.reportDoc");
        Map<String,Object> map = new HashMap();
        List<Map<String,String>> mapList = new ArrayList<Map<String,String>>();
        CreateDocWithImage cdw = new CreateDocWithImage();
        for(int i=0;i<5;i++){
            Map<String,String> temp = new HashMap();
            temp.put("name","slin"+i);
            temp.put("age","age"+i);
            temp.put("sex","sex"+i);
            mapList.add(temp);
        }
        map.put("users",mapList);
        map.put("imgUrl",cdw.getImageStr(Config.getProperty("save.url")+"2015-12-22/"+"1450781430601.jpg"));
        cdw.createDoc(map,"reportInfo",tempStr);
    }
}