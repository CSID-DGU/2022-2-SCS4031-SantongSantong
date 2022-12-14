package com.example.frontend;

import android.content.ContentValues;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class RequestHttpConnection {
    public String request(String _url, ContentValues _params){

        // HttpURLConnection 참조 변수
        HttpURLConnection urlConn = null;
        // URL 뒤에 붙여서 보낼 파라미터
        StringBuffer sbParams = new StringBuffer();
        // 1. StringBuffer에 파라미터 연결
        // 보낼 데이터가 없으면 파라미터를 비움
        if(_params == null)
            sbParams.append("");
        // 보낼 데이터가 있으면 파라미터를 채움
        else{
            // 파라미터가 2개 이상이면 파라미터 연결에 &가 필요하므로 스위칭할 변수 생성
            boolean isAnd = false;
            // 파라미터 키, 값
            String key;
            String value;

            for(Map.Entry<String, Object> parameter : _params.valueSet()){
                key = parameter.getKey();
                value = parameter.getValue().toString();

                // 파라미터가 두 개 이상일 때, 파라미터 사이에 &를 붙인다.
                if(isAnd)
                    sbParams.append("&");

                sbParams.append(key).append("=").append(value);

                // 파라미터가 두 개 이상이면 isAnd를 true로 바꾸고 다음 루푸부터 &를 붙인다.
                if(isAnd)
                    if(_params.size() >= 2)
                        isAnd = true;
            }
        }
        // 2. HttpURLConnection을 통해 web의 데이터를 가져온다
        try{
            URL url = new URL(_url);
            urlConn = (HttpURLConnection) url.openConnection();

            // 2-1. urlconn 설정
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Accept-Charset","UTF-8");
            urlConn.setRequestProperty("Context_Type","application/x-www-form-urlencoded;cahrset=UTF-8");

            // 2-2. parameter 전달 및 데이터 읽어오기
            String strParams = sbParams.toString();
            OutputStream os = urlConn.getOutputStream();
            os.write(strParams.getBytes("UTF-8"));
            os.flush();
            os.close();

            // 2-3. 연결 요청 확인
            // 실패 시 null 리턴 후 메소드 종료
            if(urlConn.getResponseCode() != HttpURLConnection.HTTP_OK)
                return null;

            // 2-4. 읽어온 결과물 리턴
            // 요청한 URL의 출력물을 BufferedReader로 받는다
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF-8"));

            // 출력물의 라인과 그 합에 대한 변수
            String line;
            String page = "";

            // 라인을 받아와 합친다
            while((line = reader.readLine()) != null){
                page += line;
            }
            return page;
        } catch (MalformedURLException e) { // for URL
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(urlConn != null)
                urlConn.disconnect();
        }
        return null;
    }
}
