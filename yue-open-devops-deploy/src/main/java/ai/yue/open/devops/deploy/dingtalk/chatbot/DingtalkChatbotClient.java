package ai.yue.open.devops.deploy.dingtalk.chatbot;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;

import ai.yue.open.devops.deploy.dingtalk.chatbot.message.Message;

/**
 * Created by 孙金川 on 2017/3/17.
 */
@Component
public class DingtalkChatbotClient {

	@Autowired
	RestTemplate restTemplate;
	
    public SendResult send(String webhook, Message message) {
    	RequestEntity<String> requestEntity = RequestEntity.post(URI.create(webhook))
    	.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
    	.body(message.toJsonString());
    	
        SendResult sendResult = new SendResult();
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(requestEntity, JSONObject.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            JSONObject result = responseEntity.getBody();
            
            Integer errcode = result.getInteger("errcode");
            String errmsg = result.getString("errmsg");
            sendResult.setErrorCode(errcode);
            sendResult.setErrorMsg(errmsg);
            sendResult.setIsSuccess(errcode.equals(0));
        }
        
        return sendResult;
    }
    
}


