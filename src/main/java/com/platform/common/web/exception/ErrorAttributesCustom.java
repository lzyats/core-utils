import com.platform.common.web.domain.AjaxResult;
import org.springframework.boot.web.error.ErrorAttributeOptions; // 注意导入正确的包
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Component
public class ErrorAttributesCustom extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        // 这里可以根据需要处理 options（例如是否包含堆栈信息）
        return AjaxResult.fail(); // 你的自定义返回
    }

}