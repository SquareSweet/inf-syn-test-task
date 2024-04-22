package utils;

import org.apache.hc.core5.http.impl.EnglishReasonPhraseCatalog;
import rawhttp.core.HttpVersion;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpResponse;
import rawhttp.core.body.StringBody;

import java.util.Locale;

public class HttpUtils {
    public RawHttpResponse buildResponse(Integer httpStatus, String body) {
        StringBuilder builder = new StringBuilder();
        builder
                .append(HttpVersion.HTTP_1_1)
                .append(" ")
                .append(httpStatus)
                .append(" ")
                .append(EnglishReasonPhraseCatalog.INSTANCE.getReason(httpStatus, Locale.ENGLISH))
                .append("\n")
                .append("Content-Type: application/json")
                .append("\n");

        RawHttp http = new RawHttp();
        RawHttpResponse response = http.parseResponse(builder.toString());
        if (body != null)
            response = response.withBody(new StringBody(body));
        return response;
    }

    public RawHttpResponse buildExceptionResponse(Integer httpStatus, Exception exception) {
        String body = "{ \"message\":\"" + exception.getMessage() + "\"}";
        return buildResponse(httpStatus, body);
    }
}
