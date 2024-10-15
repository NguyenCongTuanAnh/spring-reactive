package vn.fpt.springwebflux.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;
import vn.fpt.springwebflux.exception.BusinessException;

import java.util.Collection;
import java.util.Map;

import static vn.fpt.springwebflux.constant.CommonConstant.INPUT_NOT_VALID;
import static vn.fpt.springwebflux.constant.CommonConstant.PARSE_DATA_UNSUCCESSFULLY;
import static vn.fpt.springwebflux.constant.ErrorCodeConstant.ERROR_CODE_500;

public class DataUtils {

    public static boolean isNullOrEmpty(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNullOrEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNullOrEmpty(final Object[] collection) {
        return collection == null || collection.length == 0;
    }

    public static boolean isNullOrEmpty(final Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNullOrEmpty(Object obj1) {
        return obj1 == null || obj1.toString().trim().equals("");
    }


    public static <T> Mono<T> parseStringToObject(String values, Class<T> clazz) {
        if (DataUtils.isNullOrEmpty(values)) {
            return Mono.error(new BusinessException(ERROR_CODE_500, INPUT_NOT_VALID, null));
        }
        try {
            T result = new ObjectMapper().readValue(values, clazz);
            return Mono.just(result);
        } catch (JsonProcessingException e) {
            return Mono.error(new BusinessException(ERROR_CODE_500, PARSE_DATA_UNSUCCESSFULLY, null));
        }
    }

}
