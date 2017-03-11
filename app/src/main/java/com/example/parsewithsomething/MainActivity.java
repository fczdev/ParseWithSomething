package com.example.parsewithsomething;

import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.R.attr.breadCrumbShortTitle;
import static android.R.attr.id;
import static android.R.attr.logoDescription;
import static android.view.View.X;

public class MainActivity extends AppCompatActivity {

    TextView responseText;

    Button requestBtn;

    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        responseText = (TextView)findViewById(R.id.responseText);
        requestBtn = (Button)findViewById(R.id.sendResquest);
        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.sendResquest){
                    //sendRequestWithHttpURIConnection();
                    sendRequestWithOkhttp();
                }
            }
        });
    }

    private void sendRequestWithOkhttp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://11.14.4.18/get_data.xml")
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    showResponse(responseData);
                    //parseXMLWithPull(responseData);
                    parseXMLWithSAX(responseData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseXMLWithSAX(String xmlData) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            XMLReader xmlReader = factory.newSAXParser().getXMLReader();
            ContentHandler handler = new ContentHandler();
            //将ContentHandler的实例对象设置到XMLReader中去
            xmlReader.setContentHandler(handler);
            xmlReader.parse(new InputSource(new StringReader(xmlData)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseXMLWithPull(String responseData) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(responseData));
            int eventType = xmlPullParser.getEventType();
            String id = "";
            String name = "";
            String version = "";
            while (eventType!=XmlPullParser.END_DOCUMENT){
                String nodeName = xmlPullParser.getName();
                switch (eventType){
                    case XmlPullParser.START_TAG:{
                        if("id".equals(nodeName)){
                            id = xmlPullParser.nextText();
                        }else if("name".equals(nodeName)){
                            name = xmlPullParser.nextText();
                        }else if("version".equals(nodeName)){
                            version = xmlPullParser.nextText();
                        }
                        break;
                    }
                    case XmlPullParser.END_TAG:{
                        if("app".equals(nodeName)){
                            Log.d(TAG, "parseXMLWithPull: id is "+id);
                            Log.d(TAG, "parseXMLWithPull:  name is "+name);
                            Log.d(TAG, "parseXMLWithPull:  version is "+version);
                        }
                        break;
                    }
                    default:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendRequestWithHttpURIConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL("https://www.baidu.com");
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(8000);
                    connection.setConnectTimeout(8000);
                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine())!=null){
                        response.append(line);
                    }
                    showResponse(response.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showResponse(final String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                responseText.setText(response);
            }
        });
    }

}
