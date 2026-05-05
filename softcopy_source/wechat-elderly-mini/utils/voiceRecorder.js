/**
 * 语音录音工具 - 封装 uni.getRecorderManager
 *
 * 用法:
 *   import { startRecord, stopRecord, onRecordEnd } from '@/utils/voiceRecorder'
 *   onRecordEnd((filePath) => { ... })
 *   startRecord()
 *   // 用户说完后:
 *   stopRecord()
 */

let recorderManager = null;
let endCallback = null;
let errorCallback = null;
let isRecording = false;

function getRecorder() {
  if (!recorderManager) {
    recorderManager = uni.getRecorderManager();

    recorderManager.onStop((res) => {
      isRecording = false;
      if (endCallback && res.tempFilePath) {
        endCallback(res.tempFilePath, res.duration);
      }
    });

    recorderManager.onError((err) => {
      isRecording = false;
      console.error('[voiceRecorder] 录音错误:', err);
      if (errorCallback) {
        errorCallback(err);
      }
    });
  }
  return recorderManager;
}

/**
 * 注册录音结束回调
 * @param {Function} cb (filePath: string, duration: number) => void
 */
export function onRecordEnd(cb) {
  endCallback = cb;
}

/**
 * 注册录音错误回调
 * @param {Function} cb (err) => void
 */
export function onRecordError(cb) {
  errorCallback = cb;
}

/**
 * 开始录音
 * @param {Object} options 可选配置
 * @returns {Boolean} 是否成功开始
 */
export function startRecord(options = {}) {
  const recorder = getRecorder();
  if (isRecording) {
    console.warn('[voiceRecorder] 已在录音中');
    return false;
  }

  const config = {
    duration: options.duration || 60000,   // 最长60秒
    sampleRate: options.sampleRate || 16000,
    numberOfChannels: 1,
    encodeBitRate: 96000,
    format: options.format || 'wav',       // wav 兼容百度ASR
    ...options
  };

  isRecording = true;
  recorder.start(config);
  return true;
}

/**
 * 停止录音
 */
export function stopRecord() {
  if (!isRecording) return;
  const recorder = getRecorder();
  recorder.stop();
}

/**
 * 当前是否正在录音
 */
export function getIsRecording() {
  return isRecording;
}
