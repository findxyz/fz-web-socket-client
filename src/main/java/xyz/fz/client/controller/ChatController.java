package xyz.fz.client.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import xyz.fz.client.ChatClient;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @RequestMapping("/send")
    @ResponseBody
    public Map<String, Object> send(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            ChatClient.send(params.get("message").toString());
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
        }
        return result;
    }

    @RequestMapping("/close")
    @ResponseBody
    public Map<String, Object> close() {
        Map<String, Object> result = new HashMap<>();
        try {
            ChatClient.disconnect();
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
        }
        return result;
    }
}
