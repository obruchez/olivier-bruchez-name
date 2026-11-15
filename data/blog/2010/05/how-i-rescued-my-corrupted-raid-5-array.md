My setup was the following: four 500-GB drives connected to a [Gigabyte GA-8I955X Royal](http://www.pcstats.com/artvnl.cfm?articleID=1834) motherboard from 2005 and configured as a RAID 5 array for a total of 1397 GB of usable space. On that motherboard, the RAID 5 functionality was provided by an Intel 955X Express Chipset ("[Intel Matrix Storage Technology](https://web.archive.org/web/20100527031840/http://www.intel.com/design/chipsets/matrixstorage_sb.htm)").

After a few years, the motherboard started to show problems (my PC would reboot randomly, for example). At the beginning, it only happened once every few weeks. My data never got corrupted and the RAID 5 array was always reconstructed when needed. After a while, though, booting my PC became harder and harder (requiring several reboots every time), until I couldn’t boot it at all.

My first attempt at retrieving the data from my hard drives was unsuccessful. I bought another motherboard (the exact same model, of course) from eBay and connected the four drives to it. I naively expected the motherboard to recognize the hard drives as a RAID 5 array and boot from it without any problem. That’s not what happened, though. Instead, the Intel Matrix Storage Manager complained that two of my drives were "Non-RAID Disks" (although it did detect the presence of a RAID 5 array).

![](/blog/images/2010/05/how-i-rescued-my-corrupted-raid-5-array-2.jpg){width=380}

That’s when I started panicking. Of course, my most important data was backed up (DVDs, [Jungle Disk](https://www.jungledisk.com/), etc.), but some of my files were not backed up at all (large audio/video files, downloads, etc.). I contacted Intel for advice, but they were not very helpful. After a few searches on the Web, I decided to try a RAID recovery tool (something I didn't know even existed): the demo/trial version of [RAID Reconstructor](http://www.runtime.org/raid.htm). Other recovery tools are also available, but that’s the first one I tried.

The idea is to create a [BartPE](http://www.runtime.org/peb.htm) "boot CD-ROM", boot from it, and then let the recovery software analyze the drives and automatically (more or less) detect the RAID parameters (start sector, drive order, block size, etc.). Using that trial version, I was able to view all the files on my hard drives and no error was reported, so I decided to buy the RAID Recovery Bundle (which includes the full version of RAID Reconstructor). It’s pretty expensive, but I guess it’s better than losing all my files...

Using the full version of RAID Reconstructor, I created an image of my RAID array on another hard drive (not a single image file, actually, but a set of more than 2100 688-MB files). I don’t remember how much time that step took exactly. Probably a couple of days at least (!). Files can then be extracted from that image using two different tools: Captain Nemo (if the file system is in good shape) and GetDataBack (if deleted files need to be recovered or if the file system is corrupted).

According to my experience, Captain Nemo and GetDataBack both have bugs/limitations:

- Captain Nemo (4.20) cannot retrieve files whose full names are longer than 260 characters; every time it tries to extract such a file, it will display an error and wait for the user to choose an option ("Ignore", "Abort", or "Ignore all"). Some large files (a few hundreds megabytes or more) are also incorrectly copied (the resulting file is zero-sized).

- GetDataBack (4.00) doesn’t have any problem with long file names or large files, but it won’t extract/copy empty folders (not a serious limitation, I admit).

Since I’m a bit paranoid with my data, I chose to extract my files both with Captain Nemo and GetDataBack. I then compared the two sets of files using [DiffMerge](http://www.sourcegear.com/diffmerge/) (GNU diff doesn’t handle large files well, as it apparently tends to load them into memory...) and replaced the zero-sized files extracted by Captain Nemo with the same (correctly sized) files extracted by GetDataBack. I also compared the files from the RAID array with my backups and checked the integrity of a subset of those files (mainly FLAC, JPEG, ZIP, and GZ files) using a small utility I wrote.

The moral of the story is that you should never trust a hard drive or, more generally, the hardware your data depends on (a RAID controller, a motherboard, etc.), as it will always fail, eventually. So, always backup your data. All of it.
