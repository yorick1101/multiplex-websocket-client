package me.yorick.network.ws;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;

public class WebSocketClientHandler extends WebSocketClientProtocolHandler {

	private final WebSocketListener listener;
	private final WebSocket ws;
	
	public WebSocketClientHandler(WebSocket ws, WebSocketClientHandshaker handshaker, WebSocketListener listener) {
		super(handshaker);
		this.ws = ws;
		this.listener = (listener==null)? WebSocketListener.DUMMY : listener;
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws java.lang.Exception{
		if(evt == 	WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
			listener.onOpen(ws);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		listener.onFailed(cause);
	}


}
