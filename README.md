<p align="center"><img src="githubsrc/rfid.png" alt="RFID Tools" width="100" height="100"></p>

<h1 align="center">RFID Tools</h1>

<p>RRG Android App for use with the following devices</p>

|Proxmark3                  |
| ------------------------- |
| ![pm3](/githubsrc/rdv4.png "pm3") |

| NFC Reader                |
| ------------------------- |
| ![NFC Reader](/githubsrc/phone_nfc_icon.png "NFC Reader") |

| ACS ACR-122u              |
| ------------------------- |
| ![ACS ACR-122u](/githubsrc/acr122u.png "ACS ACR-122u") |

| Chameleon Mini            |
| ------------------------- |
| ![Chameleon Mini](/githubsrc/chameleon_rdv2.jpg "Chameleon Mini") |

| PN532                     |
| ------------------------- |
| ![pn532](/githubsrc/pn532core.png "pn532") |

The app runs on a non-rooted phone.

## APK download at Google playstore

You find the app on playstore  [RRG RFID Tools](https://play.google.com/store/apps/details?id=com.rfidresearchgroup.rfidtools&hl=en_US)

## Special firmware

To use your Proxmark3 RDV4 with blueshark via bluetooth with this app there is an extra step.
Until some android issues is resolved with the RRG/Iceman repo you must use a special android adapted firmware with your Proxmark3 RDV4.  

Download and flash this one
[Compiled FW 12 August](https://www.dropbox.com/s/416lsrqpr2lfeis/%5BCompiled%5DPM3-RRG-20190812.rar?dl=0)

## Where to buy these devices?

- www.sneaktechnology.com

## Developement tools

1. IDE: Android Studio 3.5
2. JRE: 1.8.0_202-release-1483-b03 amd64
3. JVM: OpenJDK 64-Bit Server VM by JetBrains s.r.o
4. OS: Windows 10 10.0
5. SDK: MIN 18 -> MAX 29
6. ANDROID SDK Tools: 26.1.1
7. CMAKE: 3.10
8. NDK: 20.0.5594570

## Core implementation

1. Communication implementation: using JNI mapping C & Java communication, encapsulating posix-compliant UART library, using Google API in the upper layer (Java) and posix-compliant UART in the lower layer (C/C+++).

2. Console program: Android defaults stdin, stdout, stderr to / dev / null, so the console program will not get normal output, we need to redirect them to the correct path, such as pointing to the file / sdcard / forward. stdxx, core: function freopen (), defined by C stdio. h, can redirect the standard stream. To local file.

3. Program architecture: DXL programming complies with MVP, the core architecture uses MVP to achieve layering, all the underlying data is placed in Model, all UI actions are placed in View, and all data calls are placed in Presenter. Module is used to realize resource management, reusable, and separate business can be placed in separate modules to reduce coupling and achieve reuse. Replacing concrete implementation with abstraction achieves the effects of extracting public implementation, abstracting private implementation, Interface-oriented and abstract programming, such as PN53X and mobile phone NFC read-write card, and information display.

## Maintainer

- DXL

## Compability

These Android phones has been tested with 
