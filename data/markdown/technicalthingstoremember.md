# Technical things to remember

## Access Debian/Ubuntu from OS X (VNC)

* launch terminal
* connect to Debian server (ssh user@server)
* launch VNC server (vnc4server or vnc4server -geometry 1440x900 -depth 16)
* make sure the desktop used is the number 1 (or change router configuration to allow access on port 5900 + desktop number)
* connect with VNC client
* to quit, logout then kill VNC server (vnc4server -kill :1)

## Access Debian/Ubuntu from OS X (X11)

* launch X11 application
* connect to Debian server (ssh -X user@server)
* launch remote applications from terminal

## Add environment variable in OS X

* create /Users/<user>/.MacOSX/environment.plist file with PlistEdit Pro application
* log out and in again

## Apache

* enable an Apache 2 module : use a2enmod
* restart Apache 2 : /etc/init.d/apache2 restart or apache2ctl restart

## Archive an arbitrary Web site

* wget -r -k -p <url>
* see other arguments here: [http://www.gnu.org/software/wget/manual/wget.html](https://www.gnu.org/software/wget/manual/wget.html)

## Batch convert images

* mogrify -format jpeg -quality 25 *.bmp

## Batch rename files (with regular expressions) (Mac OS)

* use [NameChanger](https://mrrsoftware.com/namechanger/)

## Change FLAC tags via command line

* metaflac --list test.flac
* metaflac --remove-tag=Album *.flac
* metaflac --set-tag='album=Complete album name [Test]' *.flac
* on an entire folder
  * find . -iname "*.flac" -and -type f -print0 | xargs -0 -i metaflac --with-filename --show-tag='Compilation' "{}"
  * find . -iname "*.flac" -and -type f -print0 | xargs -0 -i metaflac --with-filename --show-tag='Album Artist' "{}"
  * find . -iname "*.flac" -and -type f -print0 | xargs -0 -i metaflac --with-filename --show-tag='ALBUMARTIST' "{}"
  * find . -iname "*.flac" -and -type f -print0 | xargs -0 -i metaflac --remove-tag='Compilation' "{}"
  * find . -iname "*.flac" -and -type f -print0 | xargs -0 -i metaflac --remove-tag='Album Artist' "{}"
  * find . -iname "*.flac" -and -type f -print0 | xargs -0 -i metaflac --set-tag='COMPILATION=1' "{}"
  * find . -iname "*.flac" -and -type f -print0 | xargs -0 -i metaflac --set-tag='ALBUMARTIST=Prince' "{}"

## Concatenate all files in a folder

* ls -1 | sed -e 's/^\\(.*\\)$/"\1"/g' | xargs cat > Folder1

## Control Tracker (indexer)

* start with : tracker-control -s
* get status with : tracker-control -S
* website : http://projects.gnome.org/tracker/

## Convert anything to H.264/AAC

* ffmpeg -i input -c:v libx264 -preset slow -crf 22 -c:a libfdk_aac -b:a 128k output.mkv
* more information [here](https://trac.ffmpeg.org/wiki/Encode/H.264)

## Convert a DVD to MKV files

* use Handbrake (but impossible to create a single MKV file?)
* use MakeMKV (but impossible to select the codecs?)

## Convert FLAC files to MP3 (including tags)

* use flac2mp3 Perl script (nothing better?)
* fix fixUpTrackNumber sub function, add following code before "if ($trackNum) {"
```perl
my $pos = index($trackNum, "/");
if ($pos >= 0) {
  $trackNum = substr($trackNum, 0, $pos);
}
```
* use Active Perl binary (Windows)

## Convert FLAC files to MP3 (including tags) (Mac OS X)

* for f in *.flac; do ffmpeg -i "$f" -ab 320k -map_metadata 0 -id3v2_version 3 "${f%flac}mp3"; done

## Convert PNG files to an ICO Windows icon file

* use ImageMagick: convert file1.png file2.png file3.png file.ico
* as of October 8, 2008, there's a transparency bug in ImageMagick

## Copy EXIF tags from one file to another one

* exiftool -TagsFromFile source.jpg dest.jpg

## Create a customized bootable image of Linux from scratch (using ptxdist, for e.g.)

* create a root image (kernel compilation, etc.) using ptxdist
* follow the instructions on "How to create an initramfs image" (correction: in the init script, "busybox --install -s" should be called before the first mounts)
* additional information about initramfs here and there
* trick : to determine which options to activate for kernel compilation, boot on the target hardware using u USB stick containing SystemRescueCd and display the hardware information (take note of the drivers to use)

## Create a script to launch a Wine application (Mac OS)

* follow instructions on [Making a Dock Icon](https://www.davidbaumgold.com/tutorials/wine-mac/#making-a-dock-icon)

## Download entire Flickr sets

* use [Flickr Downloadr](https://flickrdownloadr.com/)

## Download video streams

* use HiDownload and URL Helper (Windows)

## Extract a clip from a video file

* ffmpeg -ss 01:15:51 -t 00:05:59 -i in.mp4 -acodec copy -vcodec copy out.mp4

## Extract the audio of a DVD-Audio

* decrypt AUDIO_TS folder using DVDARipper 0.99f and WinDVD 7
* extract MLP files using DVDAExplorer Alpha 7
* uncompress MLP files to WAV using SurCode MLP 1.0.29 [eac3to](http://forum.doom9.org/showthread.php?t=125966) (FFmpeg?)
* convert 2-channel WAV files to 6-channel WAV files using wavewizard 0.54b
