package View;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.sarxos.webcam.Webcam;
import java.awt.Color;

public class WebCamViewPnael {

	private JFrame frame;
	public JButton Capturer=new JButton();
	public JButton envoyer=new JButton();
	public JLabel photo=new JLabel();
	public JPanel btn=new JPanel();

	Webcam webcam ;
	Boolean isRunning=true;
	Thread t=new VideoFeedTaker();


	public Webcam getWebcam() {
		return webcam;
	}


	public JFrame getFrame() {
		return frame;
	}
	

	public JButton getCapturer() {
		return Capturer;
	}


	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WebCamViewPnael window = new WebCamViewPnael();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public WebCamViewPnael() {
		initialize();
		webcam = webcam.getDefault();
		webcam.setViewSize(new Dimension(640,480));
		webcam.open();
		new VideoFeedTaker().start();

		
	}


	private void initialize() {
		frame = new JFrame();
		
		frame.setBounds(300, 100, 640, 500);
		photo.setBounds(100,100,500,200);
	    //photo.setIcon(new ImageIcon("Capture d��cran 2022-04-20 203800.png"));
		frame.add(photo);
		
		Capturer.setText("Capturer");
		envoyer.setText("Envoyer l'image");
		envoyer.setEnabled(false);


		frame.add(btn,BorderLayout.SOUTH);
        btn.setLayout(new GridLayout(1, 4,10,10));
		btn.add(Capturer);
//		btn.setLayout(new GridLayout(1, 4,10,10));
		btn.add(envoyer);
                
                Capturer.setBackground(new Color(0x1A844C));
                Capturer.setForeground(Color.WHITE);
                envoyer.setBackground(new Color(0x1A844C));
                envoyer.setForeground(Color.WHITE);

//	
//		Capturer.setBounds(500,350,200,100);
//		frame.add(Capturer,BorderLayout.SOUTH);
//		envoyer.setBounds(500,350,200,100);
//		frame.add(envoyer,BorderLayout.SOUTH);

		Capturer.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				envoyer.setEnabled(true);
				Capturer.setEnabled(false);

					new VideoFeedTaker().start();
					try {
						ImageIO.write(webcam.getImage(), "JPG", new File("cam/IMAGE-CAMERA-TMP.jpg"));
						System.out.println("Image Save");

					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					isRunning=false;


			}
		});
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				frame.dispose();
				webcam.close();
			

			}
		
		});
		
	}
	class VideoFeedTaker extends Thread{

		@Override
		public void run() {
			while(true&&isRunning) {
			Image image = webcam.getImage();
			photo.setIcon(new ImageIcon(image));
			
			try {
				Thread.sleep(40);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
		}
}
	public void ajouterListener(ActionListener listener) {
		envoyer.addActionListener(listener);
	}	
}