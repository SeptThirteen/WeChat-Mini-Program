# 生成 FAQ 语音播报音频(WAV, 16kHz 16bit mono)
# 用法: powershell -NoProfile -ExecutionPolicy Bypass -File generate_faq_audio.ps1
# 依赖 Windows 自带 SAPI 语音(优先中文语音), 无需外部 API Key。
$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Speech

$audioDir = Join-Path $PSScriptRoot "wechat-elderly-service\src\main\resources\static\audio"
New-Item -ItemType Directory -Force -Path $audioDir | Out-Null

$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer

# 选择中文语音
$zhVoice = $synth.GetInstalledVoices() | Where-Object { $_.VoiceInfo.Culture.Name -like "zh*" } | Select-Object -First 1
if ($zhVoice) {
    $synth.SelectVoice($zhVoice.VoiceInfo.Name)
    Write-Output "使用语音: $($zhVoice.VoiceInfo.Name) ($($zhVoice.VoiceInfo.Culture.Name))"
} else {
    Write-Output "警告: 未找到中文语音, 使用默认语音"
}

$faqs = [ordered]@{
    "001" = "如何查询社保？您可以告诉我您的需求，我来帮您查询社保信息。也可以在首页点击公共服务办理，选择社保事项，按提示填写个人信息即可查询。如果操作有困难，随时叫我帮您一步一步操作。"
    "002" = "如何预约帮扶服务？在首页点击生活服务预约，选择您需要的服务，比如跑腿帮买、上门维修、贴心出行。填好地址和时间，点击立即预约就可以了。工作人员会尽快接单联系您。"
    "003" = "水电费怎么查询？在首页点击便民缴费查询，选择电费或水费，输入您的户号，点击立即查询，就能看到本月的账单金额和缴费截止日期。"
    "004" = "紧急联系人怎么设置？在我的页面点击紧急联系人，添加联系人的姓名和电话，可以添加多位家人。遇到紧急情况时，可以一键拨打他们的电话。"
    "005" = "订单怎么取消或评价？在我的订单里找到对应的订单。未接单的订单可以点击取消按钮。服务完成后，点击评价按钮，给您的工作人员打分并留言。"
    "006" = "政务代办如何申请？在首页点击公共服务办理，选择您要办理的事项，比如社保、医保或补贴申领，填好信息后提交，会有社区工作人员协助您办理。"
}

$format = New-Object System.Speech.AudioFormat.SpeechAudioFormatInfo(16000, [System.Speech.AudioFormat.AudioBitsPerSample]::Sixteen, [System.Speech.AudioFormat.AudioChannel]::Mono)
foreach ($id in $faqs.Keys) {
    $file = Join-Path $audioDir ("faq-" + $id + ".wav")
    $synth.SetOutputToWaveFile($file, $format)
    $synth.Speak($faqs[$id])
    $synth.SetOutputToNull()
    Write-Output "已生成: faq-$id.wav"
}

$synth.Dispose()
Write-Output "全部完成"
