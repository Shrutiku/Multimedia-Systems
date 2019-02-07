package main.java.VideoSegmentationCompressionVR;

import java.io.IOException;

public class DCTThread implements Runnable {
	private MyEncoder3 encoder;
	private int frame;
	
	
	public DCTThread(MyEncoder3 encoder, int frame) {
		this.encoder = encoder;
		this.frame = frame;
	}
	
    public void run() {
    	try {
			encoder.calculateDCT(frame,encoder.dctResultMap);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}