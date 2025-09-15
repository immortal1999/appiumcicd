package com.qa.ultils;

import com.qa.BaseTest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;

public class TestUtils {

    public static Duration WAIT = Duration.ofSeconds(10);

    public HashMap<String, String> parseStringXML(InputStream file) throws Exception{
        HashMap<String, String> stringMap = new HashMap<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document = builder.parse(file);

        document.getDocumentElement().normalize();

        Element root = document.getDocumentElement();

        NodeList nList = root.getElementsByTagName("string");

        for(int temp = 0; temp < nList.getLength(); temp++)
        {
            Node node = nList.item(temp);

            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                Element eElement = (Element) node;
                // store each element key value in map
                stringMap.put(eElement.getAttribute("name"), eElement.getTextContent());
            }
        }
        return stringMap;
    }

    public String dateTime(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date date = new Date();
        return  dateFormat.format(date);
    }

    public void log(String txt){
        BaseTest base = new BaseTest();
        String msg = Thread.currentThread().getId() + ":" + base.getPlatform() + base.getDeviceName() + ":"
                + Thread.currentThread().getStackTrace()[2].getClassName() + ":" + txt;
        System.out.println(msg);

        String strFile = "logs" + File.separator + base.getPlatform() + "_" + base.getDeviceName() +
                File.separator + base.getDateTime();

        File logFile = new File(strFile);

        if(!logFile.exists()){
            logFile.mkdirs();
        }

        FileWriter fileWriter = null;
        try{
            fileWriter = new FileWriter(logFile + File.separator + "log.txt", true);
        } catch(IOException e)
        {
            e.printStackTrace();
        }
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println(msg);
        printWriter.close();
    }




}
