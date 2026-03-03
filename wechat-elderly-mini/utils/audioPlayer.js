/**
 * 音频播放工具 - 封装 uni.createInnerAudioContext
 *
 * 用法:
 *   import { playAudio, stopAudio, isPlaying } from '@/utils/audioPlayer'
 *   playAudio('https://xxx/faq-001.mp3')
 *   stopAudio()
 */

let audioCtx = null;
let playing = false;
let playEndCallback = null;

function getAudioContext() {
  if (!audioCtx) {
    audioCtx = uni.createInnerAudioContext();
    audioCtx.autoplay = false;

    audioCtx.onEnded(() => {
      playing = false;
      if (playEndCallback) playEndCallback();
    });

    audioCtx.onError((err) => {
      playing = false;
      console.error('[audioPlayer] 播放错误:', err);
    });

    audioCtx.onStop(() => {
      playing = false;
    });
  }
  return audioCtx;
}

/**
 * 播放音频
 * @param {String} src 音频URL或本地路径
 * @param {Function} onEnd 播放结束回调（可选）
 */
export function playAudio(src, onEnd) {
  const ctx = getAudioContext();
  if (playing) {
    ctx.stop();
  }
  playEndCallback = onEnd || null;
  ctx.src = src;
  ctx.play();
  playing = true;
}

/**
 * 停止播放
 */
export function stopAudio() {
  if (!audioCtx) return;
  audioCtx.stop();
  playing = false;
}

/**
 * 当前是否正在播放
 */
export function isPlaying() {
  return playing;
}

/**
 * 销毁播放器（页面卸载时调用）
 */
export function destroyAudio() {
  if (audioCtx) {
    audioCtx.destroy();
    audioCtx = null;
    playing = false;
  }
}
