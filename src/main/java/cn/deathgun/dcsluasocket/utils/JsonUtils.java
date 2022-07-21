package cn.deathgun.dcsluasocket.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author leo
 * @desc Json工具.
 */
public final class JsonUtils {

    private JsonUtils() {
    }

    /**
     * 对象转换为json
     *
     * @param object
     * @return
     */
    public static String beanToJson(Object object) {
        return JSON.toJSONString(object);
    }


}