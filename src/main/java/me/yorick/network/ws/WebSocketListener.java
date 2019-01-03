package me.yorick.network.ws;

public interface WebSocketListener {

	default public void onOpen(WebSocket ws) {}
	
	default public void onFailed(Throwable e) {}

	default public void onMessage(String text) {};
	
	public static WebSocketListener DUMMY = new WebSocketListener() {}; 
}
