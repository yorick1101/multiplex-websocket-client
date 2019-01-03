package me.yorick.network.ws;

public interface WebSocket {

	public void connect() throws InterruptedException;
	
	public void disConnect() throws InterruptedException;
	
	public void send(String text);
}
