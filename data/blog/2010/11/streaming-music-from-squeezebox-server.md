[A few years ago](/blog/2007/12/apple-iphone-ipod-touch-as-slimserver), I noticed that it was not possible to stream music from Squeezebox Server (previously called SlimServer, then SqueezeCenter) to an iPod Touch. Fortunately, things have changed: [bugs](http://bugs.slimdevices.com/show_bug.cgi?id=6266) have been fixed and iOS 4 now supports [multitasking](http://en.wikipedia.org/wiki/IOS_%28Apple%29#Multitasking) (since June 2010). A few weeks ago, I reinstalled Squeezebox Server on my Ubuntu server and realized that it was finally possible to use my iPhone to play music from my server. Here’s how I currently do it:

1. Start Safari on your iPhone
2. Enter the following URL: http://myserver:9000/stream.mp3 (or http://user:password@myserver:9000/stream.mp3, but this is less secure and will cause Safari to display a warning message)
3. Start [iPeng](http://penguinlovesmusic.de/) (a remote control application for Squeezebox Server) or any other equivalent application
4. Add tracks to the player matching your iPhone and play them

I don’t know if it’s the optimal way to do it or not, so any hints or suggestions will be appreciated. In my experience, the main drawback of this method is that Safari buffers a lot of audio before playing it, so expect a long delay (15-30 seconds) when playing a new track. Also, you won’t be able to use Safari to access any other sites. I guess a dedicated streaming client (instead of Safari Mobile) could help, but I don’t know if such an application even exists.

Update (November 27, 2010). There's a streaming client called [OPlayer Lite](http://itunes.apple.com/us/app/oplayer-lite/id385907472) that, according to my experience, buffers about half as much audio as Safari Mobile. I've also just discovered another application called [Squeezecast](https://web.archive.org/web/20110204103419/http://itunes.apple.com:80/us/app/squeezecast/id344982128?), but it's not free (it's $5.99, which sounds expensive as it apparently still requires iPeng to manage the playlists). Does anybody have any experience with it?

Another "trick" that I'm now using is to connect from my iPhone using the following URL:

http://user:password@myserver:9000/stream.mp3?name=Olivier%27s+iPhone&bitrate=160

The "bitrate" parameter in the URL forces the bitrate to the specified value if the player doesn't exist yet (so you don't have to change it in the settings). The "name" parameter is the name of the player to use or create. That way, your iPhone won't create a new player every time your IP address changes. Unfortunately, that feature is still not supported in Squeezebox Server. There's a [feature request for that on bugs.slimdevices.com](http://bugs.slimdevices.com/show_bug.cgi?id=801) that has been open since 2005! The good news is that it's pretty straightfoward to patch Squeezebox Server. In the processURL function in Slim/Web/HTTP.pm, add the following lines:

if ($params->{'name'}) {
 $address = $params->{'name'};
 main::INFOLOG && $log->is_info && $log->info("processURL using address=$address");
}

just before:

$client = Slim::Player::Client::getClient($address);

and the following lines:

if ($params->{'name'}) {
 $client->name($params->{'name'});
}

just after:

if ( $params->{userAgent} ) {
 ...
 $client->name( $agent . ' ' . string('FROM') . ' ' . $address );
}

Update (February 9, 2011). [iPeng now supports playback](http://penguinlovesmusic.de/2011/02/08/playback-comes-to-ipeng/) (i.e. it can act as a Squeezbox player without any third-party application). The feature is not free (it is available as an in app purchase), but, according to my tests, it is well worth the price ($4.99).
