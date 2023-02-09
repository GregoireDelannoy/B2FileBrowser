# B2FileBrowser
Minimalist Android file browser for Backblaze B2

Coded in a few days as a way to discover Kotlin + Android dev, and ~~to fill the lack of ANY usable android app to browse B2.~~
**UPDATE: Backblaze android app now supports B2 buckets: https://play.google.com/store/apps/details?id=com.backblaze.android**

## How to use:
* Set API id & key in settings
* Optional, if your api key is not restricted to a single bucket: insert bucket id
* Back in main screen, app will load files from the bucket's root

## What works:
* Browsing bucket
* Downloading file (By clicking on a file, it will be downloaded in Android's download folder, if allowed in settings)
* Back in main screen, app will load files from the bucket's root

## What could be improved/added:
* Better navigation
  * Breadcrumbs
  * Meaningful icons
  * Give user a way to reorder files
  * Look&Feel is pretty horrible by now
* More efficient fetching of file list. A balance has to be found between the number of requests and their size (Get all files metadata at once, or do it directory-by-directory as we browse?)
* Ability to download folders
* Upload capabilities (Files and folders to sync)



Screenshot or it didn't happen!
![Screenshot of main activity](https://i.imgur.com/RHOJ5Kz.png)
