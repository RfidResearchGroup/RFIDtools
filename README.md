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

|PN53X Derived|
|----------- |
| ![img_532] |

## Application features

- (non-rooted) it runs on a non-rooted phone.
- (multi device supported) app supports five device. More support in the future.
- (easy user interface) abstract UI action, implement tag operations like read & write with simple UI.

## Where to download app?

- Google playstore: [RRG RFID Tools][link_download_google]

- Github release: [Go][link_download_github]


## Updated note for app.

- [View note][link_updated_note]

## Proxmark3 firmware

To use your Proxmark3 RDV4 with blueshark via bluetooth with this app there is an extra step.
Until some android issues is resolved with the RRG/Iceman repo you must use a special android adapted firmware with your Proxmark3 RDV4.  

Download and flash [Compiled FW 12 August](https://www.dropbox.com/s/416lsrqpr2lfeis/%5BCompiled%5DPM3-RRG-20190812.rar?dl=0)

## Developement tools list

- IDE: Android Studio 3.5
- JRE: 1.8.0_202-release-1483-b03 amd64
- JVM: OpenJDK 64-Bit Server VM by JetBrains s.r.o
- OS: Windows 10 10.0
- SDK: MIN 18 -> MAX 29
- ANDROID SDK Tools: 26.1.1
- CMAKE: 3.10
- NDK: 20.0.5594570

## App core implementation

If you want to join our project, you must comply with the following development specifications to some extent, including macro architecture and micro implementation. Let us have fun time coding.

- Communication implementation: using JNI mapping C & Java communication, encapsulating posix-compliant UART library, using Google API in the upper layer (Java) and posix-compliant UART in the lower layer (C/C+++).

- Console program: Android defaults stdin, stdout, stderr to / dev / null, so the console program will not get normal output, we need to redirect them to the correct path, such as pointing to the file / sdcard / forward. stdxx, core: function freopen (), defined by C stdio. h, can redirect the standard stream. To local file.

- Program architecture: DXL programming complies with MVP, the core architecture uses MVP to achieve layering, all the underlying data is placed in Model, all UI actions are placed in View, and all data calls are placed in Presenter. Module is used to realize resource management, reusable, and separate business can be placed in separate modules to reduce coupling and achieve reuse. Replacing concrete implementation with abstraction achieves the effects of extracting public implementation, abstracting private implementation, Interface-oriented and abstract programming, such as PN53X and mobile phone NFC read-write card use same UI, and information display.

## Compability list

The app has been tested with these Android phones.  Feel free to contribute with your own findings.

- Redmi k20 pro (MIUI 10 & android 9)
- Redmi k20 (MIUI 10 & android 9)
- OnePlus 5T (H2OS 5.1.2 & Android 8.1.0)


## Where to buy these devices?

- www.sneaktechnology.com

## Support

Open issues here relating to the source code,  other support questions use `android@rfidresearchgroup.com`

## Maintainer
Feel free to contribute and make this app better!

- DXL

## Copyright
Copyright DXL 2019

## Open source license
GPL

