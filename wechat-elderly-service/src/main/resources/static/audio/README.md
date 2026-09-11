# FAQ 音频文件目录

此目录存放常见问题语音播报的 WAV 文件（已预生成，可直接使用）。

## 文件命名规则
- `faq-001.wav` — 如何查询社保？
- `faq-002.wav` — 如何预约帮扶服务？
- `faq-003.wav` — 水电费怎么查询？
- `faq-004.wav` — 紧急联系人怎么设置？
- `faq-005.wav` — 订单取消/评价怎么操作？
- `faq-006.wav` — 政务代办如何申请？

## 音频规格
- 采样率: 16kHz、16bit、单声道
- 格式: WAV（微信小程序 InnerAudioContext 与 H5 Audio 均支持）
- 语言: 普通话（Windows SAPI 中文语音预生成）

## 重新生成
预生成音频由 Windows 自带 TTS 生成，可随时替换为真人录音：
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File generate_faq_audio.ps1
```
脚本位于仓库根目录，修改脚本中的播报文案后重新运行即可。
