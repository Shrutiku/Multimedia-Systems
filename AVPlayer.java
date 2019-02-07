package main.java.VideoSegmentationCompressionVR;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

public class AVPlayer extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;
	
	MyDecoder3 decoder;
	
	private Thread imageThread;
	
	private PlayingTimer timer;
	
	private boolean isPlaying = false;
	private boolean isPause = false;
	private boolean isStop = false;
	
	private String imageFileName;
	
	private JLabel labelFileNameImage = new JLabel("Image File:");
	private JLabel labelTimeCounter = new JLabel("00:00:00");
	private JLabel labelDuration = new JLabel("00:00:00");
	
	private JButton buttonPlay = new JButton("Play");
	private JButton buttonPause = new JButton("Pause");
	private JButton buttonStop = new JButton("Stop");
	
	private JSlider sliderTime = new JSlider();
	
	ImageReaderComponent component = new ImageReaderComponent();
	
	private final int width = 960;
	private final int height = 544;
	private final double fps = 30; // Frames Per Second
	private BufferedImage img;
	private long startTime;
	private long stopTime;
	
	/**
	 * Design the Java Swing UI with JFrame, buttons.
	 */
	public AVPlayer(String[] args, MyDecoder3 decoder) {		
		super("CSCI 576 Project Player");
		this.decoder = decoder;
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.anchor = GridBagConstraints.WEST;
		
		imageFileName = args[0];
		
		buttonPlay.setFont(new Font("Sans", Font.BOLD, 14));
//		buttonPlay.setIcon(iconPlay);
		buttonPlay.setEnabled(true);
		
		buttonPause.setFont(new Font("Sans", Font.BOLD, 14));
//		buttonPause.setIcon(iconPause);
		buttonPause.setEnabled(false);
		
		buttonStop.setFont(new Font("Sans", Font.BOLD, 14));
//		buttonStop.setIcon(iconStop);
		buttonStop.setEnabled(false);
		
		labelTimeCounter.setFont(new Font("Sans", Font.BOLD, 12));
		labelDuration.setFont(new Font("Sans", Font.BOLD, 12));
		
		sliderTime.setPreferredSize(new Dimension(960, 20));
		sliderTime.setEnabled(false);
		sliderTime.setValue(0);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		
		constraints.gridy = 1;
		add(labelFileNameImage, constraints);
		
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.gridy = 2;
		constraints.gridwidth = 1;
		add(labelTimeCounter, constraints);
		
		constraints.gridx = 1;
		add(sliderTime, constraints);
		
		constraints.gridx = 2;
		add(labelDuration, constraints);
		
		JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
		panelButtons.add(buttonPlay);
		panelButtons.add(buttonPause);
		panelButtons.add(buttonStop);
		
		constraints.gridwidth = 3;
		constraints.gridx = 0;
		constraints.gridy = 3;
		add(panelButtons, constraints);
		
		component.setPreferredSize(new Dimension(960, 544));
		constraints.gridheight = 3;
		constraints.gridwidth = 12;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 1;
		constraints.gridy = 12;		
		add(component, constraints);
		
		buttonPlay.addActionListener(this);
		buttonPause.addActionListener(this);
		buttonStop.addActionListener(this);
		
		pack();
		setResizable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

	}
	
	/**
	 * Handle click events on the buttons.
	 * Open : Choose Audio, Image files
	 * Play : Play Video
	 * Stop : Stop Video and restart both threads
	 * Pause : Interrupt both threads and resume them on play
	 */
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source instanceof JButton) {
			JButton button = (JButton) source;
			if (button == buttonPlay) {
				if (!isPlaying) {
					playBack();
				}
			} else if (button == buttonPause) {
				if (!isPause) {
					System.out.println("~~~~Pausing player~~~~");
					pausePlaying();
				} else {
					resumePlaying();
				}
			} else if (button == buttonStop) {
				if (!isStop) {
					System.out.println("~~~~Stopping player~~~~");
					stopPlaying();
				}
			}
		}
	}
	
	
	/**
	 * Stop, Pause, Resume audio and video
	 */
	private void stopPlaying() {
		isPause = false;
		isPlaying = false;
		isStop = true;
		
		buttonPause.setText("Pause");
		buttonPause.setEnabled(false);
		buttonPlay.setEnabled(true);
		buttonStop.setEnabled(false);
		
		timer.reset();
		timer.interrupt();	
		imageThread.stop();
	}
	
	private void pausePlaying() {
		buttonPause.setText("Resume");
		isPlaying = false;
		isPause = true;
		timer.pauseTimer();
		
		imageThread.suspend();
	}
	
	private void resumePlaying() {
		buttonPause.setText("Pause");
		isPause = false;
		isPlaying = true;
		timer.resumeTimer();
		
		imageThread.resume();
	}
	

	/**
	 * Start playing sound and images in sync
	 */
	private void playBack() {
		timer = new PlayingTimer(labelTimeCounter, sliderTime);
		timer.start();
		isPlaying = true;
		isPause = false;
		isStop = false;
		
		buttonPlay.setEnabled(false);
		buttonPause.setEnabled(true);
		buttonStop.setEnabled(true);
		
		System.out.println("--------in playback------");
		
		//Load Image Reader 
				
		imageThread = new Thread(new Runnable() {
			public void run() {						
				
				labelFileNameImage.setText("Image File: " + imageFileName);
				
				System.out.println("# image run...");
				long length = width*height*3;

				long timePerFrame = (long)(1000/fps);
				while(true){
					for(int i = 0; i < decoder.video.getFrameCount() && isPlaying; i++) {
						startTime = System.currentTimeMillis();
						
						img = decoder.readBytes(i);
						component.setImg(img);
						repaint();
						
						stopTime = System.currentTimeMillis();
						
						try {
							TimeUnit.MILLISECONDS.sleep(timePerFrame - (stopTime - startTime));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});

		System.out.println("--------start image thread-----");
		imageThread.start();
		
	}
}