package me.yorick.network.ws;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestWebsocket {

	
	private static Logger logger = LoggerFactory.getLogger(TestWebsocket.class);
	@Test
	public void connect() throws Exception {
		WebSocketFacotry wsf = new WebSocketFacotry();
		
		WebSocket ws = wsf.createWebSocket(URI.create("wss://ws.lightstream.bitflyer.com/json-rpc"), new BitFlyerWSListener());
		ws.connect();
		System.out.println(Thread.activeCount());
		
		//WebSocket ws2 = wsf.createWebSocket(URI.create("wss://ws-feed.pro.coinbase.com"), new GDAXWSListener());
		//ws2.connect();
		System.out.println(Thread.activeCount());
		Thread.currentThread().sleep(Long.MAX_VALUE);
	}
	
	private class BitFlyerWSListener implements WebSocketListener{
		@Override
		public void onOpen(WebSocket ws) {
			System.out.println("open");
			ws.send(" {\"jsonrpc\": \"2.0\", \"method\": \"subscribe\", \"params\": { \"channel\": \"lightning_board_snapshot_BTC_JPY\" }}");
		}
		
		@Override
		public void onFailed(Throwable e) {
			logger.error("error",e);
		}

		@Override
		public void onMessage(String text) {
			logger.info("onMessage:{}",text);
		}
	}
	
	private class GDAXWSListener implements WebSocketListener{
		@Override
		public void onOpen(WebSocket ws) {
			System.out.println("open");
			ws.send(" {\r\n" + 
					"    \"type\": \"subscribe\",\r\n" + 
					"    \"product_ids\": [\r\n" + 
					"        \"ETH-USD\",\r\n" + 
					"        \"ETH-EUR\"\r\n" + 
					"    ],\r\n" + 
					"    \"channels\": [\r\n" + 
					"        \"level2\",\r\n" + 
					"        \"heartbeat\",\r\n" + 
					"        {\r\n" + 
					"            \"name\": \"ticker\",\r\n" + 
					"            \"product_ids\": [\r\n" + 
					"                \"ETH-BTC\",\r\n" + 
					"                \"ETH-USD\"\r\n" + 
					"            ]\r\n" + 
					"        }\r\n" + 
					"    ]\r\n" + 
					"}");
		}
		
		@Override
		public void onFailed(Throwable e) {
			logger.error("error",e);
		}

		@Override
		public void onMessage(String text) {
			logger.info("onMessage:{}",text);
		}
	}
}
