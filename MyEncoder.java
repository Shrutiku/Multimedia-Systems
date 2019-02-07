package  main.java.VideoSegmentationCompressionVR;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JFrame;
import java.io.PrintStream;
public class MyEncoder {

	
	public void setFgBgBlocks(MyEncoder3 encoder, String filename){
		int width           = 960;
		int height          = 540;
		int megaBlockSize   = 16;
		int dctBlockSize    = 8;

		byte[]	RBytes;
		byte[]  IBytes;
		byte[]  TBytes;


		//Trial
		JFrame frame;

		IBytes = new byte[width * height * 3];
		RBytes = new byte[width * height * 3];
		TBytes = new byte[width * height * 3];
		

		try {
			

			byte a = 0;
			int size = width * height;			

			File imageFile = new File(filename);
			int numFrames = (int)(imageFile.length()/(width * height * 3));
			
//			PrintWriter encodedOutput = new PrintWriter("encoded.txt","UTF-8");
			
			/* Mod - Store */
		    File file = new File("tpeople.txt");
		    PrintStream ps = new PrintStream(file);
			InputStream is = new FileInputStream(imageFile);
			is.read(IBytes, 0, IBytes.length);
			MyFrame CurFrame = new MyFrame(IBytes,width,height);
			CurFrame.convertRGB2YUV();			
			System.setOut(ps);	
			int nfrm = 0;
			while (nfrm <  numFrames){
				is.read(RBytes, 0, RBytes.length);
				for (int h=0;h<width*height*3;h++){
					TBytes[h] = RBytes[h];
				}
				MyFrame RefFrame = new MyFrame(RBytes,width,height);

				RefFrame.convertRGB2YUV();
				nfrm++;

				MotionVectors mvec = new MotionVectors(IBytes,RBytes,width,height,megaBlockSize);
				mvec.SumAbsoluteDifference();

			//	IndexConverter converter = new IndexConverter(width,height,dctBlockSize);
				IndexConverter conv = new IndexConverter(width,height,megaBlockSize);

				//Before DCT convert R
				RefFrame.convertYUV2RGB();

				for (int rY=0; rY < RefFrame.height;rY = rY + dctBlockSize ){
					for (int rX = 0; rX < RefFrame.width;rX = rX + dctBlockSize){
			
						//System.out.print(conv.getBlockXIndex(rX, rY) + "   " + conv.getBlockYIndex(rX, rY));
						//Write to the output file
						if (mvec.refFrame.iBlocks[conv.getBlockXIndex(rX, rY)][conv.getBlockYIndex(rX, rY)].background) {
//							encodedOutput.write("0 ");
							System.out.print("B");
						}
						else{
//							encodedOutput.write("1 ");
							System.out.print("F");
						}						
					}
					System.out.println();
			}				
	System.out.println("***************************************************************************************************************************************************");

			}
		
		}
		catch (FileNotFoundException e) {
			System.out.println("ERROR - File Not Found");
		}catch (IOException e) {
			System.out.println("ERROR - File Length Error");
		} 
		
	}
}
