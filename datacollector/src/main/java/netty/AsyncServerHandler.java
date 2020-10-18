package netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import common.RestHelper;
import common.kafka.KafkaWriter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpMethod.OPTIONS;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpResponseStatus.*;


public class AsyncServerHandler extends SimpleChannelInboundHandler<HttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(AsyncServerHandler.class);

    private final String kafkaBroker = "127.0.0.1:9092";
    private final String topic = "collector_event";
    private final KafkaWriter kafkaWriter = new KafkaWriter(kafkaBroker);


    // step1: decode message

    private JSONObject decode(ChannelHandlerContext ctx, HttpRequest request) {
        logger.info(String.format("decode thread[%s]", Thread.currentThread().toString()));

        if(!request.getDecoderResult().isSuccess()) {
            throw new RequestException(BAD_REQUEST.code(),
                    RestHelper.genResponseString(BAD_REQUEST.code(), "invalid request"));
        }

        if(OPTIONS.equals(request.getMethod())) {
            throw new RequestException(OK.code(),
                    RestHelper.genResponseString(OK.code(), "OPTIONS"));
        }

        if(request.getMethod() != POST) {
            throw new RequestException(METHOD_NOT_ALLOWED.code(),
                    RestHelper.genResponseString(METHOD_NOT_ALLOWED.code(), "method not allowed"));
        }
        String uri = request.getUri();
        if(!uri.equals("/event")) {
            throw new RequestException(BAD_REQUEST.code(),
                    RestHelper.genResponseString(BAD_REQUEST.code(), "invalid uri"));
        }

        byte[] body = readRequestBodyAsString((HttpContent) request);
        String jsonString = new String(body, Charsets.UTF_8);
        return JSON.parseObject(jsonString);
    }

    private byte[] readRequestBodyAsString(HttpContent httpContent) {
        ByteBuf byteBuf = httpContent.content();
        byte[] data = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(data);
        return data;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpRequest httpRequest) throws Exception {

    }
}
