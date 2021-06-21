# Java-Audio-Player
A swing based application that can playback audio files. Typical features include play ,pause ,resume and stop. Devloped using Java swing and Threading

We would like to present a swing based application that can playback audio files.This program works like a typical audio player. To select an audio file, we will be adding a standard mp3 library to import mp3 files. The features of this player include the duration of the audio file, which is shown in rightmost, current playing time is shown in leftmost. During the playback, the slider is moving gradually from left to the right to indicate the progress. Click the PAUSE button to temporarily pause the playback. The slider stops moving and the Pause button label becomes RESUME, which will consume the playback when clicked. The button STOP, to terminate playing the audio. The slider moves to thebeginning position and the Stop button becomes Play button which will start over the audio playback when clicked. During the playback, we can click the OPEN button to chooseanother audio file. In this case, the current playback will be stopped and start playing the newly chosen audio file. The sample player may look in this way


The program consists of three main classes:
-AudioPlayer.java: This is a utility class that provides primary functionalities for playing back an audio file like play, stop, pause and resume. It is based on the Java sound API. A separate thread should be spawned to use this utility class.
-PlayingTimer.java: This thread-based class is responsible to generate current playing time during the playback. It will also update the slider accordingly.
-SwingAudioPlayer.java: This is the main program which is based on a JFrame. It constructs the user interface and wires to AudioPlayer and PlayerTimer together to form a nice-looking and functional audio player program.
front end: swing,netbeans,visual studio.

