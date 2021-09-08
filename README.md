<!-- def -->
[img_pm3]: /githubsrc/rdv4x173.png
[img_nfc]: /githubsrc/phone_nfcx173.png
[img_122]: /githubsrc/acr122ux173.png
[img_cml]: /githubsrc/chameleonx173.png
[img_532]: /githubsrc/PN532x173.png
[link_download_google]: https://play.google.com/store/apps/details?id=com.rfidresearchgroup.rfidtools&hl=en_US
[link_download_github]: https://github.com/RfidResearchGroup/RFIDtools/releases
[link_updated_note]: https://github.com/xianglin1998/RFIDtools/blob/master/update_note.txt

<p align="center" background="#000000">
    <img align="center" src="/githubsrc/rfidx100.png" alt="RFID Tools" width="100" height="100">
</p>

<h1 align="center">RFID Tools android app</h1>

<br/>

<h4>:boom:Supported devices:dash:</h4>

The app support the following devices

|Proxmark3   |NFC Reader  |ACS ACR-122u  |Chameleon Mini  |PN532       |  
|----------- |----------- |------------- |--------------- |----------- |
| ![img_pm3] | ![img_nfc] |  ![img_122]  |   ![img_cml]   | ![img_532] |

|                                           PN53X Derived(Testing)                                      |
|-------------------------------------------------------------------------------------------------------|
| NXP_PN533    .   NXP_PN531    .   SONY_PN531    .   SCM_SCL3711    .   SCM_SCL3712    .   SONY_RCS360 |

## Application features

- (non-rooted) it runs on a non-rooted phone.
- (multi device supported) app supports five device. More support in the future.
- (easy user interface) abstract UI action, implement tag operations like read & write with simple UI.

## Where to download app?

- Google playstore: [RRG RFID Tools][link_download_google]

- Github release: [Go][link_download_github]


## Updated note for app.

- [View note][link_updated_note]

## Developement tools list

- IDE: Android Studio
- CMAKE: 3.10 + 
- NDK: 20.0.5594570 + 

## How to build?
So easy, first you can get the source from GitHub:

      git clone https://github.com/xianglin1998/RFIDtools RFIDtools
  
after git clone, open "RFIDtools" with AndroidStudio.

## App core implementation

Comunication: LocalSocket & LocalServerSocket(Android)
    Linux api: socket & abstract namespace
    
Build: gradle & cmake

Framework: MVP

Now all lib is compile to "libxxx.so",  jni and ndk is basic. core is jni and ndk from java to c map.  
~~not a linux executable file(Future)~~  
Now, proxmark3 client is a linux executable.

## It's second crazy reconstruction

It should take some time, because I'm the fighting alone
Before the reconstruction is completed, the whole project may have some big problems, such as build failure or crash at runtime.

- 1. Redesign the UI, simplify the page hierarchy and simplify the operation logic.
- 2. Discard the strong dependency of proxmark3 repo and use the external proxmark3 executable(and resources) instead. 
- 3. Discard the strong dependency of pn53x tools(mfcuk, mfoc...) and use the external executable(and resources) instead.
- 4. Optimize communication implementation and reduce communication delay.
- 5. Add support for some ble devices.

## Compability list

The app has been tested with these Android phones.  Feel free to contribute with your own findings.

- Redmi k20 pro (MIUI 10 & android 9)
- Redmi k20 (MIUI 10 & android 9)
- OnePlus 5T (H2OS 5.1.2 & Android 8.1.0)


## Where to buy these devices?

- www.sneaktechnology.com

## Support

Open issues here relating to the source code,  other support questions use `android@rfidresearchgroup.com`

## Dependents

Thanks:

- Terminal: [TERMUX](https://github.com/termux)
- Communication: [UsbSerial](https://github.com/felHR85/UsbSerial) 

## Maintainer
Feel free to contribute and make this app better!

- DXL

## Copyright
Copyright DXL 2019

## Open source license
GPL

