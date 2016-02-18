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
