package dk.stigc.javatunes.audioplayer.tests;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import dk.stigc.javatunes.audioplayer.other.Codec;
import dk.stigc.javatunes.audioplayer.other.Common;
import dk.stigc.javatunes.audioplayer.other.Track;
import dk.stigc.javatunes.audioplayer.player.AudioInfoInternal;
import dk.stigc.javatunes.audioplayer.player.AudioPlayer;
import dk.stigc.javatunes.audioplayer.player.BasePlayer;
import dk.stigc.javatunes.audioplayer.player.IAudio;
import dk.stigc.javatunes.audioplayer.player.IAudioPlayerHook;
import dk.stigc.javatunes.audioplayer.tagreader.TagReaderManager;

public class Tests
{
	String root = "C:\\data\\Projekter\\Eclipse.workspace\\JavaTunes\\other\\Test audio files\\";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception 
	{
	   Logger logger = Logger.getLogger("javatunes.mediaplayer");
	   logger.setUseParentHandlers(false);
	   ConsoleHandler handler = new ConsoleHandler();
	   handler.setFormatter(new LogFormatter());
	   logger.addHandler(handler);
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void whenPathDoesNotExists() throws Exception
	{
	    thrown.expect(Exception.class);
	    thrown.expectMessage("not.found does not exists");
		new AudioPlayer().play("not.found");
	}

	@Test
	public void decoderShouldReportError() throws Exception
	{
		Track track = new Track();
		track.path = "lincense.txt";
		track.codec = Codec.mp3;
		
		final List<String> list = Collections.synchronizedList(new ArrayList<String>());

		final AudioPlayer audioPlayer = new AudioPlayer();
		audioPlayer.addHook(new IAudioPlayerHook() {
			@Override
			public void audioInterrupted(IAudio audio)
			{
				list.add("audioInterrupted");
			}
			@Override
			public void audioFailed(IAudio audio, Exception ex)
			{
				list.add(ex.getMessage());	
				
			}
			@Override
			public void audioEnded(IAudio audio)
			{
				list.add("audioEnded");
				
			}
		});
		
		audioPlayer.play(track);
		
		while(list.size()==0)
			Thread.sleep(100);
		
		assertEquals(1, list.size());
		assertTrue(list.contains("Missing mp3 header"));
	}
	
	@Test
	public void alacWillPlay() throws Exception
	{
		playFor5Seconds(root + "ALAC\\08 Lilac.m4a");
	}
	
	@Test
	public void vorbisWillPlay() throws Exception
	{
		playFor5Seconds(root + "Vorbis\\Abba-Chiquitta.ogg");
	}
	
	@Test
	public void flacWillPlay() throws Exception
	{
		playFor5Seconds(root + "FLAC\\07 Det �r en n�d.flac");
	}
	
	@Test
	public void wavPackWillPlay() throws Exception
	{
		playFor5Seconds(root + "WavPack\\Track01.wv");
	}
	

	@Test
	public void aacAdtsWillPlay() throws Exception
	{
		playFor5Seconds(root + "AAC\\dr.aac");
	}
	
	@Test
	public void twoAudioPlayers() throws Exception
	{
		AudioPlayer ap1 = new AudioPlayer();
		ap1.play(root + "AAC\\03 Down The Nightclub.m4a");
		
		AudioPlayer ap2 = new AudioPlayer();
		ap2.play(root + "AAC\\dr.aac");
		
		Thread.sleep(5000);
		
		ap1.stop();
		ap2.stop();
	}

	@Test
	public void aac2() throws Exception
	{
		playFor5Seconds(root + "AAC\\SBR 02 Loca (feat. Dizzee Rascal).m4a");
	}
	
	
	@Test
	public void aacWithLcWillPlay() throws Exception
	{
		playFor5Seconds(root + "AAC\\03 Down The Nightclub.m4a");
	}
	

	@Test
	public void aacWithSbrWillWork1() throws Exception
	{
		playFor5Seconds(root + "AAC\\SBR 06 One In A Million.m4a");
	}

	@Test
	public void aacWithSbrWillWork2() throws Exception
	{
		playFor5Seconds("http://51.254.29.40:80/stream3");
	}
	
	@Test
	public void opusShoutcastWillWork() throws Exception
	{
		playFor5Seconds("http://dir.xiph.org/listen/1086535/listen.m3u");
	}
	
	@Test
	public void opusWillWork() throws Exception
	{
	//	playFor5Seconds(root + "opus\\2-8000hz.opus");
		playFor5Seconds(root + "opus\\11025.opus");
		playFor5Seconds(root + "opus\\11025-mono.opus");
		playFor5Seconds(root + "opus\\22500.opus");
		playFor5Seconds(root + "opus\\22500-mono.opus");
		playFor5Seconds(root + "opus\\44100.opus");
		playFor5Seconds(root + "opus\\44100-mono.opus");
	}
	
	@Test
	public void gaplessPlaybackWillWork() throws Exception
	{
		TestPlayer player = new TestPlayer();
		player.tracks.add(root + "gapless.test.samples\\Vorbis\\01 Track01.ogg");
		player.tracks.add(root + "gapless.test.samples\\Vorbis\\02 Track02.ogg");
		player.tracks.add(root + "gapless.test.samples\\Vorbis\\03 Track03.ogg");
		player.start();
		
		while (player.noMoreTracks == false)
		{
			player.printInfo();
			Thread.sleep(1000);
		}
	}
	
	@Test
	public void shoutCastWillWork() throws Exception
	{
		TestPlayer player = new TestPlayer();
		player.tracks.add("http://178.33.104.250:80/stream");
		player.tracks.add("http://streaming.radio24syv.dk/pls/24syv_96_IR.pls");
		player.tracks.add("http://live-icy.gss.dr.dk:8000/A/A03L.mp3.m3u");
		player.tracks.add("http://51.254.29.40:80/stream3");
		player.tracks.add("http://178.33.45.203:80/stream2");
		
		player.start();
		
		int seconds = 0;
		while (player.noMoreTracks == false)
		{
			seconds++;
			Thread.sleep(1000);
			player.printInfo();
			if (seconds % 5 == 0)
				player.playNextTrack();
		}
		
		player.stop();
	}
	
	public static String execCmd(String[] cmds) throws java.io.IOException {
	    java.util.Scanner s = new java.util.Scanner(
	    		Runtime.getRuntime().exec(cmds).getInputStream())
	    		.useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	
	public void testingYoutube() throws Exception
	{
		String[] cmds = new String[]{
				"C:\\Users\\Stig\\Desktop\\youtube-dl.exe"
				, "--cookies"
				, "cookies.txt"
				, "--get-url"
				, "https://www.youtube.com/watch?v=kJQP7kiw5Fk"
		};
		
		String result = execCmd(cmds);
		System.out.println(result);
		
		String lines[] = result.split("\\r?\\n");
		if (lines[1].indexOf("https://")==0 || lines[1].indexOf("http://")==0)
		{
			AudioPlayer player = new AudioPlayer();
			player.play(lines[1]);
			while (player.isPlaying())
			{
				Common.sleep(1000);	
				write(" * " + player.getAudioInfo().toString());
			}
		}
//		AudioPlayer player = new AudioPlayer();
//		player.play("http://streaming.radio24syv.dk/pls/24syv_96_IR.pls");
//		while (player.isPlaying())
//		{
//			Common.sleep(1000);	
//			write(" * " + player.getAudioInfo().toString());
//		}
	}
	
	@Test
	public void globalReplayGain() throws Exception
	{
		BasePlayer.setGlobalRpgain(-20);
		playFor5Seconds(root + "AAC\\03 Down The Nightclub.m4a");
		
		BasePlayer.setGlobalRpgain(5);
		playFor5Seconds(root + "AAC\\03 Down The Nightclub.m4a");
		
		BasePlayer.setGlobalRpgain(0);
		playFor5Seconds(root + "AAC\\03 Down The Nightclub.m4a");
	}

	@Test
	public void mp3WillPlay() throws Exception
	{
		playFor5Seconds(root + "MP3\\id3v2.4 UTF-8 Nanna.mp3");

	}

	@Test
	public void bps24WillWork() throws Exception
	{
		playFor5Seconds(root + "FLAC\\24bps-96khz.01 999,999.flac");
	}
	
	@Test
	public void bps8WillWork() throws Exception
	{
		playFor5Seconds(root + "WavPack\\8bit.wv");
	}
	
	@Test
	public void tracksCanBeChanged() throws Exception
	{
		AudioPlayer audioPlayer = new AudioPlayer();
		audioPlayer.play(root + "WavPack\\8bit.wv");
		Thread.sleep(1000);
		
		audioPlayer.play(root + "AAC\\03 Down The Nightclub.m4a");
		Thread.sleep(1000);
		audioPlayer.stop();
	}
	
	@Test
	public void gitHubDemoTest() throws Exception
	{
		File file = new File(root + "WavPack\\8bit.wv");
		Track track = new TagReaderManager().read(file);
		write(track.toString());
		
		AudioPlayer player = new AudioPlayer();
		player.play(track, false);
		
		while (player.isPlaying()) 
		{
			write(player.getAudioInfo().toString());
			Thread.sleep(1000);
		}
	}
	
	@Test
	public void gitHubDemoTest2() throws Exception
	{
		AudioPlayer player = new AudioPlayer();
		player.enableFlacOutput(new File("output.flac"));
		player.play(root + "ALAC\\08 Lilac.m4a");
		player.setOutputToMixer(false);
		player.waitUntilCurrentAudioHasEnded();
		player.finishFlacOutput();

		player.setOutputToMixer(true); 
		player.play("output.flac");
		while (player.isPlaying()) 
		{
			write(player.getAudioInfo().toString());
			Thread.sleep(1000);
		}
	}
	
	@Test
	public void pauseShouldWork() throws Exception
	{
		String path = root + "MP3\\id3v2.4 UTF-8 Nanna.mp3";
		AudioPlayer audioPlayer = new AudioPlayer();
		audioPlayer.play(path);
		write("pause");
		audioPlayer.pause();
		audioPlayer.pause(); //allowed more than 1..
		Thread.sleep(2000);
		
		write("pause");
		audioPlayer.start();
		audioPlayer.start(); //allowed more than 1..
		Thread.sleep(2000);
		
		write("pause and play");
		audioPlayer.pause();
		audioPlayer.play(path);
		Thread.sleep(2000);
		Thread.sleep(2000);
		audioPlayer.stopAndWaitUntilPlayerThreadEnds();
	}

	
	@Test
	public void audioInfoWillWork() throws Exception
	{
		AudioPlayer audioPlayer = new AudioPlayer();
		audioPlayer.play(root + "AAC\\03 Down The Nightclub.m4a");

		while(audioPlayer.isPlaying())
		{
			write(audioPlayer.getAudioInfo().toString());
			Thread.sleep(50);
		}
	}
	
	
	@Test
	public void displayMixerInfo()
	{
	  Mixer.Info [] mixersInfo = AudioSystem.getMixerInfo();

	  for (Mixer.Info mixerInfo : mixersInfo)
	  {
		 write("Mixer: " + mixerInfo.getName());

	     Mixer mixer = AudioSystem.getMixer(mixerInfo);

	     Line.Info [] sourceLineInfo = mixer.getSourceLineInfo();
	     for (Line.Info info : sourceLineInfo)
	       showLineInfo(info);

	     Line.Info [] targetLineInfo = mixer.getTargetLineInfo();
	     for (Line.Info info : targetLineInfo)
	       showLineInfo(info);
	   }
	}


	private static void showLineInfo(final Line.Info lineInfo)
	{
		write("  " + lineInfo.toString());

	  if (lineInfo instanceof DataLine.Info)
	   {
	     DataLine.Info dataLineInfo = (DataLine.Info)lineInfo;

	     AudioFormat [] formats = dataLineInfo.getFormats();
	     for (final AudioFormat format : formats)
	    	 write("    " + format.toString());
	   }
	}
	
	private void playFor5Seconds(String path) throws Exception
	{
		playFor5Seconds(path, null);
	}
	
	private void playFor5Seconds(String path, Track track) throws Exception
	{
		AudioPlayer audioPlayer = new AudioPlayer();
		
		if (track != null)
			audioPlayer.play(track, false);
		else
			audioPlayer.play(path);
				
		long startTime = System.currentTimeMillis();
		
		while (System.currentTimeMillis() - startTime < 5000)
		{
			Common.sleep(1000);	
			write(" * " + audioPlayer.getAudioInfo().toString());
		}
		
		audioPlayer.stopAndWaitUntilPlayerThreadEnds();
	}

	private static void write(String msg)
	{
		System.out.println("      " + msg);
	}
}
