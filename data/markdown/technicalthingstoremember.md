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
