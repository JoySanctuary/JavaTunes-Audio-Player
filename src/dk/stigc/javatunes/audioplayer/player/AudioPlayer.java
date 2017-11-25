package dk.stigc.javatunes.audioplayer.player;

import java.io.*;

import dk.stigc.javatunes.audioplayer.other.*;
import dk.stigc.javatunes.audioplayer.tagreader.*;

public class AudioPlayer
{
	IAudioPlayerHook hook;
	private volatile int volume;
    public AudioInfo audioInfo;
    private BasePlayer player = new VoidPlayer();
    private SourceDataLineManager dlm = new SourceDataLineManager();
    
	public AudioPlayer()
	{
		this.setVolume(100);		
	}  
	
	public void addHook(IAudioPlayerHook hook)
	{
		this.hook = hook;
	}    

    public synchronized void pause() 
    {
   		dlm.pause();
    }
    
    /**
     * Use this to continue after paused
     */
    public synchronized void start() 
    {
   		dlm.start();
    }
    
    public synchronized void setVolume (int volume) 
    {
    	if (volume<0 || volume>100)
    		throw new RuntimeException("Volume should be between 0 and 100");
    	
    	this.volume = volume;
    	double gain = volume/100.0;
    	dlm.setVolume(gain);
    }
    
    public synchronized void stop()
    {	
    	Log.write("Stopping");
    	
    	player.stopThread();
    	
   		dlm.discardDataInLine();
    }
   
    public synchronized void stopAndWaitUntilPlayerThreadEnds() throws InterruptedException
    {	
    	stop();

    	player.join();
    }
    
    public boolean isPlaying()
    {
    	return player.running;
    }
    
	public synchronized AudioInfo play(String path) throws Exception
	{
		return play(new AudioImpl(path), false);
	}

	public synchronized AudioInfo play(IAudio audio) throws Exception
	{
		return play(audio, false);
	}
	
	public synchronized AudioInfo play(IAudio audio, boolean isAlbumMode) throws Exception
	{
    	if (player.hasEnded == false)
    		stop();
    	
    	audioInfo = new AudioInfo();
    	audioInfo.codec = audio.getCodec();
    	
    	InputstreamSelector inputStreamSelector = new InputstreamSelector();
    	InputStream is = inputStreamSelector.getInputStream(audio, audioInfo);
		audioInfo.lengthInBytes = inputStreamSelector.contentLength;
		audioInfo.granules = inputStreamSelector.granules;
		
    	extractMp4ContainerCodec(audio, inputStreamSelector);
    	
    	switch (audioInfo.codec)
		{
			case flac:
				player = new FLACPlayer();
				break;
			case vorbis:
				player = new OggPlayer();
				break;
			case wavpack:
				player = new WavPackPlayer();
				break;
			case aacadts:
				player = new AacAdtsPlayer();
				break;					
			case aac:
			case mp4container:
				player = new AacMp4Player();
				break;					
			case alac:
				player = new AlacPlayer();
				break;							
			default:
				player = new MP3Player();				
		}
		
		double gain = volume/100.0;
		
		player.hook = hook;
		player.playBackApi = dlm;
		player.initialize(is, audio, audioInfo, gain, isAlbumMode);	
		player.start();
		
		dlm.start();
		
		Log.write(player.getClass().getSimpleName() + " -> " + audio.getPath());

		return audioInfo;
	}

	private void extractMp4ContainerCodec(IAudio audio,
			InputstreamSelector inputStreamSelector)
			throws FileNotFoundException
	{
    	if (audioInfo.codec == Codec.mp4container 
    			&& inputStreamSelector.isRemote == false)
    	{
    		File file = new File(audio.getPath());
    		Track track = new TagReaderManager().read(file);
    		if (track != null)
    			audioInfo.codec = track.codec;
    	}
	}
    
    public int getVolume()
    {
    	return volume;
    }

	public void enableFalcOutput(OutputStream os) throws IOException
	{
		dlm.enableFlacOutput(null, os);
	}
	
	public void enableFalcOutput(File file) throws IOException
	{
		dlm.enableFlacOutput(file, null);
	}
	
	public void stopFlacOutput() throws IOException
	{
		dlm.stopFlacOutput();
	}
}
