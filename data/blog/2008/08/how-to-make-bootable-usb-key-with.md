This is something that should be obvious, but I managed to lose several days on this problem. I wanted to install Debian on my brand new mini-ITX server (with no CD/DVD drive, only several USB ports and an SSD drive), but I only had a Windows machine available. These are the steps I followed:

1. Download [NTRawrite](http://ntrawrite.sourceforge.net/).
2. Download [syslinux](http://www.kernel.org/pub/linux/utils/boot/syslinux/). The file that you need is syslinux.exe. It is located in the "win32" folder of the ZIP archive you downloaded.
3. Download a Debian installer, e.g. the [current installer for Lenny](ftp://ftp.debian.org/debian/dists/lenny/main/installer-i386/current/images/hd-media/boot.img.gz).
4. Download a "netinst" ISO image for Debian, e.g. the [most current image](http://ftp.acc.umu.se/cdimage/release/current/i386/iso-cd/debian-500-i386-netinst.iso).
5. Uncompress boot.img.gz.
6. Format your USB flash drive, using FAT16, if possible.
7. Use NTRawrite to write boot.img to the USB flash drive. Just launch NTRawrite.exe, enter "boot.img" and the drive letter of the USB flash drive. This step can be quite slow.
8. Use syslinux to make the USB flash drive bootable. Just launch syslinux.exe with "-f e:" as argument (replace "e" by the drive letter of the USB flash drive).
9. "Safely remove" the USB flash drive and put it back (I'm not sure this step is mandatory/useful).
10. Copy the ISO image to the USB flash drive.
11. "Safely remove" the USB flash drive again. Voilà. It is ready.

If, for a reason or another, you don't manage to boot on your USB flash drive, try formatting it using [HP USB Disk Storage Format Tool](http://www.google.com/search?q=HP+USB+Disk+Storage+Format+Tool). It might help.
