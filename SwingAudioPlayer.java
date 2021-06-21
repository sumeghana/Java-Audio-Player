package net.codejava.audio;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

public class SwingAudioPlayer extends JFrame implements ActionListener {
	private AudioPlayer player = new AudioPlayer();
	private Thread playbackThread;
	private PlayingTimer timer;

	private boolean isPlaying = false;
	private boolean isPause = false;
	
	private String audioFilePath;
	private String lastOpenPath;
	
	private JLabel labelFileName = new JLabel("Playing File:");
	private JLabel labelTimeCounter = new JLabel("00:00:00");
	private JLabel labelDuration = new JLabel("00:00:00");
	
	private JButton buttonOpen = new JButton("Open");
	private JButton buttonPlay = new JButton("Play");
	private JButton buttonPause = new JButton("Pause");
	
	private JSlider sliderTime = new JSlider();
	
	// Icons used for buttons
	private ImageIcon iconOpen = new ImageIcon(getClass().getResource(
			"/net/codejava/audio/images/Open.png"));
	private ImageIcon iconPlay = new ImageIcon(getClass().getResource(
			"/net/codejava/audio/images/Play.gif"));
	private ImageIcon iconStop = new ImageIcon(getClass().getResource(
			"/net/codejava/audio/images/Stop.gif"));
	private ImageIcon iconPause = new ImageIcon(getClass().getResource(
			"/net/codejava/audio/images/Pause.png"));
	
	
	public SwingAudioPlayer() {
		super("Java Audio Player");
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.anchor = GridBagConstraints.WEST;
		
		buttonOpen.setFont(new Font("Sans", Font.BOLD, 14));
		buttonOpen.setIcon(iconOpen);
		
		buttonPlay.setFont(new Font("Sans", Font.BOLD, 14));
		buttonPlay.setIcon(iconPlay);
		buttonPlay.setEnabled(false);
		
		buttonPause.setFont(new Font("Sans", Font.BOLD, 14));
		buttonPause.setIcon(iconPause);
		buttonPause.setEnabled(false);
		
		labelTimeCounter.setFont(new Font("Sans", Font.BOLD, 12));
		labelDuration.setFont(new Font("Sans", Font.BOLD, 12));
		
		sliderTime.setPreferredSize(new Dimension(400, 20));
		sliderTime.setEnabled(false);
		sliderTime.setValue(0);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		add(labelFileName, constraints);
		
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		add(labelTimeCounter, constraints);
		
		constraints.gridx = 1;
		add(sliderTime, constraints);
		
		constraints.gridx = 2;
		add(labelDuration, constraints);
		
		JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
		panelButtons.add(buttonOpen);
		panelButtons.add(buttonPlay);
		panelButtons.add(buttonPause);
		
		constraints.gridwidth = 3;
		constraints.gridx = 0;
		constraints.gridy = 2;
		add(panelButtons, constraints);
		
		buttonOpen.addActionListener(this);
		buttonPlay.addActionListener(this);
		buttonPause.addActionListener(this);
		
		pack();
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source instanceof JButton) {
			JButton button = (JButton) source;
			if (button == buttonOpen) {
				openFile();
			} else if (button == buttonPlay) {
				if (!isPlaying) {
					playBack();
				} else {
					stopPlaying();
				}
			} else if (button == buttonPause) {
				if (!isPause) {
					pausePlaying();
				} else {
					resumePlaying();
				}
			}
		}
	}

	private void openFile() {
		JFileChooser fileChooser = null;
		
		if (lastOpenPath != null && !lastOpenPath.equals("")) {
			fileChooser = new JFileChooser(lastOpenPath);
		} else {
			fileChooser = new JFileChooser();
		}
		
		FileFilter wavFilter = new FileFilter() {
			@Override
			public String getDescription() {
				return "Sound file (*.WAV)";
			}

			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				} else {
					return file.getName().toLowerCase().endsWith(".wav");
				}
			}
		};

		
		fileChooser.setFileFilter(wavFilter);
		fileChooser.setDialogTitle("Open Audio File");
		fileChooser.setAcceptAllFileFilterUsed(false);

		int userChoice = fileChooser.showOpenDialog(this);
		if (userChoice == JFileChooser.APPROVE_OPTION) {
			audioFilePath = fileChooser.getSelectedFile().getAbsolutePath();
			lastOpenPath = fileChooser.getSelectedFile().getParent();
			if (isPlaying || isPause) {
				stopPlaying();
				while (player.getAudioClip().isRunning()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			}
			playBack();
		}
	}

	private void playBack() {
		timer = new PlayingTimer(labelTimeCounter, sliderTime);
		timer.start();
		isPlaying = true;
		playbackThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {

					buttonPlay.setText("Stop");
					buttonPlay.setIcon(iconStop);
					buttonPlay.setEnabled(true);
					
					buttonPause.setText("Pause");
					buttonPause.setEnabled(true);
					
					player.load(audioFilePath);
					timer.setAudioClip(player.getAudioClip());
					labelFileName.setText("Playing File: " + audioFilePath);
					sliderTime.setMaximum((int) player.getClipSecondLength());
					
					labelDuration.setText(player.getClipLengthString());
					player.play();
					
					resetControls();

				} catch (UnsupportedAudioFileException ex) {
					JOptionPane.showMessageDialog(SwingAudioPlayer.this,  
							"The audio format is unsupported!", "Error", JOptionPane.ERROR_MESSAGE);
					resetControls();
					ex.printStackTrace();
				} catch (LineUnavailableException ex) {
					JOptionPane.showMessageDialog(SwingAudioPlayer.this,  
							"Could not play the audio file because line is unavailable!", "Error", JOptionPane.ERROR_MESSAGE);
					resetControls();
					ex.printStackTrace();
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(SwingAudioPlayer.this,  
							"I/O error while playing the audio file!", "Error", JOptionPane.ERROR_MESSAGE);
					resetControls();
					ex.printStackTrace();
				}

			}
		});

		playbackThread.start();
	}

	private void stopPlaying() {
		isPause = false;
		buttonPause.setText("Pause");
		buttonPause.setEnabled(false);
		timer.reset();
		timer.interrupt();
		player.stop();
		playbackThread.interrupt();
	}
	
	private void pausePlaying() {
		buttonPause.setText("Resume");
		isPause = true;
		player.pause();
		timer.pauseTimer();
		playbackThread.interrupt();
	}
	
	private void resumePlaying() {
		buttonPause.setText("Pause");
		isPause = false;
		player.resume();
		timer.resumeTimer();
		playbackThread.interrupt();		
	}
	
	private void resetControls() {
		timer.reset();
		timer.interrupt();

		buttonPlay.setText("Play");
		buttonPlay.setIcon(iconPlay);
		
		buttonPause.setEnabled(false);
		
		isPlaying = false;		
	}

	public static void main(String[] args) {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				new SwingAudioPlayer().setVisible(true);
			}
		});
	}


}