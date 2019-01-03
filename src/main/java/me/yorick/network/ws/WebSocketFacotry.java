package me.yorick.network.ws;

import java.net.URI;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketScheme;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public class WebSocketFacotry {
	private final Logger logger = LoggerFactory.getLogger(WebSocketFacotry.class);
	private final EventLoopGroup eventLoopGrop;

	public WebSocketFacotry() {
		this(1);
	}

	public WebSocketFacotry(int threadCount) {
		eventLoopGrop = new NioEventLoopGroup(threadCount);
	}

	public void createWebSocket() {

	}

	public WebSocket createWebSocket(URI uri, WebSocketListener listener) throws Exception {
		WebSocketScheme scheme; 
		if(uri.getScheme().equalsIgnoreCase("ws")) {
			scheme = WebSocketScheme.WS;
		}else if(uri.getScheme().equalsIgnoreCase("wss")) {
			scheme = WebSocketScheme.WSS;
		}else
			throw new IllegalArgumentException("Illegal Scheme:"+uri.toString());

		final Bootstrap bootstrap = new Bootstrap();
		final WebSocket ws =  new WebSocketImpl(bootstrap, uri.getHost(), scheme.port());
		final WebSocketClientHandler wsHandler = new WebSocketClientHandler(ws, WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders(), 327680), listener);
		final SslContext sslCtx = (scheme.equals(WebSocketScheme.WSS))?SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build():null;

		bootstrap.option(ChannelOption.SO_KEEPALIVE,true)
		.option(ChannelOption.TCP_NODELAY,true)
		.group(eventLoopGrop)
		.channel(NioSocketChannel.class)
		.handler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline p = ch.pipeline();
				if (sslCtx != null) {
					p.addLast(sslCtx.newHandler(ch.alloc(), uri.getHost(), scheme.port()));
				}
				p.addLast(new HttpClientCodec());
				p.addLast(new HttpObjectAggregator(131072)); 
				p.addLast(wsHandler);
				p.addLast(new SimpleChannelInboundHandler<WebSocketFrame>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
                    	logger.info("{}:{}",frame.getClass().getName(),frame.content().readableBytes());
                    	try {
                    		byte[] data = new byte[frame.content().readableBytes()];
                    		frame.content().readBytes(data);
                    		StringBuilder strb = new StringBuilder();
                    		for(int i =0;i<data.length;i++) strb.append((int)data[i]);
                    		logger.info("bytes: {}",strb);
                    		listener.onMessage("Message:"+new String(data));
                    	}catch(Exception e) {
                    		e.printStackTrace();
                    	}
                    }
                    
                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                            throws Exception {
                    	System.out.println(cause.getMessage());
                    	cause.printStackTrace(System.out);
                        ctx.fireExceptionCaught(cause);
                        
                    }
                });
			}

		});
		return ws;
	}
	
	private static class WebSocketImpl implements WebSocket{
		final private Bootstrap bootstrap;
		final private String host;
		final private int port;
		private Channel channel;
		
		WebSocketImpl(final Bootstrap bootstrap, String host, int port){
			this.bootstrap = bootstrap;
			this.host = host;
			this.port = port;
		}
		
		@Override
		public void connect() throws InterruptedException {
			channel = bootstrap.connect(host, port).sync().channel();
		}

		@Override
		public void disConnect() throws InterruptedException {
			channel.disconnect().sync();
		}

		@Override
		public void send(String text) {
			try {
				channel.writeAndFlush(new TextWebSocketFrame(true, 0, text)).sync();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}
