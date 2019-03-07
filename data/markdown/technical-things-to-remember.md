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

## Convert FLAC files to MP3 (including tags) (OS X)

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

## Fix MySQL not starting

* error: "Do you already have another mysqld server running on socket: /var/run/mysqld/mysqld.sock"
* solution: [http://ubuntuforums.org/showthread.php?t=1861136](http://ubuntuforums.org/showthread.php?t=1861136)
* important : partie "/{,var/}run" (accolades)

## Install postfix on Debian to send e-mail without an external mail server

* install postfix package
* sudo mkfifo /var/spool/postfix/public/pickup
* sudo /etc/init.d/sendmail stop
* sudo /etc/init.d/postfix restart
* appeler newaliases après avoir modifié aliases (/etc/aliases)

## Make/rip an image from a DVD-Video (Mac only)

* use [RipIt](http://thelittleappfactory.com/ripit/)

## Install pure-ftpd on Debian

* install pure-ftpd package
* create folder for FTP user
* create new user following the instructions in [README.Virtual-Users](https://download.pureftpd.org/pub/pure-ftpd/doc/README.Virtual-Users)
* modify /etc/inetd.conf and update the FTP command as "/usr/sbin/pure-ftpd -l puredb:/etc/pure-ftpd/pureftpd.pdb" (needed only once)
* don't forget to commit the changes (pure-pw mkdb)
* restart inetd (killall -HUP inetd)

## Make a PDF from JPEG files

* use Mac OS Preview

## Make Logitech Media Server list multi-disc compilations correctly

A multi-disc compilation would be an album with several discs and different artists per track. The goal is to have it listed under each artist under "Artists", as well as a single entry under "Albums".

* add "COMPILATION" tag with a value of "1" using Mp3tag
* add an album artist, if needed
* if needed, update "MUSICBRAINZ_*" tags to force the album artist to a given name or to force the "various artists" mode
* make sure "List compilation albums under each artist" option is enabled
* make sure "List albums by all artists for that album" option is enabled
* make sure "Treat multi-disc sets as a single album" option is enabled
* make sure "Treat TPE2 MP3 tag as Album Artist" option is enabled
* if this doesn't work, make sure DISCNUMBER tags are present (one per folder)

## Make Logitech Media Server parse all folders correctly (guess tags)

* add the following "Guess Tags Formats" (Settings > Advanced > Formatting)
  * ARTIST - ALBUM/DISC COMMENT/TRACKNUM TITLE
  * ARTIST - ALBUM/DISC/TRACKNUM TITLE
  * ARTIST - ALBUM/TRACKNUM TITLE

## Make Tomcat follow symlinks

* add an "allowLinking" attribute in conf/context.xml (<Context allowLinking="true">)

## Mount a CD/DVD image under Windows

* use VCdControlTool.exe (available from microsoft.com)

## Mount an SSHFS volume with accented characters (OS X)

* use “-omodules=iconv,from_code=UTF-8,to_code=UTF-8-MAC” option

## Mount a read-only SSHFS volume using MacFUSE/MacFusion

* use "-o ro" option

## Print a PDF file including the form fields

* use [PDFPen](https://smilesoftware.com/pdfpen) (commercial, demo version prints watermarks)

## Remove extended file attributes in Mac OS X (asks for password to move/rename files)

* chmod -R -N <dir>

## Reset the System Management Controller (SMC) on MacBook Pro (late 2008)

* shutdown, remove battery, press/hold power button for 5 seconds ([link](https://support.apple.com/en-us/HT201295))
* in case of problems with fans, etc.

## Rotate a JPEG file losslessly

* use Picasa (also keeps EXIF tags)

## Set EXIF date/time from file "last modification time"

* exiftool '-FileModifyDate>DateTimeOriginal' <directory>

## Set "last modification time" of picture file from the EXIF date/time

* exiftool '-DateTimeOriginal>FileModifyDate' <directory>

or

* exiftool '-CreateDate>FileModifyDate' <directory> (works for iPhone MOV/MP4 files as well)

## Shift EXIF date/time in pictures

* exiftool "-AllDates+=01:02:03" -P <directory>

## Show hidden files in Finder (OS X)

* defaults write com.apple.finder AppleShowAllFiles TRUE
* killall Finder

## Shutdown completely an Ubuntu server (remotely)

* sudo shutdown -h -P now

## Split an animated GIF file

* use ImageMagick (convert image.gif image_%d.gif)

## Test the integrity of a JPEG file (old)

* use jpeginfo
* to make jpeginfo
  * download [jpeginfo](https://github.com/tjko/jpeginfo) sources
  * download [jpeglib](http://www.ijg.org/) sources
  * configure/make jpeglib
  * configure/make jpeginfo

## Wordpress - Fix home / site URL in Wordpress

* mysql -u root -p
* update wp_options set option_value='http://...' where option_name='home';
* update wp_options set option_value='http://...' where option_name='siteurl';
