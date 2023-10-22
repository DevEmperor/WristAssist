# WristAssist

#### WristAssist is the first app for all WearOS watches that fully brings the classic ChatGPT features to your wrist. Since a picture is worth a thousand words, here are some screenshots:

| ![menu.png](https://raw.githubusercontent.com/DevEmperor/WristAssist/master/img/readme/menu.png) | ![query.png](https://raw.githubusercontent.com/DevEmperor/WristAssist/master/img/readme/query.png) | ![answer.png](https://raw.githubusercontent.com/DevEmperor/WristAssist/master/img/readme/answer.png) | ![buttons.png](https://raw.githubusercontent.com/DevEmperor/WristAssist/master/img/readme/buttons.png) |
| :----------------------------------------------------------: | :----------------------------------------------------------: | :----------------------------------------------------------: | :----------------------------------------------------------: |
| ![saved_chats.png](https://raw.githubusercontent.com/DevEmperor/WristAssist/master/img/readme/saved_chats.png) | ![edit.png](https://raw.githubusercontent.com/DevEmperor/WristAssist/master/img/readme/edit.png) | ![settings.png](https://raw.githubusercontent.com/DevEmperor/WristAssist/master/img/readme/settings.png) | ![about.png](https://raw.githubusercontent.com/DevEmperor/WristAssist/master/img/readme/about.png) |

#### Or maybe a showcase video?
[![showcase cideo](https://raw.githubusercontent.com/DevEmperor/WristAssist/master/img/readme/player.png)](https://www.youtube.com/watch?v=goIoDTxPllc)
<br>

## Installation

I plan to release the app to the PlayStore as soon as possible. Until then, you can **download an APK** from the **[release page](https://github.com/DevEmperor/WristAssist/releases)**. The APK can be installed as follows:

1. **Enable developer options on your watch:** Open the watch's **Settings**. Tap **System > About**. Scroll to **Build number** and tap the build number seven times. A dialog appears confirming that you are now a developer.
2. **Enable Wi-Fi debugging:** Open the watch's **Settings**. Tap **Developer options > Debug over Wi-Fi**. After a moment, the screen displays the watch's IP address, such as `192.168.1.100`. You need this for the next step, so make a note of it.
3. **Connect the debugger to the watch:** Connect the debugger to the watch using the watch's IP address and a port  number. For example, if the IP address is `192.168.1.100` and the port number is `5555`, the  `adb connect` command look like this: `adb connect 192.168.1.100:5555`
4. **Install the ADB:** `adb install WristAssist_2.1.0.apk`

If this explanation was not enough, you can find a detailed explanation [here](https://www.guidingtech.com/how-to-install-apks-on-wear-os-smartwatches/).



## Usage

You will find a detailed explaination on how to set up and use WristAssist on the **[Wiki page](https://github.com/DevEmperor/WristAssist/wiki/Intro-on-how-to-set-up-and-use-WristAssist)** of this repository.



## Planned features

- [x] possibility to switch between GPT 3.5 and GPT 4 (as soon as access is possible)
- [x] intro explaining how to generate an API key
- [x] First time setup immediately opens the settings page
- [ ] typewriter effect for the answer
- [ ] Azure OpenAI API support
- [ ] PlayStore release



## License

WristAssist is under the terms of the [Apapche 2.0 license](https://www.apache.org/licenses/LICENSE-2.0), following all clarifications stated in the [license file](https://raw.githubusercontent.com/DevEmperor/WristAssist/master/LICENSE)