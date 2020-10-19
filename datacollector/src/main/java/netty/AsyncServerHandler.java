package netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import common.ExecutorHelper;
import common.RestHelper;
import common.kafka.KafkaWriter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
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

    //step2: clean extract transform decoded event
    private JSONObject doExtractCleanTransform(ChannelHandlerContext context, HttpRequest req,
                                               JSONObject event) {
        logger.info(String.format("doExtractCleanTransform thread[%s]", Thread.currentThread().toString()));
        Preconditions.checkNotNull(event, "event is null");

        //TODO: add extract clean and transform logic
        return event;
    }

    //step3: send transformed event to kafka
    private Void send(ChannelHandlerContext context, HttpRequest request,
                      JSONObject event) {
        logger.info(String.format("send thread[%s]", Thread.currentThread().toString()));
        Preconditions.checkNotNull(event, "event is null");
        try {
            kafkaWriter.send(topic, event.toJSONString().getBytes(Charsets.UTF_8));
            sendResponse(context, OK,
                    RestHelper.genResponseString(200, "send to kafka success"));
        } catch (Exception e) {
            logger.error(String.format("exception caught, normEvent[%s]", event), e);
            sendResponse(context, INTERNAL_SERVER_ERROR,
                    RestHelper.genResponseString(500, "send to kafka failure"));
        }
        return null;
    }

    private static void sendResponse(ChannelHandlerContext context, HttpResponseStatus status, String msg) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        setAllowDomain(response);
        context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

    }

    private static void setAllowDomain(FullHttpResponse response) {
        response.headers().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.headers().set("Access-Control-Max-Age", "3600");
        response.headers().set("Access-Control-Allow-Credentials", "true");
    }


    final private Executor decoderExecutor = ExecutorHelper.createExecutor(2, "decoder");
    final private Executor ectExecutor = ExecutorHelper.createExecutor(8, "ect");
    final private Executor sendExecutor = ExecutorHelper.createExecutor(2, "send");

    // inner static helper class
    private static class ReferenceController {
        private final ChannelHandlerContext context;
        private final HttpRequest request;

        public ReferenceController(ChannelHandlerContext context, HttpRequest request) {
            this.context = context;
            this.request = request;
        }

        public void retain() {
            // Increase the reference count of this object, default 1
            ReferenceCountUtil.retain(context);
            ReferenceCountUtil.retain(request);
        }

        public void release() {
            // Decreases the reference count by 1
            // and de-allocates this object if the reference count reaches at 0.
            ReferenceCountUtil.release(context);
            ReferenceCountUtil.release(request);
        }
    }


    @Override
    protected void channelRead0(ChannelHandlerContext context, HttpRequest httpRequest) throws Exception {
        logger.info(String.format("current thread[%s]", Thread.currentThread().toString()));
        final ReferenceController referenceController = new ReferenceController(context, httpRequest);
        referenceController.retain();

        CompletableFuture
                .supplyAsync(() -> this.decode(context, httpRequest), this.decoderExecutor)
                .thenApplyAsync(e -> this.doExtractCleanTransform(context, httpRequest, e), this.ectExecutor)
                .thenApplyAsync(e -> this.send(context, httpRequest, e), this.sendExecutor)
                .thenAccept(e -> referenceController.release())
                .exceptionally(ex -> {
                    try {
                        logger.error(String.format("exception caught - [%s]", ex));
                        if(RequestException.class.isInstance(ex.getCause())) {
                            RequestException re = (RequestException)ex.getCause();
                            sendResponse(context, HttpResponseStatus.valueOf(re.getCode()), re.getResponse());
                        } else {
                            sendResponse(context, INTERNAL_SERVER_ERROR,
                                    RestHelper.genResponseString(500, "internal server error"));
                        }
                        return null;
                    } finally {
                        referenceController.release();
                    }
                });
    }
}
