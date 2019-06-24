package com.osf.sp.controller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osf.sp.dao.NaverTranslationDAO;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class NaverTranslationController {
	@Resource
	private NaverTranslationDAO ntdao;
	
	@GetMapping("/translations")
	public @ResponseBody List<Map<String,Object>> getTranslations(){
		return ntdao.selectTranslationHisList();
	}
	
	@GetMapping("/translation/{target}/{source}/{text}")
	public @ResponseBody Map<String,Object> doTranslation(
			@PathVariable("target") String target,
			@PathVariable("source") String source,
			@PathVariable("text") String text
			) {
		log.info("target=>{},source=>{},text=>{}",
				new String[]{target,source,text});
		Map<String,String> param = new HashMap<>();
		param.put("target", target);
		param.put("source", source);
		param.put("text", text);
		Map<String,Object> rMap = ntdao.selectTranslationHisOne(param);
		if(rMap==null) {
			rMap = translationTest(param);
			if(rMap.get("errorCode")!=null) {
				param.put("error", rMap.get("errorCode").toString());
				param.put("result", "");
            }else {
				param.put("error", "");
				/*
				 * {
						message={
							@type=response, 
							@service=naverservice.nmt.proxy, 
							@version=1.0.0, 
							result={
								srcLangType=en, 
								tarLangType=ko, 
								translatedText=안녕하세요
							}
						}
					}
				 */
				Map<String,Object> map2 = (Map)rMap.get("message");
				Map<String,Object> map3 = (Map)map2.get("result");
				String result = map3.get("translatedText").toString();
            	param.put("result", ((Map)((Map)rMap.get("message")).get("result")).get("translatedText").toString());
            }
			ntdao.insertTranslationHis(param);
			rMap = ntdao.selectTranslationHisOne(param);
		}else {
			ntdao.updateTranslationHis(rMap);
		}
		if(rMap.get("TH_RESULT") instanceof Clob) {
			rMap.put("TH_RESULT", clobToString((Clob)rMap.get("TH_RESULT")));
		}
		return rMap;
	}
	private String clobToString(Clob data) {
	    StringBuilder sb = new StringBuilder();
	    try {
	        Reader reader = data.getCharacterStream();
	        BufferedReader br = new BufferedReader(reader);

	        String line;
	        while(null != (line = br.readLine())) {
	            sb.append(line);
	        }
	        br.close();
	    } catch (SQLException e) {
	    } catch (IOException e) {
	    }
	    return sb.toString();
	}
	private Map<String,Object> translationTest(Map<String,String> param) {
		String clientId = "PPzE6AulijIdSvRmJ1Mg";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "gPB08e6FBr";//애플리케이션 클라이언트 시크릿값";
        String text = param.get("text");
        String source = param.get("source");
        String target = param.get("target");
        try {
            text = URLEncoder.encode(text, "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            // post request
            String postParams = "source=" + source + "&target=" + target + "&text=" + text;
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            ObjectMapper om = new ObjectMapper();
            br.close();
            Map<String,Object> rMap = om.readValue(response.toString(), Map.class);
            if(rMap.get("errorCode")!=null) {
            	
            }
            log.info("rMap=>{}",rMap);
            return rMap;
        } catch (Exception e) {
            log.error("error=>{}",e);
        }
        return null;
	}
	
	public static void main(String[] args) {
		String str ="";
		for(int i=0;i<=10000;i++){
			str += i+"";
		}
		System.out.print(str);
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<=10000;i++){
			sb.append(i+"");
		}
		System.out.print(sb.toString());
	}
}















