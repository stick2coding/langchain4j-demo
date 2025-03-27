package com.dong.service;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ToolsService {

    @Tool("名字个数")
    public Integer nameCount(@P("姓名") String name){
        log.info("nameCount:{}", name);
        return name.length();
    }

    @Tool("天气")
    public String weather(@P("城市") String city){
        log.info("weather:{}", city);
        // 定义一个天气数组，里面包含十种不同的天气
        String[] weathers = {"阴天", "多云", "晴朗", "雨天", "雪天", "雷电", "雾天", "沙尘暴", "冰雹"};
        // 从1-9中获取随机一个随机数
        int random = (int) (Math.random() * 9) + 1;

        return weathers[random];
    }

}
