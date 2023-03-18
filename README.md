# ChatGPT (for WearOS)

#### ChatGPT for WearOS is the first app for all WearOS watches that fully brings the classic ChatGPT features to your wrist. Since a picture is worth a thousand words, here are some screenshots:

| ![menu.png](https://raw.githubusercontent.com/DevEmperor/ChatGPT-WearOS/master/img/menu.png) | ![query.png](https://raw.githubusercontent.com/DevEmperor/ChatGPT-WearOS/master/img/query.png) | ![answer.png](https://raw.githubusercontent.com/DevEmperor/ChatGPT-WearOS/master/img/answer.png) | ![buttons.png](https://raw.githubusercontent.com/DevEmperor/ChatGPT-WearOS/master/img/buttons.png) |
| :----------------------------------------------------------: | :--: | :--: | :--: |
| ![saved_chats.png](https://raw.githubusercontent.com/DevEmperor/ChatGPT-WearOS/master/img/saved_chats.png) | ![edit.png](https://raw.githubusercontent.com/DevEmperor/ChatGPT-WearOS/master/img/edit.png) | ![settings.png](https://raw.githubusercontent.com/DevEmperor/ChatGPT-WearOS/master/img/settings.png) | ![about.png](https://raw.githubusercontent.com/DevEmperor/ChatGPT-WearOS/master/img/about.png) |



## Installation

I plan to release the app to the PlayStore as soon as possible. Until then, you can **download an APK** from the **[release page](https://github.com/DevEmperor/ChatGPT-WearOS/releases)**. The APK can be installed as follows:

1. **Enable developer options on your watch:** Open the watch's **Settings**. Tap **System > About**. Scroll to **Build number** and tap the build number seven times. A dialog appears confirming that you are now a developer.
2. **Enable Wi-Fi debugging:** Open the watch's **Settings**. Tap **Developer options > Debug over Wi-Fi**. After a moment, the screen displays the watch's IP address, such as `192.168.1.100`. You need this for the next step, so make a note of it.
3. **Connect the debugger to the watch:** Connect the debugger to the watch using the watch's IP address and a port  number. For example, if the IP address is `192.168.1.100` and the port number is `5555`, the  `adb connect` command look like this: `adb connect 192.168.1.100:5555`
4. **Install the ADB:** `adb install ChatGPT_WearOS_1.0.apk`

If this explanation was not enough, you can find a detailed explanation [here](https://www.guidingtech.com/how-to-install-apks-on-wear-os-smartwatches/).



## Usage

The app is overall very self-explanatory, but there are a few things to keep in mind:

- Before the first use, an API key must be set in the settings. You can **get an API key for one month for free from [OpenAI](https://platform.openai.com/)**, after that you have to specify a credit card, because the usage costs a little money.
- After the first request in a chat, a Save-button appears, which saves the entire chat (including future ones).
- With a touch on a saved chat, a new interface appears to change the title or delete it.



## Planned features

- [x] each answer shows the cost
- [x] chats should be able to be saved
- [ ] show total consumption in settings
- [ ] possibility to switch between GPT 3.5 and GPT 4 (as soon as access is possible)
- [ ] intro explaining how to generate an API key
- [ ] PlayStore release



## License

ChatGPT for WearOS is under the terms of the [Apapche 2.0 license](https://www.apache.org/licenses/LICENSE-2.0), following all clarifications stated in the [license file](https://raw.githubusercontent.com/DevEmperor/ChatGPT-WearOS/master/LICENSE)