package com.ysf.delayschedule.core.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Yeshufeng
 * @title
 * @date 2018/9/20
 */
@Component
@Scope("prototype")
@Slf4j
public class HttpSendProcessor {

    private CloseableHttpClient httpClient;

    protected static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public HttpSendProcessor(){
        createHttpClient();
    }

    public String execute(String uri,String params,String method,Header... headers){
        String response;

        switch (method){
            case "GET":
                response = get(uri,params,headers);
                break;
            case "POST":
                response = post(uri,params,headers);
                break;
            default: throw new RuntimeException();
        }
        return response;
    }


    private String get(String request, String params, Header... headers ){
        StringBuffer response = new StringBuffer();
        try {
            Map<String,String>  paramMap = mapper.readValue(params,HashMap.class);
            List<NameValuePair> pairs = Lists.newArrayList();

            for (Map.Entry<String,String> entity : paramMap.entrySet()) {
                NameValuePair pair = new BasicNameValuePair(entity.getKey(),entity.getValue());
                pairs.add(pair);
            }

            URI uri = new URIBuilder(request)
                    .setParameters(pairs)
                    .build();

            HttpGet httpGet = new HttpGet(uri);
            if (headers.length > 0) {
                Arrays.stream(headers).forEach(httpGet::setHeader);
            }
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

            log.info("GET Response Status:{},reasonPhrase:{}",httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent()));

            String inputLine;
            response = new StringBuffer();

            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
            reader.close();
            httpClient.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response.toString();
    }


    private String post(String request,String params,Header... headers){
        StringBuffer response = new StringBuffer();
        try {
            Map<String,String>  paramMap = mapper.readValue(params,HashMap.class);
            List<NameValuePair> pairs = Lists.newArrayList();

            for (Map.Entry<String,String> entity : paramMap.entrySet()) {
                NameValuePair pair = new BasicNameValuePair(entity.getKey(),entity.getValue());
                pairs.add(pair);
            }

            URI uri = new URIBuilder(request)
                    .setParameters(pairs)
                    .build();

            HttpPost httpPost = new HttpPost(uri);
            if (headers.length > 0) {
                Arrays.stream(headers).forEach(httpPost::setHeader);
            }
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

            log.info("GET Response Status:{},reasonPhrase:{}",httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent()));

            String inputLine;
            response = new StringBuffer();

            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
            reader.close();
            httpClient.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response.toString();
    }

    private void createHttpClient(){
        this.httpClient = HttpClients.createDefault();
    }



}
